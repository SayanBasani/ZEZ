# Graph Report - Call Log Mannager  (2026-11-18)

## Corpus Check
- 77 files · ~59,482 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 93 file(s) not represented in the graph (top: .xml 84, .properties 5, (none) 2)
- WARNING: 93 of 183 files seen (51%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1168 nodes · 2907 edges · 65 communities (25 shown, 40 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 190 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `13135555`
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
- ImageViewerActivity.java
- MainActivity
- Override
- android.net.Uri
- AlarmRingingService.java
- compilerOptions
- Data Encryption & Token Auth (C15)
- index.d.ts
- android.view.View
- Network APIs & Background Sync (C19)
- chat.js
- Data Encryption & Token Auth (C21)
- Network APIs & Background Sync (C22)
- .stopTelemetryUpdates
- AppUpdateHelper
- Image Viewing & Editing Tools (C25)
- admin.js
- Hardware Info & Performance Telemetry (C27)
- Device Registration & Authorization (C28)
- Network APIs & Background Sync (C29)
- Backend Admin Routes & APIs (C30)
- AlarmHelper.java
- ContactsActivity
- Network APIs & Background Sync (C35)
- .triggerManualSync
- ImageEditActivity.java
- ChatMessage
- CallSyncWorker
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- .loadSettingsData
- android.text.TextWatcher
- .loadAlarmsData
- .showUpdateDialog

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 86 edges
2. `ChatMessage` - 47 edges
3. `ViewHolder` - 31 edges
4. `ChatActivity` - 31 edges
5. `ChatApiClient` - 29 edges
6. `ChatCallback` - 27 edges
7. `GalleryAdapter` - 26 edges
8. `MediaItem` - 26 edges
9. `AlarmItem` - 26 edges
10. `ImageViewerActivity` - 23 edges

## Surprising Connections (you probably didn't know these)
- `MainActivity` --references--> `AlarmAdapter`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/AlarmAdapter.java
- `MainActivity` --references--> `AlarmItem`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/model/AlarmItem.java
- `MainActivity` --references--> `TelemetryGraphView`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java → ZezAndroid/app/src/main/java/com/zez/app/util/TelemetryGraphView.java
- `PageVH` --inherits--> `ViewHolder`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/MediaPagerAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/adapter/ConversationsAdapter.java
- `PageVH` --references--> `ZoomImageView`  [EXTRACTED]
  ZezAndroid/app/src/main/java/com/zez/app/adapter/MediaPagerAdapter.java → ZezAndroid/app/src/main/java/com/zez/app/util/ZoomImageView.java

## Import Cycles
- None detected.

## Communities (65 total, 40 thin omitted)

### Community 0 - "Gallery & Media Presentation (C0)"
Cohesion: 0.05
Nodes (13): FilmstripAdapter, OnFilmstripClickListener, GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, MediaPagerAdapter, OnTapListener, PageVH (+5 more)

### Community 1 - "Gallery & Media Presentation (C1)"
Cohesion: 0.07
Nodes (11): ApiCallback, ApiClient, okhttp3.Callback, ChatPreferencesHelper, DeviceHelper, FavoritesHelper, MediaHelper, PreferencesHelper (+3 more)

### Community 2 - "ChatConversation"
Cohesion: 0.11
Nodes (3): ConversationsAdapter, OnConversationClickListener, ChatConversation

### Community 3 - "Alarm Scheduling & Triggering (C3)"
Cohesion: 0.15
Nodes (5): AlarmAdapter, OnAlarmClickListener, AlarmItem, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 4 - "CallLogItem"
Cohesion: 0.22
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 5 - "Contacts & User Discovery (C5)"
Cohesion: 0.06
Nodes (4): DisplayItem, OnContactClickListener, ChatContactItem, ChatUser

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.07
Nodes (5): TelemetryGraphView, GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - ".postError"
Cohesion: 0.07
Nodes (23): ChatActivity, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, ChatApiClient.ChatCallback<ChatMessage>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient, Callback, Callback (+15 more)

### Community 10 - "Override"
Cohesion: 0.15
Nodes (4): com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<ChatConversation>>, com.zez.app.util.AppUpdateHelper.DownloadProgressCallback

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 15 - "Data Encryption & Token Auth (C15)"
Cohesion: 0.07
Nodes (36): cors, dotenv, multer, @noble/ciphers, pg, @types/cors, @types/express, @types/multer (+28 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "android.view.View"
Cohesion: 0.07
Nodes (17): AlarmViewHolder, AlbumsAdapter, AlbumVH, OnAlbumClickListener, DateHeaderViewHolder, IncomingViewHolder, OutgoingViewHolder, ContactsAdapter (+9 more)

### Community 20 - "chat.js"
Cohesion: 0.10
Nodes (13): fs, path, avatarStorage, avatarUploadDir, express, fs, mediaStorage, mediaUploadDir (+5 more)

### Community 21 - "Data Encryption & Token Auth (C21)"
Cohesion: 0.24
Nodes (11): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+3 more)

### Community 22 - "Network APIs & Background Sync (C22)"
Cohesion: 0.11
Nodes (15): closeDatabase(), crypto, hashPassword(), initializeDatabase(), router, app, cors, express (+7 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.20
Nodes (4): AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "Image Viewing & Editing Tools (C25)"
Cohesion: 0.22
Nodes (3): ImageEditActivity, SeekBar.OnSeekBarChangeListener, ImageEditHelper

### Community 26 - "admin.js"
Cohesion: 0.17
Nodes (3): verifyPassword(), crypto, express

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 28 - "Device Registration & Authorization (C28)"
Cohesion: 0.18
Nodes (3): CallLogListener, CallLogObserver, ZezApplication

### Community 29 - "Network APIs & Background Sync (C29)"
Cohesion: 0.11
Nodes (18): express, express@^5.1.0, { Pool }, crypto, deviceAuth(), express, parseLimit(), parsePositiveInteger() (+10 more)

### Community 30 - "Backend Admin Routes & APIs (C30)"
Cohesion: 0.15
Nodes (15): adminAuth(), base64UrlDecode(), base64UrlEncode(), createAdminToken(), crypto, getSecret(), verifyAdminToken(), downloadsDir (+7 more)

### Community 35 - "Network APIs & Background Sync (C35)"
Cohesion: 0.47
Nodes (4): get(), http, post(), run()

### Community 38 - "ChatMessage"
Cohesion: 0.07
Nodes (4): ChatMessageAdapter, ChatRowItem, ChatApiClient.ChatCallback<ChatMessage>, ChatMessage

## Knowledge Gaps
- **4 isolated node(s):** `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide`, `Admin Dashboard Web Interface`
  These have ≤1 connection - possible missing edges. (Counts symbols only; 264 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **40 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `Gallery & Media Presentation (C0)`, `Gallery & Media Presentation (C1)`, `ChatConversation`, `Alarm Scheduling & Triggering (C3)`, `Hardware Info & Performance Telemetry (C6)`, `ImageViewerActivity.java`, `Override`, `android.view.View`, `.stopTelemetryUpdates`, `AppUpdateHelper`, `.triggerManualSync`, `ImageEditActivity.java`, `ChatActivity.java`, `.loadSettingsData`, `MainActivity.java`, `.switchTab`, `android.text.TextWatcher`, `.loadAlarmsData`, `.showUpdateDialog`?**
  _High betweenness centrality (0.118) - this node is a cross-community bridge._
- **What connects `New Requirements Specification`, `Project README`, `System Architecture and Sync Guide` to the rest of the system?**
  _4 weakly-connected nodes found - possible missing edges._
- **Should `Gallery & Media Presentation (C0)` be split into smaller, more focused modules?**
  _Cohesion score 0.050724637681159424 - nodes in this community are weakly interconnected._
- **Why does `ChatMessage` connect `ChatMessage` to `ChatApiClient.java`, `Contacts & User Discovery (C5)`, `.postError`, `android.view.View`, `ChatActivity.java`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **Should `Gallery & Media Presentation (C1)` be split into smaller, more focused modules?**
  _Cohesion score 0.0710868079289132 - nodes in this community are weakly interconnected._
- **Should `ChatConversation` be split into smaller, more focused modules?**
  _Cohesion score 0.11182795698924732 - nodes in this community are weakly interconnected._
- **Should `Alarm Scheduling & Triggering (C3)` be split into smaller, more focused modules?**
  _Cohesion score 0.14717741935483872 - nodes in this community are weakly interconnected._