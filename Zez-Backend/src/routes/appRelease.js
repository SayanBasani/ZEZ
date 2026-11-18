const express = require("express");
const path = require("node:path");
const fs = require("node:fs");
const multer = require("multer");
const { pool } = require("../db");
const { adminAuth } = require("../middleware/adminAuth");

const router = express.Router();

// Ensure downloads directory exists
const downloadsDir = path.join(__dirname, "../public/downloads");
if (!fs.existsSync(downloadsDir)) {
  fs.mkdirSync(downloadsDir, { recursive: true });
}

// Multer storage configuration for APK files
const storage = multer.diskStorage({
  destination: (_req, _file, cb) => {
    cb(null, downloadsDir);
  },
  filename: (req, file, cb) => {
    const versionName = (req.body.versionName || "update").replace(/[^a-zA-Z0-9.-]/g, "_");
    const uniqueSuffix = Date.now();
    cb(null, `zez-app-v${versionName}-${uniqueSuffix}.apk`);
  },
});

const upload = multer({
  storage,
  limits: {
    fileSize: 100 * 1024 * 1024, // 100 MB max
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
 * Public Endpoint: GET /api/app/latest
 * Used by Android client to check for new releases
 */
router.get("/latest", async (_req, res, next) => {
  try {
    const result = await pool.query(
      `SELECT id, version_name, version_code, release_notes, file_name, file_size, download_url, created_at
       FROM public.app_releases
       WHERE is_active = TRUE
       ORDER BY version_code DESC, id DESC
       LIMIT 1`
    );

    if (result.rows.length === 0) {
      return res.json({
        success: true,
        hasRelease: false,
        message: "No releases published yet.",
      });
    }

    const row = result.rows[0];
    res.json({
      success: true,
      hasRelease: true,
      id: row.id,
      versionName: row.version_name,
      versionCode: row.version_code,
      releaseNotes: row.release_notes || "General performance improvements and bug fixes.",
      fileName: row.file_name,
      fileSize: Number(row.file_size),
      downloadUrl: row.download_url,
      createdAt: row.created_at,
    });
  } catch (err) {
    next(err);
  }
});

/**
 * Protected Endpoints below require valid admin session
 */

/**
 * GET /api/app/releases
 * List all releases for admin dashboard
 */
router.get("/releases", adminAuth, async (_req, res, next) => {
  try {
    const result = await pool.query(
      `SELECT id, version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by, created_at
       FROM public.app_releases
       ORDER BY version_code DESC, id DESC`
    );
    res.json({
      success: true,
      releases: result.rows,
    });
  } catch (err) {
    next(err);
  }
});

/**
 * POST /api/app/upload
 * Upload and publish new APK release
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

      const { versionName, versionCode, releaseNotes } = req.body || {};
      const cleanVersionName = String(versionName || "").trim();
      const numVersionCode = parseInt(versionCode, 10);

      if (!cleanVersionName) {
        fs.unlinkSync(req.file.path);
        return res.status(400).json({ success: false, error: "Version Name (e.g. 1.0.1) is required." });
      }

      if (Number.isNaN(numVersionCode) || numVersionCode <= 0) {
        fs.unlinkSync(req.file.path);
        return res.status(400).json({ success: false, error: "Version Code must be a positive integer." });
      }

      const downloadUrl = `/downloads/${req.file.filename}`;
      const uploadedBy = req.admin ? req.admin.username : "admin";

      const insertResult = await pool.query(
        `INSERT INTO public.app_releases (version_name, version_code, release_notes, file_name, file_size, download_url, is_active, uploaded_by)
         VALUES ($1, $2, $3, $4, $5, $6, TRUE, $7)
         RETURNING *`,
        [
          cleanVersionName,
          numVersionCode,
          releaseNotes || "Performance optimizations and bug fixes.",
          req.file.filename,
          req.file.size,
          downloadUrl,
          uploadedBy,
        ]
      );

      res.status(201).json({
        success: true,
        message: `Version ${cleanVersionName} (Code ${numVersionCode}) published successfully!`,
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
 * DELETE /api/app/releases/:id
 * Delete release from database and disk
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
    const filePath = path.join(downloadsDir, row.file_name);
    if (fs.existsSync(filePath)) {
      try { fs.unlinkSync(filePath); } catch (e) {
        console.warn("Could not remove APK file from disk:", e.message);
      }
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
