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

        TextView tvTime = findViewById(R.id.tv_firing_time);
        TextView tvLabel = findViewById(R.id.tv_firing_label);
        MaterialButton btnDismiss = findViewById(R.id.btn_dismiss);
        MaterialButton btnSnooze = findViewById(R.id.btn_snooze);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        tvTime.setText(sdf.format(new Date()));
        tvLabel.setText(label);

        btnDismiss.setOnClickListener(v -> {
            stopService(new Intent(this, AlarmRingingService.class));
            finish();
        });

        btnSnooze.setOnClickListener(v -> {
            Intent snoozeIntent = new Intent(this, AlarmReceiver.class);
            snoozeIntent.setAction(AlarmReceiver.ACTION_SNOOZE_ALARM);
            snoozeIntent.putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId);
            snoozeIntent.putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label);
            sendBroadcast(snoozeIntent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent accidental dismissal via back button, stop service on explicit action
        stopService(new Intent(this, AlarmRingingService.class));
        super.onBackPressed();
    }
}
