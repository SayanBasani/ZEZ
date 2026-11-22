package com.zez.app.recording;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class RecordingPermissionManager {
    private static final String PREF_NAME = "call_recording_prefs";
    private static final String KEY_AUTO_RECORD = "auto_record_calls";
    private static final String KEY_CONSENT_ACKNOWLEDGED = "recording_consent_acknowledged";

    private final Context context;
    private final SharedPreferences prefs;

    public RecordingPermissionManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isAutoRecordEnabled() {
        return prefs.getBoolean(KEY_AUTO_RECORD, true);
    }

    public void setAutoRecordEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_RECORD, enabled).apply();
    }

    public boolean isConsentAcknowledged() {
        return prefs.getBoolean(KEY_CONSENT_ACKNOWLEDGED, true);
    }

    public void setConsentAcknowledged(boolean acknowledged) {
        prefs.edit().putBoolean(KEY_CONSENT_ACKNOWLEDGED, acknowledged).apply();
    }

    public boolean hasRecordAudioPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasPhoneStatePermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasCallLogPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public boolean isReadyToRecord() {
        return isAutoRecordEnabled() && isConsentAcknowledged() && hasRecordAudioPermission();
    }
}
