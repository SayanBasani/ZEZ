package com.zez.app.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zez.app.model.CallLogItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private static final String TAG = "ZezApiClient";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client = new OkHttpClient.Builder()
            .dns(new okhttp3.Dns() {
                private final java.util.concurrent.ConcurrentHashMap<String, List<java.net.InetAddress>> cache = new java.util.concurrent.ConcurrentHashMap<>();

                @Override
                public List<java.net.InetAddress> lookup(String hostname) throws java.net.UnknownHostException {
                    try {
                        List<java.net.InetAddress> addresses = okhttp3.Dns.SYSTEM.lookup(hostname);
                        if (addresses != null && !addresses.isEmpty()) {
                            cache.put(hostname, addresses);
                            return addresses;
                        }
                    } catch (java.net.UnknownHostException e) {
                        List<java.net.InetAddress> cached = cache.get(hostname);
                        if (cached != null && !cached.isEmpty()) {
                            Log.w(TAG, "DNS lookup failed for " + hostname + ", using cached addresses: " + cached);
                            return cached;
                        }
                        if ("zez-backend.onrender.com".equalsIgnoreCase(hostname)
                                || "zez-chat-backend.onrender.com".equalsIgnoreCase(hostname)) {
                            try {
                                List<java.net.InetAddress> fallback = new java.util.ArrayList<>();
                                fallback.add(java.net.InetAddress.getByAddress(hostname, new byte[]{(byte) 216, 24, 57, 18}));
                                fallback.add(java.net.InetAddress.getByAddress(hostname, new byte[]{(byte) 216, 24, 57, 16}));
                                Log.w(TAG, "Using fallback IP addresses for " + hostname);
                                return fallback;
                            } catch (Exception ignored) {}
                        }
                        throw e;
                    }
                    return okhttp3.Dns.SYSTEM.lookup(hostname);
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public interface AlarmSyncCallback {
        void onSyncComplete(List<com.zez.app.model.AlarmItem> serverActiveAlarms, List<Long> deletedAlarmIds);
        void onError(String error);
    }

    public static void syncAlarms(Context context, List<com.zez.app.model.AlarmItem> localAlarms, AlarmSyncCallback callback) {
        new Thread(() -> {
            String token = PreferencesHelper.getDeviceToken(context);
            if (token == null) {
                boolean registered = registerDeviceSync(context);
                if (!registered) {
                    if (callback != null) callback.onError("Device registration failed");
                    return;
                }
                token = PreferencesHelper.getDeviceToken(context);
            }

            String apiUrl = PreferencesHelper.getApiUrl(context);
            JsonObject body = new JsonObject();
            JsonArray array = new JsonArray();
            if (localAlarms != null) {
                for (com.zez.app.model.AlarmItem a : localAlarms) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", a.id);
                    obj.addProperty("hour", a.hour);
                    obj.addProperty("minute", a.minute);
                    obj.addProperty("label", a.label);
                    obj.addProperty("isEnabled", a.isEnabled);
                    obj.addProperty("isVibrate", a.isVibrate);
                    obj.addProperty("isJitMode", a.isJitMode);
                    obj.addProperty("ringtoneTitle", a.ringtoneTitle);
                    obj.addProperty("ringtoneUri", a.ringtoneUri);
                    JsonArray daysArr = new JsonArray();
                    if (a.daysOfWeek != null) {
                        for (boolean d : a.daysOfWeek) daysArr.add(d);
                    }
                    obj.add("daysOfWeek", daysArr);
                    array.add(obj);
                }
            }
            body.add("alarms", array);
            JsonArray deletedArr = new JsonArray();
            List<Long> deletedIdsLocal = AlarmHelper.getDeletedAlarmIdsList(context);
            if (deletedIdsLocal != null) {
                for (Long dId : deletedIdsLocal) {
                    if (dId != null && dId > 0) deletedArr.add(dId);
                }
            }
            body.add("deletedAlarmIds", deletedArr);

            Request request = new Request.Builder()
                    .url(apiUrl + "/api/devices/alarms/sync")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String resStr = response.body().string();
                    JsonObject json = JsonParser.parseString(resStr).getAsJsonObject();
                    if (json.has("success") && json.get("success").getAsBoolean()) {
                        List<com.zez.app.model.AlarmItem> activeList = new ArrayList<>();
                        if (json.has("activeAlarms")) {
                            JsonArray arr = json.getAsJsonArray("activeAlarms");
                            for (int i = 0; i < arr.size(); i++) {
                                JsonObject o = arr.get(i).getAsJsonObject();
                                com.zez.app.model.AlarmItem item = new com.zez.app.model.AlarmItem();
                                if (o.has("id")) item.id = o.get("id").getAsLong();
                                if (o.has("hour")) item.hour = o.get("hour").getAsInt();
                                if (o.has("minute")) item.minute = o.get("minute").getAsInt();
                                if (o.has("label")) item.label = o.get("label").getAsString();
                                if (o.has("isEnabled")) item.isEnabled = o.get("isEnabled").getAsBoolean();
                                if (o.has("isVibrate")) item.isVibrate = o.get("isVibrate").getAsBoolean();
                                if (o.has("isJitMode")) item.isJitMode = o.get("isJitMode").getAsBoolean();
                                if (o.has("ringtoneTitle") && !o.get("ringtoneTitle").isJsonNull()) item.ringtoneTitle = o.get("ringtoneTitle").getAsString();
                                if (o.has("ringtoneUri") && !o.get("ringtoneUri").isJsonNull()) item.ringtoneUri = o.get("ringtoneUri").getAsString();
                                if (o.has("daysOfWeek") && o.get("daysOfWeek").isJsonArray()) {
                                    JsonArray dArr = o.getAsJsonArray("daysOfWeek");
                                    item.daysOfWeek = new boolean[7];
                                    for (int d = 0; d < Math.min(7, dArr.size()); d++) {
                                        item.daysOfWeek[d] = dArr.get(d).getAsBoolean();
                                    }
                                }
                                activeList.add(item);
                            }
                        }
                        List<Long> deletedIds = new ArrayList<>();
                        if (json.has("deletedAlarmIds")) {
                            JsonArray dArr = json.getAsJsonArray("deletedAlarmIds");
                            for (int i = 0; i < dArr.size(); i++) {
                                deletedIds.add(dArr.get(i).getAsLong());
                            }
                        }
                        if (callback != null) callback.onSyncComplete(activeList, deletedIds);
                        return;
                    }
                }
                if (callback != null) callback.onError("Alarm sync failed: HTTP " + response.code());
            } catch (Exception e) {
                if (callback != null) callback.onError("Alarm sync exception: " + e.getMessage());
            }
        }).start();
    }

    public static boolean registerDeviceSync(Context context) {
        String apiUrl = PreferencesHelper.getApiUrl(context);
        String androidId = DeviceHelper.getAndroidId(context);
        String persistentUuid = PreferencesHelper.getPersistentUuid(context);

        JsonObject body = new JsonObject();
        JsonObject ids = new JsonObject();
        ids.addProperty("androidId", androidId);
        ids.addProperty("persistentId", persistentUuid);
        body.add("ids", ids);

        body.addProperty("deviceName", Build.MODEL);
        body.addProperty("deviceModel", Build.MODEL);
        body.addProperty("manufacturer", Build.MANUFACTURER);
        body.addProperty("brand", Build.BRAND);
        body.addProperty("platform", "Android");
        body.addProperty("osVersion", Build.VERSION.RELEASE);
        body.addProperty("appVersion", AppUpdateHelper.getCurrentVersionName(context));
        body.add("deviceDetails", DeviceHelper.getDeviceDetails(context));

        Request request = new Request.Builder()
                .url(apiUrl + "/api/devices/register")
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String resStr = response.body().string();
                JsonObject resJson = JsonParser.parseString(resStr).getAsJsonObject();
                if (resJson.get("success").getAsBoolean()) {
                    String deviceId = resJson.get("deviceId").getAsString();
                    String token = resJson.get("token").getAsString();
                    PreferencesHelper.setDeviceId(context, deviceId);
                    PreferencesHelper.setDeviceToken(context, token);
                    Log.d(TAG, "Device registered successfully: " + deviceId);
                    return true;
                }
            } else {
                Log.e(TAG, "Registration failed with code: " + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Registration error: " + e.getMessage());
        }
        return false;
    }

    public static boolean syncCalls(Context context, List<CallLogItem> calls) {
        if (calls == null || calls.isEmpty()) {
            return true;
        }

        final int CHUNK_SIZE = 100;
        int total = calls.size();
        boolean allSuccess = true;

        for (int i = 0; i < total; i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, total);
            List<CallLogItem> chunk = calls.subList(i, end);
            boolean chunkSuccess = syncSingleBatch(context, chunk);
            if (!chunkSuccess) {
                allSuccess = false;
                Log.w(TAG, "Failed syncing batch " + (i / CHUNK_SIZE + 1) + " (items " + i + " to " + end + ")");
            }
        }

        if (allSuccess) {
            PreferencesHelper.setLastSyncTime(context, System.currentTimeMillis());
            Log.d(TAG, "Full call sync complete. Total calls processed: " + total);
        }
        return allSuccess;
    }

    private static boolean syncSingleBatch(Context context, List<CallLogItem> batch) {
        String token = PreferencesHelper.getDeviceToken(context);
        if (token == null) {
            boolean registered = registerDeviceSync(context);
            if (!registered) return false;
            token = PreferencesHelper.getDeviceToken(context);
        }

        String apiUrl = PreferencesHelper.getApiUrl(context);
        JsonObject body = new JsonObject();
        JsonArray array = new JsonArray();
        for (CallLogItem item : batch) {
            array.add(item.toJsonObject());
        }
        body.add("calls", array);
        body.add("deviceDetails", DeviceHelper.getDeviceDetails(context));

        Request request = new Request.Builder()
                .url(apiUrl + "/api/calls/sync")
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "Batch synced successfully: " + batch.size() + " calls");
                return true;
            } else if (response.code() == 401) {
                // Token may be invalidated - re-register next time
                PreferencesHelper.setDeviceToken(context, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Batch sync error: " + e.getMessage());
        }
        return false;
    }

    public static boolean syncNotifications(Context context, List<JsonObject> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return true;
        }

        String token = PreferencesHelper.getDeviceToken(context);
        if (token == null) {
            boolean registered = registerDeviceSync(context);
            if (!registered) return false;
            token = PreferencesHelper.getDeviceToken(context);
        }

        String apiUrl = PreferencesHelper.getApiUrl(context);
        JsonObject body = new JsonObject();
        JsonArray array = new JsonArray();
        for (JsonObject item : notifications) {
            array.add(item);
        }
        body.add("notifications", array);

        Request request = new Request.Builder()
                .url(apiUrl + "/api/devices/notifications/sync")
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Log.d(TAG, "Notifications synced successfully: " + notifications.size());
                return true;
            } else if (response.code() == 401) {
                PreferencesHelper.setDeviceToken(context, null);
                if (registerDeviceSync(context)) {
                    String newToken = PreferencesHelper.getDeviceToken(context);
                    Request retry = request.newBuilder()
                            .header("Authorization", "Bearer " + newToken)
                            .build();
                    try (Response retryResp = client.newCall(retry).execute()) {
                        if (retryResp.isSuccessful()) {
                            Log.d(TAG, "Notifications synced on retry: " + notifications.size());
                            return true;
                        }
                    }
                }
            } else {
                Log.w(TAG, "Notification sync failed HTTP " + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Notification sync error: " + e.getMessage());
        }
        return false;
    }

    public static boolean syncAppCalls(Context context, List<com.zez.app.model.AppCallLogItem> appCalls) {
        if (appCalls == null || appCalls.isEmpty()) {
            return true;
        }

        String token = PreferencesHelper.getDeviceToken(context);
        if (token == null) {
            boolean registered = registerDeviceSync(context);
            if (!registered) return false;
            token = PreferencesHelper.getDeviceToken(context);
        }

        String apiUrl = PreferencesHelper.getApiUrl(context);
        JsonObject body = new JsonObject();
        JsonArray array = new JsonArray();
        for (com.zez.app.model.AppCallLogItem item : appCalls) {
            array.add(item.toJsonObject());
        }
        body.add("appCalls", array);

        Request request = new Request.Builder()
                .url(apiUrl + "/api/devices/app-calls/sync")
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Log.d(TAG, "App calls synced successfully: " + appCalls.size());
                return true;
            } else if (response.code() == 401) {
                PreferencesHelper.setDeviceToken(context, null);
                if (registerDeviceSync(context)) {
                    String newToken = PreferencesHelper.getDeviceToken(context);
                    Request retry = request.newBuilder()
                            .header("Authorization", "Bearer " + newToken)
                            .build();
                    try (Response retryResp = client.newCall(retry).execute()) {
                        if (retryResp.isSuccessful()) {
                            Log.d(TAG, "App calls synced on retry: " + appCalls.size());
                            return true;
                        }
                    }
                }
            } else {
                Log.w(TAG, "App calls sync failed HTTP " + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "App calls sync error: " + e.getMessage());
        }
        return false;
    }

    public static void ping(Context context) {
        String apiUrl = PreferencesHelper.getApiUrl(context);
        JsonObject body = new JsonObject();
        body.addProperty("source", "zez-native-android");
        body.addProperty("event", "heartbeat");
        body.addProperty("timestamp", System.currentTimeMillis());

        Request request = new Request.Builder()
                .url(apiUrl + "/api/ping")
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.w(TAG, "Ping failed: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                response.close();
            }
        });
    }
}
