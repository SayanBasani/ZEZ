package com.zez.manager.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zez.manager.model.AppCallLogItem;
import com.zez.manager.model.CallLogItem;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Dns;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private static final String TAG = "ZezApiClient";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final ConcurrentHashMap<String, List<InetAddress>> dnsCache = new ConcurrentHashMap<>();

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .dns(new Dns() {
                @Override
                public List<InetAddress> lookup(String hostname) throws UnknownHostException {
                    try {
                        List<InetAddress> all = Dns.SYSTEM.lookup(hostname);
                        if (all != null && !all.isEmpty()) {
                            List<InetAddress> v4First = new ArrayList<>();
                            List<InetAddress> others = new ArrayList<>();
                            for (InetAddress a : all) {
                                if (a instanceof Inet4Address) v4First.add(a);
                                else others.add(a);
                            }
                            v4First.addAll(others);
                            dnsCache.put(hostname, v4First);
                            return v4First;
                        }
                    } catch (UnknownHostException e) {
                        List<InetAddress> cached = dnsCache.get(hostname);
                        if (cached != null && !cached.isEmpty()) return cached;
                        if (hostname.toLowerCase().contains("onrender.com")) {
                            try {
                                List<InetAddress> fallback = new ArrayList<>();
                                fallback.add(InetAddress.getByAddress(hostname, new byte[]{(byte) 216, 24, 57, 18}));
                                fallback.add(InetAddress.getByAddress(hostname, new byte[]{(byte) 216, 24, 57, 16}));
                                return fallback;
                            } catch (Exception ignored) {}
                        }
                        throw e;
                    }
                    return Dns.SYSTEM.lookup(hostname);
                }
            })
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public static class SyncResult {
        public final boolean success;
        public final int received;
        public final int inserted;
        public final int duplicates;
        public final String error;

        public SyncResult(boolean success, int received, int inserted, int duplicates, String error) {
            this.success = success;
            this.received = received;
            this.inserted = inserted;
            this.duplicates = duplicates;
            this.error = error;
        }
    }

    public static class CloudBackupStats {
        public final boolean success;
        public final long totalCalls;
        public final long callsToday;
        public final String latestCall;
        public final String earliestCall;
        public final String error;

        public CloudBackupStats(boolean success, long totalCalls, long callsToday, String latestCall, String earliestCall, String error) {
            this.success = success;
            this.totalCalls = totalCalls;
            this.callsToday = callsToday;
            this.latestCall = latestCall;
            this.earliestCall = earliestCall;
            this.error = error;
        }
    }

    public static class CloudRestoreResult {
        public final boolean success;
        public final List<CallLogItem> calls;
        public final int decryptionFailedCount;
        public final String error;

        public CloudRestoreResult(boolean success, List<CallLogItem> calls, int decryptionFailedCount, String error) {
            this.success = success;
            this.calls = calls;
            this.decryptionFailedCount = decryptionFailedCount;
            this.error = error;
        }
    }

    public interface AlarmSyncCallback {
        void onSyncComplete(List<com.zez.manager.alarm.model.AlarmItem> serverActiveAlarms, List<Long> deletedAlarmIds);
        void onError(String error);
    }

    public static void syncAlarms(Context context, List<com.zez.manager.alarm.model.AlarmItem> localAlarms, AlarmSyncCallback callback) {
        new Thread(() -> {
            String token = ensureToken(context);
            if (token == null) {
                if (callback != null) callback.onError("Device registration failed");
                return;
            }

            String apiUrl = PreferencesHelper.getApiUrl(context);
            JsonObject body = new JsonObject();
            JsonArray array = new JsonArray();
            if (localAlarms != null) {
                for (com.zez.manager.alarm.model.AlarmItem a : localAlarms) {
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
            List<Long> deletedIdsLocal = com.zez.manager.alarm.util.AlarmHelper.getDeletedAlarmIdsList(context);
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
                        List<com.zez.manager.alarm.model.AlarmItem> activeList = new ArrayList<>();
                        if (json.has("activeAlarms")) {
                            JsonArray arr = json.getAsJsonArray("activeAlarms");
                            for (int i = 0; i < arr.size(); i++) {
                                JsonObject o = arr.get(i).getAsJsonObject();
                                com.zez.manager.alarm.model.AlarmItem item = new com.zez.manager.alarm.model.AlarmItem();
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

    public static boolean ping(Context context) {
        String baseUrl = PreferencesHelper.getApiUrl(context);
        Request req = new Request.Builder().url(baseUrl + "/health").get().build();
        try (Response resp = client.newCall(req).execute()) {
            return resp.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    public static synchronized boolean registerDeviceSync(Context context) {
        String baseUrl = PreferencesHelper.getApiUrl(context);
        JsonObject ids = new JsonObject();
        ids.addProperty("androidId", DeviceHelper.getAndroidId(context));
        ids.addProperty("persistentId", PreferencesHelper.getPersistentUuid(context));

        JsonObject body = new JsonObject();
        body.add("ids", ids);
        body.addProperty("deviceName", Build.MODEL);
        body.addProperty("deviceModel", Build.MODEL);
        body.addProperty("manufacturer", Build.MANUFACTURER);
        body.addProperty("brand", Build.BRAND);
        body.addProperty("platform", "Android");
        body.addProperty("osVersion", Build.VERSION.RELEASE);
        body.addProperty("appVersion", DeviceHelper.getAppVersionName(context));
        body.add("deviceDetails", DeviceHelper.getDeviceDetails(context));

        DeviceHelper.SimInfo sim = DeviceHelper.getSimInfo(context);
        if (sim.sim1PhoneNumber != null) body.addProperty("sim1PhoneNumber", sim.sim1PhoneNumber);
        if (sim.sim2PhoneNumber != null) body.addProperty("sim2PhoneNumber", sim.sim2PhoneNumber);
        if (sim.sim1Carrier != null) body.addProperty("sim1Carrier", sim.sim1Carrier);
        if (sim.sim2Carrier != null) body.addProperty("sim2Carrier", sim.sim2Carrier);
        if (sim.sim1CountryIso != null) body.addProperty("sim1CountryIso", sim.sim1CountryIso);
        if (sim.sim2CountryIso != null) body.addProperty("sim2CountryIso", sim.sim2CountryIso);
        if (sim.primaryPhoneNumber != null) body.addProperty("primaryPhoneNumber", sim.primaryPhoneNumber);

        Request req = new Request.Builder()
                .url(baseUrl + "/api/devices/register")
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                Log.w(TAG, "Device registration HTTP " + resp.code());
                return false;
            }
            JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
            if (json.has("success") && json.get("success").getAsBoolean()) {
                if (json.has("deviceId") && !json.get("deviceId").isJsonNull()) {
                    PreferencesHelper.setDeviceId(context, json.get("deviceId").getAsString());
                }
                if (json.has("token") && !json.get("token").isJsonNull()) {
                    PreferencesHelper.setDeviceToken(context, json.get("token").getAsString());
                }
                if (json.has("syncCallsEnabled")) {
                    PreferencesHelper.setCallsSyncEnabled(context, json.get("syncCallsEnabled").getAsBoolean());
                }
                if (json.has("syncNotificationsEnabled")) {
                    PreferencesHelper.setNotifSyncEnabled(context, json.get("syncNotificationsEnabled").getAsBoolean());
                }
                if (json.has("syncAppCallsEnabled")) {
                    PreferencesHelper.setAppCallsSyncEnabled(context, json.get("syncAppCallsEnabled").getAsBoolean());
                }
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Device registration error: " + e.getMessage());
        }
        return false;
    }

    private static String ensureToken(Context context) {
        String token = PreferencesHelper.getDeviceToken(context);
        if (token == null || token.isEmpty()) {
            if (registerDeviceSync(context)) {
                token = PreferencesHelper.getDeviceToken(context);
            }
        }
        return token;
    }

    public static SyncResult syncCalls(Context context, List<CallLogItem> calls) {
        if (!PreferencesHelper.isCallsSyncEnabled(context)) {
            return new SyncResult(true, 0, 0, 0, null);
        }
        if (calls == null || calls.isEmpty()) {
            PreferencesHelper.setLastSync(context, System.currentTimeMillis(), 0);
            return new SyncResult(true, 0, 0, 0, null);
        }

        String token = ensureToken(context);
        if (token == null) {
            PreferencesHelper.setLastSyncError(context, "Device authentication failed");
            return new SyncResult(false, 0, 0, 0, "Device authentication failed");
        }

        String baseUrl = PreferencesHelper.getApiUrl(context);
        int chunkSize = 100;
        int totalReceived = 0;
        int totalInserted = 0;
        int totalDuplicates = 0;

        for (int i = 0; i < calls.size(); i += chunkSize) {
            int end = Math.min(calls.size(), i + chunkSize);
            JsonArray arr = new JsonArray();
            for (int j = i; j < end; j++) {
                arr.add(calls.get(j).toJsonObject());
            }

            JsonObject payload = new JsonObject();
            payload.add("calls", arr);
            if (i == 0) {
                payload.add("deviceDetails", DeviceHelper.getDeviceDetails(context));
            }

            boolean retried = false;
            while (true) {
                Request req = new Request.Builder()
                        .url(baseUrl + "/api/calls/sync")
                        .header("Authorization", "Bearer " + token)
                        .post(RequestBody.create(payload.toString(), JSON))
                        .build();

                try (Response resp = client.newCall(req).execute()) {
                    if (resp.code() == 401 && !retried) {
                        retried = true;
                        PreferencesHelper.setDeviceToken(context, null);
                        token = ensureToken(context);
                        if (token != null) continue;
                    }
                    if (!resp.isSuccessful() || resp.body() == null) {
                        String err = "Server error HTTP " + resp.code();
                        PreferencesHelper.setLastSyncError(context, err);
                        return new SyncResult(false, totalReceived, totalInserted, totalDuplicates, err);
                    }
                    JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
                    totalReceived += json.has("received") ? json.get("received").getAsInt() : (end - i);
                    totalInserted += json.has("inserted") ? json.get("inserted").getAsInt() : 0;
                    totalDuplicates += json.has("duplicates") ? json.get("duplicates").getAsInt() : 0;
                    break;
                } catch (Exception e) {
                    String err = e.getMessage() != null ? e.getMessage() : "Network error";
                    PreferencesHelper.setLastSyncError(context, err);
                    return new SyncResult(false, totalReceived, totalInserted, totalDuplicates, err);
                }
            }
        }

        PreferencesHelper.setLastSync(context, System.currentTimeMillis(), totalReceived);
        return new SyncResult(true, totalReceived, totalInserted, totalDuplicates, null);
    }

    public static boolean syncNotifications(Context context, List<JsonObject> notifications) {
        if (!PreferencesHelper.isNotifSyncEnabled(context)) return true;
        if (notifications == null || notifications.isEmpty()) return true;
        String token = ensureToken(context);
        if (token == null) return false;

        String baseUrl = PreferencesHelper.getApiUrl(context);
        JsonArray arr = new JsonArray();
        for (JsonObject n : notifications) arr.add(n);
        JsonObject payload = new JsonObject();
        payload.add("notifications", arr);

        for (int attempt = 0; attempt < 2; attempt++) {
            Request req = new Request.Builder()
                    .url(baseUrl + "/api/devices/notifications/sync")
                    .header("Authorization", "Bearer " + token)
                    .post(RequestBody.create(payload.toString(), JSON))
                    .build();
            try (Response resp = client.newCall(req).execute()) {
                if (resp.code() == 401 && attempt == 0) {
                    PreferencesHelper.setDeviceToken(context, null);
                    token = ensureToken(context);
                    if (token != null) continue;
                }
                return resp.isSuccessful();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static boolean syncAppCalls(Context context, List<AppCallLogItem> appCalls) {
        if (!PreferencesHelper.isAppCallsSyncEnabled(context)) return true;
        if (appCalls == null || appCalls.isEmpty()) return true;
        String token = ensureToken(context);
        if (token == null) return false;

        String baseUrl = PreferencesHelper.getApiUrl(context);
        JsonArray arr = new JsonArray();
        for (AppCallLogItem item : appCalls) arr.add(item.toJsonObject());
        JsonObject payload = new JsonObject();
        payload.add("appCalls", arr);

        for (int attempt = 0; attempt < 2; attempt++) {
            Request req = new Request.Builder()
                    .url(baseUrl + "/api/devices/app-calls/sync")
                    .header("Authorization", "Bearer " + token)
                    .post(RequestBody.create(payload.toString(), JSON))
                    .build();
            try (Response resp = client.newCall(req).execute()) {
                if (resp.code() == 401 && attempt == 0) {
                    PreferencesHelper.setDeviceToken(context, null);
                    token = ensureToken(context);
                    if (token != null) continue;
                }
                return resp.isSuccessful();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static CloudBackupStats fetchCloudBackupStats(Context context) {
        String token = ensureToken(context);
        if (token == null) {
            return new CloudBackupStats(false, 0, 0, null, null, "Device authentication failed");
        }
        String baseUrl = PreferencesHelper.getApiUrl(context);
        for (int attempt = 0; attempt < 2; attempt++) {
            Request req = new Request.Builder()
                    .url(baseUrl + "/api/calls/read/stats")
                    .header("Authorization", "Bearer " + token)
                    .get()
                    .build();
            try (Response resp = client.newCall(req).execute()) {
                if (resp.code() == 401 && attempt == 0) {
                    PreferencesHelper.setDeviceToken(context, null);
                    token = ensureToken(context);
                    if (token != null) continue;
                }
                if (!resp.isSuccessful() || resp.body() == null) {
                    return new CloudBackupStats(false, 0, 0, null, null, "HTTP " + resp.code());
                }
                JsonObject root = JsonParser.parseString(resp.body().string()).getAsJsonObject();
                JsonObject stats = root.getAsJsonObject("stats");
                long total = stats.has("totalCalls") ? Long.parseLong(stats.get("totalCalls").getAsString()) : 0;
                long today = stats.has("callsToday") ? Long.parseLong(stats.get("callsToday").getAsString()) : 0;
                String latest = stats.has("latestCall") && !stats.get("latestCall").isJsonNull() ? stats.get("latestCall").getAsString() : null;
                String earliest = stats.has("earliestCall") && !stats.get("earliestCall").isJsonNull() ? stats.get("earliestCall").getAsString() : null;
                return new CloudBackupStats(true, total, today, latest, earliest, null);
            } catch (Exception e) {
                return new CloudBackupStats(false, 0, 0, null, null, e.getMessage());
            }
        }
        return new CloudBackupStats(false, 0, 0, null, null, "Authentication retry exhausted");
    }

    public static CloudRestoreResult fetchCloudCallsForRestore(Context context, int maxRecords) {
        String token = ensureToken(context);
        if (token == null) {
            return new CloudRestoreResult(false, new ArrayList<>(), 0, "Device authentication failed");
        }
        String baseUrl = PreferencesHelper.getApiUrl(context);
        List<CallLogItem> restored = new ArrayList<>();
        String beforeId = null;
        int decFailures = 0;
        int target = Math.max(100, Math.min(maxRecords, 2000));

        while (restored.size() < target) {
            String url = baseUrl + "/api/calls/read?limit=100" + (beforeId != null ? "&beforeId=" + beforeId : "");
            Request req = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + token)
                    .get()
                    .build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    return new CloudRestoreResult(false, restored, decFailures, "HTTP " + resp.code());
                }
                JsonObject root = JsonParser.parseString(resp.body().string()).getAsJsonObject();
                JsonArray arr = root.getAsJsonArray("calls");
                if (arr == null || arr.size() == 0) break;

                for (JsonElement el : arr) {
                    JsonObject rec = el.getAsJsonObject();
                    if (rec.has("decryptionFailed") && rec.get("decryptionFailed").getAsBoolean()) {
                        decFailures++;
                        continue;
                    }
                    if (!rec.has("call") || rec.get("call").isJsonNull()) continue;
                    JsonObject c = rec.getAsJsonObject("call");
                    CallLogItem item = new CallLogItem();
                    item.id = rec.has("id") ? rec.get("id").getAsString() : "";
                    item.externalId = c.has("externalId") ? c.get("externalId").getAsString() : item.id;
                    item.name = c.has("name") && !c.get("name").isJsonNull() ? c.get("name").getAsString() : "";
                    item.phoneNumber = c.has("phoneNumber") && !c.get("phoneNumber").isJsonNull() ? c.get("phoneNumber").getAsString() : "";
                    item.type = c.has("type") && !c.get("type").isJsonNull() ? c.get("type").getAsString() : "UNKNOWN";
                    item.duration = c.has("duration") && !c.get("duration").isJsonNull() ? c.get("duration").getAsLong() : 0L;
                    item.timestamp = parseIsoOrMillis(c.has("timestamp") && !c.get("timestamp").isJsonNull() ? c.get("timestamp").getAsString() : null);
                    item.fromBackup = true;
                    restored.add(item);
                }

                boolean hasMore = root.has("hasMore") && root.get("hasMore").getAsBoolean();
                if (!hasMore || !root.has("nextBeforeId") || root.get("nextBeforeId").isJsonNull()) break;
                beforeId = root.get("nextBeforeId").getAsString();
            } catch (Exception e) {
                return new CloudRestoreResult(false, restored, decFailures, e.getMessage());
            }
        }
        return new CloudRestoreResult(true, restored, decFailures, null);
    }

    private static long parseIsoOrMillis(String val) {
        if (val == null || val.trim().isEmpty()) return 0L;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException ignored) {}
        try {
            return java.time.Instant.parse(val.trim()).toEpochMilli();
        } catch (Exception ignored) {}
        return 0L;
    }
}
