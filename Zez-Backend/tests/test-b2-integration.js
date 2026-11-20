require("dotenv").config();
const http = require("node:http");
const fs = require("node:fs");
const path = require("node:path");
const { pool } = require("../src/db");
const app = require("../src/server");
const b2Storage = require("../src/services/b2StorageService");

let server;
let baseUrl;
let adminToken;

function request(method, pathUrl, headers = {}, body = null) {
  return new Promise((resolve, reject) => {
    const url = new URL(pathUrl, baseUrl);
    const reqHeaders = { ...headers };
    let postData = null;

    if (body) {
      if (typeof body === "string" || Buffer.isBuffer(body)) {
        postData = body;
      } else if (typeof body === "object") {
        postData = JSON.stringify(body);
        reqHeaders["Content-Type"] = "application/json";
      }
      reqHeaders["Content-Length"] = Buffer.byteLength(postData);
    }

    const req = http.request(
      url,
      {
        method,
        headers: reqHeaders
      },
      (res) => {
        const chunks = [];
        res.on("data", (chunk) => chunks.push(chunk));
        res.on("end", () => {
          const raw = Buffer.concat(chunks).toString("utf-8");
          let data = raw;
          try {
            data = JSON.parse(raw);
          } catch (_) {}
          resolve({
            status: res.statusCode,
            headers: res.headers,
            body: data,
            rawBody: Buffer.concat(chunks)
          });
        });
      }
    );

    req.on("error", reject);
    if (postData) req.write(postData);
    req.end();
  });
}

function sendMultipart(pathUrl, fieldName, fileName, fileBuffer, mimeType, extraFields = {}, headers = {}) {
  return new Promise((resolve, reject) => {
    const boundary = "----WebKitFormBoundary" + Math.random().toString(36).substring(2);
    const crlf = "\r\n";
    let body = Buffer.from("");

    // Add extra text fields
    for (const [key, val] of Object.entries(extraFields)) {
      let fieldPart = `--${boundary}${crlf}`;
      fieldPart += `Content-Disposition: form-data; name="${key}"${crlf}${crlf}`;
      fieldPart += `${val}${crlf}`;
      body = Buffer.concat([body, Buffer.from(fieldPart)]);
    }

    // Add file part
    let fileHeader = `--${boundary}${crlf}`;
    fileHeader += `Content-Disposition: form-data; name="${fieldName}"; filename="${fileName}"${crlf}`;
    fileHeader += `Content-Type: ${mimeType}${crlf}${crlf}`;
    body = Buffer.concat([body, Buffer.from(fileHeader), fileBuffer, Buffer.from(crlf)]);

    // Closing boundary
    body = Buffer.concat([body, Buffer.from(`--${boundary}--${crlf}`)]);

    const url = new URL(pathUrl, baseUrl);
    const reqHeaders = {
      ...headers,
      "Content-Type": `multipart/form-data; boundary=${boundary}`,
      "Content-Length": body.length
    };

    const req = http.request(url, { method: "POST", headers: reqHeaders }, (res) => {
      const chunks = [];
      res.on("data", (chunk) => chunks.push(chunk));
      res.on("end", () => {
        const raw = Buffer.concat(chunks).toString("utf-8");
        let data = raw;
        try { data = JSON.parse(raw); } catch (_) {}
        resolve({ status: res.statusCode, headers: res.headers, body: data });
      });
    });

    req.on("error", reject);
    req.write(body);
    req.end();
  });
}

async function runB2Tests() {
  console.log("=================================================");
  console.log("🚀 Starting Backblaze B2 Integration Test Suite");
  console.log("=================================================");

  // Start test server
  await new Promise((res) => {
    server = app.listen(0, "127.0.0.1", () => {
      const port = server.address().port;
      baseUrl = `http://127.0.0.1:${port}`;
      console.log(`Test server running on ${baseUrl}`);
      res();
    });
  });

  let passed = 0;
  let failed = 0;

  function assert(condition, message) {
    if (condition) {
      console.log(`  ✅ PASSED: ${message}`);
      passed++;
    } else {
      console.error(`  ❌ FAILED: ${message}`);
      failed++;
    }
  }

  try {
    // 1. Admin Login
    console.log("\n[Test 1] Admin Authentication for B2 Management");
    const loginRes = await request("POST", "/api/admin/login", {}, {
      username: process.env.ADMIN_USERNAME || "admin",
      password: process.env.ADMIN_PASSWORD || "admin123"
    });
    assert(loginRes.status === 200 && loginRes.body?.token, "Admin login returns valid JWT token");
    adminToken = loginRes.body?.token;
    const authHeaders = { Authorization: `Bearer ${adminToken}` };

    // 2. Storage Service direct upload & retrieval
    console.log("\n[Test 2] Storage Service Direct B2 Upload & Metadata Verification");
    const testContent = Buffer.from("Direct B2 storage service test content - " + Date.now());
    const uploadRes = await b2Storage.uploadFile({
      buffer: testContent,
      mimeType: "text/plain",
      category: "uploads",
      originalName: "service-test.txt"
    });
    assert(uploadRes.success && uploadRes.objectKey.startsWith("uploads/"), "uploadFile() returns success with uploads/ prefix");

    const meta = await b2Storage.getFileMetadata(uploadRes.objectKey);
    assert(meta.exists && meta.contentLength === testContent.length, "getFileMetadata() accurately reports file existence and size");

    // 3. File retrieval via GET /api/storage/file/*
    console.log("\n[Test 3] GET /api/storage/file/* (Streamed from B2)");
    const streamRes = await request("GET", `/api/storage/file/${uploadRes.objectKey}`);
    assert(streamRes.status === 200, "File endpoint responds with HTTP 200");
    assert(streamRes.headers["content-type"] === "text/plain", "Response Content-Type matches uploaded MIME");
    assert(streamRes.headers["accept-ranges"] === "bytes", "Response supports Accept-Ranges: bytes");
    assert(streamRes.body === testContent.toString("utf-8"), "Downloaded body content matches uploaded binary");

    // 4. HTTP Range Requests (for video streaming and seek operations)
    console.log("\n[Test 4] HTTP Range Requests Support (Partial Content 206)");
    const rangeRes = await request("GET", `/api/storage/file/${uploadRes.objectKey}`, {
      Range: "bytes=0-15"
    });
    assert(rangeRes.status === 206, "Range request returns HTTP 206 Partial Content");
    assert(rangeRes.headers["content-range"].startsWith("bytes 0-15/"), "Content-Range header returned properly");
    assert(rangeRes.body === testContent.toString("utf-8").substring(0, 16), "Range chunk content matches requested byte slice");

    // 5. Presigned Download URL generation
    console.log("\n[Test 5] POST /api/storage/presigned-download");
    const dlPresignRes = await request("POST", "/api/storage/presigned-download", {}, {
      objectKey: uploadRes.objectKey,
      downloadFilename: "custom-download.txt",
      expiresInSeconds: 1800
    });
    assert(dlPresignRes.status === 200 && dlPresignRes.body?.downloadUrl, "Presigned download URL returned successfully");
    assert(dlPresignRes.body.downloadUrl.includes("backblazeb2.com"), "Download URL points to Backblaze B2 S3 endpoint");

    // 6. Presigned Upload URL generation
    console.log("\n[Test 6] POST /api/storage/presigned-upload");
    const upPresignRes = await request("POST", "/api/storage/presigned-upload", {}, {
      fileName: "client-direct.png",
      mimeType: "image/png",
      fileSize: 1024,
      category: "chat-media"
    });
    assert(upPresignRes.status === 200 && upPresignRes.body?.uploadUrl, "Presigned upload PUT URL generated successfully");
    assert(upPresignRes.body.objectKey.startsWith("chat-media/"), "Object key respects category prefix 'chat-media/'");

    // 7. Verify pending record in public.media_files
    console.log("\n[Test 7] Database media_files Tracking for Presigned Upload");
    const dbPending = await pool.query("SELECT * FROM public.media_files WHERE object_key = $1", [upPresignRes.body.objectKey]);
    assert(dbPending.rows.length === 1 && dbPending.rows[0].upload_status === "pending", "Pending upload pre-registered in database");

    // Clean up test upload
    await b2Storage.deleteFile(uploadRes.objectKey);
    await pool.query("DELETE FROM public.media_files WHERE object_key = $1", [upPresignRes.body.objectKey]);

    // 8. Backend-mediated upload via POST /api/storage/upload
    console.log("\n[Test 8] POST /api/storage/upload (Multipart upload to B2)");
    const sampleImage = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00, 0x00]); // PNG magic bytes
    const apiUploadRes = await sendMultipart(
      "/api/storage/upload",
      "file",
      "profile-picture.png",
      sampleImage,
      "image/png",
      { category: "avatars" }
    );
    assert(apiUploadRes.status === 200 && apiUploadRes.body?.success, "Upload API returns success: true");
    assert(apiUploadRes.body?.url.startsWith("/api/storage/file/avatars/"), "Returned access URL starts with /api/storage/file/avatars/");
    assert(apiUploadRes.body?.fileSize === sampleImage.length, "File size recorded accurately");

    const uploadedKey = apiUploadRes.body.objectKey;

    // 9. Verify in database
    console.log("\n[Test 9] Database media_files Completed Status Verification");
    const dbCompleted = await pool.query("SELECT * FROM public.media_files WHERE object_key = $1", [uploadedKey]);
    assert(dbCompleted.rows.length === 1 && dbCompleted.rows[0].upload_status === "completed", "Database record marked as 'completed'");
    assert(dbCompleted.rows[0].storage_provider === "backblaze_b2", "Storage provider recorded as 'backblaze_b2'");

    // 10. Chat User Avatar upload via POST /api/chat/upload-avatar
    console.log("\n[Test 10] Chat Avatar Upload Integration (/api/chat/upload-avatar)");
    // Get an existing user or create temporary user
    const userRes = await pool.query("SELECT id FROM public.chat_users LIMIT 1");
    let testUserId = userRes.rows[0]?.id;
    if (!testUserId) {
      const insUser = await pool.query(
        "INSERT INTO public.chat_users (phone_number, name) VALUES ('9999999999', 'B2 Test User') RETURNING id"
      );
      testUserId = insUser.rows[0].id;
    }

    const avatarUploadRes = await sendMultipart(
      "/api/chat/upload-avatar",
      "avatar",
      "avatar-b2.jpg",
      Buffer.from([0xff, 0xd8, 0xff, 0xe0, 0x00, 0x10, 0x4a, 0x46, 0x49, 0x46]), // JPEG header
      "image/jpeg",
      { userId: testUserId }
    );
    assert(avatarUploadRes.status === 200 && avatarUploadRes.body?.success, "Avatar upload responds with success");
    assert(avatarUploadRes.body?.avatar_url.startsWith("/api/storage/file/avatars/"), "User avatar URL points to cloud storage endpoint");
    assert(avatarUploadRes.body?.storage === "backblaze_b2", "Storage provider explicitly reports 'backblaze_b2'");

    // Verify avatar in chat_users table
    const userCheck = await pool.query("SELECT avatar_url FROM public.chat_users WHERE id = $1", [testUserId]);
    assert(userCheck.rows[0].avatar_url === avatarUploadRes.body.avatar_url, "User's avatar_url updated in database with cloud URL");

    // Clean up avatar from B2 and DB
    if (avatarUploadRes.body.objectKey) {
      await b2Storage.deleteFile(avatarUploadRes.body.objectKey);
      await pool.query("DELETE FROM public.media_files WHERE object_key = $1", [avatarUploadRes.body.objectKey]);
    }

    // 11. Chat Media upload via POST /api/chat/upload
    console.log("\n[Test 11] Chat Media Upload Integration (/api/chat/upload)");
    const chatMediaUploadRes = await sendMultipart(
      "/api/chat/upload",
      "file",
      "voice-note.mp4",
      Buffer.from("sample-video-or-audio-stream"),
      "video/mp4",
      {}
    );
    assert(chatMediaUploadRes.status === 200 && chatMediaUploadRes.body?.success, "Chat media upload responds with success");
    assert(chatMediaUploadRes.body?.url.startsWith("/api/storage/file/videos/"), "Video media stored under 'videos/' prefix");
    assert(chatMediaUploadRes.body?.mediaType === "video", "MediaType detected properly as 'video'");
    assert(chatMediaUploadRes.body?.storage === "backblaze_b2", "Chat media storage provider is 'backblaze_b2'");

    if (chatMediaUploadRes.body.objectKey) {
      await b2Storage.deleteFile(chatMediaUploadRes.body.objectKey);
      await pool.query("DELETE FROM public.media_files WHERE object_key = $1", [chatMediaUploadRes.body.objectKey]);
    }

    // 12. Admin B2 Stats Endpoint
    console.log("\n[Test 12] GET /api/admin/b2/stats");
    const statsRes = await request("GET", "/api/admin/b2/stats", authHeaders);
    assert(statsRes.status === 200 && statsRes.body?.success, "Admin B2 stats returns HTTP 200 and success");
    assert(statsRes.body?.stats?.isConfigured === true, "B2 isConfigured reports true");
    assert(statsRes.body?.stats?.bucketName === process.env.B2_BUCKET_NAME, "Stats reports accurate B2 bucket name");
    assert(statsRes.body?.stats?.freeTierFormatted === "10 GB", "Free tier capacity calculated accurately (10 GB)");

    // 13. Admin B2 Files List Endpoint
    console.log("\n[Test 13] GET /api/admin/b2/files");
    const filesRes = await request("GET", "/api/admin/b2/files", authHeaders);
    assert(filesRes.status === 200 && filesRes.body?.success, "Admin B2 files list returns HTTP 200");
    assert(Array.isArray(filesRes.body?.files), "Files list returns array of media files");
    const foundOurFile = filesRes.body.files.find(f => f.object_key === uploadedKey);
    assert(Boolean(foundOurFile), "Uploaded file is found in Admin B2 files list");

    // 14. Admin B2 File Deletion Endpoint
    console.log("\n[Test 14] DELETE /api/admin/b2/file");
    const deleteRes = await request("DELETE", "/api/admin/b2/file", authHeaders, {
      objectKey: uploadedKey
    });
    assert(deleteRes.status === 200 && deleteRes.body?.success, "Admin B2 file deletion responds with success");

    const existsAfterDelete = await b2Storage.fileExists(uploadedKey);
    assert(existsAfterDelete === false, "File is no longer present in Backblaze B2");

    const dbAfterDelete = await pool.query("SELECT * FROM public.media_files WHERE object_key = $1", [uploadedKey]);
    assert(dbAfterDelete.rows.length === 0, "File record removed from public.media_files database");

    // 15. Invalid MIME Type rejection test
    console.log("\n[Test 15] Security: Invalid MIME Type Rejection");
    let rejected = false;
    try {
      b2Storage.validateFile("avatars", "application/x-msdownload", 1024);
    } catch (err) {
      rejected = true;
    }
    assert(rejected, "Invalid MIME type rejected by category validation");

    // 16. Oversized file rejection test
    console.log("\n[Test 16] Security: Oversized File Rejection");
    let sizeRejected = false;
    try {
      b2Storage.validateFile("avatars", "image/jpeg", 25 * 1024 * 1024); // 25MB > 10MB limit
    } catch (err) {
      sizeRejected = true;
    }
    assert(sizeRejected, "Oversized file (>10MB for avatars) rejected by size validation");

    // 17. Path Traversal Protection
    console.log("\n[Test 17] Security: Path Traversal Protection");
    const sanitized = b2Storage.sanitizeKey("../../../etc/passwd");
    assert(!sanitized.includes(".."), "Path traversal '..' stripped from object key");
    assert(sanitized === "etc/passwd", "Sanitized key normalized to safe relative path");

    // 18. System Environment Telemetry includes b2Cloud
    console.log("\n[Test 18] GET /api/admin/system/environment Telemetry verification");
    const sysEnvRes = await request("GET", "/api/admin/system/environment", authHeaders);
    assert(sysEnvRes.status === 200 && sysEnvRes.body?.environment?.storage?.b2Cloud, "Telemetry storage breakdown includes b2Cloud metrics");
    assert(sysEnvRes.body.environment.storage.b2Cloud.status === "connected", "b2Cloud status reports 'connected'");

    console.log("\n=================================================");
    console.log(`Results: ${passed} PASSED | ${failed} FAILED | Total: ${passed + failed}`);
    console.log("=================================================");

    if (failed > 0) {
      process.exit(1);
    } else {
      process.exit(0);
    }
  } catch (err) {
    console.error("Fatal test runner error:", err);
    process.exit(1);
  } finally {
    if (server) server.close();
  }
}

runB2Tests();
