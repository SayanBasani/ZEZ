const express = require("express");
const crypto = require("node:crypto");
const os = require("node:os");
const fs = require("node:fs");
const path = require("node:path");
const { pool, hashPassword, verifyPassword } = require("../db");
const { adminAuth, createAdminToken } = require("../middleware/adminAuth");
const { decryptStoredPayload } = require("../crypto");

const router = express.Router();

const PUBLIC_DIR = path.resolve(__dirname, "..", "public");
const DOWNLOADS_DIR = path.join(PUBLIC_DIR, "downloads");
const UPLOADS_DIR = path.join(PUBLIC_DIR, "uploads");
const AVATARS_DIR = path.join(UPLOADS_DIR, "avatars");
const CHAT_MEDIA_DIR = path.join(UPLOADS_DIR, "chat_media");

// Ensure required public directories exist
for (const dir of [PUBLIC_DIR, DOWNLOADS_DIR, UPLOADS_DIR, AVATARS_DIR, CHAT_MEDIA_DIR]) {
  if (!fs.existsSync(dir)) {
    try { fs.mkdirSync(dir, { recursive: true }); } catch (_) {}
  }
}

function formatBytes(bytes) {
  if (!bytes || bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
}

function scanDirectoryFiles(dirPath, category, categoryLabel, urlPrefix) {
  const items = [];
  if (!fs.existsSync(dirPath)) return items;

  try {
    const files = fs.readdirSync(dirPath, { withFileTypes: true });
    for (const f of files) {
      if (f.isFile()) {
        const fullPath = path.join(dirPath, f.name);
        try {
          const stat = fs.statSync(fullPath);
          const ext = path.extname(f.name).toLowerCase();
          const relPath = path.relative(PUBLIC_DIR, fullPath).replace(/\\/g, "/");
          items.push({
            id: Buffer.from(relPath).toString("base64url"),
            name: f.name,
            ext: ext.replace(".", ""),
            category,
            categoryLabel,
            relPath,
            url: `/${relPath}`,
            size: stat.size,
            sizeFormatted: formatBytes(stat.size),
            createdAt: stat.birthtime || stat.ctime,
            modifiedAt: stat.mtime
          });
        } catch (_) {}
      }
    }
  } catch (_) {}
  return items;
}

function isSafePublicFilePath(targetPath) {
  if (!targetPath || typeof targetPath !== "string") {
    return { valid: false, reason: "Missing or invalid path parameter." };
  }
  let clean = targetPath.trim().replace(/^[/\\]+/, "");
  if (clean.startsWith("downloads/")) {
    clean = clean.substring("downloads/".length);
    const resolved = path.resolve(DOWNLOADS_DIR, clean);
    if (!resolved.startsWith(DOWNLOADS_DIR)) {
      return { valid: false, reason: "Directory traversal detected." };
    }
    if (!fs.existsSync(resolved) || !fs.statSync(resolved).isFile()) {
      return { valid: false, reason: "File not found." };
    }
    const relPath = path.relative(PUBLIC_DIR, resolved).replace(/\\/g, "/");
    return { valid: true, resolved, relPath };
  } else if (clean.startsWith("uploads/")) {
    clean = clean.substring("uploads/".length);
    const resolved = path.resolve(UPLOADS_DIR, clean);
    if (!resolved.startsWith(UPLOADS_DIR)) {
      return { valid: false, reason: "Directory traversal detected." };
    }
    if (!fs.existsSync(resolved) || !fs.statSync(resolved).isFile()) {
      return { valid: false, reason: "File not found." };
    }
    const relPath = path.relative(PUBLIC_DIR, resolved).replace(/\\/g, "/");
    return { valid: true, resolved, relPath };
  }

  const resolved = path.resolve(PUBLIC_DIR, clean);
  const inDownloads = resolved.startsWith(DOWNLOADS_DIR + path.sep);
  const inUploads = resolved.startsWith(UPLOADS_DIR + path.sep);
  if ((inDownloads || inUploads) && fs.existsSync(resolved) && fs.statSync(resolved).isFile()) {
    const relPath = path.relative(PUBLIC_DIR, resolved).replace(/\\/g, "/");
    return { valid: true, resolved, relPath };
  }

  return { valid: false, reason: "File is not in a manageable public folder (downloads or uploads)." };
}

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

function escapeCsv(val) {
  if (val === null || val === undefined) return '""';
  const str = String(val).replace(/"/g, '""');
  return `"${str}"`;
}

function formatDurationSec(sec) {
  const s = parseInt(sec, 10) || 0;
  const m = Math.floor(s / 60);
  const remaining = s % 60;
  return `${m}m ${remaining}s`;
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
 * GET /api/admin/calls/export
 * Export full call records with complete device metadata in CSV or JSON
 */
router.get("/calls/export", async (req, res, next) => {
  try {
    const format = String(req.query.format || "json").toLowerCase();
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

    const query = `
      SELECT 
        cl.id, cl.device_id, cl.call_timestamp, cl.encrypted_payload, cl.created_at,
        d.device_name, d.device_model, d.manufacturer, d.brand, d.platform,
        d.os_version, d.app_version, d.androidid, d.primary_phone_number,
        d.sim1_phone_number, d.sim2_phone_number, d.sim1_carrier, d.sim2_carrier,
        d.last_seen_at AS device_last_seen_at, d.created_at AS device_registered_at
      FROM public.call_logs cl
      LEFT JOIN public.devices d ON d.id = cl.device_id
      ${whereClause}
      ORDER BY cl.call_timestamp DESC, cl.id DESC
      LIMIT 50000
    `;

    const result = await pool.query(query, params);
    let calls = result.rows.map(row => {
      const decoded = decodeRecord(row);
      return {
        ...decoded,
        manufacturer: row.manufacturer,
        brand: row.brand,
        platform: row.platform,
        osVersion: row.os_version,
        appVersion: row.app_version,
        primaryPhone: row.primary_phone_number,
        sim1Phone: row.sim1_phone_number,
        sim2Phone: row.sim2_phone_number,
        sim1Carrier: row.sim1_carrier,
        sim2Carrier: row.sim2_carrier,
        deviceLastSeen: row.device_last_seen_at,
        deviceRegisteredAt: row.device_registered_at
      };
    });

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

    const safeFilenamePart = deviceId && deviceId !== "ALL" ? `device-${deviceId.slice(0, 8)}` : "all-devices";
    const timestampStr = new Date().toISOString().replace(/[:.]/g, "-");

    if (format === "csv") {
      res.setHeader("Content-Disposition", `attachment; filename="call-records-${safeFilenamePart}-${timestampStr}.csv"`);
      res.setHeader("Content-Type", "text/csv; charset=utf-8");

      const headers = [
        "Call ID", "Contact Name", "Phone Number", "Call Type",
        "Duration Seconds", "Formatted Duration", "Call Timestamp",
        "Cipher Status", "Device ID", "Device Name", "Device Model",
        "Manufacturer", "Android ID", "Primary Phone Number",
        "SIM 1 Carrier", "SIM 2 Carrier", "Device Last Seen", "Sync Timestamp"
      ].join(",");

      const rows = calls.map(c => {
        const d = c.call || {};
        return [
          escapeCsv(c.id),
          escapeCsv(d.name || "Unknown"),
          escapeCsv(d.phoneNumber || ""),
          escapeCsv(d.type || "UNKNOWN"),
          escapeCsv(d.duration || 0),
          escapeCsv(formatDurationSec(d.duration || 0)),
          escapeCsv(c.callTimestamp ? new Date(c.callTimestamp).toISOString() : ""),
          escapeCsv(c.decryptionFailed ? "Decryption Error" : "Verified AES-256"),
          escapeCsv(c.deviceId),
          escapeCsv(c.deviceName || ""),
          escapeCsv(c.deviceModel || ""),
          escapeCsv(c.manufacturer || ""),
          escapeCsv(c.androidId || ""),
          escapeCsv(c.primaryPhone || ""),
          escapeCsv(c.sim1Carrier || ""),
          escapeCsv(c.sim2Carrier || ""),
          escapeCsv(c.deviceLastSeen ? new Date(c.deviceLastSeen).toISOString() : ""),
          escapeCsv(c.createdAt ? new Date(c.createdAt).toISOString() : "")
        ].join(",");
      });

      return res.send([headers, ...rows].join("\r\n"));
    }

    // Default JSON
    res.setHeader("Content-Disposition", `attachment; filename="call-records-${safeFilenamePart}-${timestampStr}.json"`);
    res.setHeader("Content-Type", "application/json; charset=utf-8");
    return res.send(JSON.stringify({
      success: true,
      exportedAt: new Date().toISOString(),
      filter: { deviceId, type: typeFilter, year, month, from, to, search },
      totalRecords: calls.length,
      records: calls.map(c => {
        const d = c.call || {};
        return {
          id: c.id,
          call: {
            name: d.name || "Unknown",
            phoneNumber: d.phoneNumber || "",
            type: d.type || "UNKNOWN",
            duration: d.duration || 0,
            formattedDuration: formatDurationSec(d.duration || 0),
            timestamp: c.callTimestamp,
            cipherStatus: c.decryptionFailed ? "failed" : "verified_aes256"
          },
          device: {
            id: c.deviceId,
            deviceName: c.deviceName,
            deviceModel: c.deviceModel,
            manufacturer: c.manufacturer,
            brand: c.brand,
            platform: c.platform,
            osVersion: c.osVersion,
            appVersion: c.appVersion,
            androidId: c.androidId,
            primaryPhoneNumber: c.primaryPhone,
            sim1Carrier: c.sim1Carrier,
            sim2Carrier: c.sim2Carrier,
            lastSeenAt: c.deviceLastSeen,
            registeredAt: c.deviceRegisteredAt
          },
          syncedAt: c.createdAt
        };
      })
    }, null, 2));
  } catch (error) {
    next(error);
  }
});

/**
 * GET /api/admin/contacts/export
 * Aggregates unique contacts with calling metadata across devices in CSV or JSON
 */
router.get("/contacts/export", async (req, res, next) => {
  try {
    const format = String(req.query.format || "json").toLowerCase();
    const deviceId = req.query.deviceId ? String(req.query.deviceId) : null;

    let whereClause = "";
    const params = [];
    if (deviceId && deviceId !== "ALL") {
      params.push(deviceId);
      if (isValidUuid(deviceId)) {
        whereClause = `WHERE cl.device_id = $1`;
      } else {
        whereClause = `WHERE cl.device_id::text = $1`;
      }
    }

    const query = `
      SELECT 
        cl.id, cl.device_id, cl.call_timestamp, cl.encrypted_payload,
        d.device_name, d.device_model, d.androidid, d.primary_phone_number
      FROM public.call_logs cl
      LEFT JOIN public.devices d ON d.id = cl.device_id
      ${whereClause}
      ORDER BY cl.call_timestamp DESC
      LIMIT 50000
    `;

    const result = await pool.query(query, params);
    const contactsMap = new Map();

    for (const row of result.rows) {
      const decoded = decodeRecord(row);
      const call = decoded.call || {};
      const rawPhone = String(call.phoneNumber || "Unknown").trim();
      const normPhone = rawPhone.replace(/\D/g, "");
      const key = normPhone || rawPhone;

      if (!contactsMap.has(key)) {
        contactsMap.set(key, {
          contactName: call.name || "Unknown",
          phoneNumber: rawPhone,
          totalCalls: 0,
          incomingCalls: 0,
          outgoingCalls: 0,
          missedCalls: 0,
          rejectedCalls: 0,
          totalDurationSeconds: 0,
          firstCallTimestamp: decoded.callTimestamp,
          lastCallTimestamp: decoded.callTimestamp,
          devices: new Set(),
          deviceModels: new Set()
        });
      }

      const entry = contactsMap.get(key);
      entry.totalCalls++;
      const type = String(call.type || "").toUpperCase();
      if (type === "INCOMING") entry.incomingCalls++;
      else if (type === "OUTGOING") entry.outgoingCalls++;
      else if (type === "MISSED") entry.missedCalls++;
      else if (type === "REJECTED") entry.rejectedCalls++;

      entry.totalDurationSeconds += (parseInt(call.duration, 10) || 0);

      if (decoded.deviceName || decoded.deviceModel) {
        entry.deviceModels.add(decoded.deviceName || decoded.deviceModel);
      }
      if (decoded.deviceId) {
        entry.devices.add(decoded.deviceId);
      }
      if (decoded.callTimestamp) {
        if (!entry.lastCallTimestamp || decoded.callTimestamp > entry.lastCallTimestamp) {
          entry.lastCallTimestamp = decoded.callTimestamp;
        }
        if (!entry.firstCallTimestamp || decoded.callTimestamp < entry.firstCallTimestamp) {
          entry.firstCallTimestamp = decoded.callTimestamp;
        }
      }
    }

    const contactsList = Array.from(contactsMap.values()).map(c => ({
      contactName: c.contactName,
      phoneNumber: c.phoneNumber,
      totalCalls: c.totalCalls,
      incomingCalls: c.incomingCalls,
      outgoingCalls: c.outgoingCalls,
      missedCalls: c.missedCalls,
      rejectedCalls: c.rejectedCalls,
      totalDurationSeconds: c.totalDurationSeconds,
      formattedTotalDuration: formatDurationSec(c.totalDurationSeconds),
      firstCallTimestamp: c.firstCallTimestamp,
      lastCallTimestamp: c.lastCallTimestamp,
      associatedDevicesCount: c.devices.size,
      associatedDeviceModels: Array.from(c.deviceModels).join("; ")
    }));

    const timestampStr = new Date().toISOString().replace(/[:.]/g, "-");

    if (format === "csv") {
      res.setHeader("Content-Disposition", `attachment; filename="contacts-metadata-${timestampStr}.csv"`);
      res.setHeader("Content-Type", "text/csv; charset=utf-8");

      const headers = [
        "Contact Name", "Phone Number", "Total Calls", "Incoming Calls",
        "Outgoing Calls", "Missed Calls", "Rejected Calls", "Total Duration Seconds",
        "Formatted Duration", "First Call Timestamp", "Last Call Timestamp",
        "Associated Device Count", "Associated Device Models"
      ].join(",");

      const rows = contactsList.map(c => [
        escapeCsv(c.contactName),
        escapeCsv(c.phoneNumber),
        escapeCsv(c.totalCalls),
        escapeCsv(c.incomingCalls),
        escapeCsv(c.outgoingCalls),
        escapeCsv(c.missedCalls),
        escapeCsv(c.rejectedCalls),
        escapeCsv(c.totalDurationSeconds),
        escapeCsv(c.formattedTotalDuration),
        escapeCsv(c.firstCallTimestamp ? new Date(c.firstCallTimestamp).toISOString() : ""),
        escapeCsv(c.lastCallTimestamp ? new Date(c.lastCallTimestamp).toISOString() : ""),
        escapeCsv(c.associatedDevicesCount),
        escapeCsv(c.associatedDeviceModels)
      ].join(","));

      return res.send([headers, ...rows].join("\r\n"));
    }

    // Default JSON
    res.setHeader("Content-Disposition", `attachment; filename="contacts-metadata-${timestampStr}.json"`);
    res.setHeader("Content-Type", "application/json; charset=utf-8");
    return res.send(JSON.stringify({
      success: true,
      exportedAt: new Date().toISOString(),
      totalContacts: contactsList.length,
      contacts: contactsList
    }, null, 2));
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
 * GET /api/admin/chats/devices-roster
 * Roster of devices paired with their chat accounts for the Chat Monitor selector
 */
router.get("/chats/devices-roster", async (req, res, next) => {
  try {
    const devicesRes = await pool.query(`
      SELECT 
        d.id, d.device_name, d.device_model, d.manufacturer, d.androidid,
        d.primary_phone_number, d.sim1_phone_number, d.sim1_carrier, d.last_seen_at,
        u.id AS user_id, u.name AS user_name, u.phone_number AS user_phone, u.avatar_url, u.last_seen AS user_last_seen,
        (SELECT COUNT(*)::int FROM public.call_logs WHERE device_id = d.id) AS calls_count
      FROM public.devices d
      LEFT JOIN public.chat_users u ON (
        d.primary_phone_number IS NOT NULL AND (
          u.phone_number = d.primary_phone_number OR
          RIGHT(REGEXP_REPLACE(u.phone_number, '\\D', '', 'g'), 10) = RIGHT(REGEXP_REPLACE(d.primary_phone_number, '\\D', '', 'g'), 10)
        )
      )
      ORDER BY d.last_seen_at DESC NULLS LAST
    `);

    const usersRes = await pool.query(`
      SELECT 
        u.id, u.name, u.phone_number, u.avatar_url, u.last_seen,
        (
          SELECT COUNT(*)::int 
          FROM public.chat_conversations c 
          WHERE c.user1_id = u.id OR c.user2_id = u.id
        ) AS conversation_count,
        (
          SELECT COUNT(*)::int 
          FROM public.chat_messages m 
          WHERE m.sender_id = u.id OR m.receiver_id = u.id
        ) AS message_count
      FROM public.chat_users u
      ORDER BY u.last_seen DESC NULLS LAST
    `);

    res.json({
      success: true,
      devices: devicesRes.rows,
      users: usersRes.rows
    });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/admin/chats/conversations
 * List conversations with participant details, message counts, and active mode.
 * Supports optional ?deviceId=..., ?phone=..., or ?userId=... filtering.
 */
router.get("/chats/conversations", async (req, res, next) => {
  try {
    const { deviceId, phone, userId } = req.query;

    let targetUserId = null;
    let targetDevice = null;
    let targetUser = null;

    if (userId) {
      targetUserId = userId;
      const uRes = await pool.query("SELECT * FROM public.chat_users WHERE id = $1", [userId]);
      if (uRes.rowCount > 0) targetUser = uRes.rows[0];
    } else if (phone) {
      const clean = String(phone).trim();
      const uRes = await pool.query(
        "SELECT * FROM public.chat_users WHERE phone_number = $1 OR RIGHT(REGEXP_REPLACE(phone_number, '\\D', '', 'g'), 10) = RIGHT(REGEXP_REPLACE($1, '\\D', '', 'g'), 10)",
        [clean]
      );
      if (uRes.rowCount > 0) {
        targetUser = uRes.rows[0];
        targetUserId = targetUser.id;
      }
      const dRes = await pool.query(
        "SELECT * FROM public.devices WHERE primary_phone_number = $1 OR RIGHT(REGEXP_REPLACE(COALESCE(primary_phone_number, ''), '\\D', '', 'g'), 10) = RIGHT(REGEXP_REPLACE($1, '\\D', '', 'g'), 10)",
        [clean]
      );
      if (dRes.rowCount > 0) targetDevice = dRes.rows[0];
    } else if (deviceId && deviceId !== "ALL") {
      const dRes = await pool.query("SELECT * FROM public.devices WHERE id::text = $1 OR androidid = $1", [deviceId]);
      if (dRes.rowCount > 0) {
        targetDevice = dRes.rows[0];
        const p = targetDevice.primary_phone_number || targetDevice.sim1_phone_number;
        if (p) {
          const uRes = await pool.query(
            "SELECT * FROM public.chat_users WHERE phone_number = $1 OR RIGHT(REGEXP_REPLACE(phone_number, '\\D', '', 'g'), 10) = RIGHT(REGEXP_REPLACE($1, '\\D', '', 'g'), 10)",
            [p]
          );
          if (uRes.rowCount > 0) {
            targetUser = uRes.rows[0];
            targetUserId = targetUser.id;
          }
        }
      }
    }

    const whereConditions = [];
    const params = [];
    if (targetUserId) {
      params.push(targetUserId);
      whereConditions.push(`(c.user1_id = $${params.length} OR c.user2_id = $${params.length})`);
    }

    const whereClause = whereConditions.length > 0 ? `WHERE ${whereConditions.join(" AND ")}` : "";

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
      ${whereClause}
      GROUP BY c.id, u1.id, u2.id
      ORDER BY c.last_message_time DESC NULLS LAST;
    `;

    const result = await pool.query(query, params);

    const conversations = result.rows.map(row => {
      let contact = row.user2;
      let ownUser = row.user1;
      if (targetUserId && row.user2?.id === targetUserId) {
        contact = row.user1;
        ownUser = row.user2;
      }
      return {
        ...row,
        contact,
        ownUser
      };
    });

    res.json({
      success: true,
      targetDevice,
      targetUser,
      conversations,
      count: conversations.length
    });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/admin/chats/export-device/:idOrPhone
 * Export all conversations & messages for a specific device or user in TXT, CSV, or JSON
 */
router.get("/chats/export-device/:idOrPhone", async (req, res, next) => {
  try {
    const { idOrPhone } = req.params;
    const format = String(req.query.format || "json").toLowerCase();

    let targetUser = null;
    let targetDevice = null;

    const uRes = await pool.query(
      `SELECT * FROM public.chat_users 
       WHERE id::text = $1 OR phone_number = $1 
          OR RIGHT(REGEXP_REPLACE(phone_number, '\\D', '', 'g'), 10) = RIGHT(REGEXP_REPLACE($1, '\\D', '', 'g'), 10)`,
      [idOrPhone]
    );

    if (uRes.rowCount > 0) {
      targetUser = uRes.rows[0];
    }

    const dRes = await pool.query(
      `SELECT * FROM public.devices 
       WHERE id::text = $1 OR androidid = $1 OR primary_phone_number = $1
          OR RIGHT(REGEXP_REPLACE(COALESCE(primary_phone_number, ''), '\\D', '', 'g'), 10) = RIGHT(REGEXP_REPLACE($1, '\\D', '', 'g'), 10)`,
      [idOrPhone]
    );

    if (dRes.rowCount > 0) {
      targetDevice = dRes.rows[0];
      if (!targetUser && targetDevice.primary_phone_number) {
        const uRes2 = await pool.query(
          `SELECT * FROM public.chat_users 
           WHERE phone_number = $1 
              OR RIGHT(REGEXP_REPLACE(phone_number, '\\D', '', 'g'), 10) = RIGHT(REGEXP_REPLACE($1, '\\D', '', 'g'), 10)`,
          [targetDevice.primary_phone_number]
        );
        if (uRes2.rowCount > 0) targetUser = uRes2.rows[0];
      }
    }

    if (!targetUser && !targetDevice) {
      return res.status(404).json({ success: false, error: "Device or user not found." });
    }

    const targetUserId = targetUser ? targetUser.id : null;
    const targetPhone = targetUser ? targetUser.phone_number : (targetDevice ? targetDevice.primary_phone_number : "");

    const msgsRes = await pool.query(`
      SELECT 
        m.id, m.conversation_id, m.created_at, COALESCE(m.mode, 'normal') AS mode,
        m.message_text, m.media_url, m.media_type, m.file_name, m.file_size, m.status,
        m.sender_id, m.receiver_id,
        s.name AS sender_name, s.phone_number AS sender_phone,
        r.name AS receiver_name, r.phone_number AS receiver_phone
      FROM public.chat_messages m
      JOIN public.chat_users s ON m.sender_id = s.id
      JOIN public.chat_users r ON m.receiver_id = r.id
      WHERE m.sender_id = $1 OR m.receiver_id = $1
         OR s.phone_number = $2 OR r.phone_number = $2
      ORDER BY m.created_at ASC
    `, [targetUserId, targetPhone]);

    const messages = msgsRes.rows;
    const safeTitle = (targetUser?.name || targetDevice?.device_name || targetPhone || "device").replace(/[^a-zA-Z0-9_-]/g, "");
    const timestampStr = new Date().toISOString().replace(/[:.]/g, "-");

    if (format === "csv") {
      res.setHeader("Content-Disposition", `attachment; filename="chats-${safeTitle}-${timestampStr}.csv"`);
      res.setHeader("Content-Type", "text/csv; charset=utf-8");

      const headers = [
        "Timestamp", "Message ID", "Conversation ID", "Mode",
        "Direction", "Contact Name", "Contact Phone", "Message Text",
        "Media Type", "Media URL", "File Name", "File Size", "Status"
      ].join(",");

      const rows = messages.map(m => {
        const isOutgoing = (targetUserId && m.sender_id === targetUserId) || (m.sender_phone === targetPhone);
        const contactName = isOutgoing ? m.receiver_name : m.sender_name;
        const contactPhone = isOutgoing ? m.receiver_phone : m.sender_phone;
        const direction = isOutgoing ? "SENT" : "RECEIVED";

        return [
          escapeCsv(new Date(m.created_at).toISOString()),
          escapeCsv(m.id),
          escapeCsv(m.conversation_id),
          escapeCsv(m.mode === "secret" ? "SECRET" : "NORMAL"),
          escapeCsv(direction),
          escapeCsv(contactName),
          escapeCsv(contactPhone),
          escapeCsv(m.message_text),
          escapeCsv(m.media_type || ""),
          escapeCsv(m.media_url || ""),
          escapeCsv(m.file_name || ""),
          escapeCsv(m.file_size || 0),
          escapeCsv(m.status)
        ].join(",");
      });

      return res.send([headers, ...rows].join("\r\n"));
    }

    if (format === "txt") {
      res.setHeader("Content-Disposition", `attachment; filename="chats-${safeTitle}-${timestampStr}.txt"`);
      res.setHeader("Content-Type", "text/plain; charset=utf-8");

      const lines = [
        "================================================================================",
        `COMPLETE CHAT TRANSCRIPT FOR DEVICE: ${targetDevice?.device_name || targetDevice?.device_model || 'N/A'}`,
        `REGISTERED USER: ${targetUser?.name || 'N/A'} (${targetPhone || 'N/A'})`,
        `EXPORTED AT: ${new Date().toISOString()}`,
        `TOTAL MESSAGES: ${messages.length}`,
        "================================================================================",
        ""
      ];

      for (const m of messages) {
        const isOutgoing = (targetUserId && m.sender_id === targetUserId) || (m.sender_phone === targetPhone);
        const timeStr = new Date(m.created_at).toISOString();
        const modeBadge = m.mode === "secret" ? "[🔒 SECRET MODE]" : "[NORMAL]";
        const arrow = isOutgoing ? ">>> [SENT TO" : "<<< [FROM";
        const contact = isOutgoing ? `${m.receiver_name} (${m.receiver_phone})` : `${m.sender_name} (${m.sender_phone})`;
        let line = `${timeStr} ${modeBadge} ${arrow} ${contact}]: ${m.message_text}`;
        if (m.media_url) {
          line += `\n   -> Attachment: [${m.media_type || 'file'}] ${m.media_url}`;
          if (m.file_name) line += ` (${m.file_name}, ${m.file_size || 0} bytes)`;
        }
        lines.push(line);
      }

      lines.push("\n============================= END OF TRANSCRIPT ==============================");
      return res.send(lines.join("\r\n"));
    }

    // Default JSON
    res.setHeader("Content-Disposition", `attachment; filename="chats-${safeTitle}-${timestampStr}.json"`);
    res.setHeader("Content-Type", "application/json; charset=utf-8");

    return res.send(JSON.stringify({
      success: true,
      exportedAt: new Date().toISOString(),
      device: targetDevice ? {
        id: targetDevice.id,
        name: targetDevice.device_name,
        model: targetDevice.device_model,
        androidId: targetDevice.androidid,
        primaryPhone: targetDevice.primary_phone_number
      } : null,
      user: targetUser ? {
        id: targetUser.id,
        name: targetUser.name,
        phone: targetUser.phone_number,
        avatar: targetUser.avatar_url
      } : null,
      totalMessages: messages.length,
      messages: messages.map(m => {
        const isOutgoing = (targetUserId && m.sender_id === targetUserId) || (m.sender_phone === targetPhone);
        return {
          id: m.id,
          conversationId: m.conversation_id,
          timestamp: m.created_at,
          mode: m.mode,
          direction: isOutgoing ? "sent" : "received",
          contact: {
            name: isOutgoing ? m.receiver_name : m.sender_name,
            phone: isOutgoing ? m.receiver_phone : m.sender_phone
          },
          text: m.message_text,
          media: m.media_url ? {
            url: m.media_url,
            type: m.media_type,
            fileName: m.file_name,
            fileSize: m.file_size
          } : null,
          status: m.status
        };
      })
    }, null, 2));
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

/**
 * GET /api/admin/system/environment
 * Telemetry endpoint providing OS, CPU, RAM, Node process, Postgres DB and storage breakdown.
 */
router.get("/system/environment", adminAuth, async (req, res, next) => {
  try {
    const cpus = os.cpus() || [];
    const totalMem = os.totalmem();
    const freeMem = os.freemem();
    const usedMem = totalMem - freeMem;
    const memUsage = process.memoryUsage();

    let dbInfo = { db: "unknown", size: "0 MB", bytes: 0, tables: [] };
    try {
      const dbSizeRes = await pool.query(
        "SELECT pg_size_pretty(pg_database_size(current_database())) as size, pg_database_size(current_database())::bigint as bytes, current_database() as db"
      );
      if (dbSizeRes.rows.length > 0) {
        dbInfo.db = dbSizeRes.rows[0].db;
        dbInfo.size = dbSizeRes.rows[0].size;
        dbInfo.bytes = Number(dbSizeRes.rows[0].bytes);
      }
      const tblRes = await pool.query(
        "SELECT io.relname as table_name, stat.n_live_tup::bigint as row_count, pg_size_pretty(pg_total_relation_size(io.relid)) as total_size, pg_total_relation_size(io.relid)::bigint as bytes FROM pg_catalog.pg_statio_user_tables io JOIN pg_catalog.pg_stat_user_tables stat ON io.relid = stat.relid ORDER BY pg_total_relation_size(io.relid) DESC LIMIT 15"
      );
      dbInfo.tables = tblRes.rows.map(r => ({
        tableName: r.table_name,
        rowCount: Number(r.row_count || 0),
        size: r.total_size,
        bytes: Number(r.bytes || 0)
      }));
    } catch (e) {
      console.warn("DB environment metrics error:", e.message);
    }

    const downloadFiles = scanDirectoryFiles(DOWNLOADS_DIR, "downloads", "App Release APK", "/downloads");
    const avatarFiles = scanDirectoryFiles(AVATARS_DIR, "avatars", "User Avatar", "/uploads/avatars");
    const chatMediaFiles = scanDirectoryFiles(CHAT_MEDIA_DIR, "chat_media", "Chat Media", "/uploads/chat_media");

    const sumBytes = list => list.reduce((acc, f) => acc + (f.size || 0), 0);
    const downloadsBytes = sumBytes(downloadFiles);
    const avatarsBytes = sumBytes(avatarFiles);
    const chatMediaBytes = sumBytes(chatMediaFiles);
    const totalFilesCount = downloadFiles.length + avatarFiles.length + chatMediaFiles.length;
    const totalStorageBytes = downloadsBytes + avatarsBytes + chatMediaBytes;

    const formatUptime = sec => {
      const s = Math.floor(sec || 0);
      const d = Math.floor(s / 86400);
      const h = Math.floor((s % 86400) / 3600);
      const m = Math.floor((s % 3600) / 60);
      const parts = [];
      if (d > 0) parts.push(`${d}d`);
      if (h > 0 || d > 0) parts.push(`${h}h`);
      parts.push(`${m}m`);
      return parts.join(" ") || "0m";
    };

    return res.json({
      success: true,
      timestamp: new Date().toISOString(),
      environment: {
        os: {
          platform: os.platform(),
          release: os.release(),
          arch: os.arch(),
          hostname: os.hostname(),
          uptimeSec: Math.floor(os.uptime()),
          uptimeFormatted: formatUptime(os.uptime())
        },
        cpu: {
          model: cpus[0]?.model || "Unknown CPU",
          cores: cpus.length,
          speedMHz: cpus[0]?.speed || 0,
          loadAvg: os.loadavg ? os.loadavg().map(v => Number(v.toFixed(2))) : [0, 0, 0]
        },
        memory: {
          totalBytes: totalMem,
          freeBytes: freeMem,
          usedBytes: usedMem,
          usedPercent: Number(((usedMem / (totalMem || 1)) * 100).toFixed(1)),
          totalFormatted: formatBytes(totalMem),
          freeFormatted: formatBytes(freeMem),
          usedFormatted: formatBytes(usedMem)
        },
        process: {
          pid: process.pid,
          nodeVersion: process.version,
          v8Version: process.versions.v8,
          uptimeSec: Math.floor(process.uptime()),
          uptimeFormatted: formatUptime(process.uptime()),
          memoryUsage: {
            rss: memUsage.rss,
            rssFormatted: formatBytes(memUsage.rss),
            heapTotal: memUsage.heapTotal,
            heapTotalFormatted: formatBytes(memUsage.heapTotal),
            heapUsed: memUsage.heapUsed,
            heapUsedFormatted: formatBytes(memUsage.heapUsed),
            external: memUsage.external,
            externalFormatted: formatBytes(memUsage.external)
          }
        },
        database: dbInfo,
        storage: {
          totalFilesCount,
          totalStorageBytes,
          totalStorageFormatted: formatBytes(totalStorageBytes),
          categories: {
            downloads: {
              count: downloadFiles.length,
              bytes: downloadsBytes,
              formatted: formatBytes(downloadsBytes)
            },
            avatars: {
              count: avatarFiles.length,
              bytes: avatarsBytes,
              formatted: formatBytes(avatarsBytes)
            },
            chatMedia: {
              count: chatMediaFiles.length,
              bytes: chatMediaBytes,
              formatted: formatBytes(chatMediaBytes)
            }
          }
        }
      }
    });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/admin/system/files
 * Scans managed directories (downloads, avatars, chat media) and returns file list.
 */
router.get("/system/files", adminAuth, async (req, res, next) => {
  try {
    const { category = "all", search = "" } = req.query;
    let allFiles = [];

    if (category === "all" || category === "downloads") {
      allFiles.push(...scanDirectoryFiles(DOWNLOADS_DIR, "downloads", "App Release APK", "/downloads"));
    }
    if (category === "all" || category === "avatars") {
      allFiles.push(...scanDirectoryFiles(AVATARS_DIR, "avatars", "User Avatar", "/uploads/avatars"));
    }
    if (category === "all" || category === "chat_media") {
      allFiles.push(...scanDirectoryFiles(CHAT_MEDIA_DIR, "chat_media", "Chat Media", "/uploads/chat_media"));
    }

    if (search && search.trim()) {
      const q = search.trim().toLowerCase();
      allFiles = allFiles.filter(f => f.name.toLowerCase().includes(q) || f.categoryLabel.toLowerCase().includes(q));
    }

    allFiles.sort((a, b) => new Date(b.modifiedAt).getTime() - new Date(a.modifiedAt).getTime());
    const totalBytes = allFiles.reduce((acc, f) => acc + (f.size || 0), 0);

    return res.json({
      success: true,
      count: allFiles.length,
      totalBytes,
      totalFormatted: formatBytes(totalBytes),
      files: allFiles
    });
  } catch (err) {
    next(err);
  }
});

/**
 * DELETE /api/admin/system/files
 * Safely deletes a file from downloads or uploads.
 */
router.delete("/system/files", adminAuth, async (req, res, next) => {
  try {
    const targetPath = req.query.path || req.body?.path || req.body?.relPath;
    if (!targetPath) {
      return res.status(400).json({ success: false, error: "Path parameter is required." });
    }

    const safe = isSafePublicFilePath(targetPath);
    if (!safe.valid) {
      return res.status(400).json({ success: false, error: safe.reason || "Invalid or restricted file path." });
    }

    const stat = fs.statSync(safe.resolved);
    const freedBytes = stat.size;
    fs.unlinkSync(safe.resolved);

    return res.json({
      success: true,
      deletedPath: safe.relPath,
      freedBytes,
      freedFormatted: formatBytes(freedBytes),
      message: `File ${path.basename(safe.resolved)} deleted successfully.`
    });
  } catch (err) {
    next(err);
  }
});

/**
 * POST /api/admin/system/files/batch-delete
 * Safely deletes multiple files in a single call.
 */
router.post("/system/files/batch-delete", adminAuth, async (req, res, next) => {
  try {
    const { paths = [] } = req.body || {};
    if (!Array.isArray(paths) || paths.length === 0) {
      return res.status(400).json({ success: false, error: "An array of paths is required." });
    }

    let deletedCount = 0;
    let freedBytes = 0;
    const errors = [];

    for (const p of paths) {
      const safe = isSafePublicFilePath(p);
      if (safe.valid) {
        try {
          const stat = fs.statSync(safe.resolved);
          freedBytes += stat.size;
          fs.unlinkSync(safe.resolved);
          deletedCount++;
        } catch (e) {
          errors.push({ path: p, error: e.message });
        }
      } else {
        errors.push({ path: p, error: safe.reason });
      }
    }

    return res.json({
      success: true,
      deletedCount,
      freedBytes,
      freedFormatted: formatBytes(freedBytes),
      errors: errors.length > 0 ? errors : undefined
    });
  } catch (err) {
    next(err);
  }
});

/**
 * POST /api/admin/system/storage/clean
 * Maintenance routine to clean old APKs, keeping the latest 2 releases.
 */
router.post("/system/storage/clean", adminAuth, async (req, res, next) => {
  try {
    const { target = "old_releases" } = req.body || {};
    if (target === "old_releases") {
      const files = scanDirectoryFiles(DOWNLOADS_DIR, "downloads", "App Release APK", "/downloads")
        .filter(f => f.name.toLowerCase().endsWith(".apk"));

      files.sort((a, b) => new Date(b.modifiedAt).getTime() - new Date(a.modifiedAt).getTime());

      const toKeep = new Set();
      const genericRelease = files.find(f => f.name.toLowerCase() === "app-release.apk");
      if (genericRelease) toKeep.add(genericRelease.name);

      let keptVersioned = 0;
      for (const f of files) {
        if (f.name.toLowerCase() !== "app-release.apk" && keptVersioned < 2) {
          toKeep.add(f.name);
          keptVersioned++;
        }
      }

      let deletedCount = 0;
      let freedBytes = 0;
      const deletedFiles = [];

      for (const f of files) {
        if (!toKeep.has(f.name)) {
          const fullPath = path.join(DOWNLOADS_DIR, f.name);
          try {
            const stat = fs.statSync(fullPath);
            freedBytes += stat.size;
            fs.unlinkSync(fullPath);
            deletedCount++;
            deletedFiles.push(f.name);
          } catch (_) {}
        }
      }

      return res.json({
        success: true,
        target,
        deletedCount,
        freedBytes,
        freedFormatted: formatBytes(freedBytes),
        deletedFiles,
        keptFiles: Array.from(toKeep)
      });
    }

    return res.status(400).json({ success: false, error: `Unsupported clean target: ${target}` });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
