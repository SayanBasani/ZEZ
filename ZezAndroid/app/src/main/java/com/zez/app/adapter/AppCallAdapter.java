package com.zez.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zez.app.R;
import com.zez.app.model.AppCallItem;
import com.zez.app.util.ChatApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AppCallAdapter extends RecyclerView.Adapter<AppCallAdapter.CallViewHolder> {

    public interface OnCallActionListener {
        void onItemClick(AppCallItem item);
        void onAudioCallClick(AppCallItem item);
        void onVideoCallClick(AppCallItem item);
    }

    private final Context context;
    private final List<AppCallItem> items = new ArrayList<>();
    private final OnCallActionListener listener;
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public AppCallAdapter(Context context, OnCallActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void setCalls(List<AppCallItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_call, parent, false);
        return new CallViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
        AppCallItem item = items.get(position);
        if (item == null) return;

        holder.tvName.setText(item.getPeerName());

        // Call details: "Voice call · 2 min" or "Missed video call"
        String typeLabel = item.isVideo() ? "Video call" : "Voice call";
        if (item.isMissed()) {
            holder.tvDetails.setText("Missed " + (item.isVideo() ? "video" : "voice") + " call");
            holder.tvDetails.setTextColor(Color.parseColor("#EF4444"));
            holder.ivDirection.setImageResource(R.drawable.ic_call_missed);
            holder.ivDirection.setColorFilter(Color.parseColor("#EF4444"));
        } else if (item.isOutgoing()) {
            holder.tvDetails.setText(typeLabel + " · " + item.getFormattedDuration());
            holder.tvDetails.setTextColor(Color.parseColor("#94A3B8"));
            holder.ivDirection.setImageResource(R.drawable.ic_call_made);
            holder.ivDirection.setColorFilter(Color.parseColor("#22C55E"));
        } else {
            holder.tvDetails.setText(typeLabel + " · " + item.getFormattedDuration());
            holder.tvDetails.setTextColor(Color.parseColor("#94A3B8"));
            holder.ivDirection.setImageResource(R.drawable.ic_call_received);
            holder.ivDirection.setColorFilter(Color.parseColor("#22C55E"));
        }

        // Timestamp
        holder.tvTime.setText(formatTime(item.getStartedAt()));

        // Peer avatar
        if (item.getPeerAvatar() != null && !item.getPeerAvatar().trim().isEmpty()) {
            holder.tvInitials.setVisibility(View.GONE);
            holder.ivAvatar.setVisibility(View.VISIBLE);
            String fullUrl = ChatApiClient.getAbsoluteMediaUrl(context, item.getPeerAvatar());
            Glide.with(context)
                    .load(fullUrl)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_circle_glass)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setVisibility(View.VISIBLE);
            holder.ivAvatar.setImageResource(R.drawable.ic_person);
            holder.ivAvatar.setColorFilter(Color.parseColor("#94A3B8"));
            holder.tvInitials.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        holder.btnAudio.setOnClickListener(v -> {
            if (listener != null) listener.onAudioCallClick(item);
        });

        holder.btnVideo.setOnClickListener(v -> {
            if (listener != null) listener.onVideoCallClick(item);
        });
    }

    private String formatTime(String isoString) {
        if (isoString == null || isoString.isEmpty()) return "";
        try {
            String clean = isoString.replaceAll("\\.\\d+", "");
            if (clean.endsWith("Z")) clean = clean.substring(0, clean.length() - 1);
            Date date = isoFormat.parse(clean);
            if (date == null) return isoString;

            Calendar now = Calendar.getInstance();
            Calendar then = Calendar.getInstance();
            then.setTime(date);

            boolean sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR)
                    && now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR);
            if (sameDay) {
                return "Today, " + timeFmt.format(date);
            }

            now.add(Calendar.DAY_OF_YEAR, -1);
            boolean yesterday = now.get(Calendar.YEAR) == then.get(Calendar.YEAR)
                    && now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR);
            if (yesterday) {
                return "Yesterday, " + timeFmt.format(date);
            }

            return dateFmt.format(date);
        } catch (Exception e) {
            return isoString;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CallViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvInitials;
        TextView tvName;
        ImageView ivDirection;
        TextView tvDetails;
        TextView tvTime;
        ImageButton btnAudio;
        ImageButton btnVideo;

        CallViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_call_avatar);
            tvInitials = itemView.findViewById(R.id.tv_call_avatar_initials);
            tvName = itemView.findViewById(R.id.tv_call_name);
            ivDirection = itemView.findViewById(R.id.iv_call_direction);
            tvDetails = itemView.findViewById(R.id.tv_call_details);
            tvTime = itemView.findViewById(R.id.tv_call_time);
            btnAudio = itemView.findViewById(R.id.btn_action_audiocall);
            btnVideo = itemView.findViewById(R.id.btn_action_videocall);
        }
    }
}
