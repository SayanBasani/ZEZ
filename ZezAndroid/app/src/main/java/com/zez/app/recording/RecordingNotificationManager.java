package com.zez.app.recording;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.zez.app.R;
import com.zez.app.recording.model.RecordingState;

public class RecordingNotificationManager {
    public static final int NOTIFICATION_ID = 10091;
    public static final String CHANNEL_ID = "twing_call_recording_channel";

    private final Context context;
    private final NotificationManager notificationManager;

    public RecordingNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Call Protection & Recording",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Ongoing indicator when call recording or protection is active");
            channel.setSound(null, null);
            channel.enableVibration(false);
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Notification buildRecordingNotification(String contactOrNumber) {
        return buildRecordingNotification(contactOrNumber, RecordingState.RECORDING_ACTIVE);
    }

    public Notification buildRecordingNotification(String contactOrNumber, RecordingState state) {
        String title = "TWING Call Recording";
        String subtitle;

        String target = (contactOrNumber != null && !contactOrNumber.isEmpty()) ? contactOrNumber : "Active Call";

        if (state == RecordingState.AUDIO_CAPTURE_CONFIRMED) {
            subtitle = "🎙️ Audio capture verified (" + target + ")";
        } else if (state == RecordingState.AUDIO_CAPTURE_SILENCED) {
            subtitle = "⚠️ Audio input blocked by Android (" + target + ")";
        } else if (state == RecordingState.RECORDING_INITIALIZING) {
            subtitle = "Initializing recording engine...";
        } else {
            subtitle = "Active call recording: " + target;
        }

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_shield_check)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSilent(true)
                .setOngoing(true)
                .build();
    }

    public void updateNotification(String contactOrNumber, RecordingState state) {
        if (notificationManager != null) {
            try {
                notificationManager.notify(NOTIFICATION_ID, buildRecordingNotification(contactOrNumber, state));
            } catch (Exception ignored) {}
        }
    }
}
