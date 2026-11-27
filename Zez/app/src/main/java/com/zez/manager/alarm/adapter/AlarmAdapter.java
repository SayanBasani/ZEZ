package com.zez.manager.alarm.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.zez.manager.R;
import com.zez.manager.alarm.model.AlarmItem;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    public interface OnAlarmClickListener {
        void onAlarmClick(AlarmItem alarm);
        void onAlarmToggle(AlarmItem alarm, boolean isEnabled);
        void onAlarmLongClick(AlarmItem alarm);
        default void onSelectionChanged(int selectedCount, boolean selectionMode) {}
    }

    private List<AlarmItem> alarms = new ArrayList<>();
    private final OnAlarmClickListener listener;
    private final Set<Long> selectedIds = new LinkedHashSet<>();
    private boolean selectionMode = false;

    public AlarmAdapter(OnAlarmClickListener listener) {
        this.listener = listener;
    }

    public void setAlarms(List<AlarmItem> list) {
        this.alarms = (list != null) ? list : new ArrayList<>();
        Set<Long> validIds = new LinkedHashSet<>();
        for (AlarmItem a : this.alarms) {
            if (selectedIds.contains(a.id)) {
                validIds.add(a.id);
            }
        }
        selectedIds.clear();
        selectedIds.addAll(validIds);
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size(), selectionMode);
        }
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public void enterSelectionMode() {
        selectionMode = true;
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size(), true);
        }
    }

    public void clearSelection() {
        selectionMode = false;
        selectedIds.clear();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(0, false);
        }
    }

    public void toggleSelectAll() {
        selectionMode = true;
        if (selectedIds.size() >= alarms.size() && !alarms.isEmpty()) {
            selectedIds.clear();
        } else {
            selectedIds.clear();
            for (AlarmItem a : alarms) {
                selectedIds.add(a.id);
            }
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size(), true);
        }
    }

    public List<Long> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    public void toggleSelection(long id) {
        selectionMode = true;
        if (selectedIds.contains(id)) {
            selectedIds.remove(id);
        } else {
            selectedIds.add(id);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size(), true);
        }
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmItem alarm = alarms.get(position);

        holder.tvTime.setText(alarm.getFormattedTime12());
        holder.tvAmPm.setText(alarm.getAmPm());
        holder.tvLabel.setText(alarm.label);
        holder.tvDays.setText(alarm.getDaysDisplay());
        holder.tvRingtone.setText(alarm.ringtoneTitle);

        if (alarm.isJitMode) {
            holder.tvJitBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvJitBadge.setVisibility(View.GONE);
        }

        holder.switchAlarm.setOnCheckedChangeListener(null);
        holder.switchAlarm.setChecked(alarm.isEnabled);

        float alpha = alarm.isEnabled ? 1.0f : 0.45f;
        holder.tvTime.setAlpha(alpha);
        holder.tvAmPm.setAlpha(alpha);
        holder.tvLabel.setAlpha(alpha);
        holder.tvDays.setAlpha(alpha);
        holder.tvRingtone.setAlpha(alpha);

        boolean isSelected = selectedIds.contains(alarm.id);
        if (selectionMode) {
            holder.ivSelectCheck.setVisibility(View.VISIBLE);
            holder.switchAlarm.setVisibility(View.GONE);
            if (isSelected) {
                holder.ivSelectCheck.setImageResource(R.drawable.ic_check_circle);
                holder.ivSelectCheck.setColorFilter(Color.parseColor("#10B981"));
                holder.cardRoot.setStrokeColor(Color.parseColor("#10B981"));
                holder.cardRoot.setStrokeWidth(4);
            } else {
                holder.ivSelectCheck.setImageResource(R.drawable.ic_check_circle);
                holder.ivSelectCheck.setColorFilter(Color.parseColor("#64748B"));
                holder.cardRoot.setStrokeColor(Color.TRANSPARENT);
                holder.cardRoot.setStrokeWidth(1);
            }
        } else {
            holder.ivSelectCheck.setVisibility(View.GONE);
            holder.switchAlarm.setVisibility(View.VISIBLE);
            holder.cardRoot.setStrokeColor(Color.TRANSPARENT);
            holder.cardRoot.setStrokeWidth(1);
        }

        holder.switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alarm.isEnabled = isChecked;
            float newAlpha = isChecked ? 1.0f : 0.45f;
            holder.tvTime.setAlpha(newAlpha);
            holder.tvAmPm.setAlpha(newAlpha);
            holder.tvLabel.setAlpha(newAlpha);
            holder.tvDays.setAlpha(newAlpha);
            holder.tvRingtone.setAlpha(newAlpha);

            if (listener != null) {
                listener.onAlarmToggle(alarm, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(alarm.id);
            } else if (listener != null) {
                listener.onAlarmClick(alarm);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onAlarmLongClick(alarm);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardRoot;
        ImageView ivSelectCheck;
        TextView tvTime;
        TextView tvAmPm;
        SwitchMaterial switchAlarm;
        TextView tvLabel;
        TextView tvDays;
        TextView tvRingtone;
        TextView tvJitBadge;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.card_alarm_root);
            ivSelectCheck = itemView.findViewById(R.id.iv_alarm_select_check);
            tvTime = itemView.findViewById(R.id.tv_alarm_time);
            tvAmPm = itemView.findViewById(R.id.tv_alarm_ampm);
            switchAlarm = itemView.findViewById(R.id.switch_alarm);
            tvLabel = itemView.findViewById(R.id.tv_alarm_label);
            tvDays = itemView.findViewById(R.id.tv_alarm_days);
            tvRingtone = itemView.findViewById(R.id.tv_alarm_ringtone);
            tvJitBadge = itemView.findViewById(R.id.tv_alarm_jit_badge);
        }
    }
}
