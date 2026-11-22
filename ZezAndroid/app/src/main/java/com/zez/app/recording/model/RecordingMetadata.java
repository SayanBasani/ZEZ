package com.zez.app.recording.model;

import android.text.format.DateFormat;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

public class RecordingMetadata implements Serializable {
    private String recordingId;
    private String callId;
    private String phoneNumber;
    private String contactName;
    private String callType;        // "VOICE_CALL" or "PHONE_CALL"
    private String direction;       // "INCOMING" or "OUTGOING"
    private long startedAt;
    private long endedAt;
    private int durationSeconds;
    private String filePath;
    private long fileSize;
    private String mimeType;
    private String recordingStatus; // "COMPLETED", "SILENCED", "IN_PROGRESS", "FAILED"
    private String recordingSource; // "VOIP_STREAM", "MIC_AMBIENT", "RESTRICTED_CELLULAR"
    private String cloudUrl;
    private boolean isSynced;
    private int peakAmplitude;
    private String silenceReason;
    private double rmsLevelDb;

    public RecordingMetadata(String recordingId, String callId, String phoneNumber, String contactName,
                             String callType, String direction, long startedAt, long endedAt,
                             int durationSeconds, String filePath, long fileSize, String mimeType,
                             String recordingStatus, String recordingSource, String cloudUrl, boolean isSynced) {
        this(recordingId, callId, phoneNumber, contactName, callType, direction, startedAt, endedAt,
                durationSeconds, filePath, fileSize, mimeType, recordingStatus, recordingSource, cloudUrl, isSynced, 0, "", -96.0);
    }

    public RecordingMetadata(String recordingId, String callId, String phoneNumber, String contactName,
                             String callType, String direction, long startedAt, long endedAt,
                             int durationSeconds, String filePath, long fileSize, String mimeType,
                             String recordingStatus, String recordingSource, String cloudUrl, boolean isSynced,
                             int peakAmplitude, String silenceReason, double rmsLevelDb) {
        this.recordingId = recordingId;
        this.callId = callId;
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
        this.callType = callType;
        this.direction = direction;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationSeconds = durationSeconds;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.recordingStatus = recordingStatus;
        this.recordingSource = recordingSource;
        this.cloudUrl = cloudUrl;
        this.isSynced = isSynced;
        this.peakAmplitude = peakAmplitude;
        this.silenceReason = silenceReason;
        this.rmsLevelDb = rmsLevelDb;
    }

    public String getRecordingId() { return recordingId; }
    public String getCallId() { return callId; }
    public String getPhoneNumber() { return phoneNumber != null ? phoneNumber : ""; }
    public String getContactName() {
        if (contactName != null && !contactName.trim().isEmpty() && !contactName.equalsIgnoreCase("Unknown")) {
            return contactName;
        }
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }
        return "Unknown Contact";
    }
    public String getCallType() { return callType != null ? callType : "VOICE_CALL"; }
    public String getDirection() { return direction != null ? direction : "INCOMING"; }
    public long getStartedAt() { return startedAt; }
    public long getEndedAt() { return endedAt; }
    public int getDurationSeconds() { return durationSeconds; }
    public String getFilePath() { return filePath; }
    public long getFileSize() { return fileSize; }
    public String getMimeType() { return mimeType != null ? mimeType : "audio/mp4"; }
    public String getRecordingStatus() { return recordingStatus != null ? recordingStatus : "COMPLETED"; }
    public String getRecordingSource() { return recordingSource != null ? recordingSource : "MIC_AMBIENT"; }
    public String getCloudUrl() { return cloudUrl; }
    public boolean isSynced() { return isSynced; }
    public int getPeakAmplitude() { return peakAmplitude; }
    public String getSilenceReason() { return silenceReason != null ? silenceReason : ""; }
    public double getRmsLevelDb() { return rmsLevelDb; }

    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public void setEndedAt(long endedAt) { this.endedAt = endedAt; }
    public void setRecordingStatus(String recordingStatus) { this.recordingStatus = recordingStatus; }
    public void setCloudUrl(String cloudUrl) { this.cloudUrl = cloudUrl; }
    public void setSynced(boolean synced) { isSynced = synced; }
    public void setPeakAmplitude(int peakAmplitude) { this.peakAmplitude = peakAmplitude; }
    public void setSilenceReason(String silenceReason) { this.silenceReason = silenceReason; }
    public void setRmsLevelDb(double rmsLevelDb) { this.rmsLevelDb = rmsLevelDb; }

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
        return DateFormat.format("dd MMM yyyy, hh:mm a", startedAt).toString();
    }

    public String getFormattedSize() {
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", fileSize / 1024.0);
        return String.format(Locale.getDefault(), "%.1f MB", fileSize / (1024.0 * 1024.0));
    }
}
