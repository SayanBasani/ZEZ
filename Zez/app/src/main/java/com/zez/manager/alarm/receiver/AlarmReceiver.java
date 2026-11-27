package com.zez.manager.alarm.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.zez.manager.alarm.model.AlarmItem;
import com.zez.manager.alarm.service.AlarmRingingService;
import com.zez.manager.alarm.util.AlarmHelper;

import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    public static final String ACTION_FIRE_ALARM = "com.zez.manager.ACTION_FIRE_ALARM";
    public static final String ACTION_DISMISS_ALARM = "com.zez.manager.ACTION_DISMISS_ALARM";
    public static final String ACTION_SNOOZE_ALARM = "com.zez.manager.ACTION_SNOOZE_ALARM";
    public static final String ACTION_SYNC_ALARMS = "com.zez.manager.ACTION_SYNC_ALARMS";

    public static final String EXTRA_ALARM_ID = "extra_alarm_id";
    public static final String EXTRA_ALARM_LABEL = "extra_alarm_label";
    public static final String EXTRA_RINGTONE_URI = "extra_ringtone_uri";
    public static final String EXTRA_VIBRATE = "extra_vibrate";
    public static final String EXTRA_IS_JIT_MODE = "extra_is_jit_mode";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        Log.d(TAG, "AlarmReceiver onReceive action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(action) ||
                Intent.ACTION_TIME_CHANGED.equals(action) ||
                Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            AlarmHelper.rescheduleAllAlarms(context);
            AlarmHelper.schedulePeriodicAlarmSync(context);
            final PendingResult pr = goAsync();
            new Thread(() -> {
                try {
                    AlarmHelper.syncWithBackend(context.getApplicationContext(), null);
                } catch (Exception ignored) {
                } finally {
                    pr.finish();
                }
            }).start();
            return;
        }

        if (ACTION_SYNC_ALARMS.equals(action)) {
            Log.d(TAG, "ACTION_SYNC_ALARMS received: syncing alarms with backend in background");
            final PendingResult pr = goAsync();
            new Thread(() -> {
                try {
                    AlarmHelper.syncWithBackend(context.getApplicationContext(), null);
                } catch (Exception e) {
                    Log.e(TAG, "Error in background alarm sync: " + e.getMessage());
                } finally {
                    AlarmHelper.schedulePeriodicAlarmSync(context.getApplicationContext());
                    pr.finish();
                }
            }).start();
            return;
        }

        if (ACTION_FIRE_ALARM.equals(action)) {
            long alarmId = intent.getLongExtra(EXTRA_ALARM_ID, 0);
            String label = intent.getStringExtra(EXTRA_ALARM_LABEL);
            String ringtoneUri = intent.getStringExtra(EXTRA_RINGTONE_URI);
            boolean vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true);
            boolean isJitMode = intent.getBooleanExtra(EXTRA_IS_JIT_MODE, false);

            Intent serviceIntent = new Intent(context, AlarmRingingService.class);
            serviceIntent.putExtra(EXTRA_ALARM_ID, alarmId);
            serviceIntent.putExtra(EXTRA_ALARM_LABEL, label);
            serviceIntent.putExtra(EXTRA_RINGTONE_URI, ringtoneUri);
            serviceIntent.putExtra(EXTRA_VIBRATE, vibrate);
            serviceIntent.putExtra(EXTRA_IS_JIT_MODE, isJitMode);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }

            if (isJitMode) {
                Log.d(TAG, "Jit Mode is active: scheduling next relentless ring in 60 seconds");
                AlarmHelper.scheduleJitRepeat(context, alarmId, label, ringtoneUri, vibrate);
            }

            // Reschedule repeating alarm or disable one-shot
            List<AlarmItem> alarms = AlarmHelper.getAlarms(context);
            for (AlarmItem a : alarms) {
                if (a.id == alarmId) {
                    boolean hasRepeatingDays = false;
                    if (a.daysOfWeek != null) {
                        for (boolean d : a.daysOfWeek) {
                            if (d) { hasRepeatingDays = true; break; }
                        }
                    }
                    if (!hasRepeatingDays && !isJitMode) {
                        a.isEnabled = false;
                        AlarmHelper.saveAlarms(context, alarms);
                    } else if (hasRepeatingDays) {
                        AlarmHelper.scheduleAlarm(context, a);
                    }
                    break;
                }
            }
            return;
        }

        if (ACTION_DISMISS_ALARM.equals(action)) {
            long alarmId = intent.getLongExtra(EXTRA_ALARM_ID, 0);
            Log.d(TAG, "ACTION_DISMISS_ALARM for alarm: " + alarmId);
            AlarmHelper.cancelJitRepeat(context, alarmId);
            Intent stopService = new Intent(context, AlarmRingingService.class);
            context.stopService(stopService);
            return;
        }

        if (ACTION_SNOOZE_ALARM.equals(action)) {
            long alarmId = intent.getLongExtra(EXTRA_ALARM_ID, 0);
            String label = intent.getStringExtra(EXTRA_ALARM_LABEL);
            String ringtoneUri = intent.getStringExtra(EXTRA_RINGTONE_URI);
            boolean vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true);
            boolean isJitMode = intent.getBooleanExtra(EXTRA_IS_JIT_MODE, false);

            Intent stopService = new Intent(context, AlarmRingingService.class);
            context.stopService(stopService);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                long snoozeDurationMs = isJitMode ? (60 * 1000L) : (5 * 60 * 1000L); // 1 min for Jit, 5 min normal
                long snoozeTime = System.currentTimeMillis() + snoozeDurationMs;

                Intent fireIntent = new Intent(context, AlarmReceiver.class);
                fireIntent.setAction(ACTION_FIRE_ALARM);
                fireIntent.putExtra(EXTRA_ALARM_ID, alarmId);
                fireIntent.putExtra(EXTRA_ALARM_LABEL, label);
                fireIntent.putExtra(EXTRA_RINGTONE_URI, ringtoneUri);
                fireIntent.putExtra(EXTRA_VIBRATE, vibrate);
                fireIntent.putExtra(EXTRA_IS_JIT_MODE, isJitMode);

                PendingIntent pi = PendingIntent.getBroadcast(
                        context,
                        (int) (alarmId % Integer.MAX_VALUE),
                        fireIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pi);
                    } else {
                        am.setExact(AlarmManager.RTC_WAKEUP, snoozeTime, pi);
                    }
                } catch (SecurityException se) {
                    am.set(AlarmManager.RTC_WAKEUP, snoozeTime, pi);
                }
            }
        }
    }
}
