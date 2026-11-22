package com.zez.app.model;

import android.text.format.DateFormat;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

public class CallRecordItem implements Serializable {
    private String id;
    private String phoneNumber;
    private String contactName;
    private String callType;    // "VOICE_CALL" (TWING) or "PHONE_CALL" (Cellular) or "CELLULAR_IMPORTED"
    private String direction;   // "INCOMING" or "OUTGOING"
    private long timestamp;
    private int durationSeconds;
    private String filePath;
    private long fileSize;
    private String cloudUrl;
    private boolean isSynced;
    private String status = "COMPLETED"; // "COMPLETED", "SILENCED", "FAILED"
    private String silenceReason = "";
    private int peakAmplitude = 0;

    // Additive fields for OEM Native Recording Import
    private String importSource = "LOCAL_ENGINE"; // "LOCAL_ENGINE", "OEM_SAF_IMPORT", "MANUAL_SHARE"
    private String oemFileUri = null;
    private boolean isImported = false;
    private String recordingType = "CELLULAR_LIVE"; // "CELLULAR_IMPORTED", "TWING_VOIP", "CELLULAR_LIVE", "CELLULAR_SILENCED"

    public CallRecordItem(String id, String phoneNumber, String contactName, String callType,
                          String direction, long timestamp, int durationSeconds, String filePath, long fileSize) {
        this(id, phoneNumber, contactName, callType, direction, timestamp, durationSeconds, filePath, fileSize, null, false);
    }

    public CallRecordItem(String id, String phoneNumber, String contactName, String callType,
                          String direction, long timestamp, int durationSeconds, String filePath, long fileSize,
                          String cloudUrl, boolean isSynced) {
        this(id, phoneNumber, contactName, callType, direction, timestamp, durationSeconds, filePath, fileSize, cloudUrl, isSynced, "COMPLETED", "", 0);
    }

    public CallRecordItem(String id, String phoneNumber, String contactName, String callType,
                          String direction, long timestamp, int durationSeconds, String filePath, long fileSize,
                          String cloudUrl, boolean isSynced, String status, String silenceReason, int peakAmplitude) {
        this(id, phoneNumber, contactName, callType, direction, timestamp, durationSeconds, filePath, fileSize,
                cloudUrl, isSynced, status, silenceReason, peakAmplitude, "LOCAL_ENGINE", null, false, "CELLULAR_LIVE");
    }

    public CallRecordItem(String id, String phoneNumber, String contactName, String callType,
                          String direction, long timestamp, int durationSeconds, String filePath, long fileSize,
                          String cloudUrl, boolean isSynced, String status, String silenceReason, int peakAmplitude,
                          String importSource, String oemFileUri, boolean isImported, String recordingType) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
        this.callType = callType;
        this.direction = direction;
        this.timestamp = timestamp;
        this.durationSeconds = durationSeconds;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.cloudUrl = cloudUrl;
        this.isSynced = isSynced;
        this.status = (status != null && !status.isEmpty()) ? status : "COMPLETED";
        this.silenceReason = silenceReason != null ? silenceReason : "";
        this.peakAmplitude = peakAmplitude;
        this.importSource = importSource != null ? importSource : "LOCAL_ENGINE";
        this.oemFileUri = oemFileUri;
        this.isImported = isImported;
        this.recordingType = recordingType != null ? recordingType : (isImported ? "CELLULAR_IMPORTED" : "CELLULAR_LIVE");
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
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getStatus() { return status != null ? status : "COMPLETED"; }
    public void setStatus(String status) { this.status = status; }

    public String getSilenceReason() { return silenceReason != null ? silenceReason : ""; }
    public void setSilenceReason(String silenceReason) { this.silenceReason = silenceReason; }

    public int getPeakAmplitude() { return peakAmplitude; }
    public void setPeakAmplitude(int peakAmplitude) { this.peakAmplitude = peakAmplitude; }

    public String getImportSource() { return importSource; }
    public void setImportSource(String importSource) { this.importSource = importSource; }

    public String getOemFileUri() { return oemFileUri; }
    public void setOemFileUri(String oemFileUri) { this.oemFileUri = oemFileUri; }

    public boolean isImported() { return isImported; }
    public void setImported(boolean imported) { isImported = imported; }

    public String getRecordingType() { return recordingType; }
    public void setRecordingType(String recordingType) { this.recordingType = recordingType; }

    public String getSourceBadgeText() {
        if (isImported || "CELLULAR_IMPORTED".equalsIgnoreCase(recordingType) || "CELLULAR_IMPORTED".equalsIgnoreCase(callType)) {
            return "Native Cellular";
        }
        if ("VOICE_CALL".equalsIgnoreCase(callType) || "TWING_VOIP".equalsIgnoreCase(recordingType)) {
            return "TWING Voice";
        }
        return "Phone Call";
    }

    public boolean isSilenced() {
        return !isImported && ("SILENCED".equalsIgnoreCase(status) || "AUDIO_CAPTURE_SILENCED".equalsIgnoreCase(status));
    }

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

    public String getCloudUrl() { return cloudUrl; }
    public void setCloudUrl(String cloudUrl) { this.cloudUrl = cloudUrl; }
    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}
