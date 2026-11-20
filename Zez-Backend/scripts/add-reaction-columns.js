require('dotenv').config();
const { pool } = require('../src/db');

async function run() {
  await pool.query(`
    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS reaction VARCHAR(32);
    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS is_deleted_for_everyone BOOLEAN DEFAULT FALSE;
    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS deleted_for_users JSONB DEFAULT '[]'::jsonb;
  `);
  console.log('Columns reaction, is_deleted_for_everyone, deleted_for_users successfully added!');
  process.exit(0);
}

run().catch(err => {
  console.error('Migration error:', err);
  process.exit(1);
});
