package com.zez.app.recording;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.UUID;

public class RecordingStorageManager {
    private static final String TAG = "RecordingStorageManager";
    private static final String DIR_NAME = "CallRecordings";
    private final Context context;

    public RecordingStorageManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public File getStorageDirectory() {
        File dir = new File(context.getFilesDir(), DIR_NAME);
        if (!dir.exists() && !dir.mkdirs()) {
            dir = new File(context.getExternalFilesDir(null), DIR_NAME);
            if (!dir.exists()) dir.mkdirs();
        }
        return dir;
    }

    public File generateOutputFile(String phoneNumber) {
        String safePhone = (phoneNumber != null && !phoneNumber.trim().isEmpty())
                ? phoneNumber.replaceAll("[^0-9a-zA-Z]", "_")
                : "unknown";
        String fileName = "call_" + System.currentTimeMillis() + "_" + safePhone + ".m4a";
        return new File(getStorageDirectory(), fileName);
    }

    public String generateRecordId() {
        return "rec_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6);
    }

    public boolean validateAndCleanup(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.length() < 100) {
            Log.w(TAG, "Recording file empty or corrupted (" + file.length() + " bytes). Deleting: " + file.getAbsolutePath());
            file.delete();
            return false;
        }
        return true;
    }

    public boolean deleteFile(String filePath) {
        if (filePath == null) return false;
        try {
            File f = new File(filePath);
            if (f.exists()) {
                return f.delete();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting recording file: " + e.getMessage(), e);
        }
        return false;
    }

    public long getTotalStorageUsed() {
        long total = 0;
        File dir = getStorageDirectory();
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) total += f.length();
                }
            }
        }
        return total;
    }
}
