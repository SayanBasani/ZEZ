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

        // Check if CAPTURE_AUDIO_OUTPUT (privileged signature permission) is granted
        boolean hasPrivilegedCapture = ContextCompat.checkSelfPermission(appContext, "android.permission.CAPTURE_AUDIO_OUTPUT") == PackageManager.PERMISSION_GRANTED;

        // Cellular recording support level evaluation based on manufacturer & OS
        RecordingCapability.SupportLevel cellularLevel;
        String summaryTitle;
        String description;

        String lowerMfr = manufacturer.toLowerCase();

        if (hasPrivilegedCapture) {
            cellularLevel = RecordingCapability.SupportLevel.FULL_SUPPORT;
            summaryTitle = "Privileged Direct Recording";
            description = "Device firmware supports system-level call audio capture via platform privileges.";
        } else if (sdkInt >= 29) { // Android 10+
            cellularLevel = RecordingCapability.SupportLevel.RESTRICTED_BY_OS;

            if (lowerMfr.contains("samsung")) {
                summaryTitle = "Samsung One UI Audio Policy (Android " + androidVersion + ")";
                description = "Samsung One UI silences third-party microphone capture during cellular calls via AudioPolicyService. Built-in recording is exclusive to Samsung Phone dialer.";
            } else if (lowerMfr.contains("xiaomi") || lowerMfr.contains("redmi") || lowerMfr.contains("poco")) {
                summaryTitle = "Xiaomi HyperOS/MIUI Audio Policy (Android " + androidVersion + ")";
                description = "Xiaomi HyperOS/MIUI enforces background capture silencing on non-system apps during active calls. Call recording is limited to system dialer.";
            } else if (lowerMfr.contains("oneplus") || lowerMfr.contains("oppo") || lowerMfr.contains("realme")) {
                summaryTitle = "ColorOS/OxygenOS Audio Policy (Android " + androidVersion + ")";
                description = "OxygenOS/ColorOS blocks third-party audio capture during cellular conversations. AudioPolicyService injects zeroed PCM buffers.";
            } else if (lowerMfr.contains("vivo") || lowerMfr.contains("iqoo")) {
                summaryTitle = "Vivo Funtouch OS Audio Policy (Android " + androidVersion + ")";
                description = "Vivo Funtouch OS restricts call audio interception to proprietary system service com.vivo.callrecorder with platform signing.";
            } else {
                summaryTitle = "Restricted by Android OS (Android " + androidVersion + ")";
                description = "Android " + androidVersion + " AudioPolicyService silences microphone capture for third-party apps during cellular calls (AudioFlinger zero-buffer policy).";
            }
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
