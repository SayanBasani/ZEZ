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

    if (from) {
      params.push(from);
      conditions.push(`call_timestamp >= $${params.length}::timestamptz`);
    }

    if (to) {
      params.push(to);
      conditions.push(`call_timestamp <= $${params.length}::timestamptz`);
    }

    // Total call records count for device matching date range
    const countResult = await pool.query(
      `SELECT COUNT(*)::int AS total FROM public.call_logs WHERE ${conditions.join(" AND ")}`,
      params
    );
    const totalCount = countResult.rows[0]?.total || 0;

    // Fetch call logs ordered descending
    params.push(limit);
    params.push(offset);
    const query = `
      SELECT id, device_id, call_timestamp, encrypted_payload, created_at
      FROM public.call_logs
      WHERE ${conditions.join(" AND ")}
      ORDER BY call_timestamp DESC, id DESC
      LIMIT $${params.length - 1} OFFSET $${params.length}
    `;

    const result = await pool.query(query, params);
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
      conditions.push(`cl.device_id = $${params.length}`);
    }

    if (from) {
      params.push(from);
      conditions.push(`cl.call_timestamp >= $${params.length}::timestamptz`);
    }

    if (to) {
      params.push(to);
      conditions.push(`cl.call_timestamp <= $${params.length}::timestamptz`);
    }

    if (year && !isNaN(year)) {
      params.push(year);
      conditions.push(`EXTRACT(YEAR FROM cl.call_timestamp) = $${params.length}`);
    }

    if (month && !isNaN(month)) {
      params.push(month);
      conditions.push(`EXTRACT(MONTH FROM cl.call_timestamp) = $${params.length}`);
    }

    const whereClause = conditions.length > 0 ? `WHERE ${conditions.join(" AND ")}` : "";

    const countResult = await pool.query(
      `SELECT COUNT(*)::int AS total FROM public.call_logs cl ${whereClause}`,
      params
    );
    const total = countResult.rows[0]?.total || 0;

    params.push(limit);
    params.push(offset);

    const query = `
      SELECT 
        cl.id, cl.device_id, cl.call_timestamp, cl.encrypted_payload, cl.created_at,
        d.device_name, d.device_model, d.androidid
      FROM public.call_logs cl
      LEFT JOIN public.devices d ON d.id = cl.device_id
      ${whereClause}
      ORDER BY cl.call_timestamp DESC, cl.id DESC
      LIMIT $${params.length - 1} OFFSET $${params.length}
    `;

    const result = await pool.query(query, params);
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

module.exports = router;
