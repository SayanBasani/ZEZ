# 03 — YMusic Module Specification (Tab 1: Primary Launch Screen)

In Quacky, `MusicActivity` was a secondary screen launched from a top-bar button. In this Companion App, **YMusic is Tab 1 (`R.id.nav_music`) and the default home screen**.

---

## 1. UI Architecture: Embedding YMusic as Tab 1

Host `view_music.xml` inside `MainActivity` (as a top-level view container inside the `FrameLayout` above `BottomNavigationView`, or inside a dedicated `MusicFragment` managed by `MainActivity`) with these exact components from `activity_music.xml` + `MusicActivity.java`:

1. **DrawerLayout (`@+id/drawer_music`)**:
   - Slide-out navigation drawer with:
     - `drawer_item_trending`, `drawer_item_top_tracks`, `drawer_item_viral`, `drawer_item_recommended`, `drawer_item_charts`, `drawer_item_downloads`, `drawer_item_history`
     - `drawer_item_ytmusic` (`switchMode(true)` -> `"YT Music"` badge `#EF4444` loading `"Viral 50"`)
     - `drawer_item_sleep_timer` -> opens Sleep Timer dialog
     - *Remove `drawer_item_back_app` ("Back to Chat")* — no chat exists in the Companion App!
2. **Top Header (`@+id/app_bar_music`)**:
   - `btn_music_menu` (opens drawer)
   - *Remove `btn_music_back`* when embedded in Tab 1 (no back arrow needed on root tab!)
   - Title `"YMusic"` (`tv_music_main_title`) + red badge `"Music"` (`#FF0000`) / `"YouTube Music"` (`#EF4444`) (`tv_music_mode_badge`) + subtitle `"Stream, Search & Download"`
   - `btn_music_search_toggle` -> expands `layout_music_search_box`
3. **Expandable Search Bar (`@+id/layout_music_search_box`)**:
   - `et_music_search` (`imeOptions="actionSearch"`, `singleLine="true"`)
   - `btn_music_search_clear`
   - **300ms Debounced Live Autocomplete**: `searchDebounceHandler.postDelayed(..., 300)` calls `YouTubeApiHelper.getSuggestions(query)`. When empty/`length <= 1`, displays `MusicHistoryManager.getSearchHistory()` (`rv_suggestions` with Clear History option).
4. **Horizontal Category Chips (`@+id/scroll_music_categories`)**:
   - `Trending`, `Top Tracks`, `Viral 50`, `Recommended`, `Charts`, `Downloads`, `History`
   - Active chip styling: `bg_mode_toggle_active` + white text; inactive: `bg_mode_toggle_normal` + `theme_text_secondary`.
5. **Downloads Sub-tabs (`@+id/layout_downloads_subtabs`)**:
   - Shown when `"Downloads"` chip is selected:
     - `"⏳ Downloading (N)"` (`ActiveDownloadAdapter` showing live download progress/speed + Cancel button)
     - `"Completed"` (`MusicAdapter` populated from `MusicDownloadHelper.getDownloadedTracks()`)
6. **History Header (`@+id/layout_history_header`)**:
   - Shown when `"History"` chip is selected: displays `"Recently Played (N)"` + Clear History button (`AlertDialog` confirmation).
7. **Infinite Scroll Track List (`@+id/rv_tracks` inside `SwipeRefreshLayout`)**:
   - When scrolled within 4 items of the bottom (`(visibleCount + firstVisible) >= totalCount - 4 && totalCount >= 10`) and `currentContinuationToken` is non-empty, triggers `YouTubeApiHelper.searchContinuation(token)` to append next page.
8. **Dockable Mini-Player & Full Player BottomSheet (`bottom_sheet_music_player.xml`)**:
   - **Mini-player**: visible whenever `MusicPlayerManager.getCurrentTrack() != null` (both inside YMusic and across Tabs 2/3/4 above `BottomNavigationView`). Controls: Thumbnail, Title, Artist, Play/Pause, Next, Close (`stop()`).
   - **Full Player BottomSheet**:
     - Audio / Video mode toggle (`btn_mode_audio`, `btn_mode_video`). In Video mode, mounts a `WebView` into `layout_player_video_container` loading `https://www.youtube.com/embed/<videoId>?autoplay=1` (`setJavaScriptEnabled(true)`, `setDomStorageEnabled(true)`, `setMediaPlaybackRequiresUserGesture(false)`).
     - Album art, Title, Artist, `SeekBar` + current/total time, Play/Pause FAB, Previous, Next, Shuffle, Repeat (`NONE` -> `ALL` -> `ONE`), Download button (`MusicDownloadHelper.showDownloadDialog`), Share (`https://www.youtube.com/watch?v=<id>`), Queue sheet (`btn_player_queue_top` / `btn_player_queue_btn`), More track options (`musicAdapter.showTrackOptionsSheet`).

---

## 2. YouTube Extraction & Search (`YouTubeApiHelper.java`)

Pure OkHttp client (no heavy Python runtime required on Android):

- **Search & Suggestions**:
  - Live suggestions: YouTube Suggest HTTP endpoint (`clients=firefox` / Google Suggest API).
  - Track Search & Category Queries (`Trending`, `Top tracks`, `Viral 50`, `Recommended`, `Charts`): queries YouTube Innertube `/youtubei/v1/search` and parses `videoRenderer` / `musicResponsiveListItemRenderer` + `continuationCommand.token` for infinite scrolling.
- **Audio Stream Resolution (`extractAudioStream(videoId)`)** — multi-strategy cascade:
  1. Direct Innertube `/youtubei/v1/player` with **VISIONOS** and **ANDROID_VR** client contexts (bypasses web signature cipher on direct audio `adaptiveFormats` such as Opus/AAC `.m4a`).
  2. Falls back to **Zez-Backend Proxy** (`GET <baseUrl>/api/music/resolve?videoId=<id>`) if direct extraction fails on restricted tracks.
  3. Last-resort fallback URL: `<baseUrl>/api/music/stream?videoId=<id>`.

---

## 3. Playback Engine (`MusicPlayerManager` & `MusicPlayerService`)

Preserve every lifecycle & audio-focus stability fix implemented in `MusicPlayerService.java` (verified in v1.0.61):

### 3.1 `MusicPlayerService` Contract
- **Foreground Channel**: `music_playback_channel` (`IMPORTANCE_LOW`, silent). Notification appears **ONLY while actively playing/preparing or paused in-session**; removed immediately on `ACTION_STOP` or notification swipe!
- **Service Return Sticky Flag**: `onStartCommand(...)` MUST return **`START_NOT_STICKY`** so Android never silently restarts a stopped player notification after swipe-dismiss or OS kill.
- **Notification Actions & Swipe Dismissal (`deleteIntent`)**:
  - Actions: `ACTION_PREV`, `ACTION_PLAY_PAUSE`, `ACTION_NEXT`, `ACTION_STOP`.
  - MediaStyle (`androidx.media.app.NotificationCompat.MediaStyle`) linked to `MediaSessionCompat` (handling lockscreen controls, Bluetooth headset buttons, and media artwork).
  - **Dismissable when paused**: `.setOngoing(isPlaying || isPreparing)` — false when paused so user can swipe away the notification!
  - **`deleteIntent`**: `PendingIntent.getService(..., ACTION_STOP)` attached via `.setDeleteIntent(...)` so swiping away a paused notification calls `stopPlayback()` + `stopSelf()` cleanly instead of leaving a phantom paused state.
- **Audio Focus Handling (`resumeOnFocusGain`)**:
  - `AUDIOFOCUS_LOSS_TRANSIENT` or `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK`: pauses playback (`shouldResume = isPlaying`) and sets `resumeOnFocusGain = shouldResume`.
  - `AUDIOFOCUS_GAIN`: resumes **ONLY if `resumeOnFocusGain == true`** (and clears `resumeOnFocusGain = false`). When user manually hits Pause, `pausePlayback()` explicitly clears `resumeOnFocusGain = false` so the track never unexpectedly auto-resumes later!
  - `AUDIOFOCUS_LOSS` (permanent loss): stops playback cleanly rather than holding paused state indefinitely.
- **Offline-First Playback Priority (`playTrack(MusicTrack)`)**:
  1. Checks `track.getLocalFilePath()`:
     - If `content://...` URI -> `mediaPlayer.setDataSource(ctx, uri)`.
     - Else -> opens `File(lp)` (or fallback `MusicDownloadHelper.getDestinationFile(this, file.getName())`) and binds via `FileInputStream.getFD()`.
  2. Only if no valid local file exists -> resolves online stream (`YouTubeApiHelper.extractAudioStream`) with Safari/YouTube `User-Agent`, `Origin`, `Referer`, and `Range: bytes=0-` headers -> `MusicDownloadManager.registerCapturedStreamUrl(videoId, streamUrl)`.
- **WakeLock**: acquires `PARTIAL_WAKE_LOCK` only while actively playing/preparing; releases immediately on pause/stop/destroy.

### 3.2 `MusicPlayerManager` & `MusicHistoryManager`
- Singleton managing `List<MusicTrack> queue`, `currentIndex`, `isShuffle`, `repeatMode` (`REPEAT_MODE_NONE`, `REPEAT_MODE_ALL`, `REPEAT_MODE_ONE`), `isVideoMode`, and Sleep Timer countdown.
- `MusicHistoryManager` (`SharedPreferences("zez_music_history")`):
  - `KEY_SEARCH_HISTORY = "search_history"` -> keeps last **10** unique search queries (`MAX_SEARCH_HISTORY = 10`).
  - `KEY_PLAYBACK_HISTORY = "playback_history"` -> keeps last **50** unique `MusicTrack` items (`MAX_PLAYBACK_HISTORY = 50`).

---

## 4. Offline Downloader & Device MediaStore Export

### 4.1 `MusicDownloadManager.java` & `DownloadTask.java`
- Uses an isolated background download queue (`ConcurrentHashMap<String, DownloadTask>` + executor).
- **Stream Resolution Reuse**: if the user already streamed the song, `registerCapturedStreamUrl(videoId, url)` avoids a second resolution call.
- **Chunked Range Downloading**:
  - Downloads in **512 KB HTTP Range chunks** (`Range: bytes=<start>-<end>`) with retry logic to defeat YouTube bandwidth throttling and handle mobile drop-outs cleanly.
  - Calculates live download percentage, downloaded bytes vs total bytes, and KB/s speed for `ActiveDownloadAdapter`.
- **Magic-Byte Format Verification**:
  - Inspects first bytes (`ftyp` for `.mp4`/`.m4a`, WebM EBML header `0x1A45DFA3`, MP3 `ID3`) to save the proper file extension so stock Android music players and `MediaPlayer` decode it reliably.
- **Backend Fallback**: if direct YouTube download URL hits HTTP `403`, falls back automatically to `<baseUrl>/api/music/download?videoId=<id>&title=<title>`.

### 4.2 `QuackyStorageHelper.java`
- Initializes clean app storage folders in `initDirectories(Context)` (`Environment.DIRECTORY_MUSIC`, `DIRECTORY_MOVIES`, app-private `filesDir/music`).
- After downloading audio/video, writes/copies directly into public **MediaStore (`MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` / `RelativePath = Music/<AppName>`)** on Android 10+ (or `MediaScannerConnection.scanFile` on Android 8–9) so downloaded songs appear **immediately** in the phone's built-in Music player and file manager even offline!
- `MusicDownloadHelper.getDownloadedTracks(Context)` scans MediaStore / downloads directory so tracks stay playable offline inside Tab 1 -> `Downloads -> Completed`.
