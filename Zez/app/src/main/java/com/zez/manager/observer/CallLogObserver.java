package com.zez.manager.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.zez.manager.service.CallSyncWorker;
import com.zez.manager.util.PreferencesHelper;

public class CallLogObserver extends ContentObserver {
    private static final long DEBOUNCE_MS = 1200L;
    private final Context appContext;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable syncRunnable;

    public CallLogObserver(Handler handler, Context context) {
        super(handler);
        this.appContext = context.getApplicationContext();
        this.syncRunnable = () -> {
            if (!PreferencesHelper.isAutoSyncEnabled(appContext) || !PreferencesHelper.isCallsSyncEnabled(appContext)) return;
            CallSyncWorker.syncNowAsync(appContext);
            CallSyncWorker.scheduleImmediateSync(appContext);
        };
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        handler.removeCallbacks(syncRunnable);
        handler.postDelayed(syncRunnable, DEBOUNCE_MS);
    }
}
