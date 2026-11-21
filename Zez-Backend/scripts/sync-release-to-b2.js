require('dotenv').config();
const fs = require('fs');
const path = require('path');
const { pool } = require('../src/db');
const b2Storage = require('../src/services/b2StorageService');

async function syncReleases() {
  const filePath = path.join(__dirname, '../src/public/downloads/app-release-1.0.22.apk');
  if (!fs.existsSync(filePath)) {
    console.error('File not found:', filePath);
    process.exit(1);
  }

  const fileBuffer = fs.readFileSync(filePath);
  console.log(`Uploading ${filePath} (${fileBuffer.length} bytes) to Backblaze B2...`);

  const uploadResult = await b2Storage.uploadFile({
    buffer: fileBuffer,
    originalName: 'app-release-1.0.22.apk',
    category: 'releases',
    mimeType: 'application/vnd.android.package-archive'
  });

  console.log('Upload result:', uploadResult);

  // Update app_releases download_url to point to B2 endpoint
  const newUrl = `/api/storage/file/${uploadResult.objectKey}`;
  await pool.query(
    `UPDATE public.app_releases 
     SET download_url = $1 
     WHERE version_code = 23 OR file_name = 'app-release-1.0.22.apk'`,
    [newUrl]
  );

  console.log(`Updated release v1.0.22 download_url to: ${newUrl}`);
  process.exit(0);
}

syncReleases().catch(err => {
  console.error('Sync failed:', err);
  process.exit(1);
});
