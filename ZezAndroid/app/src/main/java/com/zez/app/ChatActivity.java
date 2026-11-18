package com.zez.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.zez.app.adapter.ChatMessageAdapter;
import com.zez.app.model.ChatMessage;
import com.zez.app.util.ChatApiClient;
import com.zez.app.util.ChatPreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private FrameLayout btnSendContainer;
    private ImageView btnSend;
    private ProgressBar pbSending;

    private ChatMessageAdapter adapter;
    private LinearLayoutManager layoutManager;
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private boolean isPollingActive = false;
    private String lastPollTimestamp = null;
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        initViews();
        setupRecyclerView();
        loadMessages();
    }

    private void initViews() {
        tvHeaderName = findViewById(R.id.tv_chat_header_name);
        tvHeaderStatus = findViewById(R.id.tv_chat_header_status);
        btnBack = findViewById(R.id.btn_chat_back);
        recyclerView = findViewById(R.id.recycler_chat_messages);
        etInput = findViewById(R.id.et_chat_message_input);
        btnSendContainer = findViewById(R.id.btn_chat_send_container);
        btnSend = findViewById(R.id.btn_chat_send);
        pbSending = findViewById(R.id.pb_chat_sending);

        tvHeaderName.setText(otherUserName != null ? otherUserName : otherUserPhone);
        tvHeaderStatus.setText("online");

        btnBack.setOnClickListener(v -> finish());
        btnSendContainer.setOnClickListener(v -> sendMessage());
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
                adapter.setMessages(messages);
                if (adapter.getItemCount() > 0) {
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
                lastPollTimestamp = isoFormat.format(new Date());
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

        ChatMessage optimisticMsg = new ChatMessage(
                null,
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
                if (conversationId == null && sentMessage != null) {
                    conversationId = sentMessage.getConversationId();
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
                            for (ChatMessage msg : result.incoming) {
                                if (otherUserId != null && otherUserId.equals(msg.getSenderId())) {
                                    adapter.addMessage(msg);
                                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                                }
                            }
                        }
                    }
                    if (isPollingActive) {
                        pollHandler.postDelayed(pollRunnable, 2500);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (isPollingActive) {
                        pollHandler.postDelayed(pollRunnable, 4000);
                    }
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        isPollingActive = true;
        pollHandler.postDelayed(pollRunnable, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPollingActive = false;
        pollHandler.removeCallbacks(pollRunnable);
    }
}
