package com.zez.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonObject;
import com.zez.app.CallActivity;
import com.zez.app.util.ChatApiClient;
import com.zez.app.util.ChatNotificationHelper;
import com.zez.app.util.ChatPreferencesHelper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Handles Answer / Decline actions directly from the incoming call notification.
 */
public class IncomingCallActionReceiver extends BroadcastReceiver {
    public static final String ACTION_ANSWER = "com.zez.app.ACTION_ANSWER_CALL";
    public static final String ACTION_DECLINE = "com.zez.app.ACTION_DECLINE_CALL";

    public static final String EXTRA_CALL_ID = "extra_call_id";
    public static final String EXTRA_CALL_TYPE = "extra_call_type";
    public static final String EXTRA_CALLER_ID = "extra_caller_id";
    public static final String EXTRA_CALLER_NAME = "extra_caller_name";
    public static final String EXTRA_CALLER_AVATAR = "extra_caller_avatar";
    public static final String EXTRA_CALLER_PHONE = "extra_caller_phone";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        String callId = intent.getStringExtra(EXTRA_CALL_ID);
        ChatNotificationHelper.cancelIncomingCallNotification(context);

        if (callId == null || callId.isEmpty()) return;
        String userId = ChatPreferencesHelper.getUserId(context);
        OkHttpClient http = ChatApiClient.getOkHttpClient();

        if (ACTION_DECLINE.equals(action)) {
            PendingResult pr = goAsync();
            String url = ChatApiClient.getBaseUrl(context) + "/api/call/respond";
            JsonObject body = new JsonObject();
            body.addProperty("callId", callId);
            if (userId != null) body.addProperty("userId", userId);
            body.addProperty("action", "reject");
            Request req = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json; charset=utf-8")))
                    .build();
            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) { pr.finish(); }
                @Override public void onResponse(Call call, Response response) {
                    try { if (response.body() != null) response.body().close(); } catch (Exception ignored) {}
                    pr.finish();
                }
            });
        } else if (ACTION_ANSWER.equals(action)) {
            PendingResult pr = goAsync();
            String url = ChatApiClient.getBaseUrl(context) + "/api/call/respond";
            JsonObject body = new JsonObject();
            body.addProperty("callId", callId);
            if (userId != null) body.addProperty("userId", userId);
            body.addProperty("action", "accept");
            Request req = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json; charset=utf-8")))
                    .build();
            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) { pr.finish(); }
                @Override public void onResponse(Call call, Response response) {
                    try { if (response.body() != null) response.body().close(); } catch (Exception ignored) {}
                    pr.finish();
                }
            });

            Intent callIntent = new Intent(context, CallActivity.class);
            callIntent.putExtra(CallActivity.EXTRA_CALL_ID, callId);
            callIntent.putExtra(CallActivity.EXTRA_IS_CALLER, false);
            callIntent.putExtra(CallActivity.EXTRA_CALL_TYPE, intent.getStringExtra(EXTRA_CALL_TYPE));
            callIntent.putExtra(CallActivity.EXTRA_PEER_ID, intent.getStringExtra(EXTRA_CALLER_ID));
            callIntent.putExtra(CallActivity.EXTRA_PEER_NAME, intent.getStringExtra(EXTRA_CALLER_NAME));
            callIntent.putExtra(CallActivity.EXTRA_PEER_AVATAR, intent.getStringExtra(EXTRA_CALLER_AVATAR));
            callIntent.putExtra(CallActivity.EXTRA_PEER_PHONE, intent.getStringExtra(EXTRA_CALLER_PHONE));
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(callIntent);
        }
    }
}
