# Call Recording Subsystem Architecture

## 1. Overview
The Call Recording Subsystem for TWING (`com.zez.app`) is an isolated, modular component designed to manage call recording operations while respecting Android OS boundaries and manufacturer policies.

```
                      ┌─────────────────────────────────────────┐
                      │          MainActivity / Settings        │
                      └────────────────────┬────────────────────┘
                                           │
                                           ▼
                      ┌─────────────────────────────────────────┐
                      │          CallRecordingManager           │
                      │               (Facade)                  │
                      └───┬───────────┬─────────────┬─────────┬─┘
                          │           │             │         │
          ┌───────────────┘           │             │         └──────────────┐
          ▼                           ▼             ▼                        ▼
┌──────────────────┐        ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ CallStateMonitor │        │CapabilityDetector│  │RecordingStorage  │  │NotificationMgr   │
└─────────┬────────┘        └──────────────────┘  └──────────────────┘  └──────────────────┘
          │                           │                     │
          ▼                           ▼                     ▼
┌──────────────────┐        ┌──────────────────┐  ┌──────────────────┐
│ RecordingEngine  │◄───────┤PermissionManager │  │DatabaseManager   │
└──────────────────┘        └──────────────────┘  └──────────────────┘
```

---

## 2. Core Components & Responsibilities

### 2.1 `CallRecordingManager` (Main Coordinator)
- **Role:** Central facade orchestrating the entire lifecycle of call recording.
- **Responsibilities:**
  - Coordinates state transitions (start, pause, resume, stop).
  - Queries `RecordingCapabilityDetector` before initiating recording.
  - Verifies user consent and permissions via `RecordingPermissionManager`.
  - Dispatches recording requests to `RecordingEngine`.
  - Finalizes audio files via `RecordingStorageManager` and records metadata via `RecordingDatabaseManager`.
  - Dispatches UI and foreground notifications via `RecordingNotificationManager`.

### 2.2 `CallStateMonitor` (Telephony State Detector)
- **Role:** Tracks telephony states (`RINGING`, `OFFHOOK`, `IDLE`).
- **Implementation Strategy:**
  - Uses `TelephonyCallback` on Android 12+ (API 31+) and `PhoneStateListener` on API 26–30.
  - Receives broadcast intents from `CallStateReceiver` as an external wake-up trigger.
  - Implements debounce logic and session UUID tracking to prevent duplicate recording triggers.
  - Detects missed or rejected calls (transitioning from `RINGING` directly to `IDLE`) without triggering recording.

### 2.3 `RecordingCapabilityDetector` (Device & Policy Inspector)
- **Role:** Dynamically inspects the host hardware, Android version, and manufacturer layer.
- **Checks:**
  - `Build.MANUFACTURER`, `Build.MODEL`, `Build.VERSION.SDK_INT`.
  - Checks if `CAPTURE_AUDIO_OUTPUT` is granted (almost never on non-system apps).
  - Identifies OEM specifics (Samsung OneUI, Xiaomi MIUI/HyperOS, OnePlus ColorOS/OxygenOS, Vivo FuntouchOS).
  - Determines whether standard microphone capture, hardware call streaming, or in-app VoIP capture is viable.
  - Returns a detailed `RecordingCapability` object containing capabilities, warnings, and honest user-facing explanations.

### 2.4 `RecordingEngine` (Audio Capture Engine)
- **Role:** Handles audio capture using standard Android media frameworks (`MediaRecorder`).
- **Audio Configuration:**
  - Output Format: `MediaRecorder.OutputFormat.MPEG_4`.
  - Audio Encoder: `MediaRecorder.AudioEncoder.AAC`.
  - Bitrate: 128 kbps, Sampling Rate: 44.1 kHz.
  - Manages audio source selection (`MIC`, `DEFAULT`, `VOICE_COMMUNICATION`).
  - Catches `RuntimeException` on premature `stop()` (e.g. calls shorter than 1 second).

### 2.5 `RecordingStorageManager` (Storage & File Integrity)
- **Role:** Manages application-private directory files (`context.getFilesDir()/CallRecordings`).
- **Features:**
  - Generates collision-proof file names (`rec_TIMESTAMP_PHONE.m4a`).
  - Verifies file integrity after recording; purges empty or corrupt files (< 100 bytes).
  - Computes total disk space utilized by call recordings.
  - Safely deletes files when requested by the user.

### 2.6 `RecordingDatabaseManager` (Metadata Persistence)
- **Role:** Persists recording records to SQLite (`twing_call_recordings.db`).
- **Features:**
  - Maintains records with fields: `id`, `phone_number`, `contact_name`, `call_type`, `direction`, `timestamp`, `duration_seconds`, `file_path`, `file_size`, `cloud_url`, `is_synced`.
  - Resolves contact names and phone numbers asynchronously from system CallLog if initially unknown.

### 2.7 `RecordingNotificationManager` (User Indicators & Compliance)
- **Role:** Manages notification channels and user notifications.
- **Channels:**
  - `twing_call_recording_channel`: Low importance ongoing notification while recording is in progress.
  - Ensures transparency: Displays active recording status with contact/number.

### 2.8 `RecordingPermissionManager` (Consent & Runtime Checks)
- **Role:** Manages runtime permissions and user opt-in consent.
- **Checks:** `RECORD_AUDIO`, `READ_PHONE_STATE`, `READ_CALL_LOG`, `POST_NOTIFICATIONS`.
- **Consent:** Tracks `call_recording_opt_in` in SharedPreferences. Recording never begins unless user has opted in.
