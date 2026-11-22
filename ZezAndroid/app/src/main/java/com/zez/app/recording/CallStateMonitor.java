package com.zez.app.recording;

import android.content.Context;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class CallStateMonitor {
    private static final String TAG = "CallStateMonitor";

    public interface CallStateListener {
        void onCallStarted(String phoneNumber, String direction);
        void onCallEnded(String phoneNumber);
    }

    private final Context context;
    private final TelephonyManager telephonyManager;
    private CallStateListener listener;

    private int lastState = TelephonyManager.CALL_STATE_IDLE;
    private boolean isCallActive = false;
    private String currentPhoneNumber = "";
    private String currentDirection = "OUTGOING";
    private long lastStateChangeTime = 0;

    public CallStateMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.telephonyManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void setListener(CallStateListener listener) {
        this.listener = listener;
    }

    public synchronized void onExternalStateChanged(int state, String phoneNumber, String direction) {
        handleStateTransition(state, phoneNumber, direction);
    }

    private synchronized void handleStateTransition(int state, String phoneNumber, String direction) {
        long now = System.currentTimeMillis();
        if (state == lastState && (now - lastStateChangeTime < 1000)) {
            return; // Debounce duplicate state events
        }

        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            currentPhoneNumber = phoneNumber.trim();
        }
        if (direction != null) {
            currentDirection = direction;
        }

        Log.d(TAG, "CallStateTransition: " + stateToString(lastState) + " -> " + stateToString(state)
                + " [Phone: " + currentPhoneNumber + ", Dir: " + currentDirection + "]");

        if (state == TelephonyManager.CALL_STATE_RINGING) {
            currentDirection = "INCOMING";
        } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
            if (!isCallActive) {
                isCallActive = true;
                if (listener != null) {
                    listener.onCallStarted(currentPhoneNumber, currentDirection);
                }
            }
        } else if (state == TelephonyManager.CALL_STATE_IDLE) {
            if (isCallActive) {
                isCallActive = false;
                if (listener != null) {
                    listener.onCallEnded(currentPhoneNumber);
                }
            }
            // Reset state
            currentPhoneNumber = "";
            currentDirection = "OUTGOING";
        }

        lastState = state;
        lastStateChangeTime = now;
    }

    private String stateToString(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING: return "RINGING";
            case TelephonyManager.CALL_STATE_OFFHOOK: return "OFFHOOK";
            case TelephonyManager.CALL_STATE_IDLE: return "IDLE";
            default: return "UNKNOWN(" + state + ")";
        }
    }
}
