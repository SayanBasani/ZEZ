package com.zez.manager.model;

import com.google.gson.JsonObject;

public class AppCallLogItem {
    public String appName;
    public String packageName;
    public String callerName;
    public String callType;
    public String callDirection;
    public int durationSeconds;
    public long callTime;
    public String rawDetails;

    public AppCallLogItem(String appName, String packageName, String callerName,
                          String callType, String callDirection, int durationSeconds,
                          long callTime, String rawDetails) {
        this.appName = appName;
        this.packageName = packageName;
        this.callerName = callerName;
        this.callType = callType;
        this.callDirection = callDirection;
        this.durationSeconds = durationSeconds;
        this.callTime = callTime;
        this.rawDetails = rawDetails;
    }

    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty("appName", appName);
        obj.addProperty("packageName", packageName);
        obj.addProperty("callerName", callerName);
        obj.addProperty("callType", callType);
        obj.addProperty("callDirection", callDirection);
        obj.addProperty("durationSeconds", durationSeconds);
        obj.addProperty("callTime", callTime);
        if (rawDetails != null) {
            obj.addProperty("rawDetails", rawDetails);
        }
        return obj;
    }
}
