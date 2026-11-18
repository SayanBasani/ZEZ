package com.zez.app.model;

import java.io.Serializable;

public class ChatContactItem implements Serializable {
    private String displayName;
    private String phoneNumber;
    private String photoUri;
    private boolean isRegistered;
    private String chatUserId;
    private String statusMessage;

    public ChatContactItem(String displayName, String phoneNumber, String photoUri, boolean isRegistered, String chatUserId, String statusMessage) {
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.photoUri = photoUri;
        this.isRegistered = isRegistered;
        this.chatUserId = chatUserId;
        this.statusMessage = statusMessage;
    }

    public String getDisplayName() { return displayName != null ? displayName : phoneNumber; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPhotoUri() { return photoUri; }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }

    public boolean isRegistered() { return isRegistered; }
    public void setRegistered(boolean registered) { isRegistered = registered; }

    public String getChatUserId() { return chatUserId; }
    public void setChatUserId(String chatUserId) { this.chatUserId = chatUserId; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
