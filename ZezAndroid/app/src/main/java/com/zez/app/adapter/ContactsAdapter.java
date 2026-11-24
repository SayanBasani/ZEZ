package com.zez.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.zez.app.R;
import com.zez.app.model.ChatContactItem;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTACT = 1;

    public interface OnContactClickListener {
        void onContactClick(ChatContactItem contact);
        void onInviteClick(ChatContactItem contact);
    }

    public static class DisplayItem {
        public boolean isHeader;
        public String headerTitle;
        public ChatContactItem contact;

        public static DisplayItem header(String title) {
            DisplayItem item = new DisplayItem();
            item.isHeader = true;
            item.headerTitle = title;
            return item;
        }

        public static DisplayItem contact(ChatContactItem contact) {
            DisplayItem item = new DisplayItem();
            item.isHeader = false;
            item.contact = contact;
            return item;
        }
    }

    private final List<DisplayItem> items = new ArrayList<>();
    private final OnContactClickListener listener;

    public ContactsAdapter(OnContactClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<ChatContactItem> registered, List<ChatContactItem> notRegistered) {
        items.clear();
        if (registered != null && !registered.isEmpty()) {
            items.add(DisplayItem.header("CONTACTS ON ZEZ (" + registered.size() + ")"));
            for (ChatContactItem c : registered) {
                items.add(DisplayItem.contact(c));
            }
        }
        if (notRegistered != null && !notRegistered.isEmpty()) {
            items.add(DisplayItem.header("INVITE TO ZEZ (" + notRegistered.size() + ")"));
            for (ChatContactItem c : notRegistered) {
                items.add(DisplayItem.contact(c));
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isHeader ? TYPE_HEADER : TYPE_CONTACT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_contact_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_contact_entry, parent, false);
            return new ContactViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvTitle.setText(item.headerTitle);
        } else if (holder instanceof ContactViewHolder) {
            ((ContactViewHolder) holder).bind(item.contact, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_section_title);
        }
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvInitials;
        TextView tvName;
        TextView tvDetail;
        MaterialButton btnInvite;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_contact_avatar);
            tvInitials = itemView.findViewById(R.id.tv_avatar_initials);
            tvName = itemView.findViewById(R.id.tv_contact_name);
            tvDetail = itemView.findViewById(R.id.tv_contact_detail);
            btnInvite = itemView.findViewById(R.id.btn_contact_invite);
        }

        void bind(ChatContactItem contact, OnContactClickListener listener) {
            tvName.setText(contact.getDisplayName());

            String avatarUrl = contact.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                String fullUrl = com.zez.app.util.ChatApiClient.getAbsoluteMediaUrl(itemView.getContext(), avatarUrl);
                ivAvatar.setImageTintList(null);
                ivAvatar.setPadding(0, 0, 0, 0);
                com.bumptech.glide.Glide.with(itemView.getContext())
                        .load(fullUrl)
                        .circleCrop()
                        .placeholder(R.drawable.bg_circle_glass)
                        .error(R.drawable.bg_circle_glass)
                        .into(ivAvatar);
                if (tvInitials != null) tvInitials.setVisibility(View.GONE);
            } else {
                int p = (int) (8 * itemView.getContext().getResources().getDisplayMetrics().density);
                ivAvatar.setPadding(p, p, p, p);
                ivAvatar.setImageTintList(androidx.core.content.ContextCompat.getColorStateList(itemView.getContext(), R.color.theme_text_secondary));
                ivAvatar.setImageResource(R.drawable.ic_person);
                ivAvatar.setBackgroundResource(R.drawable.bg_circle_glass);
                if (tvInitials != null) tvInitials.setVisibility(View.GONE);
            }

            if (contact.isRegistered()) {
                String status = contact.getStatusMessage();
                tvDetail.setText(status != null && !status.isEmpty() ? status : "Hey there! I am using Quacky.");
                btnInvite.setVisibility(View.GONE);
                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onContactClick(contact);
                });
            } else {
                tvDetail.setText(contact.getPhoneNumber());
                btnInvite.setVisibility(View.VISIBLE);
                btnInvite.setOnClickListener(v -> {
                    if (listener != null) listener.onInviteClick(contact);
                });
                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onInviteClick(contact);
                });
            }
        }
    }
}
