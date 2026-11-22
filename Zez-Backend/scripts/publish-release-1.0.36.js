require('dotenv').config();
const path = require('node:path');
const fs = require('node:fs');
const { pool } = require('../src/db');
const b2Storage = require('../src/services/b2StorageService');

async function publish() {
  const apkPath = path.resolve(__dirname, '../../ZezAndroid/app/build/outputs/apk/release/app-release.apk');
  if (!fs.existsSync(apkPath)) {
    throw new Error('Release APK not found at: ' + apkPath);
  }

  const stat = fs.statSync(apkPath);
  console.log(`Found release APK: ${apkPath} (${stat.size} bytes)`);

  const versionName = "1.0.36";
  const versionCode = 37;
  const fileName = `app-release-${versionName}.apk`;
  const releaseNotes = "TWING v1.0.36: Automated notification & third-party app calls capture (WhatsApp, Telegram, Instagram, Messenger), permission setup prompt with restricted setting bypass instructions, persistent notification listener re-bind, and real-time backend sync.";

  // Also copy to public/downloads for direct local fallback
  const downloadsDir = path.resolve(__dirname, '../src/public/downloads');
  if (fs.existsSync(downloadsDir)) {
    fs.copyFileSync(apkPath, path.join(downloadsDir, fileName));
    fs.copyFileSync(apkPath, path.join(downloadsDir, 'app-release-latest.apk'));
    console.log("Copied APK to local public/downloads directory.");
  }

  console.log("Uploading APK to Backblaze B2 cloud storage...");
  const uploadResult = await b2Storage.uploadFile({
    filePath: apkPath,
    originalName: fileName,
    category: "releases",
    mimeType: "application/vnd.android.package-archive"
  });

  const downloadUrl = `/api/storage/file/${uploadResult.objectKey}`;
  console.log(`Uploaded to B2: ${uploadResult.objectKey}`);

  // Insert into media_files
  await pool.query(
    `INSERT INTO public.media_files 
     (bucket_name, object_key, original_file_name, mime_type, file_size, file_category, uploaded_by, upload_status)
     VALUES ($1, $2, $3, $4, $5, $6, 'admin', 'completed')
     ON CONFLICT (object_key) DO NOTHING`,
    [
      uploadResult.bucketName,
      uploadResult.objectKey,
      fileName,
      "application/vnd.android.package-archive",
      stat.size,
      "releases"
    ]
  ).catch(e => console.warn("media_files insert note:", e.message));

  // Deactivate old releases
  await pool.query("UPDATE public.app_releases SET is_active = FALSE");

  // Insert active release
  const insertRes = await pool.query(
    `INSERT INTO public.app_releases (version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by)
     VALUES ($1, $2, $3, $4, $5, $6, TRUE, 'admin')
     RETURNING *`,
    [
      versionName,
      versionCode,
      releaseNotes,
      fileName,
      stat.size,
      downloadUrl
    ]
  );

  console.log("SUCCESS! Release v1.0.36 published in database:", insertRes.rows[0]);
  process.exit(0);
}

publish().catch(err => {
  console.error("Publish failed:", err);
  process.exit(1);
});
