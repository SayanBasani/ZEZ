# Graph Report - Call Log Mannager  (2026-11-21)

## Corpus Check
- 111 files · ~95,399 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 135 file(s) not represented in the graph (top: .xml 126, .properties 5, (none) 2)
- WARNING: 135 of 259 files seen (52%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1567 nodes · 4077 edges · 88 communities (32 shown, 56 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 385 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b6cb99ce`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Override
- AlarmItem
- ImageViewerActivity
- GalleryAdapter
- e2e_device_test.py
- ContactsActivity
- Hardware Info & Performance Telemetry (C6)
- ChatApiClient
- android.view.View
- MainActivity
- .toContentValues
- db.js
- PreferencesHelper
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
- MainActivity.java
- AppUpdateHelper
- ImageEditActivity
- admin.js
- Hardware Info & Performance Telemetry (C27)
- CallSyncWorker
- AlarmRingingService.java
- Override
- test-roster.js
- CallLogItem
- ZezApplication.java
- chat.js
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- ChatNotificationManager.java
- MediaPagerAdapter
- MediaItem
- .getPrefs
- android.widget.SeekBar
- extreme-test-suite.js
- FilmstripAdapter
- .handleReaction
- AlarmRingingService
- .onResponse
- ChatConversation
- callRead.js
- .onItemSelected
- android.content.Context
- com.google.android.material.tabs.TabLayout.OnTabSelectedListener
- GalleryAdapter.Listener
- ChatLocalDbHelper
- AlarmFiringActivity
- benchmark
- ChatActivity

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 99 edges
2. `ChatMessage` - 85 edges
3. `ChatActivity` - 68 edges
4. `ChatMessageAdapter` - 45 edges
5. `ChatApiClient` - 35 edges
6. `ChatCallback` - 34 edges
7. `ViewHolder` - 33 edges
8. `ImageViewerActivity` - 28 edges
9. `ConversationsAdapter` - 27 edges
10. `AlarmItem` - 26 edges

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

## Communities (88 total, 56 thin omitted)

### Community 0 - "Override"
Cohesion: 0.11
Nodes (6): android.text.TextWatcher, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, android.text.TextWatcher, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, ChatApiClient.ChatCallback<String>

### Community 1 - "AlarmItem"
Cohesion: 0.11
Nodes (7): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 3 - "GalleryAdapter"
Cohesion: 0.12
Nodes (3): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "ContactsActivity"
Cohesion: 0.09
Nodes (3): DisplayItem, OnContactClickListener, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.08
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - "ChatApiClient"
Cohesion: 0.07
Nodes (26): ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient, Callback, Callback, Callback (+18 more)

### Community 8 - "android.view.View"
Cohesion: 0.14
Nodes (10): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder, FilmstripVH (+2 more)

### Community 9 - "MainActivity"
Cohesion: 0.06
Nodes (3): TextWatcher, MainActivity, Runnable

### Community 11 - "db.js"
Cohesion: 0.08
Nodes (10): crypto, hashPassword(), { Pool }, verifyPassword(), crypto, express, router, http (+2 more)

### Community 12 - "PreferencesHelper"
Cohesion: 0.13
Nodes (4): ApiCallback, ApiClient, DeviceHelper, PreferencesHelper

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - ".loadGalleryData"
Cohesion: 0.26
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 15 - "package.json"
Cohesion: 0.05
Nodes (60): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, @noble/ciphers, pg, @types/cors, @types/express (+52 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "ChatMessageAdapter"
Cohesion: 0.11
Nodes (4): ChatMessageAdapter, ChatRowItem, OnMediaRetryListener, OnMessageActionListener

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.07
Nodes (17): fs, path, fs, path, fs, path, fs, path (+9 more)

### Community 22 - "server.js"
Cohesion: 0.09
Nodes (20): closeDatabase(), initializeDatabase(), router, app, cors, express, fs, path (+12 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.17
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 26 - "admin.js"
Cohesion: 0.10
Nodes (12): AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs, os (+4 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 28 - "CallSyncWorker"
Cohesion: 0.13
Nodes (4): CallLogListener, CallLogObserver, CallSyncWorker, CallLogHelper

### Community 31 - "Override"
Cohesion: 0.11
Nodes (7): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, ProfileMediaAdapter, ViewHolder

### Community 32 - "test-roster.js"
Cohesion: 0.08
Nodes (27): http, run(), http, run(), http, request(), run(), token (+19 more)

### Community 33 - "CallLogItem"
Cohesion: 0.09
Nodes (9): AlbumsAdapter, OnAlbumClickListener, CallLogAdapter, CallViewHolder, BinViewHolder, OnRestoreClickListener, RecycleBinAdapter, AlbumItem (+1 more)

### Community 35 - "ZezApplication.java"
Cohesion: 0.16
Nodes (3): ChatNotificationHelper, ChatApiClient.ChatCallback<List<ChatConversation>>, ZezApplication

### Community 36 - "chat.js"
Cohesion: 0.09
Nodes (19): express, multer, express@^5.1.0, multer@^2.5.0, avatarStorage, avatarUploadDir, express, fs (+11 more)

### Community 38 - "ChatMessage"
Cohesion: 0.14
Nodes (3): PendingMediaTask, PendingMessage, ChatMessage

### Community 52 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 59 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 66 - "ChatConversation"
Cohesion: 0.08
Nodes (6): ConversationsAdapter, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, ChatConversation, PhoneHelper

### Community 67 - "callRead.js"
Cohesion: 0.09
Nodes (23): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+15 more)

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 327 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **56 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `Override`, `AlarmItem`, `ChatConversation`, `ImageViewerActivity`, `ZezApplication.java`, `.onItemSelected`, `android.content.Context`, `GalleryAdapter`, `android.view.View`, `ChatApiClient`, `Hardware Info & Performance Telemetry (C6)`, `PreferencesHelper`, `.loadGalleryData`, `ImageViewerActivity.java`, `MediaItem`, `MainActivity.java`, `AppUpdateHelper`, `AlarmRingingService.java`?**
  _High betweenness centrality (0.083) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `Override` be split into smaller, more focused modules?**
  _Cohesion score 0.11076923076923077 - nodes in this community are weakly interconnected._
- **Why does `ChatMessage` connect `ChatMessage` to `ChatApiClient.java`, `ChatApiClient`, `ChatLocalDbHelper`, `.toContentValues`, `ChatActivity`, `ChatMessageAdapter`, `ChatUser`, `.bind`, `MainActivity.java`, `Override`, `list`, `.handleReaction`?**
  _High betweenness centrality (0.049) - this node is a cross-community bridge._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.11219512195121951 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `ChatMessage`, `ChatApiClient`, `android.view.View`, `ChatLocalDbHelper`, `.toContentValues`, `Override`, `ChatMessageAdapter`, `ChatUser`, `ChatNotificationManager.java`, `ImageViewerActivity.java`, `.bind`, `MainActivity.java`, `CallSyncWorker`, `AlarmRingingService.java`, `.handleReaction`?**
  _High betweenness centrality (0.039) - this node is a cross-community bridge._
- **Should `GalleryAdapter` be split into smaller, more focused modules?**
  _Cohesion score 0.11931818181818182 - nodes in this community are weakly interconnected._