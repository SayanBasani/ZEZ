package com.zez.app;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.zez.app.adapter.MediaPagerAdapter;
import com.zez.app.model.MediaItem;
import com.zez.app.util.FavoritesHelper;
import com.zez.app.util.RecycleBinHelper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImageViewerActivity extends AppCompatActivity {

    public static final String EXTRA_URI       = "extra_uri";
    public static final String EXTRA_NAME      = "extra_name";
    public static final String EXTRA_ID        = "extra_id";
    public static final String EXTRA_SIZE      = "extra_size";
    public static final String EXTRA_WIDTH     = "extra_width";
    public static final String EXTRA_HEIGHT    = "extra_height";
    public static final String EXTRA_START_IDX = "extra_start_idx";

    public static final String EXTRA_URI_LIST   = "extra_uri_list";
    public static final String EXTRA_ID_LIST    = "extra_id_list";
    public static final String EXTRA_NAME_LIST  = "extra_name_list";
    public static final String EXTRA_SIZE_LIST  = "extra_size_list";
    public static final String EXTRA_W_LIST     = "extra_w_list";
    public static final String EXTRA_H_LIST     = "extra_h_list";
    public static final String EXTRA_VIDEO_LIST = "extra_video_list";
    public static final String EXTRA_DUR_LIST   = "extra_dur_list";

    private ViewPager2     viewPager;
    private LinearLayout   topBar, bottomBar;
    private TextView       tvTitle;
    private ImageButton    btnFavorite, btnMore;
    private MaterialButton btnEdit;
    private boolean        barsVisible = true;
    private boolean        dataChanged = false;

    private List<MediaItem> mediaList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        viewPager   = findViewById(R.id.view_pager);
        topBar      = findViewById(R.id.top_bar);
        bottomBar   = findViewById(R.id.bottom_bar);
        tvTitle     = findViewById(R.id.tv_image_title);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnMore     = findViewById(R.id.btn_viewer_options);
        ImageButton    btnBack   = findViewById(R.id.btn_back);
        ImageButton    btnInfo   = findViewById(R.id.btn_info);
        btnEdit                  = findViewById(R.id.btn_edit);
        MaterialButton btnShare  = findViewById(R.id.btn_share);
        MaterialButton btnDelete = findViewById(R.id.btn_delete);

        // Reconstruct media list
        ArrayList<String>  uriList  = getIntent().getStringArrayListExtra(EXTRA_URI_LIST);
        ArrayList<Long>    idList   = (ArrayList<Long>)    getIntent().getSerializableExtra(EXTRA_ID_LIST);
        ArrayList<String>  nameList = getIntent().getStringArrayListExtra(EXTRA_NAME_LIST);
        ArrayList<Long>    sizeList = (ArrayList<Long>)    getIntent().getSerializableExtra(EXTRA_SIZE_LIST);
        ArrayList<Integer> wList    = (ArrayList<Integer>) getIntent().getSerializableExtra(EXTRA_W_LIST);
        ArrayList<Integer> hList    = (ArrayList<Integer>) getIntent().getSerializableExtra(EXTRA_H_LIST);
        ArrayList<Boolean> vidList  = (ArrayList<Boolean>) getIntent().getSerializableExtra(EXTRA_VIDEO_LIST);
        ArrayList<Long>    durList  = (ArrayList<Long>)    getIntent().getSerializableExtra(EXTRA_DUR_LIST);
        int startIdx = getIntent().getIntExtra(EXTRA_START_IDX, 0);

        if (uriList != null && idList != null && !uriList.isEmpty()) {
            for (int i = 0; i < uriList.size(); i++) {
                boolean isVid = vidList  != null && i < vidList.size()  && vidList.get(i);
                long    dur   = durList  != null && i < durList.size()  ? durList.get(i)  : 0;
                int     w     = wList    != null && i < wList.size()    ? wList.get(i)    : 0;
                int     h     = hList    != null && i < hList.size()    ? hList.get(i)    : 0;
                long    sz    = sizeList != null && i < sizeList.size() ? sizeList.get(i) : 0;
                String  nm    = nameList != null && i < nameList.size() ? nameList.get(i) : "";
                mediaList.add(new MediaItem(idList.get(i), Uri.parse(uriList.get(i)),
                    nm, sz, 0, isVid, dur, w, h, ""));
            }
        } else {
            // Single-item fallback
            String uriStr = getIntent().getStringExtra(EXTRA_URI);
            String name   = getIntent().getStringExtra(EXTRA_NAME);
            long   id     = getIntent().getLongExtra(EXTRA_ID, -1);
            long   size   = getIntent().getLongExtra(EXTRA_SIZE, 0);
            int    width  = getIntent().getIntExtra(EXTRA_WIDTH, 0);
            int    height = getIntent().getIntExtra(EXTRA_HEIGHT, 0);
            if (uriStr != null) {
                mediaList.add(new MediaItem(id, Uri.parse(uriStr), name, size, 0, false, 0, width, height, ""));
            }
        }

        if (mediaList.isEmpty()) { finish(); return; }

        MediaPagerAdapter pagerAdapter = new MediaPagerAdapter(mediaList, this::toggleBars);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(startIdx, false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                updateUIForPosition(position);
            }
        });
        updateUIForPosition(startIdx);

        btnBack.setOnClickListener(v -> handleBack());

        btnFavorite.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos >= 0 && pos < mediaList.size()) {
                MediaItem current = mediaList.get(pos);
                boolean isFav = FavoritesHelper.toggleFavorite(this, current.getId());
                updateFavoriteIcon(isFav);
                dataChanged = true;
                Toast.makeText(this, isFav ? "Added to Favorites" : "Removed from Favorites", Toast.LENGTH_SHORT).show();
            }
        });

        btnInfo.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos < mediaList.size()) showMeta(mediaList.get(pos));
        });

        btnEdit.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos < mediaList.size()) {
                MediaItem item = mediaList.get(pos);
                if (item.isVideo()) {
                    Toast.makeText(this, "Editing is available for photos", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, ImageEditActivity.class);
                intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, item.getUri().toString());
                intent.putExtra(ImageEditActivity.EXTRA_IMAGE_NAME, item.getName());
                startActivityForResult(intent, 3001);
            }
        });

        btnShare.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos < mediaList.size()) shareItem(mediaList.get(pos));
        });

        btnDelete.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos < mediaList.size()) confirmDelete(pos);
        });

        btnMore.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos >= mediaList.size()) return;
            MediaItem item = mediaList.get(pos);

            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add(0, 1, 0, "Details");
            popup.getMenu().add(0, 2, 1, "Set as Wallpaper");
            popup.getMenu().add(0, 3, 2, "Share");

            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 1) {
                    showMeta(item);
                    return true;
                } else if (menuItem.getItemId() == 2) {
                    setAsWallpaper(item);
                    return true;
                } else if (menuItem.getItemId() == 3) {
                    shareItem(item);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void setAsWallpaper(MediaItem item) {
        if (item.isVideo()) {
            Toast.makeText(this, "Wallpapers can only be set from photos", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"Home screen", "Lock screen", "Both"};
        new AlertDialog.Builder(this)
                .setTitle("Set wallpaper as")
                .setItems(options, (dialog, which) -> {
                    new Thread(() -> {
                        try {
                            WallpaperManager wm = WallpaperManager.getInstance(this);
                            try (InputStream is = getContentResolver().openInputStream(item.getUri())) {
                                Bitmap bitmap = BitmapFactory.decodeStream(is);
                                if (bitmap != null) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        int whichFlag = (which == 0) ? WallpaperManager.FLAG_SYSTEM :
                                                (which == 1) ? WallpaperManager.FLAG_LOCK :
                                                        (WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
                                        wm.setBitmap(bitmap, null, true, whichFlag);
                                    } else {
                                        wm.setBitmap(bitmap);
                                    }
                                    runOnUiThread(() -> Toast.makeText(this, "Wallpaper applied successfully!", Toast.LENGTH_SHORT).show());
                                } else {
                                    throw new Exception("Bitmap decode returned null");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> Toast.makeText(this, "Failed to set wallpaper: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                })
                .show();
    }

    private void updateUIForPosition(int pos) {
        if (pos >= 0 && pos < mediaList.size()) {
            MediaItem item = mediaList.get(pos);
            String name = item.getName();
            tvTitle.setText(name != null ? name : "Photo");

            boolean isFav = FavoritesHelper.isFavorite(this, item.getId());
            updateFavoriteIcon(isFav);

            if (btnEdit != null) {
                btnEdit.setVisibility(item.isVideo() ? View.GONE : View.VISIBLE);
            }
        }
    }

    private void updateFavoriteIcon(boolean isFav) {
        if (isFav) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            btnFavorite.setColorFilter(0xFFE11D48); // Red filled heart
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            btnFavorite.setColorFilter(0xFFFFFFFF); // White outline
        }
    }

    private void toggleBars() {
        barsVisible = !barsVisible;
        int vis = barsVisible ? View.VISIBLE : View.GONE;
        topBar.setVisibility(vis);
        bottomBar.setVisibility(vis);
    }

    private void shareItem(MediaItem item) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(item.isVideo() ? "video/*" : "image/*");
        intent.putExtra(Intent.EXTRA_STREAM, item.getUri());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void confirmDelete(int pos) {
        MediaItem item = mediaList.get(pos);
        new AlertDialog.Builder(this)
            .setTitle("Move to Recycle Bin?")
            .setMessage("\"" + item.getName() + "\" will be moved to the Recycle Bin.")
            .setPositiveButton("Move", (d, w) -> {
                RecycleBinHelper.addToRecycleBin(this, item.getId(),
                    item.getUri().toString(), item.getName());
                Toast.makeText(this, "Moved to Recycle Bin", Toast.LENGTH_SHORT).show();
                dataChanged = true;
                if (mediaList.size() == 1) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    mediaList.remove(pos);
                    viewPager.getAdapter().notifyDataSetChanged();
                    int newPos = Math.min(pos, mediaList.size() - 1);
                    updateUIForPosition(newPos);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showMeta(MediaItem item) {
        String size = item.getSize() > 0
            ? String.format(Locale.US, "%.2f MB", item.getSize() / (1024.0 * 1024.0))
            : "Unknown";
        StringBuilder sb = new StringBuilder();
        sb.append("File: ").append(item.getName()).append("\n");
        sb.append("Size: ").append(size).append("\n");
        if (item.getWidth() > 0)
            sb.append("Dimensions: ").append(item.getWidth()).append(" × ").append(item.getHeight()).append(" px\n");
        if (item.isVideo() && item.getDuration() > 0)
            sb.append("Duration: ").append(fmtDur(item.getDuration())).append("\n");
        new AlertDialog.Builder(this)
            .setTitle("Media Info")
            .setMessage(sb.toString().trim())
            .setPositiveButton("Close", null)
            .show();
    }

    private String fmtDur(long ms) {
        long s = ms / 1000, m = s / 60; s %= 60;
        return String.format(Locale.US, "%d:%02d", m, s);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3001 && resultCode == RESULT_OK) {
            dataChanged = true;
        }
    }

    private void handleBack() {
        if (dataChanged) {
            setResult(RESULT_OK);
        }
        finish();
    }

    @Override public void onBackPressed() {
        handleBack();
    }
}
