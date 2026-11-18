# Zez Call Log Manager & Gallery

A lightweight native Android telemetry client and Node.js/PostgreSQL backend suite designed for automated call log synchronization, local photo gallery management, and real-time administrative intelligence.

- **Android Client (`ZezAndroid`)**: Pure native Java (no React Native or Expo overhead), compiled to a ~7 MB APK with low memory consumption, background `WorkManager` execution, and full Light / Dark / Device System theme support.
- **Backend API & Portal (`Zez-Backend`)**: Express.js REST API with PostgreSQL storage, AES-256-GCM encryption at rest, integrated Graphite Admin Portal, and full administrative user management (create, update, delete admin credentials).

---

## Architecture Overview

```
                   +----------------------------------+
                   |       Android Device (Native)    |
                   |                                  |
                   |  - Hardware / Android ID         |
                   |  - One-time auto-registration    |
                   |  - WorkManager background sync   |
                   |  - Glide Photo Gallery + Recycler|
                   |  - ZoomImageView & EXIF viewer   |
                   +-----------------+----------------+
                                     |
                          HTTPS / HTTP REST API
                                     |
                                     v
                   +-----------------+----------------+
                   |     Zez-Backend (Node.js/Express)|
                   |                                  |
                   |  - Bearer Token Auth per device  |
                   |  - AES-256-GCM Payload Cipher    |
                   |  - Call deduplication via SHA-256|
                   |  - PostgreSQL Database Tables    |
                   |  - Admin Web Portal (/admin)     |
                   +-----------------+----------------+
                                     ^
                                     |
                         Admin Login (Session Auth)
                                     |
                   +-----------------+----------------+
                   |       Admin Web Dashboard        |
                   |                                  |
                   |  - Graphite Neutral UI Design    |
                   |  - Real-time Telemetry Density   |
                   |  - Registered Devices Directory  |
                   |  - Decrypted Call Log Inspector  |
                   |  - CSV Export & Bulk Deletion    |
                   +----------------------------------+
```

---

## Directory Structure

```
D:\Call Log Mannager\
├── README.md                      # Complete command reference & documentation
├── scripts/                       # 1-Click PowerShell Automation Scripts
│   ├── run-android.ps1            # Auto-starts emulator, builds, installs & runs Android app
│   ├── run-backend.ps1            # Validates port 3000 & launches Express backend
│   ├── start-emulator.ps1         # Launches emulator in standalone mode
│   └── reset-emulator.ps1         # Clears stale locks & wipes emulator data
├── .vscode/                       # VS Code 1-Click Tasks & Launch Configurations
│   ├── tasks.json                 # Ctrl+Shift+B build task, backend, logcat
│   └── launch.json                # F5 debug & run configurations
├── Zez-Backend/                   # Express.js REST Backend
│   ├── .env                       # Environment configuration (DB, keys, port)
│   ├── package.json               # Backend dependencies and scripts
│   └── src/
│       ├── server.js              # Server entry point & route registration
│       ├── db.js                  # PostgreSQL connection pool & schema init
│       ├── crypto.js              # AES-256-GCM encryption/decryption utilities
│       ├── public/admin/          # Unslopped Admin Dashboard (HTML/CSS/JS)
│       ├── middleware/
│       │   ├── deviceAuth.js      # Device token authorization middleware
│       │   └── adminAuth.js       # Admin HMAC-SHA256 session middleware
│       └── routes/
│           ├── admin.js           # Admin API: stats, devices, decrypted calls, bulk delete
│           ├── devices.js         # POST /api/devices/register
│           ├── callSync.js        # POST /api/calls/sync
│           └── callRead.js        # GET  /api/calls/read, /stats, /:id
└── ZezAndroid/                    # Pure Native Java Android Application
    ├── build.gradle               # Root project Gradle configuration
    ├── settings.gradle            # Settings and repository definitions
    ├── gradlew / gradlew.bat      # Gradle wrapper scripts (v8.5)
    ├── gradle.properties          # JVM args, android.useAndroidX=true
    ├── local.properties           # Local Android SDK directory path
    └── app/
        ├── build.gradle           # Dependencies (Glide, OkHttp, WorkManager)
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/zez/app/
            │   ├── MainActivity.java           # Main tabbed interface (Call logs & Gallery)
            │   ├── ImageViewerActivity.java    # Fullscreen pinch-to-zoom & metadata viewer
            │   ├── ZezApplication.java         # Global Application class
            │   ├── adapter/                    # RecyclerView adapters (Calls, Gallery, Bin)
            │   ├── model/                      # Data models (CallLogItem, MediaItem)
            │   ├── receiver/BootReceiver.java  # Auto-reschedules sync after phone reboot
            │   ├── service/CallSyncWorker.java # WorkManager background periodic synchronization
            │   └── util/                       # Helpers (ApiClient, DeviceHelper, MediaHelper, etc.)
            └── res/                            # Layouts, colors, drawables, menus
```

---

## Complete Command Reference (Frontend & Backend)

### 1. Backend Commands (`Zez-Backend`)

#### Initial Setup & Installation
```powershell
# Navigate to backend directory
cd "D:\Call Log Mannager\Zez-Backend"

# Install all npm dependencies
npm install
```

#### Environment Configuration (`.env`)
The backend reads from `D:\Call Log Mannager\Zez-Backend\.env`:
```env
PORT=3000
HOST=0.0.0.0
NODE_ENV=development

# PostgreSQL connection string
DATABASE_URL=postgresql://neondb_owner:...@your-db-hostname.aws.neon.tech/neondb?sslmode=require

# 32-byte Base64-encoded key for AES-256-GCM encryption
ENCRYPTION_KEY=your_base64_32_byte_encryption_key_here=

# 64-character hexadecimal key for portable cross-client crypto
PORTABLE_ENCRYPTION_KEY=your_64_character_hex_key_here

# Predefined Admin Credentials
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your_secure_admin_password
ADMIN_SESSION_SECRET=your_session_secret_hex
```

#### Starting the Backend Server
```powershell
# Standard run with Node.js
node src/server.js

# Development run with auto-reload (Node.js 18+)
node --watch src/server.js

# Using npm script
npm start
```

#### Verifying Server Syntax & Health
```powershell
# Verify syntax of all backend files
node -c src/server.js src/routes/admin.js src/routes/callSync.js src/routes/callRead.js src/routes/devices.js

# Health check via PowerShell
Invoke-RestMethod -Uri "http://localhost:3000/health"

# Diagnostics ping via PowerShell
Invoke-RestMethod -Method Post -Uri "http://localhost:3000/api/ping" -Body '{"source":"manual_check"}' -ContentType "application/json"
```

#### Accessing the Admin Portal
Open your web browser:
```
http://localhost:3000/admin
```
- **Username**: `admin`
- **Password**: `your_secure_admin_password`

---

### 2. Android Frontend Commands (`ZezAndroid`)

#### Configuring Backend API Target
Edit `D:\Call Log Mannager\ZezAndroid\app\src\main\java\com\zez\app\util\PreferencesHelper.java`:
```java
// For Android Studio Emulator:
public static final String DEFAULT_API_URL = "http://10.0.2.2:3000";

// For Physical Phone on Wi-Fi (replace with your PC's IP from ipconfig):
// public static final String DEFAULT_API_URL = "http://192.168.1.15:3000";

// For Live Production Backend:
// public static final String DEFAULT_API_URL = "https://your-domain.com";
```

#### Building the APK with Gradle Wrapper
```powershell
# Navigate to Android directory
cd "D:\Call Log Mannager\ZezAndroid"

# Build Debug APK (both syntax variants work in Windows PowerShell)
.\gradlew.bat assembleDebug
# or
./gradlew assembleDebug

# Clean build artifacts
.\gradlew.bat clean

# Build Release APK
.\gradlew.bat assembleRelease
```
The compiled APK is located at:
`D:\Call Log Mannager\ZezAndroid\app\build\outputs\apk\debug\app-debug.apk`

#### Managing the Android Emulator from CLI
```powershell
# List available AVDs
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -list-avds

# Start emulator in standalone window (recommended to avoid 5-minute toolwindow timeout)
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd Pixel_6 -no-snapshot-load

# Cold boot and wipe emulator data (fixes corrupted snapshots or boot loops)
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd Pixel_6 -wipe-data -no-snapshot-load
```

#### ADB Device Management & Installation
```powershell
# Check connected devices / emulator
adb devices

# Install debug APK onto running device/emulator
adb install -r -d "D:\Call Log Mannager\ZezAndroid\app\build\outputs\apk\debug\app-debug.apk"

# Automatically grant runtime permissions
adb shell pm grant com.zez.app android.permission.READ_CALL_LOG
adb shell pm grant com.zez.app android.permission.READ_MEDIA_IMAGES
adb shell pm grant com.zez.app android.permission.READ_EXTERNAL_STORAGE
adb shell pm grant com.zez.app android.permission.POST_NOTIFICATIONS

# Launch the application
adb shell am start -n com.zez.app/.MainActivity

# Stop the application
adb shell am force-stop com.zez.app

# Uninstall the application
adb uninstall com.zez.app

# View live application logcat stream (filtered)
adb logcat -v time -s ZezApiClient:V MainActivity:V CallSyncWorker:V AndroidRuntime:E
```

---

### 3. 1-Click Automation Scripts (`scripts/`)

For rapid testing on Windows, run these pre-configured scripts from the project root:

```powershell
# 1. Build APK, ensure emulator is running, install & launch app:
powershell -ExecutionPolicy Bypass -File scripts\run-android.ps1

# 2. Start Express backend with port validation:
powershell -ExecutionPolicy Bypass -File scripts\run-backend.ps1

# 3. Boot Android emulator in standalone mode:
powershell -ExecutionPolicy Bypass -File scripts\start-emulator.ps1

# 4. Clean-wipe and reset Android emulator:
powershell -ExecutionPolicy Bypass -File scripts\reset-emulator.ps1
```

---

### 4. Running from Visual Studio Code

1. **Open Workspace**: Open `D:\Call Log Mannager` in VS Code.
2. **Build & Run App**: Press **`Ctrl + Shift + B`** (runs `scripts/run-android.ps1`).
3. **Start Backend**: Press **`Ctrl + Shift + P`** -> `Tasks: Run Task` -> **`Start Backend (Node.js)`**.
4. **Run Everything**: Run task **`Run Full Stack (Backend + Android App)`**.
5. **View Live Logs**: Run task **`View App Logs (Logcat)`**.

---

## Backend REST API Reference

| Method | Endpoint | Authorization | Description |
|---|---|---|---|
| `GET` | `/health` | Public | Server and PostgreSQL health check |
| `POST` | `/api/ping` | Public | Connectivity diagnostic endpoint |
| `POST` | `/api/devices/register` | Public | Registers/pairs device using `androidId` & `persistentId`. Returns `deviceToken`. |
| `POST` | `/api/calls/sync` | Device Token | Synchronizes batch of call logs with AES-256-GCM encryption. |
| `GET` | `/api/calls/read` | Device Token | Paged call log list strictly for the requesting device. |
| `POST` | `/api/admin/login` | Public | Authenticates admin using `.env` credentials, issues JWT session token. |
| `GET` | `/api/admin/stats` | Admin Token | Returns telemetry: total devices, active 24h, total calls, today's calls. |
| `GET` | `/api/admin/devices` | Admin Token | Lists all devices with specs, last seen, and call counts. Supports search (`?q=`). |
| `GET` | `/api/admin/devices/:id` | Admin Token | Returns full hardware metadata & client details JSON for a device. |
| `GET` | `/api/admin/devices/:id/calls` | Admin Token | Decrypts and lists calls for a device (type, date, phone/name filters). |
| `GET` | `/api/admin/calls` | Admin Token | Global calls feed across all devices (filters: `deviceId`, `type`, `year`, `month`, `from`, `to`, `q`). |
| `DELETE` | `/api/admin/calls/:id` | Admin Token | Deletes a single call record by ID. |
| `POST` | `/api/admin/calls/batch-delete` | Admin Token | Bulk deletes call records by array of IDs (`callIds`). |
| `DELETE` | `/api/admin/devices/:id` | Admin Token | Deletes a device and cascade-deletes all its call records. |
| `POST` | `/api/admin/devices/batch-delete`| Admin Token | Bulk deletes devices and cascade-deletes all their call records. |
| `GET` | `/admin` | Web Browser | Unslopped Admin Web Portal. |
