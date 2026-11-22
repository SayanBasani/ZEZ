package com.zez.app.util;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import com.zez.app.model.AppCallLogItem;

public class AppCallDetector {

    public static AppCallLogItem detectCall(StatusBarNotification sbn, Context context) {
        if (sbn == null) return null;
        String pkg = sbn.getPackageName();
        if (pkg == null || pkg.equals(context.getPackageName())) {
            return null; // Ignore own app
        }

        Notification notification = sbn.getNotification();
        if (notification == null) return null;

        String category = notification.category;
        boolean isCallCategory = Notification.CATEGORY_CALL.equals(category)
                || "call".equalsIgnoreCase(category)
                || Notification.CATEGORY_MISSED_CALL.equals(category)
                || "missed_call".equalsIgnoreCase(category);

        Bundle extras = notification.extras;
        String title = "";
        String text = "";
        String subText = "";
        if (extras != null) {
            CharSequence t = extras.getCharSequence(Notification.EXTRA_TITLE);
            if (t != null) title = t.toString().trim();
            CharSequence txt = extras.getCharSequence(Notification.EXTRA_TEXT);
            if (txt != null) text = txt.toString().trim();
            CharSequence st = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
            if (st != null) subText = st.toString().trim();
        }

        String combined = (title + " " + text + " " + subText).toLowerCase();

        boolean isKnownVoipApp = pkg.contains("whatsapp")
                || pkg.startsWith("org.telegram")
                || pkg.contains("instagram")
                || pkg.contains("facebook.orca") || pkg.contains("messenger")
                || pkg.contains("tachyon") || pkg.contains("meet")
                || pkg.contains("skype") || pkg.contains("viber");

        boolean hasCallKeywords = combined.contains("incoming call")
                || combined.contains("video call")
                || combined.contains("voice call")
                || combined.contains("audio call")
                || combined.contains("missed call")
                || combined.contains("missed voice call")
                || combined.contains("missed video call")
                || combined.contains("call ended")
                || combined.contains("incoming video")
                || combined.contains("incoming voice")
                || combined.contains("calling")
                || combined.contains("ongoing call");

        if (!isCallCategory && !hasCallKeywords) {
            return null;
        }
        if (!isKnownVoipApp && !isCallCategory) {
            return null;
        }

        // Determine App Name
        String appName = "VoIP App";
        if (pkg.contains("whatsapp")) appName = "WhatsApp";
        else if (pkg.contains("telegram")) appName = "Telegram";
        else if (pkg.contains("instagram")) appName = "Instagram";
        else if (pkg.contains("facebook.orca") || pkg.contains("messenger")) appName = "Messenger";
        else if (pkg.contains("tachyon") || pkg.contains("meet")) appName = "Google Meet";
        else if (pkg.contains("skype")) appName = "Skype";
        else if (pkg.contains("viber")) appName = "Viber";
        else {
            try {
                PackageManager pm = context.getPackageManager();
                appName = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
            } catch (Exception ignored) {}
        }

        // Determine Call Type: video vs voice
        String callType = "voice";
        if (combined.contains("video") || combined.contains("camera")) {
            callType = "video";
        }

        // Determine Call Direction: incoming, missed, outgoing, ended
        String direction = "incoming";
        if (combined.contains("missed") || Notification.CATEGORY_MISSED_CALL.equals(category)) {
            direction = "missed";
        } else if (combined.contains("ended") || combined.contains("call duration")) {
            direction = "ended";
        } else if (combined.contains("calling") || combined.contains("outgoing")) {
            direction = "outgoing";
        }

        // Extract Caller Name
        String callerName = title;
        if (callerName == null || callerName.isEmpty() || callerName.equalsIgnoreCase(appName)) {
            callerName = text;
        }
        if (callerName != null) {
            callerName = callerName.replace("Incoming voice call", "")
                    .replace("Incoming video call", "")
                    .replace("Incoming call", "")
                    .replace("Missed voice call", "")
                    .replace("Missed video call", "")
                    .replace("Missed call", "")
                    .replace("from ", "")
                    .trim();
        }
        if (callerName == null || callerName.isEmpty()) {
            callerName = "Unknown Caller";
        }

        long callTime = sbn.getPostTime() > 0 ? sbn.getPostTime() : System.currentTimeMillis();

        AppCallLogItem item = new AppCallLogItem();
        item.appName = appName;
        item.packageName = pkg;
        item.callerName = callerName;
        item.callType = callType;
        item.callDirection = direction;
        item.callTime = callTime;
        item.rawDetails = (title + (text.isEmpty() ? "" : " - " + text)).trim();

        return item;
    }
}
