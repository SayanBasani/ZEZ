const express = require("express");
const crypto = require("node:crypto");
const { pool, hashPassword, verifyPassword } = require("../db");
const { adminAuth, createAdminToken } = require("../middleware/adminAuth");
const { decryptStoredPayload } = require("../crypto");

const router = express.Router();

function safeCompare(a, b) {
  if (typeof a !== "string" || typeof b !== "string") return false;
  const bufA = Buffer.from(a);
  const bufB = Buffer.from(b);
  if (bufA.length !== bufB.length) return false;
  return crypto.timingSafeEqual(bufA, bufB);
}

function parseIsoDate(val) {
  if (!val || typeof val !== "string") return null;
  val = val.trim();
  if (!val) return null;
  const ts = Date.parse(val);
  if (isNaN(ts)) return null;
  return new Date(ts).toISOString();
}

function isValidUuid(val) {
  if (!val || typeof val !== "string") return false;
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(val.trim());
}

function decodeRecord(row) {
  let call = null;
  let decryptionFailed = false;

  try {
    call = decryptStoredPayload(row.encrypted_payload);
  } catch {
    decryptionFailed = true;
  }

  return {
    id: String(row.id),
    deviceId: row.device_id,
    callTimestamp: row.call_timestamp,
    createdAt: row.created_at,
    deviceName: row.device_name || row.device_model || "Unknown Device",
    deviceModel: row.device_model || null,
    androidId: row.androidid || null,
    call,
    decryptionFailed,
  };
}

/**
 * POST /api/admin/login
 * Public endpoint to exchange username and password for an admin JWT.
 */
router.post("/login", async (req, res, next) => {
  try {
    const { username, password } = req.body || {};
    const inputUser = String(username || "").trim();
    const inputPass = String(password || "");

    if (!inputUser || !inputPass) {
      return res.status(400).json({
        success: false,
        error: "Username and password are required.",
      });
    }

    // 1. Check in public.admin_users database table
    try {
      const userRes = await pool.query(
        "SELECT id, username, password_hash FROM public.admin_users WHERE LOWER(username) = LOWER($1)",
        [inputUser]
      );

      if (userRes.rows.length > 0) {
        const row = userRes.rows[0];
        if (verifyPassword(inputPass, row.password_hash)) {
          const token = createAdminToken(row.username);
          return res.json({
            success: true,
            token,
            username: row.username,
          });
        } else {
          return res.status(401).json({
            success: false,
            error: "Invalid admin username or password.",
          });
        }
      }
    } catch (dbErr) {
      console.warn("admin_users table lookup error:", dbErr.message);
    }

    // 2. Fallback check against process.env
    const expectedUsername = process.env.ADMIN_USERNAME || "admin";
    const expectedPassword = process.env.ADMIN_PASSWORD;

    if (!expectedPassword) {
      return res.status(500).json({
        success: false,
        error: "ADMIN_PASSWORD is not configured in backend .env file.",
      });
    }

    const isUserValid = safeCompare(inputUser, expectedUsername);
    const isPassValid = safeCompare(inputPass, expectedPassword);

    if (!isUserValid || !isPassValid) {
      return res.status(401).json({
        success: false,
        error: "Invalid admin username or password.",
      });
    }

    // Auto-seed into database for persistence
    try {
      const hash = hashPassword(expectedPassword);
      await pool.query(
        `INSERT INTO public.admin_users (username, password_hash)
         VALUES ($1, $2)
         ON CONFLICT (username) DO NOTHING`,
        [expectedUsername, hash]
      );
    } catch (seedErr) {
      console.warn("Could not auto-seed admin user:", seedErr.message);
    }

    const token = createAdminToken(expectedUsername);

    return res.json({
      success: true,
      token,
      username: expectedUsername,
    });
  } catch (err) {
    next(err);
  }
});

// All routes below require valid admin authentication
router.use(adminAuth);

/**
 * GET /api/admin/me
 * Validate admin session and return profile.
 */
router.get("/me", (req, res) => {
  res.json({
    success: true,
    admin: req.admin,
  });
});

/**
 * GET /api/admin/stats
 * Dashboard summary statistics.
 */
router.get("/stats", async (_req, res, next) => {
  try {
    const statsResult = await pool.query(`
      SELECT
        (SELECT COUNT(*)::int FROM public.devices) AS total_devices,
        (SELECT COUNT(*)::int FROM public.devices WHERE last_seen_at >= NOW() - INTERVAL '24 hours') AS active_devices_24h,
        (SELECT COUNT(*)::int FROM public.call_logs) AS total_calls,
        (SELECT COUNT(*)::int FROM public.call_logs WHERE call_timestamp >= date_trunc('day', NOW())) AS calls_today,
        (SELECT MAX(last_seen_at) FROM public.devices) AS latest_sync
    `);

    res.json({
      success: true,
      stats: statsResult.rows[0],
    });
  } catch (error) {
    next(error);
  }
});

/**
 * GET /api/admin/devices
 * List all devices with aggregated call counts and search filtering.
 */
router.get("/devices", async (req, res, next) => {
  try {
    const { q } = req.query || {};
    let query = `
      SELECT 
        d.id, d.androidid, d.persistentid, d.device_name, d.device_model,
        d.manufacturer, d.brand, d.platform, d.os_version, d.app_version,
        d.primary_phone_number, d.sim1_carrier, d.sim2_carrier,
        d.registration_status, d.last_seen_at, d.created_at, d.updated_at,
        COUNT(cl.id)::int AS total_calls
      FROM public.devices d
      LEFT JOIN public.call_logs cl ON cl.device_id = d.id
    `;

    const params = [];

    if (q && typeof q === "string" && q.trim()) {
      params.push(`%${q.trim()}%`);
      query += `
        WHERE d.device_name ILIKE $1
           OR d.device_model ILIKE $1
           OR d.androidid ILIKE $1
           OR d.id::text ILIKE $1
           OR d.manufacturer ILIKE $1
           OR d.brand ILIKE $1
           OR d.primary_phone_number ILIKE $1
           OR d.sim1_carrier ILIKE $1
      `;
    }

    query += `
      GROUP BY d.id
      ORDER BY d.last_seen_at DESC NULLS LAST, d.created_at DESC
    `;

    const result = await pool.query(query, params);

    res.json({
      success: true,
      count: result.rows.length,
      devices: result.rows,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * GET /api/admin/devices/:deviceId
 * Retrieve full details for a specific device.
 */
router.get("/devices/:deviceId", async (req, res, next) => {
  try {
    const { deviceId } = req.params;

    const deviceResult = await pool.query(
      `SELECT * FROM public.devices WHERE id = $1 LIMIT 1`,
      [deviceId]
    );

    if (deviceResult.rows.length === 0) {
      return res.status(404).json({
        success: false,
        error: "Device not found.",
      });
    }

    const statsResult = await pool.query(
      `SELECT 
         COUNT(*)::int AS total_calls,
         COUNT(*) FILTER (WHERE call_timestamp >= date_trunc('day', NOW()))::int AS calls_today,
         MIN(call_timestamp) AS earliest_call,
         MAX(call_timestamp) AS latest_call
       FROM public.call_logs
       WHERE device_id = $1`,
      [deviceId]
    );

    const device = deviceResult.rows[0];
    delete device.token_hash; // Never expose token hashes to client

    res.json({
      success: true,
      device,
      stats: statsResult.rows[0],
    });
  } catch (error) {
    next(error);
  }
});

/**
 * GET /api/admin/devices/:deviceId/calls
 * Retrieve and decrypt call logs for a specific device.
 */
router.get("/devices/:deviceId/calls", async (req, res, next) => {
  try {
    const { deviceId } = req.params;
    const limit = Math.min(Math.max(parseInt(req.query.limit, 10) || 50, 1), 500);
    const page = Math.max(parseInt(req.query.page, 10) || 1, 1);
    const offset = (page - 1) * limit;
    const typeFilter = req.query.type ? String(req.query.type).toUpperCase() : null;
    const search = req.query.q ? String(req.query.q).toLowerCase().trim() : null;
    const from = req.query.from ? String(req.query.from) : null;
    const to = req.query.to ? String(req.query.to) : null;

    // Verify device exists
    const deviceCheck = await pool.query(
      `SELECT id, device_name, device_model, androidid FROM public.devices WHERE id = $1 LIMIT 1`,
      [deviceId]
    );

    if (deviceCheck.rows.length === 0) {
      return res.status(404).json({
        success: false,
        error: "Device not found.",
      });
    }

    const conditions = ["device_id = $1"];
    const params = [deviceId];

    const fromDate = parseIsoDate(from);
    const toDate = parseIsoDate(to);

    if (fromDate) {
      params.push(fromDate);
      conditions.push(`call_timestamp >= $${params.length}::timestamptz`);
    }

    if (toDate) {
      params.push(toDate);
      conditions.push(`call_timestamp <= $${params.length}::timestamptz`);
    }

    // Fetch count and call logs in parallel for high performance
    const countQuery = `SELECT COUNT(*)::int AS total FROM public.call_logs WHERE ${conditions.join(" AND ")}`;
    const dataParams = [...params, limit, offset];
    const dataQuery = `
      SELECT id, device_id, call_timestamp, encrypted_payload, created_at
      FROM public.call_logs
      WHERE ${conditions.join(" AND ")}
      ORDER BY call_timestamp DESC, id DESC
      LIMIT $${dataParams.length - 1} OFFSET $${dataParams.length}
    `;

    const [countResult, result] = await Promise.all([
      pool.query(countQuery, params),
      pool.query(dataQuery, dataParams)
    ]);
    const totalCount = countResult.rows[0]?.total || 0;
    let calls = result.rows.map(decodeRecord);

    // Apply optional client-side type or phone/name filter on decrypted payloads
    if (typeFilter && typeFilter !== "ALL") {
      calls = calls.filter((c) => c.call?.type?.toUpperCase() === typeFilter);
    }

    if (search) {
      calls = calls.filter((c) => {
        const phone = (c.call?.phoneNumber || "").toLowerCase();
        const name = (c.call?.name || "").toLowerCase();
        return phone.includes(search) || name.includes(search);
      });
    }

    res.json({
      success: true,
      device: deviceCheck.rows[0],
      total: totalCount,
      page,
      limit,
      returned: calls.length,
      calls,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * GET /api/admin/calls
 * Global feed of calls across all registered devices with filtering.
 */
router.get("/calls", async (req, res, next) => {
  try {
    const limit = Math.min(Math.max(parseInt(req.query.limit, 10) || 50, 1), 500);
    const page = Math.max(parseInt(req.query.page, 10) || 1, 1);
    const offset = (page - 1) * limit;
    const deviceId = req.query.deviceId ? String(req.query.deviceId) : null;
    const from = req.query.from ? String(req.query.from) : null;
    const to = req.query.to ? String(req.query.to) : null;
    const year = req.query.year ? parseInt(req.query.year, 10) : null;
    const month = req.query.month ? parseInt(req.query.month, 10) : null;
    const search = req.query.q ? String(req.query.q).toLowerCase().trim() : null;
    const typeFilter = req.query.type ? String(req.query.type).toUpperCase() : null;

    const conditions = [];
    const params = [];

    if (deviceId && deviceId !== "ALL") {
      params.push(deviceId);
      if (isValidUuid(deviceId)) {
        conditions.push(`cl.device_id = $${params.length}`);
      } else {
        conditions.push(`cl.device_id::text = $${params.length}`);
      }
    }

    const fromDate = parseIsoDate(from);
    const toDate = parseIsoDate(to);

    if (fromDate) {
      params.push(fromDate);
      conditions.push(`cl.call_timestamp >= $${params.length}::timestamptz`);
    }

    if (toDate) {
      params.push(toDate);
      conditions.push(`cl.call_timestamp <= $${params.length}::timestamptz`);
    }

    if (year && !isNaN(year) && year >= 1970 && year <= 2100) {
      params.push(year);
      conditions.push(`EXTRACT(YEAR FROM cl.call_timestamp) = $${params.length}`);
    }

    if (month && !isNaN(month) && month >= 1 && month <= 12) {
      params.push(month);
      conditions.push(`EXTRACT(MONTH FROM cl.call_timestamp) = $${params.length}`);
    }

    const whereClause = conditions.length > 0 ? `WHERE ${conditions.join(" AND ")}` : "";

    const countQuery = `SELECT COUNT(*)::int AS total FROM public.call_logs cl ${whereClause}`;
    const dataParams = [...params, limit, offset];
    const dataQuery = `
      SELECT 
        cl.id, cl.device_id, cl.call_timestamp, cl.encrypted_payload, cl.created_at,
        d.device_name, d.device_model, d.androidid
      FROM public.call_logs cl
      LEFT JOIN public.devices d ON d.id = cl.device_id
      ${whereClause}
      ORDER BY cl.call_timestamp DESC, cl.id DESC
      LIMIT $${dataParams.length - 1} OFFSET $${dataParams.length}
    `;

    const [countResult, result] = await Promise.all([
      pool.query(countQuery, params),
      pool.query(dataQuery, dataParams)
    ]);
    const total = countResult.rows[0]?.total || 0;
    let calls = result.rows.map(decodeRecord);

    if (typeFilter && typeFilter !== "ALL") {
      calls = calls.filter((c) => c.call?.type?.toUpperCase() === typeFilter);
    }

    if (search) {
      calls = calls.filter((c) => {
        const phone = (c.call?.phoneNumber || "").toLowerCase();
        const name = (c.call?.name || "").toLowerCase();
        const devName = (c.deviceName || "").toLowerCase();
        const devModel = (c.deviceModel || "").toLowerCase();
        const aId = (c.androidId || "").toLowerCase();
        return phone.includes(search) || name.includes(search) || devName.includes(search) || devModel.includes(search) || aId.includes(search);
      });
    }

    res.json({
      success: true,
      total,
      page,
      limit,
      returned: calls.length,
      calls,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * DELETE /api/admin/calls/:callId
 * Delete a single call log by ID.
 */
router.delete("/calls/:callId", async (req, res, next) => {
  try {
    const { callId } = req.params;

    if (!/^[1-9]\d{0,18}$/.test(callId)) {
      return res.status(400).json({ success: false, error: "Invalid call ID." });
    }

    const result = await pool.query(
      `DELETE FROM public.call_logs WHERE id = $1::bigint RETURNING id`,
      [callId]
    );

    if (result.rowCount === 0) {
      return res.status(404).json({ success: false, error: "Call record not found." });
    }

    res.json({ success: true, message: "Call record deleted successfully." });
  } catch (error) {
    next(error);
  }
});

/**
 * POST /api/admin/calls/batch-delete
 * Delete multiple call logs by array of IDs.
 */
router.post("/calls/batch-delete", async (req, res, next) => {
  try {
    const { callIds } = req.body || {};

    if (!Array.isArray(callIds) || callIds.length === 0) {
      return res.status(400).json({ success: false, error: "callIds array is required." });
    }

    // Filter valid IDs
    const validIds = callIds.filter((id) => typeof id === "string" && /^[1-9]\d{0,18}$/.test(id));
    if (validIds.length === 0) {
      return res.status(400).json({ success: false, error: "No valid call IDs provided." });
    }

    const result = await pool.query(
      `DELETE FROM public.call_logs WHERE id = ANY($1::bigint[])`,
      [validIds]
    );

    res.json({
      success: true,
      message: `${result.rowCount} call record(s) deleted.`,
      deletedCount: result.rowCount,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * DELETE /api/admin/devices/:deviceId
 * Remove a single device and its associated call logs.
 */
router.delete("/devices/:deviceId", async (req, res, next) => {
  try {
    const { deviceId } = req.params;

    const client = await pool.connect();
    try {
      await client.query("BEGIN");
      await client.query(`DELETE FROM public.call_logs WHERE device_id = $1`, [deviceId]);
      const deleteResult = await client.query(
        `DELETE FROM public.devices WHERE id = $1 RETURNING id`,
        [deviceId]
      );
      await client.query("COMMIT");

      if (deleteResult.rowCount === 0) {
        return res.status(404).json({ success: false, error: "Device not found." });
      }

      res.json({ success: true, message: "Device and associated records deleted." });
    } catch (err) {
      await client.query("ROLLBACK");
      throw err;
    } finally {
      client.release();
    }
  } catch (error) {
    next(error);
  }
});

/**
 * POST /api/admin/devices/batch-delete
 * Remove multiple devices and all their associated call logs.
 */
router.post("/devices/batch-delete", async (req, res, next) => {
  try {
    const { deviceIds } = req.body || {};

    if (!Array.isArray(deviceIds) || deviceIds.length === 0) {
      return res.status(400).json({ success: false, error: "deviceIds array is required." });
    }

    const validUuids = deviceIds.filter(
      (id) => typeof id === "string" && /^[0-9a-fA-F-]{36}$/.test(id)
    );

    if (validUuids.length === 0) {
      return res.status(400).json({ success: false, error: "No valid device UUIDs provided." });
    }

    const client = await pool.connect();
    try {
      await client.query("BEGIN");
      await client.query(
        `DELETE FROM public.call_logs WHERE device_id = ANY($1::uuid[])`,
        [validUuids]
      );
      const deleteResult = await client.query(
        `DELETE FROM public.devices WHERE id = ANY($1::uuid[])`,
        [validUuids]
      );
      await client.query("COMMIT");

      res.json({
        success: true,
        message: `${deleteResult.rowCount} device(s) and their records deleted.`,
        deletedCount: deleteResult.rowCount,
      });
    } catch (err) {
      await client.query("ROLLBACK");
      throw err;
    } finally {
      client.release();
    }
  } catch (error) {
    next(error);
  }
});

/**
 * ============================================================================
 * ADMIN SETTINGS & CREDENTIALS MANAGEMENT
 * ============================================================================
 */

/**
 * GET /api/admin/users
 * Lists all registered admin accounts
 */
router.get("/users", async (req, res, next) => {
  try {
    const result = await pool.query(
      "SELECT id, username, created_at, updated_at FROM public.admin_users ORDER BY id ASC"
    );
    res.json({
      success: true,
      users: result.rows,
      currentUser: req.admin ? req.admin.username : "admin",
    });
  } catch (err) {
    next(err);
  }
});

/**
 * POST /api/admin/users
 * Creates a new admin account
 */
router.post("/users", async (req, res, next) => {
  try {
    const { username, password } = req.body || {};
    const cleanUser = String(username || "").trim();
    const cleanPass = String(password || "");

    if (!cleanUser || cleanUser.length < 3) {
      return res.status(400).json({ success: false, error: "Username must be at least 3 characters." });
    }
    if (!cleanPass || cleanPass.length < 6) {
      return res.status(400).json({ success: false, error: "Password must be at least 6 characters." });
    }

    const existing = await pool.query(
      "SELECT id FROM public.admin_users WHERE LOWER(username) = LOWER($1)",
      [cleanUser]
    );
    if (existing.rowCount > 0) {
      return res.status(409).json({ success: false, error: `Username '${cleanUser}' already exists.` });
    }

    const hash = hashPassword(cleanPass);
    const insert = await pool.query(
      "INSERT INTO public.admin_users (username, password_hash) VALUES ($1, $2) RETURNING id, username, created_at",
      [cleanUser, hash]
    );

    res.status(201).json({
      success: true,
      message: `Admin account '${cleanUser}' created successfully.`,
      user: insert.rows[0],
    });
  } catch (err) {
    next(err);
  }
});

/**
 * PUT /api/admin/users/me
 * Update current admin username and/or password
 */
router.put("/users/me", async (req, res, next) => {
  try {
    const currentAdminName = req.admin ? req.admin.username : "admin";
    const { currentPassword, newUsername, newPassword } = req.body || {};

    if (!currentPassword) {
      return res.status(400).json({ success: false, error: "Current password is required to save changes." });
    }

    // Find current admin record
    let adminRes = await pool.query(
      "SELECT * FROM public.admin_users WHERE LOWER(username) = LOWER($1)",
      [currentAdminName]
    );

    let adminRow = adminRes.rows[0];

    // If not found in DB, check against .env and seed
    if (!adminRow) {
      const envUser = process.env.ADMIN_USERNAME || "admin";
      const envPass = process.env.ADMIN_PASSWORD || "";
      if (safeCompare(currentAdminName, envUser) && safeCompare(currentPassword, envPass)) {
        const hash = hashPassword(currentPassword);
        const ins = await pool.query(
          "INSERT INTO public.admin_users (username, password_hash) VALUES ($1, $2) RETURNING *",
          [currentAdminName, hash]
        );
        adminRow = ins.rows[0];
      } else {
        return res.status(401).json({ success: false, error: "Incorrect current password." });
      }
    }

    if (!verifyPassword(currentPassword, adminRow.password_hash)) {
      return res.status(401).json({ success: false, error: "Incorrect current password." });
    }

    let updatedUsername = adminRow.username;
    let updatedHash = adminRow.password_hash;

    if (newUsername && String(newUsername).trim()) {
      const cleanNewUser = String(newUsername).trim();
      if (cleanNewUser.length < 3) {
        return res.status(400).json({ success: false, error: "New username must be at least 3 characters." });
      }
      if (cleanNewUser.toLowerCase() !== adminRow.username.toLowerCase()) {
        const dup = await pool.query(
          "SELECT id FROM public.admin_users WHERE LOWER(username) = LOWER($1) AND id != $2",
          [cleanNewUser, adminRow.id]
        );
        if (dup.rowCount > 0) {
          return res.status(409).json({ success: false, error: `Username '${cleanNewUser}' is already taken.` });
        }
        updatedUsername = cleanNewUser;
      }
    }

    if (newPassword && String(newPassword).length > 0) {
      if (String(newPassword).length < 6) {
        return res.status(400).json({ success: false, error: "New password must be at least 6 characters." });
      }
      updatedHash = hashPassword(String(newPassword));
    }

    await pool.query(
      "UPDATE public.admin_users SET username = $1, password_hash = $2, updated_at = NOW() WHERE id = $3",
      [updatedUsername, updatedHash, adminRow.id]
    );

    const newToken = createAdminToken(updatedUsername);

    res.json({
      success: true,
      message: "Credentials updated successfully.",
      username: updatedUsername,
      token: newToken,
    });
  } catch (err) {
    next(err);
  }
});

/**
 * DELETE /api/admin/users/:id
 * Delete an admin account
 */
router.delete("/users/:id", async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (!id || Number.isNaN(id)) {
      return res.status(400).json({ success: false, error: "Invalid admin user ID." });
    }

    const totalCount = await pool.query("SELECT COUNT(*)::int AS count FROM public.admin_users");
    if (totalCount.rows[0].count <= 1) {
      return res.status(400).json({ success: false, error: "Cannot delete the last remaining admin account." });
    }

    const target = await pool.query("SELECT username FROM public.admin_users WHERE id = $1", [id]);
    if (target.rowCount === 0) {
      return res.status(404).json({ success: false, error: "Admin user not found." });
    }

    const deletedUsername = target.rows[0].username;
    const isSelf = req.admin && req.admin.username.toLowerCase() === deletedUsername.toLowerCase();

    await pool.query("DELETE FROM public.admin_users WHERE id = $1", [id]);

    res.json({
      success: true,
      message: `Admin user '${deletedUsername}' deleted successfully.`,
      isSelf,
    });
  } catch (err) {
    next(err);
  }
});

/**
 * Helper: CSV escape
 */
function escapeCsv(val) {
  if (val === null || val === undefined) return '""';
  const str = String(val).replace(/"/g, '""');
  return `"${str}"`;
}

/**
 * GET /api/admin/chats/conversations
 * List all conversations with participant details, message counts, and active mode
 */
router.get("/chats/conversations", async (req, res, next) => {
  try {
    const query = `
      SELECT 
        c.id,
        c.is_secret_mode,
        c.secret_session_id,
        c.secret_mode_updated_at,
        c.last_message_text,
        c.last_message_time,
        c.created_at,
        json_build_object(
          'id', u1.id,
          'name', u1.name,
          'phone', u1.phone_number,
          'avatar', u1.avatar_url,
          'last_seen', u1.last_seen
        ) AS user1,
        json_build_object(
          'id', u2.id,
          'name', u2.name,
          'phone', u2.phone_number,
          'avatar', u2.avatar_url,
          'last_seen', u2.last_seen
        ) AS user2,
        COUNT(m.id)::int AS total_messages,
        COUNT(m.id) FILTER (WHERE COALESCE(m.mode, 'normal') = 'normal')::int AS normal_messages,
        COUNT(m.id) FILTER (WHERE m.mode = 'secret')::int AS secret_messages
      FROM public.chat_conversations c
      JOIN public.chat_users u1 ON c.user1_id = u1.id
      JOIN public.chat_users u2 ON c.user2_id = u2.id
      LEFT JOIN public.chat_messages m ON m.conversation_id = c.id
      GROUP BY c.id, u1.id, u2.id
      ORDER BY c.last_message_time DESC NULLS LAST;
    `;

    const result = await pool.query(query);
    res.json({
      success: true,
      conversations: result.rows,
      count: result.rowCount
    });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/admin/chats/conversations/:id/messages
 * Full chat inspector: returns all messages with mode badges, sender/receiver info, and media
 */
router.get("/chats/conversations/:id/messages", async (req, res, next) => {
  try {
    const convId = req.params.id;
    const { mode, search } = req.query;

    // Verify conversation
    const convRes = await pool.query(
      `SELECT 
         c.id, c.is_secret_mode, c.secret_session_id, c.secret_mode_updated_at, c.created_at,
         u1.id AS u1_id, u1.name AS u1_name, u1.phone_number AS u1_phone, u1.avatar_url AS u1_avatar,
         u2.id AS u2_id, u2.name AS u2_name, u2.phone_number AS u2_phone, u2.avatar_url AS u2_avatar
       FROM public.chat_conversations c
       JOIN public.chat_users u1 ON c.user1_id = u1.id
       JOIN public.chat_users u2 ON c.user2_id = u2.id
       WHERE c.id = $1`,
      [convId]
    );

    if (convRes.rowCount === 0) {
      return res.status(404).json({ success: false, error: "Conversation not found." });
    }

    const conv = convRes.rows[0];

    const params = [convId];
    let modeFilter = "";
    if (mode === "normal") {
      modeFilter = "AND COALESCE(m.mode, 'normal') = 'normal'";
    } else if (mode === "secret") {
      modeFilter = "AND m.mode = 'secret'";
    }

    let searchFilter = "";
    if (search && String(search).trim()) {
      params.push(`%${String(search).trim()}%`);
      searchFilter = `AND (m.message_text ILIKE $${params.length} OR m.file_name ILIKE $${params.length})`;
    }

    const msgQuery = `
      SELECT 
        m.id,
        m.conversation_id,
        m.sender_id,
        m.receiver_id,
        m.message_text,
        m.media_url,
        m.media_type,
        m.file_name,
        m.file_size,
        COALESCE(m.mode, 'normal') AS mode,
        m.secret_session_id,
        m.status,
        m.created_at,
        sender.name AS sender_name,
        sender.phone_number AS sender_phone,
        sender.avatar_url AS sender_avatar,
        receiver.name AS receiver_name,
        receiver.phone_number AS receiver_phone,
        receiver.avatar_url AS receiver_avatar
      FROM public.chat_messages m
      JOIN public.chat_users sender ON m.sender_id = sender.id
      JOIN public.chat_users receiver ON m.receiver_id = receiver.id
      WHERE m.conversation_id = $1
        ${modeFilter}
        ${searchFilter}
      ORDER BY m.created_at ASC;
    `;

    const msgRes = await pool.query(msgQuery, params);

    res.json({
      success: true,
      conversation: {
        id: conv.id,
        isSecretMode: !!conv.is_secret_mode,
        secretSessionId: conv.secret_session_id,
        secretModeUpdatedAt: conv.secret_mode_updated_at,
        user1: { id: conv.u1_id, name: conv.u1_name, phone: conv.u1_phone, avatar: conv.u1_avatar },
        user2: { id: conv.u2_id, name: conv.u2_name, phone: conv.u2_phone, avatar: conv.u2_avatar }
      },
      messages: msgRes.rows,
      total: msgRes.rowCount
    });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/admin/chats/export/:id
 * Export a single conversation in TXT, CSV, or JSON
 */
router.get("/chats/export/:id", async (req, res, next) => {
  try {
    const convId = req.params.id;
    const format = String(req.query.format || "txt").toLowerCase();

    // Fetch conversation info
    const convRes = await pool.query(
      `SELECT 
         c.id, c.is_secret_mode, c.created_at,
         u1.name AS u1_name, u1.phone_number AS u1_phone,
         u2.name AS u2_name, u2.phone_number AS u2_phone
       FROM public.chat_conversations c
       JOIN public.chat_users u1 ON c.user1_id = u1.id
       JOIN public.chat_users u2 ON c.user2_id = u2.id
       WHERE c.id = $1`,
      [convId]
    );

    if (convRes.rowCount === 0) {
      return res.status(404).json({ success: false, error: "Conversation not found." });
    }
    const conv = convRes.rows[0];

    // Fetch messages
    const msgRes = await pool.query(
      `SELECT 
        m.id, m.created_at, COALESCE(m.mode, 'normal') AS mode, m.secret_session_id,
        m.message_text, m.media_url, m.media_type, m.file_name, m.file_size, m.status,
        sender.name AS sender_name, sender.phone_number AS sender_phone,
        receiver.name AS receiver_name, receiver.phone_number AS receiver_phone
       FROM public.chat_messages m
       JOIN public.chat_users sender ON m.sender_id = sender.id
       JOIN public.chat_users receiver ON m.receiver_id = receiver.id
       WHERE m.conversation_id = $1
       ORDER BY m.created_at ASC`,
      [convId]
    );
    const messages = msgRes.rows;

    const safeTitle = `${conv.u1_phone}_vs_${conv.u2_phone}`.replace(/[^a-zA-Z0-9_-]/g, "");

    if (format === "json") {
      res.setHeader("Content-Disposition", `attachment; filename="chat-${safeTitle}-${Date.now()}.json"`);
      res.setHeader("Content-Type", "application/json; charset=utf-8");
      return res.send(JSON.stringify({
        success: true,
        conversationId: conv.id,
        participants: [
          { name: conv.u1_name, phone: conv.u1_phone },
          { name: conv.u2_name, phone: conv.u2_phone }
        ],
        exportedAt: new Date().toISOString(),
        totalMessages: messages.length,
        messages
      }, null, 2));
    }

    if (format === "csv") {
      res.setHeader("Content-Disposition", `attachment; filename="chat-${safeTitle}-${Date.now()}.csv"`);
      res.setHeader("Content-Type", "text/csv; charset=utf-8");
      const headers = [
        "Timestamp", "MessageId", "Mode", "SenderName", "SenderPhone",
        "ReceiverName", "ReceiverPhone", "MessageText", "MediaType", "MediaUrl", "FileName", "FileSize", "Status"
      ].join(",");

      const rows = messages.map(m => [
        escapeCsv(m.created_at),
        escapeCsv(m.id),
        escapeCsv(m.mode),
        escapeCsv(m.sender_name),
        escapeCsv(m.sender_phone),
        escapeCsv(m.receiver_name),
        escapeCsv(m.receiver_phone),
        escapeCsv(m.message_text),
        escapeCsv(m.media_type || ""),
        escapeCsv(m.media_url || ""),
        escapeCsv(m.file_name || ""),
        escapeCsv(m.file_size || 0),
        escapeCsv(m.status)
      ].join(","));

      return res.send([headers, ...rows].join("\r\n"));
    }

    // Default TXT Transcript
    res.setHeader("Content-Disposition", `attachment; filename="transcript-${safeTitle}-${Date.now()}.txt"`);
    res.setHeader("Content-Type", "text/plain; charset=utf-8");

    const lines = [
      "================================================================================",
      "ZEZ SYSTEM - OFFICIAL CHAT TRANSCRIPT EXPORT",
      `Conversation ID: ${conv.id}`,
      `Participants:    ${conv.u1_name} (${conv.u1_phone}) <---> ${conv.u2_name} (${conv.u2_phone})`,
      `Total Messages:  ${messages.length}`,
      `Generated At:    ${new Date().toISOString()}`,
      "================================================================================",
      ""
    ];

    for (const m of messages) {
      const timeStr = new Date(m.created_at).toISOString();
      const modeIndicator = m.mode === "secret" ? "[🔒 SECRET MODE]" : "[NORMAL]";
      let line = `${timeStr} ${modeIndicator} ${m.sender_name} (${m.sender_phone}): ${m.message_text}`;
      if (m.media_url) {
        line += `\n   -> Attachment: [${m.media_type || "file"}] ${m.media_url}`;
        if (m.file_name) line += ` (File: ${m.file_name}, Size: ${m.file_size || 0} bytes)`;
      }
      lines.push(line);
    }

    lines.push("\n============================= END OF TRANSCRIPT ==============================");
    return res.send(lines.join("\r\n"));
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/admin/chats/export-all
 * Export complete system chat history in JSON or CSV
 */
router.get("/chats/export-all", async (req, res, next) => {
  try {
    const format = String(req.query.format || "json").toLowerCase();

    const result = await pool.query(`
      SELECT 
        m.id, m.conversation_id, m.created_at, COALESCE(m.mode, 'normal') AS mode,
        m.message_text, m.media_url, m.media_type, m.file_name, m.file_size, m.status,
        s.name AS sender_name, s.phone_number AS sender_phone,
        r.name AS receiver_name, r.phone_number AS receiver_phone
      FROM public.chat_messages m
      JOIN public.chat_users s ON m.sender_id = s.id
      JOIN public.chat_users r ON m.receiver_id = r.id
      ORDER BY m.created_at ASC
    `);

    const messages = result.rows;

    if (format === "csv") {
      res.setHeader("Content-Disposition", `attachment; filename="all-chats-${Date.now()}.csv"`);
      res.setHeader("Content-Type", "text/csv; charset=utf-8");
      const headers = [
        "Timestamp", "ConversationId", "MessageId", "Mode",
        "SenderName", "SenderPhone", "ReceiverName", "ReceiverPhone",
        "MessageText", "MediaType", "MediaUrl", "FileName", "FileSize", "Status"
      ].join(",");

      const rows = messages.map(m => [
        escapeCsv(m.created_at),
        escapeCsv(m.conversation_id),
        escapeCsv(m.id),
        escapeCsv(m.mode),
        escapeCsv(m.sender_name),
        escapeCsv(m.sender_phone),
        escapeCsv(m.receiver_name),
        escapeCsv(m.receiver_phone),
        escapeCsv(m.message_text),
        escapeCsv(m.media_type || ""),
        escapeCsv(m.media_url || ""),
        escapeCsv(m.file_name || ""),
        escapeCsv(m.file_size || 0),
        escapeCsv(m.status)
      ].join(","));

      return res.send([headers, ...rows].join("\r\n"));
    }

    // Default JSON
    res.setHeader("Content-Disposition", `attachment; filename="all-chats-${Date.now()}.json"`);
    res.setHeader("Content-Type", "application/json; charset=utf-8");
    return res.send(JSON.stringify({
      success: true,
      exportedAt: new Date().toISOString(),
      totalMessages: messages.length,
      messages
    }, null, 2));
  } catch (err) {
    next(err);
  }
});

module.exports = router;
