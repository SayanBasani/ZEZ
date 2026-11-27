# 05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates

---

## 1. Settings Screen Architecture (`view_settings.xml`)

Tab 4 (`R.id.nav_settings`) displays clean utility preferences (with **NO Chat Account card or Chat Account Deletion row**):

1. **Appearance & Theme** (`row_setting_theme`): opens `view_appearance_page` sub-page (`tv_setting_theme_current` displays current Night Mode + Palette name).
2. **Accent Color Swatches + Custom HSV Studio**:
   - 7 circular clickable swatches (`#2563EB` Blue, `#10B981` Emerald, `#EF4444` Crimson, `#EC4899` Pink, `#8B5CF6` Violet, `#F59E0B` Amber, `#06B6D4` Cyan) + custom color picker dialog (`dialog_color_picker.xml` with Hue/Saturation/Value sliders + live hex input).
3. **Font & Typography** (`row_setting_font`): opens `view_font_page` sub-page (`tv_setting_font_current`).
4. **Device Hardware & Storage** (`row_setting_hardware`): opens `view_hardware_page` sub-page (`tv_setting_hardware_summary` shows free space / RAM summary).
5. **Notification Cloud Backup** (`row_setting_notifications`):
   - Shows `"Active"` (`#22C55E`) when `NotificationCaptureService.isPermissionGranted(this)` is true (`"Access granted · Automatically backing up notifications"`), or `"Enable"` (`@color/primary`) when false.
   - Tapping when disabled opens the 2-step **Restricted Settings + Notification Access** dialog (`1. App Info` -> `2. Enable Access`).
   - Tapping when enabled confirms active status + button `"Manage in Settings"` (`ACTION_NOTIFICATION_LISTENER_SETTINGS`).
6. **Software Update (`btn_setting_update` & `tv_setting_version_info`)**:
   - Displays `"Installed: v<versionName> (Build <versionCode>)"`.
   - Button triggers `AppUpdateHelper.checkForUpdates(this, true)`.
7. **Hidden Protected Server Configuration (`row_setting_server`)**:
   - Hidden (`View.GONE`) by default!
   - Unlocked via **either** of the two hidden 5-tap gestures documented in Section 4 below.
8. **Footer (`tv_footer_app_version`)**: shows `"<AppName> v<versionName> (Build <versionCode>)"` (doubles as the 5-tap trigger target!).

---

## 2. Dynamic Theme, Palette & Font Engine (`ThemeManager.java`, `PreferencesHelper.java`)

### 2.1 Theme Modes (`KEY_THEME_MODE`)
- `THEME_SYSTEM = 0` (`MODE_NIGHT_FOLLOW_SYSTEM`)
- `THEME_LIGHT = 1` (`MODE_NIGHT_NO`)
- `THEME_DARK = 2` (`MODE_NIGHT_YES`)

### 2.2 All 8 Color Palettes (`KEY_COLOR_PALETTE`)
Exact verified light hex definitions from `ZezAndroid/app/src/main/res/values/colors.xml` (and matching dark pairs in `values-night/colors.xml` & `styles.xml`):

| Palette Key | Display Name | Bg | Surface | Surface Variant | Border | Primary | Accent |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `"graphite"` | Graphite | `#F8F9FA` | `#FFFFFF` | `#F1F3F5` | `#E2E8F0` | `#2563EB` | `#3B82F6` |
| `"arctic"` | Arctic Light | `#EFF6FB` | `#FFFFFF` | `#E0EDF7` | `#CCE2F2` | `#0284C7` | `#38BDF8` |
| `"midnight"` | Midnight | `#EEF2FF` | `#FFFFFF` | `#E0E7FF` | `#C7D2FE` | `#4F46E5` | `#6366F1` |
| `"amoled"` | AMOLED | `#F4F4F5` | `#FFFFFF` | `#E4E4E7` | `#D4D4D8` | `#0891B2` | `#06B6D4` |
| `"ocean"` | Ocean | `#EBF5FF` | `#FFFFFF` | `#DBEAFE` | `#BFDBFE` | `#0284C7` | `#0EA5E9` |
| `"sage"` | Sage | `#ECFDF5` | `#FFFFFF` | `#D1FAE5` | `#A7F3D0` | `#059669` | `#10B981` |
| `"sunset"` | Sunset | `#FFF7ED` | `#FFFFFF` | `#FFEDD5` | `#FED7AA` | `#EA580C` | `#F97316` |
| `"rose"` | Rose | `#FFF1F2` | `#FFFFFF` | `#FFE4E6` | `#FECDD3` | `#E11D48` | `#F43F5E` |

- **Dynamic Colors Switch (`KEY_DYNAMIC_COLOR`)**: applies `DynamicColors.applyToActivityIfAvailable(activity)` on Android 12+ (`API 31+`) when toggled ON.
- **Custom Theme Attributes (`res/values/attrs.xml`)**: `?attr/colorThemeBackground`, `?attr/colorThemeSurface`, `?attr/colorThemeSurfaceVariant`, `?attr/colorThemeInputBg`, `?attr/colorThemeBorder`, `?attr/colorThemeTextPrimary`, `?attr/colorThemeTextSecondary`, `?attr/colorThemeTextMuted`, `?attr/colorThemePrimary`, `?attr/colorThemeIconTint`.

### 2.3 Font Families & Live Scaling (`ThemeManager.java`)
- **9 Font Families (`KEY_FONT_FAMILY`)** applied via `getTheme().applyStyle(fontOverlayResId, true)` in `BaseActivity` / `MainActivity` before `super.onCreate()`:
  - `"sans"` (Default Roboto), `"light"` (`sans-serif-light`), `"condensed"` (`sans-serif-condensed`), `"medium"` (`sans-serif-medium`), `"black"` (`sans-serif-black`), `"serif"`, `"monospace"`, `"casual"`, `"cursive"`.
- **Font Scale (`KEY_FONT_SCALE`)**:
  - 4 Presets (`0.85f` Small, `1.0f` Default `14sp`, `1.15f` Large, `1.30f` Huge) **OR** custom integer size in `sp` (`customSp / 14.0f` clamped cleanly).
  - Applied globally by wrapping `attachBaseContext(Context newBase)` using `Configuration.fontScale = PreferencesHelper.getFontScale(newBase)` and `createConfigurationContext(config)`.

---

## 3. Hardware Diagnostics & Live Telemetry (`view_hardware_page.xml`)

### 3.1 `HardwareInfoHelper.java`
Provides:
- **Storage**: `StatFs(Environment.getDataDirectory().getPath())` -> total, available, used bytes, percentage.
- **RAM**: `ActivityManager.MemoryInfo` -> `totalMem`, `availMem`, used bytes, percentage.
- **CPU & SoC**:
  - Core count: `Runtime.getRuntime().availableProcessors()`.
  - Supported ABIs: `Build.SUPPORTED_ABIS[0]`.
  - Hardware / SoC: `Build.HARDWARE` + `/proc/cpuinfo` `Hardware` / `model name` fallback.
  - Clock frequency: reads `/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq` and `cpuinfo_max_freq` (formats MHz/GHz).
  - CPU Load % estimate for live telemetry sampling.

### 3.2 `TelemetryGraphView.java`
- Custom `View` keeping a rolling history buffer of float load samples (`0f..100f`).
- Renders curved path (`CornerPathEffect(12f)`), translucent gradient fill under the curve, primary stroke line, and subtle horizontal grid lines.
- Refreshed every **1500ms** (`telemetryHandler.postDelayed(telemetryRunnable, 1500)`) ONLY while `view_hardware_page` is visible (`View.VISIBLE`). Stopped cleanly on `closeSubPages()` / `onPause()`.

---

## 4. Hidden Developer Server Configuration Unlock

Replicate both hidden 5-tap entry points exactly as implemented in `MainActivity.java`:

1. **Trigger 1 — PIN-Protected Settings Server Row (`handleDeveloperTap`, lines 3990–4043)**:
   - Targets: `tv_setting_version_info` and `tv_footer_app_version`.
   - Tapping **5 times within 3000ms** prompts for Developer PIN (`ADMIN_PIN = "7788"`).
   - Correct PIN unlocks `row_setting_server` (`View.VISIBLE`) and opens `showServerConfigDialog()` with:
     - Quick Preset buttons: **Local Dev (`http://10.0.2.2:3000`)** and **Production Render (`https://zez-chat-backend.onrender.com`)**.
     - Custom URL `EditText`.
     - **Save & Connect**: validates URL, saves via `PreferencesHelper.setApiBaseUrl(ctx, url)`, runs `ApiClient.ping()` + re-registers device on background thread, and reports status.
     - **Reset Default**: clears custom URL back to `DEFAULT_API_URL` / emulator detection.

2. **Trigger 2 — Hardware Page Diagnostic Card (`tv_hardware_diagnostic_hint`, lines 492–516)**:
   - On `view_hardware_page.xml`, TextView `"System Diagnostics & Telemetry Integrity"` (`tv_hardware_diagnostic_hint`).
   - Tapping **5 times within 3000ms** toggles `card_hardware_server_config` (`View.VISIBLE` / `View.GONE`), revealing `et_setting_backend_url`, `btn_setting_save_url`, and `btn_setting_reset_url` inline.

---

## 5. OTA Software Update (`AppUpdateHelper.java`)

- Checks `GET <baseUrl>/api/app/latest?appId=companion` (see multi-app isolation note in `02_BACKEND_API_AND_DATABASE_SPEC.md`).
- Uses `OkHttpClient` with IPv4-first DNS (same Render fallback resilience).
- Compares server `versionCode` > `getCurrentVersionCode(context)`:
  - When up-to-date and `isManualCheck == true`, shows `"You are on the latest version"`.
  - When update is available, presents release dialog showing `versionName`, `versionCode`, `fileSize`, and `releaseNotes` -> `"Update Now"`.
- **Chunked/Streaming Download & Integrity Verification**:
  - Downloads APK into `getExternalCacheDir()/update_<versionCode>.apk` (or `cacheDir`) while showing live `ProgressDialog`/progress bar.
  - Verifies magic bytes (`buffer[0] == 'P' && buffer[1] == 'K'`) to catch HTML proxy errors before invoking package installer.
- **Installation**:
  - On API 26+, checks `getPackageManager().canRequestPackageInstalls()`. If false, prompts user to open `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES` for `package:<applicationId>`.
  - Fires `Intent.ACTION_VIEW` with `FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", apkFile)`, MIME type `"application/vnd.android.package-archive"`, and `FLAG_GRANT_READ_URI_PERMISSION | FLAG_ACTIVITY_NEW_TASK`.

---

## 6. Final Implementation Verification Checklist (For Next Build Agent)

Before handing the built standalone Companion App to the user:

1. **Clean Separation Check**:
   - Confirm `AndroidManifest.xml` has NO `ChatActivity`, `CallActivity`, `IncomingCallActivity`, `TicTacToeActivity`, `SnakesAndLaddersActivity`, or `QuackyBackgroundService`.
   - Confirm no `"Ready for messages & calls"` notification channel or persistent foreground notification exists anywhere in code.
   - Confirm zero Toast calls exist inside `CallLogObserver`, `CallStateReceiver`, `CallSyncWorker`, `NotificationCaptureService`, or `ApiClient`.
2. **Visible 4-Tab Flow**:
   - App cold-boots directly onto **Tab 1 (YMusic)**.
   - Search autocomplete, track streaming, offline download + MediaStore visibility, Sleep Timer, and Mini-player cross-tab persistence work smoothly.
   - **Tab 2 (Alarms)**: creating, toggling, multi-selecting, Jit Mode, and lock-screen firing work cleanly.
   - **Tab 3 (Gallery)**: Photos/Albums tabs, `All/Photos/Videos` filter, `ImageViewerActivity`, Recycle Bin restore, and Vivo 5-button multi-select -> `PDF/PPT` export + Share (`${applicationId}.provider`) work without `FileProvider` crash.
   - **Tab 4 (Settings)**: 8 palettes, night modes, accent swatches + HSV custom picker, 9 font families + scale, hardware telemetry graphs, 5-tap server config, and Notification Access status row work cleanly.
3. **Silent Background Sync Test (Emulator / Device)**:
   - Install APK cleanly alongside/without Quacky.
   - Grant runtime permissions + follow 2-step Restricted Settings -> Notification Access unlock once.
   - Force-close/swipe away the app from Recents.
   - Trigger an incoming/missed call on the emulator (`adb emu gsm call ...`) and post a test WhatsApp/notification message:
     - Verify zero notifications/toasts appear from the Companion App.
     - Verify `public.devices`, `public.call_logs`, `public.device_notifications`, and `public.device_app_calls` receive the new rows in `Zez-Backend`!
