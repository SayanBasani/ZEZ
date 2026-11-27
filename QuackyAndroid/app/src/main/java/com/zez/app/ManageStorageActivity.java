package com.zez.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zez.app.adapter.ManageStorageAdapter;
import com.zez.app.model.ChatMessage;
import com.zez.app.model.StorageMediaItem;
import com.zez.app.util.ChatApiClient;
import com.zez.app.util.ChatLocalDbHelper;
import com.zez.app.util.QuackyStorageHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManageStorageActivity extends AppCompatActivity {

    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    public static final String EXTRA_OTHER_USER_ID = "extra_other_user_id";
    public static final String EXTRA_CONTACT_NAME = "extra_contact_name";
    public static final String EXTRA_CONTACT_AVATAR = "extra_contact_avatar";

    private String conversationId;
    private String otherUserId;
    private String contactName;
    private String contactAvatar;

    private ImageButton btnBack;
    private ImageView ivAvatar;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private ImageButton btnDelete;
    private ImageButton btnSort;
    private TextView tvSortLabel;
    private View layoutSelectAll;
    private TextView tvSelectAll;
    private CheckBox cbSelectAll;
    private RecyclerView rvGrid;
    private View layoutEmpty;

    private ManageStorageAdapter adapter;
    private final List<StorageMediaItem> mediaItems = new ArrayList<>();
    private boolean sortByLargest = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        com.zez.app.theme.ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_storage);

        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        otherUserId = getIntent().getStringExtra(EXTRA_OTHER_USER_ID);
        contactName = getIntent().getStringExtra(EXTRA_CONTACT_NAME);
        contactAvatar = getIntent().getStringExtra(EXTRA_CONTACT_AVATAR);

        if (contactName == null || contactName.trim().isEmpty()) {
            contactName = "Chat Media";
        }

        initViews();
        setupListeners();
        loadMedia();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_storage_back);
        ivAvatar = findViewById(R.id.iv_storage_avatar);
        tvTitle = findViewById(R.id.tv_storage_title);
        tvSubtitle = findViewById(R.id.tv_storage_subtitle);
        btnDelete = findViewById(R.id.btn_storage_delete);
        btnSort = findViewById(R.id.btn_storage_sort);
        tvSortLabel = findViewById(R.id.tv_storage_sort_label);
        layoutSelectAll = findViewById(R.id.layout_select_all);
        tvSelectAll = findViewById(R.id.tv_select_all);
        cbSelectAll = findViewById(R.id.cb_select_all);
        rvGrid = findViewById(R.id.rv_manage_storage);
        layoutEmpty = findViewById(R.id.layout_storage_empty);

        tvTitle.setText(contactName);

        if (contactAvatar != null && !contactAvatar.trim().isEmpty()) {
            Glide.with(this)
                    .load(ChatApiClient.getAbsoluteMediaUrl(this, contactAvatar))
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_person)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person);
        }

        adapter = new ManageStorageAdapter(this, (selectedCount, selectedBytes) -> {
            if (selectedCount > 0) {
                tvSubtitle.setText(selectedCount + " selected (" + QuackyStorageHelper.formatFileSize(selectedBytes) + ")");
                btnDelete.setVisibility(View.VISIBLE);
                cbSelectAll.setChecked(selectedCount == adapter.getItemCount());
            } else {
                updateTotalSizeSubtitle();
                btnDelete.setVisibility(View.GONE);
                cbSelectAll.setChecked(false);
            }
        });

        rvGrid.setLayoutManager(new GridLayoutManager(this, 3));
        rvGrid.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        View.OnClickListener toggleSortListener = v -> {
            sortByLargest = !sortByLargest;
            tvSortLabel.setText(sortByLargest ? "SIZE ▾" : "DATE ▾");
            applySort();
        };
        tvSortLabel.setOnClickListener(toggleSortListener);
        btnSort.setOnClickListener(toggleSortListener);

        layoutSelectAll.setOnClickListener(v -> {
            boolean newState = !cbSelectAll.isChecked();
            cbSelectAll.setChecked(newState);
            adapter.selectAll(newState);
        });

        btnDelete.setOnClickListener(v -> confirmDeleteSelected());
    }

    private void loadMedia() {
        mediaItems.clear();
        ChatLocalDbHelper db = ChatLocalDbHelper.getInstance(this);
        List<ChatMessage> messages = db.getMessages(conversationId, otherUserId, 1500);

        long totalBytes = 0;
        for (ChatMessage m : messages) {
            if (m.getMediaUrl() != null && !m.getMediaUrl().trim().isEmpty() && !m.isDeletedForEveryone()) {
                String fileName = m.getFileName();
                if (fileName == null || fileName.isEmpty()) {
                    String[] parts = m.getMediaUrl().split("/");
                    fileName = parts[parts.length - 1];
                }

                // Check local files in Quacky folders or app cache
                File localFile = findLocalFile(m.getMediaType(), fileName);
                long size = m.getFileSize() != null && m.getFileSize() > 0 ? m.getFileSize() :
                        (localFile != null && localFile.exists() ? localFile.length() : 350 * 1024L);

                long ts = 0;
                try {
                    ts = Long.parseLong(m.getId());
                } catch (Exception ignored) {
                    ts = System.currentTimeMillis();
                }

                StorageMediaItem item = new StorageMediaItem(
                        m, localFile, m.getMediaUrl(), m.getMediaType(), fileName, size, ts
                );
                mediaItems.add(item);
                totalBytes += size;
            }
        }

        applySort();
        updateTotalSizeSubtitle();
        updateEmptyState();
    }

    private File findLocalFile(String mediaType, String fileName) {
        String mt = mediaType != null ? mediaType.toLowerCase() : "";
        File dir;
        if (mt.contains("video")) {
            dir = QuackyStorageHelper.getMediaVideoDir(this, true);
        } else if (mt.contains("audio")) {
            dir = QuackyStorageHelper.getMediaAudioDir(this, true);
        } else if (mt.contains("doc") || mt.contains("pdf")) {
            dir = QuackyStorageHelper.getMediaDocumentsDir(this);
        } else {
            dir = QuackyStorageHelper.getMediaImagesDir(this, true);
        }

        File target = new File(dir, fileName);
        if (target.exists()) return target;

        // Also check private dir
        File privDir;
        if (mt.contains("video")) {
            privDir = QuackyStorageHelper.getMediaVideoDir(this, false);
        } else if (mt.contains("audio")) {
            privDir = QuackyStorageHelper.getMediaAudioDir(this, false);
        } else {
            privDir = QuackyStorageHelper.getMediaImagesDir(this, false);
        }
        File privTarget = new File(privDir, fileName);
        if (privTarget.exists()) return privTarget;

        return null;
    }

    private void applySort() {
        if (sortByLargest) {
            Collections.sort(mediaItems, (a, b) -> Long.compare(b.getFileSizeBytes(), a.getFileSizeBytes()));
        } else {
            Collections.sort(mediaItems, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        }
        adapter.setItems(mediaItems);
    }

    private void updateTotalSizeSubtitle() {
        long total = 0;
        for (StorageMediaItem it : mediaItems) {
            total += it.getFileSizeBytes();
        }
        tvSubtitle.setText(QuackyStorageHelper.formatFileSize(total));
    }

    private void updateEmptyState() {
        if (mediaItems.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvGrid.setVisibility(View.GONE);
            btnSort.setVisibility(View.GONE);
            layoutSelectAll.setVisibility(View.GONE);
            tvSortLabel.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvGrid.setVisibility(View.VISIBLE);
            btnSort.setVisibility(View.VISIBLE);
            layoutSelectAll.setVisibility(View.VISIBLE);
            tvSortLabel.setVisibility(View.VISIBLE);
        }
    }

    private void confirmDeleteSelected() {
        List<StorageMediaItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) return;

        long bytes = 0;
        for (StorageMediaItem it : selected) {
            bytes += it.getFileSizeBytes();
        }
        final long totalSelectedBytes = bytes;

        new AlertDialog.Builder(this)
                .setTitle("Delete " + selected.size() + " items?")
                .setMessage("These files (" + QuackyStorageHelper.formatFileSize(totalSelectedBytes) + ") will be deleted from your device storage.")
                .setPositiveButton("Delete", (dialog, which) -> deleteSelectedMedia(selected, totalSelectedBytes))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSelectedMedia(List<StorageMediaItem> selected, long freedBytes) {
        ChatLocalDbHelper db = ChatLocalDbHelper.getInstance(this);

        for (StorageMediaItem it : selected) {
            // Delete local file on disk
            if (it.getLocalFile() != null && it.getLocalFile().exists()) {
                it.getLocalFile().delete();
            }
            // Remove from chat db if message exists
            if (it.getMessage() != null && it.getMessage().getId() != null) {
                db.deleteMessageForMe(it.getMessage().getId());
            }
        }

        mediaItems.removeAll(selected);
        adapter.removeItems(selected);
        cbSelectAll.setChecked(false);
        btnDelete.setVisibility(View.GONE);

        updateTotalSizeSubtitle();
        updateEmptyState();

        Toast.makeText(this, "Freed " + QuackyStorageHelper.formatFileSize(freedBytes), Toast.LENGTH_SHORT).show();
    }
}
