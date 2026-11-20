const { pool } = require('../src/db');
const fs = require('fs');
const path = require('path');

async function registerRelease() {
  const filePath = path.join(__dirname, '../src/public/downloads/app-release-1.0.18.apk');
  const stats = fs.statSync(filePath);
  const fileSize = stats.size;

  await pool.query('UPDATE public.app_releases SET is_active = FALSE');
  const res = await pool.query(
    'INSERT INTO public.app_releases (version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by) VALUES ($1, $2, $3, $4, $5, $6, TRUE, $7) RETURNING *',
    [
      '1.0.18',
      19,
      'WhatsApp-style in-chat message search with smooth scrolling and match navigation, custom photo wallpaper from gallery, accurate online/offline last-seen timestamps, modern contact profile bottom sheet with quick call and message actions, inline gallery video player with play controls, backend server URL hidden inside Storage and Hardware with developer unlock, and credentials sanitization.',
      'app-release-1.0.18.apk',
      fileSize,
      '/downloads/app-release-1.0.18.apk',
      'sayan'
    ]
  );
  console.log('Successfully registered release v1.0.18:');
  console.log(JSON.stringify(res.rows[0], null, 2));
  process.exit(0);
}

registerRelease().catch(err => {
  console.error('Failed to register release:', err);
  process.exit(1);
});
