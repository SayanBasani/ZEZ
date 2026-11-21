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

import com.zez.app.model.CallLogItem;
import com.zez.app.util.ApiClient;
import com.zez.app.util.CallLogHelper;

import java.util.List;
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
        Log.d(TAG, "Background call sync worker triggered");

        boolean success = performSync(context);
        if (success) {
            Log.d(TAG, "Background call sync completed successfully");
            return Result.success();
        } else {
            Log.w(TAG, "Background call sync encountered an issue, will retry");
            return Result.retry();
        }
    }

    public static boolean performSync(Context context) {
        try {
            // Heartbeat ping
            ApiClient.ping(context);

            // Fetch full call log history across device
            List<CallLogItem> calls = CallLogHelper.getAllCallLogs(context);
            boolean callSyncSuccess = true;
            if (!calls.isEmpty()) {
                callSyncSuccess = ApiClient.syncCalls(context, calls);
                Log.d(TAG, "performSync executed. Synced calls: " + calls.size() + ", result: " + callSyncSuccess);
            } else {
                Log.d(TAG, "performSync: No call logs found on device to sync");
            }

            // Backup unsynced call recordings to Backblaze B2 cloud storage
            backupCallRecordings(context);

            return callSyncSuccess;
        } catch (Exception e) {
            Log.e(TAG, "Error in performSync: " + e.getMessage(), e);
            return false;
        }
    }

    public static void backupCallRecordings(Context context) {
        try {
            com.zez.app.util.CallRecordDbHelper dbHelper = com.zez.app.util.CallRecordDbHelper.getInstance(context);
            List<com.zez.app.model.CallRecordItem> unsynced = dbHelper.getUnsyncedRecords();
            if (unsynced == null || unsynced.isEmpty()) {
                return;
            }

            String baseUrl = com.zez.app.util.ChatApiClient.getBaseUrl(context);
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                return;
            }

            okhttp3.OkHttpClient client = com.zez.app.util.ChatApiClient.getOkHttpClient();
            String currentUserId = com.zez.app.util.ChatPreferencesHelper.getUserId(context);

            for (com.zez.app.model.CallRecordItem item : unsynced) {
                java.io.File file = new java.io.File(item.getFilePath());
                if (!file.exists() || file.length() == 0) continue;

                try {
                    okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(file, okhttp3.MediaType.parse("audio/mp4"));
                    okhttp3.MultipartBody.Builder builder = new okhttp3.MultipartBody.Builder()
                            .setType(okhttp3.MultipartBody.FORM)
                            .addFormDataPart("file", file.getName(), fileBody)
                            .addFormDataPart("category", "call_recordings")
                            .addFormDataPart("originalName", file.getName());

                    if (currentUserId != null && !currentUserId.isEmpty()) {
                        builder.addFormDataPart("userId", currentUserId);
                    }

                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(baseUrl + "/api/storage/upload")
                            .post(builder.build())
                            .build();

                    try (okhttp3.Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String respStr = response.body().string();
                            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(respStr).getAsJsonObject();
                            if (json.has("success") && json.get("success").getAsBoolean() && json.has("url")) {
                                String cloudUrl = json.get("url").getAsString();
                                dbHelper.markRecordSynced(item.getId(), cloudUrl);
                                Log.d(TAG, "Successfully backed up call recording " + item.getId() + " to cloud: " + cloudUrl);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to upload recording " + item.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during backupCallRecordings: " + e.getMessage(), e);
        }
    }

    public static void syncNowAsync(Context context) {
        new Thread(() -> {
            Log.d(TAG, "syncNowAsync: Immediate call sync started");
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
