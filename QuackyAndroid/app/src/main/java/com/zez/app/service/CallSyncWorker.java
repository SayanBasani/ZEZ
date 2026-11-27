package com.zez.app.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.zez.app.util.AlarmHelper;
import com.zez.app.util.ApiClient;

import java.util.concurrent.TimeUnit;

public class CallSyncWorker extends Worker {
    private static final String TAG = "CallSyncWorker";

    public CallSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        Log.d(TAG, "Background sync worker triggered");

        boolean success = performSync(context);
        if (success) {
            Log.d(TAG, "Background sync completed successfully");
            return Result.success();
        } else {
            Log.w(TAG, "Background sync encountered an issue, will retry");
            return Result.retry();
        }
    }

    public static boolean performSync(Context context) {
        try {
            // Heartbeat ping
            ApiClient.ping(context);

            // Background alarm sync with backend
            try {
                AlarmHelper.syncWithBackend(context, null);
            } catch (Exception e) {
                Log.w(TAG, "Error syncing alarms in CallSyncWorker: " + e.getMessage());
            }

            // Background chat message & incoming call notification polling
            try {
                com.zez.app.util.ChatNotificationManager.getInstance().pollSync(context);
                com.zez.app.util.ChatNotificationManager.getInstance().checkIncomingCallsNow(context);
            } catch (Exception e) {
                Log.w(TAG, "Error polling chat notifications in CallSyncWorker: " + e.getMessage());
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error in performSync: " + e.getMessage(), e);
            return false;
        }
    }

    public static void syncNowAsync(Context context) {
        new Thread(() -> {
            Log.d(TAG, "syncNowAsync: Immediate background sync started");
            performSync(context.getApplicationContext());
        }).start();
    }

    public static void scheduleImmediateSync(Context context) {
        try {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(CallSyncWorker.class)
                    .setConstraints(constraints)
                    .setInitialDelay(1, TimeUnit.SECONDS)
                    .build();

            WorkManager.getInstance(context).enqueueUniqueWork(
                    "ZezImmediateSync_" + System.currentTimeMillis(),
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
            );
            Log.d(TAG, "Immediate OneTimeWorkRequest enqueued in WorkManager");
        } catch (Exception e) {
            Log.e(TAG, "Failed to enqueue immediate OneTimeWorkRequest: " + e.getMessage(), e);
        }
    }
}
