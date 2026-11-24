# Graph Report - Call Log Mannager  (2026-11-24)

## Corpus Check
- 164 files · ~154,509 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 191 file(s) not represented in the graph (top: .xml 181, .properties 5, (none) 3)
- WARNING: 191 of 369 files seen (52%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 2596 nodes · 6703 edges · 207 communities (67 shown, 140 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 673 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f8dad011`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- .showMediaLinksDocsDialog
- b2StorageService.js
- test_backend_suite.js
- e2e_device_test.py
- ChatContactItem
- Hardware Info & Performance Telemetry (C6)
- android.view.View
- ActiveCallManager
- ChatApiClient
- DownloadTask
- ChatUser
- compilerOptions
- ContactsAdapter
- package.json
- index.d.ts
- db.js
- AlarmHelper
- AppCallItem
- d_call_log_mannager_zez_backend_src_db_pool
- Override
- MainActivity
- AppUpdateHelper
- admin.js
- Hardware Info & Performance Telemetry (C27)
- ChatActivity
- MusicPlayerService
- Call Recording Audio Diagnostics & Root-Cause Technical Report
- Override
- test-roster.js
- CallActivity.java
- ChatNotificationManager
- Override
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- appRelease.js
- android.content.Context
- ImageViewerActivity
- IncomingCallActivity
- server.js
- YouTubeApiHelper
- okhttp3.Call
- 2. Core Components & Responsibilities
- Call Recording Implementation Audit
- com.zez.app.model.ChatConversation
- callSync.js
- 3. Detailed Component Breakdown
- com.zez.app.music.model.MusicTrack
- .loadGalleryData
- CallStateReceiver
- benchmark
- Adapter
- 2. Manufacturer-by-Manufacturer Breakdown
- .onBindViewHolder
- android.hardware.camera2.CameraDevice
- MusicPlayerManager
- 4. Troubleshooting & FAQs
- .startDirectCall
- .getMessages
- TWING: Device-Specific Zero-Audio Investigation — Android 16
- AlarmItem
- Technical Proposal: Unified Call Recording & Native OEM Import Integration
- 1. System-Level Architectural Limitations
- Detailed Test Analysis:
- com.zez.app.model.CallLogItem
- ChatLocalDbHelper
- CallStateReceiver
- .toContentValues
- TWING OEM Call Recording Compatibility & Test Report
- NotificationCaptureService.java
- AlbumDetailActivity
- chat.js
- PreferencesHelper
- GalleryAdapter
- check-recordings.js
- CallActivity
- AlarmRingingService
- com.zez.app.model.ChatMessage
- dependencies
- android.net.Uri
- extreme-test-suite.js
- com.google.android.material.tabs.TabLayout.OnTabSelectedListener
- .getSpanSizeLookup
- MusicPlayerService.java
- MediaPagerAdapter
- MusicTrack
- B. Manifest Declarations Removed
- PhoneHelper
- FavoritesHelper
- Override
- ChatMessageAdapter
- FilmstripAdapter
- scripts
- 3. Manufacturer-by-Manufacturer Native Recording Breakdown
- Override
- MainActivity.java
- .onResponse
- engines
- PlayerListener
- .initThemeSpinner
- okhttp3.Call
- ApiCallback

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 116 edges
2. `ChatActivity` - 76 edges
3. `MusicActivity` - 58 edges
4. `MusicPlayerManager` - 47 edges
5. `MusicPlayerService` - 45 edges
6. `DownloadTask` - 44 edges
7. `CallActivity` - 43 edges
8. `ChatMessageAdapter` - 43 edges
9. `ChatApiClient` - 42 edges
10. `AppCallItem` - 42 edges

## Surprising Connections (you probably didn't know these)
- `4. Test Scenarios Executed & Verified` --references--> `CallStateReceiver`  [INFERRED]
  TWING_OEM_COMPATIBILITY_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `A. Vivo T3x 5G (Android 16)` --references--> `CallStateReceiver`  [INFERRED]
  TWING_OEM_COMPATIBILITY_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `1.2 Core Functional Modules` --references--> `CallActivity`  [INFERRED]
  CALL_RECORDING_IMPLEMENTATION_AUDIT.md → ZezAndroid/app/src/main/java/com/zez/app/CallActivity.java
- `B. Manifest Declarations Removed` --references--> `CallActivity`  [INFERRED]
  docs/GALLERY_2_ROLLBACK_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/CallActivity.java
- `6. Build & Test Verification` --references--> `ContactsActivity`  [INFERRED]
  docs/GALLERY_2_ROLLBACK_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/ContactsActivity.java

## Import Cycles
- None detected.

## Communities (207 total, 140 thin omitted)

### Community 1 - ".showMediaLinksDocsDialog"
Cohesion: 0.13
Nodes (11): DocsAdapter, Holder, LinkItem, LinksAdapter, MediaGridAdapter, MediaLinksDocsHelper, OnDeleteMessagesListener, OnDocGoToChatListener (+3 more)

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 3 - "test_backend_suite.js"
Cohesion: 0.12
Nodes (14): closeDatabase(), initializeDatabase(), app, startServer(), http, path, run(), tests (+6 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "ChatContactItem"
Cohesion: 0.10
Nodes (4): DisplayItem, OnContactClickListener, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.05
Nodes (9): ImageEditActivity, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, ImageEditHelper, TelemetryGraphView, GestureListener, OnTapListener, ScaleListener (+1 more)

### Community 7 - "android.view.View"
Cohesion: 0.10
Nodes (9): CallViewHolder, DateHeaderViewHolder, ViewHolder, FilmstripVH, HeaderVH, MediaVH, BinViewHolder, DownloadViewHolder (+1 more)

### Community 10 - "ChatApiClient"
Cohesion: 0.22
Nodes (5): ChatApiClient, Callback, ChatCallback, ContactSyncResult, MediaUploadResult

### Community 11 - "DownloadTask"
Cohesion: 0.08
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, DownloadProgressListener, MusicDownloadManager

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - "ContactsAdapter"
Cohesion: 0.30
Nodes (3): ContactsAdapter, ContactViewHolder, HeaderViewHolder

### Community 15 - "package.json"
Cohesion: 0.14
Nodes (18): @types/cors, @types/express, @types/multer, @types/node, @types/pg, typescript, description, devDependencies (+10 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "db.js"
Cohesion: 0.07
Nodes (11): fs, path, crypto, crypto, hashPassword(), { Pool }, crypto, deviceAuth() (+3 more)

### Community 18 - "AlarmHelper"
Cohesion: 0.22
Nodes (3): AlarmHelper, TypeToken<List<AlarmItem>>, ApiClient.AlarmSyncCallback

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.04
Nodes (29): content, fs, lines, navMatch, fs, path, fs, path (+21 more)

### Community 21 - "Override"
Cohesion: 0.10
Nodes (5): YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<List<MusicTrack>>, YouTubeApiHelper.Callback<List<MusicTrack>>, TextWatcher, SeekBar.OnSeekBarChangeListener

### Community 24 - "AppUpdateHelper"
Cohesion: 0.16
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 26 - "admin.js"
Cohesion: 0.09
Nodes (14): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+6 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 29 - "MusicPlayerService"
Cohesion: 0.08
Nodes (6): MusicBridge, MusicPlayerService, WebViewClient, CustomTarget<Bitmap>, MediaSessionCompat.Callback, Callback

### Community 30 - "Call Recording Audio Diagnostics & Root-Cause Technical Report"
Cohesion: 0.11
Nodes (17): 1.1 The Core Problem, 1.2 The Root Cause, 1. Executive Summary & Root Cause Analysis, 2. Audio Source Investigation Matrix, 3.1 `AudioManager.AudioRecordingCallback` (API 29+), 3.2 Real-Time Peak & Silence Monitor, 3.3 Post-Recording Frame Decoded Sample Validation (`AudioCaptureValidator`), 3.4 Elimination of False Recordings (+9 more)

### Community 31 - "Override"
Cohesion: 0.06
Nodes (12): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, android.text.TextWatcher, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener (+4 more)

### Community 32 - "test-roster.js"
Cohesion: 0.09
Nodes (22): http, run(), http, run(), http, request(), run(), token (+14 more)

### Community 35 - "ChatNotificationManager"
Cohesion: 0.11
Nodes (5): BootReceiver, ChatNotificationHelper, ChatNotificationManager, OnChatsUpdateListener, ZezApplication

### Community 36 - "Override"
Cohesion: 0.15
Nodes (7): Callback, TextureView.SurfaceTextureListener, ActiveCallManager.CallStateListener, Runnable, Callback, Runnable, Callback

### Community 52 - "appRelease.js"
Cohesion: 0.06
Nodes (20): fs, path, fs, path, fs, path, fs, path (+12 more)

### Community 55 - "ImageViewerActivity"
Cohesion: 0.15
Nodes (3): 7. Affected Files & Isolation Boundaries, ImageViewerActivity, ViewPager2.OnPageChangeCallback

### Community 58 - "server.js"
Cohesion: 0.07
Nodes (25): express, express@^5.1.0, downloadsDir, express, fs, multer, path, router (+17 more)

### Community 60 - "YouTubeApiHelper"
Cohesion: 0.19
Nodes (3): Callback, YouTubeApiHelper, Dns

### Community 61 - "okhttp3.Call"
Cohesion: 0.09
Nodes (13): Callback, Callback, Callback, ChatCallback<MessagesResult>, Callback, Callback, Callback, Callback (+5 more)

### Community 63 - "2. Core Components & Responsibilities"
Cohesion: 0.18
Nodes (10): 1. Overview, 2.1 `CallRecordingManager` (Main Coordinator), 2.3 `RecordingCapabilityDetector` (Device & Policy Inspector), 2.4 `RecordingEngine` (Audio Capture Engine), 2.5 `RecordingStorageManager` (Storage & File Integrity), 2.6 `RecordingDatabaseManager` (Metadata Persistence), 2.7 `RecordingNotificationManager` (User Indicators & Compliance), 2.8 `RecordingPermissionManager` (Consent & Runtime Checks) (+2 more)

### Community 65 - "Call Recording Implementation Audit"
Cohesion: 0.20
Nodes (9): 1.1 Project Structure & Build Configuration, 1.2 Core Functional Modules, 1. Existing Architecture & Baseline, 2. Inventory of Existing Call Recording Files, 3. Untouchable Files & Subsystems, 4. Android Security & Hardware Restrictions Analysis, 5. Proposed Modular Integration Strategy, Call Recording Implementation Audit (+1 more)

### Community 66 - "com.zez.app.model.ChatConversation"
Cohesion: 0.07
Nodes (7): ConversationsAdapter, androidx.recyclerview.widget.DiffUtil.Callback, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, ChatConversation, ChatApiClient.ChatCallback<List<ChatConversation>>

### Community 67 - "callSync.js"
Cohesion: 0.11
Nodes (21): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+13 more)

### Community 68 - "3. Detailed Component Breakdown"
Cohesion: 0.15
Nodes (12): 1. Architectural Overview & Context, 2. Implemented Architecture & Subsystems, 3. Detailed Component Breakdown, 4. Verification and Security Validation, A. Storage Access Framework & Permission Persistence (`OemRecordingImporter.java`), B. Intelligent Pattern & CallLog Correlation Engine (`RecordingMatcher.java`), C. Background Synchronization (`RecordingSyncWorker.java`), D. Universal Sharing Fallback (`RecordingShareActivity.java`) (+4 more)

### Community 69 - "com.zez.app.music.model.MusicTrack"
Cohesion: 0.14
Nodes (5): MusicAdapter, OnTrackClickListener, DownloadCallback, MusicDownloadHelper, TypeToken<List<MusicTrack>>

### Community 70 - ".loadGalleryData"
Cohesion: 0.22
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 71 - "CallStateReceiver"
Cohesion: 0.22
Nodes (9): 2.2 `CallStateMonitor` (Telephony State Detector), 7. Final Technical Architecture Decision, Architectural Rules Enforced in TWING:, 4. Ingestion Architecture: Three Legal Integration Pathways, 8. Implementation Steps & Rollout Strategy, Pathway A: Storage Access Framework (SAF) Folder Link (Recommended for Automatic Sync), Pathway B: Universal "Share to TWING" Intent Handler (Zero Permissions Needed), Pathway C: In-App VoIP Call Recording (Existing & 100% Functional) (+1 more)

### Community 77 - "Adapter"
Cohesion: 0.08
Nodes (8): CallLogAdapter, CallViewHolder, OnRestoreClickListener, RecycleBinAdapter, CallLogItem, MusicSuggestionAdapter, OnSuggestionClickListener, ViewHolder

### Community 81 - "2. Manufacturer-by-Manufacturer Breakdown"
Cohesion: 0.20
Nodes (9): 1. Overview, 2.1 Samsung (One UI / Android 10–14), 2.2 Xiaomi / Redmi / POCO (MIUI / HyperOS / Android 10–14), 2.3 OnePlus / Oppo / Realme (ColorOS / OxygenOS / Android 10–14), 2.4 Vivo / iQOO (Funtouch OS / OriginOS / Android 10–14), 2. Manufacturer-by-Manufacturer Breakdown, 3. Comprehensive Device Capability Matrix, 4. Architectural Honesty & Reporting (+1 more)

### Community 88 - "4. Troubleshooting & FAQs"
Cohesion: 0.12
Nodes (16): 1. Quick Start Setup (One-Time Configuration), 2. How the Automatic Import Works, 3. Universal Manual Fallback ("Share to TWING"), 4. Troubleshooting & FAQs, OnePlus / OPPO / Realme Phones (OxygenOS / ColorOS), Q1: My recordings are not importing automatically after a call ends., Q2: Why does an old recording say "Audio capture silenced by Android OS"?, Q3: Why does Google Phone say out loud "This call is now being recorded"? (+8 more)

### Community 90 - ".getMessages"
Cohesion: 0.36
Nodes (6): Callback, ConversationModeInfo, IncomingCallInfo, MessagesResult, PeerStatus, PollResult

### Community 94 - "TWING: Device-Specific Zero-Audio Investigation — Android 16"
Cohesion: 0.13
Nodes (14): 1. Executive Summary, 2. Exact Device Profiles & Specifications, 4.1 Telephony Audio Mode Transition, 4.2 `AudioRecordingConfiguration` Parameters During Call, 4. Deep Inspection of Android Recording Configuration, 5.1 Vivo (Funtouch OS) Native Call Recorder, 5.2 OnePlus (OxygenOS / ColorOS) Native Call Recorder, 5. Investigation of OEM-Supported Call Recording Mechanisms (+6 more)

### Community 95 - "AlarmItem"
Cohesion: 0.10
Nodes (5): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem

### Community 99 - "Technical Proposal: Unified Call Recording & Native OEM Import Integration"
Cohesion: 0.22
Nodes (8): 1. Executive Summary & Objective, 2. Physical Device ADB Status Verification, 5. Intelligent Call Matching Algorithm (`RecordingMatcher`), 6.1 Database Schema (v4 Upgrade in `CallRecordDbHelper`):, 6.2 `CallRecordItem` & `RecordingMetadata` Extensions:, 6. Proposed Database & Model Schema Extension, 9. Recommendation & Review Request, Technical Proposal: Unified Call Recording & Native OEM Import Integration

### Community 100 - "1. System-Level Architectural Limitations"
Cohesion: 0.25
Nodes (7): 1.1 The `CAPTURE_AUDIO_OUTPUT` Security Boundary, 1.2 Android `AudioPolicyService` & `AudioFlinger` Silencing, 1.3 Android 11+ "While-In-Use" Microphone Restrictions, 1.4 Google Play Policy Regarding Accessibility Services, 1. System-Level Architectural Limitations, 2. Summary for End Users & Operators, Call Recording Known Limitations & Technical Explanations

### Community 104 - "Detailed Test Analysis:"
Cohesion: 0.33
Nodes (6): 3. Controlled Audio Test Protocol & Empirical Measurements, Detailed Test Analysis:, Test 1: Outside of Calls (Idle / Normal State), Test 5: After Call Ends, Test Results Matrix, Tests 2, 3, and 4: During Cellular Call (`MODE_IN_CALL`)

### Community 110 - "com.zez.app.model.CallLogItem"
Cohesion: 0.11
Nodes (4): CallLogListener, CallLogObserver, CallSyncWorker, CallLogHelper

### Community 118 - "CallStateReceiver"
Cohesion: 0.33
Nodes (5): 1. Automated & Emulator Test Results (Pixel 6a / Android 14–17), 2.1 Test Matrix Protocol Checklist, 2.2 OEM Audio Capture Verification Summary, 2. Physical Device Testing Protocol, Call Recording Test Report & Audio Validation Verification

### Community 122 - "TWING OEM Call Recording Compatibility & Test Report"
Cohesion: 0.22
Nodes (8): 1. Executive Summary, 2. Multi-OEM Compatibility Matrix, 3. Physical Device Verification Findings, 4. Test Scenarios Executed & Verified, A. Vivo T3x 5G (Android 16), B. OnePlus Nord CE 3 Lite 5G (Android 16), C. Google Phone Dialer (Fallback Mode), TWING OEM Call Recording Compatibility & Test Report

### Community 129 - "chat.js"
Cohesion: 0.08
Nodes (22): multer, multer@^2.5.0, avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir (+14 more)

### Community 130 - "PreferencesHelper"
Cohesion: 0.15
Nodes (4): AlarmSyncCallback, ApiClient, DeviceHelper, PreferencesHelper

### Community 131 - "GalleryAdapter"
Cohesion: 0.12
Nodes (4): GalleryAdapter, Listener, GalleryListItem, MediaItem

### Community 132 - "check-recordings.js"
Cohesion: 0.15
Nodes (10): @aws-sdk/client-s3, pg, @aws-sdk/client-s3@^3.1173.0, pg@^8.23.1, { Pool }, s3Client, { S3Client, ListObjectsV2Command }, { Pool } (+2 more)

### Community 136 - "com.zez.app.model.ChatMessage"
Cohesion: 0.14
Nodes (5): ChatRowItem, ChatMessageAdapter.OnMessageActionListener, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, PendingMediaTask, PendingMessage

### Community 137 - "dependencies"
Cohesion: 0.18
Nodes (11): @aws-sdk/s3-request-presigner, cors, @distube/ytdl-core, dotenv, @noble/ciphers, dependencies, @aws-sdk/s3-request-presigner@^3.1173.0, cors@^2.8.5 (+3 more)

### Community 138 - "android.net.Uri"
Cohesion: 0.15
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 139 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 156 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 175 - "B. Manifest Declarations Removed"
Cohesion: 0.17
Nodes (11): 1. Executive Summary, 2. Baseline Identification & Evidence, 3. Scope of Modifications & Discontinued Components, 4. Original Gallery Architecture Restored, 5. APK Size Verification, 6. Build & Test Verification, 7. Conclusion, A. Dependencies Removed (+3 more)

### Community 189 - "Override"
Cohesion: 0.07
Nodes (9): android.text.TextWatcher, android.text.TextWatcher, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.music.player.MusicPlayerManager.PlayerListener, android.text.TextWatcher, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, Runnable (+1 more)

### Community 190 - "ChatMessageAdapter"
Cohesion: 0.12
Nodes (3): ChatMessageAdapter, OnMediaRetryListener, OnMessageActionListener

### Community 195 - "scripts"
Cohesion: 0.40
Nodes (5): scripts, build, dev, start, type-check

### Community 196 - "3. Manufacturer-by-Manufacturer Native Recording Breakdown"
Cohesion: 0.40
Nodes (5): 3.1 Vivo (Target: Vivo T3x 5G / Funtouch OS 14–16 & OriginOS), 3.2 OnePlus / OPPO / Realme (Target: OnePlus Nord CE 3 Lite 5G / OxygenOS & ColorOS 13–16), 3.3 Samsung (Galaxy S, A, M, F Series / One UI 4–7 on Android 12–16), 3.4 Xiaomi / Redmi / POCO (MIUI 13/14 & HyperOS 1/2 on Android 12–16), 3. Manufacturer-by-Manufacturer Native Recording Breakdown

### Community 198 - "MainActivity.java"
Cohesion: 0.07
Nodes (5): IncomingViewHolder, OutgoingViewHolder, AlarmFiringActivity, ContactsActivity, MusicActivity

### Community 207 - "okhttp3.Call"
Cohesion: 0.14
Nodes (5): Callback, Callback, Callback, Callback, Callback

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 650 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **140 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `PreferencesHelper`, `GalleryAdapter`, `Hardware Info & Performance Telemetry (C6)`, `android.view.View`, `ChatApiClient`, `AppCallItem`, `AppUpdateHelper`, `ChatNotificationManager`, `B. Manifest Declarations Removed`, `android.content.Context`, `Override`, `FilmstripAdapter`, `com.zez.app.model.ChatConversation`, `MainActivity.java`, `.loadGalleryData`, `PlayerListener`, `.initThemeSpinner`, `.onBindViewHolder`, `.startDirectCall`, `AlarmItem`, `com.zez.app.model.CallLogItem`, `ChatLocalDbHelper`, `CallStateReceiver`, `AlbumDetailActivity`?**
  _High betweenness centrality (0.079) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `MainActivity` (e.g. with `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` and `B. Manifest Declarations Removed`) actually correct?**
  _`MainActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.showMediaLinksDocsDialog` be split into smaller, more focused modules?**
  _Cohesion score 0.13306451612903225 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `CallActivity.java`, `.bind`, `MainActivity.java`, `android.view.View`, `com.zez.app.model.ChatMessage`, `ChatMessage`, `B. Manifest Declarations Removed`, `.toContentValues`, `ChatLocalDbHelper`, `android.content.Context`, `ImageViewerActivity`, `MusicPlayerService.java`, `ChatMessageAdapter`, `Override`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ChatActivity` (e.g. with `B. Manifest Declarations Removed` and `7. Affected Files & Isolation Boundaries`) actually correct?**
  _`ChatActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `test_backend_suite.js` be split into smaller, more focused modules?**
  _Cohesion score 0.12380952380952381 - nodes in this community are weakly interconnected._