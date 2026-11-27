package com.zez.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.zez.app.service.CallSyncWorker;
import com.zez.app.service.QuackyBackgroundService;
import com.zez.app.util.AlarmHelper;
import com.zez.app.util.ChatNotificationManager;
import com.zez.app.util.ChatPreferencesHelper;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "ZezBootReceiver";
    public static final String SYNC_WORK_NAME = "ZezCallPeriodicSync";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            Log.d(TAG, "Device booted or app replaced. Registering background sync & call/chat listener.");
            Context appCtx = context.getApplicationContext();
            schedulePeriodicSync(appCtx);
            AlarmHelper.schedulePeriodicAlarmSync(appCtx);
            AlarmHelper.syncWithBackend(appCtx, null);
            if (ChatPreferencesHelper.isChatRegistered(appCtx)) {
                ChatNotificationManager.getInstance().start(appCtx);
                QuackyRestartReceiver.scheduleNextAlarm(appCtx);
            }
            QuackyBackgroundService.ensureRunning(appCtx);
        }
    }

    public static void schedulePeriodicSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // 15-minute periodic sync (standard Android minimum)
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                CallSyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );
        Log.d(TAG, "Scheduled 15-minute background call sync work");
    }
}
