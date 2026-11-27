package com.zez.app.model;

import com.google.gson.JsonObject;

public class CallLogItem {
    private String id;
    private String name;
    private String phoneNumber;
    private String type; // INCOMING, OUTGOING, MISSED
    private long date;
    private long duration;

    public CallLogItem(String id, String name, String phoneNumber, String type, long date, long duration) {
        this.id = id;
        this.name = name != null && !name.trim().isEmpty() ? name : "Unknown";
        this.phoneNumber = phoneNumber;
        this.type = type;
        this.date = date;
        this.duration = duration;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getType() { return type; }
    public long getDate() { return date; }
    public long getDuration() { return duration; }

    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty("externalId", "android:" + id);
        obj.addProperty("phoneNumber", phoneNumber != null ? phoneNumber : "");
        obj.addProperty("name", name);
        obj.addProperty("type", type);
        obj.addProperty("timestamp", date);
        obj.addProperty("duration", duration);
        return obj;
    }
}
