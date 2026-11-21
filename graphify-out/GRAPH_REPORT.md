# Graph Report - Call Log Mannager  (2026-11-21)

## Corpus Check
- 125 files · ~109,099 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 156 file(s) not represented in the graph (top: .xml 146, .properties 5, (none) 3)
- WARNING: 156 of 294 files seen (53%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1828 nodes · 4916 edges · 111 communities (34 shown, 77 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 507 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `aaf469f6`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- CallActivity
- AlarmItem
- b2StorageService.js
- AudioPlaybackManager
- e2e_device_test.py
- ChatContactItem
- Hardware Info & Performance Telemetry (C6)
- CallRecordDbHelper
- android.view.View
- CallRecordItem
- ChatLocalDbHelper
- ZezApplication.java
- android.content.Context
- compilerOptions
- .loadGalleryData
- package.json
- index.d.ts
- ChatMessageAdapter
- ChatUser
- log
- d_call_log_mannager_zez_backend_src_db_pool
- server.js
- MainActivity
- AppUpdateHelper
- ImageEditActivity.java
- admin.js
- Hardware Info & Performance Telemetry (C27)
- CallRecordManager
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
- android.os.Handler
- appRelease.js
- db.js
- ImageViewerActivity
- GalleryAdapter
- CallLogItem
- extreme-test-suite.js
- android.text.TextWatcher
- okhttp3.Call
- .downloadAndInstall
- android.content.Intent
- .checkIncomingCalls
- com.zez.app.model.ChatConversation
- callRead.js
- AlarmRingingService.java
- .getPrefs
- com.zez.app.model.ChatMessage
- .exitSelectMode
- MainActivity.java
- benchmark
- ChatActivity
- GalleryAdapter.Listener
- .applyModeState
- .onResponse
- Adapter
- com.zez.app.model.CallLogItem
- MediaItem
- .getPrefs
- com.zez.app.model.ChatContactItem
- ChatConversation
- MediaPagerAdapter
- ChatNotificationManager
- RecycleBinAdapter
- CallSyncWorker
- Override
- .onSuccess
- .onItemSelected
- TextWatcher
- com.google.android.material.tabs.TabLayout.OnTabSelectedListener
- PhoneHelper
- .onSuccess
- ApiCallback

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 108 edges
2. `ChatActivity` - 71 edges
3. `ChatMessage` - 48 edges
4. `ChatMessageAdapter` - 47 edges
5. `CallRecordItem` - 45 edges
6. `ChatApiClient` - 39 edges
7. `CallActivity` - 37 edges
8. `ChatCallback` - 35 edges
9. `ViewHolder` - 32 edges
10. `ImageViewerActivity` - 28 edges

## Surprising Connections (you probably didn't know these)
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `MainActivity` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `ChatActivity` --references--> `ChatLocalDbHelper`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/ChatLocalDbHelper.java
- `RecycleBinAdapter` --references--> `RecycledItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/RecycleBinAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/util/RecycleBinHelper.java
- `ChatMessageAdapter` --references--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/ChatMessageAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java

## Import Cycles
- None detected.

## Communities (111 total, 77 thin omitted)

### Community 0 - "CallActivity"
Cohesion: 0.08
Nodes (9): CallActivity, Callback, TextureView.SurfaceTextureListener, CameraDevice.StateCallback, CameraCaptureSession.StateCallback, Runnable, Callback, Runnable (+1 more)

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

### Community 8 - "android.view.View"
Cohesion: 0.14
Nodes (11): AlbumVH, RecordViewHolder, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder (+3 more)

### Community 12 - "android.content.Context"
Cohesion: 0.16
Nodes (3): ApiClient, DeviceHelper, PreferencesHelper

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - ".loadGalleryData"
Cohesion: 0.20
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 15 - "package.json"
Cohesion: 0.06
Nodes (39): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, @noble/ciphers, pg, @types/cors, @types/express (+31 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "ChatMessageAdapter"
Cohesion: 0.12
Nodes (4): ChatMessageAdapter, OnMediaRetryListener, OnMessageActionListener, ChatMessageAdapter.OnMessageActionListener

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.06
Nodes (19): fs, path, fs, path, fs, path, fs, path (+11 more)

### Community 22 - "server.js"
Cohesion: 0.10
Nodes (21): closeDatabase(), initializeDatabase(), router, router, app, cors, express, fs (+13 more)

### Community 23 - "MainActivity"
Cohesion: 0.08
Nodes (4): ConversationsAdapter, OnAvatarClickListener, OnSelectionListener, MainActivity

### Community 24 - "AppUpdateHelper"
Cohesion: 0.24
Nodes (3): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "ImageEditActivity.java"
Cohesion: 0.10
Nodes (7): CallRecordAdapter, OnRecordDeletedListener, SeekBar.OnSeekBarChangeListener, ImageEditActivity, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.09
Nodes (14): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+6 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 31 - "Override"
Cohesion: 0.11
Nodes (7): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, ProfileMediaAdapter, ViewHolder

### Community 32 - "test-roster.js"
Cohesion: 0.09
Nodes (22): http, run(), http, run(), http, request(), run(), token (+14 more)

### Community 33 - "android.net.Uri"
Cohesion: 0.18
Nodes (3): AlbumsAdapter, OnAlbumClickListener, AlbumItem

### Community 35 - "Override"
Cohesion: 0.14
Nodes (5): android.text.TextWatcher, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, Runnable, ChatApiClient.ChatCallback<String>

### Community 36 - "chat.js"
Cohesion: 0.07
Nodes (24): express, multer, multer@^2.5.0, avatarStorage, avatarUploadDir, express, fs, mediaStorage (+16 more)

### Community 52 - "appRelease.js"
Cohesion: 0.11
Nodes (12): fs, path, fs, path, downloadsDir, express, fs, multer (+4 more)

### Community 53 - "db.js"
Cohesion: 0.07
Nodes (9): fs, path, fs, path, crypto, hashPassword(), { Pool }, callMediaCache (+1 more)

### Community 56 - "GalleryAdapter"
Cohesion: 0.13
Nodes (4): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, GalleryListItem

### Community 59 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 61 - "okhttp3.Call"
Cohesion: 0.06
Nodes (28): Callback, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, IncomingCallActivity, Callback, Callback, ChatApiClient, Callback, Callback (+20 more)

### Community 67 - "callRead.js"
Cohesion: 0.09
Nodes (23): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+15 more)

### Community 70 - "com.zez.app.model.ChatMessage"
Cohesion: 0.17
Nodes (3): ChatRowItem, PendingMediaTask, PendingMessage

### Community 89 - "Adapter"
Cohesion: 0.19
Nodes (5): CallLogAdapter, CallViewHolder, FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 96 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 98 - "RecycleBinAdapter"
Cohesion: 0.31
Nodes (3): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 359 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **77 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `AlarmItem`, `AudioPlaybackManager`, `Hardware Info & Performance Telemetry (C6)`, `CallRecordDbHelper`, `android.view.View`, `CallRecordItem`, `ZezApplication.java`, `android.content.Context`, `.loadGalleryData`, `AppUpdateHelper`, `ImageEditActivity.java`, `ChatActivity.java`, `Override`, `Override`, `ImageViewerActivity`, `android.text.TextWatcher`, `okhttp3.Call`, `com.zez.app.model.ChatConversation`, `AlarmRingingService.java`, `.getPrefs`, `.exitSelectMode`, `MainActivity.java`, `GalleryAdapter.Listener`, `com.zez.app.model.CallLogItem`, `MediaItem`, `ChatNotificationManager`, `RecycleBinAdapter`, `.onItemSelected`?**
  _High betweenness centrality (0.055) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `CallActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.07609427609427609 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `.getPrefs`, `com.zez.app.model.ChatMessage`, `ChatMessage`, `android.view.View`, `MainActivity.java`, `ChatLocalDbHelper`, `ChatMessageAdapter`, `android.os.Handler`, `.bind`, `.applyModeState`, `AlarmHelper.java`, `okhttp3.Call`, `ChatActivity.java`, `Override`?**
  _High betweenness centrality (0.038) - this node is a cross-community bridge._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.11219512195121951 - nodes in this community are weakly interconnected._
- **Why does `ChatMessageAdapter` connect `ChatMessageAdapter` to `com.zez.app.model.ChatMessage`, `ChatMessage`, `android.view.View`, `MainActivity.java`, `ChatLocalDbHelper`, `ChatActivity`, `.bind`, `Adapter`, `CallRecordAdapter.java`, `ChatActivity.java`?**
  _High betweenness centrality (0.031) - this node is a cross-community bridge._
- **Should `AudioPlaybackManager` be split into smaller, more focused modules?**
  _Cohesion score 0.1422924901185771 - nodes in this community are weakly interconnected._