# Graph Report - Call Log Mannager  (2026-11-26)

## Corpus Check
- 165 files · ~184,264 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 217 file(s) not represented in the graph (top: .xml 207, .properties 5, (none) 3)
- WARNING: 217 of 396 files seen (55%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 3105 nodes · 9387 edges · 118 communities (70 shown, 48 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 911 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `77c887c9`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- .getUserId
- .showMediaLinksDocsDialog
- b2StorageService.js
- db.js
- e2e_device_test.py
- MusicSuggestionAdapter
- SnakesLaddersBoardView
- android.view.View
- SnakesLaddersBoardView.java
- ActiveCallManager
- ChatApiClient
- DownloadTask
- ChatLocalDbHelper
- compilerOptions
- ChatUser
- package.json
- index.d.ts
- Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation
- AlarmHelper
- AppCallItem
- CallMediaEngine
- MusicActivity
- CallLogItem
- MainActivity
- AppUpdateHelper
- SnakesLaddersDiceView.java
- admin.js
- express
- CallSyncWorker.java
- MusicPlayerService
- ApiCallback
- Override
- adminAuth.js
- .initViews
- CallActivity.java
- ChatNotificationHelper
- ManageStorageActivity
- callRelayWs.js
- ChatConversation
- ThemeManager
- Project README
- PollResult
- Admin Dashboard Web Interface
- 4.1 5-Tab Navigation & Activities
- ZezApplication.java
- ChatPreferencesHelper
- HardwareInfoHelper
- MediaItem
- IncomingCallActivity
- QuackyStorageHelper
- AlarmRingingService
- GameState
- YouTubeApiHelper
- okhttp3.Call
- Call
- QuackyBackgroundService
- FavoritesHelper
- RecycleBinAdapter
- callRead.js
- .initThemeSpinner
- ChatMessage
- RecycleBinHelper
- server.js
- benchmark
- SnakesAndLaddersActivity
- TicTacToeActivity
- ChatContactItem
- engine.js
- MusicPlayerManager
- .getInstance
- AlarmItem
- QuickCameraActivity
- AlbumDetailActivity
- GameRuleError
- ChatActivity
- .getUri
- chat.js
- MusicHistoryManager
- appRelease.js
- engine.test.js
- GalleryAdapter
- SnakesLaddersRepository
- .applyModeState
- Override
- MediaPagerAdapter
- MusicTrack
- ZoomImageView
- PhoneHelper
- AlbumDetailActivity.java
- ImageEditActivity
- Override
- ChatMessageAdapter
- FilmstripAdapter
- service.test.js
- index.js
- ChatActivity.java
- Override
- .onResponse
- Override
- android.content.Context
- SnakesLaddersBoardConfig
- SnakesLaddersCoordinates
- extreme-test-suite.js
- android.hardware.camera2.CameraCaptureSession
- auto-release.md

## God Nodes (most connected - your core abstractions)
1. `ChatMessage` - 141 edges
2. `MainActivity` - 139 edges
3. `ChatActivity` - 97 edges
4. `ChatApiClient` - 72 edges
5. `MusicTrack` - 71 edges
6. `MusicActivity` - 69 edges
7. `MediaItem` - 62 edges
8. `ChatCallback` - 62 edges
9. `CallActivity` - 60 edges
10. `ChatMessageAdapter` - 54 edges

## Surprising Connections (you probably didn't know these)
- `15. Security Notes & Known Limitations` --references--> `adminAuth()`  [INFERRED]
  PROJECT_DOCUMENTATION.md → Zez-Backend/src/middleware/adminAuth.js
- `11. Backend REST API Reference` --references--> `adminAuth()`  [INFERRED]
  PROJECT_DOCUMENTATION.md → Zez-Backend/src/middleware/adminAuth.js
- `4.1 5-Tab Navigation & Activities` --references--> `AlbumDetailActivity`  [INFERRED]
  PROJECT_DOCUMENTATION.md → ZezAndroid/app/src/main/java/com/zez/app/AlbumDetailActivity.java
- `2. Feature Status Matrix` --references--> `CallActivity`  [INFERRED]
  PROJECT_DOCUMENTATION.md → ZezAndroid/app/src/main/java/com/zez/app/CallActivity.java
- `4.1 5-Tab Navigation & Activities` --references--> `CallActivity`  [INFERRED]
  PROJECT_DOCUMENTATION.md → ZezAndroid/app/src/main/java/com/zez/app/CallActivity.java

## Import Cycles
- None detected.

## Communities (118 total, 48 thin omitted)

### Community 0 - ".getUserId"
Cohesion: 0.08
Nodes (8): IncomingCallActionReceiver, Callback, Callback, ChatNotificationManager, okhttp3.WebSocketListener, LiveMessageListener, OnChatsUpdateListener, ChatApiClient.ChatCallback<List<ChatConversation>>

### Community 1 - ".showMediaLinksDocsDialog"
Cohesion: 0.10
Nodes (14): MediaLinksDocsHelper, DocsAdapter, Holder, OnDocGoToChatListener, LinkItem, LinksAdapter, Holder, OnLinkGoToChatListener (+6 more)

### Community 2 - "b2StorageService.js"
Cohesion: 0.14
Nodes (31): express, fs, memoryUpload, multer, path, router, saveToStorageCache(), STORAGE_CACHE_DIR (+23 more)

### Community 3 - "db.js"
Cohesion: 0.09
Nodes (8): crypto, crypto, hashPassword(), { Pool }, verifyPassword(), http, makeRequest(), runChatTestSuite()

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "MusicSuggestionAdapter"
Cohesion: 0.23
Nodes (4): MusicSuggestionAdapter, ClearViewHolder, OnSuggestionClickListener, ViewHolder

### Community 6 - "SnakesLaddersBoardView"
Cohesion: 0.20
Nodes (3): SnakesLaddersBoardView, AnimatorListenerAdapter, PlayerState

### Community 7 - "android.view.View"
Cohesion: 0.11
Nodes (13): AlarmViewHolder, AlbumVH, CallViewHolder, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactViewHolder, ViewHolder (+5 more)

### Community 9 - "ActiveCallManager"
Cohesion: 0.11
Nodes (3): ActiveCallManager, CallStateListener, MediaListener

### Community 10 - "ChatApiClient"
Cohesion: 0.18
Nodes (5): ChatApiClient, ChatCallback, ContactSyncResult, Callback, MediaUploadResult

### Community 11 - "DownloadTask"
Cohesion: 0.07
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, MusicDownloadManager, DownloadProgressListener

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 15 - "package.json"
Cohesion: 0.07
Nodes (38): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, @noble/ciphers, pg, @types/cors, @types/express (+30 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation"
Cohesion: 0.05
Nodes (23): 10.1 Active Permissions in `ZezAndroid/app/src/main/AndroidManifest.xml`, 10.2 Disabled / Play Protect-Sensitive Features & Exact Restore Instructions, 10. Android Permissions & Play Protect Restore Guide, 12. PostgreSQL Schema (20 Tables in `public`), 13. Cloud Storage (B2), OTA Releases & Developer Mode, 14. Configuration, Build & Verification Commands, 15. Security Notes & Known Limitations, 16. Historical Appendix: Cellular Call Recording Investigation (`v1.0.24`–`v1.0.32`) (+15 more)

### Community 18 - "AlarmHelper"
Cohesion: 0.17
Nodes (6): AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>>, ApiClient.AlarmSyncCallback, AlarmSyncCallback

### Community 19 - "AppCallItem"
Cohesion: 0.07
Nodes (5): AppCallAdapter, OnCallActionListener, com.zez.app.adapter.AppCallAdapter.OnCallActionListener, AppCallItem, Callback

### Community 20 - "CallMediaEngine"
Cohesion: 0.10
Nodes (4): CallMediaEngine, WebSocketListener, Callback, Callback

### Community 21 - "MusicActivity"
Cohesion: 0.09
Nodes (6): MusicActivity, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher

### Community 24 - "AppUpdateHelper"
Cohesion: 0.16
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 26 - "admin.js"
Cohesion: 0.10
Nodes (13): AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs, os (+5 more)

### Community 27 - "express"
Cohesion: 0.18
Nodes (9): @distube/ytdl-core, @distube/ytdl-core@^5.2.1, express, http, https, router, streamCache, ytdl (+1 more)

### Community 29 - "MusicPlayerService"
Cohesion: 0.09
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 31 - "Override"
Cohesion: 0.05
Nodes (18): com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<ChatMessage>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationState>, ChatApiClient.ChatCallback<Void>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, android.text.TextWatcher, ProfileMediaAdapter (+10 more)

### Community 32 - "adminAuth.js"
Cohesion: 0.12
Nodes (19): http, run(), http, run(), http, request(), run(), token (+11 more)

### Community 36 - "ManageStorageActivity"
Cohesion: 0.12
Nodes (4): ManageStorageAdapter, OnSelectionListener, ManageStorageActivity, StorageMediaItem

### Community 37 - "callRelayWs.js"
Cohesion: 0.12
Nodes (18): callAudioCache, callMediaCache, express, router, acceptUpgrade(), attachRealtimeWebSockets(), callRooms, createWsClient() (+10 more)

### Community 39 - "ChatConversation"
Cohesion: 0.08
Nodes (7): ConversationsAdapter, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, androidx.recyclerview.widget.DiffUtil.Callback, ChatConversation, Callback

### Community 43 - "PollResult"
Cohesion: 0.19
Nodes (7): ChatApiClient.ChatCallback<ChatApiClient.PollResult>, ConversationModeInfo, IncomingCallInfo, LiveUpdateResult, PeerStatus, PollResult, Callback

### Community 51 - "4.1 5-Tab Navigation & Activities"
Cohesion: 0.19
Nodes (8): 2. Feature Status Matrix, 4.1 5-Tab Navigation & Activities, 8.1 Alarms, AlarmFiringActivity, AlarmReceiver, BootReceiver, CallStateReceiver, QuackyRestartReceiver

### Community 52 - "ZezApplication.java"
Cohesion: 0.19
Nodes (3): CallLogObserver, CallLogListener, ZezApplication

### Community 54 - "HardwareInfoHelper"
Cohesion: 0.22
Nodes (6): 4.2 Theming, Typography, Hardware Telemetry & Evolutions (Nov 2026 Wishlist), 4. Android Application Architecture, HardwareInfoHelper, CpuInfo, RamInfo, StorageInfo

### Community 55 - "MediaItem"
Cohesion: 0.15
Nodes (3): ImageViewerActivity, ViewPager2.OnPageChangeCallback, MediaItem

### Community 56 - "IncomingCallActivity"
Cohesion: 0.23
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 60 - "YouTubeApiHelper"
Cohesion: 0.08
Nodes (8): YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper, CachedStream, Callback, SearchResult, Callback<SearchResult>, CookieJar, Dns

### Community 61 - "okhttp3.Call"
Cohesion: 0.09
Nodes (15): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+7 more)

### Community 66 - "RecycleBinAdapter"
Cohesion: 0.31
Nodes (3): RecycleBinAdapter, BinViewHolder, OnRestoreClickListener

### Community 67 - "callRead.js"
Cohesion: 0.08
Nodes (32): 11. Backend REST API Reference, 5.1 Root Cause & Fix of the 10–12 Day History Truncation Bug, 5.2 At-Rest Payload Encryption (`Zez-Backend/src/crypto.js`), 5. Call Log Synchronization & Cryptography, express, express@^5.1.0, crypto, decryptJson() (+24 more)

### Community 69 - "ChatMessage"
Cohesion: 0.10
Nodes (3): PendingMediaTask, PendingMessage, ChatMessage

### Community 70 - "RecycleBinHelper"
Cohesion: 0.30
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 71 - "server.js"
Cohesion: 0.09
Nodes (22): fs, parseNotesFromArgs(), path, publish(), closeDatabase(), initializeDatabase(), app, cors (+14 more)

### Community 77 - "SnakesAndLaddersActivity"
Cohesion: 0.05
Nodes (23): SnakesAndLaddersActivity, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.RollResult>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.InvitationState>, SnakesLaddersApiClient, ApiCallback, Callback (+15 more)

### Community 82 - "TicTacToeActivity"
Cohesion: 0.13
Nodes (6): TicTacToeActivity, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>

### Community 84 - "ChatContactItem"
Cohesion: 0.08
Nodes (7): ContactsAdapter, DisplayItem, HeaderViewHolder, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 85 - "engine.js"
Cohesion: 0.19
Nodes (12): BOARD_CONFIGS, DEFAULT_BOARD_CONFIG, getBoardConfig(), applyDiceRoll(), buildLookupMaps(), crypto, crypto, CONNECTION_STATUS (+4 more)

### Community 90 - ".getInstance"
Cohesion: 0.10
Nodes (4): Callback, Callback, Runnable, Callback

### Community 117 - "AlbumDetailActivity"
Cohesion: 0.10
Nodes (5): GalleryAdapter, GridLayoutManager.SpanSizeLookup, AlbumDetailActivity, GalleryAdapter.Listener, GalleryListItem

### Community 119 - "GameRuleError"
Cohesion: 0.33
Nodes (4): GameRuleError, SnakesLaddersService, runServiceAndMultiplayerTests(), isValidUuid()

### Community 122 - "ChatActivity"
Cohesion: 0.07
Nodes (4): ChatActivity, android.text.TextWatcher, GalleryPopupHelper.GallerySelectionCallback, Callback

### Community 129 - "chat.js"
Cohesion: 0.06
Nodes (20): content, fs, lines, navMatch, fs, os, path, avatarStorage (+12 more)

### Community 130 - "MusicHistoryManager"
Cohesion: 0.22
Nodes (4): MusicSuggestionAdapter.OnSuggestionClickListener, MusicHistoryManager, TypeToken<List<MusicTrack>>, TypeToken<List<String>>

### Community 131 - "appRelease.js"
Cohesion: 0.18
Nodes (10): multer, multer@^2.5.0, downloadsDir, express, fs, multer, path, router (+2 more)

### Community 132 - "engine.test.js"
Cohesion: 0.28
Nodes (12): coordinatesToSquare(), squareCenterNormalized(), squareToCoordinates(), crypto, rollDice(), createInitialSessionState(), resignSession(), assert (+4 more)

### Community 134 - "GalleryAdapter"
Cohesion: 0.13
Nodes (7): GalleryPopupHelper, GalleryAdapter, HeaderVH, GallerySelectionCallback, GridRow, OnTileClickListener, GridLayoutManager.SpanSizeLookup

### Community 139 - ".applyModeState"
Cohesion: 0.16
Nodes (5): ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatCallback<MessagesResult>, MessagesResult

### Community 149 - "Override"
Cohesion: 0.21
Nodes (3): CallStateListener, Callback, CallMediaEngine.MediaListener

### Community 156 - "MediaPagerAdapter"
Cohesion: 0.30
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 166 - "MusicTrack"
Cohesion: 0.07
Nodes (8): MusicAdapter, OnTrackClickListener, TrackViewHolder, MusicDownloadHelper, DownloadCallback, TypeToken<List<MusicTrack>>, MusicTrack, PlayerListener

### Community 175 - "ZoomImageView"
Cohesion: 0.14
Nodes (4): ZoomImageView, GestureListener, OnTapListener, ScaleListener

### Community 180 - "AlbumDetailActivity.java"
Cohesion: 0.20
Nodes (3): AlbumsAdapter, OnAlbumClickListener, AlbumItem

### Community 181 - "ImageEditActivity"
Cohesion: 0.12
Nodes (5): 8.2 Gallery & Recycle Bin (+ `v1.0.33` Gallery 2.0 Rollback Record), ImageEditActivity, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 189 - "Override"
Cohesion: 0.05
Nodes (12): ChatApiClient.ChatCallback<Boolean>, android.text.TextWatcher, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>>, ChatApiClient.ChatCallback<List<ChatConversation>>, android.text.style.MetricAffectingSpan (+4 more)

### Community 190 - "ChatMessageAdapter"
Cohesion: 0.08
Nodes (7): ChatMessageAdapter, ChatRowItem, OnGameDeclineClickListener, OnGameInviteClickListener, OnMediaRetryListener, OnMessageActionListener, ChatMessageAdapter.OnMessageActionListener

### Community 192 - "FilmstripAdapter"
Cohesion: 0.36
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 195 - "service.test.js"
Cohesion: 0.16
Nodes (6): EventEmitter, SnakesLaddersEvents, assert, createInMemoryRepository(), crypto, GAME_EVENTS

### Community 197 - "index.js"
Cohesion: 0.25
Nodes (9): createSnakesLaddersController(), express, extractRequesterId(), handleError(), initializeSnakesLaddersSchema(), repository, router, service (+1 more)

### Community 204 - "Override"
Cohesion: 0.19
Nodes (3): ActiveCallManager.CallStateListener, CameraDevice.StateCallback, CameraDevice.StateCallback

### Community 206 - "android.content.Context"
Cohesion: 0.09
Nodes (3): ApiClient, DeviceHelper, PreferencesHelper

### Community 207 - "SnakesLaddersBoardConfig"
Cohesion: 0.33
Nodes (3): SnakesLaddersBoardConfig, LadderDef, SnakeDef

### Community 210 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

## Knowledge Gaps
- **2 isolated node(s):** `Project README`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 449 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **48 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `5.1 Root Cause & Fix of the 10–12 Day History Truncation Bug` connect `callRead.js` to `android.content.Context`?**
  _High betweenness centrality (0.156) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ChatActivity` (e.g. with `4.1 5-Tab Navigation & Activities` and `6. Chat Subsystem (Normal & Secret Modes)`) actually correct?**
  _`ChatActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Project README`, `Admin Dashboard Web Interface` to the rest of the system?**
  _2 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.getUserId` be split into smaller, more focused modules?**
  _Cohesion score 0.08305647840531562 - nodes in this community are weakly interconnected._
- **Why does `MainActivity` connect `MainActivity` to `.getUserId`, `GalleryPopupHelper.java`, `android.view.View`, `SnakesLaddersBoardView.java`, `ChatApiClient`, `ChatLocalDbHelper`, `AlarmHelper`, `AppCallItem`, `CallLogItem`, `AppUpdateHelper`, `.initViews`, `MusicTrack`, `ChatConversation`, `4.1 5-Tab Navigation & Activities`, `AlbumDetailActivity.java`, `ChatPreferencesHelper`, `MediaItem`, `Override`, `Call`, `RecycleBinAdapter`, `.initThemeSpinner`, `ChatActivity.java`, `android.content.Context`, `AlarmItem`, `MusicPlayerService.java`, `AlbumDetailActivity`, `.getUri`?**
  _High betweenness centrality (0.106) - this node is a cross-community bridge._
- **Should `.showMediaLinksDocsDialog` be split into smaller, more focused modules?**
  _Cohesion score 0.09856035437430787 - nodes in this community are weakly interconnected._
- **Should `b2StorageService.js` be split into smaller, more focused modules?**
  _Cohesion score 0.1361344537815126 - nodes in this community are weakly interconnected._