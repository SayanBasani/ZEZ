package com.zez.app.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.zez.app.service.CallSyncWorker;
import com.zez.app.service.QuackyBackgroundService;
import com.zez.app.util.ChatNotificationManager;
import com.zez.app.util.ChatPreferencesHelper;

/**
 * Periodically wakes the app in background to check for incoming audio/video calls
 * and unread chat messages without keeping an annoying persistent foreground notification.
 */
public class QuackyRestartReceiver extends BroadcastReceiver {
    private static final String TAG = "QuackyRestartReceiver";
    public static final String ACTION_ALARM_POLL = "com.zez.app.ACTION_ALARM_POLL";
    private static final long ALARM_INTERVAL_MS = 35_000L; // 35 seconds

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) return;
        final Context appCtx = context.getApplicationContext();
        QuackyBackgroundService.dismissPersistentNotification(appCtx);
        if (!ChatPreferencesHelper.isChatRegistered(appCtx)) return;

        final PendingResult asyncResult = goAsync();
        new Thread(() -> {
            try {
                // Check incoming calls and poll for messages
                ChatNotificationManager.getInstance().checkIncomingCallsNow(appCtx);
                ChatNotificationManager.getInstance().pollSync(appCtx);
            } catch (Throwable t) {
                Log.w(TAG, "Background alarm poll error: " + t.getMessage());
            } finally {
                scheduleNextAlarm(appCtx);
                try {
                    asyncResult.finish();
                } catch (Throwable ignored) {}
            }
        }).start();
    }

    public static void scheduleNextAlarm(Context context) {
        if (context == null) return;
        Context appCtx = context.getApplicationContext();
        if (!ChatPreferencesHelper.isChatRegistered(appCtx)) return;
        try {
            AlarmManager am = (AlarmManager) appCtx.getSystemService(Context.ALARM_SERVICE);
            if (am == null) return;
            Intent intent = new Intent(appCtx, QuackyRestartReceiver.class);
            intent.setAction(ACTION_ALARM_POLL);
            PendingIntent pi = PendingIntent.getBroadcast(
                    appCtx,
                    401,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            long triggerAt = SystemClock.elapsedRealtime() + ALARM_INTERVAL_MS;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi);
            } else {
                am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi);
            }
        } catch (Throwable t) {
            Log.w(TAG, "scheduleNextAlarm failed: " + t.getMessage());
        }
    }
}
