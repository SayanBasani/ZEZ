# Graph Report - Call Log Mannager  (2026-11-22)

## Corpus Check
- 253 files · ~204,830 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 191 file(s) not represented in the graph (top: .xml 180, .properties 5, (none) 3)
- WARNING: 191 of 457 files seen (42%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 3912 nodes · 10506 edges · 197 communities (111 shown, 86 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 1196 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `7b6c071d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- StudioLayer
- AlarmItem
- b2StorageService.js
- AppCallItem
- e2e_device_test.py
- ContactsActivity
- Hardware Info & Performance Telemetry (C6)
- DocumentScannerActivity
- ExportOptions
- ActiveCallManager
- AiMetadata
- DocumentCorner
- AiIndexingService
- compilerOptions
- .loadGalleryData
- package.json
- index.d.ts
- Override
- ChatUser
- FilmstripAdapter
- d_call_log_mannager_zez_backend_src_db_pool
- ChatApiClient
- AiVisionEngine.java
- MainActivity
- AppUpdateHelper
- Override
- admin.js
- Hardware Info & Performance Telemetry (C27)
- server.js
- list
- Call Recording Audio Diagnostics & Root-Cause Technical Report
- Override
- test-roster.js
- CallSyncWorker
- CallActivity
- ChatMessage
- AiModelManager
- New Requirements Specification Module (C41)
- Project Readme Module (C42)
- System Architecture And Sync Guide Module (C43)
- Backend Admin Routes & APIs (C44)
- appRelease.js
- Override
- SmartCategory
- ImageViewerActivity
- com.zez.app.model.ChatMessage
- TWING OEM Call Recording Compatibility & Test Report
- ImageLabelTag
- com.zez.app.model.ChatConversation
- okhttp3.Call
- DocumentCropOverlayView
- 2. Core Components & Responsibilities
- RemoteModelProvider
- Call Recording Implementation Audit
- com.zez.app.model.ChatConversation
- callRead.js
- 3. Detailed Component Breakdown
- android.content.Context
- ChatLocalDbHelper
- .onResponse
- SmartSearchActivity
- benchmark
- org.junit.Test
- ChatActivity
- EraserCanvasView
- 2. Manufacturer-by-Manufacturer Breakdown
- android.net.Uri
- android.net.Uri
- 4. Troubleshooting & FAQs
- ApiCallback
- PreferencesHelper
- SelectionEngine
- TWING: Device-Specific Zero-Audio Investigation — Android 16
- android.os.Bundle
- AiGalleryDatabaseHelper
- TextLayer
- Technical Proposal: Unified Call Recording & Native OEM Import Integration
- 1. System-Level Architectural Limitations
- .prepareObjectMove
- PhotoEditorActivity
- CreativeStudioActivity
- Override
- EditAdjustment
- ScannedPageAdapter
- AiEraserStudioActivity.java
- CallStateReceiver
- android.graphics.Bitmap
- AiFeature
- extreme-test-suite.js
- CreativeStudioAcceptanceTest
- CallLogItem
- DetectedObjectBox
- FrameLayer
- LocalModelProvider
- server.js
- list
- AppCallAdapter
- .initViews
- Phase 1: Architecture, Paginated Data Layer & Edit Ledger Setup
- Phase 2: Gallery 2.0 Modern Timeline Grid & Album Navigation
- Phase 3: High-Performance Media Viewer (Zero Intent Overhead)
- Phase 4: Non-Destructive Editing Engine Core & Undo/Redo Pipeline
- Phase 5: Transform & Crop Tools
- Phase 6: Professional Tone, Color & Detail Adjustments
- Phase 7: Curated Filters & Creative Markup Suite
- Phase 8: Original Media Safety & Non-Destructive Safe Exporter
- MarkupItem
- Phase 9: System Integration, Performance Hardening & Rollback Verification
- Override
- DocumentOcrHelper
- Detailed Phase Breakdown
- MarkupCanvasView
- WatermarkLayer
- AiEnhancementStudioActivity
- CropAspectRatio
- EnhancementMode
- EnhancementCallback
- MediaPagerAdapter
- AiSuperResolutionPipeline
- Override
- StickerLayer
- PerspectiveQuad
- AiEnhancePipeline
- EditState
- StudioStateHistory
- SelectionTool
- 2.3 Modified Existing Files
- AiEraserAcceptanceTest
- TWING GALLERY 2.0 — FINAL QUALITY ASSURANCE & ACCEPTANCE REPORT
- GeometryTransform
- PortraitEnhancePipeline
- SocialTemplate
- FilterType
- LayerType
- CallStateReceiver
- .enhance
- TelemetryGraphView
- CompressionQuality
- ViewHolder
- .getPrefs
- Override
- Type
- EditorPreviewView
- Detailed Test Analysis:
- 3. Manufacturer-by-Manufacturer Native Recording Breakdown
- AlarmRingingService
- ChatConversation
- android.hardware.camera2.CameraDevice
- FilterAdapter
- Override
- .executeCurrentAction
- AiIndexingService.IndexingListener
- PhotoExportEngine.ExportCallback
- PhoneHelper
- .onSuccess

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 112 edges
2. `ChatActivity` - 71 edges
3. `AiMetadata` - 56 edges
4. `EditAdjustment` - 52 edges
5. `DocumentScannerActivity` - 49 edges
6. `StudioLayer` - 47 edges
7. `EraserCanvasView` - 44 edges
8. `ChatMessageAdapter` - 43 edges
9. `PhotoEditorActivity` - 42 edges
10. `AppCallItem` - 42 edges

## Surprising Connections (you probably didn't know these)
- `2.5 Album Detail Screen` --references--> `AlbumDetailActivity`  [INFERRED]
  docs/gallery2/GALLERY2_BASELINE_AUDIT.md → ZezAndroid/app/src/main/java/com/zez/app/AlbumDetailActivity.java
- `3. Scoped-Out Features & External Model Dependencies` --references--> `PassportPhotoHelper`  [INFERRED]
  docs/gallery2/GALLERY2_FINAL_ACCEPTANCE_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/creative/utilities/PassportPhotoHelper.java
- `4. Test Scenarios Executed & Verified` --references--> `CallStateReceiver`  [INFERRED]
  TWING_OEM_COMPATIBILITY_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `A. Vivo T3x 5G (Android 16)` --references--> `CallStateReceiver`  [INFERRED]
  TWING_OEM_COMPATIBILITY_TEST_REPORT.md → ZezAndroid/app/src/main/java/com/zez/app/receiver/CallStateReceiver.java
- `2.1 Navigation & Entry Point` --references--> `MainActivity`  [INFERRED]
  docs/gallery2/GALLERY2_BASELINE_AUDIT.md → ZezAndroid/app/src/main/java/com/zez/app/MainActivity.java

## Import Cycles
- None detected.

## Communities (197 total, 86 thin omitted)

### Community 1 - "AlarmItem"
Cohesion: 0.12
Nodes (7): AlarmAdapter, OnAlarmClickListener, com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener, AlarmItem, AlarmReceiver, AlarmHelper, TypeToken<List<AlarmItem>>

### Community 2 - "b2StorageService.js"
Cohesion: 0.21
Nodes (22): CATEGORY_LIMITS, crypto, deleteFile(), fileExists(), formatBytes(), fs, generateDownloadUrl(), generateObjectKey() (+14 more)

### Community 3 - "AppCallItem"
Cohesion: 0.09
Nodes (3): OnCallActionListener, com.zez.app.adapter.AppCallAdapter.OnCallActionListener, AppCallItem

### Community 4 - "e2e_device_test.py"
Cohesion: 0.19
Nodes (19): configure_backend(), adb_shell(), dump_ui(), find_node(), get_nodes(), send_chat_text(), tap_node(), adb_shell() (+11 more)

### Community 5 - "ContactsActivity"
Cohesion: 0.09
Nodes (5): DisplayItem, OnContactClickListener, ContactsActivity, TextWatcher, ChatContactItem

### Community 6 - "Hardware Info & Performance Telemetry (C6)"
Cohesion: 0.13
Nodes (4): GestureListener, OnTapListener, ScaleListener, ZoomImageView

### Community 7 - "DocumentScannerActivity"
Cohesion: 0.12
Nodes (3): DocumentScannerActivity, DocumentOcrHelper.OcrResultCallback, ScannedPageAdapter.OnPageActionListener

### Community 8 - "ExportOptions"
Cohesion: 0.09
Nodes (11): DocumentExporter, ExportCallback, ExportOptions, Format, JPG, PDF, PNG, PageSize (+3 more)

### Community 10 - "AiMetadata"
Cohesion: 0.10
Nodes (4): AiIntelligenceAcceptanceTest, AiMetadata, OnItemClickListener, SmartSearchAdapter

### Community 11 - "DocumentCorner"
Cohesion: 0.13
Nodes (3): DocumentScannerAcceptanceTest, DocumentCorner, DocumentCornerTest

### Community 12 - "AiIndexingService"
Cohesion: 0.10
Nodes (3): AiIndexingService, IndexingListener, AiPreferences

### Community 13 - "compilerOptions"
Cohesion: 0.10
Nodes (20): compilerOptions, allowJs, checkJs, declaration, declarationMap, esModuleInterop, forceConsistentCasingInFileNames, lib (+12 more)

### Community 14 - ".loadGalleryData"
Cohesion: 0.13
Nodes (7): 5. Performance, Memory & Scalability Bottlenecks, OnRestoreClickListener, RecycleBinAdapter, MediaHelper, RecycleBinHelper, TypeToken<List<RecycledItem>>, RecycledItem

### Community 15 - "package.json"
Cohesion: 0.05
Nodes (44): @aws-sdk/client-s3, @aws-sdk/s3-request-presigner, cors, dotenv, @noble/ciphers, pg, @types/cors, @types/express (+36 more)

### Community 16 - "index.d.ts"
Cohesion: 0.12
Nodes (15): AdminCallsQuery, AdminChatExportFormat, AdminUserRecord, ApiResponse, AppReleaseRecord, CallLogRecord, CallSyncBatchPayload, ChatConversationRecord (+7 more)

### Community 17 - "Override"
Cohesion: 0.15
Nodes (7): Callback, TextureView.SurfaceTextureListener, ActiveCallManager.CallStateListener, Runnable, Callback, Runnable, Callback

### Community 19 - "FilmstripAdapter"
Cohesion: 0.36
Nodes (3): FilmstripAdapter, FilmstripVH, OnFilmstripClickListener

### Community 20 - "d_call_log_mannager_zez_backend_src_db_pool"
Cohesion: 0.04
Nodes (34): content, fs, lines, navMatch, fs, path, fs, path (+26 more)

### Community 21 - "ChatApiClient"
Cohesion: 0.14
Nodes (13): ChatApiClient, Callback, Callback, ChatCallback<MessagesResult>, Callback, ChatCallback, ContactSyncResult, ConversationModeInfo (+5 more)

### Community 24 - "AppUpdateHelper"
Cohesion: 0.17
Nodes (5): com.zez.app.util.AppUpdateHelper.UpdateCheckCallback, AppUpdateHelper, DownloadProgressCallback, ReleaseInfo, UpdateCheckCallback

### Community 25 - "Override"
Cohesion: 0.10
Nodes (8): AiModelManager.DownloadCallback, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, EnhancementCallback, EnhancementCallback, EnhancementCallback

### Community 26 - "admin.js"
Cohesion: 0.09
Nodes (14): verifyPassword(), AVATARS_DIR, CHAT_MEDIA_DIR, crypto, DOWNLOADS_DIR, express, formatBytes(), fs (+6 more)

### Community 27 - "Hardware Info & Performance Telemetry (C27)"
Cohesion: 0.27
Nodes (4): CpuInfo, HardwareInfoHelper, RamInfo, StorageInfo

### Community 28 - "server.js"
Cohesion: 0.12
Nodes (14): closeDatabase(), initializeDatabase(), app, startServer(), http, path, run(), tests (+6 more)

### Community 29 - "list"
Cohesion: 0.12
Nodes (4): AlarmViewHolder, HeaderVH, MediaVH, BinViewHolder

### Community 30 - "Call Recording Audio Diagnostics & Root-Cause Technical Report"
Cohesion: 0.11
Nodes (17): 1.1 The Core Problem, 1.2 The Root Cause, 1. Executive Summary & Root Cause Analysis, 2. Audio Source Investigation Matrix, 3.1 `AudioManager.AudioRecordingCallback` (API 29+), 3.2 Real-Time Peak & Silence Monitor, 3.3 Post-Recording Frame Decoded Sample Validation (`AudioCaptureValidator`), 3.4 Elimination of False Recordings (+9 more)

### Community 31 - "Override"
Cohesion: 0.08
Nodes (11): ChatApiClient.ChatCallback<Void>, ChatApiClient.ChatCallback<Void>, android.text.TextWatcher, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>, ItemTouchHelper.SimpleCallback, android.widget.SeekBar.OnSeekBarChangeListener, ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>, ChatApiClient.ChatCallback<ChatApiClient.MessagesResult> (+3 more)

### Community 32 - "test-roster.js"
Cohesion: 0.09
Nodes (22): http, run(), http, run(), http, request(), run(), token (+14 more)

### Community 33 - "CallSyncWorker"
Cohesion: 0.09
Nodes (4): CallLogListener, CallLogObserver, CallSyncWorker, CallLogHelper

### Community 39 - "AiModelManager"
Cohesion: 0.08
Nodes (10): AiModelManager, DeviceCompatibility, DownloadCallback, AiModelPackage, Status, CORRUPTED, DOWNLOADING, ERROR (+2 more)

### Community 52 - "appRelease.js"
Cohesion: 0.07
Nodes (16): fs, path, fs, path, fs, path, fs, path (+8 more)

### Community 53 - "Override"
Cohesion: 0.06
Nodes (13): okhttp3.Callback, android.text.TextWatcher, android.text.TextWatcher, com.google.android.material.tabs.TabLayout.OnTabSelectedListener, GalleryAdapter.Listener, ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>>, ChatApiClient.ChatCallback<List<ChatConversation>>, android.text.TextWatcher (+5 more)

### Community 54 - "SmartCategory"
Cohesion: 0.10
Nodes (12): SmartCategory, ANIMALS, DOCUMENTS, FOOD, NATURE, NOTES, PEOPLE, RECEIPTS (+4 more)

### Community 55 - "ImageViewerActivity"
Cohesion: 0.05
Nodes (12): 2.4 Full-Screen Media Viewer, 7. Affected Files & Isolation Boundaries, GalleryAdapter, GridLayoutManager.SpanSizeLookup, Listener, AlarmFiringActivity, AlbumDetailActivity, GalleryAdapter.Listener (+4 more)

### Community 56 - "com.zez.app.model.ChatMessage"
Cohesion: 0.07
Nodes (7): ChatMessageAdapter, ChatRowItem, IncomingViewHolder, OnMediaRetryListener, OnMessageActionListener, OutgoingViewHolder, ChatMessageAdapter.OnMessageActionListener

### Community 58 - "TWING OEM Call Recording Compatibility & Test Report"
Cohesion: 0.22
Nodes (8): 1. Executive Summary, 2. Multi-OEM Compatibility Matrix, 3. Physical Device Verification Findings, 4. Test Scenarios Executed & Verified, A. Vivo T3x 5G (Android 16), B. OnePlus Nord CE 3 Lite 5G (Android 16), C. Google Phone Dialer (Fallback Mode), TWING OEM Call Recording Compatibility & Test Report

### Community 60 - "com.zez.app.model.ChatConversation"
Cohesion: 0.11
Nodes (5): BootReceiver, ChatNotificationHelper, ChatNotificationManager, OnChatsUpdateListener, ZezApplication

### Community 61 - "okhttp3.Call"
Cohesion: 0.12
Nodes (11): Callback, Callback, Callback, Callback, Callback, Callback, Callback, Callback (+3 more)

### Community 63 - "2. Core Components & Responsibilities"
Cohesion: 0.18
Nodes (10): 1. Overview, 2.1 `CallRecordingManager` (Main Coordinator), 2.3 `RecordingCapabilityDetector` (Device & Policy Inspector), 2.4 `RecordingEngine` (Audio Capture Engine), 2.5 `RecordingStorageManager` (Storage & File Integrity), 2.6 `RecordingDatabaseManager` (Metadata Persistence), 2.7 `RecordingNotificationManager` (User Indicators & Compliance), 2.8 `RecordingPermissionManager` (Consent & Runtime Checks) (+2 more)

### Community 64 - "RemoteModelProvider"
Cohesion: 0.12
Nodes (3): InpaintingProvider, InpaintingProviderRegistry, RemoteModelProvider

### Community 65 - "Call Recording Implementation Audit"
Cohesion: 0.20
Nodes (9): 1.1 Project Structure & Build Configuration, 1.2 Core Functional Modules, 1. Existing Architecture & Baseline, 2. Inventory of Existing Call Recording Files, 3. Untouchable Files & Subsystems, 4. Android Security & Hardware Restrictions Analysis, 5. Proposed Modular Integration Strategy, Call Recording Implementation Audit (+1 more)

### Community 66 - "com.zez.app.model.ChatConversation"
Cohesion: 0.16
Nodes (4): ConversationsAdapter, OnAvatarClickListener, OnConversationClickListener, OnSelectionListener

### Community 67 - "callRead.js"
Cohesion: 0.10
Nodes (22): crypto, decryptJson(), decryptPortableHex(), decryptStoredPayload(), encryptJson(), { gcm }, getKey(), { managedNonce, hexToBytes, bytesToUtf8 } (+14 more)

### Community 68 - "3. Detailed Component Breakdown"
Cohesion: 0.15
Nodes (12): 1. Architectural Overview & Context, 2. Implemented Architecture & Subsystems, 3. Detailed Component Breakdown, 4. Verification and Security Validation, A. Storage Access Framework & Permission Persistence (`OemRecordingImporter.java`), B. Intelligent Pattern & CallLog Correlation Engine (`RecordingMatcher.java`), C. Background Synchronization (`RecordingSyncWorker.java`), D. Universal Sharing Fallback (`RecordingShareActivity.java`) (+4 more)

### Community 75 - "org.junit.Test"
Cohesion: 0.10
Nodes (4): Gallery2QualityAssuranceTest, AiEnhancementUnitTest, AiEraserUnitTest, CreativeStudioUnitTest

### Community 77 - "ChatActivity"
Cohesion: 0.06
Nodes (6): ChatActivity, ChatApiClient.ChatCallback<ChatMessage>, Callback, ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>, PendingMediaTask, PendingMessage

### Community 79 - "EraserCanvasView"
Cohesion: 0.09
Nodes (4): AiEraserStudioActivity, EraserCanvasView, ScaleGestureDetector.SimpleOnScaleGestureListener, OnSelectionChangedListener

### Community 81 - "2. Manufacturer-by-Manufacturer Breakdown"
Cohesion: 0.20
Nodes (9): 1. Overview, 2.1 Samsung (One UI / Android 10–14), 2.2 Xiaomi / Redmi / POCO (MIUI / HyperOS / Android 10–14), 2.3 OnePlus / Oppo / Realme (ColorOS / OxygenOS / Android 10–14), 2.4 Vivo / iQOO (Funtouch OS / OriginOS / Android 10–14), 2. Manufacturer-by-Manufacturer Breakdown, 3. Comprehensive Device Capability Matrix, 4. Architectural Honesty & Reporting (+1 more)

### Community 82 - "android.net.Uri"
Cohesion: 0.22
Nodes (4): AlbumsAdapter, AlbumVH, OnAlbumClickListener, AlbumItem

### Community 87 - "android.net.Uri"
Cohesion: 0.07
Nodes (4): GeometryEngine, PerspectiveEngine, PhotoExportEngine, ScannedPage

### Community 88 - "4. Troubleshooting & FAQs"
Cohesion: 0.12
Nodes (16): 1. Quick Start Setup (One-Time Configuration), 2. How the Automatic Import Works, 3. Universal Manual Fallback ("Share to TWING"), 4. Troubleshooting & FAQs, OnePlus / OPPO / Realme Phones (OxygenOS / ColorOS), Q1: My recordings are not importing automatically after a call ends., Q2: Why does an old recording say "Audio capture silenced by Android OS"?, Q3: Why does Google Phone say out loud "This call is now being recorded"? (+8 more)

### Community 90 - "PreferencesHelper"
Cohesion: 0.14
Nodes (4): android.widget.AdapterView.OnItemSelectedListener, ApiClient, DeviceHelper, PreferencesHelper

### Community 93 - "SelectionEngine"
Cohesion: 0.15
Nodes (4): SelectionEngine, SelectionMode, ADD, SUBTRACT

### Community 94 - "TWING: Device-Specific Zero-Audio Investigation — Android 16"
Cohesion: 0.13
Nodes (14): 1. Executive Summary, 2. Exact Device Profiles & Specifications, 4.1 Telephony Audio Mode Transition, 4.2 `AudioRecordingConfiguration` Parameters During Call, 4. Deep Inspection of Android Recording Configuration, 5.1 Vivo (Funtouch OS) Native Call Recorder, 5.2 OnePlus (OxygenOS / ColorOS) Native Call Recorder, 5. Investigation of OEM-Supported Call Recording Mechanisms (+6 more)

### Community 95 - "android.os.Bundle"
Cohesion: 0.23
Nodes (3): IncomingCallActivity, Callback, Callback

### Community 97 - "TextLayer"
Cohesion: 0.07
Nodes (7): FontStyle, BOLD, DEFAULT, MONOSPACE, SANS_SERIF, SERIF, TextLayer

### Community 99 - "Technical Proposal: Unified Call Recording & Native OEM Import Integration"
Cohesion: 0.22
Nodes (8): 1. Executive Summary & Objective, 2. Physical Device ADB Status Verification, 5. Intelligent Call Matching Algorithm (`RecordingMatcher`), 6.1 Database Schema (v4 Upgrade in `CallRecordDbHelper`):, 6.2 `CallRecordItem` & `RecordingMetadata` Extensions:, 6. Proposed Database & Model Schema Extension, 9. Recommendation & Review Request, Technical Proposal: Unified Call Recording & Native OEM Import Integration

### Community 100 - "1. System-Level Architectural Limitations"
Cohesion: 0.25
Nodes (7): 1.1 The `CAPTURE_AUDIO_OUTPUT` Security Boundary, 1.2 Android `AudioPolicyService` & `AudioFlinger` Silencing, 1.3 Android 11+ "While-In-Use" Microphone Restrictions, 1.4 Google Play Policy Regarding Accessibility Services, 1. System-Level Architectural Limitations, 2. Summary for End Users & Operators, Call Recording Known Limitations & Technical Explanations

### Community 102 - ".prepareObjectMove"
Cohesion: 0.24
Nodes (3): InpaintingCallback, InpaintingCallback, EraserCanvasView.OnSelectionChangedListener

### Community 103 - "PhotoEditorActivity"
Cohesion: 0.08
Nodes (7): PhotoEditorActivity, SeekBar.OnSeekBarChangeListener, PhotoExportEngine.ExportCallback, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener

### Community 106 - "Override"
Cohesion: 0.13
Nodes (6): AiEnhancementAcceptanceTest, EnhancementCallback, EnhancementCallback, AiModelManager.DownloadCallback, EnhancementCallback, EnhancementCallback

### Community 108 - "EditAdjustment"
Cohesion: 0.07
Nodes (3): PhotoEditorAcceptanceTest, ColorFilterEngine, EditAdjustment

### Community 109 - "ScannedPageAdapter"
Cohesion: 0.29
Nodes (3): OnPageActionListener, PageViewHolder, ScannedPageAdapter

### Community 117 - "AiEraserStudioActivity.java"
Cohesion: 0.11
Nodes (3): InpaintingCallback, DedicatedToolsRegistry, EraserToolDescriptor

### Community 118 - "CallStateReceiver"
Cohesion: 0.33
Nodes (5): 1. Automated & Emulator Test Results (Pixel 6a / Android 14–17), 2.1 Test Matrix Protocol Checklist, 2.2 OEM Audio Capture Verification Summary, 2. Physical Device Testing Protocol, Call Recording Test Report & Audio Validation Verification

### Community 119 - "android.graphics.Bitmap"
Cohesion: 0.05
Nodes (8): BeforeAfterView, ExpansionResult, ImageExpansionEngine, CollageCell, BatchCallback, PhotoToPdfConverter, MosaicEngine, PerspectiveTransformer

### Community 121 - "AiFeature"
Cohesion: 0.11
Nodes (7): AiFeature, Availability, AVAILABLE, DOWNLOAD_REQUIRED, PROCESSING, UNAVAILABLE_LOW_RAM, AiFeatureRegistry

### Community 122 - "extreme-test-suite.js"
Cohesion: 0.38
Nodes (5): api(), assert, crypto, runExtremeTestSuite(), test()

### Community 123 - "CreativeStudioAcceptanceTest"
Cohesion: 0.08
Nodes (15): CreativeStudioAcceptanceTest, CollageEngine, CutoutCallback, CutoutEngine, CollageLayoutType, GRID_2X2, GRID_2X3, GRID_3X3 (+7 more)

### Community 125 - "CallLogItem"
Cohesion: 0.17
Nodes (3): CallLogAdapter, CallViewHolder, CallLogItem

### Community 127 - "FrameLayer"
Cohesion: 0.12
Nodes (8): FrameLayer, FrameStyle, CLASSIC_SOLID, FILM_STRIP, NONE, POLAROID, ROUNDED, VIGNETTE

### Community 128 - "LocalModelProvider"
Cohesion: 0.19
Nodes (4): InpaintingAlgorithm, EXEMPLAR_PATCH, FAST_MARCHING_TELEA, LocalModelProvider

### Community 129 - "server.js"
Cohesion: 0.04
Nodes (43): express, multer, express@^5.1.0, multer@^2.5.0, express, multer, router, upload (+35 more)

### Community 130 - "list"
Cohesion: 0.08
Nodes (9): 2. Implemented Features Matrix (Phases 1 – 7), BatchImageProcessor, ResizePreset, SCALE_50, SCALE_75, TARGET_1080P, TARGET_720P, PassportPhotoHelper (+1 more)

### Community 131 - "AppCallAdapter"
Cohesion: 0.29
Nodes (3): AppCallAdapter, CallViewHolder, SearchViewHolder

### Community 132 - ".initViews"
Cohesion: 0.08
Nodes (19): 1. Executive Summary, 2.1 Navigation & Entry Point, 2.2 Media Data Model, 2.3 Grid Adapters, 2.5 Album Detail Screen, 2.6 Existing Photo Editor, 2.7 Favorites & Recycle Bin Subsystems, 2. Inventory of Existing Gallery Implementation (+11 more)

### Community 133 - "Phase 1: Architecture, Paginated Data Layer & Edit Ledger Setup"
Cohesion: 0.22
Nodes (9): 1. Features, 2. Files to be Created, 3. Existing Files Likely to be Modified, 4. Dependencies, 5. Permissions, 6. Risks, 7. Acceptance Tests, 8. Rollback Considerations (+1 more)

### Community 134 - "Phase 2: Gallery 2.0 Modern Timeline Grid & Album Navigation"
Cohesion: 0.25
Nodes (8): 2.1 Features, 2.2 Files to be Created, 2.3 Existing Files Likely to be Modified, 2.4 Dependencies, 2.5 Risks, 2.6 Acceptance Tests, 2.7 Rollback Considerations, Phase 2: Gallery 2.0 Modern Timeline Grid & Album Navigation

### Community 135 - "Phase 3: High-Performance Media Viewer (Zero Intent Overhead)"
Cohesion: 0.25
Nodes (8): 3.1 Features, 3.2 Files to be Created, 3.3 Existing Files Likely to be Modified, 3.4 Dependencies, 3.5 Risks, 3.6 Acceptance Tests, 3.7 Rollback Considerations, Phase 3: High-Performance Media Viewer (Zero Intent Overhead)

### Community 136 - "Phase 4: Non-Destructive Editing Engine Core & Undo/Redo Pipeline"
Cohesion: 0.25
Nodes (8): 4.1 Features, 4.2 Files to be Created, 4.3 Existing Files Likely to be Modified, 4.4 Dependencies, 4.5 Risks, 4.6 Acceptance Tests, 4.7 Rollback Considerations, Phase 4: Non-Destructive Editing Engine Core & Undo/Redo Pipeline

### Community 137 - "Phase 5: Transform & Crop Tools"
Cohesion: 0.25
Nodes (8): 5.1 Features, 5.2 Files to be Created, 5.3 Existing Files Likely to be Modified, 5.4 Dependencies, 5.5 Risks, 5.6 Acceptance Tests, 5.7 Rollback Considerations, Phase 5: Transform & Crop Tools

### Community 138 - "Phase 6: Professional Tone, Color & Detail Adjustments"
Cohesion: 0.25
Nodes (8): 6.1 Features, 6.2 Files to be Created, 6.3 Existing Files Likely to be Modified, 6.4 Dependencies, 6.5 Risks, 6.6 Acceptance Tests, 6.7 Rollback Considerations, Phase 6: Professional Tone, Color & Detail Adjustments

### Community 139 - "Phase 7: Curated Filters & Creative Markup Suite"
Cohesion: 0.25
Nodes (8): 7.1 Features, 7.2 Files to be Created, 7.3 Existing Files Likely to be Modified, 7.4 Dependencies, 7.5 Risks, 7.6 Acceptance Tests, 7.7 Rollback Considerations, Phase 7: Curated Filters & Creative Markup Suite

### Community 140 - "Phase 8: Original Media Safety & Non-Destructive Safe Exporter"
Cohesion: 0.25
Nodes (8): 8.1 Features, 8.2 Files to be Created, 8.3 Existing Files Likely to be Modified, 8.4 Dependencies, 8.5 Risks, 8.6 Acceptance Tests, 8.7 Rollback Considerations, Phase 8: Original Media Safety & Non-Destructive Safe Exporter

### Community 141 - "MarkupItem"
Cohesion: 0.06
Nodes (18): MarkupItem, MosaicItem, PathItem, ShapeItem, MarkupTool, ARROW, BORDER, CIRCLE (+10 more)

### Community 142 - "Phase 9: System Integration, Performance Hardening & Rollback Verification"
Cohesion: 0.29
Nodes (7): 9.1 Features, 9.2 Files to be Created, 9.3 Existing Files Likely to be Modified, 9.4 Dependencies, 9.5 Risks, 9.6 Acceptance Tests, Phase 9: System Integration, Performance Hardening & Rollback Verification

### Community 143 - "Override"
Cohesion: 0.14
Nodes (4): CreativeStudioCanvasView.OnLayerSelectionListener, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener, SeekBar.OnSeekBarChangeListener

### Community 145 - "Detailed Phase Breakdown"
Cohesion: 0.40
Nodes (4): 3. Rollback Strategy Summary, Detailed Phase Breakdown, Roadmap Overview, TWING GALLERY 2.0 — PHASED IMPLEMENTATION ROADMAP

### Community 146 - "MarkupCanvasView"
Cohesion: 0.06
Nodes (5): BorderItem, TextItem, WatermarkItem, MarkupCanvasView, OnMarkupChangedListener

### Community 147 - "WatermarkLayer"
Cohesion: 0.13
Nodes (6): WatermarkLayer, WatermarkMode, CENTER, CORNER_BOTTOM_LEFT, CORNER_BOTTOM_RIGHT, TILED_DIAGONAL

### Community 150 - "CropAspectRatio"
Cohesion: 0.07
Nodes (14): AspectRatioAdapter, AspectRatioViewHolder, OnAspectRatioSelectedListener, CropAspectRatio, CUSTOM, FREE, ORIGINAL, RATIO_16_9 (+6 more)

### Community 154 - "EnhancementMode"
Cohesion: 0.15
Nodes (8): EnhancementMode, BACKGROUND_CLEANUP, BLACK_AND_WHITE, CONTRAST, GRAYSCALE, MAGIC_COLOR, ORIGINAL, SHADOW_REMOVAL

### Community 155 - "EnhancementCallback"
Cohesion: 0.24
Nodes (5): EnhancementCallback, PhotoUnblurPipeline, UnblurMode, ITERATIVE_DECONVOLUTION, MILD_SHARPEN

### Community 156 - "MediaPagerAdapter"
Cohesion: 0.26
Nodes (3): MediaPagerAdapter, OnTapListener, PageVH

### Community 158 - "Override"
Cohesion: 0.23
Nodes (3): InpaintingCallback, InpaintingCallback, InpaintingCallback

### Community 160 - "PerspectiveQuad"
Cohesion: 0.12
Nodes (4): PerspectiveQuad, OnPerspectiveChangedListener, PerspectiveOverlayView, PhotoEditorEngineTest

### Community 162 - "EditState"
Cohesion: 0.16
Nodes (3): ExportCallback, EditHistoryTracker, EditState

### Community 164 - "SelectionTool"
Cohesion: 0.18
Nodes (8): SelectionTool, BRUSH, CIRCLE, LASSO, MOVE_RESIZE, OBJECT_DETECT, OUTPAINT, PEOPLE_DETECT

### Community 165 - "2.3 Modified Existing Files"
Cohesion: 0.11
Nodes (17): 1. Absolute Data Protection Guarantees, 2.1 Newly Added Source Packages & Classes, 2.2 Newly Added XML Layouts & Drawables, 2.3 Modified Existing Files, 2.4 Database Migrations, 2.5 Workers and Background Services, 2. Inventory of Changes Introduced in Gallery 2.0, 3. Disabling Gallery 2.0 (Zero-Code / Instant Soft Disable) (+9 more)

### Community 167 - "AiEraserAcceptanceTest"
Cohesion: 0.13
Nodes (5): AiEraserAcceptanceTest, ExemplarInpainter, FastMarchingTeleaInpainter, PixelPoint, LocalInpaintingEngine

### Community 168 - "TWING GALLERY 2.0 — FINAL QUALITY ASSURANCE & ACCEPTANCE REPORT"
Cohesion: 0.12
Nodes (15): 1. Executive Summary, 3. Scoped-Out Features & External Model Dependencies, 4. Hardware & Testing Environment Specifications, 5. Automated Android Test Suite Execution, 6.1 Memory Management & OOM Prevention, 6.2 Security & Privacy Guarantees, 6.3 Database Segregation, 6. Performance, Memory & Security Audit (+7 more)

### Community 171 - "SocialTemplate"
Cohesion: 0.08
Nodes (18): SocialTemplate, INSTAGRAM_POST_PORTRAIT, INSTAGRAM_POST_SQUARE, INSTAGRAM_STORY, PROFILE_PICTURE, WHATSAPP_STATUS, YOUTUBE_COMMUNITY, YOUTUBE_THUMBNAIL (+10 more)

### Community 172 - "FilterType"
Cohesion: 0.12
Nodes (13): FilterType, BLACK_AND_WHITE, CINEMATIC, COOL, DRAMATIC, NATURAL, ORIGINAL, PORTRAIT (+5 more)

### Community 173 - "LayerType"
Cohesion: 0.22
Nodes (6): LayerType, FRAME, IMAGE, STICKER, TEXT, WATERMARK

### Community 174 - "CallStateReceiver"
Cohesion: 0.22
Nodes (9): 2.2 `CallStateMonitor` (Telephony State Detector), 7. Final Technical Architecture Decision, Architectural Rules Enforced in TWING:, 4. Ingestion Architecture: Three Legal Integration Pathways, 8. Implementation Steps & Rollout Strategy, Pathway A: Storage Access Framework (SAF) Folder Link (Recommended for Automatic Sync), Pathway B: Universal "Share to TWING" Intent Handler (Zero Permissions Needed), Pathway C: In-App VoIP Call Recording (Existing & 100% Functional) (+1 more)

### Community 177 - "CompressionQuality"
Cohesion: 0.40
Nodes (4): CompressionQuality, BALANCED, COMPACT, HIGH

### Community 178 - "ViewHolder"
Cohesion: 0.21
Nodes (5): DateHeaderViewHolder, ContactsAdapter, ContactViewHolder, HeaderViewHolder, ViewHolder

### Community 182 - "Type"
Cohesion: 0.12
Nodes (15): Type, BRIGHTNESS, CLARITY, CONTRAST, EXPOSURE, FADE, GRAIN, HIGHLIGHTS (+7 more)

### Community 184 - "Detailed Test Analysis:"
Cohesion: 0.33
Nodes (6): 3. Controlled Audio Test Protocol & Empirical Measurements, Detailed Test Analysis:, Test 1: Outside of Calls (Idle / Normal State), Test 5: After Call Ends, Test Results Matrix, Tests 2, 3, and 4: During Cellular Call (`MODE_IN_CALL`)

### Community 185 - "3. Manufacturer-by-Manufacturer Native Recording Breakdown"
Cohesion: 0.40
Nodes (5): 3.1 Vivo (Target: Vivo T3x 5G / Funtouch OS 14–16 & OriginOS), 3.2 OnePlus / OPPO / Realme (Target: OnePlus Nord CE 3 Lite 5G / OxygenOS & ColorOS 13–16), 3.3 Samsung (Galaxy S, A, M, F Series / One UI 4–7 on Android 12–16), 3.4 Xiaomi / Redmi / POCO (MIUI 13/14 & HyperOS 1/2 on Android 12–16), 3. Manufacturer-by-Manufacturer Native Recording Breakdown

### Community 190 - "FilterAdapter"
Cohesion: 0.36
Nodes (3): FilterAdapter, FilterViewHolder, OnFilterSelectedListener

## Knowledge Gaps
- **144 isolated node(s):** `FREE`, `ORIGINAL`, `SQUARE_1_1`, `RATIO_4_3`, `RATIO_3_4` (+139 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 951 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **86 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `AlarmItem`, `AppCallAdapter`, `.initViews`, `AppCallItem`, `.loadGalleryData`, `AppUpdateHelper`, `list`, `CallSyncWorker`, `TelemetryGraphView`, `Override`, `ImageViewerActivity`, `com.zez.app.model.ChatMessage`, `.bind`, `com.zez.app.model.ChatConversation`, `com.zez.app.model.ChatConversation`, `android.content.Context`, `ChatLocalDbHelper`, `PreferencesHelper`, `ChatActivity.java`, `CallStateReceiver`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `MainActivity` (e.g. with `1. Automated & Emulator Test Results (Pixel 6a / Android 14–17)` and `2.1 Navigation & Entry Point`) actually correct?**
  _`MainActivity` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `FREE`, `ORIGINAL`, `SQUARE_1_1` to the rest of the system?**
  _144 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `StudioLayer` be split into smaller, more focused modules?**
  _Cohesion score 0.13076923076923078 - nodes in this community are weakly interconnected._
- **Why does `ChatActivity` connect `ChatActivity` to `CallActivity.java`, `android.content.Context`, `ChatLocalDbHelper`, `AlarmRingingService.java`, `ChatActivity.java`, `ChatUser`, `ChatApiClient`, `ImageViewerActivity`, `com.zez.app.model.ChatMessage`, `list`, `Override`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **Should `AlarmItem` be split into smaller, more focused modules?**
  _Cohesion score 0.11605937921727395 - nodes in this community are weakly interconnected._
- **Why does `EraserCanvasView` connect `EraserCanvasView` to `.executeCurrentAction`, `SelectionTool`, `Hardware Info & Performance Telemetry (C6)`, `SelectionEngine`, `android.graphics.Bitmap`, `DetectedObjectBox`, `list`, `DocumentCropOverlayView`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._