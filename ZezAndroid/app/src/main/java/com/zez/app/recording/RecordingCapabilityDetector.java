package com.zez.app.recording;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.zez.app.recording.model.RecordingCapability;

public class RecordingCapabilityDetector {

    public static RecordingCapability inspect(Context context) {
        Context appContext = context.getApplicationContext();

        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER : "Unknown";
        String model = Build.MODEL != null ? Build.MODEL : "Unknown";
        String androidVersion = Build.VERSION.RELEASE != null ? Build.VERSION.RELEASE : "Unknown";
        int sdkInt = Build.VERSION.SDK_INT;

        boolean hasRecordAudio = ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean hasPhoneState = ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean hasCallLog = ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;

        // Check if CAPTURE_AUDIO_OUTPUT (privileged) is granted
        boolean hasPrivilegedCapture = ContextCompat.checkSelfPermission(appContext, "android.permission.CAPTURE_AUDIO_OUTPUT") == PackageManager.PERMISSION_GRANTED;

        // Cellular recording support level evaluation
        RecordingCapability.SupportLevel cellularLevel;
        String summaryTitle;
        String description;

        if (hasPrivilegedCapture) {
            cellularLevel = RecordingCapability.SupportLevel.FULL_SUPPORT;
            summaryTitle = "Privileged Direct Recording";
            description = "Device firmware supports system-level call audio capture.";
        } else if (sdkInt >= 29) { // Android 10+
            cellularLevel = RecordingCapability.SupportLevel.RESTRICTED_BY_OS;
            summaryTitle = "Restricted by Android OS (Android " + androidVersion + ")";
            description = "Android " + androidVersion + " policies mute third-party cellular call recording without system firmware privileges. Ambient microphone recording is attempted where allowed.";
        } else {
            cellularLevel = RecordingCapability.SupportLevel.AMBIENT_MIC_ONLY;
            summaryTitle = "Standard Microphone Audio";
            description = "Microphone capture active. Local audio and ambient surroundings are recorded.";
        }

        return new RecordingCapability(
                manufacturer,
                model,
                androidVersion,
                sdkInt,
                hasRecordAudio,
                hasPhoneState,
                hasCallLog,
                hasPrivilegedCapture,
                true, // In-app VoIP calling is 100% supported on all devices
                cellularLevel,
                summaryTitle,
                description
        );
    }
}
