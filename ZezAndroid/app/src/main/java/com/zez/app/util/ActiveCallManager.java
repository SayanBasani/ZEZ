package com.zez.app.util;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.zez.app.CallActivity;
import com.zez.app.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActiveCallManager {
    private static final String TAG = "ActiveCallManager";
    private static volatile ActiveCallManager instance;

    public interface CallStateListener {
        void onCallStateChanged(boolean isActive, boolean isMinimized);
        void onDurationTick(int durationSeconds);
        void onMuteStateChanged(boolean isMuted);
    }

    private boolean isCallActive = false;
    private boolean isMinimized = false;
    private String callId;
    private String callType = "audio";
    private boolean isCaller = false;
    private String peerId;
    private String peerName;
    private String peerPhone;
    private String peerAvatar;
    private int durationSeconds = 0;
    private boolean isMuted = false;
    private boolean isSpeakerOn = true;

    private final List<CallStateListener> listeners = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final OkHttpClient httpClient = new OkHttpClient();
    private volatile CallMediaEngine mediaEngine;
    private volatile CallMediaEngine.MediaListener activeMediaListener;

    public static synchronized ActiveCallManager getInstance() {
        if (instance == null) {
            instance = new ActiveCallManager();
        }
        return instance;
    }

    private ActiveCallManager() {}

    public synchronized void registerListener(CallStateListener listener) {
        if (listener != null) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
            listener.onCallStateChanged(isCallActive, isMinimized);
            listener.onDurationTick(durationSeconds);
            listener.onMuteStateChanged(isMuted);
        }
    }

    public synchronized void unregisterListener(CallStateListener listener) {
        listeners.remove(listener);
    }

    public synchronized void setCallStarted(String callId, String callType, boolean isCaller,
                                            String peerId, String peerName, String peerPhone, String peerAvatar) {
        this.isCallActive = true;
        this.isMinimized = false;
        this.callId = callId;
        this.callType = callType != null ? callType : "audio";
        this.isCaller = isCaller;
        this.peerId = peerId;
        this.peerName = peerName != null ? peerName : "Contact";
        this.peerPhone = peerPhone;
        this.peerAvatar = peerAvatar;
        this.durationSeconds = 0;
        this.isMuted = false;
        notifyStateChanged();
    }

    public synchronized void setDuration(int seconds) {
        this.durationSeconds = seconds;
        mainHandler.post(() -> {
            for (CallStateListener l : new ArrayList<>(listeners)) {
                l.onDurationTick(seconds);
            }
        });
    }

    public synchronized void setMinimized(boolean minimized) {
        this.isMinimized = minimized;
        notifyStateChanged();
    }

    public synchronized boolean isCallActive() {
        return isCallActive;
    }

    public synchronized boolean isMinimized() {
        return isMinimized && isCallActive;
    }

    public synchronized String getCallId() { return callId; }
    public synchronized String getCallType() { return callType; }
    public synchronized String getPeerName() { return peerName; }
    public synchronized String getPeerPhone() { return peerPhone; }
    public synchronized int getDurationSeconds() { return durationSeconds; }
    public synchronized boolean isMuted() { return isMuted; }

    public void startMedia(Context context, CallMediaEngine.MediaListener listener) {
        final CallMediaEngine toStart;
        synchronized (this) {
            this.activeMediaListener = listener;
            if (!isCallActive || callId == null || context == null) return;
            if (mediaEngine != null) return;
            String userId = ChatPreferencesHelper.getUserId(context);
            if (userId == null) return;

            toStart = new CallMediaEngine(context, callId, userId, peerId, new CallMediaEngine.MediaListener() {
                @Override
                public void onRemoteVideoFrame(byte[] jpegBytes) {
                    CallMediaEngine.MediaListener l = activeMediaListener;
                    if (l != null) l.onRemoteVideoFrame(jpegBytes);
                }

                @Override
                public void onRemoteHangup(String reason) {
                    endCall();
                    CallMediaEngine.MediaListener l = activeMediaListener;
                    if (l != null) l.onRemoteHangup(reason);
                }

                @Override
                public void onNetworkQualityChanged(int tier, int rttMs) {
                    CallMediaEngine.MediaListener l = activeMediaListener;
                    if (l != null) l.onNetworkQualityChanged(tier, rttMs);
                }
            });
            toStart.setMuted(isMuted);
            mediaEngine = toStart;
        }
        // Start audio/WebSocket outside monitor lock so UI & camera threads never stall
        toStart.start();
    }

    public void setMediaListener(CallMediaEngine.MediaListener listener) {
        this.activeMediaListener = listener;
    }

    public void enableAudioIfPermitted() {
        CallMediaEngine engine = mediaEngine;
        if (engine != null) {
            engine.enableAudioIfPermitted();
        }
    }

    public boolean canSendVideoFrame() {
        CallMediaEngine engine = mediaEngine;
        return engine != null && engine.canSendVideoFrame();
    }

    public boolean isWsMediaConnected() {
        CallMediaEngine engine = mediaEngine;
        return engine != null && engine.isWsConnected();
    }

    public int getNetworkTier() {
        CallMediaEngine engine = mediaEngine;
        return engine != null ? engine.getNetworkTier() : CallMediaEngine.TIER_GOOD;
    }

    public void sendVideoFrame(byte[] jpegBytes) {
        CallMediaEngine engine = mediaEngine;
        if (engine != null) {
            engine.sendVideoFrame(jpegBytes);
        }
    }

    public synchronized void toggleMute(Context context) {
        setMuted(context, !isMuted);
        Toast.makeText(context, isMuted ? "Microphone muted" : "Microphone unmuted", Toast.LENGTH_SHORT).show();
    }

    public synchronized void setMuted(Context context, boolean muteState) {
        isMuted = muteState;
        if (mediaEngine != null) {
            mediaEngine.setMuted(isMuted);
        }
        if (context != null) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                am.setMicrophoneMute(isMuted);
            }
        }
        mainHandler.post(() -> {
            for (CallStateListener l : new ArrayList<>(listeners)) {
                l.onMuteStateChanged(isMuted);
            }
        });
    }

    public synchronized void hangup(Context context) {
        if (!isCallActive) return;
        String curCallId = this.callId;
        String curUserId = ChatPreferencesHelper.getUserId(context);
        endCall();

        if (curCallId != null && !curCallId.isEmpty()) {
            String url = ChatApiClient.getBaseUrl(context) + "/api/call/end";
            JsonObject json = new JsonObject();
            json.addProperty("callId", curCallId);
            if (curUserId != null) json.addProperty("userId", curUserId);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request req = new Request.Builder().url(url).post(body).build();
            httpClient.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {}
                @Override
                public void onResponse(Call call, Response response) {
                    try { if (response.body() != null) response.body().close(); } catch (Exception ignored) {}
                }
            });
        }
        Toast.makeText(context, "Call ended", Toast.LENGTH_SHORT).show();
    }

    public synchronized void endCall() {
        String prevCallId = this.callId;
        this.isCallActive = false;
        this.isMinimized = false;
        this.activeMediaListener = null;
        if (mediaEngine != null) {
            mediaEngine.stop();
            mediaEngine = null;
        }
        ChatNotificationHelper.clearHandledCall(prevCallId);
        notifyStateChanged();
    }

    public void restoreCall(Context context) {
        if (!isCallActive || callId == null) return;
        this.isMinimized = false;
        notifyStateChanged();

        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(CallActivity.EXTRA_CALL_ID, callId);
        intent.putExtra(CallActivity.EXTRA_IS_CALLER, isCaller);
        intent.putExtra(CallActivity.EXTRA_CALL_TYPE, callType);
        intent.putExtra(CallActivity.EXTRA_PEER_ID, peerId);
        intent.putExtra(CallActivity.EXTRA_PEER_NAME, peerName);
        intent.putExtra(CallActivity.EXTRA_PEER_AVATAR, peerAvatar);
        intent.putExtra(CallActivity.EXTRA_PEER_PHONE, peerPhone);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    private void notifyStateChanged() {
        mainHandler.post(() -> {
            for (CallStateListener l : new ArrayList<>(listeners)) {
                l.onCallStateChanged(isCallActive, isMinimized);
            }
        });
    }

    /**
     * Binds a view_active_call_banner view to automatically update with call state.
     */
    public CallStateListener bindBanner(View bannerView, Context context) {
        if (bannerView == null) return null;

        ImageView ivIcon = bannerView.findViewById(R.id.iv_banner_call_icon);
        TextView tvName = bannerView.findViewById(R.id.tv_banner_peer_name);
        TextView tvDuration = bannerView.findViewById(R.id.tv_banner_call_duration);
        ImageButton btnMute = bannerView.findViewById(R.id.btn_banner_mute);
        ImageButton btnHangup = bannerView.findViewById(R.id.btn_banner_hangup);

        CallStateListener listener = new CallStateListener() {
            @Override
            public void onCallStateChanged(boolean isActive, boolean isMinimized) {
                if (isActive && isMinimized) {
                    bannerView.setVisibility(View.VISIBLE);
                    if (tvName != null) tvName.setText(peerName != null ? peerName : "Contact");
                    if (ivIcon != null) {
                        ivIcon.setImageResource("video".equalsIgnoreCase(callType) ? R.drawable.ic_videocall : R.drawable.ic_call);
                    }
                } else {
                    bannerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDurationTick(int durationSeconds) {
                if (tvDuration != null) {
                    int mins = durationSeconds / 60;
                    int secs = durationSeconds % 60;
                    tvDuration.setText(String.format(Locale.getDefault(), "Tap to return to call · %02d:%02d", mins, secs));
                }
            }

            @Override
            public void onMuteStateChanged(boolean isMuted) {
                if (btnMute != null) {
                    btnMute.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic);
                    btnMute.getBackground().mutate().setTint(isMuted ? android.graphics.Color.parseColor("#EF4444") : android.graphics.Color.parseColor("#40FFFFFF"));
                }
            }
        };

        registerListener(listener);

        bannerView.setOnClickListener(v -> restoreCall(context));
        if (btnMute != null) btnMute.setOnClickListener(v -> toggleMute(context));
        if (btnHangup != null) btnHangup.setOnClickListener(v -> hangup(context));

        return listener;
    }
}
