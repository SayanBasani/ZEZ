# Graph Report - Call Log Mannager  (2026-11-21)

## Corpus Check
- 124 files · ~108,867 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 155 file(s) not represented in the graph (top: .xml 146, .properties 5, (none) 2)
- WARNING: 155 of 292 files seen (53%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1824 nodes · 4909 edges · 95 communities (38 shown, 57 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 507 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `27d887b3`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- CallActivity
- AlarmItem
- b2StorageService.js
- AudioPlaybackManager
- e2e_device_test.py
- ContactsActivity
- Hardware Info & Performance Telemetry (C6)
- CallRecordDbHelper
- android.view.View
- CallRecordItem
- .toContentValues
- ChatNotificationManager
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
- IncomingCallActivity
- AlarmRingingService.java
- Override
- test-roster.js
- AlbumDetailActivity.java
- ChatApiClient.java
- Override
- chat.js
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- appRelease.js
- { Pool }
- .getMessages
- ImageViewerActivity
- CallLogItem
- extreme-test-suite.js
- android.text.TextWatcher
- okhttp3.Call
- .downloadAndInstall
- AlarmRingingService.java
- .checkIncomingCalls
- ConversationsAdapter
- callRead.js
- IncomingCallActivity.java
- .getPrefs
- .onResponse
- .initViews
- android.text.TextWatcher
- benchmark
- ChatActivity
- GalleryAdapter.Listener
- ChatApiClient
- .onResponse
- .startCall
- CallRecordAdapter
- .getPrefs

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
- `CallRecordAdapter` --references--> `CallRecordItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/CallRecordAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/model/CallRecordItem.java
- `MainActivity` --references--> `CallRecordAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/CallRecordAdapter.java
- `MainActivity` --references--> `CallRecordItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/CallRecordItem.java
- `CallRecordManager` --references--> `CallRecordItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/util/CallRecordManager.java → ZezAndroid/app/src/main/java/com/zez/app/model/CallRecordItem.java
- `CallRecordManager` --references--> `CallRecordDbHelper`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/util/CallRecordManager.java → ZezAndroid/app/src/main/java/com/zez/app/util/CallRecordDbHelper.java

## Import Cycles
- None detected.

## Communities (95 total, 57 thin omitted)

### Community 0 - "CallActivity"
Cohesion: 0.08
Nodes (9): CallActivity, Callback, TextureView.SurfaceTextureListener, CameraDevice.StateCallback, CameraCaptureSession.StateCallback, Runnable, Callback, Runnable (+1 more)

### Community 1 - "AlarmItem"
Cohesion: 0.13
Nodes (6): AlarmAdapter, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "ContactsActivity"
Cohesion: 0.09
Nodes (5): DisplayItem, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.08
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 8 - "android.view.View"
Cohesion: 0.05
Nodes (24): AlarmViewHolder, AlbumsAdapter, AlbumVH, OnAlbumClickListener, CallLogAdapter, CallViewHolder, DateHeaderViewHolder, IncomingViewHolder (+16 more)

### Community 10 - ".toContentValues"
Cohesion: 0.10
Nodes (3): ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatMessage>, ChatLocalDbHelper

### Community 11 - "ChatNotificationManager"
Cohesion: 0.11
Nodes (5): BootReceiver, ChatNotificationHelper, ChatNotificationManager, OnChatsUpdateListener, ZezApplication

### Community 12 - "android.content.Context"
Cohesion: 0.13
Nodes (3): android.widget.AdapterView.OnItemSelectedListener, DeviceHelper, PreferencesHelper

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
Cohesion: 0.10
Nodes (6): ChatMessageAdapter, ChatRowItem, OnMediaRetryListener, OnMessageActionListener, ChatMessageAdapter.OnMessageActionListener, PendingMediaTask

### Community 19 - "CallSyncWorker.java"
Cohesion: 0.10
Nodes (3): CallStateReceiver, CallSyncWorker, CallLogHelper

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.05
Nodes (27): fs, path, fs, path, fs, path, fs, path (+19 more)

### Community 22 - "server.js"
Cohesion: 0.07
Nodes (27): express, closeDatabase(), initializeDatabase(), callMediaCache, express, router, router, crypto (+19 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.23
Nodes (4): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, ReleaseInfo, UpdateCheckCallback

### Community 25 - "ImageEditActivity.java"
Cohesion: 0.16
Nodes (4): ImageEditActivity, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.10
Nodes (13): AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs, os (+5 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 29 - "IncomingCallActivity"
Cohesion: 0.23
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 31 - "Override"
Cohesion: 0.08
Nodes (9): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ProfileMediaAdapter (+1 more)

### Community 32 - "test-roster.js"
Cohesion: 0.11
Nodes (19): http, run(), http, run(), http, request(), run(), token (+11 more)

### Community 35 - "Override"
Cohesion: 0.14
Nodes (5): com.google.android.material.tabs.TabLayout.OnTabSelectedListener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, Runnable, ChatApiClient.ChatCallback<String>

### Community 36 - "chat.js"
Cohesion: 0.09
Nodes (20): multer, multer@^2.5.0, avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir (+12 more)

### Community 52 - "appRelease.js"
Cohesion: 0.13
Nodes (10): fs, path, downloadsDir, express, fs, multer, path, router (+2 more)

### Community 53 - "{ Pool }"
Cohesion: 0.18
Nodes (3): fs, path, { Pool }

### Community 55 - ".getMessages"
Cohesion: 0.24
Nodes (4): ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, Callback, ChatCallback<MessagesResult>, MessagesResult

### Community 56 - "ImageViewerActivity"
Cohesion: 0.06
Nodes (9): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener, ImageViewerActivity, ViewPager2.OnPageChangeCallback, GalleryListItem (+1 more)

### Community 59 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 61 - "okhttp3.Call"
Cohesion: 0.14
Nodes (8): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback

### Community 66 - "ConversationsAdapter"
Cohesion: 0.06
Nodes (9): ConversationsAdapter, androidx.recyclerview.widget.DiffUtil.Callback, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener, ChatConversation, Callback, ChatApiClient.ChatCallback<List<ChatConversation>> (+1 more)

### Community 67 - "callRead.js"
Cohesion: 0.09
Nodes (23): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+15 more)

### Community 70 - ".onResponse"
Cohesion: 0.53
Nodes (5): Callback, ConversationModeInfo, IncomingCallInfo, PeerStatus, PollResult

### Community 86 - "ChatApiClient"
Cohesion: 0.16
Nodes (7): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient, Callback, Callback, ChatCallback, ContactSyncResult, MediaUploadResult

### Community 90 - "CallRecordAdapter"
Cohesion: 0.15
Nodes (3): CallRecordAdapter, OnRecordDeletedListener, SeekBar.OnSeekBarChangeListener

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 355 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **57 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `AlarmItem`, `AudioPlaybackManager`, `Hardware Info & Performance Telemetry (C6)`, `android.view.View`, `CallRecordItem`, `ChatNotificationManager`, `android.content.Context`, `.loadGalleryData`, `CallSyncWorker.java`, `AppUpdateHelper`, `AlbumDetailActivity.java`, `Override`, `ImageViewerActivity`, `.checkIncomingCalls`, `ConversationsAdapter`, `IncomingCallActivity.java`, `.getPrefs`, `.initViews`, `android.text.TextWatcher`, `ChatActivity.java`, `GalleryAdapter.Listener`, `ChatApiClient`, `CallRecordAdapter`?**
  _High betweenness centrality (0.067) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `CallActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.07686932215234102 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `.startCall`, `AlarmRingingService.java`, `IncomingCallActivity.java`, `.getPrefs`, `ChatMessage`, `.initViews`, `android.view.View`, `ChatActivity.java`, `.toContentValues`, `ChatMessageAdapter`, `ChatNotificationManager.java`, `ChatApiClient`, `.getMessages`, `.bind`, `ImageEditActivity.java`, `Override`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.12612612612612611 - nodes in this community are weakly interconnected._
- **Why does `CallRecordItem` connect `CallRecordItem` to `CallRecordDbHelper`, `android.view.View`, `ChatActivity.java`, `.initViews`, `locale`, `MainActivity`, `CallRecordAdapter`, `CallSyncWorker`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Should `ContactsActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.0859465737514518 - nodes in this community are weakly interconnected._