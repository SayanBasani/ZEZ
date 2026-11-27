# 04 — Alarms (Tab 2) & Gallery + PDF/PPT Studio (Tab 3) Specification

---

## Part A: Alarms Module (Tab 2: `R.id.nav_alarms`)

Replicates ZezAndroid's full local + cloud-synced alarm engine without any notification noise.

### 1. Data Model (`AlarmItem.java`)
```java
public long id;                  // Unique ID (System.currentTimeMillis() or server-assigned)
public int hour;                 // 0..23
public int minute;               // 0..59
public String label;             // Default "Alarm"
public boolean isEnabled;        // Active toggle
public boolean isVibrate;        // Default true
public boolean isJitMode;        // Special Jitter/Persistent mode: repeats every 60s if active
public boolean[] daysOfWeek;     // length 7: [Sun, Mon, Tue, Wed, Thu, Fri, Sat]
public String ringtoneUri;       // "" -> system default alarm ringtone
public String ringtoneTitle;     // Default "Default Alarm Ringtone"
```

### 2. Scheduling & Persistence (`AlarmHelper.java`)
- **Local Store**: `Gson`-serialized JSON array stored in `SharedPreferences` (`"alarms_list"`), plus a `"deleted_alarm_ids"` set queued until confirmed by backend sync.
- **Exact Alarm Scheduling**:
  - Calculates next fire time: if all `daysOfWeek` are false -> one-shot (today or tomorrow if time already passed); else -> nearest enabled weekday >= now.
  - Uses `AlarmManager.setAlarmClock(new AlarmClockInfo(triggerAtMs, showIntent), pendingIntent)` on API 21+ (reliable across Doze mode and displays the clock icon in system status bar; falls back to `setExactAndAllowWhileIdle` if needed; wraps `SecurityException` on Android 14 `SCHEDULE_EXACT_ALARM` denial).
  - Reschedules all enabled alarms on `CompanionApplication.onCreate()` and `BootReceiver.onReceive()`.
- **Multi-Select Management UI (`view_alarms.xml`)**:
  - Top bar `layout_alarms_quick_bar` with `"Select"` button (`btn_alarms_enter_select`), or long-press any `AlarmAdapter` card to enter multi-select mode.
  - Multi-select action bar (`layout_alarms_selection_bar`): Close, `"N selected"`, **All**, **Turn On** (`#10B981`), **Turn Off** (`#F59E0B`), **Delete** (`#EF4444` with confirmation dialog).
- **Add / Edit Dialog (`dialog_edit_alarm.xml`)**:
  - `TimePicker` (`timePickerMode="spinner"`)
  - `Alarm Label` (`TextInputEditText`)
  - `Alarm Ringtone` picker (`RingtoneManager.ACTION_RINGTONE_PICKER`, `TYPE_ALARM`, `requestCode = RINGTONE_PICKER_REQUEST_CODE`)
  - Vibrate toggle, Jit Mode toggle (60s repeat), 7 weekday toggle chips (`S M T W T F S`).
- **Bidirectional Backend Sync (`syncWithBackend`)**:
  - Calls `ApiClient.syncAlarms(context, localAlarmsJson, deletedIds)`.
  - Merges `activeAlarms` pushed remotely from the web Admin Panel (`created_by = 'admin'`), removes `deletedAlarmIds` deleted remotely, re-saves locally, and calls `rescheduleAllAlarms()` + UI refresh callback!

### 3. Alarm Firing Pipeline (`AlarmReceiver`, `AlarmRingingService`, `AlarmFiringActivity`)
1. `AlarmReceiver.onReceive(context, intent)`:
   - Extracts `alarmId`, `label`, `ringtoneUri`, `isVibrate`, `isJitMode`.
   - Re-arms the next occurrence automatically (either next repeating weekday OR +60 seconds if `isJitMode == true`, OR disables if one-shot).
   - Starts `AlarmRingingService` via `ContextCompat.startForegroundService(...)`.
2. `AlarmRingingService` (Foreground service of type `mediaPlayback` active **ONLY while ringing**):
   - Builds high-priority notification with `.setFullScreenIntent(firingPendingIntent, true)` targeting `AlarmFiringActivity` plus a notification Dismiss action.
   - Plays alarm audio using `MediaPlayer` configured with `AudioAttributes.USAGE_ALARM` (fallback `RingtoneManager.getDefaultUri(TYPE_ALARM)`) in looping mode + `Vibrator` waveform pattern if `isVibrate == true`.
3. `AlarmFiringActivity`:
   - Uses `setShowWhenLocked(true)`, `setTurnScreenOn(true)` (API 27+) + `KeyguardManager.requestDismissKeyguard(...)`.
   - Shows current time, alarm label, `"Dismiss"` (stops `AlarmRingingService` + closes activity) and `"Snooze (5 min)"` (schedules 5-minute snooze via `AlarmHelper` + stops service).

---

## Part B: Gallery + PDF/PPT Studio (Tab 3: `R.id.nav_gallery`)

Vivo-Album-inspired media browser with integrated multi-page **PDF & PowerPoint generator**.

### 1. Media & Album Engine (`MediaHelper.java`, `FavoritesHelper.java`, `RecycleBinHelper.java`)
- **`MediaHelper.getImagesAndVideos(Context)`**:
  - Queries `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` and `MediaStore.Video.Media.EXTERNAL_CONTENT_URI`, filters out items marked in `RecycleBinHelper`, sorts descending by `DATE_ADDED`.
  - Extracts `id`, `uri`, `displayName`, `dateAdded`, `size`, `width`, `height`, `duration` (videos), `bucketId`, `bucketName`, `isVideo`.
- **`MediaHelper.getAlbums(Context)`**:
  - Builds virtual smart albums first (`Favorites`, `Videos`, `Recycle Bin` with `AlbumItem.TYPE_BIN`), followed by device folders grouped by `BUCKET_ID` with cover thumbnail & count.

### 2. Main Gallery Screen (`view_gallery.xml`)
- **Two Top Tabs (`TabLayout`)**:
  - **Photos** (`GALLERY_TAB_PHOTOS = 0`):
    - Horizontal filter chips: `All`, `Photos`, `Videos` (`chip_all`, `chip_photos`, `chip_videos`).
    - 3-column `GridLayoutManager` (`GalleryAdapter`) with date-section headers (`SpanSizeLookup` spans 3 for headers, 1 for tiles), duration badge for videos, and multi-select checkboxes on long-press.
  - **Albums** (`GALLERY_TAB_ALBUMS = 1`):
    - 2-column `GridLayoutManager` (`AlbumsAdapter`).
    - Tapping Recycle Bin opens `recycler_bin` restore/purge screen; tapping any folder or smart album launches `AlbumDetailActivity`.
- **Empty Permission State**:
  - If `!hasStoragePermission()`, displays `btn_gallery_grant_permission` (`"Grant Storage Permission"`) which triggers the API-level appropriate media permission prompt.

### 3. Vivo-Style Multi-Select Bottom Bar (`gallery_action_bar`)
Appears at the bottom whenever 1 or more items are selected in `GalleryAdapter`, changing the top bar title to `"<N> selected"`:

| Button | ID | Behavior |
| :--- | :--- | :--- |
| **Share** | `btn_gallery_share` | `Intent.ACTION_SEND` (1 item) or `ACTION_SEND_MULTIPLE` (>1 item) with `FLAG_GRANT_READ_URI_PERMISSION` |
| **Move/Copy** | `btn_gallery_move` | Optional local folder move/copy or coming-soon stub |
| **Create** | `btn_gallery_create` | Can directly launch `PdfGeneratorActivity` or collage/album prompt |
| **Delete** | `btn_gallery_bin` | Soft-moves selected items to `RecycleBinHelper` + removes from grid (`<N> item(s) moved to Recycle Bin`) |
| **More** | `btn_gallery_more` | Opens `PopupMenu`: `Hide`, `Move to Private space`, `Copy to clipboard`, **`PDF/PPT`** -> `launchPdfGeneratorFromGallery()`, `Print` |

### 4. Full-Screen Viewer & Editor
- **`ImageViewerActivity`**:
  - Receives parallel `ArrayList` extras (`EXTRA_URI_LIST`, `EXTRA_ID_LIST`, `EXTRA_NAME_LIST`, `EXTRA_SIZE_LIST`, `EXTRA_W_LIST`, `EXTRA_H_LIST`, `EXTRA_DATE_LIST`, `EXTRA_VIDEO_LIST`, `EXTRA_DUR_LIST`, `EXTRA_START_IDX`) for smooth horizontal `ViewPager2` swiping across photos/videos.
  - Supports pinch-to-zoom, inline video playback, EXIF/file info dialog, Favorite toggle (`FavoritesHelper`), Delete to Recycle Bin (returns `RESULT_OK`), Share, and Edit -> `ImageEditActivity`.
- **`ImageEditActivity`**:
  - Non-destructive crop, 90° rotate, flip, brightness/contrast/filter adjustments, saving edited copy back into `MediaStore` / Pictures folder.

### 5. Document Generator (`PdfGeneratorActivity`, `PdfDocumentHelper`, `PptxGenerator`)
Launched from Gallery multi-select `More -> PDF/PPT` with `EXTRA_IMAGE_URIS`:
- **Top Subheader**:
  - Format Dropdown (`btn_format_dropdown`): switches between `"Generate PDF"` (`isPdfMode = true`) and `"Generate PPT"` (`isPdfMode = false`).
  - Add More Images (`btn_add_more` / `btn_adjust_add` using `ActivityResultContracts.GetMultipleContents()` `"image/*"`).
- **Two Modes**:
  - **Preview Mode (`rv_pdf_preview`)**: vertical `LinearLayoutManager` rendering full-width preview cards with page badges (`"1/5"`, `"2/5"`). Top bar has **Share** (`btn_pdf_share`) and **Save** (`btn_pdf_save`).
  - **Adjust Mode (`rv_pdf_adjust`)**: 2-column `GridLayoutManager` with **`ItemTouchHelper`** drag-and-drop reordering (`UP | DOWN | LEFT | RIGHT` swaps `imageUris` live and updates `1/N` badges), `"Done"` button, and `"Add"` button.
- **Bottom Action Bar (`layout_pdf_bottom_bar`)**:
  - `"View files"` (`btn_bottom_view_files`): opens `dialog_pdf_files` bottom sheet listing all `.pdf` / `.pptx` files in `getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)/PDFs` sorted newest-first, with click-to-open and share/delete actions.
  - `"Adjust image"` (`btn_bottom_adjust_image`): enters Drag & Drop reorder mode.
- **PDF Builder (`PdfDocumentHelper.createPdf`)**:
  - Standard A4 canvas (`PAGE_WIDTH_A4 = 595`, `PAGE_HEIGHT_A4 = 842` at 72 dpi), white background, decodes scaled bitmap (`<= 1190x1684`) maintaining aspect ratio, writes to `Documents/PDFs/Companion_yyyyMMdd_HHmmss.pdf`.
- **PPTX Builder (`PptxGenerator.createPresentation`)**:
  - Pure `ZipOutputStream` generating compliant OpenXML 16:9 presentation (`cx="12192000" cy="6858000"` EMUs), embedding `[Content_Types].xml`, `_rels/.rels`, `ppt/presentation.xml`, per-slide `slideN.xml` + `.rels` + `ppt/media/imageN.jpeg`.
- **`FileProvider` Bug Fix (Mandatory)**:
  - Quacky's `PdfGeneratorActivity.java` line 286 mistakenly passed `getPackageName() + ".fileprovider"`, whereas `AndroidManifest.xml` registered `${applicationId}.provider`!
  - In the Companion App, `PdfGeneratorActivity.shareFile()` (and saved-files sheet viewer/share) MUST use **`getPackageName() + ".provider"`** consistently!
