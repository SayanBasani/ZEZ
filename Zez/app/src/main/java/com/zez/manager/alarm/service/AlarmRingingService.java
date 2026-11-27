package com.zez.manager.alarm.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.zez.manager.R;
import com.zez.manager.alarm.AlarmFiringActivity;
import com.zez.manager.alarm.receiver.AlarmReceiver;

public class AlarmRingingService extends Service {
    private static final String TAG = "AlarmRingingService";
    private static final String CHANNEL_ID = "zez_alarm_firing_channel";
    private static final int NOTIF_ID = 9001;

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        if (AlarmReceiver.ACTION_DISMISS_ALARM.equals(action)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        long alarmId = intent.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, 0);
        String label = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL);
        if (label == null || label.trim().isEmpty()) label = "Alarm";
        String ringtoneUri = intent.getStringExtra(AlarmReceiver.EXTRA_RINGTONE_URI);
        boolean isVibrate = intent.getBooleanExtra(AlarmReceiver.EXTRA_VIBRATE, true);
        boolean isJitMode = intent.getBooleanExtra(AlarmReceiver.EXTRA_IS_JIT_MODE, false);

        // Foreground notification with full screen intent
        Intent fullScreenIntent = new Intent(this, AlarmFiringActivity.class);
        fullScreenIntent.putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId);
        fullScreenIntent.putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label);
        fullScreenIntent.putExtra(AlarmReceiver.EXTRA_IS_JIT_MODE, isJitMode);
        fullScreenIntent.putExtra(AlarmReceiver.EXTRA_RINGTONE_URI, ringtoneUri);
        fullScreenIntent.putExtra(AlarmReceiver.EXTRA_VIBRATE, isVibrate);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                this,
                (int) (alarmId % Integer.MAX_VALUE),
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        Intent dismissIntent = new Intent(this, AlarmReceiver.class);
        dismissIntent.setAction(AlarmReceiver.ACTION_DISMISS_ALARM);
        dismissIntent.putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                this,
                (int) (alarmId % Integer.MAX_VALUE) + 1,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        String notifTitle = isJitMode ? "⚡ JIT MODE ALARM: " + label : label;
        String notifDesc = isJitMode ? "Relentless Morning Wakeup · Ringing repeatedly" : "Alarm is ringing";

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(notifTitle)
                .setContentText(notifDesc)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
                .setOngoing(true)
                .build();

        startForeground(NOTIF_ID, notification);

        // Play alarm audio & vibrate
        playAlarmAudio(ringtoneUri);
        if (isVibrate) {
            startVibration();
        }

        return START_STICKY;
    }

    private void playAlarmAudio(String ringtoneUri) {
        stopAudio();
        try {
            Uri alertUri;
            if (ringtoneUri != null && !ringtoneUri.isEmpty()) {
                alertUri = Uri.parse(ringtoneUri);
            } else {
                alertUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);
                if (alertUri == null) {
                    alertUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);
                }
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, alertUri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                mediaPlayer.setAudioAttributes(attributes);
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            }

            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Failed to play alarm audio: " + e.getMessage(), e);
        }
    }

    private void startVibration() {
        stopVibration();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = new long[]{0, 500, 500, 500};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
    }

    private void stopVibration() {
        if (vibrator != null) {
            try {
                vibrator.cancel();
            } catch (Exception ignored) {}
            vibrator = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Zez Alarm Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("High priority ringing alarms and wakeup notifications");
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        stopAudio();
        stopVibration();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
