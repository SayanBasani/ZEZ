package com.zez.app.music.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.zez.app.R;
import com.zez.app.music.MusicActivity;
import com.zez.app.music.download.MusicDownloadHelper;
import com.zez.app.music.download.MusicDownloadManager;
import com.zez.app.music.model.MusicTrack;
import com.zez.app.music.util.YouTubeApiHelper;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class MusicPlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "MusicPlayerService";

    public static final String ACTION_PLAY_TRACK = "com.zez.app.music.ACTION_PLAY_TRACK";
    public static final String ACTION_RESUME = "com.zez.app.music.ACTION_RESUME";
    public static final String ACTION_PAUSE = "com.zez.app.music.ACTION_PAUSE";
    public static final String ACTION_TOGGLE = "com.zez.app.music.ACTION_TOGGLE";
    public static final String ACTION_NEXT = "com.zez.app.music.ACTION_NEXT";
    public static final String ACTION_PREV = "com.zez.app.music.ACTION_PREV";
    public static final String ACTION_SEEK = "com.zez.app.music.ACTION_SEEK";
    public static final String ACTION_STOP = "com.zez.app.music.ACTION_STOP";

    public static final String EXTRA_TRACK = "extra_track";
    public static final String EXTRA_SEEK_SECONDS = "extra_seek_seconds";

    private static final String CHANNEL_ID = "zez_music_channel";
    private static final int NOTIFICATION_ID = 4001;

    private static WebView activeWebView = null;

    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;
    private Handler handler;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private PowerManager.WakeLock wakeLock;

    private MusicTrack currentTrack;
    private String currentVideoId;
    private Bitmap currentThumbnailBitmap;
    private boolean isPlaying = false;
    private boolean isDataSourceSet = false;
    private boolean isPreparing = false;
    private boolean isPrepared = false;
    private boolean playWhenReady = true;
    private boolean resumeOnFocusGain = false;
    private boolean hasRetriedWithFallback = false;
    private int lastReportedPositionSeconds = 0;

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

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying && mediaPlayer != null) {
                try {
                    int pos = mediaPlayer.getCurrentPosition() / 1000;
                    int dur = mediaPlayer.getDuration() / 1000;
                    if (dur <= 0 && currentTrack != null) {
                        dur = parseDurationToSeconds(currentTrack.getDuration());
                    }
                    lastReportedPositionSeconds = pos;
                    MusicPlayerManager.getInstance().updateProgress(pos, dur);
                    syncMediaSessionPlaybackState(pos * 1000L);
                } catch (Exception ignored) {}
                handler.postDelayed(this, 500);
            }
        }
    };

    private void syncMediaSessionPlaybackState(long positionMs) {
        if (mediaSession != null) {
            long actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SEEK_TO;
            int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setActions(actions)
                    .setState(state, positionMs, isPlaying ? 1.0f : 0.0f)
                    .build());
        }
    }

    private void startProgressUpdates() {
        handler.removeCallbacks(progressRunnable);
        handler.post(progressRunnable);
    }

    private void stopProgressUpdates() {
        handler.removeCallbacks(progressRunnable);
    }

    private void initMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
        isDataSourceSet = false;

        try {
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        } catch (Exception ignored) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        mediaPlayer.setOnPreparedListener(mp -> {
            isPreparing = false;
            isPrepared = true;
            Log.i(TAG, "Native MediaPlayer prepared! playWhenReady=" + playWhenReady);
            try {
                int dur = mp.getDuration() / 1000;
                if (dur <= 0 && currentTrack != null) {
                    dur = parseDurationToSeconds(currentTrack.getDuration());
                }
                if (playWhenReady) {
                    mp.start();
                    isPlaying = true;
                    acquireWakeLock();
                    MusicPlayerManager.getInstance().updatePlaybackState(true, false);
                    MusicPlayerManager.getInstance().updateProgress(0, dur);
                    updateNotification();
                    startProgressUpdates();
                } else {
                    isPlaying = false;
                    MusicPlayerManager.getInstance().updatePlaybackState(false, false);
                    MusicPlayerManager.getInstance().updateProgress(0, dur);
                    updateNotification();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error starting MediaPlayer onPrepared", e);
            }
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            Log.i(TAG, "Native MediaPlayer completed track");
            isPlaying = false;
            isPrepared = false;
            isPreparing = false;
            releaseWakeLock();
            stopProgressUpdates();
            if (currentTrack != null) {
                com.zez.app.music.util.MusicRecommendationEngine.recordTrackPlayed(getApplicationContext(), currentTrack, true);
            }
            MusicPlayerManager.getInstance().updatePlaybackState(false, false);
            MusicPlayerManager.getInstance().next();
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            isPreparing = false;
            isPrepared = false;
            Log.w(TAG, "Native MediaPlayer error what=" + what + " extra=" + extra);
            if (currentVideoId != null && !hasRetriedWithFallback) {
                hasRetriedWithFallback = true;
                Log.i(TAG, "Retrying with universal AAC fallback stream for " + currentVideoId);
                new Thread(() -> {
                    String fallbackUrl = YouTubeApiHelper.getInstance().getFallbackStreamUrlSync(currentVideoId);
                    if (fallbackUrl != null && !fallbackUrl.isEmpty()) {
                        handler.post(() -> {
                            try {
                                initMediaPlayer();
                                mediaPlayer.reset();
                                Map<String, String> headers = new HashMap<>();
                                headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15");
                                headers.put("Origin", "https://www.youtube.com");
                                headers.put("Referer", "https://www.youtube.com/");
                                headers.put("Range", "bytes=0-");
                                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(fallbackUrl), headers);
                                isDataSourceSet = true;
                                mediaPlayer.prepareAsync();
                            } catch (Exception e) {
                                Log.e(TAG, "Fallback playback failed", e);
                                isPlaying = false;
                                stopProgressUpdates();
                                MusicPlayerManager.getInstance().updatePlaybackState(false, false);
                            }
                        });
                    } else {
                        handler.post(() -> {
                            isPlaying = false;
                            stopProgressUpdates();
                            MusicPlayerManager.getInstance().updatePlaybackState(false, false);
                        });
                    }
                }).start();
                return true;
            }
            isPlaying = false;
            stopProgressUpdates();
            MusicPlayerManager.getInstance().updatePlaybackState(false, false);
            return true;
        });
    }

    public static WebView getActiveWebView() {
        return activeWebView;
    }

    public static void setActiveWebView(WebView wv) {
        activeWebView = wv;
    }

    private final BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                pausePlayback();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "QuackyMusic:WakeLock");
            wakeLock.setReferenceCounted(false);
        }

        createNotificationChannel();

        try {
            mediaSession = new MediaSessionCompat(this, "QuackyMusicSession");
            mediaSession.setCallback(new MediaSessionCompat.Callback() {
                @Override
                public void onPlay() { resumePlayback(); }
                @Override
                public void onPause() { pausePlayback(); }
                @Override
                public void onSkipToNext() { MusicPlayerManager.getInstance().next(); }
                @Override
                public void onSkipToPrevious() { MusicPlayerManager.getInstance().previous(); }
                @Override
                public void onSeekTo(long pos) { seekTo((int) (pos / 1000)); }
                @Override
                public void onStop() { stopPlayback(); stopSelf(); }
            });
            mediaSession.setActive(true);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing MediaSessionCompat", e);
        }

        try {
            registerReceiver(noisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        } catch (Exception e) {
            Log.e(TAG, "Error registering noisy receiver", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background playback controls for Quacky Music");
            channel.setShowBadge(false);
            channel.setSound(null, null);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        switch (action) {
            case ACTION_PLAY_TRACK:
                MusicTrack track = (MusicTrack) intent.getSerializableExtra(EXTRA_TRACK);
                if (track != null) {
                    playTrack(track);
                }
                break;
            case ACTION_RESUME:
                resumePlayback();
                break;
            case ACTION_PAUSE:
                pausePlayback();
                break;
            case ACTION_TOGGLE:
                if (isPlaying) pausePlayback(); else resumePlayback();
                break;
            case ACTION_NEXT:
                MusicPlayerManager.getInstance().next();
                break;
            case ACTION_PREV:
                MusicPlayerManager.getInstance().previous();
                break;
            case ACTION_SEEK:
                int seconds = intent.getIntExtra(EXTRA_SEEK_SECONDS, 0);
                seekTo(seconds);
                break;
            case ACTION_STOP:
                stopPlayback();
                stopSelf();
                break;
        }

        return START_NOT_STICKY;
    }

    private void playTrack(MusicTrack track) {
        this.currentTrack = track;
        this.currentVideoId = track.getId();
        this.isPlaying = false;
        this.isPreparing = true;
        this.isPrepared = false;
        this.playWhenReady = true;
        this.hasRetriedWithFallback = false;

        stopProgressUpdates();
        requestAudioFocus();
        loadThumbnailBitmap(track.getThumbnailUrl());
        updateNotification();

        // Immediately update player manager with track duration so UI shows total length right away
        int initialDur = parseDurationToSeconds(track.getDuration());
        MusicPlayerManager.getInstance().updatePlaybackState(false, true);
        MusicPlayerManager.getInstance().updateProgress(0, initialDur);

        // 1. Check if track is already downloaded locally for offline playback
        if (track.getLocalFilePath() != null) {
            String lp = track.getLocalFilePath();
            if (lp.startsWith("content://")) {
                if (playLocalContentUri(Uri.parse(lp))) {
                    return;
                }
            } else {
                File localFile = new File(lp);
                if (!localFile.exists() || localFile.length() == 0) {
                    File altFile = MusicDownloadHelper.getDestinationFile(this, localFile.getName());
                    if (altFile.exists() && altFile.length() > 0) {
                        localFile = altFile;
                    }
                }
                if (localFile.exists() && localFile.length() > 0) {
                    playDownloadedTrack(localFile);
                    return;
                }
            }
        }

        // 2. Play online stream directly on-device with native MediaPlayer (client-side pure audio, loud, clear, zero ads)
        playOnlineStream(track.getId());
    }

    private boolean playLocalContentUri(Uri uri) {
        try {
            initMediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            isDataSourceSet = true;
            mediaPlayer.prepareAsync();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to play local content URI: " + e.getMessage(), e);
            return false;
        }
    }

    private void playDownloadedTrack(File localFile) {
        try {
            Log.i(TAG, "Playing local downloaded audio via FileDescriptor: " + localFile.getAbsolutePath());
            initMediaPlayer();
            mediaPlayer.reset();
            try (FileInputStream fis = new FileInputStream(localFile)) {
                mediaPlayer.setDataSource(fis.getFD(), 0, localFile.length());
            }
            isDataSourceSet = true;
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "Failed to play local file via FileDescriptor: " + e.getMessage(), e);
            if (currentVideoId != null) {
                playOnlineStream(currentVideoId);
            }
        }
    }

    private void playOnlineStream(String videoId) {
        Log.i(TAG, "Resolving native audio stream for videoId: " + videoId);
        YouTubeApiHelper.getInstance().extractAudioStream(videoId, new YouTubeApiHelper.Callback<String>() {
            @Override
            public void onSuccess(String streamUrl) {
                if (currentVideoId != null && !currentVideoId.equals(videoId)) {
                    Log.d(TAG, "Track changed before stream resolved, discarding " + videoId);
                    return;
                }
                try {
                    Log.i(TAG, "Direct stream resolved! Setting native MediaPlayer data source: " + streamUrl.substring(0, Math.min(streamUrl.length(), 80)));
                    // Register with download manager for instant downloads
                    MusicDownloadManager.getInstance().registerCapturedStreamUrl(videoId, streamUrl);

                    initMediaPlayer();
                    mediaPlayer.reset();
                    Map<String, String> headers = new HashMap<>();
                    headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15");
                    headers.put("Origin", "https://www.youtube.com");
                    headers.put("Referer", "https://www.youtube.com/");
                    headers.put("Range", "bytes=0-");
                    mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(streamUrl), headers);
                    isDataSourceSet = true;
                    mediaPlayer.prepareAsync();
                } catch (Exception e) {
                    Log.e(TAG, "Error setting native stream dataSource: " + e.getMessage(), e);
                    MusicPlayerManager.getInstance().updatePlaybackState(false, false);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to extract audio stream for " + videoId + ": " + e.getMessage());
                MusicPlayerManager.getInstance().updatePlaybackState(false, false);
                MusicPlayerManager.getInstance().notifyError("Unable to stream: " + (e.getMessage() != null ? e.getMessage() : "Network error"));
            }
        });
    }

    private void resumePlayback() {
        playWhenReady = true;
        requestAudioFocus();
        if (mediaPlayer != null && isPrepared) {
            try {
                mediaPlayer.start();
                isPlaying = true;
                acquireWakeLock();
                MusicPlayerManager.getInstance().updatePlaybackState(true, false);
                updateNotification();
                startProgressUpdates();
            } catch (Exception e) {
                Log.e(TAG, "Error resuming native player", e);
            }
        } else if (isPreparing) {
            MusicPlayerManager.getInstance().updatePlaybackState(true, true);
            updateNotification();
        } else if (currentTrack != null) {
            playTrack(currentTrack);
        }
    }

    private void pausePlayback() {
        playWhenReady = false;
        resumeOnFocusGain = false;
        if (mediaPlayer != null && isPrepared) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error pausing native player", e);
            }
        }
        isPlaying = false;
        releaseWakeLock();
        stopProgressUpdates();
        MusicPlayerManager.getInstance().updatePlaybackState(false, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH);
        } else {
            stopForeground(false);
        }
        updateNotification();
    }

    private void seekTo(int seconds) {
        if (mediaPlayer != null && isPrepared) {
            try {
                mediaPlayer.seekTo(seconds * 1000);
            } catch (Exception e) {
                Log.e(TAG, "Error seeking native player", e);
            }
        }
    }

    private void stopPlayback() {
        playWhenReady = false;
        isPreparing = false;
        isPrepared = false;
        resumeOnFocusGain = false;
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        stopProgressUpdates();
        isPlaying = false;
        releaseWakeLock();
        abandonAudioFocus();
        MusicPlayerManager.getInstance().clearFromServiceStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(NOTIFICATION_ID);
        }
    }

    private void loadThumbnailBitmap(String url) {
        currentThumbnailBitmap = null;
        if (url == null || url.isEmpty()) return;

        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>(256, 256) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        currentThumbnailBitmap = resource;
                        updateNotification();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void updateNotification() {
        if (currentTrack == null) return;

        Intent contentIntent = new Intent(this, MusicActivity.class);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent prevIntent = new Intent(this, MusicPlayerService.class).setAction(ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getService(
                this, 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent toggleIntent = new Intent(this, MusicPlayerService.class).setAction(ACTION_TOGGLE);
        PendingIntent togglePendingIntent = PendingIntent.getService(
                this, 2, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent nextIntent = new Intent(this, MusicPlayerService.class).setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(
                this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(this, MusicPlayerService.class).setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int playPauseIcon = isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        String playPauseTitle = isPlaying ? "Pause" : "Play";

        if (mediaSession != null) {
            long actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SEEK_TO;
            int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            long posMs = (mediaPlayer != null)
                    ? mediaPlayer.getCurrentPosition()
                    : (lastReportedPositionSeconds * 1000L);

            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setActions(actions)
                    .setState(state, posMs, isPlaying ? 1.0f : 0.0f)
                    .build());

            // Set MediaSession metadata for System Media Controls (Android 13/14/15/16)
            if (currentTrack != null) {
                long durationMs = parseDurationToSeconds(currentTrack.getDuration()) * 1000L;
                MediaMetadataCompat.Builder metaBuilder = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack.getTitle())
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrack.getArtist())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Quacky")
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs);
                if (currentThumbnailBitmap != null) {
                    metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, currentThumbnailBitmap);
                    metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, currentThumbnailBitmap);
                }
                mediaSession.setMetadata(metaBuilder.build());
            }
        }

        MediaStyle mediaStyle = new MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopPendingIntent);
        if (mediaSession != null) {
            mediaStyle.setMediaSession(mediaSession.getSessionToken());
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(currentTrack.getTitle())
                .setContentText(currentTrack.getArtist())
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(stopPendingIntent)
                .setOngoing(isPlaying)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(mediaStyle)
                .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
                .addAction(playPauseIcon, playPauseTitle, togglePendingIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", stopPendingIntent);

        if (currentThumbnailBitmap != null) {
            builder.setLargeIcon(currentThumbnailBitmap);
        }

        Notification notification = builder.build();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (isPlaying) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH);
            } else {
                stopForeground(false);
            }
            if (nm != null) {
                nm.notify(NOTIFICATION_ID, notification);
            }
        }
    }

    private void requestAudioFocus() {
        if (audioManager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attributes)
                    .setOnAudioFocusChangeListener(this, handler)
                    .build();
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    private void abandonAudioFocus() {
        if (audioManager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(this);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            resumeOnFocusGain = false;
            pausePlayback();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            if (isPlaying) {
                resumeOnFocusGain = true;
                pausePlayback();
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            if (resumeOnFocusGain) {
                resumeOnFocusGain = false;
                resumePlayback();
            }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (!isPlaying) {
            stopPlayback();
            stopSelf();
        }
    }

    private void acquireWakeLock() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            try {
                wakeLock.acquire(10 * 60 * 1000L); // 10 minutes maximum per wake
            } catch (Exception ignored) {}
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            try {
                wakeLock.release();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayback();
        try {
            unregisterReceiver(noisyReceiver);
        } catch (Exception ignored) {}
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
