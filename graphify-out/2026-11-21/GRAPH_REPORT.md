# Graph Report - Call Log Mannager  (2026-11-21)

## Corpus Check
- 117 files · ~101,685 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 146 file(s) not represented in the graph (top: .xml 137, .properties 5, (none) 2)
- WARNING: 146 of 276 files seen (53%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1683 nodes · 4422 edges · 97 communities (39 shown, 58 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 388 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `27f0fa47`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- CallActivity
- AlarmItem
- b2StorageService.js
- AlbumDetailActivity
- e2e_device_test.py
- ContactsActivity
- Hardware Info & Performance Telemetry (C6)
- okhttp3.Call
- android.view.View
- .initViews
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
- server.js
- MainActivity
- Override
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
- .bind
- .getPrefs
- ImageViewerActivity
- CallLogItem
- extreme-test-suite.js
- Override
- ChatApiClient
- GalleryAdapter
- AlarmHelper.java
- .onResponse
- com.zez.app.model.ChatConversation
- callRead.js
- IncomingCallActivity
- android.content.Context
- MediaItem
- ConversationsAdapter
- ChatLocalDbHelper
- ChatConversation
- benchmark
- ChatActivity
- ChatNotificationManager
- .onResponse
- Adapter
- .applyModeState
- PhoneHelper
- AlarmFiringActivity
- .onReceive
- .onSuccess

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 100 edges
2. `ChatActivity` - 70 edges
3. `ChatMessageAdapter` - 47 edges
4. `ChatMessage` - 46 edges
5. `ChatApiClient` - 38 edges
6. `CallActivity` - 37 edges
7. `ChatCallback` - 34 edges
8. `ImageViewerActivity` - 28 edges
9. `AlarmItem` - 26 edges
10. `GalleryAdapter` - 26 edges

## Surprising Connections (you probably didn't know these)
- `ChatActivity` --references--> `ChatMessageAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ChatMessageAdapter.java
- `ChatActivity` --references--> `ChatLocalDbHelper`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/ChatLocalDbHelper.java
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `MainActivity` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `MainActivity` --references--> `TelemetryGraphView`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/TelemetryGraphView.java

## Import Cycles
- None detected.

## Communities (97 total, 58 thin omitted)

### Community 0 - "CallActivity"
Cohesion: 0.11
Nodes (5): CallActivity, CameraDevice.StateCallback, CameraCaptureSession.StateCallback, Runnable, Runnable

### Community 1 - "AlarmItem"
Cohesion: 0.11
Nodes (7): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "ContactsActivity"
Cohesion: 0.09
Nodes (4): DisplayItem, OnContactClickListener, ContactsActivity, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.06
Nodes (6): Runnable, TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - "okhttp3.Call"
Cohesion: 0.14
Nodes (10): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+2 more)

### Community 8 - "android.view.View"
Cohesion: 0.17
Nodes (7): AlbumVH, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder, HeaderVH, MediaVH

### Community 11 - "appRelease.js"
Cohesion: 0.12
Nodes (14): downloadsDir, express, fs, multer, path, router, storage, upload (+6 more)

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
Cohesion: 0.08
Nodes (6): ChatMessageAdapter, ChatRowItem, OnMediaRetryListener, OnMessageActionListener, ChatMessageAdapter.OnMessageActionListener, PendingMessage

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.04
Nodes (29): fs, path, fs, path, fs, path, fs, path (+21 more)

### Community 22 - "server.js"
Cohesion: 0.09
Nodes (17): closeDatabase(), initializeDatabase(), { Pool }, callMediaCache, express, router, app, cors (+9 more)

### Community 24 - "Override"
Cohesion: 0.05
Nodes (13): okhttp3.Callback, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, android.text.TextWatcher, com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback (+5 more)

### Community 25 - "ImageEditActivity"
Cohesion: 0.17
Nodes (4): ImageEditActivity, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.10
Nodes (13): AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs, os (+5 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 28 - "CallSyncWorker"
Cohesion: 0.13
Nodes (3): CallLogListener, CallLogObserver, CallStateReceiver

### Community 31 - "Override"
Cohesion: 0.10
Nodes (7): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, ProfileMediaAdapter, ViewHolder

### Community 32 - "test-roster.js"
Cohesion: 0.11
Nodes (19): http, run(), http, run(), http, request(), run(), token (+11 more)

### Community 33 - "android.net.Uri"
Cohesion: 0.18
Nodes (4): AlbumsAdapter, OnAlbumClickListener, PendingMediaTask, AlbumItem

### Community 36 - "chat.js"
Cohesion: 0.09
Nodes (19): express, multer, express@^5.1.0, multer@^2.5.0, avatarStorage, avatarUploadDir, express, fs (+11 more)

### Community 51 - ".registerDeviceSync"
Cohesion: 0.14
Nodes (4): ApiCallback, ApiClient, CallLogHelper, DeviceHelper

### Community 52 - "MediaPagerAdapter"
Cohesion: 0.19
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 53 - "RecycleBinAdapter"
Cohesion: 0.36
Nodes (3): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter

### Community 54 - ".bind"
Cohesion: 0.15
Nodes (3): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder

### Community 58 - "CallLogItem"
Cohesion: 0.17
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 59 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 60 - "Override"
Cohesion: 0.20
Nodes (4): Callback, TextureView.SurfaceTextureListener, Callback, Callback

### Community 61 - "ChatApiClient"
Cohesion: 0.18
Nodes (7): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient, Callback, Callback, ChatCallback, ContactSyncResult, MediaUploadResult

### Community 63 - "GalleryAdapter"
Cohesion: 0.18
Nodes (3): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener

### Community 67 - "callRead.js"
Cohesion: 0.08
Nodes (26): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+18 more)

### Community 68 - "IncomingCallActivity"
Cohesion: 0.23
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 71 - "ConversationsAdapter"
Cohesion: 0.20
Nodes (3): ConversationsAdapter, OnAvatarClickListener, OnSelectionListener

### Community 86 - ".onResponse"
Cohesion: 0.23
Nodes (7): ChatCallback<MessagesResult>, Callback, ConversationModeInfo, IncomingCallInfo, MessagesResult, PeerStatus, PollResult

### Community 88 - "Adapter"
Cohesion: 0.31
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 341 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **58 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `AlarmItem`, `AlbumDetailActivity`, `Hardware Info & Performance Telemetry (C6)`, `android.view.View`, `.initViews`, `PreferencesHelper`, `.loadGalleryData`, `Override`, `Override`, `ZezApplication.java`, `.registerDeviceSync`, `MediaPagerAdapter`, `.bind`, `ImageViewerActivity`, `CallLogItem`, `ChatApiClient`, `AlarmHelper.java`, `com.zez.app.model.ChatConversation`, `android.content.Context`, `MediaItem`, `ChatNotificationManager`, `.switchTab`?**
  _High betweenness centrality (0.073) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `CallActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.11428571428571428 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `android.net.Uri`, `android.content.Context`, `ChatMessage`, `android.view.View`, `ChatLocalDbHelper`, `.toContentValues`, `ChatMessageAdapter`, `ChatApiClient`, `.bind`, `MainActivity`, `.onResponse`, `.applyModeState`, `CallSyncWorker`, `list`, `AlarmRingingService.java`, `Override`?**
  _High betweenness centrality (0.049) - this node is a cross-community bridge._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.11219512195121951 - nodes in this community are weakly interconnected._
- **Should `ContactsActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.08536585365853659 - nodes in this community are weakly interconnected._
- **Should `Hardware Info & Performance Telemetry (C6)` be split into smaller, more focused modules?**
  _Cohesion score 0.06390977443609022 - nodes in this community are weakly interconnected._