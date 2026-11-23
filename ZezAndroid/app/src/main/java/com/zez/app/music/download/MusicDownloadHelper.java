package com.zez.app.music.download;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zez.app.R;
import com.zez.app.music.model.MusicTrack;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MusicDownloadHelper {
    private static final String TAG = "MusicDownloadHelper";
    private static final String PREF_NAME = "zez_music_downloads";
    private static final String KEY_DOWNLOADS = "downloaded_tracks";

    public interface DownloadCallback {
        void onDownloadStarted(String fileName);
        void onError(String message);
    }

    public static void showDownloadDialog(Context context, MusicTrack track, DownloadCallback callback) {
        if (track == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_music_download, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView ivThumb = dialogView.findViewById(R.id.iv_download_thumb);
        TextView tvTitle = dialogView.findViewById(R.id.tv_download_title);
        TextView tvArtist = dialogView.findViewById(R.id.tv_download_artist);
        RadioGroup rgFormat = dialogView.findViewById(R.id.rg_download_format);
        Button btnCancel = dialogView.findViewById(R.id.btn_download_cancel);
        Button btnStart = dialogView.findViewById(R.id.btn_download_start);

        tvTitle.setText(track.getTitle());
        tvArtist.setText(track.getArtist());
        Glide.with(context).load(track.getThumbnailUrl()).into(ivThumb);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnStart.setOnClickListener(v -> {
            dialog.dismiss();
            int checkedId = rgFormat.getCheckedRadioButtonId();
            boolean isAudio = (checkedId == R.id.rb_format_mp3);
            startDownloadWithWebView(context, track, isAudio, callback);
        });

        dialog.show();
    }

    public static void startDownloadWithWebView(Context context, MusicTrack track, boolean isAudio, DownloadCallback callback) {
        String cleanTitle = track.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (cleanTitle.isEmpty()) cleanTitle = "music_" + track.getId();
        final String fileName = cleanTitle + (isAudio ? ".mp3" : ".mp4");

        Toast.makeText(context, "Preparing download: " + fileName, Toast.LENGTH_SHORT).show();

        // Create a non-intrusive downloader dialog with WebView
        Dialog dlDialog = new Dialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_webview_downloader, null);
        dlDialog.setContentView(view);
        if (dlDialog.getWindow() != null) {
            dlDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvStatus = view.findViewById(R.id.tv_dl_status);
        ProgressBar pbProgress = view.findViewById(R.id.pb_dl_progress);
        Button btnClose = view.findViewById(R.id.btn_dl_close);
        WebView wv = view.findViewById(R.id.wv_downloader);

        tvStatus.setText("Connecting to YouTube downloader for " + (isAudio ? "MP3..." : "MP4..."));
        btnClose.setOnClickListener(v -> {
            wv.destroy();
            dlDialog.dismiss();
        });

        WebSettings ws = wv.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");

        wv.setWebChromeClient(new WebChromeClient());
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                tvStatus.setText("Extracting media stream...");
            }
        });

        wv.setDownloadListener(new DownloadListener() {
            private boolean downloadTriggered = false;

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (downloadTriggered) return;
                downloadTriggered = true;

                Log.d(TAG, "Download started: " + url + " MIME: " + mimetype);
                enqueueSystemDownload(context, url, userAgent, fileName, mimetype);

                // Save to downloads store
                File destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                track.setDownloaded(true);
                track.setLocalFilePath(destFile.getAbsolutePath());
                saveDownloadedTrack(context, track);

                Toast.makeText(context, "Download enqueued: " + fileName, Toast.LENGTH_LONG).show();
                if (callback != null) callback.onDownloadStarted(fileName);

                wv.destroy();
                dlDialog.dismiss();
            }
        });

        // Use standard high-reliability converter gateway
        try {
            String encodedVid = URLEncoder.encode("https://www.youtube.com/watch?v=https://www.youtube.com/watch?v=" + track.getId(), "UTF-8");
            String targetUrl = isAudio
                    ? "https://yt1s.com/en/youtube-to-mp3?q=https://www.youtube.com/watch?v=" + track.getId()
                    : "https://yt1s.com/en/youtube-to-mp4?q=https://www.youtube.com/watch?v=" + track.getId();
            wv.loadUrl(targetUrl);
            dlDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load downloader url", e);
            Toast.makeText(context, "Failed to start download", Toast.LENGTH_SHORT).show();
        }
    }

    private static void enqueueSystemDownload(Context context, String url, String userAgent, String fileName, String mimeType) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            if (mimeType != null && !mimeType.isEmpty()) {
                request.setMimeType(mimeType);
            }
            if (userAgent != null && !userAgent.isEmpty()) {
                request.addRequestHeader("User-Agent", userAgent);
            }
            request.setDescription("Downloading YMusic track...");
            request.setTitle(fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error enqueuing download in DownloadManager", e);
        }
    }

    public static List<MusicTrack> getDownloadedTracks(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_DOWNLOADS, "[]");
        try {
            Type listType = new TypeToken<List<MusicTrack>>() {}.getType();
            List<MusicTrack> list = new Gson().fromJson(json, listType);
            if (list == null) list = new ArrayList<>();

            // Verify local files still exist
            List<MusicTrack> existing = new ArrayList<>();
            for (MusicTrack t : list) {
                if (t.getLocalFilePath() != null && new File(t.getLocalFilePath()).exists()) {
                    existing.add(t);
                }
            }
            return existing;
        } catch (Exception e) {
            Log.e(TAG, "Error loading downloaded tracks", e);
            return new ArrayList<>();
        }
    }

    public static void saveDownloadedTrack(Context context, MusicTrack track) {
        List<MusicTrack> tracks = getDownloadedTracks(context);
        tracks.remove(track);
        tracks.add(0, track);

        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_DOWNLOADS, new Gson().toJson(tracks)).apply();
    }

    public static void removeDownloadedTrack(Context context, MusicTrack track) {
        List<MusicTrack> tracks = getDownloadedTracks(context);
        tracks.remove(track);

        if (track.getLocalFilePath() != null) {
            try {
                File f = new File(track.getLocalFilePath());
                if (f.exists()) f.delete();
            } catch (Exception ignored) {}
        }

        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_DOWNLOADS, new Gson().toJson(tracks)).apply();
    }
}
