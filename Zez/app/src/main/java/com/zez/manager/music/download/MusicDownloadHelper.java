package com.zez.manager.music.download;

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
import com.zez.manager.R;
import com.zez.manager.music.model.MusicTrack;
import com.zez.manager.util.ZezStorageHelper;

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
        // Start download directly without disruptive modal dialogs or blocking circles
        MusicDownloadManager.getInstance().startDownload(context, track, null, true);
        if (callback != null) {
            String cleanTitle = track.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_").trim();
            callback.onDownloadStarted(cleanTitle + ".m4a");
        }
    }

    public static void startDownloadWithWebView(Context context, MusicTrack track, boolean isAudio, DownloadCallback callback) {
        if (track == null) return;
        MusicDownloadManager.getInstance().startDownload(context, track, null, isAudio);
        if (callback != null) {
            String cleanTitle = track.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_").trim();
            callback.onDownloadStarted(cleanTitle + (isAudio ? ".m4a" : ".mp4"));
        }
    }

    public static void startDownloadWithWebView(Context context, MusicTrack track, String targetFileName, boolean isAudio, String qualityTag, DownloadCallback callback) {
        if (track == null) return;
        MusicDownloadManager.getInstance().startDownload(context, track, targetFileName, isAudio);
        if (callback != null) {
            callback.onDownloadStarted(targetFileName);
        }
    }

    public static File getDestinationFile(Context context, String fileName) {
        File musicDir = ZezStorageHelper.getMusicDir(context);
        return new File(musicDir, fileName);
    }

    private static void downloadDirectlyToFile(Context context, String downloadUrl, File destFile, MusicTrack track, DownloadCallback callback) {
        new Thread(() -> {
            try {
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(downloadUrl)
                        .get()
                        .build();
                okhttp3.Response response = new okhttp3.OkHttpClient().newCall(request).execute();
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
                    String lp = t.getLocalFilePath();
                    if (lp.startsWith("content://")) {
                        try (android.os.ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(Uri.parse(lp), "r")) {
                            if (pfd != null && pfd.getStatSize() > 0) {
                                valid.add(t);
                                continue;
                            }
                        } catch (Exception ignored) {}
                    } else {
                        File f = new File(lp);
                        if (f.exists() && f.length() > 0) {
                            valid.add(t);
                            continue;
                        }
                        File alt = getDestinationFile(context, f.getName());
                        if (alt.exists() && alt.length() > 0) {
                            t.setLocalFilePath(alt.getAbsolutePath());
                            valid.add(t);
                            continue;
                        }
                        File pub = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "Zez/" + f.getName());
                        if (pub.exists() && pub.length() > 0) {
                            t.setLocalFilePath(pub.getAbsolutePath());
                            valid.add(t);
                            continue;
                        }
                    }
                }
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
                String lp = track.getLocalFilePath();
                if (lp.startsWith("content://")) {
                    context.getContentResolver().delete(Uri.parse(lp), null, null);
                } else {
                    File f = new File(lp);
                    if (f.exists()) f.delete();
                }
            } catch (Exception ignored) {}
        }

        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_DOWNLOADS, new Gson().toJson(tracks)).apply();
    }
}
