package com.zez.manager.music.download;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zez.manager.MainActivity;
import com.zez.manager.R;
import com.zez.manager.music.model.MusicTrack;
import com.zez.manager.music.util.YouTubeApiHelper;
import com.zez.manager.util.PreferencesHelper;
import com.zez.manager.util.ZezStorageHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MusicDownloadManager {
    private static final String TAG = "MusicDownloadManager";
    private static final String CHANNEL_ID = "zez_music_download_channel";
    private static volatile MusicDownloadManager instance;

    public interface DownloadProgressListener {
        void onDownloadStarted(DownloadTask task);
        void onProgress(DownloadTask task);
        void onCompleted(DownloadTask task);
        void onError(DownloadTask task, String error);
        void onCancelled(DownloadTask task);
    }

    private final Map<String, DownloadTask> activeTasks = new ConcurrentHashMap<>();
    private final Map<String, String> capturedStreamUrls = new ConcurrentHashMap<>();
    private final List<DownloadProgressListener> listeners = new CopyOnWriteArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService downloadExecutor = Executors.newFixedThreadPool(3);

    private MusicDownloadManager() {}

    public static MusicDownloadManager getInstance() {
        if (instance == null) {
            synchronized (MusicDownloadManager.class) {
                if (instance == null) {
                    instance = new MusicDownloadManager();
                }
            }
        }
        return instance;
    }

    public void registerCapturedStreamUrl(String videoId, String streamUrl) {
        if (videoId != null && streamUrl != null && !streamUrl.isEmpty()) {
            capturedStreamUrls.put(videoId, streamUrl);
            Log.i(TAG, "Registered captured stream URL for " + videoId);
        }
    }

    public String getCapturedStreamUrl(String videoId) {
        return capturedStreamUrls.get(videoId);
    }

    public void addListener(DownloadProgressListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(DownloadProgressListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public List<DownloadTask> getActiveTasks() {
        return new ArrayList<>(activeTasks.values());
    }

    public boolean isDownloading(String videoId) {
        return activeTasks.containsKey(videoId);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Media Downloads",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Progress and speed of media downloads");
            channel.setShowBadge(false);
            channel.setSound(null, null);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    public void startDownload(Context context, MusicTrack track, String customFileName, boolean isAudio) {
        if (track == null || track.getId() == null) return;
        final Context appContext = context.getApplicationContext();
        createNotificationChannel(appContext);

        if (activeTasks.containsKey(track.getId())) {
            Toast.makeText(appContext, "Download already in progress: " + track.getTitle(), Toast.LENGTH_SHORT).show();
            return;
        }

        String cleanTitle = (customFileName != null && !customFileName.trim().isEmpty())
                ? customFileName.trim()
                : track.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (cleanTitle.isEmpty()) cleanTitle = "track_" + track.getId();

        String extension = isAudio ? ".m4a" : ".mp4";
        if (!cleanTitle.toLowerCase().endsWith(".m4a") && !cleanTitle.toLowerCase().endsWith(".mp3") && !cleanTitle.toLowerCase().endsWith(".mp4")) {
            cleanTitle += extension;
        }

        File destFile = MusicDownloadHelper.getDestinationFile(appContext, cleanTitle);
        DownloadTask task = new DownloadTask(track, cleanTitle, destFile);
        task.setStatus(DownloadTask.STATUS_DOWNLOADING);
        activeTasks.put(track.getId(), task);

        mainHandler.post(() -> {
            Toast.makeText(appContext, "Starting download: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            for (DownloadProgressListener l : listeners) {
                l.onDownloadStarted(task);
            }
        });

        downloadExecutor.execute(() -> executeDownload(appContext, task, isAudio));
    }

    private String resolveStreamUrl(Context context, String videoId, boolean isAudio) {
        if (isAudio) {
            String dlUrl = YouTubeApiHelper.getInstance().getDownloadAudioUrlSync(videoId);
            if (dlUrl != null && !dlUrl.isEmpty()) {
                return dlUrl;
            }
        }
        String existing = capturedStreamUrls.get(videoId);
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }
        String resolved = YouTubeApiHelper.getInstance().getStreamUrlSync(videoId, !isAudio);
        if (resolved != null && !resolved.isEmpty()) {
            registerCapturedStreamUrl(videoId, resolved);
            return resolved;
        }
        return null;
    }

    private void executeDownload(Context context, DownloadTask task, boolean isAudio) {
        String videoId = task.getVideoId();
        OkHttpClient client = YouTubeApiHelper.getInstance().getDownloadHttpClient();

        // Always download into app-private cache first to guarantee Scoped Storage write reliability
        File cacheDir = context.getCacheDir();
        String ext = task.getFileName().contains(".")
                ? task.getFileName().substring(task.getFileName().lastIndexOf('.'))
                : (isAudio ? ".m4a" : ".mp4");
        File tempFile = new File(cacheDir, "ymusic_tmp_" + videoId + "_" + System.currentTimeMillis() + ext);

        int notifId = Math.abs(videoId.hashCode());
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);

        try {
            long totalRead = 0L;
            long knownTotalBytes = -1L;
            final long CHUNK_SIZE = 512L * 1024L; // 512 KB range slices bypass YouTube stream throttling & prevent read timeouts

            if (tempFile.getParentFile() != null && !tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }

            Exception lastErr = null;
            boolean downloadFinished = false;
            String currentUrl = null;
            boolean usingBackendFallback = false;

            try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(tempFile, "rw")) {
                raf.setLength(0L);

                for (int attempt = 0; attempt < 4 && !downloadFinished; attempt++) {
                    if (task.getStatus() == DownloadTask.STATUS_CANCELLED) {
                        try { tempFile.delete(); } catch (Exception ignored) {}
                        activeTasks.remove(videoId);
                        return;
                    }

                    if (attempt == 0) {
                        currentUrl = resolveStreamUrl(context, videoId, isAudio);
                    } else if (attempt < 3) {
                        capturedStreamUrls.remove(videoId);
                        YouTubeApiHelper.getInstance().invalidateCachedStream(videoId);
                        currentUrl = YouTubeApiHelper.getInstance().getStreamUrlSync(videoId, attempt == 2);
                    } else {
                        // Last-resort proxy fallback via configured backend
                        usingBackendFallback = true;
                        currentUrl = PreferencesHelper.getApiUrl(context)
                                + "/api/music/download?videoId=" + URLEncoder.encode(videoId, "UTF-8")
                                + "&title=" + URLEncoder.encode(task.getTitle(), "UTF-8");
                    }

                    if (currentUrl == null || currentUrl.trim().isEmpty() || currentUrl.contains(".m3u8")) {
                        lastErr = new Exception("No downloadable audio stream URL found for " + videoId);
                        continue;
                    }

                    try {
                        long lastTime = System.currentTimeMillis();
                        long lastBytes = totalRead;
                        long lastNotifUpdate = 0L;

                        while (true) {
                            if (task.getStatus() == DownloadTask.STATUS_CANCELLED) {
                                try { tempFile.delete(); } catch (Exception ignored) {}
                                activeTasks.remove(videoId);
                                return;
                            }

                            Request.Builder reqBuilder = new Request.Builder()
                                    .url(currentUrl)
                                    .get()
                                    .addHeader("Accept", "*/*")
                                    .addHeader("Accept-Encoding", "identity")
                                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 12; Quest 3) AppleWebKit/537.36");

                            if (!usingBackendFallback) {
                                long rangeEnd = (knownTotalBytes > 0)
                                        ? Math.min(knownTotalBytes - 1, totalRead + CHUNK_SIZE - 1)
                                        : (totalRead + CHUNK_SIZE - 1);
                                reqBuilder.addHeader("Range", "bytes=" + totalRead + "-" + rangeEnd);
                            } else if (totalRead > 0) {
                                reqBuilder.addHeader("Range", "bytes=" + totalRead + "-");
                            }

                            Call call = client.newCall(reqBuilder.build());
                            task.setActiveCall(call);

                            try (Response response = call.execute()) {
                                int code = response.code();
                                if (code == 416 && knownTotalBytes > 0 && totalRead >= knownTotalBytes) {
                                    downloadFinished = true;
                                    break;
                                }
                                if (!response.isSuccessful() || response.body() == null) {
                                    throw new java.io.IOException("HTTP " + code + " reading stream");
                                }

                                ResponseBody body = response.body();
                                String contentRange = response.header("Content-Range");
                                boolean isPartial = (code == 206 && contentRange != null);
                                if (isPartial) {
                                    int slash = contentRange.lastIndexOf('/');
                                    if (slash > 0) {
                                        try {
                                            long parsedTotal = Long.parseLong(contentRange.substring(slash + 1).trim());
                                            if (parsedTotal > 0) {
                                                knownTotalBytes = parsedTotal;
                                                task.setTotalBytes(knownTotalBytes);
                                            }
                                        } catch (NumberFormatException ignored) {}
                                    }
                                } else {
                                    // Full stream returned (200 OK): rewind to 0 unless backend resumed
                                    if (code == 200) {
                                        totalRead = 0L;
                                        raf.seek(0L);
                                    }
                                    long cl = body.contentLength();
                                    if (cl > 0) {
                                        knownTotalBytes = totalRead + cl;
                                        task.setTotalBytes(knownTotalBytes);
                                    }
                                }

                                raf.seek(totalRead);
                                byte[] buffer = new byte[65536];
                                int read;
                                try (InputStream is = body.byteStream()) {
                                    while ((read = is.read(buffer)) != -1) {
                                        if (task.getStatus() == DownloadTask.STATUS_CANCELLED) {
                                            try { tempFile.delete(); } catch (Exception ignored) {}
                                            activeTasks.remove(videoId);
                                            return;
                                        }
                                        raf.write(buffer, 0, read);
                                        totalRead += read;
                                        task.setDownloadedBytes(totalRead);

                                        long now = System.currentTimeMillis();
                                        long timeDelta = now - lastTime;
                                        if (timeDelta >= 350) {
                                            long bytesDelta = totalRead - lastBytes;
                                            long speed = (bytesDelta * 1000) / Math.max(1L, timeDelta);
                                            task.setSpeedBytesPerSec(speed);
                                            lastTime = now;
                                            lastBytes = totalRead;

                                            mainHandler.post(() -> {
                                                for (DownloadProgressListener l : listeners) {
                                                    l.onProgress(task);
                                                }
                                            });

                                            if (now - lastNotifUpdate >= 1000) {
                                                lastNotifUpdate = now;
                                                updateProgressNotification(context, nm, task, notifId);
                                            }
                                        }
                                    }
                                }

                                if (!isPartial || (knownTotalBytes > 0 && totalRead >= knownTotalBytes) || body.contentLength() == 0) {
                                    downloadFinished = true;
                                    break;
                                }
                            }
                        }
                    } catch (Exception chunkErr) {
                        lastErr = chunkErr;
                        Log.w(TAG, "Download attempt " + attempt + " failed at " + totalRead + " bytes: " + chunkErr.getMessage());
                        try { Thread.sleep(400); } catch (InterruptedException ignored) { break; }
                    }
                }
            }

            if (!downloadFinished || tempFile.length() == 0) {
                throw (lastErr != null) ? lastErr : new java.io.IOException("Stream returned 0 bytes");
            }

            // Detect real container magic bytes so MediaStore & phone music players always get exact extension & MIME
            String finalFileName = detectActualFileName(tempFile, task.getFileName(), isAudio);
            MusicTrack orig = task.getOriginalTrack();
            String savedLocation = ZezStorageHelper.saveAudioToPublicStorage(
                    context,
                    tempFile,
                    finalFileName,
                    isAudio,
                    orig != null ? orig.getTitle() : null,
                    orig != null ? orig.getArtist() : null);
            try { tempFile.delete(); } catch (Exception ignored) {}

            if (savedLocation == null || savedLocation.trim().isEmpty()) {
                throw new java.io.IOException("Failed to write downloaded track to device storage");
            }

            // Success!
            task.setStatus(DownloadTask.STATUS_COMPLETED);
            if (orig != null) {
                orig.setDownloaded(true);
                orig.setLocalFilePath(savedLocation);
                MusicDownloadHelper.saveDownloadedTrack(context, orig);
            }

            activeTasks.remove(videoId);
            showCompletedNotification(context, nm, task, notifId);

            mainHandler.post(() -> {
                Toast.makeText(context, "Saved to Music/Quacky: " + task.getTitle(), Toast.LENGTH_SHORT).show();
                for (DownloadProgressListener l : listeners) {
                    l.onCompleted(task);
                }
            });

        } catch (Exception e) {
            if (task.getStatus() == DownloadTask.STATUS_CANCELLED) {
                try { tempFile.delete(); } catch (Exception ignored) {}
                activeTasks.remove(videoId);
                nm.cancel(notifId);
                return;
            }

            Log.e(TAG, "Download failed for " + videoId, e);
            task.setStatus(DownloadTask.STATUS_FAILED);
            task.setErrorMessage(e.getMessage());
            try { tempFile.delete(); } catch (Exception ignored) {}
            activeTasks.remove(videoId);
            nm.cancel(notifId);

            mainHandler.post(() -> {
                Toast.makeText(context, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                for (DownloadProgressListener l : listeners) {
                    l.onError(task, e.getMessage());
                }
            });
        }
    }

    public void cancelDownload(String videoId) {
        DownloadTask task = activeTasks.remove(videoId);
        if (task != null) {
            task.setStatus(DownloadTask.STATUS_CANCELLED);
            if (task.getActiveCall() != null) {
                try { task.getActiveCall().cancel(); } catch (Exception ignored) {}
            }
            if (task.getDestinationFile() != null) {
                try {
                    File temp = new File(task.getDestinationFile().getAbsolutePath() + ".download");
                    if (temp.exists()) temp.delete();
                } catch (Exception ignored) {}
            }
            mainHandler.post(() -> {
                for (DownloadProgressListener l : listeners) {
                    l.onCancelled(task);
                }
            });
        }
    }

    private void updateProgressNotification(Context context, NotificationManagerCompat nm, DownloadTask task, int notifId) {
        Intent intent = new Intent(context, com.zez.manager.MainActivity.class);
        intent.putExtra("extra_open_tab", com.zez.manager.R.id.nav_music);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Downloading: " + task.getTitle())
                .setContentText(task.getPercent() + "% • " + task.getSpeedFormatted() + " • " + task.getSizeFormatted())
                .setProgress(100, task.getPercent(), task.getTotalBytes() <= 0)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pi);

        try {
            nm.notify(notifId, builder.build());
        } catch (SecurityException ignored) {}
    }

    private void showCompletedNotification(Context context, NotificationManagerCompat nm, DownloadTask task, int notifId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("select_tab", "music");
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Download completed")
                .setContentText(task.getTitle())
                .setAutoCancel(true)
                .setContentIntent(pi);

        try {
            nm.notify(notifId, builder.build());
        } catch (SecurityException ignored) {}
    }

    private static String detectActualFileName(File file, String originalFileName, boolean isAudio) {
        String baseName = originalFileName != null ? originalFileName : "track";
        int dot = baseName.lastIndexOf('.');
        String stem = dot > 0 ? baseName.substring(0, dot) : baseName;
        String fallbackExt = dot > 0 ? baseName.substring(dot) : (isAudio ? ".m4a" : ".mp4");
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            byte[] header = new byte[16];
            int n = fis.read(header);
            if (n >= 4) {
                // WebM / Matroska magic bytes: 1A 45 DF A3
                if ((header[0] & 0xFF) == 0x1A && (header[1] & 0xFF) == 0x45
                        && (header[2] & 0xFF) == 0xDF && (header[3] & 0xFF) == 0xA3) {
                    return stem + ".webm";
                }
                // MP3 ID3 or MPEG ADTS sync
                if ((header[0] == 'I' && header[1] == 'D' && header[2] == '3')
                        || ((header[0] & 0xFF) == 0xFF && (header[1] & 0xE0) == 0xE0)) {
                    return stem + ".mp3";
                }
                // ISO Base Media / MP4 / M4A ('ftyp' at offset 4)
                if (n >= 8 && header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p') {
                    return stem + (isAudio ? ".m4a" : ".mp4");
                }
            }
        } catch (Exception ignored) {}
        return stem + fallbackExt;
    }

    private static void copyFile(File src, File dst) throws Exception {
        try (InputStream in = new java.io.FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }
}
