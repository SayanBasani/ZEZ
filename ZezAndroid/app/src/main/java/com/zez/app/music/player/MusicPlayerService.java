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
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zez.app.R;
import com.zez.app.music.MusicActivity;
import com.zez.app.music.download.MusicDownloadManager;
import com.zez.app.music.model.MusicTrack;
import com.zez.app.util.ChatApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    private static final int ENGINE_NATIVE_AUDIO = 0;
    private static final int ENGINE_IFRAME = 1;
    private static final int ENGINE_DIRECT_MOBILE = 2;

    private static WebView activeWebView;

    private MediaPlayer mediaPlayer;
    private MediaPlayer silentPlayer;
    private MediaSessionCompat mediaSession;
    private WebView webView;
    private Handler handler;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private PowerManager.WakeLock wakeLock;

    private MusicTrack currentTrack;
    private String currentVideoId;
    private Bitmap currentThumbnailBitmap;
    private boolean isPlaying = false;
    private boolean isPlayerReady = false;
    private String pendingVideoId = null;

    private int currentEngine = ENGINE_NATIVE_AUDIO;
    private boolean fallbackTriggeredForCurrent = false;
    private Runnable watchdogRunnable = null;

    private String currentDirectAudioUrl = null;
    private String currentBackendStreamUrl = null;
    private boolean triedBackendStream = false;
    private boolean isDataSourceSet = false;

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
            if (mediaPlayer != null && isPlaying && currentEngine == ENGINE_NATIVE_AUDIO) {
                try {
                    int pos = mediaPlayer.getCurrentPosition() / 1000;
                    int dur = mediaPlayer.getDuration() / 1000;
                    if (dur <= 0 && currentTrack != null) {
                        dur = parseDurationToSeconds(currentTrack.getDuration());
                    }
                    MusicPlayerManager.getInstance().updateProgress(pos, dur);
                } catch (Exception ignored) {}
                handler.postDelayed(this, 500);
            }
        }
    };

    private void startProgressUpdates() {
        handler.removeCallbacks(progressRunnable);
        handler.post(progressRunnable);
    }

    private void stopProgressUpdates() {
        handler.removeCallbacks(progressRunnable);
    }

    private void startSilentAudioKeepalive() {
        if (silentPlayer != null) return;
        try {
            File silentFile = new File(getCacheDir(), "keepalive_silence.wav");
            if (!silentFile.exists() || silentFile.length() == 0) {
                generateSilentWav(silentFile);
            }
            silentPlayer = new MediaPlayer();
            silentPlayer.setDataSource(silentFile.getAbsolutePath());
            silentPlayer.setLooping(true);
            silentPlayer.setVolume(0.001f, 0.001f);
            silentPlayer.prepare();
            silentPlayer.start();
            Log.i(TAG, "Silent audio keepalive started to prevent OS sleep");
        } catch (Exception e) {
            Log.w(TAG, "Could not start silent keepalive: " + e.getMessage());
        }
    }

    private void stopSilentAudioKeepalive() {
        if (silentPlayer != null) {
            try {
                silentPlayer.stop();
                silentPlayer.release();
            } catch (Exception ignored) {}
            silentPlayer = null;
        }
    }

    private void generateSilentWav(File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            int sampleRate = 44100;
            int numSamples = sampleRate; // 1 second
            int dataSize = numSamples * 2; // 16-bit mono
            int totalSize = 36 + dataSize;

            byte[] header = new byte[44];
            header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
            header[4] = (byte)(totalSize & 0xff);
            header[5] = (byte)((totalSize >> 8) & 0xff);
            header[6] = (byte)((totalSize >> 16) & 0xff);
            header[7] = (byte)((totalSize >> 24) & 0xff);
            header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
            header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
            header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0;
            header[20] = 1; header[21] = 0; // PCM
            header[22] = 1; header[23] = 0; // Mono
            header[24] = (byte)(sampleRate & 0xff);
            header[25] = (byte)((sampleRate >> 8) & 0xff);
            header[26] = (byte)((sampleRate >> 16) & 0xff);
            header[27] = (byte)((sampleRate >> 24) & 0xff);
            int byteRate = sampleRate * 2;
            header[28] = (byte)(byteRate & 0xff);
            header[29] = (byte)((byteRate >> 8) & 0xff);
            header[30] = (byte)((byteRate >> 16) & 0xff);
            header[31] = (byte)((byteRate >> 24) & 0xff);
            header[32] = 2; header[33] = 0;
            header[34] = 16; header[35] = 0;
            header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
            header[40] = (byte)(dataSize & 0xff);
            header[41] = (byte)((dataSize >> 8) & 0xff);
            header[42] = (byte)((dataSize >> 16) & 0xff);
            header[43] = (byte)((dataSize >> 24) & 0xff);

            fos.write(header);
            byte[] silence = new byte[dataSize];
            fos.write(silence);
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate silent WAV", e);
        }
    }

    private void initMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {}
        }
        mediaPlayer = new MediaPlayer();
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
            Log.i(TAG, "Native MediaPlayer prepared, starting playback...");
            mp.start();
            isPlaying = true;
            acquireWakeLock();
            MusicPlayerManager.getInstance().updatePlaybackState(true, false);
            int dur = mp.getDuration() / 1000;
            if (dur <= 0 && currentTrack != null) {
                dur = parseDurationToSeconds(currentTrack.getDuration());
            }
            MusicPlayerManager.getInstance().updateProgress(0, dur);
            updateNotification();
            startProgressUpdates();
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            Log.i(TAG, "Native MediaPlayer completed track");
            isPlaying = false;
            releaseWakeLock();
            stopProgressUpdates();
            MusicPlayerManager.getInstance().updatePlaybackState(false, false);
            MusicPlayerManager.getInstance().next();
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.w(TAG, "Native MediaPlayer error what=" + what + " extra=" + extra + " isDataSourceSet=" + isDataSourceSet);
            if (!isDataSourceSet) {
                // Ignore state errors if dataSource hasn't been set yet
                return true;
            }
            handleNativePlayerError();
            return true;
        });
    }

    private void handleNativePlayerError() {
        stopProgressUpdates();
        if (currentVideoId != null && !fallbackTriggeredForCurrent) {
            fallbackTriggeredForCurrent = true;
            Log.i(TAG, "Local/native playback error for " + currentVideoId + ", falling back to unified player engine");
            loadTrackVideo(currentVideoId);
        }
    }

    public static WebView getActiveWebView() {
        return activeWebView;
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
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ZezMusic:WakeLock");
            wakeLock.setReferenceCounted(false);
        }

        createNotificationChannel();
        initWebView();

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
            currentEngine = ENGINE_IFRAME;
            isPlayerReady = false;
            fallbackTriggeredForCurrent = false;

            if (webView == null) {
                webView = new WebView(this);
                activeWebView = webView;
            }

            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            settings.setMediaPlaybackRequiresUserGesture(false);
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            settings.setUserAgentString("Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");

            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public Bitmap getDefaultVideoPoster() {
                    return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                }
            });

            webView.setWebViewClient(new WebViewClient() {
                @Nullable
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if (request != null && request.getUrl() != null) {
                        String url = request.getUrl().toString();
                        if (url.contains("googlevideo.com/videoplayback")) {
                            if (currentVideoId != null) {
                                MusicDownloadManager.getInstance().registerCapturedStreamUrl(currentVideoId, url);
                            }
                        }

                        // Patch Referer header for YouTube embed & API calls to eliminate Error 150/152
                        if (url.contains("youtube.com/embed/") || url.contains("youtube-nocookie.com/embed/") || url.contains("youtubei/v1/player")) {
                            try {
                                OkHttpClient client = ChatApiClient.getOkHttpClient();
                                Request.Builder reqBuilder = new Request.Builder().url(url);
                                if (request.getRequestHeaders() != null) {
                                    for (Map.Entry<String, String> entry : request.getRequestHeaders().entrySet()) {
                                        if (!entry.getKey().equalsIgnoreCase("Referer") && !entry.getKey().equalsIgnoreCase("Origin")) {
                                            reqBuilder.addHeader(entry.getKey(), entry.getValue());
                                        }
                                    }
                                }
                                reqBuilder.header("Referer", "https://www.youtube.com/");
                                reqBuilder.header("Origin", "https://www.youtube.com");
                                reqBuilder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

                                Response response = client.newCall(reqBuilder.build()).execute();
                                if (response.isSuccessful() && response.body() != null) {
                                    String contentType = response.header("Content-Type", "text/html; charset=utf-8");
                                    String mimeType = "text/html";
                                    String encoding = "utf-8";
                                    if (contentType != null) {
                                        String[] parts = contentType.split(";");
                                        mimeType = parts[0].trim();
                                        if (parts.length > 1 && parts[1].contains("charset=")) {
                                            encoding = parts[1].split("charset=")[1].trim();
                                        }
                                    }
                                    Map<String, String> respHeaders = new HashMap<>();
                                    for (String name : response.headers().names()) {
                                        if (!name.equalsIgnoreCase("x-frame-options") && !name.equalsIgnoreCase("content-security-policy")) {
                                            respHeaders.put(name, response.header(name));
                                        }
                                    }
                                    return new WebResourceResponse(mimeType, encoding, response.code(), "OK", respHeaders, response.body().byteStream());
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "shouldInterceptRequest proxy error for " + url + ": " + e.getMessage());
                            }
                        }
                    }
                    return super.shouldInterceptRequest(view, request);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    view.onResume();
                    view.resumeTimers();
                }
            });

            webView.addJavascriptInterface(new MusicBridge(), "MusicBridge");

            String html = "<!DOCTYPE html><html><head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">" +
                    "<meta name=\"referrer\" content=\"strict-origin-when-cross-origin\">" +
                    "<style>" +
                    "html,body{margin:0;padding:0;width:100%;height:100%;background:#000;overflow:hidden;}" +
                    "#player{width:100%;height:100%;position:absolute;top:0;left:0;border:none;}" +
                    ".ytp-ad-module,.ytp-ad-overlay-container,.ytp-ad-message-container,.ytp-ad-action-interstitial," +
                    ".ytp-suggested-action,.ytp-pause-overlay,.ytp-chrome-top,.ytp-watermark,.ytp-show-cards-title," +
                    ".iv-branding,.ytp-scroll-min,.ytp-endscreen-content,.ytp-ad-player-overlay," +
                    ".ytp-ad-player-overlay-flyout-cta,.ytp-paid-content-overlay{display:none!important;opacity:0!important;visibility:hidden!important;pointer-events:none!important;}" +
                    "</style>" +
                    "</head><body><div id=\"player\"></div><script>" +
                    "try{Object.defineProperty(document,'hidden',{get:function(){return false;}});Object.defineProperty(document,'visibilityState',{get:function(){return 'visible';}});window.addEventListener('visibilitychange',function(e){e.stopImmediatePropagation();},true);}catch(e){}" +
                    "var tag=document.createElement('script');tag.src='https://www.youtube.com/iframe_api';" +
                    "var firstScriptTag=document.getElementsByTagName('script')[0];firstScriptTag.parentNode.insertBefore(tag,firstScriptTag);" +
                    "var player=null,isReady=false,pendingId=null,hasStarted=false;" +
                    "function onYouTubeIframeAPIReady(){" +
                    "  player=new YT.Player('player',{" +
                    "    height:'100%',width:'100%'," +
                    "    playerVars:{'autoplay':1,'controls':1,'fs':0,'rel':0,'modestbranding':1,'playsinline':1,'origin':'https://www.youtube.com','enablejsapi':1,'iv_load_policy':3}," +
                    "    events:{'onReady':onPlayerReady,'onStateChange':onPlayerStateChange,'onError':onPlayerError}" +
                    "  });" +
                    "}" +
                    "function onPlayerReady(e){isReady=true;window.MusicBridge.onReady();if(pendingId){loadVideo(pendingId);pendingId=null;}}" +
                    "function onPlayerStateChange(e){if(e.data===1)hasStarted=true;window.MusicBridge.onStateChange(e.data);}" +
                    "function onPlayerError(e){window.MusicBridge.onError(e.data);}" +
                    "function loadVideo(id){hasStarted=false;if(isReady&&player&&player.loadVideoById){player.loadVideoById({videoId:id,startSeconds:0});}else{pendingId=id;}}" +
                    "function playVideo(){if(isReady&&player&&player.playVideo)player.playVideo();var v=document.querySelector('video');if(v)v.play().catch(function(){});}" +
                    "function pauseVideo(){if(isReady&&player&&player.pauseVideo)player.pauseVideo();var v=document.querySelector('video');if(v)v.pause();}" +
                    "function seekTo(s){if(isReady&&player&&player.seekTo)player.seekTo(s,true);}" +
                    "setInterval(function(){" +
                    "  var skipBtn=document.querySelector('.ytp-ad-skip-button,.ytp-ad-skip-button-modern,.videoAdUiSkipButton,button.ytp-ad-skip-button,.ytp-skip-ad-button');" +
                    "  if(skipBtn){try{skipBtn.click();}catch(e){}}" +
                    "  var closeAd=document.querySelector('.ytp-ad-overlay-close-button');if(closeAd){try{closeAd.click();}catch(e){}}" +
                    "  var v=document.querySelector('video');" +
                    "  var isAd=document.querySelector('.ad-showing,.ad-interrupting');" +
                    "  if(isAd&&v&&isFinite(v.duration)&&v.duration>0){v.currentTime=v.duration;}" +
                    "  if(isReady&&player&&typeof player.getCurrentTime==='function'){" +
                    "    var cur=player.getCurrentTime()||(v?v.currentTime:0);" +
                    "    var dur=player.getDuration()||(v?v.duration:0);" +
                    "    if(window.MusicBridge&&(hasStarted||cur>0)){window.MusicBridge.onProgress(cur,dur);}" +
                    "  }" +
                    "},400);" +
                    "</script></body></html>";

            webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", "https://www.youtube.com");
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
                    startSilentAudioKeepalive();
                    MusicPlayerManager.getInstance().updatePlaybackState(true, false);
                    updateNotification();
                } else if (state == 2) { // PAUSED
                    isPlaying = false;
                    releaseWakeLock();
                    stopSilentAudioKeepalive();
                    MusicPlayerManager.getInstance().updatePlaybackState(false, false);
                    updateNotification();
                } else if (state == 3) { // BUFFERING
                    MusicPlayerManager.getInstance().updatePlaybackState(isPlaying, true);
                } else if (state == 0) { // ENDED
                    isPlaying = false;
                    releaseWakeLock();
                    stopSilentAudioKeepalive();
                    MusicPlayerManager.getInstance().updatePlaybackState(false, false);
                    MusicPlayerManager.getInstance().next();
                }
            });
        }

        @JavascriptInterface
        public void onProgress(float current, float duration) {
            handler.post(() -> {
                int cur = (int) current;
                int dur = (int) duration;
                if (dur <= 0 && currentTrack != null) {
                    dur = parseDurationToSeconds(currentTrack.getDuration());
                }
                MusicPlayerManager.getInstance().updateProgress(cur, dur);
            });
        }

        @JavascriptInterface
        public void onError(int code) {
            handler.post(() -> {
                Log.w(TAG, "YouTube Player Error: " + code);
                if (currentVideoId != null && !fallbackTriggeredForCurrent) {
                    fallbackTriggeredForCurrent = true;
                    handler.postDelayed(() -> {
                        if (webView != null) {
                            webView.evaluateJavascript("loadVideo('" + currentVideoId + "');", null);
                        }
                    }, 500);
                } else {
                    MusicPlayerManager.getInstance().notifyError("Player error code: " + code);
                }
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
        this.currentVideoId = track.getId();
        this.isPlaying = false;
        this.fallbackTriggeredForCurrent = false;

        if (watchdogRunnable != null) {
            handler.removeCallbacks(watchdogRunnable);
            watchdogRunnable = null;
        }

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
            File localFile = new File(track.getLocalFilePath());
            if (!localFile.exists() || localFile.length() == 0) {
                File altFile = com.zez.app.music.download.MusicDownloadHelper.getDestinationFile(this, localFile.getName());
                if (altFile.exists() && altFile.length() > 0) {
                    localFile = altFile;
                }
            }
            if (localFile.exists() && localFile.length() > 0) {
                try {
                    Log.i(TAG, "Playing local downloaded audio: " + localFile.getAbsolutePath());
                    currentEngine = ENGINE_NATIVE_AUDIO;
                    if (webView != null) {
                        webView.evaluateJavascript("pauseVideo();", null);
                    }
                    stopSilentAudioKeepalive();
                    initMediaPlayer();
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(localFile.getAbsolutePath());
                    isDataSourceSet = true;
                    mediaPlayer.prepareAsync();
                    return;
                } catch (Exception e) {
                    Log.w(TAG, "Failed to play local file, falling back to player: " + e.getMessage());
                }
            }
        }

        // 2. Try native hardware audio streaming from backend for instant, 100% ad-free playback
        String baseUrl = ChatApiClient.getBaseUrl(this);
        String resolveUrl = baseUrl + "/api/music/resolve?videoId=" + track.getId();
        OkHttpClient client = ChatApiClient.getOkHttpClient();
        Request request = new Request.Builder().url(resolveUrl).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.w(TAG, "Backend resolve failed, falling back to web player: " + e.getMessage());
                handler.post(() -> loadTrackVideo(track.getId()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String respBody = response.body().string();
                        JsonObject json = JsonParser.parseString(respBody).getAsJsonObject();
                        if (json.has("success") && json.get("success").getAsBoolean()) {
                            String streamUrl = baseUrl + "/api/music/stream?videoId=" + track.getId();
                            handler.post(() -> {
                                if (currentTrack != null && track.getId().equals(currentTrack.getId())) {
                                    Log.i(TAG, "Streaming native audio from: " + streamUrl);
                                    playAudioUrlNatively(streamUrl);
                                }
                            });
                            return;
                        }
                    } catch (Exception ignored) {}
                }
                handler.post(() -> loadTrackVideo(track.getId()));
            }
        });
    }

    private void playAudioUrlNatively(String audioUrl) {
        try {
            currentEngine = ENGINE_NATIVE_AUDIO;
            stopSilentAudioKeepalive();
            if (webView != null) {
                webView.evaluateJavascript("pauseVideo();", null);
            }
            initMediaPlayer();
            isDataSourceSet = false;
            mediaPlayer.reset();
            try {
                mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            } catch (Exception ignored) {}

            mediaPlayer.setDataSource(audioUrl);
            isDataSourceSet = true;
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            isDataSourceSet = false;
            Log.e(TAG, "Error playing audio URL natively: " + e.getMessage());
            loadTrackVideo(currentVideoId);
        }
    }

    private void loadTrackVideo(String videoId) {
        if (webView == null) return;
        this.currentVideoId = videoId;

        if (watchdogRunnable != null) {
            handler.removeCallbacks(watchdogRunnable);
            watchdogRunnable = null;
        }

        if (isPlayerReady) {
            webView.evaluateJavascript("loadVideo('" + videoId + "');", null);
        } else {
            pendingVideoId = videoId;
        }

        startSilentAudioKeepalive();
    }

    private void resumePlayback() {
        requestAudioFocus();
        if (currentEngine == ENGINE_NATIVE_AUDIO) {
            if (mediaPlayer != null) {
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
            }
        } else {
            if (webView != null) {
                webView.evaluateJavascript("playVideo();", null);
            }
            startSilentAudioKeepalive();
            isPlaying = true;
            acquireWakeLock();
            MusicPlayerManager.getInstance().updatePlaybackState(true, false);
            updateNotification();
        }
    }

    private void pausePlayback() {
        if (currentEngine == ENGINE_NATIVE_AUDIO) {
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.pause();
                } catch (Exception e) {
                    Log.e(TAG, "Error pausing native player", e);
                }
            }
            stopProgressUpdates();
        } else {
            if (webView != null) {
                webView.evaluateJavascript("pauseVideo();", null);
            }
            stopSilentAudioKeepalive();
        }
        isPlaying = false;
        releaseWakeLock();
        MusicPlayerManager.getInstance().updatePlaybackState(false, false);
        updateNotification();
    }

    private void seekTo(int seconds) {
        if (currentEngine == ENGINE_NATIVE_AUDIO) {
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.seekTo(seconds * 1000);
                } catch (Exception e) {
                    Log.e(TAG, "Error seeking native player", e);
                }
            }
        } else {
            if (webView != null) {
                webView.evaluateJavascript("seekTo(" + seconds + ");", null);
            }
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.reset();
            } catch (Exception ignored) {}
        }
        stopProgressUpdates();
        stopSilentAudioKeepalive();
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

        if (mediaSession != null) {
            long actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SEEK_TO;
            int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            long posMs = (mediaPlayer != null) ? mediaPlayer.getCurrentPosition() : 0;
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setActions(actions)
                    .setState(state, posMs, 1.0f)
                    .build());
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFICATION_ID, notification);
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
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            pausePlayback();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            if (mediaPlayer != null) {
                try { mediaPlayer.setVolume(0.2f, 0.2f); } catch (Exception ignored) {}
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            if (mediaPlayer != null) {
                try { mediaPlayer.setVolume(1.0f, 1.0f); } catch (Exception ignored) {}
            }
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
        stopProgressUpdates();
        stopSilentAudioKeepalive();
        if (mediaSession != null) {
            try {
                mediaSession.setActive(false);
                mediaSession.release();
            } catch (Exception ignored) {}
            mediaSession = null;
        }
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        if (watchdogRunnable != null) {
            handler.removeCallbacks(watchdogRunnable);
            watchdogRunnable = null;
        }
        try {
            unregisterReceiver(noisyReceiver);
        } catch (Exception ignored) {}
        if (webView != null) {
            if (webView.getParent() instanceof ViewGroup) {
                ((ViewGroup) webView.getParent()).removeView(webView);
            }
            webView.destroy();
            webView = null;
            activeWebView = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
