package com.zez.app.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ChatMessage implements Serializable {
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_READ = "read";

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

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    public ChatMessage() {}

    public ChatMessage(String id, String conversationId, String senderId, String receiverId, String messageText, String mediaUrl, String status, String createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.mediaUrl = mediaUrl;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getStatus() { return status != null ? status : STATUS_SENT; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
