package com.zez.app.recording;

import android.content.Context;

import com.zez.app.model.CallRecordItem;
import com.zez.app.recording.model.RecordingMetadata;
import com.zez.app.util.CallRecordDbHelper;

import java.util.ArrayList;
import java.util.List;

public class RecordingDatabaseManager {
    private final CallRecordDbHelper dbHelper;

    public RecordingDatabaseManager(Context context) {
        this.dbHelper = CallRecordDbHelper.getInstance(context.getApplicationContext());
    }

    public void insertRecord(RecordingMetadata metadata) {
        if (metadata == null) return;
        CallRecordItem item = new CallRecordItem(
                metadata.getRecordingId(),
                metadata.getPhoneNumber(),
                metadata.getContactName(),
                metadata.getCallType(),
                metadata.getDirection(),
                metadata.getStartedAt(),
                metadata.getDurationSeconds(),
                metadata.getFilePath(),
                metadata.getFileSize(),
                metadata.getCloudUrl(),
                metadata.isSynced()
        );
        dbHelper.insertRecord(item);
    }

    public void updateDurationAndSize(String recordId, int durationSec, long fileSize) {
        dbHelper.updateDurationAndSize(recordId, durationSec, fileSize);
    }

    public void updateContactAndPhone(String recordId, String phone, String contactName) {
        dbHelper.updateContactAndPhone(recordId, phone, contactName);
    }

    public boolean deleteRecord(String recordId) {
        return dbHelper.deleteRecording(recordId);
    }

    public List<CallRecordItem> getAllRecordings() {
        return dbHelper.getAllRecordings();
    }

    public List<CallRecordItem> getRecordingsForNumber(String number) {
        return dbHelper.getRecordingsForNumber(number);
    }

    public int getRecordingsCount() {
        return dbHelper.getRecordingsCount();
    }

    public long getTotalSizeBytes() {
        return dbHelper.getTotalSizeBytes();
    }
}
