# Graph Report - Call Log Mannager  (2026-11-20)

## Corpus Check
- 108 files · ~84,707 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 125 file(s) not represented in the graph (top: .xml 116, .properties 5, (none) 2)
- WARNING: 125 of 248 files seen (50%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1464 nodes · 3708 edges · 87 communities (29 shown, 58 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 299 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b1eaaee8`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Override
- AlarmItem
- ChatConversation
- ImageViewerActivity
- e2e_device_test.py
- ChatContactItem
- Hardware Info & Performance Telemetry (C6)
- ChatCallback
- android.view.View
- MainActivity
- .drainPendingQueue
- d_call_log_mannager_zez_backend_src_db_pool
- android.content.Context
- compilerOptions
- ChatNotificationManager.java
- package.json
- index.d.ts
- ChatMessageAdapter
- ChatUser
- Network APIs & Background Sync (C19)
- d_call_log_mannager_zez_backend_src_db_pool
- Network APIs & Background Sync (C22)
- MainActivity.java
- AppUpdateHelper
- ImageEditActivity
- admin.js
- Hardware Info & Performance Telemetry (C27)
- Device Registration & Authorization (C28)
- appRelease.js
- Override
- test-roster.js
- CallLogItem
- ChatApiClient.java
- RecycleBinAdapter
- chat.js
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- ContactsActivity
- .onItemSelected
- com.zez.app.model.ChatConversation
- PhoneHelper
- android.net.Uri
- .onResponse
- chat-extreme-test-suite.js
- AlarmHelper.java
- AlarmRingingService
- callSync.js
- ChatNotificationManager
- callRead.js
- .switchTab
- .onCreate
- Override
- ChatLocalDbHelper
- benchmark
- PageVH
- ChatActivity
- .onReceive
- .loadAlarmsData
- .setData
- .onSuccess

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
- `ChatActivity` --references--> `ChatMessageAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ChatMessageAdapter.java
- `ChatActivity` --references--> `ChatLocalDbHelper`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/ChatLocalDbHelper.java
- `ContactsActivity` --implements--> `OnContactClickListener`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ContactsActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ContactsAdapter.java
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `MainActivity` --references--> `ConversationsAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java

## Import Cycles
- None detected.

## Communities (87 total, 58 thin omitted)

### Community 0 - "Override"
Cohesion: 0.11
Nodes (5): android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback

### Community 1 - "AlarmItem"
Cohesion: 0.13
Nodes (6): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 3 - "ImageViewerActivity"
Cohesion: 0.05
Nodes (13): FilmstripAdapter, OnFilmstripClickListener, GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, MediaPagerAdapter, OnTapListener, AlbumDetailActivity (+5 more)

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.07
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - "ChatCallback"
Cohesion: 0.09
Nodes (21): ChatApiClient, Callback, Callback, Callback, ChatCallback<MessagesResult>, Callback, Callback, Callback (+13 more)

### Community 8 - "android.view.View"
Cohesion: 0.14
Nodes (10): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder, FilmstripVH (+2 more)

### Community 11 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.10
Nodes (4): crypto, hashPassword(), { Pool }, verifyPassword()

### Community 12 - "android.content.Context"
Cohesion: 0.07
Nodes (9): CallLogHelper, ChatPreferencesHelper, DeviceHelper, FavoritesHelper, MediaHelper, PreferencesHelper, RecycleBinHelper, TypeToken<List<RecycledItem>> (+1 more)

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 15 - "package.json"
Cohesion: 0.07
Nodes (36): cors, dotenv, multer, @noble/ciphers, pg, @types/cors, @types/express, @types/multer (+28 more)

### Community 16 - "index.d.ts"
Cohesion: 0.09
Nodes (20): express, express@^5.1.0, crypto, express, router, AdminCallsQuery, AdminChatExportFormat, AdminUserRecord (+12 more)

### Community 17 - "ChatMessageAdapter"
Cohesion: 0.11
Nodes (4): ChatMessageAdapter, ChatRowItem, OnMessageActionListener, ChatMessageAdapter.OnMessageActionListener

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.07
Nodes (17): fs, path, fs, path, fs, path, fs, path (+9 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.13
Nodes (14): closeDatabase(), initializeDatabase(), router, app, cors, express, fs, path (+6 more)

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

### Community 30 - "appRelease.js"
Cohesion: 0.20
Nodes (8): downloadsDir, express, fs, multer, path, router, storage, upload

### Community 31 - "Override"
Cohesion: 0.09
Nodes (8): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>

### Community 32 - "test-roster.js"
Cohesion: 0.13
Nodes (15): http, run(), http, run(), http, request(), run(), token (+7 more)

### Community 33 - "CallLogItem"
Cohesion: 0.22
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 35 - "RecycleBinAdapter"
Cohesion: 0.27
Nodes (3): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter

### Community 36 - "chat.js"
Cohesion: 0.15
Nodes (10): avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir, multer, path (+2 more)

### Community 58 - "android.net.Uri"
Cohesion: 0.17
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 60 - "chat-extreme-test-suite.js"
Cohesion: 0.24
Nodes (7): get(), http, post(), run(), http, makeRequest(), runChatTestSuite()

### Community 65 - "callSync.js"
Cohesion: 0.31
Nodes (6): crypto, express, getTimestamp(), httpError(), normalizeCall(), router

### Community 67 - "callRead.js"
Cohesion: 0.09
Nodes (22): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+14 more)

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 313 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **58 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `Override`, `AlarmItem`, `ImageViewerActivity`, `Hardware Info & Performance Telemetry (C6)`, `android.view.View`, `android.content.Context`, `ImageViewerActivity.java`, `MainActivity.java`, `AppUpdateHelper`, `Override`, `ContactsActivity`, `.onItemSelected`, `com.zez.app.model.ChatConversation`, `android.net.Uri`, `AlarmHelper.java`, `.bind`, `ChatNotificationManager`, `.switchTab`, `.onCreate`, `.loadAlarmsData`?**
  _High betweenness centrality (0.103) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `Override` be split into smaller, more focused modules?**
  _Cohesion score 0.11076923076923077 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `ChatNotificationHelper.java`, `ChatMessage`, `ChatCallback`, `android.view.View`, `ChatLocalDbHelper`, `.drainPendingQueue`, `ChatNotificationManager.java`, `ChatMessageAdapter`, `ContactsActivity`, `.bind`, `MainActivity.java`, `android.net.Uri`, `Device Registration & Authorization (C28)`, `Override`?**
  _High betweenness centrality (0.043) - this node is a cross-community bridge._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.12857142857142856 - nodes in this community are weakly interconnected._
- **Why does `ChatMessage` connect `ChatMessage` to `ChatApiClient.java`, `ChatCallback`, `ChatLocalDbHelper`, `serializable`, `.drainPendingQueue`, `ChatMessageAdapter`, `.bind`?**
  _High betweenness centrality (0.038) - this node is a cross-community bridge._
- **Should `ImageViewerActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.050101010101010104 - nodes in this community are weakly interconnected._