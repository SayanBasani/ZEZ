# Graph Report - Call Log Mannager  (2026-11-27)

## Corpus Check
- 250 files · ~256,138 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 403 file(s) not represented in the graph (top: .xml 388, .properties 8, (none) 3)
- WARNING: 403 of 680 files seen (59%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 4763 nodes · 13915 edges · 352 communities (108 shown, 244 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 1086 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `94a5ee33`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- com.google.gson.JsonObject
- .showMediaLinksDocsDialog
- b2StorageService.js
- db.js
- e2e_device_test.py
- ChatActivity.java
- android.view.View
- ChatActivity.java
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
- log
- MusicPlayerService
- adminAuth.js
- CallLogItem
- android.service.notification.StatusBarNotification
- callRelayWs.js
- .bind
- ChatActivity
- android.graphics.Bitmap
- Project README
- DownloadTask
- Admin Dashboard Web Interface
- ChatConversation
- MusicTrack
- ChatApiClient
- .initViews
- .getAbsoluteMediaUrl
- MusicPlayerManager
- appRelease.js
- CallMediaEngine.java
- YouTubeApiHelper
- java.net.InetAddress
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
- MusicActivity
- ImageViewerActivity
- ViewHolder
- ZoomImageView
- CallLogItem
- PageVH
- manager/music/player/MusicPlayerService.java
- AppUpdateHelper
- MusicPlayerManager
- PdfGeneratorActivity
- .loadChatsData
- Override
- AppUpdateHelper
- GameRuleError
- AlbumDetailActivity
- ChatUser
- AlbumDetailActivity
- ChatConversation
- GalleryViewController
- HistoryAdapter
- ZezStorageHelper
- chat.js
- 4. Complete `AndroidManifest.xml`
- ApiClient
- engine.test.js
- MarkdownRenderer.java
- PdfGeneratorActivity
- Override
- QuackyStorageHelper
- SnakesLaddersRepository
- music.js
- RecycleBinAdapter
- 01 — Silent Background Sync, Notification Capture & App-Call Detection
- android.widget.SeekBar
- FavoritesHelper
- .run
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
- 3. PostgreSQL Database Tables
- FilmstripAdapter
- SessionState
- GalleryAdapter
- CallSyncWorker
- FavoritesHelper
- android.hardware.camera2.CameraCaptureSession
- AlbumsAdapter
- SnakesLaddersEvents
- PhoneHelper
- index.js
- PdfDocumentHelper
- PptxGenerator
- extreme-test-suite.js
- android.content.Context
- auto-release.md
- android.app.Activity
- AlarmRingingService
- ChatNotificationHelper
- com.zez.app.model.MediaItem
- SnakesLaddersBoardView
- Override
- .onResponse
- FilmstripAdapter
- ChatNotificationManager
- .getCallLogs
- android.os.Handler
- 4. Call Log Capture & Sync Pipeline
- README.md
- 5.2 `AppCallDetector.java`
- AppCallLogItem
- Strict Non-Negotiable Rules for the Implementing Agent
- PollResult

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
- `4. Complete `AndroidManifest.xml`` --references--> `ChatActivity`  [INFERRED]
  Companion-App-Spec/00_ARCHITECTURE_AND_PROJECT_SETUP.md → QuackyAndroid/app/src/main/java/com/zez/app/ChatActivity.java
- `6. Final Implementation Verification Checklist (For Next Build Agent)` --references--> `ChatActivity`  [INFERRED]
  Companion-App-Spec/05_SETTINGS_THEMES_HARDWARE_OTA_SPEC.md → QuackyAndroid/app/src/main/java/com/zez/app/ChatActivity.java
- `Strict Non-Negotiable Rules for the Implementing Agent` --references--> `ChatActivity`  [INFERRED]
  Companion-App-Spec/README.md → QuackyAndroid/app/src/main/java/com/zez/app/ChatActivity.java

## Import Cycles
- None detected.

## Communities (352 total, 244 thin omitted)

### Community 0 - "com.google.gson.JsonObject"
Cohesion: 0.17
Nodes (5): SnakesLaddersApiClient, ApiCallback, Callback, Callback, Parser

### Community 1 - ".showMediaLinksDocsDialog"
Cohesion: 0.13
Nodes (10): MediaLinksDocsHelper, DocsAdapter, OnDocGoToChatListener, LinkItem, LinksAdapter, OnLinkGoToChatListener, MediaGridAdapter, OnMediaItemClickListener (+2 more)

### Community 2 - "b2StorageService.js"
Cohesion: 0.11
Nodes (36): express, fs, memoryUpload, multer, path, router, saveToStorageCache(), STORAGE_CACHE_DIR (+28 more)

### Community 3 - "db.js"
Cohesion: 0.07
Nodes (15): crypto, fs, parseBoolFlag(), parseFlagValue(), path, publish(), crypto, crypto (+7 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 8 - "android.view.View"
Cohesion: 0.04
Nodes (28): AlarmViewHolder, AlbumVH, CallViewHolder, IncomingViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder (+20 more)

### Community 9 - "ChatActivity.java"
Cohesion: 0.07
Nodes (3): OutgoingViewHolder, ContactsActivity, SnakesLaddersHistoryActivity

### Community 10 - "MainActivity"
Cohesion: 0.10
Nodes (3): MainActivity, BootReceiver, ZezManagerApplication

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - "MusicViewController"
Cohesion: 0.06
Nodes (12): MusicViewController, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher, MusicSuggestionAdapter.OnSuggestionClickListener (+4 more)

### Community 15 - "package.json"
Cohesion: 0.06
Nodes (41): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, multer, @noble/ciphers, pg, @types/cors (+33 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation"
Cohesion: 0.08
Nodes (26): 10.1 Active Permissions in `ZezAndroid/app/src/main/AndroidManifest.xml`, 10.2 Disabled / Play Protect-Sensitive Features & Exact Restore Instructions, 10. Android Permissions & Play Protect Restore Guide, 11. Backend REST API Reference, 12. PostgreSQL Schema (20 Tables in `public`), 13. Cloud Storage (B2), OTA Releases & Developer Mode, 14. Configuration, Build & Verification Commands, 15. Security Notes & Known Limitations (+18 more)

### Community 20 - "MusicTrack"
Cohesion: 0.07
Nodes (10): MusicAdapter, OnTrackClickListener, MusicDownloadHelper, DownloadCallback, TypeToken<List<MusicTrack>>, MusicTrack, MusicRecommendationEngine, TypeToken<Map<String, Float>> (+2 more)

### Community 24 - "Override"
Cohesion: 0.05
Nodes (18): com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, ChatApiClient.ChatCallback<Void>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationState>, ChatApiClient.ChatCallback<Void>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, android.text.TextWatcher, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, GalleryPopupHelper.GallerySelectionCallback (+10 more)

### Community 25 - "SnakesAndLaddersActivity"
Cohesion: 0.11
Nodes (5): SnakesAndLaddersActivity, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.RollResult>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.InvitationState>

### Community 26 - "admin.js"
Cohesion: 0.10
Nodes (13): AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs, os (+5 more)

### Community 27 - "Override"
Cohesion: 0.05
Nodes (14): okhttp3.Callback, android.widget.AdapterView.OnItemSelectedListener, android.text.TextWatcher, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>>, android.text.style.MetricAffectingSpan, com.zez.app.music.player.MusicPlayerManager.PlayerListener (+6 more)

### Community 28 - "log"
Cohesion: 0.09
Nodes (6): AlarmReceiver, BootReceiver, CallStateReceiver, QuackyRestartReceiver, AlarmReceiver, CallStateReceiver

### Community 29 - "MusicPlayerService"
Cohesion: 0.12
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 32 - "adminAuth.js"
Cohesion: 0.10
Nodes (21): http, run(), http, run(), http, request(), run(), token (+13 more)

### Community 33 - "CallLogItem"
Cohesion: 0.06
Nodes (13): MusicRecommendationEngineTest, CallLogAdapter, OnCallClickListener, ViewHolder, AppCallLogItem, CallLogItem, AppCallDetector, BackupManager (+5 more)

### Community 37 - "callRelayWs.js"
Cohesion: 0.10
Nodes (19): callAudioCache, callMediaCache, express, router, startServer(), acceptUpgrade(), attachRealtimeWebSockets(), callRooms (+11 more)

### Community 40 - "ChatActivity"
Cohesion: 0.06
Nodes (5): ChatActivity, android.text.TextWatcher, PendingMessage, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationState>, LiveMessageListener

### Community 41 - "android.graphics.Bitmap"
Cohesion: 0.08
Nodes (5): AlarmFiringActivity, ImageEditHelper, AlarmFiringActivity, ImageEditActivity, ImageEditHelper

### Community 43 - "DownloadTask"
Cohesion: 0.07
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, MusicDownloadManager, DownloadProgressListener

### Community 48 - "ChatConversation"
Cohesion: 0.12
Nodes (5): ConversationsAdapter, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, androidx.recyclerview.widget.DiffUtil.Callback

### Community 49 - "MusicTrack"
Cohesion: 0.09
Nodes (4): MusicDownloadHelper, DownloadCallback, TypeToken<List<MusicTrack>>, MusicTrack

### Community 50 - "ChatApiClient"
Cohesion: 0.12
Nodes (7): ChatApiClient, ChatCallback, ContactSyncResult, Callback, GameEventsResult, GameState, MediaUploadResult

### Community 53 - ".getAbsoluteMediaUrl"
Cohesion: 0.13
Nodes (3): ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatMessage>, MediaGridAdapter.OnMediaItemClickListener

### Community 54 - "MusicPlayerManager"
Cohesion: 0.09
Nodes (4): MusicAdapter, OnTrackClickListener, SeekBar.OnSeekBarChangeListener, MusicPlayerManager

### Community 57 - "appRelease.js"
Cohesion: 0.12
Nodes (13): ALLOWED_APP_IDS, crypto, DEFAULT_PACKAGES, downloadsDir, express, fs, multer, parseAppId() (+5 more)

### Community 58 - "CallMediaEngine.java"
Cohesion: 0.12
Nodes (3): WebSocketListener, Callback, Callback

### Community 59 - "YouTubeApiHelper"
Cohesion: 0.09
Nodes (7): YouTubeApiHelper, CachedSearch, CachedStream, Callback, SearchResult, Callback<SearchResult>, Dns

### Community 60 - "java.net.InetAddress"
Cohesion: 0.27
Nodes (3): CookieJar, Dns, CookieJar

### Community 61 - "okhttp3.Call"
Cohesion: 0.09
Nodes (16): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+8 more)

### Community 63 - "AppCallItem"
Cohesion: 0.09
Nodes (5): AppCallAdapter, OnCallActionListener, com.zez.app.adapter.AppCallAdapter.OnCallActionListener, AppCallItem, Callback

### Community 65 - "Adapter"
Cohesion: 0.10
Nodes (4): AlbumsAdapter, OnAlbumClickListener, AlbumItem, AlbumItem

### Community 67 - "callRead.js"
Cohesion: 0.06
Nodes (39): 4.1 Health Ping, 4.2 Register / Refresh Device, 4.3 Sync Cellular Call Logs, 4.4 Sync Notifications, 4.5 Sync Third-Party App Calls, 4.6 Bidirectional Alarms Sync, 4.7 YMusic Backend Proxy Endpoints (`routes/music.js`), 4.8 OTA Latest Release (`routes/appRelease.js`) (+31 more)

### Community 68 - "MediaItem"
Cohesion: 0.16
Nodes (3): ImageViewerActivity, ViewPager2.OnPageChangeCallback, MediaItem

### Community 69 - "ChatMessageAdapter"
Cohesion: 0.06
Nodes (11): ChatMessageAdapter, ChatRowItem, DateHeaderViewHolder, OnGameDeclineClickListener, OnGameInviteClickListener, OnMediaRetryListener, OnMessageActionListener, OnQuoteClickListener (+3 more)

### Community 71 - "server.js"
Cohesion: 0.08
Nodes (25): closeDatabase(), adminLoginRateLimiter(), apiRequests, getClientIp(), globalApiRateLimiter(), loginAttempts, sanitizePrototypePollution(), securityHeaders() (+17 more)

### Community 72 - "AlarmItem"
Cohesion: 0.08
Nodes (9): AlarmAdapter, OnAlarmClickListener, AlarmsViewController, AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>> (+1 more)

### Community 75 - "ActiveCallManager"
Cohesion: 0.09
Nodes (3): ActiveCallManager, CallStateListener, MediaListener

### Community 76 - ".getBaseUrl"
Cohesion: 0.13
Nodes (6): TicTacToeActivity, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>

### Community 78 - "YouTubeApiHelper"
Cohesion: 0.10
Nodes (6): YouTubeApiHelper, CachedSearch, CachedStream, Callback, SearchResult, Callback<SearchResult>

### Community 79 - "GalleryViewController"
Cohesion: 0.23
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 81 - "GalleryPopupHelper"
Cohesion: 0.22
Nodes (4): GalleryPopupHelper, GallerySelectionCallback, GridRow, GridLayoutManager.SpanSizeLookup

### Community 84 - "MusicRecommendationEngine"
Cohesion: 0.10
Nodes (4): MusicRecommendationEngine, TypeToken<Map<String, Float>>, TypeToken<Set<String>>, ScoredTrack

### Community 85 - "engine.js"
Cohesion: 0.21
Nodes (10): BOARD_CONFIGS, DEFAULT_BOARD_CONFIG, crypto, crypto, CONNECTION_STATUS, DEFAULT_PLAYER_COLORS, GAME_EVENTS, INVITATION_STATUS (+2 more)

### Community 88 - "AlarmHelper"
Cohesion: 0.11
Nodes (6): OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>>, ApiClient.AlarmSyncCallback

### Community 89 - "MusicPlayerService"
Cohesion: 0.10
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 91 - "StorageMediaItem"
Cohesion: 0.11
Nodes (4): ManageStorageAdapter, OnSelectionListener, ManageStorageActivity, StorageMediaItem

### Community 92 - "DownloadTask"
Cohesion: 0.05
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, MusicDownloadManager, DownloadProgressListener

### Community 96 - "GalleryAdapter"
Cohesion: 0.11
Nodes (4): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, GalleryListItem

### Community 98 - "IncomingCallActivity"
Cohesion: 0.22
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 101 - "MusicActivity"
Cohesion: 0.06
Nodes (11): MusicActivity, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher, MusicSuggestionAdapter.OnSuggestionClickListener (+3 more)

### Community 106 - "ViewHolder"
Cohesion: 0.07
Nodes (4): DisplayItem, OnContactClickListener, TextWatcher, ChatContactItem

### Community 107 - "ZoomImageView"
Cohesion: 0.06
Nodes (9): LinkTouchMovementMethod, ZoomImageView, GestureListener, OnTapListener, ScaleListener, ZoomImageView, GestureListener, OnTapListener (+1 more)

### Community 108 - "CallLogItem"
Cohesion: 0.09
Nodes (5): CallLogItem, ApiClient, AlarmSyncCallback, ApiCallback, CallLogHelper

### Community 109 - "PageVH"
Cohesion: 0.33
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 111 - "AppUpdateHelper"
Cohesion: 0.09
Nodes (7): AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback, DeviceHelper, SimInfo

### Community 113 - "PdfGeneratorActivity"
Cohesion: 0.11
Nodes (8): PdfGeneratorActivity, AdjustAdapter, AdjustViewHolder, PreviewAdapter, PreviewViewHolder, SavedFilesAdapter, FileViewHolder, ItemTouchHelper.SimpleCallback

### Community 116 - "Override"
Cohesion: 0.14
Nodes (5): Callback, Callback, TextureView.SurfaceTextureListener, ActiveCallManager.CallStateListener, CameraDevice.StateCallback

### Community 117 - "AppUpdateHelper"
Cohesion: 0.18
Nodes (4): AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 119 - "GameRuleError"
Cohesion: 0.33
Nodes (4): GameRuleError, SnakesLaddersService, runServiceAndMultiplayerTests(), isValidUuid()

### Community 122 - "AlbumDetailActivity"
Cohesion: 0.09
Nodes (6): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener, GalleryListItem

### Community 125 - "GalleryViewController"
Cohesion: 0.14
Nodes (3): GalleryViewController, GalleryAdapter.Listener, TabLayout.OnTabSelectedListener

### Community 126 - "HistoryAdapter"
Cohesion: 0.23
Nodes (5): HistoryAdapter, VH, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.HistoryResponse>, HistoryMatch, HistoryResponse

### Community 129 - "chat.js"
Cohesion: 0.06
Nodes (20): content, fs, lines, navMatch, fs, os, path, avatarStorage (+12 more)

### Community 130 - "4. Complete `AndroidManifest.xml`"
Cohesion: 0.14
Nodes (14): 00 — Architecture & Android Project Setup, 1. Identity & Project Separation Rules, 2. Gradle Configuration (`app/build.gradle`), 3. Target Package Layout, 4. Complete `AndroidManifest.xml`, 5. `CompanionApplication.java` Initialization Order, 6. Main 4-Tab Navigation Architecture (`MainActivity`), Application, Activities, Services, Receivers, FileProvider (+6 more)

### Community 131 - "ApiClient"
Cohesion: 0.26
Nodes (4): ApiClient, AlarmSyncCallback, CloudBackupStats, CloudRestoreResult

### Community 132 - "engine.test.js"
Cohesion: 0.22
Nodes (15): getBoardConfig(), coordinatesToSquare(), squareCenterNormalized(), squareToCoordinates(), crypto, rollDice(), applyDiceRoll(), buildLookupMaps() (+7 more)

### Community 133 - "MarkdownRenderer.java"
Cohesion: 0.18
Nodes (3): AppCallDetector, MarkdownRenderer, SpanApplier

### Community 135 - "PdfGeneratorActivity"
Cohesion: 0.08
Nodes (9): PdfGeneratorActivity, AdjustAdapter, AdjustViewHolder, PreviewAdapter, PreviewViewHolder, SavedFilesAdapter, ItemTouchHelper.SimpleCallback, PdfDocumentHelper (+1 more)

### Community 136 - "Override"
Cohesion: 0.20
Nodes (3): CallStateListener, Callback, CallMediaEngine.MediaListener

### Community 139 - "music.js"
Cohesion: 0.18
Nodes (9): @distube/ytdl-core, @distube/ytdl-core@^5.2.1, express, http, https, router, streamCache, ytdl (+1 more)

### Community 140 - "RecycleBinAdapter"
Cohesion: 0.30
Nodes (3): RecycleBinAdapter, BinViewHolder, OnRestoreClickListener

### Community 141 - "01 — Silent Background Sync, Notification Capture & App-Call Detection"
Cohesion: 0.15
Nodes (12): 01 — Silent Background Sync, Notification Capture & App-Call Detection, 1. Strict Silent Operation Rules, 2. One-Time Permission & Restricted Settings Onboarding, 3.1 `PreferencesHelper.java`, 3.2 `DeviceHelper.java`, 3.3 `ApiClient.java` (Networking, DNS Resilience, Registration & Sync), 3. Device Identity, Preferences & HTTP Client, 6. Optional Remote Control Toggle Architecture (Recommended Enhancement) (+4 more)

### Community 142 - "android.widget.SeekBar"
Cohesion: 0.09
Nodes (9): ImageEditActivity, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, TextWatcher, AppUpdateHelper.DownloadProgressCallback, Runnable (+1 more)

### Community 148 - "Part B: Gallery + PDF/PPT Studio (Tab 3: `R.id.nav_gallery`)"
Cohesion: 0.17
Nodes (11): 04 — Alarms (Tab 2) & Gallery + PDF/PPT Studio (Tab 3) Specification, 1. Data Model (`AlarmItem.java`), 1. Media & Album Engine (`MediaHelper.java`, `FavoritesHelper.java`, `RecycleBinHelper.java`), 2. Main Gallery Screen (`view_gallery.xml`), 2. Scheduling & Persistence (`AlarmHelper.java`), 3. Alarm Firing Pipeline (`AlarmReceiver`, `AlarmRingingService`, `AlarmFiringActivity`), 3. Vivo-Style Multi-Select Bottom Bar (`gallery_action_bar`), 4. Full-Screen Viewer & Editor (+3 more)

### Community 150 - "05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates"
Cohesion: 0.17
Nodes (12): 05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates, 1. Settings Screen Architecture (`view_settings.xml`), 2.1 Theme Modes (`KEY_THEME_MODE`), 2.2 All 8 Color Palettes (`KEY_COLOR_PALETTE`), 2.3 Font Families & Live Scaling (`ThemeManager.java`), 2. Dynamic Theme, Palette & Font Engine (`ThemeManager.java`, `PreferencesHelper.java`), 3.1 `HardwareInfoHelper.java`, 3.2 `TelemetryGraphView.java` (+4 more)

### Community 151 - "SettingsViewController"
Cohesion: 0.09
Nodes (5): SettingsViewController, HardwareInfoHelper, CpuInfo, RamInfo, StorageInfo

### Community 152 - "HardwareInfoHelper"
Cohesion: 0.27
Nodes (4): HardwareInfoHelper, CpuInfo, RamInfo, StorageInfo

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

### Community 175 - "PROJECT EXECUTION STATUS — CALL LOG MANAGER (QUACKY + ZEZ + BACKEND + GRAPHIFY)"
Cohesion: 0.18
Nodes (10): 1. Environment Inspection — `COMPLETED`, 2. Previous Task Recovery (`task-38173.log`) — `COMPLETED`, 3. Graphify Integration — `COMPLETED`, 4. Architecture Audit — `COMPLETED`, 5. Backend Improvements & Release Separation (`Zez-Backend`) — `COMPLETED & TESTED`, 6. Admin Dashboard Audit (`Zez-Backend/src/public/admin/index.html`) — `COMPLETED & TESTED`, 7. Independent Zez Application (`D:\Call Log Mannager\Zez`) — `COMPLETED, BUILT & FULLY EMULATOR-TESTED`, 8. Quacky Application (`D:\Call Log Mannager\QuackyAndroid`) — `COMPLETED, BUILT & TESTED` (+2 more)

### Community 176 - "3. PostgreSQL Database Tables"
Cohesion: 0.25
Nodes (8): 3.1 `public.devices`, 3.2 `public.call_logs`, 3.3 `public.device_notifications` (`src/db.js` lines 131–145), 3.4 `public.device_app_calls` (`src/db.js` lines 170–186), 3.5 `public.device_alarms` (`src/db.js` lines 148–167), 3.6 `public.app_releases` (`src/db.js` lines 55–68), 3. PostgreSQL Database Tables, AES-256-GCM Envelope (`src/crypto.js`)

### Community 179 - "SessionState"
Cohesion: 0.42
Nodes (8): SnakesLaddersModels, InvitationEventsResult, InvitationResult, InvitationState, MoveRecord, RollResult, SessionEventsResult, SessionState

### Community 180 - "GalleryAdapter"
Cohesion: 0.26
Nodes (3): GalleryAdapter, HeaderVH, OnTileClickListener

### Community 194 - "AlbumsAdapter"
Cohesion: 0.36
Nodes (3): AlbumsAdapter, AlbumVH, OnAlbumClickListener

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
Nodes (4): DeviceHelper, CallSyncWorker, SyncResult, PreferencesHelper

### Community 330 - "ChatNotificationHelper"
Cohesion: 0.15
Nodes (3): ChatNotificationHelper, okhttp3.WebSocketListener, ChatApiClient.ChatCallback<List<ChatConversation>>

### Community 332 - "SnakesLaddersBoardView"
Cohesion: 0.09
Nodes (5): SnakesLaddersBoardView, AnimatorListenerAdapter, PlayerState, TelemetryGraphView, TelemetryGraphView

### Community 334 - ".onResponse"
Cohesion: 0.33
Nodes (4): Callback, ChatCallback<MessagesResult>, MessagesResult, PeerStatus

### Community 336 - "ChatNotificationManager"
Cohesion: 0.14
Nodes (3): QuackyBackgroundService, ChatNotificationManager, OnChatsUpdateListener

### Community 338 - "android.os.Handler"
Cohesion: 0.21
Nodes (4): CallLogObserver, CallLogListener, ZezApplication, CallLogObserver

### Community 343 - "4. Call Log Capture & Sync Pipeline"
Cohesion: 0.40
Nodes (5): 4.1 `CallLogItem.java`, 4.2 `CallLogHelper.java`, 4.3 `ApiClient.syncCalls(Context, List<CallLogItem>)`, 4.4 Four Triggers Ensuring Call Logs Never Miss an Entry, 4. Call Log Capture & Sync Pipeline

### Community 345 - "README.md"
Cohesion: 0.29
Nodes (3): 02 — Backend API & Database Specification (`Zez-Backend`), 1. Base URLs & Environments, 2. Device Authentication (`middleware/deviceAuth.js`)

### Community 349 - "5.2 `AppCallDetector.java`"
Cohesion: 0.33
Nodes (6): 5.1 `NotificationCaptureService` (`extends NotificationListenerService`), 5.2 `AppCallDetector.java`, 5. Silent Notification Capture & Third-Party App Call Detection, Detection Rules (`extractAppCall(Context, StatusBarNotification)`), Key Methods & Anti-Noise Deduplication, Tracked Packages Map (`CALL_PACKAGES`)

### Community 355 - "Strict Non-Negotiable Rules for the Implementing Agent"
Cohesion: 0.50
Nodes (4): Feature Separation Matrix, Specification Index & Recommended Build Order, Standalone Companion Sync & Utility App — Complete A-to-Z Blueprint, Strict Non-Negotiable Rules for the Implementing Agent

### Community 356 - "PollResult"
Cohesion: 0.27
Nodes (5): ChatApiClient.ChatCallback<ChatApiClient.PollResult>, ConversationModeInfo, IncomingCallInfo, LiveUpdateResult, PollResult

## Knowledge Gaps
- **2 isolated node(s):** `Project README`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 817 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **244 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Standalone Companion Sync & Utility App — Complete A-to-Z Blueprint` connect `Strict Non-Negotiable Rules for the Implementing Agent` to `README.md`?**
  _High betweenness centrality (0.171) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `ChatActivity` (e.g. with `4. Complete `AndroidManifest.xml`` and `6. Final Implementation Verification Checklist (For Next Build Agent)`) actually correct?**
  _`ChatActivity` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Project README`, `Admin Dashboard Web Interface` to the rest of the system?**
  _2 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.showMediaLinksDocsDialog` be split into smaller, more focused modules?**
  _Cohesion score 0.13333333333333333 - nodes in this community are weakly interconnected._
- **Why does `deviceAuth()` connect `callRead.js` to `Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation`, `Strict Non-Negotiable Rules for the Implementing Agent`?**
  _High betweenness centrality (0.165) - this node is a cross-community bridge._
- **Should `b2StorageService.js` be split into smaller, more focused modules?**
  _Cohesion score 0.10801393728222997 - nodes in this community are weakly interconnected._
- **Why does `Specification Index & Recommended Build Order` connect `Strict Non-Negotiable Rules for the Implementing Agent` to `callRead.js`?**
  _High betweenness centrality (0.164) - this node is a cross-community bridge._