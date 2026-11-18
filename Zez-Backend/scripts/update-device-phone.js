require('dotenv').config();
const { pool } = require('../src/db');

async function run() {
  await pool.query("UPDATE public.devices SET primary_phone_number = '9735154080' WHERE androidid = '5560dc41208899a8' OR device_model = 'V2338'");
  const res = await pool.query("SELECT id, device_model, androidid, primary_phone_number FROM public.devices");
  console.log("DEVICES NOW:", res.rows);
  await pool.end();
}
run();
