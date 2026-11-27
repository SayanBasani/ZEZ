package com.zez.manager.util;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.zez.manager.model.CallLogItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Handles PBKDF2 + AES-256-GCM encrypted local backup export/restore with SHA-256 integrity
 * verification, plus local restored cloud-archive storage (never storing keys in backups).
 */
public class BackupManager {
    private static final String TAG = "ZezBackupManager";
    private static final String ARCHIVE_FILE = "zez_restored_archive.json";
    private static final int BACKUP_FORMAT_VERSION = 1;
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int SALT_BYTES = 16;
    private static final int IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;

    private static final Gson gson = new Gson();

    public static class BackupExportResult {
        public final boolean success;
        public final int recordCount;
        public final String sha256Digest;
        public final String error;

        public BackupExportResult(boolean success, int recordCount, String sha256Digest, String error) {
            this.success = success;
            this.recordCount = recordCount;
            this.sha256Digest = sha256Digest;
            this.error = error;
        }
    }

    public static class BackupRestoreResult {
        public final boolean success;
        public final int totalParsed;
        public final int newRecordsAdded;
        public final int duplicatesSkipped;
        public final boolean integrityVerified;
        public final String error;

        public BackupRestoreResult(boolean success, int totalParsed, int newRecordsAdded,
                                   int duplicatesSkipped, boolean integrityVerified, String error) {
            this.success = success;
            this.totalParsed = totalParsed;
            this.newRecordsAdded = newRecordsAdded;
            this.duplicatesSkipped = duplicatesSkipped;
            this.integrityVerified = integrityVerified;
            this.error = error;
        }
    }

    public static synchronized List<CallLogItem> getRestoredArchive(Context context) {
        File file = new File(context.getFilesDir(), ARCHIVE_FILE);
        if (!file.exists()) return new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<CallLogItem>>() {}.getType();
            List<CallLogItem> items = gson.fromJson(isr, listType);
            return items != null ? items : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Failed reading archive: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static synchronized BackupRestoreResult mergeIntoRestoredArchive(Context context, List<CallLogItem> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return new BackupRestoreResult(true, 0, 0, 0, true, null);
        }
        List<CallLogItem> existing = getRestoredArchive(context);
        Map<String, CallLogItem> byKey = new LinkedHashMap<>();
        for (CallLogItem item : existing) {
            byKey.put(dedupKey(item), item);
        }

        int added = 0;
        int dups = 0;
        for (CallLogItem item : incoming) {
            if (item == null) continue;
            item.fromBackup = true;
            String k = dedupKey(item);
            if (!byKey.containsKey(k)) {
                byKey.put(k, item);
                added++;
            } else {
                dups++;
            }
        }

        List<CallLogItem> merged = new ArrayList<>(byKey.values());
        merged.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

        File target = new File(context.getFilesDir(), ARCHIVE_FILE);
        File tmp = new File(context.getFilesDir(), ARCHIVE_FILE + ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fos.write(gson.toJson(merged).getBytes(StandardCharsets.UTF_8));
            fos.getFD().sync();
        } catch (Exception e) {
            return new BackupRestoreResult(false, incoming.size(), 0, dups, false, "Failed saving archive: " + e.getMessage());
        }
        if (tmp.exists() && !tmp.renameTo(target)) {
            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(gson.toJson(merged).getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                return new BackupRestoreResult(false, incoming.size(), 0, dups, false, e.getMessage());
            }
        }
        PreferencesHelper.setLastRestoreTime(context, System.currentTimeMillis());
        return new BackupRestoreResult(true, incoming.size(), added, dups, true, null);
    }

    public static BackupExportResult writeEncryptedBackup(Context context, OutputStream out,
                                                            List<CallLogItem> calls, String passphrase) {
        if (passphrase == null || passphrase.length() < 4) {
            return new BackupExportResult(false, 0, null, "Passphrase must be at least 4 characters.");
        }
        try {
            JsonArray arr = new JsonArray();
            for (CallLogItem item : calls) {
                arr.add(item.toJsonObject());
            }
            String payloadStr = arr.toString();
            String payloadSha256 = sha256Hex(payloadStr.getBytes(StandardCharsets.UTF_8));

            SecureRandom rng = new SecureRandom();
            byte[] salt = new byte[SALT_BYTES];
            byte[] iv = new byte[IV_BYTES];
            rng.nextBytes(salt);
            rng.nextBytes(iv);

            SecretKeySpec keySpec = deriveKey(passphrase.toCharArray(), salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(payloadStr.getBytes(StandardCharsets.UTF_8));

            JsonObject envelope = new JsonObject();
            envelope.addProperty("format", "zez-call-backup");
            envelope.addProperty("version", BACKUP_FORMAT_VERSION);
            envelope.addProperty("createdAt", System.currentTimeMillis());
            envelope.addProperty("recordCount", calls.size());
            envelope.addProperty("payloadSha256", payloadSha256);
            envelope.addProperty("kdf", "PBKDF2WithHmacSHA256");
            envelope.addProperty("iterations", PBKDF2_ITERATIONS);
            envelope.addProperty("salt", Base64.encodeToString(salt, Base64.NO_WRAP));
            envelope.addProperty("iv", Base64.encodeToString(iv, Base64.NO_WRAP));
            envelope.addProperty("ciphertext", Base64.encodeToString(ciphertext, Base64.NO_WRAP));

            out.write(envelope.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
            PreferencesHelper.setLastBackupTime(context, System.currentTimeMillis());
            return new BackupExportResult(true, calls.size(), payloadSha256, null);
        } catch (Exception e) {
            return new BackupExportResult(false, 0, null, "Backup encryption failed: " + e.getMessage());
        }
    }

    public static BackupRestoreResult readAndRestoreEncryptedBackup(Context context, InputStream in, String passphrase) {
        if (passphrase == null || passphrase.isEmpty()) {
            return new BackupRestoreResult(false, 0, 0, 0, false, "Passphrase is required.");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            JsonObject env = JsonParser.parseString(sb.toString()).getAsJsonObject();
            if (!env.has("format") || !"zez-call-backup".equals(env.get("format").getAsString())) {
                return new BackupRestoreResult(false, 0, 0, 0, false, "Invalid Zez backup file format.");
            }
            byte[] salt = Base64.decode(env.get("salt").getAsString(), Base64.NO_WRAP);
            byte[] iv = Base64.decode(env.get("iv").getAsString(), Base64.NO_WRAP);
            byte[] ciphertext = Base64.decode(env.get("ciphertext").getAsString(), Base64.NO_WRAP);
            String expectedSha = env.has("payloadSha256") ? env.get("payloadSha256").getAsString() : "";

            SecretKeySpec keySpec = deriveKey(passphrase.toCharArray(), salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);

            String actualSha = sha256Hex(plaintext);
            if (!expectedSha.isEmpty() && !expectedSha.equalsIgnoreCase(actualSha)) {
                return new BackupRestoreResult(false, 0, 0, 0, false, "Backup integrity checksum mismatch!");
            }

            JsonArray arr = JsonParser.parseString(new String(plaintext, StandardCharsets.UTF_8)).getAsJsonArray();
            List<CallLogItem> parsed = new ArrayList<>();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                CallLogItem item = new CallLogItem();
                item.externalId = o.has("externalId") ? o.get("externalId").getAsString() : "";
                item.id = item.externalId;
                item.phoneNumber = o.has("phoneNumber") ? o.get("phoneNumber").getAsString() : "";
                item.name = o.has("name") ? o.get("name").getAsString() : "";
                item.type = o.has("type") ? o.get("type").getAsString() : "UNKNOWN";
                item.timestamp = o.has("timestamp") ? o.get("timestamp").getAsLong() : 0L;
                item.duration = o.has("duration") ? o.get("duration").getAsLong() : 0L;
                item.fromBackup = true;
                parsed.add(item);
            }

            return mergeIntoRestoredArchive(context, parsed);
        } catch (javax.crypto.AEADBadTagException badTag) {
            return new BackupRestoreResult(false, 0, 0, 0, false, "Incorrect passphrase or corrupted backup.");
        } catch (Exception e) {
            return new BackupRestoreResult(false, 0, 0, 0, false, "Failed reading backup: " + e.getMessage());
        }
    }

    private static SecretKeySpec deriveKey(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, 256);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static String dedupKey(CallLogItem item) {
        if (item.externalId != null && !item.externalId.isEmpty()) return item.externalId;
        return (item.phoneNumber != null ? item.phoneNumber : "") + "|" + item.timestamp + "|" + item.type;
    }
}
