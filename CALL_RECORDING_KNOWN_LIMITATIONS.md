# Call Recording Known Limitations & Technical Explanations

## 1. System-Level Architectural Limitations

### 1.1 The `CAPTURE_AUDIO_OUTPUT` Security Boundary
To capture digital audio directly from the cellular baseband uplink and downlink, Android requires the permission:
```xml
<uses-permission android:name="android.permission.CAPTURE_AUDIO_OUTPUT" />
```
* **Protection Level:** `signature|privileged`.
* **Constraint:** This permission can only be granted to applications installed in the system image (`/system/priv-app`) and signed with the OEM's platform key. Standard consumer APKs installed via package manager or sideloading cannot be granted this permission by the Android OS.

### 1.2 Android `AudioPolicyService` & `AudioFlinger` Silencing
When a cellular call is active, Android transitions to `AUDIO_MODE_IN_CALL`.
Under this mode:
1. The cellular modem hardware (RIL) maintains a dedicated hardware audio path to the earpiece/speaker and microphone.
2. The Android audio framework executes `AudioPolicyService::setRecordSilenced(portId, true)` on all non-privileged clients attempting to record from the hardware microphone or any audio source.
3. `AudioFlinger` overwrites the application's audio buffer with blocks of zeroes (`0x0000`), yielding digital silence (`-91.0 dB`).

### 1.3 Android 11+ "While-In-Use" Microphone Restrictions
Starting in Android 11 (API level 30):
* A foreground service launched from the background (e.g. from a `BroadcastReceiver` responding to `ACTION_PHONE_STATE_CHANGED`) is strictly prohibited from accessing the microphone.
* Even with `foregroundServiceType="microphone"`, the microphone stream is silenced unless the app had a visible Activity in the foreground at the time the recording session was initiated.

### 1.4 Google Play Policy Regarding Accessibility Services
Historically, several utilities (e.g., Cube ACR, Boldbeast) used Android's Accessibility API (`AccessibilityService`) to detect call state and capture software audio loops.
* **May 2022 Policy Update:** Google updated Google Play Developer Program policies to explicitly ban using the Accessibility API for remote call audio recording.
* As a production application respecting platform policies, TWING does not employ deceptive or policy-violating accessibility exploits.

---

## 2. Summary for End Users & Operators

| Feature Goal | Feasibility | Technical Reason |
| :--- | :--- | :--- |
| **In-App VoIP Calls (TWING Voice)** | ✅ **100% Fully Supported** | App manages the WebRTC/Twilio audio stream; no OS-level cellular restriction applies. |
| **Local Voice / Mic Capture (Idle or In-App)** | ✅ **100% Fully Supported** | Standard microphone permissions (`RECORD_AUDIO`) are respected. |
| **PSTN Cellular Call (Direct 2-Way Stream)** | ❌ **Restricted by Android OS** | Requires `CAPTURE_AUDIO_OUTPUT` (system/privileged permission). |
| **PSTN Cellular Call (Ambient via Speakerphone)**| ⚠️ **Acoustically Limited** | Captures only sound physically reaching the bottom microphone; remote audio quality depends on phone volume. |
