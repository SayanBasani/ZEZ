package com.zez.app.util;

import android.content.Context;
import android.content.SharedPreferences;

public class ChatPreferencesHelper {
    private static final String PREF_NAME = "zez_chat_prefs";
    private static final String KEY_USER_ID = "chat_user_id";
    private static final String KEY_PHONE = "chat_user_phone";
    private static final String KEY_NAME = "chat_user_name";
    private static final String KEY_AVATAR = "chat_user_avatar";
    private static final String KEY_STATUS = "chat_status_message";
    private static final String KEY_REGISTERED = "is_chat_registered";
    private static final String KEY_CHAT_THEME = "chat_wallpaper_theme";
    private static final String KEY_WALLPAPER_DIM = "chat_wallpaper_dim";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isChatRegistered(Context context) {
        return getPrefs(context).getBoolean(KEY_REGISTERED, false) && getUserId(context) != null;
    }

    public static void saveChatUser(Context context, String userId, String phone, String name, String avatar, String status) {
        getPrefs(context).edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_PHONE, phone)
                .putString(KEY_NAME, name)
                .putString(KEY_AVATAR, avatar)
                .putString(KEY_STATUS, status)
                .putBoolean(KEY_REGISTERED, true)
                .apply();
    }

    public static String getUserId(Context context) {
        return getPrefs(context).getString(KEY_USER_ID, null);
    }

    public static String getUserPhone(Context context) {
        return getPrefs(context).getString(KEY_PHONE, "");
    }

    public static String getUserName(Context context) {
        return getPrefs(context).getString(KEY_NAME, "");
    }

    public static String getUserAvatar(Context context) {
        return getPrefs(context).getString(KEY_AVATAR, null);
    }

    public static String getStatusMessage(Context context) {
        return getPrefs(context).getString(KEY_STATUS, "Hey there! I am using Zez.");
    }

    public static void clearChatUser(Context context) {
        getPrefs(context).edit().clear().apply();
    }

    public static void saveUserAvatar(Context context, String avatar) {
        getPrefs(context).edit().putString(KEY_AVATAR, avatar).apply();
    }

    public static void saveUserName(Context context, String name) {
        getPrefs(context).edit().putString(KEY_NAME, name).apply();
    }

    public static void saveStatusMessage(Context context, String status) {
        getPrefs(context).edit().putString(KEY_STATUS, status).apply();
    }

    public static String getChatTheme(Context context) {
        return getPrefs(context).getString(KEY_CHAT_THEME, "default");
    }

    public static void saveChatTheme(Context context, String theme) {
        getPrefs(context).edit().putString(KEY_CHAT_THEME, theme).apply();
    }

    public static float getWallpaperDim(Context context) {
        return getPrefs(context).getFloat(KEY_WALLPAPER_DIM, 0.0f);
    }

    public static void setWallpaperDim(Context context, float dim) {
        getPrefs(context).edit().putFloat(KEY_WALLPAPER_DIM, Math.max(0.0f, Math.min(0.85f, dim))).apply();
    }

    // Pinned Conversations
    private static final String KEY_PINNED_CONVERSATIONS = "pinned_conversations_set";

    public static java.util.Set<String> getPinnedConversations(Context context) {
        if (context == null) return new java.util.HashSet<>();
        return new java.util.HashSet<>(getPrefs(context).getStringSet(KEY_PINNED_CONVERSATIONS, new java.util.HashSet<>()));
    }

    public static boolean isConversationPinned(Context context, String conversationId) {
        if (context == null || conversationId == null) return false;
        return getPinnedConversations(context).contains(conversationId);
    }

    public static void setConversationPinned(Context context, String conversationId, boolean pinned) {
        if (context == null || conversationId == null) return;
        java.util.Set<String> current = getPinnedConversations(context);
        if (pinned) {
            current.add(conversationId);
        } else {
            current.remove(conversationId);
        }
        getPrefs(context).edit().putStringSet(KEY_PINNED_CONVERSATIONS, current).apply();
    }

    // Muted Conversations: stored as key = "mute_" + convId, value = expiration timestamp ms (-1 for always)
    public static void muteConversation(Context context, String conversationId, long durationMs) {
        if (context == null || conversationId == null) return;
        long until = durationMs <= 0 ? 0 : (durationMs == Long.MAX_VALUE ? -1 : System.currentTimeMillis() + durationMs);
        getPrefs(context).edit().putLong("mute_" + conversationId, until).apply();
    }

    public static boolean isConversationMuted(Context context, String conversationId) {
        if (context == null || conversationId == null) return false;
        long until = getPrefs(context).getLong("mute_" + conversationId, 0);
        if (until == 0) return false;
        if (until == -1) return true; // Always
        return System.currentTimeMillis() < until;
    }

    public static String getMuteStatusText(Context context, String conversationId) {
        if (context == null || conversationId == null) return "Off";
        long until = getPrefs(context).getLong("mute_" + conversationId, 0);
        if (until == 0) return "Off";
        if (until == -1) return "Always";
        if (System.currentTimeMillis() >= until) return "Off";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, h:mm a", java.util.Locale.getDefault());
        return "Until " + sdf.format(new java.util.Date(until));
    }

    // Media Visibility: 0 = Default (Yes), 1 = Yes, 2 = No
    public static int getMediaVisibility(Context context, String conversationId) {
        if (context == null || conversationId == null) return 0;
        return getPrefs(context).getInt("media_vis_" + conversationId, 0);
    }

    public static void setMediaVisibility(Context context, String conversationId, int visibility) {
        if (context == null || conversationId == null) return;
        getPrefs(context).edit().putInt("media_vis_" + conversationId, visibility).apply();
    }
}
