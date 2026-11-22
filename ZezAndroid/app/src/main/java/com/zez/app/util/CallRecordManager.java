package com.zez.app.util;

import android.content.Context;

import com.zez.app.model.CallRecordItem;
import com.zez.app.recording.CallRecordingManager;

import java.util.List;

/**
 * Backward-compatible facade delegating to the modular CallRecordingManager.
 */
public class CallRecordManager {
    private static volatile CallRecordManager instance;
    private final CallRecordingManager delegate;

    public static synchronized CallRecordManager getInstance(Context context) {
        if (instance == null) {
            instance = new CallRecordManager(context.getApplicationContext());
        }
        return instance;
    }

    private CallRecordManager(Context context) {
        this.delegate = CallRecordingManager.getInstance(context);
    }

    public boolean isAutoRecordEnabled() {
        return delegate.isAutoRecordEnabled();
    }

    public void setAutoRecordEnabled(boolean enabled) {
        delegate.setAutoRecordEnabled(enabled);
    }

    public boolean isRecording() {
        return delegate.isRecording();
    }

    public void updateCurrentCallNumber(String number) {
        delegate.updateCurrentCallNumber(number);
    }

    public boolean startRecording(String phoneNumber, String contactName, String callType, String direction) {
        return delegate.startRecording(phoneNumber, contactName, callType, direction);
    }

    public CallRecordItem stopRecording() {
        return delegate.stopRecording();
    }

    public List<CallRecordItem> getAllRecordings() {
        return delegate.getAllRecordings();
    }

    public List<CallRecordItem> getRecordingsForNumber(String numberOrId) {
        return delegate.getRecordingsForNumber(numberOrId);
    }

    public boolean deleteRecording(String id) {
        return delegate.deleteRecording(id);
    }

    public int getRecordingsCount() {
        return delegate.getRecordingsCount();
    }

    public long getTotalSizeBytes() {
        return delegate.getTotalSizeBytes();
    }
}
