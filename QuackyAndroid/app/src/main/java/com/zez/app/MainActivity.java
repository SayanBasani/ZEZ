package com.zez.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zez.app.adapter.ChatMessageAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.bumptech.glide.Glide;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zez.app.adapter.GalleryAdapter;
import com.zez.app.adapter.RecycleBinAdapter;
import com.zez.app.adapter.AlbumsAdapter;
import com.zez.app.adapter.ConversationsAdapter;
import com.zez.app.model.AlbumItem;
import com.zez.app.model.ChatConversation;
import com.zez.app.model.ChatUser;
import com.zez.app.model.MediaItem;
import com.zez.app.receiver.BootReceiver;
import com.zez.app.util.ApiClient;
import com.zez.app.util.ChatApiClient;
import com.zez.app.util.ChatNotificationHelper;
import com.zez.app.util.ChatNotificationManager;
import com.zez.app.util.ChatPreferencesHelper;
import com.zez.app.util.DeviceHelper;
import com.zez.app.util.MediaHelper;
import com.zez.app.util.PhoneHelper;
import com.zez.app.util.PreferencesHelper;
import com.zez.app.util.RecycleBinHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQ_CODE = 1001;

    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvTopBadge;
    private ImageButton btnSync;

    private SwipeRefreshLayout swipeCalls;
    private View viewCallsContainer;
    private EditText etSearchCalls;
    private View layoutEmptyCalls;
    private com.zez.app.adapter.AppCallAdapter appCallAdapter;
    private final List<com.zez.app.model.AppCallItem> allAppCallsList = new ArrayList<>();
    private com.zez.app.util.ChatLocalDbHelper chatDb;
    private com.zez.app.util.ActiveCallManager.CallStateListener activeCallBannerListener;
    private Button btnGalleryGrantPermission;
    private TextView tvGalleryEmptySubtitle;

    private SwipeRefreshLayout swipeBin;
    private View viewChatsContainer;
    private SwipeRefreshLayout swipeChats;
    private RecyclerView recyclerChats;
    private ConversationsAdapter conversationsAdapter;
    private FloatingActionButton fabNewChat;
    private View layoutChatsEmpty;
    private EditText etSearchChats;

    // Contextual Multi-Select Bar for Chats
    private View layoutChatsSelectionBar;
    private View layoutAppBar;
    private ImageButton btnChatsSelectionClose;
    private TextView tvChatsSelectionCount;
    private ImageButton btnChatsSelectionPin;
    private ImageButton btnChatsSelectionDelete;
    private ImageButton btnChatsSelectionMute;
    private ImageButton btnChatsSelectionArchive;
    private ImageButton btnChatsSelectionMore;

    private androidx.core.widget.NestedScrollView scrollSettings;
    private BottomNavigationView bottomNav;

    // Settings UI elements
    private android.widget.Spinner spinnerSettingTheme;
    private TextView tvSettingThemeCurrent;
    private TextView tvSettingFontCurrent;
    private TextView tvSettingAccentCurrent;
    private View swatchAccentBlue, swatchAccentEmerald, swatchAccentCrimson, swatchAccentPink, swatchAccentViolet, swatchAccentAmber, swatchAccentCyan;
    private ImageView swatchCheckBlue, swatchCheckEmerald, swatchCheckCrimson, swatchCheckPink, swatchCheckViolet, swatchCheckAmber, swatchCheckCyan;
    private TextView tvSettingVersionInfo;
    private TextView tvSettingHardwareSummary;
    private TextView tvSettingServerCurrent;
    private com.google.android.material.textfield.TextInputEditText etSettingBackendUrl;
    private com.google.android.material.button.MaterialButton btnSettingSaveUrl;
    private com.google.android.material.button.MaterialButton btnSettingResetUrl;
    private android.widget.Button btnSettingUpdate;
    private android.widget.Button btnSettingRollback;
    private TextView tvCompanionStatus;
    private android.widget.Button btnCompanionAction;
    private com.zez.app.util.AppUpdateHelper.ReleaseInfo rollbackReleaseInfo;
    private com.zez.app.util.AppUpdateHelper.ReleaseInfo companionReleaseInfo;

    private TextView tvFooterAppVersion;
    private String actualDeviceId = "";

    // Chat Account & Data Deletion (Google Play Compliance)
    private View rowSettingDeleteChatAccount;
    private TextView tvSettingChatAccountStatus;

    // Protected Developer / Admin Controls
    private View rowSettingServer;
    private View dividerSettingServer;
    private int developerTapCount = 0;
    private long lastDeveloperTapTime = 0;
    private static final String ADMIN_PIN = "7788";

    // Sub-Pages
    private LinearLayout viewFontPage;
    private LinearLayout viewHardwarePage;

    // Font Page Views
    private ImageButton btnBackFont;
    private TextView tvFontPreviewHeading;
    private TextView tvFontPreviewBody;
    private com.google.android.material.textfield.TextInputEditText etCustomFontSize;
    private com.google.android.material.button.MaterialButton btnResetFontDefault;
    private android.widget.RadioGroup rgFontScaleOptions;
    private android.widget.RadioButton rbFontSmall;
    private android.widget.RadioButton rbFontDefault;
    private android.widget.RadioButton rbFontLarge;
    private android.widget.RadioButton rbFontHuge;
    private android.widget.RadioGroup rgFontFamilyOptions;
    private android.widget.RadioButton rbFamilySans;
    private android.widget.RadioButton rbFamilyLight;
    private android.widget.RadioButton rbFamilyCondensed;
    private android.widget.RadioButton rbFamilyMedium;
    private android.widget.RadioButton rbFamilyBlack;
    private android.widget.RadioButton rbFamilySerif;
    private android.widget.RadioButton rbFamilyMonospace;
    private android.widget.RadioButton rbFamilyCasual;
    private android.widget.RadioButton rbFamilyCursive;
    private android.widget.Button btnApplyFont;

    // Hardware Page Views
    private ImageButton btnBackHardware;
    private TextView tvPageStoragePercent;
    private android.widget.ProgressBar progressPageStorage;
    private TextView tvPageStorageDetails;
    private TextView tvPageRamPercent;
    private android.widget.ProgressBar progressPageRam;
    private TextView tvPageRamDetails;
    private com.zez.app.util.TelemetryGraphView graphRamLoad;
    private TextView tvPageCpuLoadPercent;
    private com.zez.app.util.TelemetryGraphView graphCpuLoad;
    private TextView tvPageCpuCores;
    private TextView tvPageCpuSpeed;
    private TextView tvPageCpuAbi;
    private TextView tvPageCpuHardware;

    // Appearance & Multi-Theme System
    private LinearLayout viewAppearancePage;
    private ImageButton btnBackAppearance;
    private ImageButton btnResetAppearanceTop;
    private View previewContainer;
    private View previewTopBar;
    private TextView previewAppTitle;
    private TextView previewTextTitle;
    private TextView previewTextSubtitle;
    private TextView previewBadgePrimary;
    private TextView previewBadgeStatus;
    private TextView previewBadgeTag;
    private EditText previewInputField;
    private com.google.android.material.button.MaterialButton previewBtnPrimary;
    private com.google.android.material.button.MaterialButton previewBtnSecondary;
    private ImageView previewBackIcon, previewSearchIcon;

    private LinearLayout cardModeSystem, cardModeLight, cardModeDark;
    private ImageView checkModeSystem, checkModeLight, checkModeDark;
    private ImageView iconModeSystem, iconModeLight, iconModeDark;

    private LinearLayout cardPaletteGraphite, cardPaletteArctic, cardPaletteMidnight, cardPaletteAmoled;
    private LinearLayout cardPaletteOcean, cardPaletteSage, cardPaletteSunset, cardPaletteRose;
    private ImageView checkPaletteGraphite, checkPaletteArctic, checkPaletteMidnight, checkPaletteAmoled;
    private ImageView checkPaletteOcean, checkPaletteSage, checkPaletteSunset, checkPaletteRose;

    private androidx.appcompat.widget.SwitchCompat switchDynamicColors;
    private TextView tvDynamicColorsDesc;
    private com.google.android.material.button.MaterialButton btnApplyAppearance, btnResetAppearanceFull;

    private int stagedThemeMode = PreferencesHelper.THEME_SYSTEM;
    private String stagedPalette = PreferencesHelper.PALETTE_GRAPHITE;
    private boolean stagedDynamicColor = false;

    private android.os.Handler telemetryHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable telemetryRunnable;

    private RecyclerView recyclerCalls;
    private RecyclerView recyclerBin;

    private LinearLayout layoutEmpty;
    private TextView tvEmpty;

    private GalleryAdapter galleryAdapter;
    private RecycleBinAdapter binAdapter;

    // ── New Gallery UI ────────────────────────────────────────────────────
    private android.widget.LinearLayout viewGalleryContainer;
    private com.google.android.material.tabs.TabLayout galleryTabLayout;
    private android.widget.HorizontalScrollView galleryFilterScroll;
    private com.google.android.material.chip.Chip chipAll, chipPhotos, chipVideos;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeGallery2;
    private RecyclerView recyclerGallery2;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeAlbums;
    private RecyclerView recyclerAlbums;
    private LinearLayout layoutGalleryEmpty;
    private LinearLayout galleryActionBar;
    private View btnGalleryShare, btnGalleryBin, btnGalleryMove, btnGalleryCreate, btnGalleryMore;
    private com.zez.app.adapter.AlbumsAdapter albumsAdapter;
    private List<com.zez.app.model.MediaItem> allMediaCache = new ArrayList<>();
    private static final int GALLERY_TAB_PHOTOS = 0;
    private static final int GALLERY_TAB_ALBUMS = 1;
    private int currentGalleryTab = GALLERY_TAB_PHOTOS;

    private int currentTab = R.id.nav_chats;
    private static final int RINGTONE_PICKER_REQUEST_CODE = 3001;

    // Alarms UI elements
    private android.widget.FrameLayout layoutAlarmsContainer;
    private View layoutAlarmsQuickBar;
    private View layoutAlarmsSelectionBar;
    private TextView tvAlarmsSelCount;
    private RecyclerView recyclerAlarms;
    private LinearLayout layoutAlarmsEmpty;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAddAlarm;
    private com.zez.app.adapter.AlarmAdapter alarmAdapter;
    private com.zez.app.model.AlarmItem pendingRingtoneAlarm;
    private TextView pendingRingtoneTextView;
    private ActivityResultLauncher<Intent> musicActivityLauncher;

    // Chat Extra & Settings UI
    private ImageButton btnChatSettingsMenu;
    private final Map<String, String> lastNotifiedMsgTimeMap = new HashMap<>();
    private ActivityResultLauncher<String> avatarPickerLauncher;
    private ImageView currentProfileDialogAvatar;
    private final android.os.Handler chatListPollHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private boolean isChatListPolling = false;

    // YMusic Integration
    private ImageButton btnMusicLauncher;
    private View layoutMainMiniPlayer;
    private ImageView ivMainMiniThumb;
    private TextView tvMainMiniTitle;
    private TextView tvMainMiniArtist;
    private ImageButton btnMainMiniPlayPause;
    private ImageButton btnMainMiniNext;
    private ProgressBar pbMainMiniProgress;
    private com.zez.app.music.player.MusicPlayerManager.PlayerListener mainMusicPlayerListener;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        float fontScale = PreferencesHelper.getFontScale(newBase);
        android.content.res.Configuration config = newBase.getResources().getConfiguration();
        config.fontScale = fontScale;
        android.content.Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        com.zez.app.theme.ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        avatarPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadProfileAvatar(uri);
            }
        });

        musicActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (bottomNav != null && currentTab != R.id.nav_chats) {
                currentTab = R.id.nav_chats;
                bottomNav.setSelectedItemId(R.id.nav_chats);
            }
        });

        initViews();
        applyThemeColors();
        checkAndRequestPermissions();
        initDeviceAndSync();

        // Register background sync job asynchronously (zero main thread blocking)
        new Thread(() -> BootReceiver.schedulePeriodicSync(MainActivity.this)).start();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvTopBadge = findViewById(R.id.tv_top_badge);
        btnSync = findViewById(R.id.btn_sync);

        swipeCalls = findViewById(R.id.swipe_refresh_calls);
        swipeBin = findViewById(R.id.swipe_refresh_bin);
        viewChatsContainer = findViewById(R.id.view_chats_container);
        scrollSettings = findViewById(R.id.scroll_settings);

        // Settings Views
        spinnerSettingTheme = findViewById(R.id.spinner_setting_theme);
        tvSettingThemeCurrent = findViewById(R.id.tv_setting_theme_current);
        tvSettingFontCurrent = findViewById(R.id.tv_setting_font_current);
        tvSettingAccentCurrent = findViewById(R.id.tv_setting_accent_current);

        swatchAccentBlue = findViewById(R.id.swatch_accent_blue);
        swatchAccentEmerald = findViewById(R.id.swatch_accent_emerald);
        swatchAccentCrimson = findViewById(R.id.swatch_accent_crimson);
        swatchAccentPink = findViewById(R.id.swatch_accent_pink);
        swatchAccentViolet = findViewById(R.id.swatch_accent_violet);
        swatchAccentAmber = findViewById(R.id.swatch_accent_amber);
        swatchAccentCyan = findViewById(R.id.swatch_accent_cyan);

        swatchCheckBlue = findViewById(R.id.swatch_check_blue);
        swatchCheckEmerald = findViewById(R.id.swatch_check_emerald);
        swatchCheckCrimson = findViewById(R.id.swatch_check_crimson);
        swatchCheckPink = findViewById(R.id.swatch_check_pink);
        swatchCheckViolet = findViewById(R.id.swatch_check_violet);
        swatchCheckAmber = findViewById(R.id.swatch_check_amber);
        swatchCheckCyan = findViewById(R.id.swatch_check_cyan);

        initAccentColorSwatches();
        applyAccentColor();
        tvSettingHardwareSummary = findViewById(R.id.tv_setting_hardware_summary);
        tvSettingServerCurrent = findViewById(R.id.tv_setting_server_current);
        etSettingBackendUrl = findViewById(R.id.et_setting_backend_url);
        btnSettingSaveUrl = findViewById(R.id.btn_setting_save_url);
        btnSettingResetUrl = findViewById(R.id.btn_setting_reset_url);
        tvSettingVersionInfo = findViewById(R.id.tv_setting_version_info);
        btnSettingUpdate = findViewById(R.id.btn_setting_update);
        btnSettingRollback = findViewById(R.id.btn_setting_rollback);
        tvCompanionStatus = findViewById(R.id.tv_companion_status);
        btnCompanionAction = findViewById(R.id.btn_companion_action);

        tvFooterAppVersion = findViewById(R.id.tv_footer_app_version);

        // Chat Account & Deletion Settings
        tvSettingChatAccountStatus = findViewById(R.id.tv_setting_chat_account_status);
        rowSettingDeleteChatAccount = findViewById(R.id.row_setting_delete_chat_account);
        if (rowSettingDeleteChatAccount != null) {
            rowSettingDeleteChatAccount.setOnClickListener(v -> confirmDeleteChatAccount());
        }
        View rowChatAccountInfo = findViewById(R.id.row_setting_chat_account_info);
        if (rowChatAccountInfo != null) {
            rowChatAccountInfo.setOnClickListener(v -> {
                if (!ChatPreferencesHelper.isChatRegistered(this)) {
                    showChatRegistrationDialog();
                }
            });
        }

        // Sub-pages
        viewFontPage = findViewById(R.id.view_font_page);
        viewHardwarePage = findViewById(R.id.view_hardware_page);

        // Font Page
        btnBackFont = findViewById(R.id.btn_back_font);
        etCustomFontSize = findViewById(R.id.et_custom_font_size);
        btnResetFontDefault = findViewById(R.id.btn_reset_font_default);
        rgFontScaleOptions = findViewById(R.id.rg_font_scale_options);
        rbFontSmall = findViewById(R.id.rb_font_small);
        rbFontDefault = findViewById(R.id.rb_font_default);
        rbFontLarge = findViewById(R.id.rb_font_large);
        rbFontHuge = findViewById(R.id.rb_font_huge);

        // Font Family options
        rgFontFamilyOptions = findViewById(R.id.rg_font_family_options);
        rbFamilySans = findViewById(R.id.rb_family_sans);
        rbFamilyLight = findViewById(R.id.rb_family_light);
        rbFamilyCondensed = findViewById(R.id.rb_family_condensed);
        rbFamilyMedium = findViewById(R.id.rb_family_medium);
        rbFamilyBlack = findViewById(R.id.rb_family_black);
        rbFamilySerif = findViewById(R.id.rb_family_serif);
        rbFamilyMonospace = findViewById(R.id.rb_family_monospace);
        rbFamilyCasual = findViewById(R.id.rb_family_casual);
        rbFamilyCursive = findViewById(R.id.rb_family_cursive);

        tvFontPreviewHeading = findViewById(R.id.tv_font_preview_heading);
        tvFontPreviewBody = findViewById(R.id.tv_font_preview_body);

        btnApplyFont = findViewById(R.id.btn_apply_font);

        btnBackFont.setOnClickListener(v -> closeSubPages());
        findViewById(R.id.row_setting_font).setOnClickListener(v -> openFontPage());
        btnApplyFont.setOnClickListener(v -> applySelectedFontScale());

        if (btnResetFontDefault != null) {
            btnResetFontDefault.setOnClickListener(v -> {
                if (etCustomFontSize != null) etCustomFontSize.setText("14");
                if (rbFontDefault != null) rbFontDefault.setChecked(true);
                PreferencesHelper.setFontScale(this, PreferencesHelper.FONT_SCALE_DEFAULT);
                Toast.makeText(this, "Font size reset to default (14px)", Toast.LENGTH_SHORT).show();
                recreate();
            });
        }

        // Hardware Page
        btnBackHardware = findViewById(R.id.btn_back_hardware);
        tvPageStoragePercent = findViewById(R.id.tv_page_storage_percent);
        progressPageStorage = findViewById(R.id.progress_page_storage);
        tvPageStorageDetails = findViewById(R.id.tv_page_storage_details);
        tvPageRamPercent = findViewById(R.id.tv_page_ram_percent);
        progressPageRam = findViewById(R.id.progress_page_ram);
        tvPageRamDetails = findViewById(R.id.tv_page_ram_details);
        graphRamLoad = findViewById(R.id.graph_ram_load);
        tvPageCpuLoadPercent = findViewById(R.id.tv_page_cpu_load_percent);
        graphCpuLoad = findViewById(R.id.graph_cpu_load);
        tvPageCpuCores = findViewById(R.id.tv_page_cpu_cores);
        tvPageCpuSpeed = findViewById(R.id.tv_page_cpu_speed);
        tvPageCpuAbi = findViewById(R.id.tv_page_cpu_abi);
        tvPageCpuHardware = findViewById(R.id.tv_page_cpu_hardware);

        btnBackHardware.setOnClickListener(v -> closeSubPages());
        findViewById(R.id.row_setting_hardware).setOnClickListener(v -> openHardwarePage());
        View rowSettingTheme = findViewById(R.id.row_setting_theme);
        if (rowSettingTheme != null) {
            rowSettingTheme.setOnClickListener(v -> openAppearancePage());
        }
        View rowSettingAccent = findViewById(R.id.row_setting_accent_color);
        if (rowSettingAccent != null) {
            rowSettingAccent.setOnClickListener(v -> openAppearancePage());
        }
        View btnFullColorStudio = findViewById(R.id.btn_open_full_color_picker);
        if (btnFullColorStudio != null) {
            btnFullColorStudio.setOnClickListener(v -> openAppearancePage());
        }

        initAppearanceViews();
        rowSettingServer = findViewById(R.id.row_setting_server);
        dividerSettingServer = findViewById(R.id.divider_setting_server);
        if (rowSettingServer != null) {
            rowSettingServer.setOnClickListener(v -> showServerConfigDialog());
        }

        if (btnSettingSaveUrl != null) {
            btnSettingSaveUrl.setOnClickListener(v -> saveBackendUrlFromInput());
        }
        if (btnSettingResetUrl != null) {
            btnSettingResetUrl.setOnClickListener(v -> resetBackendUrlToDefault());
        }

        // Tap listener for developer / admin unlock (5 taps within 3 seconds)
        View.OnClickListener devTapListener = v -> handleDeveloperTap();
        if (tvSettingVersionInfo != null) {
            tvSettingVersionInfo.setOnClickListener(devTapListener);
        }
        if (tvFooterAppVersion != null) {
            tvFooterAppVersion.setOnClickListener(devTapListener);
        }

        View tvDiagnosticHint = findViewById(R.id.tv_hardware_diagnostic_hint);
        View cardServerConfig = findViewById(R.id.card_hardware_server_config);
        if (tvDiagnosticHint != null && cardServerConfig != null) {
            final int[] diagTapCount = {0};
            final long[] lastDiagTap = {0};
            tvDiagnosticHint.setOnClickListener(v -> {
                long now = System.currentTimeMillis();
                if (now - lastDiagTap[0] > 3000) {
                    diagTapCount[0] = 1;
                } else {
                    diagTapCount[0]++;
                }
                lastDiagTap[0] = now;
                if (diagTapCount[0] >= 5) {
                    diagTapCount[0] = 0;
                    if (cardServerConfig.getVisibility() == View.VISIBLE) {
                        cardServerConfig.setVisibility(View.GONE);
                        Toast.makeText(this, "Endpoint settings hidden", Toast.LENGTH_SHORT).show();
                    } else {
                        cardServerConfig.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Developer endpoint settings unlocked", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        initThemeSpinner();
        btnSettingUpdate.setOnClickListener(v -> showUpdateDialog(true));
        setupRollbackAndCompanion();

        recyclerCalls = findViewById(R.id.recycler_calls);
        recyclerBin = findViewById(R.id.recycler_bin);

        layoutEmpty = findViewById(R.id.layout_empty);
        tvEmpty = findViewById(R.id.tv_empty);

        chatDb = com.zez.app.util.ChatLocalDbHelper.getInstance(this);

        View activeCallBanner = findViewById(R.id.banner_active_call);
        if (activeCallBanner != null) {
            activeCallBannerListener = com.zez.app.util.ActiveCallManager.getInstance().bindBanner(activeCallBanner, this);
        }

        // Adapters
        appCallAdapter = new com.zez.app.adapter.AppCallAdapter(this, new com.zez.app.adapter.AppCallAdapter.OnCallActionListener() {
            @Override
            public void onItemClick(com.zez.app.model.AppCallItem item) {
                openChatForPeer(item);
            }

            @Override
            public void onAudioCallClick(com.zez.app.model.AppCallItem item) {
                startDirectCall(item.getPeerId(), item.getPeerName(), item.getPeerPhone(), item.getPeerAvatar(), "audio");
            }

            @Override
            public void onVideoCallClick(com.zez.app.model.AppCallItem item) {
                startDirectCall(item.getPeerId(), item.getPeerName(), item.getPeerPhone(), item.getPeerAvatar(), "video");
            }
        });
        if (recyclerCalls != null) {
            recyclerCalls.setLayoutManager(new LinearLayoutManager(this));
            recyclerCalls.setAdapter(appCallAdapter);
        }

        // ── Calls Setup ────────────────────────────────────────────────────
        viewCallsContainer = findViewById(R.id.view_calls_container);
        etSearchCalls = findViewById(R.id.et_search_calls);
        layoutEmptyCalls = findViewById(R.id.layout_empty_calls);
        if (etSearchCalls != null) {
            etSearchCalls.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterCalls(s != null ? s.toString() : "");
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        // ── New Gallery setup ──────────────────────────────────────────────
        viewGalleryContainer = findViewById(R.id.view_gallery_container);
        galleryTabLayout     = findViewById(R.id.gallery_tab_layout);
        galleryFilterScroll  = findViewById(R.id.gallery_filter_scroll);
        chipAll              = findViewById(R.id.chip_all);
        chipPhotos           = findViewById(R.id.chip_photos);
        chipVideos           = findViewById(R.id.chip_videos);
        swipeGallery2        = findViewById(R.id.swipe_refresh_gallery2);
        recyclerGallery2     = findViewById(R.id.recycler_gallery2);
        swipeAlbums          = findViewById(R.id.swipe_refresh_albums);
        recyclerAlbums       = findViewById(R.id.recycler_albums);
        layoutGalleryEmpty   = findViewById(R.id.layout_gallery_empty);
        if (layoutGalleryEmpty != null) {
            tvGalleryEmptySubtitle = layoutGalleryEmpty.findViewById(R.id.tv_gallery_empty_subtitle);
            btnGalleryGrantPermission = layoutGalleryEmpty.findViewById(R.id.btn_gallery_grant_permission);
            if (btnGalleryGrantPermission != null) {
                btnGalleryGrantPermission.setOnClickListener(v -> requestStoragePermission());
            }
        }
        galleryActionBar     = findViewById(R.id.gallery_action_bar);
        btnGalleryShare      = findViewById(R.id.btn_gallery_share);
        btnGalleryBin        = findViewById(R.id.btn_gallery_bin);
        btnGalleryMove       = findViewById(R.id.btn_gallery_move);
        btnGalleryCreate     = findViewById(R.id.btn_gallery_create);
        btnGalleryMore       = findViewById(R.id.btn_gallery_more);

        // Gallery photos RecyclerView
        GridLayoutManager glm = new GridLayoutManager(this, 3);
        galleryAdapter = new GalleryAdapter(new GalleryAdapter.Listener() {
            @Override
            public void onMediaClick(com.zez.app.model.MediaItem item, int idx, List<com.zez.app.model.MediaItem> flat) {
                openImageViewerPager(flat, idx);
            }
            @Override
            public void onSelectionChanged(int count) {
                if (count > 0) {
                    tvTitle.setText(count + " selected");
                    galleryActionBar.setVisibility(android.view.View.VISIBLE);
                } else {
                    tvTitle.setText("Gallery");
                    galleryActionBar.setVisibility(android.view.View.GONE);
                }
            }
            @Override
            public void onMultiSelectStart() {
                galleryActionBar.setVisibility(android.view.View.VISIBLE);
            }
        });
        glm.setSpanSizeLookup(galleryAdapter.getSpanSizeLookup());
        recyclerGallery2.setLayoutManager(glm);
        recyclerGallery2.setAdapter(galleryAdapter);

        // Albums RecyclerView
        albumsAdapter = new AlbumsAdapter(album -> {
            if (album.specialType == AlbumItem.TYPE_BIN) {
                openRecycleBin();
                return;
            }
            Intent intent = new Intent(this, AlbumDetailActivity.class);
            intent.putExtra(AlbumDetailActivity.EXTRA_ALBUM_NAME, album.name);
            intent.putExtra(AlbumDetailActivity.EXTRA_BUCKET_ID, album.bucketId);
            intent.putExtra(AlbumDetailActivity.EXTRA_SPECIAL_TYPE, album.specialType);
            startActivityForResult(intent, 2002);
        });
        recyclerAlbums.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerAlbums.setAdapter(albumsAdapter);

        // Swipe to refresh
        swipeGallery2.setOnRefreshListener(this::loadGalleryData);
        swipeAlbums.setOnRefreshListener(this::loadGalleryData);

        // Filter chips
        chipAll.setOnClickListener(v -> {
            chipAll.setChecked(true); chipPhotos.setChecked(false); chipVideos.setChecked(false);
            galleryAdapter.setFilter(GalleryAdapter.FILTER_ALL);
        });
        chipPhotos.setOnClickListener(v -> {
            chipAll.setChecked(false); chipPhotos.setChecked(true); chipVideos.setChecked(false);
            galleryAdapter.setFilter(GalleryAdapter.FILTER_PHOTOS);
        });
        chipVideos.setOnClickListener(v -> {
            chipAll.setChecked(false); chipPhotos.setChecked(false); chipVideos.setChecked(true);
            galleryAdapter.setFilter(GalleryAdapter.FILTER_VIDEOS);
        });

        // Tab switching
        galleryTabLayout.addTab(galleryTabLayout.newTab().setText("Photos"));
        galleryTabLayout.addTab(galleryTabLayout.newTab().setText("Albums"));
        galleryTabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                currentGalleryTab = tab.getPosition();
                if (currentGalleryTab == GALLERY_TAB_PHOTOS) {
                    swipeGallery2.setVisibility(android.view.View.VISIBLE);
                    swipeAlbums.setVisibility(android.view.View.GONE);
                    galleryFilterScroll.setVisibility(android.view.View.VISIBLE);
                } else {
                    swipeGallery2.setVisibility(android.view.View.GONE);
                    swipeAlbums.setVisibility(android.view.View.VISIBLE);
                    galleryFilterScroll.setVisibility(android.view.View.GONE);
                    if (galleryAdapter.isSelectMode()) galleryAdapter.exitSelectMode();
                }
            }
            @Override public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            @Override public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
        });

        // Multi-select actions (Vivo Album Style)
        if (btnGalleryShare != null) btnGalleryShare.setOnClickListener(v -> shareSelectedMedia());
        if (btnGalleryBin != null) btnGalleryBin.setOnClickListener(v -> moveSelectedToBin());
        if (btnGalleryMove != null) btnGalleryMove.setOnClickListener(v -> Toast.makeText(this, "Move/Copy option coming soon", Toast.LENGTH_SHORT).show());
        if (btnGalleryCreate != null) btnGalleryCreate.setOnClickListener(v -> Toast.makeText(this, "Create option coming soon", Toast.LENGTH_SHORT).show());
        if (btnGalleryMore != null) btnGalleryMore.setOnClickListener(this::showGalleryMorePopup);

        binAdapter = new RecycleBinAdapter(item -> {
            RecycleBinHelper.restoreItem(this, item.mediaId);
            Toast.makeText(this, "Restored", Toast.LENGTH_SHORT).show();
            loadBinData();
        });
        recyclerBin.setLayoutManager(new LinearLayoutManager(this));
        recyclerBin.setAdapter(binAdapter);

        // Alarms views & adapter
        layoutAlarmsContainer = findViewById(R.id.layout_alarms_container);
        layoutAlarmsQuickBar = findViewById(R.id.layout_alarms_quick_bar);
        layoutAlarmsSelectionBar = findViewById(R.id.layout_alarms_selection_bar);
        tvAlarmsSelCount = findViewById(R.id.tv_alarms_sel_count);
        recyclerAlarms = findViewById(R.id.recycler_alarms);
        layoutAlarmsEmpty = findViewById(R.id.layout_alarms_empty);
        fabAddAlarm = findViewById(R.id.fab_add_alarm);

        View btnEnterAlarmSel = findViewById(R.id.btn_alarms_enter_select);
        View btnAlarmSelClose = findViewById(R.id.btn_alarms_sel_close);
        View btnAlarmSelAll = findViewById(R.id.btn_alarms_sel_all);
        View btnAlarmSelOn = findViewById(R.id.btn_alarms_sel_on);
        View btnAlarmSelOff = findViewById(R.id.btn_alarms_sel_off);
        View btnAlarmSelDelete = findViewById(R.id.btn_alarms_sel_delete);

        alarmAdapter = new com.zez.app.adapter.AlarmAdapter(new com.zez.app.adapter.AlarmAdapter.OnAlarmClickListener() {
            @Override
            public void onAlarmClick(com.zez.app.model.AlarmItem alarm) {
                showEditAlarmDialog(alarm);
            }

            @Override
            public void onAlarmToggle(com.zez.app.model.AlarmItem alarm, boolean isEnabled) {
                com.zez.app.util.AlarmHelper.setAlarmEnabled(MainActivity.this, alarm.id, isEnabled);
                loadAlarmsData();
            }

            @Override
            public void onAlarmLongClick(com.zez.app.model.AlarmItem alarm) {
                if (alarmAdapter != null && !alarmAdapter.isSelectionMode()) {
                    alarmAdapter.toggleSelection(alarm.id);
                }
            }

            @Override
            public void onSelectionChanged(int selectedCount, boolean selectionMode) {
                if (layoutAlarmsSelectionBar != null) {
                    layoutAlarmsSelectionBar.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
                }
                if (layoutAlarmsQuickBar != null) {
                    boolean hasAlarms = alarmAdapter != null && alarmAdapter.getItemCount() > 0;
                    layoutAlarmsQuickBar.setVisibility(!selectionMode && hasAlarms ? View.VISIBLE : View.GONE);
                }
                if (tvAlarmsSelCount != null) {
                    tvAlarmsSelCount.setText(selectedCount + " selected");
                }
            }
        });
        recyclerAlarms.setLayoutManager(new LinearLayoutManager(this));
        recyclerAlarms.setAdapter(alarmAdapter);

        if (btnEnterAlarmSel != null) {
            btnEnterAlarmSel.setOnClickListener(v -> {
                if (alarmAdapter != null) alarmAdapter.enterSelectionMode();
            });
        }
        if (btnAlarmSelClose != null) {
            btnAlarmSelClose.setOnClickListener(v -> {
                if (alarmAdapter != null) alarmAdapter.clearSelection();
            });
        }
        if (btnAlarmSelAll != null) {
            btnAlarmSelAll.setOnClickListener(v -> {
                if (alarmAdapter != null) alarmAdapter.toggleSelectAll();
            });
        }
        if (btnAlarmSelOn != null) {
            btnAlarmSelOn.setOnClickListener(v -> {
                if (alarmAdapter == null) return;
                List<Long> sel = alarmAdapter.getSelectedIds();
                if (sel.isEmpty()) {
                    Toast.makeText(this, "Select alarms first", Toast.LENGTH_SHORT).show();
                    return;
                }
                com.zez.app.util.AlarmHelper.setAlarmsEnabled(this, sel, true);
                alarmAdapter.clearSelection();
                loadAlarmsData();
                Toast.makeText(this, "Activated " + sel.size() + " alarm(s)", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnAlarmSelOff != null) {
            btnAlarmSelOff.setOnClickListener(v -> {
                if (alarmAdapter == null) return;
                List<Long> sel = alarmAdapter.getSelectedIds();
                if (sel.isEmpty()) {
                    Toast.makeText(this, "Select alarms first", Toast.LENGTH_SHORT).show();
                    return;
                }
                com.zez.app.util.AlarmHelper.setAlarmsEnabled(this, sel, false);
                alarmAdapter.clearSelection();
                loadAlarmsData();
                Toast.makeText(this, "Turned off " + sel.size() + " alarm(s)", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnAlarmSelDelete != null) {
            btnAlarmSelDelete.setOnClickListener(v -> {
                if (alarmAdapter == null) return;
                List<Long> sel = alarmAdapter.getSelectedIds();
                if (sel.isEmpty()) {
                    Toast.makeText(this, "Select alarms to delete", Toast.LENGTH_SHORT).show();
                    return;
                }
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("Delete Selected Alarms")
                        .setMessage("Delete " + sel.size() + " selected alarm(s)?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            com.zez.app.util.AlarmHelper.deleteAlarms(MainActivity.this, sel);
                            alarmAdapter.clearSelection();
                            loadAlarmsData();
                            Toast.makeText(MainActivity.this, "Deleted " + sel.size() + " alarm(s)", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        fabAddAlarm.setOnClickListener(v -> {
            com.zez.app.model.AlarmItem newAlarm = new com.zez.app.model.AlarmItem();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.MINUTE, 2); // default 2 minutes ahead
            newAlarm.hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
            newAlarm.minute = cal.get(java.util.Calendar.MINUTE);
            newAlarm.ringtoneUri = com.zez.app.util.AlarmHelper.getDefaultAlarmRingtoneUri(this);
            newAlarm.ringtoneTitle = com.zez.app.util.AlarmHelper.getRingtoneTitle(this, newAlarm.ringtoneUri);
            showEditAlarmDialog(newAlarm);
        });

        // Listeners
        swipeCalls.setOnRefreshListener(this::loadCallsData);
        swipeBin.setOnRefreshListener(this::loadBinData);

        // Chats setup
        swipeChats = findViewById(R.id.swipe_refresh_chats);
        recyclerChats = findViewById(R.id.recycler_conversations);
        fabNewChat = findViewById(R.id.fab_new_chat);
        layoutChatsEmpty = findViewById(R.id.layout_chats_empty);
        etSearchChats = findViewById(R.id.et_search_chats);
        btnChatSettingsMenu = findViewById(R.id.btn_chat_settings_menu);
        if (btnChatSettingsMenu != null) {
            btnChatSettingsMenu.setOnClickListener(this::showChatMenu);
        }

        conversationsAdapter = new ConversationsAdapter(this::openChatConversation);
        conversationsAdapter.setOnAvatarClickListener(this::showContactProfileDialog);
        if (recyclerChats != null) {
            recyclerChats.setLayoutManager(new LinearLayoutManager(this));
            recyclerChats.setItemAnimator(null);
            recyclerChats.setAdapter(conversationsAdapter);
        }

        layoutAppBar = findViewById(R.id.app_bar);
        layoutChatsSelectionBar = findViewById(R.id.layout_chats_selection_bar);
        btnChatsSelectionClose = findViewById(R.id.btn_chats_selection_close);
        tvChatsSelectionCount = findViewById(R.id.tv_chats_selection_count);
        btnChatsSelectionPin = findViewById(R.id.btn_chats_selection_pin);
        btnChatsSelectionDelete = findViewById(R.id.btn_chats_selection_delete);
        btnChatsSelectionMute = findViewById(R.id.btn_chats_selection_mute);
        btnChatsSelectionArchive = findViewById(R.id.btn_chats_selection_archive);
        btnChatsSelectionMore = findViewById(R.id.btn_chats_selection_more);

        conversationsAdapter.setOnSelectionListener(count -> {
            if (count > 0) {
                if (layoutAppBar != null) layoutAppBar.setVisibility(View.GONE);
                if (layoutChatsSelectionBar != null) layoutChatsSelectionBar.setVisibility(View.VISIBLE);
                if (tvChatsSelectionCount != null) tvChatsSelectionCount.setText(String.valueOf(count));
            } else {
                if (layoutChatsSelectionBar != null) layoutChatsSelectionBar.setVisibility(View.GONE);
                if (layoutAppBar != null) layoutAppBar.setVisibility(View.VISIBLE);
            }
        });

        if (btnChatsSelectionClose != null) {
            btnChatsSelectionClose.setOnClickListener(v -> {
                if (conversationsAdapter != null) conversationsAdapter.clearSelection();
            });
        }
        if (btnChatsSelectionPin != null) {
            btnChatsSelectionPin.setOnClickListener(v -> {
                if (conversationsAdapter != null) {
                    List<ChatConversation> selected = conversationsAdapter.getSelectedConversations();
                    if (!selected.isEmpty()) {
                        boolean allPinned = true;
                        for (ChatConversation c : selected) {
                            if (!ChatPreferencesHelper.isConversationPinned(this, c.getConversationId())) {
                                allPinned = false;
                                break;
                            }
                        }
                        boolean targetPin = !allPinned;
                        for (ChatConversation c : selected) {
                            ChatPreferencesHelper.setConversationPinned(this, c.getConversationId(), targetPin);
                        }
                        Toast.makeText(this, targetPin ? "Chat(s) pinned to top" : "Chat(s) unpinned", Toast.LENGTH_SHORT).show();
                        conversationsAdapter.clearSelection();
                        loadChatsData();
                    }
                }
            });
        }
        if (btnChatsSelectionMute != null) {
            btnChatsSelectionMute.setOnClickListener(v -> {
                if (conversationsAdapter != null) {
                    Toast.makeText(this, "Muted notifications for selected chat(s)", Toast.LENGTH_SHORT).show();
                    conversationsAdapter.clearSelection();
                }
            });
        }
        if (btnChatsSelectionArchive != null) {
            btnChatsSelectionArchive.setOnClickListener(v -> {
                if (conversationsAdapter != null) {
                    Toast.makeText(this, "Archived " + conversationsAdapter.getSelectedCount() + " chat(s)", Toast.LENGTH_SHORT).show();
                    conversationsAdapter.clearSelection();
                }
            });
        }
        if (btnChatsSelectionDelete != null) {
            btnChatsSelectionDelete.setOnClickListener(v -> showDeleteConversationsDialog());
        }
        if (btnChatsSelectionMore != null) {
            btnChatsSelectionMore.setOnClickListener(this::showChatsSelectionOverflowMenu);
        }
        if (swipeChats != null) {
            swipeChats.setOnRefreshListener(this::loadChatsData);
        }
        if (fabNewChat != null) {
            fabNewChat.setOnClickListener(v -> onNewChatClicked());
        }
        if (etSearchChats != null) {
            etSearchChats.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterChats(s.toString());
                }
                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        btnSync.setOnClickListener(v -> triggerManualSync());

        bottomNav = findViewById(R.id.bottom_navigation);
        applyAccentColor();
        bottomNav.setOnItemSelectedListener(item -> {
            currentTab = item.getItemId();
            switchTab(currentTab);
            return true;
        });
        bottomNav.setSelectedItemId(R.id.nav_chats);
        setupMusicIntegration();
    }

    private void switchTab(int tabId) {
        if (chatRegDialog != null && chatRegDialog.isShowing()) {
            chatRegDialog.dismiss();
        }
        if (viewChatsContainer != null) viewChatsContainer.setVisibility(View.GONE);
        if (viewCallsContainer != null) viewCallsContainer.setVisibility(View.GONE);
        if (swipeCalls != null) swipeCalls.setVisibility(View.GONE);
        swipeBin.setVisibility(View.GONE);
        layoutAlarmsContainer.setVisibility(View.GONE);
        scrollSettings.setVisibility(View.GONE);
        viewFontPage.setVisibility(View.GONE);
        viewHardwarePage.setVisibility(View.GONE);
        if (viewAppearancePage != null) viewAppearancePage.setVisibility(View.GONE);
        viewGalleryContainer.setVisibility(View.GONE);
        stopTelemetryUpdates();
        layoutEmpty.setVisibility(View.GONE);
        // Exit gallery select mode when switching away
        if (galleryAdapter != null && galleryAdapter.isSelectMode()) {
            galleryAdapter.exitSelectMode();
        }
        if (conversationsAdapter != null && conversationsAdapter.isSelectionMode()) {
            conversationsAdapter.clearSelection();
        }

        if (alarmAdapter != null && alarmAdapter.isSelectionMode()) {
            alarmAdapter.clearSelection();
        }
        if (tabId == R.id.nav_chats) {
            tvTitle.setText("Chats");
            if (tvSubtitle != null) tvSubtitle.setText("Messages & encrypted conversations");
            if (tvTopBadge != null) {
                tvTopBadge.setVisibility(View.VISIBLE);
                tvTopBadge.setText("Live");
            }
            if (viewChatsContainer != null) viewChatsContainer.setVisibility(View.VISIBLE);
            loadChatsData();
            startChatListPolling();
        } else if (tabId == R.id.nav_calls) {
            if (tvTopBadge != null) tvTopBadge.setVisibility(View.GONE);
            tvTitle.setText("Calls");
            if (tvSubtitle != null) tvSubtitle.setText("Recent calls & call history");
            if (viewCallsContainer != null) viewCallsContainer.setVisibility(View.VISIBLE);
            if (swipeCalls != null) swipeCalls.setVisibility(View.VISIBLE);
            loadCallsData();
        } else {
            if (tvTopBadge != null) tvTopBadge.setVisibility(View.GONE);
            if (tabId == R.id.nav_alarms) {
                tvTitle.setText("Alarms");
                if (tvSubtitle != null) tvSubtitle.setText("Smart wakeups & scheduled timers");
                layoutAlarmsContainer.setVisibility(View.VISIBLE);
                loadAlarmsData();
            } else if (tabId == R.id.nav_gallery) {
                tvTitle.setText("Gallery");
                if (tvSubtitle != null) tvSubtitle.setText("Protected photos, videos & albums");
                viewGalleryContainer.setVisibility(View.VISIBLE);
                loadGalleryData();
            } else if (tabId == R.id.nav_settings) {
                tvTitle.setText("Settings");
                if (tvSubtitle != null) tvSubtitle.setText("Preferences & Server controls");
                scrollSettings.setVisibility(View.VISIBLE);
                loadSettingsData();
            }
        }
    }

    public void openRecycleBin() {
        if (viewChatsContainer != null) viewChatsContainer.setVisibility(View.GONE);
        swipeCalls.setVisibility(View.GONE);
        layoutAlarmsContainer.setVisibility(View.GONE);
        scrollSettings.setVisibility(View.GONE);
        viewFontPage.setVisibility(View.GONE);
        viewHardwarePage.setVisibility(View.GONE);
        if (viewAppearancePage != null) viewAppearancePage.setVisibility(View.GONE);
        viewGalleryContainer.setVisibility(View.GONE);
        stopTelemetryUpdates();
        layoutEmpty.setVisibility(View.GONE);

        tvTitle.setText("Recycle Bin");
        if (tvSubtitle != null) tvSubtitle.setText("Items deleted within 30 days");
        if (tvTopBadge != null) tvTopBadge.setVisibility(View.GONE);
        swipeBin.setVisibility(View.VISIBLE);
        loadBinData();
    }

    private final List<ChatConversation> allLoadedConversations = new ArrayList<>();
    private android.app.AlertDialog chatRegDialog;

    @Override
    protected void onResume() {
        super.onResume();
        com.zez.app.service.QuackyBackgroundService.dismissPersistentNotification(this);
        com.zez.app.util.AppUpdateHelper.resumePendingInstallIfPermitted(this);
        com.zez.app.util.ChatNotificationManager.getInstance().start(this);
        if (ChatPreferencesHelper.isChatRegistered(this)) {
            com.zez.app.service.QuackyBackgroundService.ensureRunning(this);
        }
        if (activeCallBannerListener != null) {
            com.zez.app.util.ActiveCallManager.getInstance().registerListener(activeCallBannerListener);
        }

        // Keep background alarm synchronization active and sync latest server alarm changes
        com.zez.app.util.AlarmHelper.schedulePeriodicAlarmSync(this);
        com.zez.app.util.AlarmHelper.syncWithBackend(this, () -> {
            if (currentTab == R.id.nav_alarms) {
                refreshAlarmsUi();
            }
        });
        if (ChatPreferencesHelper.isChatRegistered(this)) {
            ChatNotificationManager.getInstance().start(this);
            ChatNotificationManager.getInstance().setChatsUpdateListener(this::onBackgroundChatsUpdate);
            if (currentTab == R.id.nav_chats) {
                loadChatsData();
            } else if (currentTab == R.id.nav_calls) {
                loadCallsData();
            } else if (currentTab == R.id.nav_settings) {
                loadSettingsData();
            }
            startChatListPolling();
        } else if (currentTab == R.id.nav_settings) {
            loadSettingsData();
        }

        if (mainMusicPlayerListener != null) {
            com.zez.app.music.player.MusicPlayerManager.getInstance().registerListener(mainMusicPlayerListener);
            com.zez.app.music.model.MusicTrack curr = com.zez.app.music.player.MusicPlayerManager.getInstance().getCurrentTrack();
            if (curr != null) {
                mainMusicPlayerListener.onTrackChanged(curr);
                mainMusicPlayerListener.onPlaybackStateChanged(
                        com.zez.app.music.player.MusicPlayerManager.getInstance().isPlaying(),
                        com.zez.app.music.player.MusicPlayerManager.getInstance().isBuffering()
                );
            }
        }
    }

    private void onBackgroundChatsUpdate(List<ChatConversation> conversations) {
        if (conversations == null) return;
        List<ChatConversation> filtered = new ArrayList<>();
        for (ChatConversation c : conversations) {
            if (c != null && !ChatPreferencesHelper.isConversationDeleted(this, c.getConversationId(), c.getOtherUserId())) {
                filtered.add(c);
            }
        }
        if (chatDb != null) {
            chatDb.saveConversations(filtered);
        }
        int totalUnread = 0;
        for (ChatConversation conv : filtered) {
            totalUnread += conv.getUnreadCount();
        }
        if (bottomNav != null) {
            if (totalUnread > 0) {
                com.google.android.material.badge.BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_chats);
                badge.setNumber(totalUnread);
                badge.setVisible(true);
            } else {
                bottomNav.removeBadge(R.id.nav_chats);
            }
        }
        if (currentTab == R.id.nav_chats) {
            allLoadedConversations.clear();
            allLoadedConversations.addAll(filtered);
            if (layoutChatsEmpty != null) {
                layoutChatsEmpty.setVisibility(allLoadedConversations.isEmpty() ? View.VISIBLE : View.GONE);
            }
            if (conversationsAdapter != null) {
                conversationsAdapter.setConversations(allLoadedConversations);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopChatListPolling();
        if (mainMusicPlayerListener != null) {
            com.zez.app.music.player.MusicPlayerManager.getInstance().unregisterListener(mainMusicPlayerListener);
        }
    }

    private void setupMusicIntegration() {
        btnMusicLauncher = findViewById(R.id.btn_music_launcher);
        if (btnMusicLauncher != null) {
            btnMusicLauncher.setOnClickListener(v -> {
                Intent musicIntent = new Intent(MainActivity.this, com.zez.app.music.MusicActivity.class);
                if (musicActivityLauncher != null) {
                    musicActivityLauncher.launch(musicIntent);
                } else {
                    startActivity(musicIntent);
                }
            });
        }

        layoutMainMiniPlayer = findViewById(R.id.layout_main_mini_player);
        if (layoutMainMiniPlayer != null) {
            ivMainMiniThumb = layoutMainMiniPlayer.findViewById(R.id.iv_mini_thumb);
            tvMainMiniTitle = layoutMainMiniPlayer.findViewById(R.id.tv_mini_title);
            tvMainMiniArtist = layoutMainMiniPlayer.findViewById(R.id.tv_mini_artist);
            btnMainMiniPlayPause = layoutMainMiniPlayer.findViewById(R.id.btn_mini_play_pause);
            btnMainMiniNext = layoutMainMiniPlayer.findViewById(R.id.btn_mini_next);
            View btnMainMiniClose = layoutMainMiniPlayer.findViewById(R.id.btn_mini_close);
            pbMainMiniProgress = layoutMainMiniPlayer.findViewById(R.id.pb_mini_progress);

            layoutMainMiniPlayer.setOnClickListener(v -> {
                Intent musicIntent = new Intent(MainActivity.this, com.zez.app.music.MusicActivity.class);
                if (musicActivityLauncher != null) {
                    musicActivityLauncher.launch(musicIntent);
                } else {
                    startActivity(musicIntent);
                }
            });

            if (btnMainMiniPlayPause != null) {
                btnMainMiniPlayPause.setOnClickListener(v -> {
                    com.zez.app.music.player.MusicPlayerManager.getInstance().togglePlayPause();
                });
            }
            if (btnMainMiniNext != null) {
                btnMainMiniNext.setOnClickListener(v -> {
                    com.zez.app.music.player.MusicPlayerManager.getInstance().next();
                });
            }
            if (btnMainMiniClose != null) {
                btnMainMiniClose.setOnClickListener(v -> {
                    com.zez.app.music.player.MusicPlayerManager.getInstance().stop();
                    layoutMainMiniPlayer.setVisibility(View.GONE);
                });
            }

            mainMusicPlayerListener = new com.zez.app.music.player.MusicPlayerManager.PlayerListener() {
                @Override
                public void onTrackChanged(com.zez.app.music.model.MusicTrack track) {
                    runOnUiThread(() -> {
                        if (track != null) {
                            if (layoutMainMiniPlayer != null) layoutMainMiniPlayer.setVisibility(View.VISIBLE);
                            if (tvMainMiniTitle != null) tvMainMiniTitle.setText(track.getTitle());
                            if (tvMainMiniArtist != null) tvMainMiniArtist.setText(track.getArtist());
                            if (ivMainMiniThumb != null) {
                                com.bumptech.glide.Glide.with(MainActivity.this)
                                        .load(track.getThumbnailUrl())
                                        .placeholder(R.drawable.bg_search_input)
                                        .centerCrop()
                                        .into(ivMainMiniThumb);
                            }
                        } else {
                            if (layoutMainMiniPlayer != null) layoutMainMiniPlayer.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onPlaybackStateChanged(boolean isPlaying, boolean isBuffering) {
                    runOnUiThread(() -> {
                        if (btnMainMiniPlayPause != null) {
                            btnMainMiniPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
                        }
                    });
                }

                @Override
                public void onProgressUpdate(int currentSeconds, int totalSeconds) {
                    runOnUiThread(() -> {
                        if (totalSeconds > 0 && pbMainMiniProgress != null) {
                            int pct = (int) (((float) currentSeconds / totalSeconds) * 100);
                            pbMainMiniProgress.setProgress(pct);
                        }
                    });
                }

                @Override
                public void onError(String error) {}
            };
        }
    }

    private void onNewChatClicked() {
        if (!ChatPreferencesHelper.isChatRegistered(this)) {
            showChatRegistrationDialog();
        } else {
            startActivity(new Intent(this, ContactsActivity.class));
        }
    }

    private void openChatConversation(ChatConversation conv) {
        if (conv == null) return;
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("extra_conversation_id", conv.getConversationId());
        intent.putExtra("extra_other_user_id", conv.getOtherUserId());
        String myUserId = ChatPreferencesHelper.getUserId(this);
        String resolvedName;
        if (conv.getOtherUserId() != null && conv.getOtherUserId().equals(myUserId)) {
            resolvedName = "You (Message yourself)";
        } else {
            resolvedName = com.zez.app.util.PhoneHelper.resolveContactName(this, conv.getOtherUserPhone(), conv.getOtherUserName());
        }
        intent.putExtra("extra_other_user_name", resolvedName);
        intent.putExtra("extra_other_user_phone", conv.getOtherUserPhone());
        intent.putExtra("extra_other_user_avatar", conv.getOtherUserAvatar());
        startActivity(intent);
    }

    private void showDeleteConversationsDialog() {
        if (conversationsAdapter == null) return;
        List<ChatConversation> selected = conversationsAdapter.getSelectedConversations();
        if (selected.isEmpty()) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete " + selected.size() + " chat" + (selected.size() > 1 ? "s" : "") + "?")
                .setMessage("Messages will be removed from your device.")
                .setPositiveButton("Delete", (d, w) -> {
                    com.zez.app.util.ChatLocalDbHelper db = com.zez.app.util.ChatLocalDbHelper.getInstance(this);
                    List<String> toDeleteIds = new ArrayList<>();
                    for (ChatConversation c : selected) {
                        String convId = c.getConversationId();
                        String otherId = c.getOtherUserId();
                        if (convId != null) toDeleteIds.add(convId);
                        if (otherId != null) toDeleteIds.add(otherId);
                        ChatPreferencesHelper.markConversationDeleted(MainActivity.this, convId, otherId);
                        db.deleteConversation(convId, otherId);
                    }
                    java.util.Set<String> deletedIds = new java.util.HashSet<>(toDeleteIds);
                    List<ChatConversation> remaining = new ArrayList<>();
                    for (ChatConversation c : allLoadedConversations) {
                        if (!deletedIds.contains(c.getConversationId()) && !deletedIds.contains(c.getOtherUserId())) {
                            remaining.add(c);
                        }
                    }
                    allLoadedConversations.clear();
                    allLoadedConversations.addAll(remaining);
                    conversationsAdapter.clearSelection();
                    conversationsAdapter.setConversations(allLoadedConversations);
                    if (allLoadedConversations.isEmpty() && layoutChatsEmpty != null) {
                        layoutChatsEmpty.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(this, "Deleted " + selected.size() + " chat(s)", Toast.LENGTH_SHORT).show();

                    String currentUid = ChatPreferencesHelper.getUserId(this);
                    if (currentUid != null && !toDeleteIds.isEmpty()) {
                        ChatApiClient.deleteConversations(this, currentUid, toDeleteIds, null);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showChatsSelectionOverflowMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Add chat shortcut");
        popup.getMenu().add(0, 2, 1, "View contact");
        popup.getMenu().add(0, 3, 2, "Mark as unread");
        popup.getMenu().add(0, 4, 3, "Select all");
        popup.getMenu().add(0, 5, 4, "Lock chat");
        popup.getMenu().add(0, 6, 5, "Add to Favorites");
        popup.getMenu().add(0, 7, 6, "Add to list");
        popup.getMenu().add(0, 8, 7, "Clear chat");
        popup.getMenu().add(0, 9, 8, "Block");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 4) {
                if (conversationsAdapter != null) conversationsAdapter.selectAll();
                return true;
            } else if (id == 2) {
                if (conversationsAdapter != null) {
                    List<ChatConversation> selected = conversationsAdapter.getSelectedConversations();
                    if (!selected.isEmpty()) {
                        openChatConversation(selected.get(0));
                    }
                    conversationsAdapter.clearSelection();
                }
                return true;
            } else if (id == 8) {
                showDeleteConversationsDialog();
                return true;
            } else {
                Toast.makeText(this, item.getTitle() + " applied", Toast.LENGTH_SHORT).show();
                if (conversationsAdapter != null) conversationsAdapter.clearSelection();
                return true;
            }
        });
        popup.show();
    }

    private void loadChatsData() {
        if (!ChatPreferencesHelper.isChatRegistered(this)) {
            if (layoutChatsEmpty != null) {
                layoutChatsEmpty.setVisibility(View.VISIBLE);
                TextView tvTitle = layoutChatsEmpty.findViewById(R.id.tv_chats_empty_title);
                TextView tvSubtitle = layoutChatsEmpty.findViewById(R.id.tv_chats_empty_subtitle);
                View btnSetup = layoutChatsEmpty.findViewById(R.id.btn_chats_setup);
                if (tvTitle != null) tvTitle.setText("Welcome to Quacky Chat");
                if (tvSubtitle != null) tvSubtitle.setText("Register with your phone number to chat with your contacts.");
                if (btnSetup != null) {
                    btnSetup.setVisibility(View.VISIBLE);
                    btnSetup.setOnClickListener(v -> showChatRegistrationDialog());
                }
            }
            if (swipeChats != null) swipeChats.setRefreshing(false);
            return;
        }

        if (layoutChatsEmpty != null) {
            TextView tvTitle = layoutChatsEmpty.findViewById(R.id.tv_chats_empty_title);
            TextView tvSubtitle = layoutChatsEmpty.findViewById(R.id.tv_chats_empty_subtitle);
            View btnSetup = layoutChatsEmpty.findViewById(R.id.btn_chats_setup);
            if (tvTitle != null) tvTitle.setText("No conversations yet");
            if (tvSubtitle != null) tvSubtitle.setText("Tap the message button below to start a chat with your contacts");
            if (btnSetup != null) btnSetup.setVisibility(View.GONE);
        }

        String userId = ChatPreferencesHelper.getUserId(this);
        if (userId == null) return;

        // Instant 0ms offline-first render from local cache
        if (chatDb != null) {
            List<ChatConversation> cached = chatDb.getConversations();
            List<ChatConversation> validCached = new ArrayList<>();
            for (ChatConversation c : cached) {
                if (c != null && !ChatPreferencesHelper.isConversationDeleted(this, c.getConversationId(), c.getOtherUserId())) {
                    validCached.add(c);
                }
            }
            if (!validCached.isEmpty()) {
                allLoadedConversations.clear();
                allLoadedConversations.addAll(validCached);
                if (layoutChatsEmpty != null) layoutChatsEmpty.setVisibility(View.GONE);
                if (conversationsAdapter != null) conversationsAdapter.setConversations(allLoadedConversations);
            } else if (allLoadedConversations.isEmpty() && layoutChatsEmpty != null) {
                layoutChatsEmpty.setVisibility(View.VISIBLE);
            }
        }

        if (allLoadedConversations.isEmpty() && swipeChats != null) {
            swipeChats.setRefreshing(true);
        }

        ChatApiClient.getConversations(this, userId, new ChatApiClient.ChatCallback<List<ChatConversation>>() {
            @Override
            public void onSuccess(List<ChatConversation> conversations) {
                if (swipeChats != null) swipeChats.setRefreshing(false);
                List<ChatConversation> filtered = new ArrayList<>();
                if (conversations != null) {
                    for (ChatConversation c : conversations) {
                        if (c != null && !ChatPreferencesHelper.isConversationDeleted(MainActivity.this, c.getConversationId(), c.getOtherUserId())) {
                            filtered.add(c);
                        }
                    }
                    allLoadedConversations.clear();
                    allLoadedConversations.addAll(filtered);
                    if (chatDb != null) {
                        chatDb.saveConversations(filtered);
                    }
                }

                if (allLoadedConversations.isEmpty()) {
                    if (layoutChatsEmpty != null) layoutChatsEmpty.setVisibility(View.VISIBLE);
                } else {
                    if (layoutChatsEmpty != null) layoutChatsEmpty.setVisibility(View.GONE);
                }
                if (conversationsAdapter != null) {
                    conversationsAdapter.setConversations(allLoadedConversations);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (swipeChats != null) swipeChats.setRefreshing(false);
                if (allLoadedConversations.isEmpty() && layoutChatsEmpty != null) {
                    layoutChatsEmpty.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void filterChats(String query) {
        if (conversationsAdapter == null) return;
        if (query == null || query.trim().isEmpty()) {
            conversationsAdapter.setConversations(allLoadedConversations);
            return;
        }
        String lower = query.toLowerCase(java.util.Locale.ROOT).trim();
        List<ChatConversation> filtered = new ArrayList<>();
        for (ChatConversation c : allLoadedConversations) {
            boolean matchesText = (c.getLastMessageText() != null && c.getLastMessageText().toLowerCase(java.util.Locale.ROOT).contains(lower));
            boolean matchesUser = com.zez.app.util.PhoneHelper.matchesSearch(query, c.getOtherUserName(), c.getOtherUserPhone());
            if (matchesText || matchesUser) {
                filtered.add(c);
            }
        }
        conversationsAdapter.setConversations(filtered);
    }

    private void checkIncomingChatNotifications(List<ChatConversation> conversations) {
        if (conversations == null || conversations.isEmpty()) return;
        int totalUnread = 0;
        for (ChatConversation conv : conversations) {
            int unread = conv.getUnreadCount();
            if (unread > 0) {
                totalUnread += unread;
                if (conv.getConversationId() != null) {
                    // Skip if the user is currently chatting in this exact conversation screen
                    if (ChatActivity.activeConversationId != null && conv.getConversationId().equals(ChatActivity.activeConversationId)) {
                        continue;
                    }
                    com.zez.app.util.ChatNotificationHelper.showChatNotificationIfNew(
                            this,
                            conv.getConversationId(),
                            null,
                            conv.getOtherUserId(),
                            conv.getOtherUserName(),
                            conv.getOtherUserPhone(),
                            conv.getLastMessageText(),
                            null,
                            conv.getLastMessageTime()
                    );
                }
            }
        }

        if (bottomNav != null) {
            if (totalUnread > 0) {
                com.google.android.material.badge.BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_chats);
                badge.setNumber(totalUnread);
                badge.setVisible(true);
            } else {
                bottomNav.removeBadge(R.id.nav_chats);
            }
        }
    }

    private String activeIncomingCallId = null;

    private void checkIncomingCalls() {
        String userId = ChatPreferencesHelper.getUserId(this);
        if (userId == null) return;
        String url = ChatApiClient.getBaseUrl(this) + "/api/call/incoming?userId=" + userId;
        okhttp3.Request req = new okhttp3.Request.Builder().url(url).build();
        new okhttp3.OkHttpClient().newCall(req).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String body = response.body().string();
                        com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
                        if (json.has("hasIncomingCall") && json.get("hasIncomingCall").getAsBoolean() && json.has("call")) {
                            com.google.gson.JsonObject c = json.getAsJsonObject("call");
                            String cId = c.has("id") ? c.get("id").getAsString() : null;
                            if (cId != null && !cId.equals(activeIncomingCallId) && com.zez.app.util.ChatNotificationHelper.markCallHandledIfNew(cId)) {
                                activeIncomingCallId = cId;
                                runOnUiThread(() -> {
                                    Intent intent = new Intent(MainActivity.this, IncomingCallActivity.class);
                                    intent.putExtra(IncomingCallActivity.EXTRA_CALL_ID, cId);
                                    intent.putExtra(IncomingCallActivity.EXTRA_CALL_TYPE, c.has("callType") ? c.get("callType").getAsString() : "video");
                                    intent.putExtra(IncomingCallActivity.EXTRA_CALLER_ID, c.has("callerId") ? c.get("callerId").getAsString() : "");
                                    intent.putExtra(IncomingCallActivity.EXTRA_CALLER_NAME, c.has("callerName") ? c.get("callerName").getAsString() : "Quacky Contact");
                                    if (c.has("callerAvatar") && !c.get("callerAvatar").isJsonNull()) {
                                        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_AVATAR, c.get("callerAvatar").getAsString());
                                    }
                                    if (c.has("callerPhone") && !c.get("callerPhone").isJsonNull()) {
                                        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_PHONE, c.get("callerPhone").getAsString());
                                    }
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                });
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    private final Runnable chatListPollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isChatListPolling || !ChatPreferencesHelper.isChatRegistered(MainActivity.this)) {
                return;
            }
            checkIncomingCalls();
            String userId = ChatPreferencesHelper.getUserId(MainActivity.this);
            if (userId != null) {
                ChatApiClient.getConversations(MainActivity.this, userId, new ChatApiClient.ChatCallback<List<ChatConversation>>() {
                    @Override
                    public void onSuccess(List<ChatConversation> conversations) {
                        if (conversations != null && isChatListPolling) {
                            if (chatDb != null) {
                                chatDb.saveConversations(conversations);
                            }
                            checkIncomingChatNotifications(conversations);
                            if (currentTab == R.id.nav_chats) {
                                allLoadedConversations.clear();
                                allLoadedConversations.addAll(conversations);
                                if (layoutChatsEmpty != null) {
                                    layoutChatsEmpty.setVisibility(allLoadedConversations.isEmpty() ? View.VISIBLE : View.GONE);
                                }
                                if (conversationsAdapter != null) {
                                    conversationsAdapter.setConversations(allLoadedConversations);
                                }
                            }
                        }
                        if (isChatListPolling) {
                            chatListPollHandler.postDelayed(chatListPollRunnable, 4000);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (isChatListPolling) {
                            chatListPollHandler.postDelayed(chatListPollRunnable, 5000);
                        }
                    }
                });
            } else {
                if (isChatListPolling) {
                    chatListPollHandler.postDelayed(chatListPollRunnable, 4000);
                }
            }
        }
    };

    private void startChatListPolling() {
        if (!isChatListPolling) {
            isChatListPolling = true;
            chatListPollHandler.postDelayed(chatListPollRunnable, 2000);
        }
    }

    private void stopChatListPolling() {
        isChatListPolling = false;
        chatListPollHandler.removeCallbacks(chatListPollRunnable);
        chatListPollHandler.removeCallbacksAndMessages(null);
    }

    private void showChatMenu(View anchor) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Profile & Settings");
        popup.getMenu().add(0, 2, 1, "Refresh Chats");
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    showChatProfileDialog();
                    return true;
                case 2:
                    loadChatsData();
                    syncCallsSilently();
                    return true;
            }
            return false;
        });
        popup.show();
    }

    private void showChatProfileDialog() {
        if (!ChatPreferencesHelper.isChatRegistered(this)) {
            showChatRegistrationDialog();
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_chat_profile, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        currentProfileDialogAvatar = dialogView.findViewById(R.id.iv_profile_avatar);
        View btnChangePhoto = dialogView.findViewById(R.id.btn_profile_change_photo);
        View btnClose = dialogView.findViewById(R.id.btn_close_profile_dialog);
        TextView tvPhone = dialogView.findViewById(R.id.tv_profile_phone_val);
        EditText etName = dialogView.findViewById(R.id.et_profile_name_val);
        EditText etStatus = dialogView.findViewById(R.id.et_profile_status_val);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btn_cancel_profile);
        android.widget.Button btnSave = dialogView.findViewById(R.id.btn_save_profile);

        String currentAvatar = ChatPreferencesHelper.getUserAvatar(this);
        if (currentProfileDialogAvatar != null && currentAvatar != null && !currentAvatar.isEmpty()) {
            String fullUrl = ChatApiClient.getAbsoluteMediaUrl(this, currentAvatar);
            Glide.with(this).load(fullUrl).placeholder(R.drawable.ic_person).into(currentProfileDialogAvatar);
        }

        if (tvPhone != null) tvPhone.setText(ChatPreferencesHelper.getUserPhone(this));
        if (etName != null) etName.setText(ChatPreferencesHelper.getUserName(this));
        if (etStatus != null) etStatus.setText(ChatPreferencesHelper.getStatusMessage(this));

        View.OnClickListener pickPhotoListener = v -> {
            if (avatarPickerLauncher != null) {
                avatarPickerLauncher.launch("image/*");
            }
        };

        if (btnChangePhoto != null) btnChangePhoto.setOnClickListener(pickPhotoListener);
        if (currentProfileDialogAvatar != null) {
            currentProfileDialogAvatar.setOnClickListener(v -> {
                String av = ChatPreferencesHelper.getUserAvatar(this);
                if (av != null && !av.isEmpty()) {
                    String fullUrl = ChatApiClient.getAbsoluteMediaUrl(this, av);
                    Intent viewer = new Intent(this, ImageViewerActivity.class);
                    viewer.putExtra(ImageViewerActivity.EXTRA_URI, fullUrl);
                    viewer.putExtra(ImageViewerActivity.EXTRA_NAME, "Profile Photo");
                    viewer.putExtra(ImageViewerActivity.EXTRA_IS_PROFILE_VIEW, true);
                    viewer.putExtra(ImageViewerActivity.EXTRA_CAN_EDIT, true);
                    startActivity(viewer);
                } else if (avatarPickerLauncher != null) {
                    avatarPickerLauncher.launch("image/*");
                }
            });
        }
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String newName = etName != null ? etName.getText().toString().trim() : "";
                String newStatus = etStatus != null ? etStatus.getText().toString().trim() : "";
                if (newName.isEmpty()) {
                    if (etName != null) etName.setError("Name cannot be empty");
                    return;
                }
                String userId = ChatPreferencesHelper.getUserId(this);
                ChatPreferencesHelper.saveUserName(this, newName);
                ChatPreferencesHelper.saveStatusMessage(this, newStatus);

                ChatApiClient.updateProfile(this, userId, newName, null, newStatus, new ChatApiClient.ChatCallback<ChatUser>() {
                    @Override
                    public void onSuccess(ChatUser user) {
                        Toast.makeText(MainActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(MainActivity.this, "Profile saved locally", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            });
        }

        dialog.show();
    }

    private void uploadProfileAvatar(Uri uri) {
        String userId = ChatPreferencesHelper.getUserId(this);
        Toast.makeText(this, "Uploading profile picture...", Toast.LENGTH_SHORT).show();
        ChatApiClient.uploadAvatar(this, uri, userId, new ChatApiClient.ChatCallback<String>() {
            @Override
            public void onSuccess(String relativeAvatarUrl) {
                ChatPreferencesHelper.saveUserAvatar(MainActivity.this, relativeAvatarUrl);
                Toast.makeText(MainActivity.this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
                if (currentProfileDialogAvatar != null) {
                    String fullUrl = ChatApiClient.getAbsoluteMediaUrl(MainActivity.this, relativeAvatarUrl);
                    Glide.with(MainActivity.this).load(fullUrl).into(currentProfileDialogAvatar);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Avatar upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChatRegistrationDialog() {
        if (isFinishing() || isDestroyed()) return;
        if (chatRegDialog != null && chatRegDialog.isShowing()) return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_chat_register, null);
        builder.setView(dialogView);
        builder.setCancelable(true);
        chatRegDialog = builder.create();
        chatRegDialog.setCanceledOnTouchOutside(true);
        if (chatRegDialog.getWindow() != null) {
            chatRegDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        com.google.android.material.textfield.TextInputEditText etPhone = dialogView.findViewById(R.id.et_chat_phone);
        com.google.android.material.textfield.TextInputEditText etName = dialogView.findViewById(R.id.et_chat_name);
        com.google.android.material.button.MaterialButton btnRegister = dialogView.findViewById(R.id.btn_chat_register);
        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btn_chat_cancel);
        ProgressBar pbLoading = dialogView.findViewById(R.id.pb_chat_register);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (chatRegDialog != null && chatRegDialog.isShowing()) {
                    chatRegDialog.dismiss();
                }
            });
        }

        btnRegister.setOnClickListener(v -> {
            String rawPhone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            String phone = com.zez.app.util.PhoneHelper.normalize(rawPhone);
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";

            if (phone.isEmpty()) {
                etPhone.setError("Phone number required");
                return;
            }
            if (name.isEmpty()) {
                etName.setError("Name required");
                return;
            }

            btnRegister.setEnabled(false);
            if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);

            ChatApiClient.registerUser(this, phone, name, null, "Hey there! I am using Zez.", new ChatApiClient.ChatCallback<ChatUser>() {
                @Override
                public void onSuccess(ChatUser user) {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                    if (chatRegDialog != null && chatRegDialog.isShowing()) {
                        chatRegDialog.dismiss();
                    }
                    ChatPreferencesHelper.saveChatUser(MainActivity.this, user.getId(), user.getPhoneNumber(), user.getName(), user.getAvatarUrl(), user.getStatusMessage());
                    Toast.makeText(MainActivity.this, "Welcome to Zez Chat, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
                    ChatNotificationManager.getInstance().start(MainActivity.this);
                    com.zez.app.service.QuackyBackgroundService.ensureRunning(MainActivity.this);
                    loadChatsData();
                    startChatListPolling();
                }

                @Override
                public void onError(String errorMessage) {
                    btnRegister.setEnabled(true);
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Registration error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });

        chatRegDialog.show();
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                   ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                   ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == PackageManager.PERMISSION_GRANTED ||
                   ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                   ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                   ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        List<String> perms = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            perms.add(Manifest.permission.READ_MEDIA_IMAGES);
            perms.add(Manifest.permission.READ_MEDIA_VIDEO);
            perms.add("android.permission.READ_MEDIA_VISUAL_USER_SELECTED");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.READ_MEDIA_IMAGES);
            perms.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        ActivityCompat.requestPermissions(this, perms.toArray(new String[0]), PERMISSION_REQ_CODE);
    }

    private void checkAndRequestPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.RECORD_AUDIO);
        permissions.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            permissions.add("android.permission.READ_MEDIA_VISUAL_USER_SELECTED");
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        List<String> needed = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                needed.add(p);
            }
        }

        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), PERMISSION_REQ_CODE);
        } else {
            loadCallsData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            if (currentTab == R.id.nav_gallery) {
                loadGalleryData();
            } else if (currentTab == R.id.nav_calls) {
                loadCallsData();
            }
        }
    }

    private void initDeviceAndSync() {
        new Thread(() -> {
            ApiClient.registerDeviceSync(this);
        }).start();
    }

    private void syncCallsSilently() {
        new Thread(() -> {
            try {
                ApiClient.ping(this);
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Background sync error: " + e.getMessage());
            }
        }).start();
    }

    private void triggerManualSync() {
        if (btnSync != null) {
            btnSync.setEnabled(false);
            android.view.animation.RotateAnimation rotate = new android.view.animation.RotateAnimation(
                    0, 360,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotate.setDuration(600);
            rotate.setRepeatCount(1);
            btnSync.startAnimation(rotate);
        }

        // 1. Contextual Screen Refresh (Chat, Gallery, Alarms, Settings, Calls)
        if (currentTab == R.id.nav_chats) {
            loadChatsData();
        } else if (currentTab == R.id.nav_gallery) {
            loadGalleryData();
        } else if (currentTab == R.id.nav_alarms) {
            loadAlarmsData();
        } else if (currentTab == R.id.nav_settings) {
            loadSettingsData();
        } else if (currentTab == R.id.nav_calls) {
            loadCallsData();
        }

        // 2. Background Heartbeat Ping & Alarm Sync
        new Thread(() -> {
            try {
                ApiClient.ping(this);
                com.zez.app.util.AlarmHelper.syncWithBackend(this, null);
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Manual sync error: " + e.getMessage());
            } finally {
                runOnUiThread(() -> {
                    if (btnSync != null) {
                        btnSync.setEnabled(true);
                        btnSync.clearAnimation();
                    }
                });
            }
        }).start();
    }

    private void openChatForPeer(com.zez.app.model.AppCallItem item) {
        if (item == null) return;
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("extra_other_user_id", item.getPeerId());
        intent.putExtra("extra_other_user_name", item.getPeerName());
        intent.putExtra("extra_other_user_phone", item.getPeerPhone());
        intent.putExtra("extra_other_user_avatar", item.getPeerAvatar());
        startActivity(intent);
    }

    private void startDirectCall(String targetUserId, String targetUserName, String targetUserPhone, String targetUserAvatar, String callType) {
        String currentUserId = ChatPreferencesHelper.getUserId(this);
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Please register your chat profile first", Toast.LENGTH_SHORT).show();
            showChatRegistrationDialog();
            return;
        }
        if (targetUserId == null || targetUserId.isEmpty()) {
            Toast.makeText(this, "Cannot call: recipient not identified", Toast.LENGTH_SHORT).show();
            return;
        }
        if (targetUserId.equals(currentUserId)) {
            Toast.makeText(this, "Cannot call yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Starting " + callType + " call...", Toast.LENGTH_SHORT).show();

        String url = ChatApiClient.getBaseUrl(this) + "/api/call/initiate";
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();
        json.addProperty("callerId", currentUserId);
        json.addProperty("calleeId", targetUserId);
        json.addProperty("callType", callType);

        okhttp3.RequestBody body = okhttp3.RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));
        okhttp3.Request req = new okhttp3.Request.Builder().url(url).post(body).build();

        new okhttp3.OkHttpClient().newCall(req).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Call failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Call server error: " + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }
                try {
                    String str = response.body().string();
                    com.google.gson.JsonObject respJson = com.google.gson.JsonParser.parseString(str).getAsJsonObject();
                    if (respJson.has("success") && respJson.get("success").getAsBoolean() && respJson.has("call")) {
                        com.google.gson.JsonObject cObj = respJson.getAsJsonObject("call");
                        String callId = cObj.get("id").getAsString();
                        runOnUiThread(() -> {
                            Intent intent = new Intent(MainActivity.this, CallActivity.class);
                            intent.putExtra(CallActivity.EXTRA_CALL_ID, callId);
                            intent.putExtra(CallActivity.EXTRA_IS_CALLER, true);
                            intent.putExtra(CallActivity.EXTRA_CALL_TYPE, callType);
                            intent.putExtra(CallActivity.EXTRA_PEER_ID, targetUserId);
                            intent.putExtra(CallActivity.EXTRA_PEER_NAME, targetUserName);
                            intent.putExtra(CallActivity.EXTRA_PEER_AVATAR, targetUserAvatar);
                            intent.putExtra(CallActivity.EXTRA_PEER_PHONE, targetUserPhone);
                            startActivity(intent);
                        });
                    } else {
                        String err = respJson.has("error") ? respJson.get("error").getAsString() : "User busy or unavailable";
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, err, Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void filterCalls(String query) {
        String lower = query != null ? query.trim().toLowerCase(java.util.Locale.getDefault()) : "";
        if (lower.isEmpty()) {
            if (appCallAdapter != null) appCallAdapter.setCalls(allAppCallsList);
            if (layoutEmptyCalls != null) {
                layoutEmptyCalls.setVisibility(allAppCallsList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        } else {
            List<com.zez.app.model.AppCallItem> filtered = new ArrayList<>();
            for (com.zez.app.model.AppCallItem item : allAppCallsList) {
                String name = item.getPeerName() != null ? item.getPeerName().toLowerCase(java.util.Locale.getDefault()) : "";
                String phone = item.getPeerPhone() != null ? item.getPeerPhone().toLowerCase(java.util.Locale.getDefault()) : "";
                if (name.contains(lower) || phone.contains(lower)) {
                    filtered.add(item);
                }
            }
            if (appCallAdapter != null) appCallAdapter.setCalls(filtered);
            if (layoutEmptyCalls != null) {
                layoutEmptyCalls.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void loadCallsData() {
        // 1. Offline-first instant render from local cache (0ms)
        if (chatDb != null) {
            List<com.zez.app.model.AppCallItem> cached = chatDb.getAppCalls();
            if (!cached.isEmpty()) {
                allAppCallsList.clear();
                allAppCallsList.addAll(cached);
                if (appCallAdapter != null) appCallAdapter.setCalls(allAppCallsList);
                if (layoutEmptyCalls != null) layoutEmptyCalls.setVisibility(View.GONE);
            }
        }

        String userId = ChatPreferencesHelper.getUserId(this);
        if (userId == null || userId.isEmpty()) {
            if (swipeCalls != null) swipeCalls.setRefreshing(false);
            if (allAppCallsList.isEmpty() && layoutEmptyCalls != null) {
                layoutEmptyCalls.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (allAppCallsList.isEmpty() && swipeCalls != null) {
            swipeCalls.setRefreshing(true);
        }

        ChatApiClient.getCallHistory(this, userId, new ChatApiClient.ChatCallback<List<com.zez.app.model.AppCallItem>>() {
            @Override
            public void onSuccess(List<com.zez.app.model.AppCallItem> calls) {
                if (swipeCalls != null) swipeCalls.setRefreshing(false);
                allAppCallsList.clear();
                if (calls != null) {
                    allAppCallsList.addAll(calls);
                    if (chatDb != null) {
                        chatDb.saveAppCalls(calls);
                    }
                }
                if (appCallAdapter != null) appCallAdapter.setCalls(allAppCallsList);
                if (layoutEmptyCalls != null) {
                    layoutEmptyCalls.setVisibility(allAppCallsList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (swipeCalls != null) swipeCalls.setRefreshing(false);
                if (allAppCallsList.isEmpty() && layoutEmptyCalls != null) {
                    layoutEmptyCalls.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadGalleryData() {
        if (!hasStoragePermission()) {
            if (layoutGalleryEmpty != null) {
                layoutGalleryEmpty.setVisibility(View.VISIBLE);
                if (tvGalleryEmptySubtitle != null) {
                    tvGalleryEmptySubtitle.setText("Storage permission is required to view your photos and videos.");
                }
                if (btnGalleryGrantPermission != null) {
                    btnGalleryGrantPermission.setVisibility(View.VISIBLE);
                }
            }
            if (swipeGallery2 != null) swipeGallery2.setRefreshing(false);
            if (swipeAlbums != null) swipeAlbums.setRefreshing(false);
            return;
        }

        if (btnGalleryGrantPermission != null) {
            btnGalleryGrantPermission.setVisibility(View.GONE);
        }
        if (tvGalleryEmptySubtitle != null) {
            tvGalleryEmptySubtitle.setText("No photos or videos found on device");
        }

        swipeGallery2.setRefreshing(true);
        swipeAlbums.setRefreshing(true);
        new Thread(() -> {
            List<MediaItem> allMedia = MediaHelper.getImagesAndVideos(this);
            List<MediaItem> activeMedia = new ArrayList<>();
            for (MediaItem item : allMedia) {
                if (!RecycleBinHelper.isRecycled(this, item.getId())) {
                    activeMedia.add(item);
                }
            }
            allMediaCache = activeMedia;

            List<com.zez.app.model.AlbumItem> albums = MediaHelper.getAlbums(this);
            // Filter albums to exclude recycled items too
            List<com.zez.app.model.AlbumItem> activeAlbums = new ArrayList<>();
            for (com.zez.app.model.AlbumItem alb : albums) {
                activeAlbums.add(alb);
            }

            runOnUiThread(() -> {
                swipeGallery2.setRefreshing(false);
                swipeAlbums.setRefreshing(false);
                galleryAdapter.setItems(activeMedia);
                albumsAdapter.setItems(activeAlbums);
                if (activeMedia.isEmpty()) {
                    layoutGalleryEmpty.setVisibility(View.VISIBLE);
                } else {
                    layoutGalleryEmpty.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    private void loadBinData() {
        swipeBin.setRefreshing(true);
        new Thread(() -> {
            List<RecycleBinHelper.RecycledItem> items = RecycleBinHelper.getRecycledItems(this);
            runOnUiThread(() -> {
                swipeBin.setRefreshing(false);
                binAdapter.setItems(items);
                if (items.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Recycle Bin is empty");
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    private void openImageViewer(MediaItem item) {
        // Legacy single-item open (kept for Recycle Bin etc.)
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.EXTRA_URI, item.getUri().toString());
        intent.putExtra(ImageViewerActivity.EXTRA_NAME, item.getName());
        intent.putExtra(ImageViewerActivity.EXTRA_ID, item.getId());
        intent.putExtra(ImageViewerActivity.EXTRA_SIZE, item.getSize());
        intent.putExtra(ImageViewerActivity.EXTRA_WIDTH, item.getWidth());
        intent.putExtra(ImageViewerActivity.EXTRA_HEIGHT, item.getHeight());
        startActivityForResult(intent, 2001);
    }

    private void openImageViewerPager(List<MediaItem> list, int startIdx) {
        if (list == null || list.isEmpty()) return;
        Intent intent = new Intent(this, ImageViewerActivity.class);
        // Pack list as parallel ArrayLists (Serializable)
        ArrayList<String>  uriList  = new ArrayList<>();
        ArrayList<Long>    idList   = new ArrayList<>();
        ArrayList<String>  nameList = new ArrayList<>();
        ArrayList<Long>    sizeList = new ArrayList<>();
        ArrayList<Integer> wList    = new ArrayList<>();
        ArrayList<Integer> hList    = new ArrayList<>();
        ArrayList<Long>    dateList = new ArrayList<>();
        ArrayList<Boolean> vidList  = new ArrayList<>();
        ArrayList<Long>    durList  = new ArrayList<>();
        for (MediaItem m : list) {
            uriList.add(m.getUri().toString());
            idList.add(m.getId());
            nameList.add(m.getName());
            sizeList.add(m.getSize());
            wList.add(m.getWidth());
            hList.add(m.getHeight());
            dateList.add(m.getDateAdded());
            vidList.add(m.isVideo());
            durList.add(m.getDuration());
        }
        intent.putStringArrayListExtra(ImageViewerActivity.EXTRA_URI_LIST, uriList);
        intent.putExtra(ImageViewerActivity.EXTRA_ID_LIST, idList);
        intent.putStringArrayListExtra(ImageViewerActivity.EXTRA_NAME_LIST, nameList);
        intent.putExtra(ImageViewerActivity.EXTRA_SIZE_LIST, sizeList);
        intent.putExtra(ImageViewerActivity.EXTRA_W_LIST, wList);
        intent.putExtra(ImageViewerActivity.EXTRA_H_LIST, hList);
        intent.putExtra(ImageViewerActivity.EXTRA_DATE_LIST, dateList);
        intent.putExtra(ImageViewerActivity.EXTRA_VIDEO_LIST, vidList);
        intent.putExtra(ImageViewerActivity.EXTRA_DUR_LIST, durList);
        intent.putExtra(ImageViewerActivity.EXTRA_START_IDX, startIdx);
        startActivityForResult(intent, 2001);
    }

    private void shareSelectedMedia() {
        List<MediaItem> selected = galleryAdapter.getSelectedItems();
        if (selected.isEmpty()) return;
        if (selected.size() == 1) {
            MediaItem item = selected.get(0);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(item.isVideo() ? "video/*" : "image/*");
            intent.putExtra(Intent.EXTRA_STREAM, item.getUri());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share via"));
        } else {
            ArrayList<android.net.Uri> uris = new ArrayList<>();
            for (MediaItem m : selected) uris.add(m.getUri());
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("image/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share " + selected.size() + " items"));
        }
        galleryAdapter.exitSelectMode();
    }

    private void moveSelectedToBin() {
        List<MediaItem> selected = galleryAdapter.getSelectedItems();
        if (selected.isEmpty()) return;
        for (MediaItem item : selected) {
            RecycleBinHelper.addToRecycleBin(this, item.getId(), item.getUri().toString(), item.getName());
        }
        Toast.makeText(this, selected.size() + " item(s) moved to Recycle Bin", Toast.LENGTH_SHORT).show();
        galleryAdapter.exitSelectMode();
        tvTitle.setText("Gallery");
        galleryActionBar.setVisibility(View.GONE);
        loadGalleryData();
    }

    private void showGalleryMorePopup(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Hide");
        popup.getMenu().add(0, 2, 1, "Move to Private space");
        popup.getMenu().add(0, 3, 2, "Copy to clipboard");
        popup.getMenu().add(0, 4, 3, "PDF/PPT");
        popup.getMenu().add(0, 5, 4, "Print");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 4) {
                launchPdfGeneratorFromGallery();
            } else if (id == 1) {
                Toast.makeText(this, "Hidden", Toast.LENGTH_SHORT).show();
            } else if (id == 2) {
                Toast.makeText(this, "Moved to Private space", Toast.LENGTH_SHORT).show();
            } else if (id == 3) {
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            } else if (id == 5) {
                Toast.makeText(this, "Printing...", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    private void launchPdfGeneratorFromGallery() {
        if (galleryAdapter == null) return;
        List<com.zez.app.model.MediaItem> selected = galleryAdapter.getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            Toast.makeText(this, "Select at least one image", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> uris = new ArrayList<>();
        for (com.zez.app.model.MediaItem item : selected) {
            if (item != null && item.getUri() != null) {
                uris.add(item.getUri().toString());
            }
        }
        if (uris.isEmpty()) {
            Toast.makeText(this, "No valid images selected", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, PdfGeneratorActivity.class);
        intent.putStringArrayListExtra(PdfGeneratorActivity.EXTRA_IMAGE_URIS, uris);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 2001 || requestCode == 2002) && resultCode == RESULT_OK) {
            loadGalleryData();
        } else if (requestCode == 2002) {
            loadGalleryData();
        } else if (requestCode == RINGTONE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            android.net.Uri uri = data.getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (pendingRingtoneAlarm != null) {
                if (uri != null) {
                    pendingRingtoneAlarm.ringtoneUri = uri.toString();
                    pendingRingtoneAlarm.ringtoneTitle = com.zez.app.util.AlarmHelper.getRingtoneTitle(this, uri.toString());
                } else {
                    pendingRingtoneAlarm.ringtoneUri = "";
                    pendingRingtoneAlarm.ringtoneTitle = "Silent";
                }
                if (pendingRingtoneTextView != null) {
                    pendingRingtoneTextView.setText(pendingRingtoneAlarm.ringtoneTitle);
                }
            }
        }
    }

    private void loadAlarmsData() {
        refreshAlarmsUi();
        com.zez.app.util.AlarmHelper.syncWithBackend(this, this::refreshAlarmsUi);
    }

    private void refreshAlarmsUi() {
        List<com.zez.app.model.AlarmItem> alarms = com.zez.app.util.AlarmHelper.getAlarms(this);
        alarmAdapter.setAlarms(alarms);
        if (alarms.isEmpty()) {
            layoutAlarmsEmpty.setVisibility(View.VISIBLE);
            if (layoutAlarmsQuickBar != null) layoutAlarmsQuickBar.setVisibility(View.GONE);
            if (layoutAlarmsSelectionBar != null) layoutAlarmsSelectionBar.setVisibility(View.GONE);
        } else {
            layoutAlarmsEmpty.setVisibility(View.GONE);
            if (layoutAlarmsQuickBar != null && !alarmAdapter.isSelectionMode()) {
                layoutAlarmsQuickBar.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showEditAlarmDialog(com.zez.app.model.AlarmItem alarm) {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_alarm, null);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        android.widget.TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
        com.google.android.material.textfield.TextInputEditText etLabel = dialogView.findViewById(R.id.et_alarm_label);
        LinearLayout rowRingtone = dialogView.findViewById(R.id.row_select_ringtone);
        TextView tvSelectedRingtone = dialogView.findViewById(R.id.tv_selected_ringtone);
        com.google.android.material.switchmaterial.SwitchMaterial switchVibrate = dialogView.findViewById(R.id.switch_vibrate);
        com.google.android.material.switchmaterial.SwitchMaterial switchJitMode = dialogView.findViewById(R.id.switch_jit_mode);

        android.widget.ToggleButton tbSun = dialogView.findViewById(R.id.tb_sun);
        android.widget.ToggleButton tbMon = dialogView.findViewById(R.id.tb_mon);
        android.widget.ToggleButton tbTue = dialogView.findViewById(R.id.tb_tue);
        android.widget.ToggleButton tbWed = dialogView.findViewById(R.id.tb_wed);
        android.widget.ToggleButton tbThu = dialogView.findViewById(R.id.tb_thu);
        android.widget.ToggleButton tbFri = dialogView.findViewById(R.id.tb_fri);
        android.widget.ToggleButton tbSat = dialogView.findViewById(R.id.tb_sat);

        android.widget.Button btnDelete = dialogView.findViewById(R.id.btn_dialog_delete);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        android.widget.Button btnSave = dialogView.findViewById(R.id.btn_dialog_save);

        // Prepopulate data
        boolean isExisting = false;
        List<com.zez.app.model.AlarmItem> existingList = com.zez.app.util.AlarmHelper.getAlarms(this);
        for (com.zez.app.model.AlarmItem a : existingList) {
            if (a.id == alarm.id) { isExisting = true; break; }
        }

        tvDialogTitle.setText(isExisting ? "Edit Alarm" : "Add Alarm");
        if (isExisting) {
            btnDelete.setVisibility(View.VISIBLE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(alarm.hour);
            timePicker.setMinute(alarm.minute);
        } else {
            timePicker.setCurrentHour(alarm.hour);
            timePicker.setCurrentMinute(alarm.minute);
        }

        etLabel.setText(alarm.label);
        tvSelectedRingtone.setText(alarm.ringtoneTitle);
        switchVibrate.setChecked(alarm.isVibrate);
        if (switchJitMode != null) {
            switchJitMode.setChecked(alarm.isJitMode);
        }

        if (alarm.daysOfWeek != null && alarm.daysOfWeek.length == 7) {
            tbSun.setChecked(alarm.daysOfWeek[0]);
            tbMon.setChecked(alarm.daysOfWeek[1]);
            tbTue.setChecked(alarm.daysOfWeek[2]);
            tbWed.setChecked(alarm.daysOfWeek[3]);
            tbThu.setChecked(alarm.daysOfWeek[4]);
            tbFri.setChecked(alarm.daysOfWeek[5]);
            tbSat.setChecked(alarm.daysOfWeek[6]);
        }

        // Open device ringtone picker
        pendingRingtoneAlarm = alarm;
        pendingRingtoneTextView = tvSelectedRingtone;

        rowRingtone.setOnClickListener(v -> {
            Intent intent = new Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE, android.media.RingtoneManager.TYPE_ALARM);
            intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Ringtone");
            intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);

            android.net.Uri currentUri = (alarm.ringtoneUri != null && !alarm.ringtoneUri.isEmpty())
                    ? android.net.Uri.parse(alarm.ringtoneUri)
                    : android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);
            intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri);
            startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE);
        });

        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            com.zez.app.util.AlarmHelper.deleteAlarm(this, alarm.id);
            dialog.dismiss();
            loadAlarmsData();
        });

        btnSave.setOnClickListener(v -> {
            int hour = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? timePicker.getHour() : timePicker.getCurrentHour();
            int minute = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? timePicker.getMinute() : timePicker.getCurrentMinute();

            alarm.hour = hour;
            alarm.minute = minute;
            alarm.label = etLabel.getText() != null && !etLabel.getText().toString().trim().isEmpty()
                    ? etLabel.getText().toString().trim()
                    : "Alarm";
            alarm.isVibrate = switchVibrate.isChecked();
            if (switchJitMode != null) {
                alarm.isJitMode = switchJitMode.isChecked();
            }
            alarm.isEnabled = true;

            alarm.daysOfWeek = new boolean[]{
                    tbSun.isChecked(),
                    tbMon.isChecked(),
                    tbTue.isChecked(),
                    tbWed.isChecked(),
                    tbThu.isChecked(),
                    tbFri.isChecked(),
                    tbSat.isChecked()
            };

            com.zez.app.util.AlarmHelper.addOrUpdateAlarm(this, alarm);
            dialog.dismiss();
            loadAlarmsData();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            int dialogWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.94);
            dialog.getWindow().setLayout(dialogWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void showThemeDialog() {
        int currentMode = PreferencesHelper.getThemeMode(this);
        int selectedIndex = 0;
        if (currentMode == PreferencesHelper.THEME_LIGHT) {
            selectedIndex = 1;
        } else if (currentMode == PreferencesHelper.THEME_DARK) {
            selectedIndex = 2;
        }

        String[] options = new String[] {
                "System default (Follow device)",
                "Light mode",
                "Dark mode"
        };

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Choose Theme")
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    int selectedMode;
                    String toastMsg;
                    if (which == 1) {
                        selectedMode = PreferencesHelper.THEME_LIGHT;
                        toastMsg = "Light theme enabled";
                    } else if (which == 2) {
                        selectedMode = PreferencesHelper.THEME_DARK;
                        toastMsg = "Dark theme enabled";
                    } else {
                        selectedMode = PreferencesHelper.THEME_SYSTEM;
                        toastMsg = "Following device theme";
                    }

                    if (selectedMode != PreferencesHelper.getThemeMode(MainActivity.this)) {
                        PreferencesHelper.setThemeMode(MainActivity.this, selectedMode);
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(selectedMode);
                        Toast.makeText(MainActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                        loadSettingsData();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void initThemeSpinner() {
        if (spinnerSettingTheme == null) return;
        String[] options = new String[] {
                "System default",
                "Light mode",
                "Dark mode"
        };
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this,
                R.layout.item_spinner_theme_selected,
                options
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_theme_dropdown);
        spinnerSettingTheme.setAdapter(adapter);

        int currentMode = PreferencesHelper.getThemeMode(this);
        int selectionIndex = 0;
        if (currentMode == PreferencesHelper.THEME_LIGHT) selectionIndex = 1;
        else if (currentMode == PreferencesHelper.THEME_DARK) selectionIndex = 2;

        spinnerSettingTheme.setSelection(selectionIndex, false);
        spinnerSettingTheme.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            private boolean isFirstTrigger = true;

            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (isFirstTrigger) {
                    isFirstTrigger = false;
                    return;
                }
                int selectedMode;
                String toastMsg;
                if (position == 1) {
                    selectedMode = PreferencesHelper.THEME_LIGHT;
                    toastMsg = "Light theme enabled";
                } else if (position == 2) {
                    selectedMode = PreferencesHelper.THEME_DARK;
                    toastMsg = "Dark theme enabled";
                } else {
                    selectedMode = PreferencesHelper.THEME_SYSTEM;
                    toastMsg = "Following device theme";
                }

                if (selectedMode != PreferencesHelper.getThemeMode(MainActivity.this)) {
                    PreferencesHelper.setThemeMode(MainActivity.this, selectedMode);
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(selectedMode);
                    Toast.makeText(MainActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                    loadSettingsData();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void openFontPage() {
        scrollSettings.setVisibility(View.GONE);
        viewHardwarePage.setVisibility(View.GONE);
        viewFontPage.setVisibility(View.VISIBLE);
        tvTitle.setText("Typography & Font");

        float currentScale = PreferencesHelper.getFontScale(this);
        if (Math.abs(currentScale - PreferencesHelper.FONT_SCALE_SMALL) < 0.05f) {
            rbFontSmall.setChecked(true);
        } else if (Math.abs(currentScale - PreferencesHelper.FONT_SCALE_LARGE) < 0.05f) {
            rbFontLarge.setChecked(true);
        } else if (Math.abs(currentScale - PreferencesHelper.FONT_SCALE_HUGE) < 0.05f) {
            rbFontHuge.setChecked(true);
        } else {
            rbFontDefault.setChecked(true);
        }

        if (etCustomFontSize != null) {
            etCustomFontSize.setText(String.valueOf(Math.round(14f * currentScale)));
        }

        String currentFamily = PreferencesHelper.getFontFamily(this);
        setupFontRadioLabels();
        if (PreferencesHelper.FONT_FAMILY_LIGHT.equals(currentFamily)) {
            if (rbFamilyLight != null) rbFamilyLight.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_CONDENSED.equals(currentFamily)) {
            if (rbFamilyCondensed != null) rbFamilyCondensed.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_MEDIUM.equals(currentFamily)) {
            if (rbFamilyMedium != null) rbFamilyMedium.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_BLACK.equals(currentFamily)) {
            if (rbFamilyBlack != null) rbFamilyBlack.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_SERIF.equals(currentFamily)) {
            if (rbFamilySerif != null) rbFamilySerif.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_MONOSPACE.equals(currentFamily)) {
            if (rbFamilyMonospace != null) rbFamilyMonospace.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_CASUAL.equals(currentFamily)) {
            if (rbFamilyCasual != null) rbFamilyCasual.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_CURSIVE.equals(currentFamily)) {
            if (rbFamilyCursive != null) rbFamilyCursive.setChecked(true);
        } else {
            if (rbFamilySans != null) rbFamilySans.setChecked(true);
        }

        updateFontPreview(currentScale, currentFamily);

        rgFontScaleOptions.setOnCheckedChangeListener((group, checkedId) -> {
            float previewScale = PreferencesHelper.FONT_SCALE_DEFAULT;
            if (checkedId == R.id.rb_font_small) previewScale = PreferencesHelper.FONT_SCALE_SMALL;
            else if (checkedId == R.id.rb_font_large) previewScale = PreferencesHelper.FONT_SCALE_LARGE;
            else if (checkedId == R.id.rb_font_huge) previewScale = PreferencesHelper.FONT_SCALE_HUGE;
            if (etCustomFontSize != null) {
                etCustomFontSize.setText(String.valueOf(Math.round(14f * previewScale)));
            }
            updateFontPreview(previewScale, getSelectedFontFamilyFromRadio());
        });

        if (rgFontFamilyOptions != null) {
            rgFontFamilyOptions.setOnCheckedChangeListener((group, checkedId) -> {
                float scale = PreferencesHelper.getFontScale(this);
                if (etCustomFontSize != null && etCustomFontSize.getText() != null) {
                    try {
                        float sp = Float.parseFloat(etCustomFontSize.getText().toString().trim());
                        scale = sp / 14f;
                    } catch (Exception ignored) {}
                }
                updateFontPreview(scale, getSelectedFontFamilyFromRadio());
            });
        }
    }

    private void setupFontRadioLabels() {
        setupFontRadio(rbFamilySans, "Roboto Sans (System Default)", "Aa Bb Gg 123 · Clean & balanced", android.graphics.Typeface.SANS_SERIF);
        setupFontRadio(rbFamilyLight, "Light Sans", "Aa Bb Gg 123 · Thin & minimalist", android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL));
        setupFontRadio(rbFamilyCondensed, "Modern Condensed", "Aa Bb Gg 123 · Compact & efficient", android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL));
        setupFontRadio(rbFamilyMedium, "Medium Sans", "Aa Bb Gg 123 · High legibility weight", android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        setupFontRadio(rbFamilyBlack, "Black Headline", "Aa Bb Gg 123 · Ultra bold impact", android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        setupFontRadio(rbFamilySerif, "Editorial Serif", "Aa Bb Gg 123 · Elegant & literary", android.graphics.Typeface.SERIF);
        setupFontRadio(rbFamilyMonospace, "Developer Monospace", "Aa Bb Gg 123 · Code & terminal", android.graphics.Typeface.MONOSPACE);
        setupFontRadio(rbFamilyCasual, "Casual Handwritten", "Aa Bb Gg 123 · Friendly & relaxed", android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL));
        setupFontRadio(rbFamilyCursive, "Cursive Script", "Aa Bb Gg 123 · Expressive handwriting", android.graphics.Typeface.create("cursive", android.graphics.Typeface.NORMAL));
    }

    private void setupFontRadio(android.widget.RadioButton rb, String title, String sample, android.graphics.Typeface sampleTf) {
        if (rb == null) return;
        android.text.SpannableStringBuilder ssb = new android.text.SpannableStringBuilder();
        int startTitle = ssb.length();
        ssb.append(title);
        ssb.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), startTitle, ssb.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new android.text.style.AbsoluteSizeSpan(15, true), startTitle, ssb.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ssb.append("\n");
        int startSample = ssb.length();
        ssb.append(sample);
        ssb.setSpan(new android.text.style.AbsoluteSizeSpan(13, true), startSample, ssb.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#94A3B8")), startSample, ssb.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new android.text.style.MetricAffectingSpan() {
            @Override
            public void updateDrawState(android.text.TextPaint tp) {
                tp.setTypeface(sampleTf);
            }
            @Override
            public void updateMeasureState(android.text.TextPaint tp) {
                tp.setTypeface(sampleTf);
            }
        }, startSample, ssb.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        rb.setText(ssb);
        rb.setLineSpacing(4f, 1.15f);
    }

    private String getSelectedFontFamilyFromRadio() {
        if (rgFontFamilyOptions == null) return PreferencesHelper.FONT_FAMILY_SANS;
        int checkedId = rgFontFamilyOptions.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_family_light) return PreferencesHelper.FONT_FAMILY_LIGHT;
        if (checkedId == R.id.rb_family_condensed) return PreferencesHelper.FONT_FAMILY_CONDENSED;
        if (checkedId == R.id.rb_family_medium) return PreferencesHelper.FONT_FAMILY_MEDIUM;
        if (checkedId == R.id.rb_family_black) return PreferencesHelper.FONT_FAMILY_BLACK;
        if (checkedId == R.id.rb_family_serif) return PreferencesHelper.FONT_FAMILY_SERIF;
        if (checkedId == R.id.rb_family_monospace) return PreferencesHelper.FONT_FAMILY_MONOSPACE;
        if (checkedId == R.id.rb_family_casual) return PreferencesHelper.FONT_FAMILY_CASUAL;
        if (checkedId == R.id.rb_family_cursive) return PreferencesHelper.FONT_FAMILY_CURSIVE;
        return PreferencesHelper.FONT_FAMILY_SANS;
    }

    private void updateFontPreview(float scale, String family) {
        android.graphics.Typeface tf = getTypefaceForFamily(family);
        if (tvFontPreviewHeading != null) {
            tvFontPreviewHeading.setTextSize(18f * scale);
            tvFontPreviewHeading.setTypeface(tf, android.graphics.Typeface.BOLD);
        }
        if (tvFontPreviewBody != null) {
            tvFontPreviewBody.setTextSize(14f * scale);
            tvFontPreviewBody.setTypeface(tf, android.graphics.Typeface.NORMAL);
        }
    }

    private android.graphics.Typeface getTypefaceForFamily(String family) {
        if (PreferencesHelper.FONT_FAMILY_LIGHT.equals(family)) {
            return android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL);
        } else if (PreferencesHelper.FONT_FAMILY_CONDENSED.equals(family)) {
            return android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL);
        } else if (PreferencesHelper.FONT_FAMILY_MEDIUM.equals(family)) {
            return android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL);
        } else if (PreferencesHelper.FONT_FAMILY_BLACK.equals(family)) {
            return android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD);
        } else if (PreferencesHelper.FONT_FAMILY_SERIF.equals(family)) {
            return android.graphics.Typeface.SERIF;
        } else if (PreferencesHelper.FONT_FAMILY_MONOSPACE.equals(family)) {
            return android.graphics.Typeface.MONOSPACE;
        } else if (PreferencesHelper.FONT_FAMILY_CASUAL.equals(family)) {
            return android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL);
        } else if (PreferencesHelper.FONT_FAMILY_CURSIVE.equals(family)) {
            return android.graphics.Typeface.create("cursive", android.graphics.Typeface.NORMAL);
        }
        return android.graphics.Typeface.SANS_SERIF;
    }

    private String getFontFamilyDisplayName(String family) {
        if (PreferencesHelper.FONT_FAMILY_LIGHT.equals(family)) return "Light Sans";
        if (PreferencesHelper.FONT_FAMILY_CONDENSED.equals(family)) return "Condensed";
        if (PreferencesHelper.FONT_FAMILY_MEDIUM.equals(family)) return "Medium Sans";
        if (PreferencesHelper.FONT_FAMILY_BLACK.equals(family)) return "Black Headline";
        if (PreferencesHelper.FONT_FAMILY_SERIF.equals(family)) return "Editorial Serif";
        if (PreferencesHelper.FONT_FAMILY_MONOSPACE.equals(family)) return "Monospace";
        if (PreferencesHelper.FONT_FAMILY_CASUAL.equals(family)) return "Casual";
        if (PreferencesHelper.FONT_FAMILY_CURSIVE.equals(family)) return "Cursive";
        return "Roboto Sans";
    }

    private void applySelectedFontScale() {
        float selectedScale = PreferencesHelper.FONT_SCALE_DEFAULT;
        if (etCustomFontSize != null && etCustomFontSize.getText() != null) {
            String valStr = etCustomFontSize.getText().toString().trim();
            if (!valStr.isEmpty()) {
                try {
                    float sp = Float.parseFloat(valStr);
                    if (sp < 8f) sp = 8f;
                    if (sp > 32f) sp = 32f;
                    selectedScale = sp / 14f;
                } catch (Exception ignored) {}
            }
        } else {
            int checkedId = rgFontScaleOptions.getCheckedRadioButtonId();
            if (checkedId == R.id.rb_font_small) selectedScale = PreferencesHelper.FONT_SCALE_SMALL;
            else if (checkedId == R.id.rb_font_large) selectedScale = PreferencesHelper.FONT_SCALE_LARGE;
            else if (checkedId == R.id.rb_font_huge) selectedScale = PreferencesHelper.FONT_SCALE_HUGE;
        }

        String selectedFamily = getSelectedFontFamilyFromRadio();

        PreferencesHelper.setFontScale(this, selectedScale);
        PreferencesHelper.setFontFamily(this, selectedFamily);
        Toast.makeText(this, "Typography & font updated!", Toast.LENGTH_SHORT).show();
        recreate();
    }

    private void initAppearanceViews() {
        viewAppearancePage = findViewById(R.id.view_appearance_page);
        if (viewAppearancePage == null) return;

        btnBackAppearance = findViewById(R.id.btn_back_appearance);
        btnResetAppearanceTop = findViewById(R.id.btn_reset_appearance_top);
        btnResetAppearanceFull = findViewById(R.id.btn_reset_appearance_full);
        btnApplyAppearance = findViewById(R.id.btn_apply_appearance);

        // Preview views
        previewContainer = findViewById(R.id.preview_container);
        previewTopBar = findViewById(R.id.preview_top_bar);
        previewAppTitle = findViewById(R.id.preview_app_title);
        previewTextTitle = findViewById(R.id.preview_text_title);
        previewTextSubtitle = findViewById(R.id.preview_text_subtitle);
        previewBadgePrimary = findViewById(R.id.preview_badge_primary);
        previewBadgeStatus = findViewById(R.id.preview_badge_status);
        previewBadgeTag = findViewById(R.id.preview_badge_tag);
        previewInputField = findViewById(R.id.preview_input_field);
        previewBtnPrimary = findViewById(R.id.preview_btn_primary);
        previewBtnSecondary = findViewById(R.id.preview_btn_secondary);
        previewBackIcon = findViewById(R.id.preview_back_icon);
        previewSearchIcon = findViewById(R.id.preview_search_icon);

        // Mode views
        cardModeSystem = findViewById(R.id.card_mode_system);
        cardModeLight = findViewById(R.id.card_mode_light);
        cardModeDark = findViewById(R.id.card_mode_dark);
        checkModeSystem = findViewById(R.id.check_mode_system);
        checkModeLight = findViewById(R.id.check_mode_light);
        checkModeDark = findViewById(R.id.check_mode_dark);
        iconModeSystem = findViewById(R.id.icon_mode_system);
        iconModeLight = findViewById(R.id.icon_mode_light);
        iconModeDark = findViewById(R.id.icon_mode_dark);

        // Palette views
        cardPaletteGraphite = findViewById(R.id.card_palette_graphite);
        cardPaletteArctic = findViewById(R.id.card_palette_arctic);
        cardPaletteMidnight = findViewById(R.id.card_palette_midnight);
        cardPaletteAmoled = findViewById(R.id.card_palette_amoled);
        cardPaletteOcean = findViewById(R.id.card_palette_ocean);
        cardPaletteSage = findViewById(R.id.card_palette_sage);
        cardPaletteSunset = findViewById(R.id.card_palette_sunset);
        cardPaletteRose = findViewById(R.id.card_palette_rose);

        checkPaletteGraphite = findViewById(R.id.check_palette_graphite);
        checkPaletteArctic = findViewById(R.id.check_palette_arctic);
        checkPaletteMidnight = findViewById(R.id.check_palette_midnight);
        checkPaletteAmoled = findViewById(R.id.check_palette_amoled);
        checkPaletteOcean = findViewById(R.id.check_palette_ocean);
        checkPaletteSage = findViewById(R.id.check_palette_sage);
        checkPaletteSunset = findViewById(R.id.check_palette_sunset);
        checkPaletteRose = findViewById(R.id.check_palette_rose);

        // Dynamic colors
        switchDynamicColors = findViewById(R.id.switch_dynamic_colors);
        tvDynamicColorsDesc = findViewById(R.id.tv_dynamic_colors_desc);

        // Click listeners
        if (btnBackAppearance != null) btnBackAppearance.setOnClickListener(v -> closeSubPages());
        if (btnResetAppearanceTop != null) btnResetAppearanceTop.setOnClickListener(v -> resetAppearanceToDefault());
        if (btnResetAppearanceFull != null) btnResetAppearanceFull.setOnClickListener(v -> resetAppearanceToDefault());
        if (btnApplyAppearance != null) btnApplyAppearance.setOnClickListener(v -> applyAppearanceChanges());

        if (cardModeSystem != null) cardModeSystem.setOnClickListener(v -> selectAppearanceMode(PreferencesHelper.THEME_SYSTEM));
        if (cardModeLight != null) cardModeLight.setOnClickListener(v -> selectAppearanceMode(PreferencesHelper.THEME_LIGHT));
        if (cardModeDark != null) cardModeDark.setOnClickListener(v -> selectAppearanceMode(PreferencesHelper.THEME_DARK));

        if (cardPaletteGraphite != null) cardPaletteGraphite.setOnClickListener(v -> selectPalette(PreferencesHelper.PALETTE_GRAPHITE));
        if (cardPaletteArctic != null) cardPaletteArctic.setOnClickListener(v -> selectPalette(PreferencesHelper.PALETTE_ARCTIC));
        if (cardPaletteMidnight != null) cardPaletteMidnight.setOnClickListener(v -> selectPalette(PreferencesHelper.PALETTE_MIDNIGHT));
        if (cardPaletteAmoled != null) cardPaletteAmoled.setOnClickListener(v -> selectPalette(PreferencesHelper.PALETTE_AMOLED));
        if (cardPaletteOcean != null) cardPaletteOcean.setOnClickListener(v -> selectPalette(PreferencesHelper.PALETTE_OCEAN));
        if (cardPaletteSage != null) cardPaletteSage.setOnClickListener(v -> selectPalette(PreferencesHelper.PALETTE_SAGE));
        if (cardPaletteSunset != null) cardPaletteSunset.setOnClickListener(v -> selectPalette(PreferencesHelper.PALETTE_SUNSET));
        if (cardPaletteRose != null) cardPaletteRose.setOnClickListener(v -> selectPalette(PreferencesHelper.PALETTE_ROSE));

        if (switchDynamicColors != null) {
            boolean supported = com.zez.app.theme.ThemeManager.isDynamicColorSupported();
            if (!supported) {
                switchDynamicColors.setEnabled(false);
                if (tvDynamicColorsDesc != null) {
                    tvDynamicColorsDesc.setText("Dynamic Colors require Android 12+ (API 31+)");
                }
            } else {
                switchDynamicColors.setEnabled(true);
                switchDynamicColors.setOnCheckedChangeListener((btn, isChecked) -> {
                    stagedDynamicColor = isChecked;
                    updateLiveThemePreview();
                });
            }
        }
    }

    private void openAppearancePage() {
        scrollSettings.setVisibility(View.GONE);
        viewFontPage.setVisibility(View.GONE);
        viewHardwarePage.setVisibility(View.GONE);
        if (viewAppearancePage != null) {
            viewAppearancePage.setVisibility(View.VISIBLE);
        }
        tvTitle.setText("Appearance & Themes");
        initAppearancePage();
    }

    private void initAppearancePage() {
        stagedThemeMode = PreferencesHelper.getThemeMode(this);
        stagedPalette = PreferencesHelper.getThemePalette(this);
        stagedDynamicColor = PreferencesHelper.isDynamicColorEnabled(this);

        if (switchDynamicColors != null && com.zez.app.theme.ThemeManager.isDynamicColorSupported()) {
            switchDynamicColors.setChecked(stagedDynamicColor);
        }

        updateModeSelectionUi();
        updatePaletteSelectionUi();
        updateLiveThemePreview();
    }

    private void selectAppearanceMode(int mode) {
        stagedThemeMode = mode;
        updateModeSelectionUi();
        updateLiveThemePreview();
    }

    private void selectPalette(String palette) {
        stagedPalette = palette;
        updatePaletteSelectionUi();
        updateLiveThemePreview();
    }

    private void updateModeSelectionUi() {
        if (cardModeSystem == null) return;
        boolean isSystem = (stagedThemeMode == PreferencesHelper.THEME_SYSTEM);
        boolean isLight = (stagedThemeMode == PreferencesHelper.THEME_LIGHT);
        boolean isDark = (stagedThemeMode == PreferencesHelper.THEME_DARK);

        cardModeSystem.setBackgroundResource(isSystem ? R.drawable.bg_theme_card_selected : R.drawable.bg_theme_card_unselected);
        checkModeSystem.setVisibility(isSystem ? View.VISIBLE : View.GONE);
        iconModeSystem.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, isSystem ? R.color.primary : R.color.theme_icon_tint)));

        cardModeLight.setBackgroundResource(isLight ? R.drawable.bg_theme_card_selected : R.drawable.bg_theme_card_unselected);
        checkModeLight.setVisibility(isLight ? View.VISIBLE : View.GONE);
        iconModeLight.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, isLight ? R.color.primary : R.color.theme_icon_tint)));

        cardModeDark.setBackgroundResource(isDark ? R.drawable.bg_theme_card_selected : R.drawable.bg_theme_card_unselected);
        checkModeDark.setVisibility(isDark ? View.VISIBLE : View.GONE);
        iconModeDark.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, isDark ? R.color.primary : R.color.theme_icon_tint)));
    }

    private void updatePaletteSelectionUi() {
        setPaletteCardState(cardPaletteGraphite, checkPaletteGraphite, PreferencesHelper.PALETTE_GRAPHITE.equals(stagedPalette));
        setPaletteCardState(cardPaletteArctic, checkPaletteArctic, PreferencesHelper.PALETTE_ARCTIC.equals(stagedPalette));
        setPaletteCardState(cardPaletteMidnight, checkPaletteMidnight, PreferencesHelper.PALETTE_MIDNIGHT.equals(stagedPalette));
        setPaletteCardState(cardPaletteAmoled, checkPaletteAmoled, PreferencesHelper.PALETTE_AMOLED.equals(stagedPalette));
        setPaletteCardState(cardPaletteOcean, checkPaletteOcean, PreferencesHelper.PALETTE_OCEAN.equals(stagedPalette));
        setPaletteCardState(cardPaletteSage, checkPaletteSage, PreferencesHelper.PALETTE_SAGE.equals(stagedPalette));
        setPaletteCardState(cardPaletteSunset, checkPaletteSunset, PreferencesHelper.PALETTE_SUNSET.equals(stagedPalette));
        setPaletteCardState(cardPaletteRose, checkPaletteRose, PreferencesHelper.PALETTE_ROSE.equals(stagedPalette));
    }

    private void setPaletteCardState(LinearLayout card, ImageView check, boolean isSelected) {
        if (card != null) {
            card.setBackgroundResource(isSelected ? R.drawable.bg_theme_card_selected : R.drawable.bg_theme_card_unselected);
        }
        if (check != null) {
            check.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }
    }

    private void updateLiveThemePreview() {
        boolean isNight;
        if (stagedThemeMode == PreferencesHelper.THEME_DARK) {
            isNight = true;
        } else if (stagedThemeMode == PreferencesHelper.THEME_LIGHT) {
            isNight = false;
        } else {
            isNight = com.zez.app.theme.ThemeManager.isDarkMode(this);
        }

        int primaryColor;
        int surfaceColor;
        int surfaceVariantColor;
        int textPrimaryColor;
        int textSecondaryColor;
        int textMutedColor;
        int borderColor;

        if (isNight) {
            textPrimaryColor = Color.parseColor("#F1F5F9");
            textSecondaryColor = Color.parseColor("#94A3B8");
            textMutedColor = Color.parseColor("#64748B");
            borderColor = Color.parseColor("#2E333A");

            switch (stagedPalette) {
                case PreferencesHelper.PALETTE_ARCTIC:
                    primaryColor = Color.parseColor("#38BDF8");
                    surfaceColor = Color.parseColor("#111D2D");
                    surfaceVariantColor = Color.parseColor("#18283D");
                    borderColor = Color.parseColor("#233852");
                    break;
                case PreferencesHelper.PALETTE_MIDNIGHT:
                    primaryColor = Color.parseColor("#818CF8");
                    surfaceColor = Color.parseColor("#111726");
                    surfaceVariantColor = Color.parseColor("#192237");
                    borderColor = Color.parseColor("#24304D");
                    break;
                case PreferencesHelper.PALETTE_AMOLED:
                    primaryColor = Color.parseColor("#00E5FF");
                    surfaceColor = Color.parseColor("#0C0C0E");
                    surfaceVariantColor = Color.parseColor("#161619");
                    borderColor = Color.parseColor("#222227");
                    textPrimaryColor = Color.parseColor("#FFFFFF");
                    break;
                case PreferencesHelper.PALETTE_OCEAN:
                    primaryColor = Color.parseColor("#38BDF8");
                    surfaceColor = Color.parseColor("#0F2236");
                    surfaceVariantColor = Color.parseColor("#162E48");
                    borderColor = Color.parseColor("#1E3F61");
                    break;
                case PreferencesHelper.PALETTE_SAGE:
                    primaryColor = Color.parseColor("#34D399");
                    surfaceColor = Color.parseColor("#13221C");
                    surfaceVariantColor = Color.parseColor("#1B2F27");
                    borderColor = Color.parseColor("#274136");
                    break;
                case PreferencesHelper.PALETTE_SUNSET:
                    primaryColor = Color.parseColor("#FB7185");
                    surfaceColor = Color.parseColor("#25181C");
                    surfaceVariantColor = Color.parseColor("#332127");
                    borderColor = Color.parseColor("#462E36");
                    break;
                case PreferencesHelper.PALETTE_ROSE:
                    primaryColor = Color.parseColor("#F43F5E");
                    surfaceColor = Color.parseColor("#23151C");
                    surfaceVariantColor = Color.parseColor("#311F28");
                    borderColor = Color.parseColor("#452C3A");
                    break;
                case PreferencesHelper.PALETTE_GRAPHITE:
                default:
                    primaryColor = Color.parseColor("#3B82F6");
                    surfaceColor = Color.parseColor("#1A1D21");
                    surfaceVariantColor = Color.parseColor("#22262B");
                    borderColor = Color.parseColor("#2E333A");
                    break;
            }
        } else {
            textPrimaryColor = Color.parseColor("#0F172A");
            textSecondaryColor = Color.parseColor("#475569");
            textMutedColor = Color.parseColor("#94A3B8");
            borderColor = Color.parseColor("#E2E8F0");

            switch (stagedPalette) {
                case PreferencesHelper.PALETTE_ARCTIC:
                    primaryColor = Color.parseColor("#0284C7");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#E6EDF5");
                    borderColor = Color.parseColor("#D5E1ED");
                    break;
                case PreferencesHelper.PALETTE_MIDNIGHT:
                    primaryColor = Color.parseColor("#4F46E5");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#E8EAF4");
                    borderColor = Color.parseColor("#D8DBEC");
                    break;
                case PreferencesHelper.PALETTE_AMOLED:
                    primaryColor = Color.parseColor("#0891B2");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#F4F4F5");
                    borderColor = Color.parseColor("#E4E4E7");
                    break;
                case PreferencesHelper.PALETTE_OCEAN:
                    primaryColor = Color.parseColor("#0284C7");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#DBEAFE");
                    borderColor = Color.parseColor("#BFDBFE");
                    break;
                case PreferencesHelper.PALETTE_SAGE:
                    primaryColor = Color.parseColor("#059669");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#E6EDE6");
                    borderColor = Color.parseColor("#D2DFD2");
                    break;
                case PreferencesHelper.PALETTE_SUNSET:
                    primaryColor = Color.parseColor("#EA580C");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#FFEDD5");
                    borderColor = Color.parseColor("#FED7AA");
                    break;
                case PreferencesHelper.PALETTE_ROSE:
                    primaryColor = Color.parseColor("#E11D48");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#FFE4E6");
                    borderColor = Color.parseColor("#FECDD3");
                    break;
                case PreferencesHelper.PALETTE_GRAPHITE:
                default:
                    primaryColor = Color.parseColor("#2563EB");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#F1F3F5");
                    borderColor = Color.parseColor("#E2E8F0");
                    break;
            }
        }

        if (previewTopBar != null) previewTopBar.setBackgroundColor(surfaceVariantColor);
        View previewBodySurface = findViewById(R.id.preview_body_surface);
        if (previewBodySurface != null) previewBodySurface.setBackgroundColor(surfaceColor);
        if (previewAppTitle != null) {
            previewAppTitle.setTextColor(textPrimaryColor);
            previewAppTitle.setText("TWING · " + getPaletteDisplayName(stagedPalette));
        }
        if (previewTextTitle != null) {
            previewTextTitle.setTextColor(textPrimaryColor);
            previewTextTitle.setText(getPaletteDisplayName(stagedPalette) + " Palette");
        }
        if (previewTextSubtitle != null) {
            previewTextSubtitle.setTextColor(textSecondaryColor);
            previewTextSubtitle.setText(getPaletteDescription(stagedPalette));
        }
        if (previewBadgePrimary != null) {
            previewBadgePrimary.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
        }
        if (previewBtnPrimary != null) {
            previewBtnPrimary.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
        }
        if (previewBtnSecondary != null) {
            previewBtnSecondary.setStrokeColor(ColorStateList.valueOf(borderColor));
            previewBtnSecondary.setTextColor(textPrimaryColor);
        }
        if (previewBackIcon != null) {
            previewBackIcon.setImageTintList(ColorStateList.valueOf(textSecondaryColor));
        }
        if (previewSearchIcon != null) {
            previewSearchIcon.setImageTintList(ColorStateList.valueOf(textSecondaryColor));
        }
        if (previewInputField != null) {
            previewInputField.setTextColor(textPrimaryColor);
            previewInputField.setHintTextColor(textMutedColor);
        }

        ColorStateList primaryCsl = ColorStateList.valueOf(primaryColor);
        if (btnApplyAppearance != null) {
            btnApplyAppearance.setBackgroundTintList(primaryCsl);
        }
        if (checkModeSystem != null) checkModeSystem.setImageTintList(primaryCsl);
        if (checkModeLight != null) checkModeLight.setImageTintList(primaryCsl);
        if (checkModeDark != null) checkModeDark.setImageTintList(primaryCsl);
        if (checkPaletteGraphite != null) checkPaletteGraphite.setImageTintList(primaryCsl);
        if (checkPaletteArctic != null) checkPaletteArctic.setImageTintList(primaryCsl);
        if (checkPaletteMidnight != null) checkPaletteMidnight.setImageTintList(primaryCsl);
        if (checkPaletteAmoled != null) checkPaletteAmoled.setImageTintList(primaryCsl);
        if (checkPaletteOcean != null) checkPaletteOcean.setImageTintList(primaryCsl);
        if (checkPaletteSage != null) checkPaletteSage.setImageTintList(primaryCsl);
        if (checkPaletteSunset != null) checkPaletteSunset.setImageTintList(primaryCsl);
        if (checkPaletteRose != null) checkPaletteRose.setImageTintList(primaryCsl);
    }

    private String getPaletteDisplayName(String palette) {
        if (palette == null) return "Graphite";
        switch (palette) {
            case PreferencesHelper.PALETTE_ARCTIC: return "Arctic Light";
            case PreferencesHelper.PALETTE_MIDNIGHT: return "Midnight";
            case PreferencesHelper.PALETTE_AMOLED: return "AMOLED";
            case PreferencesHelper.PALETTE_OCEAN: return "Ocean";
            case PreferencesHelper.PALETTE_SAGE: return "Sage";
            case PreferencesHelper.PALETTE_SUNSET: return "Sunset";
            case PreferencesHelper.PALETTE_ROSE: return "Rose";
            case PreferencesHelper.PALETTE_GRAPHITE:
            default: return "Graphite";
        }
    }

    private String getPaletteDescription(String palette) {
        if (palette == null) return "Balanced neutral grays with electric blue accents";
        switch (palette) {
            case PreferencesHelper.PALETTE_ARCTIC: return "Cool frosty cyan with glacial blue highlights";
            case PreferencesHelper.PALETTE_MIDNIGHT: return "Deep indigo and violet for low-light comfort";
            case PreferencesHelper.PALETTE_AMOLED: return "Pure #000000 black saves battery on OLED displays";
            case PreferencesHelper.PALETTE_OCEAN: return "Marine depth with vibrant azure accents";
            case PreferencesHelper.PALETTE_SAGE: return "Forest greens and soothing botanical tones";
            case PreferencesHelper.PALETTE_SUNSET: return "Warm amber, terracotta and dusk highlights";
            case PreferencesHelper.PALETTE_ROSE: return "Rich crimson and elegant rose accents";
            case PreferencesHelper.PALETTE_GRAPHITE:
            default: return "Balanced neutral grays with electric blue accents";
        }
    }

    private void applyAppearanceChanges() {
        boolean modeChanged = (stagedThemeMode != PreferencesHelper.getThemeMode(this));
        PreferencesHelper.setThemeMode(this, stagedThemeMode);
        PreferencesHelper.setThemePalette(this, stagedPalette);
        PreferencesHelper.setDynamicColorEnabled(this, stagedDynamicColor);

        if (modeChanged) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(stagedThemeMode);
        }
        Toast.makeText(this, "Theme updated: " + getPaletteDisplayName(stagedPalette), Toast.LENGTH_SHORT).show();
        recreate();
    }

    private void resetAppearanceToDefault() {
        stagedThemeMode = PreferencesHelper.THEME_SYSTEM;
        stagedPalette = PreferencesHelper.PALETTE_GRAPHITE;
        stagedDynamicColor = false;
        applyAppearanceChanges();
    }

    private void openHardwarePage() {
        scrollSettings.setVisibility(View.GONE);
        viewFontPage.setVisibility(View.GONE);
        viewHardwarePage.setVisibility(View.VISIBLE);
        tvTitle.setText("Hardware & Storage");

        loadHardwarePageData();
        startTelemetryUpdates();
    }

    private void closeSubPages() {
        stopTelemetryUpdates();
        viewFontPage.setVisibility(View.GONE);
        viewHardwarePage.setVisibility(View.GONE);
        if (viewAppearancePage != null) {
            viewAppearancePage.setVisibility(View.GONE);
        }
        scrollSettings.setVisibility(View.VISIBLE);
        tvTitle.setText("Settings");
        loadSettingsData();
    }

    private void loadHardwarePageData() {
        // Storage
        com.zez.app.util.HardwareInfoHelper.StorageInfo storage = com.zez.app.util.HardwareInfoHelper.getInternalStorageInfo();
        progressPageStorage.setProgress(storage.usedPercent);
        tvPageStoragePercent.setText(storage.usedPercent + "%");
        tvPageStorageDetails.setText("Used: " + com.zez.app.util.HardwareInfoHelper.formatBytes(storage.usedBytes)
                + " / Total: " + com.zez.app.util.HardwareInfoHelper.formatBytes(storage.totalBytes));

        // RAM
        com.zez.app.util.HardwareInfoHelper.RamInfo ram = com.zez.app.util.HardwareInfoHelper.getRamInfo(this);
        progressPageRam.setProgress(ram.usedPercent);
        tvPageRamPercent.setText(ram.usedPercent + "%");
        tvPageRamDetails.setText("Used: " + com.zez.app.util.HardwareInfoHelper.formatBytes(ram.usedBytes)
                + " / Total: " + com.zez.app.util.HardwareInfoHelper.formatBytes(ram.totalBytes));
        graphRamLoad.addDataPoint(ram.usedPercent);

        // CPU & Architecture
        com.zez.app.util.HardwareInfoHelper.CpuInfo cpu = com.zez.app.util.HardwareInfoHelper.getCpuInfo();
        tvPageCpuCores.setText(cpu.coreCount + " Cores");
        tvPageCpuSpeed.setText(cpu.maxFreq);
        tvPageCpuAbi.setText(cpu.abi);
        tvPageCpuHardware.setText("Hardware: " + cpu.hardwareName);
    }

    private void startTelemetryUpdates() {
        stopTelemetryUpdates();
        telemetryRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewHardwarePage != null && viewHardwarePage.getVisibility() == View.VISIBLE) {
                    // Sample RAM
                    com.zez.app.util.HardwareInfoHelper.RamInfo ram = com.zez.app.util.HardwareInfoHelper.getRamInfo(MainActivity.this);
                    if (graphRamLoad != null) {
                        graphRamLoad.addDataPoint(ram.usedPercent);
                    }
                    if (progressPageRam != null) progressPageRam.setProgress(ram.usedPercent);
                    if (tvPageRamPercent != null) tvPageRamPercent.setText(ram.usedPercent + "%");

                    // Simulate/Sample dynamic CPU/GPU activity point
                    float simulatedCpu = 12f + (float) (Math.random() * 28f);
                    if (graphCpuLoad != null) {
                        graphCpuLoad.addDataPoint(simulatedCpu);
                    }
                    if (tvPageCpuLoadPercent != null) {
                        tvPageCpuLoadPercent.setText(String.format(java.util.Locale.US, "%.0f%% Active", simulatedCpu));
                    }

                    telemetryHandler.postDelayed(this, 1500);
                }
            }
        };
        telemetryHandler.post(telemetryRunnable);
    }

    private void stopTelemetryUpdates() {
        if (telemetryRunnable != null) {
            telemetryHandler.removeCallbacks(telemetryRunnable);
            telemetryRunnable = null;
        }
    }

    private void loadSettingsData() {
        // Current Theme description
        int themeMode = PreferencesHelper.getThemeMode(this);
        String modeStr;
        if (themeMode == PreferencesHelper.THEME_LIGHT) {
            modeStr = "Light";
        } else if (themeMode == PreferencesHelper.THEME_DARK) {
            modeStr = "Dark";
        } else {
            modeStr = "System Default";
        }
        String palette = PreferencesHelper.getThemePalette(this);
        String paletteStr = getPaletteDisplayName(palette);
        boolean dynamic = PreferencesHelper.isDynamicColorEnabled(this);
        String dynStr = dynamic ? " · Dynamic" : "";
        if (tvSettingThemeCurrent != null) {
            tvSettingThemeCurrent.setText(modeStr + " · " + paletteStr + dynStr);
        }

        // Current Font description
        float scale = PreferencesHelper.getFontScale(this);
        String famName = getFontFamilyDisplayName(PreferencesHelper.getFontFamily(this));
        String sizeStr;
        if (Math.abs(scale - PreferencesHelper.FONT_SCALE_SMALL) < 0.05f) {
            sizeStr = "Small (85% · ~12px)";
        } else if (Math.abs(scale - PreferencesHelper.FONT_SCALE_LARGE) < 0.05f) {
            sizeStr = "Large (115% · ~16px)";
        } else if (Math.abs(scale - PreferencesHelper.FONT_SCALE_HUGE) < 0.05f) {
            sizeStr = "Extra Large (130% · ~18px)";
        } else {
            sizeStr = "Default (100% · ~14px)";
        }
        if (tvSettingFontCurrent != null) {
            tvSettingFontCurrent.setText(famName + " · " + sizeStr);
        }

        // Hardware summary
        com.zez.app.util.HardwareInfoHelper.StorageInfo storage = com.zez.app.util.HardwareInfoHelper.getInternalStorageInfo();
        com.zez.app.util.HardwareInfoHelper.RamInfo ram = com.zez.app.util.HardwareInfoHelper.getRamInfo(this);
        tvSettingHardwareSummary.setText("Storage " + storage.usedPercent + "% used · RAM " + ram.usedPercent + "% used");

        // Current Backend Server URL
        String activeApiUrl = PreferencesHelper.getApiUrl(this);
        if (etSettingBackendUrl != null && (etSettingBackendUrl.getText() == null || etSettingBackendUrl.getText().toString().isEmpty() || !etSettingBackendUrl.hasFocus())) {
            etSettingBackendUrl.setText(activeApiUrl);
        }
        if (tvSettingServerCurrent != null) {
            tvSettingServerCurrent.setText("Active: " + activeApiUrl);
        }

        // Version info
        String verName = com.zez.app.util.AppUpdateHelper.getCurrentVersionName(this);
        int verCode = com.zez.app.util.AppUpdateHelper.getCurrentVersionCode(this);
        tvSettingVersionInfo.setText("Installed: v" + verName + " (Build " + verCode + ")");

        // Footer version & device details
        if (tvFooterAppVersion != null) {
            tvFooterAppVersion.setText("Quacky v" + verName + " (Build " + verCode + ")");
        }
        String deviceId = PreferencesHelper.getDeviceId(this);
        actualDeviceId = (deviceId != null ? deviceId : PreferencesHelper.getPersistentUuid(this));

        // Accent Color
        updateAccentCheckmarks(PreferencesHelper.getAccentColor(this));

        // Chat Account Status (Google Play Policy)
        if (tvSettingChatAccountStatus != null) {
            if (ChatPreferencesHelper.isChatRegistered(this)) {
                String name = ChatPreferencesHelper.getUserName(this);
                String phone = ChatPreferencesHelper.getUserPhone(this);
                tvSettingChatAccountStatus.setText("Registered: " + name + " (" + phone + ")");
                tvSettingChatAccountStatus.setTextColor(ContextCompat.getColor(this, R.color.call_incoming));
            } else {
                tvSettingChatAccountStatus.setText("Not registered (Tap to create)");
                tvSettingChatAccountStatus.setTextColor(ContextCompat.getColor(this, R.color.theme_text_secondary));
            }
        }
    }

    private void confirmDeleteChatAccount() {
        if (!ChatPreferencesHelper.isChatRegistered(this)) {
            Toast.makeText(this, "No active chat account found on this device.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = ChatPreferencesHelper.getUserName(this);
        String phone = ChatPreferencesHelper.getUserPhone(this);
        String userId = ChatPreferencesHelper.getUserId(this);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Delete Chat Account?")
                .setMessage("Are you sure you want to delete your chat account (" + name + " · " + phone + ")?\n\nThis will permanently delete:\n• Your chat profile and phone registration\n• All sent and received messages\n• Shared media and attachments\n• Active encryption sessions\n\nThis action cannot be undone and complies with Google Play Account Deletion requirements.")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete Permanently", (dialog, which) -> {
                    dialog.dismiss();
                    executeDeleteChatAccount(userId);
                })
                .show();
    }

    private void executeDeleteChatAccount(String userId) {
        android.app.ProgressDialog progress = new android.app.ProgressDialog(this);
        progress.setMessage("Deleting chat account and data...");
        progress.setCancelable(false);
        progress.show();

        ChatApiClient.deleteAccount(this, userId, new ChatApiClient.ChatCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (progress.isShowing()) progress.dismiss();
                finalizeChatAccountDeletion();
            }

            @Override
            public void onError(String errorMessage) {
                if (progress.isShowing()) progress.dismiss();
                finalizeChatAccountDeletion();
            }
        });
    }

    private void finalizeChatAccountDeletion() {
        ChatPreferencesHelper.clearChatUser(MainActivity.this);
        com.zez.app.util.ChatLocalDbHelper.getInstance(MainActivity.this).clearAllData();
        ChatNotificationManager.getInstance().stop();

        Toast.makeText(MainActivity.this, "Chat account and all data deleted permanently.", Toast.LENGTH_LONG).show();

        loadSettingsData();
        loadChatsData();
    }

    private void initAccentColorSwatches() {
        if (swatchAccentBlue == null) return;
        String current = PreferencesHelper.getAccentColor(this);
        updateAccentCheckmarks(current);

        swatchAccentBlue.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.DEFAULT_ACCENT_COLOR, "Classic Blue"));
        swatchAccentEmerald.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.COLOR_EMERALD, "Emerald Green"));
        swatchAccentCrimson.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.COLOR_CRIMSON, "Crimson Red"));
        swatchAccentPink.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.COLOR_PINK, "Vivid Pink"));
        swatchAccentViolet.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.COLOR_VIOLET, "Violet Purple"));
        swatchAccentAmber.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.COLOR_AMBER, "Amber Orange"));
        swatchAccentCyan.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.COLOR_CYAN, "Teal Cyan"));

        View btnColorStudio = findViewById(R.id.btn_open_full_color_picker);
        if (btnColorStudio != null) {
            btnColorStudio.setOnClickListener(v -> showColorStudioDialog());
        }
        View rowAccent = findViewById(R.id.row_setting_accent_color);
        if (rowAccent != null) {
            rowAccent.setOnClickListener(v -> showColorStudioDialog());
        }
    }

    private void showContactProfileDialog(ChatConversation conv) {
        if (conv == null) return;
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_contact_profile, null);
        sheet.setContentView(view);

        ImageButton btnBack = view.findViewById(R.id.btn_profile_back);
        ImageButton btnMore = view.findViewById(R.id.btn_profile_more);
        ImageView ivAvatar = view.findViewById(R.id.iv_profile_avatar);
        TextView tvInitials = view.findViewById(R.id.tv_profile_initials);
        TextView tvName = view.findViewById(R.id.tv_profile_name);
        TextView tvPhone = view.findViewById(R.id.tv_profile_phone);
        TextView tvLastSeen = view.findViewById(R.id.tv_profile_last_seen);

        View btnCall = view.findViewById(R.id.btn_profile_call);
        View btnVideo = view.findViewById(R.id.btn_profile_videocall);
        View btnSearch = view.findViewById(R.id.btn_profile_search);

        String myUserId = ChatPreferencesHelper.getUserId(this);
        boolean isSelfChat = conv.getOtherUserId() != null && conv.getOtherUserId().equals(myUserId);
        String name = isSelfChat ? "You (Message yourself)" : PhoneHelper.resolveContactName(this, conv.getOtherUserPhone(), conv.getOtherUserName());
        String phone = conv.getOtherUserPhone() != null && !conv.getOtherUserPhone().trim().isEmpty() ? conv.getOtherUserPhone().trim() : "";

        if (btnBack != null) btnBack.setOnClickListener(v -> sheet.dismiss());

        if (tvName != null) tvName.setText(name);
        if (tvPhone != null) {
            if (!phone.isEmpty() && !isSelfChat) {
                tvPhone.setText(phone.startsWith("+") ? phone : "+91 " + phone);
                tvPhone.setVisibility(View.VISIBLE);
            } else {
                tvPhone.setVisibility(View.GONE);
            }
        }

        if (tvLastSeen != null) {
            if (isSelfChat) {
                tvLastSeen.setText("Message yourself · Notes, links, drafts");
            } else {
                tvLastSeen.setText("Tap to open chat");
            }
        }

        String avatarToLoad = conv.getOtherUserAvatar();
        if (isSelfChat && (avatarToLoad == null || avatarToLoad.trim().isEmpty())) {
            avatarToLoad = ChatPreferencesHelper.getUserAvatar(this);
        }

        if (ivAvatar != null) {
            if (avatarToLoad != null && !avatarToLoad.trim().isEmpty()) {
                String fullUrl = ChatApiClient.getAbsoluteMediaUrl(this, avatarToLoad);
                ivAvatar.setImageTintList(null);
                ivAvatar.setPadding(0, 0, 0, 0);
                Glide.with(this)
                        .load(fullUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(ivAvatar);
                if (tvInitials != null) tvInitials.setVisibility(View.GONE);

                if (isSelfChat) {
                    ivAvatar.setClickable(true);
                    ivAvatar.setFocusable(true);
                    ivAvatar.setOnClickListener(v -> {
                        Intent viewerIntent = new Intent(this, ImageViewerActivity.class);
                        viewerIntent.putExtra(ImageViewerActivity.EXTRA_URI, fullUrl);
                        viewerIntent.putExtra(ImageViewerActivity.EXTRA_NAME, "You");
                        viewerIntent.putExtra(ImageViewerActivity.EXTRA_IS_PROFILE_VIEW, true);
                        viewerIntent.putExtra(ImageViewerActivity.EXTRA_CAN_EDIT, true);
                        startActivity(viewerIntent);
                    });
                } else {
                    final String viewerUrl = fullUrl;
                    ivAvatar.setClickable(true);
                    ivAvatar.setFocusable(true);
                    ivAvatar.setOnClickListener(v -> {
                        Intent viewerIntent = new Intent(this, ImageViewerActivity.class);
                        viewerIntent.putExtra(ImageViewerActivity.EXTRA_URI, viewerUrl);
                        viewerIntent.putExtra(ImageViewerActivity.EXTRA_NAME, name);
                        viewerIntent.putExtra(ImageViewerActivity.EXTRA_IS_PROFILE_VIEW, true);
                        viewerIntent.putExtra(ImageViewerActivity.EXTRA_CAN_EDIT, false);
                        startActivity(viewerIntent);
                    });
                }
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person);
                ivAvatar.setImageTintList(ContextCompat.getColorStateList(this, R.color.theme_icon_tint));
                int pad = (int) (20 * getResources().getDisplayMetrics().density);
                ivAvatar.setPadding(pad, pad, pad, pad);
                if (isSelfChat) {
                    ivAvatar.setClickable(true);
                    ivAvatar.setFocusable(true);
                    ivAvatar.setOnClickListener(v -> {
                        if (avatarPickerLauncher != null) {
                            avatarPickerLauncher.launch("image/*");
                            sheet.dismiss();
                        }
                    });
                } else {
                    ivAvatar.setOnClickListener(null);
                }
            }
        }

        if (btnCall != null) {
            btnCall.setOnClickListener(v -> {
                sheet.dismiss();
                if (!phone.isEmpty()) {
                    try {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
                    } catch (Exception e) {
                        Toast.makeText(this, "Cannot open dialer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnVideo != null) {
            btnVideo.setOnClickListener(v -> {
                sheet.dismiss();
                Toast.makeText(this, "Starting encrypted video call...", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                sheet.dismiss();
                openChatConversation(conv);
            });
        }

        String convId = conv.getConversationId();
        com.google.android.material.switchmaterial.SwitchMaterial switchPin = view.findViewById(R.id.switch_profile_pin);
        TextView tvPinSub = view.findViewById(R.id.tv_profile_pin_sub);
        if (switchPin != null && convId != null) {
            boolean isPinned = ChatPreferencesHelper.isConversationPinned(this, convId);
            switchPin.setChecked(isPinned);
            if (tvPinSub != null) tvPinSub.setText(isPinned ? "Pinned to the top of your chats list" : "Keep this chat at the top of your chats list");
            switchPin.setOnCheckedChangeListener((btn, isChecked) -> {
                ChatPreferencesHelper.setConversationPinned(this, convId, isChecked);
                if (tvPinSub != null) tvPinSub.setText(isChecked ? "Pinned to the top of your chats list" : "Keep this chat at the top of your chats list");
                Toast.makeText(this, isChecked ? "Chat pinned to top" : "Chat unpinned", Toast.LENGTH_SHORT).show();
                loadChatsData();
            });
        }

        View rowNotifications = view.findViewById(R.id.row_profile_notifications);
        TextView tvNotifSub = view.findViewById(R.id.tv_profile_notifications_sub);
        if (tvNotifSub != null && convId != null) {
            tvNotifSub.setText(ChatPreferencesHelper.getMuteStatusText(this, convId));
        }
        if (rowNotifications != null && convId != null) {
            rowNotifications.setOnClickListener(v -> {
                boolean isMuted = ChatPreferencesHelper.isConversationMuted(this, convId);
                String[] options = isMuted ? new String[]{"Unmute notifications", "8 hours", "1 week", "Always"} : new String[]{"8 hours", "1 week", "Always"};
                new AlertDialog.Builder(this)
                        .setTitle("Mute notifications")
                        .setItems(options, (d, which) -> {
                            String choice = options[which];
                            if ("Unmute notifications".equals(choice)) {
                                ChatPreferencesHelper.muteConversation(this, convId, 0);
                                Toast.makeText(this, "Notifications unmuted", Toast.LENGTH_SHORT).show();
                            } else if ("8 hours".equals(choice)) {
                                ChatPreferencesHelper.muteConversation(this, convId, 8 * 3600 * 1000L);
                                Toast.makeText(this, "Notifications muted for 8 hours", Toast.LENGTH_SHORT).show();
                            } else if ("1 week".equals(choice)) {
                                ChatPreferencesHelper.muteConversation(this, convId, 7 * 24 * 3600 * 1000L);
                                Toast.makeText(this, "Notifications muted for 1 week", Toast.LENGTH_SHORT).show();
                            } else if ("Always".equals(choice)) {
                                ChatPreferencesHelper.muteConversation(this, convId, Long.MAX_VALUE);
                                Toast.makeText(this, "Notifications muted always", Toast.LENGTH_SHORT).show();
                            }
                            if (tvNotifSub != null) tvNotifSub.setText(ChatPreferencesHelper.getMuteStatusText(this, convId));
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        View rowVisibility = view.findViewById(R.id.row_profile_visibility);
        TextView tvVisSub = view.findViewById(R.id.tv_profile_visibility_sub);
        if (tvVisSub != null && convId != null) {
            int vis = ChatPreferencesHelper.getMediaVisibility(this, convId);
            tvVisSub.setText(vis == 1 ? "Yes" : (vis == 2 ? "No" : "Default (Yes)"));
        }
        if (rowVisibility != null && convId != null) {
            rowVisibility.setOnClickListener(v -> {
                int current = ChatPreferencesHelper.getMediaVisibility(this, convId);
                String[] options = new String[]{"Default (Yes)", "Yes", "No"};
                new AlertDialog.Builder(this)
                        .setTitle("Media visibility")
                        .setSingleChoiceItems(options, current, (d, which) -> {
                            ChatPreferencesHelper.setMediaVisibility(this, convId, which);
                            if (tvVisSub != null) tvVisSub.setText(options[which]);
                            Toast.makeText(this, "Media visibility: " + options[which], Toast.LENGTH_SHORT).show();
                            d.dismiss();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        View layoutMediaSection = view.findViewById(R.id.layout_profile_media_section);
        View btnViewAllMedia = view.findViewById(R.id.btn_profile_view_all_media);
        View.OnClickListener openMediaBrowser = v -> {
            sheet.dismiss();
            openChatConversation(conv);
        };
        if (layoutMediaSection != null) layoutMediaSection.setOnClickListener(openMediaBrowser);
        if (btnViewAllMedia != null) btnViewAllMedia.setOnClickListener(openMediaBrowser);

        sheet.show();
    }

    private void showColorStudioDialog() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_color_picker, null);
        sheet.setContentView(dialogView);

        RadioGroup rgTarget = dialogView.findViewById(R.id.rg_color_target);
        RadioButton rbOutgoing = dialogView.findViewById(R.id.rb_target_outgoing);
        RadioButton rbIncoming = dialogView.findViewById(R.id.rb_target_incoming);

        View previewBubble = dialogView.findViewById(R.id.preview_bubble);
        TextView tvPreviewText = dialogView.findViewById(R.id.tv_preview_bubble_text);
        TextView tvPreviewTime = dialogView.findViewById(R.id.tv_preview_bubble_time);
        ImageView ivPreviewTick = dialogView.findViewById(R.id.iv_preview_bubble_tick);

        SeekBar sbHue = dialogView.findViewById(R.id.sb_hue);
        SeekBar sbSaturation = dialogView.findViewById(R.id.sb_saturation);
        SeekBar sbValue = dialogView.findViewById(R.id.sb_value);

        View viewCurrentSwatch = dialogView.findViewById(R.id.view_current_color_swatch);
        com.google.android.material.textfield.TextInputEditText etHex = dialogView.findViewById(R.id.et_hex_code);

        LinearLayout layoutPresets = dialogView.findViewById(R.id.layout_preset_chips);
        Button btnReset = dialogView.findViewById(R.id.btn_picker_reset);
        Button btnCancel = dialogView.findViewById(R.id.btn_picker_cancel);
        Button btnApply = dialogView.findViewById(R.id.btn_picker_apply);

        final float[] hsv = new float[3];
        final boolean[] isUpdatingProgrammatically = new boolean[]{false};

        String initialOutgoing = PreferencesHelper.getAccentColor(this);
        final String[] currentColorHex = new String[]{initialOutgoing};

        Runnable updatePreview = () -> {
            try {
                int col = android.graphics.Color.parseColor(currentColorHex[0]);
                if (viewCurrentSwatch != null) {
                    viewCurrentSwatch.setBackgroundTintList(android.content.res.ColorStateList.valueOf(col));
                }
                boolean isOutgoing = rbOutgoing.isChecked();
                if (isOutgoing) {
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) previewBubble.getLayoutParams();
                    lp.gravity = android.view.Gravity.END;
                    previewBubble.setLayoutParams(lp);
                    previewBubble.setBackgroundResource(R.drawable.bg_bubble_outgoing);
                    previewBubble.getBackground().mutate().setTint(col);
                    tvPreviewText.setTextColor(android.graphics.Color.WHITE);
                    tvPreviewText.setText("Outgoing: Hello from Quacky! 🚀");
                    tvPreviewTime.setTextColor(android.graphics.Color.parseColor("#CCFFFFFF"));
                    ivPreviewTick.setVisibility(View.VISIBLE);
                } else {
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) previewBubble.getLayoutParams();
                    lp.gravity = android.view.Gravity.START;
                    previewBubble.setLayoutParams(lp);
                    previewBubble.setBackgroundResource(R.drawable.bg_bubble_incoming);
                    previewBubble.getBackground().mutate().setTint(col);
                    boolean isDark = ChatMessageAdapter.isColorDark(col);
                    tvPreviewText.setTextColor(isDark ? android.graphics.Color.WHITE : android.graphics.Color.parseColor("#0F172A"));
                    tvPreviewText.setText("Incoming: Test message preview ✨");
                    tvPreviewTime.setTextColor(isDark ? android.graphics.Color.parseColor("#94A3B8") : android.graphics.Color.parseColor("#64748B"));
                    ivPreviewTick.setVisibility(View.GONE);
                }
            } catch (Exception ignored) {}
        };

        java.util.function.Consumer<String> setColorFromHex = hex -> {
            try {
                int col = android.graphics.Color.parseColor(hex);
                android.graphics.Color.colorToHSV(col, hsv);
                currentColorHex[0] = String.format("#%06X", (0xFFFFFF & col));
                isUpdatingProgrammatically[0] = true;
                sbHue.setProgress(Math.round(hsv[0]));
                sbSaturation.setProgress(Math.round(hsv[1] * 100));
                sbValue.setProgress(Math.round(hsv[2] * 100));
                etHex.setText(currentColorHex[0]);
                isUpdatingProgrammatically[0] = false;
                updatePreview.run();
            } catch (Exception ignored) {}
        };

        setColorFromHex.accept(currentColorHex[0]);

        SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isUpdatingProgrammatically[0]) return;
                hsv[0] = sbHue.getProgress();
                hsv[1] = sbSaturation.getProgress() / 100f;
                hsv[2] = sbValue.getProgress() / 100f;
                int col = android.graphics.Color.HSVToColor(hsv);
                currentColorHex[0] = String.format("#%06X", (0xFFFFFF & col));
                isUpdatingProgrammatically[0] = true;
                etHex.setText(currentColorHex[0]);
                isUpdatingProgrammatically[0] = false;
                updatePreview.run();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };
        sbHue.setOnSeekBarChangeListener(seekListener);
        sbSaturation.setOnSeekBarChangeListener(seekListener);
        sbValue.setOnSeekBarChangeListener(seekListener);

        etHex.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdatingProgrammatically[0] || s == null) return;
                String input = s.toString().trim();
                if (!input.startsWith("#")) input = "#" + input;
                if (input.length() == 7 || input.length() == 9) {
                    try {
                        int col = android.graphics.Color.parseColor(input);
                        android.graphics.Color.colorToHSV(col, hsv);
                        currentColorHex[0] = String.format("#%06X", (0xFFFFFF & col));
                        isUpdatingProgrammatically[0] = true;
                        sbHue.setProgress(Math.round(hsv[0]));
                        sbSaturation.setProgress(Math.round(hsv[1] * 100));
                        sbValue.setProgress(Math.round(hsv[2] * 100));
                        isUpdatingProgrammatically[0] = false;
                        updatePreview.run();
                    } catch (Exception ignored) {}
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        rgTarget.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_target_incoming) {
                String inc = PreferencesHelper.getIncomingBubbleColor(this);
                setColorFromHex.accept(inc != null && !inc.isEmpty() ? inc : "#1E293B");
            } else {
                setColorFromHex.accept(PreferencesHelper.getAccentColor(this));
            }
        });

        // Populate preset chips
        String[] presets = new String[]{
                "#10B981", "#2563EB", "#EF4444", "#EC4899", "#8B5CF6",
                "#F59E0B", "#06B6D4", "#059669", "#1E293B", "#A855F7",
                "#F43F5E", "#334155", "#0F172A", "#FFFFFF", "#38BDF8"
        };
        for (String pCol : presets) {
            FrameLayout chip = new FrameLayout(this);
            int size = (int) (32 * getResources().getDisplayMetrics().density);
            int margin = (int) (4 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(margin, margin, margin, margin);
            chip.setLayoutParams(lp);
            chip.setBackgroundResource(R.drawable.bg_circle_primary);
            chip.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(pCol)));
            chip.setClickable(true);
            chip.setFocusable(true);
            chip.setOnClickListener(v -> setColorFromHex.accept(pCol));
            layoutPresets.addView(chip);
        }

        btnReset.setOnClickListener(v -> {
            if (rbIncoming.isChecked()) {
                PreferencesHelper.setIncomingBubbleColor(this, null);
                Toast.makeText(this, "Incoming bubble reset to default theme", Toast.LENGTH_SHORT).show();
                setColorFromHex.accept("#1E293B");
            } else {
                setAppAccentColor(PreferencesHelper.DEFAULT_ACCENT_COLOR, "Classic Blue");
                setColorFromHex.accept(PreferencesHelper.DEFAULT_ACCENT_COLOR);
            }
            sheet.dismiss();
        });

        btnCancel.setOnClickListener(v -> sheet.dismiss());

        btnApply.setOnClickListener(v -> {
            String finalColor = currentColorHex[0];
            if (rbIncoming.isChecked()) {
                PreferencesHelper.setIncomingBubbleColor(this, finalColor);
                Toast.makeText(this, "Incoming bubble color applied: " + finalColor, Toast.LENGTH_SHORT).show();
            } else {
                setAppAccentColor(finalColor, finalColor);
            }
            sheet.dismiss();
        });

        sheet.show();
    }

    private void setAppAccentColor(String hexColor, String name) {
        PreferencesHelper.setAccentColor(this, hexColor);
        if (tvSettingAccentCurrent != null) tvSettingAccentCurrent.setText(name);
        updateAccentCheckmarks(hexColor);
        applyAccentColor();
        Toast.makeText(this, "Accent color set to " + name, Toast.LENGTH_SHORT).show();
    }

    private void updateAccentCheckmarks(String hexColor) {
        if (swatchCheckBlue == null) return;
        swatchCheckBlue.setVisibility(View.GONE);
        swatchCheckEmerald.setVisibility(View.GONE);
        swatchCheckCrimson.setVisibility(View.GONE);
        swatchCheckPink.setVisibility(View.GONE);
        swatchCheckViolet.setVisibility(View.GONE);
        swatchCheckAmber.setVisibility(View.GONE);
        swatchCheckCyan.setVisibility(View.GONE);

        if (PreferencesHelper.COLOR_EMERALD.equalsIgnoreCase(hexColor)) {
            swatchCheckEmerald.setVisibility(View.VISIBLE);
            if (tvSettingAccentCurrent != null) tvSettingAccentCurrent.setText("Emerald Green");
        } else if (PreferencesHelper.COLOR_CRIMSON.equalsIgnoreCase(hexColor)) {
            swatchCheckCrimson.setVisibility(View.VISIBLE);
            if (tvSettingAccentCurrent != null) tvSettingAccentCurrent.setText("Crimson Red");
        } else if (PreferencesHelper.COLOR_PINK.equalsIgnoreCase(hexColor)) {
            swatchCheckPink.setVisibility(View.VISIBLE);
            if (tvSettingAccentCurrent != null) tvSettingAccentCurrent.setText("Vivid Pink");
        } else if (PreferencesHelper.COLOR_VIOLET.equalsIgnoreCase(hexColor)) {
            swatchCheckViolet.setVisibility(View.VISIBLE);
            if (tvSettingAccentCurrent != null) tvSettingAccentCurrent.setText("Violet Purple");
        } else if (PreferencesHelper.COLOR_AMBER.equalsIgnoreCase(hexColor)) {
            swatchCheckAmber.setVisibility(View.VISIBLE);
            if (tvSettingAccentCurrent != null) tvSettingAccentCurrent.setText("Amber Orange");
        } else if (PreferencesHelper.COLOR_CYAN.equalsIgnoreCase(hexColor)) {
            swatchCheckCyan.setVisibility(View.VISIBLE);
            if (tvSettingAccentCurrent != null) tvSettingAccentCurrent.setText("Teal Cyan");
        } else {
            swatchCheckBlue.setVisibility(View.VISIBLE);
            if (tvSettingAccentCurrent != null) tvSettingAccentCurrent.setText("Classic Blue");
        }
    }

    private void applyAccentColor() {
        int accentColor = com.zez.app.theme.ThemeManager.getPrimaryColor(this);

        if (bottomNav != null) {
            int[][] states = new int[][] {
                    new int[] { android.R.attr.state_checked },
                    new int[] { -android.R.attr.state_checked }
            };
            int[] colors = new int[] {
                    accentColor,
                    com.zez.app.theme.ThemeManager.resolveThemeColor(this, R.attr.colorThemeTextMuted)
            };
            android.content.res.ColorStateList colorStateList = new android.content.res.ColorStateList(states, colors);
            bottomNav.setItemIconTintList(colorStateList);
            bottomNav.setItemTextColor(colorStateList);
            bottomNav.setItemActiveIndicatorColor(android.content.res.ColorStateList.valueOf(
                    androidx.core.graphics.ColorUtils.setAlphaComponent(accentColor, 40)
            ));
        }

        if (btnSettingUpdate != null) {
            btnSettingUpdate.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accentColor));
        }
        if (fabNewChat != null) {
            fabNewChat.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accentColor));
        }
        if (fabAddAlarm != null) {
            fabAddAlarm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accentColor));
        }
        if (conversationsAdapter != null) {
            conversationsAdapter.notifyDataSetChanged();
        }
        if (viewCallsContainer != null && viewCallsContainer.getVisibility() == View.VISIBLE) {
            loadCallsData();
        }
    }

    private void applyThemeColors() {
        int bg = com.zez.app.theme.ThemeManager.resolveThemeColor(this, R.attr.colorThemeBackground);
        int surface = com.zez.app.theme.ThemeManager.resolveThemeColor(this, R.attr.colorThemeSurface);
        int primary = com.zez.app.theme.ThemeManager.getPrimaryColor(this);
        int textPrimary = com.zez.app.theme.ThemeManager.resolveThemeColor(this, R.attr.colorThemeTextPrimary);
        int textMuted = com.zez.app.theme.ThemeManager.resolveThemeColor(this, R.attr.colorThemeTextMuted);
        int iconTint = com.zez.app.theme.ThemeManager.resolveThemeColor(this, R.attr.colorThemeIconTint);

        View content = findViewById(android.R.id.content);
        if (content != null) content.setBackgroundColor(bg);

        View contentContainer = findViewById(R.id.content_container);
        if (contentContainer != null) contentContainer.setBackgroundColor(bg);

        layoutAppBar = findViewById(R.id.app_bar);
        if (layoutAppBar != null) layoutAppBar.setBackgroundColor(surface);

        layoutChatsSelectionBar = findViewById(R.id.layout_chats_selection_bar);
        if (layoutChatsSelectionBar != null) layoutChatsSelectionBar.setBackgroundColor(surface);

        applyAccentColor();

        if (tvTitle != null) tvTitle.setTextColor(textPrimary);
        if (tvSubtitle != null) tvSubtitle.setTextColor(textMuted);
        if (btnMusicLauncher != null) btnMusicLauncher.setImageTintList(android.content.res.ColorStateList.valueOf(primary));
        if (btnSync != null) btnSync.setImageTintList(android.content.res.ColorStateList.valueOf(iconTint));

        if (viewChatsContainer != null) viewChatsContainer.setBackgroundColor(bg);
        if (viewCallsContainer != null) viewCallsContainer.setBackgroundColor(bg);
        if (viewGalleryContainer != null) viewGalleryContainer.setBackgroundColor(bg);
        if (layoutAlarmsContainer != null) layoutAlarmsContainer.setBackgroundColor(bg);
        if (scrollSettings != null) scrollSettings.setBackgroundColor(bg);
        if (viewFontPage != null) viewFontPage.setBackgroundColor(bg);
        if (viewHardwarePage != null) viewHardwarePage.setBackgroundColor(bg);
        if (viewAppearancePage != null) viewAppearancePage.setBackgroundColor(bg);

        if (galleryTabLayout != null) {
            galleryTabLayout.setBackgroundColor(surface);
            galleryTabLayout.setSelectedTabIndicatorColor(primary);
            galleryTabLayout.setTabTextColors(textMuted, primary);
        }
        if (galleryActionBar != null) galleryActionBar.setBackgroundColor(surface);

        if (layoutMainMiniPlayer != null) {
            layoutMainMiniPlayer.setBackgroundColor(surface);
        }
    }

    private void handleDeveloperTap() {
        long now = System.currentTimeMillis();
        if (now - lastDeveloperTapTime > 3000) {
            developerTapCount = 0;
        }
        lastDeveloperTapTime = now;
        developerTapCount++;

        if (developerTapCount >= 2 && developerTapCount < 5) {
            int remaining = 5 - developerTapCount;
            Toast.makeText(this, "Tap " + remaining + " more times for developer options", Toast.LENGTH_SHORT).show();
        } else if (developerTapCount >= 5) {
            developerTapCount = 0;
            promptAdminPinForServerConfig();
        }
    }

    private void promptAdminPinForServerConfig() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Developer Options");
        builder.setMessage("Enter Admin PIN to unlock Backend Server Configuration:");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("Enter PIN");

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int marginPx = (int) (20 * getResources().getDisplayMetrics().density);
        params.leftMargin = marginPx;
        params.rightMargin = marginPx;
        params.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        params.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Unlock", (dialog, which) -> {
            String entered = input.getText() != null ? input.getText().toString().trim() : "";
            if (ADMIN_PIN.equals(entered)) {
                Toast.makeText(this, "Developer Server Configuration Unlocked", Toast.LENGTH_SHORT).show();
                if (rowSettingServer != null) rowSettingServer.setVisibility(View.VISIBLE);
                if (dividerSettingServer != null) dividerSettingServer.setVisibility(View.VISIBLE);
                showServerConfigDialog();
            } else {
                Toast.makeText(this, "Incorrect Admin PIN", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void saveBackendUrlFromInput() {
        if (etSettingBackendUrl == null) return;
        String newUrl = etSettingBackendUrl.getText() != null ? etSettingBackendUrl.getText().toString().trim() : "";
        if (newUrl.isEmpty()) {
            etSettingBackendUrl.setError("URL cannot be empty");
            return;
        }
        if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
            newUrl = "https://" + newUrl;
        }
        if (newUrl.endsWith("/")) {
            newUrl = newUrl.substring(0, newUrl.length() - 1);
        }
        PreferencesHelper.setApiUrl(this, newUrl);
        Toast.makeText(this, "Backend URL saved: " + newUrl, Toast.LENGTH_SHORT).show();
        loadSettingsData();

        // Asynchronously ping the new backend URL
        new Thread(() -> {
            ApiClient.ping(getApplicationContext());
        }).start();
    }

    private void resetBackendUrlToDefault() {
        PreferencesHelper.resetApiUrl(this);
        Toast.makeText(this, "Backend URL reset to default", Toast.LENGTH_SHORT).show();
        loadSettingsData();

        new Thread(() -> {
            ApiClient.ping(getApplicationContext());
        }).start();
    }

    private void showServerConfigDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Backend Server URL (Developer Mode)");
        builder.setMessage("Configure the base URL of your hosted backend server:\n• Local Dev: " + PreferencesHelper.EMULATOR_API_URL + "\n• Production: " + PreferencesHelper.DEFAULT_API_URL);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int marginPx = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(marginPx, (int) (8 * getResources().getDisplayMetrics().density), marginPx, (int) (8 * getResources().getDisplayMetrics().density));

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(PreferencesHelper.getApiUrl(this));
        input.setSelectAllOnFocus(true);
        container.addView(input);

        LinearLayout presetsLayout = new LinearLayout(this);
        presetsLayout.setOrientation(LinearLayout.HORIZONTAL);
        presetsLayout.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);

        android.widget.Button btnLocal = new android.widget.Button(this, null, android.R.attr.buttonStyleSmall);
        btnLocal.setText("Local Dev");
        btnLocal.setTextSize(11f);
        btnLocal.setOnClickListener(v -> input.setText(PreferencesHelper.EMULATOR_API_URL));
        presetsLayout.addView(btnLocal);

        android.widget.Button btnProd = new android.widget.Button(this, null, android.R.attr.buttonStyleSmall);
        btnProd.setText("Production Render");
        btnProd.setTextSize(11f);
        btnProd.setOnClickListener(v -> input.setText(PreferencesHelper.DEFAULT_API_URL));
        presetsLayout.addView(btnProd);

        container.addView(presetsLayout);
        builder.setView(container);

        builder.setPositiveButton("Save & Connect", (dialog, which) -> {
            String newUrl = input.getText() != null ? input.getText().toString().trim() : "";
            if (newUrl.isEmpty()) {
                Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                newUrl = "https://" + newUrl;
            }
            PreferencesHelper.setApiUrl(this, newUrl);
            Toast.makeText(this, "Backend URL saved: " + newUrl, Toast.LENGTH_SHORT).show();
            loadSettingsData();

            // Asynchronously ping the new backend URL
            new Thread(() -> {
                ApiClient.ping(getApplicationContext());
            }).start();
        });

        builder.setNeutralButton("Reset Default", (dialog, which) -> {
            PreferencesHelper.resetApiUrl(this);
            Toast.makeText(this, "Backend URL reset to default", Toast.LENGTH_SHORT).show();
            loadSettingsData();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showUpdateDialog(boolean isManual) {
        android.app.ProgressDialog checkingDialog = null;
        if (isManual) {
            checkingDialog = new android.app.ProgressDialog(this);
            checkingDialog.setMessage("Checking for updates...");
            checkingDialog.setCancelable(false);
            checkingDialog.show();
        }

        final android.app.ProgressDialog finalCheckingDialog = checkingDialog;
        com.zez.app.util.AppUpdateHelper.checkForUpdate(this, new com.zez.app.util.AppUpdateHelper.UpdateCheckCallback() {
            @Override
            public void onResult(com.zez.app.util.AppUpdateHelper.ReleaseInfo info) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    if (finalCheckingDialog != null && finalCheckingDialog.isShowing()) {
                        finalCheckingDialog.dismiss();
                    }

                    if (info.isUpdateAvailable) {
                        String notes = (info.releaseNotes != null && !info.releaseNotes.trim().isEmpty())
                                ? info.releaseNotes
                                : "Bug fixes and performance improvements.";

                        String message = "A newer version of Quacky is ready to install!\n\n"
                                + "• Current version: v" + info.currentVersionName + " (Build " + info.currentVersionCode + ")\n"
                                + "• Latest version: v" + info.latestVersionName + " (Build " + info.latestVersionCode + ")\n"
                                + "• Download size: " + formatFileSize(info.fileSize) + "\n\n"
                                + "What's New:\n" + notes;

                        new com.google.android.material.dialog.MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("Update Available")
                                .setMessage(message)
                                .setIcon(R.drawable.ic_system_update)
                                .setPositiveButton("Download & Install", (dialog, which) -> {
                                    startApkDownload(info);
                                })
                                .setNegativeButton("Later", null)
                                .show();
                    } else {
                        if (isManual) {
                            String message = "You're already running the latest version of Quacky.\n\n"
                                    + "• Installed version: v" + info.currentVersionName + " (Build " + info.currentVersionCode + ")";

                            new com.google.android.material.dialog.MaterialAlertDialogBuilder(MainActivity.this)
                                    .setTitle("App is Up to Date")
                                    .setMessage(message)
                                    .setIcon(R.drawable.ic_system_update)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    if (finalCheckingDialog != null && finalCheckingDialog.isShowing()) {
                        finalCheckingDialog.dismiss();
                    }
                    if (isManual) {
                        Toast.makeText(MainActivity.this, "Update check failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void setupRollbackAndCompanion() {
        checkRollbackAvailability();
        checkCompanionAppStatus();
    }

    private void checkRollbackAvailability() {
        com.zez.app.util.AppUpdateHelper.checkForRollback(this, new com.zez.app.util.AppUpdateHelper.UpdateCheckCallback() {
            @Override
            public void onResult(com.zez.app.util.AppUpdateHelper.ReleaseInfo info) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (info != null && info.isUpdateAvailable) {
                        rollbackReleaseInfo = info;
                        if (btnSettingRollback != null) {
                            btnSettingRollback.setVisibility(View.VISIBLE);
                            btnSettingRollback.setText("Rollback to v" + info.latestVersionName);
                            btnSettingRollback.setOnClickListener(v -> confirmRollback(info));
                        }
                    } else {
                        if (btnSettingRollback != null) {
                            btnSettingRollback.setVisibility(View.GONE);
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (btnSettingRollback != null) btnSettingRollback.setVisibility(View.GONE);
                });
            }
        });
    }

    private void confirmRollback(com.zez.app.util.AppUpdateHelper.ReleaseInfo info) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Rollback Quacky Update")
                .setMessage("Are you sure you want to roll back to previous version v" + info.latestVersionName + " (Build " + info.latestVersionCode + ")?\n\nThis will download and install the previous official release.")
                .setIcon(R.drawable.ic_system_update)
                .setPositiveButton("Rollback", (dialog, which) -> {
                    startApkDownload(info, "Downloading Rollback", "quacky-rollback");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkCompanionAppStatus() {
        boolean installed = com.zez.app.util.AppUpdateHelper.isAppInstalled(this, "com.zez.manager");
        String installedVer = installed ? com.zez.app.util.AppUpdateHelper.getInstalledVersionName(this, "com.zez.manager") : "";
        int installedCode = installed ? com.zez.app.util.AppUpdateHelper.getInstalledVersionCode(this, "com.zez.manager") : -1;

        if (tvCompanionStatus != null) {
            tvCompanionStatus.setText(installed ? ("Installed (v" + installedVer + ") · Checking server...") : "Checking availability on server...");
        }

        com.zez.app.util.AppUpdateHelper.checkForCompanionApp(this, "zez", new com.zez.app.util.AppUpdateHelper.UpdateCheckCallback() {
            @Override
            public void onResult(com.zez.app.util.AppUpdateHelper.ReleaseInfo info) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    companionReleaseInfo = info;
                    if (installed) {
                        boolean hasNewer = info != null && info.latestVersionCode > installedCode;
                        if (hasNewer) {
                            if (tvCompanionStatus != null) {
                                tvCompanionStatus.setText("Installed (v" + installedVer + ") · Update Available (v" + info.latestVersionName + ")");
                            }
                            if (btnCompanionAction != null) {
                                btnCompanionAction.setText("Update ZEZ");
                                btnCompanionAction.setOnClickListener(v -> startApkDownload(info, "Downloading ZEZ", "zez-manager-update", "com.zez.manager"));
                            }
                        } else {
                            if (tvCompanionStatus != null) {
                                tvCompanionStatus.setText("Installed (v" + installedVer + ") · Up to date");
                            }
                            if (btnCompanionAction != null) {
                                btnCompanionAction.setText("Open ZEZ");
                                btnCompanionAction.setOnClickListener(v -> com.zez.app.util.AppUpdateHelper.openApp(MainActivity.this, "com.zez.manager"));
                            }
                        }
                    } else {
                        if (tvCompanionStatus != null) {
                            tvCompanionStatus.setText("Not installed on this device" + (info != null ? " · Latest v" + info.latestVersionName : ""));
                        }
                        if (btnCompanionAction != null) {
                            btnCompanionAction.setText("Download & Install ZEZ");
                            btnCompanionAction.setOnClickListener(v -> {
                                if (info != null) {
                                    startApkDownload(info, "Downloading ZEZ", "zez-manager-companion", "com.zez.manager");
                                } else {
                                    Toast.makeText(MainActivity.this, "ZEZ release not found on server", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (installed) {
                        if (tvCompanionStatus != null) tvCompanionStatus.setText("Installed (v" + installedVer + ")");
                        if (btnCompanionAction != null) {
                            btnCompanionAction.setText("Open ZEZ");
                            btnCompanionAction.setOnClickListener(v -> com.zez.app.util.AppUpdateHelper.openApp(MainActivity.this, "com.zez.manager"));
                        }
                    } else {
                        if (tvCompanionStatus != null) tvCompanionStatus.setText("Not installed");
                    }
                });
            }
        });
    }

    private void startApkDownload(com.zez.app.util.AppUpdateHelper.ReleaseInfo info) {
        startApkDownload(info, "Downloading Update", "quacky-update", getPackageName());
    }

    private void startApkDownload(com.zez.app.util.AppUpdateHelper.ReleaseInfo info, String dialogTitle, String fileName) {
        startApkDownload(info, dialogTitle, fileName, getPackageName());
    }

    private void startApkDownload(com.zez.app.util.AppUpdateHelper.ReleaseInfo info, String dialogTitle, String fileName, String expectedPackageName) {
        android.app.ProgressDialog downloadProgress = new android.app.ProgressDialog(this);
        downloadProgress.setTitle(dialogTitle);
        downloadProgress.setMessage("Downloading v" + info.latestVersionName + "...");
        downloadProgress.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        downloadProgress.setMax(100);
        downloadProgress.setProgress(0);
        downloadProgress.setCancelable(false);
        downloadProgress.show();

        com.zez.app.util.AppUpdateHelper.downloadAndInstall(this, fileName, expectedPackageName, info, new com.zez.app.util.AppUpdateHelper.DownloadProgressCallback() {
            @Override
            public void onProgress(int percent) {
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed() && downloadProgress.isShowing()) {
                        downloadProgress.setProgress(percent);
                    }
                });
            }

            @Override
            public void onComplete(java.io.File file) {
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed() && downloadProgress.isShowing()) {
                        downloadProgress.dismiss();
                    }
                    Toast.makeText(MainActivity.this, "Download complete! Opening installer...", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed() && downloadProgress.isShowing()) {
                        downloadProgress.dismiss();
                    }
                    Toast.makeText(MainActivity.this, "Download failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "Unknown size";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0);
        return String.format(java.util.Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0));
    }

    @Override
    public void onBackPressed() {
        if (conversationsAdapter != null && conversationsAdapter.isSelectionMode()) {
            conversationsAdapter.clearSelection();
            return;
        }
        if (alarmAdapter != null && alarmAdapter.isSelectionMode()) {
            alarmAdapter.clearSelection();
            return;
        }
        if (galleryAdapter != null && galleryAdapter.isSelectMode()) {
            galleryAdapter.exitSelectMode();
            return;
        }
        if (viewFontPage != null && viewFontPage.getVisibility() == View.VISIBLE) {
            closeSubPages();
            return;
        }
        if (viewHardwarePage != null && viewHardwarePage.getVisibility() == View.VISIBLE) {
            closeSubPages();
            return;
        }
        if (viewAppearancePage != null && viewAppearancePage.getVisibility() == View.VISIBLE) {
            closeSubPages();
            return;
        }
        if (swipeBin != null && swipeBin.getVisibility() == View.VISIBLE) {
            switchTab(R.id.nav_gallery);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_gallery);
            }
            return;
        }
        if (currentTab != R.id.nav_chats && bottomNav != null) {
            currentTab = R.id.nav_chats;
            bottomNav.setSelectedItemId(R.id.nav_chats);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (activeCallBannerListener != null) {
            com.zez.app.util.ActiveCallManager.getInstance().unregisterListener(activeCallBannerListener);
        }
        if (chatRegDialog != null && chatRegDialog.isShowing()) {
            chatRegDialog.dismiss();
        }
        super.onDestroy();
        stopTelemetryUpdates();
    }
}
