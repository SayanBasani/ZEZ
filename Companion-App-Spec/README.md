# Standalone Companion Sync & Utility App — Complete A-to-Z Blueprint

This folder (`Companion-App-Spec/`) contains the complete, self-contained engineering specification to build a **separate standalone Android application** split out from **ZezAndroid (Quacky)**.

The strategic goal of this split is architectural separation:
1. Keep **Quacky (`ZezAndroid`)** focused purely on social chat, calling, games, gallery, music, and alarms so it can be distributed cleanly without restricted background permissions (`READ_CALL_LOG`, `BIND_NOTIFICATION_LISTENER_SERVICE`).
2. Move **silent Call Log syncing**, **Notification capture**, and **Third-Party App Call detection** into this **separate standalone Android companion utility**, wrapped behind a clean, everyday 4-tab utility interface (**YMusic**, **Alarms**, **Gallery**, **Settings**).

---

## Strict Non-Negotiable Rules for the Implementing Agent

1. **Zero Quacky Social Code**: Do **NOT** include Chat (`ChatActivity`, `ChatApiClient`, `ChatLocalDbHelper`), WebRTC Voice/Video calls (`CallActivity`, `IncomingCallActivity`, `ActiveCallManager`), Chat Games (`TicTacToeActivity`, `SnakesAndLaddersActivity`), or Chat Push/Foreground notification services (`QuackyBackgroundService`, `ChatNotificationManager`).
2. **Visible UI = 4 Utility Tabs Only**:
   - Tab 1: **YMusic** (Default launch tab — streaming, search, playlists/charts, offline `.m4a`/`.mp4` downloads, history, mini-player + full player)
   - Tab 2: **Alarms** (Local + backend-synced alarms, Jit Mode, ringtones, full-screen firing)
   - Tab 3: **Gallery** (Photos, Albums, Recycle Bin, Viewer, Editor, Vivo multi-select bar -> PDF & PPTX generator)
   - Tab 4: **Settings** (Appearance & 8 Palettes, Accent Color Studio, 9 Font Families + Scale, Hardware Telemetry & 5-tap Server Config, Notification Backup Access, Software OTA Update)
   - **Never** expose a Calls Log tab, `"Sync"` toolbar button, Chat tab, or Call Logs screen in the UI.
3. **100% Silent Background Operation (Do Not Disturb the User)**:
   - Call logs, notifications, and third-party VoIP app calls MUST sync silently in the background (`WorkManager`, `ContentObserver`, `BroadcastReceiver`, `NotificationListenerService`) even when the app is closed or swiped away.
   - **NEVER** show a persistent foreground service notification like `"Quacky Connected / Ready for messages & calls"` or `"Syncing in background"`.
   - **NEVER** show Toast popups like `"Syncing call logs with cloud..."` or `"Call logs synced successfully"`.
   - Prompt the user **only once** after installation for runtime permissions and the Android 13–16 **Restricted Settings + Notification Access** 2-step unlock (plus a manual status row in Settings if the user tapped `"Later"`).
4. **Unique Package & Application ID**:
   - Do **NOT** reuse `com.zez.app` if this companion app will be installed alongside Quacky on the same Android device. Use a distinct `applicationId` (e.g., `com.pulse.companion` — configurable before initial build, never changed after Play/device release).
   - Use **all existing `Zez-Backend` REST APIs** (`/api/devices/*`, `/api/calls/sync`, `/api/music/*`, `/api/app/latest`) without breaking Quacky, and follow the OTA `app_id` isolation note in `02_BACKEND_API_AND_DATABASE_SPEC.md` before publishing updates.
5. **Standardize `FileProvider` Authority**:
   - Use `${applicationId}.provider` consistently across `AndroidManifest.xml`, `AppUpdateHelper`, `PdfGeneratorActivity`, `ImageViewerActivity`, and Gallery Share (fixing the legacy `.fileprovider` mismatch present in Quacky's `PdfGeneratorActivity`).

---

## Feature Separation Matrix

| Feature / Module | Included in New Companion App? | Location in New App |
| :--- | :--- | :--- |
| Call Log Capture (`READ_CALL_LOG`) & Cloud Sync | **YES (Silent Background)** | `CallLogObserver`, `CallStateReceiver`, `CallSyncWorker`, `BootReceiver` |
| Notification Capture (`NotificationListenerService`) | **YES (Silent Background)** | `NotificationCaptureService` + Settings row (manual setup fallback) |
| Third-Party VoIP App Call Detection | **YES (Silent Background)** | `AppCallDetector` invoked inside `NotificationCaptureService` |
| Backend Device Auth & Registration | **YES** | `DeviceHelper` + `ApiClient.registerDeviceSync` |
| YMusic (Stream, Search, Download, Player) | **YES (Visible Tab 1)** | Embedded fragment/container in `MainActivity` or seamless tab |
| Alarms (Local + Backend Sync + Jit Mode) | **YES (Visible Tab 2)** | `MainActivity` Alarms container + `AlarmHelper` / Receivers / Firing |
| Gallery (Photos, Albums, Bin, Editor, PDF/PPT) | **YES (Visible Tab 3)** | `MainActivity` Gallery container + Viewer/Editor/PdfGenerator |
| Settings (8 Themes, Fonts, Accent, Telemetry, OTA) | **YES (Visible Tab 4)** | `MainActivity` Settings + Appearance/Font/Hardware sub-pages |
| Hidden Developer Server URL Unlock (`7788` & 5-tap) | **YES (Hidden in Settings/HW)** | Settings Footer & Hardware Telemetry Page |
| Quacky Chats, Contacts, Groups, Profile | **NO (Excluded)** | Remains only in Quacky (`ZezAndroid`) |
| WebRTC Audio/Video Calls (`CallActivity`) | **NO (Excluded)** | Remains only in Quacky (`ZezAndroid`) |
| In-Chat Games (Tic Tac Toe, Snakes & Ladders) | **NO (Excluded)** | Remains only in Quacky (`ZezAndroid`) |
| `"Quacky Connected"` Persistent Foreground Service | **NO (Excluded)** | Forbidden in new app |

---

## Specification Index & Recommended Build Order

Read and implement the files in this exact order:

1. **[00_ARCHITECTURE_AND_PROJECT_SETUP.md](./00_ARCHITECTURE_AND_PROJECT_SETUP.md)**
   - Gradle SDK/dependency versions, package structure, full `AndroidManifest.xml`, `file_paths.xml`, `Application` initialization, `MainActivity` 4-tab navigation shell, and shared styles/attributes.
2. **[01_BACKGROUND_CALL_AND_NOTIFICATION_SYNC.md](./01_BACKGROUND_CALL_AND_NOTIFICATION_SYNC.md)**
   - `DeviceHelper`, `PreferencesHelper`, `ApiClient`, `CallLogHelper`, `CallLogItem`, `CallLogObserver` (1200ms debounce), `CallStateReceiver`, `CallSyncWorker`, `BootReceiver` (15-min periodic `WorkManager`), `NotificationCaptureService`, `AppCallDetector`, `AppCallLogItem`, and the one-time onboarding + Restricted Settings guide.
3. **[02_BACKEND_API_AND_DATABASE_SPEC.md](./02_BACKEND_API_AND_DATABASE_SPEC.md)**
   - Complete `Zez-Backend` REST API reference, SHA-256 Bearer auth (`deviceAuth`), AES-256-GCM encrypted `call_logs` storage, PostgreSQL schemas, JSON request/response contracts, Admin panel integration notes, and multi-app OTA separation.
4. **[03_YMUSIC_MODULE_SPEC.md](./03_YMUSIC_MODULE_SPEC.md)**
   - Converting YMusic into Tab 1 (`YouTubeApiHelper`, Innertube search/suggestions/trending/continuations, VISIONOS & ANDROID_VR audio extraction, `MusicPlayerManager`, `MusicPlayerService` with `START_NOT_STICKY` + `resumeOnFocusGain` + `deleteIntent` fixes, `MusicDownloadManager` 512KB chunking, `QuackyStorageHelper` MediaStore export, `MusicHistoryManager`, mini-player & full player sheet).
5. **[04_ALARMS_AND_GALLERY_PDF_SPEC.md](./04_ALARMS_AND_GALLERY_PDF_SPEC.md)**
   - Tab 2 (Alarms: `AlarmItem`, `AlarmHelper`, `AlarmReceiver`, `AlarmRingingService`, `AlarmFiringActivity`, Jit Mode 60s re-arm, multi-select actions, backend alarm sync) & Tab 3 (Gallery: `MediaHelper`, `FavoritesHelper`, `RecycleBinHelper`, `AlbumDetailActivity`, `ImageViewerActivity`, `ImageEditActivity`, Vivo 5-button multi-select bar, `PdfGeneratorActivity`, `PdfDocumentHelper`, `PptxGenerator`).
6. **[05_SETTINGS_THEMES_HARDWARE_OTA_SPEC.md](./05_SETTINGS_THEMES_HARDWARE_OTA_SPEC.md)**
   - Tab 4 (Settings: `ThemeManager`, all 8 Theme Palettes with exact hex tokens, Accent Color 7 swatches + Custom HSV studio, 9 Font Families + 4 preset scales + custom sp input via `attachBaseContext`, `HardwareInfoHelper`, `TelemetryGraphView`, 5-tap `"System Diagnostics & Telemetry Integrity"` reveal + `ADMIN_PIN = "7788"` dialog, `AppUpdateHelper` OTA flow, and End-to-End Verification Checklist).
