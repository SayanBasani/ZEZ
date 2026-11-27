package com.zez.manager.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zez.manager.R;
import com.zez.manager.model.CallLogItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.ViewHolder> {

    public interface OnCallClickListener {
        void onCallClick(CallLogItem item);
    }

    private final List<CallLogItem> items = new ArrayList<>();
    private final OnCallClickListener listener;
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public CallLogAdapter(OnCallClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CallLogItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_log, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallLogItem item = items.get(position);
        Context ctx = holder.itemView.getContext();

        String displayName = (item.name != null && !item.name.trim().isEmpty())
                ? item.name.trim()
                : ((item.phoneNumber != null && !item.phoneNumber.trim().isEmpty()) ? item.phoneNumber.trim() : "Unknown Caller");
        holder.tvName.setText(displayName);
        holder.tvNumber.setText(item.phoneNumber != null && !item.phoneNumber.isEmpty() ? item.phoneNumber : "No number");
        holder.tvBadge.setText(item.fromBackup ? (item.type + " · ARCHIVE") : item.type);

        String dateStr = item.timestamp > 0 ? dateFmt.format(new Date(item.timestamp)) : "Unknown time";
        holder.tvMeta.setText(dateStr + " · " + formatDuration(item.duration));

        int iconRes = R.drawable.ic_call;
        int tintColor = ContextCompat.getColor(ctx, R.color.call_outgoing);
        if ("INCOMING".equalsIgnoreCase(item.type)) {
            iconRes = R.drawable.ic_call_received;
            tintColor = ContextCompat.getColor(ctx, R.color.call_incoming);
        } else if ("OUTGOING".equalsIgnoreCase(item.type)) {
            iconRes = R.drawable.ic_call_made;
            tintColor = ContextCompat.getColor(ctx, R.color.call_outgoing);
        } else if ("MISSED".equalsIgnoreCase(item.type)) {
            iconRes = R.drawable.ic_call_missed;
            tintColor = ContextCompat.getColor(ctx, R.color.call_missed);
        } else if ("REJECTED".equalsIgnoreCase(item.type) || "BLOCKED".equalsIgnoreCase(item.type)) {
            iconRes = R.drawable.ic_call_end;
            tintColor = ContextCompat.getColor(ctx, R.color.call_rejected);
        }
        holder.ivIcon.setImageResource(iconRes);
        holder.ivIcon.setImageTintList(ColorStateList.valueOf(tintColor));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCallClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatDuration(long sec) {
        if (sec <= 0) return "0s";
        long mins = sec / 60;
        long rem = sec % 60;
        if (mins > 0) return String.format(Locale.US, "%dm %02ds", mins, rem);
        return sec + "s";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvName;
        final TextView tvBadge;
        final TextView tvNumber;
        final TextView tvMeta;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_call_type_icon);
            tvName = itemView.findViewById(R.id.tv_call_name);
            tvBadge = itemView.findViewById(R.id.tv_call_badge);
            tvNumber = itemView.findViewById(R.id.tv_call_number);
            tvMeta = itemView.findViewById(R.id.tv_call_meta);
        }
    }
}
