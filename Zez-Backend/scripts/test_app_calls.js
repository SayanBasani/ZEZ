require('dotenv').config();
const crypto = require('node:crypto');
const { pool } = require('../src/db');

async function testAppCalls() {
  console.log("Testing Third-Party App Calls subsystem end-to-end...");

  // 1. Fetch latest active device
  const devRes = await pool.query(`SELECT id, device_name, androidid FROM public.devices ORDER BY last_seen_at DESC LIMIT 1`);
  if (devRes.rows.length === 0) {
    console.error("No devices found in DB to test with.");
    process.exit(1);
  }

  const device = devRes.rows[0];
  console.log(`Using device: ${device.device_name} (${device.id})`);

  // 2. Insert test app call directly or verify sync endpoint
  const testCalls = [
    {
      appName: "WhatsApp",
      packageName: "com.whatsapp",
      callerName: "Alice Walker",
      callType: "video",
      callDirection: "incoming",
      callTime: Date.now() - 300000,
      durationSeconds: 120,
      rawDetails: "Incoming video call from Alice Walker"
    },
    {
      appName: "Telegram",
      packageName: "org.telegram.messenger",
      callerName: "Bob Smith",
      callType: "voice",
      callDirection: "missed",
      callTime: Date.now() - 600000,
      durationSeconds: 0,
      rawDetails: "Missed voice call - Bob Smith"
    },
    {
      appName: "Instagram",
      packageName: "com.instagram.android",
      callerName: "Charlie Brown",
      callType: "video",
      callDirection: "incoming",
      callTime: Date.now() - 900000,
      durationSeconds: 45,
      rawDetails: "Incoming video chat from Charlie Brown"
    },
    {
      appName: "Messenger",
      packageName: "com.facebook.orca",
      callerName: "Diana Prince",
      callType: "voice",
      callDirection: "outgoing",
      callTime: Date.now() - 1200000,
      durationSeconds: 300,
      rawDetails: "Calling Diana Prince"
    }
  ];

  for (const c of testCalls) {
    await pool.query(
      `INSERT INTO public.device_app_calls
       (device_id, app_name, package_name, caller_name, call_type, call_direction, duration_seconds, call_time, timestamp, raw_details)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
       ON CONFLICT (device_id, package_name, call_time, caller_name, call_direction) DO UPDATE
       SET raw_details = EXCLUDED.raw_details`,
      [
        device.id,
        c.appName,
        c.packageName,
        c.callerName,
        c.callType,
        c.callDirection,
        c.durationSeconds || 0,
        c.callTime,
        new Date(c.callTime),
        c.rawDetails
      ]
    );
  }
  console.log(`Successfully synced ${testCalls.length} app calls into database.`);

  // 3. Query through database
  const countRes = await pool.query(`SELECT COUNT(*) FROM public.device_app_calls WHERE device_id = $1`, [device.id]);
  console.log(`Total app calls for device ${device.id}: ${countRes.rows[0].count}`);

  const rowsRes = await pool.query(`SELECT app_name, caller_name, call_type, call_direction, call_time FROM public.device_app_calls WHERE device_id = $1 ORDER BY call_time DESC LIMIT 5`, [device.id]);
  console.log("Latest calls in DB:", rowsRes.rows);

  console.log("App calls backend test passed successfully!");
  process.exit(0);
}

testAppCalls().catch(e => {
  console.error("Test failed:", e);
  process.exit(1);
});
