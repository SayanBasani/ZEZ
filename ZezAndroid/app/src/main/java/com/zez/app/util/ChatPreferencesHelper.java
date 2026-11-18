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
}
