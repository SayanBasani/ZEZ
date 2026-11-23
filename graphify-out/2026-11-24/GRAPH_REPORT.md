# Graph Report - Call Log Mannager  (2026-11-23)

## Corpus Check
- 158 files · ~145,683 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 184 file(s) not represented in the graph (top: .xml 174, .properties 5, (none) 3)
- WARNING: 184 of 356 files seen (52%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 2418 nodes · 6045 edges · 205 communities (63 shown, 142 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 575 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `fd670e34`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AlarmHelper
- b2StorageService.js
- .onBindViewHolder
- e2e_device_test.py
- ChatContactItem
- Hardware Info & Performance Telemetry (C6)
- android.view.View
- ActiveCallManager
- ChatApiClient
- MusicPlayerService.java
- ChatUser
- compilerOptions
- RecycleBinAdapter
- package.json
- index.d.ts
- Override
- AppCallItem
- d_call_log_mannager_zez_backend_src_db_pool
- MusicActivity
- MainActivity
- AppUpdateHelper
- admin.js
- Hardware Info & Performance Telemetry (C27)
- appRelease.js
- MusicPlayerService
- Call Recording Audio Diagnostics & Root-Cause Technical Report
- Override
- test-roster.js
- ChatActivity.java
- .startDirectCall
- CallActivity
- ChatMessage
- android.hardware.camera2.CameraDevice
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- appRelease.js
- android.content.Context
- ImageViewerActivity
- android.os.Bundle
- server.js
- MusicTrack
- okhttp3.Call
- 2. Core Components & Responsibilities
- Call Recording Implementation Audit
- com.zez.app.model.ChatConversation
- callSync.js
- 3. Detailed Component Breakdown
- com.zez.app.music.model.MusicTrack
- ChatLocalDbHelper
- .onItemSelected
- benchmark
- ChatActivity
- 2. Manufacturer-by-Manufacturer Breakdown
- android.net.Uri
- MusicPlayerManager
- 4. Troubleshooting & FAQs
- .onCreate
- com.zez.app.model.CallLogItem
- TWING: Device-Specific Zero-Audio Investigation — Android 16
- AlarmItem
- Technical Proposal: Unified Call Recording & Native OEM Import Integration
- 1. System-Level Architectural Limitations
- Detailed Test Analysis:
- AlbumDetailActivity
- 3. Manufacturer-by-Manufacturer Native Recording Breakdown
- CallStateReceiver
- .toContentValues
- TWING OEM Call Recording Compatibility & Test Report
- NotificationCaptureService.java
- Adapter
- chat.js
- PreferencesHelper
- AlarmRingingService
- { Pool }
- MediaItem
- { Pool }
- CallLogObserver
- FilmstripAdapter
- MusicAdapter
- CallSyncWorker
- MediaPagerAdapter
- com.zez.app.model.ChatMessage
- ImageEditActivity
- .onCreate
- FavoritesHelper
- ApiCallback
- Override
- ChatMessageAdapter
- .onResponse
- B. Manifest Declarations Removed
- Override
- MainActivity.java
- PlayerListener
- com.google.android.material.tabs.TabLayout.OnTabSelectedListener
- .onResponse

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 116 edges
2. `ChatActivity` - 71 edges
3. `MusicActivity` - 51 edges
4. `MusicPlayerManager` - 48 edges
5. `ChatMessageAdapter` - 43 edges
6. `CallActivity` - 43 edges
7. `ChatApiClient` - 42 edges
8. `AppCallItem` - 42 edges
9. `ChatMessage` - 42 edges
10. `ChatCallback` - 39 edges

## Surprising Connections (you probably didn't know these)
- `2.2 `CallStateMonitor` (Telephony State Detector)` --references--> `CallStateReceiver`  [INFERRED]
  CALL_RECORDING_ARCHITECTURE.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `8. Implementation Steps & Rollout Strategy` --references--> `CallStateReceiver`  [INFERRED]
  TWING_CALL_RECORDING_INTEGRATION_PROPOSAL.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `Pathway A: Storage Access Framework (SAF) Folder Link (Recommended for Automatic Sync)` --references--> `CallStateReceiver`  [INFERRED]
  TWING_CALL_RECORDING_INTEGRATION_PROPOSAL.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` --references--> `MainActivity`  [INFERRED]
  CALL_RECORDING_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java
- `B. Manifest Declarations Removed` --references--> `MainActivity`  [INFERRED]
  docs/GALLERY_2_ROLLBACK_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java

## Import Cycles
- None detected.

## Communities (205 total, 142 thin omitted)

### Community 1 - "AlarmHelper"
Cohesion: 0.22
Nodes (3): AlarmHelper, TypeToken<List<AlarmItem>>, ApiClient.AlarmSyncCallback

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "ChatContactItem"
Cohesion: 0.07
Nodes (4): DisplayItem, OnContactClickListener, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.08
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - "android.view.View"
Cohesion: 0.12
Nodes (3): AlarmViewHolder, CallViewHolder, TrackViewHolder

### Community 9 - "ActiveCallManager"
Cohesion: 0.11
Nodes (3): ActiveCallManager, Callback, CallStateListener

### Community 10 - "ChatApiClient"
Cohesion: 0.16
Nodes (7): ChatApiClient, Callback, Callback, Callback, ChatCallback, ContactSyncResult, MediaUploadResult

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - "RecycleBinAdapter"
Cohesion: 0.22
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

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.04
Nodes (27): content, fs, lines, navMatch, fs, path, fs, path (+19 more)

### Community 21 - "MusicActivity"
Cohesion: 0.09
Nodes (6): MusicActivity, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<List<MusicTrack>>, YouTubeApiHelper.Callback<List<MusicTrack>>, TextWatcher, SeekBar.OnSeekBarChangeListener

### Community 24 - "AppUpdateHelper"
Cohesion: 0.17
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 26 - "admin.js"
Cohesion: 0.09
Nodes (14): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+6 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 28 - "appRelease.js"
Cohesion: 0.20
Nodes (8): downloadsDir, express, fs, multer, path, router, storage, upload

### Community 29 - "MusicPlayerService"
Cohesion: 0.11
Nodes (4): MusicBridge, MusicPlayerService, WebViewClient, CustomTarget<Bitmap>

### Community 30 - "Call Recording Audio Diagnostics & Root-Cause Technical Report"
Cohesion: 0.11
Nodes (17): 1.1 The Core Problem, 1.2 The Root Cause, 1. Executive Summary & Root Cause Analysis, 2. Audio Source Investigation Matrix, 3.1 `AudioManager.AudioRecordingCallback` (API 29+), 3.2 Real-Time Peak & Silence Monitor, 3.3 Post-Recording Frame Decoded Sample Validation (`AudioCaptureValidator`), 3.4 Elimination of False Recordings (+9 more)

### Community 31 - "Override"
Cohesion: 0.07
Nodes (11): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, Callback, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult> (+3 more)

### Community 32 - "test-roster.js"
Cohesion: 0.09
Nodes (22): http, run(), http, run(), http, request(), run(), token (+14 more)

### Community 52 - "appRelease.js"
Cohesion: 0.06
Nodes (22): fs, path, fs, path, fs, path, fs, path (+14 more)

### Community 55 - "ImageViewerActivity"
Cohesion: 0.14
Nodes (3): ImageViewerActivity, ViewPager2.OnPageChangeCallback, MediaItem

### Community 56 - "android.os.Bundle"
Cohesion: 0.23
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 58 - "server.js"
Cohesion: 0.10
Nodes (15): callMediaCache, express, router, app, cors, express, fs, path (+7 more)

### Community 60 - "MusicTrack"
Cohesion: 0.11
Nodes (3): MusicTrack, Callback, YouTubeApiHelper

### Community 61 - "okhttp3.Call"
Cohesion: 0.11
Nodes (12): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+4 more)

### Community 63 - "2. Core Components & Responsibilities"
Cohesion: 0.17
Nodes (11): 1. Overview, 2.1 `CallRecordingManager` (Main Coordinator), 2.2 `CallStateMonitor` (Telephony State Detector), 2.3 `RecordingCapabilityDetector` (Device & Policy Inspector), 2.4 `RecordingEngine` (Audio Capture Engine), 2.5 `RecordingStorageManager` (Storage & File Integrity), 2.6 `RecordingDatabaseManager` (Metadata Persistence), 2.7 `RecordingNotificationManager` (User Indicators & Compliance) (+3 more)

### Community 65 - "Call Recording Implementation Audit"
Cohesion: 0.20
Nodes (9): 1.1 Project Structure & Build Configuration, 1.2 Core Functional Modules, 1. Existing Architecture & Baseline, 2. Inventory of Existing Call Recording Files, 3. Untouchable Files & Subsystems, 4. Android Security & Hardware Restrictions Analysis, 5. Proposed Modular Integration Strategy, Call Recording Implementation Audit (+1 more)

### Community 66 - "com.zez.app.model.ChatConversation"
Cohesion: 0.07
Nodes (7): ConversationsAdapter, androidx.recyclerview.widget.DiffUtil.Callback, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, ChatConversation, PhoneHelper

### Community 67 - "callSync.js"
Cohesion: 0.15
Nodes (15): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+7 more)

### Community 68 - "3. Detailed Component Breakdown"
Cohesion: 0.15
Nodes (12): 1. Architectural Overview & Context, 2. Implemented Architecture & Subsystems, 3. Detailed Component Breakdown, 4. Verification and Security Validation, A. Storage Access Framework & Permission Persistence (`OemRecordingImporter.java`), B. Intelligent Pattern & CallLog Correlation Engine (`RecordingMatcher.java`), C. Background Synchronization (`RecordingSyncWorker.java`), D. Universal Sharing Fallback (`RecordingShareActivity.java`) (+4 more)

### Community 69 - "com.zez.app.music.model.MusicTrack"
Cohesion: 0.13
Nodes (5): DownloadCallback, MusicDownloadHelper, TypeToken<List<MusicTrack>>, DownloadListener, WebViewClient

### Community 81 - "2. Manufacturer-by-Manufacturer Breakdown"
Cohesion: 0.20
Nodes (9): 1. Overview, 2.1 Samsung (One UI / Android 10–14), 2.2 Xiaomi / Redmi / POCO (MIUI / HyperOS / Android 10–14), 2.3 OnePlus / Oppo / Realme (ColorOS / OxygenOS / Android 10–14), 2.4 Vivo / iQOO (Funtouch OS / OriginOS / Android 10–14), 2. Manufacturer-by-Manufacturer Breakdown, 3. Comprehensive Device Capability Matrix, 4. Architectural Honesty & Reporting (+1 more)

### Community 82 - "android.net.Uri"
Cohesion: 0.18
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 88 - "4. Troubleshooting & FAQs"
Cohesion: 0.12
Nodes (16): 1. Quick Start Setup (One-Time Configuration), 2. How the Automatic Import Works, 3. Universal Manual Fallback ("Share to TWING"), 4. Troubleshooting & FAQs, OnePlus / OPPO / Realme Phones (OxygenOS / ColorOS), Q1: My recordings are not importing automatically after a call ends., Q2: Why does an old recording say "Audio capture silenced by Android OS"?, Q3: Why does Google Phone say out loud "This call is now being recorded"? (+8 more)

### Community 89 - ".onCreate"
Cohesion: 0.09
Nodes (6): BootReceiver, ChatNotificationHelper, ChatNotificationManager, ChatApiClient.ChatCallback<List<ChatConversation>>, OnChatsUpdateListener, ZezApplication

### Community 94 - "TWING: Device-Specific Zero-Audio Investigation — Android 16"
Cohesion: 0.13
Nodes (14): 1. Executive Summary, 2. Exact Device Profiles & Specifications, 4.1 Telephony Audio Mode Transition, 4.2 `AudioRecordingConfiguration` Parameters During Call, 4. Deep Inspection of Android Recording Configuration, 5.1 Vivo (Funtouch OS) Native Call Recorder, 5.2 OnePlus (OxygenOS / ColorOS) Native Call Recorder, 5. Investigation of OEM-Supported Call Recording Mechanisms (+6 more)

### Community 95 - "AlarmItem"
Cohesion: 0.18
Nodes (4): AlarmAdapter, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem

### Community 99 - "Technical Proposal: Unified Call Recording & Native OEM Import Integration"
Cohesion: 0.14
Nodes (13): 1. Executive Summary & Objective, 2. Physical Device ADB Status Verification, 4. Ingestion Architecture: Three Legal Integration Pathways, 5. Intelligent Call Matching Algorithm (`RecordingMatcher`), 6.1 Database Schema (v4 Upgrade in `CallRecordDbHelper`):, 6.2 `CallRecordItem` & `RecordingMetadata` Extensions:, 6. Proposed Database & Model Schema Extension, 8. Implementation Steps & Rollout Strategy (+5 more)

### Community 100 - "1. System-Level Architectural Limitations"
Cohesion: 0.25
Nodes (7): 1.1 The `CAPTURE_AUDIO_OUTPUT` Security Boundary, 1.2 Android `AudioPolicyService` & `AudioFlinger` Silencing, 1.3 Android 11+ "While-In-Use" Microphone Restrictions, 1.4 Google Play Policy Regarding Accessibility Services, 1. System-Level Architectural Limitations, 2. Summary for End Users & Operators, Call Recording Known Limitations & Technical Explanations

### Community 104 - "Detailed Test Analysis:"
Cohesion: 0.33
Nodes (6): 3. Controlled Audio Test Protocol & Empirical Measurements, Detailed Test Analysis:, Test 1: Outside of Calls (Idle / Normal State), Test 5: After Call Ends, Test Results Matrix, Tests 2, 3, and 4: During Cellular Call (`MODE_IN_CALL`)

### Community 117 - "3. Manufacturer-by-Manufacturer Native Recording Breakdown"
Cohesion: 0.40
Nodes (5): 3.1 Vivo (Target: Vivo T3x 5G / Funtouch OS 14–16 & OriginOS), 3.2 OnePlus / OPPO / Realme (Target: OnePlus Nord CE 3 Lite 5G / OxygenOS & ColorOS 13–16), 3.3 Samsung (Galaxy S, A, M, F Series / One UI 4–7 on Android 12–16), 3.4 Xiaomi / Redmi / POCO (MIUI 13/14 & HyperOS 1/2 on Android 12–16), 3. Manufacturer-by-Manufacturer Native Recording Breakdown

### Community 118 - "CallStateReceiver"
Cohesion: 0.33
Nodes (5): 1. Automated & Emulator Test Results (Pixel 6a / Android 14–17), 2.1 Test Matrix Protocol Checklist, 2.2 OEM Audio Capture Verification Summary, 2. Physical Device Testing Protocol, Call Recording Test Report & Audio Validation Verification

### Community 122 - "TWING OEM Call Recording Compatibility & Test Report"
Cohesion: 0.16
Nodes (11): 7. Final Technical Architecture Decision, Architectural Rules Enforced in TWING:, 1. Executive Summary, 2. Multi-OEM Compatibility Matrix, 3. Physical Device Verification Findings, 4. Test Scenarios Executed & Verified, A. Vivo T3x 5G (Android 16), B. OnePlus Nord CE 3 Lite 5G (Android 16) (+3 more)

### Community 125 - "Adapter"
Cohesion: 0.08
Nodes (8): CallLogAdapter, CallViewHolder, OnRestoreClickListener, RecycleBinAdapter, CallLogItem, MusicSuggestionAdapter, OnSuggestionClickListener, ViewHolder

### Community 129 - "chat.js"
Cohesion: 0.08
Nodes (23): express, multer, express@^5.1.0, multer@^2.5.0, avatarStorage, avatarUploadDir, express, fs (+15 more)

### Community 130 - "PreferencesHelper"
Cohesion: 0.15
Nodes (4): AlarmSyncCallback, ApiClient, DeviceHelper, PreferencesHelper

### Community 133 - "{ Pool }"
Cohesion: 0.07
Nodes (18): crypto, { Pool }, crypto, deviceAuth(), crypto, express, getTimestamp(), httpError() (+10 more)

### Community 134 - "MediaItem"
Cohesion: 0.13
Nodes (4): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, GalleryListItem

### Community 135 - "{ Pool }"
Cohesion: 0.09
Nodes (9): closeDatabase(), crypto, hashPassword(), initializeDatabase(), startServer(), http, path, run() (+1 more)

### Community 138 - "FilmstripAdapter"
Cohesion: 0.36
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 156 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 166 - "com.zez.app.model.ChatMessage"
Cohesion: 0.13
Nodes (4): ChatRowItem, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, PendingMediaTask, PendingMessage

### Community 175 - "ImageEditActivity"
Cohesion: 0.15
Nodes (4): ImageEditActivity, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 179 - ".onCreate"
Cohesion: 0.25
Nodes (3): ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatCallback<MessagesResult>, MessagesResult

### Community 189 - "Override"
Cohesion: 0.07
Nodes (10): android.text.TextWatcher, android.text.TextWatcher, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>>, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.music.player.MusicPlayerManager.PlayerListener, android.text.TextWatcher, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback (+2 more)

### Community 190 - "ChatMessageAdapter"
Cohesion: 0.12
Nodes (4): ChatMessageAdapter, OnMediaRetryListener, OnMessageActionListener, ChatMessageAdapter.OnMessageActionListener

### Community 192 - ".onResponse"
Cohesion: 0.48
Nodes (5): Callback, ConversationModeInfo, IncomingCallInfo, PeerStatus, PollResult

### Community 195 - "B. Manifest Declarations Removed"
Cohesion: 0.12
Nodes (13): 1. Executive Summary, 2. Baseline Identification & Evidence, 3. Scope of Modifications & Discontinued Components, 4. Original Gallery Architecture Restored, 5. APK Size Verification, 6. Build & Test Verification, 7. Conclusion, A. Dependencies Removed (+5 more)

### Community 198 - "MainActivity.java"
Cohesion: 0.06
Nodes (11): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder, HeaderVH (+3 more)

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 638 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **142 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `PreferencesHelper`, `.onBindViewHolder`, `Hardware Info & Performance Telemetry (C6)`, `android.view.View`, `ChatApiClient`, `MusicPlayerService.java`, `RecycleBinAdapter`, `AppCallItem`, `AppUpdateHelper`, `.startDirectCall`, `android.content.Context`, `ImageViewerActivity`, `Override`, `com.zez.app.model.ChatConversation`, `B. Manifest Declarations Removed`, `MainActivity.java`, `.onItemSelected`, `ChatLocalDbHelper`, `PlayerListener`, `.onCreate`, `com.zez.app.model.CallLogItem`, `AlarmItem`, `AlbumDetailActivity`, `CallStateReceiver`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `MainActivity` (e.g. with `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` and `B. Manifest Declarations Removed`) actually correct?**
  _`MainActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ChatContactItem` be split into smaller, more focused modules?**
  _Cohesion score 0.06666666666666667 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `ChatActivity.java`, `B. Manifest Declarations Removed`, `.bind`, `MainActivity.java`, `android.view.View`, `com.zez.app.model.ChatMessage`, `ChatMessage`, `ChatApiClient`, `ChatLocalDbHelper`, `MusicPlayerService.java`, `ImageEditActivity`, `.onCreate`, `android.content.Context`, `.toContentValues`, `ChatActivity.java`, `ChatMessageAdapter`, `Override`?**
  _High betweenness centrality (0.047) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ChatActivity` (e.g. with `B. Manifest Declarations Removed` and `7. Affected Files & Isolation Boundaries`) actually correct?**
  _`ChatActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `Hardware Info & Performance Telemetry (C6)` be split into smaller, more focused modules?**
  _Cohesion score 0.07673469387755102 - nodes in this community are weakly interconnected._