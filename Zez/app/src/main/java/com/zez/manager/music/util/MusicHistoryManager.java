package com.zez.manager.music.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zez.manager.music.model.MusicTrack;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages search history (last 10 queries) and playback history (recently played tracks).
 */
public class MusicHistoryManager {
    private static final String PREF_NAME = "zez_music_history";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final String KEY_PLAYBACK_HISTORY = "playback_history";
    private static final int MAX_SEARCH_HISTORY = 10;
    private static final int MAX_PLAYBACK_HISTORY = 50;
    private static final Gson gson = new Gson();

    public static List<String> getSearchHistory(Context context) {
        if (context == null) return new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_SEARCH_HISTORY, null);
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void addSearchQuery(Context context, String query) {
        if (context == null || query == null || query.trim().isEmpty()) return;
        String clean = query.trim();
        List<String> list = getSearchHistory(context);
        list.remove(clean);
        list.add(0, clean);
        if (list.size() > MAX_SEARCH_HISTORY) {
            list = list.subList(0, MAX_SEARCH_HISTORY);
        }
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_SEARCH_HISTORY, gson.toJson(list)).apply();
    }

    public static void clearSearchHistory(Context context) {
        if (context == null) return;
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_SEARCH_HISTORY).apply();
    }

    public static List<MusicTrack> getPlaybackHistory(Context context) {
        if (context == null) return new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_PLAYBACK_HISTORY, null);
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            Type type = new TypeToken<List<MusicTrack>>() {}.getType();
            List<MusicTrack> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void addTrackToPlaybackHistory(Context context, MusicTrack track) {
        if (context == null || track == null || track.getId() == null) return;
        List<MusicTrack> list = getPlaybackHistory(context);
        list.remove(track);
        list.add(0, track);
        if (list.size() > MAX_PLAYBACK_HISTORY) {
            list = list.subList(0, MAX_PLAYBACK_HISTORY);
        }
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_PLAYBACK_HISTORY, gson.toJson(list)).apply();
    }

    public static void clearPlaybackHistory(Context context) {
        if (context == null) return;
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_PLAYBACK_HISTORY).apply();
    }
}
