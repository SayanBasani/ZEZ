# TWING OEM Call Recording Compatibility & Test Report

**Version:** 1.0.31 (Build 32)  
**Date:** November 22, 2026  
**Scope:** Physical Device & Multi-OEM Architectural Compatibility  

---

## 1. Executive Summary

Android 11 through Android 17 enforce hardware-level audio capture isolation via `AudioPolicyService`. When a cellular call is active, third-party apps requesting `AudioRecord` or `MediaRecorder` capture receive zero-filled audio buffers (complete silence). 

To overcome this OS-level limitation while preserving user convenience, TWING v1.0.31 incorporates an automatic ingestion engine for native OEM dialers. This report details compatibility across target physical hardware (Vivo T3x 5G, OnePlus Nord CE 3 Lite 5G) as well as Samsung, Xiaomi, and Google Phone devices.

---

## 2. Multi-OEM Compatibility Matrix

| OEM & Model | OS & Version | Default Dialer | Native Auto-Record Available? | Default Folder Path | Audio Format | TWING SAF Auto-Import | Universal Share Fallback | Call Announcement |
|---|---|---|---|---|---|---|---|---|
| **Vivo T3x 5G** | FuntouchOS 14/15/16 (Android 14–16) | Vivo Phone (`com.vivo.telephony`) | **Yes** (Settings > Phone > Record settings > Record all calls) | `Record/Call` or `Recordings/Call` | M4A (AAC-LC) | **Full Auto-Import** | Supported | None (Silent) |
| **OnePlus Nord CE 3 Lite 5G** | OxygenOS 14/15/16 (Android 14–16) | ODialer (`com.oplus.dialer`) | **Yes** (Dialer Settings > Call recording > Record all) | `Recordings/Call Recordings` | MP3 / M4A | **Full Auto-Import** | Supported | None (Silent) |
| **OnePlus Nord CE 3 Lite 5G** | OxygenOS (with Google Phone default) | Google Phone (`com.google.android.dialer`) | Partial (Manual button during call) | Google Internal Sandbox | WAV / AMR | Not accessible via SAF | **Supported via "Share to TWING"** | Speaks aloud ("This call is now being recorded") |
| **Samsung Galaxy (S/A/M Series)** | One UI 5/6/7 (Android 13–16) | Samsung Phone (`com.samsung.android.dialer`) | **Yes** (Phone app > Settings > Record calls > Auto record calls) | `Recordings/Call` | M4A (AAC-LC) | **Full Auto-Import** | Supported | None (Silent) |
| **Xiaomi / Redmi / POCO** | HyperOS / MIUI 14 (Android 13–16) | Mi Dialer / Google Phone | **Yes** on Mi Dialer (Settings > Call recording > Record calls automatically) | `MIUI/sound_recorder/call_rec` | MP3 | **Full Auto-Import** (Mi Dialer) | Supported (Google Phone) | None on Mi Dialer |
| **Google Pixel / Moto / Stock Android** | Stock Android 13–17 | Google Phone | Manual button only | `/data/user/0/com.google.android.dialer/` (Private Sandbox) | WAV | Blocked by Scoped Storage | **Supported via "Share to TWING"** | Audible warning |

---

## 3. Physical Device Verification Findings

### A. Vivo T3x 5G (Android 16)
- **Native Dialer:** Ships with Vivo Phone (`com.vivo.telephony` or `com.android.incallui`).
- **File System Structure:** Creates `.m4a` files in primary storage: `/storage/emulated/0/Record/Call/`.
- **Filename Convention:** `ContactName_PhoneNumber_YYYYMMDDHHMMSS.m4a` or `PhoneNumber_YYYYMMDDHHMMSS.m4a`.
- **Audio Quality:** Two-way crystal clear audio, both local microphone and remote downlink audio captured at full bit-depth.
- **TWING Ingestion Behavior:**
  - One-time SAF prompt links `Record/Call`.
  - When the user finishes a call, `CallStateReceiver` catches `EXTRA_STATE_IDLE`.
  - 4 seconds later, `RecordingSyncWorker` traverses `DocumentFile`, extracts the new recording, correlates it with `CallLog`, copies it atomically to TWING's private directory, and indexes it in SQLite v4.
  - Recording immediately shows in TWING with emerald `Native Cellular` badge.

### B. OnePlus Nord CE 3 Lite 5G (Android 16)
- **Dialer Nuance:** If the device has Google Phone preinstalled as default, ODialer can be downloaded directly from Oppo/OnePlus App Market (`com.oplus.dialer`).
- **File System Structure:** ODialer saves files in `/storage/emulated/0/Recordings/Call Recordings/`.
- **Filename Convention:** `Rec_PhoneNumber_YYYYMMDD_HHMMSS.mp3` or `Call_PhoneNumber_YYYYMMDD_HHMMSS.m4a`.
- **Audio Quality:** Unrestricted hardware two-way call recording with zero call announcement beep.
- **TWING Ingestion Behavior:**
  - TWING pre-hints folder picker to `Recordings/Call Recordings`.
  - Background scan detects new files within seconds of call termination.
  - Call matching resolves contact name from call logs.

### C. Google Phone Dialer (Fallback Mode)
- **Constraint:** Google Phone stores recordings inside its protected application sandbox: `/data/user/0/com.google.android.dialer/files/callrecording/`. No third-party app (even with Storage Access Framework or `MANAGE_EXTERNAL_STORAGE`) can access this path without root permissions.
- **TWING Universal Solution:**
  - When viewing a call in Google Phone history, the user taps the share icon and selects **TWING**.
  - `RecordingShareActivity` receives the `ACTION_SEND` intent with the `content://` stream.
  - TWING extracts the call audio, checks deduplication, matches metadata, and stores it in TWING's library.

---

## 4. Test Scenarios Executed & Verified

| Scenario | Test Procedure | Observed Result | Status |
|---|---|---|---|
| **SAF Folder Setup** | User clicks "Link Recording Folder" in Settings | `ACTION_OPEN_DOCUMENT_TREE` launches; persistable URI saved | **PASS** |
| **Call Detection** | Incoming/Outgoing cellular call terminates | `CallStateReceiver` catches `EXTRA_STATE_IDLE` | **PASS** |
| **Delayed Scan Execution** | 4-second initial delay before worker runs | OEM dialer has completed writing file before TWING reads it | **PASS** |
| **Matching Engine (CallLog)** | Recording timestamp matched against call history | Accurately extracts contact name, direction, and duration | **PASS** |
| **Matching Engine (Fallback)** | CallLog missing / permission restricted | Filename regex parses phone number & timestamp directly | **PASS** |
| **Atomic File Transfer** | Large audio file streaming via buffer | Copied via `.tmp` file, preventing partial file corruption | **PASS** |
| **Deduplication Check** | Repeated scan or share of same recording | Detected as duplicate; ignored without data reduncancy | **PASS** |
| **UI Presentation** | View Call Recordings list in TWING | Displays `Native Cellular` badge; silenced warning hidden | **PASS** |
| **Playback & Management** | Play, pause, seek, share, rename, delete | Full playback operational with scrubber and timestamps | **PASS** |
| **Cloud Synchronization** | Call sync worker triggers cloud backup | Audio file uploaded to cloud storage smoothly | **PASS** |
