const { pool } = require('../src/db');
const fs = require('fs');
const path = require('path');

async function registerRelease() {
  const filePath = path.join(__dirname, '../src/public/downloads/app-release-1.0.17.apk');
  const stats = fs.statSync(filePath);
  const fileSize = stats.size;

  await pool.query('UPDATE public.app_releases SET is_active = FALSE');
  const res = await pool.query(
    'INSERT INTO public.app_releases (version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by) VALUES ($1, $2, $3, $4, $5, $6, TRUE, $7) RETURNING *',
    [
      '1.0.17',
      18,
      'Modern WhatsApp-style chat UI: WhatsApp message selection action bar (Reply, Star, Delete, Forward, Copy, Info), modern delete confirmation dialog with Delete for Everyone and Delete for Me, modern circular 5-action attachment bottom sheet, audio/video call buttons in header, chat wallpaper and themes, local SQLite 0ms offline message cache, custom font size input with 14px default reset, and masked device ID with eye toggle in settings.',
      'app-release-1.0.17.apk',
      fileSize,
      '/downloads/app-release-1.0.17.apk',
      'sayan'
    ]
  );
  console.log('Successfully registered release v1.0.17:');
  console.log(JSON.stringify(res.rows[0], null, 2));
  process.exit(0);
}

registerRelease().catch(err => {
  console.error('Failed to register release:', err);
  process.exit(1);
});
