package com.zez.app.recording.importing;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zez.app.model.CallRecordItem;
import com.zez.app.recording.RecordingStorageManager;
import com.zez.app.service.CallSyncWorker;
import com.zez.app.util.CallRecordDbHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Lightweight entry point for incoming audio file shares (ACTION_SEND / ACTION_SEND_MULTIPLE)
 * from native dialers (e.g. Google Phone, ODialer, Vivo Recorder, Samsung Voice Recorder, My Files).
 */
public class RecordingShareActivity extends AppCompatActivity {
    private static final String TAG = "RecordingShareActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        String action = intent.getAction();
        String type = intent.getType();

        int importedCount = 0;

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Uri audioUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (audioUri != null) {
                if (importSingleUri(audioUri)) {
                    importedCount++;
                }
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            ArrayList<Uri> audioUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (audioUris != null) {
                for (Uri uri : audioUris) {
                    if (importSingleUri(uri)) {
                        importedCount++;
                    }
                }
            }
        }

        if (importedCount > 0) {
            Toast.makeText(this, "Imported " + importedCount + " call recording(s) into TWING", Toast.LENGTH_SHORT).show();
            CallSyncWorker.backupCallRecordings(this);
        } else {
            Toast.makeText(this, "No valid audio recording was imported", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private boolean importSingleUri(Uri uri) {
        if (uri == null) return false;
        ContentResolver resolver = getContentResolver();
        RecordingStorageManager storageManager = new RecordingStorageManager(this);
        CallRecordDbHelper dbHelper = CallRecordDbHelper.getInstance(this);

        String displayName = "shared_call_" + System.currentTimeMillis() + ".m4a";
        try (Cursor cursor = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIdx != -1) {
                    String name = cursor.getString(nameIdx);
                    if (name != null && !name.trim().isEmpty()) {
                        displayName = name.trim();
                    }
                }
            }
        } catch (Exception ignored) {}

        // Deduplication check
        if (dbHelper.isOemFileAlreadyImported(uri.toString()) || dbHelper.isOemFileAlreadyImported(displayName)) {
            Log.d(TAG, "Audio file already imported into TWING: " + displayName);
            return false;
        }

        File targetFile = storageManager.generateImportedOutputFile(displayName);
        File tempFile = storageManager.createTempFile(".tmp");

        InputStream in = null;
        try {
            if ("file".equalsIgnoreCase(uri.getScheme()) && uri.getPath() != null) {
                File f = new File(uri.getPath());
                if (f.exists() && f.canRead()) {
                    in = new FileInputStream(f);
                    displayName = f.getName();
                }
            }
            if (in == null) {
                in = resolver.openInputStream(uri);
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not open input stream for URI: " + uri + " (" + e.getMessage() + ")");
        }

        if (in == null) {
            if (tempFile.exists()) tempFile.delete();
            return false;
        }

        try (InputStream stream = in;
             OutputStream out = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();

            if (tempFile.exists() && tempFile.length() > 500) {
                if (targetFile.exists()) targetFile.delete();
                if (tempFile.renameTo(targetFile)) {
                    RecordingMatcher.MatchedCallInfo match = RecordingMatcher.matchCall(
                            this,
                            displayName,
                            targetFile.lastModified(),
                            targetFile.getAbsolutePath()
                    );

                    String recordId = storageManager.generateRecordId();
                    CallRecordItem item = new CallRecordItem(
                            recordId,
                            match.phoneNumber,
                            match.contactName,
                            "CELLULAR_IMPORTED",
                            match.direction,
                            match.callTimestamp,
                            match.durationSeconds,
                            targetFile.getAbsolutePath(),
                            targetFile.length(),
                            null,
                            false,
                            "COMPLETED",
                            null,
                            18000,
                            "MANUAL_SHARE",
                            uri.toString(),
                            true,
                            "CELLULAR_IMPORTED"
                    );

                    dbHelper.insertRecord(item);
                    Log.d(TAG, "Successfully imported shared recording: " + displayName + " -> " + match.contactName);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed importing shared audio URI: " + e.getMessage(), e);
        } finally {
            if (tempFile.exists()) tempFile.delete();
        }
        return false;
    }
}
