require('dotenv').config({ path: require('node:path').resolve(__dirname, '../.env') });
const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');
const { pool, initializeDatabase } = require('../src/db');
const b2Storage = require('../src/services/b2StorageService');

function parseFlagValue(flag, fallback = null) {
  const idx = process.argv.indexOf(flag);
  if (idx !== -1 && process.argv[idx + 1]) {
    return process.argv[idx + 1].trim();
  }
  return fallback;
}

function parseBoolFlag(flag) {
  return process.argv.includes(flag);
}

async function publish() {
  await initializeDatabase();

  const rawAppId = (parseFlagValue('--app', 'quacky') || 'quacky').toLowerCase();
  if (rawAppId !== 'quacky' && rawAppId !== 'zez') {
    throw new Error(`Unsupported --app value '${rawAppId}'. Use 'quacky' or 'zez'.`);
  }

  const projectDir = rawAppId === 'quacky' ? 'QuackyAndroid' : 'Zez';
  const defaultPackage = rawAppId === 'quacky' ? 'com.zez.app' : 'com.zez.manager';
  const defaultApk = path.resolve(__dirname, `../../${projectDir}/app/build/outputs/apk/release/app-release.apk`);
  const apkSourcePath = parseFlagValue('--apk')
    ? path.resolve(parseFlagValue('--apk'))
    : defaultApk;

  if (!fs.existsSync(apkSourcePath)) {
    throw new Error('Release APK not found at: ' + apkSourcePath);
  }

  const fileStats = fs.statSync(apkSourcePath);
  const fileSize = fileStats.size;
  if (fileSize <= 0) {
    throw new Error('Release APK is empty: ' + apkSourcePath);
  }

  const gradlePath = path.resolve(__dirname, `../../${projectDir}/app/build.gradle`);
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
  const minSupported = Math.max(1, parseInt(parseFlagValue('--min-version', '1'), 10) || 1);
  const updateRequired = parseBoolFlag('--mandatory');
  const fileName = `${rawAppId}-release-${versionName}.apk`;

  const fileBuffer = fs.readFileSync(apkSourcePath);
  const sha256Checksum = crypto.createHash('sha256').update(fileBuffer).digest('hex');

  // 1. Copy to local public/downloads staging
  const downloadsDir = path.resolve(__dirname, '../src/public/downloads');
  if (!fs.existsSync(downloadsDir)) {
    fs.mkdirSync(downloadsDir, { recursive: true });
  }
  const targetPath = path.join(downloadsDir, fileName);
  fs.copyFileSync(apkSourcePath, targetPath);
  console.log(`[Publish:${rawAppId}] Staged local APK: ${targetPath} (${fileSize} bytes, sha256=${sha256Checksum})`);

  // 2. Upload to Backblaze B2 when configured
  let downloadUrl = `/downloads/${fileName}`;
  const objectKey = `releases/${rawAppId}/${rawAppId}-release-${versionName}-${Date.now()}.apk`;
  try {
    const uploadResult = await b2Storage.uploadFile({
      buffer: fileBuffer,
      key: objectKey,
      originalName: fileName,
      category: 'releases',
      mimeType: 'application/vnd.android.package-archive'
    });
    if (uploadResult && uploadResult.objectKey) {
      downloadUrl = `/api/storage/file/${uploadResult.objectKey}`;
      console.log(`[Publish:${rawAppId}] Backblaze B2 upload success:`, downloadUrl);

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
          JSON.stringify({ etag: uploadResult.etag, appId: rawAppId, versionName, versionCode, sha256Checksum })
        ]
      ).catch(() => {});
    }
  } catch (b2Err) {
    console.warn(`[Publish:${rawAppId}] Backblaze B2 upload skipped/failed (${b2Err.message}), using local path.`);
  }

  // 3. Update active release strictly for this app_id
  const notesArg = parseFlagValue('--notes') || (process.env.RELEASE_NOTES && process.env.RELEASE_NOTES.trim());
  const label = rawAppId === 'quacky' ? 'Quacky' : 'Zez';
  const releaseNotes =
    notesArg ||
    `${label} v${versionName} (Build ${versionCode}): Latest improvements, reliability fixes, and verified OTA update.`;

  await pool.query('UPDATE public.app_releases SET is_active = FALSE WHERE app_id = $1 AND is_active = TRUE', [rawAppId]);
  const insertRes = await pool.query(
    `INSERT INTO public.app_releases
     (app_id, version_name, version_code, release_notes, file_name, file_size, download_url,
      sha256_checksum, minimum_supported_version, update_required, release_status, package_name,
      is_active, uploaded_by, created_at, published_at)
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, 'published', $11, TRUE, 'admin', NOW(), NOW())
     RETURNING id, app_id, version_name, version_code, file_size, sha256_checksum, download_url, created_at`,
    [
      rawAppId,
      versionName,
      versionCode,
      releaseNotes,
      fileName,
      fileSize,
      downloadUrl,
      sha256Checksum,
      minSupported,
      updateRequired,
      defaultPackage
    ]
  );

  console.log(`[Publish:${rawAppId}] Successfully published release in database:`);
  console.log(JSON.stringify(insertRes.rows[0], null, 2));
  process.exit(0);
}

publish().catch((err) => {
  console.error('[Publish] Fatal error:', err);
  process.exit(1);
});
