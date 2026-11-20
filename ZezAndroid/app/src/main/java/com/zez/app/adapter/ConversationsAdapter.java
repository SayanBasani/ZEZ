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
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < conversations.size()) {
            String id = conversations.get(position).getConversationId();
            if (id != null) return id.hashCode();
        }
        return position;
    }

    public void setConversations(List<ChatConversation> list) {
        if (list == null && conversations.isEmpty()) return;
        if (list != null && list.size() == conversations.size()) {
            boolean identical = true;
            for (int i = 0; i < list.size(); i++) {
                ChatConversation a = list.get(i);
                ChatConversation b = conversations.get(i);
                if (!isSameConversation(a, b)) {
                    identical = false;
                    break;
                }
            }
            if (identical) {
                return; // Nothing changed: skip notifyDataSetChanged to eliminate flickering
            }
        }
        conversations.clear();
        if (list != null) {
            conversations.addAll(list);
        }
        notifyDataSetChanged();
    }

    private boolean isSameConversation(ChatConversation a, ChatConversation b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return java.util.Objects.equals(a.getConversationId(), b.getConversationId())
                && java.util.Objects.equals(a.getLastMessageText(), b.getLastMessageText())
                && java.util.Objects.equals(a.getLastMessageTime(), b.getLastMessageTime())
                && a.getUnreadCount() == b.getUnreadCount()
                && java.util.Objects.equals(a.getOtherUserName(), b.getOtherUserName())
                && java.util.Objects.equals(a.getOtherUserAvatar(), b.getOtherUserAvatar());
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
            String myUserId = com.zez.app.util.ChatPreferencesHelper.getUserId(itemView.getContext());
            String displayName;
            if (item.getOtherUserId() != null && item.getOtherUserId().equals(myUserId)) {
                displayName = "You (Message yourself)";
            } else {
                displayName = com.zez.app.util.PhoneHelper.resolveContactName(itemView.getContext(), item.getOtherUserPhone(), item.getOtherUserName());
            }
            tvName.setText(displayName);

            String avatarUrl = item.getOtherUserAvatar();
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                String fullUrl = com.zez.app.util.ChatApiClient.getAbsoluteMediaUrl(itemView.getContext(), avatarUrl);
                com.bumptech.glide.Glide.with(itemView.getContext())
                        .load(fullUrl)
                        .circleCrop()
                        .placeholder(R.drawable.bg_circle_glass)
                        .error(R.drawable.bg_circle_glass)
                        .into(ivAvatar);
                if (tvInitials != null) tvInitials.setVisibility(View.GONE);
            } else {
                ivAvatar.setImageResource(R.drawable.bg_circle_glass);
                if (tvInitials != null) {
                    tvInitials.setVisibility(View.VISIBLE);
                    tvInitials.setText(displayName != null && !displayName.isEmpty() ? String.valueOf(displayName.charAt(0)).toUpperCase(java.util.Locale.ROOT) : "?");
                }
            }

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
                int accentColor = com.zez.app.util.PreferencesHelper.getAccentColorInt(itemView.getContext());
                if (tvUnreadBadge.getBackground() != null) {
                    tvUnreadBadge.getBackground().mutate().setTint(accentColor);
                }
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
