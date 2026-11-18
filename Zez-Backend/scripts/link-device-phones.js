require('dotenv').config();
const { pool } = require('../src/db');

console.log("DB HOST:", new URL(process.env.DATABASE_URL).host);
console.log("DB PATH:", new URL(process.env.DATABASE_URL).pathname);

async function run() {
  const c = await pool.query("SELECT current_database(), current_user");
  console.log("Connected to:", c.rows[0]);
  const tables = await pool.query("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'");
  console.log("Tables:", tables.rows.map(t => t.table_name));
  const devs = await pool.query("SELECT COUNT(*) FROM public.devices");
  console.log("Devices count:", devs.rows[0]);
  const calls = await pool.query("SELECT COUNT(*) FROM public.call_logs");
  console.log("Calls count:", calls.rows[0]);
  await pool.end();
}
run();
