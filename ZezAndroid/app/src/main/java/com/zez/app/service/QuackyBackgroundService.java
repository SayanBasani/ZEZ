package com.zez.app.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.zez.app.MainActivity;
import com.zez.app.R;
import com.zez.app.receiver.QuackyRestartReceiver;
import com.zez.app.util.ChatNotificationManager;
import com.zez.app.util.ChatPreferencesHelper;

/**
 * Sticky low-priority foreground service that keeps message and incoming call alerts
 * alive even when Activities are closed or swiped away.
 */
public class QuackyBackgroundService extends Service {
    private static final String TAG = "QuackyBgService";
    public static final String CHANNEL_ID = "quacky_bg_sync_channel";
    private static final int NOTIFICATION_ID = 3901;

    public static void ensureRunning(Context context) {
        if (context == null) return;
        Context appCtx = context.getApplicationContext();
        if (!ChatPreferencesHelper.isChatRegistered(appCtx)) {
            return;
        }
        ChatNotificationManager.getInstance().start(appCtx);

        Intent intent = new Intent(appCtx, QuackyBackgroundService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(appCtx, intent);
            } else {
                appCtx.startService(intent);
            }
        } catch (Throwable t) {
            // Android 12+/15+ background launch restriction fallback: keep in-process listener active
            Log.w(TAG, "Foreground service start deferred by OS: " + t.getMessage());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundSafe();
        ChatNotificationManager.getInstance().start(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundSafe();
        ChatNotificationManager.getInstance().start(getApplicationContext());
        return START_STICKY;
    }

    private void startForegroundSafe() {
        createNotificationChannel();
        Intent tapIntent = new Intent(this, MainActivity.class);
        tapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                this,
                0,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle("Quacky Connected")
                .setContentText("Ready for messages & calls")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .setSilent(true)
                .setShowWhen(false)
                .setContentIntent(pi)
                .build();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING);
            } else {
                startForeground(NOTIFICATION_ID, notif);
            }
        } catch (Throwable t) {
            Log.w(TAG, "startForeground fallback: " + t.getMessage());
            try {
                startForeground(NOTIFICATION_ID, notif);
            } catch (Throwable ignored) {}
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm == null) return;
            if (nm.getNotificationChannel(CHANNEL_ID) != null) return;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Quacky Connection",
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setDescription("Keeps Quacky ready to deliver incoming chats and calls");
            channel.setShowBadge(false);
            channel.setSound(null, null);
            channel.enableVibration(false);
            nm.createNotificationChannel(channel);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        scheduleRestart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ChatPreferencesHelper.isChatRegistered(getApplicationContext())) {
            scheduleRestart();
        }
    }

    private void scheduleRestart() {
        try {
            Context appCtx = getApplicationContext();
            if (!ChatPreferencesHelper.isChatRegistered(appCtx)) return;
            Intent restartIntent = new Intent(appCtx, QuackyRestartReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(
                    appCtx,
                    401,
                    restartIntent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
            );
            AlarmManager am = (AlarmManager) appCtx.getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 2000L, pi);
            }
        } catch (Exception e) {
            Log.w(TAG, "scheduleRestart failed: " + e.getMessage());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
