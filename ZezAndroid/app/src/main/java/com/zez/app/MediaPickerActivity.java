package com.zez.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.zez.app.adapter.MediaPickerAdapter;
import com.zez.app.model.MediaItem;
import com.zez.app.theme.ThemeManager;
import com.zez.app.util.MediaHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaPickerActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_URIS = "extra_selected_uris";
    public static final String EXTRA_MEDIA_MODE = "extra_media_mode"; // "all", "image", "video"
    private static final int PERM_REQUEST_CODE = 201;

    private TextView tvTitle, tvSubtitle;
    private CheckBox cbSelectAll;
    private Chip chipAll, chipPhotos, chipVideos;
    private RecyclerView recyclerGrid;
    private LinearLayout layoutEmpty;
    private ProgressBar pbLoading;
    private ExtendedFloatingActionButton fabSend;
    private MediaPickerAdapter adapter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private String mediaMode = "all";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_MEDIA_MODE)) {
            mediaMode = getIntent().getStringExtra(EXTRA_MEDIA_MODE);
        }

        initViews();
        checkPermissionsAndLoad();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_picker_back);
        tvTitle = findViewById(R.id.tv_picker_title);
        tvSubtitle = findViewById(R.id.tv_picker_subtitle);
        cbSelectAll = findViewById(R.id.cb_picker_select_all);
        chipAll = findViewById(R.id.chip_picker_all);
        chipPhotos = findViewById(R.id.chip_picker_photos);
        chipVideos = findViewById(R.id.chip_picker_videos);
        recyclerGrid = findViewById(R.id.recycler_picker_grid);
        layoutEmpty = findViewById(R.id.layout_picker_empty);
        pbLoading = findViewById(R.id.pb_picker_loading);
        fabSend = findViewById(R.id.fab_picker_send);

        btnBack.setOnClickListener(v -> finish());

        // Setup 3-column grid
        recyclerGrid.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new MediaPickerAdapter(this);
        recyclerGrid.setAdapter(adapter);

        adapter.setListener(new MediaPickerAdapter.OnSelectionChangedListener() {
            @Override
            public void onSelectionChanged(int count) {
                if (count > 0) {
                    fabSend.setVisibility(View.VISIBLE);
                    fabSend.setText("Send (" + count + ")");
                } else {
                    fabSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void onItemClick(MediaItem item) {
                // Tapping item updates selection
            }
        });

        // Filter chips
        chipAll.setOnClickListener(v -> {
            chipAll.setChecked(true);
            chipPhotos.setChecked(false);
            chipVideos.setChecked(false);
            adapter.filter(0);
        });

        chipPhotos.setOnClickListener(v -> {
            chipPhotos.setChecked(true);
            chipAll.setChecked(false);
            chipVideos.setChecked(false);
            adapter.filter(1);
        });

        chipVideos.setOnClickListener(v -> {
            chipVideos.setChecked(true);
            chipAll.setChecked(false);
            chipPhotos.setChecked(false);
            adapter.filter(2);
        });

        // Select all
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.selectAll(isChecked);
        });

        // Send action
        fabSend.setOnClickListener(v -> finishWithSelectedMedia());
    }

    private void checkPermissionsAndLoad() {
        boolean hasPerm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
            if (!hasPerm) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                }, PERM_REQUEST_CODE);
                return;
            }
        } else {
            hasPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (!hasPerm) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERM_REQUEST_CODE);
                return;
            }
        }
        loadMedia();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMedia();
        } else {
            pbLoading.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Storage permission required to pick media", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMedia() {
        pbLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        executor.execute(() -> {
            List<MediaItem> items = MediaHelper.getImagesAndVideos(this);
            long totalBytes = 0;
            for (MediaItem m : items) {
                totalBytes += m.getSize();
            }
            final String subText = items.size() + " items · " + formatTotalSize(totalBytes);

            runOnUiThread(() -> {
                pbLoading.setVisibility(View.GONE);
                if (items.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    tvSubtitle.setText(subText);
                    adapter.setItems(items);

                    if ("video".equalsIgnoreCase(mediaMode)) {
                        chipVideos.performClick();
                    } else if ("image".equalsIgnoreCase(mediaMode)) {
                        chipPhotos.performClick();
                    }
                }
            });
        });
    }

    private void finishWithSelectedMedia() {
        List<MediaItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) return;

        ArrayList<String> uriStrings = new ArrayList<>();
        for (MediaItem item : selected) {
            if (item.getUri() != null) {
                uriStrings.add(item.getUri().toString());
            }
        }

        Intent result = new Intent();
        result.putStringArrayListExtra(EXTRA_SELECTED_URIS, uriStrings);
        setResult(RESULT_OK, result);
        finish();
    }

    private static String formatTotalSize(long bytes) {
        if (bytes <= 0) return "0 MB";
        double mb = bytes / (1024.0 * 1024.0);
        if (mb >= 1024.0) {
            return String.format(Locale.getDefault(), "%.1f GB", mb / 1024.0);
        }
        return String.format(Locale.getDefault(), "%.0f MB", mb);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
