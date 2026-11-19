package com.zez.app;

import android.app.Application;
import android.provider.CallLog;
import android.util.Log;

import com.zez.app.observer.CallLogObserver;
import com.zez.app.receiver.BootReceiver;
import com.zez.app.util.ChatNotificationHelper;
import com.zez.app.util.ChatNotificationManager;

public class ZezApplication extends Application {
    private static final String TAG = "ZezApplication";
    private CallLogObserver callLogObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Zez Application initialized.");

        // Apply saved theme (System Default / Light / Dark)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                com.zez.app.util.PreferencesHelper.getThemeMode(this)
        );

        // Pre-register notification channel & start persistent background polling
        ChatNotificationHelper.createNotificationChannel(this);
        BootReceiver.schedulePeriodicSync(this);
        ChatNotificationManager.getInstance().start(this);

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
