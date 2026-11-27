package com.zez.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

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
        Context context = h.itemView.getContext();

        // Always show ZoomImageView initially with thumbnail/image
        h.zoomView.setVisibility(View.VISIBLE);
        h.videoView.setVisibility(View.GONE);

        Glide.with(context)
            .load(item.getUri())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(h.zoomView);

        if (item.isVideo()) {
            h.layoutPlayButton.setVisibility(View.VISIBLE);

            View.OnClickListener playClickListener = v -> {
                h.zoomView.setVisibility(View.GONE);
                h.layoutPlayButton.setVisibility(View.GONE);
                h.videoView.setVisibility(View.VISIBLE);

                try {
                    MediaController controller = new MediaController(context);
                    controller.setAnchorView(h.videoView);
                    h.videoView.setMediaController(controller);
                    h.videoView.setVideoURI(item.getUri());
                    h.videoView.setOnPreparedListener(mp -> {
                        mp.setLooping(false);
                        h.videoView.start();
                    });
                    h.videoView.setOnCompletionListener(mp -> {
                        h.zoomView.setVisibility(View.VISIBLE);
                        h.layoutPlayButton.setVisibility(View.VISIBLE);
                        h.videoView.setVisibility(View.GONE);
                    });
                    h.videoView.setOnErrorListener((mp, what, extra) -> {
                        // Launch system video player fallback
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(item.getUri(), "video/*");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            context.startActivity(intent);
                        } catch (Exception ex) {
                            Toast.makeText(context, "Cannot play video format", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    });
                } catch (Exception e) {
                    Toast.makeText(context, "Playback error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };

            h.layoutPlayButton.setOnClickListener(playClickListener);
            h.zoomView.setOnTapListener(() -> {
                if (tapListener != null) tapListener.onTap();
            });
        } else {
            h.layoutPlayButton.setVisibility(View.GONE);
            h.zoomView.setOnTapListener(() -> {
                if (tapListener != null) tapListener.onTap();
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull PageVH holder) {
        super.onViewRecycled(holder);
        if (holder.videoView != null && holder.videoView.isPlaying()) {
            holder.videoView.stopPlayback();
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class PageVH extends RecyclerView.ViewHolder {
        ZoomImageView zoomView;
        VideoView videoView;
        FrameLayout layoutPlayButton;

        PageVH(View v) {
            super(v);
            zoomView = v.findViewById(R.id.zoom_image_page);
            videoView = v.findViewById(R.id.video_player_page);
            layoutPlayButton = v.findViewById(R.id.layout_play_button);
        }
    }
}
