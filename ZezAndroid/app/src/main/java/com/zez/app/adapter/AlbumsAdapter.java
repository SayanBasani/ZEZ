package com.zez.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.zez.app.R;
import com.zez.app.model.AlbumItem;

import java.util.ArrayList;
import java.util.List;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumVH> {

    public interface OnAlbumClickListener {
        void onAlbumClick(AlbumItem album);
    }

    private List<AlbumItem> items = new ArrayList<>();
    private OnAlbumClickListener listener;

    public AlbumsAdapter(OnAlbumClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<AlbumItem> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlbumVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
        return new AlbumVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumVH h, int pos) {
        AlbumItem album = items.get(pos);
        Context ctx = h.itemView.getContext();

        // 1. Cover image handling
        if (album.coverUri != null) {
            Glide.with(ctx)
                    .load(album.coverUri)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(DrawableTransitionOptions.withCrossFade(150))
                    .into(h.ivCover);
        } else {
            // Fallback placeholder for empty album (e.g. Empty Favorites or Empty Bin)
            if (album.specialType == AlbumItem.TYPE_FAVORITES) {
                h.ivCover.setImageResource(R.drawable.ic_favorite_filled);
                h.ivCover.setScaleType(ImageView.ScaleType.CENTER);
            } else if (album.specialType == AlbumItem.TYPE_BIN) {
                h.ivCover.setImageResource(android.R.drawable.ic_menu_delete);
                h.ivCover.setScaleType(ImageView.ScaleType.CENTER);
            } else {
                h.ivCover.setImageResource(R.drawable.ic_photo_library);
                h.ivCover.setScaleType(ImageView.ScaleType.CENTER);
            }
        }

        // 2. Special Type Badge (Favorites star/heart, Videos, Bin, etc.)
        if (album.specialType == AlbumItem.TYPE_FAVORITES) {
            h.ivSpecialBadge.setVisibility(View.VISIBLE);
            h.ivSpecialBadge.setImageResource(R.drawable.ic_favorite_filled);
        } else if (album.specialType == AlbumItem.TYPE_VIDEOS) {
            h.ivSpecialBadge.setVisibility(View.VISIBLE);
            h.ivSpecialBadge.setImageResource(R.drawable.ic_star_filled);
        } else if (album.specialType == AlbumItem.TYPE_SCREENSHOTS) {
            h.ivSpecialBadge.setVisibility(View.VISIBLE);
            h.ivSpecialBadge.setImageResource(R.drawable.ic_photo_library);
        } else if (album.specialType == AlbumItem.TYPE_BIN) {
            h.ivSpecialBadge.setVisibility(View.VISIBLE);
            h.ivSpecialBadge.setImageResource(android.R.drawable.ic_menu_delete);
        } else {
            h.ivSpecialBadge.setVisibility(View.GONE);
        }

        // 3. Name & Count
        h.tvName.setText(album.name);
        String countText = album.count + (album.count == 1 ? " item" : " items");
        h.tvCount.setText(countText);
        h.tvCountBadge.setText(String.valueOf(album.count));

        // 4. Click event
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAlbumClick(album);
        });

        // 5. Overflow menu on album card
        h.btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(ctx, v);
            popup.getMenu().add(0, 1, 0, "Open Album");
            popup.getMenu().add(0, 2, 1, "Album Details");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    if (listener != null) listener.onAlbumClick(album);
                    return true;
                } else if (item.getItemId() == 2) {
                    Toast.makeText(ctx, album.name + " (" + countText + ")", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AlbumVH extends RecyclerView.ViewHolder {
        ImageView ivCover, ivSpecialBadge;
        ImageButton btnMenu;
        TextView tvName, tvCount, tvCountBadge;

        AlbumVH(View v) {
            super(v);
            ivCover = v.findViewById(R.id.iv_album_cover);
            ivSpecialBadge = v.findViewById(R.id.iv_special_badge);
            btnMenu = v.findViewById(R.id.btn_album_menu);
            tvName = v.findViewById(R.id.tv_album_name);
            tvCount = v.findViewById(R.id.tv_album_count);
            tvCountBadge = v.findViewById(R.id.tv_album_count_badge);
        }
    }
}
