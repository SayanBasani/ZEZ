# Graph Report - Call Log Mannager  (2026-11-24)

## Corpus Check
- 169 files · ~158,651 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 216 file(s) not represented in the graph (top: .xml 206, .properties 5, (none) 3)
- WARNING: 216 of 399 files seen (54%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 2705 nodes · 7041 edges · 218 communities (71 shown, 147 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 713 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ab8e57f5`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Adapter
- b2StorageService.js
- test_backend_suite.js
- e2e_device_test.py
- ContactsActivity
- Hardware Info & Performance Telemetry (C6)
- ActiveCallManager
- ChatApiClient
- DownloadTask
- ChatUser
- compilerOptions
- server.js
- package.json
- index.d.ts
- MediaItem
- AlarmHelper
- AppCallItem
- d_call_log_mannager_zez_backend_src_db_pool
- MusicActivity
- MainActivity
- AppUpdateHelper
- admin.js
- Hardware Info & Performance Telemetry (C27)
- ChatActivity
- MusicPlayerService
- Call Recording Audio Diagnostics & Root-Cause Technical Report
- Override
- test-roster.js
- B. Manifest Declarations Removed
- CallActivity.java
- ChatNotificationManager
- StorageMediaItem
- ChatMessage
- .initViews
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- appRelease.js
- android.content.Context
- ImageViewerActivity
- IncomingCallActivity
- appRelease.js
- YouTubeApiHelper
- okhttp3.Call
- 2. Core Components & Responsibilities
- Call Recording Implementation Audit
- .bind
- callSync.js
- 3. Detailed Component Breakdown
- android.view.View
- .loadGalleryData
- CallStateReceiver
- benchmark
- okhttp3.Cookie
- 2. Manufacturer-by-Manufacturer Breakdown
- MusicPlayerService.java
- db.js
- MusicPlayerManager
- 4. Troubleshooting & FAQs
- .checkIncomingCalls
- com.zez.app.model.ChatConversation
- TWING: Device-Specific Zero-Audio Investigation — Android 16
- AlarmItem
- Technical Proposal: Unified Call Recording & Native OEM Import Integration
- 1. System-Level Architectural Limitations
- Detailed Test Analysis:
- log
- ChatLocalDbHelper
- CallStateReceiver
- .getAbsoluteMediaUrl
- com.zez.app.model.ChatMessage
- ChatConversation
- android.widget.TextView
- server.js
- com.zez.app.model.CallLogItem
- GalleryAdapter
- check-recordings.js
- CallLogObserver
- CallActivity
- com.zez.app.music.model.MusicTrack
- dependencies
- android.net.Uri
- extreme-test-suite.js
- CallLogItem
- MusicSuggestionAdapter
- ImageEditActivity
- MediaPagerAdapter
- com.zez.app.music.model.MusicTrack
- .setupRecyclerViews
- PhoneHelper
- FavoritesHelper
- QuackyStorageHelper
- android.widget.SeekBar
- Override
- com.zez.app.model.ChatMessage
- com.zez.app.model.MediaItem
- scripts
- TWING GALLERY 2.0 COMPLETE ROLLBACK & RESTORATION REPORT
- CallLogAdapter
- .setCalls
- .onResponse
- engines
- PlayerListener
- PreferencesHelper
- .initThemeSpinner
- .syncAlarms
- ApiCallback

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 116 edges
2. `ChatActivity` - 76 edges
3. `MusicActivity` - 62 edges
4. `ChatMessage` - 55 edges
5. `MusicPlayerManager` - 50 edges
6. `DownloadTask` - 44 edges
7. `CallActivity` - 43 edges
8. `ChatMessageAdapter` - 43 edges
9. `ChatApiClient` - 42 edges
10. `AppCallItem` - 42 edges

## Surprising Connections (you probably didn't know these)
- `B. Manifest Declarations Removed` --references--> `ChatActivity`  [INFERRED]
  docs/GALLERY_2_ROLLBACK_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java
- `7. Affected Files & Isolation Boundaries` --references--> `ChatActivity`  [INFERRED]
  TWING_CALL_RECORDING_INTEGRATION_PROPOSAL.md → ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java
- `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` --references--> `MainActivity`  [INFERRED]
  CALL_RECORDING_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java
- `B. Manifest Declarations Removed` --references--> `MainActivity`  [INFERRED]
  docs/GALLERY_2_ROLLBACK_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java
- `4. Original Gallery Architecture Restored` --references--> `AlbumDetailActivity`  [INFERRED]
  docs/GALLERY_2_ROLLBACK_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/AlbumDetailActivity.java

## Import Cycles
- None detected.

## Communities (218 total, 147 thin omitted)

### Community 1 - "Adapter"
Cohesion: 0.14
Nodes (10): DocsAdapter, LinkItem, LinksAdapter, MediaGridAdapter, MediaLinksDocsHelper, OnDeleteMessagesListener, OnDocGoToChatListener, OnGoToChatListener (+2 more)

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 3 - "test_backend_suite.js"
Cohesion: 0.12
Nodes (14): closeDatabase(), initializeDatabase(), app, startServer(), http, path, run(), tests (+6 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "ContactsActivity"
Cohesion: 0.07
Nodes (8): ContactsAdapter, ContactViewHolder, DisplayItem, HeaderViewHolder, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.07
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 9 - "ActiveCallManager"
Cohesion: 0.09
Nodes (4): ActiveCallManager, CallStateListener, Callback, CallStateListener

### Community 10 - "ChatApiClient"
Cohesion: 0.10
Nodes (16): ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient, Callback, ChatCallback<MessagesResult>, Callback, ChatCallback (+8 more)

### Community 11 - "DownloadTask"
Cohesion: 0.07
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, DownloadProgressListener, MusicDownloadManager

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - "server.js"
Cohesion: 0.08
Nodes (19): express, callMediaCache, express, router, crypto, express, router, express (+11 more)

### Community 15 - "package.json"
Cohesion: 0.14
Nodes (18): @types/cors, @types/express, @types/multer, @types/node, @types/pg, typescript, description, devDependencies (+10 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 18 - "AlarmHelper"
Cohesion: 0.22
Nodes (3): AlarmHelper, TypeToken<List<AlarmItem>>, ApiClient.AlarmSyncCallback

### Community 19 - "AppCallItem"
Cohesion: 0.09
Nodes (6): AppCallAdapter, CallViewHolder, OnCallActionListener, com.zez.app.adapter.AppCallAdapter.OnCallActionListener, AppCallItem, Callback

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.04
Nodes (27): content, fs, lines, navMatch, fs, path, fs, path (+19 more)

### Community 21 - "MusicActivity"
Cohesion: 0.07
Nodes (7): Callback, MusicActivity, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<List<MusicTrack>>, YouTubeApiHelper.Callback<List<MusicTrack>>, TextWatcher, SeekBar.OnSeekBarChangeListener

### Community 24 - "AppUpdateHelper"
Cohesion: 0.20
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 26 - "admin.js"
Cohesion: 0.09
Nodes (14): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+6 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 29 - "MusicPlayerService"
Cohesion: 0.10
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 30 - "Call Recording Audio Diagnostics & Root-Cause Technical Report"
Cohesion: 0.11
Nodes (17): 1.1 The Core Problem, 1.2 The Root Cause, 1. Executive Summary & Root Cause Analysis, 2. Audio Source Investigation Matrix, 3.1 `AudioManager.AudioRecordingCallback` (API 29+), 3.2 Real-Time Peak & Silence Monitor, 3.3 Post-Recording Frame Decoded Sample Validation (`AudioCaptureValidator`), 3.4 Elimination of False Recordings (+9 more)

### Community 31 - "Override"
Cohesion: 0.07
Nodes (9): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, ChatApiClient.ChatCallback<ChatMessage>, ProfileMediaAdapter (+1 more)

### Community 32 - "test-roster.js"
Cohesion: 0.09
Nodes (22): http, run(), http, run(), http, request(), run(), token (+14 more)

### Community 33 - "B. Manifest Declarations Removed"
Cohesion: 0.25
Nodes (6): 3. Scope of Modifications & Discontinued Components, A. Dependencies Removed, B. Manifest Declarations Removed, C. File-Level Comparison & Removals, 7. Affected Files & Isolation Boundaries, AlarmFiringActivity

### Community 35 - "ChatNotificationManager"
Cohesion: 0.14
Nodes (4): ChatNotificationHelper, ChatNotificationManager, ChatApiClient.ChatCallback<List<ChatConversation>>, OnChatsUpdateListener

### Community 36 - "StorageMediaItem"
Cohesion: 0.11
Nodes (4): ManageStorageAdapter, OnSelectionListener, ManageStorageActivity, StorageMediaItem

### Community 52 - "appRelease.js"
Cohesion: 0.06
Nodes (20): fs, path, fs, path, fs, path, fs, path (+12 more)

### Community 56 - "IncomingCallActivity"
Cohesion: 0.23
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 58 - "appRelease.js"
Cohesion: 0.20
Nodes (8): downloadsDir, express, fs, multer, path, router, storage, upload

### Community 60 - "YouTubeApiHelper"
Cohesion: 0.23
Nodes (3): CachedStream, Callback, YouTubeApiHelper

### Community 61 - "okhttp3.Call"
Cohesion: 0.12
Nodes (13): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+5 more)

### Community 63 - "2. Core Components & Responsibilities"
Cohesion: 0.18
Nodes (10): 1. Overview, 2.1 `CallRecordingManager` (Main Coordinator), 2.3 `RecordingCapabilityDetector` (Device & Policy Inspector), 2.4 `RecordingEngine` (Audio Capture Engine), 2.5 `RecordingStorageManager` (Storage & File Integrity), 2.6 `RecordingDatabaseManager` (Metadata Persistence), 2.7 `RecordingNotificationManager` (User Indicators & Compliance), 2.8 `RecordingPermissionManager` (Consent & Runtime Checks) (+2 more)

### Community 65 - "Call Recording Implementation Audit"
Cohesion: 0.20
Nodes (9): 1.1 Project Structure & Build Configuration, 1.2 Core Functional Modules, 1. Existing Architecture & Baseline, 2. Inventory of Existing Call Recording Files, 3. Untouchable Files & Subsystems, 4. Android Security & Hardware Restrictions Analysis, 5. Proposed Modular Integration Strategy, Call Recording Implementation Audit (+1 more)

### Community 67 - "callSync.js"
Cohesion: 0.10
Nodes (23): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+15 more)

### Community 68 - "3. Detailed Component Breakdown"
Cohesion: 0.15
Nodes (12): 1. Architectural Overview & Context, 2. Implemented Architecture & Subsystems, 3. Detailed Component Breakdown, 4. Verification and Security Validation, A. Storage Access Framework & Permission Persistence (`OemRecordingImporter.java`), B. Intelligent Pattern & CallLog Correlation Engine (`RecordingMatcher.java`), C. Background Synchronization (`RecordingSyncWorker.java`), D. Universal Sharing Fallback (`RecordingShareActivity.java`) (+4 more)

### Community 69 - "android.view.View"
Cohesion: 0.11
Nodes (11): AlbumVH, DateHeaderViewHolder, IncomingViewHolder, ViewHolder, FilmstripVH, HeaderVH, MediaVH, ViewHolder (+3 more)

### Community 70 - ".loadGalleryData"
Cohesion: 0.16
Nodes (6): OnRestoreClickListener, RecycleBinAdapter, MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 71 - "CallStateReceiver"
Cohesion: 0.12
Nodes (17): 2.2 `CallStateMonitor` (Telephony State Detector), 7. Final Technical Architecture Decision, Architectural Rules Enforced in TWING:, 4. Ingestion Architecture: Three Legal Integration Pathways, 8. Implementation Steps & Rollout Strategy, Pathway A: Storage Access Framework (SAF) Folder Link (Recommended for Automatic Sync), Pathway B: Universal "Share to TWING" Intent Handler (Zero Permissions Needed), Pathway C: In-App VoIP Call Recording (Existing & 100% Functional) (+9 more)

### Community 81 - "2. Manufacturer-by-Manufacturer Breakdown"
Cohesion: 0.20
Nodes (9): 1. Overview, 2.1 Samsung (One UI / Android 10–14), 2.2 Xiaomi / Redmi / POCO (MIUI / HyperOS / Android 10–14), 2.3 OnePlus / Oppo / Realme (ColorOS / OxygenOS / Android 10–14), 2.4 Vivo / iQOO (Funtouch OS / OriginOS / Android 10–14), 2. Manufacturer-by-Manufacturer Breakdown, 3. Comprehensive Device Capability Matrix, 4. Architectural Honesty & Reporting (+1 more)

### Community 82 - "MusicPlayerService.java"
Cohesion: 0.06
Nodes (3): AlarmRingingService, NotificationCaptureService, AppCallDetector

### Community 84 - "db.js"
Cohesion: 0.07
Nodes (8): fs, path, fs, path, crypto, crypto, hashPassword(), { Pool }

### Community 88 - "4. Troubleshooting & FAQs"
Cohesion: 0.12
Nodes (16): 1. Quick Start Setup (One-Time Configuration), 2. How the Automatic Import Works, 3. Universal Manual Fallback ("Share to TWING"), 4. Troubleshooting & FAQs, OnePlus / OPPO / Realme Phones (OxygenOS / ColorOS), Q1: My recordings are not importing automatically after a call ends., Q2: Why does an old recording say "Audio capture silenced by Android OS"?, Q3: Why does Google Phone say out loud "This call is now being recorded"? (+8 more)

### Community 90 - "com.zez.app.model.ChatConversation"
Cohesion: 0.18
Nodes (4): ConversationsAdapter, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener

### Community 94 - "TWING: Device-Specific Zero-Audio Investigation — Android 16"
Cohesion: 0.13
Nodes (14): 1. Executive Summary, 2. Exact Device Profiles & Specifications, 4.1 Telephony Audio Mode Transition, 4.2 `AudioRecordingConfiguration` Parameters During Call, 4. Deep Inspection of Android Recording Configuration, 5.1 Vivo (Funtouch OS) Native Call Recorder, 5.2 OnePlus (OxygenOS / ColorOS) Native Call Recorder, 5. Investigation of OEM-Supported Call Recording Mechanisms (+6 more)

### Community 95 - "AlarmItem"
Cohesion: 0.15
Nodes (5): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem

### Community 99 - "Technical Proposal: Unified Call Recording & Native OEM Import Integration"
Cohesion: 0.14
Nodes (13): 1. Executive Summary & Objective, 2. Physical Device ADB Status Verification, 3.1 Vivo (Target: Vivo T3x 5G / Funtouch OS 14–16 & OriginOS), 3.2 OnePlus / OPPO / Realme (Target: OnePlus Nord CE 3 Lite 5G / OxygenOS & ColorOS 13–16), 3.3 Samsung (Galaxy S, A, M, F Series / One UI 4–7 on Android 12–16), 3.4 Xiaomi / Redmi / POCO (MIUI 13/14 & HyperOS 1/2 on Android 12–16), 3. Manufacturer-by-Manufacturer Native Recording Breakdown, 5. Intelligent Call Matching Algorithm (`RecordingMatcher`) (+5 more)

### Community 100 - "1. System-Level Architectural Limitations"
Cohesion: 0.25
Nodes (7): 1.1 The `CAPTURE_AUDIO_OUTPUT` Security Boundary, 1.2 Android `AudioPolicyService` & `AudioFlinger` Silencing, 1.3 Android 11+ "While-In-Use" Microphone Restrictions, 1.4 Google Play Policy Regarding Accessibility Services, 1. System-Level Architectural Limitations, 2. Summary for End Users & Operators, Call Recording Known Limitations & Technical Explanations

### Community 104 - "Detailed Test Analysis:"
Cohesion: 0.33
Nodes (6): 3. Controlled Audio Test Protocol & Empirical Measurements, Detailed Test Analysis:, Test 1: Outside of Calls (Idle / Normal State), Test 5: After Call Ends, Test Results Matrix, Tests 2, 3, and 4: During Cellular Call (`MODE_IN_CALL`)

### Community 110 - "log"
Cohesion: 0.10
Nodes (3): AlarmReceiver, BootReceiver, CallSyncWorker

### Community 118 - "CallStateReceiver"
Cohesion: 0.33
Nodes (5): 1. Automated & Emulator Test Results (Pixel 6a / Android 14–17), 2.1 Test Matrix Protocol Checklist, 2.2 OEM Audio Capture Verification Summary, 2. Physical Device Testing Protocol, Call Recording Test Report & Audio Validation Verification

### Community 122 - "com.zez.app.model.ChatMessage"
Cohesion: 0.17
Nodes (4): ChatRowItem, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, PendingMediaTask, PendingMessage

### Community 125 - "android.widget.TextView"
Cohesion: 0.13
Nodes (3): OutgoingViewHolder, AlbumDetailActivity, DownloadViewHolder

### Community 129 - "server.js"
Cohesion: 0.07
Nodes (24): multer, multer@^2.5.0, avatarStorage, avatarUploadDir, chatEventEmitter, EventEmitter, express, fs (+16 more)

### Community 131 - "GalleryAdapter"
Cohesion: 0.11
Nodes (4): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, GalleryListItem

### Community 132 - "check-recordings.js"
Cohesion: 0.15
Nodes (10): @aws-sdk/client-s3, pg, @aws-sdk/client-s3@^3.1173.0, pg@^8.23.1, { Pool }, s3Client, { S3Client, ListObjectsV2Command }, { Pool } (+2 more)

### Community 134 - "CallActivity"
Cohesion: 0.07
Nodes (10): CallActivity, Callback, TextureView.SurfaceTextureListener, ActiveCallManager.CallStateListener, CameraDevice.StateCallback, CameraCaptureSession.StateCallback, Runnable, Callback (+2 more)

### Community 135 - "com.zez.app.music.model.MusicTrack"
Cohesion: 0.30
Nodes (3): DownloadCallback, MusicDownloadHelper, TypeToken<List<MusicTrack>>

### Community 137 - "dependencies"
Cohesion: 0.17
Nodes (12): @aws-sdk/s3-request-presigner, cors, @distube/ytdl-core, dotenv, @noble/ciphers, dependencies, @aws-sdk/s3-request-presigner@^3.1173.0, cors@^2.8.5 (+4 more)

### Community 138 - "android.net.Uri"
Cohesion: 0.20
Nodes (3): AlbumsAdapter, OnAlbumClickListener, AlbumItem

### Community 139 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 152 - "MusicSuggestionAdapter"
Cohesion: 0.22
Nodes (4): ClearViewHolder, MusicSuggestionAdapter, OnSuggestionClickListener, ViewHolder

### Community 156 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 166 - "com.zez.app.music.model.MusicTrack"
Cohesion: 0.10
Nodes (3): MusicAdapter, OnTrackClickListener, MusicTrack

### Community 175 - ".setupRecyclerViews"
Cohesion: 0.16
Nodes (4): MusicSuggestionAdapter.OnSuggestionClickListener, MusicHistoryManager, TypeToken<List<MusicTrack>>, TypeToken<List<String>>

### Community 189 - "Override"
Cohesion: 0.06
Nodes (9): android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>>, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.music.player.MusicPlayerManager.PlayerListener, android.text.TextWatcher, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback (+1 more)

### Community 190 - "com.zez.app.model.ChatMessage"
Cohesion: 0.10
Nodes (4): ChatMessageAdapter, OnMediaRetryListener, OnMessageActionListener, ChatMessageAdapter.OnMessageActionListener

### Community 192 - "com.zez.app.model.MediaItem"
Cohesion: 0.20
Nodes (3): FilmstripAdapter, OnFilmstripClickListener, GalleryAdapter.Listener

### Community 195 - "scripts"
Cohesion: 0.40
Nodes (5): scripts, build, dev, start, type-check

### Community 196 - "TWING GALLERY 2.0 COMPLETE ROLLBACK & RESTORATION REPORT"
Cohesion: 0.25
Nodes (7): 1. Executive Summary, 2. Baseline Identification & Evidence, 4. Original Gallery Architecture Restored, 5. APK Size Verification, 6. Build & Test Verification, 7. Conclusion, TWING GALLERY 2.0 COMPLETE ROLLBACK & RESTORATION REPORT

### Community 206 - "PreferencesHelper"
Cohesion: 0.14
Nodes (3): ApiClient, DeviceHelper, PreferencesHelper

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 681 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **147 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `com.zez.app.model.CallLogItem`, `GalleryAdapter`, `Hardware Info & Performance Telemetry (C6)`, `androidx.annotation.NonNull`, `ChatApiClient`, `MediaItem`, `AppCallItem`, `AppUpdateHelper`, `B. Manifest Declarations Removed`, `ChatNotificationManager`, `.initViews`, `android.content.Context`, `Override`, `com.zez.app.model.MediaItem`, `.bind`, `android.view.View`, `ChatActivity.java`, `.setCalls`, `PlayerListener`, `PreferencesHelper`, `.initThemeSpinner`, `.checkIncomingCalls`, `com.zez.app.model.ChatConversation`, `AlarmItem`, `ChatLocalDbHelper`, `CallStateReceiver`, `MusicDownloadManager.java`, `android.widget.TextView`?**
  _High betweenness centrality (0.067) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `MainActivity` (e.g. with `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` and `B. Manifest Declarations Removed`) actually correct?**
  _`MainActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Adapter` be split into smaller, more focused modules?**
  _Cohesion score 0.13675213675213677 - nodes in this community are weakly interconnected._
- **Why does `MusicActivity` connect `MusicActivity` to `CallActivity.java`, `android.view.View`, `ChatActivity.java`, `androidx.annotation.NonNull`, `com.zez.app.music.model.MusicTrack`, `DownloadTask`, `PlayerListener`, `.setupRecyclerViews`, `MusicPlayerService.java`, `android.widget.TextView`, `MusicPlayerManager`, `MusicSuggestionAdapter`, `MusicDownloadManager.java`, `android.widget.SeekBar`, `MusicPlayerService`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ChatActivity` (e.g. with `B. Manifest Declarations Removed` and `7. Affected Files & Isolation Boundaries`) actually correct?**
  _`ChatActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `test_backend_suite.js` be split into smaller, more focused modules?**
  _Cohesion score 0.12380952380952381 - nodes in this community are weakly interconnected._