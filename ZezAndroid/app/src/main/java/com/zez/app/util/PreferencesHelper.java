package com.zez.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class PreferencesHelper {
    private static final String PREF_NAME = "zez_prefs";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_DEVICE_TOKEN = "device_token";
    private static final String KEY_PERSISTENT_UUID = "persistent_uuid";
    private static final String KEY_API_URL = "api_url";
    private static final String KEY_LAST_SYNC = "last_sync_time";
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final String KEY_FONT_SCALE = "font_scale";
    public static final String KEY_ACCENT_COLOR = "accent_color";

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

    public static final float FONT_SCALE_SMALL = 0.85f;
    public static final float FONT_SCALE_DEFAULT = 1.0f;
    public static final float FONT_SCALE_LARGE = 1.15f;
    public static final float FONT_SCALE_HUGE = 1.30f;

    public static final int THEME_SYSTEM = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; // -1
    public static final int THEME_LIGHT = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO; // 1
    public static final int THEME_DARK = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES; // 2

    // Default API URL from user configuration
    public static final String DEFAULT_API_URL = "https://zez-chat-backend.onrender.com";
    public static final String EMULATOR_API_URL = "http://10.0.2.2:3000";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isEmulator() {
        return android.os.Build.FINGERPRINT.contains("generic")
                || android.os.Build.FINGERPRINT.contains("emu64")
                || android.os.Build.FINGERPRINT.contains("sdk_gphone")
                || android.os.Build.HARDWARE.contains("goldfish")
                || android.os.Build.HARDWARE.contains("ranchu")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("sdk_gphone")
                || android.os.Build.MODEL.contains("Android SDK")
                || android.os.Build.PRODUCT.contains("sdk_gphone");
    }

    public static String getPersistentUuid(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String uuid = prefs.getString(KEY_PERSISTENT_UUID, null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_PERSISTENT_UUID, uuid).apply();
        }
        return uuid;
    }

    public static String getDeviceId(Context context) {
        return getPrefs(context).getString(KEY_DEVICE_ID, null);
    }

    public static void setDeviceId(Context context, String deviceId) {
        getPrefs(context).edit().putString(KEY_DEVICE_ID, deviceId).apply();
    }

    public static String getDeviceToken(Context context) {
        return getPrefs(context).getString(KEY_DEVICE_TOKEN, null);
    }

    public static void setDeviceToken(Context context, String token) {
        getPrefs(context).edit().putString(KEY_DEVICE_TOKEN, token).apply();
    }

    public static String getApiUrl(Context context) {
        String saved = getPrefs(context).getString(KEY_API_URL, null);
        if (saved != null && !saved.trim().isEmpty()) {
            String trimmed = saved.trim();
            // Automatically clean up old inactive legacy Render URL, or localhost/emulator IP on a physical device
            if ("https://zez-backend.onrender.com".equalsIgnoreCase(trimmed)
                    || ((trimmed.contains("10.0.2.2") || trimmed.contains("localhost")) && !isEmulator())) {
                setApiUrl(context, DEFAULT_API_URL);
                return DEFAULT_API_URL;
            }
            return trimmed;
        }
        return DEFAULT_API_URL;
    }

    private static String getAssetConfigUrl(Context context) {
        try (java.io.InputStream is = context.getAssets().open("backend_config.properties")) {
            java.util.Properties props = new java.util.Properties();
            props.load(is);
            return props.getProperty("BACKEND_URL");
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void setApiUrl(Context context, String url) {
        if (url != null) {
            url = url.trim();
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
        }
        getPrefs(context).edit().putString(KEY_API_URL, url).apply();
    }

    public static void resetApiUrl(Context context) {
        setApiUrl(context, DEFAULT_API_URL);
    }

    public static long getLastSyncTime(Context context) {
        return getPrefs(context).getLong(KEY_LAST_SYNC, 0);
    }

    public static void setLastSyncTime(Context context, long time) {
        getPrefs(context).edit().putLong(KEY_LAST_SYNC, time).apply();
    }

    public static int getThemeMode(Context context) {
        return getPrefs(context).getInt(KEY_THEME_MODE, THEME_SYSTEM);
    }

    public static void setThemeMode(Context context, int mode) {
        getPrefs(context).edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public static float getFontScale(Context context) {
        return getPrefs(context).getFloat(KEY_FONT_SCALE, FONT_SCALE_DEFAULT);
    }

    public static void setFontScale(Context context, float scale) {
        getPrefs(context).edit().putFloat(KEY_FONT_SCALE, scale).apply();
    }

    public static String getAccentColor(Context context) {
        return getPrefs(context).getString(KEY_ACCENT_COLOR, DEFAULT_ACCENT_COLOR);
    }

    public static int getAccentColorInt(Context context) {
        String hex = getAccentColor(context);
        try {
            return android.graphics.Color.parseColor(hex);
        } catch (Exception e) {
            return android.graphics.Color.parseColor(DEFAULT_ACCENT_COLOR);
        }
    }

    public static void setAccentColor(Context context, String colorHex) {
        if (colorHex != null) {
            getPrefs(context).edit().putString(KEY_ACCENT_COLOR, colorHex).apply();
        }
    }
}
