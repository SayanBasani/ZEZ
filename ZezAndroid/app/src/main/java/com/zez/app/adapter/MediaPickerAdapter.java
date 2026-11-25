package com.zez.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zez.app.R;
import com.zez.app.model.MediaItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MediaPickerAdapter extends RecyclerView.Adapter<MediaPickerAdapter.MediaViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
        void onItemClick(MediaItem item);
    }

    private final Context context;
    private final List<MediaItem> allItems = new ArrayList<>();
    private final List<MediaItem> displayedItems = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();
    private OnSelectionChangedListener listener;

    public MediaPickerAdapter(Context context) {
        this.context = context;
    }

    public void setListener(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    public void setItems(List<MediaItem> items) {
        allItems.clear();
        if (items != null) allItems.addAll(items);
        filter(0); // 0 = ALL
    }

    public void filter(int mode) {
        displayedItems.clear();
        for (MediaItem item : allItems) {
            if (mode == 1 && item.isVideo()) continue; // Photos only
            if (mode == 2 && !item.isVideo()) continue; // Videos only
            displayedItems.add(item);
        }
        notifyDataSetChanged();
    }

    public void selectAll(boolean select) {
        selectedIds.clear();
        if (select) {
            for (MediaItem item : displayedItems) {
                selectedIds.add(item.getId());
            }
        }
        notifyDataSetChanged();
        if (listener != null) listener.onSelectionChanged(selectedIds.size());
    }

    public List<MediaItem> getSelectedItems() {
        List<MediaItem> res = new ArrayList<>();
        for (MediaItem item : allItems) {
            if (selectedIds.contains(item.getId())) {
                res.add(item);
            }
        }
        return res;
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    public void toggleSelection(MediaItem item) {
        if (selectedIds.contains(item.getId())) {
            selectedIds.remove(item.getId());
        } else {
            selectedIds.add(item.getId());
        }
        notifyDataSetChanged();
        if (listener != null) listener.onSelectionChanged(selectedIds.size());
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media_picker, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = displayedItems.get(position);

        // Load thumbnail
        Glide.with(context)
                .load(item.getUri())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(holder.ivThumb);

        // Format file size
        holder.tvSize.setText(formatFileSize(item.getSize()));

        // Video duration badge
        if (item.isVideo()) {
            holder.layoutVideoBadge.setVisibility(View.VISIBLE);
            holder.tvDuration.setText(formatDuration(item.getDuration()));
        } else {
            holder.layoutVideoBadge.setVisibility(View.GONE);
        }

        // File name
        holder.tvName.setText(item.getName() != null ? item.getName() : "");

        // Selection state
        boolean isSelected = selectedIds.contains(item.getId());
        holder.viewOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            toggleSelection(item);
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return displayedItems.size();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvSize;
        LinearLayout layoutVideoBadge;
        TextView tvDuration;
        TextView tvName;
        View viewOverlay;
        ImageView ivCheck;

        MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_picker_thumb);
            tvSize = itemView.findViewById(R.id.tv_picker_size);
            layoutVideoBadge = itemView.findViewById(R.id.layout_picker_video_badge);
            tvDuration = itemView.findViewById(R.id.tv_picker_duration);
            tvName = itemView.findViewById(R.id.tv_picker_name);
            viewOverlay = itemView.findViewById(R.id.view_picker_selected_overlay);
            ivCheck = itemView.findViewById(R.id.iv_picker_check);
        }
    }

    private static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        if (bytes < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.0f KB", bytes / 1024.0);
        }
        return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private static String formatDuration(long ms) {
        if (ms <= 0) return "0:00";
        long totalSecs = ms / 1000;
        long mins = totalSecs / 60;
        long secs = totalSecs % 60;
        return String.format(Locale.getDefault(), "%d:%02d", mins, secs);
    }
}
