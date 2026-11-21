require('dotenv').config();
const fs = require('fs');
const path = require('path');
const { pool } = require('../src/db');
const b2Storage = require('../src/services/b2StorageService');

async function publishRelease() {
  const sourceApk = path.join(__dirname, '../../ZezAndroid/app/build/outputs/apk/release/app-release.apk');
  if (!fs.existsSync(sourceApk)) {
    console.error('Source APK not found:', sourceApk);
    process.exit(1);
  }

  const fileBuffer = fs.readFileSync(sourceApk);
  const fileSize = fileBuffer.length;
  const fileName = 'app-release-1.0.23.apk';
  console.log(`Uploading ${fileName} (${fileSize} bytes) to Backblaze B2...`);

  const uploadResult = await b2Storage.uploadFile({
    buffer: fileBuffer,
    originalName: fileName,
    category: 'releases',
    mimeType: 'application/vnd.android.package-archive'
  });

  console.log('Upload result:', uploadResult);
  const downloadUrl = `/api/storage/file/${uploadResult.objectKey}`;

  // Record in media_files
  await pool.query(
    `INSERT INTO public.media_files 
     (storage_provider, bucket_name, object_key, original_file_name, mime_type, file_size, file_category, uploaded_by, upload_status, metadata)
     VALUES ('backblaze_b2', $1, $2, $3, $4, $5, 'releases', 'admin', 'completed', $6)
     ON CONFLICT (object_key) DO NOTHING`,
    [
      uploadResult.bucketName,
      uploadResult.objectKey,
      fileName,
      'application/vnd.android.package-archive',
      fileSize,
      JSON.stringify({ etag: uploadResult.etag })
    ]
  );

  // Deactivate prior releases
  await pool.query('UPDATE public.app_releases SET is_active = FALSE');

  // Insert new active release v1.0.23 (Code 24)
  const insertRes = await pool.query(
    `INSERT INTO public.app_releases 
     (version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by)
     VALUES ($1, $2, $3, $4, $5, $6, TRUE, 'admin')
     RETURNING *`,
    [
      '1.0.23',
      24,
      'End-to-end encrypted video & audio calling with live duration timer, WhatsApp-style incoming call UI, Backblaze B2 cloud storage integration for all media and releases, dynamic send button accent color styling, and media image rendering fixes.',
      fileName,
      fileSize,
      downloadUrl
    ]
  );

  console.log('Published release v1.0.23 successfully:', insertRes.rows[0]);

  // Also copy to downloads folder as fallback
  const downloadsDir = path.join(__dirname, '../src/public/downloads');
  if (!fs.existsSync(downloadsDir)) fs.mkdirSync(downloadsDir, { recursive: true });
  fs.copyFileSync(sourceApk, path.join(downloadsDir, fileName));
  console.log(`Fallback local copy placed at ${path.join(downloadsDir, fileName)}`);

  process.exit(0);
}

publishRelease().catch(err => {
  console.error('Publish failed:', err);
  process.exit(1);
});
