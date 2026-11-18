package com.zez.app.adapter;

import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zez.app.R;
import com.zez.app.model.CallLogItem;

import java.util.ArrayList;
import java.util.List;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.CallViewHolder> {
    private List<CallLogItem> calls = new ArrayList<>();

    public void setCalls(List<CallLogItem> list) {
        this.calls = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_log, parent, false);
        return new CallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
        CallLogItem item = calls.get(position);
        holder.tvName.setText(item.getName());
        holder.tvNumber.setText(item.getPhoneNumber() != null ? item.getPhoneNumber() : "");
        holder.tvType.setText(item.getType());

        if ("INCOMING".equals(item.getType())) {
            holder.tvType.setTextColor(Color.parseColor("#16A34A"));
        } else if ("OUTGOING".equals(item.getType())) {
            holder.tvType.setTextColor(Color.parseColor("#2563EB"));
        } else {
            holder.tvType.setTextColor(Color.parseColor("#DC2626"));
        }

        String dateStr = DateFormat.format("dd MMM yyyy, hh:mm a", item.getDate()).toString();
        holder.tvDate.setText(dateStr);
        holder.tvDuration.setText("Duration: " + item.getDuration() + "s");
    }

    @Override
    public int getItemCount() {
        return calls.size();
    }

    static class CallViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber, tvType, tvDate, tvDuration;

        public CallViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_caller_name);
            tvNumber = itemView.findViewById(R.id.tv_phone_number);
            tvType = itemView.findViewById(R.id.tv_call_type);
            tvDate = itemView.findViewById(R.id.tv_call_date);
            tvDuration = itemView.findViewById(R.id.tv_duration);
        }
    }
}
