# Graph Report - Call Log Mannager  (2026-11-18)

## Corpus Check
- 93 files · ~70,586 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 93 file(s) not represented in the graph (top: .xml 84, .properties 5, (none) 2)
- WARNING: 93 of 199 files seen (47%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1246 nodes · 3048 edges · 66 communities (26 shown, 40 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 195 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `299722d8`
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
- ChatUser
- MainActivity
- Override
- .onCreate
- AlarmRingingService.java
- compilerOptions
- ChatActivity.java
- package.json
- index.d.ts
- Network APIs & Background Sync (C19)
- chat.js
- Data Encryption & Token Auth (C21)
- Network APIs & Background Sync (C22)
- AppUpdateHelper
- Image Viewing & Editing Tools (C25)
- admin.js
- Hardware Info & Performance Telemetry (C27)
- Device Registration & Authorization (C28)
- android.view.View
- Backend Admin Routes & APIs (C30)
- ImageViewerActivity.java
- test-roster.js
- ChatNotificationHelper
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- server.js
- android.net.Uri
- android.text.TextWatcher
- log
- db.js
- ref_node_crypto
- .onResponse
- DeviceHelper.java
- benchmark

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 87 edges
2. `ChatMessage` - 34 edges
3. `ChatActivity` - 32 edges
4. `ViewHolder` - 31 edges
5. `ImageViewerActivity` - 28 edges
6. `ChatApiClient` - 27 edges
7. `ChatCallback` - 27 edges
8. `GalleryAdapter` - 26 edges
9. `MediaItem` - 26 edges
10. `AlarmItem` - 26 edges

## Surprising Connections (you probably didn't know these)
- `decodeRecord()` --calls--> `decryptStoredPayload()`  [EXTRACTED]
  Zez-Backend/src/routes/admin.js → Zez-Backend/src/crypto.js
- `ChatActivity` --references--> `ChatMessageAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ChatMessageAdapter.java
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `MainActivity` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `MainActivity` --references--> `TelemetryGraphView`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/TelemetryGraphView.java

## Import Cycles
- None detected.

## Communities (66 total, 40 thin omitted)

### Community 0 - "ImageViewerActivity"
Cohesion: 0.06
Nodes (8): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, GalleryAdapter.Listener, ImageViewerActivity, ViewPager2.OnPageChangeCallback, GalleryListItem, MediaItem

### Community 1 - "android.content.Context"
Cohesion: 0.06
Nodes (11): android.widget.AdapterView.OnItemSelectedListener, ApiCallback, ApiClient, ChatPreferencesHelper, DeviceHelper, FavoritesHelper, MediaHelper, PreferencesHelper (+3 more)

### Community 2 - "ChatConversation"
Cohesion: 0.10
Nodes (4): ConversationsAdapter, OnConversationClickListener, ChatConversation, Callback

### Community 3 - "AlarmItem"
Cohesion: 0.11
Nodes (7): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 5 - "Contacts & User Discovery (C5)"
Cohesion: 0.09
Nodes (5): DisplayItem, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.08
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - ".postError"
Cohesion: 0.07
Nodes (21): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient, Callback, ChatCallback<MessagesResult>, Callback (+13 more)

### Community 10 - "Override"
Cohesion: 0.11
Nodes (6): com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, Runnable, ChatApiClient.ChatCallback<String>

### Community 12 - "AlarmRingingService.java"
Cohesion: 0.07
Nodes (3): AlarmFiringActivity, AlarmReceiver, AlarmRingingService

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 15 - "package.json"
Cohesion: 0.07
Nodes (36): cors, dotenv, multer, @noble/ciphers, pg, @types/cors, @types/express, @types/multer (+28 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 20 - "chat.js"
Cohesion: 0.07
Nodes (18): fs, path, fs, path, fs, os, path, avatarStorage (+10 more)

### Community 21 - "Data Encryption & Token Auth (C21)"
Cohesion: 0.24
Nodes (11): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+3 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.16
Nodes (9): closeDatabase(), hashPassword(), initializeDatabase(), app, startServer(), http, path, run() (+1 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.16
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "Image Viewing & Editing Tools (C25)"
Cohesion: 0.22
Nodes (3): ImageEditActivity, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.08
Nodes (13): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+5 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 29 - "android.view.View"
Cohesion: 0.06
Nodes (20): AlbumsAdapter, AlbumVH, OnAlbumClickListener, CallLogAdapter, CallViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder (+12 more)

### Community 30 - "Backend Admin Routes & APIs (C30)"
Cohesion: 0.15
Nodes (15): adminAuth(), base64UrlDecode(), base64UrlEncode(), createAdminToken(), crypto, getSecret(), verifyAdminToken(), downloadsDir (+7 more)

### Community 32 - "test-roster.js"
Cohesion: 0.13
Nodes (12): http, run(), http, run(), http, request(), run(), token (+4 more)

### Community 38 - "ChatMessage"
Cohesion: 0.06
Nodes (7): ChatMessageAdapter, ChatRowItem, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ChatApiClient.ChatCallback<ChatMessage>, ChatMessage

### Community 51 - "server.js"
Cohesion: 0.14
Nodes (10): express, express@^5.1.0, router, crypto, express, router, cors, express (+2 more)

### Community 67 - "db.js"
Cohesion: 0.12
Nodes (14): crypto, { Pool }, crypto, deviceAuth(), express, parseLimit(), parsePositiveInteger(), router (+6 more)

### Community 68 - "ref_node_crypto"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 298 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **40 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `ImageViewerActivity`, `android.content.Context`, `ChatConversation`, `AlarmItem`, `ChatNotificationHelper`, `ChatMessage`, `.postError`, `Hardware Info & Performance Telemetry (C6)`, `Override`, `.onCreate`, `AlarmRingingService.java`, `ChatActivity.java`, `MainActivity.java`, `ImageEditActivity.java`, `AppUpdateHelper`, `android.text.TextWatcher`, `android.view.View`, `ImageViewerActivity.java`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `ImageViewerActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.06112616681722373 - nodes in this community are weakly interconnected._
- **Why does `ViewHolder` connect `android.view.View` to `ImageViewerActivity`, `ChatConversation`, `AlarmItem`, `ChatMessage`?**
  _High betweenness centrality (0.026) - this node is a cross-community bridge._
- **Should `android.content.Context` be split into smaller, more focused modules?**
  _Cohesion score 0.06495589414595028 - nodes in this community are weakly interconnected._
- **Should `ChatConversation` be split into smaller, more focused modules?**
  _Cohesion score 0.10416666666666667 - nodes in this community are weakly interconnected._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.11219512195121951 - nodes in this community are weakly interconnected._