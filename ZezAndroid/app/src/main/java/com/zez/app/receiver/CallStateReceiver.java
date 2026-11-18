package com.zez.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zez.app.service.CallSyncWorker;

public class CallStateReceiver extends BroadcastReceiver {
    private static final String TAG = "CallStateReceiver";
    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();

        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            Log.d(TAG, "ACTION_PHONE_STATE_CHANGED: state=" + state + ", number=" + number);

            if (TelephonyManager.EXTRA_STATE_IDLE.equals(state) && !TelephonyManager.EXTRA_STATE_IDLE.equals(lastState)) {
                // Call just transitioned to IDLE (call hung up, missed, or rejected)
                Log.d(TAG, "Call ended (IDLE). Triggering immediate backend sync...");
                CallSyncWorker.syncNowAsync(context);
                CallSyncWorker.scheduleImmediateSync(context);
            }

            if (state != null) {
                lastState = state;
            }
        }
    }
}
