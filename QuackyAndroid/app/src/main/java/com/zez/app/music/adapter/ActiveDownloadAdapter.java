package com.zez.app.music.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zez.app.R;
import com.zez.app.music.download.DownloadTask;

import java.util.ArrayList;
import java.util.List;

public class ActiveDownloadAdapter extends RecyclerView.Adapter<ActiveDownloadAdapter.DownloadViewHolder> {

    public interface OnCancelClickListener {
        void onCancelClick(DownloadTask task);
    }

    private final Context context;
    private final List<DownloadTask> tasks = new ArrayList<>();
    private final OnCancelClickListener cancelListener;

    public ActiveDownloadAdapter(Context context, OnCancelClickListener cancelListener) {
        this.context = context;
        this.cancelListener = cancelListener;
    }

    public void setTasks(List<DownloadTask> newTasks) {
        this.tasks.clear();
        if (newTasks != null) {
            this.tasks.addAll(newTasks);
        }
        notifyDataSetChanged();
    }

    public void updateTask(DownloadTask task) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getVideoId().equals(task.getVideoId())) {
                notifyItemChanged(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_downloading_track, parent, false);
        return new DownloadViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
        DownloadTask task = tasks.get(position);

        holder.tvTitle.setText(task.getTitle());
        holder.tvArtist.setText(task.getArtist());
        holder.tvPercent.setText(task.getPercent() + "%");
        holder.tvSpeed.setText(task.getSpeedFormatted());
        holder.tvSize.setText(task.getSizeFormatted());
        holder.progressBar.setProgress(task.getPercent());

        Glide.with(context)
                .load(task.getThumbnailUrl())
                .placeholder(R.drawable.bg_search_input)
                .into(holder.ivThumb);

        holder.btnCancel.setOnClickListener(v -> {
            if (cancelListener != null) {
                cancelListener.onCancelClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class DownloadViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvArtist, tvPercent, tvSpeed, tvSize;
        ProgressBar progressBar;
        ImageButton btnCancel;

        public DownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_dl_thumb);
            tvTitle = itemView.findViewById(R.id.tv_dl_title);
            tvArtist = itemView.findViewById(R.id.tv_dl_artist);
            tvPercent = itemView.findViewById(R.id.tv_dl_percent);
            tvSpeed = itemView.findViewById(R.id.tv_dl_speed);
            tvSize = itemView.findViewById(R.id.tv_dl_size);
            progressBar = itemView.findViewById(R.id.pb_dl_progress);
            btnCancel = itemView.findViewById(R.id.btn_dl_cancel);
        }
    }
}
