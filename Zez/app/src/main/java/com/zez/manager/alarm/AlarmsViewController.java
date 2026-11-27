package com.zez.manager.alarm;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.zez.manager.MainActivity;
import com.zez.manager.R;
import com.zez.manager.alarm.adapter.AlarmAdapter;
import com.zez.manager.alarm.model.AlarmItem;
import com.zez.manager.alarm.util.AlarmHelper;

import java.util.List;

public class AlarmsViewController {

    public static final int RINGTONE_PICKER_REQUEST_CODE = 9010;

    private final MainActivity activity;

    private RecyclerView recyclerAlarms;
    private View layoutAlarmsEmpty;
    private FloatingActionButton fabAddAlarm;
    private View layoutAlarmsQuickBar;
    private View layoutAlarmsSelectionBar;
    private TextView tvAlarmsSelCount;

    private AlarmAdapter alarmAdapter;
    private AlarmItem pendingRingtoneAlarm;
    private TextView pendingRingtoneTextView;

    public AlarmsViewController(@NonNull MainActivity activity) {
        this.activity = activity;
        initViews();
        setupListeners();
        loadAlarmsData();
    }

    private void initViews() {
        recyclerAlarms = activity.findViewById(R.id.recycler_alarms);
        layoutAlarmsEmpty = activity.findViewById(R.id.layout_alarms_empty);
        fabAddAlarm = activity.findViewById(R.id.fab_add_alarm);
        layoutAlarmsQuickBar = activity.findViewById(R.id.layout_alarms_quick_bar);
        layoutAlarmsSelectionBar = activity.findViewById(R.id.layout_alarms_selection_bar);
        tvAlarmsSelCount = activity.findViewById(R.id.tv_alarms_sel_count);
    }

    private void setupListeners() {
        View btnEnterAlarmSel = activity.findViewById(R.id.btn_alarms_enter_select);
        View btnAlarmSelClose = activity.findViewById(R.id.btn_alarms_sel_close);
        View btnAlarmSelAll = activity.findViewById(R.id.btn_alarms_sel_all);
        View btnAlarmSelOn = activity.findViewById(R.id.btn_alarms_sel_on);
        View btnAlarmSelOff = activity.findViewById(R.id.btn_alarms_sel_off);
        View btnAlarmSelDelete = activity.findViewById(R.id.btn_alarms_sel_delete);

        alarmAdapter = new AlarmAdapter(new AlarmAdapter.OnAlarmClickListener() {
            @Override
            public void onAlarmClick(AlarmItem alarm) {
                showEditAlarmDialog(alarm);
            }

            @Override
            public void onAlarmToggle(AlarmItem alarm, boolean isEnabled) {
                AlarmHelper.setAlarmEnabled(activity, alarm.id, isEnabled);
                loadAlarmsData();
            }

            @Override
            public void onAlarmLongClick(AlarmItem alarm) {
                if (alarmAdapter != null && !alarmAdapter.isSelectionMode()) {
                    alarmAdapter.toggleSelection(alarm.id);
                }
            }

            @Override
            public void onSelectionChanged(int selectedCount, boolean selectionMode) {
                if (layoutAlarmsSelectionBar != null) {
                    layoutAlarmsSelectionBar.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
                }
                if (layoutAlarmsQuickBar != null) {
                    boolean hasAlarms = alarmAdapter != null && alarmAdapter.getItemCount() > 0;
                    layoutAlarmsQuickBar.setVisibility(!selectionMode && hasAlarms ? View.VISIBLE : View.GONE);
                }
                if (tvAlarmsSelCount != null) {
                    tvAlarmsSelCount.setText(selectedCount + " selected");
                }
            }
        });

        if (recyclerAlarms != null) {
            recyclerAlarms.setLayoutManager(new LinearLayoutManager(activity));
            recyclerAlarms.setAdapter(alarmAdapter);
        }

        if (fabAddAlarm != null) {
            fabAddAlarm.setOnClickListener(v -> {
                AlarmItem newAlarm = new AlarmItem();
                showEditAlarmDialog(newAlarm);
            });
        }

        if (btnEnterAlarmSel != null) {
            btnEnterAlarmSel.setOnClickListener(v -> {
                if (alarmAdapter != null) alarmAdapter.enterSelectionMode();
            });
        }
        if (btnAlarmSelClose != null) {
            btnAlarmSelClose.setOnClickListener(v -> {
                if (alarmAdapter != null) alarmAdapter.clearSelection();
            });
        }
        if (btnAlarmSelAll != null) {
            btnAlarmSelAll.setOnClickListener(v -> {
                if (alarmAdapter != null) alarmAdapter.toggleSelectAll();
            });
        }
        if (btnAlarmSelOn != null) {
            btnAlarmSelOn.setOnClickListener(v -> {
                if (alarmAdapter == null) return;
                List<Long> sel = alarmAdapter.getSelectedIds();
                if (sel.isEmpty()) {
                    Toast.makeText(activity, "Select alarms first", Toast.LENGTH_SHORT).show();
                    return;
                }
                AlarmHelper.setAlarmsEnabled(activity, sel, true);
                alarmAdapter.clearSelection();
                loadAlarmsData();
                Toast.makeText(activity, "Activated " + sel.size() + " alarm(s)", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnAlarmSelOff != null) {
            btnAlarmSelOff.setOnClickListener(v -> {
                if (alarmAdapter == null) return;
                List<Long> sel = alarmAdapter.getSelectedIds();
                if (sel.isEmpty()) {
                    Toast.makeText(activity, "Select alarms first", Toast.LENGTH_SHORT).show();
                    return;
                }
                AlarmHelper.setAlarmsEnabled(activity, sel, false);
                alarmAdapter.clearSelection();
                loadAlarmsData();
                Toast.makeText(activity, "Turned off " + sel.size() + " alarm(s)", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnAlarmSelDelete != null) {
            btnAlarmSelDelete.setOnClickListener(v -> {
                if (alarmAdapter == null) return;
                List<Long> sel = alarmAdapter.getSelectedIds();
                if (sel.isEmpty()) {
                    Toast.makeText(activity, "Select alarms first", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(activity)
                        .setTitle("Delete Alarms")
                        .setMessage("Delete " + sel.size() + " selected alarm(s)?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            AlarmHelper.deleteAlarms(activity, sel);
                            alarmAdapter.clearSelection();
                            loadAlarmsData();
                            Toast.makeText(activity, "Deleted " + sel.size() + " alarm(s)", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    public void loadAlarmsData() {
        List<AlarmItem> list = AlarmHelper.getAlarms(activity);
        if (alarmAdapter != null) {
            alarmAdapter.setAlarms(list);
        }
        boolean hasAlarms = (list != null && !list.isEmpty());
        if (layoutAlarmsEmpty != null) {
            layoutAlarmsEmpty.setVisibility(hasAlarms ? View.GONE : View.VISIBLE);
        }
        if (layoutAlarmsQuickBar != null) {
            boolean inSelection = (alarmAdapter != null && alarmAdapter.isSelectionMode());
            layoutAlarmsQuickBar.setVisibility(hasAlarms && !inSelection ? View.VISIBLE : View.GONE);
        }
    }

    public void showEditAlarmDialog(AlarmItem alarm) {
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_edit_alarm, null);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
        TextInputEditText etLabel = dialogView.findViewById(R.id.et_alarm_label);
        LinearLayout rowRingtone = dialogView.findViewById(R.id.row_select_ringtone);
        TextView tvSelectedRingtone = dialogView.findViewById(R.id.tv_selected_ringtone);
        SwitchMaterial switchVibrate = dialogView.findViewById(R.id.switch_vibrate);
        SwitchMaterial switchJitMode = dialogView.findViewById(R.id.switch_jit_mode);

        ToggleButton tbSun = dialogView.findViewById(R.id.tb_sun);
        ToggleButton tbMon = dialogView.findViewById(R.id.tb_mon);
        ToggleButton tbTue = dialogView.findViewById(R.id.tb_tue);
        ToggleButton tbWed = dialogView.findViewById(R.id.tb_wed);
        ToggleButton tbThu = dialogView.findViewById(R.id.tb_thu);
        ToggleButton tbFri = dialogView.findViewById(R.id.tb_fri);
        ToggleButton tbSat = dialogView.findViewById(R.id.tb_sat);

        Button btnDelete = dialogView.findViewById(R.id.btn_dialog_delete);
        Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_dialog_save);

        boolean isExisting = false;
        List<AlarmItem> existingList = AlarmHelper.getAlarms(activity);
        for (AlarmItem a : existingList) {
            if (a.id == alarm.id) { isExisting = true; break; }
        }

        tvDialogTitle.setText(isExisting ? "Edit Alarm" : "Add Alarm");
        if (isExisting) {
            btnDelete.setVisibility(View.VISIBLE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(alarm.hour);
            timePicker.setMinute(alarm.minute);
        } else {
            timePicker.setCurrentHour(alarm.hour);
            timePicker.setCurrentMinute(alarm.minute);
        }

        etLabel.setText(alarm.label);
        tvSelectedRingtone.setText(alarm.ringtoneTitle);
        switchVibrate.setChecked(alarm.isVibrate);
        if (switchJitMode != null) {
            switchJitMode.setChecked(alarm.isJitMode);
        }

        if (alarm.daysOfWeek != null && alarm.daysOfWeek.length == 7) {
            tbSun.setChecked(alarm.daysOfWeek[0]);
            tbMon.setChecked(alarm.daysOfWeek[1]);
            tbTue.setChecked(alarm.daysOfWeek[2]);
            tbWed.setChecked(alarm.daysOfWeek[3]);
            tbThu.setChecked(alarm.daysOfWeek[4]);
            tbFri.setChecked(alarm.daysOfWeek[5]);
            tbSat.setChecked(alarm.daysOfWeek[6]);
        }

        pendingRingtoneAlarm = alarm;
        pendingRingtoneTextView = tvSelectedRingtone;

        rowRingtone.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Ringtone");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);

            Uri currentUri = (alarm.ringtoneUri != null && !alarm.ringtoneUri.isEmpty())
                    ? Uri.parse(alarm.ringtoneUri)
                    : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri);
            activity.startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE);
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            AlarmHelper.deleteAlarm(activity, alarm.id);
            dialog.dismiss();
            loadAlarmsData();
        });

        btnSave.setOnClickListener(v -> {
            int hour = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? timePicker.getHour() : timePicker.getCurrentHour();
            int minute = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? timePicker.getMinute() : timePicker.getCurrentMinute();

            alarm.hour = hour;
            alarm.minute = minute;
            alarm.label = etLabel.getText() != null && !etLabel.getText().toString().trim().isEmpty()
                    ? etLabel.getText().toString().trim()
                    : "Alarm";
            alarm.isVibrate = switchVibrate.isChecked();
            if (switchJitMode != null) {
                alarm.isJitMode = switchJitMode.isChecked();
            }
            alarm.isEnabled = true;

            alarm.daysOfWeek = new boolean[]{
                    tbSun.isChecked(),
                    tbMon.isChecked(),
                    tbTue.isChecked(),
                    tbWed.isChecked(),
                    tbThu.isChecked(),
                    tbFri.isChecked(),
                    tbSat.isChecked()
            };

            AlarmHelper.addOrUpdateAlarm(activity, alarm);
            dialog.dismiss();
            loadAlarmsData();
        });

        dialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RINGTONE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (pendingRingtoneAlarm != null) {
                if (uri != null) {
                    pendingRingtoneAlarm.ringtoneUri = uri.toString();
                    pendingRingtoneAlarm.ringtoneTitle = AlarmHelper.getRingtoneTitle(activity, uri.toString());
                } else {
                    pendingRingtoneAlarm.ringtoneUri = "";
                    pendingRingtoneAlarm.ringtoneTitle = "Silent";
                }
                if (pendingRingtoneTextView != null) {
                    pendingRingtoneTextView.setText(pendingRingtoneAlarm.ringtoneTitle);
                }
            }
        }
    }

    public boolean onBackPressed() {
        if (alarmAdapter != null && alarmAdapter.isSelectionMode()) {
            alarmAdapter.clearSelection();
            return true;
        }
        return false;
    }

    public void onResume() {
        loadAlarmsData();
        AlarmHelper.syncWithBackend(activity, this::loadAlarmsData);
    }
}
