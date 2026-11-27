package com.zez.manager.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.zez.manager.model.CallLogItem;

import java.util.ArrayList;
import java.util.List;

public class CallLogHelper {
    private static final String TAG = "ZezCallLogHelper";

    public static boolean hasCallLogPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static List<CallLogItem> getAllCallLogs(Context context) {
        return getCallLogs(context, 0);
    }

    public static List<CallLogItem> getCallLogs(Context context, int limit) {
        List<CallLogItem> list = new ArrayList<>();
        if (!hasCallLogPermission(context)) {
            return list;
        }

        boolean hasContacts = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;

        Uri queryUri = CallLog.Calls.CONTENT_URI;
        String[] projection = new String[]{
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
        };
        String sortOrder = CallLog.Calls.DATE + " DESC";

        try (Cursor cursor = context.getContentResolver().query(queryUri, projection, null, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex(CallLog.Calls._ID);
                int nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
                int numberIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE);
                int durIdx = cursor.getColumnIndex(CallLog.Calls.DURATION);

                int count = 0;
                do {
                    String id = idIdx >= 0 ? cursor.getString(idIdx) : String.valueOf(System.currentTimeMillis() + count);
                    String name = nameIdx >= 0 ? cursor.getString(nameIdx) : "";
                    String number = numberIdx >= 0 ? cursor.getString(numberIdx) : "";
                    int typeCode = typeIdx >= 0 ? cursor.getInt(typeIdx) : 0;
                    long date = dateIdx >= 0 ? cursor.getLong(dateIdx) : System.currentTimeMillis();
                    long duration = durIdx >= 0 ? cursor.getLong(durIdx) : 0L;

                    if ((name == null || name.trim().isEmpty()) && hasContacts && number != null && !number.trim().isEmpty()) {
                        name = resolveContactName(context, number.trim());
                    }

                    String typeStr = mapCallType(typeCode);
                    list.add(new CallLogItem(id, name != null ? name : "", number != null ? number : "", typeStr, date, duration));
                    count++;
                    if (limit > 0 && count >= limit) break;
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to query CallLog.Calls: " + e.getMessage());
        }

        return list;
    }

    private static String mapCallType(int typeCode) {
        switch (typeCode) {
            case CallLog.Calls.INCOMING_TYPE: return "INCOMING";
            case CallLog.Calls.OUTGOING_TYPE: return "OUTGOING";
            case CallLog.Calls.MISSED_TYPE: return "MISSED";
            case CallLog.Calls.VOICEMAIL_TYPE: return "VOICEMAIL";
            case CallLog.Calls.REJECTED_TYPE: return "REJECTED";
            case CallLog.Calls.BLOCKED_TYPE: return "BLOCKED";
            default: return "UNKNOWN";
        }
    }

    private static String resolveContactName(Context context, String phoneNumber) {
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            try (Cursor c = context.getContentResolver().query(uri,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    String display = c.getString(0);
                    return display != null ? display : "";
                }
            }
        } catch (Exception ignored) {}
        return "";
    }
}
