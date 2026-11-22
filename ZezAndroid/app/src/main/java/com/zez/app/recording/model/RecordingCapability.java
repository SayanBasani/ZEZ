package com.zez.app.recording.model;

import java.io.Serializable;

public class RecordingCapability implements Serializable {
    public enum SupportLevel {
        FULL_SUPPORT,           // Complete two-way audio (e.g. In-App VoIP)
        AMBIENT_MIC_ONLY,       // Ambient microphone audio captured
        RESTRICTED_BY_OS        // OS-level telephony silence enforced (standard third-party cellular)
    }

    private final String manufacturer;
    private final String model;
    private final String androidVersion;
    private final int sdkInt;
    private final boolean hasRecordAudioPermission;
    private final boolean hasPhoneStatePermission;
    private final boolean hasCallLogPermission;
    private final boolean hasPrivilegedCapturePermission;
    private final boolean isVoipSupported;
    private final SupportLevel cellularSupportLevel;
    private final String summaryTitle;
    private final String description;

    public RecordingCapability(String manufacturer, String model, String androidVersion, int sdkInt,
                               boolean hasRecordAudioPermission, boolean hasPhoneStatePermission,
                               boolean hasCallLogPermission, boolean hasPrivilegedCapturePermission,
                               boolean isVoipSupported, SupportLevel cellularSupportLevel,
                               String summaryTitle, String description) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.androidVersion = androidVersion;
        this.sdkInt = sdkInt;
        this.hasRecordAudioPermission = hasRecordAudioPermission;
        this.hasPhoneStatePermission = hasPhoneStatePermission;
        this.hasCallLogPermission = hasCallLogPermission;
        this.hasPrivilegedCapturePermission = hasPrivilegedCapturePermission;
        this.isVoipSupported = isVoipSupported;
        this.cellularSupportLevel = cellularSupportLevel;
        this.summaryTitle = summaryTitle;
        this.description = description;
    }

    public String getManufacturer() { return manufacturer; }
    public String getModel() { return model; }
    public String getAndroidVersion() { return androidVersion; }
    public int getSdkInt() { return sdkInt; }
    public boolean hasRecordAudioPermission() { return hasRecordAudioPermission; }
    public boolean hasPhoneStatePermission() { return hasPhoneStatePermission; }
    public boolean hasCallLogPermission() { return hasCallLogPermission; }
    public boolean hasPrivilegedCapturePermission() { return hasPrivilegedCapturePermission; }
    public boolean isVoipSupported() { return isVoipSupported; }
    public SupportLevel getCellularSupportLevel() { return cellularSupportLevel; }
    public String getSummaryTitle() { return summaryTitle; }
    public String getDescription() { return description; }

    public boolean canRecordAnything() {
        return hasRecordAudioPermission;
    }
}
