package com.zez.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zez.app.util.ActiveCallManager;
import com.zez.app.util.ChatApiClient;
import com.zez.app.util.ChatPreferencesHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CallActivity extends AppCompatActivity {
    private static final String TAG = "CallActivity";

    public static final String EXTRA_CALL_ID = "extra_call_id";
    public static final String EXTRA_IS_CALLER = "extra_is_caller";
    public static final String EXTRA_CALL_TYPE = "extra_call_type";
    public static final String EXTRA_PEER_ID = "extra_peer_id";
    public static final String EXTRA_PEER_NAME = "extra_peer_name";
    public static final String EXTRA_PEER_AVATAR = "extra_peer_avatar";
    public static final String EXTRA_PEER_PHONE = "extra_peer_phone";

    private String callId;
    private boolean isCaller = false;
    private String callType = "video";
    private String peerId;
    private String peerName;
    private String peerAvatar;
    private String peerPhone;
    private String currentUserId;

    private ImageView ivRemoteVideo;
    private LinearLayout layoutAudioContent;
    private View layoutAvatarBox;
    private TextView tvCallTypeLabel;
    private TextView tvPeerName;
    private TextView tvCallStatus;
    private ImageView ivPeerAvatar;
    private TextView tvEncryptionNote;

    private CardView cardLocalPip;
    private TextureView textureLocalCamera;
    private ImageView ivLocalCamOff;

    private ImageButton btnMinimize;
    private ImageButton btnMute;
    private ImageButton btnFlipCam;
    private ImageButton btnToggleCam;
    private ImageButton btnSpeaker;
    private ImageButton btnHangup;

    private boolean isMuted = false;
    private boolean isSpeakerOn = true;
    private boolean isCameraEnabled = true;
    private boolean isFrontCamera = true;
    private boolean isConnected = false;
    private boolean isDestroyed = false;

    private AudioManager audioManager;
    private ToneGenerator outgoingTone;

    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private String frontCameraId;
    private String backCameraId;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler frameHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean isSendingFrame = new AtomicBoolean(false);
    private final AtomicBoolean isFetchingFrame = new AtomicBoolean(false);

    private int callDurationSeconds = 0;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isDestroyed || !isConnected) return;
            callDurationSeconds++;
            int mins = callDurationSeconds / 60;
            int secs = callDurationSeconds % 60;
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
            tvCallStatus.setText(timeStr);
            ActiveCallManager.getInstance().setDuration(callDurationSeconds);
            mainHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        setContentView(R.layout.activity_call);

        callId = getIntent().getStringExtra(EXTRA_CALL_ID);
        isCaller = getIntent().getBooleanExtra(EXTRA_IS_CALLER, false);
        callType = getIntent().getStringExtra(EXTRA_CALL_TYPE);
        if (callType == null) callType = "video";
        peerId = getIntent().getStringExtra(EXTRA_PEER_ID);
        peerName = getIntent().getStringExtra(EXTRA_PEER_NAME);
        peerAvatar = getIntent().getStringExtra(EXTRA_PEER_AVATAR);
        peerPhone = getIntent().getStringExtra(EXTRA_PEER_PHONE);
        currentUserId = ChatPreferencesHelper.getUserId(this);

        ActiveCallManager.getInstance().setCallStarted(callId, callType, isCaller, peerId, peerName, peerPhone, peerAvatar);

        initViews();
        setupAudio();
        checkPermissionsAndStart();
    }

    private void initViews() {
        ivRemoteVideo = findViewById(R.id.iv_call_remote_video);
        layoutAudioContent = findViewById(R.id.layout_call_audio_content);
        layoutAvatarBox = findViewById(R.id.layout_call_avatar_box);
        tvCallTypeLabel = findViewById(R.id.tv_call_type_label);
        tvPeerName = findViewById(R.id.tv_call_peer_name);
        tvCallStatus = findViewById(R.id.tv_call_status);
        ivPeerAvatar = findViewById(R.id.iv_call_peer_avatar);
        tvEncryptionNote = findViewById(R.id.tv_call_encryption_note);

        cardLocalPip = findViewById(R.id.card_call_local_pip);
        textureLocalCamera = findViewById(R.id.texture_call_local_camera);
        ivLocalCamOff = findViewById(R.id.iv_call_local_cam_off);

        btnMinimize = findViewById(R.id.btn_call_minimize);
        btnMute = findViewById(R.id.btn_call_mute);
        btnFlipCam = findViewById(R.id.btn_call_flip_cam);
        btnToggleCam = findViewById(R.id.btn_call_toggle_cam);
        btnSpeaker = findViewById(R.id.btn_call_speaker);
        btnHangup = findViewById(R.id.btn_call_hangup);

        boolean isVideo = "video".equalsIgnoreCase(callType);
        tvCallTypeLabel.setText(isVideo ? "TWING Video Call" : "TWING Voice Call");
        tvPeerName.setText(peerName != null && !peerName.isEmpty() ? peerName : "Contact");
        tvCallStatus.setText(isCaller ? "Calling..." : "Connecting...");

        if (peerAvatar != null && !peerAvatar.isEmpty()) {
            String fullUrl = ChatApiClient.getAbsoluteMediaUrl(this, peerAvatar);
            ivPeerAvatar.setImageTintList(null);
            ivPeerAvatar.clearColorFilter();
            Glide.with(this)
                    .load(fullUrl)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_circle_glass)
                    .error(R.drawable.bg_circle_glass)
                    .into(ivPeerAvatar);
        }

        if (!isVideo) {
            cardLocalPip.setVisibility(View.GONE);
            btnFlipCam.setVisibility(View.GONE);
            btnToggleCam.setVisibility(View.GONE);
        } else {
            cardLocalPip.setVisibility(View.VISIBLE);
            btnFlipCam.setVisibility(View.VISIBLE);
            btnToggleCam.setVisibility(View.VISIBLE);
        }

        if (btnMinimize != null) {
            btnMinimize.setOnClickListener(v -> minimizeCall());
        }
        btnMute.setOnClickListener(v -> toggleMute());
        btnSpeaker.setOnClickListener(v -> toggleSpeaker());
        btnFlipCam.setOnClickListener(v -> flipCamera());
        btnToggleCam.setOnClickListener(v -> toggleVideo());
        btnHangup.setOnClickListener(v -> endCall(true));

        ActiveCallManager.getInstance().registerListener(new ActiveCallManager.CallStateListener() {
            @Override
            public void onCallStateChanged(boolean isActive, boolean isMinimized) {
                if (!isActive && !isFinishing()) {
                    finish();
                }
            }
            @Override public void onDurationTick(int durationSeconds) {}
            @Override public void onMuteStateChanged(boolean isMuted) {}
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        ActiveCallManager.getInstance().setMinimized(false);
    }

    private void minimizeCall() {
        ActiveCallManager.getInstance().setMinimized(true);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void setupAudio() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            isSpeakerOn = "video".equalsIgnoreCase(callType);
            audioManager.setSpeakerphoneOn(isSpeakerOn);
            updateSpeakerUi();
        }

        if (isCaller) {
            try {
                outgoingTone = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 60);
                outgoingTone.startTone(ToneGenerator.TONE_SUP_RINGTONE, 2000);
            } catch (Exception ignored) {}
        }
    }

    private void checkPermissionsAndStart() {
        boolean hasCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (!hasCamera || !hasMic) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 101);
        } else {
            initCameraAndLifecycle();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initCameraAndLifecycle();
    }

    private void initCameraAndLifecycle() {
        if ("video".equalsIgnoreCase(callType)) {
            setupCameraIds();
            if (textureLocalCamera.isAvailable()) {
                openCamera();
            } else {
                textureLocalCamera.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                        openCamera();
                    }
                    @Override
                    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}
                    @Override
                    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) { return true; }
                    @Override
                    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
                });
            }
        }

        if (isCaller) {
            startCallerPolling();
        } else {
            // Callee accepted from IncomingCallActivity
            onCallConnected();
        }

        // Start video frame streaming loop if video call
        if ("video".equalsIgnoreCase(callType)) {
            startFrameExchangeLoops();
        }
    }

    private void setupCameraIds() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) return;
        try {
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics chars = manager.getCameraCharacteristics(id);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = id;
                } else if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    backCameraId = id;
                }
            }
            if (frontCameraId == null && manager.getCameraIdList().length > 0) {
                frontCameraId = manager.getCameraIdList()[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error enumerating cameras: " + e.getMessage());
        }
    }

    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return;
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) return;

        String targetId = isFrontCamera ? (frontCameraId != null ? frontCameraId : backCameraId) : (backCameraId != null ? backCameraId : frontCameraId);
        if (targetId == null) return;

        try {
            closeCamera();
            manager.openCamera(targetId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    startCameraPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                }
            }, mainHandler);
        } catch (Exception e) {
            Log.e(TAG, "openCamera error: " + e.getMessage());
        }
    }

    private void startCameraPreview() {
        if (cameraDevice == null || !textureLocalCamera.isAvailable()) return;
        try {
            SurfaceTexture texture = textureLocalCamera.getSurfaceTexture();
            if (texture == null) return;
            texture.setDefaultBufferSize(320, 240);
            Surface surface = new Surface(texture);

            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                        session.setRepeatingRequest(builder.build(), null, mainHandler);
                    } catch (CameraAccessException ignored) {}
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
            }, mainHandler);
        } catch (Exception e) {
            Log.e(TAG, "startCameraPreview error: " + e.getMessage());
        }
    }

    private void closeCamera() {
        try {
            if (captureSession != null) {
                captureSession.close();
                captureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
        } catch (Exception ignored) {}
    }

    private final Runnable pollStatusRunnable = new Runnable() {
        @Override
        public void run() {
            if (isDestroyed || isConnected) return;
            String url = ChatApiClient.getBaseUrl(CallActivity.this) + "/api/call/status?callId=" + callId;
            Request req = new Request.Builder().url(url).build();
            httpClient.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (!isDestroyed && !isConnected) mainHandler.postDelayed(pollStatusRunnable, 1500);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (isDestroyed || isConnected) return;
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String body = response.body().string();
                            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                            String st = json.has("status") ? json.get("status").getAsString() : "";
                            if ("connected".equalsIgnoreCase(st)) {
                                mainHandler.post(() -> onCallConnected());
                                return;
                            } else if ("rejected".equalsIgnoreCase(st) || "busy".equalsIgnoreCase(st) || "ended".equalsIgnoreCase(st) || "missed".equalsIgnoreCase(st)) {
                                mainHandler.post(() -> {
                                    stopOutgoingTone();
                                    String msg = "rejected".equalsIgnoreCase(st) ? "Call declined" : ("busy".equalsIgnoreCase(st) ? "User busy" : "Call ended");
                                    tvCallStatus.setText(msg);
                                    Toast.makeText(CallActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    mainHandler.postDelayed(() -> finish(), 1500);
                                });
                                return;
                            }
                        } catch (Exception ignored) {}
                    }
                    if (!isDestroyed && !isConnected) mainHandler.postDelayed(pollStatusRunnable, 1500);
                }
            });
        }
    };

    private void startCallerPolling() {
        mainHandler.postDelayed(pollStatusRunnable, 1500);
    }

    private void onCallConnected() {
        if (isConnected || isDestroyed) return;
        isConnected = true;
        stopOutgoingTone();

        tvCallStatus.setTextColor(android.graphics.Color.parseColor("#22C55E"));
        tvCallStatus.setText("00:00");
        mainHandler.post(timerRunnable);

        // Keep polling status in background to detect when peer hangs up
        startEndCallWatcher();
    }

    private final Runnable endWatcherRunnable = new Runnable() {
        @Override
        public void run() {
            if (isDestroyed) return;
            String url = ChatApiClient.getBaseUrl(CallActivity.this) + "/api/call/status?callId=" + callId;
            Request req = new Request.Builder().url(url).build();
            httpClient.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (!isDestroyed) mainHandler.postDelayed(endWatcherRunnable, 2000);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (isDestroyed) return;
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String body = response.body().string();
                            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                            String st = json.has("status") ? json.get("status").getAsString() : "";
                            if ("ended".equalsIgnoreCase(st)) {
                                mainHandler.post(() -> {
                                    tvCallStatus.setText("Call ended");
                                    Toast.makeText(CallActivity.this, "Call ended", Toast.LENGTH_SHORT).show();
                                    mainHandler.postDelayed(() -> finish(), 1000);
                                });
                                return;
                            }
                        } catch (Exception ignored) {}
                    }
                    if (!isDestroyed) mainHandler.postDelayed(endWatcherRunnable, 2000);
                }
            });
        }
    };

    private void startEndCallWatcher() {
        mainHandler.postDelayed(endWatcherRunnable, 2000);
    }

    private void startFrameExchangeLoops() {
        // Send local camera frame every 250ms (non-blocking)
        Runnable sendFrameTask = new Runnable() {
            @Override
            public void run() {
                if (isDestroyed) return;
                if (isCameraEnabled && textureLocalCamera != null && textureLocalCamera.isAvailable()) {
                    if (isSendingFrame.compareAndSet(false, true)) {
                        try {
                            Bitmap srcBmp = textureLocalCamera.getBitmap();
                            if (srcBmp != null) {
                                Bitmap scaled = Bitmap.createScaledBitmap(srcBmp, 320, 240, true);
                                if (scaled != srcBmp) srcBmp.recycle();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                scaled.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                scaled.recycle();
                                byte[] bytes = baos.toByteArray();
                                String b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);

                                String url = ChatApiClient.getBaseUrl(CallActivity.this) + "/api/call/frame";
                                JsonObject json = new JsonObject();
                                json.addProperty("callId", callId);
                                json.addProperty("senderId", currentUserId);
                                json.addProperty("frameBase64", b64);

                                RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
                                Request req = new Request.Builder().url(url).post(body).build();
                                httpClient.newCall(req).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        isSendingFrame.set(false);
                                    }
                                    @Override
                                    public void onResponse(Call call, Response response) {
                                        try { if (response.body() != null) response.body().close(); } catch (Exception ignored) {}
                                        isSendingFrame.set(false);
                                    }
                                });
                            } else {
                                isSendingFrame.set(false);
                            }
                        } catch (Exception e) {
                            isSendingFrame.set(false);
                        }
                    }
                }
                if (!isDestroyed) frameHandler.postDelayed(this, 280);
            }
        };
        frameHandler.postDelayed(sendFrameTask, 400);

        // Fetch remote camera frame every 250ms (non-blocking)
        Runnable fetchFrameTask = new Runnable() {
            @Override
            public void run() {
                if (isDestroyed) return;
                if (peerId != null && !peerId.isEmpty()) {
                    if (isFetchingFrame.compareAndSet(false, true)) {
                        String url = ChatApiClient.getBaseUrl(CallActivity.this) + "/api/call/frame?callId=" + callId + "&senderId=" + peerId;
                        Request req = new Request.Builder().url(url).build();
                        httpClient.newCall(req).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                isFetchingFrame.set(false);
                            }
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (isDestroyed) {
                                    isFetchingFrame.set(false);
                                    return;
                                }
                                if (response.isSuccessful() && response.body() != null) {
                                    try {
                                        String body = response.body().string();
                                        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                                        if (json.has("hasFrame") && json.get("hasFrame").getAsBoolean() && json.has("frame")) {
                                            String b64 = json.get("frame").getAsString();
                                            byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
                                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            if (bmp != null) {
                                                runOnUiThread(() -> {
                                                    if (ivRemoteVideo != null) {
                                                        ivRemoteVideo.setImageBitmap(bmp);
                                                        ivRemoteVideo.setVisibility(View.VISIBLE);
                                                    }
                                                    if (layoutAvatarBox != null) {
                                                        layoutAvatarBox.setVisibility(View.GONE);
                                                    }
                                                    if (tvEncryptionNote != null) {
                                                        tvEncryptionNote.setVisibility(View.GONE);
                                                    }
                                                });
                                            }
                                        }
                                    } catch (Exception ignored) {}
                                }
                                isFetchingFrame.set(false);
                            }
                        });
                    }
                }
                if (!isDestroyed) frameHandler.postDelayed(this, 280);
            }
        };
        frameHandler.postDelayed(fetchFrameTask, 500);
    }

    private void toggleMute() {
        isMuted = !isMuted;
        ActiveCallManager.getInstance().toggleMute(this);
        btnMute.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic);
        btnMute.getBackground().mutate().setTint(isMuted ? android.graphics.Color.parseColor("#EF4444") : android.graphics.Color.parseColor("#40202C33"));
    }

    private void toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn;
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(isSpeakerOn);
        }
        updateSpeakerUi();
    }

    private void updateSpeakerUi() {
        btnSpeaker.setImageResource(isSpeakerOn ? R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        btnSpeaker.getBackground().mutate().setTint(isSpeakerOn ? android.graphics.Color.parseColor("#22C55E") : android.graphics.Color.parseColor("#40202C33"));
    }

    private void flipCamera() {
        isFrontCamera = !isFrontCamera;
        openCamera();
        Toast.makeText(this, isFrontCamera ? "Front camera" : "Back camera", Toast.LENGTH_SHORT).show();
    }

    private void toggleVideo() {
        isCameraEnabled = !isCameraEnabled;
        if (isCameraEnabled) {
            openCamera();
            cardLocalPip.setVisibility(View.VISIBLE);
            ivLocalCamOff.setVisibility(View.GONE);
            btnToggleCam.setImageResource(R.drawable.ic_videocall);
            btnToggleCam.getBackground().mutate().setTint(android.graphics.Color.parseColor("#40202C33"));
        } else {
            closeCamera();
            ivLocalCamOff.setVisibility(View.VISIBLE);
            btnToggleCam.setImageResource(R.drawable.ic_videocam_off);
            btnToggleCam.getBackground().mutate().setTint(android.graphics.Color.parseColor("#EF4444"));
        }
    }

    private void stopOutgoingTone() {
        try {
            if (outgoingTone != null) {
                outgoingTone.stopTone();
                outgoingTone.release();
                outgoingTone = null;
            }
        } catch (Exception ignored) {}
    }

    private void endCall(boolean notifyBackend) {
        if (isDestroyed) return;
        isDestroyed = true;
        ActiveCallManager.getInstance().endCall();
        stopOutgoingTone();
        closeCamera();

        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
        }

        if (notifyBackend && callId != null && !callId.isEmpty()) {
            String url = ChatApiClient.getBaseUrl(this) + "/api/call/end";
            JsonObject json = new JsonObject();
            json.addProperty("callId", callId);
            json.addProperty("userId", currentUserId);

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

        finish();
    }

    @Override
    public void onBackPressed() {
        minimizeCall();
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        stopOutgoingTone();
        closeCamera();
        mainHandler.removeCallbacksAndMessages(null);
        frameHandler.removeCallbacksAndMessages(null);
        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
        }
        super.onDestroy();
    }
}
