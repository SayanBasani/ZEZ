# Graph Report - Call Log Mannager  (2026-11-20)

## Corpus Check
- 111 files · ~91,254 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 125 file(s) not represented in the graph (top: .xml 116, .properties 5, (none) 2)
- WARNING: 125 of 249 files seen (50%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1504 nodes · 3803 edges · 78 communities (31 shown, 47 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 315 edges (avg confidence: 0.85)
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
- ChatCallback
- android.view.View
- MainActivity
- .drainPendingQueue
- { Pool }
- PreferencesHelper
- compilerOptions
- .loadGalleryData
- package.json
- index.d.ts
- ChatMessageAdapter
- ChatUser
- CallSyncWorker.java
- d_call_log_mannager_zez_backend_src_db_pool
- Network APIs & Background Sync (C22)
- MainActivity.java
- AppUpdateHelper
- ImageEditActivity
- admin.js
- Hardware Info & Performance Telemetry (C27)
- Device Registration & Authorization (C28)
- server.js
- Override
- test-roster.js
- CallLogItem
- ChatApiClient.java
- CallSyncWorker
- chat.js
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- com.zez.app.model.ChatMessage
- .getPrefs
- PhoneHelper
- android.net.Uri
- extreme-test-suite.js
- test_chat_endpoints.js
- AlarmRingingService.java
- AlarmFiringActivity
- AlarmRingingService
- ChatConversation
- callRead.js
- android.content.Context
- .triggerManualSync
- ChatLocalDbHelper
- benchmark

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 95 edges
2. `ChatActivity` - 60 edges
3. `ChatMessage` - 44 edges
4. `ChatMessageAdapter` - 37 edges
5. `ViewHolder` - 37 edges
6. `ChatCallback` - 32 edges
7. `ChatApiClient` - 30 edges
8. `ImageViewerActivity` - 28 edges
9. `AlarmItem` - 26 edges
10. `GalleryAdapter` - 26 edges

## Surprising Connections (you probably didn't know these)
- `decodeRecord()` --calls--> `decryptStoredPayload()`  [EXTRACTED]
  Zez-Backend/src/routes/admin.js → Zez-Backend/src/crypto.js
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `AlarmViewHolder` --inherits--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java
- `MainActivity` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `RecycleBinAdapter` --references--> `RecycledItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/RecycleBinAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/util/RecycleBinHelper.java

## Import Cycles
- None detected.

## Communities (78 total, 47 thin omitted)

### Community 0 - "Override"
Cohesion: 0.10
Nodes (7): android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, Runnable, ChatApiClient.ChatCallback<String>

### Community 1 - "AlarmItem"
Cohesion: 0.11
Nodes (7): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 3 - "ImageViewerActivity"
Cohesion: 0.05
Nodes (12): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener, GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener (+4 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "ContactsActivity"
Cohesion: 0.08
Nodes (6): ContactsAdapter, DisplayItem, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.08
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - "ChatCallback"
Cohesion: 0.06
Nodes (25): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, okhttp3.Callback, ChatApiClient, Callback, Callback, Callback (+17 more)

### Community 8 - "android.view.View"
Cohesion: 0.14
Nodes (10): AlbumVH, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactViewHolder, HeaderViewHolder, ViewHolder, HeaderVH (+2 more)

### Community 12 - "PreferencesHelper"
Cohesion: 0.10
Nodes (3): android.widget.AdapterView.OnItemSelectedListener, DeviceHelper, PreferencesHelper

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - ".loadGalleryData"
Cohesion: 0.26
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 15 - "package.json"
Cohesion: 0.07
Nodes (38): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, @noble/ciphers, pg, @types/cors, @types/express (+30 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "ChatMessageAdapter"
Cohesion: 0.14
Nodes (3): ChatMessageAdapter, OnMessageActionListener, ChatMessageAdapter.OnMessageActionListener

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.05
Nodes (23): fs, path, fs, path, fs, path, fs, path (+15 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.11
Nodes (14): closeDatabase(), initializeDatabase(), app, startServer(), http, path, run(), tests (+6 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.17
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "ImageEditActivity"
Cohesion: 0.22
Nodes (3): ImageEditActivity, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.10
Nodes (12): AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs, os (+4 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 30 - "server.js"
Cohesion: 0.08
Nodes (25): express, multer, express@^5.1.0, multer@^2.5.0, router, downloadsDir, express, fs (+17 more)

### Community 31 - "Override"
Cohesion: 0.10
Nodes (6): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener

### Community 32 - "test-roster.js"
Cohesion: 0.13
Nodes (15): http, run(), http, run(), http, request(), run(), token (+7 more)

### Community 33 - "CallLogItem"
Cohesion: 0.09
Nodes (8): CallLogAdapter, CallViewHolder, MediaPagerAdapter, OnTapListener, PageVH, OnRestoreClickListener, RecycleBinAdapter, CallLogItem

### Community 36 - "chat.js"
Cohesion: 0.14
Nodes (11): avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir, multer, path (+3 more)

### Community 58 - "android.net.Uri"
Cohesion: 0.18
Nodes (3): AlbumsAdapter, OnAlbumClickListener, AlbumItem

### Community 59 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 60 - "test_chat_endpoints.js"
Cohesion: 0.60
Nodes (4): get(), http, post(), run()

### Community 66 - "ChatConversation"
Cohesion: 0.06
Nodes (8): ConversationsAdapter, OnConversationClickListener, ChatConversation, ChatNotificationHelper, ChatNotificationManager, ChatApiClient.ChatCallback<List<ChatConversation>>, OnChatsUpdateListener, ZezApplication

### Community 67 - "callRead.js"
Cohesion: 0.09
Nodes (23): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+15 more)

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 325 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **47 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `Override`, `AlarmItem`, `ChatConversation`, `ImageViewerActivity`, `android.content.Context`, `.triggerManualSync`, `ChatCallback`, `android.view.View`, `Hardware Info & Performance Telemetry (C6)`, `PreferencesHelper`, `.loadGalleryData`, `ImageEditActivity.java`, `MainActivity.java`, `AppUpdateHelper`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `Override` be split into smaller, more focused modules?**
  _Cohesion score 0.09852216748768473 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `MainActivity.java` to `ChatConversation`, `android.content.Context`, `ChatMessage`, `ChatCallback`, `android.view.View`, `ChatLocalDbHelper`, `.drainPendingQueue`, `.initViews`, `ChatMessageAdapter`, `com.zez.app.model.ChatMessage`, `.bind`, `Device Registration & Authorization (C28)`, `AlarmRingingService.java`, `Override`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.11219512195121951 - nodes in this community are weakly interconnected._
- **Why does `ChatMessage` connect `ChatMessage` to `ChatApiClient.java`, `ChatCallback`, `.drainPendingQueue`, `ChatMessageAdapter`, `ChatUser`, `com.zez.app.model.ChatMessage`, `.bind`?**
  _High betweenness centrality (0.033) - this node is a cross-community bridge._
- **Should `ImageViewerActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.05112560488112771 - nodes in this community are weakly interconnected._