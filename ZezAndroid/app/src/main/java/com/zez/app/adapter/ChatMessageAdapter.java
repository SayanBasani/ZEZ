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

    private OnMessageActionListener actionListener;
    private OnMediaRetryListener retryListener;

    public void setOnMessageActionListener(OnMessageActionListener listener) {
        this.actionListener = listener;
    }

    public void setOnMediaRetryListener(OnMediaRetryListener retryListener) {
        this.retryListener = retryListener;
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
        String day = getDayString(message.getCreatedAt());
        boolean needHeader = rowItems.isEmpty() || !day.equals(getDayString(rowItems.get(rowItems.size() - 1).message != null ? rowItems.get(rowItems.size() - 1).message.getCreatedAt() : null));
        if (needHeader) {
            rowItems.add(ChatRowItem.header(day));
        }
        rowItems.add(ChatRowItem.message(message));
        notifyItemInserted(rowItems.size() - 1);
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
            if (payloads.contains("status_payload") && holder instanceof OutgoingViewHolder) {
                ((OutgoingViewHolder) holder).bindStatusOnly(item.message);
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
            holder.itemView.setBackgroundColor(Color.parseColor("#3322C55E"));
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
                    if (tvQuoteText != null) tvQuoteText.setText(msg.getQuotedText());
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

            String text = msg.getCleanMessageText();
            if (text != null && !text.isEmpty() && !text.equals("📷 Photo") && !text.equals("🎥 Video") && !text.equals("🎵 Audio") && !text.startsWith("📄 ") && !text.equals("📎 Attachment")) {
                tvText.setVisibility(View.VISIBLE);
                tvText.setText(text);
            } else if (msg.getMediaUrl() == null || msg.getMediaUrl().isEmpty()) {
                tvText.setVisibility(View.VISIBLE);
                tvText.setText(text != null ? text : "");
            } else {
                tvText.setVisibility(View.GONE);
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
            int nightMode = itemView.getContext().getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            boolean isSysDark = (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES);
            String customIncoming = com.zez.app.util.PreferencesHelper.getIncomingBubbleColor(itemView.getContext());
            boolean isDark = isSysDark;

            if (msg.isDeletedForEveryone()) {
                tvText.setVisibility(View.VISIBLE);
                tvText.setText("🚫 This message was deleted");
                tvText.setTypeface(null, android.graphics.Typeface.ITALIC);
                if (customIncoming != null && !customIncoming.trim().isEmpty()) {
                    try {
                        isDark = isColorDark(Color.parseColor(customIncoming));
                    } catch (Exception ignored) {}
                }
                tvText.setTextColor(isDark ? Color.parseColor("#94A3B8") : Color.parseColor("#475569"));
                if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
                if (layoutFile != null) layoutFile.setVisibility(View.GONE);
                if (layoutQuote != null) layoutQuote.setVisibility(View.GONE);
                if (layoutReaction != null) layoutReaction.setVisibility(View.GONE);
                tvTime.setText(formatTime(msg.getCreatedAt(), isoFmt, timeFmt));
                return;
            }

            tvText.setTypeface(null, android.graphics.Typeface.NORMAL);

            // Contrast & Custom Incoming Bubble Color
            boolean bubbleIsDark = isSysDark;
            if (customIncoming != null && !customIncoming.trim().isEmpty() && layoutBubble != null && layoutBubble.getBackground() != null) {
                try {
                    int bubbleColor = Color.parseColor(customIncoming);
                    layoutBubble.getBackground().mutate().setTint(bubbleColor);
                    bubbleIsDark = isColorDark(bubbleColor);
                } catch (Exception e) {
                    layoutBubble.getBackground().mutate().setTintList(null);
                    bubbleIsDark = isSysDark;
                }
            } else {
                if (layoutBubble != null && layoutBubble.getBackground() != null) {
                    layoutBubble.getBackground().mutate().setTintList(null);
                }
                bubbleIsDark = isSysDark;
            }

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
                        tvQuoteText.setText(msg.getQuotedText());
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

            String text = msg.getCleanMessageText();
            if (text != null && !text.isEmpty() && !text.equals("📷 Photo") && !text.equals("🎥 Video") && !text.equals("🎵 Audio") && !text.startsWith("📄 ") && !text.equals("📎 Attachment")) {
                tvText.setVisibility(View.VISIBLE);
                tvText.setText(text);
            } else if (msg.getMediaUrl() == null || msg.getMediaUrl().isEmpty()) {
                tvText.setVisibility(View.VISIBLE);
                tvText.setText(text != null ? text : "");
            } else {
                tvText.setVisibility(View.GONE);
            }

            tvTime.setText(formatTime(msg.getCreatedAt(), isoFmt, timeFmt));
            bindReactionOnly(msg);
        }
    }

    private static void bindMedia(Context context, ChatMessage msg, FrameLayout layoutMedia, ImageView ivImage, ImageView ivPlay, LinearLayout layoutFile, TextView tvFileName, TextView tvFileSize) {
        String rawMedia = (msg.getLocalUri() != null && !msg.getLocalUri().trim().isEmpty())
                ? msg.getLocalUri().trim()
                : msg.getMediaUrl();

        if (rawMedia == null || rawMedia.trim().isEmpty()) {
            if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
            if (layoutFile != null) layoutFile.setVisibility(View.GONE);
            return;
        }

        final Object loadTarget;
        final String fullUrl;
        if (rawMedia.startsWith("content://") || rawMedia.startsWith("file://")) {
            loadTarget = Uri.parse(rawMedia);
            fullUrl = rawMedia;
        } else {
            fullUrl = ChatApiClient.getAbsoluteMediaUrl(context, rawMedia);
            loadTarget = fullUrl;
        }

        String mediaType = msg.getMediaType();
        if (mediaType == null || mediaType.trim().isEmpty() || "file".equalsIgnoreCase(mediaType)) {
            String lowerUrl = rawMedia.toLowerCase();
            String lowerName = msg.getFileName() != null ? msg.getFileName().toLowerCase() : "";
            String lowerText = msg.getMessageText() != null ? msg.getMessageText().toLowerCase() : "";

            if (isImageExtension(lowerUrl) || isImageExtension(lowerName) || lowerUrl.contains("/chat-media/") || lowerUrl.contains("/avatars/") || lowerText.contains("📷") || lowerText.contains("photo")) {
                mediaType = "image";
            } else if (isVideoExtension(lowerUrl) || isVideoExtension(lowerName) || lowerUrl.contains("/videos/") || lowerText.contains("🎥") || lowerText.contains("video")) {
                mediaType = "video";
            } else if (lowerUrl.startsWith("content://")) {
                try {
                    String mime = context.getContentResolver().getType(Uri.parse(rawMedia));
                    if (mime != null) {
                        if (mime.startsWith("image/")) mediaType = "image";
                        else if (mime.startsWith("video/")) mediaType = "video";
                    }
                } catch (Exception ignored) {}
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
                    Glide.with(context)
                            .load(loadTarget)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.bg_search_input)
                            .error(R.drawable.ic_photo_library)
                            .centerCrop()
                            .into(ivImage);
                }
                if (ivPlay != null) {
                    ivPlay.setVisibility("video".equalsIgnoreCase(resolvedType) ? View.VISIBLE : View.GONE);
                }
                layoutMedia.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(fullUrl), "video".equalsIgnoreCase(resolvedType) ? "video/*" : "image/*");
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
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
