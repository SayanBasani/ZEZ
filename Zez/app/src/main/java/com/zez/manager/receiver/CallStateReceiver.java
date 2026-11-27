package com.zez.manager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;

import com.zez.manager.service.CallSyncWorker;
import com.zez.manager.util.PreferencesHelper;

public class CallStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) return;

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            Context appCtx = context.getApplicationContext();
            if (!PreferencesHelper.isAutoSyncEnabled(appCtx) || !PreferencesHelper.isCallsSyncEnabled(appCtx)) return;
            final PendingResult pendingResult = goAsync();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    CallSyncWorker.syncNowAsync(appCtx);
                    CallSyncWorker.scheduleImmediateSync(appCtx);
                } finally {
                    try {
                        pendingResult.finish();
                    } catch (Exception ignored) {}
                }
            }, 1500L);
        }
    }
}
