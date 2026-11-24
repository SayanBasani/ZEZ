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
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.ViewGroup;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zez.app.R;
import com.zez.app.music.MusicActivity;
import com.zez.app.music.model.MusicTrack;
import com.zez.app.util.ChatApiClient;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
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

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying && currentEngine == ENGINE_NATIVE_AUDIO) {
                try {
                    int pos = mediaPlayer.getCurrentPosition() / 1000;
                    int dur = mediaPlayer.getDuration() / 1000;
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

    private void initMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {}
        }
        mediaPlayer = new MediaPlayer();
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
            Log.w(TAG, "Native MediaPlayer error what=" + what + " extra=" + extra);
            stopProgressUpdates();
            if (currentVideoId != null && !fallbackTriggeredForCurrent) {
                fallbackTriggeredForCurrent = true;
                Log.i(TAG, "Falling back to Direct Mobile engine after native player error");
                playViaDirectMobileEngine(currentVideoId);
            }
            return true;
        });
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

            webView.setWebChromeClient(new WebChromeClient());
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if (currentEngine == ENGINE_DIRECT_MOBILE) {
                        injectDirectMobileControlScript();
                    }
                }
            });

            webView.addJavascriptInterface(new MusicBridge(), "MusicBridge");

            String originUrl = "https://" + getPackageName();
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
                    "    playerVars:{'autoplay':1,'controls':1,'fs':0,'rel':0,'modestbranding':1,'playsinline':1,'origin':'" + originUrl + "'}," +
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

            webView.loadDataWithBaseURL(originUrl, html, "text/html", "UTF-8", null);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing webView player", e);
        }
    }

    private void playViaDirectMobileEngine(String videoId) {
        if (videoId == null || webView == null) return;
        currentEngine = ENGINE_DIRECT_MOBILE;
        isPlayerReady = false;
        isPlaying = false;
        Log.i(TAG, "Activating Direct Mobile YouTube Playback Engine for video: " + videoId);

        if (watchdogRunnable != null) {
            handler.removeCallbacks(watchdogRunnable);
            watchdogRunnable = null;
        }

        webView.post(() -> {
            WebSettings settings = webView.getSettings();
            settings.setUserAgentString("Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
            webView.loadUrl("https://m.youtube.com/watch?v=" + videoId);
        });

        watchdogRunnable = () -> {
            if (!isPlaying) {
                Log.w(TAG, "Watchdog: Direct mobile engine taking long for " + videoId + ", re-triggering script...");
                injectDirectMobileControlScript();
            }
        };
        handler.postDelayed(watchdogRunnable, 5000);
    }

    private void injectDirectMobileControlScript() {
        if (webView == null) return;
        String js = "javascript:(function() {" +
                "  var s = document.getElementById('zez-mobile-style');" +
                "  if (!s) {" +
                "    s = document.createElement('style');" +
                "    s.id = 'zez-mobile-style';" +
                "    s.innerHTML = 'header, ytm-pivot-bar-renderer, ytm-item-section-renderer, #comments, .watch-below-the-player, ytm-watch-card-entry { display: none !important; } html, body { background: #000 !important; margin: 0; padding: 0; overflow: hidden; } .player-container, #player-container-id { width: 100vw !important; height: 100vh !important; }';" +
                "    document.head.appendChild(s);" +
                "  }" +
                "  function hook() {" +
                "    var dismiss = document.querySelector('button[aria-label*=\"Dismiss\"], button[aria-label*=\"Reject\"], .yt-spec-button-shape-next--tonal');" +
                "    if (dismiss) { try { dismiss.click(); } catch(e){} }" +
                "    var v = document.querySelector('video');" +
                "    if (v) {" +
                "      v.muted = false;" +
                "      v.play().catch(function(e) {" +
                "        var playBtn = document.querySelector('.ytp-large-play-button, button[aria-label*=\"Play\"], .player-control-play');" +
                "        if (playBtn) { try { playBtn.click(); } catch(e2){} }" +
                "      });" +
                "      v.ontimeupdate = function() {" +
                "        if (window.MusicBridge && v.duration) {" +
                "          window.MusicBridge.onProgress(v.currentTime, v.duration);" +
                "        }" +
                "        var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytp-skip-ad-button');" +
                "        if (skipBtn) { try { skipBtn.click(); } catch(e3){} }" +
                "      };" +
                "      v.onplay = function() { if (window.MusicBridge) window.MusicBridge.onStateChange(1); };" +
                "      v.onpause = function() { if (window.MusicBridge) window.MusicBridge.onStateChange(2); };" +
                "      v.onended = function() {" +
                "        var isAd = document.querySelector('.ad-showing, .ad-interrupting');" +
                "        if (isAd) return;" +
                "        if (window.MusicBridge) window.MusicBridge.onStateChange(0);" +
                "      };" +
                "      v.onwaiting = function() { if (window.MusicBridge) window.MusicBridge.onStateChange(3); };" +
                "      if (window.MusicBridge) {" +
                "        window.MusicBridge.onReady();" +
                "        if (!v.paused) window.MusicBridge.onStateChange(1);" +
                "      }" +
                "      return true;" +
                "    }" +
                "    return false;" +
                "  }" +
                "  if (!hook()) {" +
                "    var tries = 0;" +
                "    var timer = setInterval(function() {" +
                "      tries++;" +
                "      if (hook() || tries > 40) clearInterval(timer);" +
                "    }, 300);" +
                "  }" +
                "})();";

        webView.evaluateJavascript(js, null);
    }

    private class MusicBridge {
        @JavascriptInterface
        public void onReady() {
            handler.post(() -> {
                isPlayerReady = true;
                if (watchdogRunnable != null) {
                    handler.removeCallbacks(watchdogRunnable);
                    watchdogRunnable = null;
                }
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
                    if (watchdogRunnable != null) {
                        handler.removeCallbacks(watchdogRunnable);
                        watchdogRunnable = null;
                    }
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
                Log.w(TAG, "YouTube Player Error: " + code);
                // Error 150 / 152 / 101 indicate YouTube embed embedding restrictions
                if (!fallbackTriggeredForCurrent && currentVideoId != null) {
                    fallbackTriggeredForCurrent = true;
                    Log.i(TAG, "Auto-recovering via direct mobile playback engine...");
                    playViaDirectMobileEngine(currentVideoId);
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
        this.isPlayerReady = false;
        this.fallbackTriggeredForCurrent = false;

        if (watchdogRunnable != null) {
            handler.removeCallbacks(watchdogRunnable);
            watchdogRunnable = null;
        }

        stopProgressUpdates();
        requestAudioFocus();
        loadThumbnailBitmap(track.getThumbnailUrl());
        updateNotification();

        // Immediately update player manager so UI resets to 0:00 and buffering
        MusicPlayerManager.getInstance().updatePlaybackState(false, true);
        MusicPlayerManager.getInstance().updateProgress(0, 0);

        if (MusicPlayerManager.getInstance().isVideoMode()) {
            if (mediaPlayer != null) {
                try { mediaPlayer.reset(); } catch (Exception ignored) {}
            }
            currentEngine = ENGINE_DIRECT_MOBILE;
            loadTrackVideo(track.getId());
            return;
        }

        currentEngine = ENGINE_NATIVE_AUDIO;
        if (webView != null) {
            webView.evaluateJavascript("var v=document.querySelector('video');if(v){v.pause();v.src='';}", null);
        }

        initMediaPlayer();

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
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(localFile.getAbsolutePath());
                    mediaPlayer.prepareAsync();
                    return;
                } catch (Exception e) {
                    Log.w(TAG, "Failed to play local file, falling back to stream: " + e.getMessage());
                }
            }
        }

        // 2. Resolve stream URL from backend
        resolveAndPlayNativeStream(track.getId());
    }

    private void resolveAndPlayNativeStream(String videoId) {
        String baseUrl = ChatApiClient.getBaseUrl(this);
        String url = baseUrl + "/api/music/resolve?videoId=" + videoId;
        Log.i(TAG, "Resolving audio stream from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        ChatApiClient.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.w(TAG, "Stream resolve network failed: " + e.getMessage());
                handler.post(() -> {
                    if (!fallbackTriggeredForCurrent) {
                        fallbackTriggeredForCurrent = true;
                        playViaDirectMobileEngine(videoId);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "Stream resolve unsuccessful code=" + response.code());
                    handler.post(() -> {
                        if (!fallbackTriggeredForCurrent) {
                            fallbackTriggeredForCurrent = true;
                            playViaDirectMobileEngine(videoId);
                        }
                    });
                    return;
                }

                try {
                    String jsonStr = response.body().string();
                    JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
                    if (json.has("success") && json.get("success").getAsBoolean()) {
                        String streamUrl = baseUrl + "/api/music/stream?videoId=" + videoId;
                        handler.post(() -> {
                            if (currentTrack != null && videoId.equals(currentTrack.getId()) && currentEngine == ENGINE_NATIVE_AUDIO) {
                                playAudioUrlNatively(streamUrl);
                            }
                        });
                    } else {
                        handler.post(() -> {
                            if (!fallbackTriggeredForCurrent) {
                                fallbackTriggeredForCurrent = true;
                                playViaDirectMobileEngine(videoId);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing resolve response", e);
                    handler.post(() -> {
                        if (!fallbackTriggeredForCurrent) {
                            fallbackTriggeredForCurrent = true;
                            playViaDirectMobileEngine(videoId);
                        }
                    });
                }
            }
        });
    }

    private void playAudioUrlNatively(String audioUrl) {
        try {
            if (mediaPlayer == null) initMediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "Error playing audio URL natively", e);
            if (!fallbackTriggeredForCurrent && currentVideoId != null) {
                fallbackTriggeredForCurrent = true;
                playViaDirectMobileEngine(currentVideoId);
            }
        }
    }

    private void loadTrackVideo(String videoId) {
        if (webView == null) return;
        this.currentVideoId = videoId;

        if (watchdogRunnable != null) {
            handler.removeCallbacks(watchdogRunnable);
            watchdogRunnable = null;
        }

        // Clean up previous playback immediately
        webView.evaluateJavascript(
                "(function(){ " +
                "  var v = document.querySelector('video'); if (v) { try { v.pause(); v.src = ''; } catch(e){} } " +
                "  if (typeof player !== 'undefined' && player && player.stopVideo) { try { player.stopVideo(); } catch(e){} } " +
                "})();", null);

        if (currentEngine == ENGINE_DIRECT_MOBILE) {
            webView.post(() -> webView.loadUrl("https://m.youtube.com/watch?v=" + videoId));

            watchdogRunnable = () -> {
                if (!isPlaying) {
                    Log.w(TAG, "Watchdog: Direct mobile engine taking long for " + videoId + ", re-triggering script...");
                    injectDirectMobileControlScript();
                }
            };
            handler.postDelayed(watchdogRunnable, 5000);
        } else {
            if (isPlayerReady) {
                webView.evaluateJavascript("loadVideo('" + videoId + "');", null);
            } else {
                pendingVideoId = videoId;
            }

            // Set a 4-second watchdog timer: if iframe fails to start or gets blocked silently
            watchdogRunnable = () -> {
                if (!isPlaying && !fallbackTriggeredForCurrent) {
                    Log.w(TAG, "Watchdog timeout on IFrame player for " + videoId + ". Auto-switching to direct mobile engine.");
                    fallbackTriggeredForCurrent = true;
                    playViaDirectMobileEngine(videoId);
                }
            };
            handler.postDelayed(watchdogRunnable, 4000);
        }
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
                if (currentEngine == ENGINE_DIRECT_MOBILE) {
                    webView.evaluateJavascript("var v=document.querySelector('video'); if(v) v.play();", null);
                } else {
                    webView.evaluateJavascript("playVideo();", null);
                }
            }
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
                if (currentEngine == ENGINE_DIRECT_MOBILE) {
                    webView.evaluateJavascript("var v=document.querySelector('video'); if(v) v.pause();", null);
                } else {
                    webView.evaluateJavascript("pauseVideo();", null);
                }
            }
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
                if (currentEngine == ENGINE_DIRECT_MOBILE) {
                    webView.evaluateJavascript("var v=document.querySelector('video'); if(v) v.currentTime = " + seconds + ";", null);
                } else {
                    webView.evaluateJavascript("seekTo(" + seconds + ");", null);
                }
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
        if (webView != null) {
            if (currentEngine == ENGINE_DIRECT_MOBILE) {
                webView.evaluateJavascript("var v=document.querySelector('video'); if(v) v.pause();", null);
            } else {
                webView.evaluateJavascript("pauseVideo();", null);
            }
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
        stopProgressUpdates();
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
