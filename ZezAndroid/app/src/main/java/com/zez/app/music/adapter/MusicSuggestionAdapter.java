package com.zez.app.music.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zez.app.R;

import java.util.ArrayList;
import java.util.List;

public class MusicSuggestionAdapter extends RecyclerView.Adapter<MusicSuggestionAdapter.ViewHolder> {

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String query);
    }

    private final List<String> suggestions = new ArrayList<>();
    private final OnSuggestionClickListener listener;

    public MusicSuggestionAdapter(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    public void setSuggestions(List<String> list) {
        suggestions.clear();
        if (list != null) {
            suggestions.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = suggestions.get(position);
        holder.tvText.setText(text);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(text);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_suggestion_text);
        }
    }
}
