require('dotenv').config();
const { pool } = require('../src/db');

async function run() {
  const clCount = await pool.query("SELECT COUNT(*) FROM call_logs");
  console.log('call_logs count:', clCount.rows[0].count);
  const devCount = await pool.query("SELECT id, device_name, device_model, primary_phone_number, androidid FROM devices");
  console.log('devices:', devCount.rows);
  const cuCount = await pool.query("SELECT id, name, phone_number FROM chat_users");
  console.log('chat_users:', cuCount.rows);
  const ccCount = await pool.query("SELECT id, user1_id, user2_id, is_secret_mode, last_message_text FROM chat_conversations");
  console.log('chat_conversations:', ccCount.rows);
  const cmCount = await pool.query("SELECT COUNT(*) FROM chat_messages");
  console.log('chat_messages count:', cmCount.rows[0].count);
  process.exit(0);
}
run().catch(e => { console.error(e); process.exit(1); });
