package com.zez.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.zez.app.model.AlarmItem;
import com.zez.app.service.AlarmRingingService;
import com.zez.app.util.AlarmHelper;

import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    public static final String ACTION_FIRE_ALARM = "com.zez.app.ACTION_FIRE_ALARM";
    public static final String ACTION_DISMISS_ALARM = "com.zez.app.ACTION_DISMISS_ALARM";
    public static final String ACTION_SNOOZE_ALARM = "com.zez.app.ACTION_SNOOZE_ALARM";
    public static final String ACTION_SYNC_ALARMS = "com.zez.app.ACTION_SYNC_ALARMS";

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
            // Reschedule all active alarms after reboot or time change
            AlarmHelper.rescheduleAllAlarms(context);
            AlarmHelper.schedulePeriodicAlarmSync(context);
            final PendingResult pr = goAsync();
            new Thread(() -> {
                try {
                    AlarmHelper.syncWithBackend(context.getApplicationContext(), null);
                    com.zez.app.util.ChatNotificationManager.getInstance().pollSync(context.getApplicationContext());
                } catch (Exception ignored) {
                } finally {
                    pr.finish();
                }
            }).start();
            return;
        }

        if (ACTION_SYNC_ALARMS.equals(action)) {
            Log.d(TAG, "ACTION_SYNC_ALARMS received: syncing alarms and chat notifications with backend in background");
            final PendingResult pr = goAsync();
            new Thread(() -> {
                try {
                    AlarmHelper.syncWithBackend(context.getApplicationContext(), null);
                    com.zez.app.util.ChatNotificationManager.getInstance().pollSync(context.getApplicationContext());
                } catch (Exception e) {
                    Log.e(TAG, "Error in background alarm/chat sync: " + e.getMessage());
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

            // Start foreground ringing service
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

            // Jit Mode: Schedule relentless repeat in 1 minute so user is repeatedly alerted until dismissed
            if (isJitMode) {
                Log.d(TAG, "Jit Mode is active: scheduling next relentless ring in 60 seconds");
                AlarmHelper.scheduleJitRepeat(context, alarmId, label, ringtoneUri, vibrate);
            }

            // If non-repeating alarm and NOT jit mode, disable it in list; if repeating, reschedule for next day
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
                        // Reschedule for next occurrence
                        AlarmHelper.scheduleAlarm(context, a);
                    }
                    break;
                }
            }
        } else if (ACTION_DISMISS_ALARM.equals(action)) {
            long alarmId = intent.getLongExtra(EXTRA_ALARM_ID, 0);
            if (alarmId > 0) {
                AlarmHelper.cancelJitRepeat(context, alarmId);
            }
            Intent stopIntent = new Intent(context, AlarmRingingService.class);
            context.stopService(stopIntent);
        } else if (ACTION_SNOOZE_ALARM.equals(action)) {
            Intent stopIntent = new Intent(context, AlarmRingingService.class);
            context.stopService(stopIntent);

            long alarmId = intent.getLongExtra(EXTRA_ALARM_ID, 0);
            boolean isJitMode = intent.getBooleanExtra(EXTRA_IS_JIT_MODE, false);

            if (isJitMode) {
                // In Jit Mode, snooze is aggressive: 1 minute only!
                String label = intent.getStringExtra(EXTRA_ALARM_LABEL);
                String ringtoneUri = intent.getStringExtra(EXTRA_RINGTONE_URI);
                boolean vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true);
                AlarmHelper.scheduleJitRepeat(context, alarmId, label, ringtoneUri, vibrate);
            } else {
                // Standard 10 minutes snooze
                AlarmItem temp = new AlarmItem();
                temp.id = alarmId > 0 ? alarmId : System.currentTimeMillis();
                temp.label = "Snoozed: " + intent.getStringExtra(EXTRA_ALARM_LABEL);
                temp.ringtoneUri = intent.getStringExtra(EXTRA_RINGTONE_URI);
                temp.isVibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true);
                temp.isEnabled = true;
                temp.daysOfWeek = null; // one-time

                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MINUTE, 10);
                temp.hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
                temp.minute = cal.get(java.util.Calendar.MINUTE);

                AlarmHelper.scheduleAlarm(context, temp);
            }
        }
    }
}
