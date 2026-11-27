package com.zez.manager.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.zez.manager.model.CallLogItem;
import com.zez.manager.util.ApiClient;
import com.zez.manager.util.CallLogHelper;
import com.zez.manager.util.DeviceHelper;
import com.zez.manager.util.PreferencesHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallSyncWorker extends Worker {
    private static final String UNIQUE_IMMEDIATE_WORK = "ZezManagerImmediateSync";
    private static final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();

    public CallSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        if (!PreferencesHelper.isAutoSyncEnabled(ctx) || !PreferencesHelper.isCallsSyncEnabled(ctx)) {
            return Result.success();
        }
        if (PreferencesHelper.isWifiOnlySync(ctx) && !DeviceHelper.isWifiConnected(ctx)) {
            return Result.retry();
        }
        ApiClient.SyncResult res = performSync(ctx);
        return res.success ? Result.success() : Result.retry();
    }

    public static ApiClient.SyncResult performSync(Context context) {
        if (!PreferencesHelper.isAutoSyncEnabled(context) || !PreferencesHelper.isCallsSyncEnabled(context)) {
            return new ApiClient.SyncResult(true, 0, 0, 0, null);
        }
        if (!CallLogHelper.hasCallLogPermission(context)) {
            return new ApiClient.SyncResult(false, 0, 0, 0, "READ_CALL_LOG permission not granted");
        }
        if (PreferencesHelper.getDeviceToken(context) == null) {
            ApiClient.registerDeviceSync(context);
        }
        List<CallLogItem> calls = CallLogHelper.getAllCallLogs(context);
        return ApiClient.syncCalls(context, calls);
    }

    public static void syncNowAsync(Context context) {
        Context appCtx = context.getApplicationContext();
        asyncExecutor.execute(() -> {
            if (PreferencesHelper.isWifiOnlySync(appCtx) && !DeviceHelper.isWifiConnected(appCtx)) return;
            performSync(appCtx);
        });
    }

    public static void scheduleImmediateSync(Context context) {
        Context appCtx = context.getApplicationContext();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(PreferencesHelper.isWifiOnlySync(appCtx)
                        ? NetworkType.UNMETERED : NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(CallSyncWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(appCtx).enqueueUniqueWork(
                UNIQUE_IMMEDIATE_WORK,
                ExistingWorkPolicy.REPLACE,
                req
        );
    }
}
