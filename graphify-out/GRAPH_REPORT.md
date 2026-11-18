# Graph Report - Call Log Mannager  (2026-11-18)

## Corpus Check
- 73 files · ~56,780 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 88 file(s) not represented in the graph (top: .xml 79, .properties 5, (none) 2)
- WARNING: 88 of 174 files seen (51%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1069 nodes · 2743 edges · 56 communities (24 shown, 32 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 183 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `6b756d96`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Gallery & Media Presentation (C0)
- Gallery & Media Presentation (C1)
- ChatConversation
- Alarm Scheduling & Triggering (C3)
- CallLogItem
- Contacts & User Discovery (C5)
- Hardware Info & Performance Telemetry (C6)
- .postError
- ContactsActivity
- MainActivity
- Override
- android.net.Uri
- AlarmRingingService.java
- Data Encryption & Token Auth (C15)
- ChatApiClient.java
- android.view.View
- android.text.TextWatcher
- Network APIs & Background Sync (C19)
- chat.js
- Data Encryption & Token Auth (C21)
- Network APIs & Background Sync (C22)
- com.zez.app.model.MediaItem
- AppUpdateHelper
- Image Viewing & Editing Tools (C25)
- admin.js
- Hardware Info & Performance Telemetry (C27)
- Device Registration & Authorization (C28)
- Network APIs & Background Sync (C29)
- Backend Admin Routes & APIs (C30)
- .openHardwarePage
- .onResponse
- Network APIs & Background Sync (C35)
- ChatMessage
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 86 edges
2. `ChatMessage` - 47 edges
3. `MediaItem` - 39 edges
4. `ChatActivity` - 31 edges
5. `ViewHolder` - 30 edges
6. `GalleryAdapter` - 29 edges
7. `ChatApiClient` - 29 edges
8. `ChatCallback` - 27 edges
9. `AlarmItem` - 26 edges
10. `ContactsActivity` - 23 edges

## Surprising Connections (you probably didn't know these)
- `decodeRecord()` --calls--> `decryptStoredPayload()`  [EXTRACTED]
  Zez-Backend/src/routes/admin.js → Zez-Backend/src/crypto.js
- `ContactsActivity` --implements--> `OnContactClickListener`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ContactsActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ContactsAdapter.java
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `MainActivity` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `MainActivity` --references--> `TelemetryGraphView`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/TelemetryGraphView.java

## Import Cycles
- None detected.

## Communities (56 total, 32 thin omitted)

### Community 0 - "Gallery & Media Presentation (C0)"
Cohesion: 0.06
Nodes (9): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener, ImageViewerActivity, ViewPager2.OnPageChangeCallback, GalleryListItem (+1 more)

### Community 1 - "Gallery & Media Presentation (C1)"
Cohesion: 0.07
Nodes (9): android.widget.AdapterView.OnItemSelectedListener, ChatPreferencesHelper, DeviceHelper, FavoritesHelper, MediaHelper, PreferencesHelper, RecycleBinHelper, TypeToken<List<RecycledItem>> (+1 more)

### Community 2 - "ChatConversation"
Cohesion: 0.10
Nodes (4): ConversationsAdapter, OnConversationClickListener, ChatConversation, Callback

### Community 3 - "Alarm Scheduling & Triggering (C3)"
Cohesion: 0.13
Nodes (6): AlarmAdapter, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 5 - "Contacts & User Discovery (C5)"
Cohesion: 0.06
Nodes (4): DisplayItem, OnContactClickListener, ChatContactItem, ChatUser

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.10
Nodes (3): TelemetryGraphView, ScaleListener, ZoomImageView

### Community 7 - ".postError"
Cohesion: 0.10
Nodes (17): ChatApiClient, Callback, ChatCallback<MessagesResult>, Callback, Callback, Callback, Callback, Callback (+9 more)

### Community 10 - "Override"
Cohesion: 0.16
Nodes (4): com.google.android.material.tabs.TabLayout.OnTabSelectedListener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback, ChatApiClient.ChatCallback<String>

### Community 11 - "android.net.Uri"
Cohesion: 0.20
Nodes (3): AlbumsAdapter, OnAlbumClickListener, AlbumItem

### Community 12 - "AlarmRingingService.java"
Cohesion: 0.06
Nodes (5): AlarmFiringActivity, AlarmReceiver, BootReceiver, CallStateReceiver, AlarmRingingService

### Community 15 - "Data Encryption & Token Auth (C15)"
Cohesion: 0.10
Nodes (23): cors, dotenv, express, multer, @noble/ciphers, pg, dependencies, cors@^2.8.5 (+15 more)

### Community 17 - "android.view.View"
Cohesion: 0.07
Nodes (19): AlarmViewHolder, AlbumVH, CallLogAdapter, CallViewHolder, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactsAdapter (+11 more)

### Community 20 - "chat.js"
Cohesion: 0.14
Nodes (10): avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir, multer, path (+2 more)

### Community 21 - "Data Encryption & Token Auth (C21)"
Cohesion: 0.16
Nodes (14): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+6 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.12
Nodes (15): closeDatabase(), crypto, hashPassword(), initializeDatabase(), router, app, cors, express (+7 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.16
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

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
Cohesion: 0.16
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

### Community 38 - "ChatMessage"
Cohesion: 0.05
Nodes (9): ChatMessageAdapter, ChatRowItem, ChatActivity, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo> (+1 more)

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 214 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **32 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `Gallery & Media Presentation (C0)`, `Gallery & Media Presentation (C1)`, `ChatConversation`, `Alarm Scheduling & Triggering (C3)`, `.onCreate`, `.openHardwarePage`, `Hardware Info & Performance Telemetry (C6)`, `.postError`, `ContactsActivity`, `Override`, `AlarmRingingService.java`, `AlbumDetailActivity.java`, `ContactsActivity.java`, `android.view.View`, `android.text.TextWatcher`, `com.zez.app.model.MediaItem`, `AppUpdateHelper`, `ChatActivity.java`?**
  _High betweenness centrality (0.118) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `Gallery & Media Presentation (C0)` be split into smaller, more focused modules?**
  _Cohesion score 0.06458941901979877 - nodes in this community are weakly interconnected._
- **Why does `GalleryAdapter` connect `Gallery & Media Presentation (C0)` to `android.view.View`, `AlbumDetailActivity.java`?**
  _High betweenness centrality (0.054) - this node is a cross-community bridge._
- **Should `Gallery & Media Presentation (C1)` be split into smaller, more focused modules?**
  _Cohesion score 0.06582633053221289 - nodes in this community are weakly interconnected._
- **Why does `ChatMessage` connect `ChatMessage` to `Contacts & User Discovery (C5)`, `.postError`, `ChatApiClient.java`, `android.view.View`, `ChatActivity.java`?**
  _High betweenness centrality (0.043) - this node is a cross-community bridge._
- **Should `ChatConversation` be split into smaller, more focused modules?**
  _Cohesion score 0.10416666666666667 - nodes in this community are weakly interconnected._