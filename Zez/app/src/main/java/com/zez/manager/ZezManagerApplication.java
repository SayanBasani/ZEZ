package com.zez.manager;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.zez.manager.observer.CallLogObserver;
import com.zez.manager.receiver.BootReceiver;
import com.zez.manager.service.NotificationCaptureService;
import com.zez.manager.util.CallLogHelper;
import com.zez.manager.util.PreferencesHelper;

public class ZezManagerApplication extends Application {
    private static final String TAG = "ZezManagerApp";
    private CallLogObserver callLogObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        com.zez.manager.theme.ThemeManager.init(this);
        new Thread(() -> BootReceiver.schedulePeriodicSync(this)).start();
        new Thread(() -> {
            com.zez.manager.alarm.util.AlarmHelper.rescheduleAllAlarms(this);
            com.zez.manager.alarm.util.AlarmHelper.schedulePeriodicAlarmSync(this);
        }).start();
        registerCallLogObserverIfPermitted();
        NotificationCaptureService.ensureServiceBound(this);
    }

    public synchronized void registerCallLogObserverIfPermitted() {
        if (callLogObserver != null) return;
        if (!CallLogHelper.hasCallLogPermission(this)) return;
        try {
            callLogObserver = new CallLogObserver(new Handler(Looper.getMainLooper()), this);
            getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogObserver);
            Log.d(TAG, "Registered CallLogObserver");
        } catch (Exception e) {
            Log.w(TAG, "Failed registering CallLogObserver: " + e.getMessage());
        }
    }
}
