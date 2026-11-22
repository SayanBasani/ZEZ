# Graph Report - Call Log Mannager  (2026-11-23)

## Corpus Check
- 145 files · ~128,721 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 159 file(s) not represented in the graph (top: .xml 149, .properties 5, (none) 3)
- WARNING: 159 of 317 files seen (50%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 2131 nodes · 5158 edges · 199 communities (56 shown, 143 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 489 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `7b6c071d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AlarmItem
- b2StorageService.js
- AppCallItem
- e2e_device_test.py
- ChatContactItem
- Hardware Info & Performance Telemetry (C6)
- ActiveCallManager
- AlarmHelper.java
- com.zez.app.model.ChatContactItem
- CallViewHolder
- compilerOptions
- .loadGalleryData
- package.json
- index.d.ts
- Override
- ChatUser
- RecycleBinAdapter
- d_call_log_mannager_zez_backend_src_db_pool
- ChatApiClient
- MainActivity
- AppUpdateHelper
- admin.js
- Hardware Info & Performance Telemetry (C27)
- server.js
- android.view.View
- Call Recording Audio Diagnostics & Root-Cause Technical Report
- Override
- test-roster.js
- CallSyncWorker
- Callback
- CallActivity
- ChatMessage
- android.hardware.camera2.CameraDevice
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- appRelease.js
- Override
- ImageViewerActivity
- server.js
- ZezApplication.java
- okhttp3.Call
- 2. Core Components & Responsibilities
- Call Recording Implementation Audit
- com.zez.app.model.ChatConversation
- callRead.js
- 3. Detailed Component Breakdown
- android.content.Context
- ChatLocalDbHelper
- .onResponse
- benchmark
- ChatActivity
- 2. Manufacturer-by-Manufacturer Breakdown
- android.net.Uri
- 4. Troubleshooting & FAQs
- com.zez.app.model.CallLogItem
- PreferencesHelper
- TWING: Device-Specific Zero-Audio Investigation — Android 16
- android.os.Bundle
- Technical Proposal: Unified Call Recording & Native OEM Import Integration
- 1. System-Level Architectural Limitations
- CallSyncWorker.java
- CallStateReceiver
- db.js
- CallStateReceiver
- TWING OEM Call Recording Compatibility & Test Report
- NotificationCaptureService.java
- CallLogItem
- chat.js
- ChatNotificationManager.java
- .onSuccess
- AlarmRingingService
- TextWatcher
- ContactsActivity
- Detailed Test Analysis:
- .onItemSelected
- 3. Manufacturer-by-Manufacturer Native Recording Breakdown
- FilmstripAdapter
- .applyModeState
- .setCalls
- MediaPagerAdapter
- com.zez.app.model.ChatMessage
- ImageEditActivity
- .showUpdateDialog
- FavoritesHelper
- Override
- ChatCallback<MessagesResult>
- ChatMessageAdapter

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 112 edges
2. `ChatActivity` - 72 edges
3. `ChatApiClient` - 43 edges
4. `ChatMessageAdapter` - 43 edges
5. `CallActivity` - 43 edges
6. `AppCallItem` - 42 edges
7. `ChatMessage` - 42 edges
8. `ChatCallback` - 39 edges
9. `ChatConversation` - 32 edges
10. `ViewHolder` - 32 edges

## Surprising Connections (you probably didn't know these)
- `4. Test Scenarios Executed & Verified` --references--> `CallStateReceiver`  [INFERRED]
  TWING_OEM_COMPATIBILITY_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `A. Vivo T3x 5G (Android 16)` --references--> `CallStateReceiver`  [INFERRED]
  TWING_OEM_COMPATIBILITY_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `B. Manifest Declarations Removed` --references--> `ChatActivity`  [INFERRED]
  docs/GALLERY_2_ROLLBACK_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java
- `7. Affected Files & Isolation Boundaries` --references--> `ChatActivity`  [INFERRED]
  TWING_CALL_RECORDING_INTEGRATION_PROPOSAL.md → ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java
- `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` --references--> `MainActivity`  [INFERRED]
  CALL_RECORDING_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java

## Import Cycles
- None detected.

## Communities (199 total, 143 thin omitted)

### Community 1 - "AlarmItem"
Cohesion: 0.15
Nodes (5): AlarmAdapter, OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 3 - "AppCallItem"
Cohesion: 0.09
Nodes (4): OnCallActionListener, com.zez.app.adapter.AppCallAdapter.OnCallActionListener, AppCallItem, Callback

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.07
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 9 - "ActiveCallManager"
Cohesion: 0.11
Nodes (3): ActiveCallManager, Callback, CallStateListener

### Community 12 - "CallViewHolder"
Cohesion: 0.20
Nodes (4): AppCallAdapter, CallViewHolder, CallLogAdapter, CallViewHolder

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - ".loadGalleryData"
Cohesion: 0.30
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 15 - "package.json"
Cohesion: 0.05
Nodes (44): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, @noble/ciphers, pg, @types/cors, @types/express (+36 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "Override"
Cohesion: 0.14
Nodes (7): Callback, TextureView.SurfaceTextureListener, ActiveCallManager.CallStateListener, Runnable, Callback, Runnable, Callback

### Community 19 - "RecycleBinAdapter"
Cohesion: 0.27
Nodes (3): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.06
Nodes (23): content, fs, lines, navMatch, fs, path, fs, path (+15 more)

### Community 21 - "ChatApiClient"
Cohesion: 0.15
Nodes (12): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient, Callback, Callback, ChatCallback, ContactSyncResult, ConversationModeInfo, IncomingCallInfo (+4 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.21
Nodes (4): AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 26 - "admin.js"
Cohesion: 0.09
Nodes (14): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+6 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 28 - "server.js"
Cohesion: 0.12
Nodes (14): closeDatabase(), initializeDatabase(), app, startServer(), http, path, run(), tests (+6 more)

### Community 29 - "android.view.View"
Cohesion: 0.14
Nodes (10): AlarmViewHolder, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder (+2 more)

### Community 30 - "Call Recording Audio Diagnostics & Root-Cause Technical Report"
Cohesion: 0.11
Nodes (17): 1.1 The Core Problem, 1.2 The Root Cause, 1. Executive Summary & Root Cause Analysis, 2. Audio Source Investigation Matrix, 3.1 `AudioManager.AudioRecordingCallback` (API 29+), 3.2 Real-Time Peak & Silence Monitor, 3.3 Post-Recording Frame Decoded Sample Validation (`AudioCaptureValidator`), 3.4 Elimination of False Recordings (+9 more)

### Community 31 - "Override"
Cohesion: 0.10
Nodes (7): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, ProfileMediaAdapter, ViewHolder

### Community 32 - "test-roster.js"
Cohesion: 0.09
Nodes (22): http, run(), http, run(), http, request(), run(), token (+14 more)

### Community 35 - "Callback"
Cohesion: 0.24
Nodes (3): Callback, okhttp3.Callback, okhttp3.Callback

### Community 52 - "appRelease.js"
Cohesion: 0.07
Nodes (16): fs, path, fs, path, fs, path, fs, path (+8 more)

### Community 53 - "Override"
Cohesion: 0.06
Nodes (9): android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>>, ChatApiClient.ChatCallback<List<ChatConversation>>, android.text.TextWatcher, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback (+1 more)

### Community 55 - "ImageViewerActivity"
Cohesion: 0.05
Nodes (15): 3. Scope of Modifications & Discontinued Components, A. Dependencies Removed, B. Manifest Declarations Removed, C. File-Level Comparison & Removals, 7. Affected Files & Isolation Boundaries, GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener (+7 more)

### Community 58 - "server.js"
Cohesion: 0.09
Nodes (18): express, express@^5.1.0, downloadsDir, express, fs, multer, path, router (+10 more)

### Community 60 - "ZezApplication.java"
Cohesion: 0.16
Nodes (3): BootReceiver, ChatNotificationHelper, ZezApplication

### Community 61 - "okhttp3.Call"
Cohesion: 0.11
Nodes (12): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+4 more)

### Community 63 - "2. Core Components & Responsibilities"
Cohesion: 0.18
Nodes (10): 1. Overview, 2.1 `CallRecordingManager` (Main Coordinator), 2.3 `RecordingCapabilityDetector` (Device & Policy Inspector), 2.4 `RecordingEngine` (Audio Capture Engine), 2.5 `RecordingStorageManager` (Storage & File Integrity), 2.6 `RecordingDatabaseManager` (Metadata Persistence), 2.7 `RecordingNotificationManager` (User Indicators & Compliance), 2.8 `RecordingPermissionManager` (Consent & Runtime Checks) (+2 more)

### Community 65 - "Call Recording Implementation Audit"
Cohesion: 0.20
Nodes (9): 1.1 Project Structure & Build Configuration, 1.2 Core Functional Modules, 1. Existing Architecture & Baseline, 2. Inventory of Existing Call Recording Files, 3. Untouchable Files & Subsystems, 4. Android Security & Hardware Restrictions Analysis, 5. Proposed Modular Integration Strategy, Call Recording Implementation Audit (+1 more)

### Community 66 - "com.zez.app.model.ChatConversation"
Cohesion: 0.06
Nodes (9): ConversationsAdapter, androidx.recyclerview.widget.DiffUtil.Callback, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, ChatConversation, Callback, ChatApiClient.ChatCallback<List<ChatConversation>> (+1 more)

### Community 67 - "callRead.js"
Cohesion: 0.07
Nodes (31): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+23 more)

### Community 68 - "3. Detailed Component Breakdown"
Cohesion: 0.15
Nodes (12): 1. Architectural Overview & Context, 2. Implemented Architecture & Subsystems, 3. Detailed Component Breakdown, 4. Verification and Security Validation, A. Storage Access Framework & Permission Persistence (`OemRecordingImporter.java`), B. Intelligent Pattern & CallLog Correlation Engine (`RecordingMatcher.java`), C. Background Synchronization (`RecordingSyncWorker.java`), D. Universal Sharing Fallback (`RecordingShareActivity.java`) (+4 more)

### Community 77 - "ChatActivity"
Cohesion: 0.11
Nodes (3): ChatActivity, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, PendingMessage

### Community 81 - "2. Manufacturer-by-Manufacturer Breakdown"
Cohesion: 0.20
Nodes (9): 1. Overview, 2.1 Samsung (One UI / Android 10–14), 2.2 Xiaomi / Redmi / POCO (MIUI / HyperOS / Android 10–14), 2.3 OnePlus / Oppo / Realme (ColorOS / OxygenOS / Android 10–14), 2.4 Vivo / iQOO (Funtouch OS / OriginOS / Android 10–14), 2. Manufacturer-by-Manufacturer Breakdown, 3. Comprehensive Device Capability Matrix, 4. Architectural Honesty & Reporting (+1 more)

### Community 82 - "android.net.Uri"
Cohesion: 0.17
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 88 - "4. Troubleshooting & FAQs"
Cohesion: 0.12
Nodes (16): 1. Quick Start Setup (One-Time Configuration), 2. How the Automatic Import Works, 3. Universal Manual Fallback ("Share to TWING"), 4. Troubleshooting & FAQs, OnePlus / OPPO / Realme Phones (OxygenOS / ColorOS), Q1: My recordings are not importing automatically after a call ends., Q2: Why does an old recording say "Audio capture silenced by Android OS"?, Q3: Why does Google Phone say out loud "This call is now being recorded"? (+8 more)

### Community 89 - "com.zez.app.model.CallLogItem"
Cohesion: 0.18
Nodes (4): ApiCallback, ApiClient, CallLogHelper, DeviceHelper

### Community 94 - "TWING: Device-Specific Zero-Audio Investigation — Android 16"
Cohesion: 0.13
Nodes (14): 1. Executive Summary, 2. Exact Device Profiles & Specifications, 4.1 Telephony Audio Mode Transition, 4.2 `AudioRecordingConfiguration` Parameters During Call, 4. Deep Inspection of Android Recording Configuration, 5.1 Vivo (Funtouch OS) Native Call Recorder, 5.2 OnePlus (OxygenOS / ColorOS) Native Call Recorder, 5. Investigation of OEM-Supported Call Recording Mechanisms (+6 more)

### Community 95 - "android.os.Bundle"
Cohesion: 0.21
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 99 - "Technical Proposal: Unified Call Recording & Native OEM Import Integration"
Cohesion: 0.22
Nodes (8): 1. Executive Summary & Objective, 2. Physical Device ADB Status Verification, 5. Intelligent Call Matching Algorithm (`RecordingMatcher`), 6.1 Database Schema (v4 Upgrade in `CallRecordDbHelper`):, 6.2 `CallRecordItem` & `RecordingMetadata` Extensions:, 6. Proposed Database & Model Schema Extension, 9. Recommendation & Review Request, Technical Proposal: Unified Call Recording & Native OEM Import Integration

### Community 100 - "1. System-Level Architectural Limitations"
Cohesion: 0.25
Nodes (7): 1.1 The `CAPTURE_AUDIO_OUTPUT` Security Boundary, 1.2 Android `AudioPolicyService` & `AudioFlinger` Silencing, 1.3 Android 11+ "While-In-Use" Microphone Restrictions, 1.4 Google Play Policy Regarding Accessibility Services, 1. System-Level Architectural Limitations, 2. Summary for End Users & Operators, Call Recording Known Limitations & Technical Explanations

### Community 118 - "CallStateReceiver"
Cohesion: 0.33
Nodes (5): 1. Automated & Emulator Test Results (Pixel 6a / Android 14–17), 2.1 Test Matrix Protocol Checklist, 2.2 OEM Audio Capture Verification Summary, 2. Physical Device Testing Protocol, Call Recording Test Report & Audio Validation Verification

### Community 119 - "db.js"
Cohesion: 0.07
Nodes (7): fs, path, fs, path, crypto, hashPassword(), { Pool }

### Community 121 - "CallStateReceiver"
Cohesion: 0.22
Nodes (9): 2.2 `CallStateMonitor` (Telephony State Detector), 7. Final Technical Architecture Decision, Architectural Rules Enforced in TWING:, 4. Ingestion Architecture: Three Legal Integration Pathways, 8. Implementation Steps & Rollout Strategy, Pathway A: Storage Access Framework (SAF) Folder Link (Recommended for Automatic Sync), Pathway B: Universal "Share to TWING" Intent Handler (Zero Permissions Needed), Pathway C: In-App VoIP Call Recording (Existing & 100% Functional) (+1 more)

### Community 122 - "TWING OEM Call Recording Compatibility & Test Report"
Cohesion: 0.22
Nodes (8): 1. Executive Summary, 2. Multi-OEM Compatibility Matrix, 3. Physical Device Verification Findings, 4. Test Scenarios Executed & Verified, A. Vivo T3x 5G (Android 16), B. OnePlus Nord CE 3 Lite 5G (Android 16), C. Google Phone Dialer (Fallback Mode), TWING OEM Call Recording Compatibility & Test Report

### Community 129 - "chat.js"
Cohesion: 0.08
Nodes (21): multer, multer@^2.5.0, avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir (+13 more)

### Community 135 - "Detailed Test Analysis:"
Cohesion: 0.33
Nodes (6): 3. Controlled Audio Test Protocol & Empirical Measurements, Detailed Test Analysis:, Test 1: Outside of Calls (Idle / Normal State), Test 5: After Call Ends, Test Results Matrix, Tests 2, 3, and 4: During Cellular Call (`MODE_IN_CALL`)

### Community 137 - "3. Manufacturer-by-Manufacturer Native Recording Breakdown"
Cohesion: 0.40
Nodes (5): 3.1 Vivo (Target: Vivo T3x 5G / Funtouch OS 14–16 & OriginOS), 3.2 OnePlus / OPPO / Realme (Target: OnePlus Nord CE 3 Lite 5G / OxygenOS & ColorOS 13–16), 3.3 Samsung (Galaxy S, A, M, F Series / One UI 4–7 on Android 12–16), 3.4 Xiaomi / Redmi / POCO (MIUI 13/14 & HyperOS 1/2 on Android 12–16), 3. Manufacturer-by-Manufacturer Native Recording Breakdown

### Community 138 - "FilmstripAdapter"
Cohesion: 0.36
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 156 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 166 - "com.zez.app.model.ChatMessage"
Cohesion: 0.18
Nodes (3): ChatRowItem, ChatMessageAdapter.OnMessageActionListener, PendingMediaTask

### Community 175 - "ImageEditActivity"
Cohesion: 0.11
Nodes (11): 1. Executive Summary, 2. Baseline Identification & Evidence, 4. Original Gallery Architecture Restored, 5. APK Size Verification, 6. Build & Test Verification, 7. Conclusion, TWING GALLERY 2.0 COMPLETE ROLLBACK & RESTORATION REPORT, ImageEditActivity (+3 more)

### Community 190 - "ChatMessageAdapter"
Cohesion: 0.10
Nodes (3): ChatMessageAdapter, OnMediaRetryListener, OnMessageActionListener

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 592 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **143 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `AlarmItem`, `AppCallItem`, `ContactsActivity`, `Hardware Info & Performance Telemetry (C6)`, `.onItemSelected`, `CallViewHolder`, `RecycleBinAdapter`, `ChatApiClient`, `AppUpdateHelper`, `.setCalls`, `android.view.View`, `Override`, `ChatActivity.java`, `Callback`, `.showUpdateDialog`, `Override`, `ImageViewerActivity`, `ZezApplication.java`, `com.zez.app.model.ChatConversation`, `android.content.Context`, `MainActivity.java`, `ChatLocalDbHelper`, `android.net.Uri`, `com.zez.app.model.CallLogItem`, `PreferencesHelper`, `CallStateReceiver`?**
  _High betweenness centrality (0.078) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `MainActivity` (e.g. with `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` and `B. Manifest Declarations Removed`) actually correct?**
  _`MainActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `AppCallItem` be split into smaller, more focused modules?**
  _Cohesion score 0.09390243902439024 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `ChatActivity.java`, `ChatNotificationManager.java`, `android.content.Context`, `MainActivity.java`, `ContactsActivity`, `com.zez.app.model.ChatMessage`, `ChatMessage`, `ChatLocalDbHelper`, `.applyModeState`, `android.net.Uri`, `ChatApiClient`, `ImageViewerActivity`, `ChatMessageAdapter`, `AlarmRingingService.java`, `android.view.View`, `ChatMessageAdapter`, `Override`?**
  _High betweenness centrality (0.038) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ChatActivity` (e.g. with `B. Manifest Declarations Removed` and `7. Affected Files & Isolation Boundaries`) actually correct?**
  _`ChatActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `Hardware Info & Performance Telemetry (C6)` be split into smaller, more focused modules?**
  _Cohesion score 0.07315233785822021 - nodes in this community are weakly interconnected._