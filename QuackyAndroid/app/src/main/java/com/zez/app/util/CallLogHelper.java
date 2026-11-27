package com.zez.app.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

import com.zez.app.model.CallLogItem;

import java.util.ArrayList;
import java.util.List;

public class CallLogHelper {
    private static final String TAG = "CallLogHelper";

    public static List<CallLogItem> getAllCallLogs(Context context) {
        return getCallLogs(context, 0);
    }

    public static CallLogItem getLastCallLog(Context context) {
        List<CallLogItem> list = getCallLogs(context, 1);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static List<CallLogItem> getCallLogs(Context context, int limit) {
        List<CallLogItem> list = new ArrayList<>();
        Uri queryUri = CallLog.Calls.CONTENT_URI;
        String[] projection = new String[] {
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
                int durationIdx = cursor.getColumnIndex(CallLog.Calls.DURATION);

                int count = 0;
                do {
                    String id = idIdx >= 0 ? cursor.getString(idIdx) : String.valueOf(System.currentTimeMillis() + count);
                    String name = nameIdx >= 0 ? cursor.getString(nameIdx) : "Unknown";
                    String number = numberIdx >= 0 ? cursor.getString(numberIdx) : "";
                    int typeCode = typeIdx >= 0 ? cursor.getInt(typeIdx) : 0;
                    long date = dateIdx >= 0 ? cursor.getLong(dateIdx) : System.currentTimeMillis();
                    long duration = durationIdx >= 0 ? cursor.getLong(durationIdx) : 0;

                    String typeStr = "UNKNOWN";
                    switch (typeCode) {
                        case CallLog.Calls.INCOMING_TYPE: typeStr = "INCOMING"; break;
                        case CallLog.Calls.OUTGOING_TYPE: typeStr = "OUTGOING"; break;
                        case CallLog.Calls.MISSED_TYPE: typeStr = "MISSED"; break;
                        case CallLog.Calls.VOICEMAIL_TYPE: typeStr = "VOICEMAIL"; break;
                        case CallLog.Calls.REJECTED_TYPE: typeStr = "REJECTED"; break;
                        case CallLog.Calls.BLOCKED_TYPE: typeStr = "BLOCKED"; break;
                    }

                    list.add(new CallLogItem(id, name, number, typeStr, date, duration));
                    count++;
                    if (limit > 0 && count >= limit) {
                        break;
                    }
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "Successfully read " + list.size() + " calls from CallLog provider");
        } catch (Exception e) {
            Log.e(TAG, "Error querying CallLog provider", e);
        }

        return list;
    }
}
