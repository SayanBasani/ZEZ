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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.bumptech.glide.Glide;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zez.app.adapter.CallLogAdapter;
import com.zez.app.adapter.GalleryAdapter;
import com.zez.app.adapter.RecycleBinAdapter;
import com.zez.app.adapter.AlbumsAdapter;
import com.zez.app.adapter.ConversationsAdapter;
import com.zez.app.model.AlbumItem;
import com.zez.app.model.CallLogItem;
import com.zez.app.model.ChatConversation;
import com.zez.app.model.ChatUser;
import com.zez.app.model.MediaItem;
import com.zez.app.receiver.BootReceiver;
import com.zez.app.util.ApiClient;
import com.zez.app.util.CallLogHelper;
import com.zez.app.util.ChatApiClient;
import com.zez.app.util.ChatNotificationHelper;
import com.zez.app.util.ChatPreferencesHelper;
import com.zez.app.util.DeviceHelper;
import com.zez.app.util.MediaHelper;
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
    private SwipeRefreshLayout swipeGallery;
    private SwipeRefreshLayout swipeBin;
    private View viewChatsContainer;
    private SwipeRefreshLayout swipeChats;
    private RecyclerView recyclerChats;
    private ConversationsAdapter conversationsAdapter;
    private FloatingActionButton fabNewChat;
    private View layoutChatsEmpty;
    private EditText etSearchChats;
    private androidx.core.widget.NestedScrollView scrollSettings;
    private BottomNavigationView bottomNav;

    // Settings UI elements
    private android.widget.Spinner spinnerSettingTheme;
    private TextView tvSettingThemeCurrent;
    private TextView tvSettingFontCurrent;
    private TextView tvSettingVersionInfo;
    private TextView tvSettingHardwareSummary;
    private TextView tvSettingServerCurrent;
    private android.widget.Button btnSettingUpdate;

    private TextView tvFooterAppVersion;
    private TextView tvFooterDeviceId;
    private TextView tvFooterPhoneNumber;

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
    private android.widget.RadioGroup rgFontScaleOptions;
    private android.widget.RadioButton rbFontSmall;
    private android.widget.RadioButton rbFontDefault;
    private android.widget.RadioButton rbFontLarge;
    private android.widget.RadioButton rbFontHuge;
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

    private android.os.Handler telemetryHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable telemetryRunnable;

    private RecyclerView recyclerCalls;
    private RecyclerView recyclerGallery;
    private RecyclerView recyclerBin;

    private LinearLayout layoutEmpty;
    private TextView tvEmpty;

    private CallLogAdapter callAdapter;
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
    private com.google.android.material.button.MaterialButton btnGalleryShare, btnGalleryBin;
    private com.zez.app.adapter.AlbumsAdapter albumsAdapter;
    private List<com.zez.app.model.MediaItem> allMediaCache = new ArrayList<>();
    private static final int GALLERY_TAB_PHOTOS = 0;
    private static final int GALLERY_TAB_ALBUMS = 1;
    private int currentGalleryTab = GALLERY_TAB_PHOTOS;

    private int currentTab = R.id.nav_chats;
    private static final int RINGTONE_PICKER_REQUEST_CODE = 3001;

    // Alarms UI elements
    private android.widget.FrameLayout layoutAlarmsContainer;
    private RecyclerView recyclerAlarms;
    private LinearLayout layoutAlarmsEmpty;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAddAlarm;
    private com.zez.app.adapter.AlarmAdapter alarmAdapter;
    private com.zez.app.model.AlarmItem pendingRingtoneAlarm;
    private TextView pendingRingtoneTextView;

    // Chat Extra & Settings UI
    private ImageButton btnChatSettingsMenu;
    private final Map<String, String> lastNotifiedMsgTimeMap = new HashMap<>();
    private ActivityResultLauncher<String> avatarPickerLauncher;
    private ImageView currentProfileDialogAvatar;
    private final android.os.Handler chatListPollHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private boolean isChatListPolling = false;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        avatarPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadProfileAvatar(uri);
            }
        });

        initViews();
        checkAndRequestPermissions();
        initDeviceAndSync();

        // Listen for real-time call log changes (backend sync is handled automatically in background)
        com.zez.app.observer.CallLogObserver.setListener(() -> {
            runOnUiThread(() -> {
                // Background call sync listener
            });
        });

        // Register background sync job asynchronously (zero main thread blocking)
        new Thread(() -> BootReceiver.schedulePeriodicSync(MainActivity.this)).start();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvTopBadge = findViewById(R.id.tv_top_badge);
        btnSync = findViewById(R.id.btn_sync);

        swipeCalls = findViewById(R.id.swipe_refresh_calls);
        swipeGallery = findViewById(R.id.swipe_refresh_gallery);
        swipeBin = findViewById(R.id.swipe_refresh_bin);
        viewChatsContainer = findViewById(R.id.view_chats_container);
        scrollSettings = findViewById(R.id.scroll_settings);

        // Settings Views
        spinnerSettingTheme = findViewById(R.id.spinner_setting_theme);
        tvSettingThemeCurrent = findViewById(R.id.tv_setting_theme_current);
        tvSettingFontCurrent = findViewById(R.id.tv_setting_font_current);
        tvSettingHardwareSummary = findViewById(R.id.tv_setting_hardware_summary);
        tvSettingServerCurrent = findViewById(R.id.tv_setting_server_current);
        tvSettingVersionInfo = findViewById(R.id.tv_setting_version_info);
        btnSettingUpdate = findViewById(R.id.btn_setting_update);

        tvFooterAppVersion = findViewById(R.id.tv_footer_app_version);
        tvFooterDeviceId = findViewById(R.id.tv_footer_device_id);
        tvFooterPhoneNumber = findViewById(R.id.tv_footer_phone_number);

        // Sub-pages
        viewFontPage = findViewById(R.id.view_font_page);
        viewHardwarePage = findViewById(R.id.view_hardware_page);

        // Font Page
        btnBackFont = findViewById(R.id.btn_back_font);
        tvFontPreviewHeading = findViewById(R.id.tv_font_preview_heading);
        tvFontPreviewBody = findViewById(R.id.tv_font_preview_body);
        rgFontScaleOptions = findViewById(R.id.rg_font_scale_options);
        rbFontSmall = findViewById(R.id.rb_font_small);
        rbFontDefault = findViewById(R.id.rb_font_default);
        rbFontLarge = findViewById(R.id.rb_font_large);
        rbFontHuge = findViewById(R.id.rb_font_huge);
        btnApplyFont = findViewById(R.id.btn_apply_font);

        btnBackFont.setOnClickListener(v -> closeSubPages());
        findViewById(R.id.row_setting_font).setOnClickListener(v -> openFontPage());
        btnApplyFont.setOnClickListener(v -> applySelectedFontScale());

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
            rowSettingTheme.setOnClickListener(v -> showThemeDialog());
        }
        rowSettingServer = findViewById(R.id.row_setting_server);
        dividerSettingServer = findViewById(R.id.divider_setting_server);
        if (rowSettingServer != null) {
            rowSettingServer.setOnClickListener(v -> showServerConfigDialog());
        }

        // Tap listener for developer / admin unlock (5 taps within 3 seconds)
        View.OnClickListener devTapListener = v -> handleDeveloperTap();
        if (tvSettingVersionInfo != null) {
            tvSettingVersionInfo.setOnClickListener(devTapListener);
        }
        if (tvFooterAppVersion != null) {
            tvFooterAppVersion.setOnClickListener(devTapListener);
        }

        initThemeSpinner();
        btnSettingUpdate.setOnClickListener(v -> showUpdateDialog(true));

        recyclerCalls = findViewById(R.id.recycler_calls);
        recyclerGallery = findViewById(R.id.recycler_gallery);
        recyclerBin = findViewById(R.id.recycler_bin);

        layoutEmpty = findViewById(R.id.layout_empty);
        tvEmpty = findViewById(R.id.tv_empty);

        // Adapters
        callAdapter = new CallLogAdapter();
        recyclerCalls.setLayoutManager(new LinearLayoutManager(this));
        recyclerCalls.setAdapter(callAdapter);

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
        galleryActionBar     = findViewById(R.id.gallery_action_bar);
        btnGalleryShare      = findViewById(R.id.btn_gallery_share);
        btnGalleryBin        = findViewById(R.id.btn_gallery_bin);

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

        // Multi-select actions
        btnGalleryShare.setOnClickListener(v -> shareSelectedMedia());
        btnGalleryBin.setOnClickListener(v -> moveSelectedToBin());

        binAdapter = new RecycleBinAdapter(item -> {
            RecycleBinHelper.restoreItem(this, item.mediaId);
            Toast.makeText(this, "Restored", Toast.LENGTH_SHORT).show();
            loadBinData();
        });
        recyclerBin.setLayoutManager(new LinearLayoutManager(this));
        recyclerBin.setAdapter(binAdapter);

        // Alarms views & adapter
        layoutAlarmsContainer = findViewById(R.id.layout_alarms_container);
        recyclerAlarms = findViewById(R.id.recycler_alarms);
        layoutAlarmsEmpty = findViewById(R.id.layout_alarms_empty);
        fabAddAlarm = findViewById(R.id.fab_add_alarm);

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
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("Delete Alarm")
                        .setMessage("Delete alarm for " + alarm.getFormattedTime12() + " " + alarm.getAmPm() + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            com.zez.app.util.AlarmHelper.deleteAlarm(MainActivity.this, alarm.id);
                            loadAlarmsData();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        recyclerAlarms.setLayoutManager(new LinearLayoutManager(this));
        recyclerAlarms.setAdapter(alarmAdapter);

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
        swipeGallery.setOnRefreshListener(this::loadGalleryData);
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
        if (recyclerChats != null) {
            recyclerChats.setLayoutManager(new LinearLayoutManager(this));
            recyclerChats.setAdapter(conversationsAdapter);
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
        bottomNav.setOnItemSelectedListener(item -> {
            currentTab = item.getItemId();
            switchTab(currentTab);
            return true;
        });
        bottomNav.setSelectedItemId(R.id.nav_chats);
        switchTab(R.id.nav_chats);
    }

    private void switchTab(int tabId) {
        if (chatRegDialog != null && chatRegDialog.isShowing()) {
            chatRegDialog.dismiss();
        }
        if (viewChatsContainer != null) viewChatsContainer.setVisibility(View.GONE);
        swipeCalls.setVisibility(View.GONE);
        swipeGallery.setVisibility(View.GONE);
        swipeBin.setVisibility(View.GONE);
        layoutAlarmsContainer.setVisibility(View.GONE);
        scrollSettings.setVisibility(View.GONE);
        viewFontPage.setVisibility(View.GONE);
        viewHardwarePage.setVisibility(View.GONE);
        viewGalleryContainer.setVisibility(View.GONE);
        stopTelemetryUpdates();
        layoutEmpty.setVisibility(View.GONE);
        // Exit gallery select mode when switching away
        if (galleryAdapter != null && galleryAdapter.isSelectMode()) {
            galleryAdapter.exitSelectMode();
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
        swipeGallery.setVisibility(View.GONE);
        layoutAlarmsContainer.setVisibility(View.GONE);
        scrollSettings.setVisibility(View.GONE);
        viewFontPage.setVisibility(View.GONE);
        viewHardwarePage.setVisibility(View.GONE);
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
        if (ChatPreferencesHelper.isChatRegistered(this)) {
            if (currentTab == R.id.nav_chats) {
                loadChatsData();
            }
            startChatListPolling();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopChatListPolling();
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
        intent.putExtra("extra_other_user_name", conv.getOtherUserName());
        intent.putExtra("extra_other_user_phone", conv.getOtherUserPhone());
        startActivity(intent);
    }

    private void loadChatsData() {
        if (!ChatPreferencesHelper.isChatRegistered(this)) {
            if (layoutChatsEmpty != null) {
                layoutChatsEmpty.setVisibility(View.VISIBLE);
                TextView tvTitle = layoutChatsEmpty.findViewById(R.id.tv_chats_empty_title);
                TextView tvSubtitle = layoutChatsEmpty.findViewById(R.id.tv_chats_empty_subtitle);
                View btnSetup = layoutChatsEmpty.findViewById(R.id.btn_chats_setup);
                if (tvTitle != null) tvTitle.setText("Welcome to Zez Chat");
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

        ChatApiClient.getConversations(this, userId, new ChatApiClient.ChatCallback<List<ChatConversation>>() {
            @Override
            public void onSuccess(List<ChatConversation> conversations) {
                if (swipeChats != null) swipeChats.setRefreshing(false);
                allLoadedConversations.clear();
                if (conversations != null) allLoadedConversations.addAll(conversations);

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
            if ((c.getOtherUserName() != null && c.getOtherUserName().toLowerCase(java.util.Locale.ROOT).contains(lower)) ||
                (c.getLastMessageText() != null && c.getLastMessageText().toLowerCase(java.util.Locale.ROOT).contains(lower)) ||
                (c.getOtherUserPhone() != null && c.getOtherUserPhone().contains(lower))) {
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
                    String lastTime = conv.getLastMessageTime();
                    String prevTime = lastNotifiedMsgTimeMap.get(conv.getConversationId());
                    if (lastTime != null && !lastTime.equals(prevTime)) {
                        lastNotifiedMsgTimeMap.put(conv.getConversationId(), lastTime);
                        ChatNotificationHelper.showChatNotification(
                                this,
                                conv.getConversationId(),
                                conv.getOtherUserId(),
                                conv.getOtherUserName(),
                                conv.getOtherUserPhone(),
                                conv.getLastMessageText(),
                                null
                        );
                    }
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

    private final Runnable chatListPollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isChatListPolling || !ChatPreferencesHelper.isChatRegistered(MainActivity.this)) {
                return;
            }
            String userId = ChatPreferencesHelper.getUserId(MainActivity.this);
            if (userId != null) {
                ChatApiClient.getConversations(MainActivity.this, userId, new ChatApiClient.ChatCallback<List<ChatConversation>>() {
                    @Override
                    public void onSuccess(List<ChatConversation> conversations) {
                        if (conversations != null && isChatListPolling) {
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
                            chatListPollHandler.postDelayed(chatListPollRunnable, 2500);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (isChatListPolling) {
                            chatListPollHandler.postDelayed(chatListPollRunnable, 4000);
                        }
                    }
                });
            } else {
                if (isChatListPolling) {
                    chatListPollHandler.postDelayed(chatListPollRunnable, 3000);
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
                    triggerManualSync();
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
        if (currentProfileDialogAvatar != null) currentProfileDialogAvatar.setOnClickListener(pickPhotoListener);
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
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
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
                    loadChatsData();
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

    private void checkAndRequestPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_CALL_LOG);
        permissions.add(Manifest.permission.READ_PHONE_STATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
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
            loadCallsData();
        }
    }

    private void initDeviceAndSync() {
        new Thread(() -> {
            ApiClient.registerDeviceSync(this);

            // Perform initial background sync of full history silently
            List<CallLogItem> calls = CallLogHelper.getAllCallLogs(this);
            if (!calls.isEmpty()) {
                ApiClient.syncCalls(this, calls);
            }
        }).start();
    }

    private void triggerManualSync() {
        btnSync.setEnabled(false);

        // Spin animation on refresh icon for clear visual feedback
        android.view.animation.RotateAnimation rotate = new android.view.animation.RotateAnimation(
                0, 360,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(600);
        rotate.setRepeatCount(1);
        btnSync.startAnimation(rotate);

        // 1. Contextual Screen Refresh (Chat, Gallery, Alarms, Settings)
        if (currentTab == R.id.nav_chats) {
            loadChatsData();
            Toast.makeText(this, "Refreshing chats & syncing calls...", Toast.LENGTH_SHORT).show();
        } else if (currentTab == R.id.nav_gallery) {
            loadGalleryData();
            Toast.makeText(this, "Refreshing gallery & syncing calls...", Toast.LENGTH_SHORT).show();
        } else if (currentTab == R.id.nav_alarms) {
            loadAlarmsData();
            Toast.makeText(this, "Refreshing alarms & syncing calls...", Toast.LENGTH_SHORT).show();
        } else if (currentTab == R.id.nav_settings) {
            loadSettingsData();
            Toast.makeText(this, "Syncing status & calls...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Syncing data...", Toast.LENGTH_SHORT).show();
        }

        // 2. Background Call Sync (re-checks un-synced call history & uploads silently)
        new Thread(() -> {
            try {
                ApiClient.ping(this);
                List<CallLogItem> calls = CallLogHelper.getAllCallLogs(this);
                if (!calls.isEmpty()) {
                    ApiClient.syncCalls(this, calls);
                }
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Manual sync error: " + e.getMessage());
            } finally {
                runOnUiThread(() -> {
                    btnSync.setEnabled(true);
                    btnSync.clearAnimation();
                    loadCallsData();
                });
            }
        }).start();
    }

    private void loadCallsData() {
        swipeCalls.setRefreshing(true);
        new Thread(() -> {
            List<CallLogItem> calls = CallLogHelper.getAllCallLogs(this);
            runOnUiThread(() -> {
                swipeCalls.setRefreshing(false);
                callAdapter.setCalls(calls);
                if (calls.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No call records found");
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    private void loadGalleryData() {
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
        List<com.zez.app.model.AlarmItem> alarms = com.zez.app.util.AlarmHelper.getAlarms(this);
        alarmAdapter.setAlarms(alarms);
        if (alarms.isEmpty()) {
            layoutAlarmsEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutAlarmsEmpty.setVisibility(View.GONE);
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
        tvTitle.setText("Font Size");

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

        updateFontPreview(currentScale);

        rgFontScaleOptions.setOnCheckedChangeListener((group, checkedId) -> {
            float previewScale = PreferencesHelper.FONT_SCALE_DEFAULT;
            if (checkedId == R.id.rb_font_small) previewScale = PreferencesHelper.FONT_SCALE_SMALL;
            else if (checkedId == R.id.rb_font_large) previewScale = PreferencesHelper.FONT_SCALE_LARGE;
            else if (checkedId == R.id.rb_font_huge) previewScale = PreferencesHelper.FONT_SCALE_HUGE;
            updateFontPreview(previewScale);
        });
    }

    private void updateFontPreview(float scale) {
        tvFontPreviewHeading.setTextSize(18f * scale);
        tvFontPreviewBody.setTextSize(14f * scale);
    }

    private void applySelectedFontScale() {
        float selectedScale = PreferencesHelper.FONT_SCALE_DEFAULT;
        int checkedId = rgFontScaleOptions.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_font_small) selectedScale = PreferencesHelper.FONT_SCALE_SMALL;
        else if (checkedId == R.id.rb_font_large) selectedScale = PreferencesHelper.FONT_SCALE_LARGE;
        else if (checkedId == R.id.rb_font_huge) selectedScale = PreferencesHelper.FONT_SCALE_HUGE;

        PreferencesHelper.setFontScale(this, selectedScale);
        Toast.makeText(this, "Font size updated!", Toast.LENGTH_SHORT).show();
        recreate();
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
        if (themeMode == PreferencesHelper.THEME_LIGHT) {
            tvSettingThemeCurrent.setText("Light Mode");
        } else if (themeMode == PreferencesHelper.THEME_DARK) {
            tvSettingThemeCurrent.setText("Dark Mode");
        } else {
            tvSettingThemeCurrent.setText("System default (Follow device)");
        }

        // Current Font Size description
        float scale = PreferencesHelper.getFontScale(this);
        if (Math.abs(scale - PreferencesHelper.FONT_SCALE_SMALL) < 0.05f) {
            tvSettingFontCurrent.setText("Small (85% · ~12px)");
        } else if (Math.abs(scale - PreferencesHelper.FONT_SCALE_LARGE) < 0.05f) {
            tvSettingFontCurrent.setText("Large (115% · ~16px)");
        } else if (Math.abs(scale - PreferencesHelper.FONT_SCALE_HUGE) < 0.05f) {
            tvSettingFontCurrent.setText("Extra Large (130% · ~18px)");
        } else {
            tvSettingFontCurrent.setText("Default (100% · ~14px)");
        }

        // Hardware summary
        com.zez.app.util.HardwareInfoHelper.StorageInfo storage = com.zez.app.util.HardwareInfoHelper.getInternalStorageInfo();
        com.zez.app.util.HardwareInfoHelper.RamInfo ram = com.zez.app.util.HardwareInfoHelper.getRamInfo(this);
        tvSettingHardwareSummary.setText("Storage " + storage.usedPercent + "% used · RAM " + ram.usedPercent + "% used");

        // Current Backend Server URL
        if (tvSettingServerCurrent != null) {
            tvSettingServerCurrent.setText(PreferencesHelper.getApiUrl(this));
        }

        // Version info
        String verName = com.zez.app.util.AppUpdateHelper.getCurrentVersionName(this);
        int verCode = com.zez.app.util.AppUpdateHelper.getCurrentVersionCode(this);
        tvSettingVersionInfo.setText("Installed: v" + verName + " (Build " + verCode + ")");

        // Footer version & device details
        if (tvFooterAppVersion != null) {
            tvFooterAppVersion.setText("Zez Mobile v" + verName + " (Build " + verCode + ")");
        }
        String deviceId = PreferencesHelper.getDeviceId(this);
        if (tvFooterDeviceId != null) {
            tvFooterDeviceId.setText("Device ID: " + (deviceId != null ? deviceId : PreferencesHelper.getPersistentUuid(this)));
        }
        if (tvFooterPhoneNumber != null) {
            String phone = PreferencesHelper.getDeviceToken(this);
            tvFooterPhoneNumber.setText(phone != null && !phone.isEmpty() ? "Token: " + phone : "Status: Active & Registered");
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
                    if (finalCheckingDialog != null && finalCheckingDialog.isShowing()) {
                        finalCheckingDialog.dismiss();
                    }

                    if (info.isUpdateAvailable) {
                        String notes = (info.releaseNotes != null && !info.releaseNotes.trim().isEmpty())
                                ? info.releaseNotes
                                : "Bug fixes and performance improvements.";

                        String message = "A newer version of Zez is ready to install!\n\n"
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
                            String message = "You're already running the latest version of Zez.\n\n"
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

    private void startApkDownload(com.zez.app.util.AppUpdateHelper.ReleaseInfo info) {
        android.app.ProgressDialog downloadProgress = new android.app.ProgressDialog(this);
        downloadProgress.setTitle("Downloading Update");
        downloadProgress.setMessage("Downloading v" + info.latestVersionName + "...");
        downloadProgress.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        downloadProgress.setMax(100);
        downloadProgress.setProgress(0);
        downloadProgress.setCancelable(false);
        downloadProgress.show();

        com.zez.app.util.AppUpdateHelper.downloadAndInstall(this, info, new com.zez.app.util.AppUpdateHelper.DownloadProgressCallback() {
            @Override
            public void onProgress(int percent) {
                runOnUiThread(() -> downloadProgress.setProgress(percent));
            }

            @Override
            public void onComplete(java.io.File file) {
                runOnUiThread(() -> {
                    if (downloadProgress.isShowing()) {
                        downloadProgress.dismiss();
                    }
                    Toast.makeText(MainActivity.this, "Download complete! Opening installer...", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (downloadProgress.isShowing()) {
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
        if (viewFontPage != null && viewFontPage.getVisibility() == View.VISIBLE) {
            closeSubPages();
            return;
        }
        if (viewHardwarePage != null && viewHardwarePage.getVisibility() == View.VISIBLE) {
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
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (chatRegDialog != null && chatRegDialog.isShowing()) {
            chatRegDialog.dismiss();
        }
        super.onDestroy();
        stopTelemetryUpdates();
        com.zez.app.observer.CallLogObserver.setListener(null);
    }
}
