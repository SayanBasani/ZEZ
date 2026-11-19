package com.zez.app;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.zez.app.adapter.ChatMessageAdapter;
import com.zez.app.model.ChatMessage;
import com.zez.app.util.ChatApiClient;
import com.zez.app.util.ChatNotificationHelper;
import com.zez.app.util.ChatPreferencesHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    public static volatile String activeConversationId = null;

    private String conversationId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserPhone;
    private String currentUserId;

    private TextView tvHeaderName;
    private TextView tvHeaderStatus;
    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private EditText etInput;
    private ImageButton btnAttach;
    private FrameLayout btnSendContainer;
    private ImageView btnSend;
    private ProgressBar pbSending;

    private LinearLayout btnModeToggle;
    private ImageView ivModeIcon;
    private TextView tvModeLabel;
    private LinearLayout layoutSecretBanner;
    private TextView btnExitSecretMode;

    private boolean isSecretMode = false;
    private String currentSecretSessionId = null;
    private final List<ChatMessage> normalMessages = new ArrayList<>();
    private final List<ChatMessage> ephemeralSecretMessages = new ArrayList<>();

    private ChatMessageAdapter adapter;
    private LinearLayoutManager layoutManager;
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private boolean isPollingActive = false;
    private String lastPollTimestamp = null;
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private final Set<String> loadedMessageIds = new HashSet<>();

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> pickVideoLauncher;
    private ActivityResultLauncher<String> pickFileLauncher;
    private ActivityResultLauncher<Void> takePhotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        setContentView(R.layout.activity_chat);

        conversationId = getIntent().getStringExtra("extra_conversation_id");
        otherUserId = getIntent().getStringExtra("extra_other_user_id");
        otherUserName = getIntent().getStringExtra("extra_other_user_name");
        otherUserPhone = getIntent().getStringExtra("extra_other_user_phone");
        currentUserId = ChatPreferencesHelper.getUserId(this);

        if (currentUserId == null) {
            Toast.makeText(this, "Please register for chat first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initActivityLaunchers();
        initViews();
        setupRecyclerView();
        loadMessages();
    }

    private void initActivityLaunchers() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) handleMediaSelection(uri, "image");
        });

        pickVideoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) handleMediaSelection(uri, "video");
        });

        pickFileLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) handleMediaSelection(uri, "file");
        });

        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
            if (bitmap != null) handleBitmapCapture(bitmap);
        });
    }

    private void initViews() {
        tvHeaderName = findViewById(R.id.tv_chat_header_name);
        tvHeaderStatus = findViewById(R.id.tv_chat_header_status);
        btnBack = findViewById(R.id.btn_chat_back);
        recyclerView = findViewById(R.id.recycler_chat_messages);
        etInput = findViewById(R.id.et_chat_message_input);
        btnAttach = findViewById(R.id.btn_chat_attach);
        btnSendContainer = findViewById(R.id.btn_chat_send_container);
        btnSend = findViewById(R.id.btn_chat_send);
        pbSending = findViewById(R.id.pb_chat_sending);

        btnModeToggle = findViewById(R.id.btn_chat_mode_toggle);
        ivModeIcon = findViewById(R.id.iv_mode_icon);
        tvModeLabel = findViewById(R.id.tv_mode_label);
        layoutSecretBanner = findViewById(R.id.layout_secret_mode_banner);
        btnExitSecretMode = findViewById(R.id.btn_exit_secret_mode);

        tvHeaderName.setText(otherUserName != null ? otherUserName : otherUserPhone);
        tvHeaderStatus.setText("online");

        btnBack.setOnClickListener(v -> finish());
        btnSendContainer.setOnClickListener(v -> sendMessage());
        if (btnAttach != null) {
            btnAttach.setOnClickListener(v -> showAttachmentDialog());
        }

        if (btnModeToggle != null) {
            btnModeToggle.setOnClickListener(v -> toggleMode());
        }
        if (btnExitSecretMode != null) {
            btnExitSecretMode.setOnClickListener(v -> {
                if (isSecretMode) toggleMode();
            });
        }
    }

    private void updateModeUi() {
        if (btnModeToggle == null || ivModeIcon == null || tvModeLabel == null) return;
        if (isSecretMode) {
            btnModeToggle.setBackgroundResource(R.drawable.bg_mode_toggle_secret);
            ivModeIcon.setImageResource(R.drawable.ic_incognito);
            ivModeIcon.setColorFilter(Color.parseColor("#A855F7"));
            tvModeLabel.setText("Secret");
            tvModeLabel.setTextColor(Color.parseColor("#A855F7"));
            if (layoutSecretBanner != null) layoutSecretBanner.setVisibility(View.VISIBLE);
        } else {
            btnModeToggle.setBackgroundResource(R.drawable.bg_mode_toggle_normal);
            ivModeIcon.setImageResource(R.drawable.ic_shield_check);
            ivModeIcon.setColorFilter(Color.parseColor("#22C55E"));
            tvModeLabel.setText("Normal");
            tvModeLabel.setTextColor(Color.parseColor("#22C55E"));
            if (layoutSecretBanner != null) layoutSecretBanner.setVisibility(View.GONE);
        }
    }

    private void toggleMode() {
        final boolean targetSecret = !isSecretMode;
        if (conversationId == null) {
            ChatApiClient.getMessages(this, null, currentUserId, otherUserId, 10, "normal", null, new ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>() {
                @Override
                public void onSuccess(ChatApiClient.MessagesResult res) {
                    if (res != null && res.conversationId != null) {
                        conversationId = res.conversationId;
                        activeConversationId = conversationId;
                        toggleMode();
                    } else {
                        applyModeState(targetSecret, UUID.randomUUID().toString(), true, true);
                    }
                }

                @Override
                public void onError(String error) {
                    applyModeState(targetSecret, UUID.randomUUID().toString(), true, true);
                }
            });
            return;
        }

        ChatApiClient.setConversationMode(this, conversationId, currentUserId, targetSecret, new ChatApiClient.ChatCallback<ChatApiClient.ConversationModeInfo>() {
            @Override
            public void onSuccess(ChatApiClient.ConversationModeInfo info) {
                applyModeState(info.isSecretMode, info.secretSessionId, true, true);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ChatActivity.this, "Mode toggle error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyModeState(boolean secret, String sessionId, boolean notifyToast) {
        applyModeState(secret, sessionId, notifyToast, false);
    }

    private void applyModeState(boolean secret, String sessionId, boolean notifyToast, boolean forceRefresh) {
        if (!forceRefresh && this.isSecretMode == secret && (secret ? (sessionId != null && sessionId.equals(this.currentSecretSessionId)) : true)) {
            return;
        }
        this.isSecretMode = secret;
        this.currentSecretSessionId = sessionId;
        updateModeUi();

        loadedMessageIds.clear();
        if (isSecretMode) {
            // Fresh clean screen for secret session!
            ephemeralSecretMessages.clear();
            adapter.setMessages(ephemeralSecretMessages);
            if (notifyToast) {
                Toast.makeText(this, "Secret Mode Active • Messages disappear on exit", Toast.LENGTH_SHORT).show();
            }
        } else {
            ephemeralSecretMessages.clear();
            for (ChatMessage m : normalMessages) {
                if (m.getId() != null) loadedMessageIds.add(m.getId());
            }
            adapter.setMessages(normalMessages);
            if (adapter.getItemCount() > 0) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
            if (notifyToast) {
                Toast.makeText(this, "Normal Mode Active", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(currentUserId);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void loadMessages() {
        ChatApiClient.getMessages(this, conversationId, currentUserId, otherUserId, 100, "normal", null, new ChatApiClient.ChatCallback<ChatApiClient.MessagesResult>() {
            @Override
            public void onSuccess(ChatApiClient.MessagesResult result) {
                if (result != null && result.conversationId != null) {
                    conversationId = result.conversationId;
                    activeConversationId = conversationId;
                }
                if (result != null && result.timestamp != null) {
                    lastPollTimestamp = result.timestamp;
                }
                normalMessages.clear();
                loadedMessageIds.clear();
                if (result != null && result.messages != null) {
                    normalMessages.addAll(result.messages);
                }

                if (result != null && result.isSecretMode) {
                    applyModeState(true, result.secretSessionId, false, true);
                } else {
                    applyModeState(false, null, false, true);
                }

                if (lastPollTimestamp == null) {
                    if (!normalMessages.isEmpty()) {
                        lastPollTimestamp = normalMessages.get(normalMessages.size() - 1).getCreatedAt();
                    } else {
                        lastPollTimestamp = isoFormat.format(new Date());
                    }
                }
                if (adapter.getItemCount() > 0) {
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
                if (conversationId != null) {
                    ChatApiClient.markMessagesAsRead(ChatActivity.this, conversationId, currentUserId);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ChatActivity.this, "Failed to load messages: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) return;

        etInput.setText("");
        btnSend.setVisibility(View.GONE);
        pbSending.setVisibility(View.VISIBLE);

        final String activeMode = isSecretMode ? ChatMessage.MODE_SECRET : ChatMessage.MODE_NORMAL;
        final String sessionUuid = isSecretMode ? currentSecretSessionId : null;

        String tempId = "temp_" + System.currentTimeMillis();
        ChatMessage optimisticMsg = new ChatMessage(
                tempId,
                conversationId,
                currentUserId,
                otherUserId,
                text,
                null,
                ChatMessage.STATUS_SENT,
                isoFormat.format(new Date())
        );
        optimisticMsg.setMode(activeMode);
        optimisticMsg.setSecretSessionId(sessionUuid);

        if (isSecretMode) {
            ephemeralSecretMessages.add(optimisticMsg);
        } else {
            normalMessages.add(optimisticMsg);
        }
        adapter.addMessage(optimisticMsg);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

        ChatApiClient.sendMessage(this, currentUserId, otherUserId, text, null, null, null, null, activeMode, sessionUuid, new ChatApiClient.ChatCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage sentMessage) {
                btnSend.setVisibility(View.VISIBLE);
                pbSending.setVisibility(View.GONE);
                if (sentMessage != null) {
                    if (sentMessage.getId() != null) {
                        loadedMessageIds.add(sentMessage.getId());
                        optimisticMsg.setId(sentMessage.getId());
                    }
                    if (sentMessage.getStatus() != null) {
                        optimisticMsg.setStatus(sentMessage.getStatus());
                        adapter.updateMessageStatus(sentMessage.getId(), sentMessage.getStatus());
                    }
                    if (conversationId == null && sentMessage.getConversationId() != null) {
                        conversationId = sentMessage.getConversationId();
                        activeConversationId = conversationId;
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                btnSend.setVisibility(View.VISIBLE);
                pbSending.setVisibility(View.GONE);
                Toast.makeText(ChatActivity.this, "Failed to send: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAttachmentDialog() {
        String[] options = {"📷 Camera", "🖼️ Photo / Gallery", "🎥 Video", "📄 Document / File"};
        new AlertDialog.Builder(this)
                .setTitle("Share Media")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            takePhotoLauncher.launch(null);
                            break;
                        case 1:
                            pickImageLauncher.launch("image/*");
                            break;
                        case 2:
                            pickVideoLauncher.launch("video/*");
                            break;
                        case 3:
                            pickFileLauncher.launch("*/*");
                            break;
                    }
                })
                .show();
    }

    private void handleMediaSelection(Uri uri, String defaultType) {
        String displayName = "attachment";
        long fileSize = 0;
        String mimeType = getContentResolver().getType(uri);

        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIdx != -1) displayName = cursor.getString(nameIdx);
                if (sizeIdx != -1) fileSize = cursor.getLong(sizeIdx);
            }
        } catch (Exception ignored) {}

        Toast.makeText(this, "Uploading " + displayName + "...", Toast.LENGTH_SHORT).show();
        btnSend.setVisibility(View.GONE);
        pbSending.setVisibility(View.VISIBLE);

        final String finalName = displayName;
        final long finalSize = fileSize;
        final String finalType = defaultType;
        final String activeMode = isSecretMode ? ChatMessage.MODE_SECRET : ChatMessage.MODE_NORMAL;
        final String sessionUuid = isSecretMode ? currentSecretSessionId : null;

        ChatApiClient.uploadChatMedia(this, uri, displayName, mimeType, new ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>() {
            @Override
            public void onSuccess(ChatApiClient.MediaUploadResult result) {
                btnSend.setVisibility(View.VISIBLE);
                pbSending.setVisibility(View.GONE);

                String caption = etInput.getText().toString().trim();
                etInput.setText("");

                ChatApiClient.sendMessage(
                        ChatActivity.this,
                        currentUserId,
                        otherUserId,
                        caption,
                        result.url,
                        result.mediaType != null ? result.mediaType : finalType,
                        result.fileName != null ? result.fileName : finalName,
                        result.fileSize > 0 ? result.fileSize : finalSize,
                        activeMode,
                        sessionUuid,
                        new ChatApiClient.ChatCallback<ChatMessage>() {
                            @Override
                            public void onSuccess(ChatMessage sentMessage) {
                                if (sentMessage != null) {
                                    if (sentMessage.getId() != null) loadedMessageIds.add(sentMessage.getId());
                                    if (conversationId == null && sentMessage.getConversationId() != null) {
                                        conversationId = sentMessage.getConversationId();
                                        activeConversationId = conversationId;
                                    }
                                    
                                    if (isSecretMode) {
                                        ephemeralSecretMessages.add(sentMessage);
                                    } else {
                                        normalMessages.add(sentMessage);
                                    }
                                    adapter.addMessage(sentMessage);
                                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                                }
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(ChatActivity.this, "Send failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }

            @Override
            public void onError(String errorMessage) {
                btnSend.setVisibility(View.VISIBLE);
                pbSending.setVisibility(View.GONE);
                Toast.makeText(ChatActivity.this, "Upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleBitmapCapture(Bitmap bitmap) {
        try {
            File cacheDir = getCacheDir();
            File tempFile = new File(cacheDir, "camera_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            handleMediaSelection(Uri.fromFile(tempFile), "image");
        } catch (Exception e) {
            Toast.makeText(this, "Error processing photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPollingActive) return;

            ChatApiClient.pollUpdates(ChatActivity.this, currentUserId, lastPollTimestamp, conversationId, new ChatApiClient.ChatCallback<ChatApiClient.PollResult>() {
                @Override
                public void onSuccess(ChatApiClient.PollResult result) {
                    if (result != null) {
                        if (result.timestamp != null) lastPollTimestamp = result.timestamp;

                        // Synchronize dual mode across peers
                        if (result.conversationMode != null) {
                            boolean remoteSecret = result.conversationMode.isSecretMode;
                            String remoteSession = result.conversationMode.secretSessionId;
                            if (remoteSecret != isSecretMode || (remoteSecret && remoteSession != null && !remoteSession.equals(currentSecretSessionId))) {
                                applyModeState(remoteSecret, remoteSession, true, false);
                            }
                        }

                        // Process status updates for sent messages (sent -> delivered -> read)
                        if (result.statusUpdates != null && !result.statusUpdates.isEmpty()) {
                            for (JsonObject su : result.statusUpdates) {
                                if (su.has("id") && su.has("status")) {
                                    String msgId = su.get("id").getAsString();
                                    String newStatus = su.get("status").getAsString();
                                    adapter.updateMessageStatus(msgId, newStatus);
                                    for (ChatMessage nm : normalMessages) {
                                        if (msgId.equals(nm.getId())) {
                                            nm.setStatus(newStatus);
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if (result.incoming != null && !result.incoming.isEmpty()) {
                            boolean hasNew = false;
                            for (ChatMessage msg : result.incoming) {
                                if (msg.getId() != null && loadedMessageIds.contains(msg.getId())) {
                                    continue;
                                }
                                boolean belongsToThisChat = (conversationId != null && conversationId.equals(msg.getConversationId()))
                                        || (otherUserId != null && otherUserId.equals(msg.getSenderId()));
                                if (belongsToThisChat) {
                                    if (msg.getId() != null) loadedMessageIds.add(msg.getId());
                                    
                                    if (msg.isSecretMode()) {
                                        if (isSecretMode) {
                                            ephemeralSecretMessages.add(msg);
                                            adapter.addMessage(msg);
                                            hasNew = true;
                                        }
                                    } else {
                                        normalMessages.add(msg);
                                        if (!isSecretMode) {
                                            adapter.addMessage(msg);
                                            hasNew = true;
                                        }
                                    }
                                }
                            }
                            if (hasNew) {
                                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                                if (conversationId != null) {
                                    ChatApiClient.markMessagesAsRead(ChatActivity.this, conversationId, currentUserId);
                                }
                            }
                        }
                    }
                    if (isPollingActive) {
                        pollHandler.postDelayed(pollRunnable, 1500); // Fast 1.5s live polling
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (isPollingActive) {
                        pollHandler.postDelayed(pollRunnable, 2500);
                    }
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        activeConversationId = conversationId;
        if (conversationId != null) {
            ChatNotificationHelper.clearNotification(this, conversationId);
        }
        isPollingActive = true;
        updateModeUi();
        pollHandler.postDelayed(pollRunnable, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activeConversationId = null;
        isPollingActive = false;
        pollHandler.removeCallbacks(pollRunnable);
        // Ephemeral Secret Mode rule: Purge all in-memory secret messages when user navigates away or pauses
        ephemeralSecretMessages.clear();
        if (isSecretMode) {
            adapter.setMessages(ephemeralSecretMessages);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (conversationId != null && conversationId.equals(activeConversationId)) {
            activeConversationId = null;
        }
        ephemeralSecretMessages.clear();
    }
}
