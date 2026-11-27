package com.zez.manager.model;

import com.google.gson.JsonObject;

public class CallLogItem {
    public String id;
    public String externalId;
    public String name;
    public String phoneNumber;
    public String type; // INCOMING, OUTGOING, MISSED, REJECTED, VOICEMAIL, BLOCKED, UNKNOWN
    public long timestamp;
    public long duration;
    public boolean fromBackup;

    public CallLogItem() {}

    public CallLogItem(String id, String name, String phoneNumber, String type, long timestamp, long duration) {
        this.id = id;
        this.externalId = "android:" + id;
        this.name = name != null ? name : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
        this.type = type != null ? type : "UNKNOWN";
        this.timestamp = timestamp;
        this.duration = Math.max(0, duration);
        this.fromBackup = false;
    }

    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty("externalId", externalId != null && !externalId.isEmpty() ? externalId : ("android:" + id));
        obj.addProperty("phoneNumber", phoneNumber != null ? phoneNumber : "");
        obj.addProperty("name", name != null ? name : "");
        obj.addProperty("type", type != null ? type : "UNKNOWN");
        obj.addProperty("timestamp", timestamp);
        obj.addProperty("duration", duration);
        return obj;
    }
}
