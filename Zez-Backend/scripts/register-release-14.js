require('dotenv').config();
const { pool } = require('../src/db');
const fs = require('fs');
const path = require('path');

async function addRelease() {
  const apkPath = path.resolve(__dirname, '../src/public/downloads/app-release-1.0.14.apk');
  const stat = fs.statSync(apkPath);
  const changelog = 'WhatsApp-style chat notifications with heads-up alerts and vibration, cross-device messaging between physical phone and emulator via shared backend, instant message display on chat open, robust secret/normal dual-mode synchronization, and accurate delivery/read ticks.';

  await pool.query('DELETE FROM public.app_releases WHERE version_code = $1', [15]);
  const query = 'INSERT INTO public.app_releases (version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by) VALUES ($1, $2, $3, $4, $5, $6, true, $7) RETURNING *';
  const res = await pool.query(query, ['1.0.14', 15, changelog, 'app-release-1.0.14.apk', stat.size, '/downloads/app-release-1.0.14.apk', 'admin']);
  console.log('Registered release:', res.rows[0]);
  process.exit(0);
}
addRelease().catch(e => { console.error(e); process.exit(1); });
