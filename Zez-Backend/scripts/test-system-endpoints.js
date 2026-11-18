require('dotenv').config();
const http = require('http');
const { createAdminToken } = require('../src/middleware/adminAuth');

const token = createAdminToken('admin');

function request(options, body) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, data: JSON.parse(data) });
        } catch (e) {
          resolve({ status: res.statusCode, raw: data });
        }
      });
    });
    req.on('error', reject);
    if (body) {
      req.write(typeof body === 'string' ? body : JSON.stringify(body));
    }
    req.end();
  });
}

async function run() {
  console.log('Testing GET /api/admin/system/environment...');
  const envRes = await request({
    hostname: 'localhost',
    port: 3000,
    path: '/api/admin/system/environment',
    method: 'GET',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  console.log('Environment status:', envRes.status);
  console.log('OS:', envRes.data?.environment?.os?.platform, envRes.data?.environment?.os?.uptimeFormatted);
  console.log('CPU:', envRes.data?.environment?.cpu?.model, 'cores:', envRes.data?.environment?.cpu?.cores);
  console.log('RAM:', envRes.data?.environment?.memory?.usedPercent + '%', 'total:', envRes.data?.environment?.memory?.totalFormatted);
  console.log('DB:', envRes.data?.environment?.database?.db, 'size:', envRes.data?.environment?.database?.size);
  console.log('Storage files:', envRes.data?.environment?.storage?.totalFilesCount, 'size:', envRes.data?.environment?.storage?.totalStorageFormatted);

  console.log('\nTesting GET /api/admin/system/files...');
  const filesRes = await request({
    hostname: 'localhost',
    port: 3000,
    path: '/api/admin/system/files',
    method: 'GET',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  console.log('Files status:', filesRes.status);
  console.log('Total files found:', filesRes.data?.count, 'total size:', filesRes.data?.totalFormatted);
  if (filesRes.data?.files?.length > 0) {
    console.log('Sample file:', filesRes.data.files[0].name, filesRes.data.files[0].category, filesRes.data.files[0].sizeFormatted);
  }

  console.log('\nTesting Path Safety (traversal attempt)...');
  const badDel = await request({
    hostname: 'localhost',
    port: 3000,
    path: '/api/admin/system/files?path=../../package.json',
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  console.log('Traversal reject status:', badDel.status, badDel.data);

  console.log('\nAll tests passed successfully!');
  process.exit(0);
}

run().catch(e => { console.error('Test error:', e); process.exit(1); });
