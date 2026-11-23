package com.zez.app.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.zez.app.ChatActivity;
import com.zez.app.model.ChatConversation;

import java.util.List;
import java.util.Map;
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
    private final Map<String, String> lastNotifiedMsgTimeMap = new ConcurrentHashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Context appContext;
    private int alarmSyncCounter = 0;

    public interface OnChatsUpdateListener {
        void onChatsUpdated(List<ChatConversation> conversations);
    }

    private OnChatsUpdateListener chatsUpdateListener;

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
        if (pollingTask != null && !pollingTask.isCancelled() && !pollingTask.isDone()) {
            return;
        }

        ChatNotificationHelper.createNotificationChannel(appContext);

        pollingTask = scheduler.scheduleWithFixedDelay(this::pollConversationsForNotifications, 1, 3, TimeUnit.SECONDS);
        Log.d(TAG, "Chat notification background poller started (3s interval)");
    }

    public synchronized void stop() {
        if (pollingTask != null) {
            pollingTask.cancel(true);
            pollingTask = null;
            Log.d(TAG, "Chat notification background poller stopped");
        }
    }

    public void setChatsUpdateListener(OnChatsUpdateListener listener) {
        this.chatsUpdateListener = listener;
    }

    public void pollOnce(Context context) {
        if (context != null) {
            this.appContext = context.getApplicationContext();
        }
        scheduler.execute(this::pollConversationsForNotifications);
    }

    private void pollConversationsForNotifications() {
        if (appContext == null) return;

        // Periodic background alarm sync (every ~30 seconds, 10 ticks x 3s)
        alarmSyncCounter++;
        if (alarmSyncCounter >= 10) {
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

                    for (ChatConversation conv : conversations) {
                        int unread = conv.getUnreadCount();
                        if (unread > 0) {
                            String convId = conv.getConversationId();
                            if (convId != null) {
                                // Skip notification if user is currently inside ChatActivity for this conversation
                                if (ChatActivity.activeConversationId != null && convId.equals(ChatActivity.activeConversationId)) {
                                    continue;
                                }

                                String lastTime = conv.getLastMessageTime();
                                String prevTime = lastNotifiedMsgTimeMap.get(convId);
                                if (lastTime != null && !lastTime.equals(prevTime)) {
                                    lastNotifiedMsgTimeMap.put(convId, lastTime);
                                    ChatNotificationHelper.showChatNotification(
                                            appContext,
                                            convId,
                                            conv.getOtherUserId(),
                                            conv.getOtherUserName(),
                                            conv.getOtherUserPhone(),
                                            conv.getLastMessageText(),
                                            null
                                    );
                                }
                            }
                        }
                    }

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
