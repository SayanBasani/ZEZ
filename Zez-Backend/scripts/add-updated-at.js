require('dotenv').config();
const { pool } = require('../src/db');

async function run() {
  await pool.query("ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()");
  console.log("Column updated_at added or already exists.");
  const res = await pool.query("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'chat_messages'");
  console.log("chat_messages columns:", res.rows.map(r => r.column_name));
  await pool.end();
}
run().catch(e => { console.error(e); process.exit(1); });
