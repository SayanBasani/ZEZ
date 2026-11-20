require('dotenv').config();
const { pool } = require('../src/db');
const fs = require('fs');
const path = require('path');

async function addRelease() {
  const apkPath = path.resolve(__dirname, '../src/public/downloads/app-release-1.0.15.apk');
  const stat = fs.statSync(apkPath);
  const changelog = 'Configurable Backend Server URL input box directly in Settings; Indian (+91) phone number normalization & seamless contact search matching; Instant real-time message delivery & polling clock-skew fix; Eye toggle to show/hide admin login password; Telemetry distribution chart properly scoped to Devices and Calls tabs.';

  await pool.query('DELETE FROM public.app_releases WHERE version_code = $1', [16]);
  const query = 'INSERT INTO public.app_releases (version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by) VALUES ($1, $2, $3, $4, $5, $6, true, $7) RETURNING *';
  const res = await pool.query(query, ['1.0.15', 16, changelog, 'app-release-1.0.15.apk', stat.size, '/downloads/app-release-1.0.15.apk', 'admin']);
  console.log('Registered release v1.0.15:', res.rows[0]);
  process.exit(0);
}
addRelease().catch(e => { console.error(e); process.exit(1); });
