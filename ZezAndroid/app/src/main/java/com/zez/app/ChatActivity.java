package com.zez.app;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.ItemTouchHelper;
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
    private ImageButton btnMore;

    private View layoutReplyPreview;
    private TextView tvReplyPreviewSender;
    private TextView tvReplyPreviewText;
    private ImageButton btnCancelReply;
    private ChatMessage replyingToMessage = null;

    private View layoutNormalHeader;
    private View layoutSelectionBar;
    private ImageButton btnSelectionClose;
    private TextView tvSelectionCount;
    private ImageButton btnSelectionReply;
    private ImageButton btnSelectionDelete;
    private ImageButton btnSelectionStar;
    private ImageButton btnSelectionForward;
    private ImageButton btnSelectionMore;

    private View layoutChatRoot;
    private ImageButton btnVideocall;
    private ImageButton btnAudiocall;
    private com.zez.app.util.ChatLocalDbHelper chatDb;

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

    private static class PendingMessage {
        ChatMessage optimisticMsg;
        String text;
        String activeMode;
        String sessionUuid;
        String mediaUrl;
        String mediaType;
        String fileName;
        Long fileSize;
    }
    private final List<PendingMessage> pendingMessagesQueue = new ArrayList<>();
    private boolean isDrainingQueue = false;

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

        chatDb = com.zez.app.util.ChatLocalDbHelper.getInstance(this);
        initActivityLaunchers();
        initViews();
        setupRecyclerView();
        loadCachedMessages();
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
        btnMore = findViewById(R.id.btn_chat_more);

        layoutReplyPreview = findViewById(R.id.layout_reply_preview);
        tvReplyPreviewSender = findViewById(R.id.tv_reply_preview_sender);
        tvReplyPreviewText = findViewById(R.id.tv_reply_preview_text);
        btnCancelReply = findViewById(R.id.btn_cancel_reply);

        layoutNormalHeader = findViewById(R.id.layout_chat_normal_header);
        layoutSelectionBar = findViewById(R.id.layout_chat_selection_bar);
        btnSelectionClose = findViewById(R.id.btn_selection_close);
        tvSelectionCount = findViewById(R.id.tv_selection_count);
        btnSelectionReply = findViewById(R.id.btn_selection_reply);
        btnSelectionDelete = findViewById(R.id.btn_selection_delete);

        layoutChatRoot = findViewById(R.id.layout_chat_root);
        btnVideocall = findViewById(R.id.btn_chat_videocall);
        btnAudiocall = findViewById(R.id.btn_chat_audiocall);
        btnSelectionStar = findViewById(R.id.btn_selection_star);
        btnSelectionForward = findViewById(R.id.btn_selection_forward);
        btnSelectionMore = findViewById(R.id.btn_selection_more);

        applyChatTheme(ChatPreferencesHelper.getChatTheme(this));

        tvHeaderName.setText(otherUserName != null ? otherUserName : otherUserPhone);
        tvHeaderStatus.setText("Connecting...");

        btnBack.setOnClickListener(v -> finish());
        btnSendContainer.setOnClickListener(v -> sendMessage());
        if (btnAttach != null) {
            btnAttach.setOnClickListener(v -> showAttachmentDialog());
        }

        if (btnAudiocall != null) {
            btnAudiocall.setOnClickListener(v -> {
                if (otherUserPhone != null && !otherUserPhone.trim().isEmpty()) {
                    try {
                        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + otherUserPhone.trim()));
                        startActivity(dialIntent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Cannot open dialer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No phone number available for this contact", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnVideocall != null) {
            btnVideocall.setOnClickListener(v -> {
                Toast.makeText(this, "Starting encrypted video call...", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnMore != null) {
            btnMore.setOnClickListener(this::showChatOverflowMenu);
        }
        if (btnCancelReply != null) {
            btnCancelReply.setOnClickListener(v -> cancelReply());
        }

        if (btnSelectionClose != null) {
            btnSelectionClose.setOnClickListener(v -> {
                if (adapter != null) adapter.clearSelection();
            });
        }

        if (btnSelectionReply != null) {
            btnSelectionReply.setOnClickListener(v -> {
                if (adapter != null) {
                    List<ChatMessage> selected = adapter.getSelectedMessages();
                    if (!selected.isEmpty()) {
                        startReply(selected.get(0));
                    }
                    adapter.clearSelection();
                }
            });
        }

        if (btnSelectionStar != null) {
            btnSelectionStar.setOnClickListener(v -> {
                if (adapter != null) {
                    List<ChatMessage> selected = adapter.getSelectedMessages();
                    if (!selected.isEmpty()) {
                        Toast.makeText(this, "Starred " + selected.size() + " message(s)", Toast.LENGTH_SHORT).show();
                    }
                    adapter.clearSelection();
                }
            });
        }

        if (btnSelectionForward != null) {
            btnSelectionForward.setOnClickListener(v -> {
                if (adapter != null) {
                    List<ChatMessage> selected = adapter.getSelectedMessages();
                    if (!selected.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (ChatMessage m : selected) {
                            if (sb.length() > 0) sb.append("\n");
                            sb.append(m.getCleanMessageText());
                        }
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "Forward message"));
                    }
                    adapter.clearSelection();
                }
            });
        }

        if (btnSelectionMore != null) {
            btnSelectionMore.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, v);
                popup.getMenu().add(0, 1, 0, "Copy");
                popup.getMenu().add(0, 2, 1, "Info");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        copySelectedMessages();
                        return true;
                    } else if (item.getItemId() == 2) {
                        if (adapter != null) {
                            List<ChatMessage> selected = adapter.getSelectedMessages();
                            if (!selected.isEmpty()) {
                                ChatMessage m = selected.get(0);
                                String info = "Status: " + (m.getStatus() != null ? m.getStatus() : "sent") +
                                        "\nTime: " + (m.getCreatedAt() != null ? m.getCreatedAt() : "now");
                                new AlertDialog.Builder(this)
                                        .setTitle("Message Info")
                                        .setMessage(info)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                            adapter.clearSelection();
                        }
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        if (btnSelectionDelete != null) {
            btnSelectionDelete.setOnClickListener(v -> {
                if (adapter != null) {
                    List<ChatMessage> selected = adapter.getSelectedMessages();
                    if (!selected.isEmpty()) {
                        showDeleteMessagesDialog(selected);
                    }
                }
            });
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

    private void copySelectedMessages() {
        if (adapter != null) {
            List<ChatMessage> selected = adapter.getSelectedMessages();
            if (!selected.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (ChatMessage m : selected) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(m.getCleanMessageText());
                }
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Zez Message", sb.toString()));
                    Toast.makeText(ChatActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            }
            adapter.clearSelection();
        }
    }

    private void loadCachedMessages() {
        if (chatDb == null) return;
        String convKey = conversationId != null ? conversationId : otherUserId;
        if (convKey == null) return;
        List<ChatMessage> cached = chatDb.getMessages(convKey);
        if (cached != null && !cached.isEmpty()) {
            normalMessages.clear();
            normalMessages.addAll(cached);
            for (ChatMessage m : cached) {
                if (m.getId() != null) loadedMessageIds.add(m.getId());
            }
            if (!isSecretMode && adapter != null) {
                adapter.setMessages(normalMessages);
                if (adapter.getItemCount() > 0) {
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        }
    }

    private void applyChatTheme(String theme) {
        if (layoutChatRoot == null) return;
        int resId = R.drawable.bg_chat_theme_default;
        if ("charcoal".equalsIgnoreCase(theme)) resId = R.drawable.bg_chat_theme_charcoal;
        else if ("emerald".equalsIgnoreCase(theme)) resId = R.drawable.bg_chat_theme_emerald;
        else if ("ocean".equalsIgnoreCase(theme)) resId = R.drawable.bg_chat_theme_ocean;
        else if ("sunset".equalsIgnoreCase(theme)) resId = R.drawable.bg_chat_theme_sunset;
        layoutChatRoot.setBackgroundResource(resId);
    }

    private void showThemeDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog sheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_chat_theme, null);
        sheet.setContentView(view);

        int[] optIds = {
                R.id.theme_opt_default,
                R.id.theme_opt_charcoal,
                R.id.theme_opt_emerald,
                R.id.theme_opt_ocean,
                R.id.theme_opt_sunset
        };
        String[] themeKeys = {"default", "charcoal", "emerald", "ocean", "sunset"};

        for (int i = 0; i < optIds.length; i++) {
            final String themeKey = themeKeys[i];
            View opt = view.findViewById(optIds[i]);
            if (opt != null) {
                opt.setOnClickListener(v -> {
                    ChatPreferencesHelper.saveChatTheme(this, themeKey);
                    applyChatTheme(themeKey);
                    sheet.dismiss();
                    Toast.makeText(this, "Wallpaper theme updated", Toast.LENGTH_SHORT).show();
                });
            }
        }

        sheet.show();
    }

    private void showContactInfoDialog() {
        String name = otherUserName != null ? otherUserName : "Contact";
        String phone = otherUserPhone != null ? otherUserPhone : "Not available";
        new AlertDialog.Builder(this)
                .setTitle("Contact info")
                .setMessage("Name: " + name + "\nPhone: " + phone + "\nEncryption: 256-bit End-to-End")
                .setPositiveButton("OK", null)
                .show();
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(currentUserId);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnMessageActionListener(new ChatMessageAdapter.OnMessageActionListener() {
            @Override
            public void onSelectionChanged(int selectedCount) {
                if (selectedCount > 0) {
                    if (layoutNormalHeader != null) layoutNormalHeader.setVisibility(View.GONE);
                    if (layoutSelectionBar != null) layoutSelectionBar.setVisibility(View.VISIBLE);
                    if (tvSelectionCount != null) tvSelectionCount.setText(String.valueOf(selectedCount));
                    if (btnSelectionReply != null) btnSelectionReply.setVisibility(selectedCount == 1 ? View.VISIBLE : View.GONE);
                } else {
                    if (layoutSelectionBar != null) layoutSelectionBar.setVisibility(View.GONE);
                    if (layoutNormalHeader != null) layoutNormalHeader.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onMessageLongClick(ChatMessage message, View anchorView) {
                if (message == null || message.isDeletedForEveryone()) return;
                showEmojiReactionPopup(message, anchorView);
            }
        });

        // WhatsApp-Style Effortless Swipe to Reply (both left & right, very low threshold)
        ItemTouchHelper.SimpleCallback swipeToReplyCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof ChatMessageAdapter.DateHeaderViewHolder) {
                    return 0;
                }
                return ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.10f; // Only 10% swipe required to trigger reply effortlessly
            }

            @Override
            public float getSwipeVelocityThreshold(float defaultValue) {
                return defaultValue * 0.2f;
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return defaultValue * 0.2f;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // Smoothly clamp visual offset so the bubble doesn't get dragged away
                float maxOffset = 140f;
                float clampedDx = dX > 0 ? Math.min(dX, maxOffset) : Math.max(dX, -maxOffset);
                super.onChildDraw(c, recyclerView, viewHolder, clampedDx, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    ChatMessage msg = adapter.getMessageAt(pos);
                    if (msg != null && !msg.isDeletedForEveryone()) {
                        startReply(msg);
                    }
                    adapter.notifyItemChanged(pos); // Snap message bubble smoothly back into place
                }
            }
        };
        new ItemTouchHelper(swipeToReplyCallback).attachToRecyclerView(recyclerView);
    }

    private void showEmojiReactionPopup(ChatMessage message, View anchorView) {
        if (message == null || anchorView == null) return;
        try {
            View popupView = LayoutInflater.from(this).inflate(R.layout.popup_emoji_reactions, null);
            PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );
            popupWindow.setOutsideTouchable(true);
            popupWindow.setElevation(16f);

            int[] emojiBtnIds = {
                    R.id.btn_react_thumb,
                    R.id.btn_react_heart,
                    R.id.btn_react_laugh,
                    R.id.btn_react_surprised,
                    R.id.btn_react_sad,
                    R.id.btn_react_pray,
                    R.id.btn_react_fire,
                    R.id.btn_react_party
            };
            String[] emojis = {"👍", "❤️", "😂", "😮", "😢", "🙏", "🔥", "🎉"};

            for (int i = 0; i < emojiBtnIds.length; i++) {
                final String emoji = emojis[i];
                View btn = popupView.findViewById(emojiBtnIds[i]);
                if (btn != null) {
                    btn.setOnClickListener(v -> {
                        popupWindow.dismiss();
                        handleReaction(message, emoji);
                    });
                }
            }

            popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int popupHeight = popupView.getMeasuredHeight();
            int popupWidth = popupView.getMeasuredWidth();

            int[] location = new int[2];
            anchorView.getLocationOnScreen(location);
            int anchorX = location[0];
            int anchorY = location[1];

            int displayWidth = getResources().getDisplayMetrics().widthPixels;
            int xPos = anchorX + (anchorView.getWidth() / 2) - (popupWidth / 2);
            if (xPos < 20) xPos = 20;
            if (xPos + popupWidth > displayWidth - 20) xPos = displayWidth - popupWidth - 20;

            int yPos = anchorY - popupHeight - 10;
            if (yPos < 120) {
                yPos = anchorY + anchorView.getHeight() + 10;
            }

            popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xPos, yPos);
        } catch (Exception e) {
            Log.e("ChatActivity", "Failed to show reaction popup", e);
        }
    }

    private void handleReaction(ChatMessage message, String emoji) {
        if (message == null || message.getId() == null) return;
        final String newReaction = (emoji != null && emoji.equals(message.getReaction())) ? null : emoji;
        message.setReaction(newReaction);
        if (chatDb != null) chatDb.updateMessageReaction(message.getId(), newReaction);
        adapter.updateMessageReaction(message.getId(), newReaction);
        for (ChatMessage nm : normalMessages) {
            if (message.getId().equals(nm.getId())) {
                nm.setReaction(newReaction);
                break;
            }
        }

        ChatApiClient.reactToMessage(this, message.getId(), newReaction, currentUserId, new ChatApiClient.ChatCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}

            @Override
            public void onError(String error) {
                Toast.makeText(ChatActivity.this, "Failed to update reaction: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteMessagesDialog(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) return;

        boolean canDeleteForEveryone = true;
        for (ChatMessage m : messages) {
            if (currentUserId == null || !currentUserId.equals(m.getSenderId())) {
                canDeleteForEveryone = false;
                break;
            }
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_messages, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_delete_title);
        View btnEveryone = dialogView.findViewById(R.id.btn_dialog_delete_everyone);
        View btnForMe = dialogView.findViewById(R.id.btn_dialog_delete_for_me);
        View btnCancel = dialogView.findViewById(R.id.btn_dialog_delete_cancel);

        if (tvTitle != null) {
            tvTitle.setText(messages.size() == 1 ? "Delete message?" : "Delete " + messages.size() + " messages?");
        }

        if (btnEveryone != null) {
            btnEveryone.setVisibility(canDeleteForEveryone ? View.VISIBLE : View.GONE);
            btnEveryone.setOnClickListener(v -> {
                dialog.dismiss();
                deleteSelectedMessages(messages, true);
            });
        }

        if (btnForMe != null) {
            btnForMe.setOnClickListener(v -> {
                dialog.dismiss();
                deleteSelectedMessages(messages, false);
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void deleteSelectedMessages(List<ChatMessage> messages, boolean forEveryone) {
        if (messages == null || messages.isEmpty()) return;
        List<String> idsToRemove = new ArrayList<>();
        for (ChatMessage m : messages) {
            if (m.getId() == null) continue;
            idsToRemove.add(m.getId());
            if (forEveryone) {
                if (chatDb != null) chatDb.markDeletedForEveryone(m.getId());
                adapter.markMessageDeleted(m.getId());
                for (ChatMessage nm : normalMessages) {
                    if (m.getId().equals(nm.getId())) {
                        nm.setDeletedForEveryone(true);
                        nm.setMessageText("🚫 This message was deleted");
                        nm.setMediaUrl(null);
                        nm.setReaction(null);
                        break;
                    }
                }
            } else {
                if (chatDb != null) chatDb.deleteMessageForMe(m.getId());
                for (int i = normalMessages.size() - 1; i >= 0; i--) {
                    if (m.getId().equals(normalMessages.get(i).getId())) {
                        normalMessages.remove(i);
                        break;
                    }
                }
            }
            String deleteType = forEveryone ? "for_everyone" : "for_me";
            ChatApiClient.deleteMessage(this, m.getId(), currentUserId, deleteType, new ChatApiClient.ChatCallback<Void>() {
                @Override
                public void onSuccess(Void result) {}

                @Override
                public void onError(String error) {
                    Toast.makeText(ChatActivity.this, "Delete failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (!forEveryone) {
            adapter.removeMessages(idsToRemove);
        }
        adapter.clearSelection();
    }

    private void startReply(ChatMessage msg) {
        this.replyingToMessage = msg;
        if (layoutReplyPreview != null && tvReplyPreviewSender != null && tvReplyPreviewText != null) {
            boolean isSelf = currentUserId != null && currentUserId.equals(msg.getSenderId());
            String senderName = isSelf ? "You" : (otherUserName != null ? otherUserName : "Contact");
            tvReplyPreviewSender.setText("Reply to " + senderName);
            tvReplyPreviewText.setText(msg.getDisplaySnippet());
            layoutReplyPreview.setVisibility(View.VISIBLE);
        }
        if (etInput != null) {
            etInput.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void cancelReply() {
        this.replyingToMessage = null;
        if (layoutReplyPreview != null) {
            layoutReplyPreview.setVisibility(View.GONE);
        }
    }

    private void showChatOverflowMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "View contact");
        popup.getMenu().add(0, 2, 1, "Search");
        popup.getMenu().add(0, 3, 2, "Media, links, and docs");
        popup.getMenu().add(0, 4, 3, "Mute notifications");
        popup.getMenu().add(0, 5, 4, isSecretMode ? "Normal mode (Disable disappearing)" : "Secret mode (Disappearing messages)");
        popup.getMenu().add(0, 6, 5, "Chat theme");
        popup.getMenu().add(0, 7, 6, "Clear chat");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                showContactInfoDialog();
                return true;
            } else if (id == 2) {
                Toast.makeText(this, "Search active in conversation", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == 3) {
                Toast.makeText(this, "Media & documents browser", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == 4) {
                Toast.makeText(this, "Notifications muted", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == 5) {
                toggleMode();
                return true;
            } else if (id == 6) {
                showThemeDialog();
                return true;
            } else if (id == 7) {
                confirmClearChat();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void confirmClearChat() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Chat")
                .setMessage("Are you sure you want to clear messages from this chat?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    if (isSecretMode) {
                        ephemeralSecretMessages.clear();
                    } else {
                        normalMessages.clear();
                        if (chatDb != null) {
                            chatDb.clearConversation(conversationId != null ? conversationId : otherUserId);
                        }
                    }
                    adapter.setMessages(new ArrayList<>());
                    Toast.makeText(this, "Chat cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updatePeerStatus(ChatApiClient.PeerStatus peerStatus) {
        if (tvHeaderStatus == null) return;
        if (peerStatus == null) {
            tvHeaderStatus.setText("offline");
            tvHeaderStatus.setTextColor(Color.parseColor("#94A3B8"));
            return;
        }
        if (peerStatus.isOnline) {
            tvHeaderStatus.setText("online");
            tvHeaderStatus.setTextColor(Color.parseColor("#22C55E"));
        } else if (peerStatus.lastSeen != null && !peerStatus.lastSeen.isEmpty()) {
            tvHeaderStatus.setText(formatLastSeen(peerStatus.lastSeen));
            tvHeaderStatus.setTextColor(Color.parseColor("#94A3B8"));
        } else {
            tvHeaderStatus.setText("offline");
            tvHeaderStatus.setTextColor(Color.parseColor("#94A3B8"));
        }
    }

    private String formatLastSeen(String rawIso) {
        try {
            String clean = rawIso.split("\\.")[0];
            Date d = isoFormat.parse(clean);
            if (d == null) return "offline";
            long now = System.currentTimeMillis();
            long diff = now - d.getTime();
            if (diff < 60000) return "last seen just now";
            SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String timeStr = timeFmt.format(d);
            long days = diff / (24 * 60 * 60 * 1000);
            if (days == 0) {
                return "last seen today at " + timeStr;
            } else if (days == 1) {
                return "last seen yesterday at " + timeStr;
            } else {
                SimpleDateFormat dateFmt = new SimpleDateFormat("MMM d 'at' h:mm a", Locale.getDefault());
                return "last seen " + dateFmt.format(d);
            }
        } catch (Exception e) {
            return "offline";
        }
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
                    if (chatDb != null) {
                        chatDb.saveMessages(result.messages);
                    }
                }

                if (result != null && result.isSecretMode) {
                    applyModeState(true, result.secretSessionId, false, true);
                } else {
                    applyModeState(false, null, false, true);
                }

                if (result != null) {
                    updatePeerStatus(result.peerStatus);
                }

                if (lastPollTimestamp == null) {
                    if (!normalMessages.isEmpty()) {
                        lastPollTimestamp = normalMessages.get(normalMessages.size() - 1).getCreatedAt();
                    } else {
                        // Do not use local client clock which can be ahead of server! Leave null so first poll uses server lookback
                        lastPollTimestamp = null;
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
        String text = etInput.getText() != null ? etInput.getText().toString().trim() : "";
        if (text.isEmpty()) return;

        if (replyingToMessage != null) {
            boolean isSelf = currentUserId != null && currentUserId.equals(replyingToMessage.getSenderId());
            String senderName = isSelf ? "You" : (otherUserName != null ? otherUserName : "Contact");
            String snippet = replyingToMessage.getDisplaySnippet();
            text = ChatMessage.formatReply(senderName, snippet, text);
            cancelReply();
        }

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
                ChatMessage.STATUS_PENDING,
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

        PendingMessage task = new PendingMessage();
        task.optimisticMsg = optimisticMsg;
        task.text = text;
        task.activeMode = activeMode;
        task.sessionUuid = sessionUuid;
        pendingMessagesQueue.add(task);

        drainPendingQueue();
    }

    private void drainPendingQueue() {
        if (isDrainingQueue || pendingMessagesQueue.isEmpty()) return;
        final PendingMessage task = pendingMessagesQueue.get(0);
        isDrainingQueue = true;

        ChatApiClient.sendMessage(this, currentUserId, otherUserId, task.text, task.mediaUrl, task.mediaType, task.fileName, task.fileSize, task.activeMode, task.sessionUuid, new ChatApiClient.ChatCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage sentMessage) {
                isDrainingQueue = false;
                pendingMessagesQueue.remove(task);
                btnSend.setVisibility(View.VISIBLE);
                pbSending.setVisibility(View.GONE);
                if (sentMessage != null) {
                    String serverId = sentMessage.getId();
                    String st = sentMessage.getStatus() != null ? sentMessage.getStatus() : ChatMessage.STATUS_SENT;
                    if (serverId != null) {
                        loadedMessageIds.add(serverId);
                    }
                    adapter.updateTempMessage(task.optimisticMsg.getId(), serverId, st);
                    if (serverId != null) {
                        task.optimisticMsg.setId(serverId);
                    }
                    task.optimisticMsg.setStatus(st);

                    if (chatDb != null && !isSecretMode) {
                        chatDb.saveMessage(sentMessage);
                    }

                    if (conversationId == null && sentMessage.getConversationId() != null) {
                        conversationId = sentMessage.getConversationId();
                        activeConversationId = conversationId;
                    }
                }
                if (!pendingMessagesQueue.isEmpty()) {
                    drainPendingQueue();
                }
            }

            @Override
            public void onError(String errorMessage) {
                isDrainingQueue = false;
                btnSend.setVisibility(View.VISIBLE);
                pbSending.setVisibility(View.GONE);
                // Retain in pending queue to auto-retry as soon as network returns
                if (task.optimisticMsg != null) {
                    task.optimisticMsg.setStatus(ChatMessage.STATUS_PENDING);
                    adapter.updateMessageStatus(task.optimisticMsg.getId(), ChatMessage.STATUS_PENDING);
                }
            }
        });
    }

    private void showAttachmentDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog sheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_attachment_picker, null);
        sheet.setContentView(view);

        View btnDoc = view.findViewById(R.id.btn_attach_doc);
        View btnCamera = view.findViewById(R.id.btn_attach_camera);
        View btnGallery = view.findViewById(R.id.btn_attach_gallery);
        View btnVideo = view.findViewById(R.id.btn_attach_video);
        View btnContact = view.findViewById(R.id.btn_attach_contact);

        if (btnDoc != null) {
            btnDoc.setOnClickListener(v -> {
                sheet.dismiss();
                pickFileLauncher.launch("*/*");
            });
        }
        if (btnCamera != null) {
            btnCamera.setOnClickListener(v -> {
                sheet.dismiss();
                takePhotoLauncher.launch(null);
            });
        }
        if (btnGallery != null) {
            btnGallery.setOnClickListener(v -> {
                sheet.dismiss();
                pickImageLauncher.launch("image/*");
            });
        }
        if (btnVideo != null) {
            btnVideo.setOnClickListener(v -> {
                sheet.dismiss();
                pickVideoLauncher.launch("video/*");
            });
        }
        if (btnContact != null) {
            btnContact.setOnClickListener(v -> {
                sheet.dismiss();
                Toast.makeText(this, "Select contact to share", Toast.LENGTH_SHORT).show();
            });
        }

        sheet.show();
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
                                        if (chatDb != null) {
                                            chatDb.saveMessage(sentMessage);
                                        }
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

                        if (result.peerStatus != null) {
                            updatePeerStatus(result.peerStatus);
                        }

                        // Synchronize dual mode across peers
                        if (result.conversationMode != null) {
                            boolean remoteSecret = result.conversationMode.isSecretMode;
                            String remoteSession = result.conversationMode.secretSessionId;
                            if (remoteSecret != isSecretMode || (remoteSecret && remoteSession != null && !remoteSession.equals(currentSecretSessionId))) {
                                applyModeState(remoteSecret, remoteSession, true, false);
                            }
                        }

                        // Process status updates for messages (sent -> delivered -> read, reactions, deletions)
                        if (result.statusUpdates != null && !result.statusUpdates.isEmpty()) {
                            for (JsonObject su : result.statusUpdates) {
                                if (su.has("id")) {
                                    String msgId = su.get("id").getAsString();
                                    if (su.has("status") && !su.get("status").isJsonNull()) {
                                        String newStatus = su.get("status").getAsString();
                                        if (chatDb != null) chatDb.updateMessageStatus(msgId, newStatus);
                                        adapter.updateMessageStatus(msgId, newStatus);
                                        for (ChatMessage nm : normalMessages) {
                                            if (msgId.equals(nm.getId())) {
                                                nm.setStatus(newStatus);
                                                break;
                                            }
                                        }
                                    }
                                    if (su.has("reaction")) {
                                        String rx = su.get("reaction").isJsonNull() ? null : su.get("reaction").getAsString();
                                        if (chatDb != null) chatDb.updateMessageReaction(msgId, rx);
                                        adapter.updateMessageReaction(msgId, rx);
                                        for (ChatMessage nm : normalMessages) {
                                            if (msgId.equals(nm.getId())) {
                                                nm.setReaction(rx);
                                                break;
                                            }
                                        }
                                    }
                                    if (su.has("is_deleted_for_everyone") && !su.get("is_deleted_for_everyone").isJsonNull() && su.get("is_deleted_for_everyone").getAsBoolean()) {
                                        if (chatDb != null) chatDb.markDeletedForEveryone(msgId);
                                        adapter.markMessageDeleted(msgId);
                                        for (ChatMessage nm : normalMessages) {
                                            if (msgId.equals(nm.getId())) {
                                                nm.setDeletedForEveryone(true);
                                                nm.setMessageText("🚫 This message was deleted");
                                                nm.setMediaUrl(null);
                                                nm.setReaction(null);
                                                break;
                                            }
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
                                    if (conversationId == null && msg.getConversationId() != null) {
                                        conversationId = msg.getConversationId();
                                        activeConversationId = conversationId;
                                    }
                                    if (msg.getId() != null) loadedMessageIds.add(msg.getId());
                                    
                                    if (msg.isSecretMode()) {
                                        if (isSecretMode) {
                                            ephemeralSecretMessages.add(msg);
                                            adapter.addMessage(msg);
                                            hasNew = true;
                                        }
                                    } else {
                                        normalMessages.add(msg);
                                        if (chatDb != null) {
                                            chatDb.saveMessage(msg);
                                        }
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

                        // Resilient offline auto-retry: if messages were queued while offline, retry now that connection is active!
                        if (!pendingMessagesQueue.isEmpty()) {
                            drainPendingQueue();
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
        if (!pendingMessagesQueue.isEmpty()) {
            drainPendingQueue();
        }
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
    public void onBackPressed() {
        if (adapter != null && adapter.isSelectionMode()) {
            adapter.clearSelection();
            return;
        }
        super.onBackPressed();
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
