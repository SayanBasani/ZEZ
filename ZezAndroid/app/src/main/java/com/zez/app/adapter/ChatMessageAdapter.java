package com.zez.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zez.app.R;
import com.zez.app.model.ChatMessage;
import com.zez.app.util.ChatApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_INCOMING = 1;
    private static final int TYPE_OUTGOING = 2;

    public static class ChatRowItem {
        public boolean isHeader;
        public String headerDate;
        public ChatMessage message;

        public static ChatRowItem header(String date) {
            ChatRowItem item = new ChatRowItem();
            item.isHeader = true;
            item.headerDate = date;
            return item;
        }

        public static ChatRowItem message(ChatMessage msg) {
            ChatRowItem item = new ChatRowItem();
            item.isHeader = false;
            item.message = msg;
            return item;
        }
    }

    private final String currentUserId;
    private final List<ChatRowItem> rowItems = new ArrayList<>();
    private final java.util.Set<String> selectedMessageIds = new java.util.HashSet<>();
    private boolean isSelectionMode = false;
    private String highlightedMessageId = null;
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public interface OnMessageActionListener {
        void onSelectionChanged(int selectedCount);
        void onMessageLongClick(ChatMessage message, View anchorView);
    }

    public interface OnMediaRetryListener {
        void onMediaRetry(ChatMessage message);
    }

    public interface OnGameInviteClickListener {
        void onGameInviteClick(ChatMessage message);
    }

    public interface OnGameDeclineClickListener {
        void onGameDeclineClick(ChatMessage message);
    }

    private OnMessageActionListener actionListener;
    private OnMediaRetryListener retryListener;
    private OnGameInviteClickListener gameInviteClickListener;
    private OnGameDeclineClickListener gameDeclineClickListener;
    private static final java.util.Set<String> declinedGameInviteIds =
            java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    public void markGameInviteDeclined(String gameId) {
        if (gameId != null && !gameId.trim().isEmpty()) {
            declinedGameInviteIds.add(gameId.trim());
            notifyDataSetChanged();
        }
    }

    public void setOnMessageActionListener(OnMessageActionListener listener) {
        this.actionListener = listener;
    }

    public void setOnMediaRetryListener(OnMediaRetryListener retryListener) {
        this.retryListener = retryListener;
    }

    public void setOnGameInviteClickListener(OnGameInviteClickListener listener) {
        this.gameInviteClickListener = listener;
    }

    public void setOnGameDeclineClickListener(OnGameDeclineClickListener listener) {
        this.gameDeclineClickListener = listener;
    }

    public String findMediaUrlForQuote(String quotedSender, String quotedText) {
        if (quotedText != null && !quotedText.isEmpty()) {
            for (int i = rowItems.size() - 1; i >= 0; i--) {
                ChatRowItem item = rowItems.get(i);
                if (!item.isHeader && item.message != null) {
                    ChatMessage m = item.message;
                    String source = (m.getLocalUri() != null && !m.getLocalUri().trim().isEmpty())
                            ? m.getLocalUri().trim()
                            : m.getMediaUrl();
                    if (source != null && !source.trim().isEmpty()) {
                        String snip = m.getDisplaySnippet();
                        if (quotedText.equals(snip) || (m.getMessageText() != null && m.getMessageText().equals(quotedText))) {
                            return source;
                        }
                    }
                }
            }
        }
        for (int i = rowItems.size() - 1; i >= 0; i--) {
            ChatRowItem item = rowItems.get(i);
            if (!item.isHeader && item.message != null) {
                ChatMessage m = item.message;
                String source = (m.getLocalUri() != null && !m.getLocalUri().trim().isEmpty())
                        ? m.getLocalUri().trim()
                        : m.getMediaUrl();
                if (source != null && !source.trim().isEmpty()) {
                    return source;
                }
            }
        }
        return null;
    }

    public static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255.0;
        return darkness >= 0.5;
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void toggleSelection(ChatMessage msg) {
        if (msg == null || msg.getId() == null) return;
        if (selectedMessageIds.contains(msg.getId())) {
            selectedMessageIds.remove(msg.getId());
        } else {
            selectedMessageIds.add(msg.getId());
        }
        isSelectionMode = !selectedMessageIds.isEmpty();
        notifyDataSetChanged();
        if (actionListener != null) {
            actionListener.onSelectionChanged(selectedMessageIds.size());
        }
    }

    public void clearSelection() {
        selectedMessageIds.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
        if (actionListener != null) {
            actionListener.onSelectionChanged(0);
        }
    }

    public List<ChatMessage> getSelectedMessages() {
        List<ChatMessage> list = new ArrayList<>();
        for (ChatRowItem row : rowItems) {
            if (!row.isHeader && row.message != null && selectedMessageIds.contains(row.message.getId())) {
                list.add(row.message);
            }
        }
        return list;
    }

    public void setHighlightedMessageId(String messageId) {
        this.highlightedMessageId = messageId;
        notifyDataSetChanged();
    }

    public List<Integer> searchMessagePositions(String query) {
        List<Integer> matches = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return matches;
        String q = query.trim().toLowerCase(Locale.getDefault());
        for (int i = 0; i < rowItems.size(); i++) {
            ChatRowItem item = rowItems.get(i);
            if (!item.isHeader && item.message != null && item.message.getMessageText() != null) {
                if (item.message.getMessageText().toLowerCase(Locale.getDefault()).contains(q)) {
                    matches.add(i);
                }
            }
        }
        return matches;
    }

    public void updateMessageReaction(String messageId, String reaction) {
        if (messageId == null) return;
        for (int i = 0; i < rowItems.size(); i++) {
            ChatRowItem row = rowItems.get(i);
            if (!row.isHeader && row.message != null && messageId.equals(row.message.getId())) {
                if (java.util.Objects.equals(row.message.getReaction(), reaction)) {
                    return;
                }
                row.message.setReaction(reaction);
                notifyItemChanged(i, "reaction_payload");
                break;
            }
        }
    }

    public void markMessageDeleted(String messageId) {
        if (messageId == null) return;
        for (int i = 0; i < rowItems.size(); i++) {
            ChatRowItem row = rowItems.get(i);
            if (!row.isHeader && row.message != null && messageId.equals(row.message.getId())) {
                if (row.message.isDeletedForEveryone()) return;
                row.message.setDeletedForEveryone(true);
                row.message.setMessageText("🚫 This message was deleted");
                row.message.setMediaUrl(null);
                row.message.setReaction(null);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeMessages(List<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) return;
        for (String id : messageIds) {
            for (int i = rowItems.size() - 1; i >= 0; i--) {
                ChatRowItem row = rowItems.get(i);
                if (!row.isHeader && row.message != null && id.equals(row.message.getId())) {
                    rowItems.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }
    }

    public ChatMessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
        this.isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < rowItems.size()) {
            ChatRowItem item = rowItems.get(position);
            if (item != null) {
                if (item.isHeader) {
                    return ("header_" + item.headerDate).hashCode();
                } else if (item.message != null && item.message.getId() != null) {
                    return item.message.getId().hashCode();
                }
            }
        }
        return position;
    }

    public void setMessages(List<ChatMessage> messages) {
        rowItems.clear();
        if (messages != null && !messages.isEmpty()) {
            String lastDay = null;
            for (ChatMessage m : messages) {
                String day = getDayString(m.getCreatedAt());
                if (!day.equals(lastDay)) {
                    lastDay = day;
                    rowItems.add(ChatRowItem.header(day));
                }
                rowItems.add(ChatRowItem.message(m));
            }
        }
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage message) {
        if (message == null) return;
        int startIndex = rowItems.size();
        String day = getDayString(message.getCreatedAt());
        boolean needHeader = rowItems.isEmpty() || !day.equals(getDayString(rowItems.get(rowItems.size() - 1).message != null ? rowItems.get(rowItems.size() - 1).message.getCreatedAt() : null));
        if (needHeader) {
            rowItems.add(ChatRowItem.header(day));
        }
        rowItems.add(ChatRowItem.message(message));
        int addedCount = rowItems.size() - startIndex;
        notifyItemRangeInserted(startIndex, addedCount);
    }

    public ChatMessage getMessageAt(int position) {
        if (position >= 0 && position < rowItems.size()) {
            ChatRowItem row = rowItems.get(position);
            if (!row.isHeader) {
                return row.message;
            }
        }
        return null;
    }

    public void updateMessageStatus(String messageId, String newStatus) {
        if (messageId == null || newStatus == null) return;
        for (int i = 0; i < rowItems.size(); i++) {
            ChatRowItem row = rowItems.get(i);
            if (!row.isHeader && row.message != null && messageId.equals(row.message.getId())) {
                if (newStatus.equalsIgnoreCase(row.message.getStatus())) {
                    return;
                }
                row.message.setStatus(newStatus);
                notifyItemChanged(i, "status_payload");
                break;
            }
        }
    }

    public void updateTempMessage(String tempId, String realId, String newStatus) {
        for (int i = 0; i < rowItems.size(); i++) {
            ChatRowItem row = rowItems.get(i);
            if (!row.isHeader && row.message != null) {
                if ((tempId != null && tempId.equals(row.message.getId())) || (realId != null && realId.equals(row.message.getId()))) {
                    if (realId != null) row.message.setId(realId);
                    if (newStatus != null) row.message.setStatus(newStatus);
                    notifyItemChanged(i);
                    return;
                }
            }
        }
    }

    public void updateTempMessage(String tempId, ChatMessage realMsg) {
        if (realMsg == null) return;
        String realId = realMsg.getId();
        String newStatus = realMsg.getStatus();
        for (int i = 0; i < rowItems.size(); i++) {
            ChatRowItem row = rowItems.get(i);
            if (!row.isHeader && row.message != null) {
                if ((tempId != null && tempId.equals(row.message.getId())) || (realId != null && realId.equals(row.message.getId()))) {
                    if (realId != null) row.message.setId(realId);
                    if (newStatus != null) row.message.setStatus(newStatus);
                    if (realMsg.getMediaUrl() != null) row.message.setMediaUrl(realMsg.getMediaUrl());
                    if (realMsg.getLocalUri() != null) row.message.setLocalUri(realMsg.getLocalUri());
                    if (realMsg.getMediaType() != null) row.message.setMediaType(realMsg.getMediaType());
                    if (realMsg.getFileName() != null) row.message.setFileName(realMsg.getFileName());
                    if (realMsg.getFileSize() != null && realMsg.getFileSize() > 0) row.message.setFileSize(realMsg.getFileSize());
                    notifyItemChanged(i);
                    return;
                }
            }
        }
    }

    private String getDayString(String rawTime) {
        if (rawTime == null || rawTime.isEmpty()) return "Today";
        try {
            String clean = rawTime.split("\\.")[0];
            Date d = isoFormat.parse(clean);
            if (d == null) return "Today";

            long now = System.currentTimeMillis();
            long diff = now - d.getTime();
            if (diff < 24 * 60 * 60 * 1000) return "Today";
            if (diff < 48 * 60 * 60 * 1000) return "Yesterday";
            return dayFormat.format(d);
        } catch (Exception e) {
            return "Today";
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatRowItem item = rowItems.get(position);
        if (item.isHeader) return TYPE_DATE_HEADER;
        if (item.message != null && currentUserId != null && currentUserId.equals(item.message.getSenderId())) {
            return TYPE_OUTGOING;
        }
        return TYPE_INCOMING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_DATE_HEADER) {
            View view = inflater.inflate(R.layout.item_chat_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else if (viewType == TYPE_OUTGOING) {
            View view = inflater.inflate(R.layout.item_message_outgoing, parent, false);
            return new OutgoingViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_incoming, parent, false);
            return new IncomingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        onBindViewHolder(holder, position, java.util.Collections.emptyList());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        ChatRowItem item = rowItems.get(position);
        if (item == null) return;

        if (!payloads.isEmpty() && item.message != null) {
            if (payloads.contains("status_payload")) {
                if (holder instanceof OutgoingViewHolder) {
                    ((OutgoingViewHolder) holder).bindStatusOnly(item.message);
                }
                return;
            }
            if (payloads.contains("reaction_payload")) {
                if (holder instanceof OutgoingViewHolder) {
                    ((OutgoingViewHolder) holder).bindReactionOnly(item.message);
                    return;
                } else if (holder instanceof IncomingViewHolder) {
                    ((IncomingViewHolder) holder).bindReactionOnly(item.message);
                    return;
                }
            }
        }

        if (holder instanceof DateHeaderViewHolder) {
            ((DateHeaderViewHolder) holder).tvHeader.setText(item.headerDate);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.itemView.setOnLongClickListener(null);
            holder.itemView.setOnClickListener(null);
        } else if (holder instanceof OutgoingViewHolder) {
            OutgoingViewHolder vh = (OutgoingViewHolder) holder;
            vh.bind(item.message, this, isoFormat, timeFormat);
            bindSelectionAndClicks(holder, item);
        } else if (holder instanceof IncomingViewHolder) {
            IncomingViewHolder vh = (IncomingViewHolder) holder;
            vh.bind(item.message, this, isoFormat, timeFormat);
            bindSelectionAndClicks(holder, item);
        }
    }

    private void bindSelectionAndClicks(RecyclerView.ViewHolder holder, ChatRowItem item) {
        if (item == null || item.message == null) return;
        ChatMessage msg = item.message;

        if (selectedMessageIds.contains(msg.getId())) {
            holder.itemView.setBackgroundColor(com.zez.app.theme.ThemeManager.getSelectionHighlightColor(holder.itemView.getContext()));
        } else if (highlightedMessageId != null && highlightedMessageId.equals(msg.getId())) {
            holder.itemView.setBackgroundColor(Color.parseColor("#44F59E0B"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (msg.isDeletedForEveryone()) return false;
            v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
            if (!selectedMessageIds.contains(msg.getId())) {
                toggleSelection(msg);
            }
            if (actionListener != null) {
                actionListener.onMessageLongClick(msg, holder.itemView);
            }
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(msg);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rowItems.size();
    }

    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tv_chat_date_header);
        }
    }

    static class OutgoingViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        TextView tvTime;
        ImageView ivTick;
        FrameLayout layoutMedia;
        ImageView ivImage;
        ImageView ivPlay;
        FrameLayout layoutMediaProgress;
        ProgressBar pbMediaProgress;
        FrameLayout layoutMediaRetry;
        ImageView btnMediaRetry;
        LinearLayout layoutFile;
        TextView tvFileName;
        TextView tvFileSize;
        LinearLayout layoutQuote;
        TextView tvQuoteSender;
        TextView tvQuoteText;
        ImageView ivQuoteImage;
        LinearLayout layoutReaction;
        TextView tvReaction;
        LinearLayout layoutGameCard;
        TextView tvGameTitle;
        TextView tvGameSubtitle;
        TextView tvGameCta;
        View layoutBubble;

        OutgoingViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutBubble = itemView.findViewById(R.id.layout_bubble_outgoing);
            tvText = itemView.findViewById(R.id.tv_msg_outgoing_text);
            tvTime = itemView.findViewById(R.id.tv_msg_outgoing_time);
            ivTick = itemView.findViewById(R.id.iv_msg_status_tick);
            layoutMedia = itemView.findViewById(R.id.layout_msg_outgoing_media);
            ivImage = itemView.findViewById(R.id.iv_msg_outgoing_image);
            ivPlay = itemView.findViewById(R.id.iv_msg_outgoing_play);
            layoutMediaProgress = itemView.findViewById(R.id.layout_msg_outgoing_media_progress);
            pbMediaProgress = itemView.findViewById(R.id.pb_msg_outgoing_media_progress);
            layoutMediaRetry = itemView.findViewById(R.id.layout_msg_outgoing_media_retry);
            btnMediaRetry = itemView.findViewById(R.id.btn_msg_outgoing_media_retry);
            layoutFile = itemView.findViewById(R.id.layout_msg_outgoing_file);
            tvFileName = itemView.findViewById(R.id.tv_msg_outgoing_filename);
            tvFileSize = itemView.findViewById(R.id.tv_msg_outgoing_filesize);
            layoutQuote = itemView.findViewById(R.id.layout_msg_outgoing_quote);
            tvQuoteSender = itemView.findViewById(R.id.tv_msg_outgoing_quote_sender);
            tvQuoteText = itemView.findViewById(R.id.tv_msg_outgoing_quote_text);
            ivQuoteImage = itemView.findViewById(R.id.iv_msg_outgoing_quote_image);
            layoutReaction = itemView.findViewById(R.id.layout_msg_outgoing_reaction);
            tvReaction = itemView.findViewById(R.id.tv_msg_outgoing_reaction);
            layoutGameCard = itemView.findViewById(R.id.layout_msg_outgoing_game_card);
            tvGameTitle = itemView.findViewById(R.id.tv_msg_outgoing_game_title);
            tvGameSubtitle = itemView.findViewById(R.id.tv_msg_outgoing_game_subtitle);
            tvGameCta = itemView.findViewById(R.id.tv_msg_outgoing_game_cta);
        }

        void bindStatusOnly(ChatMessage msg) {
            if (ivTick == null || msg == null) return;
            if (msg.isDeletedForEveryone()) {
                ivTick.setVisibility(View.GONE);
                return;
            }
            ivTick.setVisibility(View.VISIBLE);
            String status = msg.getStatus();
            if (ChatMessage.STATUS_READ.equalsIgnoreCase(status)) {
                ivTick.setImageResource(R.drawable.ic_check_double);
                ivTick.setColorFilter(Color.parseColor("#34B7F1"));
            } else if (ChatMessage.STATUS_DELIVERED.equalsIgnoreCase(status)) {
                ivTick.setImageResource(R.drawable.ic_check_double);
                ivTick.setColorFilter(Color.parseColor("#94A3B8"));
            } else if (ChatMessage.STATUS_PENDING.equalsIgnoreCase(status)) {
                ivTick.setImageResource(R.drawable.ic_clock);
                ivTick.setColorFilter(Color.parseColor("#94A3B8"));
            } else if (ChatMessage.STATUS_FAILED.equalsIgnoreCase(status)) {
                ivTick.setImageResource(R.drawable.ic_refresh);
                ivTick.setColorFilter(Color.parseColor("#EF4444"));
            } else {
                ivTick.setImageResource(R.drawable.ic_check_single);
                ivTick.setColorFilter(Color.parseColor("#94A3B8"));
            }
        }

        void bindReactionOnly(ChatMessage msg) {
            if (layoutReaction == null || tvReaction == null || msg == null) return;
            String react = msg.getReaction();
            if (react != null && !react.trim().isEmpty() && !msg.isDeletedForEveryone()) {
                layoutReaction.setVisibility(View.VISIBLE);
                tvReaction.setText(react.trim());
            } else {
                layoutReaction.setVisibility(View.GONE);
            }
        }

        void bind(ChatMessage msg, ChatMessageAdapter adapter, SimpleDateFormat isoFmt, SimpleDateFormat timeFmt) {
            if (layoutBubble != null && layoutBubble.getBackground() != null) {
                int accentColor = com.zez.app.util.PreferencesHelper.getAccentColorInt(itemView.getContext());
                layoutBubble.getBackground().mutate().setTint(accentColor);
            }

            if (msg.isDeletedForEveryone()) {
                tvText.setVisibility(View.VISIBLE);
                tvText.setText("🚫 This message was deleted");
                tvText.setTypeface(null, android.graphics.Typeface.ITALIC);
                tvText.setTextColor(Color.parseColor("#94A3B8"));
                if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
                if (layoutFile != null) layoutFile.setVisibility(View.GONE);
                if (layoutQuote != null) layoutQuote.setVisibility(View.GONE);
                if (layoutReaction != null) layoutReaction.setVisibility(View.GONE);
                if (layoutGameCard != null) layoutGameCard.setVisibility(View.GONE);
                if (ivTick != null) ivTick.setVisibility(View.GONE);
                tvTime.setText(formatTime(msg.getCreatedAt(), isoFmt, timeFmt));
                return;
            }

            tvText.setTypeface(null, android.graphics.Typeface.NORMAL);
            tvText.setTextColor(Color.WHITE);
            if (ivTick != null) ivTick.setVisibility(View.VISIBLE);

            if (layoutQuote != null) {
                if (msg.hasQuotedReply()) {
                    layoutQuote.setVisibility(View.VISIBLE);
                    if (tvQuoteSender != null) tvQuoteSender.setText(msg.getQuotedSender());
                    if (tvQuoteText != null) tvQuoteText.setText(com.zez.app.util.MarkdownRenderer.render(msg.getQuotedText()));
                    if (ivQuoteImage != null) {
                        String qMediaUrl = msg.getQuotedMediaUrl();
                        String qText = msg.getQuotedText();
                        boolean isMedia = (qMediaUrl != null && !qMediaUrl.isEmpty()) ||
                                (qText != null && (qText.contains("Photo") || qText.contains("📷") || qText.contains("Video") || qText.contains("🎥")));
                        if (isMedia) {
                            ivQuoteImage.setVisibility(View.VISIBLE);
                            if (qMediaUrl == null || qMediaUrl.isEmpty()) {
                                qMediaUrl = adapter.findMediaUrlForQuote(msg.getQuotedSender(), qText);
                            }
                            if (qMediaUrl != null && !qMediaUrl.isEmpty()) {
                                Object quoteTarget = (qMediaUrl.startsWith("content://") || qMediaUrl.startsWith("file://"))
                                        ? Uri.parse(qMediaUrl)
                                        : ChatApiClient.getAbsoluteMediaUrl(itemView.getContext(), qMediaUrl);
                                ivQuoteImage.clearColorFilter();
                                Glide.with(itemView.getContext())
                                        .load(quoteTarget)
                                        .centerCrop()
                                        .placeholder(R.drawable.ic_photo_library)
                                        .error(R.drawable.ic_photo_library)
                                        .into(ivQuoteImage);
                            } else {
                                ivQuoteImage.setImageResource(R.drawable.ic_photo_library);
                                ivQuoteImage.setColorFilter(Color.parseColor("#A7F3D0"));
                            }
                        } else {
                            ivQuoteImage.setVisibility(View.GONE);
                        }
                    }
                } else {
                    layoutQuote.setVisibility(View.GONE);
                }
            }

            bindMedia(itemView.getContext(), msg, layoutMedia, ivImage, ivPlay, layoutFile, tvFileName, tvFileSize);

            // WhatsApp-style media upload progress & retry overlays
            if (ChatMessage.STATUS_PENDING.equalsIgnoreCase(msg.getStatus())) {
                if (layoutMediaProgress != null) layoutMediaProgress.setVisibility(View.VISIBLE);
                if (layoutMediaRetry != null) layoutMediaRetry.setVisibility(View.GONE);
            } else if (ChatMessage.STATUS_FAILED.equalsIgnoreCase(msg.getStatus())) {
                if (layoutMediaProgress != null) layoutMediaProgress.setVisibility(View.GONE);
                if (layoutMediaRetry != null) layoutMediaRetry.setVisibility(View.VISIBLE);
                if (btnMediaRetry != null) {
                    btnMediaRetry.setOnClickListener(v -> {
                        if (adapter.retryListener != null) adapter.retryListener.onMediaRetry(msg);
                    });
                }
            } else {
                if (layoutMediaProgress != null) layoutMediaProgress.setVisibility(View.GONE);
                if (layoutMediaRetry != null) layoutMediaRetry.setVisibility(View.GONE);
            }

            tvText.setOnClickListener(null);
            if (msg.isGameInvite() && layoutGameCard != null) {
                layoutGameCard.setVisibility(View.VISIBLE);
                tvText.setVisibility(View.GONE);
                if (msg.isSnakesLaddersInvite()) {
                    if (tvGameTitle != null) tvGameTitle.setText("Snakes & Ladders Challenge");
                    long exp = msg.getGameExpiresAtMillis();
                    boolean expired = exp > 0 && System.currentTimeMillis() > exp;
                    boolean declined = declinedGameInviteIds.contains(msg.getGameId());
                    if (tvGameSubtitle != null) {
                        tvGameSubtitle.setText(declined ? "Challenge declined" : (expired ? "Challenge expired" : "Invited by: " + msg.getGameHostName()));
                    }
                    if (tvGameCta != null) tvGameCta.setText(declined ? "Declined" : (expired ? "Expired" : "Open"));
                } else {
                    if (tvGameTitle != null) tvGameTitle.setText("Tic Tac Toe");
                    if (tvGameSubtitle != null) tvGameSubtitle.setText("Tap Open to play together");
                    if (tvGameCta != null) tvGameCta.setText("Open");
                }
                View.OnClickListener openGame = v -> {
                    if (adapter.gameInviteClickListener != null) {
                        adapter.gameInviteClickListener.onGameInviteClick(msg);
                    }
                };
                layoutGameCard.setOnClickListener(openGame);
                if (tvGameCta != null) tvGameCta.setOnClickListener(openGame);
            } else {
                if (layoutGameCard != null) layoutGameCard.setVisibility(View.GONE);
                if (msg.isGameSummary()) {
                    tvText.setVisibility(View.VISIBLE);
                    tvText.setText("\uD83C\uDFAE " + msg.getGameSummaryText());
                } else if (msg.isContactCard()) {
                    String cName = msg.getSharedContactName();
                    String cPhone = msg.getSharedContactPhone();
                    tvText.setVisibility(View.VISIBLE);
                    tvText.setText(com.zez.app.util.MarkdownRenderer.render("\uD83D\uDC64 **" + cName + "**\n" + cPhone));
                    if (cPhone != null && !cPhone.trim().isEmpty()) {
                        tvText.setOnClickListener(v -> {
                            try {
                                Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + cPhone.trim()));
                                itemView.getContext().startActivity(dial);
                            } catch (Exception ignored) {}
                        });
                    }
                } else {
                    String text = msg.getCleanMessageText();
                    if (text != null && !text.isEmpty() && !text.equals("📷 Photo") && !text.equals("🎥 Video") && !text.equals("🎵 Audio") && !text.startsWith("📄 ") && !text.equals("📎 Attachment")) {
                        tvText.setVisibility(View.VISIBLE);
                        tvText.setText(com.zez.app.util.MarkdownRenderer.render(text));
                    } else if (msg.getMediaUrl() == null || msg.getMediaUrl().isEmpty()) {
                        tvText.setVisibility(View.VISIBLE);
                        tvText.setText(text != null ? com.zez.app.util.MarkdownRenderer.render(text) : "");
                    } else {
                        tvText.setVisibility(View.GONE);
                    }
                }
            }

            tvTime.setText(formatTime(msg.getCreatedAt(), isoFmt, timeFmt));
            bindReactionOnly(msg);
            bindStatusOnly(msg);
        }
    }

    static class IncomingViewHolder extends RecyclerView.ViewHolder {
        View layoutBubble;
        TextView tvText;
        TextView tvTime;
        FrameLayout layoutMedia;
        ImageView ivImage;
        ImageView ivPlay;
        LinearLayout layoutFile;
        TextView tvFileName;
        TextView tvFileSize;
        LinearLayout layoutQuote;
        TextView tvQuoteSender;
        TextView tvQuoteText;
        ImageView ivQuoteImage;
        LinearLayout layoutReaction;
        TextView tvReaction;
        LinearLayout layoutGameCard;
        TextView tvGameTitle;
        TextView tvGameSubtitle;
        TextView tvGameDecline;
        TextView tvGameCta;

        IncomingViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutBubble = itemView.findViewById(R.id.layout_msg_incoming_bubble);
            tvText = itemView.findViewById(R.id.tv_msg_incoming_text);
            tvTime = itemView.findViewById(R.id.tv_msg_incoming_time);
            layoutMedia = itemView.findViewById(R.id.layout_msg_incoming_media);
            ivImage = itemView.findViewById(R.id.iv_msg_incoming_image);
            ivPlay = itemView.findViewById(R.id.iv_msg_incoming_play);
            layoutFile = itemView.findViewById(R.id.layout_msg_incoming_file);
            tvFileName = itemView.findViewById(R.id.tv_msg_incoming_filename);
            tvFileSize = itemView.findViewById(R.id.tv_msg_incoming_filesize);
            layoutQuote = itemView.findViewById(R.id.layout_msg_incoming_quote);
            tvQuoteSender = itemView.findViewById(R.id.tv_msg_incoming_quote_sender);
            tvQuoteText = itemView.findViewById(R.id.tv_msg_incoming_quote_text);
            ivQuoteImage = itemView.findViewById(R.id.iv_msg_incoming_quote_image);
            layoutReaction = itemView.findViewById(R.id.layout_msg_incoming_reaction);
            tvReaction = itemView.findViewById(R.id.tv_msg_incoming_reaction);
            layoutGameCard = itemView.findViewById(R.id.layout_msg_incoming_game_card);
            tvGameTitle = itemView.findViewById(R.id.tv_msg_incoming_game_title);
            tvGameSubtitle = itemView.findViewById(R.id.tv_msg_incoming_game_subtitle);
            tvGameDecline = itemView.findViewById(R.id.tv_msg_incoming_game_decline);
            tvGameCta = itemView.findViewById(R.id.tv_msg_incoming_game_cta);
        }

        void bindReactionOnly(ChatMessage msg) {
            if (layoutReaction == null || tvReaction == null || msg == null) return;
            String react = msg.getReaction();
            if (react != null && !react.trim().isEmpty() && !msg.isDeletedForEveryone()) {
                layoutReaction.setVisibility(View.VISIBLE);
                tvReaction.setText(react.trim());
            } else {
                layoutReaction.setVisibility(View.GONE);
            }
        }

        void bind(ChatMessage msg, ChatMessageAdapter adapter, SimpleDateFormat isoFmt, SimpleDateFormat timeFmt) {
            Context ctx = itemView.getContext();
            int surfaceColor = ContextCompat.getColor(ctx, R.color.theme_surface);
            int themeMode = com.zez.app.util.PreferencesHelper.getThemeMode(ctx);
            boolean isEffectiveDark;
            if (themeMode == com.zez.app.util.PreferencesHelper.THEME_LIGHT) {
                isEffectiveDark = false;
            } else if (themeMode == com.zez.app.util.PreferencesHelper.THEME_DARK) {
                isEffectiveDark = true;
            } else {
                isEffectiveDark = isColorDark(surfaceColor);
            }

            String customIncoming = com.zez.app.util.PreferencesHelper.getIncomingBubbleColor(ctx);
            boolean bubbleIsDark = isEffectiveDark;

            if (customIncoming != null && !customIncoming.trim().isEmpty() && layoutBubble != null && layoutBubble.getBackground() != null) {
                try {
                    int bubbleColor = Color.parseColor(customIncoming);
                    layoutBubble.getBackground().mutate().setTint(bubbleColor);
                    bubbleIsDark = isColorDark(bubbleColor);
                } catch (Exception e) {
                    layoutBubble.getBackground().mutate().setTintList(null);
                    bubbleIsDark = isEffectiveDark;
                }
            } else {
                if (layoutBubble != null && layoutBubble.getBackground() != null) {
                    layoutBubble.getBackground().mutate().setTintList(null);
                }
                bubbleIsDark = isEffectiveDark;
            }

            if (msg.isDeletedForEveryone()) {
                tvText.setVisibility(View.VISIBLE);
                tvText.setText("🚫 This message was deleted");
                tvText.setTypeface(null, android.graphics.Typeface.ITALIC);
                tvText.setTextColor(bubbleIsDark ? Color.parseColor("#94A3B8") : Color.parseColor("#475569"));
                if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
                if (layoutFile != null) layoutFile.setVisibility(View.GONE);
                if (layoutQuote != null) layoutQuote.setVisibility(View.GONE);
                if (layoutReaction != null) layoutReaction.setVisibility(View.GONE);
                if (layoutGameCard != null) layoutGameCard.setVisibility(View.GONE);
                tvTime.setText(formatTime(msg.getCreatedAt(), isoFmt, timeFmt));
                return;
            }

            tvText.setTypeface(null, android.graphics.Typeface.NORMAL);

            // Always enforce high contrast: Crisp dark charcoal on light bubble, pure white on dark bubble
            tvText.setTextColor(bubbleIsDark ? Color.parseColor("#F8FAFC") : Color.parseColor("#0F172A"));
            tvTime.setTextColor(bubbleIsDark ? Color.parseColor("#94A3B8") : Color.parseColor("#64748B"));

            if (layoutQuote != null) {
                if (msg.hasQuotedReply()) {
                    layoutQuote.setVisibility(View.VISIBLE);
                    if (tvQuoteSender != null) {
                        tvQuoteSender.setText(msg.getQuotedSender());
                        tvQuoteSender.setTextColor(bubbleIsDark ? Color.parseColor("#4ADE80") : Color.parseColor("#16A34A"));
                    }
                    if (tvQuoteText != null) {
                        tvQuoteText.setText(com.zez.app.util.MarkdownRenderer.render(msg.getQuotedText()));
                        tvQuoteText.setTextColor(bubbleIsDark ? Color.parseColor("#CBD5E1") : Color.parseColor("#334155"));
                    }
                    if (ivQuoteImage != null) {
                        String qMediaUrl = msg.getQuotedMediaUrl();
                        String qText = msg.getQuotedText();
                        boolean isMedia = (qMediaUrl != null && !qMediaUrl.isEmpty()) ||
                                (qText != null && (qText.contains("Photo") || qText.contains("📷") || qText.contains("Video") || qText.contains("🎥")));
                        if (isMedia) {
                            ivQuoteImage.setVisibility(View.VISIBLE);
                            if (qMediaUrl == null || qMediaUrl.isEmpty()) {
                                qMediaUrl = adapter.findMediaUrlForQuote(msg.getQuotedSender(), qText);
                            }
                            if (qMediaUrl != null && !qMediaUrl.isEmpty()) {
                                Object quoteTarget = (qMediaUrl.startsWith("content://") || qMediaUrl.startsWith("file://"))
                                        ? Uri.parse(qMediaUrl)
                                        : ChatApiClient.getAbsoluteMediaUrl(itemView.getContext(), qMediaUrl);
                                ivQuoteImage.clearColorFilter();
                                Glide.with(itemView.getContext())
                                        .load(quoteTarget)
                                        .centerCrop()
                                        .placeholder(R.drawable.ic_photo_library)
                                        .error(R.drawable.ic_photo_library)
                                        .into(ivQuoteImage);
                            } else {
                                ivQuoteImage.setImageResource(R.drawable.ic_photo_library);
                                ivQuoteImage.setColorFilter(Color.parseColor("#22C55E"));
                            }
                        } else {
                            ivQuoteImage.setVisibility(View.GONE);
                        }
                    }
                } else {
                    layoutQuote.setVisibility(View.GONE);
                }
            }

            bindMedia(itemView.getContext(), msg, layoutMedia, ivImage, ivPlay, layoutFile, tvFileName, tvFileSize);

            tvText.setOnClickListener(null);
            if (msg.isGameInvite() && layoutGameCard != null) {
                layoutGameCard.setVisibility(View.VISIBLE);
                tvText.setVisibility(View.GONE);
                if (msg.isSnakesLaddersInvite()) {
                    if (tvGameTitle != null) tvGameTitle.setText("Snakes & Ladders Challenge");
                    long exp = msg.getGameExpiresAtMillis();
                    boolean expired = exp > 0 && System.currentTimeMillis() > exp;
                    boolean declined = declinedGameInviteIds.contains(msg.getGameId());
                    if (tvGameSubtitle != null) {
                        tvGameSubtitle.setText(declined ? "Invitation declined" : (expired ? "Invitation expired" : "Invited by: " + msg.getGameHostName()));
                    }
                    if (tvGameDecline != null) {
                        tvGameDecline.setVisibility((expired || declined) ? View.GONE : View.VISIBLE);
                        tvGameDecline.setOnClickListener(v -> {
                            if (adapter.gameDeclineClickListener != null) {
                                adapter.gameDeclineClickListener.onGameDeclineClick(msg);
                            }
                        });
                    }
                    if (tvGameCta != null) {
                        tvGameCta.setText(declined ? "Declined" : (expired ? "Expired" : "Accept Game"));
                    }
                } else {
                    if (tvGameTitle != null) tvGameTitle.setText("Tic Tac Toe");
                    if (tvGameSubtitle != null) tvGameSubtitle.setText(msg.getGameHostName() + " invited you to play!");
                    if (tvGameDecline != null) tvGameDecline.setVisibility(View.GONE);
                    if (tvGameCta != null) tvGameCta.setText("Join");
                }
                View.OnClickListener openGame = v -> {
                    if (adapter.gameInviteClickListener != null) {
                        adapter.gameInviteClickListener.onGameInviteClick(msg);
                    }
                };
                layoutGameCard.setOnClickListener(openGame);
                if (tvGameCta != null) tvGameCta.setOnClickListener(openGame);
            } else {
                if (layoutGameCard != null) layoutGameCard.setVisibility(View.GONE);
                if (msg.isGameSummary()) {
                    tvText.setVisibility(View.VISIBLE);
                    tvText.setText("\uD83C\uDFAE " + msg.getGameSummaryText());
                } else if (msg.isContactCard()) {
                    String cName = msg.getSharedContactName();
                    String cPhone = msg.getSharedContactPhone();
                    tvText.setVisibility(View.VISIBLE);
                    tvText.setText(com.zez.app.util.MarkdownRenderer.render("\uD83D\uDC64 **" + cName + "**\n" + cPhone));
                    if (cPhone != null && !cPhone.trim().isEmpty()) {
                        tvText.setOnClickListener(v -> {
                            try {
                                Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + cPhone.trim()));
                                itemView.getContext().startActivity(dial);
                            } catch (Exception ignored) {}
                        });
                    }
                } else {
                    String text = msg.getCleanMessageText();
                    if (text != null && !text.isEmpty() && !text.equals("📷 Photo") && !text.equals("🎥 Video") && !text.equals("🎵 Audio") && !text.startsWith("📄 ") && !text.equals("📎 Attachment")) {
                        tvText.setVisibility(View.VISIBLE);
                        tvText.setText(com.zez.app.util.MarkdownRenderer.render(text));
                    } else if (msg.getMediaUrl() == null || msg.getMediaUrl().isEmpty()) {
                        tvText.setVisibility(View.VISIBLE);
                        tvText.setText(text != null ? com.zez.app.util.MarkdownRenderer.render(text) : "");
                    } else {
                        tvText.setVisibility(View.GONE);
                    }
                }
            }

            tvTime.setText(formatTime(msg.getCreatedAt(), isoFmt, timeFmt));
            bindReactionOnly(msg);
        }
    }

    private static void bindMedia(Context context, ChatMessage msg, FrameLayout layoutMedia, ImageView ivImage, ImageView ivPlay, LinearLayout layoutFile, TextView tvFileName, TextView tvFileSize) {
        String localUri = msg.getLocalUri();
        String remoteUrl = msg.getMediaUrl();
        String remoteAbsolute = (remoteUrl != null && !remoteUrl.trim().isEmpty())
                ? ChatApiClient.getAbsoluteMediaUrl(context, remoteUrl)
                : null;

        Object primaryTarget = null;
        Object fallbackTarget = remoteAbsolute;

        // Verify if localUri is actually readable right now:
        if (localUri != null && !localUri.trim().isEmpty()) {
            Uri parsed = Uri.parse(localUri.trim());
            boolean canRead = false;
            try {
                if ("content".equalsIgnoreCase(parsed.getScheme())) {
                    try (java.io.InputStream is = context.getContentResolver().openInputStream(parsed)) {
                        canRead = (is != null);
                    }
                } else if ("file".equalsIgnoreCase(parsed.getScheme())) {
                    java.io.File f = new java.io.File(parsed.getPath());
                    canRead = f.exists() && f.canRead();
                } else {
                    java.io.File f = new java.io.File(localUri.trim());
                    if (f.exists() && f.canRead()) {
                        parsed = Uri.fromFile(f);
                        canRead = true;
                    }
                }
            } catch (Exception ignored) {}

            if (canRead) {
                primaryTarget = parsed;
            } else {
                primaryTarget = remoteAbsolute;
                fallbackTarget = null;
            }
        } else {
            primaryTarget = remoteAbsolute;
            fallbackTarget = null;
        }

        if (primaryTarget == null && fallbackTarget == null) {
            if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
            if (layoutFile != null) layoutFile.setVisibility(View.GONE);
            return;
        }

        final String clickUrl = (remoteAbsolute != null) ? remoteAbsolute : (localUri != null ? localUri : "");

        String mediaType = msg.getMediaType();
        if (mediaType == null || mediaType.trim().isEmpty() || "file".equalsIgnoreCase(mediaType)) {
            String checkStr = ((msg.getFileName() != null ? msg.getFileName() : "") + " " +
                               (remoteUrl != null ? remoteUrl : "") + " " +
                               (localUri != null ? localUri : "") + " " +
                               (msg.getMessageText() != null ? msg.getMessageText() : "")).toLowerCase();
            if (checkStr.contains(".jpg") || checkStr.contains(".jpeg") || checkStr.contains(".png")
                    || checkStr.contains(".webp") || checkStr.contains(".gif") || checkStr.contains("/chat-media/")
                    || checkStr.contains("/avatars/") || checkStr.contains("📷") || checkStr.contains("photo")
                    || checkStr.contains("image")) {
                mediaType = "image";
            } else if (checkStr.contains(".mp4") || checkStr.contains(".mov") || checkStr.contains(".mkv")
                    || checkStr.contains(".webm") || checkStr.contains(".3gp") || checkStr.contains("/videos/")
                    || checkStr.contains("🎥") || checkStr.contains("video")) {
                mediaType = "video";
            }
        }
        if (mediaType == null || mediaType.trim().isEmpty()) {
            mediaType = "file";
        }
        final String resolvedType = mediaType;

        if ("image".equalsIgnoreCase(resolvedType) || "video".equalsIgnoreCase(resolvedType)) {
            if (layoutFile != null) layoutFile.setVisibility(View.GONE);
            if (layoutMedia != null) {
                layoutMedia.setVisibility(View.VISIBLE);
                if (ivImage != null) {
                    ivImage.clearColorFilter();
                    com.bumptech.glide.RequestBuilder<android.graphics.drawable.Drawable> req = Glide.with(context)
                            .load(primaryTarget != null ? primaryTarget : fallbackTarget)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.bg_search_input)
                            .centerCrop();
                    if (fallbackTarget != null && primaryTarget != null && !primaryTarget.equals(fallbackTarget)) {
                        req = req.error(Glide.with(context)
                                .load(fallbackTarget)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                                .error(R.drawable.ic_photo_library)
                                .centerCrop());
                    } else {
                        req = req.error(R.drawable.ic_photo_library);
                    }
                    req.into(ivImage);
                }
                if (ivPlay != null) {
                    ivPlay.setVisibility("video".equalsIgnoreCase(resolvedType) ? View.VISIBLE : View.GONE);
                }
                layoutMedia.setOnClickListener(v -> {
                    if ("image".equalsIgnoreCase(resolvedType)) {
                        try {
                            Intent viewer = new Intent(context, com.zez.app.ImageViewerActivity.class);
                            viewer.putExtra(com.zez.app.ImageViewerActivity.EXTRA_URI, clickUrl);
                            viewer.putExtra(com.zez.app.ImageViewerActivity.EXTRA_NAME, msg.getFileName() != null ? msg.getFileName() : "Photo");
                            viewer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(viewer);
                            return;
                        } catch (Exception ignored) {}
                    }
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(clickUrl), "video".equalsIgnoreCase(resolvedType) ? "video/*" : "image/*");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        try {
                            Intent web = new Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl));
                            web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(web);
                        } catch (Exception ignored) {}
                    }
                });
            }
        } else {
            if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
            if (layoutFile != null) {
                layoutFile.setVisibility(View.VISIBLE);
                if (tvFileName != null) {
                    tvFileName.setText(msg.getFileName() != null ? msg.getFileName() : "Document");
                }
                if (tvFileSize != null) {
                    tvFileSize.setText(formatFileSize(msg.getFileSize()));
                }
                layoutFile.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(intent);
                    } catch (Exception ignored) {}
                });
            }
        }
    }

    private static String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) return "";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
        return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private static String formatTime(String raw, SimpleDateFormat isoFmt, SimpleDateFormat timeFmt) {
        if (raw == null || raw.isEmpty()) return "";
        try {
            String clean = raw.split("\\.")[0];
            Date d = isoFmt.parse(clean);
            return d != null ? timeFmt.format(d) : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean isImageExtension(String str) {
        if (str == null || str.isEmpty()) return false;
        String s = str.toLowerCase();
        return s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".png") ||
               s.endsWith(".webp") || s.endsWith(".gif") || s.endsWith(".bmp") ||
               s.endsWith(".heic");
    }

    private static boolean isVideoExtension(String str) {
        if (str == null || str.isEmpty()) return false;
        String s = str.toLowerCase();
        return s.endsWith(".mp4") || s.endsWith(".mkv") || s.endsWith(".mov") ||
               s.endsWith(".3gp") || s.endsWith(".webm") || s.endsWith(".avi");
    }
}
