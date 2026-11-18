const { pool } = require('../src/db');

async function main() {
  const indexes = await pool.query(`
    SELECT tablename, indexname, indexdef 
    FROM pg_indexes 
    WHERE schemaname = 'public'
    ORDER BY tablename, indexname;
  `);
  console.log('Indexes in DB:', indexes.rows);

  const counts = await pool.query(`
    SELECT 
      (SELECT COUNT(*) FROM devices) AS devices_count,
      (SELECT COUNT(*) FROM call_logs) AS calls_count,
      (SELECT COUNT(*) FROM chat_users) AS users_count,
      (SELECT COUNT(*) FROM chat_conversations) AS convs_count,
      (SELECT COUNT(*) FROM chat_messages) AS msgs_count;
  `);
  console.log('Row counts:', counts.rows[0]);
  process.exit(0);
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
