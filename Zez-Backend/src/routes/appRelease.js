const express = require("express");
const crypto = require("node:crypto");
const path = require("node:path");
const fs = require("node:fs");
const multer = require("multer");
const { pool } = require("../db");
const { adminAuth } = require("../middleware/adminAuth");
const b2Storage = require("../services/b2StorageService");

const router = express.Router();

const ALLOWED_APP_IDS = new Set(["quacky", "zez"]);
const DEFAULT_PACKAGES = {
  quacky: "com.zez.app",
  zez: "com.zez.manager",
};

const downloadsDir = path.join(__dirname, "../public/downloads");
if (!fs.existsSync(downloadsDir)) {
  fs.mkdirSync(downloadsDir, { recursive: true });
}

const storageCacheDir = path.join(__dirname, "../cache/storage");

function parseAppId(raw, fallback = null) {
  if (raw === undefined || raw === null || String(raw).trim() === "") {
    return fallback;
  }
  const normalized = String(raw).trim().toLowerCase();
  if (!ALLOWED_APP_IDS.has(normalized)) {
    return null;
  }
  return normalized;
}

function parseBool(val, defaultVal = false) {
  if (val === undefined || val === null || val === "") return defaultVal;
  if (typeof val === "boolean") return val;
  const s = String(val).trim().toLowerCase();
  return s === "true" || s === "1" || s === "yes" || s === "on";
}

function isValidZipApk(filePath) {
  const fd = fs.openSync(filePath, "r");
  try {
    const header = Buffer.alloc(4);
    const bytesRead = fs.readSync(fd, header, 0, 4, 0);
    return bytesRead >= 2 && header[0] === 0x50 && header[1] === 0x4b; // 'PK'
  } finally {
    fs.closeSync(fd);
  }
}

function sha256File(filePath) {
  const hash = crypto.createHash("sha256");
  const fd = fs.openSync(filePath, "r");
  try {
    const buf = Buffer.alloc(65536);
    let bytesRead;
    while ((bytesRead = fs.readSync(fd, buf, 0, buf.length, null)) > 0) {
      hash.update(buf.subarray(0, bytesRead));
    }
  } finally {
    fs.closeSync(fd);
  }
  return hash.digest("hex");
}

function formatReleaseRow(row, clientVersionCode = null) {
  const minSupported = Number(row.minimum_supported_version) || 1;
  const mandatoryFlag = row.update_required === true;
  const forceUpdate =
    mandatoryFlag ||
    (Number.isInteger(clientVersionCode) && clientVersionCode > 0 && clientVersionCode < minSupported);
  const updateAvailable =
    Number.isInteger(clientVersionCode) && clientVersionCode > 0
      ? Number(row.version_code) > clientVersionCode
      : null;

  return {
    releaseId: row.id,
    id: row.id,
    appId: row.app_id,
    versionName: row.version_name,
    versionCode: Number(row.version_code),
    releaseNotes: row.release_notes || "Performance optimizations and bug fixes.",
    fileName: row.file_name,
    fileSize: Number(row.file_size),
    sha256Checksum: row.sha256_checksum || null,
    minimumSupportedVersion: minSupported,
    updateRequired: mandatoryFlag,
    forceUpdate,
    releaseStatus: row.release_status || (row.is_active ? "published" : "disabled"),
    packageName: row.package_name || DEFAULT_PACKAGES[row.app_id] || null,
    isActive: row.is_active === true,
    updateAvailable,
    downloadUrl: row.download_url,
    directDownloadUrl: `/api/app/download/${row.id}`,
    createdAt: row.created_at,
    publishedAt: row.published_at || row.created_at,
  };
}

// Multer storage configuration for APK files
const storage = multer.diskStorage({
  destination: (_req, _file, cb) => {
    cb(null, downloadsDir);
  },
  filename: (req, _file, cb) => {
    const appId = parseAppId(req.body?.appId, "quacky") || "app";
    const versionName = (req.body?.versionName || "update").replace(/[^a-zA-Z0-9.-]/g, "_");
    cb(null, `${appId}-release-v${versionName}-${Date.now()}.apk`);
  },
});

const upload = multer({
  storage,
  limits: {
    fileSize: 120 * 1024 * 1024, // 120 MB max
  },
  fileFilter: (_req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    if (ext !== ".apk") {
      return cb(new Error("Only Android APK files (.apk) are accepted."));
    }
    cb(null, true);
  },
});

/**
 * Public Endpoint: GET /api/app/latest?appId=quacky|zez&currentVersionCode=1
 * Backward-compatible: defaults to 'quacky' only when appId is omitted,
 * rejects unknown non-empty appId values with 400 to guarantee app isolation.
 */
router.get("/latest", async (req, res, next) => {
  try {
    res.set("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
    res.set("Pragma", "no-cache");

    const appId = parseAppId(req.query.appId, "quacky");
    if (!appId) {
      return res.status(400).json({
        success: false,
        error: "Unsupported application identifier. Expected 'quacky' or 'zez'.",
      });
    }

    const rawCode = req.query.currentVersionCode ?? req.query.versionCode;
    const parsedCode = rawCode !== undefined ? parseInt(String(rawCode), 10) : null;
    const clientVersionCode = parsedCode !== null && !Number.isNaN(parsedCode) ? parsedCode : null;

    const result = await pool.query(
      `SELECT id, app_id, version_name, version_code, release_notes, file_name, file_size,
              download_url, sha256_checksum, minimum_supported_version, update_required,
              release_status, package_name, is_active, created_at, published_at
       FROM public.app_releases
       WHERE app_id = $1
         AND is_active = TRUE
         AND COALESCE(release_status, 'published') = 'published'
       ORDER BY version_code DESC, id DESC
       LIMIT 1`,
      [appId]
    );

    if (result.rows.length === 0) {
      return res.json({
        success: true,
        hasRelease: false,
        appId,
        message: `No active releases published yet for '${appId}'.`,
      });
    }

    const formatted = formatReleaseRow(result.rows[0], clientVersionCode);
    res.json({
      success: true,
      hasRelease: true,
      ...formatted,
    });
  } catch (err) {
    next(err);
  }
});

/**
 * Public Endpoint: GET /api/app/rollback?appId=quacky|zez&currentVersionCode=...
 * Returns the immediate previous active published release for this app,
 * strictly governed by the administrator rollback toggle setting.
 */
router.get("/rollback", async (req, res, next) => {
  try {
    res.set("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
    res.set("Pragma", "no-cache");

    const appId = parseAppId(req.query.appId, "quacky");
    if (!appId) {
      return res.status(400).json({
        success: false,
        rollbackAvailable: false,
        error: "Unsupported application identifier. Expected 'quacky' or 'zez'.",
      });
    }

    // 1. Check if rollback is allowed by admin for this app
    const settingKey = `rollback_enabled_${appId}`;
    const settingRes = await pool.query(
      "SELECT setting_value FROM public.app_release_settings WHERE setting_key = $1",
      [settingKey]
    );
    const isRollbackEnabled =
      settingRes.rows.length === 0 || parseBool(settingRes.rows[0].setting_value, true);

    if (!isRollbackEnabled) {
      return res.json({
        success: true,
        rollbackAvailable: false,
        appId,
        reason: `Rollback for ${appId.toUpperCase()} is currently disabled by administrator.`,
      });
    }

    // 2. Query previous release
    const rawCode = req.query.currentVersionCode ?? req.query.versionCode;
    const parsedCode = rawCode !== undefined ? parseInt(String(rawCode), 10) : null;
    const clientVersionCode = parsedCode !== null && !Number.isNaN(parsedCode) ? parsedCode : null;

    let query;
    let params;

    if (clientVersionCode !== null && clientVersionCode > 0) {
      query = `
        SELECT id, app_id, version_name, version_code, release_notes, file_name, file_size,
               download_url, sha256_checksum, minimum_supported_version, update_required,
               release_status, package_name, is_active, created_at, published_at
        FROM public.app_releases
        WHERE app_id = $1
          AND COALESCE(release_status, 'published') <> 'archived'
          AND version_code < $2
        ORDER BY version_code DESC, id DESC
        LIMIT 1
      `;
      params = [appId, clientVersionCode];
    } else {
      query = `
        SELECT id, app_id, version_name, version_code, release_notes, file_name, file_size,
               download_url, sha256_checksum, minimum_supported_version, update_required,
               release_status, package_name, is_active, created_at, published_at
        FROM public.app_releases
        WHERE app_id = $1
          AND COALESCE(release_status, 'published') <> 'archived'
        ORDER BY version_code DESC, id DESC
        OFFSET 1
        LIMIT 1
      `;
      params = [appId];
    }

    const prevRes = await pool.query(query, params);
    if (prevRes.rows.length === 0) {
      return res.json({
        success: true,
        rollbackAvailable: false,
        appId,
        reason: "No previous version available to roll back to.",
      });
    }

    const formatted = formatReleaseRow(prevRes.rows[0], clientVersionCode);
    return res.json({
      success: true,
      rollbackAvailable: true,
      ...formatted,
    });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/app/settings/rollback
 * Get rollback enable status for Quacky and ZEZ.
 */
router.get("/settings/rollback", async (req, res, next) => {
  try {
    const sRes = await pool.query(
      "SELECT setting_key, setting_value FROM public.app_release_settings WHERE setting_key IN ('rollback_enabled_quacky', 'rollback_enabled_zez')"
    );
    const map = {};
    for (const r of sRes.rows) {
      map[r.setting_key] = parseBool(r.setting_value, true);
    }
    res.json({
      success: true,
      quackyRollbackEnabled: map["rollback_enabled_quacky"] !== false,
      zezRollbackEnabled: map["rollback_enabled_zez"] !== false,
    });
  } catch (err) {
    next(err);
  }
});

/**
 * POST /api/app/settings/rollback
 * Admin toggles rollback availability for Quacky and/or ZEZ.
 */
router.post("/settings/rollback", adminAuth, async (req, res, next) => {
  try {
    const { quackyRollbackEnabled, zezRollbackEnabled } = req.body || {};

    if (quackyRollbackEnabled !== undefined) {
      await pool.query(
        `INSERT INTO public.app_release_settings (setting_key, setting_value, updated_at)
         VALUES ('rollback_enabled_quacky', $1, NOW())
         ON CONFLICT (setting_key) DO UPDATE SET setting_value = $1, updated_at = NOW()`,
        [String(quackyRollbackEnabled === true)]
      );
    }
    if (zezRollbackEnabled !== undefined) {
      await pool.query(
        `INSERT INTO public.app_release_settings (setting_key, setting_value, updated_at)
         VALUES ('rollback_enabled_zez', $1, NOW())
         ON CONFLICT (setting_key) DO UPDATE SET setting_value = $1, updated_at = NOW()`,
        [String(zezRollbackEnabled === true)]
      );
    }

    const sRes = await pool.query(
      "SELECT setting_key, setting_value FROM public.app_release_settings WHERE setting_key IN ('rollback_enabled_quacky', 'rollback_enabled_zez')"
    );
    const map = {};
    for (const r of sRes.rows) {
      map[r.setting_key] = parseBool(r.setting_value, true);
    }

    res.json({
      success: true,
      message: "Rollback settings successfully updated.",
      quackyRollbackEnabled: map["rollback_enabled_quacky"] !== false,
      zezRollbackEnabled: map["rollback_enabled_zez"] !== false,
    });
  } catch (err) {
    next(err);
  }
});

/**
 * Public Endpoint: GET /api/app/download/:id
 * Downloads an APK release with SHA-256 header and local/B2 fallback.
 */
router.get("/download/:id", async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (!id || Number.isNaN(id)) {
      return res.status(400).json({ success: false, error: "Invalid release ID." });
    }

    const rowRes = await pool.query("SELECT * FROM public.app_releases WHERE id = $1", [id]);
    if (rowRes.rowCount === 0) {
      return res.status(404).json({ success: false, error: "Release not found." });
    }

    const row = rowRes.rows[0];
    const cleanFileName = path.basename(row.file_name || `${row.app_id || 'app'}-release.apk`);
    const expectedSize = Number(row.file_size) || null;

    if (row.sha256_checksum) {
      res.setHeader("X-Checksum-Sha256", row.sha256_checksum);
    }
    res.setHeader("X-Application-Id", row.app_id || "quacky");
    res.setHeader("Content-Type", "application/vnd.android.package-archive");
    res.setHeader("Content-Disposition", `attachment; filename="${cleanFileName}"`);

    // 1. Check local disk candidates (downloadsDir or storageCacheDir)
    const candidates = [];
    if (row.download_url && row.download_url.startsWith("/downloads/")) {
      candidates.push(path.join(downloadsDir, path.basename(row.download_url)));
    }
    if (row.file_name) {
      candidates.push(path.join(downloadsDir, path.basename(row.file_name)));
    }
    if (row.download_url && row.download_url.startsWith("/api/storage/file/")) {
      const safeKey = b2Storage.sanitizeKey(row.download_url.replace("/api/storage/file/", ""));
      candidates.push(path.join(storageCacheDir, safeKey));
    }

    let validCandidate = null;
    for (const candidate of candidates) {
      if (candidate && fs.existsSync(candidate)) {
        try {
          const st = fs.statSync(candidate);
          if (expectedSize && st.size === expectedSize && st.size > 0) {
            validCandidate = candidate;
            break;
          } else if (!expectedSize && st.size > 0) {
            validCandidate = candidate;
            break;
          } else if (expectedSize && st.size !== expectedSize) {
            // Delete truncated or corrupt cached file
            try { fs.unlinkSync(candidate); } catch (_) {}
          }
        } catch (_) {}
      }
    }

    if (validCandidate) {
      return res.sendFile(path.resolve(validCandidate), {
        acceptRanges: true,
        cacheControl: true,
        maxAge: 86400000,
        headers: {
          "Content-Disposition": `attachment; filename="${cleanFileName}"`,
          "Content-Type": "application/vnd.android.package-archive",
          "Accept-Ranges": "bytes"
        }
      });
    }

    // 2. Stream directly from Backblaze B2 and cache atomically
    if (row.download_url && row.download_url.startsWith("/api/storage/file/")) {
      const safeKey = b2Storage.sanitizeKey(row.download_url.replace("/api/storage/file/", ""));
      const cacheDest = path.join(storageCacheDir, safeKey);
      const rangeHeader = req.headers.range || null;

      try {
        const b2Res = await b2Storage.getFileStream(safeKey, rangeHeader);
        if (b2Res && b2Res.stream) {
          res.status(b2Res.statusCode || 200);
          res.setHeader("Content-Type", "application/vnd.android.package-archive");
          res.setHeader("Content-Disposition", `attachment; filename="${cleanFileName}"`);
          res.setHeader("Accept-Ranges", "bytes");
          if (b2Res.contentLength !== undefined) {
            res.setHeader("Content-Length", String(b2Res.contentLength));
          }
          if (b2Res.contentRange) {
            res.setHeader("Content-Range", String(b2Res.contentRange));
          }

          let writeStream = null;
          let tmpFile = null;
          let streamEnded = false;

          if (!rangeHeader && (!b2Res.statusCode || b2Res.statusCode === 200)) {
            try {
              fs.mkdirSync(path.dirname(cacheDest), { recursive: true });
              tmpFile = `${cacheDest}.tmp.${Date.now()}`;
              writeStream = fs.createWriteStream(tmpFile);
              b2Res.stream.pipe(writeStream);

              writeStream.on("finish", () => {
                try {
                  if (streamEnded && tmpFile && fs.existsSync(tmpFile)) {
                    const st = fs.statSync(tmpFile);
                    if (expectedSize ? st.size === expectedSize : st.size > 0) {
                      fs.renameSync(tmpFile, cacheDest);
                    } else {
                      fs.unlinkSync(tmpFile);
                    }
                  } else if (tmpFile && fs.existsSync(tmpFile)) {
                    fs.unlinkSync(tmpFile);
                  }
                } catch (_) {}
              });

              writeStream.on("error", () => {
                try { if (tmpFile && fs.existsSync(tmpFile)) fs.unlinkSync(tmpFile); } catch (_) {}
              });
            } catch (_) {}
          }

          b2Res.stream.on("end", () => {
            streamEnded = true;
          });

          b2Res.stream.on("error", (sErr) => {
            console.warn("[Download] B2 stream error:", sErr.message);
            if (writeStream) writeStream.destroy();
            try { if (tmpFile && fs.existsSync(tmpFile)) fs.unlinkSync(tmpFile); } catch (_) {}
            if (!res.writableEnded) res.destroy(sErr);
          });

          res.on("close", () => {
            if (!streamEnded && b2Res.stream && typeof b2Res.stream.destroy === "function") {
              b2Res.stream.destroy();
            }
            if (!streamEnded && writeStream) {
              writeStream.destroy();
              try { if (tmpFile && fs.existsSync(tmpFile)) fs.unlinkSync(tmpFile); } catch (_) {}
            }
          });

          return b2Res.stream.pipe(res);
        }
      } catch (b2StreamErr) {
        console.warn("[Download] B2 getFileStream failed, trying getFile:", b2StreamErr.message);
      }

      try {
        const fileData = await b2Storage.getFile(safeKey);
        if (fileData && fileData.body) {
          res.setHeader("Content-Disposition", `attachment; filename="${cleanFileName}"`);
          res.setHeader("Content-Type", "application/vnd.android.package-archive");
          if (fileData.contentLength || fileData.body.length) {
            res.setHeader("Content-Length", String(fileData.contentLength || fileData.body.length));
          }
          // Cache buffer to disk atomically
          try {
            fs.mkdirSync(path.dirname(cacheDest), { recursive: true });
            fs.writeFileSync(cacheDest, fileData.body);
          } catch (_) {}
          return res.send(fileData.body);
        }
      } catch (b2Err) {
        console.warn("[Download] B2 getFile fallback failed:", b2Err.message);
      }

      return res.redirect(row.download_url);
    }

    return res.status(404).json({ success: false, error: "APK binary not found on server or cloud storage." });
  } catch (err) {
    next(err);
  }
});

/**
 * Protected Endpoints below require valid admin session
 */

/**
 * GET /api/app/releases?appId=quacky|zez
 */
router.get("/releases", adminAuth, async (req, res, next) => {
  try {
    const rawApp = req.query.appId;
    let appFilter = null;
    if (rawApp && String(rawApp).trim().toLowerCase() !== "all") {
      appFilter = parseAppId(rawApp, null);
      if (!appFilter) {
        return res.status(400).json({
          success: false,
          error: "Unsupported application identifier. Use 'quacky', 'zez', or 'all'.",
        });
      }
    }

    const params = [];
    let sql = `
      SELECT id, app_id, version_name, version_code, release_notes, file_name, file_size,
             download_url, sha256_checksum, minimum_supported_version, update_required,
             release_status, package_name, is_active, uploaded_by, created_at, published_at
      FROM public.app_releases
    `;
    if (appFilter) {
      params.push(appFilter);
      sql += ` WHERE app_id = $1 `;
    }
    sql += ` ORDER BY version_code DESC, id DESC`;

    const result = await pool.query(sql, params);
    res.json({
      success: true,
      appId: appFilter || "all",
      releases: result.rows,
    });
  } catch (err) {
    next(err);
  }
});

/**
 * POST /api/app/upload
 * Upload and optionally publish new APK release independently per application_id ('quacky' | 'zez').
 */
router.post("/upload", adminAuth, (req, res, next) => {
  upload.single("apkFile")(req, res, async (err) => {
    if (err) {
      return res.status(400).json({ success: false, error: err.message });
    }

    try {
      if (!req.file) {
        return res.status(400).json({ success: false, error: "Please select an APK file to upload." });
      }

      const body = req.body || {};
      const appId = parseAppId(body.appId, "quacky");
      if (!appId) {
        if (fs.existsSync(req.file.path)) fs.unlinkSync(req.file.path);
        return res.status(400).json({
          success: false,
          error: "Unsupported application identifier. Expected 'quacky' or 'zez'.",
        });
      }

      if (!isValidZipApk(req.file.path)) {
        if (fs.existsSync(req.file.path)) fs.unlinkSync(req.file.path);
        return res.status(400).json({
          success: false,
          error: "Uploaded file is not a valid Android APK archive (missing PK signature).",
        });
      }

      const cleanVersionName = String(body.versionName || "").trim();
      const numVersionCode = parseInt(body.versionCode, 10);
      const minSupportedRaw = parseInt(body.minimumSupportedVersion ?? "1", 10);
      const minSupportedVersion = Number.isNaN(minSupportedRaw) || minSupportedRaw < 1 ? 1 : minSupportedRaw;
      const updateRequired = parseBool(body.updateRequired, false);
      const publishImmediately = parseBool(body.publishImmediately ?? body.publish, true);
      const packageName =
        String(body.packageName || "").trim() || DEFAULT_PACKAGES[appId];

      if (!cleanVersionName) {
        if (fs.existsSync(req.file.path)) fs.unlinkSync(req.file.path);
        return res.status(400).json({ success: false, error: "Version Name (e.g. 1.0.1) is required." });
      }

      if (Number.isNaN(numVersionCode) || numVersionCode <= 0) {
        if (fs.existsSync(req.file.path)) fs.unlinkSync(req.file.path);
        return res.status(400).json({ success: false, error: "Version Code must be a positive integer." });
      }

      const sha256Checksum = sha256File(req.file.path);
      const uploadedBy = req.admin ? req.admin.username : "admin";
      const safeVer = cleanVersionName.replace(/[^a-zA-Z0-9.-]/g, "_");
      const canonicalFileName = `${appId}-release-${safeVer}.apk`;
      const diskFileName = req.file.filename;
      let downloadUrl = `/downloads/${diskFileName}`;

      // Copy into local public/downloads cleanly as canonical name too, and upload to B2 if configured
      const releaseKey = `releases/${appId}/${appId}-release-${safeVer}-${Date.now()}.apk`;
      try {
        const fileBuffer = fs.readFileSync(req.file.path);
        const uploadResult = await b2Storage.uploadFile({
          buffer: fileBuffer,
          key: releaseKey,
          originalName: canonicalFileName,
          category: "releases",
          mimeType: "application/vnd.android.package-archive",
        });
        if (uploadResult && uploadResult.objectKey) {
          downloadUrl = `/api/storage/file/${uploadResult.objectKey}`;
          await pool.query(
            `INSERT INTO public.media_files
             (storage_provider, bucket_name, object_key, original_file_name, mime_type, file_size, file_category, uploaded_by, upload_status, metadata)
             VALUES ('backblaze_b2', $1, $2, $3, $4, $5, 'releases', $6, 'completed', $7)
             ON CONFLICT (object_key) DO NOTHING`,
            [
              uploadResult.bucketName,
              uploadResult.objectKey,
              canonicalFileName,
              "application/vnd.android.package-archive",
              req.file.size,
              uploadedBy,
              JSON.stringify({ etag: uploadResult.etag, appId, sha256Checksum }),
            ]
          ).catch(() => {});

          // Cache in storageCacheDir so /api/storage/file/ serves immediately from disk as well
          try {
            const cacheDest = path.join(storageCacheDir, b2Storage.sanitizeKey(uploadResult.objectKey));
            fs.mkdirSync(path.dirname(cacheDest), { recursive: true });
            fs.copyFileSync(req.file.path, cacheDest);
            fs.unlinkSync(req.file.path);
          } catch {}
        }
      } catch (b2Err) {
        console.warn("Backblaze B2 release upload note, serving from local downloads:", b2Err.message);
      }

      if (publishImmediately) {
        await pool.query(
          "UPDATE public.app_releases SET is_active = FALSE WHERE app_id = $1 AND is_active = TRUE",
          [appId]
        );
      }

      const releaseStatus = publishImmediately ? "published" : "disabled";
      const insertResult = await pool.query(
        `INSERT INTO public.app_releases
         (app_id, version_name, version_code, release_notes, file_name, file_size, download_url,
          sha256_checksum, minimum_supported_version, update_required, release_status, package_name,
          is_active, uploaded_by, published_at)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15)
         RETURNING *`,
        [
          appId,
          cleanVersionName,
          numVersionCode,
          body.releaseNotes || "Performance optimizations and bug fixes.",
          downloadUrl.startsWith("/downloads/") ? diskFileName : canonicalFileName,
          req.file.size,
          downloadUrl,
          sha256Checksum,
          minSupportedVersion,
          updateRequired,
          releaseStatus,
          packageName,
          publishImmediately,
          uploadedBy,
          publishImmediately ? new Date() : null,
        ]
      );

      res.status(201).json({
        success: true,
        message: `${appId.toUpperCase()} v${cleanVersionName} (Code ${numVersionCode}) ${
          publishImmediately ? "published" : "uploaded (draft/disabled)"
        } successfully!`,
        release: insertResult.rows[0],
      });
    } catch (dbErr) {
      if (req.file && fs.existsSync(req.file.path)) {
        try { fs.unlinkSync(req.file.path); } catch {}
      }
      next(dbErr);
    }
  });
});

/**
 * POST /api/app/releases/:id/publish
 * Publish or roll back to a specific existing release for its application_id.
 */
router.post("/releases/:id/publish", adminAuth, async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (!id || Number.isNaN(id)) {
      return res.status(400).json({ success: false, error: "Invalid release ID." });
    }

    const rowRes = await pool.query("SELECT * FROM public.app_releases WHERE id = $1", [id]);
    if (rowRes.rowCount === 0) {
      return res.status(404).json({ success: false, error: "Release not found." });
    }

    const row = rowRes.rows[0];
    const appId = row.app_id || "quacky";

    await pool.query(
      "UPDATE public.app_releases SET is_active = FALSE WHERE app_id = $1 AND is_active = TRUE",
      [appId]
    );
    const updated = await pool.query(
      `UPDATE public.app_releases
       SET is_active = TRUE, release_status = 'published', published_at = NOW()
       WHERE id = $1
       RETURNING *`,
      [id]
    );

    res.json({
      success: true,
      message: `${appId.toUpperCase()} v${row.version_name} (Code ${row.version_code}) is now the active published release.`,
      release: updated.rows[0],
    });
  } catch (err) {
    next(err);
  }
});

/**
 * PATCH /api/app/releases/:id
 * Enable/disable a release or update mandatory/min-supported flags.
 */
router.patch("/releases/:id", adminAuth, async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (!id || Number.isNaN(id)) {
      return res.status(400).json({ success: false, error: "Invalid release ID." });
    }

    const rowRes = await pool.query("SELECT * FROM public.app_releases WHERE id = $1", [id]);
    if (rowRes.rowCount === 0) {
      return res.status(404).json({ success: false, error: "Release not found." });
    }

    const existing = rowRes.rows[0];
    const body = req.body || {};

    let nextActive = existing.is_active === true;
    let nextStatus = existing.release_status || (nextActive ? "published" : "disabled");

    if (body.isActive !== undefined || body.is_active !== undefined) {
      nextActive = parseBool(body.isActive ?? body.is_active, nextActive);
      nextStatus = nextActive ? "published" : "disabled";
    }
    if (body.releaseStatus !== undefined || body.release_status !== undefined) {
      const s = String(body.releaseStatus ?? body.release_status).trim().toLowerCase();
      if (s === "published" || s === "disabled" || s === "archived") {
        nextStatus = s;
        if (s !== "published") nextActive = false;
      }
    }

    if (nextActive) {
      await pool.query(
        "UPDATE public.app_releases SET is_active = FALSE WHERE app_id = $1 AND id <> $2 AND is_active = TRUE",
        [existing.app_id, id]
      );
    }

    const minSupported =
      body.minimumSupportedVersion !== undefined
        ? Math.max(1, parseInt(body.minimumSupportedVersion, 10) || 1)
        : existing.minimum_supported_version || 1;
    const updateRequired =
      body.updateRequired !== undefined ? parseBool(body.updateRequired, false) : existing.update_required === true;
    const releaseNotes =
      body.releaseNotes !== undefined ? String(body.releaseNotes) : existing.release_notes;

    const updated = await pool.query(
      `UPDATE public.app_releases
       SET is_active = $2,
           release_status = $3,
           minimum_supported_version = $4,
           update_required = $5,
           release_notes = $6,
           published_at = CASE WHEN $2 = TRUE THEN COALESCE(published_at, NOW()) ELSE published_at END
       WHERE id = $1
       RETURNING *`,
      [id, nextActive, nextStatus, minSupported, updateRequired, releaseNotes]
    );

    res.json({
      success: true,
      message: `Release v${existing.version_name} updated (${nextStatus}${nextActive ? ", active" : ""}).`,
      release: updated.rows[0],
    });
  } catch (err) {
    next(err);
  }
});

/**
 * DELETE /api/app/releases/:id
 * Delete release from database and disk/B2.
 */
router.delete("/releases/:id", adminAuth, async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (!id || Number.isNaN(id)) {
      return res.status(400).json({ success: false, error: "Invalid release ID." });
    }

    const rowRes = await pool.query("SELECT * FROM public.app_releases WHERE id = $1", [id]);
    if (rowRes.rowCount === 0) {
      return res.status(404).json({ success: false, error: "Release not found." });
    }

    const row = rowRes.rows[0];
    const candidates = [
      row.file_name ? path.join(downloadsDir, path.basename(row.file_name)) : null,
      row.download_url && row.download_url.startsWith("/downloads/")
        ? path.join(downloadsDir, path.basename(row.download_url))
        : null,
    ].filter(Boolean);

    for (const filePath of candidates) {
      if (fs.existsSync(filePath)) {
        try { fs.unlinkSync(filePath); } catch {}
      }
    }

    if (row.download_url && row.download_url.startsWith("/api/storage/file/")) {
      const b2Key = row.download_url.replace("/api/storage/file/", "");
      try { await b2Storage.deleteFile(b2Key); } catch {}
    }

    await pool.query("DELETE FROM public.app_releases WHERE id = $1", [id]);

    res.json({
      success: true,
      message: `Release v${row.version_name} (Code ${row.version_code}) deleted.`,
    });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
