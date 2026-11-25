package com.zez.app.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AppUpdateHelper {
    private static final String TAG = "AppUpdateHelper";

    private static volatile File pendingInstallApkFile;

    private static final Dns IPV4_FIRST_DNS = new Dns() {
        @NonNull
        @Override
        public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
            List<InetAddress> addresses = Dns.SYSTEM.lookup(hostname);
            List<InetAddress> ipv4 = new ArrayList<>();
            List<InetAddress> others = new ArrayList<>();
            for (InetAddress addr : addresses) {
                if (addr instanceof Inet4Address) {
                    ipv4.add(addr);
                } else {
                    others.add(addr);
                }
            }
            if (!ipv4.isEmpty()) {
                ipv4.addAll(others);
                return ipv4;
            }
            return addresses;
        }
    };

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .dns(IPV4_FIRST_DNS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
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

    private static String normalizeBaseUrl(Context context) {
        String baseUrl = PreferencesHelper.getApiUrl(context);
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = PreferencesHelper.DEFAULT_API_URL;
        }
        baseUrl = baseUrl.trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    public static void checkForUpdate(Context context, UpdateCheckCallback callback) {
        new Thread(() -> {
            String baseUrl = normalizeBaseUrl(context);
            String checkUrl = baseUrl + "/api/app/latest";
            Log.d(TAG, "Checking update at: " + checkUrl);

            Request request = new Request.Builder()
                    .url(checkUrl)
                    .header("Cache-Control", "no-cache")
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
                long size = json.has("fileSize") && !json.get("fileSize").isJsonNull() ? json.get("fileSize").getAsLong() : 0;
                String dlUrl = json.get("downloadUrl").getAsString();

                if (dlUrl.startsWith("/")) {
                    dlUrl = baseUrl + dlUrl;
                }

                boolean updateAvailable = latestCode > currCode;
                callback.onResult(new ReleaseInfo(updateAvailable, currName, currCode, latestName, latestCode, notes, size, dlUrl));

            } catch (Exception e) {
                Log.e(TAG, "Update check failed: " + e.getMessage());
                callback.onError(e.getMessage() != null ? e.getMessage() : "Unable to check updates");
            }
        }).start();
    }

    public static void downloadAndInstall(Activity activity, ReleaseInfo release, DownloadProgressCallback callback) {
        new Thread(() -> {
            File tmpFile = null;
            try {
                if (release == null || release.downloadUrl == null || release.downloadUrl.trim().isEmpty()) {
                    callback.onError("Invalid update download URL.");
                    return;
                }

                Request request = new Request.Builder()
                        .url(release.downloadUrl)
                        .header("Cache-Control", "no-cache")
                        .get()
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        callback.onError("Failed to download APK: HTTP " + response.code());
                        return;
                    }

                    ResponseBody body = response.body();
                    long contentLength = body.contentLength();
                    long expectedLength = contentLength > 0 ? contentLength : release.fileSize;

                    File destDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    if (destDir == null) {
                        destDir = activity.getCacheDir();
                    }
                    if (!destDir.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        destDir.mkdirs();
                    }

                    tmpFile = new File(destDir, "zez-app-update.apk.tmp");
                    File destFile = new File(destDir, "zez-app-update.apk");
                    if (tmpFile.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        tmpFile.delete();
                    }
                    if (destFile.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        destFile.delete();
                    }

                    long totalRead = 0;
                    int lastPercent = -1;
                    try (InputStream is = body.byteStream();
                         FileOutputStream fos = new FileOutputStream(tmpFile)) {

                        byte[] buffer = new byte[16384];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            totalRead += bytesRead;

                            if (expectedLength > 0) {
                                int percent = (int) Math.min(100, (totalRead * 100) / expectedLength);
                                if (percent != lastPercent) {
                                    lastPercent = percent;
                                    callback.onProgress(percent);
                                }
                            }
                        }
                        fos.flush();
                    }

                    if (totalRead <= 0) {
                        throw new IllegalStateException("Downloaded APK file is empty.");
                    }
                    if (expectedLength > 0 && totalRead != expectedLength) {
                        throw new IllegalStateException(
                                "Download incomplete (" + totalRead + " of " + expectedLength + " bytes). Please try again.");
                    }
                    if (!isValidApkZipHeader(tmpFile)) {
                        throw new IllegalStateException("Downloaded file is not a valid APK package.");
                    }

                    if (!tmpFile.renameTo(destFile)) {
                        try (InputStream in = new FileInputStream(tmpFile);
                             FileOutputStream out = new FileOutputStream(destFile)) {
                            byte[] buf = new byte[16384];
                            int len;
                            while ((len = in.read(buf)) != -1) {
                                out.write(buf, 0, len);
                            }
                            out.flush();
                        }
                        //noinspection ResultOfMethodCallIgnored
                        tmpFile.delete();
                    }

                    callback.onProgress(100);
                    callback.onComplete(destFile);

                    activity.runOnUiThread(() -> launchInstaller(activity, destFile));
                }
            } catch (Exception e) {
                if (tmpFile != null && tmpFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    tmpFile.delete();
                }
                Log.e(TAG, "Download error: " + e.getMessage(), e);
                callback.onError(e.getMessage() != null ? e.getMessage() : "Download failed");
            }
        }).start();
    }

    private static boolean isValidApkZipHeader(File file) {
        if (file == null || !file.exists() || file.length() < 4) {
            return false;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] header = new byte[4];
            if (fis.read(header) < 2) {
                return false;
            }
            return header[0] == 'P' && header[1] == 'K';
        } catch (Exception e) {
            return false;
        }
    }

    public static void resumePendingInstallIfPermitted(Activity activity) {
        File pending = pendingInstallApkFile;
        if (pending == null || activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        if (!pending.exists()) {
            pendingInstallApkFile = null;
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || activity.getPackageManager().canRequestPackageInstalls()) {
            pendingInstallApkFile = null;
            launchInstaller(activity, pending);
        }
    }

    public static void launchInstaller(Activity activity, File apkFile) {
        try {
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            if (apkFile == null || !apkFile.exists()) {
                Log.e(TAG, "APK file does not exist");
                Toast.makeText(activity, "Downloaded update file was not found.", Toast.LENGTH_LONG).show();
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!activity.getPackageManager().canRequestPackageInstalls()) {
                    pendingInstallApkFile = apkFile;
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                            .setTitle("Install Permission Needed")
                            .setMessage("To install this update, please allow 'Install unknown apps' for Quacky. The installer will open automatically when you return.")
                            .setPositiveButton("Allow in Settings", (dialog, which) -> {
                                try {
                                    Intent permIntent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                                    permIntent.setData(Uri.parse("package:" + activity.getPackageName()));
                                    activity.startActivity(permIntent);
                                } catch (Exception e) {
                                    try {
                                        Intent secIntent = new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS);
                                        activity.startActivity(secIntent);
                                    } catch (Exception ignored) {
                                        Toast.makeText(activity, "Please enable 'Install unknown apps' in Android Settings.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> pendingInstallApkFile = null)
                            .show();
                    return;
                }
            }

            pendingInstallApkFile = null;
            //noinspection ResultOfMethodCallIgnored
            apkFile.setReadable(true, false);
            Uri apkUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".provider",
                    apkFile
            );

            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            try {
                activity.startActivity(viewIntent);
                Log.d(TAG, "Package installer initiated successfully for " + apkFile.getAbsolutePath());
            } catch (ActivityNotFoundException | SecurityException firstErr) {
                Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                installIntent.setData(apkUri);
                installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                installIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                activity.startActivity(installIntent);
                Log.d(TAG, "Package installer fallback initiated for " + apkFile.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch package installer: " + e.getMessage(), e);
            if (activity != null && !activity.isFinishing()) {
                Toast.makeText(activity, "Could not open app installer: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
