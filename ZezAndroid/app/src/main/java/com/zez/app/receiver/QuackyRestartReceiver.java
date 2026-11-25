package com.zez.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zez.app.service.CallSyncWorker;
import com.zez.app.service.QuackyBackgroundService;
import com.zez.app.util.ChatNotificationManager;

/**
 * Restarts background message & call notification listeners after task removal.
 */
public class QuackyRestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) return;
        ChatNotificationManager.getInstance().start(context.getApplicationContext());
        QuackyBackgroundService.ensureRunning(context.getApplicationContext());
        CallSyncWorker.scheduleImmediateSync(context.getApplicationContext());
    }
}
