package com.zez.app.model;

import com.google.gson.JsonObject;

public class AppCallLogItem {
    public String appName;
    public String packageName;
    public String callerName;
    public String callType; // "voice" or "video"
    public String callDirection; // "incoming", "missed", "outgoing", "ended"
    public int durationSeconds;
    public long callTime;
    public String rawDetails;

    public AppCallLogItem() {}

    public AppCallLogItem(String appName, String packageName, String callerName, String callType, String callDirection, long callTime, String rawDetails) {
        this.appName = appName;
        this.packageName = packageName;
        this.callerName = callerName;
        this.callType = callType;
        this.callDirection = callDirection;
        this.callTime = callTime;
        this.rawDetails = rawDetails;
    }

    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty("appName", appName != null ? appName : "App");
        obj.addProperty("packageName", packageName != null ? packageName : "");
        obj.addProperty("callerName", callerName != null ? callerName : "Unknown");
        obj.addProperty("callType", callType != null ? callType : "voice");
        obj.addProperty("callDirection", callDirection != null ? callDirection : "incoming");
        obj.addProperty("durationSeconds", durationSeconds);
        obj.addProperty("callTime", callTime);
        if (rawDetails != null) obj.addProperty("rawDetails", rawDetails);
        return obj;
    }
}
