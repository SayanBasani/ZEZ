package com.zez.app.music.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zez.app.R;

import java.util.ArrayList;
import java.util.List;

public class MusicSuggestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SUGGESTION = 0;
    private static final int TYPE_CLEAR_HISTORY = 1;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String query);
        void onClearHistoryClick();
    }

    private final List<String> suggestions = new ArrayList<>();
    private final OnSuggestionClickListener listener;
    private boolean isHistory = false;

    public MusicSuggestionAdapter(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    public void setSuggestions(List<String> list) {
        this.isHistory = false;
        suggestions.clear();
        if (list != null) {
            suggestions.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void setHistorySuggestions(List<String> list) {
        this.isHistory = true;
        suggestions.clear();
        if (list != null) {
            suggestions.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHistory && position == suggestions.size()) {
            return TYPE_CLEAR_HISTORY;
        }
        return TYPE_SUGGESTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CLEAR_HISTORY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_clear_history, parent, false);
            return new ClearViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ClearViewHolder) {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClearHistoryClick();
            });
            return;
        }

        ViewHolder vh = (ViewHolder) holder;
        String text = suggestions.get(position);
        vh.tvText.setText(text);
        if (isHistory) {
            vh.ivIcon.setImageResource(R.drawable.ic_history);
        } else {
            vh.ivIcon.setImageResource(R.drawable.ic_search);
        }
        vh.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(text);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (isHistory && !suggestions.isEmpty()) {
            return suggestions.size() + 1;
        }
        return suggestions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_suggestion_icon);
            tvText = itemView.findViewById(R.id.tv_suggestion_text);
        }
    }

    public static class ClearViewHolder extends RecyclerView.ViewHolder {
        public ClearViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
