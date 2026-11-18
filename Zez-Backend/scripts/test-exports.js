require('dotenv').config();
const { createAdminToken } = require('../src/middleware/adminAuth');
const http = require('http');

async function run() {
  const token = createAdminToken('admin');
  function get(url) {
    return new Promise((resolve) => {
      http.get('http://localhost:3000' + url, { headers: { Authorization: 'Bearer ' + token } }, (res) => {
        let b = '';
        res.on('data', c => b += c);
        res.on('end', () => resolve({ status: res.statusCode, body: b, headers: res.headers }));
      });
    });
  }

  const expJson = await get('/api/admin/chats/export-device/9735154080?format=json');
  console.log('Export JSON status:', expJson.status, 'len:', expJson.body.length);

  const expCsv = await get('/api/admin/chats/export-device/9735154080?format=csv');
  console.log('Export CSV status:', expCsv.status, 'len:', expCsv.body.length, 'first 150:', expCsv.body.substring(0, 150));

  const expTxt = await get('/api/admin/chats/export-device/9735154080?format=txt');
  console.log('Export TXT status:', expTxt.status, 'len:', expTxt.body.length, 'first 150:', expTxt.body.substring(0, 150));

  const callsExpCsv = await get('/api/admin/calls/export?format=csv');
  console.log('Calls export CSV status:', callsExpCsv.status, 'len:', callsExpCsv.body.length, 'first 150:', callsExpCsv.body.substring(0, 150));

  const contactsExpJson = await get('/api/admin/contacts/export?format=json');
  console.log('Contacts export JSON status:', contactsExpJson.status, 'len:', contactsExpJson.body.length);

  process.exit(0);
}
run().catch(e => { console.error(e); process.exit(1); });
