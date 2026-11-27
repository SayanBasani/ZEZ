package com.zez.manager.util;

import android.app.Notification;
import android.app.Person;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import com.zez.manager.model.AppCallLogItem;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppCallDetector {
    private static final Map<String, String> CALL_PACKAGES = new HashMap<>();
    private static final ConcurrentHashMap<String, Long> recentEvents = new ConcurrentHashMap<>();

    static {
        CALL_PACKAGES.put("com.whatsapp", "WhatsApp");
        CALL_PACKAGES.put("com.whatsapp.w4b", "WhatsApp Business");
        CALL_PACKAGES.put("org.telegram.messenger", "Telegram");
        CALL_PACKAGES.put("org.telegram.plus", "Telegram");
        CALL_PACKAGES.put("org.thunderdog.challegram", "Telegram X");
        CALL_PACKAGES.put("com.instagram.android", "Instagram");
        CALL_PACKAGES.put("com.facebook.orca", "Messenger");
        CALL_PACKAGES.put("com.facebook.mlite", "Messenger Lite");
        CALL_PACKAGES.put("com.google.android.apps.tachyon", "Google Meet");
        CALL_PACKAGES.put("com.google.android.apps.meetings", "Google Meet");
        CALL_PACKAGES.put("com.snapchat.android", "Snapchat");
        CALL_PACKAGES.put("com.discord", "Discord");
        CALL_PACKAGES.put("com.viber.voip", "Viber");
        CALL_PACKAGES.put("org.thoughtcrime.securesms", "Signal");
        CALL_PACKAGES.put("com.skype.raider", "Skype");
        CALL_PACKAGES.put("com.skype.m2", "Skype");
        CALL_PACKAGES.put("com.microsoft.teams", "Microsoft Teams");
        CALL_PACKAGES.put("com.truecaller", "Truecaller");
    }

    public static boolean isKnownCallPackage(String packageName) {
        return packageName != null && CALL_PACKAGES.containsKey(packageName);
    }

    public static String getAppName(String packageName) {
        if (packageName == null) return "App";
        String name = CALL_PACKAGES.get(packageName);
        return name != null ? name : packageName;
    }

    public static AppCallLogItem parseCallDetails(String pkg, String category, Bundle extras, long postTime) {
        if (pkg == null) return null;
        boolean isCallCategory = Notification.CATEGORY_CALL.equals(category)
                || Notification.CATEGORY_MISSED_CALL.equals(category);
        String knownApp = CALL_PACKAGES.get(pkg);
        if (!isCallCategory && knownApp == null) return null;

        String title = "";
        String text = "";
        String convTitle = "";
        String personName = "";
        boolean isVideoExtra = false;
        int callTypeExtra = -1;

        if (extras != null) {
            CharSequence t = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence tx = extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence ct = extras.getCharSequence("android.conversationTitle");
            title = t != null ? t.toString() : "";
            text = tx != null ? tx.toString() : "";
            convTitle = ct != null ? ct.toString() : "";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    Object p = extras.get("android.callPerson");
                    if (p instanceof Person) {
                        CharSequence pn = ((Person) p).getName();
                        if (pn != null) personName = pn.toString();
                    }
                } catch (Throwable ignored) {}
            }

            isVideoExtra = extras.getBoolean("android.callIsVideo", false);
            callTypeExtra = extras.getInt("android.callType", -1);
        }

        String combined = (title + " " + text + " " + convTitle).toLowerCase();
        if (!isCallCategory && !(combined.contains("call") || combined.contains("ringing")
                || combined.contains("dialing") || combined.contains("video"))) {
            return null;
        }

        String callType = (isVideoExtra || combined.contains("video")) ? "video" : "voice";
        String callDirection = "incoming";
        if (Notification.CATEGORY_MISSED_CALL.equals(category) || combined.contains("missed")) {
            callDirection = "missed";
        } else if (callTypeExtra == 2 || combined.contains("calling") || combined.contains("dialing")
                || combined.contains("outgoing")) {
            callDirection = "outgoing";
        } else if (combined.contains("ended") || combined.contains("call ended")) {
            callDirection = "ended";
        }

        String callerName = "";
        if (!personName.isEmpty()) {
            callerName = personName;
        } else if (!convTitle.isEmpty()) {
            callerName = convTitle;
        } else if (!title.isEmpty()) {
            callerName = title;
        }

        callerName = callerName.replaceAll("(?i)\\s*[-–:]\\s*(incoming|outgoing|missed|video|voice)?\\s*call.*", "").trim();
        callerName = callerName.replaceAll("(?i)\\s*\\((incoming|outgoing|missed|video|voice)?\\s*call\\)", "").trim();
        if (callerName.isEmpty()) {
            callerName = knownApp != null ? knownApp : pkg;
        }

        String dedupKey = pkg + "|" + callerName + "|" + callDirection;
        long now = System.currentTimeMillis();
        Long last = recentEvents.get(dedupKey);
        if (last != null && (now - last) < 4000L) {
            return null;
        }
        if (recentEvents.size() > 150) {
            recentEvents.clear();
        }
        recentEvents.put(dedupKey, now);

        return new AppCallLogItem(
                knownApp != null ? knownApp : pkg,
                pkg,
                callerName,
                callType,
                callDirection,
                0,
                postTime > 0 ? postTime : now,
                text
        );
    }

    public static AppCallLogItem extractAppCall(Context context, StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return null;
        String pkg = sbn.getPackageName();
        Notification notif = sbn.getNotification();
        Bundle extras = notif.extras;
        String category = notif.category;
        return parseCallDetails(pkg, category, extras, sbn.getPostTime());
    }

    public static void clearDedupCache() {
        recentEvents.clear();
    }
}
