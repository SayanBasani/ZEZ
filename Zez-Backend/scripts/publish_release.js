require('dotenv').config({ path: require('node:path').resolve(__dirname, '../.env') });
const fs = require('node:fs');
const path = require('node:path');
const { pool } = require('../src/db');
const b2Storage = require('../src/services/b2StorageService');

function parseNotesFromArgs() {
  const idx = process.argv.indexOf('--notes');
  if (idx !== -1 && process.argv[idx + 1]) {
    return process.argv[idx + 1].trim();
  }
  if (process.env.RELEASE_NOTES && process.env.RELEASE_NOTES.trim()) {
    return process.env.RELEASE_NOTES.trim();
  }
  return null;
}

async function publish() {
  const apkSourcePath = path.resolve(__dirname, '../../ZezAndroid/app/build/outputs/apk/release/app-release.apk');
  if (!fs.existsSync(apkSourcePath)) {
    throw new Error('Release APK not found at: ' + apkSourcePath);
  }

  const fileStats = fs.statSync(apkSourcePath);
  const fileSize = fileStats.size;
  if (fileSize <= 0) {
    throw new Error('Release APK is empty: ' + apkSourcePath);
  }

  const gradlePath = path.resolve(__dirname, '../../ZezAndroid/app/build.gradle');
  if (!fs.existsSync(gradlePath)) {
    throw new Error('build.gradle not found at: ' + gradlePath);
  }

  const gradleContent = fs.readFileSync(gradlePath, 'utf8');
  const vNameMatch = gradleContent.match(/versionName\s+["']([^"']+)["']/);
  const vCodeMatch = gradleContent.match(/versionCode\s+(\d+)/);
  if (!vNameMatch || !vCodeMatch) {
    throw new Error('Could not parse versionName/versionCode from build.gradle');
  }

  const versionName = vNameMatch[1].trim();
  const versionCode = parseInt(vCodeMatch[1], 10);
  const fileName = `app-release-${versionName}.apk`;

  // 1. Copy to local public/downloads staging
  const downloadsDir = path.resolve(__dirname, '../src/public/downloads');
  if (!fs.existsSync(downloadsDir)) {
    fs.mkdirSync(downloadsDir, { recursive: true });
  }
  const targetPath = path.join(downloadsDir, fileName);
  const latestPath = path.join(downloadsDir, 'app-release-latest.apk');
  fs.copyFileSync(apkSourcePath, targetPath);
  fs.copyFileSync(apkSourcePath, latestPath);
  console.log(`[Publish] Staged local APK: ${targetPath} (${fileSize} bytes)`);

  // 2. Upload strictly to Backblaze B2 so Render ephemeral restarts never lose the APK
  const fileBuffer = fs.readFileSync(apkSourcePath);
  const objectKey = `releases/app-release-${versionName}-${Date.now()}.apk`;
  console.log(`[Publish] Uploading ${fileName} to Backblaze B2 (${objectKey})...`);
  const uploadResult = await b2Storage.uploadFile({
    buffer: fileBuffer,
    key: objectKey,
    originalName: fileName,
    category: 'releases',
    mimeType: 'application/vnd.android.package-archive'
  });

  if (!uploadResult || !uploadResult.objectKey) {
    throw new Error('Backblaze B2 upload returned empty objectKey');
  }

  const downloadUrl = `/api/storage/file/${uploadResult.objectKey}`;
  console.log('[Publish] Backblaze B2 upload success:', downloadUrl);

  // Record in media_files for cloud storage tracking
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
      JSON.stringify({ etag: uploadResult.etag, versionName, versionCode })
    ]
  ).catch((e) => console.warn('[Publish] media_files insert note:', e.message));

  // 3. Update active release in PostgreSQL
  const releaseNotes =
    parseNotesFromArgs() ||
    `Quacky v${versionName} (Build ${versionCode}): Latest improvements, reliability fixes, and in-app updater enhancements.`;

  await pool.query('UPDATE public.app_releases SET is_active = FALSE WHERE is_active = TRUE');
  const insertRes = await pool.query(
    `INSERT INTO public.app_releases (version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by, created_at)
     VALUES ($1, $2, $3, $4, $5, $6, TRUE, 'admin', NOW())
     RETURNING id, version_name, version_code, file_size, download_url, created_at`,
    [versionName, versionCode, releaseNotes, fileName, fileSize, downloadUrl]
  );

  console.log('[Publish] Successfully published release in database:');
  console.log(JSON.stringify(insertRes.rows[0], null, 2));
  process.exit(0);
}

publish().catch((err) => {
  console.error('[Publish] Fatal error:', err);
  process.exit(1);
});
