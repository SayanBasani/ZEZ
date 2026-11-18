package com.zez.app.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zez.app.R;
import com.zez.app.model.MediaItem;

import java.util.List;

public class FilmstripAdapter extends RecyclerView.Adapter<FilmstripAdapter.FilmstripVH> {

    public interface OnFilmstripClickListener {
        void onItemClick(int position);
    }

    private final List<MediaItem> items;
    private final OnFilmstripClickListener listener;
    private int selectedPosition = 0;

    public FilmstripAdapter(List<MediaItem> items, OnFilmstripClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        if (position < 0 || position >= items.size() || position == selectedPosition) return;
        int old = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(old);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public FilmstripVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filmstrip_thumb, parent, false);
        return new FilmstripVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmstripVH holder, int position) {
        MediaItem item = items.get(position);
        boolean isSelected = (position == selectedPosition);

        Glide.with(holder.ivThumb.getContext())
                .load(item.getUri())
                .override(120, 120)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivThumb);

        if (isSelected) {
            GradientDrawable border = new GradientDrawable();
            border.setColor(Color.TRANSPARENT);
            border.setStroke(4, Color.WHITE);
            border.setCornerRadius(8f);
            holder.ivBorder.setBackground(border);
            holder.ivBorder.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(1.0f);
        } else {
            holder.ivBorder.setVisibility(View.GONE);
            holder.itemView.setAlpha(0.6f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FilmstripVH extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        View ivBorder;

        FilmstripVH(View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.thumb_image);
            ivBorder = itemView.findViewById(R.id.thumb_border);
        }
    }
}
