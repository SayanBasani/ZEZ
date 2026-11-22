package com.zez.app.model;

import java.io.Serializable;
import java.util.Locale;

public class AlarmItem implements Serializable {
    public long id;
    public int hour;       // 0 - 23
    public int minute;     // 0 - 59
    public String label;
    public boolean isEnabled;
    public boolean isVibrate;
    public boolean isJitMode; // 'Jit' mode: persistent, relentless alarm that rings repeatedly
    public String ringtoneUri;
    public String ringtoneTitle;
    public boolean[] daysOfWeek; // length 7: Sun, Mon, Tue, Wed, Thu, Fri, Sat

    public AlarmItem() {
        this.id = System.currentTimeMillis();
        this.hour = 7;
        this.minute = 0;
        this.label = "Alarm";
        this.isEnabled = true;
        this.isVibrate = true;
        this.isJitMode = false;
        this.ringtoneUri = "";
        this.ringtoneTitle = "Default Alarm Ringtone";
        this.daysOfWeek = new boolean[]{false, true, true, true, true, true, false}; // Mon-Fri default
    }

    public AlarmItem(long id, int hour, int minute, String label, boolean isEnabled, boolean isVibrate, String ringtoneUri, String ringtoneTitle, boolean[] daysOfWeek) {
        this(id, hour, minute, label, isEnabled, isVibrate, false, ringtoneUri, ringtoneTitle, daysOfWeek);
    }

    public AlarmItem(long id, int hour, int minute, String label, boolean isEnabled, boolean isVibrate, boolean isJitMode, String ringtoneUri, String ringtoneTitle, boolean[] daysOfWeek) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.label = label;
        this.isEnabled = isEnabled;
        this.isVibrate = isVibrate;
        this.isJitMode = isJitMode;
        this.ringtoneUri = ringtoneUri;
        this.ringtoneTitle = ringtoneTitle;
        this.daysOfWeek = daysOfWeek != null ? daysOfWeek : new boolean[7];
    }

    public String getFormattedTime12() {
        int h = hour % 12;
        if (h == 0) h = 12;
        return String.format(Locale.US, "%02d:%02d", h, minute);
    }

    public String getAmPm() {
        return hour >= 12 ? "PM" : "AM";
    }

    public String getDaysDisplay() {
        if (daysOfWeek == null) return "Once";
        boolean all = true;
        boolean none = true;
        boolean weekdays = true;
        boolean weekends = true;

        for (int i = 0; i < 7; i++) {
            if (daysOfWeek[i]) none = false;
            else all = false;
        }

        if (none) return "Once";
        if (all) return "Every day";

        // Mon-Fri: indices 1,2,3,4,5
        // Sun: 0, Sat: 6
        if (!daysOfWeek[0] && daysOfWeek[1] && daysOfWeek[2] && daysOfWeek[3] && daysOfWeek[4] && daysOfWeek[5] && !daysOfWeek[6]) {
            return "Mon - Fri";
        }
        if (daysOfWeek[0] && !daysOfWeek[1] && !daysOfWeek[2] && !daysOfWeek[3] && !daysOfWeek[4] && !daysOfWeek[5] && daysOfWeek[6]) {
            return "Weekends";
        }

        String[] shortNames = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (daysOfWeek[i]) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(shortNames[i]);
            }
        }
        return sb.toString();
    }
}
