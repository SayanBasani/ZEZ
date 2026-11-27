package com.zez.manager.gallery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.zez.manager.MainActivity;
import com.zez.manager.R;
import com.zez.manager.gallery.adapter.AlbumsAdapter;
import com.zez.manager.gallery.adapter.GalleryAdapter;
import com.zez.manager.gallery.adapter.RecycleBinAdapter;
import com.zez.manager.gallery.model.AlbumItem;
import com.zez.manager.gallery.model.MediaItem;
import com.zez.manager.gallery.util.MediaHelper;
import com.zez.manager.gallery.util.RecycleBinHelper;

import java.util.ArrayList;
import java.util.List;

public class GalleryViewController {

    private static final int GALLERY_TAB_PHOTOS = 0;
    private static final int GALLERY_TAB_ALBUMS = 1;

    public static final int REQUEST_IMAGE_VIEWER = 2001;
    public static final int REQUEST_ALBUM_DETAIL = 2002;

    private final MainActivity activity;

    private TabLayout galleryTabLayout;
    private HorizontalScrollView galleryFilterScroll;
    private Chip chipAll, chipPhotos, chipVideos;
    private SwipeRefreshLayout swipeGallery2;
    private RecyclerView recyclerGallery2;
    private SwipeRefreshLayout swipeAlbums;
    private RecyclerView recyclerAlbums;
    private LinearLayout layoutGalleryEmpty;
    private TextView tvGalleryEmptySubtitle;
    private Button btnGalleryGrantPermission;

    private LinearLayout galleryActionBar;
    private View btnGalleryShare, btnGalleryMove, btnGalleryCreate, btnGalleryBin, btnGalleryMore;

    private GalleryAdapter galleryAdapter;
    private AlbumsAdapter albumsAdapter;

    private List<MediaItem> allMediaCache = new ArrayList<>();
    private int currentGalleryTab = GALLERY_TAB_PHOTOS;

    public GalleryViewController(MainActivity activity) {
        this.activity = activity;
        initViews();
        setupAdapters();
        setupListeners();
    }

    private void initViews() {
        galleryTabLayout         = activity.findViewById(R.id.gallery_tab_layout);
        galleryFilterScroll      = activity.findViewById(R.id.gallery_filter_scroll);
        chipAll                  = activity.findViewById(R.id.chip_all);
        chipPhotos               = activity.findViewById(R.id.chip_photos);
        chipVideos               = activity.findViewById(R.id.chip_videos);
        swipeGallery2            = activity.findViewById(R.id.swipe_refresh_gallery2);
        recyclerGallery2         = activity.findViewById(R.id.recycler_gallery2);
        swipeAlbums              = activity.findViewById(R.id.swipe_refresh_albums);
        recyclerAlbums           = activity.findViewById(R.id.recycler_albums);
        layoutGalleryEmpty       = activity.findViewById(R.id.layout_gallery_empty);

        if (layoutGalleryEmpty != null) {
            tvGalleryEmptySubtitle = layoutGalleryEmpty.findViewById(R.id.tv_gallery_empty_subtitle);
            btnGalleryGrantPermission = layoutGalleryEmpty.findViewById(R.id.btn_gallery_grant_permission);
            if (btnGalleryGrantPermission != null) {
                btnGalleryGrantPermission.setOnClickListener(v -> {
                    // Let MainActivity handle permission request
                    activity.requestGalleryPermissions();
                });
            }
        }

        galleryActionBar = activity.findViewById(R.id.gallery_action_bar);
        btnGalleryShare  = activity.findViewById(R.id.btn_gallery_share);
        btnGalleryMove   = activity.findViewById(R.id.btn_gallery_move);
        btnGalleryCreate = activity.findViewById(R.id.btn_gallery_create);
        btnGalleryBin    = activity.findViewById(R.id.btn_gallery_bin);
        btnGalleryMore   = activity.findViewById(R.id.btn_gallery_more);
    }

    private void setupAdapters() {
        // Photos 3-column grid
        GridLayoutManager glm = new GridLayoutManager(activity, 3);
        galleryAdapter = new GalleryAdapter(new GalleryAdapter.Listener() {
            @Override
            public void onMediaClick(MediaItem item, int idx, List<MediaItem> flat) {
                openImageViewerPager(flat, idx);
            }

            @Override
            public void onSelectionChanged(int count) {
                if (count > 0) {
                    activity.updateHeaderForSelection(count + " selected", "Tap checkmark to toggle, More for PDF/PPT");
                    if (galleryActionBar != null) galleryActionBar.setVisibility(View.VISIBLE);
                } else {
                    activity.resetHeaderTitle();
                    if (galleryActionBar != null) galleryActionBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onMultiSelectStart() {
                if (galleryActionBar != null) galleryActionBar.setVisibility(View.VISIBLE);
            }
        });
        glm.setSpanSizeLookup(galleryAdapter.getSpanSizeLookup());
        recyclerGallery2.setLayoutManager(glm);
        recyclerGallery2.setAdapter(galleryAdapter);

        // Albums 2-column grid
        albumsAdapter = new AlbumsAdapter(album -> {
            if (album.specialType == AlbumItem.TYPE_BIN) {
                openRecycleBinDialog();
                return;
            }
            Intent intent = new Intent(activity, AlbumDetailActivity.class);
            intent.putExtra(AlbumDetailActivity.EXTRA_ALBUM_NAME, album.name);
            intent.putExtra(AlbumDetailActivity.EXTRA_BUCKET_ID, album.bucketId);
            intent.putExtra(AlbumDetailActivity.EXTRA_SPECIAL_TYPE, album.specialType);
            activity.startActivityForResult(intent, REQUEST_ALBUM_DETAIL);
        });
        recyclerAlbums.setLayoutManager(new GridLayoutManager(activity, 2));
        recyclerAlbums.setAdapter(albumsAdapter);
    }

    private void setupListeners() {
        // Swipe to refresh
        if (swipeGallery2 != null) swipeGallery2.setOnRefreshListener(this::loadGalleryData);
        if (swipeAlbums != null) swipeAlbums.setOnRefreshListener(this::loadGalleryData);

        // Filter chips
        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                chipAll.setChecked(true);
                if (chipPhotos != null) chipPhotos.setChecked(false);
                if (chipVideos != null) chipVideos.setChecked(false);
                galleryAdapter.setFilter(GalleryAdapter.FILTER_ALL);
            });
        }
        if (chipPhotos != null) {
            chipPhotos.setOnClickListener(v -> {
                if (chipAll != null) chipAll.setChecked(false);
                chipPhotos.setChecked(true);
                if (chipVideos != null) chipVideos.setChecked(false);
                galleryAdapter.setFilter(GalleryAdapter.FILTER_PHOTOS);
            });
        }
        if (chipVideos != null) {
            chipVideos.setOnClickListener(v -> {
                if (chipAll != null) chipAll.setChecked(false);
                if (chipPhotos != null) chipPhotos.setChecked(false);
                chipVideos.setChecked(true);
                galleryAdapter.setFilter(GalleryAdapter.FILTER_VIDEOS);
            });
        }

        // TabLayout switching
        if (galleryTabLayout != null) {
            galleryTabLayout.addTab(galleryTabLayout.newTab().setText("Photos"));
            galleryTabLayout.addTab(galleryTabLayout.newTab().setText("Albums"));
            galleryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    currentGalleryTab = tab.getPosition();
                    if (currentGalleryTab == GALLERY_TAB_PHOTOS) {
                        if (swipeGallery2 != null) swipeGallery2.setVisibility(View.VISIBLE);
                        if (swipeAlbums != null) swipeAlbums.setVisibility(View.GONE);
                        if (galleryFilterScroll != null) galleryFilterScroll.setVisibility(View.VISIBLE);
                    } else {
                        if (swipeGallery2 != null) swipeGallery2.setVisibility(View.GONE);
                        if (swipeAlbums != null) swipeAlbums.setVisibility(View.VISIBLE);
                        if (galleryFilterScroll != null) galleryFilterScroll.setVisibility(View.GONE);
                        if (galleryAdapter != null && galleryAdapter.isSelectMode()) {
                            galleryAdapter.exitSelectMode();
                        }
                    }
                }

                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        // Multi-select actions (Vivo Album style)
        if (btnGalleryShare != null) btnGalleryShare.setOnClickListener(v -> shareSelectedMedia());
        if (btnGalleryBin != null) btnGalleryBin.setOnClickListener(v -> moveSelectedToBin());
        if (btnGalleryMove != null) btnGalleryMove.setOnClickListener(v -> Toast.makeText(activity, "Move/Copy option coming soon", Toast.LENGTH_SHORT).show());
        if (btnGalleryCreate != null) btnGalleryCreate.setOnClickListener(v -> Toast.makeText(activity, "Create option coming soon", Toast.LENGTH_SHORT).show());
        if (btnGalleryMore != null) btnGalleryMore.setOnClickListener(this::showGalleryMorePopup);
    }

    public void onTabSelected() {
        loadGalleryData();
    }

    public void loadGalleryData() {
        if (!hasStoragePermission()) {
            if (layoutGalleryEmpty != null) {
                layoutGalleryEmpty.setVisibility(View.VISIBLE);
                if (tvGalleryEmptySubtitle != null) {
                    tvGalleryEmptySubtitle.setText("Storage permission is required to view your photos and videos.");
                }
                if (btnGalleryGrantPermission != null) {
                    btnGalleryGrantPermission.setVisibility(View.VISIBLE);
                }
            }
            if (swipeGallery2 != null) swipeGallery2.setRefreshing(false);
            if (swipeAlbums != null) swipeAlbums.setRefreshing(false);
            return;
        }

        if (btnGalleryGrantPermission != null) {
            btnGalleryGrantPermission.setVisibility(View.GONE);
        }
        if (tvGalleryEmptySubtitle != null) {
            tvGalleryEmptySubtitle.setText("No photos or videos found on device");
        }

        if (swipeGallery2 != null) swipeGallery2.setRefreshing(true);
        if (swipeAlbums != null) swipeAlbums.setRefreshing(true);

        new Thread(() -> {
            List<MediaItem> allMedia = MediaHelper.getImagesAndVideos(activity);
            List<MediaItem> activeMedia = new ArrayList<>();
            for (MediaItem item : allMedia) {
                if (!RecycleBinHelper.isRecycled(activity, item.getId())) {
                    activeMedia.add(item);
                }
            }
            allMediaCache = activeMedia;

            List<AlbumItem> albums = MediaHelper.getAlbums(activity);

            activity.runOnUiThread(() -> {
                if (swipeGallery2 != null) swipeGallery2.setRefreshing(false);
                if (swipeAlbums != null) swipeAlbums.setRefreshing(false);
                if (galleryAdapter != null) galleryAdapter.setItems(activeMedia);
                if (albumsAdapter != null) albumsAdapter.setItems(albums);

                if (activeMedia.isEmpty()) {
                    if (layoutGalleryEmpty != null) layoutGalleryEmpty.setVisibility(View.VISIBLE);
                } else {
                    if (layoutGalleryEmpty != null) layoutGalleryEmpty.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void shareSelectedMedia() {
        if (galleryAdapter == null) return;
        List<MediaItem> selected = galleryAdapter.getSelectedItems();
        if (selected.isEmpty()) return;

        if (selected.size() == 1) {
            MediaItem item = selected.get(0);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(item.isVideo() ? "video/*" : "image/*");
            intent.putExtra(Intent.EXTRA_STREAM, item.getUri());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(Intent.createChooser(intent, "Share via"));
        } else {
            ArrayList<Uri> uris = new ArrayList<>();
            for (MediaItem m : selected) uris.add(m.getUri());
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("image/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(Intent.createChooser(intent, "Share " + selected.size() + " items"));
        }
        galleryAdapter.exitSelectMode();
    }

    private void moveSelectedToBin() {
        if (galleryAdapter == null) return;
        List<MediaItem> selected = galleryAdapter.getSelectedItems();
        if (selected.isEmpty()) return;

        for (MediaItem item : selected) {
            RecycleBinHelper.addToRecycleBin(activity, item.getId(), item.getUri().toString(), item.getName());
        }
        Toast.makeText(activity, selected.size() + " item(s) moved to Recycle Bin", Toast.LENGTH_SHORT).show();
        galleryAdapter.exitSelectMode();
        activity.resetHeaderTitle();
        if (galleryActionBar != null) galleryActionBar.setVisibility(View.GONE);
        loadGalleryData();
    }

    private void showGalleryMorePopup(View anchor) {
        PopupMenu popup = new PopupMenu(activity, anchor);
        popup.getMenu().add(0, 1, 0, "Hide");
        popup.getMenu().add(0, 2, 1, "Move to Private space");
        popup.getMenu().add(0, 3, 2, "Copy to clipboard");
        popup.getMenu().add(0, 4, 3, "PDF/PPT");
        popup.getMenu().add(0, 5, 4, "Print");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 4) {
                launchPdfGeneratorFromGallery();
            } else if (id == 1) {
                Toast.makeText(activity, "Hidden", Toast.LENGTH_SHORT).show();
            } else if (id == 2) {
                Toast.makeText(activity, "Moved to Private space", Toast.LENGTH_SHORT).show();
            } else if (id == 3) {
                Toast.makeText(activity, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            } else if (id == 5) {
                Toast.makeText(activity, "Printing...", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    public void launchPdfGeneratorFromGallery() {
        if (galleryAdapter == null) return;
        List<MediaItem> selected = galleryAdapter.getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            Toast.makeText(activity, "Select at least one image", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> uris = new ArrayList<>();
        for (MediaItem item : selected) {
            if (item != null && item.getUri() != null) {
                uris.add(item.getUri().toString());
            }
        }
        if (uris.isEmpty()) {
            Toast.makeText(activity, "No valid images selected", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(activity, PdfGeneratorActivity.class);
        intent.putStringArrayListExtra(PdfGeneratorActivity.EXTRA_IMAGE_URIS, uris);
        activity.startActivity(intent);
    }

    private void openImageViewerPager(List<MediaItem> list, int startIdx) {
        if (list == null || list.isEmpty()) return;
        Intent intent = new Intent(activity, ImageViewerActivity.class);

        ArrayList<String>  uriList  = new ArrayList<>();
        ArrayList<Long>    idList   = new ArrayList<>();
        ArrayList<String>  nameList = new ArrayList<>();
        ArrayList<Long>    sizeList = new ArrayList<>();
        ArrayList<Integer> wList    = new ArrayList<>();
        ArrayList<Integer> hList    = new ArrayList<>();
        ArrayList<Long>    dateList = new ArrayList<>();
        ArrayList<Boolean> vidList  = new ArrayList<>();
        ArrayList<Long>    durList  = new ArrayList<>();

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

        activity.startActivityForResult(intent, REQUEST_IMAGE_VIEWER);
    }

    private void openRecycleBinDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_pdf_files, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tv_pdf_files_title);
        if (tvTitle != null) tvTitle.setText("Recycle Bin");

        RecyclerView rvBin = view.findViewById(R.id.rv_saved_files);
        TextView tvEmpty = view.findViewById(R.id.tv_empty_files);
        if (tvEmpty != null) tvEmpty.setText("Recycle Bin is empty");
        View btnClose = view.findViewById(R.id.btn_close_files_dialog);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        List<RecycleBinHelper.RecycledItem> items = RecycleBinHelper.getRecycledItems(activity);

        if (items.isEmpty()) {
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            if (rvBin != null) rvBin.setVisibility(View.GONE);
        } else {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
            if (rvBin != null) {
                rvBin.setVisibility(View.VISIBLE);
                rvBin.setLayoutManager(new LinearLayoutManager(activity));
                RecycleBinAdapter binAdapter = new RecycleBinAdapter(item -> {
                    RecycleBinHelper.restoreItem(activity, item.mediaId);
                    Toast.makeText(activity, "Restored: " + item.name, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadGalleryData();
                });
                binAdapter.setItems(items);
                rvBin.setAdapter(binAdapter);
            }
        }

        dialog.show();
    }

    public boolean onBackPressed() {
        if (galleryAdapter != null && galleryAdapter.isSelectMode()) {
            galleryAdapter.exitSelectMode();
            activity.resetHeaderTitle();
            if (galleryActionBar != null) galleryActionBar.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_IMAGE_VIEWER || requestCode == REQUEST_ALBUM_DETAIL) && resultCode == activity.RESULT_OK) {
            loadGalleryData();
        }
    }
}
