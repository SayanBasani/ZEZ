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
    private Thread realtimeWorkerThread;
    private volatile boolean isRunning = false;
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
        startRealtimeListener();

        if (pollingTask != null && !pollingTask.isCancelled() && !pollingTask.isDone()) {
            return;
        }

        ChatNotificationHelper.createNotificationChannel(appContext);

        pollingTask = scheduler.scheduleWithFixedDelay(this::pollConversationsForNotifications, 2, 8, TimeUnit.SECONDS);
        Log.d(TAG, "Chat notification background poller started (8s interval)");
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

                    ChatApiClient.LiveUpdateResult result = ChatApiClient.listenForLiveUpdatesSync(appContext, userId, lastLiveSince);
                    if (result != null && result.hasNew && result.messages != null && !result.messages.isEmpty()) {
                        List<ChatMessage> unseen = new ArrayList<>();
                        synchronized (seenLiveMessageIds) {
                            for (ChatMessage msg : result.messages) {
                                if (msg == null) continue;
                                String ts = msg.getCreatedAt();
                                if (ts != null && !ts.isEmpty() && (lastLiveSince == null || ts.compareTo(lastLiveSince) > 0)) {
                                    lastLiveSince = ts;
                                    if (appContext != null) {
                                        appContext.getSharedPreferences("zez_chat_notif_dedup", Context.MODE_PRIVATE)
                                                .edit().putString("last_live_since_ts", lastLiveSince).apply();
                                    }
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

                        if (!unseen.isEmpty()) {
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
                                    if (l != null) {
                                        l.onMessagesReceived(deliveredBatch);
                                    }
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

                            long now = System.currentTimeMillis();
                            if (now - lastPollSyncMs >= 2500L) {
                                scheduler.execute(() -> pollSync(appContext));
                            }
                        } else {
                            // Server returned only previously seen unread rows without blocking; avoid tight spin loop
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
        if (realtimeWorkerThread != null) {
            realtimeWorkerThread.interrupt();
            realtimeWorkerThread = null;
        }
        if (pollingTask != null) {
            pollingTask.cancel(true);
            pollingTask = null;
            Log.d(TAG, "Chat notification background poller stopped");
        }
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
