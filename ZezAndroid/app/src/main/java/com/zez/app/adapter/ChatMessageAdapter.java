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
import android.widget.TextView;

import androidx.annotation.NonNull;
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
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public ChatMessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
        this.isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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

    public void updateMessageStatus(String messageId, String newStatus) {
        if (messageId == null || newStatus == null) return;
        for (int i = 0; i < rowItems.size(); i++) {
            ChatRowItem row = rowItems.get(i);
            if (!row.isHeader && row.message != null && messageId.equals(row.message.getId())) {
                if (!newStatus.equalsIgnoreCase(row.message.getStatus())) {
                    row.message.setStatus(newStatus);
                    notifyItemChanged(i);
                }
                break;
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
        ChatRowItem item = rowItems.get(position);
        if (holder instanceof DateHeaderViewHolder) {
            ((DateHeaderViewHolder) holder).tvHeader.setText(item.headerDate);
        } else if (holder instanceof OutgoingViewHolder) {
            ((OutgoingViewHolder) holder).bind(item.message, isoFormat, timeFormat);
        } else if (holder instanceof IncomingViewHolder) {
            ((IncomingViewHolder) holder).bind(item.message, isoFormat, timeFormat);
        }
    }

    @Override
    public int getItemCount() {
        return rowItems.size();
    }

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
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
        LinearLayout layoutFile;
        TextView tvFileName;
        TextView tvFileSize;

        OutgoingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_msg_outgoing_text);
            tvTime = itemView.findViewById(R.id.tv_msg_outgoing_time);
            ivTick = itemView.findViewById(R.id.iv_msg_status_tick);
            layoutMedia = itemView.findViewById(R.id.layout_msg_outgoing_media);
            ivImage = itemView.findViewById(R.id.iv_msg_outgoing_image);
            ivPlay = itemView.findViewById(R.id.iv_msg_outgoing_play);
            layoutFile = itemView.findViewById(R.id.layout_msg_outgoing_file);
            tvFileName = itemView.findViewById(R.id.tv_msg_outgoing_filename);
            tvFileSize = itemView.findViewById(R.id.tv_msg_outgoing_filesize);
        }

        void bind(ChatMessage msg, SimpleDateFormat isoFmt, SimpleDateFormat timeFmt) {
            bindMedia(itemView.getContext(), msg, layoutMedia, ivImage, ivPlay, layoutFile, tvFileName, tvFileSize);

            String text = msg.getMessageText();
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

            String status = msg.getStatus();
            if (ChatMessage.STATUS_READ.equalsIgnoreCase(status)) {
                ivTick.setImageResource(R.drawable.ic_check_double);
                ivTick.setColorFilter(Color.parseColor("#34B7F1")); // WhatsApp vibrant blue double tick
            } else if (ChatMessage.STATUS_DELIVERED.equalsIgnoreCase(status)) {
                ivTick.setImageResource(R.drawable.ic_check_double);
                ivTick.setColorFilter(Color.parseColor("#94A3B8")); // Ash double tick
            } else {
                ivTick.setImageResource(R.drawable.ic_check_single);
                ivTick.setColorFilter(Color.parseColor("#94A3B8")); // Ash single tick
            }
        }
    }

    static class IncomingViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        TextView tvTime;
        FrameLayout layoutMedia;
        ImageView ivImage;
        ImageView ivPlay;
        LinearLayout layoutFile;
        TextView tvFileName;
        TextView tvFileSize;

        IncomingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_msg_incoming_text);
            tvTime = itemView.findViewById(R.id.tv_msg_incoming_time);
            layoutMedia = itemView.findViewById(R.id.layout_msg_incoming_media);
            ivImage = itemView.findViewById(R.id.iv_msg_incoming_image);
            ivPlay = itemView.findViewById(R.id.iv_msg_incoming_play);
            layoutFile = itemView.findViewById(R.id.layout_msg_incoming_file);
            tvFileName = itemView.findViewById(R.id.tv_msg_incoming_filename);
            tvFileSize = itemView.findViewById(R.id.tv_msg_incoming_filesize);
        }

        void bind(ChatMessage msg, SimpleDateFormat isoFmt, SimpleDateFormat timeFmt) {
            bindMedia(itemView.getContext(), msg, layoutMedia, ivImage, ivPlay, layoutFile, tvFileName, tvFileSize);

            String text = msg.getMessageText();
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
        }
    }

    private static void bindMedia(Context context, ChatMessage msg, FrameLayout layoutMedia, ImageView ivImage, ImageView ivPlay, LinearLayout layoutFile, TextView tvFileName, TextView tvFileSize) {
        String mediaUrl = msg.getMediaUrl();
        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
            if (layoutFile != null) layoutFile.setVisibility(View.GONE);
            return;
        }

        String fullUrl = ChatApiClient.getAbsoluteMediaUrl(context, mediaUrl);
        String mediaType = msg.getMediaType();
        if (mediaType == null) {
            String lower = mediaUrl.toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") || lower.endsWith(".gif")) {
                mediaType = "image";
            } else if (lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".mov") || lower.endsWith(".3gp")) {
                mediaType = "video";
            } else {
                mediaType = "file";
            }
        }
        final String resolvedType = mediaType;

        if ("image".equalsIgnoreCase(resolvedType) || "video".equalsIgnoreCase(resolvedType)) {
            if (layoutFile != null) layoutFile.setVisibility(View.GONE);
            if (layoutMedia != null) {
                layoutMedia.setVisibility(View.VISIBLE);
                if (ivImage != null) {
                    Glide.with(context)
                            .load(fullUrl)
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
}
