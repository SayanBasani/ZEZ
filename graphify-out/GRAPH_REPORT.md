# Graph Report - Call Log Mannager  (2026-11-27)

## Corpus Check
- 250 files · ~258,580 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 406 file(s) not represented in the graph (top: .xml 391, .properties 8, (none) 3)
- WARNING: 406 of 683 files seen (59%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 4782 nodes · 13990 edges · 350 communities (99 shown, 251 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 1117 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `de9c402c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- com.google.gson.JsonObject
- .showMediaLinksDocsDialog
- b2StorageService.js
- db.js
- e2e_device_test.py
- ZoomImageView
- ViewHolder
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
- AlarmsViewController.java
- MusicPlayerService
- adminAuth.js
- CallLogItem
- android.service.notification.StatusBarNotification
- callRelayWs.js
- .bind
- ChatActivity
- android.os.Bundle
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
- MusicActivity
- ImageViewerActivity
- ViewHolder
- ZoomImageView
- CallLogItem
- PageVH
- android.content.Intent
- AppUpdateHelper
- MusicPlayerManager
- PdfGeneratorActivity
- android.view.View
- .loadChatsData
- ApiClient
- AppUpdateHelper
- ChatNotificationManager
- AlbumDetailActivity
- ChatUser
- AlbumDetailActivity
- ChatConversation
- Override
- HistoryAdapter
- ZezStorageHelper
- MusicDownloadHelper
- chat.js
- 4. Complete `AndroidManifest.xml`
- manager/service/CallSyncWorker.java
- .bindMedia
- MarkdownRenderer.java
- PdfGeneratorActivity
- Override
- QuackyStorageHelper
- SnakesLaddersRepository
- RecycleBinAdapter
- 01 — Silent Background Sync, Notification Capture & App-Call Detection
- android.widget.SeekBar
- android.content.SharedPreferences
- LinkTouchMovementMethod.java
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
- 3. PostgreSQL Database Tables
- SnakesLaddersBoardConfig
- RecycleBinHelper
- MusicSuggestionAdapter
- AlarmItem
- MediaPagerAdapter
- MusicSuggestionAdapter
- .onResponse
- PROJECT EXECUTION STATUS — CALL LOG MANAGER (QUACKY + ZEZ + BACKEND + GRAPHIFY)
- .isDeletedForEveryone
- FilmstripAdapter
- SessionState
- MusicHistoryManager
- android.hardware.camera2.CameraCaptureSession
- AlarmRingingService
- AlbumsAdapter
- ChatNotificationHelper
- PhoneHelper
- .onResponse
- GalleryViewController
- PdfDocumentHelper
- PptxGenerator
- extreme-test-suite.js
- android.content.Context
- auto-release.md
- .checkIncomingCalls
- android.app.Activity
- AlarmRingingService
- .onMessage
- MusicHistoryManager
- SnakesLaddersBoardView
- android.hardware.camera2.CameraDevice
- .getMessages
- FilmstripAdapter
- ChatNotificationManager
- .getCallLogs
- CallLogObserver
- AppCallLogItem
- AppCallLogItem

## God Nodes (most connected - your core abstractions)
1. `ChatMessage` - 148 edges
2. `MainActivity` - 138 edges
3. `ChatActivity` - 104 edges
4. `MusicTrack` - 78 edges
5. `ChatApiClient` - 69 edges
6. `MusicActivity` - 67 edges
7. `ChatCallback` - 63 edges
8. `MusicViewController` - 62 edges
9. `CallActivity` - 61 edges
10. `ChatMessageAdapter` - 58 edges

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

## Communities (350 total, 251 thin omitted)

### Community 0 - "com.google.gson.JsonObject"
Cohesion: 0.24
Nodes (3): SnakesLaddersApiClient, ApiCallback, Parser

### Community 1 - ".showMediaLinksDocsDialog"
Cohesion: 0.12
Nodes (11): MediaLinksDocsHelper, DocsAdapter, OnDocGoToChatListener, LinkItem, LinksAdapter, OnLinkGoToChatListener, MediaGridAdapter, OnMediaItemClickListener (+3 more)

### Community 2 - "b2StorageService.js"
Cohesion: 0.20
Nodes (23): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+15 more)

### Community 3 - "db.js"
Cohesion: 0.08
Nodes (11): crypto, crypto, hashPassword(), { Pool }, crypto, crypto, express, router (+3 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 7 - "ZoomImageView"
Cohesion: 0.20
Nodes (4): ZoomImageView, GestureListener, OnTapListener, ScaleListener

### Community 8 - "ViewHolder"
Cohesion: 0.28
Nodes (4): ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - "MusicViewController"
Cohesion: 0.06
Nodes (9): MusicViewController, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher, MusicSuggestionAdapter.OnSuggestionClickListener (+1 more)

### Community 15 - "package.json"
Cohesion: 0.06
Nodes (42): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, @distube/ytdl-core, dotenv, multer, @noble/ciphers, pg (+34 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation"
Cohesion: 0.08
Nodes (26): 10.1 Active Permissions in `ZezAndroid/app/src/main/AndroidManifest.xml`, 10.2 Disabled / Play Protect-Sensitive Features & Exact Restore Instructions, 10. Android Permissions & Play Protect Restore Guide, 11. Backend REST API Reference, 12. PostgreSQL Schema (20 Tables in `public`), 13. Cloud Storage (B2), OTA Releases & Developer Mode, 14. Configuration, Build & Verification Commands, 15. Security Notes & Known Limitations (+18 more)

### Community 20 - "MusicTrack"
Cohesion: 0.07
Nodes (9): MusicAdapter, OnTrackClickListener, TrackViewHolder, MusicTrack, TypeToken<List<MusicTrack>>, MusicRecommendationEngine, TypeToken<Map<String, Float>>, TypeToken<Set<String>> (+1 more)

### Community 24 - "Override"
Cohesion: 0.04
Nodes (22): com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, ChatApiClient.ChatCallback<Void>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationState>, ChatApiClient.ChatCallback<Void>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, android.text.TextWatcher, android.text.TextWatcher, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult> (+14 more)

### Community 25 - "SnakesAndLaddersActivity"
Cohesion: 0.11
Nodes (5): SnakesAndLaddersActivity, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.RollResult>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.InvitationState>

### Community 26 - "admin.js"
Cohesion: 0.04
Nodes (43): express, express@^5.1.0, verifyPassword(), adminLoginRateLimiter(), apiRequests, getClientIp(), globalApiRateLimiter(), loginAttempts (+35 more)

### Community 27 - "Override"
Cohesion: 0.05
Nodes (12): android.widget.AdapterView.OnItemSelectedListener, android.text.TextWatcher, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>>, android.text.style.MetricAffectingSpan, com.zez.app.music.player.MusicPlayerManager.PlayerListener (+4 more)

### Community 29 - "MusicPlayerService"
Cohesion: 0.11
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 32 - "adminAuth.js"
Cohesion: 0.10
Nodes (21): http, run(), http, run(), http, request(), run(), token (+13 more)

### Community 33 - "CallLogItem"
Cohesion: 0.07
Nodes (12): MusicRecommendationEngineTest, CallLogAdapter, OnCallClickListener, ViewHolder, CallLogItem, AppCallDetector, BackupManager, BackupExportResult (+4 more)

### Community 37 - "callRelayWs.js"
Cohesion: 0.11
Nodes (18): callAudioCache, callMediaCache, express, router, acceptUpgrade(), attachRealtimeWebSockets(), callRooms, createWsClient() (+10 more)

### Community 40 - "ChatActivity"
Cohesion: 0.06
Nodes (6): ChatActivity, ChatApiClient.ChatCallback<ChatUser>, SimpleShareContact, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, LiveMessageListener

### Community 43 - "DownloadTask"
Cohesion: 0.06
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, MusicDownloadManager, DownloadProgressListener

### Community 48 - "ChatConversation"
Cohesion: 0.13
Nodes (5): ConversationsAdapter, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, androidx.recyclerview.widget.DiffUtil.Callback

### Community 49 - "MusicTrack"
Cohesion: 0.09
Nodes (4): MusicDownloadHelper, DownloadCallback, TypeToken<List<MusicTrack>>, MusicTrack

### Community 50 - "ChatApiClient"
Cohesion: 0.10
Nodes (20): ChatApiClient, ChatCallback, Callback, ContactSyncResult, ConversationModeInfo, Callback, Callback, Callback (+12 more)

### Community 53 - "ChatMessage"
Cohesion: 0.08
Nodes (4): ChatApiClient.ChatCallback<ChatMessage>, PendingMediaTask, PendingMessage, ChatMessage

### Community 57 - "appRelease.js"
Cohesion: 0.12
Nodes (13): ALLOWED_APP_IDS, crypto, DEFAULT_PACKAGES, downloadsDir, express, fs, multer, parseAppId() (+5 more)

### Community 58 - "CallMediaEngine.java"
Cohesion: 0.08
Nodes (5): CallMediaEngine, WebSocketListener, MediaListener, Callback, Callback

### Community 59 - "YouTubeApiHelper"
Cohesion: 0.13
Nodes (5): YouTubeApiHelper, CachedSearch, CachedStream, Callback, SearchResult

### Community 60 - "java.net.InetAddress"
Cohesion: 0.16
Nodes (5): CookieJar, Dns, Callback<SearchResult>, CookieJar, Dns

### Community 61 - "okhttp3.Call"
Cohesion: 0.11
Nodes (9): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+1 more)

### Community 63 - "AppCallItem"
Cohesion: 0.09
Nodes (5): AppCallAdapter, CallViewHolder, OnCallActionListener, com.zez.app.adapter.AppCallAdapter.OnCallActionListener, AppCallItem

### Community 65 - "Adapter"
Cohesion: 0.11
Nodes (5): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem, AlbumItem

### Community 67 - "callRead.js"
Cohesion: 0.08
Nodes (34): 4.1 Health Ping, 4.2 Register / Refresh Device, 4.3 Sync Cellular Call Logs, 4.4 Sync Notifications, 4.5 Sync Third-Party App Calls, 4.6 Bidirectional Alarms Sync, 4.7 YMusic Backend Proxy Endpoints (`routes/music.js`), 4.8 OTA Latest Release (`routes/appRelease.js`) (+26 more)

### Community 68 - "MediaItem"
Cohesion: 0.16
Nodes (3): ImageViewerActivity, ViewPager2.OnPageChangeCallback, MediaItem

### Community 69 - "ChatMessageAdapter"
Cohesion: 0.07
Nodes (9): ChatMessageAdapter, ChatRowItem, DateHeaderViewHolder, OnGameDeclineClickListener, OnGameInviteClickListener, OnMediaRetryListener, OnMessageActionListener, OnQuoteClickListener (+1 more)

### Community 71 - "server.js"
Cohesion: 0.08
Nodes (26): crypto, fs, parseBoolFlag(), parseFlagValue(), path, publish(), closeDatabase(), initializeDatabase() (+18 more)

### Community 72 - "AlarmItem"
Cohesion: 0.07
Nodes (10): AlarmAdapter, OnAlarmClickListener, AlarmsViewController, AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>> (+2 more)

### Community 76 - ".getBaseUrl"
Cohesion: 0.12
Nodes (7): Strict Non-Negotiable Rules for the Implementing Agent, TicTacToeActivity, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>

### Community 78 - "YouTubeApiHelper"
Cohesion: 0.11
Nodes (6): YouTubeApiHelper, CachedSearch, CachedStream, Callback, SearchResult, Callback<SearchResult>

### Community 79 - "GalleryViewController"
Cohesion: 0.28
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 81 - "GalleryAdapter"
Cohesion: 0.12
Nodes (8): GalleryPopupHelper, GalleryAdapter, HeaderVH, ThumbVH, GallerySelectionCallback, GridRow, OnTileClickListener, GridLayoutManager.SpanSizeLookup

### Community 84 - "MusicRecommendationEngine"
Cohesion: 0.11
Nodes (6): MusicAdapter, OnTrackClickListener, MusicRecommendationEngine, TypeToken<Map<String, Float>>, TypeToken<Set<String>>, ScoredTrack

### Community 85 - "engine.js"
Cohesion: 0.08
Nodes (34): BOARD_CONFIGS, DEFAULT_BOARD_CONFIG, getBoardConfig(), coordinatesToSquare(), squareCenterNormalized(), squareToCoordinates(), crypto, rollDice() (+26 more)

### Community 87 - "SnakesLaddersBoardView"
Cohesion: 0.19
Nodes (3): SnakesLaddersBoardView, AnimatorListenerAdapter, PlayerState

### Community 88 - "AlarmHelper"
Cohesion: 0.12
Nodes (6): OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>>, ApiClient.AlarmSyncCallback

### Community 89 - "MusicPlayerService"
Cohesion: 0.11
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 90 - "CallActivity"
Cohesion: 0.08
Nodes (7): CallActivity, Callback, Callback, TextureView.SurfaceTextureListener, ActiveCallManager.CallStateListener, Runnable, Callback

### Community 91 - "StorageMediaItem"
Cohesion: 0.11
Nodes (4): ManageStorageAdapter, OnSelectionListener, ManageStorageActivity, StorageMediaItem

### Community 92 - "DownloadTask"
Cohesion: 0.05
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, MusicDownloadManager, DownloadProgressListener

### Community 96 - "GalleryAdapter"
Cohesion: 0.11
Nodes (6): GalleryAdapter, GridLayoutManager.SpanSizeLookup, HeaderVH, Listener, MediaVH, GalleryListItem

### Community 97 - "QuickCameraActivity"
Cohesion: 0.13
Nodes (3): QuickCameraActivity, TextureView.SurfaceTextureListener, CameraCaptureSession.StateCallback

### Community 98 - "IncomingCallActivity"
Cohesion: 0.24
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 101 - "MusicActivity"
Cohesion: 0.06
Nodes (8): MusicActivity, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher, SeekBar.OnSeekBarChangeListener

### Community 106 - "ViewHolder"
Cohesion: 0.09
Nodes (5): DisplayItem, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 107 - "ZoomImageView"
Cohesion: 0.12
Nodes (4): ZoomImageView, GestureListener, OnTapListener, ScaleListener

### Community 109 - "PageVH"
Cohesion: 0.33
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 110 - "android.content.Intent"
Cohesion: 0.06
Nodes (4): AlarmReceiver, CallStateReceiver, AlarmReceiver, CallStateReceiver

### Community 111 - "AppUpdateHelper"
Cohesion: 0.09
Nodes (7): AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback, DeviceHelper, SimInfo

### Community 113 - "PdfGeneratorActivity"
Cohesion: 0.11
Nodes (8): PdfGeneratorActivity, AdjustAdapter, AdjustViewHolder, PreviewAdapter, PreviewViewHolder, SavedFilesAdapter, FileViewHolder, ItemTouchHelper.SimpleCallback

### Community 114 - "android.view.View"
Cohesion: 0.08
Nodes (13): AlarmViewHolder, IncomingViewHolder, ViewHolder, SnakesLaddersHistoryActivity, DownloadViewHolder, TrackViewHolder, Holder, Holder (+5 more)

### Community 116 - "ApiClient"
Cohesion: 0.22
Nodes (3): ApiClient, AlarmSyncCallback, DeviceHelper

### Community 117 - "AppUpdateHelper"
Cohesion: 0.19
Nodes (4): AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 122 - "AlbumDetailActivity"
Cohesion: 0.13
Nodes (4): GalleryAdapter, AlbumDetailActivity, GalleryAdapter.Listener, GalleryListItem

### Community 126 - "HistoryAdapter"
Cohesion: 0.23
Nodes (5): HistoryAdapter, VH, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.HistoryResponse>, HistoryMatch, HistoryResponse

### Community 128 - "MusicDownloadHelper"
Cohesion: 0.23
Nodes (3): MusicDownloadHelper, DownloadCallback, TypeToken<List<MusicTrack>>

### Community 129 - "chat.js"
Cohesion: 0.06
Nodes (20): content, fs, lines, navMatch, fs, os, path, avatarStorage (+12 more)

### Community 130 - "4. Complete `AndroidManifest.xml`"
Cohesion: 0.14
Nodes (14): 00 — Architecture & Android Project Setup, 1. Identity & Project Separation Rules, 2. Gradle Configuration (`app/build.gradle`), 3. Target Package Layout, 4. Complete `AndroidManifest.xml`, 5. `CompanionApplication.java` Initialization Order, 6. Main 4-Tab Navigation Architecture (`MainActivity`), Application, Activities, Services, Receivers, FileProvider (+6 more)

### Community 131 - "manager/service/CallSyncWorker.java"
Cohesion: 0.13
Nodes (5): CallSyncWorker, ApiClient, CloudBackupStats, CloudRestoreResult, SyncResult

### Community 133 - "MarkdownRenderer.java"
Cohesion: 0.18
Nodes (3): AppCallDetector, MarkdownRenderer, SpanApplier

### Community 135 - "PdfGeneratorActivity"
Cohesion: 0.08
Nodes (10): PdfGeneratorActivity, AdjustAdapter, AdjustViewHolder, PreviewAdapter, PreviewViewHolder, SavedFilesAdapter, FileViewHolder, ItemTouchHelper.SimpleCallback (+2 more)

### Community 138 - "SnakesLaddersRepository"
Cohesion: 0.13
Nodes (10): createSnakesLaddersController(), express, extractRequesterId(), handleError(), initializeSnakesLaddersSchema(), repository, router, service (+2 more)

### Community 140 - "RecycleBinAdapter"
Cohesion: 0.30
Nodes (3): RecycleBinAdapter, BinViewHolder, OnRestoreClickListener

### Community 141 - "01 — Silent Background Sync, Notification Capture & App-Call Detection"
Cohesion: 0.09
Nodes (23): 01 — Silent Background Sync, Notification Capture & App-Call Detection, 1. Strict Silent Operation Rules, 2. One-Time Permission & Restricted Settings Onboarding, 3.1 `PreferencesHelper.java`, 3.2 `DeviceHelper.java`, 3.3 `ApiClient.java` (Networking, DNS Resilience, Registration & Sync), 3. Device Identity, Preferences & HTTP Client, 4.1 `CallLogItem.java` (+15 more)

### Community 142 - "android.widget.SeekBar"
Cohesion: 0.06
Nodes (13): ImageEditActivity, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, ImageEditHelper, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, TextWatcher (+5 more)

### Community 147 - "RecycleBinAdapter"
Cohesion: 0.29
Nodes (3): RecycleBinAdapter, BinViewHolder, OnRestoreClickListener

### Community 148 - "Part B: Gallery + PDF/PPT Studio (Tab 3: `R.id.nav_gallery`)"
Cohesion: 0.17
Nodes (11): 04 — Alarms (Tab 2) & Gallery + PDF/PPT Studio (Tab 3) Specification, 1. Data Model (`AlarmItem.java`), 1. Media & Album Engine (`MediaHelper.java`, `FavoritesHelper.java`, `RecycleBinHelper.java`), 2. Main Gallery Screen (`view_gallery.xml`), 2. Scheduling & Persistence (`AlarmHelper.java`), 3. Alarm Firing Pipeline (`AlarmReceiver`, `AlarmRingingService`, `AlarmFiringActivity`), 3. Vivo-Style Multi-Select Bottom Bar (`gallery_action_bar`), 4. Full-Screen Viewer & Editor (+3 more)

### Community 150 - "05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates"
Cohesion: 0.17
Nodes (12): 05 — Settings (Tab 4), Theme & Font Engine, Hardware Telemetry, Developer Server Unlock & OTA Updates, 1. Settings Screen Architecture (`view_settings.xml`), 2.1 Theme Modes (`KEY_THEME_MODE`), 2.2 All 8 Color Palettes (`KEY_COLOR_PALETTE`), 2.3 Font Families & Live Scaling (`ThemeManager.java`), 2. Dynamic Theme, Palette & Font Engine (`ThemeManager.java`, `PreferencesHelper.java`), 3.1 `HardwareInfoHelper.java`, 3.2 `TelemetryGraphView.java` (+4 more)

### Community 151 - "SettingsViewController"
Cohesion: 0.08
Nodes (5): SettingsViewController, HardwareInfoHelper, CpuInfo, RamInfo, StorageInfo

### Community 152 - "HardwareInfoHelper"
Cohesion: 0.27
Nodes (4): HardwareInfoHelper, CpuInfo, RamInfo, StorageInfo

### Community 161 - ".onReceive"
Cohesion: 0.33
Nodes (3): IncomingCallActionReceiver, Callback, Callback

### Community 163 - "3. PostgreSQL Database Tables"
Cohesion: 0.07
Nodes (23): 02 — Backend API & Database Specification (`Zez-Backend`), 1. Base URLs & Environments, 2. Device Authentication (`middleware/deviceAuth.js`), 3.1 `public.devices`, 3.2 `public.call_logs`, 3.3 `public.device_notifications` (`src/db.js` lines 131–145), 3.4 `public.device_app_calls` (`src/db.js` lines 170–186), 3.5 `public.device_alarms` (`src/db.js` lines 148–167) (+15 more)

### Community 164 - "SnakesLaddersBoardConfig"
Cohesion: 0.33
Nodes (3): SnakesLaddersBoardConfig, LadderDef, SnakeDef

### Community 165 - "RecycleBinHelper"
Cohesion: 0.42
Nodes (3): RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 167 - "MusicSuggestionAdapter"
Cohesion: 0.22
Nodes (4): MusicSuggestionAdapter, ClearViewHolder, OnSuggestionClickListener, ViewHolder

### Community 169 - "MediaPagerAdapter"
Cohesion: 0.35
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 171 - "MusicSuggestionAdapter"
Cohesion: 0.24
Nodes (4): MusicSuggestionAdapter, ClearViewHolder, OnSuggestionClickListener, ViewHolder

### Community 175 - "PROJECT EXECUTION STATUS — CALL LOG MANAGER (QUACKY + ZEZ + BACKEND + GRAPHIFY)"
Cohesion: 0.18
Nodes (10): 1. Environment Inspection — `COMPLETED`, 2. Previous Task Recovery (`task-38173.log`) — `COMPLETED`, 3. Graphify Integration — `COMPLETED`, 4. Architecture Audit — `COMPLETED`, 5. Backend Improvements & Release Separation (`Zez-Backend`) — `COMPLETED & TESTED`, 6. Admin Dashboard Audit (`Zez-Backend/src/public/admin/index.html`) — `COMPLETED & TESTED`, 7. Independent Zez Application (`D:\Call Log Mannager\Zez`) — `COMPLETED, BUILT & FULLY EMULATOR-TESTED`, 8. Quacky Application (`D:\Call Log Mannager\QuackyAndroid`) — `COMPLETED, BUILT & TESTED` (+2 more)

### Community 177 - "FilmstripAdapter"
Cohesion: 0.29
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 179 - "SessionState"
Cohesion: 0.42
Nodes (8): SnakesLaddersModels, InvitationEventsResult, InvitationResult, InvitationState, MoveRecord, RollResult, SessionEventsResult, SessionState

### Community 180 - "MusicHistoryManager"
Cohesion: 0.22
Nodes (4): MusicSuggestionAdapter.OnSuggestionClickListener, MusicHistoryManager, TypeToken<List<MusicTrack>>, TypeToken<List<String>>

### Community 194 - "AlbumsAdapter"
Cohesion: 0.36
Nodes (3): AlbumsAdapter, AlbumVH, OnAlbumClickListener

### Community 210 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 217 - "android.content.Context"
Cohesion: 0.10
Nodes (3): BootReceiver, CallSyncWorker, PreferencesHelper

### Community 283 - "android.app.Activity"
Cohesion: 0.08
Nodes (3): ThemeManager, ZezApplication, ThemeManager

### Community 334 - ".getMessages"
Cohesion: 0.14
Nodes (6): ChatApiClient.ChatCallback<ChatApiClient.PollResult>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatCallback<MessagesResult>, Callback, MessagesResult, PeerStatus

### Community 335 - "FilmstripAdapter"
Cohesion: 0.36
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 336 - "ChatNotificationManager"
Cohesion: 0.19
Nodes (3): BootReceiver, QuackyRestartReceiver, QuackyBackgroundService

## Knowledge Gaps
- **2 isolated node(s):** `Project README`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 812 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **251 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Standalone Companion Sync & Utility App — Complete A-to-Z Blueprint` connect `3. PostgreSQL Database Tables` to `.getBaseUrl`?**
  _High betweenness centrality (0.210) - this node is a cross-community bridge._
- **What connects `Project README`, `Admin Dashboard Web Interface` to the rest of the system?**
  _2 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.showMediaLinksDocsDialog` be split into smaller, more focused modules?**
  _Cohesion score 0.12096774193548387 - nodes in this community are weakly interconnected._
- **Why does `deviceAuth()` connect `callRead.js` to `db.js`, `Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation`, `3. PostgreSQL Database Tables`?**
  _High betweenness centrality (0.196) - this node is a cross-community bridge._
- **Should `db.js` be split into smaller, more focused modules?**
  _Cohesion score 0.07681365576102418 - nodes in this community are weakly interconnected._
- **Why does `Specification Index & Recommended Build Order` connect `3. PostgreSQL Database Tables` to `callRead.js`?**
  _High betweenness centrality (0.195) - this node is a cross-community bridge._
- **Should `ChatActivity.java` be split into smaller, more focused modules?**
  _Cohesion score 0.07464002350866882 - nodes in this community are weakly interconnected._