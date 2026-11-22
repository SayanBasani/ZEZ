package com.zez.app.recording.importing;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.zez.app.service.CallSyncWorker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Background worker coordinating Storage Access Framework (SAF) folder reconciliation,
 * automatic import of newly finalized dialer recordings, and deduplication.
 */
public class RecordingSyncWorker extends Worker {
    private static final String TAG = "RecordingSyncWorker";
    private static final String UNIQUE_ONE_TIME_WORK = "twing_oem_recording_scan_immediate";
    private static final String UNIQUE_PERIODIC_WORK = "twing_oem_recording_scan_periodic";

    private static final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();

    public RecordingSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        Log.d(TAG, "RecordingSyncWorker executing folder scan...");

        try {
            int imported = OemRecordingImporter.getInstance(context).scanAndImportNewRecordings();
            if (imported > 0) {
                Log.d(TAG, "RecordingSyncWorker successfully imported " + imported + " new recordings. Triggering cloud backup.");
                CallSyncWorker.backupCallRecordings(context);
            }
            return Result.success();
        } catch (Exception e) {
            Log.w(TAG, "RecordingSyncWorker encountered exception: " + e.getMessage(), e);
            return Result.retry();
        }
    }

    /**
     * Executes an immediate asynchronous scan directly in a background thread for zero-latency UI updates.
     */
    public static void syncNowAsync(Context context) {
        if (context == null) return;
        final Context appCtx = context.getApplicationContext();
        asyncExecutor.execute(() -> {
            try {
                int imported = OemRecordingImporter.getInstance(appCtx).scanAndImportNewRecordings();
                if (imported > 0) {
                    CallSyncWorker.backupCallRecordings(appCtx);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error in syncNowAsync: " + e.getMessage());
            }
        });
    }

    /**
     * Enqueues an immediate WorkManager job to scan the native recording directory.
     */
    public static void triggerImmediateScan(Context context) {
        if (context == null) return;
        try {
            // Also run async pass immediately for instant UI
            syncNowAsync(context);

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(RecordingSyncWorker.class)
                    .setInitialDelay(2, TimeUnit.SECONDS) // Allow native dialer 2s to flush file to disk
                    .build();

            WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(
                    UNIQUE_ONE_TIME_WORK,
                    ExistingWorkPolicy.REPLACE,
                    request
            );
            Log.d(TAG, "Enqueued immediate RecordingSyncWorker");
        } catch (Exception e) {
            Log.w(TAG, "Failed enqueuing RecordingSyncWorker: " + e.getMessage());
        }
    }

    /**
     * Schedules periodic background reconciliation every 30 minutes.
     */
    public static void schedulePeriodicSync(Context context) {
        if (context == null) return;
        try {
            PeriodicWorkRequest periodicRequest = new PeriodicWorkRequest.Builder(
                    RecordingSyncWorker.class,
                    30, TimeUnit.MINUTES,
                    10, TimeUnit.MINUTES
            ).build();

            WorkManager.getInstance(context.getApplicationContext()).enqueueUniquePeriodicWork(
                    UNIQUE_PERIODIC_WORK,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicRequest
            );
            Log.d(TAG, "Scheduled periodic RecordingSyncWorker");
        } catch (Exception e) {
            Log.w(TAG, "Failed scheduling periodic RecordingSyncWorker: " + e.getMessage());
        }
    }
}
