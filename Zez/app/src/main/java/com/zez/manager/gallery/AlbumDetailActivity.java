package com.zez.manager.gallery;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.zez.manager.R;
import com.zez.manager.gallery.adapter.GalleryAdapter;
import com.zez.manager.gallery.model.AlbumItem;
import com.zez.manager.gallery.model.MediaItem;
import com.zez.manager.gallery.util.FavoritesHelper;
import com.zez.manager.gallery.util.MediaHelper;
import com.zez.manager.gallery.util.RecycleBinHelper;

import java.util.ArrayList;
import java.util.List;

public class AlbumDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ALBUM_NAME   = "extra_album_name";
    public static final String EXTRA_BUCKET_ID    = "extra_bucket_id";
    public static final String EXTRA_SPECIAL_TYPE = "extra_special_type";

    private String albumName = "Album";
    private long bucketId = 0;
    private int specialType = AlbumItem.TYPE_NORMAL;

    private TextView tvTitle, tvSubtitle;
    private ImageButton btnBack, btnOptions;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerMedia;
    private LinearLayout layoutEmpty, actionBar;
    private Chip chipAll, chipPhotos, chipVideos;
    private MaterialButton btnShare, btnFav, btnBin;

    private GalleryAdapter adapter;
    private List<MediaItem> currentAlbumMedia = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);

        if (getIntent() != null) {
            if (getIntent().hasExtra(EXTRA_ALBUM_NAME)) {
                albumName = getIntent().getStringExtra(EXTRA_ALBUM_NAME);
            }
            bucketId = getIntent().getLongExtra(EXTRA_BUCKET_ID, 0);
            specialType = getIntent().getIntExtra(EXTRA_SPECIAL_TYPE, AlbumItem.TYPE_NORMAL);
        }

        initViews();
        loadAlbumMedia();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_album_title);
        tvSubtitle = findViewById(R.id.tv_album_subtitle);
        btnBack = findViewById(R.id.btn_back);
        btnOptions = findViewById(R.id.btn_album_options);
        swipeRefresh = findViewById(R.id.swipe_refresh_album);
        recyclerMedia = findViewById(R.id.recycler_album_media);
        layoutEmpty = findViewById(R.id.layout_album_empty);
        actionBar = findViewById(R.id.album_action_bar);

        chipAll = findViewById(R.id.chip_album_all);
        chipPhotos = findViewById(R.id.chip_album_photos);
        chipVideos = findViewById(R.id.chip_album_videos);

        btnShare = findViewById(R.id.btn_album_share);
        btnFav = findViewById(R.id.btn_album_fav);
        btnBin = findViewById(R.id.btn_album_bin);

        tvTitle.setText(albumName);

        btnBack.setOnClickListener(v -> {
            if (adapter != null && adapter.isSelectMode()) {
                adapter.exitSelectMode();
                actionBar.setVisibility(View.GONE);
                tvTitle.setText(albumName);
            } else {
                finish();
            }
        });

        btnOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add(0, 1, 0, "Share All");
            popup.getMenu().add(0, 2, 1, "Album Info");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    shareAllMedia();
                    return true;
                } else if (item.getItemId() == 2) {
                    showAlbumInfo();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // Setup RecyclerView with 3 columns
        adapter = new GalleryAdapter(new GalleryAdapter.Listener() {
            @Override
            public void onMediaClick(MediaItem item, int index, List<MediaItem> flatList) {
                openImageViewerPager(flatList, index);
            }

            @Override
            public void onSelectionChanged(int count) {
                if (count == 0) {
                    actionBar.setVisibility(View.GONE);
                    tvTitle.setText(albumName);
                } else {
                    actionBar.setVisibility(View.VISIBLE);
                    tvTitle.setText(count + " selected");
                }
            }

            @Override
            public void onMultiSelectStart() {
                actionBar.setVisibility(View.VISIBLE);
            }
        });

        GridLayoutManager glm = new GridLayoutManager(this, 3);
        glm.setSpanSizeLookup(adapter.getSpanSizeLookup());
        recyclerMedia.setLayoutManager(glm);
        recyclerMedia.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadAlbumMedia);

        // Filter chips setup
        chipAll.setOnClickListener(v -> {
            chipAll.setChecked(true);
            chipPhotos.setChecked(false);
            chipVideos.setChecked(false);
            adapter.setFilter(GalleryAdapter.FILTER_ALL);
        });
        chipPhotos.setOnClickListener(v -> {
            chipAll.setChecked(false);
            chipPhotos.setChecked(true);
            chipVideos.setChecked(false);
            adapter.setFilter(GalleryAdapter.FILTER_PHOTOS);
        });
        chipVideos.setOnClickListener(v -> {
            chipAll.setChecked(false);
            chipPhotos.setChecked(false);
            chipVideos.setChecked(true);
            adapter.setFilter(GalleryAdapter.FILTER_VIDEOS);
        });

        // Multi-select action buttons
        btnShare.setOnClickListener(v -> shareSelected());
        btnFav.setOnClickListener(v -> favoriteSelected());
        btnBin.setOnClickListener(v -> binSelected());
    }

    private void loadAlbumMedia() {
        swipeRefresh.setRefreshing(true);
        new Thread(() -> {
            List<MediaItem> list = MediaHelper.getMediaForAlbum(this, albumName, bucketId, specialType);
            currentAlbumMedia = list;
            runOnUiThread(() -> {
                swipeRefresh.setRefreshing(false);
                adapter.setItems(list);
                tvSubtitle.setText(list.size() + (list.size() == 1 ? " item" : " items"));
                if (list.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    private void shareSelected() {
        List<MediaItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) return;

        if (selected.size() == 1) {
            MediaItem item = selected.get(0);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(item.isVideo() ? "video/*" : "image/*");
            intent.putExtra(Intent.EXTRA_STREAM, item.getUri());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share"));
        } else {
            ArrayList<Uri> uris = new ArrayList<>();
            for (MediaItem m : selected) uris.add(m.getUri());
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share " + selected.size() + " items"));
        }
        adapter.exitSelectMode();
        actionBar.setVisibility(View.GONE);
        tvTitle.setText(albumName);
    }

    private void favoriteSelected() {
        List<MediaItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) return;

        for (MediaItem item : selected) {
            FavoritesHelper.addFavorite(this, item.getId());
        }
        Toast.makeText(this, selected.size() + " item(s) added to Favorites", Toast.LENGTH_SHORT).show();
        adapter.exitSelectMode();
        actionBar.setVisibility(View.GONE);
        tvTitle.setText(albumName);
        if (specialType == AlbumItem.TYPE_FAVORITES) {
            loadAlbumMedia();
        }
    }

    private void binSelected() {
        List<MediaItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) return;

        for (MediaItem item : selected) {
            RecycleBinHelper.addToRecycleBin(this, item.getId(), item.getUri().toString(), item.getName());
        }
        Toast.makeText(this, selected.size() + " item(s) moved to Recycle Bin", Toast.LENGTH_SHORT).show();
        adapter.exitSelectMode();
        actionBar.setVisibility(View.GONE);
        tvTitle.setText(albumName);
        loadAlbumMedia();
    }

    private void shareAllMedia() {
        if (currentAlbumMedia.isEmpty()) {
            Toast.makeText(this, "Album is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<Uri> uris = new ArrayList<>();
        for (MediaItem m : currentAlbumMedia) uris.add(m.getUri());
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share all " + currentAlbumMedia.size() + " items"));
    }

    private void showAlbumInfo() {
        long totalBytes = 0;
        int photoCount = 0;
        int videoCount = 0;
        for (MediaItem item : currentAlbumMedia) {
            totalBytes += item.getSize();
            if (item.isVideo()) videoCount++;
            else photoCount++;
        }
        String sizeFormatted = String.format("%.2f MB", totalBytes / (1024.0 * 1024.0));

        new AlertDialog.Builder(this)
                .setTitle("Album Info")
                .setMessage("Name: " + albumName + "\n" +
                        "Total Items: " + currentAlbumMedia.size() + " (" + photoCount + " photos, " + videoCount + " videos)\n" +
                        "Total Size: " + sizeFormatted)
                .setPositiveButton("OK", null)
                .show();
    }

    private void openImageViewerPager(List<MediaItem> list, int startIdx) {
        if (list == null || list.isEmpty()) return;
        Intent intent = new Intent(this, ImageViewerActivity.class);
        ArrayList<String> uriList = new ArrayList<>();
        ArrayList<Long> idList = new ArrayList<>();
        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<Long> sizeList = new ArrayList<>();
        ArrayList<Integer> wList = new ArrayList<>();
        ArrayList<Integer> hList = new ArrayList<>();
        ArrayList<Long> dateList = new ArrayList<>();
        ArrayList<Boolean> vidList = new ArrayList<>();
        ArrayList<Long> durList = new ArrayList<>();
        for (MediaItem m : list) {
            uriList.add(m.getUri().toString());
            idList.add(m.getId());
            nameList.add(m.getName());
            sizeList.add(m.getSize());
            wList.add(m.getWidth());
            hList.add(m.getHeight());
            dateList.add(m.getDateAdded());
            vidList.add(m.isVideo());
            durList.add(m.getDuration());
        }
        intent.putStringArrayListExtra(ImageViewerActivity.EXTRA_URI_LIST, uriList);
        intent.putExtra(ImageViewerActivity.EXTRA_ID_LIST, idList);
        intent.putStringArrayListExtra(ImageViewerActivity.EXTRA_NAME_LIST, nameList);
        intent.putExtra(ImageViewerActivity.EXTRA_SIZE_LIST, sizeList);
        intent.putIntegerArrayListExtra(ImageViewerActivity.EXTRA_W_LIST, wList);
        intent.putIntegerArrayListExtra(ImageViewerActivity.EXTRA_H_LIST, hList);
        intent.putExtra(ImageViewerActivity.EXTRA_DATE_LIST, dateList);
        intent.putExtra(ImageViewerActivity.EXTRA_VIDEO_LIST, vidList);
        intent.putExtra(ImageViewerActivity.EXTRA_DUR_LIST, durList);
        intent.putExtra(ImageViewerActivity.EXTRA_START_IDX, startIdx);
        startActivityForResult(intent, 2002);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2002 && resultCode == RESULT_OK) {
            loadAlbumMedia();
        }
    }

    @Override
    public void onBackPressed() {
        if (adapter != null && adapter.isSelectMode()) {
            adapter.exitSelectMode();
            actionBar.setVisibility(View.GONE);
            tvTitle.setText(albumName);
        } else {
            super.onBackPressed();
        }
    }
}
