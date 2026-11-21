package com.zez.app.util;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;

public class AudioPlaybackManager {
    private static final String TAG = "AudioPlaybackManager";

    private static volatile AudioPlaybackManager instance;

    private MediaPlayer mediaPlayer;
    private String currentPlayingPath;
    private boolean isPaused = false;
    private OnPlaybackListener currentListener;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying() && currentListener != null) {
                try {
                    int cur = mediaPlayer.getCurrentPosition();
                    int total = mediaPlayer.getDuration();
                    currentListener.onProgress(cur, total);
                    progressHandler.postDelayed(this, 200);
                } catch (Exception ignored) {}
            }
        }
    };

    public interface OnPlaybackListener {
        void onPrepared(int totalDurationMs);
        void onProgress(int currentMs, int totalMs);
        void onPaused();
        void onResumed();
        void onCompleted();
        void onError(String error);
    }

    public static synchronized AudioPlaybackManager getInstance() {
        if (instance == null) {
            instance = new AudioPlaybackManager();
        }
        return instance;
    }

    private AudioPlaybackManager() {}

    public synchronized void play(String filePath, OnPlaybackListener listener) {
        if (filePath == null) return;
        File f = new File(filePath);
        if (!f.exists() || f.length() == 0) {
            if (listener != null) listener.onError("File does not exist or is empty");
            return;
        }

        // If currently playing the same file, toggle pause/resume
        if (filePath.equals(currentPlayingPath) && mediaPlayer != null) {
            if (isPaused) {
                resume();
                return;
            } else if (mediaPlayer.isPlaying()) {
                pause();
                return;
            }
        }

        stop();

        this.currentPlayingPath = filePath;
        this.currentListener = listener;
        this.isPaused = false;

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setOnPreparedListener(mp -> {
                try {
                    mp.start();
                    isPaused = false;
                    int dur = mp.getDuration();
                    if (currentListener != null) {
                        currentListener.onPrepared(dur);
                        currentListener.onResumed();
                    }
                    progressHandler.post(progressRunnable);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting playback in onPrepared: " + e.getMessage(), e);
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                progressHandler.removeCallbacks(progressRunnable);
                isPaused = false;
                if (currentListener != null) {
                    currentListener.onCompleted();
                }
                stop();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                progressHandler.removeCallbacks(progressRunnable);
                if (currentListener != null) {
                    currentListener.onError("Playback error (" + what + ", " + extra + ")");
                }
                stop();
                return true;
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "Failed to start playback for: " + filePath, e);
            if (listener != null) listener.onError(e.getMessage());
            stop();
        }
    }

    public synchronized void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
            progressHandler.removeCallbacks(progressRunnable);
            if (currentListener != null) {
                currentListener.onPaused();
            }
        }
    }

    public synchronized void resume() {
        if (mediaPlayer != null && isPaused) {
            mediaPlayer.start();
            isPaused = false;
            progressHandler.post(progressRunnable);
            if (currentListener != null) {
                currentListener.onResumed();
            }
        }
    }

    public synchronized void seekTo(int msec) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.seekTo(msec);
            } catch (Exception ignored) {}
        }
    }

    public synchronized void stop() {
        progressHandler.removeCallbacks(progressRunnable);
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        currentPlayingPath = null;
        isPaused = false;
        currentListener = null;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public String getCurrentPlayingPath() {
        return currentPlayingPath;
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (Exception ignored) {}
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getDuration();
            } catch (Exception ignored) {}
        }
        return 0;
    }
}
