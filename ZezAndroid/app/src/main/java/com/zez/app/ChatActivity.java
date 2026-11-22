package com.zez.app;

import android.content.ContentResolver;
import android.content.Context;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zez.app.adapter.ChatMessageAdapter;
import com.zez.app.model.ChatMessage;
import com.zez.app.model.ChatUser;
import com.zez.app.util.ChatApiClient;
import com.zez.app.util.ChatNotificationHelper;
import com.zez.app.util.ChatPreferencesHelper;
import com.zez.app.util.PreferencesHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    public static volatile String activeConversationId = null;

    private String conversationId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserPhone;
    private String otherUserAvatar;
    private String currentUserId;

    private TextView tvHeaderName;
    private TextView tvHeaderStatus;
    private ImageView ivChatHeaderAvatar;
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
    private ImageView ivReplyPreviewImage;
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

    // In-Chat Search
    private View layoutChatSearchBar;
    private ImageButton btnSearchBack;
    private EditText etChatSearchQuery;
    private TextView tvSearchMatchCount;
    private ImageButton btnSearchPrev;
    private ImageButton btnSearchNext;
    private ImageButton btnSearchClear;
    private final List<Integer> searchMatchPositions = new ArrayList<>();
    private int currentSearchMatchIndex = -1;
    private boolean isSearchMode = false;

    private ActivityResultLauncher<String> pickWallpaperLauncher;
    private ActivityResultLauncher<String> pickAvatarLauncher;

    private View layoutChatRoot;
    private ImageView ivChatWallpaper;
    private View viewChatWallpaperDim;
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
        otherUserAvatar = getIntent().getStringExtra("extra_other_user_avatar");
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

        pickWallpaperLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) saveAndApplyCustomWallpaper(uri);
        });

        pickAvatarLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                Toast.makeText(this, "Uploading profile picture...", Toast.LENGTH_SHORT).show();
                ChatApiClient.uploadAvatar(this, uri, currentUserId, new ChatApiClient.ChatCallback<String>() {
                    @Override
                    public void onSuccess(String relativeUrl) {
                        ChatPreferencesHelper.saveUserAvatar(ChatActivity.this, relativeUrl);
                        loadHeaderAvatar();
                        Toast.makeText(ChatActivity.this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(ChatActivity.this, "Failed to upload photo: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initViews() {
        tvHeaderName = findViewById(R.id.tv_chat_header_name);
        tvHeaderStatus = findViewById(R.id.tv_chat_header_status);
        ivChatHeaderAvatar = findViewById(R.id.iv_chat_header_avatar);
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
        ivReplyPreviewImage = findViewById(R.id.iv_reply_preview_image);
        btnCancelReply = findViewById(R.id.btn_cancel_reply);

        layoutNormalHeader = findViewById(R.id.layout_chat_normal_header);
        layoutSelectionBar = findViewById(R.id.layout_chat_selection_bar);
        btnSelectionClose = findViewById(R.id.btn_selection_close);
        tvSelectionCount = findViewById(R.id.tv_selection_count);
        btnSelectionReply = findViewById(R.id.btn_selection_reply);
        btnSelectionDelete = findViewById(R.id.btn_selection_delete);

        layoutChatRoot = findViewById(R.id.layout_chat_root);
        ivChatWallpaper = findViewById(R.id.iv_chat_wallpaper);
        viewChatWallpaperDim = findViewById(R.id.view_chat_wallpaper_dim);
        btnVideocall = findViewById(R.id.btn_chat_videocall);
        btnAudiocall = findViewById(R.id.btn_chat_audiocall);
        btnSelectionStar = findViewById(R.id.btn_selection_star);
        btnSelectionForward = findViewById(R.id.btn_selection_forward);
        btnSelectionMore = findViewById(R.id.btn_selection_more);

        layoutChatSearchBar = findViewById(R.id.layout_chat_search_bar);
        btnSearchBack = findViewById(R.id.btn_search_back);
        etChatSearchQuery = findViewById(R.id.et_chat_search_query);
        tvSearchMatchCount = findViewById(R.id.tv_search_match_count);
        btnSearchPrev = findViewById(R.id.btn_search_prev);
        btnSearchNext = findViewById(R.id.btn_search_next);
        btnSearchClear = findViewById(R.id.btn_search_clear);

        if (layoutNormalHeader != null) {
            layoutNormalHeader.setOnClickListener(v -> showContactInfoDialog());
        }
        if (ivChatHeaderAvatar != null) {
            ivChatHeaderAvatar.setOnClickListener(v -> showContactInfoDialog());
        }

        loadHeaderAvatar();
        fetchPeerProfile();

        if (btnSearchBack != null) {
            btnSearchBack.setOnClickListener(v -> closeChatSearch());
        }
        if (btnSearchClear != null) {
            btnSearchClear.setOnClickListener(v -> {
                if (etChatSearchQuery != null) etChatSearchQuery.setText("");
            });
        }
        if (btnSearchPrev != null) {
            btnSearchPrev.setOnClickListener(v -> navigateSearchMatch(-1));
        }
        if (btnSearchNext != null) {
            btnSearchNext.setOnClickListener(v -> navigateSearchMatch(1));
        }
        if (etChatSearchQuery != null) {
            etChatSearchQuery.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    performChatSearch(s != null ? s.toString() : "");
                }
                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        applyChatTheme(ChatPreferencesHelper.getChatTheme(this));

        boolean isSelfChat = otherUserId != null && otherUserId.equals(currentUserId);
        if (isSelfChat) {
            otherUserName = "You (Message yourself)";
            tvHeaderName.setText("You (Message yourself)");
            tvHeaderStatus.setText("Message yourself · Notes, links, drafts");
            if (btnAudiocall != null) btnAudiocall.setVisibility(View.GONE);
            if (btnVideocall != null) btnVideocall.setVisibility(View.GONE);
        } else {
            otherUserName = com.zez.app.util.PhoneHelper.resolveContactName(this, otherUserPhone, otherUserName);
            tvHeaderName.setText(otherUserName != null ? otherUserName : otherUserPhone);
            tvHeaderStatus.setText("Connecting...");
        }

        int accentColor = com.zez.app.util.PreferencesHelper.getAccentColorInt(this);
        if (btnSendContainer != null) {
            androidx.core.view.ViewCompat.setBackgroundTintList(btnSendContainer, android.content.res.ColorStateList.valueOf(accentColor));
        }

        btnBack.setOnClickListener(v -> finish());
        btnSendContainer.setOnClickListener(v -> sendMessage());
        if (btnAttach != null) {
            btnAttach.setOnClickListener(v -> showAttachmentDialog());
        }

        if (btnAudiocall != null) {
            btnAudiocall.setOnClickListener(v -> startCall("audio"));
        }

        if (btnVideocall != null) {
            btnVideocall.setOnClickListener(v -> startCall("video"));
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

    private void saveAndApplyCustomWallpaper(Uri uri) {
        try {
            java.io.InputStream is = getContentResolver().openInputStream(uri);
            if (is != null) {
                File wallpaperFile = new File(getFilesDir(), "custom_chat_wallpaper.jpg");
                java.io.FileOutputStream fos = new java.io.FileOutputStream(wallpaperFile);
                byte[] buf = new byte[8192];
                int read;
                while ((read = is.read(buf)) != -1) {
                    fos.write(buf, 0, read);
                }
                fos.close();
                is.close();
                ChatPreferencesHelper.saveChatTheme(this, "custom");
                applyChatTheme("custom");
                Toast.makeText(this, "Custom wallpaper applied", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to apply wallpaper: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyChatTheme(String theme) {
        if (layoutChatRoot == null) return;
        float savedDim = ChatPreferencesHelper.getWallpaperDim(this);

        if ("custom".equalsIgnoreCase(theme)) {
            File wallpaperFile = new File(getFilesDir(), "custom_chat_wallpaper.jpg");
            if (wallpaperFile.exists()) {
                if (ivChatWallpaper != null) {
                    ivChatWallpaper.setVisibility(View.VISIBLE);
                    com.bumptech.glide.Glide.with(this)
                            .load(wallpaperFile)
                            .centerCrop()
                            .into(ivChatWallpaper);
                }
                if (viewChatWallpaperDim != null) {
                    viewChatWallpaperDim.setVisibility(savedDim > 0.001f ? View.VISIBLE : View.GONE);
                    viewChatWallpaperDim.setAlpha(savedDim);
                }
                layoutChatRoot.setBackground(null);
                return;
            }
        }

        if (ivChatWallpaper != null) {
            ivChatWallpaper.setVisibility(View.GONE);
        }
        if (viewChatWallpaperDim != null) {
            viewChatWallpaperDim.setVisibility(savedDim > 0.001f ? View.VISIBLE : View.GONE);
            viewChatWallpaperDim.setAlpha(savedDim);
        }

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

        View optGallery = view.findViewById(R.id.theme_opt_gallery);
        if (optGallery != null) {
            optGallery.setOnClickListener(v -> {
                sheet.dismiss();
                if (pickWallpaperLauncher != null) {
                    pickWallpaperLauncher.launch("image/*");
                }
            });
        }

        // WhatsApp-style Wallpaper Dimming Slider
        android.widget.SeekBar seekDim = view.findViewById(R.id.seek_wallpaper_dim);
        TextView tvDimValue = view.findViewById(R.id.tv_wallpaper_dim_value);
        float currentDim = ChatPreferencesHelper.getWallpaperDim(this);
        int currentProgress = Math.round(currentDim * 100f);
        if (seekDim != null) {
            seekDim.setProgress(currentProgress);
            if (tvDimValue != null) tvDimValue.setText(currentProgress + "%");
            seekDim.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                    if (tvDimValue != null) tvDimValue.setText(progress + "%");
                    float alpha = progress / 100f;
                    ChatPreferencesHelper.setWallpaperDim(ChatActivity.this, alpha);
                    if (viewChatWallpaperDim != null) {
                        viewChatWallpaperDim.setVisibility(alpha > 0.001f ? View.VISIBLE : View.GONE);
                        viewChatWallpaperDim.setAlpha(alpha);
                    }
                }

                @Override
                public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
            });
        }

        // Reset to Default Wallpaper button
        View optResetDefault = view.findViewById(R.id.theme_opt_reset_default);
        if (optResetDefault != null) {
            optResetDefault.setOnClickListener(v -> {
                ChatPreferencesHelper.saveChatTheme(this, "default");
                ChatPreferencesHelper.setWallpaperDim(this, 0.0f);
                if (ivChatWallpaper != null) ivChatWallpaper.setVisibility(View.GONE);
                if (viewChatWallpaperDim != null) viewChatWallpaperDim.setVisibility(View.GONE);
                if (seekDim != null) seekDim.setProgress(0);
                if (tvDimValue != null) tvDimValue.setText("0%");
                applyChatTheme("default");
                sheet.dismiss();
                Toast.makeText(this, "Reset to default wallpaper", Toast.LENGTH_SHORT).show();
            });
        }

        sheet.show();
    }

    private void showContactInfoDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog sheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_contact_profile, null);
        sheet.setContentView(view);

        ImageButton btnBack = view.findViewById(R.id.btn_profile_back);
        ImageButton btnMore = view.findViewById(R.id.btn_profile_more);
        ImageView ivAvatar = view.findViewById(R.id.iv_profile_avatar);
        TextView tvInitials = view.findViewById(R.id.tv_profile_initials);
        TextView tvName = view.findViewById(R.id.tv_profile_name);
        TextView tvPhone = view.findViewById(R.id.tv_profile_phone);
        TextView tvLastSeen = view.findViewById(R.id.tv_profile_last_seen);

        View btnCall = view.findViewById(R.id.btn_profile_call);
        View btnVideo = view.findViewById(R.id.btn_profile_videocall);
        View btnPay = view.findViewById(R.id.btn_profile_pay);
        View btnSearch = view.findViewById(R.id.btn_profile_search);

        RecyclerView rvMedia = view.findViewById(R.id.rv_profile_media);
        TextView tvMediaCount = view.findViewById(R.id.tv_profile_media_count);
        TextView tvMediaEmpty = view.findViewById(R.id.tv_profile_media_empty);
        TextView tvStorageSize = view.findViewById(R.id.tv_profile_storage_size);
        TextView tvStarredCount = view.findViewById(R.id.tv_profile_starred_count);

        View rowStorage = view.findViewById(R.id.row_profile_storage);
        View rowNotifications = view.findViewById(R.id.row_profile_notifications);
        View rowVisibility = view.findViewById(R.id.row_profile_visibility);
        View rowStarred = view.findViewById(R.id.row_profile_starred);
        View rowEncryption = view.findViewById(R.id.row_profile_encryption);

        boolean isSelfChat = otherUserId != null && otherUserId.equals(currentUserId);
        String name = isSelfChat ? "You (Message yourself)" : (otherUserName != null && !otherUserName.trim().isEmpty() ? otherUserName.trim() : "Contact");
        String phone = otherUserPhone != null && !otherUserPhone.trim().isEmpty() ? otherUserPhone.trim() : "";

        if (btnBack != null) btnBack.setOnClickListener(v -> sheet.dismiss());

        if (tvName != null) tvName.setText(name);
        if (tvPhone != null) {
            if (!phone.isEmpty() && !isSelfChat) {
                tvPhone.setText(phone.startsWith("+") ? phone : "+91 " + phone);
                tvPhone.setVisibility(View.VISIBLE);
            } else {
                tvPhone.setVisibility(View.GONE);
            }
        }

        if (tvLastSeen != null) {
            if (isSelfChat) {
                tvLastSeen.setText("Message yourself · Notes, links, drafts");
            } else if (tvHeaderStatus != null && tvHeaderStatus.getText() != null) {
                tvLastSeen.setText(tvHeaderStatus.getText());
            } else {
                tvLastSeen.setText("offline");
            }
        }

        String avatarToLoad = isSelfChat ? ChatPreferencesHelper.getUserAvatar(this) : otherUserAvatar;
        if (ivAvatar != null) {
            if (avatarToLoad != null && !avatarToLoad.trim().isEmpty()) {
                String fullUrl = ChatApiClient.getAbsoluteMediaUrl(this, avatarToLoad);
                ivAvatar.setImageTintList(null);
                ivAvatar.clearColorFilter();
                ivAvatar.setPadding(0, 0, 0, 0);
                Glide.with(this)
                        .load(fullUrl)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.bg_circle_glass)
                        .error(R.drawable.ic_person)
                        .into(ivAvatar);
                if (tvInitials != null) tvInitials.setVisibility(View.GONE);

                ivAvatar.setClickable(true);
                ivAvatar.setFocusable(true);
                if (isSelfChat) {
                    ivAvatar.setOnClickListener(v -> {
                        if (pickAvatarLauncher != null) {
                            pickAvatarLauncher.launch("image/*");
                            sheet.dismiss();
                        }
                    });
                } else {
                    ivAvatar.setOnClickListener(v -> {
                        Intent viewerIntent = new Intent(this, ImageViewerActivity.class);
                        viewerIntent.putExtra(ImageViewerActivity.EXTRA_URI, fullUrl);
                        viewerIntent.putExtra(ImageViewerActivity.EXTRA_NAME, name);
                        startActivity(viewerIntent);
                    });
                }
            } else {
                ivAvatar.clearColorFilter();
                ivAvatar.setImageResource(R.drawable.ic_person);
                ivAvatar.setImageTintList(ContextCompat.getColorStateList(this, R.color.theme_icon_tint));
                int pad = (int) (20 * getResources().getDisplayMetrics().density);
                ivAvatar.setPadding(pad, pad, pad, pad);
                if (isSelfChat) {
                    ivAvatar.setClickable(true);
                    ivAvatar.setFocusable(true);
                    ivAvatar.setOnClickListener(v -> {
                        if (pickAvatarLauncher != null) {
                            pickAvatarLauncher.launch("image/*");
                            sheet.dismiss();
                        }
                    });
                } else {
                    ivAvatar.setOnClickListener(null);
                }
            }
        }

        if (btnCall != null) {
            btnCall.setOnClickListener(v -> {
                sheet.dismiss();
                startCall("audio");
            });
        }

        if (btnVideo != null) {
            btnVideo.setOnClickListener(v -> {
                sheet.dismiss();
                startCall("video");
            });
        }

        if (btnPay != null) {
            btnPay.setOnClickListener(v -> {
                try {
                    String cleanDigits = phone.replaceAll("\\D", "");
                    String upiUri = "upi://pay?pa=" + (cleanDigits.isEmpty() ? "contact" : cleanDigits) + "@upi&pn=" + Uri.encode(name) + "&cu=INR";
                    Intent payIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(upiUri));
                    startActivity(Intent.createChooser(payIntent, "Pay with"));
                } catch (Exception e) {
                    Toast.makeText(this, "No UPI payment app installed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                sheet.dismiss();
                openChatSearch();
            });
        }

        List<ChatMessage> mediaList = new ArrayList<>();
        long totalMediaBytes = 0;
        for (ChatMessage m : normalMessages) {
            if (m.getMediaUrl() != null && !m.getMediaUrl().trim().isEmpty() && !m.isDeletedForEveryone()) {
                mediaList.add(m);
                if (m.getFileSize() != null && m.getFileSize() > 0) {
                    totalMediaBytes += m.getFileSize();
                } else {
                    totalMediaBytes += 250 * 1024;
                }
            }
        }

        if (tvMediaCount != null) {
            tvMediaCount.setText(mediaList.size() + " >");
        }

        if (rvMedia != null) {
            if (mediaList.isEmpty()) {
                rvMedia.setVisibility(View.GONE);
                if (tvMediaEmpty != null) tvMediaEmpty.setVisibility(View.VISIBLE);
            } else {
                rvMedia.setVisibility(View.VISIBLE);
                if (tvMediaEmpty != null) tvMediaEmpty.setVisibility(View.GONE);
                ProfileMediaAdapter mediaAdapter = new ProfileMediaAdapter(this, mediaList);
                rvMedia.setAdapter(mediaAdapter);
            }
        }

        if (tvStorageSize != null) {
            if (totalMediaBytes > 0) {
                if (totalMediaBytes < 1024 * 1024) {
                    tvStorageSize.setText(String.format(Locale.getDefault(), "%.1f KB", totalMediaBytes / 1024.0));
                } else {
                    tvStorageSize.setText(String.format(Locale.getDefault(), "%.1f MB", totalMediaBytes / (1024.0 * 1024.0)));
                }
            } else {
                tvStorageSize.setText("0 B");
            }
        }

        if (btnMore != null) {
            btnMore.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, v);
                popup.getMenu().add(0, 1, 0, "Share");
                popup.getMenu().add(0, 2, 1, "Edit");
                popup.getMenu().add(0, 3, 2, "View in address book");
                popup.getMenu().add(0, 4, 3, "Verify security code");
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == 1) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Contact: " + name + "\nPhone: " + phone);
                        startActivity(Intent.createChooser(shareIntent, "Share contact"));
                        return true;
                    } else if (itemId == 2) {
                        showEditContactNameDialog();
                        sheet.dismiss();
                        return true;
                    } else if (itemId == 3) {
                        try {
                            Intent addressIntent = new Intent(Intent.ACTION_VIEW);
                            addressIntent.setData(android.provider.ContactsContract.Contacts.CONTENT_URI);
                            startActivity(addressIntent);
                        } catch (Exception e) {
                            Toast.makeText(this, "Cannot open address book", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    } else if (itemId == 4) {
                        showSecurityCodeDialog();
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        if (rowEncryption != null) {
            rowEncryption.setOnClickListener(v -> showSecurityCodeDialog());
        }

        if (rowNotifications != null) {
            rowNotifications.setOnClickListener(v -> Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show());
        }

        if (rowVisibility != null) {
            rowVisibility.setOnClickListener(v -> Toast.makeText(this, "Media visibility: Default (Yes)", Toast.LENGTH_SHORT).show());
        }

        if (rowStarred != null) {
            rowStarred.setOnClickListener(v -> Toast.makeText(this, "No starred messages", Toast.LENGTH_SHORT).show());
        }

        sheet.show();
    }

    private void showSecurityCodeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Verify Security Code")
                .setMessage("Messages and calls with this contact are protected with end-to-end encryption.\n\nSecurity Code:\n38920 18492 84921 04829\n58392 01849 20482 91840\n18492 84921 04829 38920\n\nIf you and this contact verify this code, you can be sure no third party can read your messages or listen to your calls.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showEditContactNameDialog() {
        EditText input = new EditText(this);
        input.setText(otherUserName);
        if (input.getText() != null) input.setSelection(input.getText().length());
        new AlertDialog.Builder(this)
                .setTitle("Edit Contact Name")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        otherUserName = newName;
                        if (tvHeaderName != null) tvHeaderName.setText(newName);
                        Toast.makeText(this, "Contact name updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadHeaderAvatar() {
        if (ivChatHeaderAvatar == null) return;
        boolean isSelfChat = otherUserId != null && otherUserId.equals(currentUserId);
        String avatarToLoad = isSelfChat ? ChatPreferencesHelper.getUserAvatar(this) : otherUserAvatar;

        if (avatarToLoad != null && !avatarToLoad.trim().isEmpty()) {
            String fullUrl = ChatApiClient.getAbsoluteMediaUrl(this, avatarToLoad);
            ivChatHeaderAvatar.setImageTintList(null);
            ivChatHeaderAvatar.clearColorFilter();
            ivChatHeaderAvatar.setPadding(0, 0, 0, 0);
            Glide.with(this)
                    .load(fullUrl)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_circle_glass)
                    .error(R.drawable.ic_person)
                    .into(ivChatHeaderAvatar);
        } else {
            ivChatHeaderAvatar.clearColorFilter();
            ivChatHeaderAvatar.setImageResource(R.drawable.ic_person);
            ivChatHeaderAvatar.setImageTintList(ContextCompat.getColorStateList(this, R.color.theme_text_secondary));
            int pad = (int) (6 * getResources().getDisplayMetrics().density);
            ivChatHeaderAvatar.setPadding(pad, pad, pad, pad);
        }
    }

    private void fetchPeerProfile() {
        boolean isSelfChat = otherUserId != null && otherUserId.equals(currentUserId);
        String lookupKey = isSelfChat ? currentUserId : (otherUserId != null ? otherUserId : otherUserPhone);
        if (lookupKey == null || lookupKey.trim().isEmpty()) return;

        ChatApiClient.getUserProfile(this, lookupKey, new ChatApiClient.ChatCallback<ChatUser>() {
            @Override
            public void onSuccess(ChatUser user) {
                if (user != null) {
                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty()) {
                        if (isSelfChat) {
                            ChatPreferencesHelper.saveUserAvatar(ChatActivity.this, user.getAvatarUrl());
                        } else {
                            otherUserAvatar = user.getAvatarUrl();
                        }
                        loadHeaderAvatar();
                    }
                    if (user.getName() != null && !user.getName().trim().isEmpty()) {
                        if (!isSelfChat && (otherUserName == null || otherUserName.equals(otherUserPhone))) {
                            otherUserName = user.getName();
                            if (tvHeaderName != null) tvHeaderName.setText(otherUserName);
                        }
                    }
                }
            }

            @Override
            public void onError(String error) {
                // Ignore silent fetch errors
            }
        });
    }

    private void openChatSearch() {
        isSearchMode = true;
        if (layoutNormalHeader != null) layoutNormalHeader.setVisibility(View.GONE);
        if (layoutSelectionBar != null) layoutSelectionBar.setVisibility(View.GONE);
        if (layoutChatSearchBar != null) layoutChatSearchBar.setVisibility(View.VISIBLE);
        if (etChatSearchQuery != null) {
            etChatSearchQuery.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etChatSearchQuery, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            performChatSearch(etChatSearchQuery.getText() != null ? etChatSearchQuery.getText().toString() : "");
        }
    }

    private void closeChatSearch() {
        isSearchMode = false;
        if (layoutChatSearchBar != null) layoutChatSearchBar.setVisibility(View.GONE);
        if (layoutNormalHeader != null) layoutNormalHeader.setVisibility(View.VISIBLE);
        if (etChatSearchQuery != null) {
            etChatSearchQuery.setText("");
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(etChatSearchQuery.getWindowToken(), 0);
        }
        if (adapter != null) {
            adapter.setHighlightedMessageId(null);
        }
        searchMatchPositions.clear();
        currentSearchMatchIndex = -1;
        if (tvSearchMatchCount != null) tvSearchMatchCount.setText("");
    }

    private void performChatSearch(String query) {
        if (adapter == null) return;
        searchMatchPositions.clear();
        searchMatchPositions.addAll(adapter.searchMessagePositions(query));
        if (searchMatchPositions.isEmpty()) {
            currentSearchMatchIndex = -1;
            adapter.setHighlightedMessageId(null);
            if (tvSearchMatchCount != null) {
                tvSearchMatchCount.setText(query.trim().isEmpty() ? "" : "0 of 0");
            }
        } else {
            currentSearchMatchIndex = searchMatchPositions.size() - 1;
            highlightAndScrollMatch();
        }
    }

    private void navigateSearchMatch(int direction) {
        if (searchMatchPositions.isEmpty()) return;
        if (direction < 0) {
            currentSearchMatchIndex--;
            if (currentSearchMatchIndex < 0) currentSearchMatchIndex = searchMatchPositions.size() - 1;
        } else {
            currentSearchMatchIndex++;
            if (currentSearchMatchIndex >= searchMatchPositions.size()) currentSearchMatchIndex = 0;
        }
        highlightAndScrollMatch();
    }

    private void highlightAndScrollMatch() {
        if (currentSearchMatchIndex < 0 || currentSearchMatchIndex >= searchMatchPositions.size()) return;
        int targetPos = searchMatchPositions.get(currentSearchMatchIndex);
        if (tvSearchMatchCount != null) {
            tvSearchMatchCount.setText((currentSearchMatchIndex + 1) + " of " + searchMatchPositions.size());
        }
        if (adapter != null) {
            ChatMessage msg = adapter.getMessageAt(targetPos);
            if (msg != null) adapter.setHighlightedMessageId(msg.getId());
        }
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(targetPos);
        }
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(currentUserId);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

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

        adapter.setOnMediaRetryListener(this::retryMediaMessage);

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

            if (ivReplyPreviewImage != null) {
                String quoteMedia = msg.getMediaUrl();
                if (quoteMedia != null && !quoteMedia.trim().isEmpty()) {
                    ivReplyPreviewImage.setVisibility(View.VISIBLE);
                    String fullUrl = ChatApiClient.getAbsoluteMediaUrl(this, quoteMedia);
                    Glide.with(this)
                            .load(fullUrl)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.bg_search_input)
                            .error(R.drawable.ic_photo_library)
                            .into(ivReplyPreviewImage);
                } else {
                    ivReplyPreviewImage.setVisibility(View.GONE);
                }
            }
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
        if (ivReplyPreviewImage != null) {
            ivReplyPreviewImage.setVisibility(View.GONE);
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
                openChatSearch();
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
        if (otherUserId != null && otherUserId.equals(currentUserId)) {
            String selfText = "Message yourself · Notes, links, drafts";
            if (!selfText.equals(tvHeaderStatus.getText())) {
                tvHeaderStatus.setText(selfText);
                tvHeaderStatus.setTextColor(Color.parseColor("#94A3B8"));
            }
            return;
        }
        String statusText;
        int statusColor;
        if (peerStatus == null) {
            statusText = "offline";
            statusColor = Color.parseColor("#94A3B8");
        } else if (peerStatus.isOnline) {
            statusText = "online";
            statusColor = Color.parseColor("#22C55E");
        } else if (peerStatus.lastSeen != null && !peerStatus.lastSeen.isEmpty()) {
            statusText = formatLastSeen(peerStatus.lastSeen);
            statusColor = Color.parseColor("#94A3B8");
        } else {
            statusText = "offline";
            statusColor = Color.parseColor("#94A3B8");
        }

        CharSequence current = tvHeaderStatus.getText();
        if (current == null || !statusText.equals(current.toString())) {
            tvHeaderStatus.setText(statusText);
            tvHeaderStatus.setTextColor(statusColor);
        }

        if (peerStatus != null) {
            if (peerStatus.avatarUrl != null && !peerStatus.avatarUrl.trim().isEmpty()) {
                if (!peerStatus.avatarUrl.equals(otherUserAvatar)) {
                    otherUserAvatar = peerStatus.avatarUrl;
                    loadHeaderAvatar();
                }
            }
            if (peerStatus.name != null && !peerStatus.name.trim().isEmpty()) {
                if (otherUserName == null || otherUserName.equals(otherUserPhone)) {
                    otherUserName = peerStatus.name;
                    if (tvHeaderName != null) tvHeaderName.setText(otherUserName);
                }
            }
        }
    }

    private Date parseIsoDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        raw = raw.trim();
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = sdf.parse(raw);
                if (d != null) return d;
            } catch (Exception ignored) {}
        }
        if (raw.contains(".")) {
            try {
                String clean = raw.split("\\.")[0];
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = sdf.parse(clean);
                if (d != null) return d;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String formatLastSeen(String rawIso) {
        try {
            Date d = parseIsoDate(rawIso);
            if (d == null) return "offline";

            Calendar nowCal = Calendar.getInstance();
            Calendar seenCal = Calendar.getInstance();
            seenCal.setTime(d);

            SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String timeStr = timeFmt.format(d);

            boolean sameYear = nowCal.get(Calendar.YEAR) == seenCal.get(Calendar.YEAR);
            int dayDiff = sameYear ? nowCal.get(Calendar.DAY_OF_YEAR) - seenCal.get(Calendar.DAY_OF_YEAR) : 999;

            if (sameYear && dayDiff == 0) {
                return "last seen today at " + timeStr;
            } else if (sameYear && dayDiff == 1) {
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
            String replyMedia = (replyingToMessage.getMediaUrl() != null && !replyingToMessage.getMediaUrl().trim().isEmpty())
                    ? replyingToMessage.getMediaUrl()
                    : replyingToMessage.getLocalUri();
            text = ChatMessage.formatReply(senderName, snippet, text, replyMedia);
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

    private static class PendingMediaTask {
        ChatMessage optimisticMsg;
        Uri uri;
        String displayName;
        String mimeType;
        String caption;
        String defaultType;
        long fileSize;
        String activeMode;
        String sessionUuid;

        PendingMediaTask(ChatMessage optimisticMsg, Uri uri, String displayName, String mimeType, String caption, String defaultType, long fileSize, String activeMode, String sessionUuid) {
            this.optimisticMsg = optimisticMsg;
            this.uri = uri;
            this.displayName = displayName;
            this.mimeType = mimeType;
            this.caption = caption;
            this.defaultType = defaultType;
            this.fileSize = fileSize;
            this.activeMode = activeMode;
            this.sessionUuid = sessionUuid;
        }
    }

    private final java.util.Map<String, PendingMediaTask> pendingMediaTasks = new java.util.concurrent.ConcurrentHashMap<>();

    private void retryMediaMessage(ChatMessage msg) {
        if (msg == null) return;
        PendingMediaTask task = pendingMediaTasks.get(msg.getId());
        if (task != null) {
            task.optimisticMsg.setStatus(ChatMessage.STATUS_PENDING);
            adapter.updateMessageStatus(msg.getId(), ChatMessage.STATUS_PENDING);
            uploadAndSendMedia(task);
        } else if (msg.getMediaUrl() != null && !msg.getMediaUrl().isEmpty()) {
            Uri uri = Uri.parse(msg.getMediaUrl());
            String dispName = msg.getFileName() != null ? msg.getFileName() : "attachment";
            String mime = getContentResolver().getType(uri);
            String cap = msg.getMessageText();
            String type = msg.getMediaType() != null ? msg.getMediaType() : "image";
            long sz = msg.getFileSize() != null ? msg.getFileSize() : 0;
            String mode = msg.getMode() != null ? msg.getMode() : ChatMessage.MODE_NORMAL;
            String sess = msg.getSecretSessionId();

            PendingMediaTask newTask = new PendingMediaTask(msg, uri, dispName, mime, cap, type, sz, mode, sess);
            pendingMediaTasks.put(msg.getId(), newTask);
            msg.setStatus(ChatMessage.STATUS_PENDING);
            adapter.updateMessageStatus(msg.getId(), ChatMessage.STATUS_PENDING);
            uploadAndSendMedia(newTask);
        }
    }

    private void uploadAndSendMedia(PendingMediaTask task) {
        ChatApiClient.uploadChatMedia(this, task.uri, task.displayName, task.mimeType, new ChatApiClient.ChatCallback<ChatApiClient.MediaUploadResult>() {
            @Override
            public void onSuccess(ChatApiClient.MediaUploadResult result) {
                ChatApiClient.sendMessage(
                        ChatActivity.this,
                        currentUserId,
                        otherUserId,
                        task.caption,
                        result.url,
                        result.mediaType != null ? result.mediaType : task.defaultType,
                        result.fileName != null ? result.fileName : task.displayName,
                        result.fileSize > 0 ? result.fileSize : task.fileSize,
                        task.activeMode,
                        task.sessionUuid,
                        new ChatApiClient.ChatCallback<ChatMessage>() {
                            @Override
                            public void onSuccess(ChatMessage sentMessage) {
                                pendingMediaTasks.remove(task.optimisticMsg.getId());
                                if (sentMessage != null) {
                                    String serverId = sentMessage.getId();
                                    String st = sentMessage.getStatus() != null ? sentMessage.getStatus() : ChatMessage.STATUS_SENT;
                                    if (serverId != null) {
                                        loadedMessageIds.add(serverId);
                                    }
                                    sentMessage.setLocalUri(task.uri.toString());
                                    task.optimisticMsg.setLocalUri(task.uri.toString());
                                    task.optimisticMsg.setMediaUrl(sentMessage.getMediaUrl());
                                    adapter.updateTempMessage(task.optimisticMsg.getId(), sentMessage);
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
                            }

                            @Override
                            public void onError(String errorMessage) {
                                task.optimisticMsg.setStatus(ChatMessage.STATUS_FAILED);
                                adapter.updateMessageStatus(task.optimisticMsg.getId(), ChatMessage.STATUS_FAILED);
                            }
                        }
                );
            }

            @Override
            public void onError(String errorMessage) {
                task.optimisticMsg.setStatus(ChatMessage.STATUS_FAILED);
                adapter.updateMessageStatus(task.optimisticMsg.getId(), ChatMessage.STATUS_FAILED);
            }
        });
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

        // Copy selected media to private internal storage to ensure permanent readability without permission timeouts
        Uri stableUri = uri;
        try {
            File mediaDir = new File(getFilesDir(), "chat_media");
            if (!mediaDir.exists()) mediaDir.mkdirs();
            String safeExt = "";
            if (displayName != null && displayName.contains(".")) {
                safeExt = displayName.substring(displayName.lastIndexOf("."));
            } else if ("image".equals(defaultType)) {
                safeExt = ".jpg";
            } else if ("video".equals(defaultType)) {
                safeExt = ".mp4";
            }
            File localMediaFile = new File(mediaDir, "media_" + System.currentTimeMillis() + safeExt);
            try (InputStream in = getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(localMediaFile)) {
                if (in != null) {
                    byte[] buf = new byte[8192];
                    int r;
                    while ((r = in.read(buf)) != -1) {
                        out.write(buf, 0, r);
                    }
                    out.flush();
                    stableUri = Uri.fromFile(localMediaFile);
                    fileSize = localMediaFile.length();
                }
            }
        } catch (Exception ignored) {}

        String caption = etInput.getText() != null ? etInput.getText().toString().trim() : "";
        etInput.setText("");

        if (replyingToMessage != null) {
            boolean isSelf = currentUserId != null && currentUserId.equals(replyingToMessage.getSenderId());
            String senderName = isSelf ? "You" : (otherUserName != null ? otherUserName : "Contact");
            String snippet = replyingToMessage.getDisplaySnippet();
            String replyMedia = (replyingToMessage.getMediaUrl() != null && !replyingToMessage.getMediaUrl().trim().isEmpty())
                    ? replyingToMessage.getMediaUrl()
                    : replyingToMessage.getLocalUri();
            caption = ChatMessage.formatReply(senderName, snippet, caption, replyMedia);
            cancelReply();
        }

        final String activeMode = isSecretMode ? ChatMessage.MODE_SECRET : ChatMessage.MODE_NORMAL;
        final String sessionUuid = isSecretMode ? currentSecretSessionId : null;

        String tempId = "temp_media_" + System.currentTimeMillis();
        ChatMessage optimisticMsg = new ChatMessage(
                tempId,
                conversationId,
                currentUserId,
                otherUserId,
                caption,
                stableUri.toString(),
                ChatMessage.STATUS_PENDING,
                isoFormat.format(new Date())
        );
        optimisticMsg.setLocalUri(stableUri.toString());
        optimisticMsg.setMediaType(defaultType);
        optimisticMsg.setFileName(displayName);
        optimisticMsg.setFileSize(fileSize);
        optimisticMsg.setMode(activeMode);
        optimisticMsg.setSecretSessionId(sessionUuid);

        if (isSecretMode) {
            ephemeralSecretMessages.add(optimisticMsg);
        } else {
            normalMessages.add(optimisticMsg);
        }
        adapter.addMessage(optimisticMsg);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

        PendingMediaTask task = new PendingMediaTask(optimisticMsg, stableUri, displayName, mimeType, caption, defaultType, fileSize, activeMode, sessionUuid);
        pendingMediaTasks.put(tempId, task);

        uploadAndSendMedia(task);
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

                        if (result.incomingCall != null) {
                            handleIncomingCall(result.incomingCall);
                        }

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
        int accentColor = com.zez.app.util.PreferencesHelper.getAccentColorInt(this);
        if (btnSendContainer != null) {
            androidx.core.view.ViewCompat.setBackgroundTintList(btnSendContainer, android.content.res.ColorStateList.valueOf(accentColor));
        }
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
        if (isSearchMode) {
            closeChatSearch();
            return;
        }
        if (adapter != null && adapter.isSelectionMode()) {
            adapter.clearSelection();
            return;
        }
        super.onBackPressed();
    }

    static class ProfileMediaAdapter extends RecyclerView.Adapter<ProfileMediaAdapter.ViewHolder> {
        private final List<ChatMessage> mediaItems;
        private final Context context;

        ProfileMediaAdapter(Context context, List<ChatMessage> mediaItems) {
            this.context = context;
            this.mediaItems = mediaItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_media_thumb, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatMessage msg = mediaItems.get(position);
            String mediaUrl = msg.getMediaUrl();
            String fullUrl = ChatApiClient.getAbsoluteMediaUrl(context, mediaUrl);
            boolean isVideo = "video".equalsIgnoreCase(msg.getMediaType()) ||
                    (mediaUrl != null && (mediaUrl.toLowerCase().endsWith(".mp4") || mediaUrl.toLowerCase().endsWith(".mkv")));

            Glide.with(context)
                    .load(fullUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.bg_search_input)
                    .error(R.drawable.ic_photo_library)
                    .into(holder.ivThumb);

            if (holder.layoutVideoBadge != null) {
                holder.layoutVideoBadge.setVisibility(isVideo ? View.VISIBLE : View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(fullUrl), isVideo ? "video/*" : "image/*");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    try {
                        Intent web = new Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl));
                        web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(web);
                    } catch (Exception ignored) {}
                }
            });
        }

        @Override
        public int getItemCount() {
            return mediaItems.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumb;
            View layoutVideoBadge;
            TextView tvDuration;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivThumb = itemView.findViewById(R.id.iv_thumb_image);
                layoutVideoBadge = itemView.findViewById(R.id.layout_thumb_video_badge);
                tvDuration = itemView.findViewById(R.id.tv_thumb_video_duration);
            }
        }
    }

    private String activeIncomingCallId = null;

    private void handleIncomingCall(ChatApiClient.IncomingCallInfo call) {
        if (call == null || call.id == null || call.id.equals(activeIncomingCallId)) return;
        activeIncomingCallId = call.id;

        Intent intent = new Intent(this, IncomingCallActivity.class);
        intent.putExtra(IncomingCallActivity.EXTRA_CALL_ID, call.id);
        intent.putExtra(IncomingCallActivity.EXTRA_CALL_TYPE, call.callType);
        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_ID, call.callerId);
        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_NAME, call.callerName);
        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_AVATAR, call.callerAvatar);
        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_PHONE, call.callerPhone);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startCall(String callType) {
        if (otherUserId == null || otherUserId.isEmpty()) {
            Toast.makeText(this, "Cannot call: recipient not identified", Toast.LENGTH_SHORT).show();
            return;
        }
        if (otherUserId.equals(currentUserId)) {
            Toast.makeText(this, "Cannot call yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Starting " + callType + " call...", Toast.LENGTH_SHORT).show();

        String url = ChatApiClient.getBaseUrl(this) + "/api/call/initiate";
        JsonObject json = new JsonObject();
        json.addProperty("callerId", currentUserId);
        json.addProperty("calleeId", otherUserId);
        json.addProperty("callType", callType);

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request req = new Request.Builder().url(url).post(body).build();

        new OkHttpClient().newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Call failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Call server error: " + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }
                try {
                    String str = response.body().string();
                    JsonObject respJson = JsonParser.parseString(str).getAsJsonObject();
                    if (respJson.has("success") && respJson.get("success").getAsBoolean() && respJson.has("call")) {
                        JsonObject cObj = respJson.getAsJsonObject("call");
                        String callId = cObj.get("id").getAsString();
                        runOnUiThread(() -> {
                            Intent intent = new Intent(ChatActivity.this, CallActivity.class);
                            intent.putExtra(CallActivity.EXTRA_CALL_ID, callId);
                            intent.putExtra(CallActivity.EXTRA_IS_CALLER, true);
                            intent.putExtra(CallActivity.EXTRA_CALL_TYPE, callType);
                            intent.putExtra(CallActivity.EXTRA_PEER_ID, otherUserId);
                            intent.putExtra(CallActivity.EXTRA_PEER_NAME, otherUserName);
                            intent.putExtra(CallActivity.EXTRA_PEER_AVATAR, otherUserAvatar);
                            intent.putExtra(CallActivity.EXTRA_PEER_PHONE, otherUserPhone);
                            startActivity(intent);
                        });
                    } else {
                        String err = respJson.has("error") ? respJson.get("error").getAsString() : "User busy or unavailable";
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this, err, Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
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
