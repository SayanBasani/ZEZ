package com.zez.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class FavoritesHelper {
    private static final String PREF_NAME = "zez_favorites_prefs";
    private static final String KEY_FAVORITES = "favorite_media_ids";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized Set<String> getFavoriteIds(Context context) {
        Set<String> set = getPrefs(context).getStringSet(KEY_FAVORITES, null);
        return set != null ? new HashSet<>(set) : new HashSet<>();
    }

    public static synchronized boolean isFavorite(Context context, long mediaId) {
        Set<String> set = getPrefs(context).getStringSet(KEY_FAVORITES, null);
        return set != null && set.contains(String.valueOf(mediaId));
    }

    public static synchronized void addFavorite(Context context, long mediaId) {
        Set<String> set = getFavoriteIds(context);
        set.add(String.valueOf(mediaId));
        getPrefs(context).edit().putStringSet(KEY_FAVORITES, set).apply();
    }

    public static synchronized void removeFavorite(Context context, long mediaId) {
        Set<String> set = getFavoriteIds(context);
        set.remove(String.valueOf(mediaId));
        getPrefs(context).edit().putStringSet(KEY_FAVORITES, set).apply();
    }

    public static synchronized boolean toggleFavorite(Context context, long mediaId) {
        Set<String> set = getFavoriteIds(context);
        String idStr = String.valueOf(mediaId);
        boolean isFav;
        if (set.contains(idStr)) {
            set.remove(idStr);
            isFav = false;
        } else {
            set.add(idStr);
            isFav = true;
        }
        getPrefs(context).edit().putStringSet(KEY_FAVORITES, set).apply();
        return isFav;
    }

    public static synchronized int getFavoritesCount(Context context) {
        Set<String> set = getPrefs(context).getStringSet(KEY_FAVORITES, null);
        return set != null ? set.size() : 0;
    }
}
