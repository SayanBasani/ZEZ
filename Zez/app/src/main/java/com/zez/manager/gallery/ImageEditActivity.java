package com.zez.manager.gallery;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.zez.manager.R;
import com.zez.manager.gallery.util.ImageEditHelper;

import java.io.InputStream;
import java.io.OutputStream;

public class ImageEditActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "extra_image_uri";
    public static final String EXTRA_IMAGE_NAME = "extra_image_name";

    private ImageView ivPreview;
    private ProgressBar pbLoading;
    private View panelTransform, panelAdjust, panelFilters;
    private TextView tabTransform, tabAdjust, tabFilters;
    private SeekBar sbBrightness, sbContrast, sbSaturation;

    private Bitmap originalBitmap;
    private Bitmap transformedBitmap;
    private Bitmap displayBitmap;

    private float currentBrightness = 0f;
    private float currentContrast = 1.0f;
    private float currentSaturation = 1.0f;
    private int currentFilter = ImageEditHelper.FILTER_ORIGINAL;

    private String originalName = "edited_image.jpg";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);

        initViews();
        loadImage();
    }

    private void initViews() {
        ivPreview = findViewById(R.id.iv_edit_preview);
        pbLoading = findViewById(R.id.pb_edit_loading);

        panelTransform = findViewById(R.id.panel_transform);
        panelAdjust = findViewById(R.id.panel_adjust);
        panelFilters = findViewById(R.id.panel_filters);

        tabTransform = findViewById(R.id.tab_transform);
        tabAdjust = findViewById(R.id.tab_adjust);
        tabFilters = findViewById(R.id.tab_filters);

        sbBrightness = findViewById(R.id.sb_brightness);
        sbContrast = findViewById(R.id.sb_contrast);
        sbSaturation = findViewById(R.id.sb_saturation);

        ImageButton btnCancel = findViewById(R.id.btn_edit_cancel);
        MaterialButton btnSave = findViewById(R.id.btn_edit_save);

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveEditedImage());

        // Category Tab Switchers
        tabTransform.setOnClickListener(v -> selectTab(0));
        tabAdjust.setOnClickListener(v -> selectTab(1));
        tabFilters.setOnClickListener(v -> selectTab(2));

        // Transform Tool Buttons
        findViewById(R.id.btn_rotate_left).setOnClickListener(v -> {
            if (transformedBitmap == null) return;
            transformedBitmap = ImageEditHelper.rotate(transformedBitmap, -90f);
            refreshAdjustments();
        });

        findViewById(R.id.btn_rotate_right).setOnClickListener(v -> {
            if (transformedBitmap == null) return;
            transformedBitmap = ImageEditHelper.rotate(transformedBitmap, 90f);
            refreshAdjustments();
        });

        findViewById(R.id.btn_flip_h).setOnClickListener(v -> {
            if (transformedBitmap == null) return;
            transformedBitmap = ImageEditHelper.flip(transformedBitmap, true, false);
            refreshAdjustments();
        });

        findViewById(R.id.btn_crop_square).setOnClickListener(v -> {
            if (transformedBitmap == null) return;
            int w = transformedBitmap.getWidth();
            int h = transformedBitmap.getHeight();
            int size = Math.min(w, h);
            int x = (w - size) / 2;
            int y = (h - size) / 2;
            transformedBitmap = ImageEditHelper.crop(transformedBitmap, x, y, size, size);
            refreshAdjustments();
        });

        // Sliders Listeners
        SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                currentBrightness = (sbBrightness.getProgress() - 100);
                currentContrast = 0.5f + (sbContrast.getProgress() / 100f);
                currentSaturation = sbSaturation.getProgress() / 100f;
                refreshAdjustments();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        sbBrightness.setOnSeekBarChangeListener(seekListener);
        sbContrast.setOnSeekBarChangeListener(seekListener);
        sbSaturation.setOnSeekBarChangeListener(seekListener);

        // Filter Buttons
        findViewById(R.id.btn_filter_original).setOnClickListener(v -> applyFilter(ImageEditHelper.FILTER_ORIGINAL));
        findViewById(R.id.btn_filter_vivid).setOnClickListener(v -> applyFilter(ImageEditHelper.FILTER_VIVID));
        findViewById(R.id.btn_filter_warm).setOnClickListener(v -> applyFilter(ImageEditHelper.FILTER_WARM));
        findViewById(R.id.btn_filter_cool).setOnClickListener(v -> applyFilter(ImageEditHelper.FILTER_COOL));
        findViewById(R.id.btn_filter_mono).setOnClickListener(v -> applyFilter(ImageEditHelper.FILTER_MONO));
        findViewById(R.id.btn_filter_faded).setOnClickListener(v -> applyFilter(ImageEditHelper.FILTER_FADED));
    }

    private void selectTab(int index) {
        panelTransform.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        panelAdjust.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        panelFilters.setVisibility(index == 2 ? View.VISIBLE : View.GONE);

        int activeColor = getResources().getColor(R.color.primary);
        int inactiveColor = 0xFF888888;

        tabTransform.setTextColor(index == 0 ? activeColor : inactiveColor);
        tabAdjust.setTextColor(index == 1 ? activeColor : inactiveColor);
        tabFilters.setTextColor(index == 2 ? activeColor : inactiveColor);
    }

    private void applyFilter(int filterType) {
        currentFilter = filterType;
        refreshAdjustments();
    }

    private void refreshAdjustments() {
        if (transformedBitmap == null) return;
        displayBitmap = ImageEditHelper.applyAdjustmentsAndFilter(
                transformedBitmap,
                currentBrightness,
                currentContrast,
                currentSaturation,
                currentFilter
        );
        ivPreview.setImageBitmap(displayBitmap);
    }

    private void loadImage() {
        pbLoading.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                String uriStr = getIntent().getStringExtra(EXTRA_IMAGE_URI);
                originalName = getIntent().getStringExtra(EXTRA_IMAGE_NAME);
                if (originalName == null || originalName.isEmpty()) originalName = "image_" + System.currentTimeMillis() + ".jpg";

                if (uriStr != null) {
                    Uri uri = Uri.parse(uriStr);
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    try (InputStream is = getContentResolver().openInputStream(uri)) {
                        BitmapFactory.decodeStream(is, null, opts);
                    }

                    int maxDim = 2048;
                    int scale = 1;
                    while (opts.outWidth / scale > maxDim || opts.outHeight / scale > maxDim) {
                        scale *= 2;
                    }

                    BitmapFactory.Options decodeOpts = new BitmapFactory.Options();
                    decodeOpts.inSampleSize = scale;
                    try (InputStream is = getContentResolver().openInputStream(uri)) {
                        originalBitmap = BitmapFactory.decodeStream(is, null, decodeOpts);
                    }
                }

                if (originalBitmap != null) {
                    transformedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                }

                runOnUiThread(() -> {
                    pbLoading.setVisibility(View.GONE);
                    if (transformedBitmap != null) {
                        refreshAdjustments();
                    } else {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void saveEditedImage() {
        if (displayBitmap == null) return;
        pbLoading.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                String fileName = "EDIT_" + System.currentTimeMillis() + "_" + originalName;
                if (!fileName.toLowerCase().endsWith(".jpg") && !fileName.toLowerCase().endsWith(".png")) {
                    fileName += ".jpg";
                }

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ZezEdits");
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);
                }

                Uri insertedUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (insertedUri != null) {
                    try (OutputStream os = getContentResolver().openOutputStream(insertedUri)) {
                        displayBitmap.compress(Bitmap.CompressFormat.JPEG, 92, os);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear();
                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        getContentResolver().update(insertedUri, values, null, null);
                    }

                    runOnUiThread(() -> {
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(this, "Saved to Pictures/ZezEdits!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                } else {
                    throw new Exception("MediaStore insertion returned null");
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
