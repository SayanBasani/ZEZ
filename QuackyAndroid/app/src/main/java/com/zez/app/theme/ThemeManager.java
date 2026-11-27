package com.zez.app.theme;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;
import com.zez.app.R;
import com.zez.app.util.PreferencesHelper;

public final class ThemeManager {

    private ThemeManager() {}

    /**
     * Called once in Application.onCreate() to setup initial night mode & dynamic colors
     */
    public static void init(Application application) {
        int mode = PreferencesHelper.getThemeMode(application);
        AppCompatDelegate.setDefaultNightMode(mode);

        if (PreferencesHelper.isDynamicColorEnabled(application) && DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(application);
        }
    }

    /**
     * Applies the user selected palette theme to an Activity before setContentView.
     */
    public static void applyTheme(Activity activity) {
        applyTheme(activity, false);
    }

    /**
     * Applies the user selected palette theme to an Activity before setContentView.
     * @param isFullscreen Whether the activity is a fullscreen window (e.g. CallActivity, ImageViewerActivity)
     */
    public static void applyTheme(Activity activity, boolean isFullscreen) {
        int themeResId = getThemeResId(activity, isFullscreen);
        activity.setTheme(themeResId);

        if (!isFullscreen) {
            int fontOverlay = getFontOverlayResId(activity);
            if (fontOverlay != 0) {
                activity.getTheme().applyStyle(fontOverlay, true);
            }
        }

        if (PreferencesHelper.isDynamicColorEnabled(activity) && DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyIfAvailable(activity);
        }

        if (activity.getWindow() != null) {
            int bg = resolveThemeColor(activity, R.attr.colorThemeBackground);
            activity.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(bg));

            if (!isFullscreen) {
                int surface = resolveThemeColor(activity, R.attr.colorThemeSurface);
                activity.getWindow().setStatusBarColor(surface);

                boolean isDark = isDarkMode(activity);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    android.view.View decor = activity.getWindow().getDecorView();
                    int flags = decor.getSystemUiVisibility();
                    if (isDark) {
                        flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    } else {
                        flags |= android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    }
                    decor.setSystemUiVisibility(flags);
                }
            }
        }
    }

    /**
     * Returns the style overlay for the user's selected font family.
     */
    public static int getFontOverlayResId(Context context) {
        String family = PreferencesHelper.getFontFamily(context);
        if (family == null) return R.style.ThemeOverlay_Zez_Font_Sans;
        switch (family) {
            case PreferencesHelper.FONT_FAMILY_CONDENSED:
                return R.style.ThemeOverlay_Zez_Font_Condensed;
            case PreferencesHelper.FONT_FAMILY_MEDIUM:
                return R.style.ThemeOverlay_Zez_Font_Medium;
            case PreferencesHelper.FONT_FAMILY_SERIF:
                return R.style.ThemeOverlay_Zez_Font_Serif;
            case PreferencesHelper.FONT_FAMILY_MONOSPACE:
                return R.style.ThemeOverlay_Zez_Font_Monospace;
            case PreferencesHelper.FONT_FAMILY_CASUAL:
                return R.style.ThemeOverlay_Zez_Font_Casual;
            case PreferencesHelper.FONT_FAMILY_CURSIVE:
                return R.style.ThemeOverlay_Zez_Font_Cursive;
            case PreferencesHelper.FONT_FAMILY_SANS:
            default:
                return R.style.ThemeOverlay_Zez_Font_Sans;
        }
    }

    /**
     * Resolves an attribute color dynamically from the current theme.
     */
    public static int resolveThemeColor(Context context, int attrResId) {
        if (context == null) return android.graphics.Color.parseColor("#22C55E");
        android.util.TypedValue typedValue = new android.util.TypedValue();
        if (context.getTheme() != null && context.getTheme().resolveAttribute(attrResId, typedValue, true)) {
            return typedValue.data;
        }
        return androidx.core.content.ContextCompat.getColor(context, R.color.primary);
    }

    /**
     * Gets the primary accent color of the currently selected theme.
     */
    public static int getPrimaryColor(Context context) {
        return resolveThemeColor(context, R.attr.colorThemePrimary);
    }

    /**
     * Gets a subtle semi-transparent primary highlight color for selection rows.
     */
    public static int getSelectionHighlightColor(Context context) {
        int primary = getPrimaryColor(context);
        return android.graphics.Color.argb(0x2E, android.graphics.Color.red(primary), android.graphics.Color.green(primary), android.graphics.Color.blue(primary));
    }

    /**
     * Resolves the theme resource ID based on user preferences.
     */
    public static int getThemeResId(Context context, boolean isFullscreen) {
        if (isFullscreen) {
            return R.style.Theme_Zez_Fullscreen;
        }

        String palette = PreferencesHelper.getThemePalette(context);
        if (palette == null) {
            palette = PreferencesHelper.PALETTE_GRAPHITE;
        }

        switch (palette) {
            case PreferencesHelper.PALETTE_ARCTIC:
                return R.style.Theme_Zez_Arctic;
            case PreferencesHelper.PALETTE_MIDNIGHT:
                return R.style.Theme_Zez_Midnight;
            case PreferencesHelper.PALETTE_AMOLED:
                return R.style.Theme_Zez_Amoled;
            case PreferencesHelper.PALETTE_OCEAN:
                return R.style.Theme_Zez_Ocean;
            case PreferencesHelper.PALETTE_SAGE:
                return R.style.Theme_Zez_Sage;
            case PreferencesHelper.PALETTE_SUNSET:
                return R.style.Theme_Zez_Sunset;
            case PreferencesHelper.PALETTE_ROSE:
                return R.style.Theme_Zez_Rose;
            case PreferencesHelper.PALETTE_GRAPHITE:
            default:
                return R.style.Theme_Zez_Graphite;
        }
    }

    /**
     * Changes appearance mode (System Default, Light, Dark).
     */
    public static void setAppearanceMode(Activity activity, int mode) {
        PreferencesHelper.setThemeMode(activity, mode);
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    /**
     * Changes color palette and recreates activity smoothly.
     */
    public static void setPalette(Activity activity, String palette) {
        PreferencesHelper.setThemePalette(activity, palette);
        activity.recreate();
    }

    /**
     * Toggles Dynamic Colors (Material You) on Android 12+.
     */
    public static void setDynamicColors(Activity activity, boolean enabled) {
        PreferencesHelper.setDynamicColorEnabled(activity, enabled);
        activity.recreate();
    }

    /**
     * Resets appearance to System Default, Graphite palette, Dynamic Colors Off.
     */
    public static void resetAppearance(Activity activity) {
        PreferencesHelper.resetAppearance(activity);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        activity.recreate();
    }

    /**
     * Checks if current context is currently rendering dark mode.
     */
    public static boolean isDarkMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Checks if dynamic color is supported on this Android OS version (Android 12+ / API 31+).
     */
    public static boolean isDynamicColorSupported() {
        return DynamicColors.isDynamicColorAvailable();
    }
}
