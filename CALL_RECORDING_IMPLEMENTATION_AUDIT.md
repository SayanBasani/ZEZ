# Call Recording Implementation Audit

**Date:** 2026-11-22  
**Codebase:** TWING Android App (`com.zez.app`)  
**Target:** Safe, production-oriented integration of an isolated Call Recording Subsystem.

---

## 1. Existing Architecture & Baseline

### 1.1 Project Structure & Build Configuration
* **Language & Framework:** Native Android (Java 17, AndroidX).
* **Build System:** Gradle (Android Gradle Plugin 8.3+).
* **SDK Versions:**
  * `compileSdk`: 34 (Android 14)
  * `targetSdk`: 30 (Kept at 30 to maintain compatibility with background services)
  * `minSdk`: 26 (Android 8.0 Oreo)
* **Architecture:** Component-based Android architecture utilizing Services, BroadcastReceivers, SQLiteOpenHelper for local persistence, WorkManager for background sync, and ViewBinding.

### 1.2 Core Functional Modules
1. **Calling & Dialpad:** Native dialer integration, call log reading/synchronization (`CallLogHelper`, `CallSyncWorker`), and in-app WebRTC/Twilio VoIP calling (`CallActivity`).
2. **Call Recordings:** Existing lightweight recorder (`CallRecordManager`, `CallRecordService`, `CallRecordDbHelper`, `CallRecordAdapter`).
3. **Messaging / Chat:** Real-time chat with attachment uploads, local caching (`ChatLocalDbHelper`), and push notifications.
4. **Gallery & Media:** Local photo/video grid, album viewing, image editing, and recycle bin management.
5. **Alarms:** Exact alarms with full-screen firing activities and ringing foreground services.
6. **Telemetry & Settings:** Hardware telemetry graphing, accent color customization, and cloud backup.

---

## 2. Inventory of Existing Call Recording Files

| File Path | Current Role | Audit Finding & Integration Plan |
| :--- | :--- | :--- |
| `com/zez/app/service/CallRecordService.java` | Foreground service started on `OFFHOOK` to run recording. | Currently coupled to `CallRecordManager`. Will be updated with minimal changes to delegate to the new modular recording architecture. |
| `com/zez/app/receiver/CallStateReceiver.java` | BroadcastReceiver for telephony state (`EXTRA_STATE_OFFHOOK`, `IDLE`, `RINGING`). | Handles initial broadcast triggers. Will cooperate with `CallStateMonitor` to eliminate duplicate sessions and detect accurate state transitions. |
| `com/zez/app/util/CallRecordManager.java` | Singleton managing `MediaRecorder` lifecycle. | Will serve as a backward-compatible adapter delegating to the new `CallRecordingManager`. |
| `com/zez/app/util/CallRecordDbHelper.java` | SQLite helper managing `call_recordings` table. | Schema is stable; will support recording metadata persistence without dropping existing records. |
| `com/zez/app/model/CallRecordItem.java` | Data model for recording items. | Represents recording metadata. Retained for full backward compatibility with UI adapters. |
| `com/zez/app/adapter/CallRecordAdapter.java` | RecyclerView adapter for recordings list with playback, share, and delete. | Fully functional UI adapter. Retained and augmented with recording source/capability badges. |

---

## 3. Untouchable Files & Subsystems

To strictly comply with the Codebase Protection Rules, the following subsystems **must not be modified**:

1. **Chat & Messaging Subsystem:**
   * `ChatActivity.java`, `ChatApiClient.java`, `ChatLocalDbHelper.java`, `ChatMessageAdapter.java`, `ChatNotificationHelper.java`, `ChatNotificationManager.java`, `ChatPreferencesHelper.java`, `ConversationsAdapter.java`, and all associated XML layouts.
2. **Gallery & Media Subsystem:**
   * `ImageViewerActivity.java`, `AlbumDetailActivity.java`, `ImageEditActivity.java`, `GalleryAdapter.java`, `AlbumsAdapter.java`, `FilmstripAdapter.java`, `MediaPagerAdapter.java`, `RecycleBinAdapter.java`, `MediaHelper.java`, `ImageEditHelper.java`, `RecycleBinHelper.java`, `FavoritesHelper.java`.
3. **Alarm Subsystem:**
   * `AlarmFiringActivity.java`, `AlarmRingingService.java`, `AlarmReceiver.java`, `AlarmHelper.java`, `AlarmAdapter.java`, `AlarmItem.java`.
4. **Contacts Subsystem:**
   * `ContactsActivity.java`, `ContactsAdapter.java`.
5. **Backend Server & APIs:**
   * Entire `Zez-Backend/` directory, database schemas, and endpoints.

---

## 4. Android Security & Hardware Restrictions Analysis

Under Android 10, 11, 12, 13, and 14:
1. **`MODE_IN_CALL` Isolation:** Standard third-party applications attempting to capture cellular call streams via `AudioSource.VOICE_CALL` are rejected with `SecurityException` (`android.permission.CAPTURE_AUDIO_OUTPUT` required, which is a privileged signature permission).
2. **HAL Silence Buffer Zeroing:** When `MODE_IN_CALL` is active, Android's `AudioPolicyService` signals `AudioFlinger::setRecordSilenced(portId, true)` on non-privileged recording streams.
3. **Android 11+ While-In-Use Rule:** Background-started foreground services are blocked from accessing the microphone unless an activity was active in the foreground or an Accessibility service was explicitly active.
4. **Manufacturer Diversity:**
   * **Samsung:** Disables third-party call recording APIs on Exynos/Snapdragon firmware in most regions; native recording is embedded in Samsung Phone app only.
   * **Xiaomi / Redmi / POCO:** MIUI / HyperOS provides native call recording hooks on global dialers, but restricts standard third-party mic capture during cellular calls.
   * **OnePlus / Oppo / Realme:** ColorOS / OxygenOS isolates baseband voice streams.
   * **Vivo / iQOO:** FuntouchOS / OriginOS isolates telephony streams.

---

## 5. Proposed Modular Integration Strategy

All new logic will be placed in a dedicated, isolated package:
`com.zez.app.recording.*`

```
com.zez.app.recording/
├── CallRecordingManager.java        (Central coordinator)
├── CallStateMonitor.java            (State detection, debounce, and duplicate prevention)
├── RecordingCapabilityDetector.java (Hardware, OEM, and API level capability detector)
├── RecordingEngine.java             (Safe, honest recording engine)
├── RecordingStorageManager.java     (Application-private sandbox storage and file operations)
├── RecordingDatabaseManager.java    (Metadata persistence and DB transactions)
├── RecordingNotificationManager.java(User notifications, start/stop alerts, ongoing status)
├── RecordingPermissionManager.java  (Permission checks, runtime requests, and user consent)
└── model/
    ├── RecordingCapability.java     (Capability inspection result)
    └── RecordingMetadata.java       (Rich recording metadata)
```

### Integration Points:
1. **`CallStateReceiver.java`:** Minimal update to delegate call events to `CallStateMonitor`.
2. **`CallRecordService.java`:** Minimal update to delegate service execution to `CallRecordingManager`.
3. **`MainActivity.java`:** Display honest device capability status and recording settings without altering navigation or UI layout.
4. **`CallRecordManager.java`:** Refactored into a facade wrapping `CallRecordingManager` so all existing UI and VoIP bindings continue to operate seamlessly.
