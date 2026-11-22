package com.zez.app.recording;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.io.File;

public class RecordingEngine {
    private static final String TAG = "RecordingEngine";

    private final Context context;
    private MediaRecorder mediaRecorder;
    private boolean isRecordingActive = false;

    public RecordingEngine(Context context) {
        this.context = context.getApplicationContext();
    }

    public synchronized boolean start(String outputFilePath, String callType) {
        if (isRecordingActive) {
            stop();
        }

        int[] candidateSources;
        if ("VOICE_CALL".equalsIgnoreCase(callType)) {
            candidateSources = new int[]{
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    MediaRecorder.AudioSource.MIC,
                    MediaRecorder.AudioSource.DEFAULT
            };
        } else {
            candidateSources = new int[]{
                    MediaRecorder.AudioSource.MIC,
                    MediaRecorder.AudioSource.DEFAULT,
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    MediaRecorder.AudioSource.VOICE_RECOGNITION
            };
        }

        boolean prepared = false;
        for (int source : candidateSources) {
            try {
                release();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    mediaRecorder = new MediaRecorder(context);
                } else {
                    mediaRecorder = new MediaRecorder();
                }

                mediaRecorder.setAudioSource(source);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    try {
                        mediaRecorder.setPrivacySensitive(false);
                    } catch (Exception ignored) {}
                }

                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setAudioEncodingBitRate(128000);
                mediaRecorder.setAudioSamplingRate(44100);
                mediaRecorder.setOutputFile(outputFilePath);

                mediaRecorder.prepare();
                prepared = true;
                Log.d(TAG, "MediaRecorder prepared with source: " + source);
                break;
            } catch (Exception e) {
                Log.w(TAG, "Failed to prepare AudioSource " + source + ": " + e.getMessage());
            }
        }

        if (!prepared) {
            Log.e(TAG, "No candidate audio source could be prepared.");
            release();
            return false;
        }

        try {
            // Ensure mic unmuted
            android.media.AudioManager am = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am != null && am.isMicrophoneMute()) {
                am.setMicrophoneMute(false);
            }
        } catch (Exception ignored) {}

        try {
            mediaRecorder.start();
            isRecordingActive = true;
            Log.d(TAG, "MediaRecorder recording started successfully to: " + outputFilePath);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to start MediaRecorder: " + e.getMessage(), e);
            release();
            isRecordingActive = false;
            return false;
        }
    }

    public synchronized void stop() {
        if (!isRecordingActive && mediaRecorder == null) {
            return;
        }
        isRecordingActive = false;
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
            release();
        }
    }

    public synchronized boolean isRecording() {
        return isRecordingActive;
    }

    private void release() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (Exception ignored) {}
            mediaRecorder = null;
        }
    }
}
