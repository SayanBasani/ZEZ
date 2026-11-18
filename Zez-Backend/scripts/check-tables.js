require('dotenv').config();
const { pool } = require('../src/db');

async function run() {
  const callsOld = await pool.query("SELECT COUNT(*) FROM public.calls").catch(e => ({ rows: [e.message] }));
  console.log("calls count:", callsOld.rows[0]);
  const chatUsers = await pool.query("SELECT COUNT(*) FROM public.chat_users");
  console.log("chatUsers count:", chatUsers.rows[0]);
  const chatConvs = await pool.query("SELECT COUNT(*) FROM public.chat_conversations");
  console.log("chatConvs count:", chatConvs.rows[0]);
  const chatMsgs = await pool.query("SELECT COUNT(*) FROM public.chat_messages");
  console.log("chatMsgs count:", chatMsgs.rows[0]);
  await pool.end();
}
run();
