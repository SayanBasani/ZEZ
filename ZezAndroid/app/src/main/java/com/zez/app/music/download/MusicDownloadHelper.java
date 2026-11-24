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
import android.widget.EditText;
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
        EditText etFilename = dialogView.findViewById(R.id.et_download_filename);
        Button btnCancel = dialogView.findViewById(R.id.btn_download_cancel);
        Button btnStart = dialogView.findViewById(R.id.btn_download_start);

        tvTitle.setText(track.getTitle());
        tvArtist.setText(track.getArtist());
        Glide.with(context).load(track.getThumbnailUrl()).into(ivThumb);

        String cleanTitle = track.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (cleanTitle.isEmpty()) cleanTitle = "music_" + track.getId();
        if (etFilename != null) {
            etFilename.setText(cleanTitle);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        final String defaultCleanTitle = cleanTitle;
        btnStart.setOnClickListener(v -> {
            dialog.dismiss();
            String customTitle = defaultCleanTitle;
            if (etFilename != null) {
                String input = etFilename.getText().toString().replaceAll("[\\\\/:*?\"<>|]", "_").trim();
                if (!input.isEmpty()) {
                    customTitle = input;
                }
            }

            int checkedId = rgFormat.getCheckedRadioButtonId();
            boolean isAudio = true;
            String extension = ".mp3";
            String qualityTag = "mp3_320";

            if (checkedId == R.id.rb_format_m4a) {
                isAudio = true;
                extension = ".m4a";
                qualityTag = "m4a_256";
            } else if (checkedId == R.id.rb_format_mp3) {
                isAudio = true;
                extension = ".mp3";
                qualityTag = "mp3_320";
            } else if (checkedId == R.id.rb_format_mp3_std) {
                isAudio = true;
                extension = ".mp3";
                qualityTag = "mp3_128";
            } else if (checkedId == R.id.rb_format_opus) {
                isAudio = true;
                extension = ".opus";
                qualityTag = "opus_160";
            } else if (checkedId == R.id.rb_format_mp4_1080) {
                isAudio = false;
                extension = ".mp4";
                qualityTag = "mp4_1080";
            } else if (checkedId == R.id.rb_format_mp4) {
                isAudio = false;
                extension = ".mp4";
                qualityTag = "mp4_720";
            } else if (checkedId == R.id.rb_format_mp4_480) {
                isAudio = false;
                extension = ".mp4";
                qualityTag = "mp4_480";
            } else if (checkedId == R.id.rb_format_mp4_360) {
                isAudio = false;
                extension = ".mp4";
                qualityTag = "mp4_360";
            }

            final String fullFileName = customTitle + extension;
            startDownloadWithWebView(context, track, fullFileName, isAudio, qualityTag, callback);
        });

        dialog.show();
    }

    public static void startDownloadWithWebView(Context context, MusicTrack track, boolean isAudio, DownloadCallback callback) {
        String cleanTitle = track.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (cleanTitle.isEmpty()) cleanTitle = "music_" + track.getId();
        final String fileName = cleanTitle + (isAudio ? ".mp3" : ".mp4");
        startDownloadWithWebView(context, track, fileName, isAudio, isAudio ? "mp3_320" : "mp4_720", callback);
    }

    public static void startDownloadWithWebView(Context context, MusicTrack track, String targetFileName, boolean isAudio, String qualityTag, DownloadCallback callback) {
        final String fileName = targetFileName;

        Toast.makeText(context, "Starting download: " + fileName, Toast.LENGTH_SHORT).show();

        try {
            String baseUrl = com.zez.app.util.ChatApiClient.getBaseUrl(context);
            String titleOnly = fileName.replaceAll("\\.[^.]+$", "");
            String encodedTitle = URLEncoder.encode(titleOnly, "UTF-8");
            String downloadUrl = baseUrl + "/api/music/download?videoId=" + track.getId() + "&title=" + encodedTitle;

            String mimeType = isAudio ? "audio/mp4" : "video/mp4";
            enqueueSystemDownload(context, downloadUrl, null, fileName, mimeType);

            // Save to downloads store for offline playback and download directly
            File destFile = getDestinationFile(context, fileName);
            track.setDownloaded(true);
            track.setLocalFilePath(destFile.getAbsolutePath());
            saveDownloadedTrack(context, track);

            downloadDirectlyToFile(context, downloadUrl, destFile, track, callback);

            Toast.makeText(context, "Download enqueued to Downloads: " + fileName, Toast.LENGTH_LONG).show();
            if (callback != null) callback.onDownloadStarted(fileName);
        } catch (Exception e) {
            Log.e(TAG, "Error starting download", e);
            Toast.makeText(context, "Download error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    public static File getDestinationFile(Context context, String fileName) {
        try {
            File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (publicDir != null && (publicDir.exists() || publicDir.mkdirs()) && publicDir.canWrite()) {
                return new File(publicDir, fileName);
            }
        } catch (Exception ignored) {}
        File appDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (appDir == null) {
            appDir = context.getFilesDir();
        }
        return new File(appDir, fileName);
    }

    private static void downloadDirectlyToFile(Context context, String downloadUrl, File destFile, MusicTrack track, DownloadCallback callback) {
        new Thread(() -> {
            try {
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(downloadUrl)
                        .get()
                        .build();
                okhttp3.Response response = com.zez.app.util.ChatApiClient.getOkHttpClient().newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    if (destFile.getParentFile() != null && !destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    try (java.io.InputStream is = response.body().byteStream();
                         java.io.FileOutputStream fos = new java.io.FileOutputStream(destFile)) {
                        byte[] buf = new byte[8192];
                        int r;
                        while ((r = is.read(buf)) != -1) {
                            fos.write(buf, 0, r);
                        }
                        fos.flush();
                    }
                    track.setDownloaded(true);
                    track.setLocalFilePath(destFile.getAbsolutePath());
                    saveDownloadedTrack(context, track);
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, "Downloaded for offline playback: " + destFile.getName(), Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Direct download to file error", e);
            }
        }).start();
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

            List<MusicTrack> valid = new ArrayList<>();
            for (MusicTrack t : list) {
                if (t.getLocalFilePath() != null) {
                    File f = new File(t.getLocalFilePath());
                    if (f.exists() && f.length() > 0) {
                        valid.add(t);
                        continue;
                    }
                    // Check alternative destination directory
                    File alt = getDestinationFile(context, f.getName());
                    if (alt.exists() && alt.length() > 0) {
                        t.setLocalFilePath(alt.getAbsolutePath());
                        valid.add(t);
                        continue;
                    }
                }
                valid.add(t);
            }
            return valid;
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
