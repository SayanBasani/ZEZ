package com.zez.app.recording;

import android.content.Context;
import android.util.Log;

import com.zez.app.model.CallLogItem;
import com.zez.app.model.CallRecordItem;
import com.zez.app.recording.model.AudioDiagnostics;
import com.zez.app.recording.model.RecordingCapability;
import com.zez.app.recording.model.RecordingMetadata;
import com.zez.app.recording.model.RecordingState;
import com.zez.app.util.CallLogHelper;

import java.io.File;
import java.util.List;

public class CallRecordingManager {
    private static final String TAG = "CallRecordingManager";

    public interface RecordingStateListener {
        void onStateChanged(RecordingState state, AudioDiagnostics diagnostics);
    }

    private static volatile CallRecordingManager instance;

    private final Context context;
    private final RecordingCapabilityDetector capabilityDetector;
    private final RecordingPermissionManager permissionManager;
    private final RecordingStorageManager storageManager;
    private final RecordingDatabaseManager databaseManager;
    private final RecordingNotificationManager notificationManager;
    private final RecordingEngine recordingEngine;
    private final CallStateMonitor stateMonitor;

    private RecordingMetadata activeMetadata;
    private long recordingStartTime = 0;
    private RecordingStateListener stateListener;

    public static synchronized CallRecordingManager getInstance(Context context) {
        if (instance == null) {
            instance = new CallRecordingManager(context.getApplicationContext());
        }
        return instance;
    }

    private CallRecordingManager(Context context) {
        this.context = context;
        this.capabilityDetector = new RecordingCapabilityDetector();
        this.permissionManager = new RecordingPermissionManager(context);
        this.storageManager = new RecordingStorageManager(context);
        this.databaseManager = new RecordingDatabaseManager(context);
        this.notificationManager = new RecordingNotificationManager(context);
        this.recordingEngine = new RecordingEngine(context);
        this.stateMonitor = new CallStateMonitor(context);

        this.recordingEngine.setDiagnosticListener((state, diagnostics) -> {
            String contact = (activeMetadata != null) ? activeMetadata.getContactName() : "Active Call";
            notificationManager.updateNotification(contact, state);
            if (stateListener != null) {
                try {
                    stateListener.onStateChanged(state, diagnostics);
                } catch (Exception ignored) {}
            }
        });
    }

    public void setStateListener(RecordingStateListener listener) {
        this.stateListener = listener;
    }

    public AudioDiagnostics getDiagnostics() {
        return recordingEngine.getDiagnostics();
    }

    public RecordingState getCurrentState() {
        return recordingEngine.getDiagnostics().getCurrentState();
    }

    public RecordingCapability getCapability() {
        return RecordingCapabilityDetector.inspect(context);
    }

    public RecordingPermissionManager getPermissionManager() {
        return permissionManager;
    }

    public RecordingStorageManager getStorageManager() {
        return storageManager;
    }

    public RecordingDatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public RecordingNotificationManager getNotificationManager() {
        return notificationManager;
    }

    public CallStateMonitor getStateMonitor() {
        return stateMonitor;
    }

    public boolean isAutoRecordEnabled() {
        return permissionManager.isAutoRecordEnabled();
    }

    public void setAutoRecordEnabled(boolean enabled) {
        permissionManager.setAutoRecordEnabled(enabled);
    }

    public synchronized boolean isRecording() {
        return recordingEngine.isRecording();
    }

    public synchronized void updateCurrentCallNumber(String number) {
        if (number == null || number.trim().isEmpty()) return;
        String trimmed = number.trim();
        if (activeMetadata != null) {
            String existing = activeMetadata.getPhoneNumber();
            if (existing == null || existing.isEmpty() || existing.equalsIgnoreCase("unknown")) {
                activeMetadata.setPhoneNumber(trimmed);
                String currentName = activeMetadata.getContactName();
                if (currentName.equalsIgnoreCase("Unknown Contact") || currentName.equalsIgnoreCase("Unknown")) {
                    activeMetadata.setContactName(trimmed);
                }
                databaseManager.updateContactAndPhone(activeMetadata.getRecordingId(), trimmed, activeMetadata.getContactName());
                Log.d(TAG, "Updated in-flight recording with caller number: " + trimmed);
            }
        }
    }

    public synchronized boolean startRecording(String phoneNumber, String contactName, String callType, String direction) {
        if (!permissionManager.isReadyToRecord()) {
            Log.d(TAG, "Recording not permitted or not opted in. Skipping.");
            return false;
        }

        if (recordingEngine.isRecording()) {
            Log.w(TAG, "Recording already active. Stopping prior session.");
            stopRecording();
        }

        try {
            String recordId = storageManager.generateRecordId();
            File outputFile = storageManager.generateOutputFile(phoneNumber);
            String filePath = outputFile.getAbsolutePath();

            String source = "VOICE_CALL".equalsIgnoreCase(callType) ? "VOIP_STREAM" : "MIC_AMBIENT";

            boolean started = recordingEngine.start(filePath, callType);
            if (!started) {
                Log.e(TAG, "RecordingEngine failed to start.");
                return false;
            }

            recordingStartTime = System.currentTimeMillis();
            activeMetadata = new RecordingMetadata(
                    recordId,
                    recordId,
                    phoneNumber != null ? phoneNumber : "unknown",
                    contactName != null ? contactName : "Unknown Contact",
                    callType != null ? callType : "PHONE_CALL",
                    direction != null ? direction : "INCOMING",
                    recordingStartTime,
                    0,
                    0,
                    filePath,
                    0,
                    "audio/mp4",
                    "IN_PROGRESS",
                    source,
                    null,
                    false,
                    0,
                    "",
                    -96.0
            );

            databaseManager.insertRecord(activeMetadata);
            Log.d(TAG, "Started recording call session: id=" + recordId + ", file=" + filePath);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception starting recording: " + e.getMessage(), e);
            recordingEngine.stop();
            return false;
        }
    }

    public synchronized CallRecordItem stopRecording() {
        if (!recordingEngine.isRecording() && activeMetadata == null) {
            return null;
        }

        RecordingMetadata finished = activeMetadata;
        recordingEngine.stop();
        AudioDiagnostics diagnostics = recordingEngine.getDiagnostics();

        long durationSec = (System.currentTimeMillis() - recordingStartTime) / 1000;
        if (durationSec < 1) durationSec = 1;

        CallRecordItem resultItem = null;

        if (finished != null && finished.getFilePath() != null) {
            File file = new File(finished.getFilePath());
            if (!storageManager.validateAndCleanup(file)) {
                // File empty or corrupted
                databaseManager.deleteRecord(finished.getRecordingId());
            } else {
                long fileSize = file.length();
                finished.setDurationSeconds((int) durationSec);
                finished.setFileSize(fileSize);
                finished.setEndedAt(System.currentTimeMillis());
                finished.setPeakAmplitude(diagnostics.getPeakAmplitude());
                finished.setRmsLevelDb(diagnostics.getRmsLevelDb());

                // Determine true recording status:
                // Distinguish genuine conversational pauses from OS-level audio muting
                String finalStatus;
                String silenceReason = diagnostics.getSilenceReason();

                if (diagnostics.isClientSilenced()) {
                    finalStatus = "SILENCED";
                    silenceReason = "Muted by Android AudioPolicyService (isClientSilenced=true)";
                    Log.w(TAG, "Recording finalized with status SILENCED: OS capture policy muted third-party mic.");
                } else if (!diagnostics.hasConfirmedAudio() && diagnostics.getPeakAmplitude() == 0) {
                    finalStatus = "SILENCED";
                    silenceReason = "Zero audio amplitude captured across entire duration (" + durationSec + "s)";
                    Log.w(TAG, "Recording finalized with status SILENCED: 100% zero audio samples.");
                } else {
                    finalStatus = "COMPLETED";
                    silenceReason = "";
                    Log.d(TAG, "Recording finalized with status COMPLETED: Verified audio present (Peak=" + diagnostics.getPeakAmplitude() + ").");
                }

                finished.setRecordingStatus(finalStatus);
                finished.setSilenceReason(silenceReason);

                // Resolve real number from CallLog if initially unknown
                if (finished.getPhoneNumber().isEmpty() || finished.getPhoneNumber().equalsIgnoreCase("unknown")) {
                    try {
                        CallLogItem lastCall = CallLogHelper.getLastCallLog(context);
                        if (lastCall != null && lastCall.getPhoneNumber() != null && !lastCall.getPhoneNumber().trim().isEmpty()) {
                            String realNum = lastCall.getPhoneNumber().trim();
                            String realName = lastCall.getName();
                            if (realName == null || realName.trim().isEmpty() || "Unknown".equalsIgnoreCase(realName.trim())) {
                                realName = realNum;
                            }
                            finished.setPhoneNumber(realNum);
                            finished.setContactName(realName);
                            databaseManager.updateContactAndPhone(finished.getRecordingId(), realNum, realName);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed resolving call number from CallLog: " + e.getMessage());
                    }
                }

                databaseManager.updateDurationSizeAndStatus(
                        finished.getRecordingId(),
                        (int) durationSec,
                        fileSize,
                        finalStatus,
                        silenceReason,
                        diagnostics.getPeakAmplitude()
                );

                resultItem = new CallRecordItem(
                        finished.getRecordingId(),
                        finished.getPhoneNumber(),
                        finished.getContactName(),
                        finished.getCallType(),
                        finished.getDirection(),
                        finished.getStartedAt(),
                        finished.getDurationSeconds(),
                        finished.getFilePath(),
                        finished.getFileSize(),
                        finished.getCloudUrl(),
                        finished.isSynced(),
                        finalStatus,
                        silenceReason,
                        diagnostics.getPeakAmplitude()
                );

                Log.d(TAG, "Call recording saved: " + finished.getFilePath() + " (" + fileSize + " bytes, " + durationSec + "s, status=" + finalStatus + ")");
            }
        }

        activeMetadata = null;
        recordingStartTime = 0;
        return resultItem;
    }

    public List<CallRecordItem> getAllRecordings() {
        return databaseManager.getAllRecordings();
    }

    public List<CallRecordItem> getRecordingsForNumber(String number) {
        return databaseManager.getRecordingsForNumber(number);
    }

    public boolean deleteRecording(String id) {
        return databaseManager.deleteRecord(id);
    }

    public int getRecordingsCount() {
        return databaseManager.getRecordingsCount();
    }

    public long getTotalSizeBytes() {
        return databaseManager.getTotalSizeBytes();
    }
}
