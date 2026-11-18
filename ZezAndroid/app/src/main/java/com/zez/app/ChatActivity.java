package com.zez.app;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zez.app.adapter.ChatMessageAdapter;
import com.zez.app.model.ChatMessage;
import com.zez.app.util.ChatApiClient;
import com.zez.app.util.ChatPreferencesHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {
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

        tvHeaderName.setText(otherUserName != null ? otherUserName : otherUserPhone);
        tvHeaderStatus.setText("online");

        btnBack.setOnClickListener(v -> finish());
        btnSendContainer.setOnClickListener(v -> sendMessage());
        if (btnAttach != null) {
            btnAttach.setOnClickListener(v -> showAttachmentDialog());
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
        ChatApiClient.getMessages(this, conversationId, currentUserId, otherUserId, 100, new ChatApiClient.ChatCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                loadedMessageIds.clear();
                if (messages != null) {
                    for (ChatMessage m : messages) {
                        if (m.getId() != null) loadedMessageIds.add(m.getId());
                    }
                    adapter.setMessages(messages);
                    if (!messages.isEmpty()) {
                        lastPollTimestamp = messages.get(messages.size() - 1).getCreatedAt();
                    }
                }
                if (lastPollTimestamp == null) {
                    lastPollTimestamp = isoFormat.format(new Date());
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
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) return;

        etInput.setText("");
        btnSend.setVisibility(View.GONE);
        pbSending.setVisibility(View.VISIBLE);

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
        adapter.addMessage(optimisticMsg);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

        ChatApiClient.sendMessage(this, currentUserId, otherUserId, text, null, new ChatApiClient.ChatCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage sentMessage) {
                btnSend.setVisibility(View.VISIBLE);
                pbSending.setVisibility(View.GONE);
                if (sentMessage != null) {
                    if (sentMessage.getId() != null) loadedMessageIds.add(sentMessage.getId());
                    if (conversationId == null) {
                        conversationId = sentMessage.getConversationId();
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
                        new ChatApiClient.ChatCallback<ChatMessage>() {
                            @Override
                            public void onSuccess(ChatMessage sentMessage) {
                                if (sentMessage != null) {
                                    if (sentMessage.getId() != null) loadedMessageIds.add(sentMessage.getId());
                                    if (conversationId == null) conversationId = sentMessage.getConversationId();
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

            ChatApiClient.pollUpdates(ChatActivity.this, currentUserId, lastPollTimestamp, new ChatApiClient.ChatCallback<ChatApiClient.PollResult>() {
                @Override
                public void onSuccess(ChatApiClient.PollResult result) {
                    if (result != null) {
                        if (result.timestamp != null) lastPollTimestamp = result.timestamp;
                        if (result.incoming != null && !result.incoming.isEmpty()) {
                            boolean hasNew = false;
                            for (ChatMessage msg : result.incoming) {
                                if (msg.getId() != null && loadedMessageIds.contains(msg.getId())) {
                                    continue;
                                }
                                if (otherUserId != null && otherUserId.equals(msg.getSenderId())) {
                                    if (msg.getId() != null) loadedMessageIds.add(msg.getId());
                                    adapter.addMessage(msg);
                                    hasNew = true;
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
        isPollingActive = true;
        pollHandler.postDelayed(pollRunnable, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPollingActive = false;
        pollHandler.removeCallbacks(pollRunnable);
    }
}
