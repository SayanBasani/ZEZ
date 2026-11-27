package com.zez.app.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.zez.app.receiver.QuackyRestartReceiver;
import com.zez.app.util.ChatNotificationManager;
import com.zez.app.util.ChatPreferencesHelper;

/**
 * Handles seamless transition away from persistent foreground notifications,
 * dismissing any legacy notification banners and keeping background delivery
 * active via AlarmManager wakeups.
 */
public class QuackyBackgroundService extends Service {
    private static final String TAG = "QuackyBgService";
    public static final String CHANNEL_ID = "quacky_bg_sync_channel";
    public static final int NOTIFICATION_ID = 3901;

    public static void ensureRunning(Context context) {
        if (context == null) return;
        Context appCtx = context.getApplicationContext();
        dismissPersistentNotification(appCtx);
        if (!ChatPreferencesHelper.isChatRegistered(appCtx)) {
            return;
        }
        // Keep in-process WebSocket and live poller active when app is running
        ChatNotificationManager.getInstance().start(appCtx);
        // Ensure closed-app background wakeups are scheduled
        QuackyRestartReceiver.scheduleNextAlarm(appCtx);
    }

    public static void dismissPersistentNotification(Context context) {
        if (context == null) return;
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.cancel(NOTIFICATION_ID);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    nm.deleteNotificationChannel(CHANNEL_ID);
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dismissPersistentNotification(this);
        try {
            stopForeground(true);
        } catch (Throwable ignored) {}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dismissPersistentNotification(this);
        try {
            stopForeground(true);
        } catch (Throwable ignored) {}
        Context appCtx = getApplicationContext();
        if (ChatPreferencesHelper.isChatRegistered(appCtx)) {
            ChatNotificationManager.getInstance().start(appCtx);
            QuackyRestartReceiver.scheduleNextAlarm(appCtx);
        }
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        QuackyRestartReceiver.scheduleNextAlarm(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        dismissPersistentNotification(this);
        super.onDestroy();
        if (ChatPreferencesHelper.isChatRegistered(getApplicationContext())) {
            QuackyRestartReceiver.scheduleNextAlarm(getApplicationContext());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
