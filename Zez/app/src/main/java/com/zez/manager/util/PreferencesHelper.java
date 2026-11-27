package com.zez.manager.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.UUID;

public class PreferencesHelper {
    private static final String PREF_NAME = "zez_manager_prefs";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_DEVICE_TOKEN = "device_token";
    private static final String KEY_PERSISTENT_UUID = "persistent_uuid";
    private static final String KEY_API_URL = "api_base_url";
    private static final String KEY_LAST_SYNC = "last_sync_timestamp";
    private static final String KEY_LAST_CALL_COUNT = "last_call_count";
    private static final String KEY_LAST_SYNC_ERROR = "last_sync_error";
    private static final String KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled";
    private static final String KEY_CALLS_SYNC_ENABLED = "calls_sync_enabled";
    private static final String KEY_NOTIF_SYNC_ENABLED = "notif_sync_enabled";
    private static final String KEY_APP_CALLS_SYNC_ENABLED = "app_calls_sync_enabled";
    private static final String KEY_WIFI_ONLY_SYNC = "wifi_only_sync";
    private static final String KEY_NOTIF_PROMPTED = "has_prompted_notif_perm";
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final String KEY_THEME_PALETTE = "theme_palette";
    public static final String KEY_DYNAMIC_COLOR = "dynamic_color";
    public static final String KEY_FONT_SCALE = "font_scale";
    public static final String KEY_FONT_FAMILY = "font_family";
    public static final String KEY_ACCENT_COLOR = "accent_color";
    private static final String KEY_LAST_BACKUP_TS = "last_local_backup_ts";
    private static final String KEY_LAST_RESTORE_TS = "last_restore_ts";

    public static final String DEFAULT_API_URL = "https://zez-chat-backend.onrender.com";
    public static final String EMULATOR_API_URL = "http://10.0.2.2:3000";

    // 8 Theme Palettes
    public static final String PALETTE_GRAPHITE = "graphite";
    public static final String PALETTE_ARCTIC = "arctic";
    public static final String PALETTE_MIDNIGHT = "midnight";
    public static final String PALETTE_AMOLED = "amoled";
    public static final String PALETTE_OCEAN = "ocean";
    public static final String PALETTE_SAGE = "sage";
    public static final String PALETTE_SUNSET = "sunset";
    public static final String PALETTE_ROSE = "rose";
    public static final String DEFAULT_PALETTE = PALETTE_GRAPHITE;

    // Accent Colors
    public static final String DEFAULT_ACCENT_COLOR = "#2563EB";
    public static final String COLOR_EMERALD = "#10B981";
    public static final String COLOR_CRIMSON = "#EF4444";
    public static final String COLOR_PINK = "#EC4899";
    public static final String COLOR_VIOLET = "#8B5CF6";
    public static final String COLOR_AMBER = "#F59E0B";
    public static final String COLOR_CYAN = "#06B6D4";

    public static final String[] ACCENT_PALETTE = {
            DEFAULT_ACCENT_COLOR,
            COLOR_EMERALD,
            COLOR_CRIMSON,
            COLOR_PINK,
            COLOR_VIOLET,
            COLOR_AMBER,
            COLOR_CYAN
    };

    // Font Scale presets
    public static final float FONT_SCALE_SMALL = 0.85f;
    public static final float FONT_SCALE_DEFAULT = 1.0f;
    public static final float FONT_SCALE_LARGE = 1.15f;
    public static final float FONT_SCALE_HUGE = 1.30f;

    // Font Families
    public static final String FONT_FAMILY_SANS = "sans";
    public static final String FONT_FAMILY_LIGHT = "light";
    public static final String FONT_FAMILY_CONDENSED = "condensed";
    public static final String FONT_FAMILY_MEDIUM = "medium";
    public static final String FONT_FAMILY_BLACK = "black";
    public static final String FONT_FAMILY_SERIF = "serif";
    public static final String FONT_FAMILY_MONOSPACE = "monospace";
    public static final String FONT_FAMILY_CASUAL = "casual";
    public static final String FONT_FAMILY_CURSIVE = "cursive";

    public static final int THEME_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; // -1
    public static final int THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO; // 1
    public static final int THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES; // 2

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic")
                || Build.FINGERPRINT.contains("emu64")
                || Build.FINGERPRINT.contains("sdk_gphone")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("sdk_gphone")
                || Build.MODEL.contains("Android SDK")
                || Build.PRODUCT.contains("sdk_gphone");
    }

    public static String getApiUrl(Context context) {
        String saved = getPrefs(context).getString(KEY_API_URL, "");
        if (saved != null && !saved.trim().isEmpty()) {
            return saved.trim();
        }
        return isEmulator() ? EMULATOR_API_URL : DEFAULT_API_URL;
    }

    public static void setApiUrl(Context context, String url) {
        if (url == null || url.trim().isEmpty()) {
            getPrefs(context).edit().remove(KEY_API_URL).apply();
        } else {
            String clean = url.trim();
            while (clean.endsWith("/")) clean = clean.substring(0, clean.length() - 1);
            getPrefs(context).edit().putString(KEY_API_URL, clean).apply();
        }
    }

    public static void resetApiUrl(Context context) {
        setApiUrl(context, DEFAULT_API_URL);
    }

    public static String getPersistentUuid(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String uuid = prefs.getString(KEY_PERSISTENT_UUID, null);
        if (uuid == null || uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_PERSISTENT_UUID, uuid).apply();
        }
        return uuid;
    }

    public static String getDeviceId(Context context) {
        return getPrefs(context).getString(KEY_DEVICE_ID, null);
    }

    public static void setDeviceId(Context context, String id) {
        getPrefs(context).edit().putString(KEY_DEVICE_ID, id).apply();
    }

    public static String getDeviceToken(Context context) {
        return getPrefs(context).getString(KEY_DEVICE_TOKEN, null);
    }

    public static void setDeviceToken(Context context, String token) {
        if (token == null) {
            getPrefs(context).edit().remove(KEY_DEVICE_TOKEN).apply();
        } else {
            getPrefs(context).edit().putString(KEY_DEVICE_TOKEN, token).apply();
        }
    }

    public static void setLastSync(Context context, long ts, int count) {
        getPrefs(context).edit()
                .putLong(KEY_LAST_SYNC, ts)
                .putInt(KEY_LAST_CALL_COUNT, count)
                .remove(KEY_LAST_SYNC_ERROR)
                .apply();
    }

    public static long getLastSyncTime(Context context) {
        return getPrefs(context).getLong(KEY_LAST_SYNC, 0L);
    }

    public static int getLastSyncCount(Context context) {
        return getPrefs(context).getInt(KEY_LAST_CALL_COUNT, 0);
    }

    public static void setLastSyncError(Context context, String err) {
        if (err == null) {
            getPrefs(context).edit().remove(KEY_LAST_SYNC_ERROR).apply();
        } else {
            getPrefs(context).edit().putString(KEY_LAST_SYNC_ERROR, err).apply();
        }
    }

    public static String getLastSyncError(Context context) {
        return getPrefs(context).getString(KEY_LAST_SYNC_ERROR, null);
    }

    public static boolean isAutoSyncEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_AUTO_SYNC_ENABLED, true);
    }

    public static void setAutoSyncEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_SYNC_ENABLED, enabled).apply();
    }

    public static boolean isCallsSyncEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_CALLS_SYNC_ENABLED, true);
    }

    public static void setCallsSyncEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_CALLS_SYNC_ENABLED, enabled).apply();
    }

    public static boolean isNotifSyncEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_NOTIF_SYNC_ENABLED, true);
    }

    public static void setNotifSyncEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_NOTIF_SYNC_ENABLED, enabled).apply();
    }

    public static boolean isAppCallsSyncEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_APP_CALLS_SYNC_ENABLED, true);
    }

    public static void setAppCallsSyncEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_APP_CALLS_SYNC_ENABLED, enabled).apply();
    }

    public static boolean isWifiOnlySync(Context context) {
        return getPrefs(context).getBoolean(KEY_WIFI_ONLY_SYNC, false);
    }

    public static void setWifiOnlySync(Context context, boolean wifiOnly) {
        getPrefs(context).edit().putBoolean(KEY_WIFI_ONLY_SYNC, wifiOnly).apply();
    }

    public static boolean hasPromptedNotifPerm(Context context) {
        return getPrefs(context).getBoolean(KEY_NOTIF_PROMPTED, false);
    }

    public static void setPromptedNotifPerm(Context context) {
        getPrefs(context).edit().putBoolean(KEY_NOTIF_PROMPTED, true).apply();
    }

    public static int getThemeMode(Context context) {
        return getPrefs(context).getInt(KEY_THEME_MODE, THEME_SYSTEM);
    }

    public static void setThemeMode(Context context, int mode) {
        getPrefs(context).edit().putInt(KEY_THEME_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static String getThemePalette(Context context) {
        return getPrefs(context).getString(KEY_THEME_PALETTE, DEFAULT_PALETTE);
    }

    public static void setThemePalette(Context context, String palette) {
        if (palette != null) {
            getPrefs(context).edit().putString(KEY_THEME_PALETTE, palette).apply();
        }
    }

    public static boolean isDynamicColorEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_DYNAMIC_COLOR, false);
    }

    public static void setDynamicColorEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply();
    }

    public static void resetAppearance(Context context) {
        setThemeMode(context, THEME_SYSTEM);
        setThemePalette(context, DEFAULT_PALETTE);
        setDynamicColorEnabled(context, false);
    }

    public static float getFontScale(Context context) {
        return getPrefs(context).getFloat(KEY_FONT_SCALE, FONT_SCALE_DEFAULT);
    }

    public static void setFontScale(Context context, float scale) {
        getPrefs(context).edit().putFloat(KEY_FONT_SCALE, scale).apply();
    }

    public static String getFontFamily(Context context) {
        return getPrefs(context).getString(KEY_FONT_FAMILY, FONT_FAMILY_SANS);
    }

    public static void setFontFamily(Context context, String family) {
        getPrefs(context).edit().putString(KEY_FONT_FAMILY, family).apply();
    }

    public static String getAccentColor(Context context) {
        return getPrefs(context).getString(KEY_ACCENT_COLOR, DEFAULT_ACCENT_COLOR);
    }

    public static int getAccentColorInt(Context context) {
        String hex = getAccentColor(context);
        try {
            return Color.parseColor(hex);
        } catch (Exception e) {
            return Color.parseColor(DEFAULT_ACCENT_COLOR);
        }
    }

    public static void setAccentColor(Context context, String colorHex) {
        if (colorHex != null) {
            getPrefs(context).edit().putString(KEY_ACCENT_COLOR, colorHex).apply();
        }
    }

    public static long getLastBackupTime(Context context) {
        return getPrefs(context).getLong(KEY_LAST_BACKUP_TS, 0L);
    }

    public static void setLastBackupTime(Context context, long ts) {
        getPrefs(context).edit().putLong(KEY_LAST_BACKUP_TS, ts).apply();
    }

    public static long getLastRestoreTime(Context context) {
        return getPrefs(context).getLong(KEY_LAST_RESTORE_TS, 0L);
    }

    public static void setLastRestoreTime(Context context, long ts) {
        getPrefs(context).edit().putLong(KEY_LAST_RESTORE_TS, ts).apply();
    }
}
