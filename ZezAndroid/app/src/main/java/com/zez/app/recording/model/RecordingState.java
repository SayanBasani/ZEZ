package com.zez.app.recording.model;

/**
 * State machine for call recording lifecycle and real audio validation.
 */
public enum RecordingState {
    IDLE("Idle", "No active call or recording session"),
    CALL_DETECTED("Call Detected", "Telephony call detected, preparing recording"),
    RECORDING_INITIALIZING("Initializing Recording", "Setting up audio capture and diagnostic listeners"),
    RECORDING_ACTIVE("Recording Active", "Recording engine active, monitoring audio stream"),
    AUDIO_CAPTURE_CONFIRMED("Audio Capture Verified", "Authentic non-silent audio data captured and confirmed"),
    AUDIO_CAPTURE_SILENCED("Audio Input Blocked by Android", "Microphone capture silenced by Android OS AudioPolicyService during call"),
    RECORDING_FAILED("Recording Failed", "Failed to start or maintain recording session"),
    RECORDING_COMPLETED("Recording Completed", "Recording session concluded and audio file saved");

    private final String displayTitle;
    private final String description;

    RecordingState(String displayTitle, String description) {
        this.displayTitle = displayTitle;
        this.description = description;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public String getDescription() {
        return description;
    }
}
