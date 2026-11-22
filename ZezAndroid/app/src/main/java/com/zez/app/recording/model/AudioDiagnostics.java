package com.zez.app.recording.model;

import java.io.Serializable;
import java.util.Locale;

/**
 * Diagnostic data snapshot tracking real-time capture states, OS silencing flags,
 * peak amplitudes, RMS levels, and input hardware devices.
 */
public class AudioDiagnostics implements Serializable {
    private RecordingState currentState = RecordingState.IDLE;
    private boolean isClientSilenced = false;
    private int peakAmplitude = 0;
    private double rmsLevelDb = -96.0;
    private long totalSamplesEvaluated = 0;
    private long nonZeroSamplesCount = 0;
    private long silenceDurationMs = 0;
    private long activeDurationMs = 0;
    private String audioInputDevice = "Default Mic";
    private int audioSource = 1; // MediaRecorder.AudioSource.MIC
    private String formatInfo = "AAC 44.1kHz 128kbps";
    private String silenceReason = "";
    private String failureReason = "";
    private boolean hasConfirmedAudio = false;
    private long sessionStartTime = 0;
    private long confirmedAudioTimestamp = 0;

    public AudioDiagnostics() {
        this.currentState = RecordingState.IDLE;
    }

    public synchronized void reset() {
        this.currentState = RecordingState.IDLE;
        this.isClientSilenced = false;
        this.peakAmplitude = 0;
        this.rmsLevelDb = -96.0;
        this.totalSamplesEvaluated = 0;
        this.nonZeroSamplesCount = 0;
        this.silenceDurationMs = 0;
        this.activeDurationMs = 0;
        this.silenceReason = "";
        this.failureReason = "";
        this.hasConfirmedAudio = false;
        this.sessionStartTime = 0;
        this.confirmedAudioTimestamp = 0;
    }

    public synchronized RecordingState getCurrentState() {
        return currentState;
    }

    public synchronized void setCurrentState(RecordingState state) {
        this.currentState = state;
    }

    public synchronized boolean isClientSilenced() {
        return isClientSilenced;
    }

    public synchronized void setClientSilenced(boolean clientSilenced) {
        this.isClientSilenced = clientSilenced;
    }

    public synchronized int getPeakAmplitude() {
        return peakAmplitude;
    }

    public synchronized void setPeakAmplitude(int peakAmplitude) {
        this.peakAmplitude = peakAmplitude;
    }

    public synchronized void updateAmplitude(int amplitude, long intervalMs) {
        this.activeDurationMs += intervalMs;
        if (amplitude > this.peakAmplitude) {
            this.peakAmplitude = amplitude;
        }

        // Audibility threshold: > 250 amplitude indicates genuine acoustic energy
        if (amplitude > 250 && !isClientSilenced) {
            this.hasConfirmedAudio = true;
            this.confirmedAudioTimestamp = System.currentTimeMillis();
            this.silenceDurationMs = 0;
            if (currentState != RecordingState.AUDIO_CAPTURE_CONFIRMED &&
                currentState != RecordingState.RECORDING_COMPLETED) {
                currentState = RecordingState.AUDIO_CAPTURE_CONFIRMED;
            }
        } else {
            this.silenceDurationMs += intervalMs;
            if (isClientSilenced) {
                currentState = RecordingState.AUDIO_CAPTURE_SILENCED;
                this.silenceReason = "Muted by Android AudioPolicyService (isClientSilenced=true)";
            } else if (!hasConfirmedAudio && silenceDurationMs >= 3000) {
                currentState = RecordingState.AUDIO_CAPTURE_SILENCED;
                this.silenceReason = "Zero acoustic amplitude captured for " + (silenceDurationMs / 1000) + "s";
            }
        }
    }

    public synchronized double getRmsLevelDb() {
        return rmsLevelDb;
    }

    public synchronized void setRmsLevelDb(double rmsLevelDb) {
        this.rmsLevelDb = rmsLevelDb;
    }

    public synchronized long getTotalSamplesEvaluated() {
        return totalSamplesEvaluated;
    }

    public synchronized void addSamples(long total, long nonZero) {
        this.totalSamplesEvaluated += total;
        this.nonZeroSamplesCount += nonZero;
    }

    public synchronized long getNonZeroSamplesCount() {
        return nonZeroSamplesCount;
    }

    public synchronized long getSilenceDurationMs() {
        return silenceDurationMs;
    }

    public synchronized long getActiveDurationMs() {
        return activeDurationMs;
    }

    public synchronized String getAudioInputDevice() {
        return audioInputDevice;
    }

    public synchronized void setAudioInputDevice(String audioInputDevice) {
        this.audioInputDevice = audioInputDevice;
    }

    public synchronized int getAudioSource() {
        return audioSource;
    }

    public synchronized void setAudioSource(int audioSource) {
        this.audioSource = audioSource;
    }

    public synchronized String getFormatInfo() {
        return formatInfo;
    }

    public synchronized void setFormatInfo(String formatInfo) {
        this.formatInfo = formatInfo;
    }

    public synchronized String getSilenceReason() {
        return silenceReason;
    }

    public synchronized void setSilenceReason(String silenceReason) {
        this.silenceReason = silenceReason;
    }

    public synchronized String getFailureReason() {
        return failureReason;
    }

    public synchronized void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public synchronized boolean hasConfirmedAudio() {
        return hasConfirmedAudio;
    }

    public synchronized void setHasConfirmedAudio(boolean hasConfirmedAudio) {
        this.hasConfirmedAudio = hasConfirmedAudio;
    }

    public synchronized long getSessionStartTime() {
        return sessionStartTime;
    }

    public synchronized void setSessionStartTime(long sessionStartTime) {
        this.sessionStartTime = sessionStartTime;
    }

    public synchronized long getConfirmedAudioTimestamp() {
        return confirmedAudioTimestamp;
    }

    public synchronized String getDiagnosticSummary() {
        return String.format(Locale.getDefault(),
                "State: %s | Silenced: %b | Peak: %d | Source: %d | Device: %s | SilenceDur: %ds",
                currentState.name(), isClientSilenced, peakAmplitude, audioSource, audioInputDevice, silenceDurationMs / 1000);
    }
}
