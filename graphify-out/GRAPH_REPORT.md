# Graph Report - Call Log Mannager  (2026-11-18)

## Corpus Check
- 73 files · ~56,575 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 88 file(s) not represented in the graph (top: .xml 79, .properties 5, (none) 2)
- WARNING: 88 of 174 files seen (51%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1062 nodes · 2741 edges · 54 communities (21 shown, 33 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 183 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f6a2a80e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Gallery & Media Presentation (C0)
- Gallery & Media Presentation (C1)
- Alarm Scheduling & Triggering (C3)
- CallLogItem
- Contacts & User Discovery (C5)
- Hardware Info & Performance Telemetry (C6)
- .postError
- MainActivity
- Gallery & Media Presentation (C11)
- AlarmRingingService.java
- ImageEditActivity.java
- Data Encryption & Token Auth (C15)
- Alarm Scheduling & Triggering (C16)
- android.view.View
- Network APIs & Background Sync (C19)
- chat.js
- Data Encryption & Token Auth (C21)
- Network APIs & Background Sync (C22)
- .addMessage
- Override
- Image Viewing & Editing Tools (C25)
- admin.js
- Hardware Info & Performance Telemetry (C27)
- Device Registration & Authorization (C28)
- Network APIs & Background Sync (C29)
- Backend Admin Routes & APIs (C30)
- AlarmRingingService
- ChatActivity
- Network APIs & Background Sync (C35)
- Override
- .sendMessage
- ChatMessage
- .onReceive
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 84 edges
2. `ChatMessage` - 47 edges
3. `MediaItem` - 39 edges
4. `ChatActivity` - 31 edges
5. `ChatApiClient` - 31 edges
6. `ViewHolder` - 30 edges
7. `GalleryAdapter` - 29 edges
8. `ChatCallback` - 27 edges
9. `ChatContactItem` - 27 edges
10. `AlarmItem` - 26 edges

## Surprising Connections (you probably didn't know these)
- `decodeRecord()` --calls--> `decryptStoredPayload()`  [EXTRACTED]
  Zez-Backend/src/routes/admin.js → Zez-Backend/src/crypto.js
- `ChatActivity` --references--> `ChatMessage`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/ChatMessage.java
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `MainActivity` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `MainActivity` --references--> `TelemetryGraphView`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/TelemetryGraphView.java

## Import Cycles
- None detected.

## Communities (54 total, 33 thin omitted)

### Community 0 - "Gallery & Media Presentation (C0)"
Cohesion: 0.06
Nodes (9): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener, ImageViewerActivity, ViewPager2.OnPageChangeCallback, GalleryListItem (+1 more)

### Community 1 - "Gallery & Media Presentation (C1)"
Cohesion: 0.06
Nodes (14): android.widget.AdapterView.OnItemSelectedListener, ApiCallback, ApiClient, CallLogHelper, ChatApiClient, Callback, ChatCallback, ContactSyncResult (+6 more)

### Community 3 - "Alarm Scheduling & Triggering (C3)"
Cohesion: 0.13
Nodes (6): AlarmAdapter, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 5 - "Contacts & User Discovery (C5)"
Cohesion: 0.09
Nodes (5): DisplayItem, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.10
Nodes (3): TelemetryGraphView, ScaleListener, ZoomImageView

### Community 7 - ".postError"
Cohesion: 0.07
Nodes (14): ChatConversation, okhttp3.Callback, Callback, Callback, ChatCallback<MessagesResult>, Callback, Callback, Callback (+6 more)

### Community 11 - "Gallery & Media Presentation (C11)"
Cohesion: 0.09
Nodes (10): AlbumsAdapter, OnAlbumClickListener, BinViewHolder, OnRestoreClickListener, RecycleBinAdapter, AlbumItem, MediaHelper, RecycleBinHelper (+2 more)

### Community 15 - "Data Encryption & Token Auth (C15)"
Cohesion: 0.10
Nodes (23): cors, dotenv, express, multer, @noble/ciphers, pg, dependencies, cors@^2.8.5 (+15 more)

### Community 17 - "android.view.View"
Cohesion: 0.06
Nodes (19): AlarmViewHolder, AlbumVH, CallLogAdapter, CallViewHolder, ChatMessageAdapter, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder (+11 more)

### Community 20 - "chat.js"
Cohesion: 0.14
Nodes (10): avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir, multer, path (+2 more)

### Community 21 - "Data Encryption & Token Auth (C21)"
Cohesion: 0.16
Nodes (14): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+6 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.12
Nodes (15): closeDatabase(), crypto, hashPassword(), initializeDatabase(), router, app, cors, express (+7 more)

### Community 24 - "Override"
Cohesion: 0.06
Nodes (11): android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, ChatApiClient.ChatCallback<String>, AppUpdateHelper (+3 more)

### Community 25 - "Image Viewing & Editing Tools (C25)"
Cohesion: 0.22
Nodes (3): ImageEditActivity, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.18
Nodes (5): verifyPassword(), crypto, decodeRecord(), express, router

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 28 - "Device Registration & Authorization (C28)"
Cohesion: 0.15
Nodes (3): CallLogListener, CallLogObserver, ZezApplication

### Community 29 - "Network APIs & Background Sync (C29)"
Cohesion: 0.17
Nodes (12): { Pool }, crypto, deviceAuth(), crypto, express, getTimestamp(), httpError(), normalizeCall() (+4 more)

### Community 30 - "Backend Admin Routes & APIs (C30)"
Cohesion: 0.15
Nodes (15): adminAuth(), base64UrlDecode(), base64UrlEncode(), createAdminToken(), crypto, getSecret(), verifyAdminToken(), downloadsDir (+7 more)

### Community 35 - "Network APIs & Background Sync (C35)"
Cohesion: 0.47
Nodes (4): get(), http, post(), run()

### Community 36 - "Override"
Cohesion: 0.20
Nodes (4): ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 212 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **33 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `Gallery & Media Presentation (C0)`, `Gallery & Media Presentation (C1)`, `Alarm Scheduling & Triggering (C3)`, `Hardware Info & Performance Telemetry (C6)`, `.postError`, `MainActivity.java`, `Gallery & Media Presentation (C11)`, `ImageEditActivity.java`, `android.view.View`, `Override`, `ChatActivity.java`?**
  _High betweenness centrality (0.103) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `Gallery & Media Presentation (C0)` be split into smaller, more focused modules?**
  _Cohesion score 0.06329113924050633 - nodes in this community are weakly interconnected._
- **Why does `ChatMessage` connect `ChatMessage` to `ChatActivity`, `ChatMessage`, `ChatApiClient.java`, `Gallery & Media Presentation (C1)`, `.sendMessage`, `.postError`, `Alarm Scheduling & Triggering (C16)`, `android.view.View`, `.addMessage`, `ChatActivity.java`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Should `Gallery & Media Presentation (C1)` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._
- **Should `Alarm Scheduling & Triggering (C3)` be split into smaller, more focused modules?**
  _Cohesion score 0.12612612612612611 - nodes in this community are weakly interconnected._
- **Should `Contacts & User Discovery (C5)` be split into smaller, more focused modules?**
  _Cohesion score 0.08637873754152824 - nodes in this community are weakly interconnected._