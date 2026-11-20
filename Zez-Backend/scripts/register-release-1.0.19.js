const { pool } = require('../src/db');
const fs = require('fs');
const path = require('path');

async function registerRelease() {
  const filePath = path.join(__dirname, '../src/public/downloads/app-release-1.0.19.apk');
  const stats = fs.statSync(filePath);
  const fileSize = stats.size;

  await pool.query('UPDATE public.app_releases SET is_active = FALSE');
  const res = await pool.query(
    'INSERT INTO public.app_releases (version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by) VALUES ($1, $2, $3, $4, $5, $6, TRUE, $7) RETURNING *',
    [
      '1.0.19',
      20,
      'Custom wallpaper center-crop aspect ratio & dimming control slider (0-80%), screen flicker and unnecessary re-render elimination, 7-palette custom accent color selector, futuristic cyberpunk telemetry diagnostics redesign, WhatsApp-style self-messaging ("You"), and smart local contact name resolution.',
      'app-release-1.0.19.apk',
      fileSize,
      '/downloads/app-release-1.0.19.apk',
      'sayan'
    ]
  );
  console.log('Successfully registered release v1.0.19:');
  console.log(JSON.stringify(res.rows[0], null, 2));
  process.exit(0);
}

registerRelease().catch(err => {
  console.error('Failed to register release:', err);
  process.exit(1);
});
