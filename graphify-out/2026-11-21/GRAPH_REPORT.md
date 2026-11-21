# Graph Report - Call Log Mannager  (2026-11-21)

## Corpus Check
- 123 files · ~107,932 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 155 file(s) not represented in the graph (top: .xml 146, .properties 5, (none) 2)
- WARNING: 155 of 291 files seen (53%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1815 nodes · 4871 edges · 104 communities (36 shown, 68 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 488 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `4b3fdbe8`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- CallActivity
- AlarmItem
- b2StorageService.js
- e2e_device_test.py
- ChatContactItem
- Hardware Info & Performance Telemetry (C6)
- AlbumDetailActivity.java
- ViewHolder
- CallRecordItem
- .toContentValues
- ZezApplication.java
- android.content.Context
- compilerOptions
- .loadGalleryData
- package.json
- index.d.ts
- ChatMessageAdapter
- ChatUser
- CallSyncWorker.java
- d_call_log_mannager_zez_backend_src_db_pool
- server.js
- MainActivity
- AppUpdateHelper
- ImageEditActivity.java
- admin.js
- Hardware Info & Performance Telemetry (C27)
- CallSyncWorker
- AlarmRingingService.java
- Override
- test-roster.js
- android.net.Uri
- ChatApiClient.java
- Override
- chat.js
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- ChatNotificationManager.java
- MediaPagerAdapter
- RecycleBinAdapter
- ChatConversation
- ImageViewerActivity
- CallLogItem
- extreme-test-suite.js
- okhttp3.Call
- GalleryAdapter
- AlarmRingingService.java
- Override
- ConversationsAdapter
- callRead.js
- IncomingCallActivity.java
- .getPrefs
- CallActivity.java
- ChatLocalDbHelper
- benchmark
- ChatActivity
- .triggerManualSync
- .applyModeState
- Adapter
- .loadAlbumMedia
- CallRecordAdapter
- com.zez.app.model.ChatContactItem
- .getPrefs
- devices.js
- AlarmHelper.java
- chat-extreme-test-suite.js
- TextWatcher
- com.zez.app.model.ChatMessage
- PhoneHelper
- .onSuccess

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 108 edges
2. `ChatActivity` - 71 edges
3. `ChatMessage` - 48 edges
4. `ChatMessageAdapter` - 47 edges
5. `CallRecordItem` - 43 edges
6. `ChatApiClient` - 39 edges
7. `CallActivity` - 37 edges
8. `ChatCallback` - 35 edges
9. `ViewHolder` - 34 edges
10. `ImageViewerActivity` - 28 edges

## Surprising Connections (you probably didn't know these)
- `ChatActivity` --references--> `ChatMessageAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ChatMessageAdapter.java
- `ChatActivity` --references--> `ChatLocalDbHelper`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/ChatLocalDbHelper.java
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `MainActivity` --references--> `CallRecordAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/CallRecordAdapter.java
- `MainActivity` --references--> `ConversationsAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java

## Import Cycles
- None detected.

## Communities (104 total, 68 thin omitted)

### Community 0 - "CallActivity"
Cohesion: 0.09
Nodes (6): CallActivity, TextureView.SurfaceTextureListener, CameraDevice.StateCallback, CameraCaptureSession.StateCallback, Runnable, Runnable

### Community 1 - "AlarmItem"
Cohesion: 0.12
Nodes (6): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.07
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 8 - "ViewHolder"
Cohesion: 0.13
Nodes (10): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder, FilmstripVH (+2 more)

### Community 9 - "CallRecordItem"
Cohesion: 0.06
Nodes (4): CallRecordItem, AudioPlaybackManager, OnPlaybackListener, CallRecordDbHelper

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
Cohesion: 0.13
Nodes (4): ChatMessageAdapter, OnMediaRetryListener, OnMessageActionListener, ChatMessageAdapter.OnMessageActionListener

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.04
Nodes (27): fs, path, fs, path, fs, path, fs, path (+19 more)

### Community 22 - "server.js"
Cohesion: 0.09
Nodes (23): closeDatabase(), initializeDatabase(), callMediaCache, express, router, router, app, cors (+15 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.21
Nodes (4): AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "ImageEditActivity.java"
Cohesion: 0.12
Nodes (5): SeekBar.OnSeekBarChangeListener, ImageEditActivity, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.10
Nodes (13): AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs, os (+5 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 28 - "CallSyncWorker"
Cohesion: 0.15
Nodes (3): CallLogListener, CallLogObserver, CallStateReceiver

### Community 31 - "Override"
Cohesion: 0.10
Nodes (7): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, ProfileMediaAdapter, ViewHolder

### Community 32 - "test-roster.js"
Cohesion: 0.11
Nodes (19): http, run(), http, run(), http, request(), run(), token (+11 more)

### Community 33 - "android.net.Uri"
Cohesion: 0.18
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 35 - "Override"
Cohesion: 0.06
Nodes (10): okhttp3.Callback, android.widget.AdapterView.OnItemSelectedListener, android.text.TextWatcher, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, android.text.TextWatcher (+2 more)

### Community 36 - "chat.js"
Cohesion: 0.06
Nodes (30): express, multer, express@^5.1.0, multer@^2.5.0, downloadsDir, express, fs, multer (+22 more)

### Community 52 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 53 - "RecycleBinAdapter"
Cohesion: 0.27
Nodes (3): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter

### Community 58 - "CallLogItem"
Cohesion: 0.23
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 59 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 61 - "okhttp3.Call"
Cohesion: 0.05
Nodes (32): Callback, Callback, Callback, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, Callback, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, Callback, Callback (+24 more)

### Community 63 - "GalleryAdapter"
Cohesion: 0.14
Nodes (5): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, GalleryListItem, MediaItem

### Community 66 - "ConversationsAdapter"
Cohesion: 0.18
Nodes (4): ConversationsAdapter, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener

### Community 67 - "callRead.js"
Cohesion: 0.09
Nodes (23): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+15 more)

### Community 90 - "CallRecordAdapter"
Cohesion: 0.39
Nodes (3): CallRecordAdapter, OnRecordDeletedListener, RecordViewHolder

### Community 93 - "devices.js"
Cohesion: 0.40
Nodes (3): crypto, express, router

### Community 95 - "chat-extreme-test-suite.js"
Cohesion: 0.50
Nodes (3): http, makeRequest(), runChatTestSuite()

### Community 98 - "com.zez.app.model.ChatMessage"
Cohesion: 0.14
Nodes (3): ChatRowItem, PendingMediaTask, PendingMessage

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 353 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **68 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `AlarmItem`, `.exitSelectMode`, `Hardware Info & Performance Telemetry (C6)`, `AlbumDetailActivity.java`, `ViewHolder`, `CallRecordItem`, `ZezApplication.java`, `android.content.Context`, `.loadGalleryData`, `ChatActivity.java`, `AppUpdateHelper`, `android.view.View`, `Override`, `Override`, `RecycleBinAdapter`, `ImageViewerActivity`, `.bind`, `okhttp3.Call`, `ConversationsAdapter`, `IncomingCallActivity.java`, `.getPrefs`, `.showMeta`, `MainActivity.java`, `.triggerManualSync`, `CallRecordAdapter`?**
  _High betweenness centrality (0.083) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `CallActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.08673469387755102 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `AlbumDetailActivity.java`, `ViewHolder`, `.toContentValues`, `ChatMessageAdapter`, `ChatActivity.java`, `CallSyncWorker`, `android.view.View`, `Override`, `ChatMessage`, `ChatNotificationManager.java`, `.bind`, `okhttp3.Call`, `AlarmRingingService.java`, `IncomingCallActivity.java`, `.getPrefs`, `ChatLocalDbHelper`, `MainActivity.java`, `.applyModeState`, `com.zez.app.model.ChatMessage`?**
  _High betweenness centrality (0.049) - this node is a cross-community bridge._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.12312312312312312 - nodes in this community are weakly interconnected._
- **Why does `CallActivity` connect `CallActivity` to `IncomingCallActivity.java`, `AlbumDetailActivity.java`, `ViewHolder`, `CallActivity.java`, `CallSyncWorker`, `android.view.View`?**
  _High betweenness centrality (0.023) - this node is a cross-community bridge._
- **Should `Hardware Info & Performance Telemetry (C6)` be split into smaller, more focused modules?**
  _Cohesion score 0.07315233785822021 - nodes in this community are weakly interconnected._