# Graph Report - Call Log Mannager  (2026-11-21)

## Corpus Check
- 126 files · ~109,306 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 156 file(s) not represented in the graph (top: .xml 146, .properties 5, (none) 3)
- WARNING: 156 of 295 files seen (53%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1833 nodes · 4923 edges · 103 communities (41 shown, 62 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 507 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `54b53b06`
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
- ChatApiClient
- CallRecordItem
- ChatLocalDbHelper
- IncomingCallActivity
- android.content.Context
- compilerOptions
- .loadGalleryData
- package.json
- index.d.ts
- Callback
- ChatUser
- AlarmReceiver.java
- d_call_log_mannager_zez_backend_src_db_pool
- AlarmRingingService
- server.js
- MainActivity
- AppUpdateHelper
- CallRecordAdapter
- admin.js
- Hardware Info & Performance Telemetry (C27)
- CallRecordAdapter.java
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
- .clearSelection
- .bind
- ImageViewerActivity
- GalleryAdapter
- CallLogItem
- extreme-test-suite.js
- okhttp3.Call
- .onResponse
- Override
- .checkIncomingCalls
- com.zez.app.model.ChatConversation
- callRead.js
- AlarmFiringActivity.java
- .getPrefs
- com.zez.app.model.ChatMessage
- .exitSelectMode
- MainActivity.java
- benchmark
- ChatActivity
- .getMessages
- .onResponse
- FilmstripAdapter
- com.zez.app.model.CallLogItem
- MediaItem
- .getPrefs
- ChatConversation
- MediaPagerAdapter
- Adapter
- CallSyncWorker.java
- .toContentValues
- .onItemSelected
- PhoneHelper

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 108 edges
2. `ChatActivity` - 71 edges
3. `ChatMessage` - 48 edges
4. `ChatMessageAdapter` - 47 edges
5. `ChatApiClient` - 39 edges
6. `CallRecordItem` - 39 edges
7. `CallActivity` - 37 edges
8. `ChatCallback` - 35 edges
9. `ViewHolder` - 32 edges
10. `ImageViewerActivity` - 28 edges

## Surprising Connections (you probably didn't know these)
- `AlarmAdapter` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `AlarmViewHolder` --inherits--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java
- `MainActivity` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `ChatActivity` --references--> `ChatLocalDbHelper`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/ChatLocalDbHelper.java

## Import Cycles
- None detected.

## Communities (103 total, 62 thin omitted)

### Community 0 - "CallActivity"
Cohesion: 0.10
Nodes (4): CallActivity, TextureView.SurfaceTextureListener, CameraDevice.StateCallback, CameraCaptureSession.StateCallback

### Community 1 - "AlarmItem"
Cohesion: 0.17
Nodes (4): com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "ContactsActivity"
Cohesion: 0.07
Nodes (8): ContactsAdapter, ContactViewHolder, DisplayItem, HeaderViewHolder, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.06
Nodes (8): ImageEditActivity, SeekBar.OnSeekBarChangeListener, ImageEditHelper, TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 8 - "ChatApiClient"
Cohesion: 0.25
Nodes (5): ChatApiClient, Callback, ChatCallback, ContactSyncResult, MediaUploadResult

### Community 11 - "IncomingCallActivity"
Cohesion: 0.23
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - ".loadGalleryData"
Cohesion: 0.24
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 15 - "package.json"
Cohesion: 0.06
Nodes (39): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, @noble/ciphers, pg, @types/cors, @types/express (+31 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "Callback"
Cohesion: 0.20
Nodes (4): Callback, Runnable, Runnable, Callback

### Community 19 - "AlarmReceiver.java"
Cohesion: 0.21
Nodes (3): AlarmReceiver, BootReceiver, CallStateReceiver

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.05
Nodes (27): fs, path, fs, path, fs, path, fs, path (+19 more)

### Community 22 - "server.js"
Cohesion: 0.06
Nodes (30): express, fs, path, closeDatabase(), initializeDatabase(), { Pool }, callMediaCache, express (+22 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.17
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "CallRecordAdapter"
Cohesion: 0.15
Nodes (5): CallRecordAdapter, OnRecordDeletedListener, RecordViewHolder, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener

### Community 26 - "admin.js"
Cohesion: 0.10
Nodes (13): AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs, os (+5 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 29 - "CallRecordAdapter.java"
Cohesion: 0.16
Nodes (7): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ViewHolder, FilmstripVH, HeaderVH, MediaVH

### Community 31 - "Override"
Cohesion: 0.09
Nodes (8): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ProfileMediaAdapter, ViewHolder

### Community 32 - "test-roster.js"
Cohesion: 0.11
Nodes (19): http, run(), http, run(), http, request(), run(), token (+11 more)

### Community 33 - "android.net.Uri"
Cohesion: 0.17
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 35 - "Override"
Cohesion: 0.08
Nodes (8): android.text.TextWatcher, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, android.text.TextWatcher, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, ChatApiClient.ChatCallback<String>

### Community 36 - "chat.js"
Cohesion: 0.09
Nodes (20): multer, multer@^2.5.0, avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir (+12 more)

### Community 52 - "appRelease.js"
Cohesion: 0.09
Nodes (14): fs, path, fs, path, fs, path, downloadsDir, express (+6 more)

### Community 56 - "GalleryAdapter"
Cohesion: 0.15
Nodes (3): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener

### Community 58 - "CallLogItem"
Cohesion: 0.17
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 59 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 61 - "okhttp3.Call"
Cohesion: 0.11
Nodes (12): Callback, Callback, ChatCallback<MessagesResult>, Callback, Callback, Callback, Callback, Callback (+4 more)

### Community 63 - ".onResponse"
Cohesion: 0.38
Nodes (4): Callback, ConversationModeInfo, IncomingCallInfo, PollResult

### Community 66 - "com.zez.app.model.ChatConversation"
Cohesion: 0.14
Nodes (4): ConversationsAdapter, androidx.recyclerview.widget.DiffUtil.Callback, OnAvatarClickListener, OnConversationClickListener

### Community 67 - "callRead.js"
Cohesion: 0.09
Nodes (23): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+15 more)

### Community 69 - ".getPrefs"
Cohesion: 0.07
Nodes (6): ChatNotificationHelper, ChatNotificationManager, ChatApiClient.ChatCallback<List<ChatConversation>>, OnChatsUpdateListener, ChatPreferencesHelper, ZezApplication

### Community 70 - "com.zez.app.model.ChatMessage"
Cohesion: 0.10
Nodes (5): ChatRowItem, OnMediaRetryListener, OnMessageActionListener, ChatMessageAdapter.OnMessageActionListener, PendingMessage

### Community 86 - ".getMessages"
Cohesion: 0.26
Nodes (5): ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, Callback, MessagesResult, PeerStatus

### Community 90 - "com.zez.app.model.CallLogItem"
Cohesion: 0.20
Nodes (4): ApiCallback, ApiClient, CallLogHelper, DeviceHelper

### Community 96 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 98 - "Adapter"
Cohesion: 0.13
Nodes (6): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, BinViewHolder, OnRestoreClickListener, RecycleBinAdapter

### Community 101 - ".toContentValues"
Cohesion: 0.11
Nodes (4): ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatMessage>, PendingMediaTask

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 362 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **62 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `AlarmItem`, `AudioPlaybackManager`, `Hardware Info & Performance Telemetry (C6)`, `CallRecordDbHelper`, `ChatApiClient`, `CallRecordItem`, `android.content.Context`, `.loadGalleryData`, `AppUpdateHelper`, `CallRecordAdapter`, `CallRecordAdapter.java`, `ChatActivity.java`, `Override`, `.clearSelection`, `ImageViewerActivity`, `CallLogItem`, `.checkIncomingCalls`, `com.zez.app.model.ChatConversation`, `AlarmFiringActivity.java`, `.getPrefs`, `MainActivity.java`, `.getUri`, `com.zez.app.model.CallLogItem`, `MediaItem`, `Adapter`, `.onItemSelected`?**
  _High betweenness centrality (0.052) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `CallActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.09830866807610994 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `ChatActivity.java`, `.getPrefs`, `com.zez.app.model.ChatMessage`, `ChatMessage`, `.toContentValues`, `MainActivity.java`, `ChatLocalDbHelper`, `ChatApiClient`, `.onResponse`, `.bind`, `.getMessages`, `CallRecordAdapter.java`, `AlarmRingingService.java`, `Override`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Should `AudioPlaybackManager` be split into smaller, more focused modules?**
  _Cohesion score 0.1422924901185771 - nodes in this community are weakly interconnected._
- **Why does `CallRecordItem` connect `CallRecordItem` to `ChatActivity.java`, `AudioPlaybackManager`, `CallRecordDbHelper`, `MainActivity.java`, `MainActivity`, `CallRecordAdapter`, `CallLogItem`, `CallRecordManager`, `CallRecordAdapter.java`, `AlarmRingingService.java`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **Should `ContactsActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.07239819004524888 - nodes in this community are weakly interconnected._