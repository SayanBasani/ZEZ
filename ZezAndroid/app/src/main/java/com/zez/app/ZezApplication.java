package com.zez.app;

import android.app.Application;
import android.provider.CallLog;
import android.util.Log;

import com.zez.app.observer.CallLogObserver;
import com.zez.app.receiver.BootReceiver;
import com.zez.app.service.QuackyBackgroundService;
import com.zez.app.util.AlarmHelper;
import com.zez.app.util.ChatNotificationHelper;
import com.zez.app.util.ChatNotificationManager;
import com.zez.app.util.QuackyStorageHelper;

public class ZezApplication extends Application {
    private static final String TAG = "ZezApplication";
    private CallLogObserver callLogObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Zez Application initialized.");

        // Apply saved theme (System Default / Light / Dark) & dynamic colors
        com.zez.app.theme.ThemeManager.init(this);

        // Pre-register notification channels & start persistent background listener/service
        ChatNotificationHelper.createNotificationChannel(this);
        ChatNotificationHelper.createCallNotificationChannel(this);
        BootReceiver.schedulePeriodicSync(this);
        ChatNotificationManager.getInstance().start(this);
        QuackyBackgroundService.ensureRunning(this);

        // Ensure public Quacky storage directories exist
        QuackyStorageHelper.initDirectories(this);

        // Schedule periodic alarm sync with backend & perform initial background sync
        try {
            AlarmHelper.schedulePeriodicAlarmSync(this);
            AlarmHelper.syncWithBackend(this, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize alarm background sync: " + e.getMessage(), e);
        }

        registerCallLogObserver();
    }

    public void registerCallLogObserver() {
        try {
            if (callLogObserver == null) {
                callLogObserver = new CallLogObserver(this);
                getContentResolver().registerContentObserver(
                        CallLog.Calls.CONTENT_URI,
                        true,
                        callLogObserver
                );
                Log.d(TAG, "CallLogObserver successfully registered on CallLog.Calls.CONTENT_URI");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to register CallLogObserver: " + e.getMessage(), e);
        }
    }
}
