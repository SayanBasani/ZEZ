# Graph Report - Call Log Mannager  (2026-11-27)

## Corpus Check
- 250 files · ~262,393 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 408 file(s) not represented in the graph (top: .xml 393, .properties 8, (none) 3)
- WARNING: 408 of 685 files seen (60%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 4831 nodes · 14135 edges · 354 communities (111 shown, 243 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 1131 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `986657f1`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- com.google.gson.JsonObject
- .showMediaLinksDocsDialog
- b2StorageService.js
- db.js
- e2e_device_test.py
- ChatActivity.java
- android.widget.SeekBar
- ViewHolder
- MainActivity
- ChatLocalDbHelper
- compilerOptions
- MusicViewController
- package.json
- index.d.ts
- Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation
- MusicTrack
- MainActivity
- Override
- SnakesAndLaddersActivity
- admin.js
- Override
- androidx.recyclerview.widget.RecyclerView
- MusicPlayerService
- music.js
- CallLogItem
- android.service.notification.StatusBarNotification
- callRelayWs.js
- .bind
- ChatActivity
- Override
- Project README
- DownloadTask
- Admin Dashboard Web Interface
- MusicTrack
- ChatApiClient
- PdfGeneratorActivity
- ChatMessage
- MusicPlayerManager
- appRelease.js
- CallMediaEngine.java
- YouTubeApiHelper
- okhttp3.Cookie
- okhttp3.Call
- AppCallItem
- Adapter
- callRead.js
- MediaItem
- ChatMessageAdapter
- server.js
- AlarmItem
- ChatPreferencesHelper
- benchmark
- ActiveCallManager
- .getBaseUrl
- YouTubeApiHelper
- GalleryViewController
- GalleryAdapter
- PreferencesHelper
- MusicRecommendationEngine
- engine.js
- SnakesLaddersBoardView
- AlarmHelper
- MusicPlayerService
- CallActivity
- StorageMediaItem
- DownloadTask
- GalleryAdapter
- QuickCameraActivity
- IncomingCallActivity
- manager/music/download/MusicDownloadHelper.java
- MusicActivity
- ImageViewerActivity
- ViewHolder
- ZoomImageView
- CallLogItem
- PageVH
- GalleryPopupHelper.java
- .startApkDownload
- MusicPlayerManager
- PdfGeneratorActivity
- android.view.View
- HardwareInfoHelper
- Override
- AppUpdateHelper
- GameRuleError
- AlbumDetailActivity
- ChatUser
- AlbumDetailActivity
- ChatConversation
- Override
- HistoryAdapter
- ZezStorageHelper
- engine.test.js
- chat.js
- 4. Complete `AndroidManifest.xml`
- manager/service/CallSyncWorker.java
- GalleryAdapter
- MarkdownRenderer.java
- Adapter
- ApiClient
- QuackyStorageHelper
- SnakesLaddersRepository
- RecycleBinAdapter
- 01 — Silent Background Sync, Notification Capture & App-Call Detection
- android.graphics.Bitmap
- FavoritesHelper
- service.test.js
- MediaItem
- RecycleBinAdapter
- Part B: Gallery + PDF/PPT Studio (Tab 3: `R.id.nav_gallery`)
- 05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates
- SettingsViewController
- HardwareInfoHelper
- SnakesLaddersDiceView
- PlayerListener
- SnakesLaddersCoordinates
- .onReceive
- 03 — YMusic Module Specification (Tab 1: Primary Launch Screen)
- SnakesLaddersBoardConfig
- RecycleBinHelper
- MusicSuggestionAdapter
- MediaPagerAdapter
- MusicSuggestionAdapter
- .onResponse
- PROJECT EXECUTION STATUS — CALL LOG MANAGER (QUACKY + ZEZ + BACKEND + GRAPHIFY)
- ImageEditActivity
- FilmstripAdapter
- SessionState
- MusicHistoryManager
- PdfGeneratorActivity
- AlarmRingingService
- .getId
- AlbumsAdapter
- ImageEditActivity
- PhoneHelper
- CallLogAdapter
- PdfDocumentHelper
- PptxGenerator
- MusicRecommendationEngine
- index.js
- extreme-test-suite.js
- 3. PostgreSQL Database Tables
- android.content.Context
- auto-release.md
- PlayerListener
- android.app.Activity
- alarm/service/AlarmRingingService.java
- FavoritesHelper
- README.md
- color
- TelemetryGraphView
- 5.2 `AppCallDetector.java`
- FilmstripAdapter
- .getUserId
- 4. Call Log Capture & Sync Pipeline
- .setData
- Override
- AppCallDetectorTest.java
- ReleaseInfo
- PptxGenerator

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 145 edges
2. `ChatActivity` - 108 edges
3. `ChatMessage` - 102 edges
4. `MusicTrack` - 78 edges
5. `ChatApiClient` - 69 edges
6. `MusicActivity` - 67 edges
7. `SettingsViewController` - 63 edges
8. `ChatCallback` - 63 edges
9. `MusicViewController` - 62 edges
10. `CallActivity` - 61 edges

## Surprising Connections (you probably didn't know these)
- `Feature Separation Matrix` --references--> `CallActivity`  [INFERRED]
  Companion-App-Spec/README.md → QuackyAndroid/app/src/main/java/com/zez/app/CallActivity.java
- `Specification Index & Recommended Build Order` --references--> `deviceAuth()`  [INFERRED]
  Companion-App-Spec/README.md → Zez-Backend/src/middleware/deviceAuth.js
- `4. Complete `AndroidManifest.xml`` --references--> `ChatActivity`  [INFERRED]
  Companion-App-Spec/00_ARCHITECTURE_AND_PROJECT_SETUP.md → QuackyAndroid/app/src/main/java/com/zez/app/ChatActivity.java
- `6. Final Implementation Verification Checklist (For Next Build Agent)` --references--> `ChatActivity`  [INFERRED]
  Companion-App-Spec/05_SETTINGS_THEMES_HARDWARE_OTA_SPEC.md → QuackyAndroid/app/src/main/java/com/zez/app/ChatActivity.java
- `Strict Non-Negotiable Rules for the Implementing Agent` --references--> `ChatActivity`  [INFERRED]
  Companion-App-Spec/README.md → QuackyAndroid/app/src/main/java/com/zez/app/ChatActivity.java

## Import Cycles
- None detected.

## Communities (354 total, 243 thin omitted)

### Community 0 - "com.google.gson.JsonObject"
Cohesion: 0.17
Nodes (5): SnakesLaddersApiClient, ApiCallback, Callback, Callback, Parser

### Community 1 - ".showMediaLinksDocsDialog"
Cohesion: 0.09
Nodes (12): ChatApiClient.ChatCallback<ChatUser>, MediaLinksDocsHelper, DocsAdapter, OnDocGoToChatListener, LinkItem, LinksAdapter, OnLinkGoToChatListener, MediaGridAdapter (+4 more)

### Community 2 - "b2StorageService.js"
Cohesion: 0.15
Nodes (28): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+20 more)

### Community 3 - "db.js"
Cohesion: 0.07
Nodes (12): crypto, http, crypto, hashPassword(), { Pool }, crypto, crypto, express (+4 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 6 - "ChatActivity.java"
Cohesion: 0.07
Nodes (3): CallLogObserver, CallLogListener, CallLogObserver

### Community 7 - "android.widget.SeekBar"
Cohesion: 0.12
Nodes (7): SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, TextWatcher, AppUpdateHelper.DownloadProgressCallback, Runnable

### Community 8 - "ViewHolder"
Cohesion: 0.23
Nodes (4): ContactsAdapter, ContactViewHolder, HeaderViewHolder, ContactsActivity

### Community 10 - "MainActivity"
Cohesion: 0.10
Nodes (3): MainActivity, BootReceiver, ZezManagerApplication

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 15 - "package.json"
Cohesion: 0.06
Nodes (44): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, @distube/ytdl-core, dotenv, express, multer, @noble/ciphers (+36 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation"
Cohesion: 0.08
Nodes (23): 10.1 Active Permissions in `ZezAndroid/app/src/main/AndroidManifest.xml`, 10.2 Disabled / Play Protect-Sensitive Features & Exact Restore Instructions, 10. Android Permissions & Play Protect Restore Guide, 12. PostgreSQL Schema (20 Tables in `public`), 13. Cloud Storage (B2), OTA Releases & Developer Mode, 14. Configuration, Build & Verification Commands, 16. Historical Appendix: Cellular Call Recording Investigation (`v1.0.24`–`v1.0.32`), 1. System Overview & Tech Stack (+15 more)

### Community 20 - "MusicTrack"
Cohesion: 0.07
Nodes (9): MusicAdapter, OnTrackClickListener, TrackViewHolder, MusicDownloadHelper, DownloadCallback, TypeToken<List<MusicTrack>>, MusicTrack, TypeToken<List<MusicTrack>> (+1 more)

### Community 23 - "MainActivity"
Cohesion: 0.04
Nodes (3): AlarmAdapter, MainActivity, Runnable

### Community 24 - "Override"
Cohesion: 0.05
Nodes (18): com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, ChatApiClient.ChatCallback<Void>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationState>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, android.text.TextWatcher, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ProfileMediaAdapter (+10 more)

### Community 25 - "SnakesAndLaddersActivity"
Cohesion: 0.11
Nodes (4): SnakesAndLaddersActivity, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.InvitationState>

### Community 26 - "admin.js"
Cohesion: 0.05
Nodes (35): verifyPassword(), adminLoginRateLimiter(), apiRequests, getClientIp(), globalApiRateLimiter(), loginAttempts, sanitizePrototypePollution(), securityHeaders() (+27 more)

### Community 27 - "Override"
Cohesion: 0.04
Nodes (16): Callback, okhttp3.Callback, ChatApiClient.ChatCallback<Boolean>, android.widget.AdapterView.OnItemSelectedListener, android.text.TextWatcher, android.text.TextWatcher, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, GalleryAdapter.Listener (+8 more)

### Community 29 - "MusicPlayerService"
Cohesion: 0.12
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 32 - "music.js"
Cohesion: 0.10
Nodes (16): http, run(), express, http, https, router, streamCache, ytdl (+8 more)

### Community 33 - "CallLogItem"
Cohesion: 0.15
Nodes (7): CallLogItem, BackupManager, BackupExportResult, BackupRestoreResult, TypeToken<List<CallLogItem>>, CallLogHelper, CallLogModelTest

### Community 37 - "callRelayWs.js"
Cohesion: 0.12
Nodes (18): callAudioCache, callMediaCache, express, router, acceptUpgrade(), attachRealtimeWebSockets(), callRooms, createWsClient() (+10 more)

### Community 40 - "ChatActivity"
Cohesion: 0.07
Nodes (5): ChatActivity, GalleryPopupHelper.GallerySelectionCallback, PendingMessage, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationState>, LiveMessageListener

### Community 41 - "Override"
Cohesion: 0.11
Nodes (7): YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher, SeekBar.OnSeekBarChangeListener

### Community 43 - "DownloadTask"
Cohesion: 0.07
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, MusicDownloadManager, DownloadProgressListener

### Community 49 - "MusicTrack"
Cohesion: 0.10
Nodes (4): MusicDownloadHelper, DownloadCallback, TypeToken<List<MusicTrack>>, MusicTrack

### Community 50 - "ChatApiClient"
Cohesion: 0.09
Nodes (13): ChatApiClient.ChatCallback<ChatApiClient.PollResult>, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient, ChatCallback, ContactSyncResult, ConversationModeInfo, Callback, GameEventsResult (+5 more)

### Community 53 - "ChatMessage"
Cohesion: 0.07
Nodes (3): ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatMessage>, ChatMessage

### Community 57 - "appRelease.js"
Cohesion: 0.07
Nodes (26): 11. Backend REST API Reference, 15. Security Notes & Known Limitations, run(), http, request(), run(), token, adminAuth() (+18 more)

### Community 58 - "CallMediaEngine.java"
Cohesion: 0.08
Nodes (5): CallMediaEngine, WebSocketListener, MediaListener, Callback, Callback

### Community 59 - "YouTubeApiHelper"
Cohesion: 0.10
Nodes (7): YouTubeApiHelper, CachedSearch, CachedStream, Callback, SearchResult, Callback<SearchResult>, Dns

### Community 61 - "okhttp3.Call"
Cohesion: 0.07
Nodes (21): Callback, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, Callback, Callback, Callback, Callback, Callback, ChatCallback<MessagesResult> (+13 more)

### Community 63 - "AppCallItem"
Cohesion: 0.09
Nodes (5): AppCallAdapter, OnCallActionListener, com.zez.app.adapter.AppCallAdapter.OnCallActionListener, AppCallItem, Callback

### Community 65 - "Adapter"
Cohesion: 0.12
Nodes (3): AlbumsAdapter, OnAlbumClickListener, AlbumItem

### Community 67 - "callRead.js"
Cohesion: 0.08
Nodes (34): 4.1 Health Ping, 4.2 Register / Refresh Device, 4.3 Sync Cellular Call Logs, 4.4 Sync Notifications, 4.5 Sync Third-Party App Calls, 4.6 Bidirectional Alarms Sync, 4.7 YMusic Backend Proxy Endpoints (`routes/music.js`), 4.8 OTA Latest Release (`routes/appRelease.js`) (+26 more)

### Community 68 - "MediaItem"
Cohesion: 0.16
Nodes (3): ImageViewerActivity, ViewPager2.OnPageChangeCallback, MediaItem

### Community 69 - "ChatMessageAdapter"
Cohesion: 0.06
Nodes (9): ChatMessageAdapter, ChatRowItem, OnGameDeclineClickListener, OnGameInviteClickListener, OnMediaRetryListener, OnMessageActionListener, OnQuoteClickListener, PendingMediaTask (+1 more)

### Community 71 - "server.js"
Cohesion: 0.11
Nodes (21): crypto, fs, parseBoolFlag(), parseFlagValue(), path, publish(), closeDatabase(), initializeDatabase() (+13 more)

### Community 72 - "AlarmItem"
Cohesion: 0.08
Nodes (9): AlarmAdapter, OnAlarmClickListener, AlarmsViewController, AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>> (+1 more)

### Community 75 - "ActiveCallManager"
Cohesion: 0.07
Nodes (5): ActiveCallManager, CallStateListener, CallStateListener, Callback, CallMediaEngine.MediaListener

### Community 76 - ".getBaseUrl"
Cohesion: 0.12
Nodes (7): 6. Final Implementation Verification Checklist (For Next Build Agent), TicTacToeActivity, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>

### Community 78 - "YouTubeApiHelper"
Cohesion: 0.09
Nodes (8): YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper, CachedSearch, CachedStream, Callback, SearchResult, Callback<SearchResult>, Dns

### Community 79 - "GalleryViewController"
Cohesion: 0.23
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 81 - "GalleryAdapter"
Cohesion: 0.13
Nodes (7): GalleryPopupHelper, GalleryAdapter, HeaderVH, GallerySelectionCallback, GridRow, OnTileClickListener, GridLayoutManager.SpanSizeLookup

### Community 83 - "PreferencesHelper"
Cohesion: 0.08
Nodes (5): ApiClient, AlarmSyncCallback, ApiCallback, DeviceHelper, PreferencesHelper

### Community 84 - "MusicRecommendationEngine"
Cohesion: 0.10
Nodes (7): MusicAdapter, OnTrackClickListener, MusicRecommendationEngine, TypeToken<Map<String, Float>>, TypeToken<Set<String>>, ScoredTrack, MusicRecommendationEngineTest

### Community 85 - "engine.js"
Cohesion: 0.19
Nodes (12): BOARD_CONFIGS, DEFAULT_BOARD_CONFIG, getBoardConfig(), applyDiceRoll(), buildLookupMaps(), crypto, crypto, CONNECTION_STATUS (+4 more)

### Community 87 - "SnakesLaddersBoardView"
Cohesion: 0.19
Nodes (3): SnakesLaddersBoardView, AnimatorListenerAdapter, PlayerState

### Community 88 - "AlarmHelper"
Cohesion: 0.12
Nodes (6): OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>>, ApiClient.AlarmSyncCallback

### Community 90 - "CallActivity"
Cohesion: 0.07
Nodes (8): CallActivity, Callback, Callback, TextureView.SurfaceTextureListener, ActiveCallManager.CallStateListener, CameraDevice.StateCallback, CameraCaptureSession.StateCallback, Runnable

### Community 91 - "StorageMediaItem"
Cohesion: 0.11
Nodes (4): ManageStorageAdapter, OnSelectionListener, ManageStorageActivity, StorageMediaItem

### Community 92 - "DownloadTask"
Cohesion: 0.05
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, MusicDownloadManager, DownloadProgressListener

### Community 96 - "GalleryAdapter"
Cohesion: 0.12
Nodes (4): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, GalleryListItem

### Community 97 - "QuickCameraActivity"
Cohesion: 0.11
Nodes (4): QuickCameraActivity, TextureView.SurfaceTextureListener, CameraDevice.StateCallback, CameraCaptureSession.StateCallback

### Community 98 - "IncomingCallActivity"
Cohesion: 0.13
Nodes (7): Feature Separation Matrix, Specification Index & Recommended Build Order, Standalone Companion Sync & Utility App — Complete A-to-Z Blueprint, Strict Non-Negotiable Rules for the Implementing Agent, IncomingCallActivity, Callback, Callback

### Community 101 - "MusicActivity"
Cohesion: 0.08
Nodes (7): MusicActivity, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher, SeekBar.OnSeekBarChangeListener

### Community 107 - "ZoomImageView"
Cohesion: 0.07
Nodes (9): LinkTouchMovementMethod, ZoomImageView, GestureListener, OnTapListener, ScaleListener, ZoomImageView, GestureListener, OnTapListener (+1 more)

### Community 109 - "PageVH"
Cohesion: 0.33
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 110 - "GalleryPopupHelper.java"
Cohesion: 0.07
Nodes (4): AlarmFiringActivity, AlarmReceiver, AlarmFiringActivity, AlarmReceiver

### Community 111 - ".startApkDownload"
Cohesion: 0.19
Nodes (3): AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper.UpdateCheckCallback

### Community 113 - "PdfGeneratorActivity"
Cohesion: 0.15
Nodes (7): AdjustAdapter, AdjustViewHolder, PreviewAdapter, PreviewViewHolder, SavedFilesAdapter, FileViewHolder, ItemTouchHelper.SimpleCallback

### Community 114 - "android.view.View"
Cohesion: 0.08
Nodes (20): AlarmViewHolder, AlbumVH, CallViewHolder, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ViewHolder, HeaderVH (+12 more)

### Community 115 - "HardwareInfoHelper"
Cohesion: 0.16
Nodes (5): HardwareInfoHelper, CpuInfo, RamInfo, StorageInfo, TelemetryGraphView

### Community 116 - "Override"
Cohesion: 0.16
Nodes (3): CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 117 - "AppUpdateHelper"
Cohesion: 0.16
Nodes (4): AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 119 - "GameRuleError"
Cohesion: 0.33
Nodes (4): GameRuleError, SnakesLaddersService, runServiceAndMultiplayerTests(), isValidUuid()

### Community 123 - "ChatConversation"
Cohesion: 0.07
Nodes (7): ConversationsAdapter, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, androidx.recyclerview.widget.DiffUtil.Callback, ChatConversation, Callback

### Community 125 - "Override"
Cohesion: 0.21
Nodes (3): com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, TabLayout.OnTabSelectedListener

### Community 126 - "HistoryAdapter"
Cohesion: 0.23
Nodes (6): SnakesLaddersHistoryActivity, HistoryAdapter, VH, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.HistoryResponse>, HistoryMatch, HistoryResponse

### Community 128 - "engine.test.js"
Cohesion: 0.28
Nodes (12): coordinatesToSquare(), squareCenterNormalized(), squareToCoordinates(), crypto, rollDice(), createInitialSessionState(), resignSession(), assert (+4 more)

### Community 129 - "chat.js"
Cohesion: 0.06
Nodes (20): content, fs, lines, navMatch, fs, os, path, avatarStorage (+12 more)

### Community 130 - "4. Complete `AndroidManifest.xml`"
Cohesion: 0.14
Nodes (14): 00 — Architecture & Android Project Setup, 1. Identity & Project Separation Rules, 2. Gradle Configuration (`app/build.gradle`), 3. Target Package Layout, 4. Complete `AndroidManifest.xml`, 5. `CompanionApplication.java` Initialization Order, 6. Main 4-Tab Navigation Architecture (`MainActivity`), Application, Activities, Services, Receivers, FileProvider (+6 more)

### Community 131 - "manager/service/CallSyncWorker.java"
Cohesion: 0.11
Nodes (3): CallStateReceiver, CallSyncWorker, CallStateReceiver

### Community 132 - "GalleryAdapter"
Cohesion: 0.11
Nodes (5): GalleryAdapter, GridLayoutManager.SpanSizeLookup, HeaderVH, Listener, GalleryListItem

### Community 133 - "MarkdownRenderer.java"
Cohesion: 0.13
Nodes (4): AppCallLogItem, AppCallDetector, MarkdownRenderer, SpanApplier

### Community 135 - "Adapter"
Cohesion: 0.16
Nodes (6): AdjustAdapter, AdjustViewHolder, PreviewAdapter, PreviewViewHolder, SavedFilesAdapter, ItemTouchHelper.SimpleCallback

### Community 136 - "ApiClient"
Cohesion: 0.26
Nodes (4): ApiClient, AlarmSyncCallback, CloudBackupStats, CloudRestoreResult

### Community 140 - "RecycleBinAdapter"
Cohesion: 0.30
Nodes (3): RecycleBinAdapter, BinViewHolder, OnRestoreClickListener

### Community 141 - "01 — Silent Background Sync, Notification Capture & App-Call Detection"
Cohesion: 0.15
Nodes (12): 01 — Silent Background Sync, Notification Capture & App-Call Detection, 1. Strict Silent Operation Rules, 2. One-Time Permission & Restricted Settings Onboarding, 3.1 `PreferencesHelper.java`, 3.2 `DeviceHelper.java`, 3.3 `ApiClient.java` (Networking, DNS Resilience, Registration & Sync), 3. Device Identity, Preferences & HTTP Client, 6. Optional Remote Control Toggle Architecture (Recommended Enhancement) (+4 more)

### Community 145 - "service.test.js"
Cohesion: 0.16
Nodes (6): EventEmitter, SnakesLaddersEvents, assert, createInMemoryRepository(), crypto, GAME_EVENTS

### Community 147 - "RecycleBinAdapter"
Cohesion: 0.29
Nodes (3): RecycleBinAdapter, BinViewHolder, OnRestoreClickListener

### Community 148 - "Part B: Gallery + PDF/PPT Studio (Tab 3: `R.id.nav_gallery`)"
Cohesion: 0.17
Nodes (11): 04 — Alarms (Tab 2) & Gallery + PDF/PPT Studio (Tab 3) Specification, 1. Data Model (`AlarmItem.java`), 1. Media & Album Engine (`MediaHelper.java`, `FavoritesHelper.java`, `RecycleBinHelper.java`), 2. Main Gallery Screen (`view_gallery.xml`), 2. Scheduling & Persistence (`AlarmHelper.java`), 3. Alarm Firing Pipeline (`AlarmReceiver`, `AlarmRingingService`, `AlarmFiringActivity`), 3. Vivo-Style Multi-Select Bottom Bar (`gallery_action_bar`), 4. Full-Screen Viewer & Editor (+3 more)

### Community 150 - "05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates"
Cohesion: 0.18
Nodes (11): 05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates, 1. Settings Screen Architecture (`view_settings.xml`), 2.1 Theme Modes (`KEY_THEME_MODE`), 2.2 All 8 Color Palettes (`KEY_COLOR_PALETTE`), 2.3 Font Families & Live Scaling (`ThemeManager.java`), 2. Dynamic Theme, Palette & Font Engine (`ThemeManager.java`, `PreferencesHelper.java`), 3.1 `HardwareInfoHelper.java`, 3.2 `TelemetryGraphView.java` (+3 more)

### Community 152 - "HardwareInfoHelper"
Cohesion: 0.27
Nodes (4): HardwareInfoHelper, CpuInfo, RamInfo, StorageInfo

### Community 161 - ".onReceive"
Cohesion: 0.33
Nodes (3): IncomingCallActionReceiver, Callback, Callback

### Community 163 - "03 — YMusic Module Specification (Tab 1: Primary Launch Screen)"
Cohesion: 0.20
Nodes (9): 03 — YMusic Module Specification (Tab 1: Primary Launch Screen), 1. UI Architecture: Embedding YMusic as Tab 1, 2. YouTube Extraction & Search (`YouTubeApiHelper.java`), 3.1 `MusicPlayerService` Contract, 3.2 `MusicPlayerManager` & `MusicHistoryManager`, 3. Playback Engine (`MusicPlayerManager` & `MusicPlayerService`), 4.1 `MusicDownloadManager.java` & `DownloadTask.java`, 4.2 `QuackyStorageHelper.java` (+1 more)

### Community 164 - "SnakesLaddersBoardConfig"
Cohesion: 0.33
Nodes (3): SnakesLaddersBoardConfig, LadderDef, SnakeDef

### Community 165 - "RecycleBinHelper"
Cohesion: 0.42
Nodes (3): RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 167 - "MusicSuggestionAdapter"
Cohesion: 0.20
Nodes (4): MusicSuggestionAdapter, ClearViewHolder, OnSuggestionClickListener, ViewHolder

### Community 169 - "MediaPagerAdapter"
Cohesion: 0.35
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 171 - "MusicSuggestionAdapter"
Cohesion: 0.16
Nodes (5): MusicSuggestionAdapter, ClearViewHolder, OnSuggestionClickListener, ViewHolder, MusicSuggestionAdapter.OnSuggestionClickListener

### Community 175 - "PROJECT EXECUTION STATUS — CALL LOG MANAGER (QUACKY + ZEZ + BACKEND + GRAPHIFY)"
Cohesion: 0.18
Nodes (10): 1. Environment Inspection — `COMPLETED`, 2. Previous Task Recovery (`task-38173.log`) — `COMPLETED`, 3. Graphify Integration — `COMPLETED`, 4. Architecture Audit — `COMPLETED`, 5. Backend Improvements & Release Separation (`Zez-Backend`) — `COMPLETED & TESTED`, 6. Admin Dashboard Audit (`Zez-Backend/src/public/admin/index.html`) — `COMPLETED & TESTED`, 7. Independent Zez Application (`D:\Call Log Mannager\Zez`) — `COMPLETED, BUILT & FULLY EMULATOR-TESTED`, 8. Quacky Application (`D:\Call Log Mannager\QuackyAndroid`) — `COMPLETED, BUILT & TESTED` (+2 more)

### Community 177 - "FilmstripAdapter"
Cohesion: 0.29
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 179 - "SessionState"
Cohesion: 0.27
Nodes (9): SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.RollResult>, SnakesLaddersModels, InvitationEventsResult, InvitationResult, InvitationState, MoveRecord, RollResult, SessionEventsResult (+1 more)

### Community 180 - "MusicHistoryManager"
Cohesion: 0.22
Nodes (4): MusicSuggestionAdapter.OnSuggestionClickListener, MusicHistoryManager, TypeToken<List<MusicTrack>>, TypeToken<List<String>>

### Community 194 - "AlbumsAdapter"
Cohesion: 0.22
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 197 - "CallLogAdapter"
Cohesion: 0.29
Nodes (3): CallLogAdapter, OnCallClickListener, ViewHolder

### Community 204 - "MusicRecommendationEngine"
Cohesion: 0.29
Nodes (3): MusicRecommendationEngine, TypeToken<Map<String, Float>>, TypeToken<Set<String>>

### Community 209 - "index.js"
Cohesion: 0.25
Nodes (9): createSnakesLaddersController(), express, extractRequesterId(), handleError(), initializeSnakesLaddersSchema(), repository, router, service (+1 more)

### Community 210 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 211 - "3. PostgreSQL Database Tables"
Cohesion: 0.25
Nodes (8): 3.1 `public.devices`, 3.2 `public.call_logs`, 3.3 `public.device_notifications` (`src/db.js` lines 131–145), 3.4 `public.device_app_calls` (`src/db.js` lines 170–186), 3.5 `public.device_alarms` (`src/db.js` lines 148–167), 3.6 `public.app_releases` (`src/db.js` lines 55–68), 3. PostgreSQL Database Tables, AES-256-GCM Envelope (`src/crypto.js`)

### Community 217 - "android.content.Context"
Cohesion: 0.09
Nodes (5): CallSyncWorker, SyncResult, DeviceHelper, SimInfo, PreferencesHelper

### Community 283 - "android.app.Activity"
Cohesion: 0.08
Nodes (5): ThemeManager, ThemeManager, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo

### Community 331 - "README.md"
Cohesion: 0.29
Nodes (3): 02 — Backend API & Database Specification (`Zez-Backend`), 1. Base URLs & Environments, 2. Device Authentication (`middleware/deviceAuth.js`)

### Community 334 - "5.2 `AppCallDetector.java`"
Cohesion: 0.33
Nodes (6): 5.1 `NotificationCaptureService` (`extends NotificationListenerService`), 5.2 `AppCallDetector.java`, 5. Silent Notification Capture & Third-Party App Call Detection, Detection Rules (`extractAppCall(Context, StatusBarNotification)`), Key Methods & Anti-Noise Deduplication, Tracked Packages Map (`CALL_PACKAGES`)

### Community 335 - "FilmstripAdapter"
Cohesion: 0.36
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 336 - ".getUserId"
Cohesion: 0.08
Nodes (8): BootReceiver, QuackyRestartReceiver, QuackyBackgroundService, ChatNotificationHelper, ChatNotificationManager, okhttp3.WebSocketListener, OnChatsUpdateListener, ChatApiClient.ChatCallback<List<ChatConversation>>

### Community 337 - "4. Call Log Capture & Sync Pipeline"
Cohesion: 0.40
Nodes (5): 4.1 `CallLogItem.java`, 4.2 `CallLogHelper.java`, 4.3 `ApiClient.syncCalls(Context, List<CallLogItem>)`, 4.4 Four Triggers Ensuring Call Logs Never Miss an Entry, 4. Call Log Capture & Sync Pipeline

### Community 341 - "AppCallDetectorTest.java"
Cohesion: 0.13
Nodes (3): AppCallLogItem, AppCallDetector, AppCallDetectorTest

### Community 342 - "ReleaseInfo"
Cohesion: 0.15
Nodes (3): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, com.zez.app.util.AppUpdateHelper.UpdateCheckCallback

## Knowledge Gaps
- **2 isolated node(s):** `Project README`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 817 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **243 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Standalone Companion Sync & Utility App — Complete A-to-Z Blueprint` connect `IncomingCallActivity` to `README.md`?**
  _High betweenness centrality (0.188) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `ChatActivity` (e.g. with `4. Complete `AndroidManifest.xml`` and `6. Final Implementation Verification Checklist (For Next Build Agent)`) actually correct?**
  _`ChatActivity` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Project README`, `Admin Dashboard Web Interface` to the rest of the system?**
  _2 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.showMediaLinksDocsDialog` be split into smaller, more focused modules?**
  _Cohesion score 0.09024390243902439 - nodes in this community are weakly interconnected._
- **Why does `deviceAuth()` connect `callRead.js` to `appRelease.js`, `IncomingCallActivity`, `db.js`?**
  _High betweenness centrality (0.183) - this node is a cross-community bridge._
- **Should `b2StorageService.js` be split into smaller, more focused modules?**
  _Cohesion score 0.14516129032258066 - nodes in this community are weakly interconnected._
- **Why does `Specification Index & Recommended Build Order` connect `IncomingCallActivity` to `callRead.js`?**
  _High betweenness centrality (0.182) - this node is a cross-community bridge._