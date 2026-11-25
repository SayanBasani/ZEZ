package com.zez.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
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
import com.zez.app.util.CallMediaEngine;
import com.zez.app.util.ChatApiClient;
import com.zez.app.util.ChatPreferencesHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    public static final String EXTRA_ACCEPT_CALL = "extra_accept_call";

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
    private Size previewSize = new Size(480, 360);
    private int sensorOrientation = 90;
    private HandlerThread videoThread;
    private Handler videoHandler;
    private ImageReader frameImageReader;
    private final ExecutorService decodeExecutor = Executors.newSingleThreadExecutor();
    private volatile long lastVideoSendTimeMs = 0L;
    private volatile boolean isFetchingRemoteFallback = false;
    private boolean isRemoteFitCenter = false;

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
        com.zez.app.theme.ThemeManager.applyTheme(this, true);
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
        handleAcceptFromNotificationIfNeeded(getIntent());

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
        tvCallTypeLabel.setText(isVideo ? "Quacky Video Call" : "Quacky Voice Call");
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
            if (ivRemoteVideo != null) {
                ivRemoteVideo.setOnClickListener(v -> {
                    isRemoteFitCenter = !isRemoteFitCenter;
                    ivRemoteVideo.setScaleType(isRemoteFitCenter ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
                    Toast.makeText(this, isRemoteFitCenter ? "Fit video to screen" : "Fill screen", Toast.LENGTH_SHORT).show();
                });
            }
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
            @Override
            public void onMuteStateChanged(boolean mutedState) {
                isMuted = mutedState;
                updateMuteUi();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isMuted = ActiveCallManager.getInstance().isMuted();
        updateMuteUi();
        ActiveCallManager.getInstance().setMediaListener(callMediaListener);
        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(isSpeakerOn);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        ActiveCallManager.getInstance().setMinimized(false);
        handleAcceptFromNotificationIfNeeded(intent);
    }

    private void handleAcceptFromNotificationIfNeeded(Intent intent) {
        if (intent == null || isCaller || !intent.getBooleanExtra(EXTRA_ACCEPT_CALL, false)) return;
        intent.removeExtra(EXTRA_ACCEPT_CALL);
        com.zez.app.util.ChatNotificationHelper.cancelIncomingCallNotification(this);
        if (callId == null || callId.isEmpty()) return;

        String url = ChatApiClient.getBaseUrl(this) + "/api/call/respond";
        JsonObject body = new JsonObject();
        body.addProperty("callId", callId);
        if (currentUserId != null) body.addProperty("userId", currentUserId);
        body.addProperty("action", "accept");
        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json; charset=utf-8")))
                .build();
        httpClient.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.w(TAG, "Accept-from-notification request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                response.close();
            }
        });
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
        ActiveCallManager.getInstance().enableAudioIfPermitted();
        initCameraAndLifecycle();
    }

    private void startVideoBackgroundThread() {
        if (videoThread == null) {
            videoThread = new HandlerThread("CallVideoThread");
            videoThread.start();
            videoHandler = new Handler(videoThread.getLooper());
        }
    }

    private void stopVideoBackgroundThread() {
        if (videoThread != null) {
            videoThread.quitSafely();
            try { videoThread.join(400); } catch (InterruptedException ignored) {}
            videoThread = null;
            videoHandler = null;
        }
    }

    private void initCameraAndLifecycle() {
        if ("video".equalsIgnoreCase(callType)) {
            startVideoBackgroundThread();
            setupCameraIds();
            if (textureLocalCamera.isAvailable()) {
                configureTransform(textureLocalCamera.getWidth(), textureLocalCamera.getHeight());
                openCamera();
            } else {
                textureLocalCamera.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                        configureTransform(width, height);
                        openCamera();
                    }
                    @Override
                    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                        configureTransform(width, height);
                    }
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
    }

    private Size chooseOptimalSize(Size[] choices) {
        if (choices == null || choices.length == 0) return new Size(640, 480);
        Size best = null;
        for (Size s : choices) {
            int w = s.getWidth();
            int h = s.getHeight();
            // Pick 4:3 resolution around 480x360 .. 640x480 for crisp unstretched video
            if (w >= 320 && w <= 640 && h >= 240 && h <= 480 && (w * 3 == h * 4)) {
                if (best == null || w > best.getWidth()) best = s;
            }
        }
        if (best != null) return best;
        for (Size s : choices) {
            if (s.getWidth() <= 640 && s.getHeight() <= 480) {
                if (best == null || s.getWidth() > best.getWidth()) best = s;
            }
        }
        return best != null ? best : choices[0];
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
            CameraCharacteristics chars = manager.getCameraCharacteristics(targetId);
            Integer orient = chars.get(CameraCharacteristics.SENSOR_ORIENTATION);
            sensorOrientation = (orient != null) ? orient : 90;
            StreamConfigurationMap map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class));
            }
            if (textureLocalCamera != null && textureLocalCamera.isAvailable()) {
                runOnUiThread(() -> configureTransform(textureLocalCamera.getWidth(), textureLocalCamera.getHeight()));
            }
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

    private void configureTransform(int viewWidth, int viewHeight) {
        if (textureLocalCamera == null || !textureLocalCamera.isAvailable() || viewWidth <= 0 || viewHeight <= 0) return;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();

        boolean swappedDimensions = (sensorOrientation == 90 || sensorOrientation == 270);
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            swappedDimensions = !swappedDimensions;
        }
        int contentW = swappedDimensions ? previewSize.getHeight() : previewSize.getWidth();
        int contentH = swappedDimensions ? previewSize.getWidth() : previewSize.getHeight();

        // Aspect-ratio-preserving center-crop scaling across ALL rotations (including ROTATION_0 portrait)
        float scaleX = (float) contentW / viewWidth;
        float scaleY = (float) contentH / viewHeight;
        float maxScale = Math.max(scaleX, scaleY);
        float targetScaleX = (maxScale > 0) ? (scaleX / maxScale) * ((float) viewHeight / contentH > (float) viewWidth / contentW ? ((float) viewHeight * contentW) / ((float) viewWidth * contentH) : 1.0f) : 1.0f;

        float ratioView = (float) viewWidth / (float) viewHeight;
        float ratioContent = (float) contentW / (float) contentH;
        if (ratioContent > ratioView) {
            matrix.setScale(ratioContent / ratioView, 1.0f, centerX, centerY);
        } else {
            matrix.setScale(1.0f, ratioView / ratioContent, centerX, centerY);
        }

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureLocalCamera.setTransform(matrix);
    }

    private int computeFrameRotationDegrees() {
        int displayRot = Surface.ROTATION_0;
        try {
            displayRot = getWindowManager().getDefaultDisplay().getRotation();
        } catch (Exception ignored) {}
        int deviceDegrees = displayRot * 90;
        if (isFrontCamera) {
            return (sensorOrientation + deviceDegrees) % 360;
        } else {
            return (sensorOrientation - deviceDegrees + 360) % 360;
        }
    }

    private void startCameraPreview() {
        if (cameraDevice == null || !textureLocalCamera.isAvailable()) return;
        try {
            SurfaceTexture texture = textureLocalCamera.getSurfaceTexture();
            if (texture == null) return;
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(texture);

            if (frameImageReader != null) {
                try { frameImageReader.close(); } catch (Exception ignored) {}
                frameImageReader = null;
            }
            startVideoBackgroundThread();
            frameImageReader = ImageReader.newInstance(
                    previewSize.getWidth(),
                    previewSize.getHeight(),
                    ImageFormat.YUV_420_888,
                    2
            );
            frameImageReader.setOnImageAvailableListener(reader -> {
                try (Image img = reader.acquireLatestImage()) {
                    if (img == null || isDestroyed || !isConnected || !isCameraEnabled) return;
                    long now = System.currentTimeMillis();
                    int tier = ActiveCallManager.getInstance().getNetworkTier();
                    long minIntervalMs = (tier == CallMediaEngine.TIER_LOW) ? 320L : ((tier == CallMediaEngine.TIER_MEDIUM) ? 180L : 95L);
                    if ((now - lastVideoSendTimeMs) < minIntervalMs) return;
                    if (!ActiveCallManager.getInstance().canSendVideoFrame()) return;
                    lastVideoSendTimeMs = now;

                    processAndSendCameraImage(img, tier);
                } catch (Exception ignored) {}
            }, videoHandler);

            Surface readerSurface = frameImageReader.getSurface();
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(previewSurface);
            builder.addTarget(readerSurface);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, readerSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                        session.setRepeatingRequest(builder.build(), null, videoHandler != null ? videoHandler : mainHandler);
                    } catch (CameraAccessException ignored) {}
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
            }, mainHandler);
        } catch (Exception e) {
            Log.e(TAG, "startCameraPreview error: " + e.getMessage());
        }
    }

    private void processAndSendCameraImage(Image image, int tier) {
        int width = image.getWidth();
        int height = image.getHeight();
        byte[] nv21 = yuv420ToNv21(image);
        if (nv21 == null) return;

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream rawJpegOut = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 80, rawJpegOut);
        byte[] rawJpeg = rawJpegOut.toByteArray();

        Bitmap decoded = BitmapFactory.decodeByteArray(rawJpeg, 0, rawJpeg.length);
        if (decoded == null) return;

        int rotationDeg = computeFrameRotationDegrees();
        int maxDim = (tier == CallMediaEngine.TIER_LOW) ? 224 : ((tier == CallMediaEngine.TIER_MEDIUM) ? 320 : 480);
        int quality = (tier == CallMediaEngine.TIER_LOW) ? 32 : ((tier == CallMediaEngine.TIER_MEDIUM) ? 46 : 62);

        int rotW = (rotationDeg == 90 || rotationDeg == 270) ? decoded.getHeight() : decoded.getWidth();
        int rotH = (rotationDeg == 90 || rotationDeg == 270) ? decoded.getWidth() : decoded.getHeight();
        float scale = Math.min(1.0f, Math.min((float) maxDim / Math.max(1, rotW), (float) maxDim / Math.max(1, rotH)));

        Matrix m = new Matrix();
        if (rotationDeg != 0) m.postRotate(rotationDeg);
        if (isFrontCamera) {
            // Un-mirror front camera only visually or mirror to match selfie consistency cleanly
            m.postScale(-scale, scale);
        } else {
            m.postScale(scale, scale);
        }

        Bitmap finalBmp = Bitmap.createBitmap(decoded, 0, 0, decoded.getWidth(), decoded.getHeight(), m, true);
        if (finalBmp != decoded) decoded.recycle();

        ByteArrayOutputStream finalOut = new ByteArrayOutputStream();
        finalBmp.compress(Bitmap.CompressFormat.JPEG, quality, finalOut);
        finalBmp.recycle();

        ActiveCallManager.getInstance().sendVideoFrame(finalOut.toByteArray());
    }

    private static byte[] yuv420ToNv21(Image image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            int ySize = width * height;
            int uvSize = ySize / 2;
            byte[] nv21 = new byte[ySize + uvSize];

            Image.Plane[] planes = image.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int yRowStride = planes[0].getRowStride();
            int yPixelStride = planes[0].getPixelStride();
            int pos = 0;
            for (int row = 0; row < height; row++) {
                int rowStart = row * yRowStride;
                if (yPixelStride == 1) {
                    yBuffer.position(rowStart);
                    yBuffer.get(nv21, pos, width);
                    pos += width;
                } else {
                    for (int col = 0; col < width; col++) {
                        nv21[pos++] = yBuffer.get(rowStart + col * yPixelStride);
                    }
                }
            }

            int chromaHeight = height / 2;
            int chromaWidth = width / 2;
            int uRowStride = planes[1].getRowStride();
            int uPixelStride = planes[1].getPixelStride();
            int vRowStride = planes[2].getRowStride();
            int vPixelStride = planes[2].getPixelStride();
            int uvPos = ySize;
            for (int row = 0; row < chromaHeight; row++) {
                int uRowStart = row * uRowStride;
                int vRowStart = row * vRowStride;
                for (int col = 0; col < chromaWidth; col++) {
                    nv21[uvPos++] = vBuffer.get(vRowStart + col * vPixelStride);
                    nv21[uvPos++] = uBuffer.get(uRowStart + col * uPixelStride);
                }
            }
            return nv21;
        } catch (Exception e) {
            return null;
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
            if (frameImageReader != null) {
                frameImageReader.close();
                frameImageReader = null;
            }
        } catch (Exception ignored) {}
    }

    private final CallMediaEngine.MediaListener callMediaListener = new CallMediaEngine.MediaListener() {
        @Override
        public void onRemoteVideoFrame(byte[] jpegBytes) {
            if (isDestroyed || jpegBytes == null) return;
            decodeExecutor.execute(() -> {
                Bitmap bmp = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);
                if (bmp != null && !isDestroyed) {
                    runOnUiThread(() -> {
                        if (isDestroyed) {
                            bmp.recycle();
                            return;
                        }
                        if (ivRemoteVideo != null) {
                            ivRemoteVideo.setScaleType(isRemoteFitCenter ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
                            ivRemoteVideo.setImageBitmap(bmp);
                            ivRemoteVideo.setVisibility(View.VISIBLE);
                        }
                        if (layoutAvatarBox != null) layoutAvatarBox.setVisibility(View.GONE);
                        if (tvEncryptionNote != null) tvEncryptionNote.setVisibility(View.GONE);
                    });
                }
            });
        }

        @Override
        public void onRemoteHangup(String reason) {
            if (isDestroyed) return;
            tvCallStatus.setText("Call ended");
            Toast.makeText(CallActivity.this, "Call ended", Toast.LENGTH_SHORT).show();
            endCall(false);
        }

        @Override
        public void onNetworkQualityChanged(int tier, int rttMs) {
            if (isDestroyed || tvEncryptionNote == null) return;
            if (tier == CallMediaEngine.TIER_LOW) {
                tvEncryptionNote.setText("Low network · Audio prioritized (" + rttMs + "ms)");
            } else if (tier == CallMediaEngine.TIER_MEDIUM) {
                tvEncryptionNote.setText("Adaptive quality (" + rttMs + "ms)");
            } else {
                tvEncryptionNote.setText("HD · Encrypted (" + rttMs + "ms)");
            }
        }
    };

    private final Runnable pollStatusRunnable = new Runnable() {
        @Override
        public void run() {
            if (isDestroyed || isConnected) return;
            String url = ChatApiClient.getBaseUrl(CallActivity.this) + "/api/call/status?callId=" + callId;
            Request req = new Request.Builder().url(url).build();
            httpClient.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (!isDestroyed && !isConnected) mainHandler.postDelayed(pollStatusRunnable, 650);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (isDestroyed || isConnected) {
                        response.close();
                        return;
                    }
                    try (Response r = response) {
                        if (r.isSuccessful() && r.body() != null) {
                            String body = r.body().string();
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
                                    ActiveCallManager.getInstance().endCall();
                                    mainHandler.postDelayed(() -> finish(), 1000);
                                });
                                return;
                            }
                        }
                    } catch (Exception ignored) {}
                    if (!isDestroyed && !isConnected) mainHandler.postDelayed(pollStatusRunnable, 650);
                }
            });
        }
    };

    private void startCallerPolling() {
        mainHandler.postDelayed(pollStatusRunnable, 500);
    }

    private void onCallConnected() {
        if (isConnected || isDestroyed) return;
        isConnected = true;
        stopOutgoingTone();

        tvCallStatus.setTextColor(android.graphics.Color.parseColor("#22C55E"));
        tvCallStatus.setText("00:00");
        mainHandler.post(timerRunnable);

        ActiveCallManager.getInstance().startMedia(this, callMediaListener);
        startEndCallWatcher();
        if ("video".equalsIgnoreCase(callType)) {
            startHttpVideoFallbackPoller();
        }
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
                    if (isDestroyed) {
                        response.close();
                        return;
                    }
                    try (Response r = response) {
                        if (r.isSuccessful() && r.body() != null) {
                            String body = r.body().string();
                            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                            String st = json.has("status") ? json.get("status").getAsString() : "";
                            if ("ended".equalsIgnoreCase(st)) {
                                mainHandler.post(() -> {
                                    tvCallStatus.setText("Call ended");
                                    Toast.makeText(CallActivity.this, "Call ended", Toast.LENGTH_SHORT).show();
                                    endCall(false);
                                });
                                return;
                            }
                        }
                    } catch (Exception ignored) {}
                    if (!isDestroyed) mainHandler.postDelayed(endWatcherRunnable, 2000);
                }
            });
        }
    };

    private void startEndCallWatcher() {
        mainHandler.postDelayed(endWatcherRunnable, 2000);
    }

    private void startHttpVideoFallbackPoller() {
        Runnable fetchFallbackTask = new Runnable() {
            @Override
            public void run() {
                if (isDestroyed) return;
                // Only poll HTTP when WebSocket relay is unavailable
                if (!ActiveCallManager.getInstance().isCallActive()) return;
                boolean wsUp = isConnected && ActiveCallManager.getInstance().getNetworkTier() < 3 && isWsUpSafe();
                if (!wsUp && peerId != null && !peerId.isEmpty() && !isFetchingRemoteFallback) {
                    isFetchingRemoteFallback = true;
                    String url = ChatApiClient.getBaseUrl(CallActivity.this) + "/api/call/frame?callId=" + callId + "&senderId=" + peerId;
                    httpClient.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            isFetchingRemoteFallback = false;
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try (Response r = response) {
                                if (!isDestroyed && r.isSuccessful() && r.body() != null) {
                                    JsonObject json = JsonParser.parseString(r.body().string()).getAsJsonObject();
                                    if (json.has("hasFrame") && json.get("hasFrame").getAsBoolean() && json.has("frame")) {
                                        byte[] bytes = Base64.decode(json.get("frame").getAsString(), Base64.DEFAULT);
                                        callMediaListener.onRemoteVideoFrame(bytes);
                                    }
                                }
                            } catch (Exception ignored) {}
                            isFetchingRemoteFallback = false;
                        }
                    });
                }
                if (!isDestroyed) frameHandler.postDelayed(this, 320);
            }
        };
        frameHandler.postDelayed(fetchFallbackTask, 600);
    }

    private boolean isWsUpSafe() {
        // Video streams over WebSocket whenever WebSocket connected; HTTP loop backs off automatically
        return ActiveCallManager.getInstance().isWsMediaConnected();
    }

    private void toggleMute() {
        isMuted = !isMuted;
        ActiveCallManager.getInstance().setMuted(this, isMuted);
        updateMuteUi();
    }

    private void updateMuteUi() {
        if (btnMute == null) return;
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
        if (textureLocalCamera != null && textureLocalCamera.isAvailable()) {
            configureTransform(textureLocalCamera.getWidth(), textureLocalCamera.getHeight());
        }
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
        stopVideoBackgroundThread();

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
        stopVideoBackgroundThread();
        decodeExecutor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
        frameHandler.removeCallbacksAndMessages(null);
        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
        }
        super.onDestroy();
    }
}
