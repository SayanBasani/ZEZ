# Call Recording Compatibility Matrix & OEM Analysis

## 1. Overview
Android's audio architecture does not provide a universal call recording API for third-party applications. Starting with Android 9 (Pie) and reinforced in Android 10, 11, 12, 13, and 14, access to cellular uplink/downlink audio streams (`VOICE_CALL`, `VOICE_DOWNLINK`, `VOICE_UPLINK`) is protected under the signature-level permission `android.permission.CAPTURE_AUDIO_OUTPUT`.

This document details the compatibility matrix across key manufacturers: **Samsung, Xiaomi / Redmi / POCO, OnePlus, and Vivo**.

---

## 2. Manufacturer-by-Manufacturer Breakdown

### 2.1 Samsung (One UI / Android 10–14)
* **OEM Native Dialer:** Samsung maintains its own Phone app. Native call recording is embedded at the firmware level, but is legally enabled only in select regions (e.g., India, Vietnam, Thailand, parts of Europe). In restricted regions (e.g., United States), it is disabled by CSC feature flags.
* **Third-Party App Behavior:**
  * Direct audio stream capture: **Blocked** (`SecurityException` on `VOICE_CALL`).
  * Microphone capture during cellular call (`MODE_IN_CALL`): **Silenced by AudioPolicyService** unless speakerphone is active.
  * In-App VoIP (TWING Voice): **Fully Supported (100% two-way audio)**.
* **Compatibility Status:** Native cellular two-way recording is unavailable to ordinary third-party APKs; in-app calls record completely.

---

### 2.2 Xiaomi / Redmi / POCO (MIUI / HyperOS / Android 10–14)
* **OEM Native Dialer:** Devices with the native MIUI dialer include built-in automatic call recording. Devices shipped with the Google Phone dialer enforce Google's recording policy (which plays an audible voice announcement).
* **Third-Party App Behavior:**
  * Direct audio stream capture: **Blocked** by SELinux and AudioPolicy permissions.
  * Microphone capture during cellular call: Hardware microphone is isolated to baseband; third-party recorder receives zero-byte buffers unless routed through loud speakerphone or native hooks.
  * In-App VoIP (TWING Voice): **Fully Supported (100% two-way audio)**.
* **Compatibility Status:** Cellular audio capture restricted by system; ambient/speakerphone capture possible depending on MIUI power-saving configurations.

---

### 2.3 OnePlus / Oppo / Realme (ColorOS / OxygenOS / Android 10–14)
* **OEM Native Dialer:** OnePlus previously included native call recording in OxygenOS dialer. Recent models using Google Phone or ColorOS dialers restrict background audio taps. ODialer provides native recording in supported regions.
* **Third-Party App Behavior:**
  * Direct audio stream capture: **Blocked**.
  * Microphone capture during cellular call: Background services started from broadcast receivers are muted by ColorOS process manager.
  * In-App VoIP (TWING Voice): **Fully Supported (100% two-way audio)**.
* **Compatibility Status:** Direct cellular recording restricted; VoIP recording fully functional.

---

### 2.4 Vivo / iQOO (Funtouch OS / OriginOS / Android 10–14)
* **OEM Native Dialer:** Vivo devices feature native call recording within the Vivo Phone app (can be toggled in Call Settings).
* **Third-Party App Behavior:**
  * Direct audio stream capture: **Blocked**.
  * Microphone capture during cellular call: Funtouch OS strictly enforces background battery and microphone management. Any background process opening the microphone is flagged or silenced.
  * In-App VoIP (TWING Voice): **Fully Supported (100% two-way audio)**.
* **Compatibility Status:** Third-party cellular stream capture restricted; native dialer recommended for PSTN calls.

---

## 3. Comprehensive Device Capability Matrix

| Environment / Device | Local Voice Captured | Remote Voice Captured | Audio Quality | Background Resilience | Recording Method |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **All Devices (TWING VoIP / In-App Call)** | ✅ Yes | ✅ Yes | Crystal Clear (128 kbps AAC) | ✅ High (Active Foreground Service) | Direct In-App Media Stream |
| **Samsung (Cellular Call - Normal Earpiece)** | ❌ No / Muted | ❌ No | Silent (HAL zeroed) | ⚠️ Medium | Standard MediaRecorder |
| **Samsung (Cellular Call - Speakerphone Active)**| ⚠️ Ambient Only | ⚠️ Acoustic Leak | Moderate / Room Echo | ⚠️ Medium | Hardware Mic Capture |
| **Xiaomi (Cellular Call - Normal Earpiece)** | ❌ No / Muted | ❌ No | Silent (HAL zeroed) | ⚠️ Low (MIUI Battery Killer) | Standard MediaRecorder |
| **Xiaomi (Cellular Call - Speakerphone Active)** | ⚠️ Ambient Only | ⚠️ Acoustic Leak | Moderate / Room Echo | ⚠️ Low | Hardware Mic Capture |
| **OnePlus (Cellular Call - Normal Earpiece)** | ❌ No / Muted | ❌ No | Silent (HAL zeroed) | ⚠️ Medium | Standard MediaRecorder |
| **Vivo (Cellular Call - Normal Earpiece)** | ❌ No / Muted | ❌ No | Silent (HAL zeroed) | ⚠️ Low | Standard MediaRecorder |

---

## 4. Architectural Honesty & Reporting
TWING's `RecordingCapabilityDetector` evaluates the host environment at runtime. Rather than fabricating a "successful recording" status when the underlying operating system feeds digital silence, the application explicitly identifies the active recording capability:
* **VoIP Call:** "Full Two-Way Recording (High Definition)".
* **Cellular Call (Microphone):** "Ambient / Microphone Only (OS-restricted)".
* **Unsupported State:** "Cellular Call Audio Restricted by Android OS".
