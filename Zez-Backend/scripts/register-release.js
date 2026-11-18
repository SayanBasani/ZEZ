const fs = require('fs');
const path = require('path');
const { pool } = require('../src/db');

async function deploy() {
  const src = path.resolve('../ZezAndroid/app/build/outputs/apk/release/app-release.apk');
  const destDir = path.resolve('src/public/downloads');
  if (!fs.existsSync(destDir)) fs.mkdirSync(destDir, { recursive: true });

  const destVersioned = path.join(destDir, 'app-release-1.0.11.apk');
  const destLatest = path.join(destDir, 'app-release.apk');

  fs.copyFileSync(src, destVersioned);
  fs.copyFileSync(src, destLatest);
  const stats = fs.statSync(destVersioned);

  // Deactivate prior releases
  await pool.query('UPDATE public.app_releases SET is_active = FALSE WHERE is_active = TRUE');

  const res = await pool.query(
    `INSERT INTO public.app_releases 
      (version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by) 
     VALUES ($1, $2, $3, $4, $5, $6, TRUE, $7) 
     RETURNING *`,
    [
      '1.0.11',
      12,
      'Admin portal call filter fix ([object Object] bug resolved), modern responsive UI layout, full TypeScript definitions, and local dev vs production render dual-endpoint config.',
      'app-release-1.0.11.apk',
      stats.size,
      '/downloads/app-release-1.0.11.apk',
      'admin'
    ]
  );
  console.log('App release successfully registered:', res.rows[0]);
  process.exit(0);
}

deploy().catch(err => {
  console.error(err);
  process.exit(1);
});
