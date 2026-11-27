const assert = require("node:assert/strict");
const crypto = require("node:crypto");
const http = require("node:http");
const path = require("node:path");
require("dotenv").config({ path: path.resolve(__dirname, "../.env") });

const app = require("../src/server");
const { initializeDatabase, pool, closeDatabase } = require("../src/db");

function httpReq(baseUrl, method, urlPath, body = null, headers = {}) {
  return new Promise((resolve, reject) => {
    const u = new URL(baseUrl + urlPath);
    const reqHeaders = { ...headers };
    let payload = null;
    if (body !== null && !Buffer.isBuffer(body)) {
      payload = JSON.stringify(body);
      reqHeaders["Content-Type"] = "application/json";
      reqHeaders["Content-Length"] = Buffer.byteLength(payload);
    } else if (Buffer.isBuffer(body)) {
      payload = body;
      reqHeaders["Content-Length"] = body.length;
    }

    const req = http.request(
      {
        method,
        hostname: u.hostname,
        port: u.port,
        path: u.pathname + u.search,
        headers: reqHeaders,
      },
      (res) => {
        const chunks = [];
        res.on("data", (c) => chunks.push(c));
        res.on("end", () => {
          const buf = Buffer.concat(chunks);
          const text = buf.toString("utf8");
          let json = null;
          try {
            json = JSON.parse(text);
          } catch {}
          resolve({ status: res.statusCode, headers: res.headers, buf, text, json });
        });
      }
    );
    req.on("error", reject);
    if (payload) req.write(payload);
    req.end();
  });
}

function buildMultipartApk(fields, fileName, fileBuffer) {
  const boundary = "----ZezAuditBoundary" + Date.now();
  const parts = [];
  for (const [k, v] of Object.entries(fields)) {
    parts.push(
      Buffer.from(
        `--${boundary}\r\nContent-Disposition: form-data; name="${k}"\r\n\r\n${v}\r\n`,
        "utf8"
      )
    );
  }
  parts.push(
    Buffer.from(
      `--${boundary}\r\nContent-Disposition: form-data; name="apkFile"; filename="${fileName}"\r\nContent-Type: application/vnd.android.package-archive\r\n\r\n`,
      "utf8"
    )
  );
  parts.push(fileBuffer);
  parts.push(Buffer.from(`\r\n--${boundary}--\r\n`, "utf8"));
  return {
    contentType: `multipart/form-data; boundary=${boundary}`,
    body: Buffer.concat(parts),
  };
}

async function run() {
  await initializeDatabase();
  const server = http.createServer(app);
  await new Promise((r) => server.listen(0, "127.0.0.1", r));
  const baseUrl = `http://127.0.0.1:${server.address().port}`;
  console.log("Audit server started:", baseUrl);

  const createdReleaseIds = [];
  let createdDeviceId = null;

  try {
    // 1. Health & Admin Login
    const health = await httpReq(baseUrl, "GET", "/health");
    assert.equal(health.status, 200);
    assert.equal(health.json.success, true);
    console.log("PASS: /health");

    const login = await httpReq(baseUrl, "POST", "/api/admin/login", {
      username: process.env.ADMIN_USERNAME || "admin",
      password: process.env.ADMIN_PASSWORD || "admin123",
    });
    assert.equal(login.status, 200, `Admin login failed: ${login.text}`);
    assert.ok(login.json.token);
    const adminHdr = { Authorization: `Bearer ${login.json.token}` };
    console.log("PASS: Admin login");

    // 2. Audit all admin dashboard API pages
    const adminHtml = await httpReq(baseUrl, "GET", "/admin/");
    assert.equal(adminHtml.status, 200);
    assert.ok(adminHtml.text.includes("releaseTabQuacky") && adminHtml.text.includes("releaseTabZez"));

    for (const ep of [
      "/api/admin/me",
      "/api/admin/stats",
      "/api/admin/devices",
      "/api/admin/calls?limit=10",
      "/api/admin/app-calls?limit=10",
      "/api/admin/chats/conversations",
      "/api/admin/system/environment",
      "/api/admin/system/files?category=downloads",
      "/api/admin/b2/stats",
      "/api/app/releases?appId=quacky",
      "/api/app/releases?appId=zez",
    ]) {
      const r = await httpReq(baseUrl, "GET", ep, null, adminHdr);
      assert.equal(r.status, 200, `Admin endpoint ${ep} returned ${r.status}: ${r.text}`);
    }
    console.log("PASS: All Admin Dashboard API endpoints & HTML UI verified");

    // 3. Invalid appId rejection + invalid APK rejection
    const badApp = await httpReq(baseUrl, "GET", "/api/app/latest?appId=invalid_app_123");
    assert.equal(badApp.status, 400);

    const invalidApk = buildMultipartApk(
      { appId: "zez", versionName: "0.0.1", versionCode: "1", publishImmediately: "false" },
      "bad.apk",
      Buffer.from("NOT_A_ZIP_HEADER", "utf8")
    );
    const badUpload = await httpReq(baseUrl, "POST", "/api/app/upload", invalidApk.body, {
      ...adminHdr,
      "Content-Type": invalidApk.contentType,
    });
    assert.equal(badUpload.status, 400);
    console.log("PASS: Invalid appId & corrupt non-PK APK rejected safely");

    // 4. Isolated Quacky vs Zez release lifecycle (without affecting Quacky's active release)
    const quackyBefore = await httpReq(baseUrl, "GET", "/api/app/latest?appId=quacky");
    const fakeApk1 = Buffer.concat([Buffer.from([0x50, 0x4b, 0x03, 0x04]), Buffer.from("ZEZ_TEST_APK_V1_" + Date.now())]);
    const sha1 = crypto.createHash("sha256").update(fakeApk1).digest("hex");
    const mp1 = buildMultipartApk(
      {
        appId: "zez",
        versionName: "99.0.1-test",
        versionCode: "9901",
        minimumSupportedVersion: "9900",
        updateRequired: "true",
        publishImmediately: "true",
        releaseNotes: "Audit draft 1",
      },
      "zez-audit-1.apk",
      fakeApk1
    );
    const up1 = await httpReq(baseUrl, "POST", "/api/app/upload", mp1.body, {
      ...adminHdr,
      "Content-Type": mp1.contentType,
    });
    assert.equal(up1.status, 201, `Upload 1 failed: ${up1.text}`);
    const relId1 = up1.json.release.id;
    createdReleaseIds.push(relId1);
    assert.equal(up1.json.release.sha256_checksum, sha1);

    const zezLatest = await httpReq(baseUrl, "GET", "/api/app/latest?appId=zez&currentVersionCode=1");
    assert.equal(zezLatest.status, 200);
    assert.equal(zezLatest.json.appId, "zez");
    assert.equal(zezLatest.json.versionCode, 9901);
    assert.equal(zezLatest.json.forceUpdate, true);
    assert.equal(zezLatest.json.updateAvailable, true);
    assert.equal(zezLatest.json.sha256Checksum, sha1);

    const quackyAfter = await httpReq(baseUrl, "GET", "/api/app/latest?appId=quacky");
    assert.equal(quackyAfter.json.id, quackyBefore.json.id, "Quacky active release MUST stay isolated from Zez upload!");

    // Download & checksum verify
    const dl = await httpReq(baseUrl, "GET", `/api/app/download/${relId1}`);
    assert.equal(dl.status, 200);
    const dlSha = crypto.createHash("sha256").update(dl.buf).digest("hex");
    assert.equal(dlSha, sha1);

    // Disable & rollback/publish test
    const disableRes = await httpReq(baseUrl, "PATCH", `/api/app/releases/${relId1}`, { isActive: false }, adminHdr);
    assert.equal(disableRes.status, 200);
    const pubRes = await httpReq(baseUrl, "POST", `/api/app/releases/${relId1}/publish`, {}, adminHdr);
    assert.equal(pubRes.status, 200);
    console.log("PASS: Quacky/Zez release isolation, upload, SHA-256 download verification, disable & publish/rollback");

    // 5. Device Registration -> Encrypted Call Sync -> Cloud Backup Read & Verification
    const reg = await httpReq(baseUrl, "POST", "/api/devices/register", {
      ids: { androidId: "audit_android_" + Date.now(), persistentId: "audit_persist_" + Date.now() },
      deviceName: "Audit Emulator",
      deviceModel: "Pixel Test",
      platform: "Android",
      osVersion: "14",
      appVersion: "1.0.0",
    });
    assert.equal(reg.status, 200);
    createdDeviceId = reg.json.deviceId;
    const devHdr = { Authorization: `Bearer ${reg.json.token}` };

    const syncRes = await httpReq(
      baseUrl,
      "POST",
      "/api/calls/sync",
      {
        calls: [
          {
            externalId: "android:audit_1",
            phoneNumber: "+15550001111",
            name: "Audit Contact",
            type: "INCOMING",
            timestamp: Date.now() - 60000,
            duration: 42,
          },
        ],
      },
      devHdr
    );
    assert.equal(syncRes.status, 200);
    assert.equal(syncRes.json.inserted, 1);

    const stats = await httpReq(baseUrl, "GET", "/api/calls/read/stats", null, devHdr);
    assert.equal(stats.status, 200);
    assert.equal(stats.json.stats.totalCalls, "1");

    const readCalls = await httpReq(baseUrl, "GET", "/api/calls/read?limit=10", null, devHdr);
    assert.equal(readCalls.status, 200);
    assert.equal(readCalls.json.calls.length, 1);
    assert.equal(readCalls.json.calls[0].call.phoneNumber, "+15550001111");
    assert.equal(readCalls.json.calls[0].decryptionFailed, false);
    console.log("PASS: Device auth, AES-256-GCM call sync, and decrypted cloud backup verification");
  } finally {
    // Cleanup synthetic audit records only
    for (const rid of createdReleaseIds) {
      try {
        await httpReq(baseUrl, "DELETE", `/api/app/releases/${rid}`, null, {
          Authorization: `Bearer ${
            (await httpReq(baseUrl, "POST", "/api/admin/login", {
              username: process.env.ADMIN_USERNAME || "admin",
              password: process.env.ADMIN_PASSWORD || "admin123",
            })).json.token
          }`,
        });
      } catch {}
    }
    if (createdDeviceId) {
      await pool.query("DELETE FROM public.call_logs WHERE device_id = $1", [createdDeviceId]).catch(() => {});
      await pool.query("DELETE FROM public.devices WHERE id = $1", [createdDeviceId]).catch(() => {});
    }
    server.close();
    await closeDatabase();
  }
}

run()
  .then(() => {
    console.log("ALL AUDIT TESTS PASSED");
    process.exit(0);
  })
  .catch((err) => {
    console.error("AUDIT TEST FAILED:", err);
    process.exit(1);
  });
