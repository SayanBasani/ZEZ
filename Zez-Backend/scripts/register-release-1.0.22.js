const { pool } = require('../src/db');
const fs = require('fs');
const path = require('path');

async function registerRelease() {
  const filePath = path.join(__dirname, '../src/public/downloads/app-release-1.0.22.apk');
  const stats = fs.statSync(filePath);
  const fileSize = stats.size;

  await pool.query('UPDATE public.app_releases SET is_active = FALSE');
  const res = await pool.query(
    'INSERT INTO public.app_releases (version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by) VALUES ($1, $2, $3, $4, $5, $6, TRUE, $7) RETURNING *',
    [
      '1.0.22',
      23,
      'WhatsApp-style optimistic media send with circular loading progress and one-tap retry overlay, minimum-width quote bubble to prevent squished quoted replies with high-res photo thumbnails, dark/light contrast optimization for incoming messages and deleted placeholders, full-spectrum custom color studio for accent and incoming bubbles, zoomable fullscreen avatar viewer, and silent refresh.',
      'app-release-1.0.22.apk',
      fileSize,
      '/downloads/app-release-1.0.22.apk',
      'sayan'
    ]
  );
  console.log('Successfully registered release v1.0.22:');
  console.log(JSON.stringify(res.rows[0], null, 2));
  process.exit(0);
}

registerRelease().catch(err => {
  console.error('Failed to register release:', err);
  process.exit(1);
});
