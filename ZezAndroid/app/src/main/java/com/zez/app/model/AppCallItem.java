package com.zez.app.model;

import java.io.Serializable;

public class AppCallItem implements Serializable {
    private String id;
    private String callType;       // "audio" or "video"
    private String direction;      // "incoming", "outgoing", "missed"
    private String status;         // "connected", "missed", "rejected", "ended"
    private String peerId;
    private String peerName;
    private String peerPhone;
    private String peerAvatar;
    private String startedAt;
    private int durationSeconds;

    public AppCallItem() {}

    public AppCallItem(String id, String callType, String direction, String status,
                       String peerId, String peerName, String peerPhone, String peerAvatar,
                       String startedAt, int durationSeconds) {
        this.id = id;
        this.callType = callType;
        this.direction = direction;
        this.status = status;
        this.peerId = peerId;
        this.peerName = peerName;
        this.peerPhone = peerPhone;
        this.peerAvatar = peerAvatar;
        this.startedAt = startedAt;
        this.durationSeconds = durationSeconds;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCallType() { return callType != null ? callType : "audio"; }
    public void setCallType(String callType) { this.callType = callType; }

    public boolean isVideo() { return "video".equalsIgnoreCase(callType); }

    public String getDirection() { return direction != null ? direction : "incoming"; }
    public void setDirection(String direction) { this.direction = direction; }

    public boolean isMissed() {
        return "missed".equalsIgnoreCase(direction) || "missed".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status);
    }

    public boolean isOutgoing() {
        return "outgoing".equalsIgnoreCase(direction);
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPeerId() { return peerId; }
    public void setPeerId(String peerId) { this.peerId = peerId; }

    public String getPeerName() { return peerName != null && !peerName.trim().isEmpty() ? peerName : "Contact"; }
    public void setPeerName(String peerName) { this.peerName = peerName; }

    public String getPeerPhone() { return peerPhone; }
    public void setPeerPhone(String peerPhone) { this.peerPhone = peerPhone; }

    public String getPeerAvatar() { return peerAvatar; }
    public void setPeerAvatar(String peerAvatar) { this.peerAvatar = peerAvatar; }

    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getFormattedDuration() {
        if (isMissed()) return "Missed";
        if (durationSeconds <= 0) return "< 1 min";
        int mins = durationSeconds / 60;
        int secs = durationSeconds % 60;
        if (mins > 0) {
            return String.format(java.util.Locale.getDefault(), "%d min %02d sec", mins, secs);
        } else {
            return String.format(java.util.Locale.getDefault(), "%d sec", secs);
        }
    }
}
