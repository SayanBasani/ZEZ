package com.zez.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.zez.app.R;
import com.zez.app.util.CallRecordManager;

public class CallRecordService extends Service {
    private static final String TAG = "CallRecordService";
    private static final int NOTIFICATION_ID = 10091;
    private static final String CHANNEL_ID = "twing_silent_call_service";

    public static final String ACTION_START = "com.zez.app.action.START_RECORDING";
    public static final String ACTION_STOP = "com.zez.app.action.STOP_RECORDING";

    public static final String EXTRA_PHONE_NUMBER = "extra_phone_number";
    public static final String EXTRA_CONTACT_NAME = "extra_contact_name";
    public static final String EXTRA_CALL_TYPE = "extra_call_type";
    public static final String EXTRA_DIRECTION = "extra_direction";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            Notification notification = buildSilentNotification();
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
                } else {
                    startForeground(NOTIFICATION_ID, notification);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed startForeground with microphone: " + e.getMessage(), e);
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
                    } else {
                        startForeground(NOTIFICATION_ID, notification);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Failed fallback startForeground: " + ex.getMessage(), ex);
                }
            }

            String phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
            String contactName = intent.getStringExtra(EXTRA_CONTACT_NAME);
            String callType = intent.getStringExtra(EXTRA_CALL_TYPE);
            String direction = intent.getStringExtra(EXTRA_DIRECTION);

            CallRecordManager.getInstance(this).startRecording(phoneNumber, contactName, callType, direction);
        } else if (ACTION_STOP.equals(action)) {
            CallRecordManager.getInstance(this).stopRecording();
            stopForeground(true);
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Call Protection Service",
                        NotificationManager.IMPORTANCE_LOW
                );
                channel.setDescription("Silent service for secure active call recording");
                channel.setSound(null, null);
                channel.enableVibration(false);
                channel.setShowBadge(false);
                nm.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildSilentNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_shield_check)
                .setContentTitle("TWING Call Protection")
                .setContentText("Active call security running")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSilent(true)
                .setOngoing(true)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        CallRecordManager.getInstance(this).stopRecording();
        super.onDestroy();
    }
}
