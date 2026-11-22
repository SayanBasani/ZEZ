package com.zez.app.recording;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioRecordingConfiguration;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.zez.app.recording.model.AudioDiagnostics;
import com.zez.app.recording.model.RecordingState;

import java.io.File;
import java.util.List;

public class RecordingEngine {
    private static final String TAG = "RecordingEngine";

    public interface RecordingDiagnosticListener {
        void onDiagnosticUpdate(RecordingState state, AudioDiagnostics diagnostics);
    }

    private final Context context;
    private final AudioManager audioManager;
    private final AudioDiagnostics diagnostics;

    private MediaRecorder mediaRecorder;
    private boolean isRecordingActive = false;
    private String currentOutputFilePath;

    private HandlerThread monitorThread;
    private Handler monitorHandler;
    private Runnable amplitudePoller;

    private AudioManager.AudioRecordingCallback recordingCallback;
    private RecordingDiagnosticListener diagnosticListener;

    public RecordingEngine(Context context) {
        this.context = context.getApplicationContext();
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        this.diagnostics = new AudioDiagnostics();
    }

    public void setDiagnosticListener(RecordingDiagnosticListener listener) {
        this.diagnosticListener = listener;
    }

    public AudioDiagnostics getDiagnostics() {
        return diagnostics;
    }

    public synchronized boolean start(String outputFilePath, String callType) {
        if (isRecordingActive) {
            stop();
        }

        diagnostics.reset();
        diagnostics.setCurrentState(RecordingState.RECORDING_INITIALIZING);
        diagnostics.setSessionStartTime(System.currentTimeMillis());
        currentOutputFilePath = outputFilePath;
        notifyStateChange(RecordingState.RECORDING_INITIALIZING);

        int[] candidateSources;
        if ("VOICE_CALL".equalsIgnoreCase(callType)) {
            candidateSources = new int[]{
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    MediaRecorder.AudioSource.MIC,
                    MediaRecorder.AudioSource.DEFAULT
            };
        } else {
            candidateSources = new int[]{
                    MediaRecorder.AudioSource.MIC,
                    MediaRecorder.AudioSource.DEFAULT,
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    MediaRecorder.AudioSource.VOICE_RECOGNITION
            };
        }

        boolean prepared = false;
        int selectedSource = MediaRecorder.AudioSource.MIC;

        for (int source : candidateSources) {
            try {
                release();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    mediaRecorder = new MediaRecorder(context);
                } else {
                    mediaRecorder = new MediaRecorder();
                }

                mediaRecorder.setAudioSource(source);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    try {
                        mediaRecorder.setPrivacySensitive(false);
                    } catch (Exception ignored) {}
                }

                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setAudioEncodingBitRate(128000);
                mediaRecorder.setAudioSamplingRate(44100);
                mediaRecorder.setOutputFile(outputFilePath);

                mediaRecorder.prepare();
                prepared = true;
                selectedSource = source;
                diagnostics.setAudioSource(source);
                Log.d(TAG, "MediaRecorder prepared with source: " + source);
                break;
            } catch (Exception e) {
                Log.w(TAG, "Failed to prepare AudioSource " + source + ": " + e.getMessage());
            }
        }

        if (!prepared) {
            Log.e(TAG, "No candidate audio source could be prepared.");
            diagnostics.setCurrentState(RecordingState.RECORDING_FAILED);
            diagnostics.setFailureReason("Failed to prepare any MediaRecorder audio source.");
            notifyStateChange(RecordingState.RECORDING_FAILED);
            release();
            return false;
        }

        // Detect current audio input device
        detectAudioDevice();

        // Register AudioManager.AudioRecordingCallback on Android 10+ (API 29+)
        registerAudioRecordingCallback();

        try {
            // Unmute mic if muted
            if (audioManager != null && audioManager.isMicrophoneMute()) {
                audioManager.setMicrophoneMute(false);
            }
        } catch (Exception ignored) {}

        try {
            mediaRecorder.start();
            isRecordingActive = true;
            diagnostics.setCurrentState(RecordingState.RECORDING_ACTIVE);
            notifyStateChange(RecordingState.RECORDING_ACTIVE);

            // Start background amplitude and silence monitor
            startAmplitudeMonitor();

            Log.d(TAG, "MediaRecorder started successfully to: " + outputFilePath + " using source " + selectedSource);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to start MediaRecorder: " + e.getMessage(), e);
            diagnostics.setCurrentState(RecordingState.RECORDING_FAILED);
            diagnostics.setFailureReason("Exception starting MediaRecorder: " + e.getMessage());
            notifyStateChange(RecordingState.RECORDING_FAILED);
            unregisterAudioRecordingCallback();
            release();
            isRecordingActive = false;
            return false;
        }
    }

    public synchronized void stop() {
        if (!isRecordingActive && mediaRecorder == null) {
            return;
        }

        isRecordingActive = false;
        stopAmplitudeMonitor();
        unregisterAudioRecordingCallback();

        try {
            if (mediaRecorder != null) {
                try {
                    mediaRecorder.stop();
                } catch (RuntimeException stopException) {
                    Log.w(TAG, "MediaRecorder stop failed (call too short?): " + stopException.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping MediaRecorder: " + e.getMessage(), e);
        } finally {
            release();
        }

        // Post-recording audio file sample verification
        if (currentOutputFilePath != null) {
            File f = new File(currentOutputFilePath);
            if (f.exists() && f.length() > 200) {
                AudioCaptureValidator.PcmAnalysisResult analysis =
                        AudioCaptureValidator.validateRecordedAudioFile(currentOutputFilePath);

                diagnostics.setPeakAmplitude(Math.max(diagnostics.getPeakAmplitude(), analysis.peakAmplitude));
                diagnostics.setRmsLevelDb(analysis.rmsLevelDb);
                diagnostics.addSamples(analysis.totalSamples, analysis.nonZeroSamples);

                if (diagnostics.isClientSilenced() || analysis.isCompletelyZero || analysis.peakAmplitude == 0) {
                    diagnostics.setCurrentState(RecordingState.AUDIO_CAPTURE_SILENCED);
                    if (diagnostics.getSilenceReason().isEmpty()) {
                        diagnostics.setSilenceReason("Silenced by Android OS AudioPolicyService during cellular call (zero audio samples)");
                    }
                    Log.w(TAG, "Post-recording validation: Audio capture was SILENCED by Android OS.");
                } else {
                    diagnostics.setCurrentState(RecordingState.RECORDING_COMPLETED);
                    diagnostics.setHasConfirmedAudio(true);
                    Log.d(TAG, "Post-recording validation: Audio verified successfully (Peak=" + analysis.peakAmplitude + ", RMS=" + analysis.rmsLevelDb + "dB)");
                }
            } else {
                diagnostics.setCurrentState(RecordingState.RECORDING_FAILED);
                diagnostics.setFailureReason("Output file empty or missing");
            }
        }

        notifyStateChange(diagnostics.getCurrentState());
    }

    public synchronized boolean isRecording() {
        return isRecordingActive;
    }

    private void release() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (Exception ignored) {}
            mediaRecorder = null;
        }
    }

    private void registerAudioRecordingCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && audioManager != null) {
            try {
                if (monitorThread == null) {
                    monitorThread = new HandlerThread("RecordingDiagnosticThread");
                    monitorThread.start();
                    monitorHandler = new Handler(monitorThread.getLooper());
                }

                recordingCallback = new AudioManager.AudioRecordingCallback() {
                    @Override
                    public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {
                        handleRecordingConfigChanged(configs);
                    }
                };
                audioManager.registerAudioRecordingCallback(recordingCallback, monitorHandler);
                Log.d(TAG, "Registered AudioManager.AudioRecordingCallback");
            } catch (Exception e) {
                Log.w(TAG, "Failed registering AudioRecordingCallback: " + e.getMessage());
            }
        }
    }

    private void unregisterAudioRecordingCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && audioManager != null && recordingCallback != null) {
            try {
                audioManager.unregisterAudioRecordingCallback(recordingCallback);
                Log.d(TAG, "Unregistered AudioManager.AudioRecordingCallback");
            } catch (Exception ignored) {}
            recordingCallback = null;
        }
    }

    private void handleRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || configs == null) return;

        for (AudioRecordingConfiguration config : configs) {
            try {
                boolean silenced = config.isClientSilenced();
                diagnostics.setClientSilenced(silenced);

                AudioDeviceInfo device = config.getAudioDevice();
                if (device != null) {
                    diagnostics.setAudioInputDevice(getDeviceTypeName(device.getType()) + " (" + device.getProductName() + ")");
                }

                diagnostics.setAudioSource(config.getClientAudioSource());

                if (silenced) {
                    diagnostics.setCurrentState(RecordingState.AUDIO_CAPTURE_SILENCED);
                    diagnostics.setSilenceReason("Silenced by Android AudioPolicyService (isClientSilenced=true)");
                    Log.w(TAG, "AudioRecordingConfiguration: isClientSilenced=TRUE -> Muted by Android OS");
                    notifyStateChange(RecordingState.AUDIO_CAPTURE_SILENCED);
                } else if (diagnostics.hasConfirmedAudio()) {
                    diagnostics.setCurrentState(RecordingState.AUDIO_CAPTURE_CONFIRMED);
                    notifyStateChange(RecordingState.AUDIO_CAPTURE_CONFIRMED);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error processing AudioRecordingConfiguration: " + e.getMessage());
            }
        }
    }

    private void detectAudioDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioManager != null) {
            try {
                AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
                if (devices != null && devices.length > 0) {
                    diagnostics.setAudioInputDevice(getDeviceTypeName(devices[0].getType()));
                }
            } catch (Exception ignored) {}
        }
    }

    private void startAmplitudeMonitor() {
        if (monitorThread == null) {
            monitorThread = new HandlerThread("RecordingDiagnosticThread");
            monitorThread.start();
            monitorHandler = new Handler(monitorThread.getLooper());
        }

        amplitudePoller = new Runnable() {
            @Override
            public void run() {
                if (!isRecordingActive || mediaRecorder == null) return;
                try {
                    int amplitude = mediaRecorder.getMaxAmplitude();
                    diagnostics.updateAmplitude(amplitude, 500);

                    // Check if state changed based on amplitude
                    notifyStateChange(diagnostics.getCurrentState());
                } catch (Exception e) {
                    Log.w(TAG, "Error polling amplitude: " + e.getMessage());
                }

                if (isRecordingActive && monitorHandler != null) {
                    monitorHandler.postDelayed(this, 500);
                }
            }
        };

        monitorHandler.postDelayed(amplitudePoller, 500);
    }

    private void stopAmplitudeMonitor() {
        if (monitorHandler != null && amplitudePoller != null) {
            monitorHandler.removeCallbacks(amplitudePoller);
        }
        if (monitorThread != null) {
            monitorThread.quitSafely();
            monitorThread = null;
            monitorHandler = null;
        }
    }

    private void notifyStateChange(RecordingState state) {
        if (diagnosticListener != null) {
            try {
                diagnosticListener.onDiagnosticUpdate(state, diagnostics);
            } catch (Exception ignored) {}
        }
    }

    private String getDeviceTypeName(int type) {
        switch (type) {
            case AudioDeviceInfo.TYPE_BUILTIN_MIC: return "Built-in Microphone";
            case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE: return "Built-in Earpiece";
            case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER: return "Built-in Speaker";
            case AudioDeviceInfo.TYPE_WIRED_HEADSET: return "Wired Headset";
            case AudioDeviceInfo.TYPE_WIRED_HEADPHONES: return "Wired Headphones";
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO: return "Bluetooth SCO Headset";
            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP: return "Bluetooth A2DP";
            case AudioDeviceInfo.TYPE_USB_DEVICE: return "USB Audio Device";
            case AudioDeviceInfo.TYPE_USB_HEADSET: return "USB Headset";
            case AudioDeviceInfo.TYPE_TELEPHONY: return "Telephony Baseband Sink";
            default: return "Audio Device (" + type + ")";
        }
    }
}
