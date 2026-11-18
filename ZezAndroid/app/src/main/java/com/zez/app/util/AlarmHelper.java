package com.zez.app.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zez.app.model.AlarmItem;
import com.zez.app.receiver.AlarmReceiver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class AlarmHelper {
    private static final String TAG = "AlarmHelper";
    private static final String PREF_ALARMS = "zez_alarms_pref";
    private static final String KEY_ALARM_LIST = "alarm_list_json";
    private static final Gson gson = new Gson();

    public static List<AlarmItem> getAlarms(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_ALARMS, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_ALARM_LIST, null);
        if (json == null || json.trim().isEmpty()) {
            List<AlarmItem> defaults = createDefaultAlarms(context);
            saveAlarms(context, defaults);
            return defaults;
        }

        try {
            Type type = new TypeToken<List<AlarmItem>>() {}.getType();
            List<AlarmItem> list = gson.fromJson(json, type);
            if (list == null) list = new ArrayList<>();
            return list;
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse alarms json", e);
            return new ArrayList<>();
        }
    }

    private static List<AlarmItem> createDefaultAlarms(Context context) {
        List<AlarmItem> list = new ArrayList<>();
        String defRingtoneUri = getDefaultAlarmRingtoneUri(context);
        String defRingtoneTitle = getRingtoneTitle(context, defRingtoneUri);

        AlarmItem a1 = new AlarmItem(
                System.currentTimeMillis(),
                7, 0,
                "Morning Alarm",
                true,
                true,
                defRingtoneUri,
                defRingtoneTitle,
                new boolean[]{false, true, true, true, true, true, false}
        );
        list.add(a1);
        return list;
    }

    public static void saveAlarms(Context context, List<AlarmItem> alarms) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_ALARMS, Context.MODE_PRIVATE);
        String json = gson.toJson(alarms);
        prefs.edit().putString(KEY_ALARM_LIST, json).apply();
    }

    public static void addOrUpdateAlarm(Context context, AlarmItem item) {
        List<AlarmItem> list = getAlarms(context);
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == item.id) {
                list.set(i, item);
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(item);
        }
        saveAlarms(context, list);

        if (item.isEnabled) {
            scheduleAlarm(context, item);
        } else {
            cancelAlarm(context, item);
        }
    }

    public static void deleteAlarm(Context context, long alarmId) {
        List<AlarmItem> list = getAlarms(context);
        AlarmItem target = null;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == alarmId) {
                target = list.remove(i);
                break;
            }
        }
        saveAlarms(context, list);
        if (target != null) {
            cancelAlarm(context, target);
        }
    }

    public static void setAlarmEnabled(Context context, long alarmId, boolean enabled) {
        List<AlarmItem> list = getAlarms(context);
        for (AlarmItem item : list) {
            if (item.id == alarmId) {
                item.isEnabled = enabled;
                if (enabled) {
                    scheduleAlarm(context, item);
                } else {
                    cancelAlarm(context, item);
                }
                break;
            }
        }
        saveAlarms(context, list);
    }

    public static void scheduleAlarm(Context context, AlarmItem item) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        long triggerTime = calculateNextTriggerTime(item);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_FIRE_ALARM);
        intent.putExtra(AlarmReceiver.EXTRA_ALARM_ID, item.id);
        intent.putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, item.label);
        intent.putExtra(AlarmReceiver.EXTRA_RINGTONE_URI, item.ringtoneUri);
        intent.putExtra(AlarmReceiver.EXTRA_VIBRATE, item.isVibrate);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                (int) (item.id % Integer.MAX_VALUE),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            }
            Log.d(TAG, "Scheduled alarm ID: " + item.id + " for timestamp: " + triggerTime);
        } catch (SecurityException se) {
            Log.w(TAG, "Exact alarm permission missing, fallback to inexact: " + se.getMessage());
            am.set(AlarmManager.RTC_WAKEUP, triggerTime, pi);
        }
    }

    public static void cancelAlarm(Context context, AlarmItem item) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_FIRE_ALARM);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                (int) (item.id % Integer.MAX_VALUE),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        am.cancel(pi);
        Log.d(TAG, "Cancelled alarm ID: " + item.id);
    }

    public static void rescheduleAllAlarms(Context context) {
        List<AlarmItem> list = getAlarms(context);
        for (AlarmItem item : list) {
            if (item.isEnabled) {
                scheduleAlarm(context, item);
            }
        }
    }

    public static long calculateNextTriggerTime(AlarmItem item) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, item.hour);
        target.set(Calendar.MINUTE, item.minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        boolean hasRepeatingDays = false;
        if (item.daysOfWeek != null) {
            for (boolean d : item.daysOfWeek) {
                if (d) { hasRepeatingDays = true; break; }
            }
        }

        if (!hasRepeatingDays) {
            // Once alarm: if today's time has passed, fire tomorrow
            if (target.before(now) || target.equals(now)) {
                target.add(Calendar.DAY_OF_YEAR, 1);
            }
            return target.getTimeInMillis();
        }

        // Repeating on specific days
        // Calendar.SUNDAY = 1, MONDAY = 2, ..., SATURDAY = 7
        for (int i = 0; i < 7; i++) {
            int dayOfWeekCalendar = target.get(Calendar.DAY_OF_WEEK);
            int dayIndex = dayOfWeekCalendar - 1; // 0=Sun .. 6=Sat

            if (item.daysOfWeek[dayIndex] && target.after(now)) {
                return target.getTimeInMillis();
            }
            target.add(Calendar.DAY_OF_YEAR, 1);
        }

        return target.getTimeInMillis();
    }

    public static String getDefaultAlarmRingtoneUri(Context context) {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        if (alert == null) {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }
        return alert != null ? alert.toString() : "";
    }

    public static String getRingtoneTitle(Context context, String uriStr) {
        if (uriStr == null || uriStr.trim().isEmpty()) {
            return "Default Alarm Ringtone";
        }
        try {
            Uri uri = Uri.parse(uriStr);
            Ringtone r = RingtoneManager.getRingtone(context, uri);
            if (r != null) {
                String title = r.getTitle(context);
                if (title != null && !title.isEmpty()) {
                    return title;
                }
            }
        } catch (Exception ignored) {}
        return "System Ringtone";
    }
}
