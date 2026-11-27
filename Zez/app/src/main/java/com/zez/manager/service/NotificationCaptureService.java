package com.zez.manager.service;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.zez.manager.model.AppCallLogItem;
import com.zez.manager.util.ApiClient;
import com.zez.manager.util.AppCallDetector;
import com.zez.manager.util.PreferencesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationCaptureService extends NotificationListenerService {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<String, Long> dedupCache = Collections.synchronizedMap(new HashMap<>());

    public static boolean isPermissionGranted(Context context) {
        if (context == null) return false;
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        return !TextUtils.isEmpty(flat) && flat.contains(context.getPackageName());
    }

    public static void ensureServiceBound(Context context) {
        if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        if (!isPermissionGranted(context)) return;
        try {
            requestRebind(new ComponentName(context, NotificationCaptureService.class));
        } catch (Exception ignored) {}
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        if (!PreferencesHelper.isNotifSyncEnabled(this) && !PreferencesHelper.isAppCallsSyncEnabled(this)) return;
        try {
            StatusBarNotification[] active = getActiveNotifications();
            if (active != null && active.length > 0) {
                executor.execute(() -> processBatch(active));
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;
        if (!PreferencesHelper.isNotifSyncEnabled(this) && !PreferencesHelper.isAppCallsSyncEnabled(this)) return;

        String pkg = sbn.getPackageName();
        if (pkg == null || pkg.equals(getPackageName())) return;
        if ("android".equals(pkg) || "com.android.systemui".equals(pkg)
                || "com.android.providers.downloads".equals(pkg)) {
            return;
        }

        executor.execute(() -> processSingle(sbn));
    }

    private void processBatch(StatusBarNotification[] list) {
        boolean canAppCalls = PreferencesHelper.isAppCallsSyncEnabled(this);
        boolean canNotifs = PreferencesHelper.isNotifSyncEnabled(this);
        List<JsonObject> notifs = new ArrayList<>();
        List<AppCallLogItem> calls = new ArrayList<>();
        for (StatusBarNotification sbn : list) {
            if (sbn == null || sbn.getPackageName() == null || sbn.getPackageName().equals(getPackageName())) continue;
            if (canAppCalls) {
                AppCallLogItem appCall = AppCallDetector.extractAppCall(this, sbn);
                if (appCall != null) calls.add(appCall);
            }
            if (canNotifs) {
                JsonObject n = buildNotificationJson(sbn);
                if (n != null) notifs.add(n);
            }
        }
        if (!calls.isEmpty()) ApiClient.syncAppCalls(this, calls);
        if (!notifs.isEmpty()) ApiClient.syncNotifications(this, notifs);
    }

    private void processSingle(StatusBarNotification sbn) {
        if (PreferencesHelper.isAppCallsSyncEnabled(this)) {
            AppCallLogItem appCall = AppCallDetector.extractAppCall(this, sbn);
            if (appCall != null) {
                List<AppCallLogItem> list = new ArrayList<>();
                list.add(appCall);
                ApiClient.syncAppCalls(this, list);
            }
        }
        if (PreferencesHelper.isNotifSyncEnabled(this)) {
            JsonObject n = buildNotificationJson(sbn);
            if (n != null) {
                List<JsonObject> list = new ArrayList<>();
                list.add(n);
                ApiClient.syncNotifications(this, list);
            }
        }
    }

    private JsonObject buildNotificationJson(StatusBarNotification sbn) {
        Notification notif = sbn.getNotification();
        if (notif == null) return null;
        if ((notif.flags & Notification.FLAG_ONGOING_EVENT) != 0) return null;

        Bundle extras = notif.extras;
        String title = "";
        String content = "";
        String subText = "";
        if (extras != null) {
            CharSequence t = extras.getCharSequence(Notification.EXTRA_TITLE);
            if (t == null) t = extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
            if (t == null) t = extras.getCharSequence("android.conversationTitle");
            title = t != null ? t.toString().trim() : "";

            CharSequence c = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
            if (c == null) c = extras.getCharSequence(Notification.EXTRA_TEXT);
            if (c != null) {
                content = c.toString().trim();
            } else {
                CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                if (lines != null && lines.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    int limit = Math.min(lines.length, 5);
                    for (int i = 0; i < limit; i++) {
                        if (lines[i] != null) {
                            if (sb.length() > 0) sb.append("\n");
                            sb.append(lines[i].toString().trim());
                        }
                    }
                    content = sb.toString();
                } else if (notif.tickerText != null) {
                    content = notif.tickerText.toString().trim();
                }
            }
            CharSequence sub = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
            if (sub == null) sub = extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
            subText = sub != null ? sub.toString().trim() : "";
        }
        if (title.isEmpty() && content.isEmpty()) return null;

        long postTime = sbn.getPostTime() > 0 ? sbn.getPostTime() : System.currentTimeMillis();
        String dedupKey = sbn.getPackageName() + "|" + title + "|" + content;
        Long prev = dedupCache.get(dedupKey);
        if (prev != null && (System.currentTimeMillis() - prev) < 10_000L) return null;
        if (dedupCache.size() > 200) dedupCache.clear();
        dedupCache.put(dedupKey, System.currentTimeMillis());

        String appName = sbn.getPackageName();
        try {
            PackageManager pm = getPackageManager();
            appName = pm.getApplicationLabel(pm.getApplicationInfo(sbn.getPackageName(), 0)).toString();
        } catch (Exception ignored) {}

        JsonObject obj = new JsonObject();
        obj.addProperty("packageName", sbn.getPackageName());
        obj.addProperty("appName", appName);
        obj.addProperty("title", title);
        obj.addProperty("content", content);
        obj.addProperty("subText", subText);
        obj.addProperty("postTime", postTime);
        return obj;
    }
}
