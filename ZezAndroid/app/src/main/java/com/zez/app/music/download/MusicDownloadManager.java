package com.zez.app.music.download;

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
import com.zez.app.R;
import com.zez.app.music.MusicActivity;
import com.zez.app.music.model.MusicTrack;
import com.zez.app.util.ChatApiClient;

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

    private String resolveStreamUrlWithHeadlessWebView(Context context, String videoId) {
        String existing = capturedStreamUrls.get(videoId);
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final String[] captured = new String[1];

        mainHandler.post(() -> {
            try {
                WebView extractor = new WebView(context);
                WebSettings settings = extractor.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setMediaPlaybackRequiresUserGesture(false);
                settings.setDomStorageEnabled(true);
                settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");

                extractor.setWebViewClient(new WebViewClient() {
                    @Nullable
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                        if (request != null && request.getUrl() != null) {
                            String u = request.getUrl().toString();
                            if (u.contains("googlevideo.com/videoplayback")) {
                                synchronized (captured) {
                                    if (captured[0] == null) {
                                        captured[0] = u;
                                        registerCapturedStreamUrl(videoId, u);
                                        latch.countDown();
                                        mainHandler.post(() -> {
                                            try {
                                                extractor.stopLoading();
                                                extractor.destroy();
                                            } catch (Exception ignored) {}
                                        });
                                    }
                                }
                            }
                        }
                        return super.shouldInterceptRequest(view, request);
                    }
                });

                String html = "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head><body>" +
                        "<div id=\"player\"></div>" +
                        "<script>" +
                        "var tag = document.createElement('script');" +
                        "tag.src = 'https://www.youtube.com/iframe_api';" +
                        "var firstScriptTag = document.getElementsByTagName('script')[0];" +
                        "firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);" +
                        "var player;" +
                        "function onYouTubeIframeAPIReady() {" +
                        "  player = new YT.Player('player', {" +
                        "    height: '100', width: '100', videoId: '" + videoId + "'," +
                        "    playerVars: { 'autoplay': 1, 'controls': 0, 'playsinline': 1, 'origin': 'https://www.youtube.com' }," +
                        "    events: { 'onReady': function(e){ e.target.mute(); e.target.playVideo(); } }" +
                        "  });" +
                        "}" +
                        "</script></body></html>";

                extractor.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", "https://www.youtube.com");
            } catch (Exception e) {
                Log.e(TAG, "Error starting headless extractor: " + e.getMessage());
                latch.countDown();
            }
        });

        try {
            latch.await(6, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}

        return captured[0] != null ? captured[0] : capturedStreamUrls.get(videoId);
    }

    private void executeDownload(Context context, DownloadTask task, boolean isAudio) {
        String videoId = task.getVideoId();
        String baseUrl = ChatApiClient.getBaseUrl(context);
        OkHttpClient client = ChatApiClient.getOkHttpClient();

        String directUrl = capturedStreamUrls.get(videoId);
        if (directUrl == null) {
            directUrl = resolveStreamUrlWithHeadlessWebView(context, videoId);
        }

        String downloadUrl;
        if (directUrl != null && !directUrl.isEmpty()) {
            downloadUrl = directUrl;
            Log.i(TAG, "Executing download via direct googlevideo URL for " + videoId);
        } else {
            try {
                String encodedTitle = URLEncoder.encode(task.getFileName().replaceAll("\\.[^.]+$", ""), "UTF-8");
                downloadUrl = baseUrl + "/api/music/download?videoId=" + videoId + "&title=" + encodedTitle;
            } catch (Exception e) {
                downloadUrl = baseUrl + "/api/music/download?videoId=" + videoId;
            }
            Log.i(TAG, "Executing download via backend endpoint for " + videoId);
        }

        File destFile = task.getDestinationFile();
        File tempFile = new File(destFile.getAbsolutePath() + ".download");

        int notifId = Math.abs(videoId.hashCode());
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);

        try {
            Request.Builder reqBuilder = new Request.Builder()
                    .url(downloadUrl)
                    .get()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .addHeader("Accept", "*/*");

            Call call = client.newCall(reqBuilder.build());
            task.setActiveCall(call);

            Response response = call.execute();
            if (!response.isSuccessful() || response.body() == null) {
                throw new Exception("HTTP response unsuccessful: " + response.code());
            }

            ResponseBody body = response.body();
            long contentLength = body.contentLength();
            if (contentLength > 0) {
                task.setTotalBytes(contentLength);
            }

            if (tempFile.getParentFile() != null && !tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }

            try (InputStream is = body.byteStream();
                 FileOutputStream fos = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[8192];
                int read;
                long totalRead = 0;
                long lastTime = System.currentTimeMillis();
                long lastBytes = 0;
                long lastNotifUpdate = 0;

                while ((read = is.read(buffer)) != -1) {
                    if (task.getStatus() == DownloadTask.STATUS_CANCELLED) {
                        try { tempFile.delete(); } catch (Exception ignored) {}
                        return;
                    }

                    fos.write(buffer, 0, read);
                    totalRead += read;
                    task.setDownloadedBytes(totalRead);

                    long now = System.currentTimeMillis();
                    long timeDelta = now - lastTime;
                    if (timeDelta >= 400) {
                        long bytesDelta = totalRead - lastBytes;
                        long speed = (bytesDelta * 1000) / timeDelta;
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
                fos.flush();
            }

            // Rename tempFile to destFile
            if (destFile.exists()) destFile.delete();
            if (!tempFile.renameTo(destFile)) {
                // If rename fails, fallback copy
                copyFile(tempFile, destFile);
                tempFile.delete();
            }

            // Success!
            task.setStatus(DownloadTask.STATUS_COMPLETED);
            MusicTrack orig = task.getOriginalTrack();
            orig.setDownloaded(true);
            orig.setLocalFilePath(destFile.getAbsolutePath());
            MusicDownloadHelper.saveDownloadedTrack(context, orig);

            activeTasks.remove(videoId);

            // Post completion notification
            showCompletedNotification(context, nm, task, notifId);

            mainHandler.post(() -> {
                Toast.makeText(context, "Downloaded: " + task.getTitle(), Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(context, MusicActivity.class);
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
        Intent intent = new Intent(context, MusicActivity.class);
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
