# Graph Report - Call Log Mannager  (2026-11-25)

## Corpus Check
- 199 files · ~196,942 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 249 file(s) not represented in the graph (top: .xml 239, .properties 5, (none) 3)
- WARNING: 249 of 462 files seen (54%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 3366 nodes · 9164 edges · 236 communities (85 shown, 151 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 847 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `12d20c0e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Adapter
- b2StorageService.js
- db.js
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
- ChatMessage
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
- .getUserId
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
- android.view.View
- YouTubeApiHelper
- okhttp3.Call
- SnakesLaddersApiClient
- 2. Core Components & Responsibilities
- Call Recording Implementation Audit
- .showChatNotificationIfNew
- callSync.js
- 3. Detailed Component Breakdown
- .drainPendingQueue
- .loadGalleryData
- server.js
- benchmark
- MediaPickerAdapter
- SnakesAndLaddersActivity
- 2. Manufacturer-by-Manufacturer Breakdown
- TicTacToeActivity
- ContactsActivity
- engine.js
- MusicPlayerManager
- 4. Troubleshooting & FAQs
- .applyModeState
- CallActivity
- TWING: Device-Specific Zero-Audio Investigation — Android 16
- AlarmAdapter
- QuickCameraActivity
- Technical Proposal: Unified Call Recording & Native OEM Import Integration
- 1. System-Level Architectural Limitations
- .getHistory
- GalleryAdapter
- MusicPlayerService.java
- ChatLocalDbHelper
- GameRuleError
- .onBindViewHolder
- ChatActivity
- TWING OEM Call Recording Compatibility & Test Report
- AlbumDetailActivity
- server.js
- com.zez.app.music.model.MusicTrack
- engine.test.js
- PlayerListener
- MusicDownloadHelper.java
- AlarmItem
- SnakesLaddersRepository
- .startCall
- 1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)
- Detailed Test Analysis:
- MediaPagerAdapter
- com.zez.app.music.model.MusicTrack
- SnakesLaddersBoardView
- PhoneHelper
- android.net.Uri
- ImageEditActivity
- Index of Discontinued / Removed Permissions
- Override
- ChatMessageAdapter
- FilmstripAdapter
- service.test.js
- Override
- index.js
- MainActivity.java
- QuackyStorageHelper
- android.hardware.camera2.CameraDevice
- .onResponse
- Callback
- PreferencesHelper
- SnakesLaddersBoardConfig
- CallStateReceiver
- SnakesLaddersCoordinates
- extreme-test-suite.js
- Override
- .onAudioCallClick
- SessionState
- FavoritesHelper
- Override
- com.zez.app.model.CallLogItem
- auto-release.md
- .onItemSelected
- 3. Manufacturer-by-Manufacturer Native Recording Breakdown
- .finalizeChatAccountDeletion
- AppCallLogItem
- ApiCallback

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 140 edges
2. `ChatMessage` - 114 edges
3. `ChatActivity` - 93 edges
4. `MusicActivity` - 66 edges
5. `ChatApiClient` - 59 edges
6. `ChatCallback` - 58 edges
7. `ChatMessageAdapter` - 50 edges
8. `MusicPlayerManager` - 48 edges
9. `DownloadTask` - 44 edges
10. `CallActivity` - 44 edges

## Surprising Connections (you probably didn't know these)
- `4. Test Scenarios Executed & Verified` --references--> `CallStateReceiver`  [INFERRED]
  TWING_OEM_COMPATIBILITY_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `A. Vivo T3x 5G (Android 16)` --references--> `CallStateReceiver`  [INFERRED]
  TWING_OEM_COMPATIBILITY_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `B. Manifest Declarations Removed` --references--> `ChatActivity`  [INFERRED]
  docs/GALLERY_2_ROLLBACK_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java
- `7. Affected Files & Isolation Boundaries` --references--> `ChatActivity`  [INFERRED]
  TWING_CALL_RECORDING_INTEGRATION_PROPOSAL.md → ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java
- `B. Manifest Declarations Removed` --references--> `AlarmFiringActivity`  [INFERRED]
  docs/GALLERY_2_ROLLBACK_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/AlarmFiringActivity.java

## Import Cycles
- None detected.

## Communities (236 total, 151 thin omitted)

### Community 1 - "Adapter"
Cohesion: 0.12
Nodes (12): DocsAdapter, Holder, LinkItem, LinksAdapter, MediaGridAdapter, MediaLinksDocsHelper, MediaGridAdapter.OnMediaItemClickListener, OnDeleteMessagesListener (+4 more)

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 3 - "db.js"
Cohesion: 0.06
Nodes (15): fs, path, fs, path, crypto, closeDatabase(), crypto, hashPassword() (+7 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "MusicSuggestionAdapter"
Cohesion: 0.13
Nodes (7): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter, ClearViewHolder, MusicSuggestionAdapter, OnSuggestionClickListener, ViewHolder

### Community 6 - "ZoomImageView"
Cohesion: 0.12
Nodes (4): GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 10 - "ChatApiClient"
Cohesion: 0.15
Nodes (8): ChatApiClient, Callback, ChatCallback, ContactSyncResult, GameEventsResult, GameState, LiveUpdateResult, MediaUploadResult

### Community 11 - "DownloadTask"
Cohesion: 0.07
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, DownloadProgressListener, MusicDownloadManager

### Community 12 - "ChatConversation"
Cohesion: 0.08
Nodes (6): ConversationsAdapter, androidx.recyclerview.widget.DiffUtil.Callback, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, ChatConversation

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 15 - "package.json"
Cohesion: 0.04
Nodes (57): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, @distube/ytdl-core, dotenv, multer, @noble/ciphers, pg (+49 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 18 - "AlarmHelper"
Cohesion: 0.17
Nodes (4): AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>>, ApiClient.AlarmSyncCallback

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.05
Nodes (25): content, fs, lines, navMatch, fs, path, fs, path (+17 more)

### Community 21 - "MusicActivity"
Cohesion: 0.07
Nodes (6): MusicActivity, YouTubeApiHelper.Callback<List<String>>, RecyclerView.OnScrollListener, TextWatcher, MusicSuggestionAdapter.OnSuggestionClickListener, SeekBar.OnSeekBarChangeListener

### Community 23 - "MainActivity"
Cohesion: 0.05
Nodes (3): MainActivity, Runnable, ChatApiClient.ChatCallback<String>

### Community 24 - "AppUpdateHelper"
Cohesion: 0.15
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 26 - "admin.js"
Cohesion: 0.09
Nodes (14): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+6 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 28 - "CallSyncWorker"
Cohesion: 0.13
Nodes (3): CallLogListener, CallLogObserver, CallSyncWorker

### Community 29 - "MusicPlayerService"
Cohesion: 0.10
Nodes (3): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback

### Community 30 - "Call Recording Audio Diagnostics & Root-Cause Technical Report"
Cohesion: 0.11
Nodes (17): 1.1 The Core Problem, 1.2 The Root Cause, 1. Executive Summary & Root Cause Analysis, 2. Audio Source Investigation Matrix, 3.1 `AudioManager.AudioRecordingCallback` (API 29+), 3.2 Real-Time Peak & Silence Monitor, 3.3 Post-Recording Frame Decoded Sample Validation (`AudioCaptureValidator`), 3.4 Elimination of False Recordings (+9 more)

### Community 31 - "Override"
Cohesion: 0.05
Nodes (16): com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, android.text.TextWatcher, GalleryPopupHelper.GallerySelectionCallback, ItemTouchHelper.SimpleCallback, RecyclerView.Adapter<RecyclerView.ViewHolder> (+8 more)

### Community 32 - "test-roster.js"
Cohesion: 0.09
Nodes (22): http, run(), http, run(), http, request(), run(), token (+14 more)

### Community 33 - "MarkdownRenderer.java"
Cohesion: 0.11
Nodes (4): NotificationCaptureService, AppCallDetector, MarkdownRenderer, SpanApplier

### Community 35 - ".getUserId"
Cohesion: 0.14
Nodes (4): ChatNotificationManager, ChatApiClient.ChatCallback<List<ChatConversation>>, LiveMessageListener, OnChatsUpdateListener

### Community 36 - "StorageMediaItem"
Cohesion: 0.10
Nodes (4): ManageStorageAdapter, OnSelectionListener, ManageStorageActivity, StorageMediaItem

### Community 38 - ".handleMediaSelection"
Cohesion: 0.07
Nodes (4): ChatRowItem, PendingMediaTask, PendingMessage, ChatMessage

### Community 52 - "appRelease.js"
Cohesion: 0.06
Nodes (24): fs, path, fs, path, fs, path, fs, path (+16 more)

### Community 56 - "IncomingCallActivity"
Cohesion: 0.23
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 58 - "android.view.View"
Cohesion: 0.08
Nodes (9): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, MediaVH, ViewHolder, MediaViewHolder, ContactsActivity, DownloadViewHolder (+1 more)

### Community 60 - "YouTubeApiHelper"
Cohesion: 0.10
Nodes (8): YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, CachedStream, Callback, SearchResult, YouTubeApiHelper, Callback<SearchResult>

### Community 61 - "okhttp3.Call"
Cohesion: 0.08
Nodes (20): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+12 more)

### Community 62 - "SnakesLaddersApiClient"
Cohesion: 0.15
Nodes (7): ApiCallback, Parser, SnakesLaddersApiClient, Callback, Callback, InvitationResult, InvitationState

### Community 63 - "2. Core Components & Responsibilities"
Cohesion: 0.18
Nodes (10): 1. Overview, 2.1 `CallRecordingManager` (Main Coordinator), 2.3 `RecordingCapabilityDetector` (Device & Policy Inspector), 2.4 `RecordingEngine` (Audio Capture Engine), 2.5 `RecordingStorageManager` (Storage & File Integrity), 2.6 `RecordingDatabaseManager` (Metadata Persistence), 2.7 `RecordingNotificationManager` (User Indicators & Compliance), 2.8 `RecordingPermissionManager` (Consent & Runtime Checks) (+2 more)

### Community 65 - "Call Recording Implementation Audit"
Cohesion: 0.20
Nodes (9): 1.1 Project Structure & Build Configuration, 1.2 Core Functional Modules, 1. Existing Architecture & Baseline, 2. Inventory of Existing Call Recording Files, 3. Untouchable Files & Subsystems, 4. Android Security & Hardware Restrictions Analysis, 5. Proposed Modular Integration Strategy, Call Recording Implementation Audit (+1 more)

### Community 66 - ".showChatNotificationIfNew"
Cohesion: 0.15
Nodes (3): BootReceiver, ChatNotificationHelper, ZezApplication

### Community 67 - "callSync.js"
Cohesion: 0.10
Nodes (23): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+15 more)

### Community 68 - "3. Detailed Component Breakdown"
Cohesion: 0.15
Nodes (12): 1. Architectural Overview & Context, 2. Implemented Architecture & Subsystems, 3. Detailed Component Breakdown, 4. Verification and Security Validation, A. Storage Access Framework & Permission Persistence (`OemRecordingImporter.java`), B. Intelligent Pattern & CallLog Correlation Engine (`RecordingMatcher.java`), C. Background Synchronization (`RecordingSyncWorker.java`), D. Universal Sharing Fallback (`RecordingShareActivity.java`) (+4 more)

### Community 70 - ".loadGalleryData"
Cohesion: 0.27
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 71 - "server.js"
Cohesion: 0.06
Nodes (27): express, callMediaCache, express, router, router, crypto, express, router (+19 more)

### Community 76 - "MediaPickerAdapter"
Cohesion: 0.11
Nodes (4): MediaPickerAdapter, OnSelectionChangedListener, MediaPickerActivity, MediaPickerAdapter.OnSelectionChangedListener

### Community 77 - "SnakesAndLaddersActivity"
Cohesion: 0.11
Nodes (5): SnakesAndLaddersActivity, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.RollResult>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.InvitationState>

### Community 81 - "2. Manufacturer-by-Manufacturer Breakdown"
Cohesion: 0.20
Nodes (9): 1. Overview, 2.1 Samsung (One UI / Android 10–14), 2.2 Xiaomi / Redmi / POCO (MIUI / HyperOS / Android 10–14), 2.3 OnePlus / Oppo / Realme (ColorOS / OxygenOS / Android 10–14), 2.4 Vivo / iQOO (Funtouch OS / OriginOS / Android 10–14), 2. Manufacturer-by-Manufacturer Breakdown, 3. Comprehensive Device Capability Matrix, 4. Architectural Honesty & Reporting (+1 more)

### Community 82 - "TicTacToeActivity"
Cohesion: 0.13
Nodes (6): TicTacToeActivity, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>

### Community 84 - "ContactsActivity"
Cohesion: 0.05
Nodes (9): CallLogAdapter, CallViewHolder, ContactsAdapter, DisplayItem, HeaderViewHolder, OnContactClickListener, TextWatcher, CallLogItem (+1 more)

### Community 85 - "engine.js"
Cohesion: 0.19
Nodes (12): BOARD_CONFIGS, DEFAULT_BOARD_CONFIG, getBoardConfig(), applyDiceRoll(), buildLookupMaps(), crypto, crypto, CONNECTION_STATUS (+4 more)

### Community 88 - "4. Troubleshooting & FAQs"
Cohesion: 0.12
Nodes (16): 1. Quick Start Setup (One-Time Configuration), 2. How the Automatic Import Works, 3. Universal Manual Fallback ("Share to TWING"), 4. Troubleshooting & FAQs, OnePlus / OPPO / Realme Phones (OxygenOS / ColorOS), Q1: My recordings are not importing automatically after a call ends., Q2: Why does an old recording say "Audio capture silenced by Android OS"?, Q3: Why does Google Phone say out loud "This call is now being recorded"? (+8 more)

### Community 89 - ".applyModeState"
Cohesion: 0.15
Nodes (9): ChatApiClient.ChatCallback<ChatApiClient.PollResult>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, Callback, ConversationModeInfo, IncomingCallInfo, MessagesResult, PeerStatus (+1 more)

### Community 94 - "TWING: Device-Specific Zero-Audio Investigation — Android 16"
Cohesion: 0.13
Nodes (14): 1. Executive Summary, 2. Exact Device Profiles & Specifications, 4.1 Telephony Audio Mode Transition, 4.2 `AudioRecordingConfiguration` Parameters During Call, 4. Deep Inspection of Android Recording Configuration, 5.1 Vivo (Funtouch OS) Native Call Recorder, 5.2 OnePlus (OxygenOS / ColorOS) Native Call Recorder, 5. Investigation of OEM-Supported Call Recording Mechanisms (+6 more)

### Community 99 - "Technical Proposal: Unified Call Recording & Native OEM Import Integration"
Cohesion: 0.22
Nodes (8): 1. Executive Summary & Objective, 2. Physical Device ADB Status Verification, 5. Intelligent Call Matching Algorithm (`RecordingMatcher`), 6.1 Database Schema (v4 Upgrade in `CallRecordDbHelper`):, 6.2 `CallRecordItem` & `RecordingMetadata` Extensions:, 6. Proposed Database & Model Schema Extension, 9. Recommendation & Review Request, Technical Proposal: Unified Call Recording & Native OEM Import Integration

### Community 100 - "1. System-Level Architectural Limitations"
Cohesion: 0.25
Nodes (7): 1.1 The `CAPTURE_AUDIO_OUTPUT` Security Boundary, 1.2 Android `AudioPolicyService` & `AudioFlinger` Silencing, 1.3 Android 11+ "While-In-Use" Microphone Restrictions, 1.4 Google Play Policy Regarding Accessibility Services, 1. System-Level Architectural Limitations, 2. Summary for End Users & Operators, Call Recording Known Limitations & Technical Explanations

### Community 104 - ".getHistory"
Cohesion: 0.20
Nodes (5): HistoryAdapter, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.HistoryResponse>, VH, HistoryMatch, HistoryResponse

### Community 105 - "GalleryAdapter"
Cohesion: 0.13
Nodes (7): GalleryAdapter, GalleryPopupHelper, GridLayoutManager.SpanSizeLookup, GallerySelectionCallback, GridRow, HeaderVH, OnTileClickListener

### Community 119 - "GameRuleError"
Cohesion: 0.33
Nodes (4): GameRuleError, SnakesLaddersService, runServiceAndMultiplayerTests(), isValidUuid()

### Community 121 - ".onBindViewHolder"
Cohesion: 0.18
Nodes (3): AppCallAdapter, CallViewHolder, OnCallActionListener

### Community 122 - "ChatActivity"
Cohesion: 0.07
Nodes (4): ChatActivity, ChatApiClient.ChatCallback<ChatUser>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, SimpleShareContact

### Community 124 - "TWING OEM Call Recording Compatibility & Test Report"
Cohesion: 0.22
Nodes (8): 1. Executive Summary, 2. Multi-OEM Compatibility Matrix, 3. Physical Device Verification Findings, 4. Test Scenarios Executed & Verified, A. Vivo T3x 5G (Android 16), B. OnePlus Nord CE 3 Lite 5G (Android 16), C. Google Phone Dialer (Fallback Mode), TWING OEM Call Recording Compatibility & Test Report

### Community 125 - "AlbumDetailActivity"
Cohesion: 0.10
Nodes (7): ViewHolder, GalleryAdapter, GridLayoutManager.SpanSizeLookup, HeaderVH, Listener, AlbumDetailActivity, GalleryAdapter.Listener

### Community 129 - "server.js"
Cohesion: 0.06
Nodes (22): avatarStorage, avatarUploadDir, chatEventEmitter, EventEmitter, express, fs, mediaStorage, mediaUploadDir (+14 more)

### Community 130 - "com.zez.app.music.model.MusicTrack"
Cohesion: 0.16
Nodes (6): DownloadCallback, MusicDownloadHelper, TypeToken<List<MusicTrack>>, MusicHistoryManager, TypeToken<List<MusicTrack>>, TypeToken<List<String>>

### Community 132 - "engine.test.js"
Cohesion: 0.28
Nodes (12): coordinatesToSquare(), squareCenterNormalized(), squareToCoordinates(), crypto, rollDice(), createInitialSessionState(), resignSession(), assert (+4 more)

### Community 137 - "AlarmItem"
Cohesion: 0.18
Nodes (3): com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmSyncCallback

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
Cohesion: 0.09
Nodes (4): MusicAdapter, OnTrackClickListener, TrackViewHolder, MusicTrack

### Community 175 - "SnakesLaddersBoardView"
Cohesion: 0.08
Nodes (5): SnakesLaddersBoardView, AnimatorListenerAdapter, SnakesLaddersDiceView, PlayerState, TelemetryGraphView

### Community 180 - "android.net.Uri"
Cohesion: 0.12
Nodes (6): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem, GalleryListItem, MediaItem

### Community 181 - "ImageEditActivity"
Cohesion: 0.09
Nodes (15): 1. Executive Summary, 2. Baseline Identification & Evidence, 3. Scope of Modifications & Discontinued Components, 4. Original Gallery Architecture Restored, 5. APK Size Verification, 6. Build & Test Verification, 7. Conclusion, A. Dependencies Removed (+7 more)

### Community 186 - "Index of Discontinued / Removed Permissions"
Cohesion: 0.20
Nodes (9): 1. NotificationCaptureService & BIND_NOTIFICATION_LISTENER_SERVICE, 2. PROCESS_OUTGOING_CALLS (Outgoing Cellular Call Interception), 3. READ_CALL_LOG & WRITE_CALL_LOG (Cellular Phone Call History), 4. READ_PHONE_STATE (SIM Serial & Carrier State), 5. REQUEST_INSTALL_PACKAGES (In-App Auto-Update Sideloading), 6. SYSTEM_ALERT_WINDOW (Floating Call Overlay Bubbles), Index of Discontinued / Removed Permissions, Quaki / TWING — Permissions & Removed Features Reference Guide (+1 more)

### Community 189 - "Override"
Cohesion: 0.05
Nodes (12): okhttp3.Callback, android.text.TextWatcher, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>>, ChatApiClient.ChatCallback<List<ChatConversation>>, android.text.style.MetricAffectingSpan (+4 more)

### Community 190 - "ChatMessageAdapter"
Cohesion: 0.09
Nodes (6): ChatMessageAdapter, OnGameDeclineClickListener, OnGameInviteClickListener, OnMediaRetryListener, OnMessageActionListener, ChatMessageAdapter.OnMessageActionListener

### Community 192 - "FilmstripAdapter"
Cohesion: 0.31
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 195 - "service.test.js"
Cohesion: 0.16
Nodes (6): EventEmitter, SnakesLaddersEvents, assert, createInMemoryRepository(), crypto, GAME_EVENTS

### Community 197 - "index.js"
Cohesion: 0.25
Nodes (9): createSnakesLaddersController(), express, extractRequesterId(), handleError(), initializeSnakesLaddersSchema(), repository, router, service (+1 more)

### Community 198 - "MainActivity.java"
Cohesion: 0.09
Nodes (3): 7. Affected Files & Isolation Boundaries, AlarmFiringActivity, SnakesLaddersHistoryActivity

### Community 204 - "Callback"
Cohesion: 0.20
Nodes (4): Callback, Runnable, Runnable, Callback

### Community 206 - "PreferencesHelper"
Cohesion: 0.10
Nodes (3): ApiClient, DeviceHelper, PreferencesHelper

### Community 207 - "SnakesLaddersBoardConfig"
Cohesion: 0.33
Nodes (3): LadderDef, SnakeDef, SnakesLaddersBoardConfig

### Community 208 - "CallStateReceiver"
Cohesion: 0.22
Nodes (9): 2.2 `CallStateMonitor` (Telephony State Detector), 7. Final Technical Architecture Decision, Architectural Rules Enforced in TWING:, 4. Ingestion Architecture: Three Legal Integration Pathways, 8. Implementation Steps & Rollout Strategy, Pathway A: Storage Access Framework (SAF) Folder Link (Recommended for Automatic Sync), Pathway B: Universal "Share to TWING" Intent Handler (Zero Permissions Needed), Pathway C: In-App VoIP Call Recording (Existing & 100% Functional) (+1 more)

### Community 210 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 213 - "SessionState"
Cohesion: 0.50
Nodes (6): InvitationEventsResult, MoveRecord, RollResult, SessionEventsResult, SessionState, SnakesLaddersModels

### Community 216 - "Override"
Cohesion: 0.20
Nodes (3): TextureView.SurfaceTextureListener, ActiveCallManager.CallStateListener, CameraCaptureSession.StateCallback

### Community 225 - "3. Manufacturer-by-Manufacturer Native Recording Breakdown"
Cohesion: 0.40
Nodes (5): 3.1 Vivo (Target: Vivo T3x 5G / Funtouch OS 14–16 & OriginOS), 3.2 OnePlus / OPPO / Realme (Target: OnePlus Nord CE 3 Lite 5G / OxygenOS & ColorOS 13–16), 3.3 Samsung (Galaxy S, A, M, F Series / One UI 4–7 on Android 12–16), 3.4 Xiaomi / Redmi / POCO (MIUI 13/14 & HyperOS 1/2 on Android 12–16), 3. Manufacturer-by-Manufacturer Native Recording Breakdown

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 743 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **151 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `PlayerListener`, `android.view.View`, `AlarmItem`, `ChatConversation`, `AppCallItem`, `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)`, `AppUpdateHelper`, `.getUserId`, `SnakesLaddersBoardView`, `ImageEditActivity`, `android.content.Context`, `ImageViewerActivity`, `android.view.View`, `Override`, `MainActivity.java`, `PreferencesHelper`, `.onAudioCallClick`, `com.zez.app.model.CallLogItem`, `AlarmAdapter`, `.onItemSelected`, `.finalizeChatAccountDeletion`, `MusicPlayerService.java`, `ChatLocalDbHelper`, `AppUpdateHelper.java`, `.onBindViewHolder`?**
  _High betweenness centrality (0.059) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `MainActivity` (e.g. with `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` and `B. Manifest Declarations Removed`) actually correct?**
  _`MainActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Adapter` be split into smaller, more focused modules?**
  _Cohesion score 0.1241565452091768 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `ChatActivity.java`, `.getUserId`, `.drainPendingQueue`, `MainActivity.java`, `android.view.View`, `.handleMediaSelection`, `GalleryAdapter`, `ChatApiClient`, `MusicPlayerService.java`, `ChatMessage`, `ImageEditActivity`, `android.content.Context`, `ChatLocalDbHelper`, `.applyModeState`, `android.view.View`, `ChatMessageAdapter`, `Override`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ChatActivity` (e.g. with `B. Manifest Declarations Removed` and `7. Affected Files & Isolation Boundaries`) actually correct?**
  _`ChatActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `db.js` be split into smaller, more focused modules?**
  _Cohesion score 0.06025369978858351 - nodes in this community are weakly interconnected._