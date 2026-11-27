package com.zez.app.games.snakesladders;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zez.app.util.ChatApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class SnakesLaddersApiClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private SnakesLaddersApiClient() {}

    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onError(String message, int httpCode, String code);
    }

    private static String baseUrl(Context context) {
        return ChatApiClient.getBaseUrl(context) + "/api/games/snakes-and-ladders";
    }

    private static OkHttpClient client() {
        return ChatApiClient.getOkHttpClient();
    }

    private static String optStr(JsonObject obj, String key, String defVal) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return defVal;
        return obj.get(key).getAsString();
    }

    private static int optInt(JsonObject obj, String key, int defVal) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return defVal;
        try { return obj.get(key).getAsInt(); } catch (Exception e) { return defVal; }
    }

    private static long optLong(JsonObject obj, String key, long defVal) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return defVal;
        try { return obj.get(key).getAsLong(); } catch (Exception e) { return defVal; }
    }

    private static boolean optBool(JsonObject obj, String key, boolean defVal) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return defVal;
        try { return obj.get(key).getAsBoolean(); } catch (Exception e) { return defVal; }
    }

    private static SnakesLaddersBoardConfig parseBoardConfig(JsonObject obj) {
        if (obj == null) return SnakesLaddersBoardConfig.defaultConfig();
        String ver = optStr(obj, "version", "sl-board-v1");
        int boardSize = optInt(obj, "boardSize", 100);
        int rows = optInt(obj, "rows", 10);
        int cols = optInt(obj, "columns", 10);

        List<SnakesLaddersBoardConfig.SnakeDef> snakes = new ArrayList<>();
        if (obj.has("snakes") && obj.get("snakes").isJsonArray()) {
            for (JsonElement el : obj.getAsJsonArray("snakes")) {
                if (el.isJsonObject()) {
                    JsonObject so = el.getAsJsonObject();
                    snakes.add(new SnakesLaddersBoardConfig.SnakeDef(optInt(so, "head", 0), optInt(so, "tail", 0)));
                }
            }
        }
        List<SnakesLaddersBoardConfig.LadderDef> ladders = new ArrayList<>();
        if (obj.has("ladders") && obj.get("ladders").isJsonArray()) {
            for (JsonElement el : obj.getAsJsonArray("ladders")) {
                if (el.isJsonObject()) {
                    JsonObject lo = el.getAsJsonObject();
                    ladders.add(new SnakesLaddersBoardConfig.LadderDef(optInt(lo, "start", 0), optInt(lo, "end", 0)));
                }
            }
        }
        if (snakes.isEmpty() && ladders.isEmpty()) {
            return SnakesLaddersBoardConfig.defaultConfig();
        }
        return new SnakesLaddersBoardConfig(ver, boardSize, rows, cols, snakes, ladders);
    }

    private static SnakesLaddersModels.MoveRecord parseMove(JsonObject obj) {
        if (obj == null) return null;
        SnakesLaddersModels.MoveRecord m = new SnakesLaddersModels.MoveRecord();
        m.actionId = optStr(obj, "actionId", null);
        m.playerId = optStr(obj, "playerId", null);
        m.playerName = optStr(obj, "playerName", "Player");
        m.diceValue = optInt(obj, "diceValue", 1);
        m.previousPosition = optInt(obj, "previousPosition", 0);
        m.normalDestination = optInt(obj, "normalDestination", 0);
        m.finalPosition = optInt(obj, "finalPosition", 0);
        m.overshot = optBool(obj, "overshot", false);
        m.specialMoveType = optStr(obj, "specialMoveType", "NONE");
        m.specialMoveFrom = obj.has("specialMoveFrom") && !obj.get("specialMoveFrom").isJsonNull()
                ? obj.get("specialMoveFrom").getAsInt() : null;
        m.specialMoveTo = obj.has("specialMoveTo") && !obj.get("specialMoveTo").isJsonNull()
                ? obj.get("specialMoveTo").getAsInt() : null;
        m.grantedExtraTurn = optBool(obj, "grantedExtraTurn", false);
        m.isWin = optBool(obj, "isWin", false);
        m.stateVersion = optInt(obj, "stateVersion", 1);
        return m;
    }

    public static SnakesLaddersModels.SessionState parseSession(JsonObject obj) {
        if (obj == null) return null;
        SnakesLaddersModels.SessionState s = new SnakesLaddersModels.SessionState();
        s.id = optStr(obj, "id", null);
        s.conversationId = optStr(obj, "conversationId", null);
        s.invitationId = optStr(obj, "invitationId", null);
        s.gameType = optStr(obj, "gameType", "snakes_and_ladders");
        s.status = optStr(obj, "status", "ACTIVE");
        s.boardConfigurationVersion = optStr(obj, "boardConfigurationVersion", "sl-board-v1");
        if (obj.has("boardConfig") && obj.get("boardConfig").isJsonObject()) {
            s.boardConfig = parseBoardConfig(obj.getAsJsonObject("boardConfig"));
        }
        s.currentTurnIndex = optInt(obj, "currentTurnIndex", 0);
        s.currentTurnPlayerId = optStr(obj, "currentTurnPlayerId", null);
        s.winnerId = optStr(obj, "winnerId", null);
        s.resignedBy = optStr(obj, "resignedBy", null);
        s.stateVersion = optInt(obj, "stateVersion", 1);

        if (obj.has("players") && obj.get("players").isJsonArray()) {
            JsonArray arr = obj.getAsJsonArray("players");
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject po = el.getAsJsonObject();
                SnakesLaddersModels.PlayerState p = new SnakesLaddersModels.PlayerState();
                p.userId = optStr(po, "userId", null);
                p.name = optStr(po, "name", "Player");
                p.avatarUrl = optStr(po, "avatarUrl", null);
                p.seatIndex = optInt(po, "seatIndex", 0);
                p.tokenColor = optStr(po, "tokenColor", "red");
                p.currentPosition = optInt(po, "currentPosition", 0);
                p.turnCount = optInt(po, "turnCount", 0);
                p.consecutiveSixes = optInt(po, "consecutiveSixes", 0);
                p.connectedStatus = optStr(po, "connectedStatus", "ONLINE");
                s.players.add(p);
            }
        }
        if (obj.has("lastMove") && obj.get("lastMove").isJsonObject()) {
            s.lastMove = parseMove(obj.getAsJsonObject("lastMove"));
        }
        return s;
    }

    private static SnakesLaddersModels.InvitationState parseInvitation(JsonObject obj) {
        if (obj == null) return null;
        SnakesLaddersModels.InvitationState inv = new SnakesLaddersModels.InvitationState();
        inv.id = optStr(obj, "id", null);
        inv.senderId = optStr(obj, "sender_id", optStr(obj, "senderId", null));
        inv.receiverId = optStr(obj, "receiver_id", optStr(obj, "receiverId", null));
        inv.senderName = optStr(obj, "sender_name", optStr(obj, "senderName", "Friend"));
        inv.receiverName = optStr(obj, "receiver_name", optStr(obj, "receiverName", "Friend"));
        inv.conversationId = optStr(obj, "conversation_id", optStr(obj, "conversationId", null));
        inv.status = optStr(obj, "status", "PENDING");
        inv.gameId = optStr(obj, "game_id", optStr(obj, "gameId", null));
        inv.expiresAt = optStr(obj, "expires_at", optStr(obj, "expiresAt", null));
        return inv;
    }

    private interface Parser<T> {
        T parse(JsonObject json);
    }

    private static <T> void enqueueJsonCall(Request request, Parser<T> parser, ApiCallback<T> callback) {
        client().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postErr(callback, "Network error: " + e.getMessage(), 0, "NETWORK_ERROR");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response resp = response) {
                    String raw = resp.body() != null ? resp.body().string() : "";
                    try {
                        JsonObject json = !raw.isEmpty() ? JsonParser.parseString(raw).getAsJsonObject() : new JsonObject();
                        if (resp.isSuccessful() && optBool(json, "success", false)) {
                            T parsed = parser.parse(json);
                            postOk(callback, parsed);
                            return;
                        }
                        String msg = optStr(json, "error", "HTTP " + resp.code());
                        String code = optStr(json, "code", "HTTP_" + resp.code());
                        postErr(callback, msg, resp.code(), code);
                    } catch (Exception parseEx) {
                        postErr(callback, "Unexpected server response (HTTP " + resp.code() + ")", resp.code(), "PARSE_ERROR");
                    }
                }
            }
        });
    }

    private static <T> void postOk(ApiCallback<T> cb, T data) {
        if (cb != null) mainHandler.post(() -> cb.onSuccess(data));
    }

    private static <T> void postErr(ApiCallback<T> cb, String msg, int http, String code) {
        if (cb != null) mainHandler.post(() -> cb.onError(msg, http, code));
    }

    public static void createInvitation(Context ctx, String senderId, String receiverId, String conversationId,
                                        ApiCallback<SnakesLaddersModels.InvitationState> cb) {
        JsonObject body = new JsonObject();
        body.addProperty("senderId", senderId);
        body.addProperty("receiverId", receiverId);
        if (conversationId != null && !conversationId.isEmpty()) {
            body.addProperty("conversationId", conversationId);
        }
        Request req = new Request.Builder()
                .url(baseUrl(ctx) + "/invitations")
                .header("X-User-Id", senderId)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        enqueueJsonCall(req, json -> parseInvitation(json.getAsJsonObject("invitation")), cb);
    }

    public static void getInvitation(Context ctx, String invitationId, String userId,
                                     ApiCallback<SnakesLaddersModels.InvitationResult> cb) {
        String url = baseUrl(ctx) + "/invitations/" + Uri.encode(invitationId) + "?userId=" + Uri.encode(userId);
        Request req = new Request.Builder()
                .url(url)
                .header("X-User-Id", userId)
                .header("Cache-Control", "no-cache")
                .get()
                .build();
        enqueueJsonCall(req, json -> {
            SnakesLaddersModels.InvitationResult res = new SnakesLaddersModels.InvitationResult();
            if (json.has("invitation") && json.get("invitation").isJsonObject()) {
                res.invitation = parseInvitation(json.getAsJsonObject("invitation"));
            }
            if (json.has("session") && json.get("session").isJsonObject()) {
                res.session = parseSession(json.getAsJsonObject("session"));
            }
            return res;
        }, cb);
    }

    public static void acceptInvitation(Context ctx, String invitationId, String userId,
                                        ApiCallback<SnakesLaddersModels.InvitationResult> cb) {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        Request req = new Request.Builder()
                .url(baseUrl(ctx) + "/invitations/" + Uri.encode(invitationId) + "/accept")
                .header("X-User-Id", userId)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        enqueueJsonCall(req, json -> {
            SnakesLaddersModels.InvitationResult res = new SnakesLaddersModels.InvitationResult();
            res.idempotent = optBool(json, "idempotent", false);
            if (json.has("invitation") && json.get("invitation").isJsonObject()) {
                res.invitation = parseInvitation(json.getAsJsonObject("invitation"));
            }
            if (json.has("session") && json.get("session").isJsonObject()) {
                res.session = parseSession(json.getAsJsonObject("session"));
            }
            return res;
        }, cb);
    }

    public static void declineInvitation(Context ctx, String invitationId, String userId,
                                         ApiCallback<SnakesLaddersModels.InvitationState> cb) {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        Request req = new Request.Builder()
                .url(baseUrl(ctx) + "/invitations/" + Uri.encode(invitationId) + "/decline")
                .header("X-User-Id", userId)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        enqueueJsonCall(req, json -> parseInvitation(json.getAsJsonObject("invitation")), cb);
    }

    public static SnakesLaddersModels.InvitationEventsResult listenInvitationEventsSync(
            Context ctx, String invitationId, String userId) {
        if (ctx == null || invitationId == null || userId == null) return null;
        String url = baseUrl(ctx) + "/invitations/" + Uri.encode(invitationId) + "/events?userId=" + Uri.encode(userId);
        Request req = new Request.Builder()
                .url(url)
                .header("X-User-Id", userId)
                .header("Cache-Control", "no-cache")
                .get()
                .build();
        OkHttpClient pollClient = client().newBuilder().readTimeout(25, TimeUnit.SECONDS).build();
        try (Response resp = pollClient.newCall(req).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) return null;
            JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
            SnakesLaddersModels.InvitationEventsResult r = new SnakesLaddersModels.InvitationEventsResult();
            r.changed = optBool(json, "changed", false);
            r.timeout = optBool(json, "timeout", false);
            r.event = optStr(json, "event", null);
            if (json.has("invitation") && json.get("invitation").isJsonObject()) {
                r.invitation = parseInvitation(json.getAsJsonObject("invitation"));
            }
            if (json.has("session") && json.get("session").isJsonObject()) {
                r.session = parseSession(json.getAsJsonObject("session"));
            }
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    public static void getSession(Context ctx, String gameId, String userId,
                                  ApiCallback<SnakesLaddersModels.SessionState> cb) {
        String url = baseUrl(ctx) + "/" + Uri.encode(gameId) + "?userId=" + Uri.encode(userId);
        Request req = new Request.Builder()
                .url(url)
                .header("X-User-Id", userId)
                .header("Cache-Control", "no-cache")
                .get()
                .build();
        enqueueJsonCall(req, json -> parseSession(json.getAsJsonObject("session")), cb);
    }

    public static SnakesLaddersModels.SessionEventsResult listenSessionEventsSync(
            Context ctx, String gameId, String userId, int sinceVersion) {
        if (ctx == null || gameId == null || userId == null) return null;
        String url = baseUrl(ctx) + "/" + Uri.encode(gameId) + "/events?userId=" + Uri.encode(userId) + "&sinceVersion=" + sinceVersion;
        Request req = new Request.Builder()
                .url(url)
                .header("X-User-Id", userId)
                .header("Cache-Control", "no-cache")
                .get()
                .build();
        OkHttpClient pollClient = client().newBuilder().readTimeout(25, TimeUnit.SECONDS).build();
        try (Response resp = pollClient.newCall(req).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) return null;
            JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
            SnakesLaddersModels.SessionEventsResult r = new SnakesLaddersModels.SessionEventsResult();
            r.changed = optBool(json, "changed", false);
            r.timeout = optBool(json, "timeout", false);
            r.event = optStr(json, "event", null);
            if (json.has("move") && json.get("move").isJsonObject()) {
                r.move = parseMove(json.getAsJsonObject("move"));
            }
            if (json.has("session") && json.get("session").isJsonObject()) {
                r.session = parseSession(json.getAsJsonObject("session"));
            }
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    public static void rollDice(Context ctx, String gameId, String userId, String actionId,
                                ApiCallback<SnakesLaddersModels.RollResult> cb) {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        body.addProperty("actionId", actionId);
        Request req = new Request.Builder()
                .url(baseUrl(ctx) + "/" + Uri.encode(gameId) + "/roll")
                .header("X-User-Id", userId)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        enqueueJsonCall(req, json -> {
            SnakesLaddersModels.RollResult res = new SnakesLaddersModels.RollResult();
            res.idempotentReplay = optBool(json, "idempotentReplay", false);
            if (json.has("move") && json.get("move").isJsonObject()) {
                res.move = parseMove(json.getAsJsonObject("move"));
            }
            if (json.has("session") && json.get("session").isJsonObject()) {
                res.session = parseSession(json.getAsJsonObject("session"));
            }
            return res;
        }, cb);
    }

    public static void resignGame(Context ctx, String gameId, String userId,
                                  ApiCallback<SnakesLaddersModels.SessionState> cb) {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        Request req = new Request.Builder()
                .url(baseUrl(ctx) + "/" + Uri.encode(gameId) + "/resign")
                .header("X-User-Id", userId)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        enqueueJsonCall(req, json -> parseSession(json.getAsJsonObject("session")), cb);
    }

    public static void reconnect(Context ctx, String gameId, String userId,
                                 ApiCallback<SnakesLaddersModels.SessionState> cb) {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        Request req = new Request.Builder()
                .url(baseUrl(ctx) + "/" + Uri.encode(gameId) + "/reconnect")
                .header("X-User-Id", userId)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        enqueueJsonCall(req, json -> parseSession(json.getAsJsonObject("session")), cb);
    }

    public static void notifyDisconnectBestEffort(Context ctx, String gameId, String userId) {
        if (ctx == null || gameId == null || userId == null) return;
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        Request req = new Request.Builder()
                .url(baseUrl(ctx) + "/" + Uri.encode(gameId) + "/disconnect")
                .header("X-User-Id", userId)
                .post(RequestBody.create(body.toString(), JSON))
                .build();
        client().newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) { response.close(); }
        });
    }

    public static void getHistory(Context ctx, String userId, ApiCallback<SnakesLaddersModels.HistoryResponse> cb) {
        String url = baseUrl(ctx) + "/history?userId=" + Uri.encode(userId);
        Request req = new Request.Builder()
                .url(url)
                .header("X-User-Id", userId)
                .header("Cache-Control", "no-cache")
                .get()
                .build();
        enqueueJsonCall(req, json -> {
            SnakesLaddersModels.HistoryResponse hr = new SnakesLaddersModels.HistoryResponse();
            if (json.has("stats") && json.get("stats").isJsonObject()) {
                JsonObject st = json.getAsJsonObject("stats");
                hr.totalGames = optInt(st, "totalGames", 0);
                hr.totalWins = optInt(st, "totalWins", 0);
                hr.winRatePercent = optInt(st, "winRatePercent", 0);
                hr.totalPlaySeconds = optLong(st, "totalPlaySeconds", 0);
            }
            if (json.has("matches") && json.get("matches").isJsonArray()) {
                for (JsonElement el : json.getAsJsonArray("matches")) {
                    if (!el.isJsonObject()) continue;
                    JsonObject mo = el.getAsJsonObject();
                    SnakesLaddersModels.HistoryMatch m = new SnakesLaddersModels.HistoryMatch();
                    m.gameId = optStr(mo, "gameId", "");
                    m.opponentId = optStr(mo, "opponentId", "");
                    m.opponentName = optStr(mo, "opponentName", "Friend");
                    m.opponentAvatar = optStr(mo, "opponentAvatar", null);
                    m.winnerId = optStr(mo, "winnerId", null);
                    m.winnerName = optStr(mo, "winnerName", null);
                    m.didWin = optBool(mo, "didWin", false);
                    m.resignedBy = optStr(mo, "resignedBy", null);
                    m.myFinalPosition = optInt(mo, "myFinalPosition", 0);
                    m.opponentFinalPosition = optInt(mo, "opponentFinalPosition", 0);
                    m.durationSeconds = optLong(mo, "durationSeconds", 0);
                    m.createdAt = optStr(mo, "createdAt", "");
                    m.finishedAt = optStr(mo, "finishedAt", "");
                    hr.matches.add(m);
                }
            }
            return hr;
        }, cb);
    }
}
