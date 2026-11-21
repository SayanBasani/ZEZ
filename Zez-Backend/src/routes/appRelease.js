const express = require("express");
const path = require("node:path");
const fs = require("node:fs");
const multer = require("multer");
const { pool } = require("../db");
const { adminAuth } = require("../middleware/adminAuth");
const b2Storage = require("../services/b2StorageService");

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

      let downloadUrl = `/downloads/${req.file.filename}`;
      let objectKey = null;
      const uploadedBy = req.admin ? req.admin.username : "admin";

      // Upload directly to Backblaze B2 Cloud Object Storage
      try {
        const fileBuffer = fs.readFileSync(req.file.path);
        const uploadResult = await b2Storage.uploadFile({
          buffer: fileBuffer,
          originalName: req.file.filename,
          category: "releases",
          mimeType: "application/vnd.android.package-archive"
        });
        objectKey = uploadResult.objectKey;
        downloadUrl = `/api/storage/file/${uploadResult.objectKey}`;

        // Save reference in storage_files
        await pool.query(
          `INSERT INTO public.storage_files 
           (bucket_name, object_key, original_name, stored_name, mime_type, file_size, file_category, uploaded_by, upload_status, metadata)
           VALUES ($1, $2, $3, $4, $5, $6, $7, $8, 'uploaded', $9)
           ON CONFLICT (object_key) DO NOTHING`,
          [
            uploadResult.bucketName,
            uploadResult.objectKey,
            req.file.originalname || req.file.filename,
            req.file.filename,
            "application/vnd.android.package-archive",
            req.file.size,
            "releases",
            uploadedBy,
            JSON.stringify({ etag: uploadResult.etag })
          ]
        );

        // Remove from local disk to keep backend server completely uncluttered
        if (fs.existsSync(req.file.path)) {
          try { fs.unlinkSync(req.file.path); } catch {}
        }
      } catch (b2Err) {
        console.warn("Backblaze B2 release upload error, keeping local file:", b2Err.message);
      }

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

    if (row.download_url && row.download_url.startsWith("/api/storage/file/")) {
      const b2Key = row.download_url.replace("/api/storage/file/", "");
      try { await b2Storage.deleteFile(b2Key); } catch (e) {
        console.warn("Could not remove APK from B2:", e.message);
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
