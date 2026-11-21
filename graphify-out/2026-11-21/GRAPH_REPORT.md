# Graph Report - Call Log Mannager  (2026-11-21)

## Corpus Check
- 112 files · ~95,652 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 135 file(s) not represented in the graph (top: .xml 126, .properties 5, (none) 2)
- WARNING: 135 of 260 files seen (52%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1572 nodes · 4085 edges · 87 communities (33 shown, 54 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 387 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `982c79cc`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Override
- AlarmItem
- b2StorageService.js
- ImageViewerActivity
- e2e_device_test.py
- ContactsActivity
- Hardware Info & Performance Telemetry (C6)
- ChatApiClient
- android.view.View
- MainActivity
- .toContentValues
- appRelease.js
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
- AppUpdateHelper
- ImageEditActivity
- admin.js
- Hardware Info & Performance Telemetry (C27)
- CallSyncWorker
- AlarmRingingService.java
- Override
- test-roster.js
- android.net.Uri
- ZezApplication.java
- chat.js
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- .registerDeviceSync
- MediaPagerAdapter
- RecycleBinAdapter
- .getPrefs
- android.widget.SeekBar
- CallLogItem
- extreme-test-suite.js
- ContactsActivity
- .drainPendingQueue
- .handleReaction
- AlarmRingingService
- .onResponse
- ChatConversation
- callRead.js
- CallLogAdapter
- android.content.Context
- GalleryAdapter.Listener
- .triggerManualSync
- ChatLocalDbHelper
- benchmark
- ChatActivity

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 99 edges
2. `ChatMessage` - 69 edges
3. `ChatActivity` - 68 edges
4. `ChatMessageAdapter` - 45 edges
5. `ChatApiClient` - 34 edges
6. `ChatCallback` - 34 edges
7. `ImageViewerActivity` - 28 edges
8. `ConversationsAdapter` - 27 edges
9. `AlarmItem` - 26 edges
10. `GalleryAdapter` - 26 edges

## Surprising Connections (you probably didn't know these)
- `ChatActivity` --references--> `ChatMessageAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ChatMessageAdapter.java
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `AlarmViewHolder` --inherits--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java
- `MainActivity` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `AlbumVH` --inherits--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/AlbumsAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java

## Import Cycles
- None detected.

## Communities (87 total, 54 thin omitted)

### Community 0 - "Override"
Cohesion: 0.11
Nodes (6): android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, ChatApiClient.ChatCallback<List<ChatConversation>>, android.text.TextWatcher, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, ChatApiClient.ChatCallback<String>

### Community 1 - "AlarmItem"
Cohesion: 0.11
Nodes (7): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 3 - "ImageViewerActivity"
Cohesion: 0.05
Nodes (11): FilmstripAdapter, OnFilmstripClickListener, GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener, ImageViewerActivity (+3 more)

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
Cohesion: 0.08
Nodes (23): ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient, Callback, Callback, Callback, ChatCallback<MessagesResult>, Callback, Callback (+15 more)

### Community 8 - "android.view.View"
Cohesion: 0.14
Nodes (9): IncomingViewHolder, OutgoingViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder, FilmstripVH, HeaderVH (+1 more)

### Community 11 - "appRelease.js"
Cohesion: 0.12
Nodes (9): { Pool }, downloadsDir, express, fs, multer, path, router, storage (+1 more)

### Community 12 - "PreferencesHelper"
Cohesion: 0.12
Nodes (3): TextWatcher, android.widget.AdapterView.OnItemSelectedListener, PreferencesHelper

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - ".loadGalleryData"
Cohesion: 0.29
Nodes (3): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>

### Community 15 - "package.json"
Cohesion: 0.07
Nodes (38): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, @noble/ciphers, pg, @types/cors, @types/express (+30 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "ChatMessageAdapter"
Cohesion: 0.16
Nodes (5): ChatMessageAdapter, ChatRowItem, DateHeaderViewHolder, OnMediaRetryListener, OnMessageActionListener

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.04
Nodes (28): fs, path, fs, path, fs, path, fs, path (+20 more)

### Community 22 - "server.js"
Cohesion: 0.10
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

### Community 31 - "Override"
Cohesion: 0.08
Nodes (10): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult> (+2 more)

### Community 32 - "test-roster.js"
Cohesion: 0.11
Nodes (19): http, run(), http, run(), http, request(), run(), token (+11 more)

### Community 33 - "android.net.Uri"
Cohesion: 0.17
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 35 - "ZezApplication.java"
Cohesion: 0.11
Nodes (5): ChatNotificationHelper, ChatNotificationManager, ChatApiClient.ChatCallback<List<ChatConversation>>, OnChatsUpdateListener, ZezApplication

### Community 36 - "chat.js"
Cohesion: 0.09
Nodes (19): express, multer, express@^5.1.0, multer@^2.5.0, avatarStorage, avatarUploadDir, express, fs (+11 more)

### Community 38 - "ChatMessage"
Cohesion: 0.14
Nodes (3): PendingMediaTask, PendingMessage, ChatMessage

### Community 51 - ".registerDeviceSync"
Cohesion: 0.16
Nodes (3): ApiCallback, ApiClient, DeviceHelper

### Community 52 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 53 - "RecycleBinAdapter"
Cohesion: 0.27
Nodes (4): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter, RecycledItem

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
  These have ≤1 connection - possible missing edges. (Counts symbols only; 330 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **54 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `Override`, `AlarmItem`, `ImageViewerActivity`, `Hardware Info & Performance Telemetry (C6)`, `ChatApiClient`, `android.view.View`, `PreferencesHelper`, `.loadGalleryData`, `ImageViewerActivity.java`, `MainActivity.java`, `AppUpdateHelper`, `list`, `Override`, `android.net.Uri`, `ZezApplication.java`, `.registerDeviceSync`, `ContactsActivity`, `AlarmRingingService`, `ChatConversation`, `android.content.Context`, `GalleryAdapter.Listener`, `.triggerManualSync`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `Override` be split into smaller, more focused modules?**
  _Cohesion score 0.10582010582010581 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `android.net.Uri`, `android.content.Context`, `ChatMessage`, `ChatApiClient`, `android.view.View`, `ChatLocalDbHelper`, `.toContentValues`, `ChatMessageAdapter`, `.handleReaction`, `.drainPendingQueue`, `ImageViewerActivity.java`, `.bind`, `MainActivity.java`, `ContactsActivity`, `list`, `AlarmRingingService.java`, `Override`?**
  _High betweenness centrality (0.043) - this node is a cross-community bridge._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.11219512195121951 - nodes in this community are weakly interconnected._
- **Why does `ConversationsAdapter` connect `ChatConversation` to `ImageViewerActivity`, `android.view.View`, `MainActivity`, `PreferencesHelper`, `.bind`, `MainActivity.java`, `list`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Should `ImageViewerActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.05307950727883538 - nodes in this community are weakly interconnected._