# Graph Report - Call Log Mannager  (2026-11-25)

## Corpus Check
- 178 files · ~180,005 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 246 file(s) not represented in the graph (top: .xml 236, .properties 5, (none) 3)
- WARNING: 246 of 438 files seen (56%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 3079 nodes · 8251 edges · 231 communities (73 shown, 158 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 788 edges (avg confidence: 0.85)
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
- MusicSuggestionAdapter
- ZoomImageView
- android.view.View
- ActiveCallManager
- ChatApiClient
- DownloadTask
- ChatConversation
- compilerOptions
- ChatUser
- package.json
- index.d.ts
- AlarmHelper
- AppCallItem
- d_call_log_mannager_zez_backend_src_db_pool
- MusicActivity
- MainActivity
- AppUpdateHelper
- admin.js
- Hardware Info & Performance Telemetry (C27)
- CallSyncWorker
- MusicPlayerService
- Call Recording Audio Diagnostics & Root-Cause Technical Report
- Override
- test-roster.js
- MarkdownRenderer.java
- ChatActivity.java
- ChatNotificationManager
- StorageMediaItem
- .handleMediaSelection
- ThemeManager
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- appRelease.js
- android.content.Context
- ImageViewerActivity
- IncomingCallActivity
- { Pool }
- YouTubeApiHelper
- okhttp3.Call
- 2. Core Components & Responsibilities
- Call Recording Implementation Audit
- .showChatNotificationIfNew
- callSync.js
- 3. Detailed Component Breakdown
- .toContentValues
- .loadGalleryData
- server.js
- benchmark
- MediaPickerAdapter
- .getMessages
- 2. Manufacturer-by-Manufacturer Breakdown
- TicTacToeActivity
- ContactsActivity
- MusicPlayerManager
- 4. Troubleshooting & FAQs
- .onResponse
- CallActivity
- TWING: Device-Specific Zero-Audio Investigation — Android 16
- AlarmAdapter
- QuickCameraActivity
- Technical Proposal: Unified Call Recording & Native OEM Import Integration
- 1. System-Level Architectural Limitations
- B. Manifest Declarations Removed
- GalleryAdapter
- MusicPlayerService.java
- ChatLocalDbHelper
- CallLogObserver
- .onBindViewHolder
- ChatActivity
- TWING OEM Call Recording Compatibility & Test Report
- AlbumDetailActivity
- server.js
- MusicHistoryManager
- GalleryAdapter
- check-recordings.js
- PlayerListener
- dependencies
- android.widget.SeekBar
- .startCall
- 1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)
- Detailed Test Analysis:
- MediaPagerAdapter
- com.zez.app.music.model.MusicTrack
- TelemetryGraphView.java
- PhoneHelper
- android.net.Uri
- ImageEditActivity
- Index of Discontinued / Removed Permissions
- Override
- ChatMessageAdapter
- FilmstripAdapter
- scripts
- Override
- Override
- MainActivity.java
- QuackyStorageHelper
- android.hardware.camera2.CameraDevice
- .onResponse
- engines
- AlarmRingingService
- PreferencesHelper
- .loadMedia
- RecycleBinAdapter
- TWING GALLERY 2.0 COMPLETE ROLLBACK & RESTORATION REPORT
- extreme-test-suite.js
- android.hardware.camera2.CameraCaptureSession
- .handleReaction
- android.text.style.MetricAffectingSpan
- Override
- .getApiUrl
- auto-release.md
- Call
- .setCalls

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 140 edges
2. `ChatMessage` - 108 edges
3. `ChatActivity` - 87 edges
4. `MusicActivity` - 66 edges
5. `ChatApiClient` - 58 edges
6. `ChatCallback` - 58 edges
7. `ChatMessageAdapter` - 48 edges
8. `MusicPlayerManager` - 48 edges
9. `DownloadTask` - 44 edges
10. `CallActivity` - 44 edges

## Surprising Connections (you probably didn't know these)
- `Architectural Rules Enforced in TWING:` --references--> `CallStateReceiver`  [INFERRED]
  TWING_ANDROID16_DEVICE_DIAGNOSTICS.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `Pathway A: Storage Access Framework (SAF) Folder Link (Recommended for Automatic Sync)` --references--> `CallStateReceiver`  [INFERRED]
  TWING_CALL_RECORDING_INTEGRATION_PROPOSAL.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` --references--> `MainActivity`  [INFERRED]
  CALL_RECORDING_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java
- `B. Manifest Declarations Removed` --references--> `MainActivity`  [INFERRED]
  docs/GALLERY_2_ROLLBACK_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java
- `7. Affected Files & Isolation Boundaries` --references--> `AlarmReceiver`  [INFERRED]
  TWING_CALL_RECORDING_INTEGRATION_PROPOSAL.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/AlarmReceiver.java

## Import Cycles
- None detected.

## Communities (231 total, 158 thin omitted)

### Community 1 - "Adapter"
Cohesion: 0.12
Nodes (12): DocsAdapter, Holder, LinkItem, LinksAdapter, MediaGridAdapter, MediaLinksDocsHelper, MediaGridAdapter.OnMediaItemClickListener, OnDeleteMessagesListener (+4 more)

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 3 - "test_backend_suite.js"
Cohesion: 0.12
Nodes (14): closeDatabase(), initializeDatabase(), app, startServer(), http, path, run(), tests (+6 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "MusicSuggestionAdapter"
Cohesion: 0.24
Nodes (4): ClearViewHolder, MusicSuggestionAdapter, OnSuggestionClickListener, ViewHolder

### Community 6 - "ZoomImageView"
Cohesion: 0.17
Nodes (3): OnTapListener, ScaleListener, ZoomImageView

### Community 7 - "android.view.View"
Cohesion: 0.10
Nodes (15): AlbumVH, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactViewHolder, ViewHolder, FilmstripVH, HeaderVH (+7 more)

### Community 10 - "ChatApiClient"
Cohesion: 0.12
Nodes (10): ChatApiClient, Callback, Callback, Callback, ChatCallback, ContactSyncResult, GameEventsResult, GameState (+2 more)

### Community 11 - "DownloadTask"
Cohesion: 0.07
Nodes (6): ActiveDownloadAdapter, DownloadViewHolder, OnCancelClickListener, DownloadTask, DownloadProgressListener, MusicDownloadManager

### Community 12 - "ChatConversation"
Cohesion: 0.08
Nodes (6): ConversationsAdapter, androidx.recyclerview.widget.DiffUtil.Callback, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, ChatConversation

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 15 - "package.json"
Cohesion: 0.14
Nodes (18): @types/cors, @types/express, @types/multer, @types/node, @types/pg, typescript, description, devDependencies (+10 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 18 - "AlarmHelper"
Cohesion: 0.12
Nodes (6): BootReceiver, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>>, ApiClient.AlarmSyncCallback, ZezApplication

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.04
Nodes (27): content, fs, lines, navMatch, fs, path, fs, path (+19 more)

### Community 21 - "MusicActivity"
Cohesion: 0.06
Nodes (9): MusicActivity, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher, SeekBar.OnSeekBarChangeListener (+1 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.17
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 26 - "admin.js"
Cohesion: 0.09
Nodes (14): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+6 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 29 - "MusicPlayerService"
Cohesion: 0.09
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 30 - "Call Recording Audio Diagnostics & Root-Cause Technical Report"
Cohesion: 0.11
Nodes (17): 1.1 The Core Problem, 1.2 The Root Cause, 1. Executive Summary & Root Cause Analysis, 2. Audio Source Investigation Matrix, 3.1 `AudioManager.AudioRecordingCallback` (API 29+), 3.2 Real-Time Peak & Silence Monitor, 3.3 Post-Recording Frame Decoded Sample Validation (`AudioCaptureValidator`), 3.4 Elimination of False Recordings (+9 more)

### Community 31 - "Override"
Cohesion: 0.06
Nodes (14): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, RecyclerView.Adapter<RecyclerView.ViewHolder>, RecyclerView.ViewHolder, TextWatcher (+6 more)

### Community 32 - "test-roster.js"
Cohesion: 0.11
Nodes (19): http, run(), http, run(), http, request(), run(), token (+11 more)

### Community 33 - "MarkdownRenderer.java"
Cohesion: 0.11
Nodes (4): NotificationCaptureService, AppCallDetector, MarkdownRenderer, SpanApplier

### Community 35 - "ChatNotificationManager"
Cohesion: 0.21
Nodes (3): ChatApiClient.ChatCallback<Boolean>, ChatNotificationManager, OnChatsUpdateListener

### Community 36 - "StorageMediaItem"
Cohesion: 0.12
Nodes (3): ManageStorageAdapter, OnSelectionListener, StorageMediaItem

### Community 38 - ".handleMediaSelection"
Cohesion: 0.12
Nodes (3): PendingMediaTask, PendingMessage, ChatMessage

### Community 52 - "appRelease.js"
Cohesion: 0.05
Nodes (29): fs, path, fs, path, fs, path, fs, path (+21 more)

### Community 56 - "IncomingCallActivity"
Cohesion: 0.23
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 58 - "{ Pool }"
Cohesion: 0.18
Nodes (3): fs, path, { Pool }

### Community 60 - "YouTubeApiHelper"
Cohesion: 0.14
Nodes (3): CachedStream, SearchResult, YouTubeApiHelper

### Community 61 - "okhttp3.Call"
Cohesion: 0.09
Nodes (16): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+8 more)

### Community 63 - "2. Core Components & Responsibilities"
Cohesion: 0.18
Nodes (10): 1. Overview, 2.1 `CallRecordingManager` (Main Coordinator), 2.3 `RecordingCapabilityDetector` (Device & Policy Inspector), 2.4 `RecordingEngine` (Audio Capture Engine), 2.5 `RecordingStorageManager` (Storage & File Integrity), 2.6 `RecordingDatabaseManager` (Metadata Persistence), 2.7 `RecordingNotificationManager` (User Indicators & Compliance), 2.8 `RecordingPermissionManager` (Consent & Runtime Checks) (+2 more)

### Community 65 - "Call Recording Implementation Audit"
Cohesion: 0.20
Nodes (9): 1.1 Project Structure & Build Configuration, 1.2 Core Functional Modules, 1. Existing Architecture & Baseline, 2. Inventory of Existing Call Recording Files, 3. Untouchable Files & Subsystems, 4. Android Security & Hardware Restrictions Analysis, 5. Proposed Modular Integration Strategy, Call Recording Implementation Audit (+1 more)

### Community 67 - "callSync.js"
Cohesion: 0.07
Nodes (27): crypto, crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey() (+19 more)

### Community 68 - "3. Detailed Component Breakdown"
Cohesion: 0.15
Nodes (12): 1. Architectural Overview & Context, 2. Implemented Architecture & Subsystems, 3. Detailed Component Breakdown, 4. Verification and Security Validation, A. Storage Access Framework & Permission Persistence (`OemRecordingImporter.java`), B. Intelligent Pattern & CallLog Correlation Engine (`RecordingMatcher.java`), C. Background Synchronization (`RecordingSyncWorker.java`), D. Universal Sharing Fallback (`RecordingShareActivity.java`) (+4 more)

### Community 70 - ".loadGalleryData"
Cohesion: 0.18
Nodes (5): FavoritesHelper, MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 71 - "server.js"
Cohesion: 0.07
Nodes (24): express, downloadsDir, express, fs, multer, path, router, storage (+16 more)

### Community 77 - ".getMessages"
Cohesion: 0.31
Nodes (4): ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, Callback, ChatCallback<MessagesResult>, MessagesResult

### Community 81 - "2. Manufacturer-by-Manufacturer Breakdown"
Cohesion: 0.20
Nodes (9): 1. Overview, 2.1 Samsung (One UI / Android 10–14), 2.2 Xiaomi / Redmi / POCO (MIUI / HyperOS / Android 10–14), 2.3 OnePlus / Oppo / Realme (ColorOS / OxygenOS / Android 10–14), 2.4 Vivo / iQOO (Funtouch OS / OriginOS / Android 10–14), 2. Manufacturer-by-Manufacturer Breakdown, 3. Comprehensive Device Capability Matrix, 4. Architectural Honesty & Reporting (+1 more)

### Community 82 - "TicTacToeActivity"
Cohesion: 0.15
Nodes (6): TicTacToeActivity, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>

### Community 84 - "ContactsActivity"
Cohesion: 0.05
Nodes (10): CallLogAdapter, CallViewHolder, ContactsAdapter, DisplayItem, HeaderViewHolder, OnContactClickListener, ContactsActivity, TextWatcher (+2 more)

### Community 88 - "4. Troubleshooting & FAQs"
Cohesion: 0.12
Nodes (16): 1. Quick Start Setup (One-Time Configuration), 2. How the Automatic Import Works, 3. Universal Manual Fallback ("Share to TWING"), 4. Troubleshooting & FAQs, OnePlus / OPPO / Realme Phones (OxygenOS / ColorOS), Q1: My recordings are not importing automatically after a call ends., Q2: Why does an old recording say "Audio capture silenced by Android OS"?, Q3: Why does Google Phone say out loud "This call is now being recorded"? (+8 more)

### Community 89 - ".onResponse"
Cohesion: 0.24
Nodes (6): ChatApiClient.ChatCallback<ChatApiClient.PollResult>, Callback, ConversationModeInfo, IncomingCallInfo, PeerStatus, PollResult

### Community 94 - "TWING: Device-Specific Zero-Audio Investigation — Android 16"
Cohesion: 0.12
Nodes (16): 1. Executive Summary, 2. Exact Device Profiles & Specifications, 4.1 Telephony Audio Mode Transition, 4.2 `AudioRecordingConfiguration` Parameters During Call, 4. Deep Inspection of Android Recording Configuration, 5.1 Vivo (Funtouch OS) Native Call Recorder, 5.2 OnePlus (OxygenOS / ColorOS) Native Call Recorder, 5. Investigation of OEM-Supported Call Recording Mechanisms (+8 more)

### Community 95 - "AlarmAdapter"
Cohesion: 0.13
Nodes (4): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, AlarmItem

### Community 99 - "Technical Proposal: Unified Call Recording & Native OEM Import Integration"
Cohesion: 0.11
Nodes (17): 1. Executive Summary & Objective, 2. Physical Device ADB Status Verification, 3.1 Vivo (Target: Vivo T3x 5G / Funtouch OS 14–16 & OriginOS), 3.2 OnePlus / OPPO / Realme (Target: OnePlus Nord CE 3 Lite 5G / OxygenOS & ColorOS 13–16), 3.3 Samsung (Galaxy S, A, M, F Series / One UI 4–7 on Android 12–16), 3.4 Xiaomi / Redmi / POCO (MIUI 13/14 & HyperOS 1/2 on Android 12–16), 3. Manufacturer-by-Manufacturer Native Recording Breakdown, 4. Ingestion Architecture: Three Legal Integration Pathways (+9 more)

### Community 100 - "1. System-Level Architectural Limitations"
Cohesion: 0.25
Nodes (7): 1.1 The `CAPTURE_AUDIO_OUTPUT` Security Boundary, 1.2 Android `AudioPolicyService` & `AudioFlinger` Silencing, 1.3 Android 11+ "While-In-Use" Microphone Restrictions, 1.4 Google Play Policy Regarding Accessibility Services, 1. System-Level Architectural Limitations, 2. Summary for End Users & Operators, Call Recording Known Limitations & Technical Explanations

### Community 104 - "B. Manifest Declarations Removed"
Cohesion: 0.25
Nodes (6): 3. Scope of Modifications & Discontinued Components, A. Dependencies Removed, B. Manifest Declarations Removed, C. File-Level Comparison & Removals, 7. Affected Files & Isolation Boundaries, AlarmFiringActivity

### Community 105 - "GalleryAdapter"
Cohesion: 0.14
Nodes (6): GalleryAdapter, GalleryPopupHelper, GridLayoutManager.SpanSizeLookup, GallerySelectionCallback, GridRow, OnTileClickListener

### Community 121 - ".onBindViewHolder"
Cohesion: 0.18
Nodes (3): AppCallAdapter, CallViewHolder, OnCallActionListener

### Community 122 - "ChatActivity"
Cohesion: 0.06
Nodes (6): ChatActivity, ChatApiClient.ChatCallback<ChatUser>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, GalleryPopupHelper.GallerySelectionCallback, SimpleShareContact, LiveMessageListener

### Community 124 - "TWING OEM Call Recording Compatibility & Test Report"
Cohesion: 0.18
Nodes (11): 2.2 `CallStateMonitor` (Telephony State Detector), 8. Implementation Steps & Rollout Strategy, 1. Executive Summary, 2. Multi-OEM Compatibility Matrix, 3. Physical Device Verification Findings, 4. Test Scenarios Executed & Verified, A. Vivo T3x 5G (Android 16), B. OnePlus Nord CE 3 Lite 5G (Android 16) (+3 more)

### Community 129 - "server.js"
Cohesion: 0.06
Nodes (25): multer, multer@^2.5.0, avatarStorage, avatarUploadDir, chatEventEmitter, EventEmitter, express, fs (+17 more)

### Community 130 - "MusicHistoryManager"
Cohesion: 0.29
Nodes (3): MusicSuggestionAdapter.OnSuggestionClickListener, MusicHistoryManager, TypeToken<List<String>>

### Community 131 - "GalleryAdapter"
Cohesion: 0.11
Nodes (5): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, GalleryListItem, MediaItem

### Community 132 - "check-recordings.js"
Cohesion: 0.15
Nodes (10): @aws-sdk/client-s3, pg, @aws-sdk/client-s3@^3.1173.0, pg@^8.23.1, { Pool }, s3Client, { S3Client, ListObjectsV2Command }, { Pool } (+2 more)

### Community 137 - "dependencies"
Cohesion: 0.17
Nodes (12): @aws-sdk/s3-request-presigner, cors, @distube/ytdl-core, dotenv, @noble/ciphers, dependencies, @aws-sdk/s3-request-presigner@^3.1173.0, cors@^2.8.5 (+4 more)

### Community 151 - "1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)"
Cohesion: 0.33
Nodes (5): 1. Automated & Emulator Test Results (Pixel 6a / Android 14–17), 2.1 Test Matrix Protocol Checklist, 2.2 OEM Audio Capture Verification Summary, 2. Physical Device Testing Protocol, Call Recording Test Report & Audio Validation Verification

### Community 152 - "Detailed Test Analysis:"
Cohesion: 0.33
Nodes (6): 3. Controlled Audio Test Protocol & Empirical Measurements, Detailed Test Analysis:, Test 1: Outside of Calls (Idle / Normal State), Test 5: After Call Ends, Test Results Matrix, Tests 2, 3, and 4: During Cellular Call (`MODE_IN_CALL`)

### Community 156 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 166 - "com.zez.app.music.model.MusicTrack"
Cohesion: 0.07
Nodes (7): MusicAdapter, OnTrackClickListener, DownloadCallback, MusicDownloadHelper, TypeToken<List<MusicTrack>>, MusicTrack, TypeToken<List<MusicTrack>>

### Community 180 - "android.net.Uri"
Cohesion: 0.20
Nodes (3): AlbumsAdapter, OnAlbumClickListener, AlbumItem

### Community 186 - "Index of Discontinued / Removed Permissions"
Cohesion: 0.20
Nodes (9): 1. NotificationCaptureService & BIND_NOTIFICATION_LISTENER_SERVICE, 2. PROCESS_OUTGOING_CALLS (Outgoing Cellular Call Interception), 3. READ_CALL_LOG & WRITE_CALL_LOG (Cellular Phone Call History), 4. READ_PHONE_STATE (SIM Serial & Carrier State), 5. REQUEST_INSTALL_PACKAGES (In-App Auto-Update Sideloading), 6. SYSTEM_ALERT_WINDOW (Floating Call Overlay Bubbles), Index of Discontinued / Removed Permissions, Quaki / TWING — Permissions & Removed Features Reference Guide (+1 more)

### Community 189 - "Override"
Cohesion: 0.08
Nodes (9): com.google.android.material.tabs.TabLayout.OnTabSelectedListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>>, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.music.player.MusicPlayerManager.PlayerListener, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, Runnable (+1 more)

### Community 190 - "ChatMessageAdapter"
Cohesion: 0.11
Nodes (5): ChatMessageAdapter, ChatRowItem, OnGameInviteClickListener, OnMediaRetryListener, OnMessageActionListener

### Community 195 - "scripts"
Cohesion: 0.40
Nodes (5): scripts, build, dev, start, type-check

### Community 196 - "Override"
Cohesion: 0.23
Nodes (3): Callback<SearchResult>, CookieJar, Dns

### Community 209 - "TWING GALLERY 2.0 COMPLETE ROLLBACK & RESTORATION REPORT"
Cohesion: 0.25
Nodes (7): 1. Executive Summary, 2. Baseline Identification & Evidence, 4. Original Gallery Architecture Restored, 5. APK Size Verification, 6. Build & Test Verification, 7. Conclusion, TWING GALLERY 2.0 COMPLETE ROLLBACK & RESTORATION REPORT

### Community 210 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 216 - "Override"
Cohesion: 0.13
Nodes (8): Callback, TextureView.SurfaceTextureListener, ActiveCallManager.CallStateListener, Runnable, Callback, Runnable, Callback, TextureView.SurfaceTextureListener

### Community 219 - ".getApiUrl"
Cohesion: 0.15
Nodes (5): AlarmSyncCallback, ApiCallback, ApiClient, CallLogHelper, DeviceHelper

### Community 225 - ".setCalls"
Cohesion: 0.12
Nodes (3): android.text.TextWatcher, android.text.TextWatcher, android.text.TextWatcher

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 713 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **158 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `GalleryAdapter`, `PlayerListener`, `android.view.View`, `MusicDownloadHelper.java`, `ChatApiClient`, `ChatConversation`, `AppCallItem`, `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)`, `AppUpdateHelper`, `ChatActivity.java`, `ChatNotificationManager`, `TelemetryGraphView.java`, `android.content.Context`, `ImageViewerActivity`, `Override`, `MainActivity.java`, `PreferencesHelper`, `.getApiUrl`, `AlarmAdapter`, `Call`, `.setCalls`, `B. Manifest Declarations Removed`, `MusicPlayerService.java`, `ChatLocalDbHelper`, `.onBindViewHolder`?**
  _High betweenness centrality (0.074) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `MainActivity` (e.g. with `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` and `B. Manifest Declarations Removed`) actually correct?**
  _`MainActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Adapter` be split into smaller, more focused modules?**
  _Cohesion score 0.11740890688259109 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `ChatActivity.java`, `.toContentValues`, `MainActivity.java`, `android.view.View`, `B. Manifest Declarations Removed`, `.handleMediaSelection`, `GalleryAdapter`, `.getMessages`, `MusicPlayerService.java`, `ChatMessage`, `.handleReaction`, `ChatLocalDbHelper`, `android.content.Context`, `.onResponse`, `ChatMessageAdapter`, `Override`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ChatActivity` (e.g. with `B. Manifest Declarations Removed` and `7. Affected Files & Isolation Boundaries`) actually correct?**
  _`ChatActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `test_backend_suite.js` be split into smaller, more focused modules?**
  _Cohesion score 0.12380952380952381 - nodes in this community are weakly interconnected._