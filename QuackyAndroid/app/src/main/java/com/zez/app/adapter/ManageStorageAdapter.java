package com.zez.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zez.app.ImageViewerActivity;
import com.zez.app.R;
import com.zez.app.model.StorageMediaItem;
import com.zez.app.util.ChatApiClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ManageStorageAdapter extends RecyclerView.Adapter<ManageStorageAdapter.ViewHolder> {

    public interface OnSelectionListener {
        void onSelectionChanged(int selectedCount, long selectedBytes);
    }

    private final Context context;
    private final List<StorageMediaItem> items = new ArrayList<>();
    private final OnSelectionListener selectionListener;
    private boolean isMultiSelectActive = false;

    public ManageStorageAdapter(Context context, OnSelectionListener listener) {
        this.context = context;
        this.selectionListener = listener;
    }

    public void setItems(List<StorageMediaItem> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public List<StorageMediaItem> getItems() {
        return items;
    }

    public List<StorageMediaItem> getSelectedItems() {
        List<StorageMediaItem> sel = new ArrayList<>();
        for (StorageMediaItem item : items) {
            if (item.isSelected()) sel.add(item);
        }
        return sel;
    }

    public void selectAll(boolean select) {
        for (StorageMediaItem item : items) {
            item.setSelected(select);
        }
        isMultiSelectActive = select && !items.isEmpty();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void clearSelection() {
        selectAll(false);
    }

    public void removeItems(List<StorageMediaItem> toRemove) {
        items.removeAll(toRemove);
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    private void notifySelectionChanged() {
        int count = 0;
        long bytes = 0;
        for (StorageMediaItem it : items) {
            if (it.isSelected()) {
                count++;
                bytes += it.getFileSizeBytes();
            }
        }
        isMultiSelectActive = count > 0;
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(count, bytes);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_storage_cell, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StorageMediaItem item = items.get(position);

        holder.tvSize.setText(item.getFormattedSize());
        holder.tvFilename.setText(item.getFileName());

        // Selection styling with dynamic theme colors
        if (item.isSelected()) {
            holder.viewOverlay.setVisibility(View.VISIBLE);
            holder.viewOverlay.setBackgroundColor(com.zez.app.theme.ThemeManager.getSelectionHighlightColor(context));
            holder.ivCheck.setVisibility(View.VISIBLE);
            holder.ivCheck.setColorFilter(com.zez.app.theme.ThemeManager.getPrimaryColor(context));
        } else {
            holder.viewOverlay.setVisibility(View.GONE);
            holder.ivCheck.setVisibility(View.GONE);
            holder.ivCheck.clearColorFilter();
        }

        // Determine badge type
        String type = item.getMediaType();
        String ext = "FILE";
        String fn = item.getFileName().toLowerCase();
        if (fn.endsWith(".mp4") || type.contains("video")) ext = "MP4";
        else if (fn.endsWith(".jpg") || fn.endsWith(".jpeg") || fn.endsWith(".png") || type.contains("image")) ext = "IMG";
        else if (fn.endsWith(".pdf") || type.contains("pdf")) ext = "PDF";
        else if (fn.endsWith(".doc") || fn.endsWith(".docx")) ext = "DOC";
        else if (fn.endsWith(".mp3") || fn.endsWith(".m4a") || type.contains("audio")) ext = "AUDIO";
        holder.tvBadge.setText(ext);

        // Thumbnail loading
        String url = item.getMediaUrl();
        File localFile = item.getLocalFile();
        boolean hasLocal = localFile != null && localFile.exists();

        Object loadTarget = hasLocal ? localFile : (url != null ? ChatApiClient.getAbsoluteMediaUrl(context, url) : null);

        if (loadTarget != null && (type.contains("image") || type.contains("video") || ext.equals("MP4") || ext.equals("IMG"))) {
            holder.ivThumb.setVisibility(View.VISIBLE);
            holder.layoutFileBadge.setVisibility(View.GONE);

            Glide.with(context)
                    .load(loadTarget)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.ivThumb);
        } else {
            holder.ivThumb.setVisibility(View.GONE);
            holder.layoutFileBadge.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isMultiSelectActive) {
                item.setSelected(!item.isSelected());
                notifyItemChanged(position);
                notifySelectionChanged();
            } else {
                openMedia(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            item.setSelected(!item.isSelected());
            notifyItemChanged(position);
            notifySelectionChanged();
            return true;
        });
    }

    private void openMedia(StorageMediaItem item) {
        File local = item.getLocalFile();
        if (local != null && local.exists()) {
            try {
                Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", local);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String mime = item.getMediaType().contains("video") ? "video/*" :
                        (item.getMediaType().contains("audio") ? "audio/*" :
                                (item.getMediaType().contains("pdf") ? "application/pdf" : "image/*"));
                intent.setDataAndType(contentUri, mime);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(intent, "Open file"));
                return;
            } catch (Exception ignored) {}
        }

        if (item.getMediaUrl() != null) {
            String fullUrl = ChatApiClient.getAbsoluteMediaUrl(context, item.getMediaUrl());
            Intent vi = new Intent(context, ImageViewerActivity.class);
            vi.putExtra(ImageViewerActivity.EXTRA_URI, fullUrl);
            vi.putExtra(ImageViewerActivity.EXTRA_NAME, item.getFileName());
            context.startActivity(vi);
        } else {
            Toast.makeText(context, "File not available locally", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivThumb;
        final LinearLayout layoutFileBadge;
        final ImageView ivIcon;
        final TextView tvBadge;
        final TextView tvSize;
        final TextView tvFilename;
        final View viewOverlay;
        final ImageView ivCheck;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_cell_thumb);
            layoutFileBadge = itemView.findViewById(R.id.layout_cell_file_badge);
            ivIcon = itemView.findViewById(R.id.iv_cell_icon);
            tvBadge = itemView.findViewById(R.id.tv_cell_badge);
            tvSize = itemView.findViewById(R.id.tv_cell_size);
            tvFilename = itemView.findViewById(R.id.tv_cell_filename);
            viewOverlay = itemView.findViewById(R.id.view_cell_overlay);
            ivCheck = itemView.findViewById(R.id.iv_cell_check);
        }
    }
}
