require('dotenv').config();
const { pool } = require('../src/db');
const os = require('os');
const fs = require('fs');
const path = require('path');

async function test() {
  const dbSizeRes = await pool.query("SELECT pg_size_pretty(pg_database_size(current_database())) as size, current_database() as db");
  console.log('DB size:', dbSizeRes.rows[0]);
  const tableSizes = await pool.query("SELECT relname as table_name, pg_size_pretty(pg_total_relation_size(relid)) as total_size, pg_total_relation_size(relid) as bytes FROM pg_catalog.pg_statio_user_tables ORDER BY pg_total_relation_size(relid) DESC LIMIT 10");
  console.log('Tables:', tableSizes.rows);

  const uploadsDir = path.join(__dirname, '..', 'uploads');
  console.log('Uploads exists:', fs.existsSync(uploadsDir));
  if (fs.existsSync(uploadsDir)) {
    console.log('Uploads contents:', fs.readdirSync(uploadsDir));
  }

  process.exit(0);
}
test().catch(e => { console.error(e); process.exit(1); });
