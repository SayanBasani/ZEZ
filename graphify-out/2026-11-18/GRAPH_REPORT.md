# Graph Report - Call Log Mannager  (2026-11-18)

## Corpus Check
- 73 files · ~51,478 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 85 file(s) not represented in the graph (top: .xml 76, .properties 5, (none) 2)
- WARNING: 85 of 171 files seen (50%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1034 nodes · 2699 edges · 59 communities (26 shown, 33 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 199 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `0c8ef32c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Gallery & Media Presentation (C0)
- Gallery & Media Presentation (C1)
- ChatMessage
- Alarm Scheduling & Triggering (C3)
- CallLogItem
- Contacts & User Discovery (C5)
- Hardware Info & Performance Telemetry (C6)
- Contacts & User Discovery (C7)
- MainActivity
- Gallery & Media Presentation (C10)
- Gallery & Media Presentation (C11)
- AlarmRingingService.java
- MainActivity.java
- Data Encryption & Token Auth (C15)
- Alarm Scheduling & Triggering (C16)
- Gallery & Media Presentation (C17)
- Override
- chat.js
- Data Encryption & Token Auth (C21)
- Network APIs & Background Sync (C22)
- Alarm Scheduling & Triggering (C23)
- Settings, Themes & User Preferences (C24)
- Image Viewing & Editing Tools (C25)
- Data Encryption & Token Auth (C26)
- Hardware Info & Performance Telemetry (C27)
- Device Registration & Authorization (C28)
- Network APIs & Background Sync (C29)
- Backend Admin Routes & APIs (C30)
- .postError
- CallSyncWorker
- ChatActivity
- Network APIs & Background Sync (C35)
- .openHardwarePage
- .initViews
- .loadSettingsData
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- AlarmAdapter
- com.zez.app.model.MediaItem
- .ping
- .loadAlarmsData

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 84 edges
2. `ChatMessage` - 43 edges
3. `MediaItem` - 39 edges
4. `ViewHolder` - 30 edges
5. `GalleryAdapter` - 29 edges
6. `ChatApiClient` - 28 edges
7. `ChatContactItem` - 27 edges
8. `AlarmItem` - 26 edges
9. `ChatActivity` - 25 edges
10. `ChatCallback` - 25 edges

## Surprising Connections (you probably didn't know these)
- `ChatActivity` --references--> `ChatMessageAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/ChatActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ChatMessageAdapter.java
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `MainActivity` --references--> `ConversationsAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java
- `MainActivity` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `MainActivity` --references--> `TelemetryGraphView`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/TelemetryGraphView.java

## Import Cycles
- None detected.

## Communities (59 total, 33 thin omitted)

### Community 0 - "Gallery & Media Presentation (C0)"
Cohesion: 0.06
Nodes (12): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, MediaPagerAdapter, OnTapListener, PageVH, AlbumDetailActivity, GalleryAdapter.Listener (+4 more)

### Community 1 - "Gallery & Media Presentation (C1)"
Cohesion: 0.07
Nodes (13): ChatApiClient.ChatCallback<String>, ApiCallback, ApiClient, ChatApiClient, Callback, ChatCallback, ContactSyncResult, MediaUploadResult (+5 more)

### Community 3 - "Alarm Scheduling & Triggering (C3)"
Cohesion: 0.17
Nodes (4): OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 4 - "CallLogItem"
Cohesion: 0.15
Nodes (4): CallLogAdapter, CallViewHolder, CallLogItem, CallLogHelper

### Community 5 - "Contacts & User Discovery (C5)"
Cohesion: 0.08
Nodes (6): ContactsAdapter, DisplayItem, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.10
Nodes (3): TelemetryGraphView, ScaleListener, ZoomImageView

### Community 7 - "Contacts & User Discovery (C7)"
Cohesion: 0.11
Nodes (4): ConversationsAdapter, OnConversationClickListener, ChatConversation, Callback

### Community 10 - "Gallery & Media Presentation (C10)"
Cohesion: 0.17
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 11 - "Gallery & Media Presentation (C11)"
Cohesion: 0.17
Nodes (6): OnRestoreClickListener, RecycleBinAdapter, MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 12 - "AlarmRingingService.java"
Cohesion: 0.06
Nodes (4): AlarmReceiver, BootReceiver, CallStateReceiver, AlarmRingingService

### Community 15 - "Data Encryption & Token Auth (C15)"
Cohesion: 0.10
Nodes (23): cors, dotenv, express, multer, @noble/ciphers, pg, dependencies, cors@^2.8.5 (+15 more)

### Community 17 - "Gallery & Media Presentation (C17)"
Cohesion: 0.15
Nodes (9): DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactViewHolder, HeaderViewHolder, ViewHolder, HeaderVH, MediaVH (+1 more)

### Community 18 - "Override"
Cohesion: 0.14
Nodes (4): android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback

### Community 20 - "chat.js"
Cohesion: 0.14
Nodes (10): avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir, multer, path (+2 more)

### Community 21 - "Data Encryption & Token Auth (C21)"
Cohesion: 0.15
Nodes (15): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+7 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.12
Nodes (15): closeDatabase(), crypto, hashPassword(), initializeDatabase(), router, app, cors, express (+7 more)

### Community 24 - "Settings, Themes & User Preferences (C24)"
Cohesion: 0.20
Nodes (4): AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "Image Viewing & Editing Tools (C25)"
Cohesion: 0.22
Nodes (3): ImageEditActivity, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "Data Encryption & Token Auth (C26)"
Cohesion: 0.22
Nodes (11): verifyPassword(), adminAuth(), base64UrlDecode(), base64UrlEncode(), createAdminToken(), crypto, getSecret(), verifyAdminToken() (+3 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 28 - "Device Registration & Authorization (C28)"
Cohesion: 0.20
Nodes (3): CallLogListener, CallLogObserver, ZezApplication

### Community 29 - "Network APIs & Background Sync (C29)"
Cohesion: 0.17
Nodes (12): { Pool }, crypto, deviceAuth(), crypto, express, getTimestamp(), httpError(), normalizeCall() (+4 more)

### Community 30 - "Backend Admin Routes & APIs (C30)"
Cohesion: 0.20
Nodes (8): downloadsDir, express, fs, multer, path, router, storage, upload

### Community 31 - ".postError"
Cohesion: 0.17
Nodes (7): Callback, Callback, Callback, Callback, Callback, Callback, Callback

### Community 33 - "ChatActivity"
Cohesion: 0.14
Nodes (5): ChatActivity, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<List<ChatMessage>>, ChatApiClient.ChatCallback<ChatMessage>

### Community 35 - "Network APIs & Background Sync (C35)"
Cohesion: 0.47
Nodes (4): get(), http, post(), run()

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 193 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **33 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `Gallery & Media Presentation (C0)`, `Gallery & Media Presentation (C1)`, `Alarm Scheduling & Triggering (C3)`, `.openHardwarePage`, `.initViews`, `Hardware Info & Performance Telemetry (C6)`, `Contacts & User Discovery (C7)`, `AlbumDetailActivity.java`, `.loadSettingsData`, `Gallery & Media Presentation (C10)`, `MainActivity.java`, `Gallery & Media Presentation (C17)`, `Override`, `AlarmAdapter`, `com.zez.app.model.MediaItem`, `.getFontScale`, `Settings, Themes & User Preferences (C24)`, `.loadAlarmsData`?**
  _High betweenness centrality (0.123) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `Gallery & Media Presentation (C0)` be split into smaller, more focused modules?**
  _Cohesion score 0.05640203154236835 - nodes in this community are weakly interconnected._
- **Why does `ChatMessage` connect `ChatMessage` to `ChatActivity`, `ChatApiClient.java`, `Gallery & Media Presentation (C1)`, `.onResponse`, `Gallery & Media Presentation (C13)`, `MainActivity.java`, `Alarm Scheduling & Triggering (C16)`, `Alarm Scheduling & Triggering (C23)`?**
  _High betweenness centrality (0.053) - this node is a cross-community bridge._
- **Should `Gallery & Media Presentation (C1)` be split into smaller, more focused modules?**
  _Cohesion score 0.075 - nodes in this community are weakly interconnected._
- **Why does `ChatContactItem` connect `Contacts & User Discovery (C5)` to `Alarm Scheduling & Triggering (C16)`, `Gallery & Media Presentation (C13)`, `MainActivity.java`?**
  _High betweenness centrality (0.034) - this node is a cross-community bridge._
- **Should `Contacts & User Discovery (C5)` be split into smaller, more focused modules?**
  _Cohesion score 0.07890070921985816 - nodes in this community are weakly interconnected._