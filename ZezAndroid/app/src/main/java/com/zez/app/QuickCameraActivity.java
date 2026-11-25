package com.zez.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.zez.app.util.PreferencesHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;

public class QuickCameraActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "extra_image_uri";
    private static final int PERM_CAMERA = 102;
    private static final int FLASH_AUTO = 0;
    private static final int FLASH_ON = 1;
    private static final int FLASH_OFF = 2;

    private TextureView textureCamera;
    private ImageView ivPreview;
    private View viewShutterFlash;
    private View viewFocusRing;
    private LinearLayout layoutLiveControls, layoutConfirmControls;
    private LinearLayout btnFlash;
    private ImageView ivFlash;
    private TextView tvFlashLabel;
    private TextView chipZoom1x, chipZoom15x, chipZoom2x;
    private FrameLayout btnShutter;
    private ImageButton btnClose, btnFlip, btnRetake, btnSend;

    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewBuilder;
    private CameraCharacteristics activeChars;
    private String frontCameraId, backCameraId;
    private boolean isFrontCamera = false;
    private int flashMode = FLASH_AUTO;
    private float currentZoom = 1.0f;
    private Bitmap capturedBitmap;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_camera);

        initViews();
        setupCameraIds();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERM_CAMERA);
        } else {
            initCameraLifecycle();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        textureCamera = findViewById(R.id.texture_quick_camera);
        ivPreview = findViewById(R.id.iv_camera_preview);
        viewShutterFlash = findViewById(R.id.view_camera_shutter_flash);
        viewFocusRing = findViewById(R.id.view_camera_focus_ring);
        layoutLiveControls = findViewById(R.id.layout_camera_live_controls);
        layoutConfirmControls = findViewById(R.id.layout_camera_confirm_controls);
        btnFlash = findViewById(R.id.btn_camera_flash);
        ivFlash = findViewById(R.id.iv_camera_flash);
        tvFlashLabel = findViewById(R.id.tv_camera_flash_label);
        chipZoom1x = findViewById(R.id.chip_zoom_1x);
        chipZoom15x = findViewById(R.id.chip_zoom_15x);
        chipZoom2x = findViewById(R.id.chip_zoom_2x);
        btnShutter = findViewById(R.id.btn_camera_shutter);
        btnClose = findViewById(R.id.btn_camera_close);
        btnFlip = findViewById(R.id.btn_camera_flip);
        btnRetake = findViewById(R.id.btn_camera_retake);
        btnSend = findViewById(R.id.btn_camera_send);

        int accentColor = PreferencesHelper.getAccentColorInt(this);
        if (btnSend != null) {
            btnSend.setBackgroundTintList(ColorStateList.valueOf(accentColor));
        }

        btnClose.setOnClickListener(v -> finish());

        btnFlip.setOnClickListener(v -> {
            isFrontCamera = !isFrontCamera;
            currentZoom = 1.0f;
            updateZoomPillsUi(accentColor);
            v.animate().rotationBy(180f).setDuration(240).start();
            if (textureCamera != null && textureCamera.isAvailable()) {
                configureTransform(textureCamera.getWidth(), textureCamera.getHeight());
            }
            openCamera();
        });

        if (btnFlash != null) {
            btnFlash.setOnClickListener(v -> {
                flashMode = (flashMode + 1) % 3;
                updateFlashUi();
                updateRepeatingPreviewRequest();
            });
            updateFlashUi();
        }

        if (chipZoom1x != null) {
            chipZoom1x.setOnClickListener(v -> setZoomLevel(1.0f, accentColor));
            chipZoom15x.setOnClickListener(v -> setZoomLevel(1.5f, accentColor));
            chipZoom2x.setOnClickListener(v -> setZoomLevel(2.0f, accentColor));
            updateZoomPillsUi(accentColor);
        }

        textureCamera.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                showFocusAnimation(event.getX(), event.getY());
            }
            return false;
        });

        btnShutter.setOnClickListener(v -> {
            v.animate().scaleX(0.90f).scaleY(0.90f).setDuration(80).withEndAction(() ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
            ).start();
            takeSnapshot();
        });

        btnRetake.setOnClickListener(v -> retakePhoto());
        btnSend.setOnClickListener(v -> confirmAndSend());
    }

    private void showFocusAnimation(float x, float y) {
        if (viewFocusRing == null) return;
        float half = viewFocusRing.getWidth() > 0 ? viewFocusRing.getWidth() / 2f : 90f;
        viewFocusRing.setX(x - half);
        viewFocusRing.setY(y - half);
        viewFocusRing.setVisibility(View.VISIBLE);
        viewFocusRing.setAlpha(1f);
        viewFocusRing.setScaleX(1.3f);
        viewFocusRing.setScaleY(1.3f);
        viewFocusRing.animate().scaleX(1f).scaleY(1f).setDuration(180).withEndAction(() ->
                viewFocusRing.animate().alpha(0f).setStartDelay(550).setDuration(200)
                        .withEndAction(() -> viewFocusRing.setVisibility(View.GONE)).start()
        ).start();
    }

    private void updateFlashUi() {
        if (tvFlashLabel == null || ivFlash == null) return;
        if (isFrontCamera) {
            tvFlashLabel.setText("Off");
            ivFlash.setColorFilter(Color.parseColor("#94A3B8"));
            return;
        }
        if (flashMode == FLASH_ON) {
            tvFlashLabel.setText("On");
            ivFlash.setColorFilter(Color.parseColor("#FACC15"));
        } else if (flashMode == FLASH_OFF) {
            tvFlashLabel.setText("Off");
            ivFlash.setColorFilter(Color.parseColor("#94A3B8"));
        } else {
            tvFlashLabel.setText("Auto");
            ivFlash.setColorFilter(Color.WHITE);
        }
    }

    private void setZoomLevel(float zoom, int accentColor) {
        currentZoom = zoom;
        updateZoomPillsUi(accentColor);
        updateRepeatingPreviewRequest();
    }

    private void updateZoomPillsUi(int accentColor) {
        if (chipZoom1x == null) return;
        styleZoomChip(chipZoom1x, Math.abs(currentZoom - 1.0f) < 0.05f, accentColor);
        styleZoomChip(chipZoom15x, Math.abs(currentZoom - 1.5f) < 0.05f, accentColor);
        styleZoomChip(chipZoom2x, Math.abs(currentZoom - 2.0f) < 0.05f, accentColor);
    }

    private void styleZoomChip(TextView chip, boolean active, int accentColor) {
        if (active) {
            chip.setBackgroundTintList(ColorStateList.valueOf(accentColor));
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            chip.setTextColor(Color.parseColor("#CBD5E1"));
        }
    }

    private void initCameraLifecycle() {
        if (textureCamera.isAvailable()) {
            configureTransform(textureCamera.getWidth(), textureCamera.getHeight());
            openCamera();
        } else {
            textureCamera.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
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
            if (backCameraId == null && manager.getCameraIdList().length > 0) {
                backCameraId = manager.getCameraIdList()[0];
            }
        } catch (Exception ignored) {}
    }

    private android.util.Size chooseOptimalPreviewSize(CameraCharacteristics chars, int viewWidth, int viewHeight) {
        try {
            android.hardware.camera2.params.StreamConfigurationMap map =
                    chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) return new android.util.Size(1280, 720);
            android.util.Size[] choices = map.getOutputSizes(SurfaceTexture.class);
            if (choices == null || choices.length == 0) return new android.util.Size(1280, 720);

            int targetW = Math.max(viewWidth, viewHeight);
            int targetH = Math.min(viewWidth, viewHeight);
            double targetRatio = targetH > 0 ? (double) targetW / targetH : (16.0 / 9.0);

            android.util.Size best = choices[0];
            double bestScore = Double.MAX_VALUE;
            for (android.util.Size option : choices) {
                int w = option.getWidth();
                int h = option.getHeight();
                if (w > 1920 || h > 1920) continue;
                double ratio = (double) Math.max(w, h) / Math.max(1, Math.min(w, h));
                double aspectDiff = Math.abs(ratio - targetRatio);
                double resPenalty = (w < 640 || h < 480) ? 2.0 : 0.0;
                double score = aspectDiff * 10.0 + resPenalty - (w * h) / 10000000.0;
                if (score < bestScore) {
                    bestScore = score;
                    best = option;
                }
            }
            return best;
        } catch (Exception e) {
            return new android.util.Size(1280, 720);
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (textureCamera == null || !textureCamera.isAvailable() || viewWidth == 0 || viewHeight == 0) return;
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) return;
        try {
            String targetId = isFrontCamera ? (frontCameraId != null ? frontCameraId : backCameraId) : (backCameraId != null ? backCameraId : frontCameraId);
            if (targetId == null) return;
            CameraCharacteristics chars = manager.getCameraCharacteristics(targetId);
            android.util.Size chosen = chooseOptimalPreviewSize(chars, viewWidth, viewHeight);
            int bufferW = chosen.getWidth();
            int bufferH = chosen.getHeight();

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            Matrix matrix = new Matrix();
            RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
            RectF bufferRect = new RectF(0, 0, bufferH, bufferW);
            float centerX = viewRect.centerX();
            float centerY = viewRect.centerY();

            if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
                bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
                matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
                float scale = Math.max((float) viewHeight / bufferH, (float) viewWidth / bufferW);
                matrix.postScale(scale, scale, centerX, centerY);
                matrix.postRotate(90 * (rotation - 2), centerX, centerY);
            } else {
                boolean swapped = true;
                Integer sensorOrientation = chars.get(CameraCharacteristics.SENSOR_ORIENTATION);
                if (sensorOrientation != null && (sensorOrientation == 0 || sensorOrientation == 180)) {
                    swapped = false;
                }
                int orientedBufferW = swapped ? bufferH : bufferW;
                int orientedBufferH = swapped ? bufferW : bufferH;
                float viewRatio = (float) viewWidth / (float) viewHeight;
                float bufferRatio = (float) orientedBufferW / (float) orientedBufferH;
                float scaleX = 1f;
                float scaleY = 1f;
                if (bufferRatio > viewRatio) {
                    scaleX = bufferRatio / viewRatio;
                } else {
                    scaleY = viewRatio / bufferRatio;
                }
                matrix.setScale(scaleX, scaleY, centerX, centerY);
                if (Surface.ROTATION_180 == rotation) {
                    matrix.postRotate(180, centerX, centerY);
                }
            }
            textureCamera.setTransform(matrix);
        } catch (Exception ignored) {}
    }

    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return;
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) return;
        String targetId = isFrontCamera ? (frontCameraId != null ? frontCameraId : backCameraId) : (backCameraId != null ? backCameraId : frontCameraId);
        if (targetId == null) return;

        try {
            closeCamera();
            activeChars = manager.getCameraCharacteristics(targetId);
            updateFlashUi();
            manager.openCamera(targetId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    startPreview();
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
        } catch (Exception ignored) {}
    }

    private void startPreview() {
        if (cameraDevice == null || !textureCamera.isAvailable()) return;
        try {
            SurfaceTexture texture = textureCamera.getSurfaceTexture();
            if (texture == null) return;
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String targetId = isFrontCamera ? (frontCameraId != null ? frontCameraId : backCameraId) : (backCameraId != null ? backCameraId : frontCameraId);
            if (manager != null && targetId != null) {
                CameraCharacteristics chars = manager.getCameraCharacteristics(targetId);
                activeChars = chars;
                android.util.Size chosen = chooseOptimalPreviewSize(chars, textureCamera.getWidth(), textureCamera.getHeight());
                texture.setDefaultBufferSize(chosen.getWidth(), chosen.getHeight());
            } else {
                texture.setDefaultBufferSize(1280, 720);
            }
            configureTransform(textureCamera.getWidth(), textureCamera.getHeight());
            Surface surface = new Surface(texture);

            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(surface);
            applyCameraSettingsToBuilder(previewBuilder);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    updateRepeatingPreviewRequest();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
            }, mainHandler);
        } catch (Exception ignored) {}
    }

    private void applyCameraSettingsToBuilder(CaptureRequest.Builder builder) {
        if (builder == null) return;
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        if (!isFrontCamera && activeChars != null) {
            Boolean hasFlash = activeChars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            if (Boolean.TRUE.equals(hasFlash)) {
                if (flashMode == FLASH_ON) {
                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                } else if (flashMode == FLASH_AUTO) {
                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                } else {
                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                }
            }
        }
        if (activeChars != null && currentZoom > 1.0f) {
            Rect sensorRect = activeChars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            Float maxZoom = activeChars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            if (sensorRect != null && maxZoom != null && maxZoom > 1.0f) {
                float clamped = Math.min(currentZoom, maxZoom);
                int cropW = Math.round(sensorRect.width() / clamped);
                int cropH = Math.round(sensorRect.height() / clamped);
                int left = (sensorRect.width() - cropW) / 2;
                int top = (sensorRect.height() - cropH) / 2;
                builder.set(CaptureRequest.SCALER_CROP_REGION, new Rect(left, top, left + cropW, top + cropH));
            }
        } else if (activeChars != null) {
            Rect sensorRect = activeChars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            if (sensorRect != null) {
                builder.set(CaptureRequest.SCALER_CROP_REGION, sensorRect);
            }
        }
    }

    private void updateRepeatingPreviewRequest() {
        if (captureSession == null || previewBuilder == null) return;
        try {
            applyCameraSettingsToBuilder(previewBuilder);
            captureSession.setRepeatingRequest(previewBuilder.build(), null, mainHandler);
        } catch (CameraAccessException | IllegalStateException ignored) {}
    }

    private void takeSnapshot() {
        if (textureCamera != null && textureCamera.isAvailable()) {
            if (viewShutterFlash != null) {
                viewShutterFlash.setVisibility(View.VISIBLE);
                viewShutterFlash.setAlpha(0.75f);
                viewShutterFlash.animate().alpha(0f).setDuration(200)
                        .withEndAction(() -> viewShutterFlash.setVisibility(View.GONE)).start();
            }
            capturedBitmap = textureCamera.getBitmap();
            if (capturedBitmap != null) {
                ivPreview.setImageBitmap(capturedBitmap);
                ivPreview.setVisibility(View.VISIBLE);
                layoutLiveControls.setVisibility(View.GONE);
                layoutConfirmControls.setVisibility(View.VISIBLE);
            }
        }
    }

    private void retakePhoto() {
        if (capturedBitmap != null) {
            capturedBitmap.recycle();
            capturedBitmap = null;
        }
        ivPreview.setVisibility(View.GONE);
        layoutConfirmControls.setVisibility(View.GONE);
        layoutLiveControls.setVisibility(View.VISIBLE);
    }

    private void confirmAndSend() {
        if (capturedBitmap == null) return;
        try {
            File cacheDir = new File(getCacheDir(), "quick_photos");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File photoFile = new File(cacheDir, "cam_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(photoFile)) {
                capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos);
                fos.flush();
            }
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
            Intent res = new Intent();
            res.setData(uri);
            res.putExtra(EXTRA_IMAGE_URI, uri.toString());
            res.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            setResult(RESULT_OK, res);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void closeCamera() {
        if (captureSession != null) {
            try { captureSession.close(); } catch (Exception ignored) {}
            captureSession = null;
        }
        if (cameraDevice != null) {
            try { cameraDevice.close(); } catch (Exception ignored) {}
            cameraDevice = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initCameraLifecycle();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCamera();
        if (capturedBitmap != null) {
            capturedBitmap.recycle();
            capturedBitmap = null;
        }
    }
}
