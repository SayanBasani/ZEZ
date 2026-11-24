require('dotenv').config();
const fs = require('fs');
const path = require('path');
const { pool } = require('../src/db');
const b2Storage = require('../src/services/b2StorageService');

async function publish() {
  const apkSourcePath = path.resolve(__dirname, '../../ZezAndroid/app/build/outputs/apk/release/app-release.apk');
  if (!fs.existsSync(apkSourcePath)) {
    throw new Error('Release APK not found at: ' + apkSourcePath);
  }

  const fileStats = fs.statSync(apkSourcePath);
  const fileSize = fileStats.size;
  const versionName = '1.0.41';
  const versionCode = 42;
  const fileName = 'app-release-1.0.41.apk';

  // 1. Copy to public downloads directory
  const downloadsDir = path.resolve(__dirname, '../src/public/downloads');
  if (!fs.existsSync(downloadsDir)) {
    fs.mkdirSync(downloadsDir, { recursive: true });
  }

  const targetPath = path.join(downloadsDir, fileName);
  const latestPath = path.join(downloadsDir, 'app-release-latest.apk');
  fs.copyFileSync(apkSourcePath, targetPath);
  fs.copyFileSync(apkSourcePath, latestPath);
  console.log(`[Publish] Copied APK to ${targetPath} and ${latestPath} (${fileSize} bytes)`);

  // 2. Upload to Backblaze B2 Storage
  let downloadUrl = `/downloads/${fileName}`;
  try {
    const fileBuffer = fs.readFileSync(apkSourcePath);
    console.log('[Publish] Uploading to Backblaze B2...');
    const uploadResult = await b2Storage.uploadFile({
      buffer: fileBuffer,
      originalName: fileName,
      category: 'releases',
      mimeType: 'application/vnd.android.package-archive'
    });
    if (uploadResult && uploadResult.objectKey) {
      downloadUrl = `/api/storage/file/${uploadResult.objectKey}`;
      console.log('[Publish] Backblaze B2 upload success! ObjectKey:', uploadResult.objectKey);
    }
  } catch (b2Err) {
    console.warn('[Publish] B2 upload warning (falling back to direct download):', b2Err.message);
  }

  // 3. Update database
  console.log('[Publish] Updating database records...');
  const releaseNotes = 'Quacky v1.0.41: Rich MediaStyle notification controls with system seeker & lock screen widget, instant audio playback with VisionOS resolver, accurate real-time duration display, live download progress with speeds and sub-tabs, and responsive 3-column mobile admin panel.';

  await pool.query('UPDATE public.app_releases SET is_active = FALSE WHERE is_active = TRUE');
  const insertRes = await pool.query(
    `INSERT INTO public.app_releases (version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by, created_at)
     VALUES ($1, $2, $3, $4, $5, $6, TRUE, 'admin', NOW())
     RETURNING id, version_name, version_code, download_url`,
    [versionName, versionCode, releaseNotes, fileName, fileSize, downloadUrl]
  );

  console.log('[Publish] Successfully published release in database:', JSON.stringify(insertRes.rows[0], null, 2));
  process.exit(0);
}

publish().catch(err => {
  console.error('[Publish] Fatal error:', err);
  process.exit(1);
});
