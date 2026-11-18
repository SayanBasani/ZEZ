package com.zez.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecycleBinHelper {
    private static final String PREF_NAME = "zez_recycle_bin";
    private static final String KEY_DELETED_ITEMS = "deleted_media_ids";

    public static class RecycledItem {
        public long mediaId;
        public String uriString;
        public String name;
        public long deletedTime;

        public RecycledItem(long mediaId, String uriString, String name, long deletedTime) {
            this.mediaId = mediaId;
            this.uriString = uriString;
            this.name = name;
            this.deletedTime = deletedTime;
        }
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static List<RecycledItem> getRecycledItems(Context context) {
        String json = getPrefs(context).getString(KEY_DELETED_ITEMS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<RecycledItem>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void addToRecycleBin(Context context, long mediaId, String uriString, String name) {
        List<RecycledItem> list = getRecycledItems(context);
        list.add(new RecycledItem(mediaId, uriString, name, System.currentTimeMillis()));
        saveList(context, list);
    }

    public static void restoreItem(Context context, long mediaId) {
        List<RecycledItem> list = getRecycledItems(context);
        list.removeIf(item -> item.mediaId == mediaId);
        saveList(context, list);
    }

    public static boolean isRecycled(Context context, long mediaId) {
        List<RecycledItem> list = getRecycledItems(context);
        for (RecycledItem item : list) {
            if (item.mediaId == mediaId) return true;
        }
        return false;
    }

    private static void saveList(Context context, List<RecycledItem> list) {
        String json = new Gson().toJson(list);
        getPrefs(context).edit().putString(KEY_DELETED_ITEMS, json).apply();
    }
}
