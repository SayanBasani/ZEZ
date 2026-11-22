# Graph Report - Call Log Mannager  (2026-11-22)

## Corpus Check
- 157 files · ~131,143 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 156 file(s) not represented in the graph (top: .xml 146, .properties 5, (none) 3)
- WARNING: 156 of 326 files seen (48%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 2301 nodes · 5819 edges · 122 communities (57 shown, 65 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 678 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `7b98ea67`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- CallActivity
- AlarmItem
- b2StorageService.js
- CallRecordItem
- e2e_device_test.py
- ChatContactItem
- Hardware Info & Performance Telemetry (C6)
- CallRecordingManager
- ChatApiClient
- RecordingMetadata
- .toContentValues
- db.js
- .initViews
- compilerOptions
- .loadGalleryData
- package.json
- index.d.ts
- com.zez.app.model.ChatMessage
- ChatUser
- CallSyncWorker.java
- d_call_log_mannager_zez_backend_src_db_pool
- android.content.Intent
- server.js
- MainActivity
- AppUpdateHelper
- ImageEditActivity
- admin.js
- Hardware Info & Performance Telemetry (C27)
- AudioPlaybackManager
- Call Recording Audio Diagnostics & Root-Cause Technical Report
- Override
- test-roster.js
- android.net.Uri
- Override
- chat.js
- OemRecordingImporter
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- RecordingEngine.java
- appRelease.js
- CallStateReceiver
- ChatMessageAdapter
- ImageViewerActivity
- GalleryAdapter
- CallRecordDbHelper
- extreme-test-suite.js
- ChatNotificationManager
- okhttp3.Call
- 2. Core Components & Responsibilities
- RecordingState
- ConversationsAdapter
- callRead.js
- .matchCall
- android.content.Context
- log
- CallRecordingManager
- .exitSelectMode
- ContactsActivity
- benchmark
- ChatActivity
- CallStateMonitor
- 2. Manufacturer-by-Manufacturer Breakdown
- AudioDiagnostics
- 4. Troubleshooting & FAQs
- com.zez.app.model.CallLogItem
- PreferencesHelper
- AudioCaptureValidator
- CallRecordAdapter
- android.widget.TextView
- TWING: Device-Specific Zero-Audio Investigation — Android 16
- .bind
- MediaPagerAdapter
- .applyModeState
- com.zez.app.model.ChatContactItem
- Technical Proposal: Unified Call Recording & Native OEM Import Integration
- 1. System-Level Architectural Limitations
- .getPrefs
- Detailed Test Analysis:
- test-b2-integration.js
- RecycleBinAdapter
- .onResponse
- .loadHardwarePageData
- .onItemSelected
- android.os.Handler
- CallStateReceiver
- .registerDeviceSync
- .getSpanSizeLookup

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 110 edges
2. `ChatActivity` - 72 edges
3. `CallRecordItem` - 71 edges
4. `RecordingMetadata` - 49 edges
5. `ChatMessage` - 48 edges
6. `ChatMessageAdapter` - 46 edges
7. `AudioDiagnostics` - 42 edges
8. `CallActivity` - 38 edges
9. `ChatApiClient` - 38 edges
10. `CallRecordingManager` - 35 edges

## Surprising Connections (you probably didn't know these)
- `The Production Solution` --references--> `RecordingShareActivity`  [INFERRED]
  TWING_NATIVE_RECORDING_IMPORT_IMPLEMENTATION.md → ZezAndroid/app/src/main/java/com/zez/app/recording/importing/RecordingShareActivity.java
- `C. Google Phone Dialer (Fallback Mode)` --references--> `RecordingShareActivity`  [INFERRED]
  TWING_OEM_COMPATIBILITY_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/recording/importing/RecordingShareActivity.java
- `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` --references--> `MainActivity`  [INFERRED]
  CALL_RECORDING_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java
- `2.2 `CallStateMonitor` (Telephony State Detector)` --references--> `CallStateReceiver`  [INFERRED]
  CALL_RECORDING_ARCHITECTURE.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` --references--> `CallStateReceiver`  [INFERRED]
  CALL_RECORDING_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java

## Import Cycles
- None detected.

## Communities (122 total, 65 thin omitted)

### Community 0 - "CallActivity"
Cohesion: 0.05
Nodes (10): CallActivity, Callback, TextureView.SurfaceTextureListener, CameraDevice.StateCallback, CameraCaptureSession.StateCallback, Runnable, Callback, Runnable (+2 more)

### Community 1 - "AlarmItem"
Cohesion: 0.11
Nodes (7): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.08
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 8 - "ChatApiClient"
Cohesion: 0.20
Nodes (6): ChatApiClient, Callback, ChatCallback, ContactSyncResult, MediaUploadResult, MessagesResult

### Community 10 - ".toContentValues"
Cohesion: 0.09
Nodes (3): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, PendingMediaTask, ChatLocalDbHelper

### Community 11 - "db.js"
Cohesion: 0.07
Nodes (13): fs, path, fs, path, crypto, hashPassword(), { Pool }, crypto (+5 more)

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - ".loadGalleryData"
Cohesion: 0.29
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 15 - "package.json"
Cohesion: 0.07
Nodes (38): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, @noble/ciphers, pg, @types/cors, @types/express (+30 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "com.zez.app.model.ChatMessage"
Cohesion: 0.19
Nodes (3): ChatRowItem, ChatMessageAdapter.OnMessageActionListener, PendingMessage

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.06
Nodes (19): fs, path, fs, path, fs, path, fs, path (+11 more)

### Community 21 - "android.content.Intent"
Cohesion: 0.07
Nodes (12): 1.1 Project Structure & Build Configuration, 1.2 Core Functional Modules, 1. Existing Architecture & Baseline, 2. Inventory of Existing Call Recording Files, 3. Untouchable Files & Subsystems, 4. Android Security & Hardware Restrictions Analysis, 5. Proposed Modular Integration Strategy, Call Recording Implementation Audit (+4 more)

### Community 22 - "server.js"
Cohesion: 0.07
Nodes (29): express, express@^5.1.0, closeDatabase(), initializeDatabase(), downloadsDir, express, fs, multer (+21 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.16
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "ImageEditActivity"
Cohesion: 0.17
Nodes (4): ImageEditActivity, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.09
Nodes (14): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+6 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 30 - "Call Recording Audio Diagnostics & Root-Cause Technical Report"
Cohesion: 0.15
Nodes (12): 1.1 The Core Problem, 1.2 The Root Cause, 1. Executive Summary & Root Cause Analysis, 2. Audio Source Investigation Matrix, 4.1 Samsung (One UI 4 / 5 / 6, Android 12–14), 4.2 Xiaomi / Redmi / POCO (MIUI 13 / 14 & HyperOS, Android 12–14), 4.3 OnePlus / OPPO / Realme (OxygenOS / ColorOS 12 / 13 / 14), 4.4 Vivo / iQOO (Funtouch OS / OriginOS, Android 12–14) (+4 more)

### Community 31 - "Override"
Cohesion: 0.08
Nodes (9): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatMessage>, ProfileMediaAdapter (+1 more)

### Community 32 - "test-roster.js"
Cohesion: 0.09
Nodes (22): http, run(), http, run(), http, request(), run(), token (+14 more)

### Community 33 - "android.net.Uri"
Cohesion: 0.18
Nodes (3): AlbumsAdapter, OnAlbumClickListener, AlbumItem

### Community 35 - "Override"
Cohesion: 0.09
Nodes (7): android.text.TextWatcher, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, android.text.TextWatcher, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback

### Community 36 - "chat.js"
Cohesion: 0.09
Nodes (20): multer, multer@^2.5.0, avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir (+12 more)

### Community 37 - "OemRecordingImporter"
Cohesion: 0.06
Nodes (7): OemRecordingImporter, RecordingCapability, SupportLevel, AMBIENT_MIC_ONLY, FULL_SUPPORT, RESTRICTED_BY_OS, RecordingStorageManager

### Community 52 - "appRelease.js"
Cohesion: 0.09
Nodes (12): fs, path, fs, path, fs, path, fs, path (+4 more)

### Community 53 - "CallStateReceiver"
Cohesion: 0.12
Nodes (17): 2.2 `CallStateMonitor` (Telephony State Detector), 7. Final Technical Architecture Decision, Architectural Rules Enforced in TWING:, 4. Ingestion Architecture: Three Legal Integration Pathways, 8. Implementation Steps & Rollout Strategy, Pathway A: Storage Access Framework (SAF) Folder Link (Recommended for Automatic Sync), Pathway B: Universal "Share to TWING" Intent Handler (Zero Permissions Needed), Pathway C: In-App VoIP Call Recording (Existing & 100% Functional) (+9 more)

### Community 54 - "ChatMessageAdapter"
Cohesion: 0.12
Nodes (3): ChatMessageAdapter, OnMediaRetryListener, OnMessageActionListener

### Community 56 - "GalleryAdapter"
Cohesion: 0.14
Nodes (4): GalleryAdapter, Listener, GalleryListItem, MediaItem

### Community 59 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 60 - "ChatNotificationManager"
Cohesion: 0.10
Nodes (6): BootReceiver, ChatNotificationHelper, ChatNotificationManager, ChatApiClient.ChatCallback<List<ChatConversation>>, OnChatsUpdateListener, ZezApplication

### Community 61 - "okhttp3.Call"
Cohesion: 0.06
Nodes (21): Callback, IncomingCallActivity, Callback, Callback, okhttp3.Callback, Callback, ChatCallback<MessagesResult>, Callback (+13 more)

### Community 63 - "2. Core Components & Responsibilities"
Cohesion: 0.14
Nodes (12): 1. Overview, 2.1 `CallRecordingManager` (Main Coordinator), 2.3 `RecordingCapabilityDetector` (Device & Policy Inspector), 2.4 `RecordingEngine` (Audio Capture Engine), 2.5 `RecordingStorageManager` (Storage & File Integrity), 2.6 `RecordingDatabaseManager` (Metadata Persistence), 2.7 `RecordingNotificationManager` (User Indicators & Compliance), 2.8 `RecordingPermissionManager` (Consent & Runtime Checks) (+4 more)

### Community 64 - "RecordingState"
Cohesion: 0.14
Nodes (9): RecordingState, AUDIO_CAPTURE_CONFIRMED, AUDIO_CAPTURE_SILENCED, CALL_DETECTED, IDLE, RECORDING_ACTIVE, RECORDING_COMPLETED, RECORDING_FAILED (+1 more)

### Community 66 - "ConversationsAdapter"
Cohesion: 0.07
Nodes (8): ConversationsAdapter, androidx.recyclerview.widget.DiffUtil.Callback, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, ChatConversation, Callback, PhoneHelper

### Community 67 - "callRead.js"
Cohesion: 0.14
Nodes (17): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+9 more)

### Community 68 - ".matchCall"
Cohesion: 0.09
Nodes (15): 1. Architectural Overview & Context, 2. Implemented Architecture & Subsystems, 3. Detailed Component Breakdown, 4. Verification and Security Validation, A. Storage Access Framework & Permission Persistence (`OemRecordingImporter.java`), B. Intelligent Pattern & CallLog Correlation Engine (`RecordingMatcher.java`), C. Background Synchronization (`RecordingSyncWorker.java`), D. Universal Sharing Fallback (`RecordingShareActivity.java`) (+7 more)

### Community 71 - "CallRecordingManager"
Cohesion: 0.12
Nodes (3): CallRecordingManager, RecordingStateListener, RecordingPermissionManager

### Community 73 - "ContactsActivity"
Cohesion: 0.12
Nodes (4): 7. Affected Files & Isolation Boundaries, AlarmFiringActivity, ContactsActivity, RecordingShareActivity

### Community 81 - "2. Manufacturer-by-Manufacturer Breakdown"
Cohesion: 0.22
Nodes (8): 1. Overview, 2.1 Samsung (One UI / Android 10–14), 2.2 Xiaomi / Redmi / POCO (MIUI / HyperOS / Android 10–14), 2.3 OnePlus / Oppo / Realme (ColorOS / OxygenOS / Android 10–14), 2.4 Vivo / iQOO (Funtouch OS / OriginOS / Android 10–14), 2. Manufacturer-by-Manufacturer Breakdown, 3. Comprehensive Device Capability Matrix, Call Recording Compatibility Matrix & OEM Analysis

### Community 86 - "AudioDiagnostics"
Cohesion: 0.07
Nodes (3): AudioDiagnostics, RecordingDiagnosticListener, RecordingEngine

### Community 88 - "4. Troubleshooting & FAQs"
Cohesion: 0.12
Nodes (16): 1. Quick Start Setup (One-Time Configuration), 2. How the Automatic Import Works, 3. Universal Manual Fallback ("Share to TWING"), 4. Troubleshooting & FAQs, OnePlus / OPPO / Realme Phones (OxygenOS / ColorOS), Q1: My recordings are not importing automatically after a call ends., Q2: Why does an old recording say "Audio capture silenced by Android OS"?, Q3: Why does Google Phone say out loud "This call is now being recorded"? (+8 more)

### Community 89 - "com.zez.app.model.CallLogItem"
Cohesion: 0.23
Nodes (3): CallLogAdapter, CallViewHolder, CallLogHelper

### Community 91 - "AudioCaptureValidator"
Cohesion: 0.25
Nodes (7): 3.1 `AudioManager.AudioRecordingCallback` (API 29+), 3.2 Real-Time Peak & Silence Monitor, 3.3 Post-Recording Frame Decoded Sample Validation (`AudioCaptureValidator`), 3.4 Elimination of False Recordings, 3. Real Audio Validation & Diagnostic Architecture (v1.0.30), AudioCaptureValidator, PcmAnalysisResult

### Community 92 - "CallRecordAdapter"
Cohesion: 0.18
Nodes (3): CallRecordAdapter, OnRecordDeletedListener, SeekBar.OnSeekBarChangeListener

### Community 93 - "android.widget.TextView"
Cohesion: 0.10
Nodes (14): AlbumVH, RecordViewHolder, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder (+6 more)

### Community 94 - "TWING: Device-Specific Zero-Audio Investigation — Android 16"
Cohesion: 0.13
Nodes (14): 1. Executive Summary, 2. Exact Device Profiles & Specifications, 4.1 Telephony Audio Mode Transition, 4.2 `AudioRecordingConfiguration` Parameters During Call, 4. Deep Inspection of Android Recording Configuration, 5.1 Vivo (Funtouch OS) Native Call Recorder, 5.2 OnePlus (OxygenOS / ColorOS) Native Call Recorder, 5. Investigation of OEM-Supported Call Recording Mechanisms (+6 more)

### Community 96 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 99 - "Technical Proposal: Unified Call Recording & Native OEM Import Integration"
Cohesion: 0.14
Nodes (13): 1. Executive Summary & Objective, 2. Physical Device ADB Status Verification, 3.1 Vivo (Target: Vivo T3x 5G / Funtouch OS 14–16 & OriginOS), 3.2 OnePlus / OPPO / Realme (Target: OnePlus Nord CE 3 Lite 5G / OxygenOS & ColorOS 13–16), 3.3 Samsung (Galaxy S, A, M, F Series / One UI 4–7 on Android 12–16), 3.4 Xiaomi / Redmi / POCO (MIUI 13/14 & HyperOS 1/2 on Android 12–16), 3. Manufacturer-by-Manufacturer Native Recording Breakdown, 5. Intelligent Call Matching Algorithm (`RecordingMatcher`) (+5 more)

### Community 100 - "1. System-Level Architectural Limitations"
Cohesion: 0.25
Nodes (7): 1.1 The `CAPTURE_AUDIO_OUTPUT` Security Boundary, 1.2 Android `AudioPolicyService` & `AudioFlinger` Silencing, 1.3 Android 11+ "While-In-Use" Microphone Restrictions, 1.4 Google Play Policy Regarding Accessibility Services, 1. System-Level Architectural Limitations, 2. Summary for End Users & Operators, Call Recording Known Limitations & Technical Explanations

### Community 102 - "Detailed Test Analysis:"
Cohesion: 0.33
Nodes (6): 3. Controlled Audio Test Protocol & Empirical Measurements, Detailed Test Analysis:, Test 1: Outside of Calls (Idle / Normal State), Test 5: After Call Ends, Test Results Matrix, Tests 2, 3, and 4: During Cellular Call (`MODE_IN_CALL`)

### Community 106 - "test-b2-integration.js"
Cohesion: 0.24
Nodes (7): app, fs, http, path, request(), runB2Tests(), sendMultipart()

### Community 108 - "RecycleBinAdapter"
Cohesion: 0.36
Nodes (3): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter

### Community 118 - "CallStateReceiver"
Cohesion: 0.33
Nodes (5): 1. Automated & Emulator Test Results (Pixel 6a / Android 14–17), 2.1 Test Matrix Protocol Checklist, 2.2 OEM Audio Capture Verification Summary, 2. Physical Device Testing Protocol, Call Recording Test Report & Audio Validation Verification

### Community 119 - ".registerDeviceSync"
Cohesion: 0.23
Nodes (3): ApiCallback, ApiClient, DeviceHelper

## Knowledge Gaps
- **15 isolated node(s):** `AMBIENT_MIC_ONLY`, `FULL_SUPPORT`, `RESTRICTED_BY_OS`, `AUDIO_CAPTURE_CONFIRMED`, `AUDIO_CAPTURE_SILENCED` (+10 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 548 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **65 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `AlarmItem`, `CallRecordItem`, `Hardware Info & Performance Telemetry (C6)`, `CallRecordingManager`, `ChatApiClient`, `.initViews`, `.loadGalleryData`, `AppUpdateHelper`, `Override`, `Override`, `OemRecordingImporter`, `ImageViewerActivity`, `ChatNotificationManager`, `ConversationsAdapter`, `android.content.Context`, `.exitSelectMode`, `ContactsActivity`, `com.zez.app.model.CallLogItem`, `PreferencesHelper`, `CallRecordAdapter`, `android.widget.TextView`, `.confirmDelete`, `.loadCallRecordingsData`, `.loadHardwarePageData`, `.onItemSelected`, `CallStateReceiver`, `.registerDeviceSync`?**
  _High betweenness centrality (0.079) - this node is a cross-community bridge._
- **What connects `AMBIENT_MIC_ONLY`, `FULL_SUPPORT`, `RESTRICTED_BY_OS` to the rest of the system?**
  _15 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `CallActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.05368421052631579 - nodes in this community are weakly interconnected._
- **Why does `CallRecordItem` connect `CallRecordItem` to `ChatActivity.java`, `OemRecordingImporter`, `log`, `CallRecordingManager`, `RecordingMetadata`, `.confirmDelete`, `ImageViewerActivity.java`, `.loadCallRecordingsData`, `MainActivity`, `CallRecordDbHelper`, `CallRecordAdapter`, `list`?**
  _High betweenness centrality (0.059) - this node is a cross-community bridge._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.11219512195121951 - nodes in this community are weakly interconnected._
- **Why does `RecordingEngine` connect `AudioDiagnostics` to `CallActivity`, `android.content.Context`, `CallRecordingManager`, `RecordingEngine.java`, `android.os.Handler`?**
  _High betweenness centrality (0.039) - this node is a cross-community bridge._
- **Should `CallRecordItem` be split into smaller, more focused modules?**
  _Cohesion score 0.09716599190283401 - nodes in this community are weakly interconnected._