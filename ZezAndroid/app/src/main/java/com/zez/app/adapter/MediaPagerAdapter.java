package com.zez.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zez.app.R;
import com.zez.app.model.MediaItem;
import com.zez.app.util.ZoomImageView;

import java.util.List;

public class MediaPagerAdapter extends RecyclerView.Adapter<MediaPagerAdapter.PageVH> {

    public interface OnTapListener { void onTap(); }

    private final List<MediaItem> items;
    private final OnTapListener   tapListener;

    public MediaPagerAdapter(List<MediaItem> items, OnTapListener tapListener) {
        this.items       = items;
        this.tapListener = tapListener;
    }

    @NonNull @Override
    public PageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_media_page, parent, false);
        return new PageVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PageVH h, int pos) {
        MediaItem item = items.get(pos);
        Glide.with(h.zoomView.getContext())
            .load(item.getUri())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(h.zoomView);
        h.zoomView.setOnClickListener(v -> { if (tapListener != null) tapListener.onTap(); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class PageVH extends RecyclerView.ViewHolder {
        ZoomImageView zoomView;
        PageVH(View v) {
            super(v);
            zoomView = v.findViewById(R.id.zoom_image_page);
        }
    }
}
