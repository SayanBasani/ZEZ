package com.zez.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zez.app.service.CallSyncWorker;
import com.zez.app.util.CallRecordManager;

public class CallStateReceiver extends BroadcastReceiver {
    private static final String TAG = "CallStateReceiver";
    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;
    private static String savedNumber = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();

        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            Log.d(TAG, "ACTION_PHONE_STATE_CHANGED: state=" + state + ", number=" + number);

            if (number != null && !number.trim().isEmpty()) {
                savedNumber = number;
            }

            if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state) && !TelephonyManager.EXTRA_STATE_OFFHOOK.equals(lastState)) {
                // Cellular phone call answered/in progress - silently start recording automatically via foreground service
                Log.d(TAG, "Cellular call active (OFFHOOK). Starting silent recording service...");
                try {
                    String direction = TelephonyManager.EXTRA_STATE_RINGING.equals(lastState) ? "INCOMING" : "OUTGOING";
                    Intent serviceIntent = new Intent(context, com.zez.app.service.CallRecordService.class);
                    serviceIntent.setAction(com.zez.app.service.CallRecordService.ACTION_START);
                    serviceIntent.putExtra(com.zez.app.service.CallRecordService.EXTRA_PHONE_NUMBER, savedNumber);
                    serviceIntent.putExtra(com.zez.app.service.CallRecordService.EXTRA_DIRECTION, direction);
                    serviceIntent.putExtra(com.zez.app.service.CallRecordService.EXTRA_CALL_TYPE, "PHONE_CALL");
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent);
                    } else {
                        context.startService(serviceIntent);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to start CallRecordService: " + e.getMessage(), e);
                }
            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state) && !TelephonyManager.EXTRA_STATE_IDLE.equals(lastState)) {
                // Call just transitioned to IDLE (call hung up, missed, or rejected)
                Log.d(TAG, "Call ended (IDLE). Stopping recording service & triggering immediate backend sync...");
                try {
                    Intent serviceIntent = new Intent(context, com.zez.app.service.CallRecordService.class);
                    serviceIntent.setAction(com.zez.app.service.CallRecordService.ACTION_STOP);
                    context.startService(serviceIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Error stopping CallRecordService on IDLE: " + e.getMessage(), e);
                }
                savedNumber = "";
                CallSyncWorker.syncNowAsync(context);
                CallSyncWorker.scheduleImmediateSync(context);
            }

            if (state != null) {
                lastState = state;
            }
        }
    }
}
