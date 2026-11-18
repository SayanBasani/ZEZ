package com.zez.app.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.zez.app.ChatActivity;
import com.zez.app.R;

public class ChatNotificationHelper {
    public static final String CHANNEL_ID = "zez_chat_channel";
    public static final String CHANNEL_NAME = "Zez Chat Messages";

    public static void createNotificationChannel(Context context) {
        if (context == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Direct incoming notifications for Zez Chat messages");
            channel.enableLights(true);
            channel.enableVibration(true);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showChatNotification(Context context, String conversationId, String senderId, String senderName, String senderPhone, String messageText, String mediaType) {
        if (context == null) return;
        createNotificationChannel(context);

        String title = (senderName != null && !senderName.trim().isEmpty())
                ? senderName.trim()
                : (senderPhone != null ? senderPhone : "New Message");

        String content = messageText;
        if (content == null || content.trim().isEmpty()) {
            if ("photo".equalsIgnoreCase(mediaType) || "image".equalsIgnoreCase(mediaType)) {
                content = "📷 Photo";
            } else if ("video".equalsIgnoreCase(mediaType)) {
                content = "🎥 Video";
            } else if ("audio".equalsIgnoreCase(mediaType)) {
                content = "🎵 Audio";
            } else {
                content = "📎 Attachment";
            }
        }

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

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(defaultSound)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        int notifId = (conversationId != null) ? Math.abs(conversationId.hashCode()) : 1001;

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notifId, builder.build());
        } catch (SecurityException ignored) {
            // Android 13+ POST_NOTIFICATIONS runtime permission pending
        } catch (Exception ignored) {}
    }

    public static void clearNotification(Context context, String conversationId) {
        if (context == null || conversationId == null) return;
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(Math.abs(conversationId.hashCode()));
        } catch (Exception ignored) {}
    }
}
