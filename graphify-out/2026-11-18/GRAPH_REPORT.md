# Graph Report - Call Log Mannager  (2026-11-18)

## Corpus Check
- 73 files · ~47,893 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 75 file(s) not represented in the graph (top: .xml 66, .properties 5, (none) 2)
- WARNING: 75 of 161 files seen (47%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 955 nodes · 2478 edges · 51 communities (23 shown, 28 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 176 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1bd96adf`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Gallery & Media Presentation (C0)
- Gallery & Media Presentation (C1)
- Gallery & Media Presentation (C2)
- Alarm Scheduling & Triggering (C3)
- Gallery & Media Presentation (C4)
- Contacts & User Discovery (C5)
- Hardware Info & Performance Telemetry (C6)
- Contacts & User Discovery (C7)
- Main Activity & View Navigation (C9)
- Gallery & Media Presentation (C10)
- Gallery & Media Presentation (C11)
- Gallery & Media Presentation (C12)
- Main Activity & View Navigation (C14)
- Data Encryption & Token Auth (C15)
- Alarm Scheduling & Triggering (C16)
- Gallery & Media Presentation (C17)
- Gallery & Media Presentation (C18)
- Network APIs & Background Sync (C19)
- Messaging & Chat Engine (C20)
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
- Alarm Scheduling & Triggering (C31)
- Gallery & Media Presentation (C32)
- Activity Module (C33)
- Result Module (C34)
- Network APIs & Background Sync (C35)
- Hardware Info & Performance Telemetry (C36)
- Alarm Scheduling & Triggering (C37)
- Editable Module (C38)
- Broadcast Receivers & System Listeners (C39)
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 76 edges
2. `MediaItem` - 44 edges
3. `ChatMessage` - 35 edges
4. `GalleryAdapter` - 32 edges
5. `ChatConversation` - 32 edges
6. `ViewHolder` - 30 edges
7. `AlarmItem` - 26 edges
8. `ChatContactItem` - 25 edges
9. `AlbumDetailActivity` - 22 edges
10. `ChatUser` - 21 edges

## Surprising Connections (you probably didn't know these)
- `GalleryAdapter` --references--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/GalleryAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java
- `MainActivity` --references--> `GalleryAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/GalleryAdapter.java
- `MediaPagerAdapter` --references--> `MediaItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/MediaPagerAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/model/MediaItem.java
- `MainActivity` --references--> `MediaItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/MediaItem.java
- `ContactSyncResult` --references--> `ChatUser`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/util/ChatApiClient.java → ZezAndroid/app/src/main/java/com/zez/app/model/ChatUser.java

## Import Cycles
- None detected.

## Communities (51 total, 28 thin omitted)

### Community 0 - "Gallery & Media Presentation (C0)"
Cohesion: 0.06
Nodes (9): GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlbumDetailActivity, GalleryAdapter.Listener, ImageViewerActivity, ViewPager2.OnPageChangeCallback, GalleryListItem (+1 more)

### Community 1 - "Gallery & Media Presentation (C1)"
Cohesion: 0.07
Nodes (12): android.widget.AdapterView.OnItemSelectedListener, ApiCallback, ApiClient, ChatApiClient, Callback, ChatCallback, ContactSyncResult, PollResult (+4 more)

### Community 2 - "Gallery & Media Presentation (C2)"
Cohesion: 0.07
Nodes (8): ChatApiClient.ChatCallback<List<ChatMessage>>, ChatApiClient.ChatCallback<ChatMessage>, ChatMessage, okhttp3.Callback, Callback, Callback, Callback, Callback

### Community 3 - "Alarm Scheduling & Triggering (C3)"
Cohesion: 0.08
Nodes (7): AlarmAdapter, AlarmViewHolder, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 4 - "Gallery & Media Presentation (C4)"
Cohesion: 0.07
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 5 - "Contacts & User Discovery (C5)"
Cohesion: 0.08
Nodes (7): ContactsAdapter, DisplayItem, HeaderViewHolder, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.09
Nodes (4): ImageEditHelper, TelemetryGraphView, ScaleListener, ZoomImageView

### Community 7 - "Contacts & User Discovery (C7)"
Cohesion: 0.10
Nodes (4): ConversationsAdapter, OnConversationClickListener, ChatConversation, Callback

### Community 10 - "Gallery & Media Presentation (C10)"
Cohesion: 0.12
Nodes (6): AlbumsAdapter, OnAlbumClickListener, MediaPagerAdapter, OnTapListener, PageVH, AlbumItem

### Community 11 - "Gallery & Media Presentation (C11)"
Cohesion: 0.15
Nodes (7): BinViewHolder, OnRestoreClickListener, RecycleBinAdapter, MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 15 - "Data Encryption & Token Auth (C15)"
Cohesion: 0.11
Nodes (21): cors, dotenv, multer, @noble/ciphers, pg, dependencies, cors@^2.8.5, dotenv@^18.0.7 (+13 more)

### Community 17 - "Gallery & Media Presentation (C17)"
Cohesion: 0.21
Nodes (8): AlbumVH, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactViewHolder, ViewHolder, HeaderVH, MediaVH

### Community 18 - "Gallery & Media Presentation (C18)"
Cohesion: 0.14
Nodes (4): com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback

### Community 20 - "Messaging & Chat Engine (C20)"
Cohesion: 0.13
Nodes (12): express, express@^5.1.0, { Pool }, express, router, crypto, express, router (+4 more)

### Community 21 - "Data Encryption & Token Auth (C21)"
Cohesion: 0.15
Nodes (15): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+7 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.16
Nodes (10): closeDatabase(), crypto, hashPassword(), initializeDatabase(), app, startServer(), http, path (+2 more)

### Community 24 - "Settings, Themes & User Preferences (C24)"
Cohesion: 0.22
Nodes (4): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, ReleaseInfo, UpdateCheckCallback

### Community 26 - "Data Encryption & Token Auth (C26)"
Cohesion: 0.22
Nodes (11): verifyPassword(), adminAuth(), base64UrlDecode(), base64UrlEncode(), createAdminToken(), crypto, getSecret(), verifyAdminToken() (+3 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 28 - "Device Registration & Authorization (C28)"
Cohesion: 0.27
Nodes (3): CallLogListener, CallLogObserver, ZezApplication

### Community 29 - "Network APIs & Background Sync (C29)"
Cohesion: 0.27
Nodes (8): crypto, deviceAuth(), crypto, express, getTimestamp(), httpError(), normalizeCall(), router

### Community 30 - "Backend Admin Routes & APIs (C30)"
Cohesion: 0.20
Nodes (8): downloadsDir, express, fs, multer, path, router, storage, upload

### Community 35 - "Network APIs & Background Sync (C35)"
Cohesion: 0.47
Nodes (4): get(), http, post(), run()

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 177 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **28 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `Main Activity & View Navigation (C9)` to `Gallery & Media Presentation (C0)`, `Gallery & Media Presentation (C1)`, `Alarm Scheduling & Triggering (C3)`, `Gallery & Media Presentation (C4)`, `Hardware Info & Performance Telemetry (C36)`, `Hardware Info & Performance Telemetry (C6)`, `Contacts & User Discovery (C7)`, `Gallery & Media Presentation (C8)`, `Gallery & Media Presentation (C10)`, `Gallery & Media Presentation (C11)`, `Main Activity & View Navigation (C14)`, `Gallery & Media Presentation (C17)`, `Gallery & Media Presentation (C18)`, `Settings, Themes & User Preferences (C24)`?**
  _High betweenness centrality (0.123) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `Gallery & Media Presentation (C0)` be split into smaller, more focused modules?**
  _Cohesion score 0.06265432098765432 - nodes in this community are weakly interconnected._
- **Why does `MediaItem` connect `Gallery & Media Presentation (C0)` to `Gallery & Media Presentation (C8)`, `Main Activity & View Navigation (C9)`, `Gallery & Media Presentation (C10)`, `Gallery & Media Presentation (C11)`, `Gallery & Media Presentation (C13)`, `Main Activity & View Navigation (C14)`, `Gallery & Media Presentation (C18)`?**
  _High betweenness centrality (0.055) - this node is a cross-community bridge._
- **Should `Gallery & Media Presentation (C1)` be split into smaller, more focused modules?**
  _Cohesion score 0.07263157894736842 - nodes in this community are weakly interconnected._
- **Why does `ChatConversation` connect `Contacts & User Discovery (C7)` to `Gallery & Media Presentation (C1)`, `Gallery & Media Presentation (C4)`, `Main Activity & View Navigation (C9)`, `Gallery & Media Presentation (C13)`, `Main Activity & View Navigation (C14)`, `Alarm Scheduling & Triggering (C16)`, `Gallery & Media Presentation (C18)`?**
  _High betweenness centrality (0.046) - this node is a cross-community bridge._
- **Should `Gallery & Media Presentation (C2)` be split into smaller, more focused modules?**
  _Cohesion score 0.07071887784921099 - nodes in this community are weakly interconnected._