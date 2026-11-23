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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.zez.app.MainActivity;
import com.zez.app.R;
import com.zez.app.music.MusicActivity;
import com.zez.app.music.model.MusicTrack;

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

    private static final String CHANNEL_ID = "zez_music_playback_channel";
    private static final int NOTIFICATION_ID = 4040;

    private WebView webView;
    private boolean isPlayerReady = false;
    private String pendingVideoId = null;
    private MusicTrack currentTrack;
    private boolean isPlaying = false;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private PowerManager.WakeLock wakeLock;
    private Handler handler;
    private Bitmap currentThumbnailBitmap;

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
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ZezMusic:WakeLock");
            wakeLock.setReferenceCounted(false);
        }

        createNotificationChannel();
        initWebView();

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
            channel.setDescription("Background playback controls for YMusic");
            channel.setShowBadge(false);
            channel.setSound(null, null);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private void initWebView() {
        try {
            webView = new WebView(this);
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setMediaPlaybackRequiresUserGesture(false);
            settings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");

            webView.setWebChromeClient(new WebChromeClient());
            webView.setWebViewClient(new WebViewClient());
            webView.addJavascriptInterface(new MusicBridge(), "MusicBridge");

            String html = "<!DOCTYPE html><html><head>" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "<style>html,body{margin:0;padding:0;width:100%;height:100%;background:#000;overflow:hidden;}#player{width:100%;height:100%;}</style>" +
                    "</head><body><div id=\"player\"></div><script>" +
                    "var tag=document.createElement('script');tag.src='https://www.youtube.com/iframe_api';" +
                    "var firstScriptTag=document.getElementsByTagName('script')[0];firstScriptTag.parentNode.insertBefore(tag,firstScriptTag);" +
                    "var player=null,isReady=false,pendingId=null;" +
                    "function onYouTubeIframeAPIReady(){" +
                    "  player=new YT.Player('player',{" +
                    "    height:'100%',width:'100%'," +
                    "    playerVars:{'autoplay':1,'controls':0,'fs':0,'rel':0,'modestbranding':1,'playsinline':1,'origin':'https://www.youtube.com'}," +
                    "    events:{'onReady':onPlayerReady,'onStateChange':onPlayerStateChange,'onError':onPlayerError}" +
                    "  });" +
                    "}" +
                    "function onPlayerReady(e){isReady=true;window.MusicBridge.onReady();if(pendingId){loadVideo(pendingId);pendingId=null;}}" +
                    "function onPlayerStateChange(e){window.MusicBridge.onStateChange(e.data);}" +
                    "function onPlayerError(e){window.MusicBridge.onError(e.data);}" +
                    "function loadVideo(id){if(isReady&&player&&player.loadVideoById){player.loadVideoById(id);}else{pendingId=id;}}" +
                    "function playVideo(){if(isReady&&player)player.playVideo();}" +
                    "function pauseVideo(){if(isReady&&player)player.pauseVideo();}" +
                    "function seekTo(s){if(isReady&&player)player.seekTo(s,true);}" +
                    "function getDuration(){return(isReady&&player&&player.getDuration)?player.getDuration():0;}" +
                    "function getCurrentTime(){return(isReady&&player&&player.getCurrentTime)?player.getCurrentTime():0;}" +
                    "setInterval(function(){if(isReady&&player&&player.getCurrentTime&&player.getPlayerState()===1){window.MusicBridge.onProgress(player.getCurrentTime(),player.getDuration());}},500);" +
                    "</script></body></html>";

            webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing webView player", e);
        }
    }

    private class MusicBridge {
        @JavascriptInterface
        public void onReady() {
            handler.post(() -> {
                isPlayerReady = true;
                if (pendingVideoId != null) {
                    loadTrackVideo(pendingVideoId);
                    pendingVideoId = null;
                }
            });
        }

        @JavascriptInterface
        public void onStateChange(int state) {
            handler.post(() -> {
                // -1=unstarted, 0=ended, 1=playing, 2=paused, 3=buffering, 5=cued
                if (state == 1) { // PLAYING
                    isPlaying = true;
                    acquireWakeLock();
                    MusicPlayerManager.getInstance().updatePlaybackState(true, false);
                    updateNotification();
                } else if (state == 2) { // PAUSED
                    isPlaying = false;
                    releaseWakeLock();
                    MusicPlayerManager.getInstance().updatePlaybackState(false, false);
                    updateNotification();
                } else if (state == 3) { // BUFFERING
                    MusicPlayerManager.getInstance().updatePlaybackState(isPlaying, true);
                } else if (state == 0) { // ENDED
                    isPlaying = false;
                    releaseWakeLock();
                    MusicPlayerManager.getInstance().updatePlaybackState(false, false);
                    MusicPlayerManager.getInstance().next();
                }
            });
        }

        @JavascriptInterface
        public void onProgress(float current, float duration) {
            handler.post(() -> {
                MusicPlayerManager.getInstance().updateProgress((int) current, (int) duration);
            });
        }

        @JavascriptInterface
        public void onError(int code) {
            handler.post(() -> {
                Log.e(TAG, "YouTube Player Error: " + code);
                MusicPlayerManager.getInstance().notifyError("Player error code: " + code);
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
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

        return START_STICKY;
    }

    private void playTrack(MusicTrack track) {
        this.currentTrack = track;
        requestAudioFocus();
        loadTrackVideo(track.getId());
        loadThumbnailBitmap(track.getThumbnailUrl());
        updateNotification();
    }

    private void loadTrackVideo(String videoId) {
        if (webView == null) return;
        if (isPlayerReady) {
            webView.evaluateJavascript("loadVideo('" + videoId + "');", null);
        } else {
            pendingVideoId = videoId;
        }
    }

    private void resumePlayback() {
        requestAudioFocus();
        if (webView != null) {
            webView.evaluateJavascript("playVideo();", null);
        }
        isPlaying = true;
        acquireWakeLock();
        MusicPlayerManager.getInstance().updatePlaybackState(true, false);
        updateNotification();
    }

    private void pausePlayback() {
        if (webView != null) {
            webView.evaluateJavascript("pauseVideo();", null);
        }
        isPlaying = false;
        releaseWakeLock();
        MusicPlayerManager.getInstance().updatePlaybackState(false, false);
        updateNotification();
    }

    private void seekTo(int seconds) {
        if (webView != null) {
            webView.evaluateJavascript("seekTo(" + seconds + ");", null);
        }
    }

    private void stopPlayback() {
        if (webView != null) {
            webView.evaluateJavascript("pauseVideo();", null);
        }
        isPlaying = false;
        releaseWakeLock();
        abandonAudioFocus();
        MusicPlayerManager.getInstance().updatePlaybackState(false, false);
        stopForeground(true);
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(currentTrack.getTitle())
                .setContentText(currentTrack.getArtist())
                .setContentIntent(contentPendingIntent)
                .setOngoing(isPlaying)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
                .addAction(playPauseIcon, playPauseTitle, togglePendingIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", stopPendingIntent);

        if (currentThumbnailBitmap != null) {
            builder.setLargeIcon(currentThumbnailBitmap);
        }

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
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
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            pausePlayback();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            if (currentTrack != null && !isPlaying) {
                resumePlayback();
            }
        }
    }

    private void acquireWakeLock() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire(120 * 60 * 1000L); // 2 hours max
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public void onDestroy() {
        stopPlayback();
        try {
            unregisterReceiver(noisyReceiver);
        } catch (Exception ignored) {}
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
