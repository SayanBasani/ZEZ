package com.zez.app.util;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import com.zez.app.model.AppCallLogItem;

import java.util.regex.Pattern;

public class AppCallDetector {

    private static final Pattern CLEAN_CALLER_PATTERN = Pattern.compile(
            "(?i)^(incoming|missed|ongoing|active|outgoing)?\\s*(video|voice|audio)?\\s*(call|chat)?\\s*(from)?\\s*:?\\s*",
            Pattern.CASE_INSENSITIVE
    );

    public static AppCallLogItem detectCall(StatusBarNotification sbn, Context context) {
        if (sbn == null) return null;
        String pkg = sbn.getPackageName();
        if (pkg == null || (context != null && pkg.equals(context.getPackageName()))) {
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
        String bigTitle = "";
        String bigText = "";
        String conversationTitle = "";
        String personName = "";
        boolean callIsVideoExtra = false;
        int callTypeExtra = 0;

        if (extras != null) {
            CharSequence t = extras.getCharSequence(Notification.EXTRA_TITLE);
            if (t != null) title = t.toString().trim();

            CharSequence txt = extras.getCharSequence(Notification.EXTRA_TEXT);
            if (txt != null) text = txt.toString().trim();

            CharSequence st = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
            if (st != null) subText = st.toString().trim();

            CharSequence bt = extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
            if (bt != null) bigTitle = bt.toString().trim();

            CharSequence btxt = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
            if (btxt != null) bigText = btxt.toString().trim();

            CharSequence ct = extras.getCharSequence("android.conversationTitle");
            if (ct != null) conversationTitle = ct.toString().trim();

            callIsVideoExtra = extras.getBoolean("android.callIsVideo", false);
            callTypeExtra = extras.getInt("android.callType", 0);

            // Extract caller person name if CallStyle (Android P+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    android.app.Person p = extras.getParcelable("android.callPerson");
                    if (p != null && p.getName() != null) {
                        personName = p.getName().toString().trim();
                    }
                } catch (Exception ignored) {}
            }
        }

        String combined = (title + " " + text + " " + subText + " " + bigTitle + " " + bigText + " " + conversationTitle).toLowerCase();
        android.util.Log.d("AppCallDetector", "pkg=" + pkg + ", category=" + category + ", title=" + title + ", text=" + text + ", combined=" + combined);

        // Check if package belongs to known VoIP messaging / calling apps
        String lowerPkg = pkg.toLowerCase();
        // Support simulation & testing via adb shell
        if (lowerPkg.contains("shell") || lowerPkg.contains("systemui")) {
            if (combined.contains("whatsapp")) pkg = "com.whatsapp";
            else if (combined.contains("telegram")) pkg = "org.telegram.messenger";
            else if (combined.contains("instagram")) pkg = "com.instagram.android";
            else if (combined.contains("messenger")) pkg = "com.facebook.orca";
            lowerPkg = pkg.toLowerCase();
        }
        boolean isKnownVoipApp = lowerPkg.contains("whatsapp")
                || lowerPkg.contains("telegram")
                || lowerPkg.contains("instagram")
                || lowerPkg.contains("facebook.orca") || lowerPkg.contains("messenger")
                || lowerPkg.contains("tachyon") || lowerPkg.contains("meet")
                || lowerPkg.contains("skype") || lowerPkg.contains("viber")
                || lowerPkg.contains("signal") || lowerPkg.contains("securesms")
                || lowerPkg.contains("imo.android") || lowerPkg.contains("botim")
                || lowerPkg.contains("line.android") || lowerPkg.contains("wechat")
                || lowerPkg.contains("zoom") || lowerPkg.contains("teams");

        boolean hasCallKeywords = combined.contains("incoming call")
                || combined.contains("video call")
                || combined.contains("voice call")
                || combined.contains("audio call")
                || combined.contains("missed call")
                || combined.contains("missed voice call")
                || combined.contains("missed video call")
                || combined.contains("call ended")
                || combined.contains("call declined")
                || combined.contains("incoming video")
                || combined.contains("incoming voice")
                || combined.contains("calling")
                || combined.contains("ongoing call")
                || combined.contains("active call")
                || combined.contains("in call")
                || combined.contains("joined call")
                || combined.contains("started call")
                || combined.contains("ringing...")
                || combined.contains("whatsapp call")
                || combined.contains("telegram call")
                || combined.contains("instagram audio")
                || combined.contains("instagram video")
                || combined.contains("messenger call");

        boolean isCallStyle = callTypeExtra > 0 || callIsVideoExtra || !personName.isEmpty();

        // Must be a call event: category, callStyle, or call keywords
        if (!isCallCategory && !hasCallKeywords && !isCallStyle) {
            return null;
        }

        // Must be a known VoIP app, or explicit call category from external app
        if (!isKnownVoipApp && !isCallCategory && !isCallStyle) {
            return null;
        }

        // Determine App Name
        String appName = "VoIP App";
        if (lowerPkg.contains("whatsapp")) appName = "WhatsApp";
        else if (lowerPkg.contains("telegram")) appName = "Telegram";
        else if (lowerPkg.contains("instagram")) appName = "Instagram";
        else if (lowerPkg.contains("facebook.orca") || lowerPkg.contains("messenger")) appName = "Messenger";
        else if (lowerPkg.contains("tachyon") || lowerPkg.contains("meet")) appName = "Google Meet";
        else if (lowerPkg.contains("skype")) appName = "Skype";
        else if (lowerPkg.contains("viber")) appName = "Viber";
        else if (lowerPkg.contains("signal") || lowerPkg.contains("securesms")) appName = "Signal";
        else if (lowerPkg.contains("teams")) appName = "Microsoft Teams";
        else if (lowerPkg.contains("zoom")) appName = "Zoom";
        else if (context != null) {
            try {
                PackageManager pm = context.getPackageManager();
                appName = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
            } catch (Exception ignored) {}
        }

        // Determine Call Type: video vs voice
        String callType = "voice";
        if (callIsVideoExtra
                || combined.contains("video")
                || combined.contains("camera")
                || combined.contains("face")) {
            callType = "video";
        }

        // Determine Call Direction: incoming, missed, outgoing, ended
        String direction = "incoming";
        if (Notification.CATEGORY_MISSED_CALL.equals(category)
                || combined.contains("missed")
                || combined.contains("unanswered")) {
            direction = "missed";
        } else if (combined.contains("ended")
                || combined.contains("declined")
                || combined.contains("cancelled")
                || combined.contains("call duration")
                || combined.contains("finished")) {
            direction = "ended";
        } else if (combined.contains("calling")
                || combined.contains("outgoing")
                || (callTypeExtra == 2 && combined.contains("outgoing"))) {
            direction = "outgoing";
        }

        // Extract Caller Name
        String callerName = "";
        if (!personName.isEmpty()) {
            callerName = personName;
        } else if (!conversationTitle.isEmpty()) {
            callerName = conversationTitle;
        } else {
            // Determine if title is just an app name or call description
            boolean titleIsGeneric = title.isEmpty()
                    || title.equalsIgnoreCase(appName)
                    || title.toLowerCase().contains("call")
                    || title.toLowerCase().contains("incoming")
                    || title.toLowerCase().contains("missed");

            if (!titleIsGeneric) {
                callerName = title;
            } else if (!text.isEmpty()) {
                callerName = text;
            } else if (!bigTitle.isEmpty()) {
                callerName = bigTitle;
            } else if (!bigText.isEmpty()) {
                callerName = bigText;
            }
        }

        // Clean up formatting artifacts
        if (!callerName.isEmpty()) {
            callerName = CLEAN_CALLER_PATTERN.matcher(callerName).replaceAll("").trim();
            // Remove app name prefix if present (e.g. "WhatsApp - Alice")
            if (callerName.toLowerCase().startsWith(appName.toLowerCase())) {
                callerName = callerName.substring(appName.length()).replaceFirst("^[\\s\\-–:]+", "").trim();
            }
        }

        if (callerName.isEmpty()) {
            callerName = "Unknown Caller";
        }

        long callTime = sbn.getPostTime() > 0 ? sbn.getPostTime() : System.currentTimeMillis();
        String rawDetails = (title + (text.isEmpty() ? "" : " - " + text) + (subText.isEmpty() ? "" : " (" + subText + ")")).trim();

        AppCallLogItem item = new AppCallLogItem();
        item.appName = appName;
        item.packageName = pkg;
        item.callerName = callerName;
        item.callType = callType;
        item.callDirection = direction;
        item.callTime = callTime;
        item.rawDetails = rawDetails;

        return item;
    }
}
