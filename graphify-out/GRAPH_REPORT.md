# Graph Report - Call Log Mannager  (2026-11-27)

## Corpus Check
- 248 files · ~254,037 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 403 file(s) not represented in the graph (top: .xml 388, .properties 8, (none) 3)
- WARNING: 403 of 676 files seen (60%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 4741 nodes · 13880 edges · 351 communities (112 shown, 239 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 1100 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `821f2ef5`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- com.google.gson.JsonObject
- .showMediaLinksDocsDialog
- b2StorageService.js
- db.js
- e2e_device_test.py
- manager/theme/ThemeManager.java
- SnakesLaddersBoardView
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
- android.content.Intent
- MusicPlayerService
- adminAuth.js
- AppCallDetectorTest.java
- android.service.notification.StatusBarNotification
- callRelayWs.js
- ChatActivity
- Override
- Project README
- DownloadTask
- Admin Dashboard Web Interface
- ChatConversation
- MusicTrack
- ChatApiClient
- .initViews
- ChatMessage
- MusicPlayerManager
- appRelease.js
- CallMediaEngine.java
- YouTubeApiHelper
- java.net.InetAddress
- okhttp3.Call
- AppCallItem
- android.net.Uri
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
- GalleryPopupHelper
- PreferencesHelper
- MusicRecommendationEngine
- engine.js
- com.zez.app.music.model.MusicTrack
- AlarmHelper
- MusicPlayerService
- CallActivity
- StorageMediaItem
- DownloadTask
- GalleryAdapter
- QuickCameraActivity
- IncomingCallActivity
- BackupManager
- MusicActivity
- ImageViewerActivity
- ChatContactItem
- ZoomImageView
- CallLogItem
- PageVH
- manager/music/player/MusicPlayerService.java
- android.app.Activity
- MusicPlayerManager
- Override
- list
- MusicSuggestionAdapter
- Override
- GalleryAdapter
- GameRuleError
- AlbumDetailActivity
- ChatUser
- AlbumDetailActivity
- PdfGeneratorActivity
- Tab
- HistoryAdapter
- ZezStorageHelper
- MusicHistoryManager
- chat.js
- 4. Complete `AndroidManifest.xml`
- Adapter
- engine.test.js
- MarkdownRenderer.java
- PdfGeneratorActivity
- HardwareInfoHelper
- QuackyStorageHelper
- SnakesLaddersRepository
- RecycleBinAdapter
- .triggerManualSync
- android.widget.SeekBar
- android.content.SharedPreferences
- SessionState
- Override
- CallLogAdapter
- Part A: Alarms Module (Tab 2: `R.id.nav_alarms`)
- 05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates
- SettingsViewController
- HardwareInfoHelper
- music.js
- PlayerListener
- SnakesLaddersCoordinates
- .onReceive
- 03 — YMusic Module Specification (Tab 1: Primary Launch Screen)
- SnakesLaddersBoardConfig
- RecycleBinHelper
- ApiClient
- MediaPagerAdapter
- MusicAdapter
- .onResponse
- PROJECT EXECUTION STATUS — CALL LOG MANAGER (QUACKY + ZEZ + BACKEND + GRAPHIFY)
- 3. PostgreSQL Database Tables
- FilmstripAdapter
- MediaItem
- ChatNotificationManager
- .drainPendingQueue
- README.md
- AlbumsAdapter
- SnakesLaddersEvents
- PhoneHelper
- index.js
- app/MainActivity.java
- PdfDocumentHelper
- PptxGenerator
- extreme-test-suite.js
- android.content.Context
- auto-release.md
- .startDirectCall
- AlarmRingingService
- ChatNotificationHelper
- SnakesLaddersDiceView
- .run
- PollResult
- FilmstripAdapter
- .isChatRegistered
- SavedFilesAdapter
- TelemetryGraphView
- Part B: Gallery + PDF/PPT Studio (Tab 3: `R.id.nav_gallery`)
- PdfGeneratorActivity
- AlarmRingingService
- .handleAcceptFromNotificationIfNeeded
- CallLogItem
- 4. Complete `AndroidManifest.xml`
- .getCallLogs
- AppCallLogItem
- PptxGenerator

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 141 edges
2. `ChatActivity` - 101 edges
3. `ChatMessage` - 99 edges
4. `MusicTrack` - 78 edges
5. `ChatApiClient` - 69 edges
6. `MusicActivity` - 68 edges
7. `MusicViewController` - 62 edges
8. `ChatCallback` - 62 edges
9. `CallActivity` - 61 edges
10. `SettingsViewController` - 57 edges

## Surprising Connections (you probably didn't know these)
- `Specification Index & Recommended Build Order` --references--> `deviceAuth()`  [INFERRED]
  Companion-App-Spec/README.md → Zez-Backend/src/middleware/deviceAuth.js
- `15. Security Notes & Known Limitations` --references--> `adminAuth()`  [INFERRED]
  PROJECT_DOCUMENTATION.md → Zez-Backend/src/middleware/adminAuth.js
- `4. Complete `AndroidManifest.xml`` --references--> `ChatActivity`  [INFERRED]
  Companion-App-Spec/00_ARCHITECTURE_AND_PROJECT_SETUP.md → QuackyAndroid/app/src/main/java/com/zez/app/ChatActivity.java
- `6. Final Implementation Verification Checklist (For Next Build Agent)` --references--> `ChatActivity`  [INFERRED]
  Companion-App-Spec/05_SETTINGS_THEMES_HARDWARE_OTA_SPEC.md → QuackyAndroid/app/src/main/java/com/zez/app/ChatActivity.java
- `Strict Non-Negotiable Rules for the Implementing Agent` --references--> `ChatActivity`  [INFERRED]
  Companion-App-Spec/README.md → QuackyAndroid/app/src/main/java/com/zez/app/ChatActivity.java

## Import Cycles
- None detected.

## Communities (351 total, 239 thin omitted)

### Community 0 - "com.google.gson.JsonObject"
Cohesion: 0.17
Nodes (5): SnakesLaddersApiClient, ApiCallback, Callback, Callback, Parser

### Community 1 - ".showMediaLinksDocsDialog"
Cohesion: 0.11
Nodes (13): MediaLinksDocsHelper, DocsAdapter, Holder, OnDocGoToChatListener, LinkItem, LinksAdapter, Holder, OnLinkGoToChatListener (+5 more)

### Community 2 - "b2StorageService.js"
Cohesion: 0.14
Nodes (31): express, fs, memoryUpload, multer, path, router, saveToStorageCache(), STORAGE_CACHE_DIR (+23 more)

### Community 3 - "db.js"
Cohesion: 0.08
Nodes (11): crypto, crypto, hashPassword(), { Pool }, crypto, crypto, express, router (+3 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 8 - "SnakesLaddersBoardView"
Cohesion: 0.14
Nodes (4): SnakesLaddersBoardView, AnimatorListenerAdapter, PlayerState, TelemetryGraphView

### Community 10 - "MainActivity"
Cohesion: 0.11
Nodes (3): MainActivity, BootReceiver, ZezManagerApplication

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - "MusicViewController"
Cohesion: 0.06
Nodes (9): MusicViewController, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher, MusicSuggestionAdapter.OnSuggestionClickListener (+1 more)

### Community 15 - "package.json"
Cohesion: 0.06
Nodes (40): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, multer, @noble/ciphers, pg, @types/cors (+32 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation"
Cohesion: 0.06
Nodes (37): 4.1 Health Ping, 4.2 Register / Refresh Device, 4.3 Sync Cellular Call Logs, 4.4 Sync Notifications, 4.5 Sync Third-Party App Calls, 4.6 Bidirectional Alarms Sync, 4.7 YMusic Backend Proxy Endpoints (`routes/music.js`), 4.8 OTA Latest Release (`routes/appRelease.js`) (+29 more)

### Community 20 - "MusicTrack"
Cohesion: 0.07
Nodes (11): MusicDownloadHelper, DownloadCallback, TypeToken<List<MusicTrack>>, MusicTrack, MusicHistoryManager, TypeToken<List<MusicTrack>>, TypeToken<List<String>>, MusicRecommendationEngine (+3 more)

### Community 24 - "Override"
Cohesion: 0.04
Nodes (19): com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<ChatApiClient.PollResult>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationState>, ChatApiClient.ChatCallback<Void>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, android.text.TextWatcher, android.text.TextWatcher (+11 more)

### Community 25 - "SnakesAndLaddersActivity"
Cohesion: 0.11
Nodes (5): SnakesAndLaddersActivity, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.RollResult>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.InvitationState>

### Community 26 - "admin.js"
Cohesion: 0.10
Nodes (14): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+6 more)

### Community 27 - "Override"
Cohesion: 0.05
Nodes (13): android.widget.AdapterView.OnItemSelectedListener, android.text.TextWatcher, android.text.TextWatcher, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>>, ChatApiClient.ChatCallback<List<ChatConversation>>, android.text.style.MetricAffectingSpan, com.zez.app.music.player.MusicPlayerManager.PlayerListener (+5 more)

### Community 28 - "android.content.Intent"
Cohesion: 0.11
Nodes (4): CallStateReceiver, QuackyRestartReceiver, CallSyncWorker, CallStateReceiver

### Community 29 - "MusicPlayerService"
Cohesion: 0.13
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 32 - "adminAuth.js"
Cohesion: 0.12
Nodes (19): http, run(), http, run(), http, request(), run(), token (+11 more)

### Community 33 - "AppCallDetectorTest.java"
Cohesion: 0.12
Nodes (4): MusicRecommendationEngineTest, AppCallLogItem, AppCallDetector, AppCallDetectorTest

### Community 37 - "callRelayWs.js"
Cohesion: 0.11
Nodes (18): callAudioCache, callMediaCache, express, router, acceptUpgrade(), attachRealtimeWebSockets(), callRooms, createWsClient() (+10 more)

### Community 40 - "ChatActivity"
Cohesion: 0.06
Nodes (6): ChatActivity, ChatApiClient.ChatCallback<ChatUser>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, PendingMessage, SimpleShareContact, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationState>

### Community 41 - "Override"
Cohesion: 0.23
Nodes (3): CameraDevice.StateCallback, TextureView.SurfaceTextureListener, CameraDevice.StateCallback

### Community 43 - "DownloadTask"
Cohesion: 0.06
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, MusicDownloadManager, DownloadProgressListener

### Community 48 - "ChatConversation"
Cohesion: 0.08
Nodes (6): ConversationsAdapter, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, androidx.recyclerview.widget.DiffUtil.Callback, ChatConversation

### Community 49 - "MusicTrack"
Cohesion: 0.09
Nodes (4): MusicDownloadHelper, DownloadCallback, TypeToken<List<MusicTrack>>, MusicTrack

### Community 50 - "ChatApiClient"
Cohesion: 0.14
Nodes (6): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient, ChatCallback, Callback, GameEventsResult, MediaUploadResult

### Community 52 - ".initViews"
Cohesion: 0.09
Nodes (3): AlarmAdapter, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener

### Community 53 - "ChatMessage"
Cohesion: 0.09
Nodes (3): PendingMediaTask, ChatMessage, MediaGridAdapter.OnMediaItemClickListener

### Community 57 - "appRelease.js"
Cohesion: 0.12
Nodes (12): ALLOWED_APP_IDS, crypto, DEFAULT_PACKAGES, downloadsDir, express, fs, multer, parseAppId() (+4 more)

### Community 58 - "CallMediaEngine.java"
Cohesion: 0.11
Nodes (3): CallMediaEngine, WebSocketListener, MediaListener

### Community 59 - "YouTubeApiHelper"
Cohesion: 0.12
Nodes (5): YouTubeApiHelper, CachedSearch, CachedStream, Callback, SearchResult

### Community 60 - "java.net.InetAddress"
Cohesion: 0.16
Nodes (5): CookieJar, Dns, Callback<SearchResult>, CookieJar, Dns

### Community 61 - "okhttp3.Call"
Cohesion: 0.08
Nodes (19): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+11 more)

### Community 63 - "AppCallItem"
Cohesion: 0.09
Nodes (4): AppCallAdapter, OnCallActionListener, com.zez.app.adapter.AppCallAdapter.OnCallActionListener, AppCallItem

### Community 65 - "android.net.Uri"
Cohesion: 0.09
Nodes (6): AlbumsAdapter, OnAlbumClickListener, AlbumItem, CallLogObserver, CallLogListener, CallLogObserver

### Community 67 - "callRead.js"
Cohesion: 0.11
Nodes (24): 5.2 At-Rest Payload Encryption (`Zez-Backend/src/crypto.js`), express, express@^5.1.0, crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson() (+16 more)

### Community 68 - "MediaItem"
Cohesion: 0.16
Nodes (3): ImageViewerActivity, ViewPager2.OnPageChangeCallback, MediaItem

### Community 69 - "ChatMessageAdapter"
Cohesion: 0.07
Nodes (11): 8. Quacky Improvements (`D:\Call Log Mannager\QuackyAndroid`) — `COMPLETED, BUILT & PARTIALLY EMULATOR-TESTED`, ChatMessageAdapter, ChatRowItem, DateHeaderViewHolder, OnGameDeclineClickListener, OnGameInviteClickListener, OnMediaRetryListener, OnMessageActionListener (+3 more)

### Community 71 - "server.js"
Cohesion: 0.07
Nodes (32): crypto, fs, parseBoolFlag(), parseFlagValue(), path, publish(), closeDatabase(), initializeDatabase() (+24 more)

### Community 72 - "AlarmItem"
Cohesion: 0.07
Nodes (9): AlarmAdapter, OnAlarmClickListener, AlarmsViewController, AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>> (+1 more)

### Community 76 - ".getBaseUrl"
Cohesion: 0.16
Nodes (7): TicTacToeActivity, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, GameState

### Community 78 - "YouTubeApiHelper"
Cohesion: 0.10
Nodes (7): YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper, CachedSearch, CachedStream, Callback, SearchResult, Callback<SearchResult>

### Community 79 - "GalleryViewController"
Cohesion: 0.16
Nodes (5): GalleryViewController, MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 81 - "GalleryPopupHelper"
Cohesion: 0.13
Nodes (7): GalleryPopupHelper, GalleryAdapter, HeaderVH, GallerySelectionCallback, GridRow, OnTileClickListener, GridLayoutManager.SpanSizeLookup

### Community 84 - "MusicRecommendationEngine"
Cohesion: 0.23
Nodes (3): MusicRecommendationEngine, TypeToken<Map<String, Float>>, TypeToken<Set<String>>

### Community 85 - "engine.js"
Cohesion: 0.21
Nodes (10): BOARD_CONFIGS, DEFAULT_BOARD_CONFIG, crypto, crypto, CONNECTION_STATUS, DEFAULT_PLAYER_COLORS, GAME_EVENTS, INVITATION_STATUS (+2 more)

### Community 87 - "com.zez.app.music.model.MusicTrack"
Cohesion: 0.12
Nodes (5): MusicAdapter, OnTrackClickListener, TrackViewHolder, PlayerListener, ScoredTrack

### Community 88 - "AlarmHelper"
Cohesion: 0.14
Nodes (5): AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>>, ApiClient.AlarmSyncCallback

### Community 89 - "MusicPlayerService"
Cohesion: 0.09
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 90 - "CallActivity"
Cohesion: 0.11
Nodes (5): Feature Separation Matrix, Specification Index & Recommended Build Order, Standalone Companion Sync & Utility App — Complete A-to-Z Blueprint, Strict Non-Negotiable Rules for the Implementing Agent, CallActivity

### Community 91 - "StorageMediaItem"
Cohesion: 0.11
Nodes (4): ManageStorageAdapter, OnSelectionListener, ManageStorageActivity, StorageMediaItem

### Community 92 - "DownloadTask"
Cohesion: 0.05
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, MusicDownloadManager, DownloadProgressListener

### Community 96 - "GalleryAdapter"
Cohesion: 0.12
Nodes (4): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, GalleryListItem

### Community 98 - "IncomingCallActivity"
Cohesion: 0.18
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 100 - "BackupManager"
Cohesion: 0.29
Nodes (4): BackupManager, BackupExportResult, BackupRestoreResult, TypeToken<List<CallLogItem>>

### Community 101 - "MusicActivity"
Cohesion: 0.07
Nodes (7): MusicActivity, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher, SeekBar.OnSeekBarChangeListener

### Community 106 - "ChatContactItem"
Cohesion: 0.09
Nodes (5): DisplayItem, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 107 - "ZoomImageView"
Cohesion: 0.06
Nodes (9): LinkTouchMovementMethod, ZoomImageView, GestureListener, OnTapListener, ScaleListener, ZoomImageView, GestureListener, OnTapListener (+1 more)

### Community 108 - "CallLogItem"
Cohesion: 0.10
Nodes (5): CallLogItem, ApiClient, AlarmSyncCallback, ApiCallback, CallLogHelper

### Community 109 - "PageVH"
Cohesion: 0.33
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 110 - "manager/music/player/MusicPlayerService.java"
Cohesion: 0.06
Nodes (4): AlarmFiringActivity, AlarmReceiver, AlarmFiringActivity, AlarmReceiver

### Community 111 - "android.app.Activity"
Cohesion: 0.06
Nodes (10): ThemeManager, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback, AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback (+2 more)

### Community 113 - "Override"
Cohesion: 0.22
Nodes (5): AdjustAdapter, AdjustViewHolder, PreviewAdapter, PreviewViewHolder, ItemTouchHelper.SimpleCallback

### Community 114 - "list"
Cohesion: 0.07
Nodes (17): AlarmViewHolder, AlbumVH, CallViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder, FilmstripVH (+9 more)

### Community 115 - "MusicSuggestionAdapter"
Cohesion: 0.22
Nodes (4): MusicSuggestionAdapter, ClearViewHolder, OnSuggestionClickListener, ViewHolder

### Community 116 - "Override"
Cohesion: 0.13
Nodes (4): TextureView.SurfaceTextureListener, ActiveCallManager.CallStateListener, CameraCaptureSession.StateCallback, CameraCaptureSession.StateCallback

### Community 117 - "GalleryAdapter"
Cohesion: 0.13
Nodes (4): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, GalleryListItem

### Community 119 - "GameRuleError"
Cohesion: 0.33
Nodes (4): GameRuleError, SnakesLaddersService, runServiceAndMultiplayerTests(), isValidUuid()

### Community 121 - "ChatUser"
Cohesion: 0.11
Nodes (3): ChatUser, ContactSyncResult, Callback

### Community 125 - "Tab"
Cohesion: 0.21
Nodes (3): com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, TabLayout.OnTabSelectedListener

### Community 126 - "HistoryAdapter"
Cohesion: 0.23
Nodes (6): SnakesLaddersHistoryActivity, HistoryAdapter, VH, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.HistoryResponse>, HistoryMatch, HistoryResponse

### Community 128 - "MusicHistoryManager"
Cohesion: 0.22
Nodes (4): MusicSuggestionAdapter.OnSuggestionClickListener, MusicHistoryManager, TypeToken<List<MusicTrack>>, TypeToken<List<String>>

### Community 129 - "chat.js"
Cohesion: 0.06
Nodes (20): content, fs, lines, navMatch, fs, os, path, avatarStorage (+12 more)

### Community 130 - "4. Complete `AndroidManifest.xml`"
Cohesion: 0.22
Nodes (9): 00 — Architecture & Android Project Setup, 1. Identity & Project Separation Rules, 2. Gradle Configuration (`app/build.gradle`), 3. Target Package Layout, 5. `CompanionApplication.java` Initialization Order, 6. Main 4-Tab Navigation Architecture (`MainActivity`), Dependencies, `res/menu/bottom_nav_menu.xml` (+1 more)

### Community 131 - "Adapter"
Cohesion: 0.26
Nodes (3): RecycleBinAdapter, BinViewHolder, OnRestoreClickListener

### Community 132 - "engine.test.js"
Cohesion: 0.22
Nodes (15): getBoardConfig(), coordinatesToSquare(), squareCenterNormalized(), squareToCoordinates(), crypto, rollDice(), applyDiceRoll(), buildLookupMaps() (+7 more)

### Community 133 - "MarkdownRenderer.java"
Cohesion: 0.18
Nodes (3): AppCallDetector, MarkdownRenderer, SpanApplier

### Community 135 - "PdfGeneratorActivity"
Cohesion: 0.15
Nodes (7): AdjustAdapter, AdjustViewHolder, PreviewAdapter, PreviewViewHolder, SavedFilesAdapter, FileViewHolder, ItemTouchHelper.SimpleCallback

### Community 136 - "HardwareInfoHelper"
Cohesion: 0.28
Nodes (4): HardwareInfoHelper, CpuInfo, RamInfo, StorageInfo

### Community 140 - "RecycleBinAdapter"
Cohesion: 0.30
Nodes (3): RecycleBinAdapter, BinViewHolder, OnRestoreClickListener

### Community 141 - ".triggerManualSync"
Cohesion: 0.08
Nodes (23): 01 — Silent Background Sync, Notification Capture & App-Call Detection, 1. Strict Silent Operation Rules, 2. One-Time Permission & Restricted Settings Onboarding, 3.1 `PreferencesHelper.java`, 3.2 `DeviceHelper.java`, 3.3 `ApiClient.java` (Networking, DNS Resilience, Registration & Sync), 3. Device Identity, Preferences & HTTP Client, 4.1 `CallLogItem.java` (+15 more)

### Community 142 - "android.widget.SeekBar"
Cohesion: 0.06
Nodes (13): ImageEditActivity, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, ImageEditHelper, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, TextWatcher (+5 more)

### Community 145 - "SessionState"
Cohesion: 0.42
Nodes (8): SnakesLaddersModels, InvitationEventsResult, InvitationResult, InvitationState, MoveRecord, RollResult, SessionEventsResult, SessionState

### Community 147 - "CallLogAdapter"
Cohesion: 0.33
Nodes (3): CallLogAdapter, OnCallClickListener, ViewHolder

### Community 148 - "Part A: Alarms Module (Tab 2: `R.id.nav_alarms`)"
Cohesion: 0.33
Nodes (5): 04 — Alarms (Tab 2) & Gallery + PDF/PPT Studio (Tab 3) Specification, 1. Data Model (`AlarmItem.java`), 2. Scheduling & Persistence (`AlarmHelper.java`), 3. Alarm Firing Pipeline (`AlarmReceiver`, `AlarmRingingService`, `AlarmFiringActivity`), Part A: Alarms Module (Tab 2: `R.id.nav_alarms`)

### Community 150 - "05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates"
Cohesion: 0.17
Nodes (12): 05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates, 1. Settings Screen Architecture (`view_settings.xml`), 2.1 Theme Modes (`KEY_THEME_MODE`), 2.2 All 8 Color Palettes (`KEY_COLOR_PALETTE`), 2.3 Font Families & Live Scaling (`ThemeManager.java`), 2. Dynamic Theme, Palette & Font Engine (`ThemeManager.java`, `PreferencesHelper.java`), 3.1 `HardwareInfoHelper.java`, 3.2 `TelemetryGraphView.java` (+4 more)

### Community 152 - "HardwareInfoHelper"
Cohesion: 0.27
Nodes (4): HardwareInfoHelper, CpuInfo, RamInfo, StorageInfo

### Community 154 - "music.js"
Cohesion: 0.18
Nodes (9): @distube/ytdl-core, @distube/ytdl-core@^5.2.1, express, http, https, router, streamCache, ytdl (+1 more)

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

### Community 167 - "ApiClient"
Cohesion: 0.26
Nodes (4): ApiClient, AlarmSyncCallback, CloudBackupStats, CloudRestoreResult

### Community 169 - "MediaPagerAdapter"
Cohesion: 0.35
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 171 - "MusicAdapter"
Cohesion: 0.11
Nodes (7): MusicAdapter, OnTrackClickListener, TrackViewHolder, MusicSuggestionAdapter, ClearViewHolder, OnSuggestionClickListener, ViewHolder

### Community 175 - "PROJECT EXECUTION STATUS — CALL LOG MANAGER (QUACKY + ZEZ + BACKEND + GRAPHIFY)"
Cohesion: 0.20
Nodes (9): 1. Environment Inspection — `COMPLETED`, 2. Previous Task Recovery (`task-38173.log`) — `COMPLETED`, 3. Graphify Integration — `COMPLETED`, 4. Architecture Audit — `COMPLETED`, 5. Backend Improvements & Release Separation (`Zez-Backend`) — `COMPLETED & TESTED`, 6. Admin Dashboard Audit (`Zez-Backend/src/public/admin/index.html`) — `COMPLETED & TESTED`, 7. Independent Zez Application (`D:\Call Log Mannager\Zez`) — `COMPLETED, BUILT & EMULATOR-TESTED`, PROJECT EXECUTION STATUS — CALL LOG MANAGER (QUACKY + ZEZ + BACKEND + GRAPHIFY) (+1 more)

### Community 176 - "3. PostgreSQL Database Tables"
Cohesion: 0.25
Nodes (8): 3.1 `public.devices`, 3.2 `public.call_logs`, 3.3 `public.device_notifications` (`src/db.js` lines 131–145), 3.4 `public.device_app_calls` (`src/db.js` lines 170–186), 3.5 `public.device_alarms` (`src/db.js` lines 148–167), 3.6 `public.app_releases` (`src/db.js` lines 55–68), 3. PostgreSQL Database Tables, AES-256-GCM Envelope (`src/crypto.js`)

### Community 183 - "ChatNotificationManager"
Cohesion: 0.11
Nodes (5): ChatNotificationManager, okhttp3.WebSocketListener, LiveMessageListener, OnChatsUpdateListener, ChatApiClient.ChatCallback<List<ChatConversation>>

### Community 193 - "README.md"
Cohesion: 0.25
Nodes (3): 02 — Backend API & Database Specification (`Zez-Backend`), 1. Base URLs & Environments, 2. Device Authentication (`middleware/deviceAuth.js`)

### Community 194 - "AlbumsAdapter"
Cohesion: 0.22
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 195 - "SnakesLaddersEvents"
Cohesion: 0.18
Nodes (5): EventEmitter, SnakesLaddersEvents, assert, createInMemoryRepository(), crypto

### Community 197 - "index.js"
Cohesion: 0.25
Nodes (9): createSnakesLaddersController(), express, extractRequesterId(), handleError(), initializeSnakesLaddersSchema(), repository, router, service (+1 more)

### Community 210 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 217 - "android.content.Context"
Cohesion: 0.09
Nodes (6): DeviceHelper, CallSyncWorker, SyncResult, DeviceHelper, SimInfo, PreferencesHelper

### Community 334 - "PollResult"
Cohesion: 0.19
Nodes (7): ConversationModeInfo, IncomingCallInfo, LiveUpdateResult, MessagesResult, PeerStatus, PollResult, Callback

### Community 335 - "FilmstripAdapter"
Cohesion: 0.36
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 336 - ".isChatRegistered"
Cohesion: 0.11
Nodes (4): ChatApiClient.ChatCallback<Boolean>, BootReceiver, QuackyBackgroundService, ZezApplication

### Community 339 - "Part B: Gallery + PDF/PPT Studio (Tab 3: `R.id.nav_gallery`)"
Cohesion: 0.33
Nodes (6): 1. Media & Album Engine (`MediaHelper.java`, `FavoritesHelper.java`, `RecycleBinHelper.java`), 2. Main Gallery Screen (`view_gallery.xml`), 3. Vivo-Style Multi-Select Bottom Bar (`gallery_action_bar`), 4. Full-Screen Viewer & Editor, 5. Document Generator (`PdfGeneratorActivity`, `PdfDocumentHelper`, `PptxGenerator`), Part B: Gallery + PDF/PPT Studio (Tab 3: `R.id.nav_gallery`)

### Community 342 - ".handleAcceptFromNotificationIfNeeded"
Cohesion: 0.09
Nodes (5): Callback, Callback, Callback, Callback, Callback

### Community 344 - "4. Complete `AndroidManifest.xml`"
Cohesion: 0.40
Nodes (5): 4. Complete `AndroidManifest.xml`, Application, Activities, Services, Receivers, FileProvider, Permissions, Queries Block (Optional / Clean Share & System Intents), `res/xml/file_paths.xml`

## Knowledge Gaps
- **2 isolated node(s):** `Project README`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 807 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **239 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Standalone Companion Sync & Utility App — Complete A-to-Z Blueprint` connect `CallActivity` to `README.md`?**
  _High betweenness centrality (0.149) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ChatActivity` (e.g. with `4. Complete `AndroidManifest.xml`` and `6. Final Implementation Verification Checklist (For Next Build Agent)`) actually correct?**
  _`ChatActivity` has 5 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Project README`, `Admin Dashboard Web Interface` to the rest of the system?**
  _2 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.showMediaLinksDocsDialog` be split into smaller, more focused modules?**
  _Cohesion score 0.11095305832147938 - nodes in this community are weakly interconnected._
- **Why does `deviceAuth()` connect `Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation` to `callRead.js`, `CallActivity`, `db.js`?**
  _High betweenness centrality (0.142) - this node is a cross-community bridge._
- **Should `b2StorageService.js` be split into smaller, more focused modules?**
  _Cohesion score 0.1361344537815126 - nodes in this community are weakly interconnected._
- **Why does `Specification Index & Recommended Build Order` connect `CallActivity` to `Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation`?**
  _High betweenness centrality (0.142) - this node is a cross-community bridge._