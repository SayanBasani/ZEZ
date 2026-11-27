package com.zez.app.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zez.app.model.AppCallItem;
import com.zez.app.model.ChatConversation;
import com.zez.app.model.ChatMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatLocalDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "ChatLocalDbHelper";
    private static final String DATABASE_NAME = "zez_chat_local.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_MESSAGES = "local_chat_messages";
    private static final String COL_ID = "id";
    private static final String COL_CONV_ID = "conversation_id";
    private static final String COL_SENDER_ID = "sender_id";
    private static final String COL_RECEIVER_ID = "receiver_id";
    private static final String COL_MSG_TEXT = "message_text";
    private static final String COL_MEDIA_URL = "media_url";
    private static final String COL_LOCAL_URI = "local_uri";
    private static final String COL_MEDIA_TYPE = "media_type";
    private static final String COL_FILE_NAME = "file_name";
    private static final String COL_FILE_SIZE = "file_size";
    private static final String COL_MODE = "mode";
    private static final String COL_SESSION_ID = "secret_session_id";
    private static final String COL_STATUS = "status";
    private static final String COL_REACTION = "reaction";
    private static final String COL_DELETED_EVERYONE = "is_deleted_for_everyone";
    private static final String COL_CREATED_AT = "created_at";

    // Conversations cache table
    private static final String TABLE_CONV = "local_conversations";
    private static final String COL_C_ID = "conversation_id";
    private static final String COL_C_OTHER_ID = "other_user_id";
    private static final String COL_C_OTHER_NAME = "other_user_name";
    private static final String COL_C_OTHER_PHONE = "other_user_phone";
    private static final String COL_C_OTHER_AVATAR = "other_user_avatar";
    private static final String COL_C_OTHER_SEEN = "other_user_last_seen";
    private static final String COL_C_LAST_TEXT = "last_message_text";
    private static final String COL_C_LAST_TIME = "last_message_time";
    private static final String COL_C_UNREAD = "unread_count";

    // In-app calls table
    private static final String TABLE_CALLS = "local_app_calls";
    private static final String COL_K_ID = "id";
    private static final String COL_K_TYPE = "call_type";
    private static final String COL_K_DIRECTION = "direction";
    private static final String COL_K_STATUS = "status";
    private static final String COL_K_PEER_ID = "peer_id";
    private static final String COL_K_PEER_NAME = "peer_name";
    private static final String COL_K_PEER_PHONE = "peer_phone";
    private static final String COL_K_PEER_AVATAR = "peer_avatar";
    private static final String COL_K_STARTED = "started_at";
    private static final String COL_K_DURATION = "duration_seconds";

    private static volatile ChatLocalDbHelper instance;
    private final Context mContext;

    public static synchronized ChatLocalDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ChatLocalDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private ChatLocalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createMessages = "CREATE TABLE " + TABLE_MESSAGES + " ("
                + COL_ID + " TEXT PRIMARY KEY, "
                + COL_CONV_ID + " TEXT, "
                + COL_SENDER_ID + " TEXT, "
                + COL_RECEIVER_ID + " TEXT, "
                + COL_MSG_TEXT + " TEXT, "
                + COL_MEDIA_URL + " TEXT, "
                + COL_LOCAL_URI + " TEXT, "
                + COL_MEDIA_TYPE + " TEXT, "
                + COL_FILE_NAME + " TEXT, "
                + COL_FILE_SIZE + " INTEGER, "
                + COL_MODE + " TEXT, "
                + COL_SESSION_ID + " TEXT, "
                + COL_STATUS + " TEXT, "
                + COL_REACTION + " TEXT, "
                + COL_DELETED_EVERYONE + " INTEGER DEFAULT 0, "
                + COL_CREATED_AT + " TEXT"
                + ");";
        db.execSQL(createMessages);
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_conv_time ON " + TABLE_MESSAGES + "(" + COL_CONV_ID + ", " + COL_CREATED_AT + ");");

        createConversationsTable(db);
        createAppCallsTable(db);
    }

    private void createConversationsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_CONV + " ("
                + COL_C_ID + " TEXT PRIMARY KEY, "
                + COL_C_OTHER_ID + " TEXT, "
                + COL_C_OTHER_NAME + " TEXT, "
                + COL_C_OTHER_PHONE + " TEXT, "
                + COL_C_OTHER_AVATAR + " TEXT, "
                + COL_C_OTHER_SEEN + " TEXT, "
                + COL_C_LAST_TEXT + " TEXT, "
                + COL_C_LAST_TIME + " TEXT, "
                + COL_C_UNREAD + " INTEGER DEFAULT 0"
                + ");";
        db.execSQL(sql);
    }

    private void createAppCallsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_CALLS + " ("
                + COL_K_ID + " TEXT PRIMARY KEY, "
                + COL_K_TYPE + " TEXT, "
                + COL_K_DIRECTION + " TEXT, "
                + COL_K_STATUS + " TEXT, "
                + COL_K_PEER_ID + " TEXT, "
                + COL_K_PEER_NAME + " TEXT, "
                + COL_K_PEER_PHONE + " TEXT, "
                + COL_K_PEER_AVATAR + " TEXT, "
                + COL_K_STARTED + " TEXT, "
                + COL_K_DURATION + " INTEGER DEFAULT 0"
                + ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_LOCAL_URI + " TEXT;");
            } catch (Exception ignored) {}
        }
        if (oldVersion < 3) {
            createConversationsTable(db);
            createAppCallsTable(db);
        }
    }

    public synchronized void saveMessage(ChatMessage msg) {
        if (msg == null || msg.getId() == null) return;
        // Do not persist ephemeral secret messages to local database!
        if (msg.isSecretMode()) return;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = toContentValues(msg);
        db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public synchronized void saveMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (ChatMessage msg : messages) {
                if (msg != null && msg.getId() != null && !msg.isSecretMode()) {
                    ContentValues cv = toContentValues(msg);
                    db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error saving messages batch: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public synchronized List<ChatMessage> getMessages(String conversationId) {
        return getMessages(conversationId, null, 200);
    }

    public synchronized List<ChatMessage> getMessages(String conversationId, int limit) {
        return getMessages(conversationId, null, limit);
    }

    public synchronized List<ChatMessage> getMessages(String conversationId, String otherUserId, int limit) {
        List<ChatMessage> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            String selection;
            String[] args;
            if (conversationId != null && otherUserId != null) {
                selection = "(" + COL_CONV_ID + " = ? OR " + COL_SENDER_ID + " = ? OR " + COL_RECEIVER_ID + " = ?) AND " + COL_MODE + " = 'normal'";
                args = new String[]{conversationId, otherUserId, otherUserId};
            } else if (conversationId != null) {
                selection = COL_CONV_ID + " = ? AND " + COL_MODE + " = 'normal'";
                args = new String[]{conversationId};
            } else if (otherUserId != null) {
                selection = "(" + COL_SENDER_ID + " = ? OR " + COL_RECEIVER_ID + " = ?) AND " + COL_MODE + " = 'normal'";
                args = new String[]{otherUserId, otherUserId};
            } else {
                return list;
            }

            cursor = db.query(
                    TABLE_MESSAGES,
                    null,
                    selection,
                    args,
                    null,
                    null,
                    COL_CREATED_AT + " DESC",
                    String.valueOf(limit > 0 ? limit : 200)
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ChatMessage msg = fromCursor(cursor);
                    list.add(msg);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying messages: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        // Return ascending chronological order
        Collections.reverse(list);
        return list;
    }

    public synchronized String getLatestTimestamp(String conversationId) {
        if (conversationId == null) return null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT " + COL_CREATED_AT + " FROM " + TABLE_MESSAGES
                            + " WHERE " + COL_CONV_ID + " = ? AND " + COL_MODE + " = 'normal' ORDER BY " + COL_CREATED_AT + " DESC LIMIT 1",
                    new String[]{conversationId}
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting latest timestamp: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public synchronized void updateMessageReaction(String messageId, String reaction) {
        if (messageId == null) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_REACTION, reaction);
        db.update(TABLE_MESSAGES, cv, COL_ID + " = ?", new String[]{messageId});
    }

    public synchronized void updateMessageStatus(String messageId, String status) {
        if (messageId == null || status == null) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS, status);
        db.update(TABLE_MESSAGES, cv, COL_ID + " = ?", new String[]{messageId});
    }

    public synchronized void markDeletedForEveryone(String messageId) {
        if (messageId == null) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DELETED_EVERYONE, 1);
        cv.put(COL_MSG_TEXT, "🚫 This message was deleted");
        cv.putNull(COL_MEDIA_URL);
        cv.putNull(COL_REACTION);
        db.update(TABLE_MESSAGES, cv, COL_ID + " = ?", new String[]{messageId});
    }

    public synchronized void deleteMessageForMe(String messageId) {
        if (messageId == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MESSAGES, COL_ID + " = ?", new String[]{messageId});
    }

    public synchronized void clearConversation(String conversationId) {
        if (conversationId == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MESSAGES, COL_CONV_ID + " = ? OR " + COL_SENDER_ID + " = ? OR " + COL_RECEIVER_ID + " = ?",
                new String[]{conversationId, conversationId, conversationId});
    }

    public synchronized void deleteConversation(String conversationId) {
        deleteConversation(conversationId, null);
    }

    public synchronized void deleteConversation(String conversationId, String otherUserId) {
        if (conversationId != null) clearConversation(conversationId);
        if (otherUserId != null) clearConversation(otherUserId);
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (conversationId != null && otherUserId != null) {
                db.delete(TABLE_CONV, COL_C_ID + " = ? OR COL_C_OTHER_ID = ? OR " + COL_C_ID + " = ? OR COL_C_OTHER_ID = ?",
                        new String[]{conversationId, conversationId, otherUserId, otherUserId});
            } else if (conversationId != null) {
                db.delete(TABLE_CONV, COL_C_ID + " = ? OR COL_C_OTHER_ID = ?", new String[]{conversationId, conversationId});
            } else if (otherUserId != null) {
                db.delete(TABLE_CONV, COL_C_ID + " = ? OR COL_C_OTHER_ID = ?", new String[]{otherUserId, otherUserId});
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting conversation: " + e.getMessage());
        }
    }

    public synchronized void clearAllData() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_MESSAGES, null, null);
            db.delete(TABLE_CONV, null, null);
            db.delete(TABLE_CALLS, null, null);
        } catch (Exception ignored) {}
    }

    // ==========================================
    // CONVERSATIONS CACHE METHODS
    // ==========================================

    public synchronized void saveConversations(List<ChatConversation> list) {
        if (list == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Delete existing cache so deleted conversations don't remain in SQLite
            db.delete(TABLE_CONV, null, null);
            for (ChatConversation c : list) {
                if (c == null || c.getConversationId() == null) continue;
                if (mContext != null && ChatPreferencesHelper.isConversationDeleted(mContext, c.getConversationId(), c.getOtherUserId())) {
                    continue;
                }
                ContentValues cv = new ContentValues();
                cv.put(COL_C_ID, c.getConversationId());
                cv.put(COL_C_OTHER_ID, c.getOtherUserId());
                cv.put(COL_C_OTHER_NAME, c.getOtherUserName());
                cv.put(COL_C_OTHER_PHONE, c.getOtherUserPhone());
                cv.put(COL_C_OTHER_AVATAR, c.getOtherUserAvatar());
                cv.put(COL_C_OTHER_SEEN, c.getOtherUserLastSeen());
                cv.put(COL_C_LAST_TEXT, c.getLastMessageText());
                cv.put(COL_C_LAST_TIME, c.getLastMessageTime());
                cv.put(COL_C_UNREAD, c.getUnreadCount());
                db.insertWithOnConflict(TABLE_CONV, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error saving conversations: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public synchronized List<ChatConversation> getConversations() {
        List<ChatConversation> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_CONV, null, null, null, null, null, COL_C_LAST_TIME + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                int iId = cursor.getColumnIndexOrThrow(COL_C_ID);
                int iOId = cursor.getColumnIndexOrThrow(COL_C_OTHER_ID);
                int iOName = cursor.getColumnIndexOrThrow(COL_C_OTHER_NAME);
                int iOPhone = cursor.getColumnIndexOrThrow(COL_C_OTHER_PHONE);
                int iOAvatar = cursor.getColumnIndexOrThrow(COL_C_OTHER_AVATAR);
                int iOSeen = cursor.getColumnIndexOrThrow(COL_C_OTHER_SEEN);
                int iText = cursor.getColumnIndexOrThrow(COL_C_LAST_TEXT);
                int iTime = cursor.getColumnIndexOrThrow(COL_C_LAST_TIME);
                int iUnread = cursor.getColumnIndexOrThrow(COL_C_UNREAD);

                do {
                    String cId = cursor.getString(iId);
                    String otherId = cursor.getString(iOId);
                    if (mContext != null && ChatPreferencesHelper.isConversationDeleted(mContext, cId, otherId)) {
                        continue;
                    }
                    ChatConversation c = new ChatConversation(
                            cId,
                            otherId,
                            cursor.getString(iOName),
                            cursor.getString(iOPhone),
                            cursor.getString(iOAvatar),
                            cursor.getString(iOSeen),
                            cursor.getString(iText),
                            cursor.getString(iTime),
                            cursor.getInt(iUnread)
                    );
                    list.add(c);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading cached conversations: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // ==========================================
    // IN-APP CALL HISTORY METHODS
    // ==========================================

    public synchronized void saveAppCalls(List<AppCallItem> calls) {
        if (calls == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (AppCallItem item : calls) {
                if (item == null || item.getId() == null) continue;
                ContentValues cv = new ContentValues();
                cv.put(COL_K_ID, item.getId());
                cv.put(COL_K_TYPE, item.getCallType());
                cv.put(COL_K_DIRECTION, item.getDirection());
                cv.put(COL_K_STATUS, item.getStatus());
                cv.put(COL_K_PEER_ID, item.getPeerId());
                cv.put(COL_K_PEER_NAME, item.getPeerName());
                cv.put(COL_K_PEER_PHONE, item.getPeerPhone());
                cv.put(COL_K_PEER_AVATAR, item.getPeerAvatar());
                cv.put(COL_K_STARTED, item.getStartedAt());
                cv.put(COL_K_DURATION, item.getDurationSeconds());
                db.insertWithOnConflict(TABLE_CALLS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error saving app calls: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public synchronized List<AppCallItem> getAppCalls() {
        List<AppCallItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_CALLS, null, null, null, null, null, COL_K_STARTED + " DESC", "100");
            if (cursor != null && cursor.moveToFirst()) {
                int iId = cursor.getColumnIndexOrThrow(COL_K_ID);
                int iType = cursor.getColumnIndexOrThrow(COL_K_TYPE);
                int iDir = cursor.getColumnIndexOrThrow(COL_K_DIRECTION);
                int iStat = cursor.getColumnIndexOrThrow(COL_K_STATUS);
                int iPId = cursor.getColumnIndexOrThrow(COL_K_PEER_ID);
                int iPName = cursor.getColumnIndexOrThrow(COL_K_PEER_NAME);
                int iPPhone = cursor.getColumnIndexOrThrow(COL_K_PEER_PHONE);
                int iPAvatar = cursor.getColumnIndexOrThrow(COL_K_PEER_AVATAR);
                int iStart = cursor.getColumnIndexOrThrow(COL_K_STARTED);
                int iDur = cursor.getColumnIndexOrThrow(COL_K_DURATION);

                do {
                    AppCallItem item = new AppCallItem(
                            cursor.getString(iId),
                            cursor.getString(iType),
                            cursor.getString(iDir),
                            cursor.getString(iStat),
                            cursor.getString(iPId),
                            cursor.getString(iPName),
                            cursor.getString(iPPhone),
                            cursor.getString(iPAvatar),
                            cursor.getString(iStart),
                            cursor.getInt(iDur)
                    );
                    list.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading cached app calls: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    private ContentValues toContentValues(ChatMessage msg) {
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, msg.getId());
        cv.put(COL_CONV_ID, msg.getConversationId());
        cv.put(COL_SENDER_ID, msg.getSenderId());
        cv.put(COL_RECEIVER_ID, msg.getReceiverId());
        cv.put(COL_MSG_TEXT, msg.getMessageText());
        cv.put(COL_MEDIA_URL, msg.getMediaUrl());
        cv.put(COL_LOCAL_URI, msg.getLocalUri());
        cv.put(COL_MEDIA_TYPE, msg.getMediaType());
        cv.put(COL_FILE_NAME, msg.getFileName());
        if (msg.getFileSize() != null) cv.put(COL_FILE_SIZE, msg.getFileSize());
        cv.put(COL_MODE, msg.getMode());
        cv.put(COL_SESSION_ID, msg.getSecretSessionId());
        cv.put(COL_STATUS, msg.getStatus());
        cv.put(COL_REACTION, msg.getReaction());
        cv.put(COL_DELETED_EVERYONE, msg.isDeletedForEveryone() ? 1 : 0);
        cv.put(COL_CREATED_AT, msg.getCreatedAt());
        return cv;
    }

    private ChatMessage fromCursor(Cursor cursor) {
        ChatMessage msg = new ChatMessage();
        msg.setId(cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)));
        msg.setConversationId(cursor.getString(cursor.getColumnIndexOrThrow(COL_CONV_ID)));
        msg.setSenderId(cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER_ID)));
        msg.setReceiverId(cursor.getString(cursor.getColumnIndexOrThrow(COL_RECEIVER_ID)));
        msg.setMessageText(cursor.getString(cursor.getColumnIndexOrThrow(COL_MSG_TEXT)));
        msg.setMediaUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDIA_URL)));
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(COL_LOCAL_URI))) {
            msg.setLocalUri(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCAL_URI)));
        }
        msg.setMediaType(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDIA_TYPE)));
        msg.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_NAME)));
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(COL_FILE_SIZE))) {
            msg.setFileSize(cursor.getLong(cursor.getColumnIndexOrThrow(COL_FILE_SIZE)));
        }
        msg.setMode(cursor.getString(cursor.getColumnIndexOrThrow(COL_MODE)));
        msg.setSecretSessionId(cursor.getString(cursor.getColumnIndexOrThrow(COL_SESSION_ID)));
        msg.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        msg.setReaction(cursor.getString(cursor.getColumnIndexOrThrow(COL_REACTION)));
        msg.setDeletedForEveryone(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DELETED_EVERYONE)) == 1);
        msg.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
        return msg;
    }
}
