package com.zez.app.service;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.google.gson.JsonObject;
import com.zez.app.util.ApiClient;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationCaptureService extends NotificationListenerService {
    private static final String TAG = "NotifCaptureService";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static boolean isPermissionGranted(Context context) {
        if (context == null) return false;
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        return packageNames != null && packageNames.contains(context.getPackageName());
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "Notification listener connected successfully.");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "Notification listener disconnected.");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        handleStatusBarNotification(sbn, "POSTED");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        handleStatusBarNotification(sbn, "REMOVED");
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
                Bundle extras = notification.extras;
                String title = null;
                String content = null;
                String subText = null;

                if (extras != null) {
                    CharSequence t = extras.getCharSequence(Notification.EXTRA_TITLE);
                    if (t != null) title = t.toString().trim();

                    CharSequence txt = extras.getCharSequence(Notification.EXTRA_TEXT);
                    if (txt != null) content = txt.toString().trim();

                    CharSequence bigTxt = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
                    if ((content == null || content.isEmpty()) && bigTxt != null) {
                        content = bigTxt.toString().trim();
                    }

                    CharSequence sub = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
                    if (sub != null) subText = sub.toString().trim();
                }

                // Skip completely empty system notification artifacts
                if ((title == null || title.isEmpty()) && (content == null || content.isEmpty())) {
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
                if (title != null) obj.addProperty("title", title);
                if (content != null) obj.addProperty("content", content);
                if (subText != null) obj.addProperty("subText", subText);
                obj.addProperty("postTime", postTime);

                Log.d(TAG, "[" + action + "] Syncing notification from " + appName + ": " + title);
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
