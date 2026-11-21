package com.zez.app.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.zez.app.service.CallSyncWorker;

public class CallLogObserver extends ContentObserver {
    private static final String TAG = "CallLogObserver";
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable syncRunnable;
    private static CallLogListener listener;

    public interface CallLogListener {
        void onCallLogsChanged();
    }

    public static void setListener(CallLogListener l) {
        listener = l;
    }

    public CallLogObserver(Context context) {
        super(new Handler(Looper.getMainLooper()));
        this.context = context.getApplicationContext();
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d(TAG, "Call log change detected via ContentObserver: " + (uri != null ? uri.toString() : "null"));

        // Debounce: Android emits multiple notifications during a call lifecycle
        if (syncRunnable != null) {
            handler.removeCallbacks(syncRunnable);
        }

        syncRunnable = () -> {
            Log.d(TAG, "Debounce window elapsed. Executing immediate sync to backend for new/updated call...");
            try {
                com.zez.app.model.CallLogItem lastCall = com.zez.app.util.CallLogHelper.getLastCallLog(context);
                if (lastCall != null && lastCall.getPhoneNumber() != null && !lastCall.getPhoneNumber().trim().isEmpty()) {
                    java.util.List<com.zez.app.model.CallRecordItem> recs = com.zez.app.util.CallRecordDbHelper.getInstance(context).getAllRecordings();
                    if (recs != null && !recs.isEmpty()) {
                        com.zez.app.model.CallRecordItem topRec = recs.get(0);
                        if (topRec != null && (topRec.getPhoneNumber().isEmpty() || topRec.getPhoneNumber().equalsIgnoreCase("unknown"))) {
                            String realNum = lastCall.getPhoneNumber().trim();
                            String realName = lastCall.getName();
                            if (realName == null || realName.trim().isEmpty() || "Unknown".equalsIgnoreCase(realName.trim())) {
                                realName = realNum;
                            }
                            com.zez.app.util.CallRecordDbHelper.getInstance(context).updateContactAndPhone(topRec.getId(), realNum, realName);
                            Log.d(TAG, "CallLogObserver synced recording " + topRec.getId() + " with caller " + realNum);
                        }
                    }
                }
            } catch (Exception ignored) {}

            CallSyncWorker.syncNowAsync(context);
            CallSyncWorker.scheduleImmediateSync(context);

            if (listener != null) {
                try {
                    listener.onCallLogsChanged();
                } catch (Exception e) {
                    Log.w(TAG, "Error in listener callback: " + e.getMessage());
                }
            }
        };

        // 1200ms debounce allows Android OS to finish writing duration, caller cached name, and status
        handler.postDelayed(syncRunnable, 1200);
    }
}
