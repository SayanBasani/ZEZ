# Graph Report - Call Log Mannager  (2026-11-19)

## Corpus Check
- 96 files · ~72,565 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 93 file(s) not represented in the graph (top: .xml 84, .properties 5, (none) 2)
- WARNING: 93 of 202 files seen (46%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1280 nodes · 3139 edges · 71 communities (29 shown, 42 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 201 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `84464234`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ImageViewerActivity
- android.content.Context
- ChatConversation
- AlarmItem
- CallLogItem
- Contacts & User Discovery (C5)
- Hardware Info & Performance Telemetry (C6)
- .postError
- android.widget.TextView
- MainActivity
- AlarmHelper
- .onReceive
- AlarmHelper.java
- compilerOptions
- ChatActivity.java
- package.json
- index.d.ts
- Network APIs & Background Sync (C19)
- d_call_log_mannager_zez_backend_src_db_pool
- Network APIs & Background Sync (C22)
- ContactsActivity
- Override
- ImageEditActivity.java
- admin.js
- Hardware Info & Performance Telemetry (C27)
- Device Registration & Authorization (C28)
- Backend Admin Routes & APIs (C30)
- test-roster.js
- RecycleBinAdapter
- com.zez.app.model.ChatConversation
- chat.js
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- server.js
- AlarmRingingService
- ChatNotificationManager.java
- android.view.View
- android.net.Uri
- FilmstripAdapter
- MediaPagerAdapter
- ConversationsAdapter
- AlarmFiringActivity
- log
- db.js
- ref_node_crypto
- .onResponse
- benchmark

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 88 edges
2. `ChatActivity` - 33 edges
3. `ViewHolder` - 31 edges
4. `ChatApiClient` - 29 edges
5. `ChatMessage` - 29 edges
6. `ChatCallback` - 28 edges
7. `ImageViewerActivity` - 28 edges
8. `GalleryAdapter` - 26 edges
9. `MediaItem` - 26 edges
10. `AlarmItem` - 26 edges

## Surprising Connections (you probably didn't know these)
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `MainActivity` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `MainActivity` --references--> `TelemetryGraphView`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/TelemetryGraphView.java
- `GalleryAdapter` --references--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/GalleryAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java
- `AlbumVH` --inherits--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/AlbumsAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java

## Import Cycles
- None detected.

## Communities (71 total, 42 thin omitted)

### Community 0 - "ImageViewerActivity"
Cohesion: 0.06
Nodes (9): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener, ImageViewerActivity, ViewPager2.OnPageChangeCallback, GalleryListItem (+1 more)

### Community 1 - "android.content.Context"
Cohesion: 0.06
Nodes (10): android.widget.AdapterView.OnItemSelectedListener, ApiCallback, ApiClient, ChatPreferencesHelper, DeviceHelper, FavoritesHelper, MediaHelper, PreferencesHelper (+2 more)

### Community 3 - "AlarmItem"
Cohesion: 0.19
Nodes (4): AlarmAdapter, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem

### Community 4 - "CallLogItem"
Cohesion: 0.21
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 5 - "Contacts & User Discovery (C5)"
Cohesion: 0.06
Nodes (4): DisplayItem, OnContactClickListener, ChatContactItem, ChatUser

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.13
Nodes (4): GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - ".postError"
Cohesion: 0.06
Nodes (23): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient, Callback, Callback (+15 more)

### Community 8 - "android.widget.TextView"
Cohesion: 0.22
Nodes (9): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder, HeaderVH (+1 more)

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 15 - "package.json"
Cohesion: 0.07
Nodes (34): cors, dotenv, @noble/ciphers, pg, @types/cors, @types/express, @types/multer, @types/node (+26 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 19 - "Network APIs & Background Sync (C19)"
Cohesion: 0.13
Nodes (3): CallStateReceiver, CallSyncWorker, CallLogHelper

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.07
Nodes (9): fs, path, fs, path, fs, path, fs, os (+1 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.12
Nodes (12): closeDatabase(), crypto, hashPassword(), initializeDatabase(), { Pool }, verifyPassword(), app, startServer() (+4 more)

### Community 24 - "Override"
Cohesion: 0.06
Nodes (12): android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, Runnable, ChatApiClient.ChatCallback<String> (+4 more)

### Community 25 - "ImageEditActivity.java"
Cohesion: 0.08
Nodes (4): ImageEditActivity, SeekBar.OnSeekBarChangeListener, ImageEditHelper, TelemetryGraphView

### Community 26 - "admin.js"
Cohesion: 0.09
Nodes (12): AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs, os (+4 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 30 - "Backend Admin Routes & APIs (C30)"
Cohesion: 0.17
Nodes (10): multer, multer@^2.5.0, downloadsDir, express, fs, multer, path, router (+2 more)

### Community 32 - "test-roster.js"
Cohesion: 0.09
Nodes (22): http, run(), http, run(), http, request(), run(), token (+14 more)

### Community 33 - "RecycleBinAdapter"
Cohesion: 0.24
Nodes (4): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter, RecycledItem

### Community 36 - "chat.js"
Cohesion: 0.14
Nodes (11): avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir, multer, path (+3 more)

### Community 38 - "ChatMessage"
Cohesion: 0.07
Nodes (4): ChatMessageAdapter, ChatRowItem, ChatApiClient.ChatCallback<ChatMessage>, ChatMessage

### Community 51 - "server.js"
Cohesion: 0.14
Nodes (10): express, express@^5.1.0, router, crypto, express, router, cors, express (+2 more)

### Community 58 - "android.net.Uri"
Cohesion: 0.18
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 59 - "FilmstripAdapter"
Cohesion: 0.36
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 60 - "MediaPagerAdapter"
Cohesion: 0.33
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 67 - "db.js"
Cohesion: 0.10
Nodes (23): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+15 more)

### Community 68 - "ref_node_crypto"
Cohesion: 0.32
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 307 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **42 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `ImageViewerActivity`, `android.content.Context`, `ChatConversation`, `com.zez.app.model.ChatConversation`, `AlarmItem`, `.postError`, `android.widget.TextView`, `AlarmHelper.java`, `ChatActivity.java`, `MainActivity.java`, `ContactsActivity`, `android.view.View`, `Override`, `ImageEditActivity.java`, `ImageViewerActivity.java`?**
  _High betweenness centrality (0.096) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `ImageViewerActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.059609730018711574 - nodes in this community are weakly interconnected._
- **Why does `ZoomImageView` connect `Hardware Info & Performance Telemetry (C6)` to `ImageEditActivity.java`, `MediaPagerAdapter`, `list`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **Should `android.content.Context` be split into smaller, more focused modules?**
  _Cohesion score 0.05924510272336359 - nodes in this community are weakly interconnected._
- **Why does `GalleryAdapter` connect `ImageViewerActivity` to `android.widget.TextView`, `android.view.View`, `CallLogItem`, `list`?**
  _High betweenness centrality (0.036) - this node is a cross-community bridge._
- **Should `ChatConversation` be split into smaller, more focused modules?**
  _Cohesion score 0.12615384615384614 - nodes in this community are weakly interconnected._