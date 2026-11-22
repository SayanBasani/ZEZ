package com.zez.app.recording.importing;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.zez.app.model.CallLogItem;
import com.zez.app.util.CallLogHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Correlates imported OEM audio files against Android system CallLog entries.
 * Evaluates filename timestamps, phone numbers, contact names, and file modification dates
 * using a configurable ±90 second correlation window.
 */
public class RecordingMatcher {
    private static final String TAG = "RecordingMatcher";
    private static final long DEFAULT_MATCH_WINDOW_MS = 90_000L; // ±90 seconds

    public static class MatchedCallInfo {
        public final String phoneNumber;
        public final String contactName;
        public final String direction;
        public final int durationSeconds;
        public final long callTimestamp;
        public final boolean isConfidentMatch;

        public MatchedCallInfo(String phoneNumber, String contactName, String direction,
                               int durationSeconds, long callTimestamp, boolean isConfidentMatch) {
            this.phoneNumber = (phoneNumber != null && !phoneNumber.trim().isEmpty()) ? phoneNumber.trim() : "Unknown";
            this.contactName = (contactName != null && !contactName.trim().isEmpty()) ? contactName.trim() : this.phoneNumber;
            this.direction = (direction != null && !direction.trim().isEmpty()) ? direction : "INCOMING";
            this.durationSeconds = Math.max(durationSeconds, 1);
            this.callTimestamp = (callTimestamp > 0) ? callTimestamp : System.currentTimeMillis();
            this.isConfidentMatch = isConfidentMatch;
        }
    }

    /**
     * Matches an imported audio file to a call in Android's CallLog.
     *
     * @param context       Application context
     * @param filename      The filename of the recording
     * @param fileModified  File last-modified timestamp in milliseconds
     * @param localFilePath Absolute path of the imported audio file (for audio duration extraction)
     * @return MatchedCallInfo containing resolved contact, phone, direction, and duration
     */
    public static MatchedCallInfo matchCall(Context context, String filename, long fileModified, String localFilePath) {
        long extractedTimestamp = parseTimestampFromFilename(filename);
        String extractedPhone = parsePhoneNumberFromFilename(filename);
        String extractedName = parseContactNameFromFilename(filename);

        long targetTime = (extractedTimestamp > 0) ? extractedTimestamp : fileModified;
        if (targetTime <= 0) {
            targetTime = System.currentTimeMillis();
        }

        int audioDurationSec = extractAudioDurationSeconds(localFilePath);

        List<CallLogItem> callLogs = null;
        try {
            callLogs = CallLogHelper.getAllCallLogs(context);
        } catch (Exception e) {
            Log.w(TAG, "Failed to read CallLog for matching: " + e.getMessage());
        }

        if (callLogs != null && !callLogs.isEmpty()) {
            CallLogItem bestMatch = null;
            long smallestTimeDiff = Long.MAX_VALUE;

            for (CallLogItem call : callLogs) {
                long callTime = call.getDate();
                long diff = Math.abs(callTime - targetTime);

                if (diff <= DEFAULT_MATCH_WINDOW_MS) {
                    // Check if phone number matches as well
                    if (extractedPhone != null && !extractedPhone.isEmpty()) {
                        String cleanCall = cleanNumber(call.getPhoneNumber());
                        String cleanExtracted = cleanNumber(extractedPhone);
                        if (cleanCall.contains(cleanExtracted) || cleanExtracted.contains(cleanCall)) {
                            // Exact phone & timestamp match!
                            bestMatch = call;
                            break;
                        }
                    }

                    if (diff < smallestTimeDiff) {
                        smallestTimeDiff = diff;
                        bestMatch = call;
                    }
                }
            }

            if (bestMatch != null) {
                String phone = bestMatch.getPhoneNumber();
                String name = bestMatch.getName();
                if (name == null || name.trim().isEmpty() || "Unknown".equalsIgnoreCase(name)) {
                    name = (extractedName != null && !extractedName.isEmpty()) ? extractedName : phone;
                }
                String dir = ("INCOMING".equalsIgnoreCase(bestMatch.getType())) ? "INCOMING" : "OUTGOING";
                int dur = (audioDurationSec > 0) ? (int) audioDurationSec : (int) bestMatch.getDuration();

                Log.d(TAG, "Confident match found in CallLog: " + name + " (" + phone + "), Time diff: " + smallestTimeDiff + "ms");
                return new MatchedCallInfo(phone, name, dir, dur, bestMatch.getDate(), true);
            }
        }

        // Fallback when no direct CallLog match exists in window
        String fallbackPhone = (extractedPhone != null && !extractedPhone.isEmpty()) ? extractedPhone : "Unknown Number";
        String fallbackName = (extractedName != null && !extractedName.isEmpty()) ? extractedName : fallbackPhone;
        int finalDur = (audioDurationSec > 0) ? audioDurationSec : 1;

        Log.d(TAG, "No CallLog match in window. Using filename metadata fallback: " + fallbackName);
        return new MatchedCallInfo(fallbackPhone, fallbackName, "INCOMING", finalDur, targetTime, false);
    }

    private static long parseTimestampFromFilename(String filename) {
        if (filename == null) return 0;

        // Pattern 1: yyyyMMddHHmmss (e.g. 20261122114500)
        Pattern p1 = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})_?(\\d{2})(\\d{2})(\\d{2})");
        Matcher m1 = p1.matcher(filename);
        if (m1.find()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                String dateStr = m1.group(1) + m1.group(2) + m1.group(3) + m1.group(4) + m1.group(5) + m1.group(6);
                Date d = sdf.parse(dateStr);
                if (d != null) return d.getTime();
            } catch (Exception ignored) {}
        }

        // Pattern 2: yyyy-MM-dd-HH-mm-ss
        Pattern p2 = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})");
        Matcher m2 = p2.matcher(filename);
        if (m2.find()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
                Date d = sdf.parse(m2.group(0));
                if (d != null) return d.getTime();
            } catch (Exception ignored) {}
        }

        return 0;
    }

    private static String parsePhoneNumberFromFilename(String filename) {
        if (filename == null) return null;

        // Vivo / ODialer pattern: sequence of 7 to 15 digits or + followed by digits
        Pattern p = Pattern.compile("(\\+?[0-9]{7,15})");
        Matcher m = p.matcher(filename);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private static String parseContactNameFromFilename(String filename) {
        if (filename == null) return null;

        // Samsung pattern: "Call with Name_..."
        Pattern pSamsung = Pattern.compile("Call with ([^_@()]+)");
        Matcher mSamsung = pSamsung.matcher(filename);
        if (mSamsung.find()) {
            return mSamsung.group(1).trim();
        }

        // ODialer pattern: "Call_Name_Number_..."
        Pattern pODialer = Pattern.compile("Call_([^_0-9]+)_");
        Matcher mODialer = pODialer.matcher(filename);
        if (mODialer.find()) {
            return mODialer.group(1).trim().replace("_", " ");
        }

        // Xiaomi pattern: "Name@Number(timestamp)"
        Pattern pXiaomi = Pattern.compile("^([^@]+)@");
        Matcher mXiaomi = pXiaomi.matcher(filename);
        if (mXiaomi.find()) {
            return mXiaomi.group(1).trim();
        }

        return null;
    }

    private static int extractAudioDurationSeconds(String filePath) {
        if (filePath == null) return 0;
        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(filePath);
            String durStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durStr != null) {
                long ms = Long.parseLong(durStr);
                return (int) (ms / 1000);
            }
        } catch (Exception ignored) {
        } finally {
            if (mmr != null) {
                try { mmr.release(); } catch (Exception ignored) {}
            }
        }
        return 0;
    }

    private static String cleanNumber(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("[^0-9]", "");
    }
}
