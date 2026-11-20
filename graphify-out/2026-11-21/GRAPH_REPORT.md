# Graph Report - Call Log Mannager  (2026-11-20)

## Corpus Check
- 111 files · ~93,591 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 133 file(s) not represented in the graph (top: .xml 124, .properties 5, (none) 2)
- WARNING: 133 of 257 files seen (52%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1538 nodes · 3957 edges · 83 communities (34 shown, 49 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 349 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `22e19c26`
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
- ViewHolder
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
- AppUpdateHelper
- ImageEditActivity
- admin.js
- Hardware Info & Performance Telemetry (C27)
- Device Registration & Authorization (C28)
- RecycleBinAdapter
- Override
- test-roster.js
- CallLogItem
- chat.js
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- com.zez.app.model.ChatMessage
- MediaPagerAdapter
- ContactsAdapter
- .getPrefs
- PhoneHelper
- android.net.Uri
- extreme-test-suite.js
- FilmstripAdapter
- .handleReaction
- AlarmRingingService
- .onResponse
- ChatConversation
- callRead.js
- .initThemeSpinner
- android.content.Context
- .triggerManualSync
- AlarmViewHolder
- ChatLocalDbHelper
- ApiCallback
- benchmark
- ChatActivity

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 97 edges
2. `ChatMessage` - 79 edges
3. `ChatActivity` - 65 edges
4. `ChatMessageAdapter` - 38 edges
5. `ChatApiClient` - 35 edges
6. `ChatCallback` - 34 edges
7. `ViewHolder` - 33 edges
8. `ImageViewerActivity` - 28 edges
9. `AlarmItem` - 26 edges
10. `GalleryAdapter` - 26 edges

## Surprising Connections (you probably didn't know these)
- `ChatActivity` --references--> `ChatMessageAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ChatMessageAdapter.java
- `ChatActivity` --references--> `ChatMessage`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/ChatMessage.java
- `ChatActivity` --references--> `ChatLocalDbHelper`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/ChatLocalDbHelper.java
- `ProfileMediaAdapter` --references--> `ChatMessage`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/ChatMessage.java
- `ContactsActivity` --references--> `ContactsAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ContactsActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ContactsAdapter.java

## Import Cycles
- None detected.

## Communities (83 total, 49 thin omitted)

### Community 0 - "Override"
Cohesion: 0.10
Nodes (6): android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, ChatApiClient.ChatCallback<String>

### Community 1 - "AlarmItem"
Cohesion: 0.13
Nodes (6): AlarmAdapter, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 3 - "ImageViewerActivity"
Cohesion: 0.06
Nodes (9): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener, ImageViewerActivity, ViewPager2.OnPageChangeCallback, GalleryListItem (+1 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "ContactsActivity"
Cohesion: 0.09
Nodes (5): DisplayItem, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.08
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - "ChatApiClient"
Cohesion: 0.08
Nodes (23): ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient, Callback, Callback, Callback, ChatCallback<MessagesResult>, Callback, Callback (+15 more)

### Community 8 - "android.view.View"
Cohesion: 0.20
Nodes (8): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactViewHolder, ViewHolder, FilmstripVH, HeaderVH, MediaVH

### Community 10 - "ViewHolder"
Cohesion: 0.19
Nodes (3): ItemTouchHelper.SimpleCallback, ProfileMediaAdapter, ViewHolder

### Community 11 - "db.js"
Cohesion: 0.07
Nodes (9): crypto, hashPassword(), verifyPassword(), crypto, express, router, http, makeRequest() (+1 more)

### Community 12 - "PreferencesHelper"
Cohesion: 0.14
Nodes (3): ApiClient, DeviceHelper, PreferencesHelper

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - ".loadGalleryData"
Cohesion: 0.26
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 15 - "package.json"
Cohesion: 0.06
Nodes (40): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, multer, @noble/ciphers, pg, @types/cors (+32 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 19 - "CallSyncWorker.java"
Cohesion: 0.17
Nodes (3): AlarmReceiver, CallStateReceiver, CallSyncWorker

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.07
Nodes (17): fs, path, fs, path, fs, path, fs, path (+9 more)

### Community 22 - "server.js"
Cohesion: 0.06
Nodes (34): express, express@^5.1.0, closeDatabase(), initializeDatabase(), { Pool }, downloadsDir, express, fs (+26 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.17
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "ImageEditActivity"
Cohesion: 0.22
Nodes (3): ImageEditActivity, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.09
Nodes (13): AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs, os (+5 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 28 - "Device Registration & Authorization (C28)"
Cohesion: 0.12
Nodes (3): AlbumItem, CallLogListener, CallLogObserver

### Community 30 - "RecycleBinAdapter"
Cohesion: 0.31
Nodes (3): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter

### Community 31 - "Override"
Cohesion: 0.09
Nodes (8): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, android.widget.SeekBar.OnSeekBarChangeListener, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>

### Community 32 - "test-roster.js"
Cohesion: 0.11
Nodes (19): http, run(), http, run(), http, request(), run(), token (+11 more)

### Community 33 - "CallLogItem"
Cohesion: 0.23
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 36 - "chat.js"
Cohesion: 0.14
Nodes (11): avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir, multer, path (+3 more)

### Community 52 - "MediaPagerAdapter"
Cohesion: 0.33
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 58 - "android.net.Uri"
Cohesion: 0.31
Nodes (3): AlbumsAdapter, AlbumVH, OnAlbumClickListener

### Community 59 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 66 - "ChatConversation"
Cohesion: 0.05
Nodes (9): ConversationsAdapter, OnConversationClickListener, OnSelectionListener, ChatConversation, ChatNotificationHelper, ChatNotificationManager, ChatApiClient.ChatCallback<List<ChatConversation>>, OnChatsUpdateListener (+1 more)

### Community 67 - "callRead.js"
Cohesion: 0.09
Nodes (23): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+15 more)

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 327 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **49 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `Override`, `AlarmItem`, `ChatConversation`, `ImageViewerActivity`, `.initThemeSpinner`, `android.content.Context`, `.triggerManualSync`, `ChatApiClient`, `android.view.View`, `Hardware Info & Performance Telemetry (C6)`, `PreferencesHelper`, `.loadGalleryData`, `ImageViewerActivity.java`, `MainActivity.java`, `AppUpdateHelper`?**
  _High betweenness centrality (0.089) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `Override` be split into smaller, more focused modules?**
  _Cohesion score 0.09852216748768473 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `AlarmRingingService`, `ChatMessage`, `ChatApiClient`, `android.view.View`, `ChatLocalDbHelper`, `ViewHolder`, `ChatMessageAdapter`, `com.zez.app.model.ChatMessage`, `ImageViewerActivity.java`, `.bind`, `MainActivity.java`, `Device Registration & Authorization (C28)`, `.handleReaction`, `Override`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.12612612612612611 - nodes in this community are weakly interconnected._
- **Why does `ChatMessage` connect `ChatMessage` to `ChatApiClient.java`, `ChatApiClient`, `ChatLocalDbHelper`, `ViewHolder`, `ChatActivity`, `ChatMessageAdapter`, `.handleReaction`, `com.zez.app.model.ChatMessage`, `ChatUser`, `AlarmHelper.java`, `.bind`, `MainActivity.java`, `list`, `Override`?**
  _High betweenness centrality (0.039) - this node is a cross-community bridge._
- **Should `ImageViewerActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.058254963427377224 - nodes in this community are weakly interconnected._