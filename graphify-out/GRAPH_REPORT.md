# Graph Report - Call Log Mannager  (2026-11-26)

## Corpus Check
- 169 files · ~187,526 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 223 file(s) not represented in the graph (top: .xml 213, .properties 5, (none) 3)
- WARNING: 223 of 406 files seen (55%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 3177 nodes · 9627 edges · 119 communities (70 shown, 49 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 913 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `bc97a682`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ChatNotificationManager
- .showMediaLinksDocsDialog
- b2StorageService.js
- db.js
- e2e_device_test.py
- MusicSuggestionAdapter
- SnakesLaddersBoardView
- android.view.View
- SnakesLaddersBoardView.java
- ActiveCallManager
- android.content.Context
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
- SnakesLaddersApiClient
- admin.js
- express
- CallSyncWorker.java
- MusicPlayerService
- HistoryAdapter
- Override
- adminAuth.js
- android.view.MotionEvent
- .start
- ManageStorageActivity
- callRelayWs.js
- ChatConversation
- ThemeManager
- Project README
- MessagesResult
- Admin Dashboard Web Interface
- 4.1 5-Tab Navigation & Activities
- ZezApplication.java
- ChatPreferencesHelper
- HardwareInfoHelper
- MediaItem
- IncomingCallActivity
- QuackyStorageHelper
- LinkTouchMovementMethod.java
- android.widget.SeekBar
- YouTubeApiHelper
- okhttp3.Call
- SessionState
- ChatNotificationHelper
- Override
- FavoritesHelper
- RecycleBinAdapter
- callRead.js
- PlayerListener
- ChatMessage
- .onReceive
- server.js
- .sendVideoFrame
- benchmark
- .hangup
- SnakesAndLaddersActivity
- TicTacToeActivity
- ChatContactItem
- engine.js
- MusicPlayerManager
- CallActivity
- QuickCameraActivity
- MusicPlayerService.java
- AlbumDetailActivity
- GameRuleError
- .initViews
- chat.js
- MusicHistoryManager
- appRelease.js
- engine.test.js
- GalleryAdapter
- PdfGeneratorActivity
- SnakesLaddersRepository
- ChatActivity
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
- auto-release.md

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 141 edges
2. `ChatMessage` - 141 edges
3. `ChatActivity` - 97 edges
4. `ChatApiClient` - 72 edges
5. `MusicTrack` - 71 edges
6. `MusicActivity` - 69 edges
7. `MediaItem` - 62 edges
8. `ChatCallback` - 62 edges
9. `CallActivity` - 60 edges
10. `ChatMessageAdapter` - 55 edges

## Surprising Connections (you probably didn't know these)
- `15. Security Notes & Known Limitations` --references--> `adminAuth()`  [INFERRED]
  PROJECT_DOCUMENTATION.md → Zez-Backend/src/middleware/adminAuth.js
- `11. Backend REST API Reference` --references--> `adminAuth()`  [INFERRED]
  PROJECT_DOCUMENTATION.md → Zez-Backend/src/middleware/adminAuth.js
- `5.1 Root Cause & Fix of the 10–12 Day History Truncation Bug` --references--> `deviceAuth()`  [INFERRED]
  PROJECT_DOCUMENTATION.md → Zez-Backend/src/middleware/deviceAuth.js
- `8.1 Alarms` --references--> `AlarmFiringActivity`  [INFERRED]
  PROJECT_DOCUMENTATION.md → ZezAndroid/app/src/main/java/com/zez/app/AlarmFiringActivity.java
- `4.1 5-Tab Navigation & Activities` --references--> `AlbumDetailActivity`  [INFERRED]
  PROJECT_DOCUMENTATION.md → ZezAndroid/app/src/main/java/com/zez/app/AlbumDetailActivity.java

## Import Cycles
- None detected.

## Communities (119 total, 49 thin omitted)

### Community 0 - "ChatNotificationManager"
Cohesion: 0.20
Nodes (3): ChatNotificationManager, LiveMessageListener, OnChatsUpdateListener

### Community 1 - ".showMediaLinksDocsDialog"
Cohesion: 0.11
Nodes (13): MediaLinksDocsHelper, DocsAdapter, Holder, OnDocGoToChatListener, LinkItem, LinksAdapter, Holder, OnLinkGoToChatListener (+5 more)

### Community 2 - "b2StorageService.js"
Cohesion: 0.14
Nodes (31): express, fs, memoryUpload, multer, path, router, saveToStorageCache(), STORAGE_CACHE_DIR (+23 more)

### Community 3 - "db.js"
Cohesion: 0.07
Nodes (14): 11. Backend REST API Reference, crypto, http, crypto, hashPassword(), { Pool }, crypto, deviceAuth() (+6 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 6 - "SnakesLaddersBoardView"
Cohesion: 0.25
Nodes (3): SnakesLaddersBoardView, AnimatorListenerAdapter, PlayerState

### Community 7 - "android.view.View"
Cohesion: 0.07
Nodes (18): AlarmViewHolder, AlbumVH, CallViewHolder, DateHeaderViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder (+10 more)

### Community 10 - "android.content.Context"
Cohesion: 0.13
Nodes (10): ChatApiClient, ChatCallback, ConversationModeInfo, Callback, GameEventsResult, GameState, IncomingCallInfo, LiveUpdateResult (+2 more)

### Community 11 - "DownloadTask"
Cohesion: 0.07
Nodes (5): ActiveDownloadAdapter, OnCancelClickListener, DownloadTask, MusicDownloadManager, DownloadProgressListener

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
Cohesion: 0.05
Nodes (23): 10.1 Active Permissions in `ZezAndroid/app/src/main/AndroidManifest.xml`, 10.2 Disabled / Play Protect-Sensitive Features & Exact Restore Instructions, 10. Android Permissions & Play Protect Restore Guide, 12. PostgreSQL Schema (20 Tables in `public`), 13. Cloud Storage (B2), OTA Releases & Developer Mode, 14. Configuration, Build & Verification Commands, 15. Security Notes & Known Limitations, 16. Historical Appendix: Cellular Call Recording Investigation (`v1.0.24`–`v1.0.32`) (+15 more)

### Community 18 - "AlarmHelper"
Cohesion: 0.09
Nodes (7): AlarmAdapter, OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>, TypeToken<List<Long>>, ApiClient.AlarmSyncCallback

### Community 19 - "AppCallItem"
Cohesion: 0.09
Nodes (5): AppCallAdapter, OnCallActionListener, com.zez.app.adapter.AppCallAdapter.OnCallActionListener, AppCallItem, Callback

### Community 20 - "CallMediaEngine"
Cohesion: 0.11
Nodes (3): CallMediaEngine, WebSocketListener, MediaListener

### Community 21 - "MusicActivity"
Cohesion: 0.08
Nodes (7): MusicActivity, YouTubeApiHelper.Callback<List<String>>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, RecyclerView.OnScrollListener, TextWatcher, SeekBar.OnSeekBarChangeListener

### Community 22 - "CallLogItem"
Cohesion: 0.10
Nodes (6): CallLogItem, ApiClient, AlarmSyncCallback, ApiCallback, CallLogHelper, DeviceHelper

### Community 24 - "AppUpdateHelper"
Cohesion: 0.16
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "SnakesLaddersApiClient"
Cohesion: 0.17
Nodes (5): SnakesLaddersApiClient, ApiCallback, Callback, Callback, Parser

### Community 26 - "admin.js"
Cohesion: 0.10
Nodes (13): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+5 more)

### Community 27 - "express"
Cohesion: 0.22
Nodes (7): express, http, https, router, streamCache, ytdl, { Ytdl, Client }

### Community 28 - "CallSyncWorker.java"
Cohesion: 0.14
Nodes (3): IncomingCallActionReceiver, QuackyRestartReceiver, CallSyncWorker

### Community 29 - "MusicPlayerService"
Cohesion: 0.09
Nodes (4): MusicPlayerService, CustomTarget<Bitmap>, MediaSessionCompat.Callback, YouTubeApiHelper.Callback<String>

### Community 30 - "HistoryAdapter"
Cohesion: 0.23
Nodes (5): HistoryAdapter, VH, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.HistoryResponse>, HistoryMatch, HistoryResponse

### Community 31 - "Override"
Cohesion: 0.06
Nodes (16): ChatApiClient.ChatCallback<Void>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationState>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, android.text.TextWatcher, ProfileMediaAdapter, ViewHolder, ChatMessageAdapter.OnMessageActionListener (+8 more)

### Community 32 - "adminAuth.js"
Cohesion: 0.12
Nodes (18): http, run(), run(), http, request(), run(), token, adminAuth() (+10 more)

### Community 36 - "ManageStorageActivity"
Cohesion: 0.14
Nodes (3): ManageStorageAdapter, OnSelectionListener, StorageMediaItem

### Community 37 - "callRelayWs.js"
Cohesion: 0.12
Nodes (18): callAudioCache, callMediaCache, express, router, acceptUpgrade(), attachRealtimeWebSockets(), callRooms, createWsClient() (+10 more)

### Community 39 - "ChatConversation"
Cohesion: 0.08
Nodes (7): ConversationsAdapter, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, androidx.recyclerview.widget.DiffUtil.Callback, ChatConversation, Callback

### Community 43 - "MessagesResult"
Cohesion: 0.33
Nodes (3): ChatCallback<MessagesResult>, MessagesResult, PeerStatus

### Community 51 - "4.1 5-Tab Navigation & Activities"
Cohesion: 0.28
Nodes (6): 2. Feature Status Matrix, 4.1 5-Tab Navigation & Activities, 4.2 Theming, Typography, Hardware Telemetry & Evolutions (Nov 2026 Wishlist), 4. Android Application Architecture, AlarmFiringActivity, CallStateReceiver

### Community 52 - "ZezApplication.java"
Cohesion: 0.16
Nodes (3): CallLogObserver, CallLogListener, ZezApplication

### Community 54 - "HardwareInfoHelper"
Cohesion: 0.27
Nodes (4): HardwareInfoHelper, CpuInfo, RamInfo, StorageInfo

### Community 55 - "MediaItem"
Cohesion: 0.15
Nodes (3): ImageViewerActivity, ViewPager2.OnPageChangeCallback, MediaItem

### Community 56 - "IncomingCallActivity"
Cohesion: 0.19
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 60 - "YouTubeApiHelper"
Cohesion: 0.08
Nodes (8): YouTubeApiHelper.Callback<YouTubeApiHelper.SearchResult>, YouTubeApiHelper, CachedStream, Callback, SearchResult, Callback<SearchResult>, CookieJar, Dns

### Community 61 - "okhttp3.Call"
Cohesion: 0.09
Nodes (16): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+8 more)

### Community 62 - "SessionState"
Cohesion: 0.42
Nodes (8): SnakesLaddersModels, InvitationEventsResult, InvitationResult, InvitationState, MoveRecord, RollResult, SessionEventsResult, SessionState

### Community 66 - "RecycleBinAdapter"
Cohesion: 0.19
Nodes (6): RecycleBinAdapter, BinViewHolder, OnRestoreClickListener, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 67 - "callRead.js"
Cohesion: 0.11
Nodes (22): 5.2 At-Rest Payload Encryption (`Zez-Backend/src/crypto.js`), crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey() (+14 more)

### Community 69 - "ChatMessage"
Cohesion: 0.07
Nodes (6): ChatRowItem, ChatApiClient.ChatCallback<ChatMessage>, PendingMediaTask, PendingMessage, ChatMessage, MediaGridAdapter.OnMediaItemClickListener

### Community 71 - "server.js"
Cohesion: 0.10
Nodes (20): closeDatabase(), initializeDatabase(), router, router, app, cors, express, fs (+12 more)

### Community 77 - "SnakesAndLaddersActivity"
Cohesion: 0.11
Nodes (5): SnakesAndLaddersActivity, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.SessionState>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.RollResult>, SnakesLaddersApiClient.ApiCallback<SnakesLaddersModels.InvitationState>

### Community 82 - "TicTacToeActivity"
Cohesion: 0.13
Nodes (6): TicTacToeActivity, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>, ChatApiClient.ChatCallback<ChatApiClient.GameState>

### Community 84 - "ChatContactItem"
Cohesion: 0.07
Nodes (4): DisplayItem, OnContactClickListener, TextWatcher, ChatContactItem

### Community 85 - "engine.js"
Cohesion: 0.19
Nodes (12): BOARD_CONFIGS, DEFAULT_BOARD_CONFIG, getBoardConfig(), applyDiceRoll(), buildLookupMaps(), crypto, crypto, CONNECTION_STATUS (+4 more)

### Community 97 - "QuickCameraActivity"
Cohesion: 0.13
Nodes (3): QuickCameraActivity, TextureView.SurfaceTextureListener, CameraCaptureSession.StateCallback

### Community 117 - "AlbumDetailActivity"
Cohesion: 0.09
Nodes (6): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener, GalleryListItem

### Community 119 - "GameRuleError"
Cohesion: 0.33
Nodes (4): GameRuleError, SnakesLaddersService, runServiceAndMultiplayerTests(), isValidUuid()

### Community 129 - "chat.js"
Cohesion: 0.07
Nodes (19): content, fs, lines, navMatch, fs, os, path, avatarStorage (+11 more)

### Community 130 - "MusicHistoryManager"
Cohesion: 0.22
Nodes (4): MusicSuggestionAdapter.OnSuggestionClickListener, MusicHistoryManager, TypeToken<List<MusicTrack>>, TypeToken<List<String>>

### Community 131 - "appRelease.js"
Cohesion: 0.14
Nodes (12): fs, parseNotesFromArgs(), path, publish(), downloadsDir, express, fs, multer (+4 more)

### Community 132 - "engine.test.js"
Cohesion: 0.28
Nodes (12): coordinatesToSquare(), squareCenterNormalized(), squareToCoordinates(), crypto, rollDice(), createInitialSessionState(), resignSession(), assert (+4 more)

### Community 134 - "GalleryAdapter"
Cohesion: 0.12
Nodes (7): GalleryPopupHelper, GalleryAdapter, HeaderVH, GallerySelectionCallback, GridRow, OnTileClickListener, GridLayoutManager.SpanSizeLookup

### Community 135 - "PdfGeneratorActivity"
Cohesion: 0.08
Nodes (9): PdfGeneratorActivity, AdjustAdapter, AdjustViewHolder, PreviewAdapter, PreviewViewHolder, SavedFilesAdapter, ItemTouchHelper.SimpleCallback, PdfDocumentHelper (+1 more)

### Community 139 - "ChatActivity"
Cohesion: 0.05
Nodes (10): ChatActivity, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, ChatApiClient.ChatCallback<ChatApiClient.PollResult>, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationResult>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, GalleryPopupHelper.GallerySelectionCallback, SimpleShareContact, com.zez.app.games.snakesladders.SnakesLaddersApiClient.ApiCallback<com.zez.app.games.snakesladders.SnakesLaddersModels.InvitationState> (+2 more)

### Community 156 - "MediaPagerAdapter"
Cohesion: 0.30
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 166 - "MusicTrack"
Cohesion: 0.07
Nodes (6): MusicAdapter, OnTrackClickListener, MusicDownloadHelper, DownloadCallback, TypeToken<List<MusicTrack>>, MusicTrack

### Community 175 - "ZoomImageView"
Cohesion: 0.17
Nodes (3): ZoomImageView, OnTapListener, ScaleListener

### Community 180 - "AlbumDetailActivity.java"
Cohesion: 0.13
Nodes (4): AlbumsAdapter, OnAlbumClickListener, AlbumItem, MediaHelper

### Community 181 - "ImageEditActivity"
Cohesion: 0.22
Nodes (6): 8.1 Alarms, 8.2 Gallery & Recycle Bin (+ `v1.0.33` Gallery 2.0 Rollback Record), 8.3 YMusic & Mini-Games, 8. Alarms, Gallery & YMusic Subsystems, ImageEditActivity, ImageEditHelper

### Community 189 - "Override"
Cohesion: 0.04
Nodes (15): okhttp3.Callback, ChatApiClient.ChatCallback<Boolean>, android.text.TextWatcher, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>> (+7 more)

### Community 190 - "ChatMessageAdapter"
Cohesion: 0.09
Nodes (5): ChatMessageAdapter, OnGameDeclineClickListener, OnGameInviteClickListener, OnMediaRetryListener, OnMessageActionListener

### Community 195 - "service.test.js"
Cohesion: 0.16
Nodes (6): EventEmitter, SnakesLaddersEvents, assert, createInMemoryRepository(), crypto, GAME_EVENTS

### Community 197 - "index.js"
Cohesion: 0.25
Nodes (9): createSnakesLaddersController(), express, extractRequesterId(), handleError(), initializeSnakesLaddersSchema(), repository, router, service (+1 more)

### Community 198 - "ChatActivity.java"
Cohesion: 0.10
Nodes (5): IncomingViewHolder, OutgoingViewHolder, ContactsActivity, SnakesLaddersHistoryActivity, ManageStorageActivity

### Community 200 - "Override"
Cohesion: 0.11
Nodes (7): Callback, Callback, TextureView.SurfaceTextureListener, ActiveCallManager.CallStateListener, CameraCaptureSession.StateCallback, Runnable, Callback

### Community 207 - "SnakesLaddersBoardConfig"
Cohesion: 0.33
Nodes (3): SnakesLaddersBoardConfig, LadderDef, SnakeDef

### Community 210 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

## Knowledge Gaps
- **2 isolated node(s):** `Project README`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 455 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **49 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `deviceAuth()` connect `db.js` to `Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation`, `callRead.js`?**
  _High betweenness centrality (0.168) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ChatActivity` (e.g. with `4.1 5-Tab Navigation & Activities` and `6. Chat Subsystem (Normal & Secret Modes)`) actually correct?**
  _`ChatActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Project README`, `Admin Dashboard Web Interface` to the rest of the system?**
  _2 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.showMediaLinksDocsDialog` be split into smaller, more focused modules?**
  _Cohesion score 0.11095305832147938 - nodes in this community are weakly interconnected._
- **Why does `5.1 Root Cause & Fix of the 10–12 Day History Truncation Bug` connect `Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation` to `db.js`, `CallLogItem`?**
  _High betweenness centrality (0.147) - this node is a cross-community bridge._
- **Should `b2StorageService.js` be split into smaller, more focused modules?**
  _Cohesion score 0.1361344537815126 - nodes in this community are weakly interconnected._
- **Why does `Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation` connect `Quacky (`ZezAndroid` + `Zez-Backend`) — Consolidated Technical Documentation` to `4.1 5-Tab Navigation & Activities`, `db.js`, `ImageEditActivity`?**
  _High betweenness centrality (0.097) - this node is a cross-community bridge._