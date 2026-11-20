# Graph Report - Call Log Mannager  (2026-11-20)

## Corpus Check
- 98 files · ~73,693 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 93 file(s) not represented in the graph (top: .xml 84, .properties 5, (none) 2)
- WARNING: 93 of 204 files seen (46%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1292 nodes · 3158 edges · 71 communities (33 shown, 38 thin omitted)
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
- CallLogItem
- Contacts & User Discovery (C5)
- Hardware Info & Performance Telemetry (C6)
- .postError
- android.widget.TextView
- MainActivity
- .showChatProfileDialog
- d_call_log_mannager_zez_backend_src_db_pool
- AlarmHelper.java
- compilerOptions
- MainActivity.java
- package.json
- index.d.ts
- .loadGalleryData
- Override
- Network APIs & Background Sync (C19)
- d_call_log_mannager_zez_backend_src_db_pool
- AlarmRingingService.java
- Network APIs & Background Sync (C22)
- ContactsActivity
- AppUpdateHelper
- ImageEditActivity
- admin.js
- Hardware Info & Performance Telemetry (C27)
- Device Registration & Authorization (C28)
- Backend Admin Routes & APIs (C30)
- test-roster.js
- Adapter
- ChatUser
- chat.js
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- server.js
- .getPrefs
- ChatNotificationManager.java
- PhoneHelper
- android.net.Uri
- .initThemeSpinner
- ApiCallback
- AlarmFiringActivity
- log
- db.js
- ref_node_crypto
- benchmark

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 91 edges
2. `ChatActivity` - 33 edges
3. `ViewHolder` - 31 edges
4. `ChatMessage` - 29 edges
5. `ImageViewerActivity` - 28 edges
6. `ChatCallback` - 28 edges
7. `ChatApiClient` - 27 edges
8. `GalleryAdapter` - 26 edges
9. `MediaItem` - 26 edges
10. `AlarmItem` - 26 edges

## Surprising Connections (you probably didn't know these)
- `ContactsActivity` --implements--> `OnContactClickListener`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ContactsActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ContactsAdapter.java
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `MainActivity` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `MainActivity` --references--> `TelemetryGraphView`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/TelemetryGraphView.java
- `GalleryAdapter` --references--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/GalleryAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java

## Import Cycles
- None detected.

## Communities (71 total, 38 thin omitted)

### Community 0 - "ImageViewerActivity"
Cohesion: 0.06
Nodes (9): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener, ImageViewerActivity, ViewPager2.OnPageChangeCallback, GalleryListItem (+1 more)

### Community 1 - "android.content.Context"
Cohesion: 0.15
Nodes (4): ApiClient, CallLogHelper, DeviceHelper, PreferencesHelper

### Community 2 - "ChatConversation"
Cohesion: 0.09
Nodes (5): ConversationsAdapter, OnConversationClickListener, ChatConversation, Callback, ChatApiClient.ChatCallback<List<ChatConversation>>

### Community 3 - "AlarmItem"
Cohesion: 0.11
Nodes (7): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 4 - "CallLogItem"
Cohesion: 0.18
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 5 - "Contacts & User Discovery (C5)"
Cohesion: 0.07
Nodes (5): ContactsAdapter, DisplayItem, OnContactClickListener, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.08
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - ".postError"
Cohesion: 0.06
Nodes (22): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, okhttp3.Callback, ChatApiClient, Callback, ChatCallback<MessagesResult> (+14 more)

### Community 8 - "android.widget.TextView"
Cohesion: 0.16
Nodes (6): DateHeaderViewHolder, ContactViewHolder, HeaderViewHolder, ViewHolder, HeaderVH, MediaVH

### Community 11 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.12
Nodes (3): http, makeRequest(), runChatTestSuite()

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - "MainActivity.java"
Cohesion: 0.13
Nodes (3): IncomingViewHolder, OutgoingViewHolder, ChatActivity

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
Cohesion: 0.09
Nodes (7): android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, Runnable, ChatApiClient.ChatCallback<String>

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.10
Nodes (11): fs, path, fs, path, fs, path, fs, path (+3 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.12
Nodes (12): closeDatabase(), crypto, hashPassword(), initializeDatabase(), { Pool }, verifyPassword(), app, startServer() (+4 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.16
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

### Community 28 - "Device Registration & Authorization (C28)"
Cohesion: 0.19
Nodes (3): CallLogListener, CallLogObserver, CallStateReceiver

### Community 30 - "Backend Admin Routes & APIs (C30)"
Cohesion: 0.20
Nodes (8): downloadsDir, express, fs, multer, path, router, storage, upload

### Community 32 - "test-roster.js"
Cohesion: 0.11
Nodes (19): http, run(), http, run(), http, request(), run(), token (+11 more)

### Community 33 - "Adapter"
Cohesion: 0.11
Nodes (9): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener, MediaPagerAdapter, OnTapListener, PageVH, BinViewHolder, OnRestoreClickListener (+1 more)

### Community 36 - "chat.js"
Cohesion: 0.14
Nodes (11): avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir, multer, path (+3 more)

### Community 38 - "ChatMessage"
Cohesion: 0.06
Nodes (5): ChatMessageAdapter, ChatRowItem, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatMessage>, ChatMessage

### Community 51 - "server.js"
Cohesion: 0.14
Nodes (10): express, express@^5.1.0, router, crypto, express, router, cors, express (+2 more)

### Community 58 - "android.net.Uri"
Cohesion: 0.17
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 66 - "log"
Cohesion: 0.17
Nodes (3): BootReceiver, ChatNotificationHelper, ZezApplication

### Community 67 - "db.js"
Cohesion: 0.10
Nodes (23): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+15 more)

### Community 68 - "ref_node_crypto"
Cohesion: 0.32
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 312 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **38 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `ImageViewerActivity`, `android.content.Context`, `ChatConversation`, `AlarmItem`, `Hardware Info & Performance Telemetry (C6)`, `.postError`, `android.widget.TextView`, `.showChatProfileDialog`, `AlarmHelper.java`, `MainActivity.java`, `.loadGalleryData`, `Override`, `ContactsActivity`, `AppUpdateHelper`, `list`, `ImageViewerActivity.java`, `android.net.Uri`, `.initThemeSpinner`, `log`?**
  _High betweenness centrality (0.105) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `ImageViewerActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.05745658835546476 - nodes in this community are weakly interconnected._
- **Why does `MediaItem` connect `ImageViewerActivity` to `.loadGalleryData`, `android.net.Uri`, `list`, `ImageViewerActivity.java`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Should `android.content.Context` be split into smaller, more focused modules?**
  _Cohesion score 0.14615384615384616 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `MainActivity.java` to `ChatMessage`, `.postError`, `android.widget.TextView`, `AlarmRingingService.java`, `ChatNotificationManager.java`, `android.net.Uri`, `Device Registration & Authorization (C28)`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Should `ChatConversation` be split into smaller, more focused modules?**
  _Cohesion score 0.08819345661450925 - nodes in this community are weakly interconnected._