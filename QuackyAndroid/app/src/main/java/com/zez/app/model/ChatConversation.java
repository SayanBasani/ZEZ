package com.zez.app.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ChatConversation implements Serializable {
    @SerializedName("conversation_id")
    private String conversationId;

    @SerializedName("other_user_id")
    private String otherUserId;

    @SerializedName("other_user_name")
    private String otherUserName;

    @SerializedName("other_user_phone")
    private String otherUserPhone;

    @SerializedName("other_user_avatar")
    private String otherUserAvatar;

    @SerializedName("other_user_last_seen")
    private String otherUserLastSeen;

    @SerializedName("last_message_text")
    private String lastMessageText;

    @SerializedName("last_message_time")
    private String lastMessageTime;

    @SerializedName("unread_count")
    private int unreadCount;

    public ChatConversation() {}

    public ChatConversation(String conversationId, String otherUserId, String otherUserName, String otherUserPhone, String otherUserAvatar, String otherUserLastSeen, String lastMessageText, String lastMessageTime, int unreadCount) {
        this.conversationId = conversationId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.otherUserPhone = otherUserPhone;
        this.otherUserAvatar = otherUserAvatar;
        this.otherUserLastSeen = otherUserLastSeen;
        this.lastMessageText = lastMessageText;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getOtherUserId() { return otherUserId; }
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }

    public String getOtherUserName() { return otherUserName != null ? otherUserName : "Unknown"; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }

    public String getOtherUserPhone() { return otherUserPhone; }
    public void setOtherUserPhone(String otherUserPhone) { this.otherUserPhone = otherUserPhone; }

    public String getOtherUserAvatar() { return otherUserAvatar; }
    public void setOtherUserAvatar(String otherUserAvatar) { this.otherUserAvatar = otherUserAvatar; }

    public String getOtherUserLastSeen() { return otherUserLastSeen; }
    public void setOtherUserLastSeen(String otherUserLastSeen) { this.otherUserLastSeen = otherUserLastSeen; }

    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }

    public String getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatConversation that = (ChatConversation) o;
        if (conversationId != null && that.conversationId != null) {
            return conversationId.equals(that.conversationId);
        }
        if (otherUserId != null && that.otherUserId != null) {
            return otherUserId.equals(that.otherUserId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (conversationId != null) {
            return conversationId.hashCode();
        }
        if (otherUserId != null) {
            return otherUserId.hashCode();
        }
        return 0;
    }
}
