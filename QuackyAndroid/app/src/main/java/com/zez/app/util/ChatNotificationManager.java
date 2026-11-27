package com.zez.app.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.zez.app.ChatActivity;
import com.zez.app.model.ChatConversation;
import com.zez.app.model.ChatMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ChatNotificationManager {
    private static final String TAG = "ChatNotifManager";
    private static volatile ChatNotificationManager instance;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pollingTask;
    private ScheduledFuture<?> callWatchdogTask;
    private Thread realtimeWorkerThread;
    private volatile boolean isRunning = false;
    private volatile okhttp3.WebSocket notifyWebSocket;
    private volatile String notifyWsUserId = null;
    private volatile String activeBgCallId = null;
    private final Map<String, String> lastNotifiedMsgTimeMap = new ConcurrentHashMap<>();
    private final Set<String> seenLiveMessageIds = Collections.synchronizedSet(new LinkedHashSet<>());
    private volatile String lastLiveSince = null;
    private volatile long lastPollSyncMs = 0L;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Context appContext;
    private int alarmSyncCounter = 0;

    public interface OnChatsUpdateListener {
        void onChatsUpdated(List<ChatConversation> conversations);
    }

    public interface LiveMessageListener {
        void onMessagesReceived(List<ChatMessage> messages);
    }

    private OnChatsUpdateListener chatsUpdateListener;
    private volatile LiveMessageListener liveMessageListener;

    private ChatNotificationManager() {}

    public static ChatNotificationManager getInstance() {
        if (instance == null) {
            synchronized (ChatNotificationManager.class) {
                if (instance == null) {
                    instance = new ChatNotificationManager();
                }
            }
        }
        return instance;
    }

    public synchronized void start(Context context) {
        if (context != null) {
            this.appContext = context.getApplicationContext();
        }
        isRunning = true;
        ChatNotificationHelper.createNotificationChannel(appContext);
        ChatNotificationHelper.createCallNotificationChannel(appContext);
        connectNotifyWebSocket();
        startRealtimeListener();

        if (callWatchdogTask == null || callWatchdogTask.isCancelled() || callWatchdogTask.isDone()) {
            callWatchdogTask = scheduler.scheduleWithFixedDelay(() -> {
                if (appContext != null) checkIncomingCallsNow(appContext);
            }, 3, 4, TimeUnit.SECONDS);
        }

        if (pollingTask != null && !pollingTask.isCancelled() && !pollingTask.isDone()) {
            return;
        }

        pollingTask = scheduler.scheduleWithFixedDelay(this::pollConversationsForNotifications, 2, 8, TimeUnit.SECONDS);
        Log.d(TAG, "Chat & call notification background poller started");
    }

    private synchronized void connectNotifyWebSocket() {
        if (!isRunning || appContext == null || !ChatPreferencesHelper.isChatRegistered(appContext)) return;
        String userId = ChatPreferencesHelper.getUserId(appContext);
        if (userId == null) return;
        if (notifyWebSocket != null && userId.equals(notifyWsUserId)) return;

        if (notifyWebSocket != null) {
            try { notifyWebSocket.close(1000, "switch"); } catch (Exception ignored) {}
            notifyWebSocket = null;
        }
        notifyWsUserId = userId;
        String baseUrl = ChatApiClient.getBaseUrl(appContext);
        String wsBase = baseUrl.startsWith("https://")
                ? "wss://" + baseUrl.substring(8)
                : (baseUrl.startsWith("http://") ? "ws://" + baseUrl.substring(7) : "ws://" + baseUrl);
        String wsUrl = wsBase + "/ws/notify?userId=" + android.net.Uri.encode(userId);

        okhttp3.OkHttpClient wsClient = ChatApiClient.getOkHttpClient().newBuilder()
                .readTimeout(0, TimeUnit.SECONDS)
                .pingInterval(20, TimeUnit.SECONDS)
                .build();

        okhttp3.Request req = new okhttp3.Request.Builder().url(wsUrl).build();
        notifyWebSocket = wsClient.newWebSocket(req, new okhttp3.WebSocketListener() {
            @Override
            public void onMessage(okhttp3.WebSocket webSocket, String text) {
                try {
                    com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(text).getAsJsonObject();
                    String type = obj.has("type") ? obj.get("type").getAsString() : "";
                    if ("message".equals(type) && obj.has("message") && obj.get("message").isJsonObject()) {
                        ChatMessage msg = new com.google.gson.GsonBuilder()
                                .setFieldNamingPolicy(com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()
                                .fromJson(obj.getAsJsonObject("message"), ChatMessage.class);
                        if (msg != null) {
                            deliverLiveMessages(Collections.singletonList(msg));
                        }
                    } else if ("call_incoming".equals(type) && obj.has("call") && obj.get("call").isJsonObject()) {
                        com.google.gson.JsonObject c = obj.getAsJsonObject("call");
                        ChatApiClient.IncomingCallInfo info = parseIncomingCallJson(c);
                        if (info != null) {
                            activeBgCallId = info.id;
                            ChatNotificationHelper.showIncomingCallNotification(appContext, info);
                        }
                    } else if ("call_ended".equals(type)) {
                        String endedCallId = obj.has("callId") ? obj.get("callId").getAsString() : null;
                        if (endedCallId != null) {
                            ChatNotificationHelper.clearHandledCall(endedCallId);
                        }
                        activeBgCallId = null;
                        ChatNotificationHelper.cancelIncomingCallNotification(appContext);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "WS notify parse error: " + e.getMessage());
                }
            }

            @Override
            public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
                if (notifyWebSocket == webSocket) {
                    notifyWebSocket = null;
                }
                if (isRunning) {
                    scheduler.schedule(ChatNotificationManager.this::connectNotifyWebSocket, 3, TimeUnit.SECONDS);
                }
            }

            @Override
            public void onFailure(okhttp3.WebSocket webSocket, Throwable t, okhttp3.Response response) {
                if (notifyWebSocket == webSocket) {
                    notifyWebSocket = null;
                }
                if (isRunning) {
                    scheduler.schedule(ChatNotificationManager.this::connectNotifyWebSocket, 4, TimeUnit.SECONDS);
                }
            }
        });
    }

    private ChatApiClient.IncomingCallInfo parseIncomingCallJson(com.google.gson.JsonObject ic) {
        if (ic == null || !ic.has("id") || ic.get("id").isJsonNull()) return null;
        ChatApiClient.IncomingCallInfo info = new ChatApiClient.IncomingCallInfo();
        info.id = ic.get("id").getAsString();
        info.callType = ic.has("callType") && !ic.get("callType").isJsonNull() ? ic.get("callType").getAsString() : "video";
        info.callerId = ic.has("callerId") && !ic.get("callerId").isJsonNull() ? ic.get("callerId").getAsString() : "";
        info.callerName = ic.has("callerName") && !ic.get("callerName").isJsonNull() ? ic.get("callerName").getAsString() : "Quacky Contact";
        info.callerAvatar = ic.has("callerAvatar") && !ic.get("callerAvatar").isJsonNull() ? ic.get("callerAvatar").getAsString() : null;
        info.callerPhone = ic.has("callerPhone") && !ic.get("callerPhone").isJsonNull() ? ic.get("callerPhone").getAsString() : null;
        return info;
    }

    private void deliverLiveMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty() || appContext == null) return;
        List<ChatMessage> unseen = new ArrayList<>();
        synchronized (seenLiveMessageIds) {
            for (ChatMessage msg : messages) {
                if (msg == null) continue;
                String ts = msg.getCreatedAt();
                if (ts != null && !ts.isEmpty() && (lastLiveSince == null || ts.compareTo(lastLiveSince) > 0)) {
                    lastLiveSince = ts;
                    appContext.getSharedPreferences("zez_chat_notif_dedup", Context.MODE_PRIVATE)
                            .edit().putString("last_live_since_ts", lastLiveSince).apply();
                }
                String mid = msg.getId();
                String dedupKey = (mid != null && !mid.isEmpty())
                        ? mid
                        : ((msg.getConversationId() != null ? msg.getConversationId() : "") + "|" + (ts != null ? ts : "") + "|" + (msg.getSenderId() != null ? msg.getSenderId() : ""));
                if (!seenLiveMessageIds.contains(dedupKey)) {
                    seenLiveMessageIds.add(dedupKey);
                    while (seenLiveMessageIds.size() > 300) {
                        String oldest = seenLiveMessageIds.iterator().next();
                        seenLiveMessageIds.remove(oldest);
                    }
                    unseen.add(msg);
                }
            }
        }
        if (unseen.isEmpty()) return;

        Collections.sort(unseen, (a, b) -> {
            String ta = a.getCreatedAt() != null ? a.getCreatedAt() : "";
            String tb = b.getCreatedAt() != null ? b.getCreatedAt() : "";
            return ta.compareTo(tb);
        });
        final List<ChatMessage> deliveredBatch = unseen;
        LiveMessageListener activeListener = liveMessageListener;
        if (activeListener != null) {
            mainHandler.post(() -> {
                LiveMessageListener l = liveMessageListener;
                if (l != null) l.onMessagesReceived(deliveredBatch);
            });
        }
        for (ChatMessage msg : deliveredBatch) {
            String convId = msg.getConversationId();
            if (convId == null) continue;
            ChatNotificationHelper.showChatNotificationIfNew(
                    appContext,
                    convId,
                    msg.getId(),
                    msg.getSenderId(),
                    msg.getSenderName() != null ? msg.getSenderName() : "Chat",
                    msg.getSenderPhone() != null ? msg.getSenderPhone() : "",
                    msg.getMessageText(),
                    msg.getMediaType(),
                    msg.getCreatedAt()
            );
        }
    }

    public void checkIncomingCallsNow(Context context) {
        Context ctx = context != null ? context.getApplicationContext() : appContext;
        if (ctx == null || !ChatPreferencesHelper.isChatRegistered(ctx)) return;
        String userId = ChatPreferencesHelper.getUserId(ctx);
        if (userId == null) return;
        try {
            String url = ChatApiClient.getBaseUrl(ctx) + "/api/call/incoming?userId=" + android.net.Uri.encode(userId);
            okhttp3.Request req = new okhttp3.Request.Builder().url(url).get().build();
            try (okhttp3.Response resp = ChatApiClient.getOkHttpClient().newCall(req).execute()) {
                if (resp.isSuccessful() && resp.body() != null) {
                    com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(resp.body().string()).getAsJsonObject();
                    if (json.has("hasIncomingCall") && json.get("hasIncomingCall").getAsBoolean()
                            && json.has("call") && json.get("call").isJsonObject()) {
                        ChatApiClient.IncomingCallInfo info = parseIncomingCallJson(json.getAsJsonObject("call"));
                        if (info != null) {
                            activeBgCallId = info.id;
                            ChatNotificationHelper.showIncomingCallNotification(ctx, info);
                        }
                    } else if (activeBgCallId != null) {
                        ChatNotificationHelper.clearHandledCall(activeBgCallId);
                        activeBgCallId = null;
                        ChatNotificationHelper.cancelIncomingCallNotification(ctx);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private synchronized void startRealtimeListener() {
        if (realtimeWorkerThread != null && realtimeWorkerThread.isAlive()) return;
        if (lastLiveSince == null && appContext != null) {
            lastLiveSince = appContext.getSharedPreferences("zez_chat_notif_dedup", Context.MODE_PRIVATE)
                    .getString("last_live_since_ts", null);
        }
        realtimeWorkerThread = new Thread(() -> {
            Log.i(TAG, "Chat notification real-time push listener active");
            while (isRunning) {
                try {
                    if (appContext == null || !ChatPreferencesHelper.isChatRegistered(appContext)) {
                        Thread.sleep(3000);
                        continue;
                    }
                    String userId = ChatPreferencesHelper.getUserId(appContext);
                    if (userId == null) {
                        Thread.sleep(3000);
                        continue;
                    }
                    connectNotifyWebSocket();

                    ChatApiClient.LiveUpdateResult result = ChatApiClient.listenForLiveUpdatesSync(appContext, userId, lastLiveSince);
                    if (result != null && result.hasIncomingCall && result.incomingCall != null) {
                        activeBgCallId = result.incomingCall.id;
                        ChatNotificationHelper.showIncomingCallNotification(appContext, result.incomingCall);
                    }
                    if (result != null && result.hasNew && result.messages != null && !result.messages.isEmpty()) {
                        int prevSize = seenLiveMessageIds.size();
                        deliverLiveMessages(result.messages);
                        long now = System.currentTimeMillis();
                        if (now - lastPollSyncMs >= 2500L) {
                            scheduler.execute(() -> pollSync(appContext));
                        }
                        if (seenLiveMessageIds.size() == prevSize && (result.incomingCall == null)) {
                            Thread.sleep(2500);
                        }
                    } else if (result != null && result.timeout) {
                        // Long-poll window closed cleanly; re-open immediately
                    } else {
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException ie) {
                    break;
                } catch (Exception e) {
                    try { Thread.sleep(2500); } catch (Exception ignored) {}
                }
            }
        }, "ChatRealtimeListener");
        realtimeWorkerThread.setDaemon(true);
        realtimeWorkerThread.start();
    }

    public synchronized void stop() {
        isRunning = false;
        if (notifyWebSocket != null) {
            try { notifyWebSocket.close(1000, "stop"); } catch (Exception ignored) {}
            notifyWebSocket = null;
        }
        notifyWsUserId = null;
        if (realtimeWorkerThread != null) {
            realtimeWorkerThread.interrupt();
            realtimeWorkerThread = null;
        }
        if (pollingTask != null) {
            pollingTask.cancel(true);
            pollingTask = null;
        }
        if (callWatchdogTask != null) {
            callWatchdogTask.cancel(true);
            callWatchdogTask = null;
        }
        Log.d(TAG, "Chat & call notification background poller stopped");
    }

    public void setChatsUpdateListener(OnChatsUpdateListener listener) {
        this.chatsUpdateListener = listener;
    }

    public void setLiveMessageListener(LiveMessageListener listener) {
        this.liveMessageListener = listener;
    }

    public void pollOnce(Context context) {
        if (context != null) {
            this.appContext = context.getApplicationContext();
        }
        scheduler.execute(this::pollConversationsForNotifications);
    }

    public void pollSync(Context context) {
        if (context != null) {
            this.appContext = context.getApplicationContext();
        }
        if (appContext == null) return;
        if (!ChatPreferencesHelper.isChatRegistered(appContext)) return;
        String userId = ChatPreferencesHelper.getUserId(appContext);
        if (userId == null) return;

        long now = System.currentTimeMillis();
        if (now - lastPollSyncMs < 1500L) return;
        lastPollSyncMs = now;

        try {
            List<ChatConversation> conversations = ChatApiClient.getConversationsSync(appContext, userId);
            if (conversations == null || conversations.isEmpty()) return;

            processConversationsForNotifications(conversations);

            if (chatsUpdateListener != null) {
                mainHandler.post(() -> {
                    if (chatsUpdateListener != null) {
                        chatsUpdateListener.onChatsUpdated(conversations);
                    }
                });
            }
        } catch (Exception e) {
            Log.w(TAG, "pollSync error: " + e.getMessage());
        }
    }

    private void processConversationsForNotifications(List<ChatConversation> conversations) {
        if (conversations == null || conversations.isEmpty() || appContext == null) return;

        for (ChatConversation conv : conversations) {
            int unread = conv.getUnreadCount();
            if (unread > 0) {
                String convId = conv.getConversationId();
                if (convId != null) {
                    ChatNotificationHelper.showChatNotificationIfNew(
                            appContext,
                            convId,
                            null,
                            conv.getOtherUserId(),
                            conv.getOtherUserName(),
                            conv.getOtherUserPhone(),
                            conv.getLastMessageText(),
                            null,
                            conv.getLastMessageTime()
                    );
                }
            }
        }
    }

    private void pollConversationsForNotifications() {
        if (appContext == null) return;

        // Periodic background alarm sync (every ~32 seconds, 4 ticks x 8s)
        alarmSyncCounter++;
        if (alarmSyncCounter >= 4) {
            alarmSyncCounter = 0;
            try {
                AlarmHelper.syncWithBackend(appContext, null);
            } catch (Exception ignored) {}
        }

        if (!ChatPreferencesHelper.isChatRegistered(appContext)) return;
        String userId = ChatPreferencesHelper.getUserId(appContext);
        if (userId == null) return;

        try {
            ChatApiClient.getConversations(appContext, userId, new ChatApiClient.ChatCallback<List<ChatConversation>>() {
                @Override
                public void onSuccess(List<ChatConversation> conversations) {
                    if (conversations == null || conversations.isEmpty()) return;

                    processConversationsForNotifications(conversations);

                    if (chatsUpdateListener != null) {
                        mainHandler.post(() -> {
                            if (chatsUpdateListener != null) {
                                chatsUpdateListener.onChatsUpdated(conversations);
                            }
                        });
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    // Silently ignore transient network drops during background polling
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Exception during chat notification polling: " + e.getMessage());
        }
    }
}
