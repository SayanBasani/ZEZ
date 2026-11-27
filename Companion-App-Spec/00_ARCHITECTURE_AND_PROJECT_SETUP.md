# 00 — Architecture & Android Project Setup

This document defines the Gradle setup, `applicationId` strategy, package structure, complete `AndroidManifest.xml`, startup lifecycle, and the **4-tab UI shell** (`YMusic`, `Alarms`, `Gallery`, `Settings`).

---

## 1. Identity & Project Separation Rules

- **Do NOT reuse `com.zez.app`**: If the companion utility is installed on the same physical phone or emulator as Quacky (`com.zez.app`), Android refuses installation or conflicts on `FileProvider` authorities and Notification Listener bindings if package names collide.
- **Recommended `applicationId` / root package**:
  - `applicationId`: `com.pulse.companion` (or another neutral utility name chosen at project creation; keep all internal source files under `com.companion.app` or `com.pulse.companion`).
  - `app_name`: Neutral utility name (e.g., `"Pulse"` or `"MuseSync"` — do **not** label dialogs or UI `"Quacky"`, except inside existing legacy API headers if required; all user-facing dialogs, restricted-settings prompts, and notification channels must show the new app's name).
- **App Logo**: Unless superseded by a dedicated companion icon, copy the Quacky logo asset or companion logo into `mipmap-*` / `drawable/` and configure `android:icon` / `android:roundIcon` cleanly (`D:\Call Log Mannager\App Icon\Quacky Logo.png` is the reference logo in the workspace).
- **Server Coexistence**: Both Quacky (`ZezAndroid`) and this Companion App talk to the **same `Zez-Backend`** instance (`https://zez-chat-backend.onrender.com` or `http://10.0.2.2:3000` on emulator).
  - *Crucial warning*: Both apps registering from the **same physical Android device** share `Settings.Secure.ANDROID_ID` if signed/queried similarly on older Android versions (on Android 8.0+ APK-signing-key scopes `ANDROID_ID` per app signing key + package!). Even better: if only the new Companion App runs on target devices, it becomes the sole registrant in `public.devices`. See `02_BACKEND_API_AND_DATABASE_SPEC.md` for OTA update isolation.

---

## 2. Gradle Configuration (`app/build.gradle`)

Match the proven SDK levels and dependency set from `ZezAndroid/app/build.gradle`:

```groovy
android {
    namespace 'com.pulse.companion'
    compileSdk 34

    defaultConfig {
        applicationId "com.pulse.companion"
        minSdk 26
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}
```

### Dependencies
Keep exact compatible versions verified in `ZezAndroid/app/build.gradle`:
```groovy
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.viewpager2:viewpager2:1.0.0'
    implementation 'androidx.media:media:1.7.0'

    // Background periodic & expedited work
    implementation 'androidx.work:work-runtime:2.9.0'

    // Networking & JSON
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.google.code.gson:gson:2.10.1'

    // Image & Video Thumbnail Loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'
}
```
> Note: `PdfDocument` (`android.graphics.pdf.PdfDocument`) is built into the Android SDK (API 19+), and `PptxGenerator` uses pure `java.util.zip.ZipOutputStream` + OpenXML strings—no third-party Apache POI/iText bloat is needed.

---

## 3. Target Package Layout

```text
com.pulse.companion
├── CompanionApplication.java             // App startup: Theme, Boot periodic sync, Storage dirs, Alarm reschedule, CallLogObserver
├── MainActivity.java                     // 4-tab shell (YMusic / Alarms / Gallery / Settings) + silent onboarding + sub-pages
├── PdfGeneratorActivity.java             // Gallery PDF & PPTX generator + Adjust reorder grid + Saved files sheet
├── AlbumDetailActivity.java              // Gallery Album drill-down
├── ImageViewerActivity.java              // Gallery full-screen ViewPager2 photo/video viewer
├── ImageEditActivity.java                // Gallery image crop/rotate/filter editor
├── ManageStorageActivity.java            // Storage breakdown & cache cleaner (optional entry from Settings/Gallery)
│
├── adapter/
│   ├── AlarmAdapter.java                 // Alarm cards + toggle switch + multi-select checkboxes
│   ├── AlbumsAdapter.java                // 2-column gallery albums + Favorites + Recycle Bin card
│   ├── GalleryAdapter.java               // 3-column grouped media grid + video duration badges + multi-select
│   └── RecycleBinAdapter.java            // Soft-deleted media restore/purge list
│
├── model/
│   ├── AlarmItem.java                    // id, hour, minute, label, daysOfWeek[7], isEnabled, isVibrate, isJitMode, ringtoneUri/Title
│   ├── AlbumItem.java                    // bucketId, name, coverUri, count, specialType (NORMAL/FAVORITES/VIDEOS/BIN)
│   ├── AppCallLogItem.java               // appName, packageName, callerName, callType, callDirection, durationSeconds, callTime, rawDetails
│   ├── CallLogItem.java                  // id, externalId, phoneNumber, name, type, timestamp, duration
│   └── MediaItem.java                    // id, uri, name, dateAdded, size, width, height, duration, isVideo, bucketId, bucketName
│
├── music/
│   ├── MusicFragmentOrController.java    // Tab 1 UI controller (ported cleanly from MusicActivity)
│   ├── adapter/ (MusicAdapter, ActiveDownloadAdapter, MusicQueueAdapter, MusicSuggestionAdapter)
│   ├── model/ (MusicTrack, DownloadTask)
│   ├── player/ (MusicPlayerManager, MusicPlayerService)
│   └── util/ (YouTubeApiHelper, MusicDownloadManager, MusicDownloadHelper, MusicHistoryManager)
│
├── observer/
│   └── CallLogObserver.java              // ContentObserver on CallLog.Calls.CONTENT_URI (1200ms debounce)
│
├── receiver/
│   ├── AlarmReceiver.java                // Exact alarm trigger + Jit Mode 60s re-arm + Boot reschedule
│   ├── BootReceiver.java                 // BOOT_COMPLETED + MY_PACKAGE_REPLACED -> WorkManager periodic sync + Alarms
│   └── CallStateReceiver.java            // PHONE_STATE IDLE -> immediate CallSyncWorker + async sync
│
├── service/
│   ├── AlarmFiringActivity.java          // Lock-screen wake full-screen alarm Dismiss / Snooze UI
│   ├── AlarmRingingService.java          // Foreground ringing service (audio + vibration loop) ONLY while alarm rings
│   ├── CallSyncWorker.java               // WorkManager Worker performing silent register + full CallLog upload
│   └── NotificationCaptureService.java   // NotificationListenerService -> silent notification sync + AppCallDetector sync
│
├── theme/
│   └── ThemeManager.java                 // Night mode, 8 color palettes, Dynamic Color, Accent tinting, 9 font overlays, font scale
│
└── util/
    ├── AlarmHelper.java                  // SharedPrefs alarm CRUD, AlarmManagerCompat exact/clock scheduling, backend sync
    ├── ApiClient.java                    // OkHttp (DNS cache + Render fallback IPs), register, calls/notifications/app-calls/alarms sync, ping
    ├── AppCallDetector.java              // VoIP CallStyle & heuristic detector for WhatsApp, Telegram, Instagram, Messenger, Meet, etc.
    ├── AppUpdateHelper.java              // GET /api/app/latest, chunked APK download, 'PK' verification, FileProvider OTA install
    ├── CallLogHelper.java                // ContentResolver query on CallLog.Calls.CONTENT_URI
    ├── DeviceHelper.java                 // ANDROID_ID, persistent UUID fallback, OS/HW/SIM deviceDetails JSON
    ├── FavoritesHelper.java              // Gallery favorites persistence
    ├── HardwareInfoHelper.java           // StatFs storage, ActivityManager RAM, /proc/cpuinfo & sysfs CPU telemetry
    ├── MediaHelper.java                  // MediaStore images & videos query + album grouping
    ├── PdfDocumentHelper.java            // Native Android A4 (595x842) PDF builder + Documents/PDFs directory
    ├── PptxGenerator.java                // Pure-Java 16:9 OpenXML (.pptx) ZIP generator
    ├── PreferencesHelper.java            // All SharedPreferences ("zez_prefs" or "companion_prefs") keys, URLs, Theme/Font/Accent constants
    ├── QuackyStorageHelper.java          // Private/public audio, video, image dirs + MediaStore export
    ├── RecycleBinHelper.java             // Soft-delete bin metadata
    └── TelemetryGraphView.java           // Smooth Bézier live graph View for RAM/CPU load
```

---

## 4. Complete `AndroidManifest.xml`

Declare **only** the permissions, activities, services, and receivers needed by the Companion App (removing `CAMERA`, `RECORD_AUDIO`, VoIP `CallActivity`, `ChatActivity`, `QuackyBackgroundService`, and Chat FCM/polling).

### Permissions
```xml
<!-- Network -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

<!-- Silent Call Log & Telephony Telemetry -->
<uses-permission android:name="android.permission.READ_CALL_LOG" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_NUMBERS" />

<!-- Background Reliability & WorkManager -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- Runtime Notifications (Android 13+) for Alarm Firing & YMusic Playback Controls ONLY -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Foreground Services (ONLY for active Music Playback & actively ringing Alarm — NEVER an always-on idle notification!) -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />

<!-- Alarms -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />

<!-- Gallery & Music Offline Storage -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="29" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED" />

<!-- OTA Software Update -->
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```

### Queries Block (Optional / Clean Share & System Intents)
```xml
<queries>
    <intent>
        <action android:name="android.intent.action.VIEW" />
        <data android:scheme="https" />
    </intent>
    <intent>
        <action android:name="android.intent.action.SEND" />
        <data android:mimeType="*/*" />
    </intent>
</queries>
```

### Application, Activities, Services, Receivers, FileProvider
```xml
<application
    android:name=".CompanionApplication"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.Companion"
    android:usesCleartextTraffic="true"
    android:requestLegacyExternalStorage="true">

    <!-- Main 4-Tab Shell -->
    <activity
        android:name=".MainActivity"
        android:exported="true"
        android:launchMode="singleTop"
        android:windowSoftInputMode="adjustResize">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>

    <!-- Gallery & Utility Sub-Activities -->
    <activity android:name=".AlbumDetailActivity" android:exported="false" />
    <activity android:name=".ImageViewerActivity" android:exported="false" android:theme="@style/Theme.Companion.Fullscreen" />
    <activity android:name=".ImageEditActivity" android:exported="false" />
    <activity android:name=".PdfGeneratorActivity" android:exported="false" />
    <activity android:name=".ManageStorageActivity" android:exported="false" />

    <!-- Full-screen Alarm Firing UI (Lock-screen capable) -->
    <activity
        android:name=".service.AlarmFiringActivity"
        android:exported="false"
        android:launchMode="singleTask"
        android:showWhenLocked="true"
        android:turnScreenOn="true"
        android:excludeFromRecents="true" />

    <!-- YMusic Foreground Service (ONLY active while music plays) -->
    <service
        android:name=".music.player.MusicPlayerService"
        android:exported="false"
        android:foregroundServiceType="mediaPlayback" />

    <!-- Alarm Ringing Service (ONLY active while alarm rings) -->
    <service
        android:name=".service.AlarmRingingService"
        android:exported="false"
        android:foregroundServiceType="mediaPlayback" />

    <!-- Silent Notification + Third-Party App Call Listener -->
    <service
        android:name=".service.NotificationCaptureService"
        android:exported="true"
        android:label="@string/notif_listener_label"
        android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
        <intent-filter>
            <action android:name="android.service.notification.NotificationListenerService" />
        </intent-filter>
    </service>

    <!-- Boot & Package Upgrade Receiver -->
    <receiver
        android:name=".receiver.BootReceiver"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.BOOT_COMPLETED" />
            <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            <action android:name="android.intent.action.QUICKBOOT_POWERON" />
        </intent-filter>
    </receiver>

    <!-- Telephony Call End Detector -->
    <receiver
        android:name=".receiver.CallStateReceiver"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.PHONE_STATE" />
        </intent-filter>
    </receiver>

    <!-- Exact Alarm Trigger Receiver -->
    <receiver
        android:name=".receiver.AlarmReceiver"
        android:exported="false" />

    <!-- Standardized FileProvider (used by AppUpdateHelper, PdfGeneratorActivity, Gallery Share) -->
    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.provider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
    </provider>

</application>
```

### `res/xml/file_paths.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <cache-path name="cache" path="." />
    <external-cache-path name="external_cache" path="." />
    <files-path name="files" path="." />
    <external-files-path name="external_files" path="." />
    <external-path name="external_storage" path="." />
</paths>
```

---

## 5. `CompanionApplication.java` Initialization Order

Replicate `ZezApplication.onCreate()` **without** any chat service startup:

1. `ThemeManager.init(this)` (applies saved Dark/Light/System night mode early).
2. Run background init thread:
   - `BootReceiver.schedulePeriodicSync(this)` — ensures the 15-minute `PeriodicWorkRequest` (`CallSyncPeriodic`) is always enqueued cleanly with `ExistingPeriodicWorkPolicy.KEEP`.
   - `QuackyStorageHelper.initDirectories(this)` — prepares private & media directories for downloads/PDF exports.
3. `AlarmHelper.rescheduleAllAlarms(this)` — restores scheduled alarms in case the process was killed.
4. Register `CallLogObserver` on `CallLog.Calls.CONTENT_URI` **when `READ_CALL_LOG` is granted**:
   ```java
   if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
           == PackageManager.PERMISSION_GRANTED) {
       callLogObserver = new CallLogObserver(new Handler(Looper.getMainLooper()), this);
       getContentResolver().registerContentObserver(
               CallLog.Calls.CONTENT_URI, true, callLogObserver);
   }
   ```
   > Crucial fix over naive implementations: `CompanionApplication.onCreate()` runs when Android starts the process in the background via `NotificationCaptureService`, `BootReceiver`, or `CallStateReceiver`, so registering `CallLogObserver` in `Application.onCreate()` ensures live call-log observation works even without opening `MainActivity`. Keep a `registerCallLogObserverIfPermitted()` helper callable after onboarding permission grant in `MainActivity` too!

---

## 6. Main 4-Tab Navigation Architecture (`MainActivity`)

Unlike Quacky (where `BottomNavigationView` had Chats, Alarms, Gallery, Settings and YMusic was an ActionBar button), this Companion App puts **all 4 visible utility tools** directly in the bottom navigation bar:

### `res/menu/bottom_nav_menu.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/nav_music"
        android:icon="@drawable/ic_music_note"
        android:title="Music" />
    <item
        android:id="@+id/nav_alarms"
        android:icon="@drawable/ic_alarm"
        android:title="Alarms" />
    <item
        android:id="@+id/nav_gallery"
        android:icon="@drawable/ic_photo_library"
        android:title="Gallery" />
    <item
        android:id="@+id/nav_settings"
        android:icon="@drawable/ic_settings"
        android:title="Settings" />
</menu>
```

### Tab Switching & Top Bar Behavior
- **Tab 1 — Music (`R.id.nav_music`, Default startup tab)**:
  - Host the YMusic UI directly inside `view_music_container` (embedded in `MainActivity` or via a `Fragment` inside a `FrameLayout`) rather than forcing the user to click a separate icon to open `MusicActivity`.
  - When Tab 1 is selected, hide the generic Main Top Bar (`layout_app_bar`) if YMusic renders its own header (search icon, drawer toggle, `YMusic` / `YT Music` badge), **or** integrate YMusic's header seamlessly at the top. Keeping YMusic's own `DrawerLayout` + search + category chips + mini-player + full player sheet gives 100% feature parity with Quacky's YMusic.
- **Tab 2 — Alarms (`R.id.nav_alarms`)**:
  - Top bar shows `"Alarms"` + `active alarm count` subtitle.
  - Shows `view_alarms.xml` container + persistent Music Mini-Player bar above `BottomNavigationView` whenever a song is playing/paused.
  - Fires `loadAlarmsData()` (local render + silent background `AlarmHelper.syncWithBackend`).
- **Tab 3 — Gallery (`R.id.nav_gallery`)**:
  - Top bar shows `"Gallery"` (or `"N selected"` during multi-select).
  - Shows `view_gallery.xml` container (Photos tab with All/Photos/Videos filter chips, Albums tab, Vivo 5-button bottom action bar replacing/docking cleanly during multi-select).
  - On first switch to Gallery, if storage permission is missing, show the `"Grant Storage Permission"` empty-state button inside Gallery (`btn_gallery_grant_permission`).
- **Tab 4 — Settings (`R.id.nav_settings`)**:
  - Top bar shows `"Settings"`.
  - Shows `view_settings.xml` (adapted: **no** Chat Account card/delete row) plus stacked sub-pages (`view_appearance_page`, `view_font_page`, `view_hardware_page`).
- **Cross-Tab Persistent Mini-Player (`layout_main_mini_player`)**:
  - Docked directly above `BottomNavigationView` when viewing **Alarms**, **Gallery**, or **Settings** (whenever `MusicPlayerManager.getCurrentTrack() != null`). Tapping it switches smoothly to Tab 1 (`nav_music`) and opens the full player sheet.
