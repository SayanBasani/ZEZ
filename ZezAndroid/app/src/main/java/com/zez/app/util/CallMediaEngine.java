package com.zez.app.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Full-duplex low-latency media transport & IMA-ADPCM voice engine for Quacky calls.
 * - Primary transport: binary WebSocket (/ws/call) with automatic RTT measurement and reconnection.
 * - Adaptive bandwidth: 16 kHz IMA-ADPCM (~64 kbps) on normal links; 8 kHz (~32 kbps) on weak links.
 * - Hardware DSP: AcousticEchoCanceler, NoiseSuppressor, AutomaticGainControl when available.
 * - Fallback transport: HTTP /api/call/audio & /api/call/frame when WebSockets cannot connect.
 */
public class CallMediaEngine {
    private static final String TAG = "CallMediaEngine";

    public static final byte PKT_AUDIO = 0x01;
    public static final byte PKT_VIDEO = 0x02;
    public static final byte PKT_PING = 0x03;
    public static final byte PKT_PONG = 0x04;
    public static final byte PKT_CTRL = 0x05;

    public static final int TIER_GOOD = 0;
    public static final int TIER_MEDIUM = 1;
    public static final int TIER_LOW = 2;

    private static final int PLAYBACK_RATE = 16000;
    private static final int CAPTURE_RATE = 16000;
    private static final int SAMPLES_PER_FRAME_16K = 640; // 40ms @ 16kHz

    private static final int[] IMA_INDEX_TABLE = {
            -1, -1, -1, -1, 2, 4, 6, 8,
            -1, -1, -1, -1, 2, 4, 6, 8
    };
    private static final int[] IMA_STEP_TABLE = {
            7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
            50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
            337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
            2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
            15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
    };

    public interface MediaListener {
        void onRemoteVideoFrame(byte[] jpegBytes);
        void onRemoteHangup(String reason);
        void onNetworkQualityChanged(int tier, int rttMs);
    }

    private final Context appContext;
    private final String callId;
    private final String userId;
    private final String peerId;
    private final MediaListener listener;

    private final OkHttpClient wsHttpClient;
    private final OkHttpClient mediaHttpClient;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile WebSocket webSocket;
    private final AtomicBoolean wsConnected = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final AtomicBoolean muted = new AtomicBoolean(false);
    private final AtomicInteger networkTier = new AtomicInteger(TIER_GOOD);
    private final AtomicInteger latestRttMs = new AtomicInteger(0);

    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private AcousticEchoCanceler echoCanceler;
    private NoiseSuppressor noiseSuppressor;
    private AutomaticGainControl gainControl;

    private Thread captureThread;
    private Thread playbackThread;
    private Thread httpFallbackThread;
    private final ArrayBlockingQueue<short[]> jitterQueue = new ArrayBlockingQueue<>(10);
    private volatile int httpAudioPollSeq = 0;

    private final AtomicInteger wsReconnectAttempts = new AtomicInteger(0);
    private final Runnable pingRunnable = new Runnable() {
        @Override
        public void run() {
            if (stopped.get()) return;
            WebSocket ws = webSocket;
            if (ws != null && wsConnected.get()) {
                ByteBuffer buf = ByteBuffer.allocate(9);
                buf.put(PKT_PING);
                buf.putLong(System.currentTimeMillis());
                ws.send(ByteString.of(buf.array()));
            }
            mainHandler.postDelayed(this, 2000);
        }
    };

    public CallMediaEngine(Context context, String callId, String userId, String peerId, MediaListener listener) {
        this.appContext = context.getApplicationContext();
        this.callId = callId;
        this.userId = userId;
        this.peerId = peerId;
        this.listener = listener;

        this.wsHttpClient = ChatApiClient.getOkHttpClient().newBuilder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .pingInterval(15, TimeUnit.SECONDS)
                .build();

        this.mediaHttpClient = ChatApiClient.getOkHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    public void start() {
        if (stopped.get()) return;
        connectWebSocket();
        mainHandler.postDelayed(pingRunnable, 1200);
        startAudioThreads();
        startHttpAudioFallbackLoop();
    }

    public void setMuted(boolean isMuted) {
        this.muted.set(isMuted);
    }

    public int getNetworkTier() {
        return networkTier.get();
    }

    public boolean isWsConnected() {
        return wsConnected.get();
    }

    public boolean canSendVideoFrame() {
        WebSocket ws = webSocket;
        if (ws != null && wsConnected.get()) {
            return ws.queueSize() < 56 * 1024L;
        }
        return true;
    }

    public void sendVideoFrame(byte[] jpegBytes) {
        if (stopped.get() || jpegBytes == null || jpegBytes.length == 0) return;
        WebSocket ws = webSocket;
        if (ws != null && wsConnected.get()) {
            if (ws.queueSize() > 56 * 1024L) {
                // Prioritize live audio over queued video on congested networks
                updateQualityTier(TIER_LOW, latestRttMs.get());
                return;
            }
            byte[] packet = new byte[1 + jpegBytes.length];
            packet[0] = PKT_VIDEO;
            System.arraycopy(jpegBytes, 0, packet, 1, jpegBytes.length);
            ws.send(ByteString.of(packet));
            return;
        }

        // HTTP fallback if WebSocket is unavailable
        try {
            String url = ChatApiClient.getBaseUrl(appContext) + "/api/call/frame";
            JsonObject json = new JsonObject();
            json.addProperty("callId", callId);
            json.addProperty("senderId", userId);
            json.addProperty("frameBase64", Base64.encodeToString(jpegBytes, Base64.NO_WRAP));
            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request req = new Request.Builder().url(url).post(body).build();
            mediaHttpClient.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) { updateQualityTier(TIER_LOW, 800); }
                @Override public void onResponse(Call call, Response response) {
                    try { if (response.body() != null) response.body().close(); } catch (Exception ignored) {}
                }
            });
        } catch (Exception ignored) {}
    }

    public void enableAudioIfPermitted() {
        if (stopped.get()) return;
        if (audioRecord == null) {
            startAudioThreads();
        }
    }

    private void connectWebSocket() {
        if (stopped.get()) return;
        String baseUrl = ChatApiClient.getBaseUrl(appContext);
        String wsBase = baseUrl.startsWith("https://")
                ? "wss://" + baseUrl.substring(8)
                : (baseUrl.startsWith("http://") ? "ws://" + baseUrl.substring(7) : "ws://" + baseUrl);
        String url = wsBase + "/ws/call?callId=" + Uri.encode(callId) + "&userId=" + Uri.encode(userId);

        Request req = new Request.Builder().url(url).build();
        webSocket = wsHttpClient.newWebSocket(req, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket ws, @NonNull Response response) {
                wsConnected.set(true);
                wsReconnectAttempts.set(0);
                Log.i(TAG, "Call WebSocket connected cleanly");
            }

            @Override
            public void onMessage(@NonNull WebSocket ws, @NonNull ByteString bytes) {
                if (stopped.get() || bytes.size() < 1) return;
                byte[] data = bytes.toByteArray();
                byte type = data[0];
                if (type == PKT_AUDIO && data.length > 5) {
                    short[] pcm16 = decodeAdpcmTo16k(data, 1, data.length - 1);
                    if (pcm16 != null) {
                        enqueueRemoteAudio(pcm16);
                    }
                } else if (type == PKT_VIDEO && data.length > 16) {
                    byte[] jpeg = new byte[data.length - 1];
                    System.arraycopy(data, 1, jpeg, 0, jpeg.length);
                    if (listener != null) {
                        listener.onRemoteVideoFrame(jpeg);
                    }
                } else if (type == PKT_PONG && data.length >= 9) {
                    long sentTs = ByteBuffer.wrap(data, 1, 8).getLong();
                    int rtt = (int) Math.max(1, Math.min(10000, System.currentTimeMillis() - sentTs));
                    latestRttMs.set(rtt);
                    long q = ws.queueSize();
                    int tier = (rtt > 420 || q > 48 * 1024L) ? TIER_LOW : ((rtt > 160 || q > 20 * 1024L) ? TIER_MEDIUM : TIER_GOOD);
                    updateQualityTier(tier, rtt);
                } else if (type == PKT_CTRL && data.length > 2) {
                    try {
                        String msg = new String(data, 1, data.length - 1);
                        JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
                        String ctrlType = json.has("type") ? json.get("type").getAsString() : "";
                        if ("ended".equalsIgnoreCase(ctrlType) || "rejected".equalsIgnoreCase(ctrlType)) {
                            if (listener != null) {
                                mainHandler.post(() -> listener.onRemoteHangup(ctrlType));
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onFailure(@NonNull WebSocket ws, @NonNull Throwable t, @Nullable Response response) {
                wsConnected.set(false);
                updateQualityTier(TIER_LOW, 600);
                scheduleReconnect();
            }

            @Override
            public void onClosed(@NonNull WebSocket ws, int code, @NonNull String reason) {
                wsConnected.set(false);
                if (!stopped.get()) scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        if (stopped.get()) return;
        int attempt = Math.min(6, wsReconnectAttempts.incrementAndGet());
        long delayMs = 400L * attempt;
        mainHandler.postDelayed(() -> {
            if (!stopped.get() && !wsConnected.get()) {
                connectWebSocket();
            }
        }, delayMs);
    }

    private void updateQualityTier(int tier, int rttMs) {
        int prev = networkTier.getAndSet(tier);
        if (prev != tier && listener != null) {
            mainHandler.post(() -> listener.onNetworkQualityChanged(tier, rttMs));
        }
    }

    private void enqueueRemoteAudio(short[] pcm16) {
        if (jitterQueue.remainingCapacity() == 0) {
            // Drain oldest packets to keep voice real-time (< 200ms) instead of building multi-second lag
            while (jitterQueue.size() > 2) {
                jitterQueue.poll();
            }
        }
        jitterQueue.offer(pcm16);
    }

    private synchronized void startAudioThreads() {
        if (stopped.get()) return;

        // 1. Playback Track (works even before mic permission granted)
        if (audioTrack == null) {
            try {
                int minPlayBuf = AudioTrack.getMinBufferSize(
                        PLAYBACK_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );
                int trackBuf = Math.max(minPlayBuf, SAMPLES_PER_FRAME_16K * 2 * 4);
                AudioAttributes attrs = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                AudioFormat fmt = new AudioFormat.Builder()
                        .setSampleRate(PLAYBACK_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build();
                audioTrack = new AudioTrack.Builder()
                        .setAudioAttributes(attrs)
                        .setAudioFormat(fmt)
                        .setBufferSizeInBytes(trackBuf)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build();
                audioTrack.play();

                playbackThread = new Thread(() -> {
                    byte[] pcmBytes = new byte[SAMPLES_PER_FRAME_16K * 2];
                    while (!stopped.get()) {
                        try {
                            short[] samples = jitterQueue.poll(100, TimeUnit.MILLISECONDS);
                            if (samples == null || audioTrack == null) continue;
                            if (pcmBytes.length != samples.length * 2) {
                                pcmBytes = new byte[samples.length * 2];
                            }
                            for (int i = 0; i < samples.length; i++) {
                                short s = samples[i];
                                pcmBytes[i * 2] = (byte) (s & 0xFF);
                                pcmBytes[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
                            }
                            audioTrack.write(pcmBytes, 0, pcmBytes.length);
                        } catch (InterruptedException ie) {
                            break;
                        } catch (Throwable ignored) {}
                    }
                }, "CallAudioPlayback");
                playbackThread.setPriority(Thread.MAX_PRIORITY);
                playbackThread.start();
            } catch (Throwable e) {
                Log.e(TAG, "AudioTrack init error: " + e.getMessage());
            }
        }

        // 2. Capture Record (requires RECORD_AUDIO)
        if (audioRecord == null && ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            try {
                int minRecBuf = AudioRecord.getMinBufferSize(
                        CAPTURE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );
                int recBuf = Math.max(minRecBuf, SAMPLES_PER_FRAME_16K * 2 * 4);
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                        CAPTURE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        recBuf
                );
                if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                    audioRecord.release();
                    audioRecord = null;
                    return;
                }

                int sessionId = audioRecord.getAudioSessionId();
                try {
                    if (AcousticEchoCanceler.isAvailable()) {
                        echoCanceler = AcousticEchoCanceler.create(sessionId);
                        if (echoCanceler != null) echoCanceler.setEnabled(true);
                    }
                    if (NoiseSuppressor.isAvailable()) {
                        noiseSuppressor = NoiseSuppressor.create(sessionId);
                        if (noiseSuppressor != null) noiseSuppressor.setEnabled(true);
                    }
                    if (AutomaticGainControl.isAvailable()) {
                        gainControl = AutomaticGainControl.create(sessionId);
                        if (gainControl != null) gainControl.setEnabled(true);
                    }
                } catch (Throwable ignored) {}

                audioRecord.startRecording();
                captureThread = new Thread(() -> {
                    short[] readBuf = new short[SAMPLES_PER_FRAME_16K];
                    short[] lowRateBuf = new short[SAMPLES_PER_FRAME_16K / 2];
                    while (!stopped.get() && audioRecord != null) {
                        int n = audioRecord.read(readBuf, 0, readBuf.length);
                        if (n <= 0) continue;
                        if (muted.get()) continue;

                        boolean useLowRate = (networkTier.get() == TIER_LOW);
                        byte[] adpcmPayload;
                        if (useLowRate) {
                            int outLen = n / 2;
                            for (int i = 0; i < outLen; i++) {
                                lowRateBuf[i] = (short) ((readBuf[i * 2] + readBuf[i * 2 + 1]) >> 1);
                            }
                            adpcmPayload = encodeAdpcmFrame(lowRateBuf, outLen, (byte) 0);
                        } else {
                            adpcmPayload = encodeAdpcmFrame(readBuf, n, (byte) 1);
                        }

                        WebSocket ws = webSocket;
                        if (ws != null && wsConnected.get()) {
                            byte[] pkt = new byte[1 + adpcmPayload.length];
                            pkt[0] = PKT_AUDIO;
                            System.arraycopy(adpcmPayload, 0, pkt, 1, adpcmPayload.length);
                            ws.send(ByteString.of(pkt));
                        } else {
                            postAudioHttpFallback(adpcmPayload);
                        }
                    }
                }, "CallAudioCapture");
                captureThread.setPriority(Thread.MAX_PRIORITY);
                captureThread.start();
            } catch (Throwable e) {
                Log.e(TAG, "AudioRecord init error: " + e.getMessage());
            }
        }
    }

    private void postAudioHttpFallback(byte[] adpcmPayload) {
        try {
            String url = ChatApiClient.getBaseUrl(appContext) + "/api/call/audio";
            JsonObject json = new JsonObject();
            json.addProperty("callId", callId);
            json.addProperty("senderId", userId);
            json.addProperty("chunkBase64", Base64.encodeToString(adpcmPayload, Base64.NO_WRAP));
            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request req = new Request.Builder().url(url).post(body).build();
            mediaHttpClient.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {}
                @Override public void onResponse(Call call, Response response) {
                    try { if (response.body() != null) response.body().close(); } catch (Exception ignored) {}
                }
            });
        } catch (Exception ignored) {}
    }

    private void startHttpAudioFallbackLoop() {
        httpFallbackThread = new Thread(() -> {
            while (!stopped.get()) {
                try {
                    if (wsConnected.get() || peerId == null || peerId.isEmpty()) {
                        Thread.sleep(300);
                        continue;
                    }
                    String url = ChatApiClient.getBaseUrl(appContext) + "/api/call/audio?callId="
                            + Uri.encode(callId) + "&senderId=" + Uri.encode(peerId) + "&afterSeq=" + httpAudioPollSeq;
                    Request req = new Request.Builder().url(url).get().build();
                    try (Response resp = mediaHttpClient.newCall(req).execute()) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
                            if (json.has("lastSeq")) {
                                httpAudioPollSeq = json.get("lastSeq").getAsInt();
                            }
                            if (json.has("chunks") && json.get("chunks").isJsonArray()) {
                                JsonArray arr = json.getAsJsonArray("chunks");
                                for (JsonElement el : arr) {
                                    byte[] raw = Base64.decode(el.getAsString(), Base64.DEFAULT);
                                    short[] pcm16 = decodeAdpcmTo16k(raw, 0, raw.length);
                                    if (pcm16 != null) enqueueRemoteAudio(pcm16);
                                }
                            }
                        }
                    }
                    Thread.sleep(70);
                } catch (InterruptedException ie) {
                    break;
                } catch (Exception ignored) {
                    try { Thread.sleep(200); } catch (Exception ignored2) { break; }
                }
            }
        }, "CallHttpAudioFallback");
        httpFallbackThread.start();
    }

    public static byte[] encodeAdpcmFrame(short[] pcm, int sampleCount, byte rateCode) {
        int count = sampleCount & ~1;
        if (count < 2) count = 2;
        byte[] out = new byte[4 + (count / 2)];
        out[0] = rateCode;
        int prevSample = pcm[0];
        out[1] = (byte) (prevSample & 0xFF);
        out[2] = (byte) ((prevSample >> 8) & 0xFF);
        int index = 0;
        out[3] = 0;

        for (int i = 0; i < count; i++) {
            int sample = pcm[i];
            int step = IMA_STEP_TABLE[index];
            int diff = sample - prevSample;
            int nibble = 0;
            if (diff < 0) {
                nibble = 8;
                diff = -diff;
            }
            int mask = 4;
            int tempStep = step;
            for (int b = 0; b < 3; b++) {
                if (diff >= tempStep) {
                    nibble |= mask;
                    diff -= tempStep;
                }
                tempStep >>= 1;
                mask >>= 1;
            }

            int vpdiff = step >> 3;
            if ((nibble & 4) != 0) vpdiff += step;
            if ((nibble & 2) != 0) vpdiff += step >> 1;
            if ((nibble & 1) != 0) vpdiff += step >> 2;
            if ((nibble & 8) != 0) prevSample -= vpdiff;
            else prevSample += vpdiff;
            if (prevSample > 32767) prevSample = 32767;
            else if (prevSample < -32768) prevSample = -32768;

            index += IMA_INDEX_TABLE[nibble];
            if (index < 0) index = 0;
            else if (index > 88) index = 88;

            int outIdx = 4 + (i >> 1);
            if ((i & 1) == 0) {
                out[outIdx] = (byte) (nibble & 0x0F);
            } else {
                out[outIdx] |= (byte) ((nibble & 0x0F) << 4);
            }
        }
        return out;
    }

    public static short[] decodeAdpcmTo16k(byte[] frame, int offset, int len) {
        if (frame == null || len < 5) return null;
        byte rateCode = frame[offset];
        int prevSample = (frame[offset + 1] & 0xFF) | (frame[offset + 2] << 8);
        int index = frame[offset + 3] & 0xFF;
        if (index > 88) index = 88;

        int dataBytes = len - 4;
        int sampleCount = dataBytes * 2;
        short[] decoded = new short[sampleCount];

        for (int i = 0; i < sampleCount; i++) {
            int byteVal = frame[offset + 4 + (i >> 1)] & 0xFF;
            int nibble = ((i & 1) == 0) ? (byteVal & 0x0F) : ((byteVal >> 4) & 0x0F);
            int step = IMA_STEP_TABLE[index];

            int vpdiff = step >> 3;
            if ((nibble & 4) != 0) vpdiff += step;
            if ((nibble & 2) != 0) vpdiff += step >> 1;
            if ((nibble & 1) != 0) vpdiff += step >> 2;
            if ((nibble & 8) != 0) prevSample -= vpdiff;
            else prevSample += vpdiff;
            if (prevSample > 32767) prevSample = 32767;
            else if (prevSample < -32768) prevSample = -32768;

            index += IMA_INDEX_TABLE[nibble];
            if (index < 0) index = 0;
            else if (index > 88) index = 88;

            decoded[i] = (short) prevSample;
        }

        if (rateCode == 0) {
            // 8 kHz -> smooth linear 2x upsample to 16 kHz AudioTrack without recreating track
            short[] up = new short[sampleCount * 2];
            for (int i = 0; i < sampleCount; i++) {
                short s0 = decoded[i];
                short s1 = (i + 1 < sampleCount) ? decoded[i + 1] : s0;
                up[i * 2] = s0;
                up[i * 2 + 1] = (short) ((s0 + s1) >> 1);
            }
            return up;
        }
        return decoded;
    }

    public synchronized void stop() {
        if (stopped.getAndSet(true)) return;
        mainHandler.removeCallbacksAndMessages(null);
        wsConnected.set(false);

        if (webSocket != null) {
            try { webSocket.close(1000, "Call ended"); } catch (Exception ignored) {}
            webSocket = null;
        }

        if (captureThread != null) captureThread.interrupt();
        if (playbackThread != null) playbackThread.interrupt();
        if (httpFallbackThread != null) httpFallbackThread.interrupt();
        jitterQueue.clear();

        try { if (echoCanceler != null) { echoCanceler.release(); echoCanceler = null; } } catch (Exception ignored) {}
        try { if (noiseSuppressor != null) { noiseSuppressor.release(); noiseSuppressor = null; } } catch (Exception ignored) {}
        try { if (gainControl != null) { gainControl.release(); gainControl = null; } } catch (Exception ignored) {}

        if (audioRecord != null) {
            try { audioRecord.stop(); } catch (Exception ignored) {}
            try { audioRecord.release(); } catch (Exception ignored) {}
            audioRecord = null;
        }
        if (audioTrack != null) {
            try { audioTrack.pause(); audioTrack.flush(); audioTrack.stop(); } catch (Exception ignored) {}
            try { audioTrack.release(); } catch (Exception ignored) {}
            audioTrack = null;
        }
    }
}
