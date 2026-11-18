# Graph Report - Call Log Mannager  (2026-11-18)

## Corpus Check
- 81 files · ~61,876 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 93 file(s) not represented in the graph (top: .xml 84, .properties 5, (none) 2)
- WARNING: 93 of 187 files seen (50%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1188 nodes · 2943 edges · 76 communities (32 shown, 44 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 195 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `475cdfcb`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ImageViewerActivity
- android.content.Context
- ChatConversation
- AlarmHelper
- CallLogItem
- Contacts & User Discovery (C5)
- Hardware Info & Performance Telemetry (C6)
- .postError
- .exitSelectMode
- MainActivity
- Override
- android.net.Uri
- AlarmRingingService.java
- compilerOptions
- ChatUser
- package.json
- index.d.ts
- MainActivity.java
- Network APIs & Background Sync (C19)
- chat.js
- Data Encryption & Token Auth (C21)
- Network APIs & Background Sync (C22)
- RecycleBinAdapter
- AppUpdateHelper
- Image Viewing & Editing Tools (C25)
- admin.js
- Hardware Info & Performance Telemetry (C27)
- Device Registration & Authorization (C28)
- callRead.js
- Backend Admin Routes & APIs (C30)
- AlarmHelper.java
- GalleryAdapter
- Network APIs & Background Sync (C35)
- .triggerManualSync
- AlarmFiringActivity
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- server.js
- .loadGalleryData
- AlarmAdapter
- .showChatProfileDialog
- MediaItem
- AlarmItem
- Adapter
- dependencies
- MediaPagerAdapter
- log
- callSync.js
- ref_node_crypto
- Override
- .onResponse
- scripts
- .onReceive
- engines
- benchmark

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 86 edges
2. `ChatMessage` - 47 edges
3. `ViewHolder` - 31 edges
4. `ChatActivity` - 31 edges
5. `ChatApiClient` - 29 edges
6. `ImageViewerActivity` - 28 edges
7. `ChatCallback` - 27 edges
8. `GalleryAdapter` - 26 edges
9. `MediaItem` - 26 edges
10. `AlarmItem` - 26 edges

## Surprising Connections (you probably didn't know these)
- `decodeRecord()` --calls--> `decryptStoredPayload()`  [EXTRACTED]
  Zez-Backend/src/routes/admin.js → Zez-Backend/src/crypto.js
- `GalleryAdapter` --references--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/GalleryAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java
- `GalleryAdapter` --references--> `GalleryListItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/GalleryAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/model/GalleryListItem.java
- `GalleryAdapter` --references--> `MediaItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/GalleryAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/model/MediaItem.java
- `PageVH` --inherits--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/MediaPagerAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java

## Import Cycles
- None detected.

## Communities (76 total, 44 thin omitted)

### Community 2 - "ChatConversation"
Cohesion: 0.10
Nodes (4): ConversationsAdapter, OnConversationClickListener, ChatConversation, Callback

### Community 4 - "CallLogItem"
Cohesion: 0.18
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 5 - "Contacts & User Discovery (C5)"
Cohesion: 0.08
Nodes (5): ContactsAdapter, DisplayItem, OnContactClickListener, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.08
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - ".postError"
Cohesion: 0.06
Nodes (21): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient, Callback, ChatCallback<MessagesResult>, Callback (+13 more)

### Community 10 - "Override"
Cohesion: 0.09
Nodes (7): android.widget.AdapterView.OnItemSelectedListener, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, ChatApiClient.ChatCallback<String>

### Community 11 - "android.net.Uri"
Cohesion: 0.18
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 15 - "package.json"
Cohesion: 0.14
Nodes (18): @types/cors, @types/express, @types/multer, @types/node, @types/pg, typescript, description, devDependencies (+10 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "MainActivity.java"
Cohesion: 0.05
Nodes (11): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactViewHolder, HeaderViewHolder, ViewHolder, HeaderVH, MediaVH (+3 more)

### Community 20 - "chat.js"
Cohesion: 0.10
Nodes (13): fs, path, avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir (+5 more)

### Community 21 - "Data Encryption & Token Auth (C21)"
Cohesion: 0.24
Nodes (11): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+3 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.13
Nodes (11): closeDatabase(), crypto, hashPassword(), initializeDatabase(), { Pool }, app, startServer(), http (+3 more)

### Community 23 - "RecycleBinAdapter"
Cohesion: 0.19
Nodes (6): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 24 - "AppUpdateHelper"
Cohesion: 0.16
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "Image Viewing & Editing Tools (C25)"
Cohesion: 0.22
Nodes (3): ImageEditActivity, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.18
Nodes (3): verifyPassword(), crypto, express

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 29 - "callRead.js"
Cohesion: 0.24
Nodes (6): crypto, deviceAuth(), express, parseLimit(), parsePositiveInteger(), router

### Community 30 - "Backend Admin Routes & APIs (C30)"
Cohesion: 0.15
Nodes (15): adminAuth(), base64UrlDecode(), base64UrlEncode(), createAdminToken(), crypto, getSecret(), verifyAdminToken(), downloadsDir (+7 more)

### Community 35 - "Network APIs & Background Sync (C35)"
Cohesion: 0.47
Nodes (4): get(), http, post(), run()

### Community 36 - ".triggerManualSync"
Cohesion: 0.14
Nodes (3): ApiCallback, ApiClient, CallLogHelper

### Community 38 - "ChatMessage"
Cohesion: 0.07
Nodes (4): ChatMessageAdapter, ChatRowItem, ChatApiClient.ChatCallback<ChatMessage>, ChatMessage

### Community 51 - "server.js"
Cohesion: 0.14
Nodes (10): express, express@^5.1.0, router, crypto, express, router, cors, express (+2 more)

### Community 55 - "AlarmAdapter"
Cohesion: 0.24
Nodes (3): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener

### Community 61 - "Adapter"
Cohesion: 0.31
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 64 - "dependencies"
Cohesion: 0.18
Nodes (11): cors, dotenv, multer, @noble/ciphers, pg, dependencies, cors@^2.8.5, dotenv@^18.0.7 (+3 more)

### Community 65 - "MediaPagerAdapter"
Cohesion: 0.33
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 67 - "callSync.js"
Cohesion: 0.36
Nodes (6): crypto, express, getTimestamp(), httpError(), normalizeCall(), router

### Community 68 - "ref_node_crypto"
Cohesion: 0.32
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 71 - "scripts"
Cohesion: 0.40
Nodes (5): scripts, build, dev, start, type-check

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 272 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **44 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `ImageViewerActivity`, `android.content.Context`, `ChatConversation`, `.triggerManualSync`, `MediaItem`, `Hardware Info & Performance Telemetry (C6)`, `.postError`, `.exitSelectMode`, `Override`, `MainActivity.java`, `.loadGalleryData`, `AlarmAdapter`, `AppUpdateHelper`, `.showChatProfileDialog`, `AlarmItem`?**
  _High betweenness centrality (0.110) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `ChatConversation` be split into smaller, more focused modules?**
  _Cohesion score 0.0957983193277311 - nodes in this community are weakly interconnected._
- **Why does `ChatMessage` connect `ChatMessage` to `MainActivity.java`, `ChatApiClient.java`, `ChatUser`, `.postError`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Should `Contacts & User Discovery (C5)` be split into smaller, more focused modules?**
  _Cohesion score 0.0753045404208195 - nodes in this community are weakly interconnected._
- **Why does `ZoomImageView` connect `Hardware Info & Performance Telemetry (C6)` to `MediaPagerAdapter`, `MainActivity.java`?**
  _High betweenness centrality (0.033) - this node is a cross-community bridge._
- **Should `Hardware Info & Performance Telemetry (C6)` be split into smaller, more focused modules?**
  _Cohesion score 0.07529411764705882 - nodes in this community are weakly interconnected._