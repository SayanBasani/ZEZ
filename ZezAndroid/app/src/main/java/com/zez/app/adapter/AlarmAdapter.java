package com.zez.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.zez.app.R;
import com.zez.app.model.AlarmItem;

import java.util.ArrayList;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    public interface OnAlarmClickListener {
        void onAlarmClick(AlarmItem alarm);
        void onAlarmToggle(AlarmItem alarm, boolean isEnabled);
        void onAlarmLongClick(AlarmItem alarm);
    }

    private List<AlarmItem> alarms = new ArrayList<>();
    private final OnAlarmClickListener listener;

    public AlarmAdapter(OnAlarmClickListener listener) {
        this.listener = listener;
    }

    public void setAlarms(List<AlarmItem> list) {
        this.alarms = (list != null) ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmItem item = alarms.get(position);
        holder.tvTime.setText(item.getFormattedTime12());
        holder.tvAmPm.setText(item.getAmPm());
        holder.tvLabel.setText(item.label);
        holder.tvDays.setText(item.getDaysDisplay());
        holder.tvRingtone.setText("Sound: " + item.ringtoneTitle);

        holder.switchAlarm.setOnCheckedChangeListener(null);
        holder.switchAlarm.setChecked(item.isEnabled);
        holder.switchAlarm.setOnCheckedChangeListener((btn, isChecked) -> {
            if (listener != null) {
                listener.onAlarmToggle(item, isChecked);
            }
        });

        // Visually dim disabled alarms
        float alpha = item.isEnabled ? 1.0f : 0.45f;
        holder.tvTime.setAlpha(alpha);
        holder.tvAmPm.setAlpha(alpha);
        holder.tvLabel.setAlpha(alpha);
        holder.tvDays.setAlpha(alpha);
        holder.tvRingtone.setAlpha(alpha);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAlarmClick(item);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onAlarmLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvAmPm, tvLabel, tvDays, tvRingtone;
        SwitchMaterial switchAlarm;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_alarm_time);
            tvAmPm = itemView.findViewById(R.id.tv_alarm_ampm);
            tvLabel = itemView.findViewById(R.id.tv_alarm_label);
            tvDays = itemView.findViewById(R.id.tv_alarm_days);
            tvRingtone = itemView.findViewById(R.id.tv_alarm_ringtone);
            switchAlarm = itemView.findViewById(R.id.switch_alarm);
        }
    }
}
