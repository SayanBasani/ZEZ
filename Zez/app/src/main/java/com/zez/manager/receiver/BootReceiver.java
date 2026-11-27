package com.zez.manager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.zez.manager.service.CallSyncWorker;
import com.zez.manager.service.NotificationCaptureService;
import com.zez.manager.util.PreferencesHelper;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {
    private static final String PERIODIC_SYNC_WORK = "ZezManagerPeriodicSync";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) return;
        Context appCtx = context.getApplicationContext();
        schedulePeriodicSync(appCtx);
        if (PreferencesHelper.isAutoSyncEnabled(appCtx)) {
            CallSyncWorker.scheduleImmediateSync(appCtx);
        }
        NotificationCaptureService.ensureServiceBound(appCtx);
        com.zez.manager.alarm.util.AlarmHelper.rescheduleAllAlarms(appCtx);
        com.zez.manager.alarm.util.AlarmHelper.schedulePeriodicAlarmSync(appCtx);
    }

    public static void schedulePeriodicSync(Context context) {
        Context appCtx = context.getApplicationContext();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(PreferencesHelper.isWifiOnlySync(appCtx)
                        ? NetworkType.UNMETERED : NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                CallSyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(appCtx).enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                req
        );
    }
}
