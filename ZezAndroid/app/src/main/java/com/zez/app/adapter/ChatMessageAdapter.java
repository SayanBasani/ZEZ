package com.zez.app.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zez.app.R;
import com.zez.app.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        OutgoingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_msg_outgoing_text);
            tvTime = itemView.findViewById(R.id.tv_msg_outgoing_time);
            ivTick = itemView.findViewById(R.id.iv_msg_status_tick);
        }

        void bind(ChatMessage msg, SimpleDateFormat isoFmt, SimpleDateFormat timeFmt) {
            tvText.setText(msg.getMessageText());
            tvTime.setText(formatTime(msg.getCreatedAt(), isoFmt, timeFmt));

            String status = msg.getStatus();
            if (ChatMessage.STATUS_READ.equalsIgnoreCase(status)) {
                ivTick.setImageResource(R.drawable.ic_check_double);
                ivTick.setColorFilter(Color.parseColor("#93C5FD")); // WhatsApp blue double tick
            } else if (ChatMessage.STATUS_DELIVERED.equalsIgnoreCase(status)) {
                ivTick.setImageResource(R.drawable.ic_check_double);
                ivTick.setColorFilter(Color.parseColor("#D1D5DB"));
            } else {
                ivTick.setImageResource(R.drawable.ic_check_single);
                ivTick.setColorFilter(Color.parseColor("#D1D5DB"));
            }
        }
    }

    static class IncomingViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        TextView tvTime;

        IncomingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_msg_incoming_text);
            tvTime = itemView.findViewById(R.id.tv_msg_incoming_time);
        }

        void bind(ChatMessage msg, SimpleDateFormat isoFmt, SimpleDateFormat timeFmt) {
            tvText.setText(msg.getMessageText());
            tvTime.setText(formatTime(msg.getCreatedAt(), isoFmt, timeFmt));
        }
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
