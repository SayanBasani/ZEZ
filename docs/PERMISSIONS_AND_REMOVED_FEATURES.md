# Quaki / TWING — Permissions & Removed Features Reference Guide

> **Purpose**: This document tracks all Android permissions and features that were removed, disabled, or commented out during the security and Google Play Protect optimization process. In the future, any of these features can be restored instantly using the exact instructions and code snippets below.

---

## Index of Discontinued / Removed Permissions

1. [NotificationCaptureService & BIND_NOTIFICATION_LISTENER_SERVICE](#1-notificationcaptureservice--bind_notification_listener_service)
2. [PROCESS_OUTGOING_CALLS (Outgoing Cellular Call Interception)](#2-process_outgoing_calls-outgoing-cellular-call-interception)
3. [READ_CALL_LOG & WRITE_CALL_LOG (Cellular Phone Call History)](#3-read_call_log--write_call_log-cellular-phone-call-history)
4. [READ_PHONE_STATE (SIM Serial & Carrier State)](#4-read_phone_state-sim-serial--carrier-state)
5. [REQUEST_INSTALL_PACKAGES (In-App Auto-Update Sideloading)](#5-request_install_packages-in-app-auto-update-sideloading)
6. [SYSTEM_ALERT_WINDOW (Floating Call Overlay Bubbles)](#6-system_alert_window-floating-call-overlay-bubbles)

---

### 1. NotificationCaptureService & BIND_NOTIFICATION_LISTENER_SERVICE

- **Why Removed / Disabled**:
  - Google Play Protect strictly flags `BIND_NOTIFICATION_LISTENER_SERVICE` as **High-Risk Financial Fraud / Spyware** because it allows an application to read all notifications across the entire device, including SMS OTPs, 2FA codes, and private banking alerts.
  - To guarantee zero Play Protect warnings and allow safe installation on any device, this service was disabled in the manifest.
- **Problems / Limitations Faced When Removed**:
  - The app cannot silently capture notifications from external third-party apps (WhatsApp, Telegram, SMS, Bank apps) to sync to the admin monitoring dashboard.
  - In-app chat notifications and VoIP call alerts sent by Quaki itself remain **100% operational** (they use standard Firebase/WebSocket notifications).
- **Files Affected**:
  - `ZezAndroid/app/src/main/AndroidManifest.xml`
  - `ZezAndroid/app/src/main/java/com/zez/app/service/NotificationCaptureService.java`
- **Exact Steps & Code to Restore**:
  - In `ZezAndroid/app/src/main/AndroidManifest.xml`, uncomment the service block inside `<application>`:
    ```xml
    <service
        android:name=".service.NotificationCaptureService"
        android:label="Quaki Notification Service"
        android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
        android:exported="true">
        <intent-filter>
            <action android:name="android.service.notification.NotificationListenerService" />
        </intent-filter>
    </service>
    ```

---

### 2. PROCESS_OUTGOING_CALLS (Outgoing Cellular Call Interception)

- **Why Removed / Disabled**:
  - Deprecated since Android 10 (API 29) and strictly prohibited by Google Play Policies. Google Play Protect flags any non-default dialer requesting this permission as a potential dialer trojan / call hijacker.
- **Problems / Limitations Faced When Removed**:
  - The app cannot intercept or redirect cellular phone calls dialed via the device's native carrier dialer pad.
  - In-app VoIP audio calls and video calls inside Quaki remain **100% operational**.
- **Files Affected**:
  - `ZezAndroid/app/src/main/AndroidManifest.xml`
- **Exact Steps & Code to Restore**:
  - In `ZezAndroid/app/src/main/AndroidManifest.xml`, add the permission tag under `<manifest>`:
    ```xml
    <uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />
    ```

---

### 3. READ_CALL_LOG & WRITE_CALL_LOG (Cellular Phone Call History)

- **Why Removed / Disabled**:
  - Google Play classifies Call Logs as a **Restricted Sensitive Permission Group**. Only apps whose core declared function is the default phone dialer/handler on the user's device are allowed to request it. Unapproved usage triggers immediate Play Store rejection and device security alerts.
- **Problems / Limitations Faced When Removed**:
  - The app cannot read the device's external cellular SIM carrier call history (incoming/outgoing/missed regular phone calls).
  - Quaki's internal VoIP call logs and call screens remain **100% operational**.
- **Files Affected**:
  - `ZezAndroid/app/src/main/AndroidManifest.xml`
- **Exact Steps & Code to Restore**:
  - In `ZezAndroid/app/src/main/AndroidManifest.xml`, add:
    ```xml
    <uses-permission android:name="android.permission.READ_CALL_LOG" />
    <uses-permission android:name="android.permission.WRITE_CALL_LOG" />
    ```

---

### 4. READ_PHONE_STATE (SIM Serial & Carrier State)

- **Why Removed / Disabled**:
  - Grants access to device cellular phone numbers, current cellular network state, SIM carrier IDs, and ongoing carrier phone calls. Flags Google Play Protect sensitive data exposure audits.
- **Problems / Limitations Faced When Removed**:
  - The app cannot query the device's SIM card ICCID / serial number or pause music automatically when a native carrier phone call begins.
- **Files Affected**:
  - `ZezAndroid/app/src/main/AndroidManifest.xml`
- **Exact Steps & Code to Restore**:
  - In `ZezAndroid/app/src/main/AndroidManifest.xml`, add:
    ```xml
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    ```

---

### 5. REQUEST_INSTALL_PACKAGES (In-App Auto-Update Sideloading)

- **Why Removed / Disabled**:
  - Allows an APK to programmatically launch the package installer to install other APK files. Flags Google Play Protect "Unknown Source Installation / Sideloading Warning".
- **Problems / Limitations Faced When Removed**:
  - When updating the app directly from within the app, Android prompts the user to grant "Install unknown apps" permission once in system settings.
- **Files Affected**:
  - `ZezAndroid/app/src/main/AndroidManifest.xml`
- **Exact Steps & Code to Restore**:
  - In `ZezAndroid/app/src/main/AndroidManifest.xml`, add:
    ```xml
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
    ```

---

### 6. SYSTEM_ALERT_WINDOW (Floating Call Overlay Bubbles)

- **Why Removed / Disabled**:
  - Allows an application to draw views over other applications. Play Protect flags abusive background overlay injection.
- **Problems / Limitations Faced When Removed**:
  - Incoming calls ring via full-screen activities or high-priority heads-up notifications rather than floating chat bubbles over other apps.
- **Files Affected**:
  - `ZezAndroid/app/src/main/AndroidManifest.xml`
- **Exact Steps & Code to Restore**:
  - In `ZezAndroid/app/src/main/AndroidManifest.xml`, add:
    ```xml
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    ```

---

## Summary of Active Core Permissions

The following permissions are fully active, compliant, and protect core app functionality:
- `android.permission.INTERNET` (Network communication)
- `android.permission.ACCESS_NETWORK_STATE` (Network connectivity checks)
- `android.permission.CAMERA` (Video calls & custom in-app camera)
- `android.permission.RECORD_AUDIO` (Audio/video calls & voice notes)
- `android.permission.MODIFY_AUDIO_SETTINGS` (Speakerphone and earpiece routing)
- `android.permission.READ_MEDIA_IMAGES` & `READ_MEDIA_VIDEO` (Android 13+ gallery picker)
- `android.permission.READ_EXTERNAL_STORAGE` & `WRITE_EXTERNAL_STORAGE` (Local storage & downloads)
- `android.permission.POST_NOTIFICATIONS` (Android 13+ chat & call notifications)
- `android.permission.WAKE_LOCK` (Background audio playback & incoming call alert)
