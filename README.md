# Quacky (`ZezAndroid` + `Zez-Backend`)

A native Java Android communication, media, and device-telemetry application paired with a Node.js (Express 5), PostgreSQL, and Backblaze B2 backend suite.

For the complete architecture, API endpoints, 20-table database schema, permissions/restore guide, security notes, and call-recording postmortem, see **[PROJECT_DOCUMENTATION.md](file:///D:/Call%20Log%20Mannager/PROJECT_DOCUMENTATION.md)**.

---

## Core Capabilities

- **Android Client (`ZezAndroid`, `com.zez.app`)**: Pure native Java 17 Android app (`minSdk 26`, `targetSdk 34`, `compileSdk 34`) with 5 primary bottom-navigation tabs (`Chats`, `Calls`, `Alarms`, `Gallery`, `Settings`), plus sub-apps:
  - **Encrypted Chat**: 1-on-1 messaging with Normal & Secret (client-ephemeral session) modes, emoji reactions, markdown rendering, reply/delete, multi-image `GalleryPopupHelper`, `QuickCameraActivity`, links/docs viewer, and Backblaze B2 media sharing.
  - **In-App VoIP Calling**: Voice & video calls (`CallActivity`, `IncomingCallActivity`, `ActiveCallManager`).
  - **Call History & Background Sync**: Full device call-log retrieval (`CallLogHelper.getAllCallLogs`) synced in 100-record batches via `WorkManager` (`CallSyncWorker`) and `CallLogObserver` with AES-256-GCM at-rest encryption.
  - **Alarms**: Local & admin-synced alarms (`AlarmHelper`, `AlarmRingingService`, `AlarmFiringActivity`) with custom device ringtones and multi-select delete.
  - **Gallery & Recycle Bin**: Photos/Albums browser (`view_gallery.xml`), pinch-to-zoom `ImageViewerActivity`, `ImageEditActivity`, favorites, `RecycleBinHelper`, and `ManageStorageActivity`.
  - **YMusic Player**: YouTube search/stream/download player (`MusicActivity`, `MusicPlayerService` foreground media playback) backed by `/api/music/*`.
  - **Multiplayer Mini-Games**: In-chat **Tic Tac Toe** and **Snakes & Ladders** (`SnakesAndLaddersActivity`, server-authoritative move engine).
  - **Settings & Hardware Telemetry**: 8 theme palettes (`ThemeManager`), 7 accent colors, 9 font families & font scaling, CPU/RAM/storage telemetry graphs (`TelemetryGraphView`), and in-app OTA updater (`AppUpdateHelper`).
- **Backend & Admin Portal (`Zez-Backend`)**: Express 5 REST API with PostgreSQL persistence, Backblaze B2 cloud object storage (`b2StorageService.js`), read-through disk cache (`src/cache/storage/`), in-app APK release distribution (`/api/app/latest`), and the `/admin` web portal.

---

## Architecture Overview

```text
+--------------------------------------------+
|         Quacky Android (com.zez.app)       |
|  Chats | Calls | Alarms | Gallery |Settings|
|  YMusic | TicTacToe | Snakes & Ladders     |
+---------------------+----------------------+
                      | HTTPS / REST (OkHttp + Gson)
                      v
+--------------------------------------------+
|         Zez-Backend (Express 5 / Node >=20)|
|  Device & Admin Auth | AES-256-GCM Crypto  |
|  /api/{devices,calls,chat,call,storage,    |
|        app,music,admin,games/snakes-...}   |
+----------+------------------------+--------+
           |                        |
           v                        v
+--------------------+   +-------------------+
|   PostgreSQL (20   |   |   Backblaze B2    |
|  tables in public) |   |  (+ local cache)  |
+--------------------+   +-------------------+
           ^
           | HMAC-SHA256 Session Auth
+----------+----------+
| Admin Web Portal    |
| GET /admin          |
+---------------------+
```

---

## Project Layout

```text
D:\Call Log Mannager\
├── README.md                              # Project overview and quick-start guide
├── PROJECT_DOCUMENTATION.md               # Consolidated technical & operational reference
├── App Icon/
│   └── Quacky Logo.png                    # Application branding asset
├── scripts/                               # Automation & E2E verification helpers
│   ├── run-backend.ps1                    # Validate port 3000 & launch Zez-Backend
│   ├── run-android.ps1                    # Start emulator, build, install & launch APK
│   ├── start-emulator.ps1                 # Launch AVD emulator standalone
│   ├── reset-emulator.ps1                 # Clear stale locks & wipe emulator data
│   ├── configure_emulators.py             # Configure dual-emulator environment
│   ├── e2e_device_test.py                 # End-to-end device test harness
│   ├── test_offline_retry.py              # Offline retry synchronization test
│   └── test_secret_mode_sync.py           # Secret chat mode peer-sync test
├── Zez-Backend/                           # Node.js Express 5 API & Admin Portal
│   ├── .env.example                       # Environment template (DB, keys, B2, admin)
│   ├── package.json
│   ├── tsconfig.json                      # allowJs TypeScript check configuration
│   ├── games/snakes-and-ladders/          # Modular Snakes & Ladders engine + tests
│   ├── scripts/                           # Canonical release & DB/maintenance utilities
│   ├── src/
│   │   ├── server.js                      # Express entry point & route wiring
│   │   ├── db.js                          # pg Pool & schema initialization
│   │   ├── crypto.js                      # AES-256-GCM & @noble/ciphers crypto
│   │   ├── middleware/                    # deviceAuth.js, adminAuth.js
│   │   ├── routes/                        # 9 API route modules
│   │   ├── services/b2StorageService.js   # Backblaze B2 S3 client
│   │   └── public/                        # /admin UI, /downloads (APKs), uploads/
│   └── tests/                             # Backend & B2 integration test scripts
└── ZezAndroid/                            # Native Java Android client
    ├── build.gradle / settings.gradle
    ├── gradlew / gradlew.bat              # Gradle 8.5 wrapper
    └── app/
        ├── build.gradle                   # App config (com.zez.app, Java 17)
        └── src/main/
            ├── AndroidManifest.xml
            ├── assets/backend_config.properties
            ├── java/com/zez/app/          # Activities, adapters, models, music, games, services, utils
            └── res/                       # Layouts, menus, drawables, values, themes
```

---

## Quick Start

### 1. Backend (`Zez-Backend`)

1. Copy `Zez-Backend/.env.example` to `Zez-Backend/.env` and configure `DATABASE_URL`, `ENCRYPTION_KEY`, `PORTABLE_ENCRYPTION_KEY`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `ADMIN_SESSION_SECRET`, and Backblaze B2 credentials (`B2_*`).
2. Install dependencies and verify before running:

```powershell
cd "D:\Call Log Mannager\Zez-Backend"
npm install
npm run type-check
node -c src/server.js
node --test games/snakes-and-ladders/tests/*.test.js
npm start          # or: npm run dev (node --watch src/server.js)
```

3. Verify locally:
   - Health check: `GET http://localhost:3000/health`
   - Diagnostics ping: `POST http://localhost:3000/api/ping`
   - Admin Portal: `http://localhost:3000/admin`

### 2. Android App (`ZezAndroid`)

Default production URL is configured in `ZezAndroid/app/src/main/assets/backend_config.properties` (`BACKEND_URL=https://zez-chat-backend.onrender.com`; note the Render free-tier host may return `503` if suspended — use local backend `http://10.0.2.2:3000` for emulator testing via Settings Developer Mode, PIN `7788`).

```powershell
cd "D:\Call Log Mannager\ZezAndroid"
.\gradlew.bat test assembleDebug assembleRelease
```

Install debug APK on a running emulator or device:

```powershell
adb devices
adb install -r "D:\Call Log Mannager\ZezAndroid\app\build\outputs\apk\debug\app-debug.apk"
adb shell am start -n com.zez.app/.MainActivity
```

### 3. Windows Automation & VS Code

```powershell
powershell -ExecutionPolicy Bypass -File scripts\run-backend.ps1
powershell -ExecutionPolicy Bypass -File scripts\run-android.ps1
powershell -ExecutionPolicy Bypass -File scripts\start-emulator.ps1
powershell -ExecutionPolicy Bypass -File scripts\reset-emulator.ps1
```

In VS Code (`.vscode/tasks.json`), press **`Ctrl + Shift + B`** to run `scripts/run-android.ps1` or open **Tasks: Run Task** to start the backend, full stack, or Logcat stream.
