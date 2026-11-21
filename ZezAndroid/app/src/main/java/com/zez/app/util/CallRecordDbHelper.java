package com.zez.app.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zez.app.model.CallRecordItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CallRecordDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "CallRecordDbHelper";
    private static final String DATABASE_NAME = "twing_call_recordings.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_RECORDINGS = "call_recordings";
    private static final String COL_ID = "id";
    private static final String COL_PHONE_NUMBER = "phone_number";
    private static final String COL_CONTACT_NAME = "contact_name";
    private static final String COL_CALL_TYPE = "call_type";
    private static final String COL_DIRECTION = "direction";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_DURATION_SECONDS = "duration_seconds";
    private static final String COL_FILE_PATH = "file_path";
    private static final String COL_FILE_SIZE = "file_size";
    private static final String COL_CLOUD_URL = "cloud_url";
    private static final String COL_IS_SYNCED = "is_synced";

    private static volatile CallRecordDbHelper instance;

    public static synchronized CallRecordDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CallRecordDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private CallRecordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSql = "CREATE TABLE " + TABLE_RECORDINGS + " ("
                + COL_ID + " TEXT PRIMARY KEY, "
                + COL_PHONE_NUMBER + " TEXT, "
                + COL_CONTACT_NAME + " TEXT, "
                + COL_CALL_TYPE + " TEXT, "
                + COL_DIRECTION + " TEXT, "
                + COL_TIMESTAMP + " INTEGER, "
                + COL_DURATION_SECONDS + " INTEGER, "
                + COL_FILE_PATH + " TEXT, "
                + COL_FILE_SIZE + " INTEGER, "
                + COL_CLOUD_URL + " TEXT, "
                + COL_IS_SYNCED + " INTEGER DEFAULT 0"
                + ");";
        db.execSQL(createSql);
        db.execSQL("CREATE INDEX idx_rec_time ON " + TABLE_RECORDINGS + "(" + COL_TIMESTAMP + " DESC);");
        db.execSQL("CREATE INDEX idx_rec_phone ON " + TABLE_RECORDINGS + "(" + COL_PHONE_NUMBER + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_RECORDINGS + " ADD COLUMN " + COL_CLOUD_URL + " TEXT;");
                db.execSQL("ALTER TABLE " + TABLE_RECORDINGS + " ADD COLUMN " + COL_IS_SYNCED + " INTEGER DEFAULT 0;");
            } catch (Exception ignored) {}
        }
    }

    public synchronized void insertRecord(CallRecordItem item) {
        if (item == null || item.getId() == null) return;
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_ID, item.getId());
            cv.put(COL_PHONE_NUMBER, item.getPhoneNumber());
            cv.put(COL_CONTACT_NAME, item.getContactName());
            cv.put(COL_CALL_TYPE, item.getCallType());
            cv.put(COL_DIRECTION, item.getDirection());
            cv.put(COL_TIMESTAMP, item.getTimestamp());
            cv.put(COL_DURATION_SECONDS, item.getDurationSeconds());
            cv.put(COL_FILE_PATH, item.getFilePath());
            cv.put(COL_FILE_SIZE, item.getFileSize());
            cv.put(COL_CLOUD_URL, item.getCloudUrl());
            cv.put(COL_IS_SYNCED, item.isSynced() ? 1 : 0);
            db.insertWithOnConflict(TABLE_RECORDINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting recording: " + e.getMessage(), e);
        }
    }

    public synchronized void updateDurationAndSize(String id, int durationSeconds, long fileSize) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_DURATION_SECONDS, durationSeconds);
            cv.put(COL_FILE_SIZE, fileSize);
            db.update(TABLE_RECORDINGS, cv, COL_ID + " = ?", new String[]{id});
        } catch (Exception e) {
            Log.e(TAG, "Error updating recording: " + e.getMessage(), e);
        }
    }

    public synchronized void updateContactAndPhone(String id, String phoneNumber, String contactName) {
        if (id == null) return;
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                cv.put(COL_PHONE_NUMBER, phoneNumber.trim());
            }
            if (contactName != null && !contactName.trim().isEmpty()) {
                cv.put(COL_CONTACT_NAME, contactName.trim());
            }
            if (cv.size() > 0) {
                db.update(TABLE_RECORDINGS, cv, COL_ID + " = ?", new String[]{id});
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating contact & phone: " + e.getMessage(), e);
        }
    }

    public synchronized List<CallRecordItem> getAllRecordings() {
        List<CallRecordItem> list = new ArrayList<>();
        try {
            SQLiteDatabase db = getReadableDatabase();
            try (Cursor cursor = db.query(TABLE_RECORDINGS, null, null, null, null, null, COL_TIMESTAMP + " DESC")) {
                while (cursor != null && cursor.moveToNext()) {
                    CallRecordItem item = cursorToItem(cursor);
                    // Only include if file actually exists on device
                    if (item != null && item.fileExists()) {
                        list.add(item);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all recordings: " + e.getMessage(), e);
        }
        return list;
    }

    public synchronized List<CallRecordItem> getRecordingsForNumber(String numberOrId) {
        List<CallRecordItem> list = new ArrayList<>();
        if (numberOrId == null || numberOrId.trim().isEmpty()) return list;
        try {
            SQLiteDatabase db = getReadableDatabase();
            String cleanNumber = numberOrId.replaceAll("[^0-9]", "");
            String query = COL_PHONE_NUMBER + " = ? OR " + COL_PHONE_NUMBER + " LIKE ?";
            String[] args = new String[]{numberOrId, "%" + (cleanNumber.length() >= 6 ? cleanNumber.substring(cleanNumber.length() - 6) : cleanNumber) + "%"};

            try (Cursor cursor = db.query(TABLE_RECORDINGS, null, query, args, null, null, COL_TIMESTAMP + " DESC")) {
                while (cursor != null && cursor.moveToNext()) {
                    CallRecordItem item = cursorToItem(cursor);
                    if (item != null && item.fileExists()) {
                        list.add(item);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting recordings for number: " + e.getMessage(), e);
        }
        return list;
    }

    public synchronized boolean deleteRecording(String id) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            // Fetch file path first to delete file on disk
            try (Cursor cursor = db.query(TABLE_RECORDINGS, new String[]{COL_FILE_PATH}, COL_ID + " = ?", new String[]{id}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String path = cursor.getString(0);
                    if (path != null) {
                        File f = new File(path);
                        if (f.exists()) f.delete();
                    }
                }
            }
            int deleted = db.delete(TABLE_RECORDINGS, COL_ID + " = ?", new String[]{id});
            return deleted > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting recording: " + e.getMessage(), e);
            return false;
        }
    }

    public synchronized int getRecordingsCount() {
        try {
            SQLiteDatabase db = getReadableDatabase();
            try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECORDINGS, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getInt(0);
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public synchronized long getTotalSizeBytes() {
        try {
            SQLiteDatabase db = getReadableDatabase();
            try (Cursor cursor = db.rawQuery("SELECT SUM(" + COL_FILE_SIZE + ") FROM " + TABLE_RECORDINGS, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getLong(0);
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public synchronized void markRecordSynced(String id, String cloudUrl) {
        if (id == null) return;
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_CLOUD_URL, cloudUrl);
            cv.put(COL_IS_SYNCED, 1);
            db.update(TABLE_RECORDINGS, cv, COL_ID + " = ?", new String[]{id});
        } catch (Exception e) {
            Log.e(TAG, "Error marking recording synced: " + e.getMessage(), e);
        }
    }

    public synchronized List<CallRecordItem> getUnsyncedRecords() {
        List<CallRecordItem> list = new ArrayList<>();
        try {
            SQLiteDatabase db = getReadableDatabase();
            try (Cursor cursor = db.query(TABLE_RECORDINGS, null, COL_IS_SYNCED + " = 0", null, null, null, COL_TIMESTAMP + " ASC")) {
                while (cursor != null && cursor.moveToNext()) {
                    CallRecordItem item = cursorToItem(cursor);
                    if (item != null && item.fileExists()) {
                        list.add(item);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting unsynced records: " + e.getMessage(), e);
        }
        return list;
    }

    private CallRecordItem cursorToItem(Cursor c) {
        String id = c.getString(c.getColumnIndexOrThrow(COL_ID));
        String phone = c.getString(c.getColumnIndexOrThrow(COL_PHONE_NUMBER));
        String name = c.getString(c.getColumnIndexOrThrow(COL_CONTACT_NAME));
        String type = c.getString(c.getColumnIndexOrThrow(COL_CALL_TYPE));
        String direction = c.getString(c.getColumnIndexOrThrow(COL_DIRECTION));
        long time = c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP));
        int duration = c.getInt(c.getColumnIndexOrThrow(COL_DURATION_SECONDS));
        String path = c.getString(c.getColumnIndexOrThrow(COL_FILE_PATH));
        long size = c.getLong(c.getColumnIndexOrThrow(COL_FILE_SIZE));
        String cloudUrl = null;
        int cloudCol = c.getColumnIndex(COL_CLOUD_URL);
        if (cloudCol != -1 && !c.isNull(cloudCol)) {
            cloudUrl = c.getString(cloudCol);
        }
        boolean isSynced = false;
        int syncCol = c.getColumnIndex(COL_IS_SYNCED);
        if (syncCol != -1 && !c.isNull(syncCol)) {
            isSynced = c.getInt(syncCol) == 1;
        }
        return new CallRecordItem(id, phone, name, type, direction, time, duration, path, size, cloudUrl, isSynced);
    }
}
