package com.zez.app.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.zez.app.ChatActivity;
import com.zez.app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.TimeZone;

public class ChatNotificationHelper {
    private static final String TAG = "ChatNotificationHelper";
    public static final String CHANNEL_ID = "zez_chat_channel_v2";
    public static final String CHANNEL_NAME = "Zez Chat Messages";
    public static final String CALL_CHANNEL_ID = "zez_incoming_call_channel_v1";
    public static final String CALL_CHANNEL_NAME = "Incoming Calls";
    private static final int INCOMING_CALL_NOTIF_ID = 4911;
    private static volatile String lastHandledCallId = null;

    private static final String PREF_DEDUP = "zez_chat_notif_dedup";
    private static final String KEY_SEEN_IDS = "seen_msg_ids";
    private static final String KEY_LAST_TS_PREFIX = "last_ts_";
    private static final String KEY_LAST_KEY_PREFIX = "last_key_";
    private static final String KEY_LAST_TEXT_PREFIX = "last_text_";
    private static final String KEY_LAST_SOUND_MS_PREFIX = "last_sound_ms_";
    private static final int MAX_SEEN_IDS = 300;
    private static final Object DEDUP_LOCK = new Object();
    private static LinkedHashSet<String> sSeenMsgIds;

    public static void createNotificationChannel(Context context) {
        if (context == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            NotificationChannel existing = manager.getNotificationChannel(CHANNEL_ID);
            if (existing != null) return;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Direct incoming WhatsApp-style notifications for Zez Chat messages");
            channel.enableLights(true);
            channel.setLightColor(0xFF22C55E);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 250, 150, 250});
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                    .build();
            channel.setSound(defaultSound, audioAttributes);

            manager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created: " + CHANNEL_ID);
        }
    }

    public static void showChatNotification(Context context, String conversationId, String senderId, String senderName, String senderPhone, String messageText, String mediaType) {
        if (context == null) return;
        String fallbackTs = String.valueOf(System.currentTimeMillis() / 5000L);
        String syntheticKey = conversationId + "|" + (senderId != null ? senderId : "") + "|" + (messageText != null ? messageText : "") + "|" + (mediaType != null ? mediaType : "") + "|" + fallbackTs;
        showChatNotificationIfNew(context, conversationId, syntheticKey, senderId, senderName, senderPhone, messageText, mediaType, null);
    }

    /**
     * Thread-safe, persisted notification dispatcher that prevents duplicate notifications
     * and repeated notification sounds across realtime long-poll, periodic poll, and WorkManager sync.
     */
    public static boolean showChatNotificationIfNew(
            Context context,
            String conversationId,
            String messageId,
            String senderId,
            String senderName,
            String senderPhone,
            String messageText,
            String mediaType,
            String createdAt
    ) {
        if (context == null || conversationId == null) return false;

        if (ChatActivity.activeConversationId != null && ChatActivity.activeConversationId.equals(conversationId)) {
            return false;
        }
        try {
            if (ChatPreferencesHelper.isConversationMuted(context, conversationId)) {
                return false;
            }
            String myUserId = ChatPreferencesHelper.getUserId(context);
            if (myUserId != null && senderId != null && myUserId.equals(senderId)) {
                return false;
            }
        } catch (Exception ignored) {}

        Context appCtx = context.getApplicationContext();
        String cleanMsgId = (messageId != null && !messageId.trim().isEmpty()) ? messageId.trim() : null;
        long incomingTs = parseTimestampMillis(createdAt);
        String formattedPreview = formatNotificationContent(messageText, mediaType);
        String normalizedSig = (incomingTs > 0L ? String.valueOf(incomingTs / 1000L) : (createdAt != null ? createdAt : ""))
                + "|" + formattedPreview;
        boolean suppressSoundOnly = false;

        synchronized (DEDUP_LOCK) {
            SharedPreferences prefs = appCtx.getSharedPreferences(PREF_DEDUP, Context.MODE_PRIVATE);
            if (sSeenMsgIds == null) {
                sSeenMsgIds = new LinkedHashSet<>();
                String saved = prefs.getString(KEY_SEEN_IDS, "");
                if (saved != null && !saved.isEmpty()) {
                    String[] parts = saved.split(",");
                    for (String p : parts) {
                        if (p != null && !p.isEmpty()) sSeenMsgIds.add(p);
                    }
                }
            }

            if (cleanMsgId != null && sSeenMsgIds.contains(cleanMsgId)) {
                return false;
            }

            long prevTs = prefs.getLong(KEY_LAST_TS_PREFIX + conversationId, 0L);
            long lastSoundMs = prefs.getLong(KEY_LAST_SOUND_MS_PREFIX + conversationId, 0L);
            String prevText = prefs.getString(KEY_LAST_TEXT_PREFIX + conversationId, null);
            String prevSig = prefs.getString(KEY_LAST_KEY_PREFIX + conversationId, null);
            long nowMs = System.currentTimeMillis();

            if (cleanMsgId == null) {
                if (normalizedSig.equals(prevSig)) {
                    return false;
                }
                if (incomingTs > 0L && prevTs > 0L && incomingTs <= prevTs + 1500L) {
                    return false;
                }
                if (formattedPreview.equals(prevText) && (nowMs - lastSoundMs) < 120_000L) {
                    return false;
                }
            } else {
                if (incomingTs > 0L && prevTs > 0L && incomingTs < prevTs - 500L) {
                    // Already notified up to a newer message in this conversation
                    return false;
                }
            }

            // If we already sounded within the last 3 seconds for this conversation, update silently
            if ((nowMs - lastSoundMs) < 3000L) {
                suppressSoundOnly = true;
            }

            SharedPreferences.Editor editor = prefs.edit();
            if (cleanMsgId != null) {
                sSeenMsgIds.add(cleanMsgId);
                while (sSeenMsgIds.size() > MAX_SEEN_IDS) {
                    String first = sSeenMsgIds.iterator().next();
                    sSeenMsgIds.remove(first);
                }
                StringBuilder sb = new StringBuilder();
                for (String id : sSeenMsgIds) {
                    if (sb.length() > 0) sb.append(',');
                    sb.append(id);
                }
                editor.putString(KEY_SEEN_IDS, sb.toString());
            }
            // Always record normalized signature and preview text across BOTH msgId & summary paths
            editor.putString(KEY_LAST_KEY_PREFIX + conversationId, normalizedSig);
            editor.putString(KEY_LAST_TEXT_PREFIX + conversationId, formattedPreview);
            if (!suppressSoundOnly) {
                editor.putLong(KEY_LAST_SOUND_MS_PREFIX + conversationId, nowMs);
            }
            if (incomingTs > 0L && incomingTs >= prevTs) {
                editor.putLong(KEY_LAST_TS_PREFIX + conversationId, incomingTs);
            }
            editor.apply();
        }

        postNotificationNow(appCtx, conversationId, senderId, senderName, senderPhone, messageText, mediaType, suppressSoundOnly);
        return true;
    }

    private static String formatNotificationContent(String messageText, String mediaType) {
        String content = messageText;
        if (content != null) {
            String trimmed = content.trim();
            if (trimmed.startsWith("[game:tictactoe|")) {
                return "\uD83C\uDFAE Invited you to play Tic Tac Toe!";
            }
            if (trimmed.startsWith("[game:snakes_and_ladders|")) {
                return "\uD83C\uDFB2 Snakes & Ladders Challenge!";
            }
            if (trimmed.startsWith("[game_end:tictactoe|")) {
                return "\uD83C\uDFAE Tic Tac Toe match ended";
            }
            if (trimmed.startsWith("[game_end:snakes_and_ladders|")) {
                return "\uD83C\uDFC6 Snakes & Ladders match ended";
            }
            if (trimmed.startsWith("[contact|")) {
                String[] parts = trimmed.substring(9, trimmed.endsWith("]") ? trimmed.length() - 1 : trimmed.length()).split("\\|");
                String contactName = parts.length > 0 ? parts[0].trim() : "";
                return contactName.isEmpty() ? "\uD83D\uDC64 Shared a contact" : "\uD83D\uDC64 Contact: " + contactName;
            }
            if (trimmed.startsWith("> ") && trimmed.contains("\n\n")) {
                int idx = trimmed.indexOf("\n\n");
                content = trimmed.substring(idx + 2).trim();
            }
        }
        if (content == null || content.trim().isEmpty()) {
            if ("photo".equalsIgnoreCase(mediaType) || "image".equalsIgnoreCase(mediaType)) {
                return "\uD83D\uDCF7 Photo";
            } else if ("video".equalsIgnoreCase(mediaType)) {
                return "\uD83C\uDFA5 Video";
            } else if ("audio".equalsIgnoreCase(mediaType)) {
                return "\uD83C\uDFB5 Audio";
            } else {
                return "\uD83D\uDCCE Attachment";
            }
        }
        return content;
    }

    private static long parseTimestampMillis(String iso) {
        if (iso == null || iso.trim().isEmpty()) return 0L;
        String clean = iso.trim();
        try {
            return java.time.Instant.parse(clean).toEpochMilli();
        } catch (Exception ignored) {}
        try {
            return java.time.OffsetDateTime.parse(clean).toInstant().toEpochMilli();
        } catch (Exception ignored) {}
        // Normalize space separator / fractional seconds to exact 3 digits (ms) so SimpleDateFormat never treats microseconds as +ms
        try {
            String normalized = clean.replace(' ', 'T').replace("+00:00", "Z").replace("+00", "Z");
            if (!normalized.endsWith("Z")) {
                normalized = normalized + "Z";
            }
            int dot = normalized.indexOf('.');
            int z = normalized.lastIndexOf('Z');
            if (dot > 0 && z > dot) {
                String frac = normalized.substring(dot + 1, z);
                if (frac.length() > 3) {
                    frac = frac.substring(0, 3);
                } else {
                    while (frac.length() < 3) frac = frac + "0";
                }
                normalized = normalized.substring(0, dot + 1) + frac + "Z";
            }
            SimpleDateFormat sdf = new SimpleDateFormat(
                    normalized.contains(".") ? "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" : "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    Locale.US
            );
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = sdf.parse(normalized);
            if (d != null) return d.getTime();
        } catch (Exception ignored) {}
        return 0L;
    }

    private static void postNotificationNow(Context context, String conversationId, String senderId, String senderName, String senderPhone, String messageText, String mediaType, boolean suppressSound) {
        if (context == null) return;
        createNotificationChannel(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Cannot post notification: POST_NOTIFICATIONS permission not granted");
                return;
            }
        }

        String title = (senderName != null && !senderName.trim().isEmpty())
                ? senderName.trim()
                : (senderPhone != null && !senderPhone.trim().isEmpty() ? senderPhone : "New Message");

        String content = formatNotificationContent(messageText, mediaType);

        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("extra_conversation_id", conversationId);
        intent.putExtra("extra_other_user_id", senderId);
        intent.putExtra("extra_other_user_name", senderName);
        intent.putExtra("extra_other_user_phone", senderPhone);
        intent.putExtra("EXTRA_CONVERSATION_ID", conversationId);
        intent.putExtra("EXTRA_OTHER_USER_ID", senderId);
        intent.putExtra("EXTRA_CONTACT_NAME", senderName);
        intent.putExtra("EXTRA_CONTACT_PHONE", senderPhone);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = (conversationId != null) ? conversationId.hashCode() : (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        int notifId = (conversationId != null) ? Math.abs(conversationId.hashCode()) : 1001;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(suppressSound)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (!suppressSound) {
            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_VIBRATE)
                    .setSound(defaultSound)
                    .setVibrate(new long[]{0, 250, 150, 250});
        } else {
            builder.setSilent(true);
        }

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notifId, builder.build());
            Log.d(TAG, "Notification posted for conversation: " + conversationId + ", title: " + title);
        } catch (SecurityException se) {
            Log.w(TAG, "SecurityException posting notification: " + se.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error posting notification: " + e.getMessage(), e);
        }
    }

    public static void clearNotification(Context context, String conversationId) {
        if (context == null || conversationId == null) return;
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(Math.abs(conversationId.hashCode()));
        } catch (Exception ignored) {}
    }

    public static void createCallNotificationChannel(Context context) {
        if (context == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;
            NotificationChannel existing = manager.getNotificationChannel(CALL_CHANNEL_ID);
            if (existing != null) return;

            NotificationChannel channel = new NotificationChannel(
                    CALL_CHANNEL_ID,
                    CALL_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Full-screen and heads-up alerts for incoming Quacky voice and video calls");
            channel.enableLights(true);
            channel.setLightColor(0xFF22C55E);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 600, 600, 600, 600});
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (ringtoneUri == null) ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();
            channel.setSound(ringtoneUri, audioAttributes);
            manager.createNotificationChannel(channel);
        }
    }

    public static synchronized boolean markCallHandledIfNew(String callId) {
        if (callId == null || callId.isEmpty()) return false;
        if (callId.equals(lastHandledCallId)) return false;
        lastHandledCallId = callId;
        return true;
    }

    public static synchronized void clearHandledCall(String callId) {
        if (callId != null && callId.equals(lastHandledCallId)) {
            lastHandledCallId = null;
        }
    }

    public static void showIncomingCallNotification(Context context, ChatApiClient.IncomingCallInfo call) {
        if (context == null || call == null || call.id == null) return;
        if (ActiveCallManager.getInstance().isCallActive()) return;
        if (!markCallHandledIfNew(call.id)) return;

        Context appCtx = context.getApplicationContext();
        createCallNotificationChannel(appCtx);

        Intent incomingIntent = new Intent(appCtx, com.zez.app.IncomingCallActivity.class);
        incomingIntent.putExtra(com.zez.app.IncomingCallActivity.EXTRA_CALL_ID, call.id);
        incomingIntent.putExtra(com.zez.app.IncomingCallActivity.EXTRA_CALL_TYPE, call.callType);
        incomingIntent.putExtra(com.zez.app.IncomingCallActivity.EXTRA_CALLER_ID, call.callerId);
        incomingIntent.putExtra(com.zez.app.IncomingCallActivity.EXTRA_CALLER_NAME, call.callerName);
        incomingIntent.putExtra(com.zez.app.IncomingCallActivity.EXTRA_CALLER_AVATAR, call.callerAvatar);
        incomingIntent.putExtra(com.zez.app.IncomingCallActivity.EXTRA_CALLER_PHONE, call.callerPhone);
        incomingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int reqFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent fullScreenPi = PendingIntent.getActivity(appCtx, 5101, incomingIntent, reqFlags);

        Intent declineIntent = new Intent(appCtx, com.zez.app.receiver.IncomingCallActionReceiver.class);
        declineIntent.setAction(com.zez.app.receiver.IncomingCallActionReceiver.ACTION_DECLINE);
        declineIntent.putExtra(com.zez.app.receiver.IncomingCallActionReceiver.EXTRA_CALL_ID, call.id);
        PendingIntent declinePi = PendingIntent.getBroadcast(appCtx, 5102, declineIntent, reqFlags);

        Intent answerActivityIntent = new Intent(appCtx, com.zez.app.CallActivity.class);
        answerActivityIntent.putExtra(com.zez.app.CallActivity.EXTRA_CALL_ID, call.id);
        answerActivityIntent.putExtra(com.zez.app.CallActivity.EXTRA_IS_CALLER, false);
        answerActivityIntent.putExtra(com.zez.app.CallActivity.EXTRA_CALL_TYPE, call.callType);
        answerActivityIntent.putExtra(com.zez.app.CallActivity.EXTRA_PEER_ID, call.callerId);
        answerActivityIntent.putExtra(com.zez.app.CallActivity.EXTRA_PEER_NAME, call.callerName);
        answerActivityIntent.putExtra(com.zez.app.CallActivity.EXTRA_PEER_AVATAR, call.callerAvatar);
        answerActivityIntent.putExtra(com.zez.app.CallActivity.EXTRA_PEER_PHONE, call.callerPhone);
        answerActivityIntent.putExtra(com.zez.app.CallActivity.EXTRA_ACCEPT_CALL, true);
        answerActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent answerPi = PendingIntent.getActivity(appCtx, 5103, answerActivityIntent, reqFlags);

        boolean isVideo = "video".equalsIgnoreCase(call.callType);
        String callerTitle = (call.callerName != null && !call.callerName.trim().isEmpty()) ? call.callerName.trim() : "Quacky Contact";
        String callSub = isVideo ? "Incoming Quacky Video Call" : "Incoming Quacky Voice Call";

        boolean canPost = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ActivityCompat.checkSelfPermission(appCtx, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

        if (canPost) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(appCtx, CALL_CHANNEL_ID)
                    .setSmallIcon(isVideo ? R.drawable.ic_videocall : R.drawable.ic_call)
                    .setContentTitle(callerTitle)
                    .setContentText(callSub)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setContentIntent(fullScreenPi)
                    .setFullScreenIntent(fullScreenPi, true)
                    .addAction(R.drawable.ic_call_end, "Decline", declinePi)
                    .addAction(isVideo ? R.drawable.ic_videocall : R.drawable.ic_call, "Answer", answerPi);

            try {
                Notification callNotif = builder.build();
                callNotif.flags |= Notification.FLAG_INSISTENT;
                NotificationManagerCompat.from(appCtx).notify(INCOMING_CALL_NOTIF_ID, callNotif);
            } catch (SecurityException ignored) {}
        }

        // Also start IncomingCallActivity immediately when OS allows foreground/background activity launch
        try {
            appCtx.startActivity(incomingIntent);
        } catch (Exception e) {
            Log.d(TAG, "Direct startActivity deferred to fullScreenIntent notification: " + e.getMessage());
        }
    }

    public static void cancelIncomingCallNotification(Context context) {
        if (context == null) return;
        try {
            NotificationManagerCompat.from(context.getApplicationContext()).cancel(INCOMING_CALL_NOTIF_ID);
        } catch (Exception ignored) {}
    }
}
