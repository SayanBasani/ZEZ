# Graph Report - Call Log Mannager  (2026-11-20)

## Corpus Check
- 103 files · ~75,682 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 94 file(s) not represented in the graph (top: .xml 85, .properties 5, (none) 2)
- WARNING: 94 of 210 files seen (45%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1323 nodes · 3244 edges · 71 communities (30 shown, 41 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 201 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `9bf7905d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ImageViewerActivity
- android.content.Context
- ChatConversation
- AlarmItem
- e2e_device_test.py
- Contacts & User Discovery (C5)
- Hardware Info & Performance Telemetry (C6)
- .postError
- android.widget.TextView
- MainActivity
- .showChatProfileDialog
- d_call_log_mannager_zez_backend_src_db_pool
- ChatActivity.java
- compilerOptions
- package.json
- index.d.ts
- .loadGalleryData
- Override
- Network APIs & Background Sync (C19)
- d_call_log_mannager_zez_backend_src_db_pool
- AlarmRingingService.java
- Network APIs & Background Sync (C22)
- callSync.js
- AppUpdateHelper
- ImageEditActivity
- admin.js
- Hardware Info & Performance Telemetry (C27)
- Device Registration & Authorization (C28)
- Backend Admin Routes & APIs (C30)
- ImageViewerActivity.java
- test-roster.js
- Adapter
- ChatUser
- chat.js
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- AlarmRingingService
- .getPrefs
- .onCreate
- PhoneHelper
- android.net.Uri
- .onResponse
- ApiCallback
- .onReceive
- db.js
- ref_node_crypto
- DeviceHelper.java
- benchmark

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 91 edges
2. `ChatMessage` - 43 edges
3. `ChatActivity` - 35 edges
4. `ViewHolder` - 31 edges
5. `ImageViewerActivity` - 28 edges
6. `ChatCallback` - 28 edges
7. `ChatApiClient` - 27 edges
8. `GalleryAdapter` - 26 edges
9. `MediaItem` - 26 edges
10. `AlarmItem` - 26 edges

## Surprising Connections (you probably didn't know these)
- `ChatActivity` --references--> `ChatMessage`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/ChatMessage.java
- `ChatMessageAdapter` --references--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/ChatMessageAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java
- `GalleryAdapter` --references--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/GalleryAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java
- `RecycleBinAdapter` --references--> `RecycledItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/RecycleBinAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/util/RecycleBinHelper.java
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java

## Import Cycles
- None detected.

## Communities (71 total, 41 thin omitted)

### Community 0 - "ImageViewerActivity"
Cohesion: 0.05
Nodes (12): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener, GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener (+4 more)

### Community 1 - "android.content.Context"
Cohesion: 0.17
Nodes (3): ApiClient, DeviceHelper, PreferencesHelper

### Community 2 - "ChatConversation"
Cohesion: 0.06
Nodes (8): OnConversationClickListener, ChatConversation, Callback, ChatNotificationHelper, ChatNotificationManager, ChatApiClient.ChatCallback<List<ChatConversation>>, OnChatsUpdateListener, ZezApplication

### Community 3 - "AlarmItem"
Cohesion: 0.09
Nodes (7): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "Contacts & User Discovery (C5)"
Cohesion: 0.07
Nodes (6): ContactsAdapter, DisplayItem, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.07
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - ".postError"
Cohesion: 0.06
Nodes (23): ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient, Callback (+15 more)

### Community 8 - "android.widget.TextView"
Cohesion: 0.16
Nodes (9): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactViewHolder, HeaderViewHolder, ConversationsAdapter, ViewHolder, HeaderVH (+1 more)

### Community 11 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.12
Nodes (6): crypto, hashPassword(), verifyPassword(), http, makeRequest(), runChatTestSuite()

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 15 - "package.json"
Cohesion: 0.07
Nodes (36): cors, dotenv, multer, @noble/ciphers, pg, @types/cors, @types/express, @types/multer (+28 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - ".loadGalleryData"
Cohesion: 0.26
Nodes (4): MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 18 - "Override"
Cohesion: 0.10
Nodes (6): android.widget.AdapterView.OnItemSelectedListener, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.10
Nodes (11): fs, path, fs, path, fs, path, fs, path (+3 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.11
Nodes (13): closeDatabase(), initializeDatabase(), router, app, cors, express, path, port (+5 more)

### Community 23 - "callSync.js"
Cohesion: 0.16
Nodes (9): { Pool }, crypto, deviceAuth(), crypto, express, getTimestamp(), httpError(), normalizeCall() (+1 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.17
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "ImageEditActivity"
Cohesion: 0.22
Nodes (3): ImageEditActivity, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.09
Nodes (12): AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs, os (+4 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 30 - "Backend Admin Routes & APIs (C30)"
Cohesion: 0.12
Nodes (13): express, express@^5.1.0, downloadsDir, express, fs, multer, path, router (+5 more)

### Community 32 - "test-roster.js"
Cohesion: 0.11
Nodes (19): http, run(), http, run(), http, request(), run(), token (+11 more)

### Community 33 - "Adapter"
Cohesion: 0.08
Nodes (9): CallLogAdapter, CallViewHolder, MediaPagerAdapter, OnTapListener, PageVH, BinViewHolder, OnRestoreClickListener, RecycleBinAdapter (+1 more)

### Community 36 - "chat.js"
Cohesion: 0.14
Nodes (11): avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir, multer, path (+3 more)

### Community 38 - "ChatMessage"
Cohesion: 0.07
Nodes (3): ChatRowItem, PendingMessage, ChatMessage

### Community 58 - "android.net.Uri"
Cohesion: 0.17
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 67 - "db.js"
Cohesion: 0.15
Nodes (15): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+7 more)

### Community 68 - "ref_node_crypto"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 314 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **41 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `ImageViewerActivity`, `android.content.Context`, `ChatConversation`, `AlarmItem`, `Hardware Info & Performance Telemetry (C6)`, `android.widget.TextView`, `.showChatProfileDialog`, `ChatActivity.java`, `MainActivity.java`, `.loadGalleryData`, `Override`, `.onCreate`, `AppUpdateHelper`, `android.net.Uri`, `list`, `ImageViewerActivity.java`?**
  _High betweenness centrality (0.103) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `ImageViewerActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.05112560488112771 - nodes in this community are weakly interconnected._
- **Why does `ChatMessage` connect `ChatMessage` to `ChatUser`, `ChatActivity.java`, `list`?**
  _High betweenness centrality (0.032) - this node is a cross-community bridge._
- **Should `ChatConversation` be split into smaller, more focused modules?**
  _Cohesion score 0.05649350649350649 - nodes in this community are weakly interconnected._
- **Why does `ImageEditActivity` connect `ImageEditActivity` to `android.widget.TextView`, `list`, `ImageViewerActivity.java`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.08599290780141844 - nodes in this community are weakly interconnected._