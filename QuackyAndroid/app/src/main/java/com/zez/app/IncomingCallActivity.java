package com.zez.app;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

public class IncomingCallActivity extends AppCompatActivity {
    public static final String EXTRA_CALL_ID = "extra_call_id";
    public static final String EXTRA_CALL_TYPE = "extra_call_type";
    public static final String EXTRA_CALLER_ID = "extra_caller_id";
    public static final String EXTRA_CALLER_NAME = "extra_caller_name";
    public static final String EXTRA_CALLER_AVATAR = "extra_caller_avatar";
    public static final String EXTRA_CALLER_PHONE = "extra_caller_phone";

    private String callId;
    private String callType = "video";
    private String callerId;
    private String callerName;
    private String callerAvatar;
    private String callerPhone;

    private Ringtone ringtone;
    private Vibrator vibrator;
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private boolean isFinished = false;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        com.zez.app.theme.ThemeManager.applyTheme(this, true);
        super.onCreate(savedInstanceState);

        // Turn screen on and show over lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        setContentView(R.layout.activity_incoming_call);

        callId = getIntent().getStringExtra(EXTRA_CALL_ID);
        callType = getIntent().getStringExtra(EXTRA_CALL_TYPE);
        if (callType == null) callType = "video";
        callerId = getIntent().getStringExtra(EXTRA_CALLER_ID);
        callerName = getIntent().getStringExtra(EXTRA_CALLER_NAME);
        callerAvatar = getIntent().getStringExtra(EXTRA_CALLER_AVATAR);
        callerPhone = getIntent().getStringExtra(EXTRA_CALLER_PHONE);

        TextView tvCallType = findViewById(R.id.tv_incoming_call_type);
        TextView tvCallerName = findViewById(R.id.tv_incoming_caller_name);
        TextView tvCallerPhone = findViewById(R.id.tv_incoming_caller_phone);
        ImageView ivAvatar = findViewById(R.id.iv_incoming_caller_avatar);
        ImageButton btnDecline = findViewById(R.id.btn_incoming_decline);
        ImageButton btnAccept = findViewById(R.id.btn_incoming_accept);
        TextView tvAcceptLabel = findViewById(R.id.tv_incoming_accept_label);

        boolean isVideo = "video".equalsIgnoreCase(callType);
        tvCallType.setText(isVideo ? "Incoming Quacky Video Call" : "Incoming Quacky Voice Call");
        tvCallerName.setText(callerName != null && !callerName.isEmpty() ? callerName : "Quacky Contact");
        if (callerPhone != null && !callerPhone.isEmpty()) {
            tvCallerPhone.setText(callerPhone.startsWith("+") ? callerPhone : "+91 " + callerPhone);
            tvCallerPhone.setVisibility(View.VISIBLE);
        } else {
            tvCallerPhone.setVisibility(View.GONE);
        }

        if (isVideo) {
            btnAccept.setImageResource(R.drawable.ic_videocall);
            tvAcceptLabel.setText("Answer Video");
        } else {
            btnAccept.setImageResource(R.drawable.ic_call);
            tvAcceptLabel.setText("Answer Voice");
        }

        if (callerAvatar != null && !callerAvatar.isEmpty()) {
            String fullUrl = ChatApiClient.getAbsoluteMediaUrl(this, callerAvatar);
            Glide.with(this)
                    .load(fullUrl)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_person)
                    .into(ivAvatar);
        }

        btnDecline.setOnClickListener(v -> declineCall());
        btnAccept.setOnClickListener(v -> acceptCall());

        startRinging();
        startStatusCheck();
    }

    private void startRinging() {
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (alert == null) alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), alert);
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());
                }
                ringtone.play();
            }

            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pattern = {0, 600, 800, 600, 800};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 1));
                } else {
                    vibrator.vibrate(pattern, 1);
                }
            }
        } catch (Exception ignored) {}
    }

    private void stopRinging() {
        try {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
        } catch (Exception ignored) {}
        try {
            if (vibrator != null) {
                vibrator.cancel();
            }
        } catch (Exception ignored) {}
    }

    private final Runnable checkerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isFinished || isFinishing()) return;
            String url = ChatApiClient.getBaseUrl(IncomingCallActivity.this) + "/api/call/status?callId=" + callId;
            Request req = new Request.Builder().url(url).build();
            httpClient.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (!isFinished) pollHandler.postDelayed(checkerRunnable, 2000);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (isFinished) return;
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String body = response.body().string();
                            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                            String st = json.has("status") ? json.get("status").getAsString() : "";
                            if ("ended".equalsIgnoreCase(st) || "missed".equalsIgnoreCase(st) || "rejected".equalsIgnoreCase(st)) {
                                pollHandler.post(() -> {
                                    stopRinging();
                                    Toast.makeText(IncomingCallActivity.this, "Call ended by caller", Toast.LENGTH_SHORT).show();
                                    isFinished = true;
                                    finish();
                                });
                                return;
                            }
                        } catch (Exception ignored) {}
                    }
                    if (!isFinished) pollHandler.postDelayed(checkerRunnable, 2000);
                }
            });
        }
    };

    private void startStatusCheck() {
        if (callId == null || callId.isEmpty()) return;
        pollHandler.postDelayed(checkerRunnable, 2000);
    }

    private void acceptCall() {
        if (isFinished) return;
        isFinished = true;
        stopRinging();
        ChatNotificationHelper.cancelIncomingCallNotification(this);

        String currentUserId = ChatPreferencesHelper.getUserId(this);
        String url = ChatApiClient.getBaseUrl(this) + "/api/call/respond";
        JsonObject json = new JsonObject();
        json.addProperty("callId", callId);
        json.addProperty("userId", currentUserId);
        json.addProperty("action", "accept");

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request req = new Request.Builder().url(url).post(body).build();
        httpClient.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> launchCallActivity());
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> launchCallActivity());
            }
        });
    }

    private void launchCallActivity() {
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra(CallActivity.EXTRA_CALL_ID, callId);
        intent.putExtra(CallActivity.EXTRA_IS_CALLER, false);
        intent.putExtra(CallActivity.EXTRA_CALL_TYPE, callType);
        intent.putExtra(CallActivity.EXTRA_PEER_ID, callerId);
        intent.putExtra(CallActivity.EXTRA_PEER_NAME, callerName);
        intent.putExtra(CallActivity.EXTRA_PEER_AVATAR, callerAvatar);
        intent.putExtra(CallActivity.EXTRA_PEER_PHONE, callerPhone);
        startActivity(intent);
        finish();
    }

    private void declineCall() {
        if (isFinished) return;
        isFinished = true;
        stopRinging();
        ChatNotificationHelper.cancelIncomingCallNotification(this);
        ChatNotificationHelper.clearHandledCall(callId);

        String currentUserId = ChatPreferencesHelper.getUserId(this);
        String url = ChatApiClient.getBaseUrl(this) + "/api/call/respond";
        JsonObject json = new JsonObject();
        json.addProperty("callId", callId);
        json.addProperty("userId", currentUserId);
        json.addProperty("action", "reject");

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request req = new Request.Builder().url(url).post(body).build();
        httpClient.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                finish();
            }

            @Override
            public void onResponse(Call call, Response response) {
                finish();
            }
        });
        finish();
    }

    @Override
    protected void onDestroy() {
        isFinished = true;
        stopRinging();
        ChatNotificationHelper.clearHandledCall(callId);
        ChatNotificationHelper.cancelIncomingCallNotification(this);
        pollHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
