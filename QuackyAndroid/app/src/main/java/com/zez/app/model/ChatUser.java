package com.zez.app.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ChatUser implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("status_message")
    private String statusMessage;

    @SerializedName("last_seen")
    private String lastSeen;

    private boolean isRegistered;

    public ChatUser() {}

    public ChatUser(String id, String phoneNumber, String name, String avatarUrl, String statusMessage, String lastSeen) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.statusMessage = statusMessage;
        this.lastSeen = lastSeen;
        this.isRegistered = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    public String getLastSeen() { return lastSeen; }
    public void setLastSeen(String lastSeen) { this.lastSeen = lastSeen; }

    public boolean isRegistered() { return isRegistered; }
    public void setRegistered(boolean registered) { isRegistered = registered; }
}
