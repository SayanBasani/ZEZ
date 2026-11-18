package com.zez.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AppUpdateHelper {
    private static final String TAG = "AppUpdateHelper";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public static class ReleaseInfo {
        public final boolean isUpdateAvailable;
        public final String currentVersionName;
        public final int currentVersionCode;
        public final String latestVersionName;
        public final int latestVersionCode;
        public final String releaseNotes;
        public final long fileSize;
        public final String downloadUrl;

        public ReleaseInfo(boolean isUpdateAvailable, String currentVersionName, int currentVersionCode,
                           String latestVersionName, int latestVersionCode, String releaseNotes,
                           long fileSize, String downloadUrl) {
            this.isUpdateAvailable = isUpdateAvailable;
            this.currentVersionName = currentVersionName;
            this.currentVersionCode = currentVersionCode;
            this.latestVersionName = latestVersionName;
            this.latestVersionCode = latestVersionCode;
            this.releaseNotes = releaseNotes;
            this.fileSize = fileSize;
            this.downloadUrl = downloadUrl;
        }
    }

    public interface UpdateCheckCallback {
        void onResult(ReleaseInfo info);
        void onError(String error);
    }

    public interface DownloadProgressCallback {
        void onProgress(int percent);
        void onComplete(File file);
        void onError(String error);
    }

    public static int getCurrentVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (int) pInfo.getLongVersionCode();
            } else {
                return pInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return 1;
        }
    }

    public static String getCurrentVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName != null ? pInfo.versionName : "1.0.0";
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0.0";
        }
    }

    public static void checkForUpdate(Context context, UpdateCheckCallback callback) {
        new Thread(() -> {
            String baseUrl = PreferencesHelper.getApiUrl(context);
            String checkUrl = baseUrl + "/api/app/latest";
            Log.d(TAG, "Checking update at: " + checkUrl);

            Request request = new Request.Builder()
                    .url(checkUrl)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Server returned status " + response.code());
                    return;
                }

                String resStr = response.body().string();
                JsonObject json = JsonParser.parseString(resStr).getAsJsonObject();

                if (!json.has("success") || !json.get("success").getAsBoolean()) {
                    callback.onError(json.has("error") ? json.get("error").getAsString() : "Check update failed");
                    return;
                }

                boolean hasRelease = json.has("hasRelease") && json.get("hasRelease").getAsBoolean();
                String currName = getCurrentVersionName(context);
                int currCode = getCurrentVersionCode(context);

                if (!hasRelease) {
                    callback.onResult(new ReleaseInfo(false, currName, currCode, currName, currCode, null, 0, null));
                    return;
                }

                String latestName = json.get("versionName").getAsString();
                int latestCode = json.get("versionCode").getAsInt();
                String notes = json.has("releaseNotes") && !json.get("releaseNotes").isJsonNull()
                        ? json.get("releaseNotes").getAsString()
                        : "Bug fixes and improvements";
                long size = json.has("fileSize") ? json.get("fileSize").getAsLong() : 0;
                String dlUrl = json.get("downloadUrl").getAsString();

                // If downloadUrl is relative, prepend base URL
                if (dlUrl.startsWith("/")) {
                    dlUrl = baseUrl + dlUrl;
                }

                boolean updateAvailable = latestCode > currCode;
                callback.onResult(new ReleaseInfo(updateAvailable, currName, currCode, latestName, latestCode, notes, size, dlUrl));

            } catch (Exception e) {
                Log.e(TAG, "Update check failed: " + e.getMessage());
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public static void downloadAndInstall(Activity activity, ReleaseInfo release, DownloadProgressCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(release.downloadUrl)
                        .get()
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        callback.onError("Failed to download APK: HTTP " + response.code());
                        return;
                    }

                    ResponseBody body = response.body();
                    long contentLength = body.contentLength();
                    File destDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    if (destDir == null) destDir = activity.getCacheDir();

                    File destFile = new File(destDir, "zez-app-update.apk");
                    if (destFile.exists()) {
                        destFile.delete();
                    }

                    try (InputStream is = body.byteStream();
                         FileOutputStream fos = new FileOutputStream(destFile)) {

                        byte[] buffer = new byte[8192];
                        long totalRead = 0;
                        int bytesRead;

                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            totalRead += bytesRead;

                            if (contentLength > 0) {
                                int percent = (int) ((totalRead * 100) / contentLength);
                                callback.onProgress(percent);
                            }
                        }

                        fos.flush();
                    }

                    callback.onComplete(destFile);

                    // Launch Package Installer
                    activity.runOnUiThread(() -> launchInstaller(activity, destFile));
                }
            } catch (Exception e) {
                Log.e(TAG, "Download error: " + e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public static void launchInstaller(Activity activity, File apkFile) {
        try {
            Uri apkUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".provider",
                    apkFile
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            activity.startActivity(intent);
            Log.d(TAG, "Package installer initiated successfully for " + apkFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch package installer: " + e.getMessage(), e);
        }
    }
}
