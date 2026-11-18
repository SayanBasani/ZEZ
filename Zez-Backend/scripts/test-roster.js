require('dotenv').config();
const { pool } = require('../src/db');
const { createAdminToken } = require('../src/middleware/adminAuth');
const http = require('http');

async function run() {
  const token = createAdminToken('admin');
  function get(url) {
    return new Promise((resolve) => {
      http.get('http://localhost:3000' + url, { headers: { Authorization: 'Bearer ' + token } }, (res) => {
        let b = '';
        res.on('data', c => b += c);
        res.on('end', () => resolve({ status: res.statusCode, body: JSON.parse(b) }));
      });
    });
  }

  const roster = await get('/api/admin/chats/devices-roster');
  console.log('Devices roster:', JSON.stringify(roster.body, null, 2));

  const convs = await get('/api/admin/chats/conversations?phone=9735154080');
  console.log('Convs for 9735154080:', JSON.stringify(convs.body, null, 2));

  process.exit(0);
}
run().catch(e => { console.error(e); process.exit(1); });
