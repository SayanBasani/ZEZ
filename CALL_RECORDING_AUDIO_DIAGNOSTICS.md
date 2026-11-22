# Call Recording Audio Diagnostics & Root-Cause Technical Report

## 1. Executive Summary & Root Cause Analysis

### 1.1 The Core Problem
When an ordinary cellular call is placed or received via the native Android dialer on Android 10+ devices, third-party call recording engines (using `MediaRecorder` or `AudioRecord`) appear to start successfully:
- A container file (e.g. `.m4a` or `.mp4`) is created on disk.
- File size grows continuously (e.g. ~300 KB – 600 KB over a 15–30 second call).
- Duration advances normally (e.g. 00:28).
- **However, playing back the recording file reveals pure silence (-96.0 dB to -91.0 dB).**

### 1.2 The Root Cause
On Android 10 (API level 29) through Android 15/16/17, cellular calls transition the telephony audio subsystem to `AudioMode: MODE_IN_CALL`. Under `MODE_IN_CALL`:
1. The cellular baseband modem maintains exclusive hardware routing to the audio codec.
2. The Android audio framework's `AudioPolicyService` enforces concurrency and privacy access control on third-party user applications (`UID >= 10000`).
3. Specifically, in `AudioFlinger` / `RecordThread`:
   ```cpp
   AudioFlinger::setRecordSilenced(portId, true);
   ```
4. When `isClientSilenced` is set to `true`, `AudioFlinger`'s record thread executes `memset(buffer, 0, bytes)` on every audio buffer before delivering it to the client application.
5. The AAC encoder receives buffers of 100% zeroes. It validly packages these silent frames into the MPEG-4 container.
6. The client application is not thrown an exception; `MediaRecorder.start()` returns successfully.
7. Consequently, third-party apps produce files containing valid AAC container metadata and silent audio frames with zero acoustic energy.

---

## 2. Audio Source Investigation Matrix

We evaluated all candidate Android audio sources during active cellular calls:

| AudioSource Constant | Numeric Value | Behavior During Cellular Call (`MODE_IN_CALL`) | Resulting Acoustic Content |
| :--- | :--- | :--- | :--- |
| `MediaRecorder.AudioSource.MIC` | `1` | Prepares & starts successfully. OS immediately triggers `isClientSilenced=true`. Buffers zeroed. | 100% Silent PCM (0.0 RMS, 0 peak) |
| `MediaRecorder.AudioSource.DEFAULT` | `0` | Defaults to `MIC`. OS triggers `isClientSilenced=true`. | 100% Silent PCM (0.0 RMS, 0 peak) |
| `MediaRecorder.AudioSource.VOICE_COMMUNICATION` | `7` | Intended for VoIP (AEC/NS). On cellular calls, OS prioritizes baseband and silences non-system client. | 100% Silent PCM (0.0 RMS, 0 peak) |
| `MediaRecorder.AudioSource.VOICE_RECOGNITION` | `6` | Silenced or pre-empted by `AudioPolicyService` concurrency policy during `MODE_IN_CALL`. | 100% Silent PCM |
| `MediaRecorder.AudioSource.VOICE_CALL` | `4` | Deprecated / permission blocked. Throws `java.lang.SecurityException` or fails `prepare()` on non-system apps. | Fails immediately |
| `MediaRecorder.AudioSource.VOICE_UPLINK` | `2` | Throws `SecurityException` without platform signature permission. | Fails immediately |
| `MediaRecorder.AudioSource.VOICE_DOWNLINK` | `3` | Throws `SecurityException` without platform signature permission. | Fails immediately |

**Key Finding:** Changing the `AudioSource` parameter does **not** bypass the operating system's `AudioPolicyService` silencing mechanism.

---

## 3. Real Audio Validation & Diagnostic Architecture (v1.0.30)

To eliminate false "successful" recordings, TWING v1.0.30 implements a multi-tier diagnostic validation engine:

### 3.1 `AudioManager.AudioRecordingCallback` (API 29+)
Before starting capture, `RecordingEngine` registers an `AudioRecordingCallback`:
```java
audioManager.registerAudioRecordingCallback(new AudioManager.AudioRecordingCallback() {
    @Override
    public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {
        for (AudioRecordingConfiguration config : configs) {
            boolean silenced = config.isClientSilenced();
            if (silenced) {
                diagnostics.setCurrentState(RecordingState.AUDIO_CAPTURE_SILENCED);
                diagnostics.setSilenceReason("Silenced by Android AudioPolicyService (isClientSilenced=true)");
            }
        }
    }
}, monitorHandler);
```
- **Diagnostic output captured on emulator/device:**
  ```
  11-22 11:36:48.805 D RecordingEngine: Registered AudioManager.AudioRecordingCallback
  11-22 11:36:49.166 W RecordingEngine: AudioRecordingConfiguration: isClientSilenced=TRUE -> Muted by Android OS
  ```

### 3.2 Real-Time Peak & Silence Monitor
- Background thread polls `mediaRecorder.getMaxAmplitude()` every 500ms.
- Tracks silence duration and peak acoustic energy.
- Requires genuine acoustic amplitude (`amplitude > 250`) and `!isClientSilenced` before transitioning to `AUDIO_CAPTURE_CONFIRMED`.

### 3.3 Post-Recording Frame Decoded Sample Validation (`AudioCaptureValidator`)
Before saving the record to the database, `AudioCaptureValidator` decodes the written `.m4a` file using `MediaExtractor` and `MediaCodec`:
- Decodes up to 50 PCM audio frames (51,200 samples).
- Computes actual peak amplitude, RMS level in dB, and non-zero sample count.
- **Verification log on real test:**
  ```
  AudioCaptureValidator: Audio file sample inspection complete for /.../call_1795327608708_198.m4a -> Peak: 0, NonZero: 0/51200, RMS: -96.0 dB
  Recording finalized with status SILENCED: OS capture policy muted third-party mic.
  ```

### 3.4 Elimination of False Recordings
- The database schema now stores `status` (`COMPLETED`, `SILENCED`, `FAILED`), `silence_reason`, and `peak_amplitude`.
- Silenced recordings are clearly flagged with `⚠️ Audio capture silenced by Android OS` in the UI.
- Tapping **Why?** reveals the complete diagnostic report explaining Android's security policy.

---

## 4. OEM & Manufacturer-Specific Findings

### 4.1 Samsung (One UI 4 / 5 / 6, Android 12–14)
- **Built-in Recording:** Exclusive to the Samsung Dialer (`com.samsung.android.incallui`). Uses proprietary Samsung framework APIs (`SecProductFeature_VOIP`, Samsung SecAudio HAL).
- **Third-Party APKs:** The Samsung audio HAL routes the physical microphone directly to the CP (Cellular Processor) modem. `AudioPolicyManager::isStreamActive(AUDIO_STREAM_VOICE_CALL)` marks all non-system recording sessions as silenced.
- **Legitimate Public SDK:** None exists for standard third-party consumer apps.

### 4.2 Xiaomi / Redmi / POCO (MIUI 13 / 14 & HyperOS, Android 12–14)
- **Built-in Recording:** Handled by system-signed `com.android.phone` / `com.android.incallui`.
- **Third-Party APKs:** MIUI's `SecurityService` and Android `AudioPolicyService` enforce background capture muting during cellular calls.
- **Legitimate Public SDK:** None exists for third-party APKs without platform ROM integration.

### 4.3 OnePlus / OPPO / Realme (OxygenOS / ColorOS 12 / 13 / 14)
- **Built-in Recording:** Handled by regional ODialer or Google Dialer with system privileges.
- **Third-Party APKs:** `oppo-audioserver` and `AudioPolicyService` silence third-party mic capture during phone calls.
- **Legitimate Public SDK:** None exists for standard apps.

### 4.4 Vivo / iQOO (Funtouch OS / OriginOS, Android 12–14)
- **Built-in Recording:** Handled by system-privileged `com.vivo.callrecorder` (`sharedUserId="android.uid.system"`).
- **Third-Party APKs:** Funtouch OS blocks non-system apps from accessing the microphone during telephony calls.
- **Legitimate Public SDK:** None exists for standard apps.

---

## 5. Summary of Supported vs Unsupported Configurations

| Use Case | Status | Real Audio Captured | Validation State |
| :--- | :--- | :--- | :--- |
| **TWING In-App Voice Calls (VoIP / WebRTC)** | ✅ **Fully Supported** | Both remote and local participants in high-definition AAC | `AUDIO_CAPTURE_CONFIRMED` |
| **Local Microphone (Idle / Dictation / In-App)** | ✅ **Fully Supported** | High-fidelity local voice recording | `AUDIO_CAPTURE_CONFIRMED` |
| **PSTN Cellular Call (Third-Party APK)** | ❌ **Silenced by OS** | Zeroed PCM buffers injected by Android OS `AudioPolicyService` | `AUDIO_CAPTURE_SILENCED` |
| **PSTN Cellular Call (System / Platform Signed)** | ⚙️ **Firmware Only** | Both sides captured via baseband audio hardware route | Requires `CAPTURE_AUDIO_OUTPUT` |

---

## 6. Technical Recommendation & Conclusion

1. **Honest User Communication:**
   Do not mislead users by showing fake successful recordings with dead silence. TWING v1.0.30 honestly identifies when Android OS silences cellular audio, displays the exact diagnostic cause, and suggests using TWING's high-definition in-app voice calling.
2. **Real Audio Validation:**
   With `AudioRecordingCallback`, continuous peak amplitude monitoring, and post-capture PCM frame decoding, TWING guarantees that no recording is ever labeled `AUDIO_CAPTURE_CONFIRMED` unless actual non-zero acoustic samples have been captured.
