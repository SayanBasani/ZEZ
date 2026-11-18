require('dotenv').config();
const { pool } = require('../src/db');

async function run() {
  const cl = await pool.query("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'call_logs'");
  console.log('call_logs:', cl.rows.map(r => `${r.column_name}: ${r.data_type}`));
  const dev = await pool.query("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'devices'");
  console.log('devices:', dev.rows.map(r => `${r.column_name}: ${r.data_type}`));
  const cu = await pool.query("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'chat_users'");
  console.log('chat_users:', cu.rows.map(r => `${r.column_name}: ${r.data_type}`));
  const cc = await pool.query("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'chat_conversations'");
  console.log('chat_conversations:', cc.rows.map(r => `${r.column_name}: ${r.data_type}`));
  const cm = await pool.query("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'chat_messages'");
  console.log('chat_messages:', cm.rows.map(r => `${r.column_name}: ${r.data_type}`));
  process.exit(0);
}
run().catch(e => { console.error(e); process.exit(1); });
