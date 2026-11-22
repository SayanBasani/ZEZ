package com.zez.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.zez.app.receiver.AlarmReceiver;
import com.zez.app.service.AlarmRingingService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmFiringActivity extends AppCompatActivity {

    private long alarmId;
    private String label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turn screen on and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }

        setContentView(R.layout.activity_alarm_firing);

        alarmId = getIntent().getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, 0);
        label = getIntent().getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL);
        if (label == null || label.trim().isEmpty()) label = "Alarm";
        boolean isJitMode = getIntent().getBooleanExtra(AlarmReceiver.EXTRA_IS_JIT_MODE, false);

        TextView tvTime = findViewById(R.id.tv_firing_time);
        TextView tvLabel = findViewById(R.id.tv_firing_label);
        MaterialButton btnDismiss = findViewById(R.id.btn_dismiss);
        MaterialButton btnSnooze = findViewById(R.id.btn_snooze);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        tvTime.setText(sdf.format(new Date()));
        if (isJitMode) {
            tvLabel.setText("⚡ JIT MODE: " + label + "\nRelentless Wakeup (Rings every 1 min)");
            tvLabel.setTextColor(android.graphics.Color.parseColor("#EF4444"));
            btnSnooze.setText("Snooze (1m)");
        } else {
            tvLabel.setText(label);
        }

        btnDismiss.setOnClickListener(v -> {
            Intent dismissIntent = new Intent(this, AlarmReceiver.class);
            dismissIntent.setAction(AlarmReceiver.ACTION_DISMISS_ALARM);
            dismissIntent.putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId);
            sendBroadcast(dismissIntent);

            stopService(new Intent(this, AlarmRingingService.class));
            finish();
        });

        btnSnooze.setOnClickListener(v -> {
            Intent snoozeIntent = new Intent(this, AlarmReceiver.class);
            snoozeIntent.setAction(AlarmReceiver.ACTION_SNOOZE_ALARM);
            snoozeIntent.putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId);
            snoozeIntent.putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label);
            snoozeIntent.putExtra(AlarmReceiver.EXTRA_IS_JIT_MODE, isJitMode);
            snoozeIntent.putExtra(AlarmReceiver.EXTRA_RINGTONE_URI, getIntent().getStringExtra(AlarmReceiver.EXTRA_RINGTONE_URI));
            snoozeIntent.putExtra(AlarmReceiver.EXTRA_VIBRATE, getIntent().getBooleanExtra(AlarmReceiver.EXTRA_VIBRATE, true));
            sendBroadcast(snoozeIntent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Intent dismissIntent = new Intent(this, AlarmReceiver.class);
        dismissIntent.setAction(AlarmReceiver.ACTION_DISMISS_ALARM);
        dismissIntent.putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId);
        sendBroadcast(dismissIntent);

        stopService(new Intent(this, AlarmRingingService.class));
        super.onBackPressed();
    }
}
