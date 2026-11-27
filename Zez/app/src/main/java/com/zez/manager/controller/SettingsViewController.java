package com.zez.manager.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zez.manager.R;
import com.zez.manager.util.ApiClient;
import com.zez.manager.service.NotificationCaptureService;
import com.zez.manager.theme.ThemeManager;
import com.zez.manager.util.AppUpdateHelper;
import com.zez.manager.util.HardwareInfoHelper;
import com.zez.manager.util.PreferencesHelper;
import com.zez.manager.util.TelemetryGraphView;

import java.io.File;
import java.util.Locale;

public class SettingsViewController {

    private static final String ADMIN_PIN = "7788";

    private final Activity activity;

    // Main Settings Views
    private View scrollSettings;
    private View rowSettingTheme;
    private TextView tvSettingThemeCurrent;
    private View rowSettingAccentColor;
    private TextView tvSettingAccentCurrent;
    private View btnOpenFullColorPicker;
    private View rowSettingFont;
    private TextView tvSettingFontCurrent;
    private View rowSettingHardware;
    private TextView tvSettingHardwareSummary;
    private View rowSettingNotifications;
    private TextView tvSettingNotificationSummary;
    private TextView tvSettingNotificationStatus;
    private View rowSettingServer;
    private View dividerSettingServer;
    private TextView tvSettingServerCurrent;
    private TextView tvSettingVersionInfo;
    private Button btnSettingUpdate;
    private Button btnSettingRollback;
    private TextView tvCompanionStatus;
    private Button btnCompanionAction;
    private AppUpdateHelper.ReleaseInfo rollbackReleaseInfo = null;
    private AppUpdateHelper.ReleaseInfo companionReleaseInfo = null;
    private TextView tvFooterAppVersion;

    // Accent Swatches & Checks
    private FrameLayout swatchAccentBlue, swatchAccentEmerald, swatchAccentCrimson, swatchAccentPink, swatchAccentViolet, swatchAccentAmber, swatchAccentCyan;
    private ImageView swatchCheckBlue, swatchCheckEmerald, swatchCheckCrimson, swatchCheckPink, swatchCheckViolet, swatchCheckAmber, swatchCheckCyan;

    // Sub-Pages
    private View viewAppearancePage;
    private View viewFontPage;
    private View viewHardwarePage;

    // Appearance Sub-Page Views
    private ImageButton btnBackAppearance;
    private ImageButton btnResetAppearanceTop;
    private Button btnResetAppearanceFull;
    private Button btnApplyAppearance;
    private LinearLayout previewContainer;
    private LinearLayout previewTopBar;
    private TextView previewAppTitle;
    private TextView previewTextTitle;
    private TextView previewTextSubtitle;
    private TextView previewBadgePrimary;
    private TextView previewBadgeStatus;
    private TextView previewBadgeTag;
    private EditText previewInputField;
    private Button previewBtnPrimary;
    private Button previewBtnSecondary;
    private ImageView previewBackIcon;
    private ImageView previewSearchIcon;

    private LinearLayout cardModeSystem, cardModeLight, cardModeDark;
    private ImageView checkModeSystem, checkModeLight, checkModeDark;
    private ImageView iconModeSystem, iconModeLight, iconModeDark;

    private LinearLayout cardPaletteGraphite, cardPaletteArctic, cardPaletteMidnight, cardPaletteAmoled;
    private LinearLayout cardPaletteOcean, cardPaletteSage, cardPaletteSunset, cardPaletteRose;
    private ImageView checkPaletteGraphite, checkPaletteArctic, checkPaletteMidnight, checkPaletteAmoled;
    private ImageView checkPaletteOcean, checkPaletteSage, checkPaletteSunset, checkPaletteRose;

    private SwitchCompat switchDynamicColors;
    private TextView tvDynamicColorsDesc;

    private int stagedThemeMode;
    private String stagedPalette;
    private boolean stagedDynamicColor;

    // Font Sub-Page Views
    private ImageButton btnBackFont;
    private EditText etCustomFontSize;
    private Button btnResetFontDefault;
    private RadioGroup rgFontScaleOptions;
    private RadioButton rbFontSmall, rbFontDefault, rbFontLarge, rbFontHuge;
    private RadioGroup rgFontFamilyOptions;
    private RadioButton rbFamilySans, rbFamilyLight, rbFamilyCondensed, rbFamilyMedium, rbFamilyBlack;
    private RadioButton rbFamilySerif, rbFamilyMonospace, rbFamilyCasual, rbFamilyCursive;
    private TextView tvFontPreviewHeading, tvFontPreviewBody;
    private Button btnApplyFont;

    private String stagedFontFamily;
    private float stagedFontScale;

    // Hardware Sub-Page Views
    private ImageButton btnBackHardware;
    private TextView tvPageStoragePercent;
    private ProgressBar progressPageStorage;
    private TextView tvPageStorageDetails;
    private TextView tvPageRamPercent;
    private ProgressBar progressPageRam;
    private TextView tvPageRamDetails;
    private TelemetryGraphView graphRamLoad;
    private TextView tvPageCpuLoadPercent;
    private TelemetryGraphView graphCpuLoad;
    private TextView tvPageCpuCores, tvPageCpuSpeed, tvPageCpuAbi, tvPageCpuHardware;
    private TextView tvHardwareDiagnosticHint;
    private View cardHardwareServerConfig;
    private EditText etSettingBackendUrl;
    private Button btnSettingSaveUrl, btnSettingResetUrl;

    // Live Telemetry Ticker
    private final Handler telemetryHandler = new Handler(Looper.getMainLooper());
    private Runnable telemetryRunnable;

    // Developer 5-tap state
    private int developerTapCount = 0;
    private long lastDeveloperTapTime = 0;

    public SettingsViewController(Activity activity) {
        this.activity = activity;
    }

    public void bindViews(View root) {
        scrollSettings = root.findViewById(R.id.scroll_settings);
        rowSettingTheme = root.findViewById(R.id.row_setting_theme);
        tvSettingThemeCurrent = root.findViewById(R.id.tv_setting_theme_current);
        rowSettingAccentColor = root.findViewById(R.id.row_setting_accent_color);
        tvSettingAccentCurrent = root.findViewById(R.id.tv_setting_accent_current);
        btnOpenFullColorPicker = root.findViewById(R.id.btn_open_full_color_picker);
        rowSettingFont = root.findViewById(R.id.row_setting_font);
        tvSettingFontCurrent = root.findViewById(R.id.tv_setting_font_current);
        rowSettingHardware = root.findViewById(R.id.row_setting_hardware);
        tvSettingHardwareSummary = root.findViewById(R.id.tv_setting_hardware_summary);
        rowSettingNotifications = root.findViewById(R.id.row_setting_notifications);
        tvSettingNotificationSummary = root.findViewById(R.id.tv_setting_notification_summary);
        tvSettingNotificationStatus = root.findViewById(R.id.tv_setting_notification_status);
        rowSettingServer = root.findViewById(R.id.row_setting_server);
        dividerSettingServer = root.findViewById(R.id.divider_setting_server);
        tvSettingServerCurrent = root.findViewById(R.id.tv_setting_server_current);
        tvSettingVersionInfo = root.findViewById(R.id.tv_setting_version_info);
        btnSettingUpdate = root.findViewById(R.id.btn_setting_update);
        btnSettingRollback = root.findViewById(R.id.btn_setting_rollback);
        tvCompanionStatus = root.findViewById(R.id.tv_companion_status);
        btnCompanionAction = root.findViewById(R.id.btn_companion_action);
        tvFooterAppVersion = root.findViewById(R.id.tv_footer_app_version);

        // Accent Swatches
        swatchAccentBlue = root.findViewById(R.id.swatch_accent_blue);
        swatchAccentEmerald = root.findViewById(R.id.swatch_accent_emerald);
        swatchAccentCrimson = root.findViewById(R.id.swatch_accent_crimson);
        swatchAccentPink = root.findViewById(R.id.swatch_accent_pink);
        swatchAccentViolet = root.findViewById(R.id.swatch_accent_violet);
        swatchAccentAmber = root.findViewById(R.id.swatch_accent_amber);
        swatchAccentCyan = root.findViewById(R.id.swatch_accent_cyan);

        swatchCheckBlue = root.findViewById(R.id.swatch_check_blue);
        swatchCheckEmerald = root.findViewById(R.id.swatch_check_emerald);
        swatchCheckCrimson = root.findViewById(R.id.swatch_check_crimson);
        swatchCheckPink = root.findViewById(R.id.swatch_check_pink);
        swatchCheckViolet = root.findViewById(R.id.swatch_check_violet);
        swatchCheckAmber = root.findViewById(R.id.swatch_check_amber);
        swatchCheckCyan = root.findViewById(R.id.swatch_check_cyan);

        // Sub-pages
        viewAppearancePage = root.findViewById(R.id.view_appearance_page);
        viewFontPage = root.findViewById(R.id.view_font_page);
        viewHardwarePage = root.findViewById(R.id.view_hardware_page);

        setupMainSettingsListeners();
        initAppearanceViews(root);
        initFontViews(root);
        initHardwareViews(root);
        loadSettingsData();
    }

    private void setupMainSettingsListeners() {
        if (rowSettingTheme != null) rowSettingTheme.setOnClickListener(v -> openAppearancePage());
        if (rowSettingAccentColor != null) rowSettingAccentColor.setOnClickListener(v -> openAppearancePage());
        if (btnOpenFullColorPicker != null) btnOpenFullColorPicker.setOnClickListener(v -> showColorPickerDialog());

        if (swatchAccentBlue != null) swatchAccentBlue.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.DEFAULT_ACCENT_COLOR, "Classic Blue"));
        if (swatchAccentEmerald != null) swatchAccentEmerald.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.COLOR_EMERALD, "Emerald Green"));
        if (swatchAccentCrimson != null) swatchAccentCrimson.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.COLOR_CRIMSON, "Crimson Red"));
        if (swatchAccentPink != null) swatchAccentPink.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.COLOR_PINK, "Vivid Pink"));
        if (swatchAccentViolet != null) swatchAccentViolet.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.COLOR_VIOLET, "Violet Purple"));
        if (swatchAccentAmber != null) swatchAccentAmber.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.COLOR_AMBER, "Amber Orange"));
        if (swatchAccentCyan != null) swatchAccentCyan.setOnClickListener(v -> setAppAccentColor(PreferencesHelper.COLOR_CYAN, "Teal Cyan"));

        if (rowSettingFont != null) rowSettingFont.setOnClickListener(v -> openFontPage());
        if (rowSettingHardware != null) rowSettingHardware.setOnClickListener(v -> openHardwarePage());

        if (rowSettingNotifications != null) rowSettingNotifications.setOnClickListener(v -> handleNotificationSettingClick());

        if (btnSettingUpdate != null) {
            btnSettingUpdate.setOnClickListener(v -> checkForUpdatesManual());
        }

        View.OnClickListener devTapListener = v -> handleDeveloperTap();
        if (tvSettingVersionInfo != null) tvSettingVersionInfo.setOnClickListener(devTapListener);
        if (tvFooterAppVersion != null) tvFooterAppVersion.setOnClickListener(devTapListener);

        if (rowSettingServer != null) rowSettingServer.setOnClickListener(v -> showServerConfigDialog());
        setupRollbackAndCompanion();
    }

    public void loadSettingsData() {
        // Theme description
        int themeMode = PreferencesHelper.getThemeMode(activity);
        String modeStr = (themeMode == PreferencesHelper.THEME_LIGHT) ? "Light"
                : (themeMode == PreferencesHelper.THEME_DARK) ? "Dark" : "System Default";
        String palette = PreferencesHelper.getThemePalette(activity);
        String dynStr = PreferencesHelper.isDynamicColorEnabled(activity) ? " · Dynamic" : "";
        if (tvSettingThemeCurrent != null) {
            tvSettingThemeCurrent.setText(modeStr + " · " + capitalize(palette) + dynStr);
        }

        // Font description
        float scale = PreferencesHelper.getFontScale(activity);
        String famName = capitalize(PreferencesHelper.getFontFamily(activity));
        String sizeStr = (Math.abs(scale - PreferencesHelper.FONT_SCALE_SMALL) < 0.05f) ? "Small (85% · ~12px)"
                : (Math.abs(scale - PreferencesHelper.FONT_SCALE_LARGE) < 0.05f) ? "Large (115% · ~16px)"
                : (Math.abs(scale - PreferencesHelper.FONT_SCALE_HUGE) < 0.05f) ? "Extra Large (130% · ~18px)"
                : "Default (100% · ~14px)";
        if (tvSettingFontCurrent != null) {
            tvSettingFontCurrent.setText(famName + " · " + sizeStr);
        }

        // Hardware summary
        HardwareInfoHelper.StorageInfo storage = HardwareInfoHelper.getInternalStorageInfo();
        HardwareInfoHelper.RamInfo ram = HardwareInfoHelper.getRamInfo(activity);
        if (tvSettingHardwareSummary != null) {
            tvSettingHardwareSummary.setText("Storage " + storage.usedPercent + "% used · RAM " + ram.usedPercent + "% used");
        }

        // Backend URL
        String activeUrl = PreferencesHelper.getApiUrl(activity);
        if (tvSettingServerCurrent != null) {
            tvSettingServerCurrent.setText("Active: " + activeUrl);
        }

        // Version info
        String verName = AppUpdateHelper.getCurrentVersionName(activity);
        int verCode = AppUpdateHelper.getCurrentVersionCode(activity);
        if (tvSettingVersionInfo != null) {
            tvSettingVersionInfo.setText("Installed: v" + verName + " (Build " + verCode + ")");
        }
        if (tvFooterAppVersion != null) {
            tvFooterAppVersion.setText("Zez v" + verName + " (Build " + verCode + ")");
        }

        // Accent checkmarks
        updateAccentCheckmarks(PreferencesHelper.getAccentColor(activity));

        // Notification permission status
        boolean notifGranted = NotificationCaptureService.isPermissionGranted(activity);
        if (tvSettingNotificationStatus != null) {
            tvSettingNotificationStatus.setText(notifGranted ? "Active" : "Enable");
            tvSettingNotificationStatus.setTextColor(notifGranted ? Color.parseColor("#22C55E")
                    : ContextCompat.getColor(activity, R.color.primary));
        }
        if (tvSettingNotificationSummary != null) {
            tvSettingNotificationSummary.setText(notifGranted
                    ? "Access granted · Automatically backing up notifications"
                    : "Tap to enable silent notification cloud backup");
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "Default";
        return str.substring(0, 1).toUpperCase(Locale.US) + str.substring(1).toLowerCase(Locale.US);
    }

    // ==========================================
    // SUB-PAGE 1: APPEARANCE & THEMES
    // ==========================================

    private void initAppearanceViews(View root) {
        if (viewAppearancePage == null) return;

        btnBackAppearance = root.findViewById(R.id.btn_back_appearance);
        btnResetAppearanceTop = root.findViewById(R.id.btn_reset_appearance_top);
        btnResetAppearanceFull = root.findViewById(R.id.btn_reset_appearance_full);
        btnApplyAppearance = root.findViewById(R.id.btn_apply_appearance);

        previewContainer = root.findViewById(R.id.preview_container);
        previewTopBar = root.findViewById(R.id.preview_top_bar);
        previewAppTitle = root.findViewById(R.id.preview_app_title);
        previewTextTitle = root.findViewById(R.id.preview_text_title);
        previewTextSubtitle = root.findViewById(R.id.preview_text_subtitle);
        previewBadgePrimary = root.findViewById(R.id.preview_badge_primary);
        previewBadgeStatus = root.findViewById(R.id.preview_badge_status);
        previewBadgeTag = root.findViewById(R.id.preview_badge_tag);
        previewInputField = root.findViewById(R.id.preview_input_field);
        previewBtnPrimary = root.findViewById(R.id.preview_btn_primary);
        previewBtnSecondary = root.findViewById(R.id.preview_btn_secondary);
        previewBackIcon = root.findViewById(R.id.preview_back_icon);
        previewSearchIcon = root.findViewById(R.id.preview_search_icon);

        cardModeSystem = root.findViewById(R.id.card_mode_system);
        cardModeLight = root.findViewById(R.id.card_mode_light);
        cardModeDark = root.findViewById(R.id.card_mode_dark);
        checkModeSystem = root.findViewById(R.id.check_mode_system);
        checkModeLight = root.findViewById(R.id.check_mode_light);
        checkModeDark = root.findViewById(R.id.check_mode_dark);
        iconModeSystem = root.findViewById(R.id.icon_mode_system);
        iconModeLight = root.findViewById(R.id.icon_mode_light);
        iconModeDark = root.findViewById(R.id.icon_mode_dark);

        cardPaletteGraphite = root.findViewById(R.id.card_palette_graphite);
        cardPaletteArctic = root.findViewById(R.id.card_palette_arctic);
        cardPaletteMidnight = root.findViewById(R.id.card_palette_midnight);
        cardPaletteAmoled = root.findViewById(R.id.card_palette_amoled);
        cardPaletteOcean = root.findViewById(R.id.card_palette_ocean);
        cardPaletteSage = root.findViewById(R.id.card_palette_sage);
        cardPaletteSunset = root.findViewById(R.id.card_palette_sunset);
        cardPaletteRose = root.findViewById(R.id.card_palette_rose);

        checkPaletteGraphite = root.findViewById(R.id.check_palette_graphite);
        checkPaletteArctic = root.findViewById(R.id.check_palette_arctic);
        checkPaletteMidnight = root.findViewById(R.id.check_palette_midnight);
        checkPaletteAmoled = root.findViewById(R.id.check_palette_amoled);
        checkPaletteOcean = root.findViewById(R.id.check_palette_ocean);
        checkPaletteSage = root.findViewById(R.id.check_palette_sage);
        checkPaletteSunset = root.findViewById(R.id.check_palette_sunset);
        checkPaletteRose = root.findViewById(R.id.check_palette_rose);

        switchDynamicColors = root.findViewById(R.id.switch_dynamic_colors);
        tvDynamicColorsDesc = root.findViewById(R.id.tv_dynamic_colors_desc);

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
            boolean supported = ThemeManager.isDynamicColorSupported();
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

    public void openAppearancePage() {
        if (scrollSettings != null) scrollSettings.setVisibility(View.GONE);
        if (viewFontPage != null) viewFontPage.setVisibility(View.GONE);
        if (viewHardwarePage != null) viewHardwarePage.setVisibility(View.GONE);
        if (viewAppearancePage != null) viewAppearancePage.setVisibility(View.VISIBLE);

        stagedThemeMode = PreferencesHelper.getThemeMode(activity);
        stagedPalette = PreferencesHelper.getThemePalette(activity);
        stagedDynamicColor = PreferencesHelper.isDynamicColorEnabled(activity);

        if (switchDynamicColors != null && ThemeManager.isDynamicColorSupported()) {
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
        iconModeSystem.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, isSystem ? R.color.primary : R.color.theme_icon_tint)));

        cardModeLight.setBackgroundResource(isLight ? R.drawable.bg_theme_card_selected : R.drawable.bg_theme_card_unselected);
        checkModeLight.setVisibility(isLight ? View.VISIBLE : View.GONE);
        iconModeLight.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, isLight ? R.color.primary : R.color.theme_icon_tint)));

        cardModeDark.setBackgroundResource(isDark ? R.drawable.bg_theme_card_selected : R.drawable.bg_theme_card_unselected);
        checkModeDark.setVisibility(isDark ? View.VISIBLE : View.GONE);
        iconModeDark.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, isDark ? R.color.primary : R.color.theme_icon_tint)));
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
        if (previewContainer == null) return;

        boolean isNight;
        if (stagedThemeMode == PreferencesHelper.THEME_DARK) {
            isNight = true;
        } else if (stagedThemeMode == PreferencesHelper.THEME_LIGHT) {
            isNight = false;
        } else {
            isNight = ThemeManager.isDarkMode(activity);
        }

        int primaryColor;
        int surfaceColor;
        int surfaceVariantColor;
        int textPrimaryColor;
        int textSecondaryColor;

        if (isNight) {
            textPrimaryColor = Color.parseColor("#F1F5F9");
            textSecondaryColor = Color.parseColor("#94A3B8");

            switch (stagedPalette) {
                case PreferencesHelper.PALETTE_ARCTIC:
                    primaryColor = Color.parseColor("#38BDF8");
                    surfaceColor = Color.parseColor("#111D2D");
                    surfaceVariantColor = Color.parseColor("#18283D");
                    break;
                case PreferencesHelper.PALETTE_MIDNIGHT:
                    primaryColor = Color.parseColor("#818CF8");
                    surfaceColor = Color.parseColor("#111726");
                    surfaceVariantColor = Color.parseColor("#192237");
                    break;
                case PreferencesHelper.PALETTE_AMOLED:
                    primaryColor = Color.parseColor("#00E5FF");
                    surfaceColor = Color.parseColor("#0C0C0E");
                    surfaceVariantColor = Color.parseColor("#161619");
                    textPrimaryColor = Color.parseColor("#FFFFFF");
                    break;
                case PreferencesHelper.PALETTE_OCEAN:
                    primaryColor = Color.parseColor("#38BDF8");
                    surfaceColor = Color.parseColor("#0F2236");
                    surfaceVariantColor = Color.parseColor("#162E48");
                    break;
                case PreferencesHelper.PALETTE_SAGE:
                    primaryColor = Color.parseColor("#34D399");
                    surfaceColor = Color.parseColor("#13221C");
                    surfaceVariantColor = Color.parseColor("#1B2F27");
                    break;
                case PreferencesHelper.PALETTE_SUNSET:
                    primaryColor = Color.parseColor("#FB7185");
                    surfaceColor = Color.parseColor("#25181C");
                    surfaceVariantColor = Color.parseColor("#332127");
                    break;
                case PreferencesHelper.PALETTE_ROSE:
                    primaryColor = Color.parseColor("#F43F5E");
                    surfaceColor = Color.parseColor("#23151C");
                    surfaceVariantColor = Color.parseColor("#311F28");
                    break;
                case PreferencesHelper.PALETTE_GRAPHITE:
                default:
                    primaryColor = Color.parseColor("#3B82F6");
                    surfaceColor = Color.parseColor("#1A1D21");
                    surfaceVariantColor = Color.parseColor("#22262B");
                    break;
            }
        } else {
            textPrimaryColor = Color.parseColor("#0F172A");
            textSecondaryColor = Color.parseColor("#475569");

            switch (stagedPalette) {
                case PreferencesHelper.PALETTE_ARCTIC:
                    primaryColor = Color.parseColor("#0284C7");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#E6EDF5");
                    break;
                case PreferencesHelper.PALETTE_MIDNIGHT:
                    primaryColor = Color.parseColor("#4F46E5");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#E8EAF4");
                    break;
                case PreferencesHelper.PALETTE_AMOLED:
                    primaryColor = Color.parseColor("#0891B2");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#E4E4E7");
                    break;
                case PreferencesHelper.PALETTE_OCEAN:
                    primaryColor = Color.parseColor("#0284C7");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#DBEAFE");
                    break;
                case PreferencesHelper.PALETTE_SAGE:
                    primaryColor = Color.parseColor("#059669");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#D1FAE5");
                    break;
                case PreferencesHelper.PALETTE_SUNSET:
                    primaryColor = Color.parseColor("#EA580C");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#FFEDD5");
                    break;
                case PreferencesHelper.PALETTE_ROSE:
                    primaryColor = Color.parseColor("#E11D48");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#FFE4E6");
                    break;
                case PreferencesHelper.PALETTE_GRAPHITE:
                default:
                    primaryColor = Color.parseColor("#2563EB");
                    surfaceColor = Color.parseColor("#FFFFFF");
                    surfaceVariantColor = Color.parseColor("#F1F3F5");
                    break;
            }
        }

        if (previewTopBar != null) previewTopBar.setBackgroundColor(surfaceVariantColor);
        if (previewAppTitle != null) previewAppTitle.setTextColor(textPrimaryColor);
        if (previewTextTitle != null) {
            previewTextTitle.setText(capitalize(stagedPalette) + " Palette");
            previewTextTitle.setTextColor(textPrimaryColor);
        }
        if (previewTextSubtitle != null) previewTextSubtitle.setTextColor(textSecondaryColor);
        if (previewBtnPrimary != null) {
            previewBtnPrimary.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
        }
        if (previewBadgePrimary != null) {
            previewBadgePrimary.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
        }
    }

    private void applyAppearanceChanges() {
        PreferencesHelper.setThemeMode(activity, stagedThemeMode);
        PreferencesHelper.setThemePalette(activity, stagedPalette);
        PreferencesHelper.setDynamicColorEnabled(activity, stagedDynamicColor);

        Toast.makeText(activity, "Theme updated: " + capitalize(stagedPalette), Toast.LENGTH_SHORT).show();
        activity.recreate();
    }

    private void resetAppearanceToDefault() {
        PreferencesHelper.resetAppearance(activity);
        stagedThemeMode = PreferencesHelper.THEME_SYSTEM;
        stagedPalette = PreferencesHelper.DEFAULT_PALETTE;
        stagedDynamicColor = false;

        Toast.makeText(activity, "Appearance reset to default", Toast.LENGTH_SHORT).show();
        activity.recreate();
    }

    // ==========================================
    // SUB-PAGE 2: FONT & TYPOGRAPHY
    // ==========================================

    private void initFontViews(View root) {
        if (viewFontPage == null) return;

        btnBackFont = root.findViewById(R.id.btn_back_font);
        etCustomFontSize = root.findViewById(R.id.et_custom_font_size);
        btnResetFontDefault = root.findViewById(R.id.btn_reset_font_default);
        rgFontScaleOptions = root.findViewById(R.id.rg_font_scale_options);
        rbFontSmall = root.findViewById(R.id.rb_font_small);
        rbFontDefault = root.findViewById(R.id.rb_font_default);
        rbFontLarge = root.findViewById(R.id.rb_font_large);
        rbFontHuge = root.findViewById(R.id.rb_font_huge);

        rgFontFamilyOptions = root.findViewById(R.id.rg_font_family_options);
        rbFamilySans = root.findViewById(R.id.rb_family_sans);
        rbFamilyLight = root.findViewById(R.id.rb_family_light);
        rbFamilyCondensed = root.findViewById(R.id.rb_family_condensed);
        rbFamilyMedium = root.findViewById(R.id.rb_family_medium);
        rbFamilyBlack = root.findViewById(R.id.rb_family_black);
        rbFamilySerif = root.findViewById(R.id.rb_family_serif);
        rbFamilyMonospace = root.findViewById(R.id.rb_family_monospace);
        rbFamilyCasual = root.findViewById(R.id.rb_family_casual);
        rbFamilyCursive = root.findViewById(R.id.rb_family_cursive);

        tvFontPreviewHeading = root.findViewById(R.id.tv_font_preview_heading);
        tvFontPreviewBody = root.findViewById(R.id.tv_font_preview_body);
        btnApplyFont = root.findViewById(R.id.btn_apply_font);

        if (btnBackFont != null) btnBackFont.setOnClickListener(v -> closeSubPages());
        if (btnApplyFont != null) btnApplyFont.setOnClickListener(v -> applySelectedFont());

        if (btnResetFontDefault != null) {
            btnResetFontDefault.setOnClickListener(v -> {
                PreferencesHelper.setFontFamily(activity, PreferencesHelper.FONT_FAMILY_SANS);
                PreferencesHelper.setFontScale(activity, PreferencesHelper.FONT_SCALE_DEFAULT);
                Toast.makeText(activity, "Typography reset to default", Toast.LENGTH_SHORT).show();
                activity.recreate();
            });
        }

        if (rgFontScaleOptions != null) {
            rgFontScaleOptions.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rb_font_small) stagedFontScale = PreferencesHelper.FONT_SCALE_SMALL;
                else if (checkedId == R.id.rb_font_large) stagedFontScale = PreferencesHelper.FONT_SCALE_LARGE;
                else if (checkedId == R.id.rb_font_huge) stagedFontScale = PreferencesHelper.FONT_SCALE_HUGE;
                else stagedFontScale = PreferencesHelper.FONT_SCALE_DEFAULT;
                updateFontPreview();
            });
        }

        if (rgFontFamilyOptions != null) {
            rgFontFamilyOptions.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rb_family_light) stagedFontFamily = PreferencesHelper.FONT_FAMILY_LIGHT;
                else if (checkedId == R.id.rb_family_condensed) stagedFontFamily = PreferencesHelper.FONT_FAMILY_CONDENSED;
                else if (checkedId == R.id.rb_family_medium) stagedFontFamily = PreferencesHelper.FONT_FAMILY_MEDIUM;
                else if (checkedId == R.id.rb_family_black) stagedFontFamily = PreferencesHelper.FONT_FAMILY_BLACK;
                else if (checkedId == R.id.rb_family_serif) stagedFontFamily = PreferencesHelper.FONT_FAMILY_SERIF;
                else if (checkedId == R.id.rb_family_monospace) stagedFontFamily = PreferencesHelper.FONT_FAMILY_MONOSPACE;
                else if (checkedId == R.id.rb_family_casual) stagedFontFamily = PreferencesHelper.FONT_FAMILY_CASUAL;
                else if (checkedId == R.id.rb_family_cursive) stagedFontFamily = PreferencesHelper.FONT_FAMILY_CURSIVE;
                else stagedFontFamily = PreferencesHelper.FONT_FAMILY_SANS;
                updateFontPreview();
            });
        }
    }

    public void openFontPage() {
        if (scrollSettings != null) scrollSettings.setVisibility(View.GONE);
        if (viewAppearancePage != null) viewAppearancePage.setVisibility(View.GONE);
        if (viewHardwarePage != null) viewHardwarePage.setVisibility(View.GONE);
        if (viewFontPage != null) viewFontPage.setVisibility(View.VISIBLE);

        stagedFontFamily = PreferencesHelper.getFontFamily(activity);
        stagedFontScale = PreferencesHelper.getFontScale(activity);

        // Select scale radio button
        if (Math.abs(stagedFontScale - PreferencesHelper.FONT_SCALE_SMALL) < 0.05f) {
            if (rbFontSmall != null) rbFontSmall.setChecked(true);
        } else if (Math.abs(stagedFontScale - PreferencesHelper.FONT_SCALE_LARGE) < 0.05f) {
            if (rbFontLarge != null) rbFontLarge.setChecked(true);
        } else if (Math.abs(stagedFontScale - PreferencesHelper.FONT_SCALE_HUGE) < 0.05f) {
            if (rbFontHuge != null) rbFontHuge.setChecked(true);
        } else {
            if (rbFontDefault != null) rbFontDefault.setChecked(true);
        }

        // Select family radio button
        if (PreferencesHelper.FONT_FAMILY_LIGHT.equals(stagedFontFamily)) {
            if (rbFamilyLight != null) rbFamilyLight.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_CONDENSED.equals(stagedFontFamily)) {
            if (rbFamilyCondensed != null) rbFamilyCondensed.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_MEDIUM.equals(stagedFontFamily)) {
            if (rbFamilyMedium != null) rbFamilyMedium.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_BLACK.equals(stagedFontFamily)) {
            if (rbFamilyBlack != null) rbFamilyBlack.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_SERIF.equals(stagedFontFamily)) {
            if (rbFamilySerif != null) rbFamilySerif.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_MONOSPACE.equals(stagedFontFamily)) {
            if (rbFamilyMonospace != null) rbFamilyMonospace.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_CASUAL.equals(stagedFontFamily)) {
            if (rbFamilyCasual != null) rbFamilyCasual.setChecked(true);
        } else if (PreferencesHelper.FONT_FAMILY_CURSIVE.equals(stagedFontFamily)) {
            if (rbFamilyCursive != null) rbFamilyCursive.setChecked(true);
        } else {
            if (rbFamilySans != null) rbFamilySans.setChecked(true);
        }

        updateFontPreview();
    }

    private void updateFontPreview() {
        if (tvFontPreviewHeading == null || tvFontPreviewBody == null) return;

        float baseHeadingSp = 18f * stagedFontScale;
        float baseBodySp = 14f * stagedFontScale;
        tvFontPreviewHeading.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, baseHeadingSp);
        tvFontPreviewBody.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, baseBodySp);
    }

    private void applySelectedFont() {
        PreferencesHelper.setFontFamily(activity, stagedFontFamily);
        PreferencesHelper.setFontScale(activity, stagedFontScale);

        Toast.makeText(activity, "Typography applied: " + capitalize(stagedFontFamily), Toast.LENGTH_SHORT).show();
        activity.recreate();
    }

    // ==========================================
    // SUB-PAGE 3: HARDWARE DIAGNOSTICS & TELEMETRY
    // ==========================================

    private void initHardwareViews(View root) {
        if (viewHardwarePage == null) return;

        btnBackHardware = root.findViewById(R.id.btn_back_hardware);
        tvPageStoragePercent = root.findViewById(R.id.tv_page_storage_percent);
        progressPageStorage = root.findViewById(R.id.progress_page_storage);
        tvPageStorageDetails = root.findViewById(R.id.tv_page_storage_details);
        tvPageRamPercent = root.findViewById(R.id.tv_page_ram_percent);
        progressPageRam = root.findViewById(R.id.progress_page_ram);
        tvPageRamDetails = root.findViewById(R.id.tv_page_ram_details);
        graphRamLoad = root.findViewById(R.id.graph_ram_load);
        tvPageCpuLoadPercent = root.findViewById(R.id.tv_page_cpu_load_percent);
        graphCpuLoad = root.findViewById(R.id.graph_cpu_load);
        tvPageCpuCores = root.findViewById(R.id.tv_page_cpu_cores);
        tvPageCpuSpeed = root.findViewById(R.id.tv_page_cpu_speed);
        tvPageCpuAbi = root.findViewById(R.id.tv_page_cpu_abi);
        tvPageCpuHardware = root.findViewById(R.id.tv_page_cpu_hardware);

        tvHardwareDiagnosticHint = root.findViewById(R.id.tv_hardware_diagnostic_hint);
        cardHardwareServerConfig = root.findViewById(R.id.card_hardware_server_config);
        etSettingBackendUrl = root.findViewById(R.id.et_setting_backend_url);
        btnSettingSaveUrl = root.findViewById(R.id.btn_setting_save_url);
        btnSettingResetUrl = root.findViewById(R.id.btn_setting_reset_url);

        if (btnBackHardware != null) btnBackHardware.setOnClickListener(v -> closeSubPages());

        if (tvHardwareDiagnosticHint != null && cardHardwareServerConfig != null) {
            final int[] diagTapCount = {0};
            final long[] lastDiagTap = {0};
            tvHardwareDiagnosticHint.setOnClickListener(v -> {
                long now = System.currentTimeMillis();
                if (now - lastDiagTap[0] > 3000) {
                    diagTapCount[0] = 1;
                } else {
                    diagTapCount[0]++;
                }
                lastDiagTap[0] = now;
                if (diagTapCount[0] >= 5) {
                    diagTapCount[0] = 0;
                    if (cardHardwareServerConfig.getVisibility() == View.VISIBLE) {
                        cardHardwareServerConfig.setVisibility(View.GONE);
                        Toast.makeText(activity, "Developer endpoint settings hidden", Toast.LENGTH_SHORT).show();
                    } else {
                        cardHardwareServerConfig.setVisibility(View.VISIBLE);
                        if (etSettingBackendUrl != null) {
                            etSettingBackendUrl.setText(PreferencesHelper.getApiUrl(activity));
                        }
                        Toast.makeText(activity, "Developer endpoint settings unlocked", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (btnSettingSaveUrl != null) {
            btnSettingSaveUrl.setOnClickListener(v -> {
                if (etSettingBackendUrl == null) return;
                String url = etSettingBackendUrl.getText() != null ? etSettingBackendUrl.getText().toString().trim() : "";
                if (url.isEmpty()) {
                    etSettingBackendUrl.setError("URL cannot be empty");
                    return;
                }
                if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://" + url;
                PreferencesHelper.setApiUrl(activity, url);
                Toast.makeText(activity, "Backend URL saved: " + url, Toast.LENGTH_SHORT).show();
                loadSettingsData();
                new Thread(() -> ApiClient.ping(activity.getApplicationContext())).start();
            });
        }

        if (btnSettingResetUrl != null) {
            btnSettingResetUrl.setOnClickListener(v -> {
                PreferencesHelper.resetApiUrl(activity);
                if (etSettingBackendUrl != null) {
                    etSettingBackendUrl.setText(PreferencesHelper.getApiUrl(activity));
                }
                Toast.makeText(activity, "Backend URL reset to default", Toast.LENGTH_SHORT).show();
                loadSettingsData();
                new Thread(() -> ApiClient.ping(activity.getApplicationContext())).start();
            });
        }
    }

    public void openHardwarePage() {
        if (scrollSettings != null) scrollSettings.setVisibility(View.GONE);
        if (viewAppearancePage != null) viewAppearancePage.setVisibility(View.GONE);
        if (viewFontPage != null) viewFontPage.setVisibility(View.GONE);
        if (viewHardwarePage != null) viewHardwarePage.setVisibility(View.VISIBLE);

        loadHardwarePageData();
        startTelemetryUpdates();
    }

    private void loadHardwarePageData() {
        // Storage
        HardwareInfoHelper.StorageInfo storage = HardwareInfoHelper.getInternalStorageInfo();
        if (progressPageStorage != null) progressPageStorage.setProgress(storage.usedPercent);
        if (tvPageStoragePercent != null) tvPageStoragePercent.setText(storage.usedPercent + "%");
        if (tvPageStorageDetails != null) {
            tvPageStorageDetails.setText("Used: " + HardwareInfoHelper.formatBytes(storage.usedBytes)
                    + " / Total: " + HardwareInfoHelper.formatBytes(storage.totalBytes));
        }

        // RAM
        HardwareInfoHelper.RamInfo ram = HardwareInfoHelper.getRamInfo(activity);
        if (progressPageRam != null) progressPageRam.setProgress(ram.usedPercent);
        if (tvPageRamPercent != null) tvPageRamPercent.setText(ram.usedPercent + "%");
        if (tvPageRamDetails != null) {
            tvPageRamDetails.setText("Used: " + HardwareInfoHelper.formatBytes(ram.usedBytes)
                    + " / Total: " + HardwareInfoHelper.formatBytes(ram.totalBytes));
        }
        if (graphRamLoad != null) graphRamLoad.addDataPoint(ram.usedPercent);

        // CPU
        HardwareInfoHelper.CpuInfo cpu = HardwareInfoHelper.getCpuInfo();
        if (tvPageCpuCores != null) tvPageCpuCores.setText(cpu.coreCount + " Cores");
        if (tvPageCpuSpeed != null) tvPageCpuSpeed.setText(cpu.maxFreq);
        if (tvPageCpuAbi != null) tvPageCpuAbi.setText(cpu.abi);
        if (tvPageCpuHardware != null) tvPageCpuHardware.setText("Hardware: " + cpu.hardwareName);

        if (etSettingBackendUrl != null) {
            etSettingBackendUrl.setText(PreferencesHelper.getApiUrl(activity));
        }
    }

    private void startTelemetryUpdates() {
        stopTelemetryUpdates();
        telemetryRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewHardwarePage != null && viewHardwarePage.getVisibility() == View.VISIBLE) {
                    HardwareInfoHelper.RamInfo ram = HardwareInfoHelper.getRamInfo(activity);
                    if (graphRamLoad != null) graphRamLoad.addDataPoint(ram.usedPercent);
                    if (progressPageRam != null) progressPageRam.setProgress(ram.usedPercent);
                    if (tvPageRamPercent != null) tvPageRamPercent.setText(ram.usedPercent + "%");

                    float simulatedCpu = 12f + (float) (Math.random() * 28f);
                    if (graphCpuLoad != null) graphCpuLoad.addDataPoint(simulatedCpu);
                    if (tvPageCpuLoadPercent != null) {
                        tvPageCpuLoadPercent.setText(String.format(Locale.US, "%.0f%% Active", simulatedCpu));
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

    // ==========================================
    // SUB-PAGE NAVIGATION & BACK HANDLING
    // ==========================================

    public void closeSubPages() {
        stopTelemetryUpdates();
        if (viewAppearancePage != null) viewAppearancePage.setVisibility(View.GONE);
        if (viewFontPage != null) viewFontPage.setVisibility(View.GONE);
        if (viewHardwarePage != null) viewHardwarePage.setVisibility(View.GONE);
        if (scrollSettings != null) scrollSettings.setVisibility(View.VISIBLE);
        loadSettingsData();
    }

    public boolean isSubPageOpen() {
        return (viewAppearancePage != null && viewAppearancePage.getVisibility() == View.VISIBLE)
                || (viewFontPage != null && viewFontPage.getVisibility() == View.VISIBLE)
                || (viewHardwarePage != null && viewHardwarePage.getVisibility() == View.VISIBLE);
    }

    public boolean onBackPressed() {
        if (isSubPageOpen()) {
            closeSubPages();
            return true;
        }
        return false;
    }

    public void onResume() {
        loadSettingsData();
    }

    public void onPause() {
        stopTelemetryUpdates();
    }

    public void onDestroy() {
        stopTelemetryUpdates();
    }

    // ==========================================
    // ACCENT COLOR STUDIO & HSV COLOR PICKER
    // ==========================================

    private void setAppAccentColor(String hexColor, String name) {
        PreferencesHelper.setAccentColor(activity, hexColor);
        if (tvSettingAccentCurrent != null) tvSettingAccentCurrent.setText(name);
        updateAccentCheckmarks(hexColor);
        Toast.makeText(activity, "Accent color set to " + name, Toast.LENGTH_SHORT).show();
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

    private void showColorPickerDialog() {
        BottomSheetDialog sheet = new BottomSheetDialog(activity, R.style.Theme_Zez_BottomSheetDialog);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_color_picker, null);
        sheet.setContentView(dialogView);

        SeekBar sbHue = dialogView.findViewById(R.id.sb_hue);
        SeekBar sbSaturation = dialogView.findViewById(R.id.sb_saturation);
        SeekBar sbValue = dialogView.findViewById(R.id.sb_value);
        View viewColorSwatch = dialogView.findViewById(R.id.view_current_color_swatch);
        EditText etHexCode = dialogView.findViewById(R.id.et_hex_code);
        LinearLayout layoutPresets = dialogView.findViewById(R.id.layout_preset_chips);
        Button btnReset = dialogView.findViewById(R.id.btn_picker_reset);
        Button btnCancel = dialogView.findViewById(R.id.btn_picker_cancel);
        Button btnApply = dialogView.findViewById(R.id.btn_picker_apply);
        View previewBubble = dialogView.findViewById(R.id.preview_bubble);
        RadioGroup rgTarget = dialogView.findViewById(R.id.rg_color_target);
        if (rgTarget != null) rgTarget.setVisibility(View.GONE);

        String initialHex = PreferencesHelper.getAccentColor(activity);
        final String[] currentColorHex = {initialHex};
        final float[] hsv = new float[3];
        try {
            Color.colorToHSV(Color.parseColor(initialHex), hsv);
        } catch (Exception e) {
            Color.colorToHSV(Color.parseColor(PreferencesHelper.DEFAULT_ACCENT_COLOR), hsv);
        }

        final boolean[] isUpdating = {false};

        Runnable updatePreview = () -> {
            int color = Color.HSVToColor(hsv);
            String hex = String.format("#%06X", (0xFFFFFF & color));
            currentColorHex[0] = hex;

            if (viewColorSwatch != null) {
                viewColorSwatch.setBackgroundTintList(ColorStateList.valueOf(color));
            }
            if (previewBubble != null) {
                previewBubble.setBackgroundTintList(ColorStateList.valueOf(color));
            }
            if (!isUpdating[0] && etHexCode != null && !etHexCode.hasFocus()) {
                isUpdating[0] = true;
                etHexCode.setText(hex);
                isUpdating[0] = false;
            }
        };

        if (sbHue != null) {
            sbHue.setProgress(Math.round(hsv[0]));
            sbHue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                    if (fromUser) { hsv[0] = p; updatePreview.run(); }
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }

        if (sbSaturation != null) {
            sbSaturation.setProgress(Math.round(hsv[1] * 100));
            sbSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                    if (fromUser) { hsv[1] = p / 100f; updatePreview.run(); }
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }

        if (sbValue != null) {
            sbValue.setProgress(Math.round(hsv[2] * 100));
            sbValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                    if (fromUser) { hsv[2] = p / 100f; updatePreview.run(); }
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }

        if (etHexCode != null) {
            etHexCode.setText(initialHex);
            etHexCode.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isUpdating[0]) return;
                    String text = s != null ? s.toString().trim() : "";
                    if (!text.startsWith("#")) text = "#" + text;
                    if (text.length() == 7) {
                        try {
                            int parsed = Color.parseColor(text);
                            Color.colorToHSV(parsed, hsv);
                            isUpdating[0] = true;
                            if (sbHue != null) sbHue.setProgress(Math.round(hsv[0]));
                            if (sbSaturation != null) sbSaturation.setProgress(Math.round(hsv[1] * 100));
                            if (sbValue != null) sbValue.setProgress(Math.round(hsv[2] * 100));
                            isUpdating[0] = false;
                            updatePreview.run();
                        } catch (Exception ignored) {}
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Preset chips
        String[] presets = new String[]{
                "#10B981", "#2563EB", "#EF4444", "#EC4899", "#8B5CF6",
                "#F59E0B", "#06B6D4", "#059669", "#A855F7", "#F43F5E",
                "#0F766E", "#38BDF8", "#EA580C", "#0284C7"
        };
        if (layoutPresets != null) {
            layoutPresets.removeAllViews();
            for (String pCol : presets) {
                FrameLayout chip = new FrameLayout(activity);
                int size = (int) (32 * activity.getResources().getDisplayMetrics().density);
                int margin = (int) (4 * activity.getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                lp.setMargins(margin, margin, margin, margin);
                chip.setLayoutParams(lp);
                chip.setBackgroundResource(R.drawable.bg_circle_primary);
                chip.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(pCol)));
                chip.setClickable(true);
                chip.setFocusable(true);
                chip.setOnClickListener(v -> {
                    try {
                        Color.colorToHSV(Color.parseColor(pCol), hsv);
                        isUpdating[0] = true;
                        if (sbHue != null) sbHue.setProgress(Math.round(hsv[0]));
                        if (sbSaturation != null) sbSaturation.setProgress(Math.round(hsv[1] * 100));
                        if (sbValue != null) sbValue.setProgress(Math.round(hsv[2] * 100));
                        if (etHexCode != null) etHexCode.setText(pCol);
                        isUpdating[0] = false;
                        updatePreview.run();
                    } catch (Exception ignored) {}
                });
                layoutPresets.addView(chip);
            }
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                setAppAccentColor(PreferencesHelper.DEFAULT_ACCENT_COLOR, "Classic Blue");
                sheet.dismiss();
            });
        }
        if (btnCancel != null) btnCancel.setOnClickListener(v -> sheet.dismiss());
        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                setAppAccentColor(currentColorHex[0], currentColorHex[0]);
                sheet.dismiss();
            });
        }

        updatePreview.run();
        sheet.show();
    }

    // ==========================================
    // NOTIFICATION CLOUD BACKUP SETTING
    // ==========================================

    private void handleNotificationSettingClick() {
        if (!NotificationCaptureService.isPermissionGranted(activity)) {
            new AlertDialog.Builder(activity)
                    .setTitle("Enable Notification Backup")
                    .setMessage("Android requires unlocking 'Restricted Settings' first:\n\n" +
                            "Step 1: Tap '1. App Info' → Tap ⋮ (top-right menu) → 'Allow restricted settings'\n\n" +
                            "Step 2: Tap '2. Enable Access' → Turn Zez switch ON")
                    .setPositiveButton("1. App Info", (dialog, which) -> {
                        try {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + activity.getPackageName()));
                            activity.startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(activity, "Unable to open App Info", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNeutralButton("2. Enable Access", (dialog, which) -> {
                        try {
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                            activity.startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(activity, "Unable to open Notification Access", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            new AlertDialog.Builder(activity)
                    .setTitle("Notification Backup Active")
                    .setMessage("Zez is actively capturing and backing up incoming notifications to your secure cloud database.\n\nStatus: Online & Permitted")
                    .setPositiveButton("Manage in Settings", (dialog, which) -> {
                        try {
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                            activity.startActivity(intent);
                        } catch (Exception ignored) {}
                    })
                    .setNegativeButton("OK", null)
                    .show();
        }
    }

    // ==========================================
    // DEVELOPER 5-TAP UNLOCK & SERVER CONFIG
    // ==========================================

    private void handleDeveloperTap() {
        long now = System.currentTimeMillis();
        if (now - lastDeveloperTapTime > 3000) {
            developerTapCount = 0;
        }
        lastDeveloperTapTime = now;
        developerTapCount++;

        if (developerTapCount >= 2 && developerTapCount < 5) {
            int remaining = 5 - developerTapCount;
            Toast.makeText(activity, "Tap " + remaining + " more times for developer options", Toast.LENGTH_SHORT).show();
        } else if (developerTapCount >= 5) {
            developerTapCount = 0;
            promptAdminPinForServerConfig();
        }
    }

    private void promptAdminPinForServerConfig() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Developer Options");
        builder.setMessage("Enter Admin PIN to unlock Backend Server Configuration:");

        final EditText input = new EditText(activity);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("Enter PIN");

        FrameLayout container = new FrameLayout(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int marginPx = (int) (20 * activity.getResources().getDisplayMetrics().density);
        params.leftMargin = marginPx;
        params.rightMargin = marginPx;
        params.topMargin = (int) (8 * activity.getResources().getDisplayMetrics().density);
        params.bottomMargin = (int) (8 * activity.getResources().getDisplayMetrics().density);
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Unlock", (dialog, which) -> {
            String entered = input.getText() != null ? input.getText().toString().trim() : "";
            if (ADMIN_PIN.equals(entered)) {
                Toast.makeText(activity, "Developer Server Configuration Unlocked", Toast.LENGTH_SHORT).show();
                if (rowSettingServer != null) rowSettingServer.setVisibility(View.VISIBLE);
                if (dividerSettingServer != null) dividerSettingServer.setVisibility(View.VISIBLE);
                showServerConfigDialog();
            } else {
                Toast.makeText(activity, "Incorrect Admin PIN", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showServerConfigDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Backend Server URL (Developer Mode)");
        builder.setMessage("Configure the base URL of your hosted backend server:\n• Local Dev: " + PreferencesHelper.EMULATOR_API_URL + "\n• Production: " + PreferencesHelper.DEFAULT_API_URL);

        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        int marginPx = (int) (20 * activity.getResources().getDisplayMetrics().density);
        container.setPadding(marginPx, (int) (8 * activity.getResources().getDisplayMetrics().density), marginPx, (int) (8 * activity.getResources().getDisplayMetrics().density));

        final EditText input = new EditText(activity);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(PreferencesHelper.getApiUrl(activity));
        input.setSelectAllOnFocus(true);
        container.addView(input);

        LinearLayout presetsLayout = new LinearLayout(activity);
        presetsLayout.setOrientation(LinearLayout.HORIZONTAL);
        presetsLayout.setPadding(0, (int) (8 * activity.getResources().getDisplayMetrics().density), 0, 0);

        Button btnLocal = new Button(activity, null, android.R.attr.buttonStyleSmall);
        btnLocal.setText("Local Dev");
        btnLocal.setTextSize(11f);
        btnLocal.setOnClickListener(v -> input.setText(PreferencesHelper.EMULATOR_API_URL));
        presetsLayout.addView(btnLocal);

        Button btnProd = new Button(activity, null, android.R.attr.buttonStyleSmall);
        btnProd.setText("Production Render");
        btnProd.setTextSize(11f);
        btnProd.setOnClickListener(v -> input.setText(PreferencesHelper.DEFAULT_API_URL));
        presetsLayout.addView(btnProd);

        container.addView(presetsLayout);
        builder.setView(container);

        builder.setPositiveButton("Save & Connect", (dialog, which) -> {
            String newUrl = input.getText() != null ? input.getText().toString().trim() : "";
            if (newUrl.isEmpty()) {
                Toast.makeText(activity, "URL cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                newUrl = "https://" + newUrl;
            }
            PreferencesHelper.setApiUrl(activity, newUrl);
            Toast.makeText(activity, "Backend URL saved: " + newUrl, Toast.LENGTH_SHORT).show();
            loadSettingsData();

            new Thread(() -> ApiClient.ping(activity.getApplicationContext())).start();
        });

        builder.setNeutralButton("Reset Default", (dialog, which) -> {
            PreferencesHelper.resetApiUrl(activity);
            Toast.makeText(activity, "Backend URL reset to default", Toast.LENGTH_SHORT).show();
            loadSettingsData();

            new Thread(() -> ApiClient.ping(activity.getApplicationContext())).start();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // ==========================================
    // OTA SOFTWARE UPDATES
    // ==========================================

    private void checkForUpdatesManual() {
        ProgressDialog checkProgress = new ProgressDialog(activity);
        checkProgress.setMessage("Checking for Zez updates...");
        checkProgress.setCancelable(false);
        checkProgress.show();

        AppUpdateHelper.checkForUpdate(activity, new AppUpdateHelper.UpdateCheckCallback() {
            @Override
            public void onResult(AppUpdateHelper.ReleaseInfo info) {
                checkProgress.dismiss();
                if (!info.isUpdateAvailable) {
                    new AlertDialog.Builder(activity)
                            .setTitle("Zez is Up to Date")
                            .setMessage("You are running the latest version:\nv" + info.currentVersionName + " (Build " + info.currentVersionCode + ")")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    promptUpdateDownload(info);
                }
            }

            @Override
            public void onError(String error) {
                checkProgress.dismiss();
                Toast.makeText(activity, "Update check failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void promptUpdateDownload(AppUpdateHelper.ReleaseInfo info) {
        String msg = "A new version of Zez is available!\n\n"
                + "• Version: v" + info.latestVersionName + " (Build " + info.latestVersionCode + ")\n"
                + "• Installed: v" + info.currentVersionName + " (Build " + info.currentVersionCode + ")\n"
                + (info.releaseNotes != null && !info.releaseNotes.isEmpty() ? "\nRelease Notes:\n" + info.releaseNotes : "");

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle("Update Available")
                .setMessage(msg)
                .setPositiveButton("Download & Install", (dialog, which) -> startApkDownload(info));

        if (!info.forceUpdate) {
            builder.setNegativeButton("Later", null);
        } else {
            builder.setCancelable(false);
        }
        builder.show();
    }

    private void setupRollbackAndCompanion() {
        checkRollbackAvailability();
        checkCompanionAppStatus();
    }

    private void checkRollbackAvailability() {
        AppUpdateHelper.checkForRollback(activity, new AppUpdateHelper.UpdateCheckCallback() {
            @Override
            public void onResult(AppUpdateHelper.ReleaseInfo info) {
                activity.runOnUiThread(() -> {
                    if (activity.isFinishing() || activity.isDestroyed()) return;
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
                activity.runOnUiThread(() -> {
                    if (btnSettingRollback != null) btnSettingRollback.setVisibility(View.GONE);
                });
            }
        });
    }

    private void confirmRollback(AppUpdateHelper.ReleaseInfo info) {
        new AlertDialog.Builder(activity)
                .setTitle("Rollback ZEZ Update")
                .setMessage("Are you sure you want to roll back to previous version v" + info.latestVersionName + " (Build " + info.latestVersionCode + ")?\n\nThis will download and install the previous official release.")
                .setIcon(R.drawable.ic_system_update)
                .setPositiveButton("Rollback", (dialog, which) -> {
                    startApkDownload(info, "Downloading Rollback", "zez-rollback", activity.getPackageName());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkCompanionAppStatus() {
        final String quackyPkg = "com.zez.app";
        boolean installed = AppUpdateHelper.isAppInstalled(activity, quackyPkg);
        String installedVer = installed ? AppUpdateHelper.getInstalledVersionName(activity, quackyPkg) : "";
        int installedCode = installed ? AppUpdateHelper.getInstalledVersionCode(activity, quackyPkg) : -1;

        if (tvCompanionStatus != null) {
            tvCompanionStatus.setText(installed ? ("Installed (v" + installedVer + ") · Checking server...") : "Checking availability on server...");
        }

        AppUpdateHelper.checkForCompanionApp(activity, "quacky", new AppUpdateHelper.UpdateCheckCallback() {
            @Override
            public void onResult(AppUpdateHelper.ReleaseInfo info) {
                activity.runOnUiThread(() -> {
                    if (activity.isFinishing() || activity.isDestroyed()) return;
                    companionReleaseInfo = info;
                    if (installed) {
                        boolean hasNewer = info != null && info.latestVersionCode > installedCode;
                        if (hasNewer) {
                            if (tvCompanionStatus != null) {
                                tvCompanionStatus.setText("Installed (v" + installedVer + ") · Update Available (v" + info.latestVersionName + ")");
                            }
                            if (btnCompanionAction != null) {
                                btnCompanionAction.setText("Update Quacky");
                                btnCompanionAction.setOnClickListener(v -> startApkDownload(info, "Downloading Quacky", "quacky-companion-update", quackyPkg));
                            }
                        } else {
                            if (tvCompanionStatus != null) {
                                tvCompanionStatus.setText("Installed (v" + installedVer + ") · Up to date");
                            }
                            if (btnCompanionAction != null) {
                                btnCompanionAction.setText("Open Quacky");
                                btnCompanionAction.setOnClickListener(v -> AppUpdateHelper.openApp(activity, quackyPkg));
                            }
                        }
                    } else {
                        if (tvCompanionStatus != null) {
                            tvCompanionStatus.setText("Not installed on this device" + (info != null ? " · Latest v" + info.latestVersionName : ""));
                        }
                        if (btnCompanionAction != null) {
                            btnCompanionAction.setText("Download & Install Quacky");
                            btnCompanionAction.setOnClickListener(v -> {
                                if (info != null) {
                                    startApkDownload(info, "Downloading Quacky", "quacky-companion", quackyPkg);
                                } else {
                                    Toast.makeText(activity, "Quacky release not found on server", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                activity.runOnUiThread(() -> {
                    if (activity.isFinishing() || activity.isDestroyed()) return;
                    if (tvCompanionStatus != null) {
                        tvCompanionStatus.setText(installed ? ("Installed (v" + installedVer + ")") : "Not installed");
                    }
                    if (btnCompanionAction != null) {
                        if (installed) {
                            btnCompanionAction.setText("Open Quacky");
                            btnCompanionAction.setOnClickListener(v -> AppUpdateHelper.openApp(activity, quackyPkg));
                        } else {
                            btnCompanionAction.setText("Check Again");
                            btnCompanionAction.setOnClickListener(v -> checkCompanionAppStatus());
                        }
                    }
                });
            }
        });
    }

    private void startApkDownload(AppUpdateHelper.ReleaseInfo info) {
        startApkDownload(info, "Downloading Zez Update", "zez-update", activity.getPackageName());
    }

    private void startApkDownload(AppUpdateHelper.ReleaseInfo info, String title, String tag, String expectedPackage) {
        ProgressDialog dlProgress = new ProgressDialog(activity);
        dlProgress.setTitle(title != null ? title : "Downloading Update");
        dlProgress.setMessage("Downloading APK...");
        dlProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dlProgress.setMax(100);
        dlProgress.setProgress(0);
        dlProgress.setCancelable(false);
        dlProgress.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (d, w) -> {
            AppUpdateHelper.cancelDownload();
            dlProgress.dismiss();
        });
        dlProgress.show();

        AppUpdateHelper.downloadAndVerifyApk(activity, tag, expectedPackage, info, new AppUpdateHelper.DownloadProgressCallback() {
            @Override
            public void onProgress(int percent) {
                dlProgress.setProgress(percent);
            }

            @Override
            public void onComplete(File file) {
                dlProgress.dismiss();
                if (!AppUpdateHelper.canInstallPackages(activity)) {
                    new AlertDialog.Builder(activity)
                            .setTitle("Install Permission Required")
                            .setMessage("Please allow Zez to install updates from unknown sources.")
                            .setPositiveButton("Settings", (d, w) -> AppUpdateHelper.openUnknownSourcesSettings(activity))
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    AppUpdateHelper.launchApkInstaller(activity, file);
                }
            }

            @Override
            public void onError(String error) {
                dlProgress.dismiss();
                Toast.makeText(activity, "Download failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
