package com.zez.app.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zez.app.model.ChatMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatLocalDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "ChatLocalDbHelper";
    private static final String DATABASE_NAME = "zez_chat_local.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGES = "local_chat_messages";
    private static final String COL_ID = "id";
    private static final String COL_CONV_ID = "conversation_id";
    private static final String COL_SENDER_ID = "sender_id";
    private static final String COL_RECEIVER_ID = "receiver_id";
    private static final String COL_MSG_TEXT = "message_text";
    private static final String COL_MEDIA_URL = "media_url";
    private static final String COL_MEDIA_TYPE = "media_type";
    private static final String COL_FILE_NAME = "file_name";
    private static final String COL_FILE_SIZE = "file_size";
    private static final String COL_MODE = "mode";
    private static final String COL_SESSION_ID = "secret_session_id";
    private static final String COL_STATUS = "status";
    private static final String COL_REACTION = "reaction";
    private static final String COL_DELETED_EVERYONE = "is_deleted_for_everyone";
    private static final String COL_CREATED_AT = "created_at";

    private static volatile ChatLocalDbHelper instance;

    public static synchronized ChatLocalDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ChatLocalDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private ChatLocalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_MESSAGES + " ("
                + COL_ID + " TEXT PRIMARY KEY, "
                + COL_CONV_ID + " TEXT, "
                + COL_SENDER_ID + " TEXT, "
                + COL_RECEIVER_ID + " TEXT, "
                + COL_MSG_TEXT + " TEXT, "
                + COL_MEDIA_URL + " TEXT, "
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
        db.execSQL(createTable);
        db.execSQL("CREATE INDEX idx_conv_time ON " + TABLE_MESSAGES + "(" + COL_CONV_ID + ", " + COL_CREATED_AT + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
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
        return getMessages(conversationId, 200);
    }

    public synchronized List<ChatMessage> getMessages(String conversationId, int limit) {
        List<ChatMessage> list = new ArrayList<>();
        if (conversationId == null) return list;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_MESSAGES,
                    null,
                    COL_CONV_ID + " = ? AND " + COL_MODE + " = 'normal'",
                    new String[]{conversationId},
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
        db.delete(TABLE_MESSAGES, COL_CONV_ID + " = ?", new String[]{conversationId});
    }

    public synchronized void deleteConversation(String conversationId) {
        clearConversation(conversationId);
    }

    private ContentValues toContentValues(ChatMessage msg) {
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, msg.getId());
        cv.put(COL_CONV_ID, msg.getConversationId());
        cv.put(COL_SENDER_ID, msg.getSenderId());
        cv.put(COL_RECEIVER_ID, msg.getReceiverId());
        cv.put(COL_MSG_TEXT, msg.getMessageText());
        cv.put(COL_MEDIA_URL, msg.getMediaUrl());
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
