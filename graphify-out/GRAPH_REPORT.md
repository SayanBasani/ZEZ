# Graph Report - Call Log Mannager  (2026-11-27)

## Corpus Check
- 248 files · ~254,242 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 403 file(s) not represented in the graph (top: .xml 388, .properties 8, (none) 3)
- WARNING: 403 of 676 files seen (60%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 4750 nodes · 13896 edges · 347 communities (107 shown, 240 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 1082 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1ecfad41`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- com.google.gson.JsonObject
- .showMediaLinksDocsDialog
- b2StorageService.js
- db.js
- e2e_device_test.py
- ChatActivity.java
- SnakesLaddersBoardView
- .syncCalls
- com.zez.app.model.ChatMessage
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
- ApiClient
- callRelayWs.js
- ChatActivity
- MusicRecommendationEngine
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
- PlayerListener
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
- PdfGeneratorActivity
- list
- .loadChatsData
- Override
- GalleryAdapter
- GameRuleError
- AlbumDetailActivity
- ChatUser
- AlbumDetailActivity
- ManageStorageActivity
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
- FavoritesHelper
- .onMessage
- ViewHolder
- CallLogAdapter
- Part B: Gallery + PDF/PPT Studio (Tab 3: `R.id.nav_gallery`)
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
- NotificationCaptureService
- MediaPagerAdapter
- MusicSuggestionAdapter
- .onResponse
- PROJECT EXECUTION STATUS — CALL LOG MANAGER (QUACKY + ZEZ + BACKEND + GRAPHIFY)
- 3. PostgreSQL Database Tables
- FilmstripAdapter
- MainActivity
- Override
- .registerDeviceSync
- ChatNotificationManager
- SnakesAndLaddersActivity
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
- FavoritesHelper
- AlarmRingingService
- ChatNotificationHelper
- .onSuccess
- SnakesLaddersBoardView.java
- .run
- PollResult
- FilmstripAdapter
- .isChatRegistered
- Override
- CallLogObserver
- CallLogItem

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 140 edges
2. `ChatActivity` - 100 edges
3. `ChatMessage` - 91 edges
4. `MusicTrack` - 78 edges
5. `ChatApiClient` - 69 edges
6. `MusicActivity` - 67 edges
7. `MusicViewController` - 62 edges
8. `ChatCallback` - 62 edges
9. `CallActivity` - 61 edges
10. `SettingsViewController` - 57 edges

## Surprising Connections (you probably didn't know these)
- `Feature Separation Matrix` --references--> `CallActivity`  [INFERRED]
  Companion-App-Spec/README.md → QuackyAndroid/app/src/main/java/com/zez/app/CallActivity.java
- `Specification Index & Recommended Build Order` --references--> `deviceAuth()`  [INFERRED]
  Companion-App-Spec/README.md → Zez-Backend/src/middleware/deviceAuth.js
- `15. Security Notes & Known Limitations` --references--> `adminAuth()`  [INFERRED]
  PROJECT_DOCUMENTATION.md → Zez-Backend/src/middleware/adminAuth.js
- `4. Complete `AndroidManifest.xml`` --references--> `QuackyBackgroundService`  [INFERRED]
  Companion-App-Spec/00_ARCHITECTURE_AND_PROJECT_SETUP.md → QuackyAndroid/app/src/main/java/com/zez/app/service/QuackyBackgroundService.java
- `6. Final Implementation Verification Checklist (For Next Build Agent)` --references--> `QuackyBackgroundService`  [INFERRED]
  Companion-App-Spec/05_SETTINGS_THEMES_HARDWARE_OTA_SPEC.md → QuackyAndroid/app/src/main/java/com/zez/app/service/QuackyBackgroundService.java

## Import Cycles
- None detected.

## Communities (347 total, 240 thin omitted)

### Community 0 - "com.google.gson.JsonObject"
Cohesion: 0.17
Nodes (5): SnakesLaddersApiClient, ApiCallback, Callback, Callback, Parser

### Community 1 - ".showMediaLinksDocsDialog"
Cohesion: 0.11
Nodes (12): MediaLinksDocsHelper, DocsAdapter, OnDocGoToChatListener, LinkItem, LinksAdapter, OnLinkGoToChatListener, MediaGridAdapter, Holder (+4 more)

### Community 2 - "b2StorageService.js"
Cohesion: 0.14
Nodes (31): express, fs, memoryUpload, multer, path, router, saveToStorageCache(), STORAGE_CACHE_DIR (+23 more)

### Community 3 - "db.js"
Cohesion: 0.08
Nodes (9): crypto, http, crypto, hashPassword(), { Pool }, crypto, http, makeRequest() (+1 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 8 - "SnakesLaddersBoardView"
Cohesion: 0.19
Nodes (3): SnakesLaddersBoardView, AnimatorListenerAdapter, PlayerState

### Community 10 - ".syncCalls"
Cohesion: 0.10
Nodes (4): BootReceiver, SyncResult, CallLogHelper, ZezManagerApplication

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - "MusicViewController"
Cohesion: 0.07
Nodes (9): MusicViewController, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher, MusicSuggestionAdapter.OnSuggestionClickListener (+1 more)

### Community 15 - "package.json"
Cohesion: 0.06
Nodes (41): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, multer, @noble/ciphers, pg, @types/cors (+33 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation"
Cohesion: 0.06
Nodes (37): 4.1 Health Ping, 4.2 Register / Refresh Device, 4.3 Sync Cellular Call Logs, 4.4 Sync Notifications, 4.5 Sync Third-Party App Calls, 4.6 Bidirectional Alarms Sync, 4.7 YMusic Backend Proxy Endpoints (`routes/music.js`), 4.8 OTA Latest Release (`routes/appRelease.js`) (+29 more)

### Community 20 - "MusicTrack"
Cohesion: 0.08
Nodes (6): MusicAdapter, OnTrackClickListener, MusicDownloadHelper, DownloadCallback, TypeToken<List<MusicTrack>>, MusicTrack

### Community 24 - "Override"
Cohesion: 0.05
Nodes (19): com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.PollResult>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationState>, ChatApiClient.ChatCallback<Void>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, android.text.TextWatcher (+11 more)

### Community 25 - "SnakesAndLaddersActivity"
Cohesion: 0.12
Nodes (12): SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.RollResult>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.InvitationState>, SnakesLaddersModels, InvitationEventsResult, InvitationResult, InvitationState (+4 more)

### Community 26 - "admin.js"
Cohesion: 0.10
Nodes (14): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+6 more)

### Community 27 - "Override"
Cohesion: 0.05
Nodes (13): okhttp3.Callback, android.widget.AdapterView.OnItemSelectedListener, android.text.TextWatcher, android.text.TextWatcher, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>>, android.text.style.MetricAffectingSpan, com.zez.app.music.player.MusicPlayerManager.PlayerListener (+5 more)

### Community 29 - "MusicPlayerService"
Cohesion: 0.12
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 32 - "adminAuth.js"
Cohesion: 0.12
Nodes (18): http, run(), run(), http, request(), run(), token, adminAuth() (+10 more)

### Community 33 - "AppCallDetectorTest.java"
Cohesion: 0.12
Nodes (3): MusicRecommendationEngineTest, AppCallLogItem, AppCallDetectorTest

### Community 34 - "ApiClient"
Cohesion: 0.13
Nodes (6): NotificationCaptureService, ApiClient, AlarmSyncCallback, CloudBackupStats, CloudRestoreResult, AppCallDetector

### Community 37 - "callRelayWs.js"
Cohesion: 0.12
Nodes (18): callAudioCache, callMediaCache, express, router, acceptUpgrade(), attachRealtimeWebSockets(), callRooms, createWsClient() (+10 more)

### Community 40 - "ChatActivity"
Cohesion: 0.07
Nodes (6): ChatActivity, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, PendingMessage, SimpleShareContact, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationState>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>

### Community 41 - "MusicRecommendationEngine"
Cohesion: 0.12
Nodes (7): MusicHistoryManager, TypeToken<List<MusicTrack>>, TypeToken<List<String>>, MusicRecommendationEngine, TypeToken<Map<String, Float>>, TypeToken<Set<String>>, ScoredTrack

### Community 43 - "DownloadTask"
Cohesion: 0.07
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, MusicDownloadManager, DownloadProgressListener

### Community 48 - "ChatConversation"
Cohesion: 0.08
Nodes (6): ConversationsAdapter, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, androidx.recyclerview.widget.DiffUtil.Callback, ChatConversation

### Community 49 - "MusicTrack"
Cohesion: 0.09
Nodes (4): MusicDownloadHelper, DownloadCallback, TypeToken<List<MusicTrack>>, MusicTrack

### Community 50 - "ChatApiClient"
Cohesion: 0.12
Nodes (10): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient, ChatCallback, Callback, Callback, GameEventsResult, GameState, Callback (+2 more)

### Community 54 - "MusicPlayerManager"
Cohesion: 0.09
Nodes (3): MusicAdapter, OnTrackClickListener, MusicPlayerManager

### Community 57 - "appRelease.js"
Cohesion: 0.12
Nodes (13): ALLOWED_APP_IDS, crypto, DEFAULT_PACKAGES, downloadsDir, express, fs, multer, parseAppId() (+5 more)

### Community 58 - "CallMediaEngine.java"
Cohesion: 0.11
Nodes (4): CallMediaEngine, WebSocketListener, Callback, Callback

### Community 59 - "YouTubeApiHelper"
Cohesion: 0.09
Nodes (7): YouTubeApiHelper, CachedSearch, CachedStream, Callback, SearchResult, Callback<SearchResult>, Dns

### Community 60 - "java.net.InetAddress"
Cohesion: 0.24
Nodes (3): CookieJar, Dns, CookieJar

### Community 61 - "okhttp3.Call"
Cohesion: 0.09
Nodes (15): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+7 more)

### Community 63 - "AppCallItem"
Cohesion: 0.09
Nodes (4): AppCallAdapter, OnCallActionListener, com.zez.app.adapter.AppCallAdapter.OnCallActionListener, AppCallItem

### Community 65 - "android.net.Uri"
Cohesion: 0.09
Nodes (6): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem, MediaHelper, AlbumItem

### Community 67 - "callRead.js"
Cohesion: 0.11
Nodes (22): 5.2 At-Rest Payload Encryption (`Zez-Backend/src/crypto.js`), crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey() (+14 more)

### Community 68 - "MediaItem"
Cohesion: 0.16
Nodes (3): ImageViewerActivity, ViewPager2.OnPageChangeCallback, MediaItem

### Community 69 - "ChatMessageAdapter"
Cohesion: 0.07
Nodes (8): ChatMessageAdapter, ChatRowItem, OnGameDeclineClickListener, OnGameInviteClickListener, OnMediaRetryListener, OnMessageActionListener, OnQuoteClickListener, ChatMessageAdapter.OnMessageActionListener

### Community 71 - "server.js"
Cohesion: 0.08
Nodes (31): crypto, fs, parseBoolFlag(), parseFlagValue(), path, publish(), closeDatabase(), initializeDatabase() (+23 more)

### Community 72 - "AlarmItem"
Cohesion: 0.08
Nodes (9): AlarmAdapter, OnAlarmClickListener, AlarmsViewController, AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>> (+1 more)

### Community 75 - "ActiveCallManager"
Cohesion: 0.06
Nodes (6): ActiveCallManager, CallStateListener, CallStateListener, Callback, CallMediaEngine.MediaListener, MediaListener

### Community 76 - ".getBaseUrl"
Cohesion: 0.13
Nodes (6): TicTacToeActivity, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>

### Community 78 - "YouTubeApiHelper"
Cohesion: 0.12
Nodes (6): YouTubeApiHelper, CachedSearch, CachedStream, Callback, SearchResult, Callback<SearchResult>

### Community 79 - "GalleryViewController"
Cohesion: 0.25
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 81 - "GalleryPopupHelper"
Cohesion: 0.12
Nodes (8): GalleryPopupHelper.GallerySelectionCallback, GalleryPopupHelper, GalleryAdapter, HeaderVH, GallerySelectionCallback, GridRow, OnTileClickListener, GridLayoutManager.SpanSizeLookup

### Community 84 - "MusicRecommendationEngine"
Cohesion: 0.12
Nodes (4): MusicRecommendationEngine, TypeToken<Map<String, Float>>, TypeToken<Set<String>>, ScoredTrack

### Community 85 - "engine.js"
Cohesion: 0.19
Nodes (12): BOARD_CONFIGS, DEFAULT_BOARD_CONFIG, getBoardConfig(), applyDiceRoll(), buildLookupMaps(), crypto, crypto, CONNECTION_STATUS (+4 more)

### Community 88 - "AlarmHelper"
Cohesion: 0.12
Nodes (6): OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>>, ApiClient.AlarmSyncCallback

### Community 89 - "MusicPlayerService"
Cohesion: 0.09
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 91 - "StorageMediaItem"
Cohesion: 0.15
Nodes (3): ManageStorageAdapter, OnSelectionListener, StorageMediaItem

### Community 92 - "DownloadTask"
Cohesion: 0.05
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, MusicDownloadManager, DownloadProgressListener

### Community 96 - "GalleryAdapter"
Cohesion: 0.11
Nodes (6): GalleryAdapter, GridLayoutManager.SpanSizeLookup, HeaderVH, Listener, MediaVH, GalleryListItem

### Community 97 - "QuickCameraActivity"
Cohesion: 0.11
Nodes (4): QuickCameraActivity, TextureView.SurfaceTextureListener, CameraDevice.StateCallback, CameraCaptureSession.StateCallback

### Community 98 - "IncomingCallActivity"
Cohesion: 0.23
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 100 - "BackupManager"
Cohesion: 0.32
Nodes (4): BackupManager, BackupExportResult, BackupRestoreResult, TypeToken<List<CallLogItem>>

### Community 101 - "MusicActivity"
Cohesion: 0.07
Nodes (8): MusicActivity, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher, SeekBar.OnSeekBarChangeListener

### Community 103 - "ImageViewerActivity"
Cohesion: 0.11
Nodes (3): ImageViewerActivity, ViewPager2.OnPageChangeCallback, MediaItem

### Community 106 - "ChatContactItem"
Cohesion: 0.12
Nodes (3): DisplayItem, OnContactClickListener, ChatContactItem

### Community 107 - "ZoomImageView"
Cohesion: 0.07
Nodes (9): LinkTouchMovementMethod, ZoomImageView, GestureListener, OnTapListener, ScaleListener, ZoomImageView, GestureListener, OnTapListener (+1 more)

### Community 108 - "CallLogItem"
Cohesion: 0.10
Nodes (5): CallLogItem, ApiClient, AlarmSyncCallback, ApiCallback, CallLogHelper

### Community 109 - "PageVH"
Cohesion: 0.33
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 110 - "manager/music/player/MusicPlayerService.java"
Cohesion: 0.06
Nodes (5): AlarmReceiver, CallStateReceiver, IncomingCallActionReceiver, AlarmReceiver, CallStateReceiver

### Community 111 - "android.app.Activity"
Cohesion: 0.06
Nodes (10): ThemeManager, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback, AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback (+2 more)

### Community 113 - "PdfGeneratorActivity"
Cohesion: 0.07
Nodes (12): MusicSuggestionAdapter, ClearViewHolder, OnSuggestionClickListener, ViewHolder, PdfGeneratorActivity, AdjustAdapter, AdjustViewHolder, PreviewAdapter (+4 more)

### Community 114 - "list"
Cohesion: 0.08
Nodes (19): AlarmViewHolder, CallViewHolder, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactViewHolder, ViewHolder, DownloadViewHolder (+11 more)

### Community 116 - "Override"
Cohesion: 0.11
Nodes (6): Callback, Callback, TextureView.SurfaceTextureListener, ActiveCallManager.CallStateListener, CameraDevice.StateCallback, CameraCaptureSession.StateCallback

### Community 117 - "GalleryAdapter"
Cohesion: 0.14
Nodes (4): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, GalleryListItem

### Community 119 - "GameRuleError"
Cohesion: 0.33
Nodes (4): GameRuleError, SnakesLaddersService, runServiceAndMultiplayerTests(), isValidUuid()

### Community 125 - "Tab"
Cohesion: 0.21
Nodes (3): com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, TabLayout.OnTabSelectedListener

### Community 126 - "HistoryAdapter"
Cohesion: 0.23
Nodes (5): HistoryAdapter, VH, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.HistoryResponse>, HistoryMatch, HistoryResponse

### Community 128 - "MusicHistoryManager"
Cohesion: 0.22
Nodes (4): MusicSuggestionAdapter.OnSuggestionClickListener, MusicHistoryManager, TypeToken<List<MusicTrack>>, TypeToken<List<String>>

### Community 129 - "chat.js"
Cohesion: 0.06
Nodes (20): content, fs, lines, navMatch, fs, os, path, avatarStorage (+12 more)

### Community 130 - "4. Complete `AndroidManifest.xml`"
Cohesion: 0.14
Nodes (14): 00 — Architecture & Android Project Setup, 1. Identity & Project Separation Rules, 2. Gradle Configuration (`app/build.gradle`), 3. Target Package Layout, 4. Complete `AndroidManifest.xml`, 5. `CompanionApplication.java` Initialization Order, 6. Main 4-Tab Navigation Architecture (`MainActivity`), Application, Activities, Services, Receivers, FileProvider (+6 more)

### Community 131 - "Adapter"
Cohesion: 0.26
Nodes (3): RecycleBinAdapter, BinViewHolder, OnRestoreClickListener

### Community 132 - "engine.test.js"
Cohesion: 0.28
Nodes (12): coordinatesToSquare(), squareCenterNormalized(), squareToCoordinates(), crypto, rollDice(), createInitialSessionState(), resignSession(), assert (+4 more)

### Community 133 - "MarkdownRenderer.java"
Cohesion: 0.13
Nodes (4): AppCallLogItem, AppCallDetector, MarkdownRenderer, SpanApplier

### Community 135 - "PdfGeneratorActivity"
Cohesion: 0.08
Nodes (10): PdfGeneratorActivity, AdjustAdapter, AdjustViewHolder, PreviewAdapter, PreviewViewHolder, SavedFilesAdapter, FileViewHolder, ItemTouchHelper.SimpleCallback (+2 more)

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

### Community 146 - "ViewHolder"
Cohesion: 0.33
Nodes (3): ContactsAdapter, HeaderViewHolder, ViewHolder

### Community 147 - "CallLogAdapter"
Cohesion: 0.29
Nodes (3): CallLogAdapter, OnCallClickListener, ViewHolder

### Community 148 - "Part B: Gallery + PDF/PPT Studio (Tab 3: `R.id.nav_gallery`)"
Cohesion: 0.17
Nodes (11): 04 — Alarms (Tab 2) & Gallery + PDF/PPT Studio (Tab 3) Specification, 1. Data Model (`AlarmItem.java`), 1. Media & Album Engine (`MediaHelper.java`, `FavoritesHelper.java`, `RecycleBinHelper.java`), 2. Main Gallery Screen (`view_gallery.xml`), 2. Scheduling & Persistence (`AlarmHelper.java`), 3. Alarm Firing Pipeline (`AlarmReceiver`, `AlarmRingingService`, `AlarmFiringActivity`), 3. Vivo-Style Multi-Select Bottom Bar (`gallery_action_bar`), 4. Full-Screen Viewer & Editor (+3 more)

### Community 150 - "05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates"
Cohesion: 0.17
Nodes (12): 05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates, 1. Settings Screen Architecture (`view_settings.xml`), 2.1 Theme Modes (`KEY_THEME_MODE`), 2.2 All 8 Color Palettes (`KEY_COLOR_PALETTE`), 2.3 Font Families & Live Scaling (`ThemeManager.java`), 2. Dynamic Theme, Palette & Font Engine (`ThemeManager.java`, `PreferencesHelper.java`), 3.1 `HardwareInfoHelper.java`, 3.2 `TelemetryGraphView.java` (+4 more)

### Community 152 - "HardwareInfoHelper"
Cohesion: 0.27
Nodes (4): HardwareInfoHelper, CpuInfo, RamInfo, StorageInfo

### Community 154 - "music.js"
Cohesion: 0.12
Nodes (13): @distube/ytdl-core, express, @distube/ytdl-core@^5.2.1, crypto, express, router, express, http (+5 more)

### Community 163 - "03 — YMusic Module Specification (Tab 1: Primary Launch Screen)"
Cohesion: 0.12
Nodes (13): 03 — YMusic Module Specification (Tab 1: Primary Launch Screen), 1. UI Architecture: Embedding YMusic as Tab 1, 2. YouTube Extraction & Search (`YouTubeApiHelper.java`), 3.1 `MusicPlayerService` Contract, 3.2 `MusicPlayerManager` & `MusicHistoryManager`, 3. Playback Engine (`MusicPlayerManager` & `MusicPlayerService`), 4.1 `MusicDownloadManager.java` & `DownloadTask.java`, 4.2 `QuackyStorageHelper.java` (+5 more)

### Community 164 - "SnakesLaddersBoardConfig"
Cohesion: 0.33
Nodes (3): SnakesLaddersBoardConfig, LadderDef, SnakeDef

### Community 165 - "RecycleBinHelper"
Cohesion: 0.42
Nodes (3): RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 169 - "MediaPagerAdapter"
Cohesion: 0.35
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 175 - "PROJECT EXECUTION STATUS — CALL LOG MANAGER (QUACKY + ZEZ + BACKEND + GRAPHIFY)"
Cohesion: 0.18
Nodes (10): 1. Environment Inspection — `COMPLETED`, 2. Previous Task Recovery (`task-38173.log`) — `COMPLETED`, 3. Graphify Integration — `COMPLETED`, 4. Architecture Audit — `COMPLETED`, 5. Backend Improvements & Release Separation (`Zez-Backend`) — `COMPLETED & TESTED`, 6. Admin Dashboard Audit (`Zez-Backend/src/public/admin/index.html`) — `COMPLETED & TESTED`, 7. Independent Zez Application (`D:\Call Log Mannager\Zez`) — `COMPLETED, BUILT & FULLY EMULATOR-TESTED`, 8. Quacky Application (`D:\Call Log Mannager\QuackyAndroid`) — `COMPLETED, BUILT & TESTED` (+2 more)

### Community 176 - "3. PostgreSQL Database Tables"
Cohesion: 0.17
Nodes (11): 02 — Backend API & Database Specification (`Zez-Backend`), 1. Base URLs & Environments, 2. Device Authentication (`middleware/deviceAuth.js`), 3.1 `public.devices`, 3.2 `public.call_logs`, 3.3 `public.device_notifications` (`src/db.js` lines 131–145), 3.4 `public.device_app_calls` (`src/db.js` lines 170–186), 3.5 `public.device_alarms` (`src/db.js` lines 148–167) (+3 more)

### Community 177 - "FilmstripAdapter"
Cohesion: 0.29
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 183 - "ChatNotificationManager"
Cohesion: 0.18
Nodes (3): ChatNotificationManager, LiveMessageListener, OnChatsUpdateListener

### Community 194 - "AlbumsAdapter"
Cohesion: 0.36
Nodes (3): AlbumsAdapter, AlbumVH, OnAlbumClickListener

### Community 195 - "SnakesLaddersEvents"
Cohesion: 0.16
Nodes (6): EventEmitter, SnakesLaddersEvents, assert, createInMemoryRepository(), crypto, GAME_EVENTS

### Community 197 - "index.js"
Cohesion: 0.25
Nodes (9): createSnakesLaddersController(), express, extractRequesterId(), handleError(), initializeSnakesLaddersSchema(), repository, router, service (+1 more)

### Community 198 - "app/MainActivity.java"
Cohesion: 0.07
Nodes (5): AlarmFiringActivity, ContactsActivity, SnakesLaddersHistoryActivity, AlarmFiringActivity, GalleryViewController

### Community 210 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 217 - "android.content.Context"
Cohesion: 0.10
Nodes (3): DeviceHelper, ThemeManager, PreferencesHelper

### Community 332 - "SnakesLaddersBoardView.java"
Cohesion: 0.08
Nodes (3): SnakesLaddersDiceView, TelemetryGraphView, TelemetryGraphView

### Community 334 - "PollResult"
Cohesion: 0.17
Nodes (8): ConversationModeInfo, ChatCallback<MessagesResult>, IncomingCallInfo, LiveUpdateResult, MessagesResult, PeerStatus, PollResult, Callback

### Community 335 - "FilmstripAdapter"
Cohesion: 0.36
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 336 - ".isChatRegistered"
Cohesion: 0.18
Nodes (3): BootReceiver, QuackyRestartReceiver, QuackyBackgroundService

### Community 338 - "CallLogObserver"
Cohesion: 0.38
Nodes (3): CallLogObserver, CallLogListener, ZezApplication

## Knowledge Gaps
- **2 isolated node(s):** `Project README`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 814 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **240 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `deviceAuth()` connect `Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation` to `db.js`, `callRead.js`, `03 — YMusic Module Specification (Tab 1: Primary Launch Screen)`, `music.js`?**
  _High betweenness centrality (0.172) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `ChatActivity` (e.g. with `4. Complete `AndroidManifest.xml`` and `6. Final Implementation Verification Checklist (For Next Build Agent)`) actually correct?**
  _`ChatActivity` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Project README`, `Admin Dashboard Web Interface` to the rest of the system?**
  _2 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.showMediaLinksDocsDialog` be split into smaller, more focused modules?**
  _Cohesion score 0.1076923076923077 - nodes in this community are weakly interconnected._
- **Why does `Specification Index & Recommended Build Order` connect `03 — YMusic Module Specification (Tab 1: Primary Launch Screen)` to `Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation`?**
  _High betweenness centrality (0.171) - this node is a cross-community bridge._
- **Should `b2StorageService.js` be split into smaller, more focused modules?**
  _Cohesion score 0.1361344537815126 - nodes in this community are weakly interconnected._
- **Should `db.js` be split into smaller, more focused modules?**
  _Cohesion score 0.08403361344537816 - nodes in this community are weakly interconnected._