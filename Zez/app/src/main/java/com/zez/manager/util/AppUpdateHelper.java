package com.zez.manager.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Isolated OTA release checker & installer for Zez ('appId=zez').
 * Validates appId, SHA-256 checksum, package name, and signing certificate compatibility.
 */
public class AppUpdateHelper {
    private static final String TAG = "ZezAppUpdate";
    public static final String APP_ID = "zez";

    private static final AtomicBoolean cancelRequested = new AtomicBoolean(false);

    private static final Dns IPV4_FIRST_DNS = new Dns() {
        @NonNull
        @Override
        public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
            List<InetAddress> all = Dns.SYSTEM.lookup(hostname);
            List<InetAddress> v4 = new ArrayList<>();
            List<InetAddress> others = new ArrayList<>();
            for (InetAddress a : all) {
                if (a instanceof Inet4Address) v4.add(a);
                else others.add(a);
            }
            if (!v4.isEmpty()) {
                v4.addAll(others);
                return v4;
            }
            return all;
        }
    };

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .dns(IPV4_FIRST_DNS)
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .build();

    public static class ReleaseInfo {
        public final boolean isUpdateAvailable;
        public final boolean forceUpdate;
        public final int minimumSupportedVersion;
        public final String currentVersionName;
        public final int currentVersionCode;
        public final String latestVersionName;
        public final int latestVersionCode;
        public final String releaseNotes;
        public final long fileSize;
        public final String sha256Checksum;
        public final String downloadUrl;

        public ReleaseInfo(boolean isUpdateAvailable, boolean forceUpdate, int minimumSupportedVersion,
                           String currentVersionName, int currentVersionCode,
                           String latestVersionName, int latestVersionCode,
                           String releaseNotes, long fileSize, String sha256Checksum, String downloadUrl) {
            this.isUpdateAvailable = isUpdateAvailable;
            this.forceUpdate = forceUpdate;
            this.minimumSupportedVersion = minimumSupportedVersion;
            this.currentVersionName = currentVersionName;
            this.currentVersionCode = currentVersionCode;
            this.latestVersionName = latestVersionName;
            this.latestVersionCode = latestVersionCode;
            this.releaseNotes = releaseNotes;
            this.fileSize = fileSize;
            this.sha256Checksum = sha256Checksum;
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

    public static String getCurrentVersionName(android.content.Context context) {
        return DeviceHelper.getAppVersionName(context);
    }

    public static int getCurrentVersionCode(android.content.Context context) {
        return DeviceHelper.getAppVersionCode(context);
    }

    public static void cancelDownload() {
        cancelRequested.set(true);
    }

    public static void checkForUpdate(Activity activity, UpdateCheckCallback callback) {
        Handler main = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            String baseUrl = PreferencesHelper.getApiUrl(activity);
            String currName = DeviceHelper.getAppVersionName(activity);
            int currCode = DeviceHelper.getAppVersionCode(activity);
            String url = baseUrl + "/api/app/latest?appId=" + APP_ID + "&currentVersionCode=" + currCode;

            Request req = new Request.Builder()
                    .url(url)
                    .header("Cache-Control", "no-cache")
                    .get()
                    .build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    main.post(() -> callback.onError("Server returned HTTP " + resp.code()));
                    return;
                }
                JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
                if (!json.has("success") || !json.get("success").getAsBoolean()) {
                    String err = json.has("error") ? json.get("error").getAsString() : "Update check failed";
                    main.post(() -> callback.onError(err));
                    return;
                }
                if (json.has("appId") && !APP_ID.equalsIgnoreCase(json.get("appId").getAsString())) {
                    main.post(() -> callback.onError("Server returned mismatched app channel."));
                    return;
                }
                boolean hasRelease = json.has("hasRelease") && json.get("hasRelease").getAsBoolean();
                if (!hasRelease) {
                    main.post(() -> callback.onResult(new ReleaseInfo(false, false, 1, currName, currCode, currName, currCode, null, 0, null, null)));
                    return;
                }
                String latestName = json.get("versionName").getAsString();
                int latestCode = json.get("versionCode").getAsInt();
                int minCode = json.has("minimumSupportedVersion") ? json.get("minimumSupportedVersion").getAsInt() : 1;
                boolean force = (json.has("forceUpdate") && json.get("forceUpdate").getAsBoolean()) || currCode < minCode;
                String notes = json.has("releaseNotes") && !json.get("releaseNotes").isJsonNull() ? json.get("releaseNotes").getAsString() : "";
                long size = json.has("fileSize") && !json.get("fileSize").isJsonNull() ? json.get("fileSize").getAsLong() : 0L;
                String sha = json.has("sha256Checksum") && !json.get("sha256Checksum").isJsonNull() ? json.get("sha256Checksum").getAsString() : null;
                String dlUrl = json.has("directDownloadUrl") && !json.get("directDownloadUrl").isJsonNull()
                        ? json.get("directDownloadUrl").getAsString()
                        : json.get("downloadUrl").getAsString();
                if (dlUrl.startsWith("/")) dlUrl = baseUrl + dlUrl;

                final String finalUrl = dlUrl;
                boolean available = latestCode > currCode;
                main.post(() -> callback.onResult(new ReleaseInfo(available, force, minCode, currName, currCode, latestName, latestCode, notes, size, sha, finalUrl)));
            } catch (Exception e) {
                main.post(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "Unable to check updates"));
            }
        }).start();
    }

    public static void checkForRollback(Activity activity, UpdateCheckCallback callback) {
        Handler main = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            String baseUrl = PreferencesHelper.getApiUrl(activity);
            String currName = DeviceHelper.getAppVersionName(activity);
            int currCode = DeviceHelper.getAppVersionCode(activity);
            String url = baseUrl + "/api/app/rollback?appId=" + APP_ID + "&currentVersionCode=" + currCode;

            Request req = new Request.Builder()
                    .url(url)
                    .header("Cache-Control", "no-cache")
                    .get()
                    .build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    main.post(() -> callback.onError("Server returned HTTP " + resp.code()));
                    return;
                }
                JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
                if (!json.has("success") || !json.get("success").getAsBoolean()) {
                    String err = json.has("error") ? json.get("error").getAsString() : "Rollback check failed";
                    main.post(() -> callback.onError(err));
                    return;
                }
                boolean available = json.has("rollbackAvailable") && json.get("rollbackAvailable").getAsBoolean();
                if (!available) {
                    main.post(() -> callback.onResult(new ReleaseInfo(false, false, 1, currName, currCode, currName, currCode, null, 0, null, null)));
                    return;
                }
                String prevName = json.get("versionName").getAsString();
                int prevCode = json.get("versionCode").getAsInt();
                String notes = json.has("releaseNotes") && !json.get("releaseNotes").isJsonNull() ? json.get("releaseNotes").getAsString() : "Rollback version";
                long size = json.has("fileSize") && !json.get("fileSize").isJsonNull() ? json.get("fileSize").getAsLong() : 0L;
                String sha = json.has("sha256Checksum") && !json.get("sha256Checksum").isJsonNull() ? json.get("sha256Checksum").getAsString() : null;
                String dlUrl = json.has("directDownloadUrl") && !json.get("directDownloadUrl").isJsonNull()
                        ? json.get("directDownloadUrl").getAsString()
                        : json.get("downloadUrl").getAsString();
                if (dlUrl.startsWith("/")) dlUrl = baseUrl + dlUrl;

                final String finalUrl = dlUrl;
                main.post(() -> callback.onResult(new ReleaseInfo(true, false, 1, currName, currCode, prevName, prevCode, notes, size, sha, finalUrl)));
            } catch (Exception e) {
                main.post(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "Unable to check rollback"));
            }
        }).start();
    }

    public static void checkForCompanionApp(Activity activity, String companionAppId, UpdateCheckCallback callback) {
        Handler main = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            String baseUrl = PreferencesHelper.getApiUrl(activity);
            String url = baseUrl + "/api/app/latest?appId=" + companionAppId;

            Request req = new Request.Builder()
                    .url(url)
                    .header("Cache-Control", "no-cache")
                    .get()
                    .build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    main.post(() -> callback.onError("Server returned HTTP " + resp.code()));
                    return;
                }
                JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
                if (!json.has("success") || !json.get("success").getAsBoolean() || !json.has("hasRelease") || !json.get("hasRelease").getAsBoolean()) {
                    main.post(() -> callback.onResult(null));
                    return;
                }
                String latestName = json.get("versionName").getAsString();
                int latestCode = json.get("versionCode").getAsInt();
                String notes = json.has("releaseNotes") && !json.get("releaseNotes").isJsonNull() ? json.get("releaseNotes").getAsString() : "";
                long size = json.has("fileSize") && !json.get("fileSize").isJsonNull() ? json.get("fileSize").getAsLong() : 0L;
                String sha = json.has("sha256Checksum") && !json.get("sha256Checksum").isJsonNull() ? json.get("sha256Checksum").getAsString() : null;
                String dlUrl = json.has("directDownloadUrl") && !json.get("directDownloadUrl").isJsonNull()
                        ? json.get("directDownloadUrl").getAsString()
                        : json.get("downloadUrl").getAsString();
                if (dlUrl.startsWith("/")) dlUrl = baseUrl + dlUrl;

                final String finalUrl = dlUrl;
                main.post(() -> callback.onResult(new ReleaseInfo(true, false, 1, "0", 0, latestName, latestCode, notes, size, sha, finalUrl)));
            } catch (Exception e) {
                main.post(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "Unable to check companion"));
            }
        }).start();
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getInstalledVersionName(Context context, String packageName) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            return pi.versionName != null ? pi.versionName : "";
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    public static int getInstalledVersionCode(Context context, String packageName) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (int) pi.getLongVersionCode();
            } else {
                return pi.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public static void openApp(Context context, String packageName) {
        try {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            } else {
                Toast.makeText(context, "Could not open application", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Failed to launch app: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void downloadAndVerifyApk(Activity activity, ReleaseInfo release, DownloadProgressCallback callback) {
        downloadAndVerifyApk(activity, "zez-v" + release.latestVersionName, activity.getPackageName(), release, callback);
    }

    public static void downloadAndVerifyApk(Activity activity, String customName, String expectedPkg, ReleaseInfo release, DownloadProgressCallback callback) {
        Handler main = new Handler(Looper.getMainLooper());
        cancelRequested.set(false);

        new Thread(() -> {
            File outDir = new File(activity.getCacheDir(), "updates");
            if (!outDir.exists()) outDir.mkdirs();
            String safeName = (customName != null && !customName.trim().isEmpty()) ? customName.trim() : ("app-v" + release.latestVersionName);
            File apkFile = new File(outDir, safeName + ".apk");
            if (apkFile.exists()) apkFile.delete();

            Request req = new Request.Builder().url(release.downloadUrl).get().build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    main.post(() -> callback.onError("Download failed (HTTP " + resp.code() + ")"));
                    return;
                }
                ResponseBody body = resp.body();
                long total = body.contentLength() > 0 ? body.contentLength() : release.fileSize;
                long downloaded = 0L;
                byte[] buf = new byte[32768];
                try (InputStream is = body.byteStream(); FileOutputStream fos = new FileOutputStream(apkFile)) {
                    int read;
                    int lastPct = -1;
                    while ((read = is.read(buf)) != -1) {
                        if (cancelRequested.get()) {
                            fos.close();
                            apkFile.delete();
                            main.post(() -> callback.onError("Download cancelled"));
                            return;
                        }
                        fos.write(buf, 0, read);
                        downloaded += read;
                        if (total > 0) {
                            int pct = (int) ((downloaded * 100L) / total);
                            if (pct != lastPct) {
                                lastPct = pct;
                                main.post(() -> callback.onProgress(pct));
                            }
                        }
                    }
                    fos.flush();
                }

                // 1. Verify ZIP/APK 'PK' magic header
                if (!hasPkHeader(apkFile)) {
                    apkFile.delete();
                    main.post(() -> callback.onError("Downloaded file is not a valid APK package."));
                    return;
                }

                // 2. Verify SHA-256 checksum if provided by backend
                if (release.sha256Checksum != null && !release.sha256Checksum.isEmpty()) {
                    String actualSha = computeSha256(apkFile);
                    if (!release.sha256Checksum.equalsIgnoreCase(actualSha)) {
                        apkFile.delete();
                        main.post(() -> callback.onError("APK SHA-256 checksum verification failed."));
                        return;
                    }
                }

                // 3. Verify package name & signing certificate compatibility before Android installer launch
                String compatErr = verifyPackageAndSignature(activity, apkFile, expectedPkg);
                if (compatErr != null) {
                    apkFile.delete();
                    main.post(() -> callback.onError(compatErr));
                    return;
                }

                main.post(() -> callback.onComplete(apkFile));
            } catch (Exception e) {
                if (apkFile.exists()) apkFile.delete();
                main.post(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "Download error"));
            }
        }).start();
    }

    public static boolean canInstallPackages(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return activity.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    public static void openUnknownSourcesSettings(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent i = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(i);
        }
    }

    public static void launchApkInstaller(Activity activity, File apkFile) {
        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", apkFile);
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(uri, "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(install);
    }

    private static boolean hasPkHeader(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] b = new byte[2];
            return fis.read(b) == 2 && b[0] == 'P' && b[1] == 'K';
        } catch (Exception e) {
            return false;
        }
    }

    private static String computeSha256(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buf = new byte[32768];
        try (FileInputStream fis = new FileInputStream(file)) {
            int n;
            while ((n = fis.read(buf)) > 0) md.update(buf, 0, n);
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : md.digest()) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @SuppressWarnings("deprecation")
    private static String verifyPackageAndSignature(Activity activity, File apkFile, String expectedPkg) {
        try {
            PackageManager pm = activity.getPackageManager();
            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                    ? PackageManager.GET_SIGNING_CERTIFICATES
                    : PackageManager.GET_SIGNATURES;
            PackageInfo archiveInfo = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), flags);
            if (archiveInfo == null) {
                return "Unable to parse downloaded APK manifest.";
            }
            String targetPkg = expectedPkg != null ? expectedPkg : activity.getPackageName();
            if (!targetPkg.equals(archiveInfo.packageName)) {
                return "Package mismatch: APK belongs to '" + archiveInfo.packageName + "' instead of " + targetPkg;
            }
            if (activity.getPackageName().equals(targetPkg)) {
                PackageInfo installedInfo = pm.getPackageInfo(activity.getPackageName(), flags);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    SigningInfo aSig = archiveInfo.signingInfo;
                    SigningInfo iSig = installedInfo.signingInfo;
                    if (aSig != null && iSig != null) {
                        Signature[] aArr = aSig.getApkContentsSigners();
                        Signature[] iArr = iSig.getApkContentsSigners();
                        if (aArr != null && iArr != null && aArr.length > 0 && iArr.length > 0) {
                            if (!aArr[0].equals(iArr[0])) {
                                return "Signing certificate mismatch: update APK cannot safely replace installed Zez build.";
                            }
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            Log.w(TAG, "Signature verification note: " + e.getMessage());
            return null;
        }
    }
}
