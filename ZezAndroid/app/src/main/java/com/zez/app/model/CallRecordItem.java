package com.zez.app.model;

import android.text.format.DateFormat;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

public class CallRecordItem implements Serializable {
    private String id;
    private String phoneNumber;
    private String contactName;
    private String callType;    // "VOICE_CALL" (TWING) or "PHONE_CALL" (Cellular)
    private String direction;   // "INCOMING" or "OUTGOING"
    private long timestamp;
    private int durationSeconds;
    private String filePath;
    private long fileSize;

    public CallRecordItem(String id, String phoneNumber, String contactName, String callType,
                          String direction, long timestamp, int durationSeconds, String filePath, long fileSize) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
        this.callType = callType;
        this.direction = direction;
        this.timestamp = timestamp;
        this.durationSeconds = durationSeconds;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    public String getId() { return id; }
    public String getPhoneNumber() { return phoneNumber != null ? phoneNumber : ""; }
    public String getContactName() {
        if (contactName != null && !contactName.trim().isEmpty() && !contactName.equals("Unknown")) {
            return contactName;
        }
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }
        return "Unknown Contact";
    }
    public String getCallType() { return callType != null ? callType : "VOICE_CALL"; }
    public String getDirection() { return direction != null ? direction : "INCOMING"; }
    public long getTimestamp() { return timestamp; }
    public int getDurationSeconds() { return durationSeconds; }
    public String getFilePath() { return filePath; }
    public long getFileSize() { return fileSize; }

    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public boolean fileExists() {
        if (filePath == null) return false;
        File f = new File(filePath);
        return f.exists() && f.length() > 0;
    }

    public String getFormattedDuration() {
        int m = durationSeconds / 60;
        int s = durationSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", m, s);
    }

    public String getFormattedDate() {
        return DateFormat.format("dd MMM yyyy, hh:mm a", timestamp).toString();
    }

    public String getFormattedSize() {
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", fileSize / 1024.0);
        return String.format(Locale.getDefault(), "%.1f MB", fileSize / (1024.0 * 1024.0));
    }
}
