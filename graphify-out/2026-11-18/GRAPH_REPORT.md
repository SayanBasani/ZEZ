# Graph Report - Call Log Mannager  (2026-11-18)

## Corpus Check
- 88 files · ~66,069 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 93 file(s) not represented in the graph (top: .xml 84, .properties 5, (none) 2)
- WARNING: 93 of 194 files seen (48%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1209 nodes · 2977 edges · 75 communities (32 shown, 43 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 195 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `8c63b091`
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
- android.view.View
- MainActivity
- Override
- android.net.Uri
- AlarmRingingService.java
- compilerOptions
- ChatActivity.java
- package.json
- index.d.ts
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
- Backend Admin Routes & APIs (C30)
- ImageViewerActivity.java
- adminAuth.js
- Network APIs & Background Sync (C35)
- ContactsActivity
- AlarmFiringActivity
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- server.js
- AlarmViewHolder
- android.net.Uri
- android.text.TextWatcher
- .openHardwarePage
- Adapter
- dependencies
- MediaPagerAdapter
- log
- callSync.js
- ref_node_crypto
- .onResponse
- scripts
- android.content.Intent
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
- `AlbumsAdapter` --references--> `AlbumItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/AlbumsAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlbumItem.java
- `AlarmViewHolder` --inherits--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java
- `CallViewHolder` --inherits--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/CallLogAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java
- `ChatMessageAdapter` --references--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/ChatMessageAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java

## Import Cycles
- None detected.

## Communities (75 total, 43 thin omitted)

### Community 0 - "ImageViewerActivity"
Cohesion: 0.06
Nodes (9): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener, ImageViewerActivity, ViewPager2.OnPageChangeCallback, GalleryListItem (+1 more)

### Community 1 - "android.content.Context"
Cohesion: 0.07
Nodes (11): android.widget.AdapterView.OnItemSelectedListener, ApiCallback, ApiClient, ChatPreferencesHelper, DeviceHelper, FavoritesHelper, MediaHelper, PreferencesHelper (+3 more)

### Community 2 - "ChatConversation"
Cohesion: 0.11
Nodes (3): ConversationsAdapter, OnConversationClickListener, ChatConversation

### Community 3 - "AlarmItem"
Cohesion: 0.13
Nodes (6): AlarmAdapter, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 4 - "CallLogItem"
Cohesion: 0.18
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 5 - "Contacts & User Discovery (C5)"
Cohesion: 0.06
Nodes (4): DisplayItem, OnContactClickListener, ChatContactItem, ChatUser

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.08
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - ".postError"
Cohesion: 0.06
Nodes (23): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient, Callback, Callback (+15 more)

### Community 8 - "android.view.View"
Cohesion: 0.14
Nodes (10): AlbumVH, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder (+2 more)

### Community 10 - "Override"
Cohesion: 0.13
Nodes (5): com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, ChatApiClient.ChatCallback<String>

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 15 - "package.json"
Cohesion: 0.14
Nodes (18): @types/cors, @types/express, @types/multer, @types/node, @types/pg, typescript, description, devDependencies (+10 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 20 - "chat.js"
Cohesion: 0.11
Nodes (13): fs, path, avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir (+5 more)

### Community 21 - "Data Encryption & Token Auth (C21)"
Cohesion: 0.13
Nodes (17): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+9 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.13
Nodes (11): closeDatabase(), crypto, hashPassword(), initializeDatabase(), { Pool }, app, startServer(), http (+3 more)

### Community 23 - "RecycleBinAdapter"
Cohesion: 0.27
Nodes (3): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter

### Community 24 - "AppUpdateHelper"
Cohesion: 0.16
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "Image Viewing & Editing Tools (C25)"
Cohesion: 0.22
Nodes (3): ImageEditActivity, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.15
Nodes (3): verifyPassword(), crypto, express

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 30 - "Backend Admin Routes & APIs (C30)"
Cohesion: 0.20
Nodes (8): downloadsDir, express, fs, multer, path, router, storage, upload

### Community 32 - "adminAuth.js"
Cohesion: 0.17
Nodes (11): http, run(), http, run(), adminAuth(), base64UrlDecode(), base64UrlEncode(), createAdminToken() (+3 more)

### Community 35 - "Network APIs & Background Sync (C35)"
Cohesion: 0.60
Nodes (4): get(), http, post(), run()

### Community 38 - "ChatMessage"
Cohesion: 0.08
Nodes (3): ChatMessageAdapter, ChatRowItem, ChatMessage

### Community 51 - "server.js"
Cohesion: 0.14
Nodes (10): express, express@^5.1.0, router, crypto, express, router, cors, express (+2 more)

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
  These have ≤1 connection - possible missing edges. (Counts symbols only; 281 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **43 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `ImageViewerActivity`, `android.content.Context`, `ChatConversation`, `AlarmItem`, `AlarmFiringActivity`, `Hardware Info & Performance Telemetry (C6)`, `.postError`, `android.view.View`, `Override`, `ChatActivity.java`, `MainActivity.java`, `AppUpdateHelper`, `android.text.TextWatcher`, `.openHardwarePage`?**
  _High betweenness centrality (0.108) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `ImageViewerActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.05851619644723093 - nodes in this community are weakly interconnected._
- **Why does `ChatMessage` connect `ChatMessage` to `ChatApiClient.java`, `Contacts & User Discovery (C5)`, `.postError`, `ChatActivity.java`, `ChatMessageAdapter.java`?**
  _High betweenness centrality (0.039) - this node is a cross-community bridge._
- **Should `android.content.Context` be split into smaller, more focused modules?**
  _Cohesion score 0.06538987688098495 - nodes in this community are weakly interconnected._
- **Why does `MediaItem` connect `ImageViewerActivity` to `MediaHelper.java`, `android.content.Context`, `android.net.Uri`, `ChatMessageAdapter.java`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **Should `ChatConversation` be split into smaller, more focused modules?**
  _Cohesion score 0.10887096774193548 - nodes in this community are weakly interconnected._