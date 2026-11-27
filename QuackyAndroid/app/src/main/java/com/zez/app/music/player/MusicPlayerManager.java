package com.zez.app.music.player;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.zez.app.music.model.MusicTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicPlayerManager {
    private static final String TAG = "MusicPlayerManager";
    private static MusicPlayerManager instance;

    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ALL = 1;
    public static final int REPEAT_MODE_ONE = 2;

    public interface PlayerListener {
        void onTrackChanged(MusicTrack track);
        void onPlaybackStateChanged(boolean isPlaying, boolean isBuffering);
        void onProgressUpdate(int currentSeconds, int totalSeconds);
        void onError(String error);
        default void onQueueChanged(List<MusicTrack> queue, int currentIndex) {}
        default void onVideoModeChanged(boolean isVideoMode) {}
    }

    private final List<MusicTrack> queue = new ArrayList<>();
    private int currentIndex = -1;
    private MusicTrack currentTrack;
    private boolean isPlaying = false;
    private boolean isBuffering = false;
    private int currentSeconds = 0;
    private int totalSeconds = 0;
    private int repeatMode = REPEAT_MODE_NONE;
    private boolean isShuffle = false;
    private boolean isVideoMode = false;

    private final CopyOnWriteArrayList<PlayerListener> listeners = new CopyOnWriteArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable sleepTimerRunnable;
    private Context appContext;

    private MusicPlayerManager() {}

    public static synchronized MusicPlayerManager getInstance() {
        if (instance == null) {
            instance = new MusicPlayerManager();
        }
        return instance;
    }

    public void init(Context context) {
        if (this.appContext == null) {
            this.appContext = context.getApplicationContext();
        }
    }

    public void registerListener(PlayerListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            if (currentTrack != null) {
                listener.onTrackChanged(currentTrack);
            }
            listener.onPlaybackStateChanged(isPlaying, isBuffering);
            listener.onProgressUpdate(currentSeconds, totalSeconds);
            listener.onQueueChanged(new ArrayList<>(queue), currentIndex);
            listener.onVideoModeChanged(isVideoMode);
        }
    }

    public void unregisterListener(PlayerListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public synchronized void playTrack(MusicTrack track) {
        if (track == null) return;
        queue.clear();
        queue.add(track);
        currentIndex = 0;
        playCurrentTrack();
        notifyQueueChanged();
    }

    public synchronized void playQueue(List<MusicTrack> tracks, int startIndex) {
        if (tracks == null || tracks.isEmpty()) return;
        queue.clear();
        queue.addAll(tracks);
        if (startIndex >= 0 && startIndex < queue.size()) {
            currentIndex = startIndex;
        } else {
            currentIndex = 0;
        }
        playCurrentTrack();
        notifyQueueChanged();
    }

    public synchronized void addToQueue(MusicTrack track) {
        if (track == null) return;
        queue.add(track);
        notifyQueueChanged();
        if (currentTrack == null) {
            currentIndex = 0;
            playCurrentTrack();
        }
    }

    public synchronized void addNextToQueue(MusicTrack track) {
        if (track == null) return;
        if (queue.isEmpty() || currentIndex < 0) {
            addToQueue(track);
            return;
        }
        queue.add(currentIndex + 1, track);
        notifyQueueChanged();
    }

    public synchronized void playQueueIndex(int index) {
        if (index >= 0 && index < queue.size()) {
            currentIndex = index;
            playCurrentTrack();
            notifyQueueChanged();
        }
    }

    public synchronized void removeFromQueue(int index) {
        if (index >= 0 && index < queue.size()) {
            queue.remove(index);
            if (index < currentIndex) {
                currentIndex--;
            } else if (index == currentIndex) {
                if (queue.isEmpty()) {
                    currentTrack = null;
                    currentIndex = -1;
                    pause();
                    notifyTrackChanged(null);
                } else {
                    if (currentIndex >= queue.size()) {
                        currentIndex = 0;
                    }
                    playCurrentTrack();
                }
            }
            notifyQueueChanged();
        }
    }

    public static int parseDurationToSeconds(String durStr) {
        if (durStr == null || durStr.trim().isEmpty()) return 0;
        try {
            String[] parts = durStr.trim().split(":");
            if (parts.length == 2) {
                return Integer.parseInt(parts[0].trim()) * 60 + Integer.parseInt(parts[1].trim());
            } else if (parts.length == 3) {
                return Integer.parseInt(parts[0].trim()) * 3600 + Integer.parseInt(parts[1].trim()) * 60 + Integer.parseInt(parts[2].trim());
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private void playCurrentTrack() {
        if (currentIndex < 0 || currentIndex >= queue.size()) return;
        currentTrack = queue.get(currentIndex);
        currentSeconds = 0;
        totalSeconds = parseDurationToSeconds(currentTrack != null ? currentTrack.getDuration() : null);
        isPlaying = true;
        isBuffering = true;

        if (appContext != null && currentTrack != null) {
            com.zez.app.music.util.MusicHistoryManager.addTrackToPlaybackHistory(appContext, currentTrack);
            com.zez.app.music.util.MusicRecommendationEngine.recordTrackPlayed(appContext, currentTrack, false);
        }

        notifyTrackChanged(currentTrack);
        notifyPlaybackState(true, true);
        notifyProgress(0, totalSeconds);

        if (appContext != null) {
            Intent intent = new Intent(appContext, MusicPlayerService.class);
            intent.setAction(MusicPlayerService.ACTION_PLAY_TRACK);
            intent.putExtra(MusicPlayerService.EXTRA_TRACK, currentTrack);
            try {
                ContextCompat.startForegroundService(appContext, intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to start MusicPlayerService", e);
            }
        }
    }

    public void togglePlayPause() {
        if (isPlaying) {
            pause();
        } else {
            play();
        }
    }

    public void play() {
        if (currentTrack == null && !queue.isEmpty()) {
            currentIndex = 0;
            playCurrentTrack();
            return;
        }
        if (appContext != null) {
            Intent intent = new Intent(appContext, MusicPlayerService.class);
            intent.setAction(MusicPlayerService.ACTION_RESUME);
            try {
                ContextCompat.startForegroundService(appContext, intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send RESUME", e);
            }
        }
    }

    public void pause() {
        if (appContext != null) {
            Intent intent = new Intent(appContext, MusicPlayerService.class);
            intent.setAction(MusicPlayerService.ACTION_PAUSE);
            try {
                ContextCompat.startForegroundService(appContext, intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send PAUSE", e);
            }
        }
    }

    public synchronized void clearFromServiceStop() {
        isPlaying = false;
        isBuffering = false;
        currentTrack = null;
        currentIndex = -1;
        currentSeconds = 0;
        totalSeconds = 0;
        notifyTrackChanged(null);
        notifyPlaybackState(false, false);
        notifyProgress(0, 0);
    }

    public void stop() {
        clearFromServiceStop();
        if (appContext != null) {
            try {
                appContext.stopService(new Intent(appContext, MusicPlayerService.class));
            } catch (Exception e) {
                Log.e(TAG, "Failed to stop MusicPlayerService", e);
            }
        }
    }

    public synchronized void next() {
        if (queue.isEmpty()) return;
        if (appContext != null && currentTrack != null && currentSeconds > 0 && currentSeconds < 25 && totalSeconds > 60) {
            com.zez.app.music.util.MusicRecommendationEngine.recordTrackSkipped(appContext, currentTrack);
        }
        if (repeatMode == REPEAT_MODE_ONE) {
            seekTo(0);
            play();
            return;
        }
        if (isShuffle) {
            int nextIdx = (int) (Math.random() * queue.size());
            currentIndex = nextIdx;
        } else {
            currentIndex++;
            if (currentIndex >= queue.size()) {
                if (repeatMode == REPEAT_MODE_ALL) {
                    currentIndex = 0;
                } else {
                    currentIndex = queue.size() - 1;
                    pause();
                    return;
                }
            }
        }
        playCurrentTrack();
        notifyQueueChanged();
    }

    public synchronized void previous() {
        if (queue.isEmpty()) return;
        if (currentSeconds > 5) {
            seekTo(0);
            return;
        }
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = (repeatMode == REPEAT_MODE_ALL) ? queue.size() - 1 : 0;
        }
        playCurrentTrack();
        notifyQueueChanged();
    }

    public void seekTo(int seconds) {
        if (appContext != null) {
            Intent intent = new Intent(appContext, MusicPlayerService.class);
            intent.setAction(MusicPlayerService.ACTION_SEEK);
            intent.putExtra(MusicPlayerService.EXTRA_SEEK_SECONDS, seconds);
            try {
                ContextCompat.startForegroundService(appContext, intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send SEEK", e);
            }
        }
    }

    public void toggleShuffle() {
        isShuffle = !isShuffle;
    }

    public void setSleepTimerMinutes(int minutes) {
        if (sleepTimerRunnable != null) {
            mainHandler.removeCallbacks(sleepTimerRunnable);
            sleepTimerRunnable = null;
        }
        if (minutes > 0) {
            sleepTimerRunnable = () -> {
                Log.d(TAG, "Sleep timer expired, pausing music");
                pause();
            };
            mainHandler.postDelayed(sleepTimerRunnable, (long) minutes * 60 * 1000L);
        }
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void toggleRepeat() {
        if (repeatMode == REPEAT_MODE_NONE) {
            repeatMode = REPEAT_MODE_ALL;
        } else if (repeatMode == REPEAT_MODE_ALL) {
            repeatMode = REPEAT_MODE_ONE;
        } else {
            repeatMode = REPEAT_MODE_NONE;
        }
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public boolean isRepeat() {
        return repeatMode != REPEAT_MODE_NONE;
    }

    public void setVideoMode(boolean isVideo) {
        this.isVideoMode = isVideo;
        mainHandler.post(() -> {
            for (PlayerListener listener : listeners) {
                listener.onVideoModeChanged(isVideoMode);
            }
        });
    }

    public boolean isVideoMode() {
        return isVideoMode;
    }

    public MusicTrack getCurrentTrack() {
        return currentTrack;
    }

    public List<MusicTrack> getQueue() {
        return new ArrayList<>(queue);
    }

    public synchronized void clearQueue() {
        queue.clear();
        if (currentTrack != null) {
            queue.add(currentTrack);
            currentIndex = 0;
        } else {
            currentIndex = -1;
        }
        notifyQueueChanged();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isBuffering() {
        return isBuffering;
    }

    public int getCurrentSeconds() {
        return currentSeconds;
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    public void updatePlaybackState(boolean playing, boolean buffering) {
        this.isPlaying = playing;
        this.isBuffering = buffering;
        notifyPlaybackState(playing, buffering);
    }

    public void updateProgress(int current, int total) {
        this.currentSeconds = current;
        if (total > 0) {
            this.totalSeconds = total;
        }
        notifyProgress(current, this.totalSeconds);
    }

    public void notifyError(String error) {
        mainHandler.post(() -> {
            for (PlayerListener listener : listeners) {
                listener.onError(error);
            }
        });
    }

    private void notifyTrackChanged(MusicTrack track) {
        mainHandler.post(() -> {
            for (PlayerListener listener : listeners) {
                listener.onTrackChanged(track);
            }
        });
    }

    private void notifyPlaybackState(boolean playing, boolean buffering) {
        mainHandler.post(() -> {
            for (PlayerListener listener : listeners) {
                listener.onPlaybackStateChanged(playing, buffering);
            }
        });
    }

    private void notifyProgress(int current, int total) {
        mainHandler.post(() -> {
            for (PlayerListener listener : listeners) {
                listener.onProgressUpdate(current, total);
            }
        });
    }

    private void notifyQueueChanged() {
        mainHandler.post(() -> {
            for (PlayerListener listener : listeners) {
                listener.onQueueChanged(new ArrayList<>(queue), currentIndex);
            }
        });
    }
}
