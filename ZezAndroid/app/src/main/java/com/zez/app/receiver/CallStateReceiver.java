package com.zez.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zez.app.model.CallLogItem;
import com.zez.app.service.CallSyncWorker;
import com.zez.app.util.CallLogHelper;
import com.zez.app.util.CallRecordManager;

public class CallStateReceiver extends BroadcastReceiver {
    private static final String TAG = "CallStateReceiver";
    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;
    private static String savedNumber = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();

        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
            String outNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (outNumber == null || outNumber.trim().isEmpty()) {
                outNumber = intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
            }
            if (outNumber != null && !outNumber.trim().isEmpty()) {
                savedNumber = outNumber.trim();
                Log.d(TAG, "Captured outgoing number via ACTION_NEW_OUTGOING_CALL: " + savedNumber);
                CallRecordManager.getInstance(context).updateCurrentCallNumber(savedNumber);
            }
            return;
        }

        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (number == null || number.trim().isEmpty()) {
                number = intent.getStringExtra("incoming_number");
            }
            Log.d(TAG, "ACTION_PHONE_STATE_CHANGED: state=" + state + ", number=" + number);

            if (number != null && !number.trim().isEmpty()) {
                savedNumber = number.trim();
                CallRecordManager.getInstance(context).updateCurrentCallNumber(savedNumber);
            }

            if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state) && !TelephonyManager.EXTRA_STATE_OFFHOOK.equals(lastState)) {
                // Cellular phone call active (OFFHOOK)
                Log.d(TAG, "Cellular call active (OFFHOOK). Starting silent recording service...");
                String direction = TelephonyManager.EXTRA_STATE_RINGING.equals(lastState) ? "INCOMING" : "OUTGOING";

                // If outgoing and number was not received via intent yet, query recent CallLog
                if ((savedNumber == null || savedNumber.isEmpty()) && "OUTGOING".equals(direction)) {
                    try {
                        CallLogItem lastCall = CallLogHelper.getLastCallLog(context);
                        if (lastCall != null && lastCall.getPhoneNumber() != null && !lastCall.getPhoneNumber().isEmpty()) {
                            savedNumber = lastCall.getPhoneNumber().trim();
                        }
                    } catch (Exception ignored) {}
                }

                try {
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
                    Log.w(TAG, "startForegroundService restricted, starting in-process recording directly: " + e.getMessage());
                    CallRecordManager.getInstance(context).startRecording(savedNumber, savedNumber, "PHONE_CALL", direction);
                }
            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state) && !TelephonyManager.EXTRA_STATE_IDLE.equals(lastState)) {
                // Call ended (IDLE)
                Log.d(TAG, "Call ended (IDLE). Stopping recording service & resolving caller info...");
                try {
                    Intent serviceIntent = new Intent(context, com.zez.app.service.CallRecordService.class);
                    serviceIntent.setAction(com.zez.app.service.CallRecordService.ACTION_STOP);
                    context.startService(serviceIntent);
                } catch (Exception ignored) {}

                CallRecordManager.getInstance(context).stopRecording();
                savedNumber = "";
                CallSyncWorker.syncNowAsync(context);
                CallSyncWorker.scheduleImmediateSync(context);

                // Trigger OEM native call recording import scan
                try {
                    com.zez.app.recording.importing.RecordingSyncWorker.triggerImmediateScan(context);
                } catch (Exception e) {
                    Log.e(TAG, "Error triggering RecordingSyncWorker: " + e.getMessage());
                }
            }

            if (state != null) {
                lastState = state;
            }
        }
    }
}
