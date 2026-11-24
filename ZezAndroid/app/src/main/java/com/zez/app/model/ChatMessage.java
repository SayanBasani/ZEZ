package com.zez.app.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ChatMessage implements Serializable {
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_READ = "read";
    public static final String STATUS_FAILED = "failed";

    public static final String MODE_NORMAL = "normal";
    public static final String MODE_SECRET = "secret";

    @SerializedName("id")
    private String id;

    @SerializedName("conversation_id")
    private String conversationId;

    @SerializedName("sender_id")
    private String senderId;

    @SerializedName("receiver_id")
    private String receiverId;

    @SerializedName("message_text")
    private String messageText;

    @SerializedName("media_url")
    private String mediaUrl;

    @SerializedName("local_uri")
    private String localUri;

    @SerializedName("media_type")
    private String mediaType;

    @SerializedName("file_name")
    private String fileName;

    @SerializedName("file_size")
    private Long fileSize;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("sender_phone")
    private String senderPhone;

    @SerializedName("mode")
    private String mode;

    @SerializedName("secret_session_id")
    private String secretSessionId;

    @SerializedName("status")
    private String status;

    @SerializedName("reaction")
    private String reaction;

    @SerializedName("is_deleted_for_everyone")
    private Boolean isDeletedForEveryone;

    @SerializedName("created_at")
    private String createdAt;

    public ChatMessage() {}

    public ChatMessage(String id, String conversationId, String senderId, String receiverId, String messageText, String mediaUrl, String mediaType, String fileName, Long fileSize, String status, String createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.status = status;
        this.createdAt = createdAt;
    }

    public ChatMessage(String id, String conversationId, String senderId, String receiverId, String messageText, String mediaUrl, String status, String createdAt) {
        this(id, conversationId, senderId, receiverId, messageText, mediaUrl, null, null, null, status, createdAt);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getLocalUri() { return localUri; }
    public void setLocalUri(String localUri) { this.localUri = localUri; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }

    public String getMode() { return mode != null ? mode : MODE_NORMAL; }
    public void setMode(String mode) { this.mode = mode; }
    public boolean isSecretMode() { return MODE_SECRET.equalsIgnoreCase(this.mode); }

    public String getSecretSessionId() { return secretSessionId; }
    public void setSecretSessionId(String secretSessionId) { this.secretSessionId = secretSessionId; }

    public String getStatus() { return status != null ? status : STATUS_SENT; }
    public void setStatus(String status) { this.status = status; }

    public String getReaction() { return reaction; }
    public void setReaction(String reaction) { this.reaction = reaction; }

    public boolean isDeletedForEveryone() { return Boolean.TRUE.equals(isDeletedForEveryone); }
    public void setDeletedForEveryone(Boolean deletedForEveryone) { this.isDeletedForEveryone = deletedForEveryone; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public static String formatReply(String sender, String quotedSnippet, String actualText) {
        return formatReply(sender, quotedSnippet, actualText, null);
    }

    public static String formatReply(String sender, String quotedSnippet, String actualText, String quotedMediaUrl) {
        String cleanSender = sender != null ? sender.replace("|", "/").replace("]", ")").trim() : "Contact";
        String cleanSnippet = quotedSnippet != null ? quotedSnippet.replace("\n", " ").replace("|", "/").replace("]", ")").trim() : "";
        if (cleanSnippet.length() > 100) {
            cleanSnippet = cleanSnippet.substring(0, 97) + "...";
        }
        String mediaPart = (quotedMediaUrl != null && !quotedMediaUrl.trim().isEmpty()) ? "|" + quotedMediaUrl.trim().replace("]", ")") : "";
        return "[reply:" + cleanSender + "|" + cleanSnippet + mediaPart + "]\n" + (actualText != null ? actualText : "");
    }

    public boolean hasQuotedReply() {
        return messageText != null && messageText.startsWith("[reply:") && messageText.contains("]\n");
    }

    public String getQuotedSender() {
        if (!hasQuotedReply()) return null;
        try {
            int pipeIdx = messageText.indexOf('|');
            if (pipeIdx > 7) {
                return messageText.substring(7, pipeIdx).trim();
            }
        } catch (Exception ignored) {}
        return "Reply";
    }

    public String getQuotedText() {
        if (!hasQuotedReply()) return null;
        try {
            int firstPipe = messageText.indexOf('|');
            int closeIdx = messageText.indexOf("]\n");
            if (firstPipe != -1 && closeIdx > firstPipe) {
                int secondPipe = messageText.indexOf('|', firstPipe + 1);
                int endIdx = (secondPipe != -1 && secondPipe < closeIdx) ? secondPipe : closeIdx;
                String snip = messageText.substring(firstPipe + 1, endIdx).trim();
                if (!snip.isEmpty()) return snip;
            }
        } catch (Exception ignored) {}
        if (getQuotedMediaUrl() != null) {
            String q = getQuotedMediaUrl().toLowerCase();
            if (q.contains(".mp4") || q.contains("video")) return "🎥 Video";
            return "📷 Photo";
        }
        return "Message";
    }

    public String getQuotedMediaUrl() {
        if (!hasQuotedReply()) return null;
        try {
            int firstPipe = messageText.indexOf('|');
            int closeIdx = messageText.indexOf("]\n");
            if (firstPipe != -1 && closeIdx > firstPipe) {
                int secondPipe = messageText.indexOf('|', firstPipe + 1);
                if (secondPipe != -1 && secondPipe < closeIdx) {
                    String url = messageText.substring(secondPipe + 1, closeIdx).trim();
                    if (!url.isEmpty()) return url;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public String getCleanMessageText() {
        if (!hasQuotedReply()) return messageText != null ? messageText : "";
        try {
            int closeIdx = messageText.indexOf("]\n");
            if (closeIdx != -1) {
                return messageText.substring(closeIdx + 2);
            }
        } catch (Exception ignored) {}
        return messageText != null ? messageText : "";
    }

    public String getDisplaySnippet() {
        String clean = getCleanMessageText();
        if (clean != null && !clean.trim().isEmpty()) {
            return clean.replace("\n", " ").trim();
        }
        String type = mediaType;
        if (type == null && mediaUrl != null) {
            String lower = mediaUrl.toLowerCase();
            if (lower.contains(".jpg") || lower.contains(".jpeg") || lower.contains(".png") || lower.contains(".webp") || lower.contains(".gif") || lower.contains("chat-media") || lower.contains("avatars")) {
                type = "image";
            } else if (lower.contains(".mp4") || lower.contains(".mkv") || lower.contains(".mov") || lower.contains(".3gp") || lower.contains("videos")) {
                type = "video";
            } else {
                type = "file";
            }
        }
        if (type != null) {
            if ("image".equalsIgnoreCase(type)) return "📷 Photo";
            if ("video".equalsIgnoreCase(type)) return "🎥 Video";
            return "📎 File";
        }
        if (fileName != null && !fileName.isEmpty()) return "📄 " + fileName;
        return "Message";
    }
}
