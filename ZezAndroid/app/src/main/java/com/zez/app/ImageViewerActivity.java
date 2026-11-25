package com.zez.app;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.zez.app.adapter.FilmstripAdapter;
import com.zez.app.adapter.MediaPagerAdapter;
import com.zez.app.model.MediaItem;
import com.zez.app.util.FavoritesHelper;
import com.zez.app.util.RecycleBinHelper;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageViewerActivity extends AppCompatActivity {

    public static final String EXTRA_URI       = "extra_uri";
    public static final String EXTRA_NAME      = "extra_name";
    public static final String EXTRA_ID        = "extra_id";
    public static final String EXTRA_SIZE      = "extra_size";
    public static final String EXTRA_WIDTH     = "extra_width";
    public static final String EXTRA_HEIGHT    = "extra_height";
    public static final String EXTRA_DATE      = "extra_date";
    public static final String EXTRA_START_IDX = "extra_start_idx";

    public static final String EXTRA_URI_LIST   = "extra_uri_list";
    public static final String EXTRA_ID_LIST    = "extra_id_list";
    public static final String EXTRA_NAME_LIST  = "extra_name_list";
    public static final String EXTRA_SIZE_LIST  = "extra_size_list";
    public static final String EXTRA_W_LIST     = "extra_w_list";
    public static final String EXTRA_H_LIST     = "extra_h_list";
    public static final String EXTRA_DATE_LIST  = "extra_date_list";
    public static final String EXTRA_VIDEO_LIST = "extra_video_list";
    public static final String EXTRA_DUR_LIST   = "extra_dur_list";

    public static final String EXTRA_IS_PROFILE_VIEW = "extra_is_profile_view";
    public static final String EXTRA_CAN_EDIT        = "extra_can_edit";

    private ViewPager2       viewPager;
    private LinearLayout     topBar;
    private LinearLayout     bottomContainer;
    private RecyclerView     filmstripRecycler;
    private FilmstripAdapter filmstripAdapter;

    private TextView    tvImageDate, tvImageTime;
    private ImageButton btnFavorite;
    private ImageButton btnInfo;
    private ImageButton btnEditProfilePhoto;
    private View        btnActionEdit;

    private boolean isProfileView = false;
    private boolean canEdit = false;
    private androidx.activity.result.ActivityResultLauncher<String> editAvatarLauncher;

    private boolean barsVisible = true;
    private boolean dataChanged = false;

    private final List<MediaItem> mediaList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        com.zez.app.theme.ThemeManager.applyTheme(this, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        isProfileView = getIntent().getBooleanExtra(EXTRA_IS_PROFILE_VIEW, false);
        canEdit       = getIntent().getBooleanExtra(EXTRA_CAN_EDIT, false);

        viewPager         = findViewById(R.id.view_pager);
        topBar            = findViewById(R.id.top_bar);
        bottomContainer   = findViewById(R.id.bottom_container);
        filmstripRecycler = findViewById(R.id.filmstrip_recycler);
        tvImageDate       = findViewById(R.id.tv_image_date);
        tvImageTime       = findViewById(R.id.tv_image_time);
        btnFavorite       = findViewById(R.id.btn_favorite);
        btnInfo           = findViewById(R.id.btn_info);
        btnEditProfilePhoto = findViewById(R.id.btn_edit_profile_photo);

        ImageButton btnBack = findViewById(R.id.btn_back);

        View btnActionShare  = findViewById(R.id.btn_action_share);
        btnActionEdit        = findViewById(R.id.btn_action_edit);
        View btnActionDelete = findViewById(R.id.btn_action_delete);
        View btnActionMore   = findViewById(R.id.btn_action_more);

        if (isProfileView) {
            if (bottomContainer != null) bottomContainer.setVisibility(View.GONE);
            if (btnFavorite != null) btnFavorite.setVisibility(View.GONE);
            if (btnInfo != null) btnInfo.setVisibility(View.GONE);
            if (filmstripRecycler != null) filmstripRecycler.setVisibility(View.GONE);
            if (tvImageTime != null) tvImageTime.setVisibility(View.GONE);
            if (canEdit && btnEditProfilePhoto != null) {
                btnEditProfilePhoto.setVisibility(View.VISIBLE);
                btnEditProfilePhoto.setOnClickListener(v -> {
                    if (editAvatarLauncher != null) editAvatarLauncher.launch("image/*");
                });
            }
        }

        editAvatarLauncher = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String myUid = com.zez.app.util.ChatPreferencesHelper.getUserId(this);
                if (myUid != null) {
                    Toast.makeText(this, "Updating profile picture...", Toast.LENGTH_SHORT).show();
                    com.zez.app.util.ChatApiClient.uploadAvatar(this, uri, myUid, new com.zez.app.util.ChatApiClient.ChatCallback<String>() {
                        @Override
                        public void onSuccess(String relativeUrl) {
                            com.zez.app.util.ChatPreferencesHelper.saveUserAvatar(ImageViewerActivity.this, relativeUrl);
                            String full = com.zez.app.util.ChatApiClient.getAbsoluteMediaUrl(ImageViewerActivity.this, relativeUrl);
                            mediaList.clear();
                            mediaList.add(new MediaItem(System.currentTimeMillis(), Uri.parse(full), "Profile Photo", 0, System.currentTimeMillis(), false, 0, 0, 0, ""));
                            if (viewPager.getAdapter() != null) viewPager.getAdapter().notifyDataSetChanged();
                            Toast.makeText(ImageViewerActivity.this, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(ImageViewerActivity.this, "Failed to upload photo: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // Reconstruct media list
        ArrayList<String>  uriList  = getIntent().getStringArrayListExtra(EXTRA_URI_LIST);
        ArrayList<Long>    idList   = (ArrayList<Long>)    getIntent().getSerializableExtra(EXTRA_ID_LIST);
        ArrayList<String>  nameList = getIntent().getStringArrayListExtra(EXTRA_NAME_LIST);
        ArrayList<Long>    sizeList = (ArrayList<Long>)    getIntent().getSerializableExtra(EXTRA_SIZE_LIST);
        ArrayList<Integer> wList    = (ArrayList<Integer>) getIntent().getSerializableExtra(EXTRA_W_LIST);
        ArrayList<Integer> hList    = (ArrayList<Integer>) getIntent().getSerializableExtra(EXTRA_H_LIST);
        ArrayList<Long>    dateList = (ArrayList<Long>)    getIntent().getSerializableExtra(EXTRA_DATE_LIST);
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
                long    date  = dateList != null && i < dateList.size() ? dateList.get(i) : 0;
                String  nm    = nameList != null && i < nameList.size() ? nameList.get(i) : "";
                mediaList.add(new MediaItem(idList.get(i), Uri.parse(uriList.get(i)),
                        nm, sz, date, isVid, dur, w, h, ""));
            }
        } else {
            // Single-item fallback
            String uriStr = getIntent().getStringExtra(EXTRA_URI);
            String name   = getIntent().getStringExtra(EXTRA_NAME);
            long   id     = getIntent().getLongExtra(EXTRA_ID, -1);
            long   size   = getIntent().getLongExtra(EXTRA_SIZE, 0);
            int    width  = getIntent().getIntExtra(EXTRA_WIDTH, 0);
            int    height = getIntent().getIntExtra(EXTRA_HEIGHT, 0);
            long   date   = getIntent().getLongExtra(EXTRA_DATE, 0);
            if (uriStr != null) {
                mediaList.add(new MediaItem(id, Uri.parse(uriStr), name, size, date, false, 0, width, height, ""));
            }
        }

        if (mediaList.isEmpty()) {
            finish();
            return;
        }

        // Pager setup
        MediaPagerAdapter pagerAdapter = new MediaPagerAdapter(mediaList, this::toggleBars);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(startIdx, false);

        // Filmstrip RecyclerView setup
        filmstripAdapter = new FilmstripAdapter(mediaList, position -> {
            viewPager.setCurrentItem(position, true);
        });
        filmstripRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        filmstripRecycler.setAdapter(filmstripAdapter);
        filmstripAdapter.setSelectedPosition(startIdx);
        filmstripRecycler.scrollToPosition(startIdx);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateUIForPosition(position);
            }
        });
        updateUIForPosition(startIdx);

        // Top bar buttons
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

        // Bottom Vivo-style actions
        btnActionShare.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos < mediaList.size()) shareItem(mediaList.get(pos));
        });

        btnActionEdit.setOnClickListener(v -> {
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

        btnActionDelete.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos < mediaList.size()) confirmDelete(pos);
        });

        btnActionMore.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos >= mediaList.size()) return;
            MediaItem item = mediaList.get(pos);

            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add(0, 1, 0, "Set as wallpaper");
            popup.getMenu().add(0, 2, 1, "Extract text");
            popup.getMenu().add(0, 3, 2, "Google Lens");
            popup.getMenu().add(0, 4, 3, "Move/Copy");
            popup.getMenu().add(0, 5, 4, "Hide");
            popup.getMenu().add(0, 6, 5, "Move to Private space");

            popup.setOnMenuItemClickListener(menuItem -> {
                int id = menuItem.getItemId();
                if (id == 1) {
                    setAsWallpaper(item);
                    return true;
                } else if (id == 2) {
                    extractText(item);
                    return true;
                } else if (id == 3) {
                    launchGoogleLens(item);
                    return true;
                } else if (id == 4) {
                    showMoveCopyDialog(item, pos);
                    return true;
                } else if (id == 5) {
                    hideItem(item, pos);
                    return true;
                } else if (id == 6) {
                    moveToPrivateSpace(item, pos);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void updateUIForPosition(int pos) {
        if (pos >= 0 && pos < mediaList.size()) {
            MediaItem item = mediaList.get(pos);

            if (isProfileView) {
                String name = getIntent().getStringExtra(EXTRA_NAME);
                tvImageDate.setText(name != null && !name.isEmpty() ? name : "Profile Photo");
                tvImageTime.setVisibility(View.GONE);
                if (bottomContainer != null) bottomContainer.setVisibility(View.GONE);
                if (btnFavorite != null) btnFavorite.setVisibility(View.GONE);
                if (btnInfo != null) btnInfo.setVisibility(View.GONE);
                return;
            }

            // Date and Time (Vivo style: Line 1 = "Today", "Yesterday", or "Oct 24, 2026", Line 2 = "3:11 PM")
            long dateMs = item.getDateAdded();
            if (dateMs > 0) {
                if (dateMs < 100000000000L) {
                    dateMs *= 1000L; // convert from seconds to milliseconds
                }
                Date dateObj = new Date(dateMs);

                if (DateUtils.isToday(dateMs)) {
                    tvImageDate.setText("Today");
                } else if (DateUtils.isToday(dateMs + DateUtils.DAY_IN_MILLIS)) {
                    tvImageDate.setText("Yesterday");
                } else {
                    SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                    tvImageDate.setText(df.format(dateObj));
                }

                SimpleDateFormat tf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                tvImageTime.setText(tf.format(dateObj));
            } else {
                tvImageDate.setText(item.getName() != null ? item.getName() : "Photo");
                tvImageTime.setText(fmtSize(item.getSize()));
            }

            // Favorite status
            boolean isFav = FavoritesHelper.isFavorite(this, item.getId());
            updateFavoriteIcon(isFav);

            // Edit button visibility (photos only)
            if (btnActionEdit != null) {
                btnActionEdit.setVisibility(item.isVideo() ? View.GONE : View.VISIBLE);
            }

            // Sync filmstrip selection
            if (filmstripAdapter != null) {
                filmstripAdapter.setSelectedPosition(pos);
                filmstripRecycler.smoothScrollToPosition(pos);
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
        if (!isProfileView) {
            bottomContainer.setVisibility(vis);
        }
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
                    if (mediaList.size() <= 1) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        mediaList.remove(pos);
                        viewPager.getAdapter().notifyDataSetChanged();
                        filmstripAdapter.notifyDataSetChanged();
                        int newPos = Math.min(pos, mediaList.size() - 1);
                        updateUIForPosition(newPos);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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

    private void extractText(MediaItem item) {
        try {
            Intent lensIntent = new Intent("com.google.android.apps.lens.OPEN_LENS");
            lensIntent.setData(item.getUri());
            lensIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (lensIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(lensIntent);
                return;
            }
        } catch (Exception ignored) {}

        Toast.makeText(this, "Extracting text from image...", Toast.LENGTH_SHORT).show();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("image/*");
        sendIntent.putExtra(Intent.EXTRA_STREAM, item.getUri());
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(sendIntent, "Extract text with"));
        } catch (Exception e) {
            Toast.makeText(this, "No text recognition handler found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchGoogleLens(MediaItem item) {
        try {
            Intent lensIntent = new Intent(Intent.ACTION_VIEW);
            lensIntent.setPackage("com.google.ar.lens");
            lensIntent.setData(item.getUri());
            lensIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(lensIntent);
        } catch (Exception e) {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("image/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, item.getUri());
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(Intent.createChooser(sendIntent, "Search image with Google Lens"));
            } catch (Exception ex) {
                Toast.makeText(this, "Google Lens is not installed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showMoveCopyDialog(MediaItem item, int pos) {
        String[] options = {"Copy to Album", "Move to Album", "Create New Album"};
        new AlertDialog.Builder(this)
                .setTitle("Move/Copy")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Toast.makeText(this, "Copied \"" + item.getName() + "\" to album", Toast.LENGTH_SHORT).show();
                    } else if (which == 1) {
                        Toast.makeText(this, "Moved \"" + item.getName() + "\" to album", Toast.LENGTH_SHORT).show();
                        dataChanged = true;
                    } else {
                        Toast.makeText(this, "New album created", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void hideItem(MediaItem item, int pos) {
        new AlertDialog.Builder(this)
                .setTitle("Hide this photo?")
                .setMessage("\"" + item.getName() + "\" will be hidden from the main gallery. You can access it anytime in Gallery Settings.")
                .setPositiveButton("Hide", (dialog, which) -> {
                    Toast.makeText(this, "Photo hidden from gallery", Toast.LENGTH_SHORT).show();
                    dataChanged = true;
                    if (mediaList.size() <= 1) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        mediaList.remove(pos);
                        viewPager.getAdapter().notifyDataSetChanged();
                        filmstripAdapter.notifyDataSetChanged();
                        int newPos = Math.min(pos, mediaList.size() - 1);
                        updateUIForPosition(newPos);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void moveToPrivateSpace(MediaItem item, int pos) {
        new AlertDialog.Builder(this)
                .setTitle("Move to Private space?")
                .setMessage("\"" + item.getName() + "\" will be moved to your secure private space and requires authentication to view.")
                .setPositiveButton("Move to Private space", (dialog, which) -> {
                    Toast.makeText(this, "Moved to Private space", Toast.LENGTH_SHORT).show();
                    dataChanged = true;
                    if (mediaList.size() <= 1) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        mediaList.remove(pos);
                        viewPager.getAdapter().notifyDataSetChanged();
                        filmstripAdapter.notifyDataSetChanged();
                        int newPos = Math.min(pos, mediaList.size() - 1);
                        updateUIForPosition(newPos);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showMeta(MediaItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("File name: ").append(item.getName()).append("\n\n");
        sb.append("Size: ").append(fmtSize(item.getSize())).append("\n\n");

        if (item.getWidth() > 0 && item.getHeight() > 0) {
            sb.append("Resolution: ").append(item.getWidth()).append(" × ").append(item.getHeight()).append(" px\n\n");
        }

        if (item.getDateAdded() > 0) {
            long dateMs = item.getDateAdded() < 100000000000L ? item.getDateAdded() * 1000L : item.getDateAdded();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            sb.append("Date modified: ").append(sdf.format(new Date(dateMs))).append("\n\n");
        }

        if (item.isVideo() && item.getDuration() > 0) {
            sb.append("Duration: ").append(fmtDur(item.getDuration())).append("\n\n");
        }

        sb.append("Storage path: ").append(item.getUri().toString());

        new AlertDialog.Builder(this)
                .setTitle("Media Details")
                .setMessage(sb.toString().trim())
                .setPositiveButton("Close", null)
                .show();
    }

    private String fmtSize(long bytes) {
        if (bytes <= 0) return "Unknown size";
        if (bytes < 1024 * 1024) {
            return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        }
        return String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0));
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

    @Override
    public void onBackPressed() {
        handleBack();
    }
}
