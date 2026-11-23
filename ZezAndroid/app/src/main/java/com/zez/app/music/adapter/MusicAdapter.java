package com.zez.app.music.adapter;

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
import com.zez.app.R;
import com.zez.app.music.download.MusicDownloadHelper;
import com.zez.app.music.model.MusicTrack;
import com.zez.app.music.player.MusicPlayerManager;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.TrackViewHolder> {

    public interface OnTrackClickListener {
        void onTrackClick(MusicTrack track, int position);
    }

    private final Context context;
    private final List<MusicTrack> tracks = new ArrayList<>();
    private final OnTrackClickListener listener;

    public MusicAdapter(Context context, OnTrackClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setTracks(List<MusicTrack> newTracks) {
        tracks.clear();
        if (newTracks != null) {
            tracks.addAll(newTracks);
        }
        notifyDataSetChanged();
    }

    public List<MusicTrack> getTracks() {
        return tracks;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        MusicTrack track = tracks.get(position);
        holder.tvTitle.setText(track.getTitle());
        holder.tvArtist.setText(track.getArtist());
        holder.tvDuration.setText(track.getDuration());

        if (track.getViews() != null && !track.getViews().isEmpty()) {
            holder.tvViews.setVisibility(View.VISIBLE);
            holder.tvViews.setText(" · " + track.getViews());
        } else {
            holder.tvViews.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(track.getThumbnailUrl())
                .placeholder(R.drawable.bg_search_input)
                .centerCrop()
                .into(holder.ivThumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTrackClick(track, holder.getAdapterPosition());
            }
        });

        holder.btnMore.setOnClickListener(v -> showPopupMenu(holder.btnMore, track));
    }

    private void showPopupMenu(View anchor, MusicTrack track) {
        PopupMenu popup = new PopupMenu(context, anchor);
        popup.getMenu().add(0, 1, 0, "Play");
        popup.getMenu().add(0, 2, 1, "Play next");
        popup.getMenu().add(0, 3, 2, "Add to queue");
        popup.getMenu().add(0, 4, 3, "Download");
        popup.getMenu().add(0, 5, 4, "Share");

        if (track.isDownloaded()) {
            popup.getMenu().add(0, 6, 5, "Delete download");
        }

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    MusicPlayerManager.getInstance().playTrack(track);
                    return true;
                case 2:
                    MusicPlayerManager.getInstance().addToQueue(track);
                    Toast.makeText(context, "Added as next: " + track.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                case 3:
                    MusicPlayerManager.getInstance().addToQueue(track);
                    Toast.makeText(context, "Added to queue: " + track.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                case 4:
                    MusicDownloadHelper.showDownloadDialog(context, track, null);
                    return true;
                case 5:
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, track.getTitle());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Listen to " + track.getTitle() + " on YouTube:\nhttps://www.youtube.com/watch?v=" + track.getId());
                    context.startActivity(Intent.createChooser(shareIntent, "Share Track"));
                    return true;
                case 6:
                    MusicDownloadHelper.removeDownloadedTrack(context, track);
                    tracks.remove(track);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Deleted from downloads", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivThumbnail;
        final TextView tvTitle;
        final TextView tvArtist;
        final TextView tvDuration;
        final TextView tvViews;
        final ImageButton btnMore;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_track_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_track_title);
            tvArtist = itemView.findViewById(R.id.tv_track_artist);
            tvDuration = itemView.findViewById(R.id.tv_track_duration);
            tvViews = itemView.findViewById(R.id.tv_track_views);
            btnMore = itemView.findViewById(R.id.btn_track_more);
        }
    }
}
