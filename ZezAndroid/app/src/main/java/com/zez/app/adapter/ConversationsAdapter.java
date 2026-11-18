package com.zez.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zez.app.R;
import com.zez.app.model.ChatConversation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> {
    public interface OnConversationClickListener {
        void onConversationClick(ChatConversation conversation);
    }

    private final List<ChatConversation> conversations = new ArrayList<>();
    private final OnConversationClickListener listener;
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());

    public ConversationsAdapter(OnConversationClickListener listener) {
        this.listener = listener;
    }

    public void setConversations(List<ChatConversation> list) {
        conversations.clear();
        if (list != null) {
            conversations.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatConversation item = conversations.get(position);
        holder.bind(item, listener, isoFormat, timeFormat, dateFormat);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvInitials;
        TextView tvName;
        TextView tvTime;
        ImageView ivTick;
        TextView tvLastMessage;
        TextView tvUnreadBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_conv_avatar);
            tvInitials = itemView.findViewById(R.id.tv_conv_avatar_initials);
            tvName = itemView.findViewById(R.id.tv_conv_name);
            tvTime = itemView.findViewById(R.id.tv_conv_time);
            ivTick = itemView.findViewById(R.id.iv_conv_tick);
            tvLastMessage = itemView.findViewById(R.id.tv_conv_last_message);
            tvUnreadBadge = itemView.findViewById(R.id.tv_conv_unread_badge);
        }

        void bind(ChatConversation item, OnConversationClickListener listener, SimpleDateFormat isoFmt, SimpleDateFormat timeFmt, SimpleDateFormat dateFmt) {
            tvName.setText(item.getOtherUserName());

            String lastMsg = item.getLastMessageText();
            tvLastMessage.setText(lastMsg != null && !lastMsg.isEmpty() ? lastMsg : "No messages yet");

            // Format timestamp
            String rawTime = item.getLastMessageTime();
            if (rawTime != null && !rawTime.isEmpty()) {
                try {
                    String cleanIso = rawTime.split("\\.")[0];
                    Date date = isoFmt.parse(cleanIso);
                    if (date != null) {
                        long diff = System.currentTimeMillis() - date.getTime();
                        if (diff < 24 * 60 * 60 * 1000) {
                            tvTime.setText(timeFmt.format(date));
                        } else {
                            tvTime.setText(dateFmt.format(date));
                        }
                    } else {
                        tvTime.setText("");
                    }
                } catch (Exception e) {
                    tvTime.setText("");
                }
            } else {
                tvTime.setText("");
            }

            // Unread badge
            if (item.getUnreadCount() > 0) {
                tvUnreadBadge.setVisibility(View.VISIBLE);
                tvUnreadBadge.setText(String.valueOf(item.getUnreadCount()));
                tvLastMessage.setTextColor(itemView.getContext().getResources().getColor(R.color.theme_text_primary));
            } else {
                tvUnreadBadge.setVisibility(View.GONE);
                tvLastMessage.setTextColor(itemView.getContext().getResources().getColor(R.color.theme_text_secondary));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onConversationClick(item);
            });
        }
    }
}
