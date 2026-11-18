const { pool } = require('../src/db');

async function main() {
  const t0 = Date.now();
  const agg = await pool.query(`
    SELECT 
      EXTRACT(HOUR FROM call_timestamp)::int AS hour,
      COUNT(*)::int AS count
    FROM public.call_logs
    GROUP BY hour
    ORDER BY hour;
  `);
  console.log(`SQL Aggregation time: ${Date.now() - t0}ms. Rows: ${agg.rows.length}`);

  // Test adding index on call_timestamp
  const t1 = Date.now();
  await pool.query(`
    CREATE INDEX IF NOT EXISTS idx_call_logs_timestamp ON public.call_logs (call_timestamp DESC, id DESC);
    CREATE INDEX IF NOT EXISTS idx_devices_last_seen ON public.devices (last_seen_at DESC NULLS LAST);
    CREATE INDEX IF NOT EXISTS idx_chat_conversations_last_msg ON public.chat_conversations (last_message_time DESC NULLS LAST);
  `);
  console.log(`Indexes created/verified in ${Date.now() - t1}ms`);

  process.exit(0);
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
