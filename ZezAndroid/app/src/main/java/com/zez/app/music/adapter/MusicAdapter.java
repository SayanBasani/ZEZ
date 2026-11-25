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
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

    public void appendTracks(List<MusicTrack> moreTracks) {
        if (moreTracks == null || moreTracks.isEmpty()) return;
        int startPos = tracks.size();
        List<MusicTrack> uniqueTracks = new ArrayList<>();
        java.util.Set<String> existingIds = new java.util.HashSet<>();
        for (MusicTrack t : tracks) {
            if (t != null && t.getId() != null) {
                existingIds.add(t.getId());
            }
        }
        for (MusicTrack t : moreTracks) {
            if (t != null && t.getId() != null && !existingIds.contains(t.getId())) {
                uniqueTracks.add(t);
                existingIds.add(t.getId());
            }
        }
        if (!uniqueTracks.isEmpty()) {
            tracks.addAll(uniqueTracks);
            notifyItemRangeInserted(startPos, uniqueTracks.size());
        }
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

        holder.btnMore.setOnClickListener(v -> showTrackOptionsSheet(track));
    }

    public void showTrackOptionsSheet(MusicTrack track) {
        if (track == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_track_options, null);
        dialog.setContentView(view);

        ImageView ivThumb = view.findViewById(R.id.iv_opt_thumb);
        TextView tvTitle = view.findViewById(R.id.tv_opt_title);
        TextView tvArtist = view.findViewById(R.id.tv_opt_artist);
        View optPlayAudio = view.findViewById(R.id.opt_play_audio);
        View optPlayVideo = view.findViewById(R.id.opt_play_video);
        View optDownload = view.findViewById(R.id.opt_download);
        View optPlayNext = view.findViewById(R.id.opt_play_next);
        View optAddQueue = view.findViewById(R.id.opt_add_queue);
        View optShare = view.findViewById(R.id.opt_share);
        View optOpenExternal = view.findViewById(R.id.opt_open_external);
        View optDelete = view.findViewById(R.id.opt_delete);

        tvTitle.setText(track.getTitle());
        tvArtist.setText(track.getArtist());
        Glide.with(context).load(track.getThumbnailUrl()).into(ivThumb);

        optPlayAudio.setOnClickListener(v -> {
            dialog.dismiss();
            MusicPlayerManager.getInstance().setVideoMode(false);
            int idx = tracks.indexOf(track);
            if (idx >= 0) {
                MusicPlayerManager.getInstance().playQueue(tracks, idx);
            } else {
                MusicPlayerManager.getInstance().playTrack(track);
            }
        });

        optPlayVideo.setOnClickListener(v -> {
            dialog.dismiss();
            MusicPlayerManager.getInstance().setVideoMode(true);
            int idx = tracks.indexOf(track);
            if (idx >= 0) {
                MusicPlayerManager.getInstance().playQueue(tracks, idx);
            } else {
                MusicPlayerManager.getInstance().playTrack(track);
            }
        });

        optDownload.setOnClickListener(v -> {
            dialog.dismiss();
            MusicDownloadHelper.showDownloadDialog(context, track, null);
        });

        optPlayNext.setOnClickListener(v -> {
            dialog.dismiss();
            MusicPlayerManager.getInstance().addNextToQueue(track);
            Toast.makeText(context, "Added as next: " + track.getTitle(), Toast.LENGTH_SHORT).show();
        });

        optAddQueue.setOnClickListener(v -> {
            dialog.dismiss();
            MusicPlayerManager.getInstance().addToQueue(track);
            Toast.makeText(context, "Added to queue: " + track.getTitle(), Toast.LENGTH_SHORT).show();
        });

        optShare.setOnClickListener(v -> {
            dialog.dismiss();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, track.getTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Listen to " + track.getTitle() + " on YouTube:\nhttps://www.youtube.com/watch?v=" + track.getId());
            context.startActivity(Intent.createChooser(shareIntent, "Share Track"));
        });

        if (track.isDownloaded()) {
            if (optOpenExternal != null) {
                optOpenExternal.setVisibility(View.VISIBLE);
                optOpenExternal.setOnClickListener(v -> {
                    dialog.dismiss();
                    openDownloadedInPhoneApp(track);
                });
            }
            optDelete.setVisibility(View.VISIBLE);
            optDelete.setOnClickListener(v -> {
                dialog.dismiss();
                MusicDownloadHelper.removeDownloadedTrack(context, track);
                tracks.remove(track);
                notifyDataSetChanged();
                Toast.makeText(context, "Deleted from downloads", Toast.LENGTH_SHORT).show();
            });
        } else {
            if (optOpenExternal != null) optOpenExternal.setVisibility(View.GONE);
            optDelete.setVisibility(View.GONE);
        }

        dialog.show();
    }

    private void openDownloadedInPhoneApp(MusicTrack track) {
        if (track == null || track.getLocalFilePath() == null) {
            Toast.makeText(context, "Downloaded file not found", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String lp = track.getLocalFilePath();
            android.net.Uri uri;
            if (lp.startsWith("content://")) {
                uri = android.net.Uri.parse(lp);
            } else {
                java.io.File file = new java.io.File(lp);
                if (!file.exists()) {
                    Toast.makeText(context, "File missing on device storage", Toast.LENGTH_SHORT).show();
                    return;
                }
                uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".provider",
                        file
                );
            }
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            String lower = lp.toLowerCase();
            String mime = lower.endsWith(".mp4") ? "video/*" : (lower.endsWith(".mp3") ? "audio/mpeg" : (lower.endsWith(".webm") ? "audio/webm" : "audio/*"));
            viewIntent.setDataAndType(uri, mime);
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(viewIntent, "Play with phone media app"));
        } catch (Exception e) {
            Toast.makeText(context, "Saved in Music/Quacky (" + e.getMessage() + ")", Toast.LENGTH_SHORT).show();
        }
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
