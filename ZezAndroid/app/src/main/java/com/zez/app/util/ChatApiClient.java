package com.zez.app.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zez.app.model.ChatConversation;
import com.zez.app.model.ChatMessage;
import com.zez.app.model.ChatUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.net.Uri;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatApiClient {
    private static final String TAG = "ChatApiClient";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final Gson gson = new com.google.gson.GsonBuilder()
            .setFieldNamingPolicy(com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private static final Dispatcher dispatcher;
    static {
        dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(64);
        dispatcher.setMaxRequestsPerHost(24);
    }

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectionPool(new ConnectionPool(12, 5, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    public static OkHttpClient getOkHttpClient() {
        return client;
    }

    public interface ChatCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    public static String getBaseUrl(Context context) {
        String base = PreferencesHelper.getApiUrl(context);
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    public static String getAbsoluteMediaUrl(Context context, String relativeOrAbsoluteUrl) {
        if (relativeOrAbsoluteUrl == null || relativeOrAbsoluteUrl.trim().isEmpty()) return null;
        String trimmed = relativeOrAbsoluteUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("content://") || trimmed.startsWith("file://")) {
            return trimmed;
        }
        String base = getBaseUrl(context);
        return base + (trimmed.startsWith("/") ? "" : "/") + trimmed;
    }

    public static class MediaUploadResult {
        public String url;
        public String fileName;
        public long fileSize;
        public String mediaType;
    }

    public static void updateProfile(Context context, String userId, String name, String avatar, String status, ChatCallback<ChatUser> callback) {
        String url = getBaseUrl(context) + "/api/chat/profile";
        JsonObject body = new JsonObject();
        body.addProperty("id", userId);
        if (name != null) body.addProperty("name", name);
        if (avatar != null) body.addProperty("avatar_url", avatar);
        if (status != null) body.addProperty("status_message", status);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Update profile failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    postError(callback, "Update profile HTTP " + response.code());
                    return;
                }
                try {
                    String str = response.body().string();
                    JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                    if (json.has("success") && json.get("success").getAsBoolean() && json.has("user")) {
                        ChatUser user = gson.fromJson(json.get("user"), ChatUser.class);
                        postSuccess(callback, user);
                    } else {
                        postError(callback, json.has("error") ? json.get("error").getAsString() : "Update failed.");
                    }
                } catch (Exception e) {
                    postError(callback, "Error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Deletes the chat account and associated data from the backend (Google Play policy compliance).
     */
    public static void deleteAccount(Context context, String userId, ChatCallback<Boolean> callback) {
        if (userId == null || userId.trim().isEmpty()) {
            postError(callback, "User ID is required.");
            return;
        }
        String url = getBaseUrl(context) + "/api/chat/account/delete";
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Failed to connect to server: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    postError(callback, "Server returned HTTP " + response.code());
                    return;
                }
                try {
                    String str = response.body().string();
                    JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                    if (json.has("success") && json.get("success").getAsBoolean()) {
                        postSuccess(callback, true);
                    } else {
                        postError(callback, json.has("error") ? json.get("error").getAsString() : "Deletion failed.");
                    }
                } catch (Exception e) {
                    postError(callback, "Response error: " + e.getMessage());
                }
            }
        });
    }

    public static void getUserProfile(Context context, String idOrPhone, ChatCallback<ChatUser> callback) {
        if (idOrPhone == null || idOrPhone.trim().isEmpty()) {
            postError(callback, "idOrPhone required");
            return;
        }
        String url = getBaseUrl(context) + "/api/chat/profile/" + Uri.encode(idOrPhone.trim());
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Profile fetch failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    postError(callback, "Profile fetch HTTP " + response.code());
                    return;
                }
                try {
                    String str = response.body().string();
                    JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                    if (json.has("success") && json.get("success").getAsBoolean() && json.has("user")) {
                        ChatUser user = gson.fromJson(json.get("user"), ChatUser.class);
                        postSuccess(callback, user);
                    } else {
                        postError(callback, json.has("error") ? json.get("error").getAsString() : "Profile not found.");
                    }
                } catch (Exception e) {
                    postError(callback, "Parse error: " + e.getMessage());
                }
            }
        });
    }

    public static void uploadChatMedia(Context context, Uri fileUri, String fileName, String mimeType, ChatCallback<MediaUploadResult> callback) {
        new Thread(() -> {
            try {
                InputStream is = context.getContentResolver().openInputStream(fileUri);
                if (is == null) {
                    postError(callback, "Cannot open selected file");
                    return;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                is.close();
                byte[] fileBytes = baos.toByteArray();

                String safeName = fileName != null ? fileName : "file_" + System.currentTimeMillis();
                MediaType okMediaType = MediaType.parse(mimeType != null ? mimeType : "application/octet-stream");
                RequestBody fileBody = RequestBody.create(fileBytes, okMediaType);

                MultipartBody multipartBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", safeName, fileBody)
                        .build();

                String url = getBaseUrl(context) + "/api/chat/upload";
                Request request = new Request.Builder()
                        .url(url)
                        .post(multipartBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        postError(callback, "Upload failed: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful() || response.body() == null) {
                            postError(callback, "Upload HTTP " + response.code());
                            return;
                        }
                        try {
                            String str = response.body().string();
                            JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                            if (json.has("success") && json.get("success").getAsBoolean() && json.has("url")) {
                                MediaUploadResult res = new MediaUploadResult();
                                res.url = json.get("url").getAsString();
                                if (json.has("fileName")) res.fileName = json.get("fileName").getAsString();
                                if (json.has("fileSize")) res.fileSize = json.get("fileSize").getAsLong();
                                if (json.has("mediaType")) res.mediaType = json.get("mediaType").getAsString();
                                postSuccess(callback, res);
                            } else {
                                postError(callback, json.has("error") ? json.get("error").getAsString() : "Upload failed");
                            }
                        } catch (Exception e) {
                            postError(callback, "Parse error: " + e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                postError(callback, "File read error: " + e.getMessage());
            }
        }).start();
    }

    public static void uploadAvatar(Context context, Uri imageUri, String userId, ChatCallback<String> callback) {
        new Thread(() -> {
            try {
                InputStream is = context.getContentResolver().openInputStream(imageUri);
                if (is == null) {
                    postError(callback, "Cannot open image");
                    return;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                is.close();
                byte[] fileBytes = baos.toByteArray();

                RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse("image/jpeg"));
                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("avatar", "avatar_" + System.currentTimeMillis() + ".jpg", fileBody);
                if (userId != null) {
                    builder.addFormDataPart("userId", userId);
                }

                String url = getBaseUrl(context) + "/api/chat/upload-avatar";
                Request request = new Request.Builder()
                        .url(url)
                        .post(builder.build())
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        postError(callback, "Avatar upload failed: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful() || response.body() == null) {
                            postError(callback, "Avatar upload HTTP " + response.code());
                            return;
                        }
                        try {
                            String str = response.body().string();
                            JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                            if (json.has("success") && json.get("success").getAsBoolean() && json.has("avatar_url")) {
                                postSuccess(callback, json.get("avatar_url").getAsString());
                            } else {
                                postError(callback, json.has("error") ? json.get("error").getAsString() : "Avatar upload failed");
                            }
                        } catch (Exception e) {
                            postError(callback, "Parse error: " + e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                postError(callback, "Avatar read error: " + e.getMessage());
            }
        }).start();
    }

    public static void resolveDirectContact(Context context, String phone, String name, ChatCallback<ChatUser> callback) {
        String url = getBaseUrl(context) + "/api/chat/direct-contact";
        JsonObject body = new JsonObject();
        body.addProperty("phone_number", phone);
        if (name != null) body.addProperty("name", name);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Failed to resolve contact: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    postError(callback, "HTTP " + response.code());
                    return;
                }
                try {
                    String str = response.body().string();
                    JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                    if (json.has("success") && json.get("success").getAsBoolean() && json.has("user")) {
                        ChatUser user = gson.fromJson(json.get("user"), ChatUser.class);
                        postSuccess(callback, user);
                    } else {
                        postError(callback, json.has("error") ? json.get("error").getAsString() : "Failed to resolve contact");
                    }
                } catch (Exception e) {
                    postError(callback, "Parse error: " + e.getMessage());
                }
            }
        });
    }

    // 1. Register or update chat profile
    public static void registerUser(Context context, String phone, String name, String avatar, String status, ChatCallback<ChatUser> callback) {
        String url = getBaseUrl(context) + "/api/chat/register";
        JsonObject body = new JsonObject();
        body.addProperty("phone_number", phone);
        body.addProperty("name", name);
        if (avatar != null) body.addProperty("avatar_url", avatar);
        if (status != null) body.addProperty("status_message", status);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    postError(callback, "Registration failed with HTTP " + response.code());
                    return;
                }
                try {
                    String str = response.body().string();
                    JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                    if (json.has("success") && json.get("success").getAsBoolean() && json.has("user")) {
                        ChatUser user = gson.fromJson(json.get("user"), ChatUser.class);
                        postSuccess(callback, user);
                    } else {
                        postError(callback, json.has("error") ? json.get("error").getAsString() : "Registration failed.");
                    }
                } catch (Exception e) {
                    postError(callback, "Error parsing response: " + e.getMessage());
                }
            }
        });
    }

    // 2. Sync Contacts
    public static class ContactSyncResult {
        public List<ChatUser> registered = new ArrayList<>();
        public List<String> notRegistered = new ArrayList<>();
    }

    public static void syncContacts(Context context, String currentUserId, List<String> phoneNumbers, ChatCallback<ContactSyncResult> callback) {
        String url = getBaseUrl(context) + "/api/chat/sync-contacts";
        JsonObject body = new JsonObject();
        if (currentUserId != null) body.addProperty("current_user_id", currentUserId);

        JsonArray phonesArr = new JsonArray();
        for (String p : phoneNumbers) {
            phonesArr.add(p);
        }
        body.add("phone_numbers", phonesArr);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Sync failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    postError(callback, "Sync contacts HTTP " + response.code());
                    return;
                }
                try {
                    String str = response.body().string();
                    JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                    ContactSyncResult result = new ContactSyncResult();
                    if (json.has("registered")) {
                        JsonArray regArr = json.getAsJsonArray("registered");
                        for (int i = 0; i < regArr.size(); i++) {
                            result.registered.add(gson.fromJson(regArr.get(i), ChatUser.class));
                        }
                    }
                    if (json.has("not_registered")) {
                        JsonArray notRegArr = json.getAsJsonArray("not_registered");
                        for (int i = 0; i < notRegArr.size(); i++) {
                            result.notRegistered.add(notRegArr.get(i).getAsString());
                        }
                    }
                    postSuccess(callback, result);
                } catch (Exception e) {
                    postError(callback, "Failed to parse contact sync: " + e.getMessage());
                }
            }
        });
    }

    // 3. Get Conversations for User
    public static void getConversations(Context context, String userId, ChatCallback<List<ChatConversation>> callback) {
        String url = getBaseUrl(context) + "/api/chat/conversations?userId=" + userId;
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Failed to load chats: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    postError(callback, "HTTP " + response.code());
                    return;
                }
                try {
                    String str = response.body().string();
                    JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                    List<ChatConversation> list = new ArrayList<>();
                    if (json.has("conversations")) {
                        JsonArray arr = json.getAsJsonArray("conversations");
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            ChatConversation conv = new ChatConversation();
                            if (obj.has("conversation_id")) conv.setConversationId(obj.get("conversation_id").getAsString());
                            if (obj.has("other_user_id")) conv.setOtherUserId(obj.get("other_user_id").getAsString());
                            if (obj.has("other_user_name")) conv.setOtherUserName(obj.get("other_user_name").getAsString());
                            if (obj.has("other_user_phone")) conv.setOtherUserPhone(obj.get("other_user_phone").getAsString());
                            if (obj.has("other_user_avatar") && !obj.get("other_user_avatar").isJsonNull()) conv.setOtherUserAvatar(obj.get("other_user_avatar").getAsString());
                            if (obj.has("other_user_last_seen") && !obj.get("other_user_last_seen").isJsonNull()) conv.setOtherUserLastSeen(obj.get("other_user_last_seen").getAsString());
                            if (obj.has("last_message_text") && !obj.get("last_message_text").isJsonNull()) conv.setLastMessageText(obj.get("last_message_text").getAsString());
                            if (obj.has("last_message_time") && !obj.get("last_message_time").isJsonNull()) conv.setLastMessageTime(obj.get("last_message_time").getAsString());
                            if (obj.has("unread_count")) conv.setUnreadCount(obj.get("unread_count").getAsInt());
                            list.add(conv);
                        }
                    }
                    postSuccess(callback, list);
                } catch (Exception e) {
                    postError(callback, "Parse error: " + e.getMessage());
                }
            }
        });
    }

    public static List<ChatConversation> getConversationsSync(Context context, String userId) {
        if (context == null || userId == null) return new ArrayList<>();
        String url = getBaseUrl(context) + "/api/chat/conversations?userId=" + Uri.encode(userId);
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return new ArrayList<>();
            }
            String str = response.body().string();
            JsonObject json = JsonParser.parseString(str).getAsJsonObject();
            List<ChatConversation> list = new ArrayList<>();
            if (json.has("conversations")) {
                JsonArray arr = json.getAsJsonArray("conversations");
                for (int i = 0; i < arr.size(); i++) {
                    JsonObject obj = arr.get(i).getAsJsonObject();
                    ChatConversation conv = new ChatConversation();
                    if (obj.has("conversation_id")) conv.setConversationId(obj.get("conversation_id").getAsString());
                    if (obj.has("other_user_id")) conv.setOtherUserId(obj.get("other_user_id").getAsString());
                    if (obj.has("other_user_name")) conv.setOtherUserName(obj.get("other_user_name").getAsString());
                    if (obj.has("other_user_phone")) conv.setOtherUserPhone(obj.get("other_user_phone").getAsString());
                    if (obj.has("other_user_avatar") && !obj.get("other_user_avatar").isJsonNull()) conv.setOtherUserAvatar(obj.get("other_user_avatar").getAsString());
                    if (obj.has("other_user_last_seen") && !obj.get("other_user_last_seen").isJsonNull()) conv.setOtherUserLastSeen(obj.get("other_user_last_seen").getAsString());
                    if (obj.has("last_message_text") && !obj.get("last_message_text").isJsonNull()) conv.setLastMessageText(obj.get("last_message_text").getAsString());
                    if (obj.has("last_message_time") && !obj.get("last_message_time").isJsonNull()) conv.setLastMessageTime(obj.get("last_message_time").getAsString());
                    if (obj.has("unread_count")) conv.setUnreadCount(obj.get("unread_count").getAsInt());
                    list.add(conv);
                }
            }
            return list;
        } catch (Exception e) {
            Log.w(TAG, "getConversationsSync error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static class ConversationModeInfo {
        public String conversationId;
        public boolean isSecretMode;
        public String secretSessionId;
        public String secretModeUpdatedAt;
        public String secretModeBy;
    }

    public static class PeerStatus {
        public String userId;
        public boolean isOnline;
        public boolean isTyping;
        public String lastSeen;
        public String avatarUrl;
        public String name;
    }

    public static class MessagesResult {
        public String conversationId = null;
        public String timestamp = null;
        public List<ChatMessage> messages = new ArrayList<>();
        public boolean isSecretMode = false;
        public String secretSessionId = null;
        public PeerStatus peerStatus = null;
    }

    public static class LiveUpdateResult {
        public boolean hasNew = false;
        public boolean timeout = false;
        public List<ChatMessage> messages = new ArrayList<>();
    }

    public static LiveUpdateResult listenForLiveUpdatesSync(Context context, String userId) {
        return listenForLiveUpdatesSync(context, userId, null);
    }

    public static LiveUpdateResult listenForLiveUpdatesSync(Context context, String userId, String sinceTimestamp) {
        if (context == null || userId == null) return null;
        StringBuilder sb = new StringBuilder(getBaseUrl(context))
                .append("/api/chat/live-updates?userId=").append(Uri.encode(userId));
        if (sinceTimestamp != null && !sinceTimestamp.trim().isEmpty()) {
            sb.append("&since=").append(Uri.encode(sinceTimestamp.trim()));
        }
        Request request = new Request.Builder().url(sb.toString()).get().build();
        OkHttpClient longPollClient = client.newBuilder()
                .readTimeout(35, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();

        try (Response response = longPollClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            String str = response.body().string();
            JsonObject json = JsonParser.parseString(str).getAsJsonObject();
            LiveUpdateResult res = new LiveUpdateResult();
            res.hasNew = json.has("hasNew") && json.get("hasNew").getAsBoolean();
            res.timeout = json.has("timeout") && json.get("timeout").getAsBoolean();
            if (res.hasNew && json.has("messages")) {
                JsonArray arr = json.getAsJsonArray("messages");
                for (int i = 0; i < arr.size(); i++) {
                    ChatMessage msg = gson.fromJson(arr.get(i), ChatMessage.class);
                    if (msg != null) res.messages.add(msg);
                }
            }
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    // 3b. Mode management for Dual-Mode (Normal <-> Ephemeral Secret) chat
    public static void setConversationMode(Context context, String conversationId, String userId, boolean isSecretMode, ChatCallback<ConversationModeInfo> callback) {
        String url = getBaseUrl(context) + "/api/chat/conversation/" + conversationId + "/mode";
        JsonObject body = new JsonObject();
        body.addProperty("isSecretMode", isSecretMode);
        if (userId != null) body.addProperty("userId", userId);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Failed to toggle mode: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response resp = response) {
                    if (!resp.isSuccessful() || resp.body() == null) {
                        postError(callback, "HTTP " + resp.code());
                        return;
                    }
                    try {
                        String str = resp.body().string();
                        JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                        ConversationModeInfo info = new ConversationModeInfo();
                        if (json.has("conversationId")) info.conversationId = json.get("conversationId").getAsString();
                        if (json.has("isSecretMode")) info.isSecretMode = json.get("isSecretMode").getAsBoolean();
                        if (json.has("secretSessionId") && !json.get("secretSessionId").isJsonNull()) {
                            info.secretSessionId = json.get("secretSessionId").getAsString();
                        }
                        if (json.has("secretModeUpdatedAt") && !json.get("secretModeUpdatedAt").isJsonNull()) {
                            info.secretModeUpdatedAt = json.get("secretModeUpdatedAt").getAsString();
                        }
                        postSuccess(callback, info);
                    } catch (Exception e) {
                        postError(callback, "Parse error: " + e.getMessage());
                    }
                }
            }
        });
    }

    // 4. Get Messages (supporting dual mode filtering)
    public static void getMessages(Context context, String conversationId, String userId, String otherUserId, int limit, ChatCallback<List<ChatMessage>> callback) {
        getMessages(context, conversationId, userId, otherUserId, limit, "normal", null, new ChatCallback<MessagesResult>() {
            @Override public void onSuccess(MessagesResult result) { callback.onSuccess(result.messages); }
            @Override public void onError(String error) { callback.onError(error); }
        });
    }

    public static void getMessages(Context context, String conversationId, String userId, String otherUserId, int limit, String mode, String secretSessionId, ChatCallback<MessagesResult> callback) {
        StringBuilder sb = new StringBuilder(getBaseUrl(context)).append("/api/chat/messages?");
        if (conversationId != null) sb.append("conversationId=").append(conversationId).append("&");
        if (userId != null) sb.append("userId=").append(userId).append("&");
        if (otherUserId != null) sb.append("otherUserId=").append(otherUserId).append("&");
        if (mode != null) sb.append("mode=").append(mode).append("&");
        if (secretSessionId != null) sb.append("secretSessionId=").append(secretSessionId).append("&");
        sb.append("limit=").append(limit);

        Request request = new Request.Builder().url(sb.toString()).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Error fetching messages: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response resp = response) {
                    if (!resp.isSuccessful() || resp.body() == null) {
                        postError(callback, "HTTP " + resp.code());
                        return;
                    }
                    try {
                        String str = resp.body().string();
                        JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                        MessagesResult res = new MessagesResult();
                        if (json.has("conversation_id") && !json.get("conversation_id").isJsonNull()) {
                            res.conversationId = json.get("conversation_id").getAsString();
                        } else if (json.has("conversationId") && !json.get("conversationId").isJsonNull()) {
                            res.conversationId = json.get("conversationId").getAsString();
                        }
                        if (json.has("timestamp") && !json.get("timestamp").isJsonNull()) {
                            res.timestamp = json.get("timestamp").getAsString();
                        }
                        if (json.has("isSecretMode")) res.isSecretMode = json.get("isSecretMode").getAsBoolean();
                        if (json.has("secretSessionId") && !json.get("secretSessionId").isJsonNull()) {
                            res.secretSessionId = json.get("secretSessionId").getAsString();
                        }

                        if (json.has("peer_status") && !json.get("peer_status").isJsonNull()) {
                            JsonObject ps = json.getAsJsonObject("peer_status");
                            PeerStatus peerStatus = new PeerStatus();
                            if (ps.has("userId")) peerStatus.userId = ps.get("userId").getAsString();
                            if (ps.has("isOnline")) peerStatus.isOnline = ps.get("isOnline").getAsBoolean();
                            if (ps.has("isTyping")) peerStatus.isTyping = ps.get("isTyping").getAsBoolean();
                            if (ps.has("lastSeen") && !ps.get("lastSeen").isJsonNull()) peerStatus.lastSeen = ps.get("lastSeen").getAsString();
                            if (ps.has("avatarUrl") && !ps.get("avatarUrl").isJsonNull()) peerStatus.avatarUrl = ps.get("avatarUrl").getAsString();
                            if (ps.has("name") && !ps.get("name").isJsonNull()) peerStatus.name = ps.get("name").getAsString();
                            res.peerStatus = peerStatus;
                        }

                        if (json.has("messages")) {
                            JsonArray arr = json.getAsJsonArray("messages");
                            for (int i = 0; i < arr.size(); i++) {
                                JsonObject m = arr.get(i).getAsJsonObject();
                                ChatMessage msg = gson.fromJson(m, ChatMessage.class);
                                res.messages.add(msg);
                            }
                        }
                        postSuccess(callback, res);
                    } catch (Exception e) {
                        postError(callback, "Parse error: " + e.getMessage());
                    }
                }
            }
        });
    }

    // 5. Send Message (with optional media & mode)
    public static void sendMessage(Context context, String senderId, String receiverId, String text, String mediaUrl, ChatCallback<ChatMessage> callback) {
        sendMessage(context, senderId, receiverId, text, mediaUrl, null, null, null, "normal", null, callback);
    }

    public static void sendMessage(Context context, String senderId, String receiverId, String text, String mediaUrl, String mediaType, String fileName, Long fileSize, ChatCallback<ChatMessage> callback) {
        sendMessage(context, senderId, receiverId, text, mediaUrl, mediaType, fileName, fileSize, "normal", null, callback);
    }

    public static void sendMessage(Context context, String senderId, String receiverId, String text, String mediaUrl, String mediaType, String fileName, Long fileSize, String mode, String secretSessionId, ChatCallback<ChatMessage> callback) {
        String url = getBaseUrl(context) + "/api/chat/messages/send";
        JsonObject body = new JsonObject();
        body.addProperty("sender_id", senderId);
        body.addProperty("receiver_id", receiverId);
        if (text != null) body.addProperty("message_text", text);
        if (mediaUrl != null) body.addProperty("media_url", mediaUrl);
        if (mediaType != null) body.addProperty("media_type", mediaType);
        if (fileName != null) body.addProperty("file_name", fileName);
        if (fileSize != null) body.addProperty("file_size", fileSize);
        if (mode != null) body.addProperty("mode", mode);
        if (secretSessionId != null) body.addProperty("secret_session_id", secretSessionId);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Failed to send: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response resp = response) {
                    if (!resp.isSuccessful() || resp.body() == null) {
                        postError(callback, "Send error HTTP " + resp.code());
                        return;
                    }
                    try {
                        String str = resp.body().string();
                        JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                        if (json.has("success") && json.get("success").getAsBoolean() && json.has("message")) {
                            JsonObject m = json.getAsJsonObject("message");
                            ChatMessage msg = gson.fromJson(m, ChatMessage.class);
                            postSuccess(callback, msg);
                        } else {
                            postError(callback, "Failed to send message.");
                        }
                    } catch (Exception e) {
                        postError(callback, "Parse error: " + e.getMessage());
                    }
                }
            }
        });
    }

    public static void markMessagesAsRead(Context context, String conversationId, String currentUserId) {
        if (conversationId == null || currentUserId == null) return;
        String url = getBaseUrl(context) + "/api/chat/messages/status";
        JsonObject body = new JsonObject();
        body.addProperty("conversation_id", conversationId);
        body.addProperty("receiver_id", currentUserId);
        body.addProperty("status", "read");

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException { response.close(); }
        });
    }

    public static class IncomingCallInfo {
        public String id;
        public String callType;
        public String callerId;
        public String callerName;
        public String callerAvatar;
        public String callerPhone;
        public String startedAt;
    }

    // 6. Polling for Live Chat Updates
    public static class PollResult {
        public String timestamp;
        public List<ChatMessage> incoming = new ArrayList<>();
        public List<JsonObject> statusUpdates = new ArrayList<>();
        public ConversationModeInfo conversationMode;
        public PeerStatus peerStatus;
        public IncomingCallInfo incomingCall;
    }

    public static void pollUpdates(Context context, String userId, String sinceTimestamp, ChatCallback<PollResult> callback) {
        pollUpdates(context, userId, sinceTimestamp, null, callback);
    }

    public static void pollUpdates(Context context, String userId, String sinceTimestamp, String conversationId, ChatCallback<PollResult> callback) {
        StringBuilder sb = new StringBuilder(getBaseUrl(context)).append("/api/chat/poll?userId=").append(Uri.encode(userId));
        if (sinceTimestamp != null && !sinceTimestamp.isEmpty()) sb.append("&since=").append(Uri.encode(sinceTimestamp));
        if (conversationId != null && !conversationId.isEmpty()) sb.append("&conversationId=").append(Uri.encode(conversationId));

        Request request = new Request.Builder().url(sb.toString()).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response resp = response) {
                    if (!resp.isSuccessful() || resp.body() == null) {
                        postError(callback, "Poll HTTP " + resp.code());
                        return;
                    }
                try {
                    String str = response.body().string();
                    JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                    PollResult pr = new PollResult();
                    if (json.has("timestamp")) pr.timestamp = json.get("timestamp").getAsString();
                    if (json.has("incoming")) {
                        JsonArray arr = json.getAsJsonArray("incoming");
                        for (int i = 0; i < arr.size(); i++) {
                            pr.incoming.add(gson.fromJson(arr.get(i), ChatMessage.class));
                        }
                    }
                    if (json.has("status_updates")) {
                        JsonArray arr = json.getAsJsonArray("status_updates");
                        for (int i = 0; i < arr.size(); i++) {
                            pr.statusUpdates.add(arr.get(i).getAsJsonObject());
                        }
                    }
                    if (json.has("conversation_mode") && !json.get("conversation_mode").isJsonNull()) {
                        JsonObject cm = json.getAsJsonObject("conversation_mode");
                        ConversationModeInfo cmi = new ConversationModeInfo();
                        if (cm.has("conversationId")) cmi.conversationId = cm.get("conversationId").getAsString();
                        if (cm.has("isSecretMode")) cmi.isSecretMode = cm.get("isSecretMode").getAsBoolean();
                        if (cm.has("secretSessionId") && !cm.get("secretSessionId").isJsonNull()) {
                            cmi.secretSessionId = cm.get("secretSessionId").getAsString();
                        }
                        if (cm.has("secretModeUpdatedAt") && !cm.get("secretModeUpdatedAt").isJsonNull()) {
                            cmi.secretModeUpdatedAt = cm.get("secretModeUpdatedAt").getAsString();
                        }
                        pr.conversationMode = cmi;
                    }
                    if (json.has("peer_status") && !json.get("peer_status").isJsonNull()) {
                        JsonObject ps = json.getAsJsonObject("peer_status");
                        PeerStatus peerStatus = new PeerStatus();
                        if (ps.has("userId")) peerStatus.userId = ps.get("userId").getAsString();
                        if (ps.has("isOnline")) peerStatus.isOnline = ps.get("isOnline").getAsBoolean();
                        if (ps.has("isTyping")) peerStatus.isTyping = ps.get("isTyping").getAsBoolean();
                        if (ps.has("lastSeen") && !ps.get("lastSeen").isJsonNull()) peerStatus.lastSeen = ps.get("lastSeen").getAsString();
                        if (ps.has("avatarUrl") && !ps.get("avatarUrl").isJsonNull()) peerStatus.avatarUrl = ps.get("avatarUrl").getAsString();
                        if (ps.has("name") && !ps.get("name").isJsonNull()) peerStatus.name = ps.get("name").getAsString();
                        pr.peerStatus = peerStatus;
                    }
                    if (json.has("incoming_call") && !json.get("incoming_call").isJsonNull()) {
                        JsonObject ic = json.getAsJsonObject("incoming_call");
                        IncomingCallInfo info = new IncomingCallInfo();
                        if (ic.has("id")) info.id = ic.get("id").getAsString();
                        if (ic.has("callType")) info.callType = ic.get("callType").getAsString();
                        if (ic.has("callerId")) info.callerId = ic.get("callerId").getAsString();
                        if (ic.has("callerName")) info.callerName = ic.get("callerName").getAsString();
                        if (ic.has("callerAvatar") && !ic.get("callerAvatar").isJsonNull()) info.callerAvatar = ic.get("callerAvatar").getAsString();
                        if (ic.has("callerPhone") && !ic.get("callerPhone").isJsonNull()) info.callerPhone = ic.get("callerPhone").getAsString();
                        if (ic.has("startedAt") && !ic.get("startedAt").isJsonNull()) info.startedAt = ic.get("startedAt").getAsString();
                        pr.incomingCall = info;
                    }
                    postSuccess(callback, pr);
                } catch (Exception e) {
                    postError(callback, "Poll parse error: " + e.getMessage());
                }
                }
            }
        });
    }

    // 6b. Send typing indicator status
    public static void sendTypingStatus(Context context, String conversationId, String userId, boolean isTyping, ChatCallback<Void> callback) {
        if (conversationId == null || userId == null) return;
        String url = getBaseUrl(context) + "/api/chat/typing";
        JsonObject body = new JsonObject();
        body.addProperty("conversationId", conversationId);
        body.addProperty("userId", userId);
        body.addProperty("isTyping", isTyping);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) postError(callback, "Failed to update typing: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) response.body().close();
                if (callback != null) {
                    if (response.isSuccessful()) postSuccess(callback, null);
                    else postError(callback, "HTTP " + response.code());
                }
            }
        });
    }

    // 7. React with emoji to a message
    public static void reactToMessage(Context context, String messageId, String reaction, String userId, ChatCallback<Void> callback) {
        String url = getBaseUrl(context) + "/api/chat/messages/" + messageId + "/react";
        JsonObject body = new JsonObject();
        body.addProperty("reaction", reaction);
        body.addProperty("userId", userId);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Failed to react: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    postError(callback, "HTTP " + response.code());
                    return;
                }
                postSuccess(callback, null);
            }
        });
    }

    // 8. Delete message (for_me or for_everyone)
    public static void deleteMessage(Context context, String messageId, String userId, String type, ChatCallback<Void> callback) {
        String url = getBaseUrl(context) + "/api/chat/messages/" + messageId + "/delete";
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        body.addProperty("type", type);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Failed to delete: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    postError(callback, "HTTP " + response.code());
                    return;
                }
                postSuccess(callback, null);
            }
        });
    }

    public static void getCallHistory(Context context, String userId, ChatCallback<List<com.zez.app.model.AppCallItem>> callback) {
        String url = getBaseUrl(context) + "/api/call/history?userId=" + Uri.encode(userId);
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    postError(callback, "HTTP " + response.code());
                    return;
                }
                try {
                    String str = response.body().string();
                    JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                    if (!json.has("success") || !json.get("success").getAsBoolean()) {
                        postError(callback, json.has("error") ? json.get("error").getAsString() : "Failed to load call history");
                        return;
                    }
                    List<com.zez.app.model.AppCallItem> list = new ArrayList<>();
                    if (json.has("calls") && json.get("calls").isJsonArray()) {
                        JsonArray arr = json.getAsJsonArray("calls");
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            com.zez.app.model.AppCallItem item = new com.zez.app.model.AppCallItem();
                            if (obj.has("id")) item.setId(obj.get("id").getAsString());
                            if (obj.has("callType")) item.setCallType(obj.get("callType").getAsString());
                            if (obj.has("direction")) item.setDirection(obj.get("direction").getAsString());
                            if (obj.has("status")) item.setStatus(obj.get("status").getAsString());
                            if (obj.has("peerId") && !obj.get("peerId").isJsonNull()) item.setPeerId(obj.get("peerId").getAsString());
                            if (obj.has("peerName") && !obj.get("peerName").isJsonNull()) item.setPeerName(obj.get("peerName").getAsString());
                            if (obj.has("peerPhone") && !obj.get("peerPhone").isJsonNull()) item.setPeerPhone(obj.get("peerPhone").getAsString());
                            if (obj.has("peerAvatar") && !obj.get("peerAvatar").isJsonNull()) item.setPeerAvatar(obj.get("peerAvatar").getAsString());
                            if (obj.has("startedAt") && !obj.get("startedAt").isJsonNull()) item.setStartedAt(obj.get("startedAt").getAsString());
                            if (obj.has("durationSeconds") && !obj.get("durationSeconds").isJsonNull()) item.setDurationSeconds(obj.get("durationSeconds").getAsInt());
                            list.add(item);
                        }
                    }
                    postSuccess(callback, list);
                } catch (Exception e) {
                    postError(callback, "Parse error: " + e.getMessage());
                }
            }
        });
    }

    // Delete Conversations (Batch or Single) for a user
    public static void deleteConversations(Context context, String userId, List<String> conversationIds, ChatCallback<Void> callback) {
        if (userId == null || conversationIds == null || conversationIds.isEmpty()) {
            postError(callback, "userId and conversationIds required");
            return;
        }
        String url = getBaseUrl(context) + "/api/chat/conversations/delete";
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        JsonArray arr = new JsonArray();
        for (String id : conversationIds) {
            if (id != null && !id.trim().isEmpty()) {
                arr.add(id.trim());
            }
        }
        body.add("conversationIds", arr);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Failed to delete chat: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    postError(callback, "Delete chat HTTP " + response.code());
                    return;
                }
                postSuccess(callback, null);
            }
        });
    }

    // Clear messages for a conversation
    public static void clearConversation(Context context, String conversationId, String userId, ChatCallback<Void> callback) {
        if (conversationId == null || userId == null) {
            postError(callback, "conversationId and userId required");
            return;
        }
        String url = getBaseUrl(context) + "/api/chat/conversations/" + Uri.encode(conversationId) + "/clear";
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Failed to clear chat: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response resp = response) {
                    if (!resp.isSuccessful()) {
                        postError(callback, "Clear chat HTTP " + resp.code());
                        return;
                    }
                    postSuccess(callback, null);
                }
            }
        });
    }

    // ==================== Two-Player Tic Tac Toe Game APIs ====================

    public static class GameState {
        public String id;
        public String conversationId;
        public String gameType;
        public String hostId;
        public String guestId;
        public String hostName;
        public String guestName;
        public String[] board = new String[]{"", "", "", "", "", "", "", "", ""};
        public String currentTurn;
        public String winnerId;
        public int[] winningLine;
        public boolean isDraw;
        public String status;
        public int hostScore;
        public int guestScore;
        public int drawsCount;
        public int roundNumber = 1;
        public boolean guestJoined;
        public int version = 1;
        public String endedBy;
    }

    public static class GameEventsResult {
        public boolean changed;
        public boolean timeout;
        public GameState game;
    }

    private static String optStr(JsonObject obj, String key, String defVal) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return defVal;
        return obj.get(key).getAsString();
    }

    private static GameState parseGameState(JsonObject g) {
        if (g == null) return null;
        GameState s = new GameState();
        s.id = optStr(g, "id", null);
        s.conversationId = optStr(g, "conversationId", null);
        s.gameType = optStr(g, "gameType", "tictactoe");
        s.hostId = optStr(g, "hostId", null);
        s.guestId = optStr(g, "guestId", null);
        s.hostName = optStr(g, "hostName", "Player X");
        s.guestName = optStr(g, "guestName", "Player O");
        if (g.has("board") && g.get("board").isJsonArray()) {
            JsonArray arr = g.getAsJsonArray("board");
            for (int i = 0; i < Math.min(9, arr.size()); i++) {
                s.board[i] = arr.get(i).isJsonNull() ? "" : arr.get(i).getAsString();
            }
        }
        s.currentTurn = optStr(g, "currentTurn", null);
        s.winnerId = optStr(g, "winnerId", null);
        if (g.has("winningLine") && g.get("winningLine").isJsonArray()) {
            JsonArray wl = g.getAsJsonArray("winningLine");
            if (wl.size() >= 3) {
                s.winningLine = new int[]{wl.get(0).getAsInt(), wl.get(1).getAsInt(), wl.get(2).getAsInt()};
            }
        }
        s.isDraw = g.has("isDraw") && !g.get("isDraw").isJsonNull() && g.get("isDraw").getAsBoolean();
        s.status = optStr(g, "status", "waiting");
        s.hostScore = (g.has("hostScore") && !g.get("hostScore").isJsonNull()) ? g.get("hostScore").getAsInt() : 0;
        s.guestScore = (g.has("guestScore") && !g.get("guestScore").isJsonNull()) ? g.get("guestScore").getAsInt() : 0;
        s.drawsCount = (g.has("drawsCount") && !g.get("drawsCount").isJsonNull()) ? g.get("drawsCount").getAsInt() : 0;
        s.roundNumber = (g.has("roundNumber") && !g.get("roundNumber").isJsonNull()) ? g.get("roundNumber").getAsInt() : 1;
        s.guestJoined = g.has("guestJoined") && !g.get("guestJoined").isJsonNull() && g.get("guestJoined").getAsBoolean();
        s.version = (g.has("version") && !g.get("version").isJsonNull()) ? g.get("version").getAsInt() : 1;
        s.endedBy = optStr(g, "endedBy", null);
        return s;
    }

    private static void executeGameCall(Request request, ChatCallback<GameState> callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response resp = response) {
                    String str = resp.body() != null ? resp.body().string() : "";
                    try {
                        JsonObject json = !str.isEmpty() ? JsonParser.parseString(str).getAsJsonObject() : new JsonObject();
                        boolean ok = json.has("success") && json.get("success").getAsBoolean();
                        if (resp.isSuccessful() && ok && json.has("game") && json.get("game").isJsonObject()) {
                            postSuccess(callback, parseGameState(json.getAsJsonObject("game")));
                            return;
                        }
                        String errMsg = optStr(json, "error", "Game HTTP " + resp.code());
                        postError(callback, errMsg);
                    } catch (Exception e) {
                        if (resp.code() == 404) {
                            postError(callback, "Deploy backend update to enable live games (HTTP 404)");
                        } else {
                            postError(callback, "Game HTTP " + resp.code());
                        }
                    }
                }
            }
        });
    }

    public static void createGame(Context context, String gameId, String conversationId, String hostId, String guestId, String hostName, String guestName, ChatCallback<GameState> callback) {
        String url = getBaseUrl(context) + "/api/chat/game/create";
        JsonObject body = new JsonObject();
        body.addProperty("gameId", gameId);
        if (conversationId != null) body.addProperty("conversationId", conversationId);
        body.addProperty("hostId", hostId);
        body.addProperty("guestId", guestId);
        body.addProperty("hostName", hostName != null ? hostName : "Player X");
        body.addProperty("guestName", guestName != null ? guestName : "Player O");
        body.addProperty("gameType", "tictactoe");

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        executeGameCall(request, callback);
    }

    public static void joinGame(Context context, String gameId, String userId, ChatCallback<GameState> callback) {
        String url = getBaseUrl(context) + "/api/chat/game/" + Uri.encode(gameId) + "/join";
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        executeGameCall(request, callback);
    }

    public static void getGame(Context context, String gameId, ChatCallback<GameState> callback) {
        String url = getBaseUrl(context) + "/api/chat/game/" + Uri.encode(gameId);
        Request request = new Request.Builder().url(url).get().build();
        executeGameCall(request, callback);
    }

    public static GameEventsResult listenGameEventsSync(Context context, String gameId, int sinceVersion) {
        if (context == null || gameId == null) return null;
        String url = getBaseUrl(context) + "/api/chat/game/" + Uri.encode(gameId) + "/events?sinceVersion=" + sinceVersion;
        Request request = new Request.Builder().url(url).get().build();
        OkHttpClient gamePollClient = client.newBuilder()
                .readTimeout(25, TimeUnit.SECONDS)
                .connectTimeout(12, TimeUnit.SECONDS)
                .build();
        try (Response resp = gamePollClient.newCall(request).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) return null;
            String str = resp.body().string();
            JsonObject json = JsonParser.parseString(str).getAsJsonObject();
            GameEventsResult res = new GameEventsResult();
            res.changed = json.has("changed") && json.get("changed").getAsBoolean();
            res.timeout = json.has("timeout") && json.get("timeout").getAsBoolean();
            if (json.has("game") && json.get("game").isJsonObject()) {
                res.game = parseGameState(json.getAsJsonObject("game"));
            }
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    public static void makeGameMove(Context context, String gameId, String userId, int cellIndex, ChatCallback<GameState> callback) {
        String url = getBaseUrl(context) + "/api/chat/game/" + Uri.encode(gameId) + "/move";
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        body.addProperty("cellIndex", cellIndex);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        executeGameCall(request, callback);
    }

    public static void rematchGame(Context context, String gameId, String userId, ChatCallback<GameState> callback) {
        String url = getBaseUrl(context) + "/api/chat/game/" + Uri.encode(gameId) + "/rematch";
        JsonObject body = new JsonObject();
        if (userId != null) body.addProperty("userId", userId);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        executeGameCall(request, callback);
    }

    public static void endGame(Context context, String gameId, String userId, ChatCallback<GameState> callback) {
        String url = getBaseUrl(context) + "/api/chat/game/" + Uri.encode(gameId) + "/end";
        JsonObject body = new JsonObject();
        if (userId != null) body.addProperty("userId", userId);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        executeGameCall(request, callback);
    }

    private static <T> void postSuccess(ChatCallback<T> callback, T data) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(data));
        }
    }

    private static <T> void postError(ChatCallback<T> callback, String error) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }
}
