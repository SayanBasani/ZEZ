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
import com.zez.app.model.ChatConversation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> {
    public interface OnConversationClickListener {
        void onConversationClick(ChatConversation conversation);
    }

    public interface OnAvatarClickListener {
        void onAvatarClick(ChatConversation conversation);
    }

    public interface OnSelectionListener {
        void onSelectionChanged(int count);
    }

    private final List<ChatConversation> conversations = new ArrayList<>();
    private final Set<String> selectedConversationIds = new HashSet<>();
    private boolean isSelectionMode = false;
    private final OnConversationClickListener listener;
    private OnAvatarClickListener avatarClickListener;
    private OnSelectionListener selectionListener;

    public void setOnAvatarClickListener(OnAvatarClickListener avatarClickListener) {
        this.avatarClickListener = avatarClickListener;
    }
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());

    public ConversationsAdapter(OnConversationClickListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void setOnSelectionListener(OnSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public int getSelectedCount() {
        return selectedConversationIds.size();
    }

    public List<ChatConversation> getSelectedConversations() {
        List<ChatConversation> selected = new ArrayList<>();
        for (ChatConversation c : conversations) {
            if (c.getConversationId() != null && selectedConversationIds.contains(c.getConversationId())) {
                selected.add(c);
            }
        }
        return selected;
    }

    public void selectAll() {
        selectedConversationIds.clear();
        for (ChatConversation c : conversations) {
            if (c.getConversationId() != null) {
                selectedConversationIds.add(c.getConversationId());
            }
        }
        isSelectionMode = !selectedConversationIds.isEmpty();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedConversationIds.size());
        }
    }

    public void clearSelection() {
        selectedConversationIds.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(0);
        }
    }

    public void toggleSelection(ChatConversation conv) {
        if (conv == null || conv.getConversationId() == null) return;
        String id = conv.getConversationId();
        if (selectedConversationIds.contains(id)) {
            selectedConversationIds.remove(id);
        } else {
            selectedConversationIds.add(id);
        }
        isSelectionMode = !selectedConversationIds.isEmpty();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedConversationIds.size());
        }
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
        if (list == null) list = new ArrayList<>();
        final List<ChatConversation> oldList = new ArrayList<>(conversations);
        final List<ChatConversation> targetList = new ArrayList<>(list);

        androidx.recyclerview.widget.DiffUtil.DiffResult diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(new androidx.recyclerview.widget.DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return targetList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                ChatConversation o = oldList.get(oldItemPosition);
                ChatConversation n = targetList.get(newItemPosition);
                if (o == null || n == null) return o == n;
                if (o.getConversationId() != null && n.getConversationId() != null) {
                    return o.getConversationId().equals(n.getConversationId());
                }
                return java.util.Objects.equals(o.getOtherUserId(), n.getOtherUserId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return isSameConversation(oldList.get(oldItemPosition), targetList.get(newItemPosition));
            }
        });

        conversations.clear();
        conversations.addAll(targetList);
        diffResult.dispatchUpdatesTo(this);
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
        boolean isSelected = item.getConversationId() != null && selectedConversationIds.contains(item.getConversationId());
        holder.bind(item, isSelected, this, listener, isoFormat, timeFormat, dateFormat);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View layoutRoot;
        View layoutAvatar;
        ImageView ivAvatar;
        TextView tvInitials;
        ImageView ivCheckBadge;
        TextView tvName;
        TextView tvTime;
        ImageView ivTick;
        TextView tvLastMessage;
        TextView tvUnreadBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutRoot = itemView.findViewById(R.id.layout_conv_root);
            layoutAvatar = itemView.findViewById(R.id.layout_conv_avatar_container);
            ivAvatar = itemView.findViewById(R.id.iv_conv_avatar);
            tvInitials = itemView.findViewById(R.id.tv_conv_avatar_initials);
            ivCheckBadge = itemView.findViewById(R.id.iv_conv_check_badge);
            tvName = itemView.findViewById(R.id.tv_conv_name);
            tvTime = itemView.findViewById(R.id.tv_conv_time);
            ivTick = itemView.findViewById(R.id.iv_conv_tick);
            tvLastMessage = itemView.findViewById(R.id.tv_conv_last_message);
            tvUnreadBadge = itemView.findViewById(R.id.tv_conv_unread_badge);
        }

        void bind(ChatConversation item, boolean isSelected, ConversationsAdapter adapter, OnConversationClickListener listener, SimpleDateFormat isoFmt, SimpleDateFormat timeFmt, SimpleDateFormat dateFmt) {
            String myUserId = com.zez.app.util.ChatPreferencesHelper.getUserId(itemView.getContext());
            String displayName;
            if (item.getOtherUserId() != null && item.getOtherUserId().equals(myUserId)) {
                displayName = "You (Message yourself)";
            } else {
                displayName = com.zez.app.util.PhoneHelper.resolveContactName(itemView.getContext(), item.getOtherUserPhone(), item.getOtherUserName());
            }
            tvName.setText(displayName);

            // Selection state: row background and green checkmark badge
            if (isSelected) {
                if (layoutRoot != null) layoutRoot.setBackgroundColor(Color.parseColor("#1B2E24")); // WhatsApp selection green
                if (ivCheckBadge != null) ivCheckBadge.setVisibility(View.VISIBLE);
            } else {
                if (layoutRoot != null) layoutRoot.setBackgroundColor(Color.TRANSPARENT);
                if (ivCheckBadge != null) ivCheckBadge.setVisibility(View.GONE);
            }

            String avatarUrl = item.getOtherUserAvatar();
            if (item.getOtherUserId() != null && item.getOtherUserId().equals(myUserId)) {
                if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                    avatarUrl = com.zez.app.util.ChatPreferencesHelper.getUserAvatar(itemView.getContext());
                }
            }
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                String fullUrl = com.zez.app.util.ChatApiClient.getAbsoluteMediaUrl(itemView.getContext(), avatarUrl);
                ivAvatar.setImageTintList(null);
                ivAvatar.clearColorFilter();
                ivAvatar.setPadding(0, 0, 0, 0);
                com.bumptech.glide.Glide.with(itemView.getContext())
                        .load(fullUrl)
                        .circleCrop()
                        .placeholder(R.drawable.bg_circle_glass)
                        .error(R.drawable.bg_circle_glass)
                        .into(ivAvatar);
                if (tvInitials != null) tvInitials.setVisibility(View.GONE);
            } else {
                ivAvatar.clearColorFilter();
                int p = (int) (10 * itemView.getContext().getResources().getDisplayMetrics().density);
                ivAvatar.setPadding(p, p, p, p);
                ivAvatar.setImageTintList(androidx.core.content.ContextCompat.getColorStateList(itemView.getContext(), R.color.theme_text_secondary));
                ivAvatar.setImageResource(R.drawable.ic_person);
                if (tvInitials != null) {
                    tvInitials.setVisibility(View.VISIBLE);
                    tvInitials.setText(displayName != null && !displayName.isEmpty() ? String.valueOf(displayName.charAt(0)).toUpperCase(java.util.Locale.ROOT) : "?");
                }
            }

            String lastMsg = item.getLastMessageText();
            if (lastMsg != null && lastMsg.startsWith("[reply:")) {
                int closeIdx = lastMsg.indexOf("]\n");
                if (closeIdx != -1) {
                    lastMsg = lastMsg.substring(closeIdx + 2).trim();
                }
            }
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

            View avatarClickTarget = layoutAvatar != null ? layoutAvatar : ivAvatar;
            if (avatarClickTarget != null) {
                avatarClickTarget.setOnClickListener(v -> {
                    if (adapter.isSelectionMode()) {
                        adapter.toggleSelection(item);
                    } else if (adapter.avatarClickListener != null) {
                        adapter.avatarClickListener.onAvatarClick(item);
                    } else if (listener != null) {
                        listener.onConversationClick(item);
                    }
                });
            }

            itemView.setOnLongClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                adapter.toggleSelection(item);
                return true;
            });

            itemView.setOnClickListener(v -> {
                if (adapter.isSelectionMode()) {
                    adapter.toggleSelection(item);
                } else if (listener != null) {
                    listener.onConversationClick(item);
                }
            });
        }
    }
}
