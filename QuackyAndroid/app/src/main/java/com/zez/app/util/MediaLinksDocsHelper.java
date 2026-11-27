package com.zez.app.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zez.app.ImageViewerActivity;
import com.zez.app.R;
import com.zez.app.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaLinksDocsHelper {

    public interface OnGoToChatListener {
        void onGoToChat(String messageId);
    }

    public interface OnDeleteMessagesListener {
        void onDeleteMessages(List<String> messageIds);
    }

    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+",
            Pattern.CASE_INSENSITIVE
    );

    public static class LinkItem {
        public String url;
        public String snippet;
        public String messageId;
        public String dateStr;
    }

    public static void showMediaLinksDocsDialog(
            Context context,
            List<ChatMessage> allMessages,
            OnGoToChatListener goToChatListener,
            OnDeleteMessagesListener deleteListener
    ) {
        if (context == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.Theme_Zez);
        View root = LayoutInflater.from(context).inflate(R.layout.dialog_media_links_docs, null);
        dialog.setContentView(root);

        // Maximize bottom sheet height
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        ImageButton btnBack = root.findViewById(R.id.btn_mld_back);
        ImageButton btnDelete = root.findViewById(R.id.btn_mld_delete_selected);

        View tabMedia = root.findViewById(R.id.tab_mld_media);
        View tabDocs = root.findViewById(R.id.tab_mld_docs);
        View tabLinks = root.findViewById(R.id.tab_mld_links);

        TextView tvTabMedia = root.findViewById(R.id.tv_tab_media_label);
        TextView tvTabDocs = root.findViewById(R.id.tv_tab_docs_label);
        TextView tvTabLinks = root.findViewById(R.id.tv_tab_links_label);

        View indMedia = root.findViewById(R.id.indicator_tab_media);
        View indDocs = root.findViewById(R.id.indicator_tab_docs);
        View indLinks = root.findViewById(R.id.indicator_tab_links);

        RecyclerView rvMedia = root.findViewById(R.id.rv_mld_media_grid);
        RecyclerView rvDocs = root.findViewById(R.id.rv_mld_docs_list);
        RecyclerView rvLinks = root.findViewById(R.id.rv_mld_links_list);
        View layoutEmpty = root.findViewById(R.id.layout_mld_empty);
        TextView tvEmptyText = root.findViewById(R.id.tv_mld_empty_text);
        ImageView ivEmptyIcon = root.findViewById(R.id.iv_mld_empty_icon);

        if (btnBack != null) btnBack.setOnClickListener(v -> dialog.dismiss());

        // Categorize messages
        List<ChatMessage> mediaList = new ArrayList<>();
        List<ChatMessage> docList = new ArrayList<>();
        List<LinkItem> linkList = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        if (allMessages != null) {
            for (ChatMessage m : allMessages) {
                if (m.isDeletedForEveryone()) continue;
                String mediaUrl = m.getMediaUrl();
                String mType = m.getMediaType();
                String text = m.getMessageText();

                if (mediaUrl != null && !mediaUrl.trim().isEmpty()) {
                    String lower = mediaUrl.toLowerCase();
                    boolean isMedia = "image".equalsIgnoreCase(mType) || "video".equalsIgnoreCase(mType)
                            || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                            || lower.endsWith(".webp") || lower.endsWith(".mp4") || lower.endsWith(".mov")
                            || lower.endsWith(".mkv");
                    if (isMedia) {
                        mediaList.add(m);
                    } else {
                        docList.add(m);
                    }
                }

                if (text != null && !text.trim().isEmpty()) {
                    Matcher matcher = URL_PATTERN.matcher(text);
                    while (matcher.find()) {
                        LinkItem li = new LinkItem();
                        li.url = matcher.group();
                        li.snippet = text;
                        li.messageId = m.getId();
                        li.dateStr = m.getCreatedAt() != null ? m.getCreatedAt() : "";
                        linkList.add(li);
                    }
                }
            }
        }

        int primaryColor = PreferencesHelper.getAccentColorInt(context);
        int textColorSecondary = ContextCompat.getColor(context, R.color.theme_text_secondary);

        tvTabMedia.setText("Media (" + mediaList.size() + ")");
        tvTabDocs.setText("Docs (" + docList.size() + ")");
        tvTabLinks.setText("Links (" + linkList.size() + ")");

        // Selection set
        Set<String> selectedMediaIds = new HashSet<>();

        // Adapters
        rvMedia.setLayoutManager(new GridLayoutManager(context, 3));
        MediaGridAdapter mediaAdapter = new MediaGridAdapter(context, mediaList, selectedMediaIds, new MediaGridAdapter.OnMediaItemClickListener() {
            @Override
            public void onItemClick(ChatMessage msg) {
                if (!selectedMediaIds.isEmpty()) {
                    toggleMediaSelection(msg.getId(), selectedMediaIds, btnDelete);
                    rvMedia.getAdapter().notifyDataSetChanged();
                    return;
                }
                String fullUrl = ChatApiClient.getAbsoluteMediaUrl(context, msg.getMediaUrl());
                boolean isVideo = "video".equalsIgnoreCase(msg.getMediaType()) ||
                        (msg.getMediaUrl() != null && msg.getMediaUrl().toLowerCase().endsWith(".mp4"));
                if (isVideo) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(fullUrl), "video/*");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception ignored) {}
                } else {
                    Intent viewer = new Intent(context, ImageViewerActivity.class);
                    viewer.putExtra(ImageViewerActivity.EXTRA_URI, fullUrl);
                    viewer.putExtra(ImageViewerActivity.EXTRA_NAME, msg.getFileName() != null ? msg.getFileName() : "Media");
                    context.startActivity(viewer);
                }
            }

            @Override
            public void onItemLongClick(ChatMessage msg) {
                new AlertDialog.Builder(context)
                        .setTitle(msg.getFileName() != null ? msg.getFileName() : "Media Option")
                        .setItems(new CharSequence[]{"Go to chat", "Select", "Delete"}, (d, which) -> {
                            if (which == 0) {
                                dialog.dismiss();
                                if (goToChatListener != null) goToChatListener.onGoToChat(msg.getId());
                            } else if (which == 1) {
                                toggleMediaSelection(msg.getId(), selectedMediaIds, btnDelete);
                                rvMedia.getAdapter().notifyDataSetChanged();
                            } else if (which == 2) {
                                if (deleteListener != null) {
                                    List<String> toDel = new ArrayList<>();
                                    toDel.add(msg.getId());
                                    deleteListener.onDeleteMessages(toDel);
                                    dialog.dismiss();
                                }
                            }
                        })
                        .show();
            }
        });
        rvMedia.setAdapter(mediaAdapter);

        rvDocs.setLayoutManager(new LinearLayoutManager(context));
        DocsAdapter docsAdapter = new DocsAdapter(context, docList, msg -> {
            dialog.dismiss();
            if (goToChatListener != null) goToChatListener.onGoToChat(msg.getId());
        });
        rvDocs.setAdapter(docsAdapter);

        rvLinks.setLayoutManager(new LinearLayoutManager(context));
        LinksAdapter linksAdapter = new LinksAdapter(context, linkList, item -> {
            dialog.dismiss();
            if (goToChatListener != null) goToChatListener.onGoToChat(item.messageId);
        });
        rvLinks.setAdapter(linksAdapter);

        // Delete button click
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                if (!selectedMediaIds.isEmpty()) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete Media")
                            .setMessage("Delete " + selectedMediaIds.size() + " selected message(s)?")
                            .setPositiveButton("Delete", (d, w) -> {
                                if (deleteListener != null) {
                                    deleteListener.onDeleteMessages(new ArrayList<>(selectedMediaIds));
                                }
                                dialog.dismiss();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });
        }

        // Tab switcher runnable
        final int[] currentTab = {0}; // 0 = Media, 1 = Docs, 2 = Links
        Runnable updateTabs = () -> {
            if (currentTab[0] == 0) {
                tvTabMedia.setTextColor(primaryColor);
                tvTabMedia.setTypeface(null, android.graphics.Typeface.BOLD);
                indMedia.setBackgroundColor(primaryColor);

                tvTabDocs.setTextColor(textColorSecondary);
                tvTabDocs.setTypeface(null, android.graphics.Typeface.NORMAL);
                indDocs.setBackgroundColor(Color.TRANSPARENT);

                tvTabLinks.setTextColor(textColorSecondary);
                tvTabLinks.setTypeface(null, android.graphics.Typeface.NORMAL);
                indLinks.setBackgroundColor(Color.TRANSPARENT);

                rvMedia.setVisibility(mediaList.isEmpty() ? View.GONE : View.VISIBLE);
                rvDocs.setVisibility(View.GONE);
                rvLinks.setVisibility(View.GONE);

                layoutEmpty.setVisibility(mediaList.isEmpty() ? View.VISIBLE : View.GONE);
                tvEmptyText.setText("No photos or videos shared yet");
                ivEmptyIcon.setImageResource(R.drawable.ic_photo_library);
            } else if (currentTab[0] == 1) {
                tvTabDocs.setTextColor(primaryColor);
                tvTabDocs.setTypeface(null, android.graphics.Typeface.BOLD);
                indDocs.setBackgroundColor(primaryColor);

                tvTabMedia.setTextColor(textColorSecondary);
                tvTabMedia.setTypeface(null, android.graphics.Typeface.NORMAL);
                indMedia.setBackgroundColor(Color.TRANSPARENT);

                tvTabLinks.setTextColor(textColorSecondary);
                tvTabLinks.setTypeface(null, android.graphics.Typeface.NORMAL);
                indLinks.setBackgroundColor(Color.TRANSPARENT);

                rvMedia.setVisibility(View.GONE);
                rvDocs.setVisibility(docList.isEmpty() ? View.GONE : View.VISIBLE);
                rvLinks.setVisibility(View.GONE);

                layoutEmpty.setVisibility(docList.isEmpty() ? View.VISIBLE : View.GONE);
                tvEmptyText.setText("No documents shared yet");
                ivEmptyIcon.setImageResource(R.drawable.ic_document);
            } else {
                tvTabLinks.setTextColor(primaryColor);
                tvTabLinks.setTypeface(null, android.graphics.Typeface.BOLD);
                indLinks.setBackgroundColor(primaryColor);

                tvTabMedia.setTextColor(textColorSecondary);
                tvTabMedia.setTypeface(null, android.graphics.Typeface.NORMAL);
                indMedia.setBackgroundColor(Color.TRANSPARENT);

                tvTabDocs.setTextColor(textColorSecondary);
                tvTabDocs.setTypeface(null, android.graphics.Typeface.NORMAL);
                indDocs.setBackgroundColor(Color.TRANSPARENT);

                rvMedia.setVisibility(View.GONE);
                rvDocs.setVisibility(View.GONE);
                rvLinks.setVisibility(linkList.isEmpty() ? View.GONE : View.VISIBLE);

                layoutEmpty.setVisibility(linkList.isEmpty() ? View.VISIBLE : View.GONE);
                tvEmptyText.setText("No links shared yet");
                ivEmptyIcon.setImageResource(R.drawable.ic_link);
            }
        };

        tabMedia.setOnClickListener(v -> {
            currentTab[0] = 0;
            updateTabs.run();
        });
        tabDocs.setOnClickListener(v -> {
            currentTab[0] = 1;
            updateTabs.run();
        });
        tabLinks.setOnClickListener(v -> {
            currentTab[0] = 2;
            updateTabs.run();
        });

        updateTabs.run();
        dialog.show();
    }

    private static void toggleMediaSelection(String msgId, Set<String> selected, ImageButton btnDelete) {
        if (selected.contains(msgId)) {
            selected.remove(msgId);
        } else {
            selected.add(msgId);
        }
        if (btnDelete != null) {
            btnDelete.setVisibility(selected.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    // Media Grid Adapter
    private static class MediaGridAdapter extends RecyclerView.Adapter<MediaGridAdapter.Holder> {
        public interface OnMediaItemClickListener {
            void onItemClick(ChatMessage msg);
            void onItemLongClick(ChatMessage msg);
        }

        private final Context context;
        private final List<ChatMessage> items;
        private final Set<String> selectedIds;
        private final OnMediaItemClickListener listener;

        MediaGridAdapter(Context context, List<ChatMessage> items, Set<String> selectedIds, OnMediaItemClickListener listener) {
            this.context = context;
            this.items = items;
            this.selectedIds = selectedIds;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mld_media_grid, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            ChatMessage msg = items.get(position);
            String fullUrl = ChatApiClient.getAbsoluteMediaUrl(context, msg.getMediaUrl());
            boolean isVideo = "video".equalsIgnoreCase(msg.getMediaType()) ||
                    (msg.getMediaUrl() != null && msg.getMediaUrl().toLowerCase().endsWith(".mp4"));

            Glide.with(context)
                    .load(fullUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.bg_search_input)
                    .error(R.drawable.ic_photo_library)
                    .into(holder.ivThumb);

            holder.layoutVideoBadge.setVisibility(isVideo ? View.VISIBLE : View.GONE);

            boolean isSelected = selectedIds.contains(msg.getId());
            holder.viewOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            holder.ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(msg);
            });
            holder.itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onItemLongClick(msg);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            ImageView ivThumb;
            View layoutVideoBadge;
            View viewOverlay;
            ImageView ivCheck;

            Holder(View v) {
                super(v);
                ivThumb = v.findViewById(R.id.iv_grid_thumb);
                layoutVideoBadge = v.findViewById(R.id.layout_grid_video_badge);
                viewOverlay = v.findViewById(R.id.view_grid_select_overlay);
                ivCheck = v.findViewById(R.id.iv_grid_check);
            }
        }
    }

    // Docs Adapter
    private static class DocsAdapter extends RecyclerView.Adapter<DocsAdapter.Holder> {
        public interface OnDocGoToChatListener {
            void onGoToChat(ChatMessage msg);
        }

        private final Context context;
        private final List<ChatMessage> items;
        private final OnDocGoToChatListener listener;

        DocsAdapter(Context context, List<ChatMessage> items, OnDocGoToChatListener listener) {
            this.context = context;
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mld_doc, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            ChatMessage msg = items.get(position);
            holder.tvName.setText(msg.getFileName() != null ? msg.getFileName() : "Document");

            String sizeStr = msg.getFileSize() != null && msg.getFileSize() > 0
                    ? formatSize(msg.getFileSize())
                    : "";
            String info = sizeStr.isEmpty() ? "Document" : sizeStr;
            holder.tvInfo.setText(info);

            holder.itemView.setOnClickListener(v -> {
                try {
                    String fullUrl = ChatApiClient.getAbsoluteMediaUrl(context, msg.getMediaUrl());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(fullUrl));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Cannot open file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            holder.btnGoToChat.setOnClickListener(v -> {
                if (listener != null) listener.onGoToChat(msg);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvName;
            TextView tvInfo;
            TextView btnGoToChat;

            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_doc_name);
                tvInfo = v.findViewById(R.id.tv_doc_info);
                btnGoToChat = v.findViewById(R.id.btn_doc_go_to_chat);
            }
        }
    }

    // Links Adapter
    private static class LinksAdapter extends RecyclerView.Adapter<LinksAdapter.Holder> {
        public interface OnLinkGoToChatListener {
            void onGoToChat(LinkItem item);
        }

        private final Context context;
        private final List<LinkItem> items;
        private final OnLinkGoToChatListener listener;

        LinksAdapter(Context context, List<LinkItem> items, OnLinkGoToChatListener listener) {
            this.context = context;
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mld_link, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            LinkItem item = items.get(position);
            holder.tvUrl.setText(item.url);
            holder.tvSnippet.setText(item.snippet != null ? item.snippet : item.url);

            holder.itemView.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Cannot open browser: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            holder.btnGoToChat.setOnClickListener(v -> {
                if (listener != null) listener.onGoToChat(item);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvUrl;
            TextView tvSnippet;
            TextView btnGoToChat;

            Holder(View v) {
                super(v);
                tvUrl = v.findViewById(R.id.tv_link_url);
                tvSnippet = v.findViewById(R.id.tv_link_snippet);
                btnGoToChat = v.findViewById(R.id.btn_link_go_to_chat);
            }
        }
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
        return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
