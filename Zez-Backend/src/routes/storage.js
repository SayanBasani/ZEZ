const express = require("express");
const multer = require("multer");
const path = require("node:path");
const fs = require("node:fs");
const { pool } = require("../db");
const b2Storage = require("../services/b2StorageService");

const router = express.Router();

const STORAGE_CACHE_DIR = path.join(__dirname, "../cache/storage");
if (!fs.existsSync(STORAGE_CACHE_DIR)) {
  fs.mkdirSync(STORAGE_CACHE_DIR, { recursive: true });
}

function saveToStorageCache(objectKey, sourcePathOrBuffer) {
  try {
    if (!objectKey) return;
    const dest = path.join(STORAGE_CACHE_DIR, b2Storage.sanitizeKey(objectKey));
    fs.mkdirSync(path.dirname(dest), { recursive: true });
    if (typeof sourcePathOrBuffer === "string" && fs.existsSync(sourcePathOrBuffer)) {
      fs.copyFileSync(sourcePathOrBuffer, dest);
    } else if (Buffer.isBuffer(sourcePathOrBuffer)) {
      fs.writeFileSync(dest, sourcePathOrBuffer);
    }
  } catch (err) {
    console.warn("Failed to write to local storage cache:", err.message);
  }
}

// Memory storage for small to medium files to stream directly to B2 without local disk footprint
const memoryUpload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 100 * 1024 * 1024 } // 100MB
});

function extractObjectKey(params) {
  if (!params) return "";
  if (Array.isArray(params.key)) {
    return params.key.join("/");
  }
  if (typeof params.key === "string") {
    return params.key;
  }
  if (Array.isArray(params[0])) {
    return params[0].join("/");
  }
  if (typeof params[0] === "string") {
    return params[0];
  }
  return "";
}

/**
 * GET /api/storage/file/*key
 * Public/application file retrieval endpoint.
 * Serves from ultra-fast local disk cache if available, else streams from Backblaze B2.
 */
router.get("/file/*key", async (req, res) => {
  try {
    const rawKey = extractObjectKey(req.params);
    if (!rawKey) {
      return res.status(400).json({ error: "Missing object key." });
    }

    const objectKey = b2Storage.sanitizeKey(rawKey);
    const cachedFilePath = path.join(STORAGE_CACHE_DIR, objectKey);

    // 1. Instant Cache Hit: Serve directly from local disk (<5ms)
    if (fs.existsSync(cachedFilePath)) {
      return res.sendFile(path.resolve(cachedFilePath), {
        acceptRanges: true,
        cacheControl: true,
        maxAge: 86400000,
        headers: {
          "Cache-Control": "public, max-age=86400, immutable"
        }
      });
    }

    // 2. Cache Miss: Stream from Backblaze B2 and cache for future requests
    const rangeHeader = req.headers.range || null;

    let b2Res;
    try {
      b2Res = await b2Storage.getFileStream(objectKey, rangeHeader);
    } catch (err) {
      if (err.name === "NoSuchKey" || err.name === "NotFound" || err.$metadata?.httpStatusCode === 404) {
        return res.status(404).json({ error: "File not found in cloud storage." });
      }
      throw err;
    }

    const headers = {
      "Content-Type": b2Res.contentType,
      "Accept-Ranges": "bytes",
      "Cache-Control": "public, max-age=86400, immutable"
    };

    if (b2Res.contentLength !== undefined) {
      headers["Content-Length"] = b2Res.contentLength;
    }
    if (b2Res.contentRange) {
      headers["Content-Range"] = b2Res.contentRange;
    }
    if (b2Res.etag) {
      headers["ETag"] = b2Res.etag;
    }
    if (b2Res.lastModified) {
      headers["Last-Modified"] = new Date(b2Res.lastModified).toUTCString();
    }

    res.writeHead(b2Res.statusCode || 200, headers);

    // Cache the full response body locally
    if (!rangeHeader && (!b2Res.statusCode || b2Res.statusCode === 200)) {
      try {
        fs.mkdirSync(path.dirname(cachedFilePath), { recursive: true });
        const tmpFile = cachedFilePath + ".tmp." + Date.now();
        const writeStream = fs.createWriteStream(tmpFile);
        b2Res.stream.pipe(writeStream);
        writeStream.on("finish", () => {
          try {
            if (fs.existsSync(tmpFile)) fs.renameSync(tmpFile, cachedFilePath);
          } catch (_) {}
        });
        writeStream.on("error", () => {
          try { if (fs.existsSync(tmpFile)) fs.unlinkSync(tmpFile); } catch (_) {}
        });
      } catch (_) {}
    }

    req.on("close", () => {
      if (b2Res.stream && typeof b2Res.stream.destroy === "function") {
        b2Res.stream.destroy();
      }
    });

    b2Res.stream.pipe(res);
  } catch (err) {
    console.error("Storage stream error:", err.message);
    if (!res.headersSent) {
      res.status(500).json({ error: "Failed to retrieve file from storage." });
    }
  }
});

/**
 * POST /api/storage/upload
 * Backend-mediated upload endpoint to Backblaze B2.
 */
router.post("/upload", memoryUpload.single("file"), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: "No file uploaded." });
    }

    const category = req.body.category || "uploads";
    const userId = req.body.userId || null;
    const originalName = req.file.originalname || "file";
    const mimeType = req.file.mimetype || "application/octet-stream";

    // Upload to Backblaze B2
    const uploadResult = await b2Storage.uploadFile({
      buffer: req.file.buffer,
      mimeType,
      category,
      originalName,
      metadata: {
        userId: userId ? String(userId) : "anonymous"
      }
    });

    // Save immediately into local fast cache
    saveToStorageCache(uploadResult.objectKey, req.file.buffer);

    // Record in public.media_files
    const insertQuery = `
      INSERT INTO public.media_files (
        user_id, storage_provider, bucket_name, object_key, original_file_name,
        mime_type, file_size, file_category, uploaded_by, upload_status, metadata
      )
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
      RETURNING *;
    `;
    const dbRes = await pool.query(insertQuery, [
      userId,
      "backblaze_b2",
      uploadResult.bucketName,
      uploadResult.objectKey,
      originalName,
      mimeType,
      req.file.size,
      category,
      userId ? "user" : "system",
      "completed",
      JSON.stringify({ etag: uploadResult.etag })
    ]);

    const accessUrl = `/api/storage/file/${uploadResult.objectKey}`;

    return res.json({
      success: true,
      fileId: dbRes.rows[0].id,
      url: accessUrl,
      objectKey: uploadResult.objectKey,
      fileName: originalName,
      fileSize: req.file.size,
      mimeType,
      category
    });
  } catch (err) {
    console.error("Storage upload error:", err.message);
    return res.status(500).json({ error: err.message || "Failed to upload file to cloud storage." });
  }
});

/**
 * POST /api/storage/presigned-upload
 * Generates a presigned PUT URL for direct client-to-B2 uploads.
 */
router.post("/presigned-upload", async (req, res) => {
  try {
    const { fileName, mimeType, fileSize, category = "uploads", userId = null } = req.body;

    if (!fileName || !mimeType) {
      return res.status(400).json({ error: "fileName and mimeType are required." });
    }

    if (fileSize) {
      b2Storage.validateFile(category, mimeType, Number(fileSize));
    }

    const objectKey = b2Storage.generateObjectKey(category, fileName);
    const presigned = await b2Storage.generateUploadUrl(objectKey, mimeType, 900); // 15 mins

    // Register pending record in DB
    const insertQuery = `
      INSERT INTO public.media_files (
        user_id, storage_provider, bucket_name, object_key, original_file_name,
        mime_type, file_size, file_category, uploaded_by, upload_status
      )
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
      RETURNING id, object_key, file_category, created_at;
    `;
    const dbRes = await pool.query(insertQuery, [
      userId,
      "backblaze_b2",
      presigned.bucketName,
      objectKey,
      fileName,
      mimeType,
      fileSize ? Number(fileSize) : 0,
      category,
      userId ? "user" : "client",
      "pending"
    ]);

    return res.json({
      success: true,
      fileId: dbRes.rows[0].id,
      uploadUrl: presigned.uploadUrl,
      objectKey: presigned.objectKey,
      expiresInSeconds: presigned.expiresInSeconds,
      accessUrl: `/api/storage/file/${presigned.objectKey}`,
      method: "PUT"
    });
  } catch (err) {
    console.error("Presigned upload generation error:", err.message);
    return res.status(500).json({ error: err.message || "Failed to generate presigned upload URL." });
  }
});

/**
 * POST /api/storage/confirm-upload
 * Verifies that a client successfully uploaded a file via presigned URL and updates status to completed.
 */
router.post("/confirm-upload", async (req, res) => {
  try {
    const { objectKey, fileId } = req.body;
    if (!objectKey) {
      return res.status(400).json({ error: "objectKey is required." });
    }

    const safeKey = b2Storage.sanitizeKey(objectKey);
    const meta = await b2Storage.getFileMetadata(safeKey);

    if (!meta.exists) {
      return res.status(404).json({ error: "File not found in Backblaze B2 storage." });
    }

    let updateQuery;
    let params;
    if (fileId) {
      updateQuery = `
        UPDATE public.media_files
        SET upload_status = 'completed',
            file_size = $1,
            mime_type = COALESCE($2, mime_type),
            metadata = metadata || $3::jsonb,
            updated_at = NOW()
        WHERE id = $4
        RETURNING *;
      `;
      params = [meta.contentLength, meta.contentType, JSON.stringify({ etag: meta.etag }), fileId];
    } else {
      updateQuery = `
        UPDATE public.media_files
        SET upload_status = 'completed',
            file_size = $1,
            mime_type = COALESCE($2, mime_type),
            metadata = metadata || $3::jsonb,
            updated_at = NOW()
        WHERE object_key = $4
        RETURNING *;
      `;
      params = [meta.contentLength, meta.contentType, JSON.stringify({ etag: meta.etag }), safeKey];
    }

    const dbRes = await pool.query(updateQuery, params);

    return res.json({
      success: true,
      file: dbRes.rows[0] || null,
      accessUrl: `/api/storage/file/${safeKey}`
    });
  } catch (err) {
    console.error("Confirm upload error:", err.message);
    return res.status(500).json({ error: err.message || "Failed to confirm upload." });
  }
});

/**
 * POST /api/storage/presigned-download
 * Generates a temporary presigned download URL for direct client download.
 */
router.post("/presigned-download", async (req, res) => {
  try {
    const { objectKey, downloadFilename, expiresInSeconds = 3600 } = req.body;
    if (!objectKey) {
      return res.status(400).json({ error: "objectKey is required." });
    }

    const safeKey = b2Storage.sanitizeKey(objectKey);
    const result = await b2Storage.generateDownloadUrl(safeKey, Number(expiresInSeconds), downloadFilename);

    return res.json({
      success: true,
      downloadUrl: result.downloadUrl,
      objectKey: result.objectKey,
      expiresInSeconds: result.expiresInSeconds
    });
  } catch (err) {
    console.error("Presigned download error:", err.message);
    return res.status(500).json({ error: err.message || "Failed to generate download URL." });
  }
});

/**
 * DELETE /api/storage/file/*key
 * Deletes a file from Backblaze B2 and updates the database.
 */
router.delete("/file/*key", async (req, res) => {
  try {
    const rawKey = extractObjectKey(req.params);
    if (!rawKey) {
      return res.status(400).json({ error: "Missing object key." });
    }

    const objectKey = b2Storage.sanitizeKey(rawKey);

    // Delete from B2
    await b2Storage.deleteFile(objectKey);

    // Delete or mark deleted in DB
    await pool.query("DELETE FROM public.media_files WHERE object_key = $1", [objectKey]);

    return res.json({
      success: true,
      message: `File '${objectKey}' deleted successfully from Backblaze B2.`
    });
  } catch (err) {
    console.error("Storage deletion error:", err.message);
    return res.status(500).json({ error: err.message || "Failed to delete file from storage." });
  }
});

module.exports = router;
module.exports.saveToStorageCache = saveToStorageCache;
module.exports.STORAGE_CACHE_DIR = STORAGE_CACHE_DIR;
