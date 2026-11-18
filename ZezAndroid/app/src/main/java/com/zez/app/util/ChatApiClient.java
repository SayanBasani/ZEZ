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

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

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
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
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

    // 4. Get Messages
    public static void getMessages(Context context, String conversationId, String userId, String otherUserId, int limit, ChatCallback<List<ChatMessage>> callback) {
        StringBuilder sb = new StringBuilder(getBaseUrl(context)).append("/api/chat/messages?");
        if (conversationId != null) sb.append("conversationId=").append(conversationId).append("&");
        if (userId != null) sb.append("userId=").append(userId).append("&");
        if (otherUserId != null) sb.append("otherUserId=").append(otherUserId).append("&");
        sb.append("limit=").append(limit);

        Request request = new Request.Builder().url(sb.toString()).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, "Error fetching messages: " + e.getMessage());
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
                    List<ChatMessage> msgs = new ArrayList<>();
                    if (json.has("messages")) {
                        JsonArray arr = json.getAsJsonArray("messages");
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject m = arr.get(i).getAsJsonObject();
                            ChatMessage msg = new ChatMessage();
                            if (m.has("id")) msg.setId(m.get("id").getAsString());
                            if (m.has("conversation_id")) msg.setConversationId(m.get("conversation_id").getAsString());
                            if (m.has("sender_id")) msg.setSenderId(m.get("sender_id").getAsString());
                            if (m.has("receiver_id")) msg.setReceiverId(m.get("receiver_id").getAsString());
                            if (m.has("message_text")) msg.setMessageText(m.get("message_text").getAsString());
                            if (m.has("media_url") && !m.get("media_url").isJsonNull()) msg.setMediaUrl(m.get("media_url").getAsString());
                            if (m.has("media_type") && !m.get("media_type").isJsonNull()) msg.setMediaType(m.get("media_type").getAsString());
                            if (m.has("file_name") && !m.get("file_name").isJsonNull()) msg.setFileName(m.get("file_name").getAsString());
                            if (m.has("file_size") && !m.get("file_size").isJsonNull()) msg.setFileSize(m.get("file_size").getAsLong());
                            if (m.has("status")) msg.setStatus(m.get("status").getAsString());
                            if (m.has("created_at")) msg.setCreatedAt(m.get("created_at").getAsString());
                            msgs.add(msg);
                        }
                    }
                    postSuccess(callback, msgs);
                } catch (Exception e) {
                    postError(callback, "Parse error: " + e.getMessage());
                }
            }
        });
    }

    // 5. Send Message (with optional media)
    public static void sendMessage(Context context, String senderId, String receiverId, String text, String mediaUrl, ChatCallback<ChatMessage> callback) {
        sendMessage(context, senderId, receiverId, text, mediaUrl, null, null, null, callback);
    }

    public static void sendMessage(Context context, String senderId, String receiverId, String text, String mediaUrl, String mediaType, String fileName, Long fileSize, ChatCallback<ChatMessage> callback) {
        String url = getBaseUrl(context) + "/api/chat/messages/send";
        JsonObject body = new JsonObject();
        body.addProperty("sender_id", senderId);
        body.addProperty("receiver_id", receiverId);
        if (text != null) body.addProperty("message_text", text);
        if (mediaUrl != null) body.addProperty("media_url", mediaUrl);
        if (mediaType != null) body.addProperty("media_type", mediaType);
        if (fileName != null) body.addProperty("file_name", fileName);
        if (fileSize != null) body.addProperty("file_size", fileSize);

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
                if (!response.isSuccessful() || response.body() == null) {
                    postError(callback, "Send error HTTP " + response.code());
                    return;
                }
                try {
                    String str = response.body().string();
                    JsonObject json = JsonParser.parseString(str).getAsJsonObject();
                    if (json.has("success") && json.get("success").getAsBoolean() && json.has("message")) {
                        JsonObject m = json.getAsJsonObject("message");
                        ChatMessage msg = new ChatMessage();
                        if (m.has("id")) msg.setId(m.get("id").getAsString());
                        if (m.has("conversation_id")) msg.setConversationId(m.get("conversation_id").getAsString());
                        if (m.has("sender_id")) msg.setSenderId(m.get("sender_id").getAsString());
                        if (m.has("receiver_id")) msg.setReceiverId(m.get("receiver_id").getAsString());
                        if (m.has("message_text")) msg.setMessageText(m.get("message_text").getAsString());
                        if (m.has("media_url") && !m.get("media_url").isJsonNull()) msg.setMediaUrl(m.get("media_url").getAsString());
                        if (m.has("media_type") && !m.get("media_type").isJsonNull()) msg.setMediaType(m.get("media_type").getAsString());
                        if (m.has("file_name") && !m.get("file_name").isJsonNull()) msg.setFileName(m.get("file_name").getAsString());
                        if (m.has("file_size") && !m.get("file_size").isJsonNull()) msg.setFileSize(m.get("file_size").getAsLong());
                        if (m.has("status")) msg.setStatus(m.get("status").getAsString());
                        if (m.has("created_at")) msg.setCreatedAt(m.get("created_at").getAsString());
                        postSuccess(callback, msg);
                    } else {
                        postError(callback, "Failed to send message.");
                    }
                } catch (Exception e) {
                    postError(callback, "Parse error: " + e.getMessage());
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

    // 6. Polling for Live Chat Updates
    public static class PollResult {
        public String timestamp;
        public List<ChatMessage> incoming = new ArrayList<>();
        public List<JsonObject> statusUpdates = new ArrayList<>();
    }

    public static void pollUpdates(Context context, String userId, String sinceTimestamp, ChatCallback<PollResult> callback) {
        StringBuilder sb = new StringBuilder(getBaseUrl(context)).append("/api/chat/poll?userId=").append(userId);
        if (sinceTimestamp != null) sb.append("&since=").append(sinceTimestamp);

        Request request = new Request.Builder().url(sb.toString()).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    postError(callback, "Poll HTTP " + response.code());
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
                    postSuccess(callback, pr);
                } catch (Exception e) {
                    postError(callback, "Poll parse error: " + e.getMessage());
                }
            }
        });
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
