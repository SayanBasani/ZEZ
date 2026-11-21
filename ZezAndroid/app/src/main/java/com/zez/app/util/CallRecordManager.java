package com.zez.app.util;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.zez.app.model.CallRecordItem;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class CallRecordManager {
    private static final String TAG = "CallRecordManager";
    private static final String PREF_NAME = "call_recording_prefs";
    private static final String KEY_AUTO_RECORD = "auto_record_calls";

    private static volatile CallRecordManager instance;

    private final Context context;
    private final CallRecordDbHelper dbHelper;
    private final SharedPreferences prefs;

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private String currentRecordId;
    private String currentFilePath;
    private long recordingStartTime = 0;
    private CallRecordItem currentItem;

    public static synchronized CallRecordManager getInstance(Context context) {
        if (instance == null) {
            instance = new CallRecordManager(context.getApplicationContext());
        }
        return instance;
    }

    private CallRecordManager(Context context) {
        this.context = context;
        this.dbHelper = CallRecordDbHelper.getInstance(context);
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isAutoRecordEnabled() {
        return prefs.getBoolean(KEY_AUTO_RECORD, true);
    }

    public void setAutoRecordEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_RECORD, enabled).apply();
    }

    public synchronized boolean isRecording() {
        return isRecording;
    }

    /**
     * Starts recording call audio silently.
     * Absolutely NO notification, NO toast, NO sound played to keep the user experience seamless.
     */
    public synchronized boolean startRecording(String phoneNumber, String contactName, String callType, String direction) {
        if (!isAutoRecordEnabled()) {
            Log.d(TAG, "Auto call recording is disabled in settings. Skipping.");
            return false;
        }

        if (isRecording) {
            Log.w(TAG, "Already recording a call. Stopping previous recording first.");
            stopRecording();
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "RECORD_AUDIO permission not granted. Cannot record call.");
            return false;
        }

        try {
            // Store strictly inside app's private sandbox storage (inaccessible to outside apps)
            File storageDir = new File(context.getFilesDir(), "CallRecordings");
            if (!storageDir.exists() && !storageDir.mkdirs()) {
                storageDir = new File(context.getExternalFilesDir(null), "CallRecordings");
                if (!storageDir.exists()) storageDir.mkdirs();
            }

            currentRecordId = "rec_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6);
            String safePhone = (phoneNumber != null ? phoneNumber.replaceAll("[^0-9a-zA-Z]", "_") : "unknown");
            String fileName = "call_" + System.currentTimeMillis() + "_" + safePhone + ".m4a";
            File outputFile = new File(storageDir, fileName);
            currentFilePath = outputFile.getAbsolutePath();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mediaRecorder = new MediaRecorder(context);
            } else {
                mediaRecorder = new MediaRecorder();
            }

            // Normal cellular calls MUST use MIC directly to prevent VoIP echo cancellation muting
            int preferredSource = "VOICE_CALL".equalsIgnoreCase(callType)
                    ? MediaRecorder.AudioSource.VOICE_COMMUNICATION
                    : MediaRecorder.AudioSource.MIC;

            try {
                mediaRecorder.setAudioSource(preferredSource);
            } catch (Exception e) {
                Log.w(TAG, "Preferred audio source " + preferredSource + " failed: " + e.getMessage());
                try {
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                } catch (Exception ex) {
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                }
            }

            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setOutputFile(currentFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            recordingStartTime = System.currentTimeMillis();

            currentItem = new CallRecordItem(
                    currentRecordId,
                    phoneNumber,
                    contactName,
                    callType,
                    direction,
                    recordingStartTime,
                    0,
                    currentFilePath,
                    0
            );

            dbHelper.insertRecord(currentItem);
            Log.d(TAG, "Silently started call recording: id=" + currentRecordId + ", file=" + currentFilePath);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to start call recording: " + e.getMessage(), e);
            releaseRecorder();
            isRecording = false;
            return false;
        }
    }

    /**
     * Stops the active call recording silently and finalizes the audio file.
     * Absolutely NO notification or toast.
     */
    public synchronized CallRecordItem stopRecording() {
        if (!isRecording) {
            return null;
        }

        CallRecordItem finishedItem = currentItem;
        isRecording = false;

        try {
            if (mediaRecorder != null) {
                try {
                    mediaRecorder.stop();
                } catch (RuntimeException stopException) {
                    Log.w(TAG, "MediaRecorder stop failed (call too short?): " + stopException.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping MediaRecorder: " + e.getMessage(), e);
        } finally {
            releaseRecorder();
        }

        long durationSec = (System.currentTimeMillis() - recordingStartTime) / 1000;
        if (durationSec < 1) durationSec = 1;

        if (currentFilePath != null) {
            File file = new File(currentFilePath);
            if (!file.exists() || file.length() < 100) {
                // File empty or corrupted, remove stub
                Log.w(TAG, "Recording file empty or missing, deleting record: " + currentFilePath);
                if (file.exists()) file.delete();
                if (currentRecordId != null) {
                    dbHelper.deleteRecording(currentRecordId);
                }
                finishedItem = null;
            } else {
                long fileSize = file.length();
                if (finishedItem != null) {
                    finishedItem.setDurationSeconds((int) durationSec);
                    finishedItem.setFileSize(fileSize);
                }
                if (currentRecordId != null) {
                    dbHelper.updateDurationAndSize(currentRecordId, (int) durationSec, fileSize);
                }
                Log.d(TAG, "Call recording successfully saved: " + currentFilePath + " (" + fileSize + " bytes, " + durationSec + "s)");
            }
        }

        currentRecordId = null;
        currentFilePath = null;
        currentItem = null;
        recordingStartTime = 0;

        return finishedItem;
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (Exception ignored) {}
            mediaRecorder = null;
        }
    }

    public List<CallRecordItem> getAllRecordings() {
        return dbHelper.getAllRecordings();
    }

    public List<CallRecordItem> getRecordingsForNumber(String numberOrId) {
        return dbHelper.getRecordingsForNumber(numberOrId);
    }

    public boolean deleteRecording(String id) {
        return dbHelper.deleteRecording(id);
    }

    public int getRecordingsCount() {
        return dbHelper.getRecordingsCount();
    }

    public long getTotalSizeBytes() {
        return dbHelper.getTotalSizeBytes();
    }
}
