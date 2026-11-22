package com.zez.app.recording.importing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.zez.app.model.CallRecordItem;
import com.zez.app.recording.RecordingStorageManager;
import com.zez.app.util.CallRecordDbHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

/**
 * Storage Access Framework (SAF) integration for importing recordings created
 * by manufacturer native phone dialers (Vivo, OnePlus, Samsung, Xiaomi, etc.).
 */
public class OemRecordingImporter {
    private static final String TAG = "OemRecordingImporter";

    public static final int REQUEST_CODE_SAF_FOLDER = 9021;
    private static final String PREF_NAME = "oem_recording_prefs";
    private static final String KEY_FOLDER_URI = "saf_recording_folder_uri";
    private static final String KEY_FOLDER_NAME = "saf_recording_folder_name";
    private static final String KEY_LAST_SYNC_TIME = "saf_last_sync_time";

    private static volatile OemRecordingImporter instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final RecordingStorageManager storageManager;
    private final CallRecordDbHelper dbHelper;

    public static synchronized OemRecordingImporter getInstance(Context context) {
        if (instance == null) {
            instance = new OemRecordingImporter(context.getApplicationContext());
        }
        return instance;
    }

    private OemRecordingImporter(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.storageManager = new RecordingStorageManager(context);
        this.dbHelper = CallRecordDbHelper.getInstance(context);
    }

    /**
     * Launches the system folder picker pre-hinted to the device's native recording path.
     */
    public void launchFolderPicker(Activity activity) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);

            // Pre-hint initial folder on Android O+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Uri initialUri = getRecommendedOemFolderUri();
                if (initialUri != null) {
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
                }
            }

            activity.startActivityForResult(intent, REQUEST_CODE_SAF_FOLDER);
            Log.d(TAG, "Launched ACTION_OPEN_DOCUMENT_TREE picker");
        } catch (Exception e) {
            Log.e(TAG, "Failed launching folder picker: " + e.getMessage(), e);
        }
    }

    /**
     * Persists the selected tree URI permission and updates preferences.
     */
    public boolean handleFolderPickerResult(Uri treeUri) {
        if (treeUri == null) return false;
        try {
            // Take persistable permission so folder access survives reboots
            context.getContentResolver().takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            DocumentFile treeDoc = DocumentFile.fromTreeUri(context, treeUri);
            String folderName = (treeDoc != null && treeDoc.getName() != null) ? treeDoc.getName() : "Call Recordings";

            prefs.edit()
                    .putString(KEY_FOLDER_URI, treeUri.toString())
                    .putString(KEY_FOLDER_NAME, folderName)
                    .apply();

            Log.d(TAG, "Successfully acquired and persisted permission for: " + folderName + " (" + treeUri + ")");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed taking persistable permission: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean isFolderConfigured() {
        String uriStr = prefs.getString(KEY_FOLDER_URI, null);
        if (uriStr == null || uriStr.trim().isEmpty()) return false;

        // Verify if permission is still valid in system table
        try {
            List<UriPermission> persisted = context.getContentResolver().getPersistedUriPermissions();
            for (UriPermission p : persisted) {
                if (p.getUri().toString().equals(uriStr) && p.isReadPermission()) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public String getLinkedFolderName() {
        return prefs.getString(KEY_FOLDER_NAME, "Not Linked");
    }

    public String getDetectedOem() {
        String mfg = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase(Locale.US) : "generic";
        if (mfg.contains("vivo") || mfg.contains("iqoo")) return "vivo";
        if (mfg.contains("oneplus") || mfg.contains("oppo") || mfg.contains("realme")) return "oneplus";
        if (mfg.contains("samsung")) return "samsung";
        if (mfg.contains("xiaomi") || mfg.contains("redmi") || mfg.contains("poco")) return "xiaomi";
        return mfg;
    }

    public String getPersistedUriDisplayName() {
        String folderName = prefs.getString(KEY_FOLDER_NAME, null);
        if (folderName != null && !folderName.isEmpty()) return folderName;
        String uriStr = prefs.getString(KEY_FOLDER_URI, null);
        if (uriStr != null) {
            try {
                Uri uri = Uri.parse(uriStr);
                return uri.getLastPathSegment() != null ? uri.getLastPathSegment() : uriStr;
            } catch (Exception ignored) {}
        }
        return "Not Linked";
    }

    public long getLastSyncTime() {
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0);
    }

    public void clearLinkedFolder() {
        String uriStr = prefs.getString(KEY_FOLDER_URI, null);
        if (uriStr != null) {
            try {
                context.getContentResolver().releasePersistableUriPermission(
                        Uri.parse(uriStr),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (Exception ignored) {}
        }
        prefs.edit().remove(KEY_FOLDER_URI).remove(KEY_FOLDER_NAME).apply();
    }

    /**
     * Recommends the likely folder path based on device manufacturer.
     */
    public String getRecommendedFolderHint() {
        String mfr = (Build.MANUFACTURER != null) ? Build.MANUFACTURER.toLowerCase(Locale.US) : "";
        if (mfr.contains("vivo") || mfr.contains("iqoo")) {
            return "Internal Storage > Record > Call";
        } else if (mfr.contains("oneplus") || mfr.contains("oppo") || mfr.contains("realme")) {
            return "Internal Storage > Recordings > Call Recordings (or Music/Recordings)";
        } else if (mfr.contains("samsung")) {
            return "Internal Storage > Recordings > Call";
        } else if (mfr.contains("xiaomi") || mfr.contains("redmi") || mfr.contains("poco")) {
            return "Internal Storage > MIUI > sound_recorder > call_rec";
        }
        return "Internal Storage > Recordings > Call";
    }

    /**
     * Scans the linked folder, imports new non-duplicate recordings, matches metadata,
     * and inserts them into TWING's private database.
     *
     * @return count of newly imported recordings
     */
    public synchronized int scanAndImportNewRecordings() {
        if (!isFolderConfigured()) {
            Log.d(TAG, "No native recording folder linked. Skipping scan.");
            return 0;
        }

        String uriStr = prefs.getString(KEY_FOLDER_URI, null);
        if (uriStr == null) return 0;

        Uri treeUri = Uri.parse(uriStr);
        DocumentFile rootDir = DocumentFile.fromTreeUri(context, treeUri);
        if (rootDir == null || !rootDir.exists() || !rootDir.isDirectory()) {
            Log.w(TAG, "Linked recording folder is no longer accessible or directory was removed.");
            return 0;
        }

        DocumentFile[] files = rootDir.listFiles();
        if (files == null || files.length == 0) {
            Log.d(TAG, "No files found in linked folder.");
            return 0;
        }

        int importedCount = 0;
        long now = System.currentTimeMillis();

        for (DocumentFile docFile : files) {
            if (docFile.isDirectory() || !isSupportedAudioFile(docFile.getName())) {
                continue;
            }

            // Check if already imported (deduplication)
            String fileUriStr = docFile.getUri().toString();
            String fileName = docFile.getName();
            if (dbHelper.isOemFileAlreadyImported(fileUriStr) || dbHelper.isOemFileAlreadyImported(fileName)) {
                continue;
            }

            // Verify file size (> 500 bytes to avoid zero-byte stubs)
            long fileLength = docFile.length();
            if (fileLength < 500) {
                continue;
            }

            // Incomplete file check: wait if file was modified in the last 1.5 seconds (still flushing)
            if (now - docFile.lastModified() < 1500) {
                Log.d(TAG, "File " + fileName + " was modified very recently; waiting for dialer to finalize.");
                continue;
            }

            // Import file atomically
            File importedLocalFile = importDocumentFileToLocalSandbox(docFile);
            if (importedLocalFile != null && importedLocalFile.exists() && importedLocalFile.length() > 0) {
                // Correlate with Android CallLog
                RecordingMatcher.MatchedCallInfo match = RecordingMatcher.matchCall(
                        context,
                        fileName,
                        docFile.lastModified(),
                        importedLocalFile.getAbsolutePath()
                );

                String recordId = storageManager.generateRecordId();
                CallRecordItem recordItem = new CallRecordItem(
                        recordId,
                        match.phoneNumber,
                        match.contactName,
                        "CELLULAR_IMPORTED",
                        match.direction,
                        match.callTimestamp,
                        match.durationSeconds,
                        importedLocalFile.getAbsolutePath(),
                        importedLocalFile.length(),
                        null,
                        false,
                        "COMPLETED",
                        null,
                        15000,
                        "OEM_SAF_IMPORT",
                        fileUriStr,
                        true,
                        "CELLULAR_IMPORTED"
                );

                dbHelper.insertRecord(recordItem);
                importedCount++;
                Log.d(TAG, "Successfully imported native recording: " + fileName + " -> " + match.contactName + " (" + match.phoneNumber + ")");
            }
        }

        prefs.edit().putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis()).apply();
        Log.d(TAG, "SAF Scan completed. Total new recordings imported: " + importedCount);
        return importedCount;
    }

    /**
     * Atomically copies a DocumentFile audio stream into application-private storage.
     */
    private File importDocumentFileToLocalSandbox(DocumentFile docFile) {
        File tempFile = null;
        File targetFile = null;
        try {
            targetFile = storageManager.generateImportedOutputFile(docFile.getName());
            tempFile = storageManager.createTempFile(getFileExtension(docFile.getName()));

            try (InputStream in = context.getContentResolver().openInputStream(docFile.getUri());
                 OutputStream out = new FileOutputStream(tempFile)) {
                if (in == null) return null;

                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }

            // Atomic rename from temp file to target file
            if (tempFile.exists() && tempFile.length() > 0) {
                if (targetFile.exists()) targetFile.delete();
                if (tempFile.renameTo(targetFile)) {
                    return targetFile;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error copying DocumentFile to private storage: " + e.getMessage(), e);
            if (tempFile != null && tempFile.exists()) tempFile.delete();
            if (targetFile != null && targetFile.exists()) targetFile.delete();
        }
        return null;
    }

    private boolean isSupportedAudioFile(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase(Locale.US);
        return lower.endsWith(".m4a") || lower.endsWith(".mp3") || lower.endsWith(".aac")
                || lower.endsWith(".wav") || lower.endsWith(".amr") || lower.endsWith(".3gp")
                || lower.endsWith(".ogg");
    }

    private String getFileExtension(String name) {
        if (name == null) return ".m4a";
        int dot = name.lastIndexOf('.');
        if (dot >= 0) return name.substring(dot);
        return ".m4a";
    }

    private Uri getRecommendedOemFolderUri() {
        return null;
    }
}
