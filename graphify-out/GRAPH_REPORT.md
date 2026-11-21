# Graph Report - Call Log Mannager  (2026-11-21)

## Corpus Check
- 122 files · ~106,374 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 155 file(s) not represented in the graph (top: .xml 146, .properties 5, (none) 2)
- WARNING: 155 of 290 files seen (53%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1792 nodes · 4798 edges · 106 communities (42 shown, 64 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 469 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `69d6fc37`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- CallActivity
- AlarmItem
- b2StorageService.js
- AlbumDetailActivity
- e2e_device_test.py
- ChatContactItem
- Hardware Info & Performance Telemetry (C6)
- okhttp3.Call
- ViewHolder
- AudioPlaybackManager
- .drainPendingQueue
- appRelease.js
- android.content.Context
- compilerOptions
- .loadGalleryData
- package.json
- index.d.ts
- ChatMessageAdapter
- ChatUser
- CallSyncWorker.java
- d_call_log_mannager_zez_backend_src_db_pool
- ImageViewerActivity.java
- server.js
- MainActivity
- AppUpdateHelper
- ImageEditActivity.java
- admin.js
- Hardware Info & Performance Telemetry (C27)
- CallSyncWorker
- android.view.View
- AlarmRingingService.java
- Override
- test-roster.js
- android.net.Uri
- Override
- chat.js
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- ApiCallback
- MediaPagerAdapter
- RecycleBinAdapter
- ImageViewerActivity
- CallLogItem
- extreme-test-suite.js
- Override
- ChatApiClient
- GalleryAdapter
- AlarmRingingService.java
- .onResponse
- ConversationsAdapter
- callRead.js
- IncomingCallActivity
- .getPrefs
- MediaItem
- CallRecordManager
- ChatLocalDbHelper
- benchmark
- ChatActivity
- .loadSettingsData
- .getMessages
- Adapter
- CallRecordItem
- com.zez.app.model.ChatContactItem
- { Pool }
- Callback
- .onReceive
- TextWatcher
- .handleReaction
- .checkIncomingCalls
- .syncContacts

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 108 edges
2. `ChatMessage` - 87 edges
3. `ChatActivity` - 71 edges
4. `ChatMessageAdapter` - 47 edges
5. `CallRecordItem` - 38 edges
6. `ChatApiClient` - 38 edges
7. `CallActivity` - 37 edges
8. `ViewHolder` - 34 edges
9. `ChatCallback` - 34 edges
10. `ImageViewerActivity` - 28 edges

## Surprising Connections (you probably didn't know these)
- `ChatActivity` --references--> `ChatMessageAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ChatMessageAdapter.java
- `ChatActivity` --references--> `ChatMessage`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/ChatMessage.java
- `ChatActivity` --references--> `ChatLocalDbHelper`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/ChatLocalDbHelper.java
- `ProfileMediaAdapter` --references--> `ChatMessage`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/ChatMessage.java
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java

## Import Cycles
- None detected.

## Communities (106 total, 64 thin omitted)

### Community 1 - "AlarmItem"
Cohesion: 0.11
Nodes (7): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.08
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - "okhttp3.Call"
Cohesion: 0.11
Nodes (12): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+4 more)

### Community 8 - "ViewHolder"
Cohesion: 0.13
Nodes (8): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder, ContactsActivity

### Community 11 - "appRelease.js"
Cohesion: 0.12
Nodes (14): downloadsDir, express, fs, multer, path, router, storage, upload (+6 more)

### Community 12 - "android.content.Context"
Cohesion: 0.14
Nodes (4): ApiClient, CallLogHelper, DeviceHelper, PreferencesHelper

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - ".loadGalleryData"
Cohesion: 0.19
Nodes (5): FavoritesHelper, MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 15 - "package.json"
Cohesion: 0.07
Nodes (38): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, @noble/ciphers, pg, @types/cors, @types/express (+30 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "ChatMessageAdapter"
Cohesion: 0.11
Nodes (4): ChatMessageAdapter, ChatRowItem, OnMediaRetryListener, OnMessageActionListener

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.05
Nodes (27): fs, path, fs, path, fs, path, fs, path (+19 more)

### Community 22 - "server.js"
Cohesion: 0.11
Nodes (17): closeDatabase(), initializeDatabase(), callMediaCache, express, router, router, app, cors (+9 more)

### Community 23 - "MainActivity"
Cohesion: 0.06
Nodes (4): CallRecordAdapter, OnRecordDeletedListener, MainActivity, Runnable

### Community 24 - "AppUpdateHelper"
Cohesion: 0.17
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "ImageEditActivity.java"
Cohesion: 0.09
Nodes (5): SeekBar.OnSeekBarChangeListener, ImageEditActivity, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.10
Nodes (13): AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs, os (+5 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 29 - "android.view.View"
Cohesion: 0.17
Nodes (4): AlbumVH, RecordViewHolder, HeaderVH, MediaVH

### Community 31 - "Override"
Cohesion: 0.09
Nodes (8): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatMessage>, ProfileMediaAdapter, ViewHolder

### Community 32 - "test-roster.js"
Cohesion: 0.11
Nodes (19): http, run(), http, run(), http, request(), run(), token (+11 more)

### Community 33 - "android.net.Uri"
Cohesion: 0.20
Nodes (3): AlbumsAdapter, OnAlbumClickListener, AlbumItem

### Community 35 - "Override"
Cohesion: 0.08
Nodes (8): android.text.TextWatcher, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, android.text.TextWatcher, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, ChatApiClient.ChatCallback<String>

### Community 36 - "chat.js"
Cohesion: 0.09
Nodes (20): multer, multer@^2.5.0, avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir (+12 more)

### Community 38 - "ChatMessage"
Cohesion: 0.11
Nodes (4): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, PendingMediaTask, PendingMessage, ChatMessage

### Community 52 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 53 - "RecycleBinAdapter"
Cohesion: 0.31
Nodes (3): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter

### Community 58 - "CallLogItem"
Cohesion: 0.16
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 59 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 60 - "Override"
Cohesion: 0.21
Nodes (3): TextureView.SurfaceTextureListener, CameraDevice.StateCallback, CameraCaptureSession.StateCallback

### Community 61 - "ChatApiClient"
Cohesion: 0.15
Nodes (9): ChatApiClient, Callback, Callback, ChatCallback, ConversationModeInfo, IncomingCallInfo, MediaUploadResult, PeerStatus (+1 more)

### Community 63 - "GalleryAdapter"
Cohesion: 0.14
Nodes (3): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener

### Community 66 - "ConversationsAdapter"
Cohesion: 0.08
Nodes (6): ConversationsAdapter, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, ChatConversation, PhoneHelper

### Community 67 - "callRead.js"
Cohesion: 0.08
Nodes (28): express, express@^5.1.0, crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm } (+20 more)

### Community 68 - "IncomingCallActivity"
Cohesion: 0.20
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 69 - ".getPrefs"
Cohesion: 0.07
Nodes (7): android.widget.SeekBar.OnSeekBarChangeListener, ChatNotificationHelper, ChatNotificationManager, ChatApiClient.ChatCallback<List<ChatConversation>>, OnChatsUpdateListener, ChatPreferencesHelper, ZezApplication

### Community 86 - ".getMessages"
Cohesion: 0.17
Nodes (4): ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatCallback<MessagesResult>, MessagesResult

### Community 88 - "Adapter"
Cohesion: 0.31
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 92 - "{ Pool }"
Cohesion: 0.18
Nodes (3): fs, path, { Pool }

### Community 93 - "Callback"
Cohesion: 0.22
Nodes (3): Callback, Callback, Runnable

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 352 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **64 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `AlarmItem`, `AlbumDetailActivity`, `Hardware Info & Performance Telemetry (C6)`, `ViewHolder`, `AudioPlaybackManager`, `android.content.Context`, `.loadGalleryData`, `ImageViewerActivity.java`, `AppUpdateHelper`, `ImageEditActivity.java`, `android.view.View`, `CallActivity.java`, `Override`, `ImageViewerActivity`, `CallLogItem`, `ChatApiClient`, `ConversationsAdapter`, `.getPrefs`, `MediaItem`, `CallRecordManager`, `MainActivity.java`, `.loadSettingsData`, `CallRecordItem`, `.checkIncomingCalls`?**
  _High betweenness centrality (0.075) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.11219512195121951 - nodes in this community are weakly interconnected._
- **Why does `ChatMessage` connect `ChatMessage` to `CallActivity.java`, `.handleReaction`, `serializable`, `ChatLocalDbHelper`, `.drainPendingQueue`, `ChatActivity`, `ChatMessageAdapter`, `ChatApiClient`, `.bind`, `.getMessages`, `locale`, `android.view.View`, `Override`?**
  _High betweenness centrality (0.053) - this node is a cross-community bridge._
- **Should `Hardware Info & Performance Telemetry (C6)` be split into smaller, more focused modules?**
  _Cohesion score 0.08067375886524823 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `AlarmRingingService.java`, `CallActivity.java`, `.handleReaction`, `.getPrefs`, `ChatMessage`, `ViewHolder`, `MainActivity.java`, `ChatLocalDbHelper`, `.drainPendingQueue`, `ChatMessageAdapter`, `ChatApiClient`, `ImageViewerActivity.java`, `.bind`, `.getMessages`, `android.view.View`, `Override`?**
  _High betweenness centrality (0.051) - this node is a cross-community bridge._
- **Should `okhttp3.Call` be split into smaller, more focused modules?**
  _Cohesion score 0.11414141414141414 - nodes in this community are weakly interconnected._