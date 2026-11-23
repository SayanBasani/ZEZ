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

    public interface PlayerListener {
        void onTrackChanged(MusicTrack track);
        void onPlaybackStateChanged(boolean isPlaying, boolean isBuffering);
        void onProgressUpdate(int currentSeconds, int totalSeconds);
        void onError(String error);
    }

    private final List<MusicTrack> queue = new ArrayList<>();
    private int currentIndex = -1;
    private MusicTrack currentTrack;
    private boolean isPlaying = false;
    private boolean isBuffering = false;
    private int currentSeconds = 0;
    private int totalSeconds = 0;
    private boolean isRepeat = false;
    private boolean isShuffle = false;

    private final CopyOnWriteArrayList<PlayerListener> listeners = new CopyOnWriteArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
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
            // Immediately notify listener of current state
            if (currentTrack != null) {
                listener.onTrackChanged(currentTrack);
            }
            listener.onPlaybackStateChanged(isPlaying, isBuffering);
            listener.onProgressUpdate(currentSeconds, totalSeconds);
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
    }

    public synchronized void addToQueue(MusicTrack track) {
        if (track == null) return;
        queue.add(track);
        if (currentTrack == null) {
            currentIndex = 0;
            playCurrentTrack();
        }
    }

    private void playCurrentTrack() {
        if (currentIndex < 0 || currentIndex >= queue.size()) return;
        currentTrack = queue.get(currentIndex);
        currentSeconds = 0;
        totalSeconds = 0;
        isPlaying = true;
        isBuffering = true;

        notifyTrackChanged(currentTrack);
        notifyPlaybackState(true, true);

        // Start/command MusicPlayerService
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

    public synchronized void next() {
        if (queue.isEmpty()) return;
        if (isShuffle) {
            int nextIdx = (int) (Math.random() * queue.size());
            currentIndex = nextIdx;
        } else {
            currentIndex++;
            if (currentIndex >= queue.size()) {
                if (isRepeat) {
                    currentIndex = 0;
                } else {
                    currentIndex = queue.size() - 1;
                    pause();
                    return;
                }
            }
        }
        playCurrentTrack();
    }

    public synchronized void previous() {
        if (queue.isEmpty()) return;
        if (currentSeconds > 5) {
            seekTo(0);
            return;
        }
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = isRepeat ? queue.size() - 1 : 0;
        }
        playCurrentTrack();
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

    public boolean isRepeat() {
        return isRepeat;
    }

    public void toggleRepeat() {
        isRepeat = !isRepeat;
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void toggleShuffle() {
        isShuffle = !isShuffle;
    }

    public MusicTrack getCurrentTrack() {
        return currentTrack;
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

    public List<MusicTrack> getQueue() {
        return Collections.unmodifiableList(queue);
    }

    // Called by MusicPlayerService on updates
    public void updatePlaybackState(boolean playing, boolean buffering) {
        this.isPlaying = playing;
        this.isBuffering = buffering;
        notifyPlaybackState(playing, buffering);
    }

    public void updateProgress(int currentSec, int totalSec) {
        this.currentSeconds = currentSec;
        this.totalSeconds = totalSec;
        notifyProgress(currentSec, totalSec);
    }

    public void notifyError(String error) {
        mainHandler.post(() -> {
            for (PlayerListener l : listeners) {
                l.onError(error);
            }
        });
    }

    private void notifyTrackChanged(MusicTrack track) {
        mainHandler.post(() -> {
            for (PlayerListener l : listeners) {
                l.onTrackChanged(track);
            }
        });
    }

    private void notifyPlaybackState(boolean playing, boolean buffering) {
        mainHandler.post(() -> {
            for (PlayerListener l : listeners) {
                l.onPlaybackStateChanged(playing, buffering);
            }
        });
    }

    private void notifyProgress(int current, int total) {
        mainHandler.post(() -> {
            for (PlayerListener l : listeners) {
                l.onProgressUpdate(current, total);
            }
        });
    }
}
