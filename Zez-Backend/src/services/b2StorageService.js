const {
  S3Client,
  PutObjectCommand,
  GetObjectCommand,
  HeadObjectCommand,
  DeleteObjectCommand,
  ListObjectsV2Command
} = require("@aws-sdk/client-s3");
const { getSignedUrl } = require("@aws-sdk/s3-request-presigner");
const crypto = require("node:crypto");
const path = require("node:path");
const fs = require("node:fs");

// Cache the S3 client instance
let s3ClientInstance = null;

/**
 * Validates and returns the Backblaze B2 configuration from environment variables.
 */
function getB2Config() {
  const keyId = process.env.B2_KEY_ID;
  const applicationKey = process.env.B2_APPLICATION_KEY;
  const bucketName = process.env.B2_BUCKET_NAME;
  const bucketId = process.env.B2_BUCKET_ID;
  const endpoint = process.env.B2_ENDPOINT;
  const region = process.env.B2_REGION || "us-east-005";
  const keyName = process.env.B2_KEY_NAME || "";

  const isConfigured = Boolean(keyId && applicationKey && bucketName && endpoint);

  let formattedEndpoint = endpoint || "";
  if (formattedEndpoint && !formattedEndpoint.startsWith("http://") && !formattedEndpoint.startsWith("https://")) {
    formattedEndpoint = `https://${formattedEndpoint}`;
  }

  return {
    isConfigured,
    keyId,
    applicationKey,
    keyName,
    bucketName,
    bucketId,
    endpoint: formattedEndpoint,
    region
  };
}

/**
 * Returns a reusable S3Client configured for Backblaze B2 S3-compatible API.
 */
function getS3Client() {
  if (s3ClientInstance) return s3ClientInstance;

  const config = getB2Config();
  if (!config.isConfigured) {
    throw new Error("Backblaze B2 is not configured. Missing required environment variables (B2_KEY_ID, B2_APPLICATION_KEY, B2_BUCKET_NAME, B2_ENDPOINT).");
  }

  s3ClientInstance = new S3Client({
    endpoint: config.endpoint,
    region: config.region,
    credentials: {
      accessKeyId: config.keyId,
      secretAccessKey: config.applicationKey
    },
    // Backblaze B2 works with both virtual-hosted and path-style; default is reliable
    forcePathStyle: false
  });

  return s3ClientInstance;
}

/**
 * Allowed MIME types and size limits per category
 */
const CATEGORY_LIMITS = {
  avatars: {
    allowedMimes: ["image/jpeg", "image/png", "image/webp", "image/gif"],
    maxSizeBytes: 10 * 1024 * 1024 // 10MB
  },
  "chat-media": {
    allowedMimes: [
      // Images
      "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml", "image/bmp",
      // Videos
      "video/mp4", "video/webm", "video/quicktime", "video/3gpp", "video/x-matroska",
      // Audio
      "audio/mpeg", "audio/mp4", "audio/ogg", "audio/wav", "audio/aac", "audio/webm", "audio/3gpp",
      // Documents
      "application/pdf", "text/plain", "application/zip", "application/x-zip-compressed",
      "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    ],
    maxSizeBytes: 100 * 1024 * 1024 // 100MB
  },
  documents: {
    allowedMimes: [
      "application/pdf", "text/plain", "application/zip",
      "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    ],
    maxSizeBytes: 50 * 1024 * 1024 // 50MB
  },
  videos: {
    allowedMimes: ["video/mp4", "video/webm", "video/quicktime", "video/3gpp", "video/x-matroska"],
    maxSizeBytes: 150 * 1024 * 1024 // 150MB
  },
  uploads: {
    allowedMimes: null, // Allow all standard
    maxSizeBytes: 100 * 1024 * 1024 // 100MB
  },
  releases: {
    allowedMimes: [
      "application/vnd.android.package-archive",
      "application/octet-stream",
      "application/x-zip-compressed",
      "application/zip"
    ],
    maxSizeBytes: 200 * 1024 * 1024 // 200MB
  }
};

/**
 * Sanitizes an object key to prevent path traversal, leading slashes, and control chars.
 */
function sanitizeKey(rawKey) {
  if (!rawKey || typeof rawKey !== "string") {
    throw new Error("Invalid object key.");
  }
  let clean = rawKey;
  try {
    clean = decodeURIComponent(clean);
  } catch (_) {}

  if (clean.includes("\0")) {
    throw new Error("Null byte injection detected.");
  }

  // Replace backslashes with forward slashes
  clean = clean.trim().replace(/\\/g, "/");
  // Normalize consecutive slashes
  clean = clean.replace(/\/+/g, "/");
  // Iteratively strip directory traversal sequences
  while (clean.includes("..")) {
    clean = clean.replace(/\.\./g, "");
  }
  // Strip leading and trailing slashes
  clean = clean.replace(/^\/+/, "").replace(/\/+$/, "");
  if (!clean || clean === "." || clean.startsWith("/")) {
    throw new Error("Sanitized object key is empty.");
  }
  return clean;
}

/**
 * Generates a collision-resistant unique object key with a logical category prefix.
 */
function generateObjectKey(category = "uploads", originalName = "") {
  const ext = originalName ? path.extname(originalName).toLowerCase() : "";
  const safeExt = ext && ext.length <= 10 && /^[a-z0-9.]+$/.test(ext) ? ext : ".bin";
  const uniqueId = `${Date.now()}-${crypto.randomBytes(6).toString("hex")}`;
  const prefix = category.trim().replace(/\/+$/, "");
  return `${prefix}/${uniqueId}${safeExt}`;
}

/**
 * Validates a file against allowed categories, MIME types, and size constraints.
 */
function validateFile(category, mimeType, sizeBytes) {
  const limits = CATEGORY_LIMITS[category] || CATEGORY_LIMITS.uploads;

  if (sizeBytes > limits.maxSizeBytes) {
    const maxMb = Math.round(limits.maxSizeBytes / (1024 * 1024));
    throw new Error(`File size (${(sizeBytes / (1024 * 1024)).toFixed(2)} MB) exceeds maximum allowed limit of ${maxMb} MB for ${category}.`);
  }

  if (limits.allowedMimes && limits.allowedMimes.length > 0) {
    const isAllowed = limits.allowedMimes.some(m => m.toLowerCase() === (mimeType || "").toLowerCase());
    if (!isAllowed) {
      throw new Error(`MIME type '${mimeType}' is not permitted for category '${category}'. Allowed types: ${limits.allowedMimes.join(", ")}`);
    }
  }

  return true;
}

/**
 * Formats bytes into a human-readable string (e.g. 10.5 MB)
 */
function formatBytes(bytes) {
  if (bytes === 0 || !bytes) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
}

/**
 * Uploads a file buffer or stream to Backblaze B2.
 */
async function uploadFile({
  buffer,
  stream,
  filePath,
  key,
  mimeType = "application/octet-stream",
  category = "uploads",
  originalName = "",
  metadata = {}
}) {
  const client = getS3Client();
  const config = getB2Config();

  let bodyData;
  let fileLength = 0;

  if (buffer) {
    bodyData = buffer;
    fileLength = buffer.length;
  } else if (filePath && fs.existsSync(filePath)) {
    bodyData = fs.readFileSync(filePath);
    fileLength = bodyData.length;
  } else if (stream) {
    bodyData = stream;
  } else {
    throw new Error("No file content provided for upload (buffer, stream, or filePath required).");
  }

  if (fileLength > 0) {
    validateFile(category, mimeType, fileLength);
  }

  const finalKey = sanitizeKey(key || generateObjectKey(category, originalName));

  const command = new PutObjectCommand({
    Bucket: config.bucketName,
    Key: finalKey,
    Body: bodyData,
    ContentType: mimeType,
    ContentLength: fileLength > 0 ? fileLength : undefined,
    Metadata: {
      ...metadata,
      originalName: encodeURIComponent(originalName || ""),
      category: category,
      uploadedAt: new Date().toISOString()
    }
  });

  const res = await client.send(command);

  return {
    success: true,
    objectKey: finalKey,
    bucketName: config.bucketName,
    fileSize: fileLength,
    mimeType,
    etag: res.ETag || "",
    category,
    publicUrl: `${config.endpoint}/${config.bucketName}/${finalKey}`
  };
}

/**
 * Retrieves a file as a Buffer from Backblaze B2.
 */
async function getFile(key) {
  const client = getS3Client();
  const config = getB2Config();
  const safeKey = sanitizeKey(key);

  const command = new GetObjectCommand({
    Bucket: config.bucketName,
    Key: safeKey
  });

  const res = await client.send(command);

  // Convert stream to Buffer
  const streamToBuffer = async (readableStream) => {
    const chunks = [];
    for await (const chunk of readableStream) {
      chunks.push(typeof chunk === "string" ? Buffer.from(chunk) : chunk);
    }
    return Buffer.concat(chunks);
  };

  const buffer = await streamToBuffer(res.Body);

  return {
    body: buffer,
    contentType: res.ContentType || "application/octet-stream",
    contentLength: res.ContentLength || buffer.length,
    lastModified: res.LastModified,
    etag: res.ETag,
    metadata: res.Metadata || {}
  };
}

/**
 * Returns a readable stream from Backblaze B2 with optional HTTP Range support.
 */
async function getFileStream(key, rangeHeader = null) {
  const client = getS3Client();
  const config = getB2Config();
  const safeKey = sanitizeKey(key);

  const params = {
    Bucket: config.bucketName,
    Key: safeKey
  };

  if (rangeHeader) {
    params.Range = rangeHeader;
  }

  const command = new GetObjectCommand(params);
  const res = await client.send(command);

  return {
    stream: res.Body,
    contentType: res.ContentType || "application/octet-stream",
    contentLength: res.ContentLength,
    contentRange: res.ContentRange,
    acceptRanges: res.AcceptRanges || "bytes",
    lastModified: res.LastModified,
    etag: res.ETag,
    statusCode: res.ContentRange ? 206 : 200,
    metadata: res.Metadata || {}
  };
}

/**
 * Returns file metadata without downloading the object body.
 */
async function getFileMetadata(key) {
  const client = getS3Client();
  const config = getB2Config();
  const safeKey = sanitizeKey(key);

  try {
    const command = new HeadObjectCommand({
      Bucket: config.bucketName,
      Key: safeKey
    });

    const res = await client.send(command);
    return {
      exists: true,
      contentType: res.ContentType,
      contentLength: res.ContentLength,
      lastModified: res.LastModified,
      etag: res.ETag,
      metadata: res.Metadata || {}
    };
  } catch (err) {
    if (err.name === "NotFound" || err.$metadata?.httpStatusCode === 404) {
      return { exists: false };
    }
    throw err;
  }
}

/**
 * Checks whether an object exists in the Backblaze B2 bucket.
 */
async function fileExists(key) {
  const meta = await getFileMetadata(key);
  return meta.exists;
}

/**
 * Deletes a file from Backblaze B2.
 */
async function deleteFile(key) {
  const client = getS3Client();
  const config = getB2Config();
  const safeKey = sanitizeKey(key);

  const command = new DeleteObjectCommand({
    Bucket: config.bucketName,
    Key: safeKey
  });

  await client.send(command);
  return { success: true, key: safeKey };
}

/**
 * Generates a short-lived presigned upload (PUT) URL for direct client-to-B2 uploads.
 */
async function generateUploadUrl(key, mimeType = "application/octet-stream", expiresInSeconds = 900) {
  const client = getS3Client();
  const config = getB2Config();
  const safeKey = sanitizeKey(key);

  const command = new PutObjectCommand({
    Bucket: config.bucketName,
    Key: safeKey,
    ContentType: mimeType
  });

  const uploadUrl = await getSignedUrl(client, command, { expiresIn: expiresInSeconds });

  return {
    uploadUrl,
    objectKey: safeKey,
    bucketName: config.bucketName,
    expiresInSeconds,
    method: "PUT"
  };
}

/**
 * Generates a temporary presigned download (GET) URL for secure direct file retrieval.
 */
async function generateDownloadUrl(key, expiresInSeconds = 3600, downloadFilename = null) {
  const client = getS3Client();
  const config = getB2Config();
  const safeKey = sanitizeKey(key);

  const params = {
    Bucket: config.bucketName,
    Key: safeKey
  };

  if (downloadFilename) {
    const encoded = encodeURIComponent(downloadFilename);
    params.ResponseContentDisposition = `attachment; filename="${encoded}"`;
  }

  const command = new GetObjectCommand(params);
  const downloadUrl = await getSignedUrl(client, command, { expiresIn: expiresInSeconds });

  return {
    downloadUrl,
    objectKey: safeKey,
    expiresInSeconds
  };
}

/**
 * Lists objects in the Backblaze B2 bucket with pagination.
 */
async function listFiles({ prefix = "", maxKeys = 100, continuationToken = undefined } = {}) {
  const client = getS3Client();
  const config = getB2Config();

  const command = new ListObjectsV2Command({
    Bucket: config.bucketName,
    Prefix: prefix ? sanitizeKey(prefix) : undefined,
    MaxKeys: Math.min(maxKeys, 1000),
    ContinuationToken: continuationToken || undefined
  });

  const res = await client.send(command);

  const objects = (res.Contents || []).map(obj => ({
    key: obj.Key,
    size: obj.Size,
    formattedSize: formatBytes(obj.Size),
    lastModified: obj.LastModified,
    etag: obj.ETag
  }));

  return {
    objects,
    keyCount: res.KeyCount || objects.length,
    isTruncated: Boolean(res.IsTruncated),
    nextContinuationToken: res.NextContinuationToken || null
  };
}

/**
 * Computes storage metrics and category breakdown for the Backblaze B2 bucket.
 */
async function getStorageStats() {
  const config = getB2Config();
  if (!config.isConfigured) {
    return {
      isConfigured: false,
      status: "unconfigured"
    };
  }

  const client = getS3Client();

  let totalObjects = 0;
  let totalBytes = 0;
  const categories = {
    avatars: { count: 0, bytes: 0, formatted: "0 B" },
    chatMedia: { count: 0, bytes: 0, formatted: "0 B" },
    documents: { count: 0, bytes: 0, formatted: "0 B" },
    videos: { count: 0, bytes: 0, formatted: "0 B" },
    uploads: { count: 0, bytes: 0, formatted: "0 B" },
    releases: { count: 0, bytes: 0, formatted: "0 B" },
    other: { count: 0, bytes: 0, formatted: "0 B" }
  };

  let continuationToken = undefined;
  const recentFiles = [];

  do {
    const command = new ListObjectsV2Command({
      Bucket: config.bucketName,
      MaxKeys: 1000,
      ContinuationToken: continuationToken
    });

    const res = await client.send(command);
    const contents = res.Contents || [];

    for (const item of contents) {
      const size = Number(item.Size || 0);
      totalObjects++;
      totalBytes += size;

      const key = item.Key || "";
      if (key.startsWith("avatars/")) {
        categories.avatars.count++;
        categories.avatars.bytes += size;
      } else if (key.startsWith("chat-media/")) {
        categories.chatMedia.count++;
        categories.chatMedia.bytes += size;
      } else if (key.startsWith("documents/")) {
        categories.documents.count++;
        categories.documents.bytes += size;
      } else if (key.startsWith("videos/")) {
        categories.videos.count++;
        categories.videos.bytes += size;
      } else if (key.startsWith("uploads/")) {
        categories.uploads.count++;
        categories.uploads.bytes += size;
      } else if (key.startsWith("releases/")) {
        categories.releases.count++;
        categories.releases.bytes += size;
      } else {
        categories.other.count++;
        categories.other.bytes += size;
      }

      if (recentFiles.length < 50) {
        recentFiles.push({
          key: item.Key,
          size,
          formattedSize: formatBytes(size),
          lastModified: item.LastModified,
          etag: item.ETag
        });
      }
    }

    continuationToken = res.IsTruncated ? res.NextContinuationToken : undefined;
  } while (continuationToken);

  // Format category bytes
  for (const cat of Object.values(categories)) {
    cat.formatted = formatBytes(cat.bytes);
  }

  // Backblaze B2 provides 10 GB (10 * 1024 * 1024 * 1024 bytes) free storage
  const freeTierBytes = 10 * 1024 * 1024 * 1024;
  const remainingBytes = Math.max(0, freeTierBytes - totalBytes);
  const usedPercentage = freeTierBytes > 0 ? parseFloat(((totalBytes / freeTierBytes) * 100).toFixed(2)) : 0;

  return {
    isConfigured: true,
    status: "connected",
    bucketName: config.bucketName,
    bucketId: config.bucketId,
    region: config.region,
    endpoint: config.endpoint,
    totalObjects,
    totalBytes,
    totalFormatted: formatBytes(totalBytes),
    freeTierBytes,
    freeTierFormatted: formatBytes(freeTierBytes),
    remainingBytes,
    remainingFormatted: formatBytes(remainingBytes),
    usedPercentage,
    categories,
    recentFiles: recentFiles.sort((a, b) => new Date(b.lastModified) - new Date(a.lastModified))
  };
}

module.exports = {
  getB2Config,
  getS3Client,
  uploadFile,
  getFile,
  getFileStream,
  getFileMetadata,
  fileExists,
  deleteFile,
  generateUploadUrl,
  generateDownloadUrl,
  listFiles,
  getStorageStats,
  sanitizeKey,
  generateObjectKey,
  validateFile,
  formatBytes,
  CATEGORY_LIMITS
};
