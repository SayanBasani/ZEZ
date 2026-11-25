package com.zez.app.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Manages public and private storage directories for Quacky:
 * - /Quacky/Music/ (downloaded music, visible to all player apps)
 * - /Quacky/Media/Quacky Images/
 * - /Quacky/Media/Quacky Video/
 * - /Quacky/Media/Quacky Documents/
 * - /Quacky/Media/Quacky Audio/
 */
public class QuackyStorageHelper {
    private static final String TAG = "QuackyStorageHelper";

    private static File getRootPublicDir(Context context) {
        File ext = Environment.getExternalStorageDirectory();
        File quackyDir = new File(ext, "Quacky");
        if (!quackyDir.exists()) {
            boolean created = quackyDir.mkdirs();
            if (!created && context != null) {
                File pubMusic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                quackyDir = new File(pubMusic, "Quacky");
                if (!quackyDir.exists()) {
                    boolean pCreated = quackyDir.mkdirs();
                    if (!pCreated) {
                        quackyDir = new File(context.getExternalFilesDir(null), "Quacky");
                        quackyDir.mkdirs();
                    }
                }
            }
        }
        return quackyDir;
    }

    public static void initDirectories(Context context) {
        try {
            getMusicDir(context);
            getMediaImagesDir(context, true);
            getMediaVideoDir(context, true);
            getMediaDocumentsDir(context);
            getMediaAudioDir(context, true);
        } catch (Exception ignored) {}
    }

    public static File getMusicDir(Context context) {
        File dir = new File(getRootPublicDir(context), "Music");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getMediaImagesDir(Context context, boolean isVisibleInGallery) {
        if (!isVisibleInGallery) {
            File privateDir = new File(context.getExternalFilesDir(null), "Quacky/Media/Quacky Images");
            if (!privateDir.exists()) privateDir.mkdirs();
            ensureNoMedia(privateDir);
            return privateDir;
        }
        File dir = new File(new File(getRootPublicDir(context), "Media"), "Quacky Images");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static File getMediaVideoDir(Context context, boolean isVisibleInGallery) {
        if (!isVisibleInGallery) {
            File privateDir = new File(context.getExternalFilesDir(null), "Quacky/Media/Quacky Video");
            if (!privateDir.exists()) privateDir.mkdirs();
            ensureNoMedia(privateDir);
            return privateDir;
        }
        File dir = new File(new File(getRootPublicDir(context), "Media"), "Quacky Video");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static File getMediaDocumentsDir(Context context) {
        File dir = new File(new File(getRootPublicDir(context), "Media"), "Quacky Documents");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static File getMediaAudioDir(Context context, boolean isVisible) {
        if (!isVisible) {
            File privateDir = new File(context.getExternalFilesDir(null), "Quacky/Media/Quacky Audio");
            if (!privateDir.exists()) privateDir.mkdirs();
            ensureNoMedia(privateDir);
            return privateDir;
        }
        File dir = new File(new File(getRootPublicDir(context), "Media"), "Quacky Audio");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private static void ensureNoMedia(File directory) {
        try {
            File noMedia = new File(directory, ".nomedia");
            if (!noMedia.exists()) {
                noMedia.createNewFile();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Broadcast to Android MediaStore so other music player apps, gallery, and file managers
     * immediately see and index the new file.
     */
    public static void scanFile(Context context, File file) {
        if (context == null || file == null || !file.exists()) return;
        try {
            MediaScannerConnection.scanFile(
                    context.getApplicationContext(),
                    new String[]{file.getAbsolutePath()},
                    null,
                    (path, uri) -> Log.i(TAG, "Indexed into MediaStore: " + path + " -> " + uri)
            );
        } catch (Exception e) {
            Log.w(TAG, "MediaScanner error: " + e.getMessage());
        }
    }

    public static String saveAudioToPublicStorage(Context context, File sourceFile, String fileName, boolean isAudio) {
        if (context == null || sourceFile == null || !sourceFile.exists() || sourceFile.length() == 0) {
            return null;
        }
        String mimeType = isAudio ? "audio/mp4" : "video/mp4";
        if (fileName != null && fileName.toLowerCase().endsWith(".mp3")) {
            mimeType = "audio/mpeg";
        } else if (fileName != null && fileName.toLowerCase().endsWith(".webm")) {
            mimeType = isAudio ? "audio/webm" : "video/webm";
        }

        // Android 10+ Scoped Storage: write cleanly into Music/Quacky or Movies/Quacky via MediaStore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                ContentResolver resolver = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                        (isAudio ? Environment.DIRECTORY_MUSIC : Environment.DIRECTORY_MOVIES) + "/Quacky");
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);

                Uri collection = isAudio
                        ? MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        : MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                Uri insertedUri = resolver.insert(collection, values);
                if (insertedUri != null) {
                    try (InputStream in = new FileInputStream(sourceFile);
                         OutputStream out = resolver.openOutputStream(insertedUri)) {
                        if (out == null) throw new java.io.IOException("Failed to open output stream for MediaStore");
                        byte[] buf = new byte[65536];
                        int n;
                        while ((n = in.read(buf)) != -1) {
                            out.write(buf, 0, n);
                        }
                        out.flush();
                    }
                    values.clear();
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    resolver.update(insertedUri, values, null, null);

                    // Return readable filesystem path if accessible, else canonical content:// URI
                    try (Cursor c = resolver.query(insertedUri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null)) {
                        if (c != null && c.moveToFirst()) {
                            int idx = c.getColumnIndex(MediaStore.MediaColumns.DATA);
                            if (idx >= 0) {
                                String path = c.getString(idx);
                                if (path != null && new File(path).exists() && new File(path).canRead()) {
                                    return path;
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                    return insertedUri.toString();
                }
            } catch (Exception e) {
                Log.w(TAG, "MediaStore public insert fallback triggered: " + e.getMessage());
            }
        }

        // Legacy / fallback file copy into Music dir or app-external Music dir
        File targetDir = getMusicDir(context);
        File dst = new File(targetDir, fileName);
        try {
            copyFileStream(sourceFile, dst);
            scanFile(context, dst);
            return dst.getAbsolutePath();
        } catch (Exception e) {
            Log.w(TAG, "Public dir copy failed, falling back to app external files dir: " + e.getMessage());
            File fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            if (fallbackDir == null) fallbackDir = context.getFilesDir();
            if (!fallbackDir.exists()) fallbackDir.mkdirs();
            File appDst = new File(fallbackDir, fileName);
            try {
                copyFileStream(sourceFile, appDst);
                return appDst.getAbsolutePath();
            } catch (Exception ex) {
                Log.e(TAG, "Fallback copy also failed: " + ex.getMessage());
                return null;
            }
        }
    }

    private static void copyFileStream(File src, File dst) throws Exception {
        if (dst.getParentFile() != null && !dst.getParentFile().exists()) {
            dst.getParentFile().mkdirs();
        }
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            out.flush();
        }
    }

    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
