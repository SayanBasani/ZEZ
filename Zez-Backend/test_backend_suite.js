const http = require('node:http');
const path = require('node:path');
require('dotenv').config({ path: path.join(__dirname, '.env') });
const { pool, initializeDatabase, closeDatabase } = require('./src/db');
const app = require('./src/server');

let server;
let baseUrl;

function request(method, path, body = null, headers = {}) {
  return new Promise((resolve, reject) => {
    const url = new URL(baseUrl + path);
    const reqHeaders = { ...headers };
    let payload = null;

    if (body !== null && typeof body === 'object') {
      payload = JSON.stringify(body);
      reqHeaders['Content-Type'] = 'application/json';
      reqHeaders['Content-Length'] = Buffer.byteLength(payload);
    }

    const options = {
      method,
      hostname: url.hostname,
      port: url.port,
      path: url.pathname + url.search,
      headers: reqHeaders,
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        let json = null;
        try {
          json = JSON.parse(data);
        } catch {}
        resolve({
          status: res.statusCode,
          headers: res.headers,
          data,
          json,
        });
      });
    });

    req.on('error', reject);
    if (payload) req.write(payload);
    req.end();
  });
}

const tests = [];
function test(name, fn) {
  tests.push({ name, fn });
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message || 'Assertion failed');
  }
}

// ------------------- TEST SUITE DEFINITIONS -------------------

test('1. Health Check (GET /health)', async () => {
  const res = await request('GET', '/health');
  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(res.json?.status === 'ok', 'Expected status: ok');
});

test('2. Ping API (POST /api/ping)', async () => {
  const res = await request('POST', '/api/ping', { source: 'test-runner', event: 'ping' });
  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(res.json?.status === 'acknowledged', 'Expected status: acknowledged');
});

let testDeviceId;
let testDeviceToken;
const testAndroidId = 'test_android_' + Date.now();
const testPersistentId = 'test_persistent_' + Date.now();

test('3. Device Registration - Missing IDs (POST /api/devices/register)', async () => {
  const res = await request('POST', '/api/devices/register', { ids: {} });
  assert(res.status === 400, `Expected 400 for missing IDs, got ${res.status}`);
  assert(res.json?.success === false, 'Expected success: false');
});

test('4. Device Registration - Success (POST /api/devices/register)', async () => {
  const res = await request('POST', '/api/devices/register', {
    ids: { androidId: testAndroidId, persistentId: testPersistentId },
    deviceName: 'Test Phone X',
    deviceModel: 'Pixel 6 Test',
    manufacturer: 'Google',
    brand: 'google',
    platform: 'Android',
    osVersion: '14',
    appVersion: '1.0.5',
    deviceDetails: { testSim: 'Carrier A', batteryLevel: 95 }
  });
  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(res.json?.deviceId, 'Expected deviceId');
  assert(res.json?.token, 'Expected token');
  testDeviceId = res.json.deviceId;
  testDeviceToken = res.json.token;
});

test('5. Device Re-registration / Update (POST /api/devices/register)', async () => {
  const res = await request('POST', '/api/devices/register', {
    ids: { androidId: testAndroidId, persistentId: testPersistentId },
    deviceName: 'Test Phone X Updated',
    deviceModel: 'Pixel 6 Test',
    appVersion: '1.0.5',
  });
  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.deviceId === testDeviceId, 'Device ID should remain unchanged on re-registration');
  assert(res.json?.token, 'A refreshed token should be issued');
  testDeviceToken = res.json.token;
});

test('6. Call Sync - Unauthorized Request without Token (POST /api/calls/sync)', async () => {
  const res = await request('POST', '/api/calls/sync', { calls: [] });
  assert(res.status === 401, `Expected 401 Unauthorized, got ${res.status}`);
});

test('7. Call Sync - Invalid Token (POST /api/calls/sync)', async () => {
  const res = await request('POST', '/api/calls/sync', { calls: [] }, {
    Authorization: 'Bearer invalid_token_12345'
  });
  assert(res.status === 401, `Expected 401 for bad token, got ${res.status}`);
});

test('8. Call Sync - Validation Error: Non-array calls (POST /api/calls/sync)', async () => {
  const res = await request('POST', '/api/calls/sync', { calls: 'not an array' }, {
    Authorization: `Bearer ${testDeviceToken}`
  });
  assert(res.status === 400, `Expected 400 for non-array calls, got ${res.status}`);
});

const callTimestamp1 = Date.now() - 86400000; // 1 day ago
const callTimestamp2 = Date.now() - (86400000 * 365); // 1 year ago (Simulating 1-year-old call)
const call1Id = 'call_test_' + Date.now() + '_1';
const call2Id = 'call_test_' + Date.now() + '_2';

test('9. Call Sync - Normal Batch with 1-Year-Old History (POST /api/calls/sync)', async () => {
  const res = await request('POST', '/api/calls/sync', {
    calls: [
      {
        id: call1Id,
        phoneNumber: '+15551234567',
        name: 'Recent Contact',
        type: 'INCOMING',
        timestamp: callTimestamp1,
        duration: 120
      },
      {
        id: call2Id,
        phoneNumber: '+15559876543',
        name: 'Old Contact 1 Year Ago',
        type: 'OUTGOING',
        timestamp: callTimestamp2,
        duration: 345
      }
    ],
    deviceDetails: { batteryPercent: 90 }
  }, {
    Authorization: `Bearer ${testDeviceToken}`
  });

  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(res.json?.received === 2, `Expected 2 received, got ${res.json?.received}`);
  assert(res.json?.inserted === 2, `Expected 2 inserted, got ${res.json?.inserted}`);
  assert(res.json?.duplicates === 0, `Expected 0 duplicates, got ${res.json?.duplicates}`);
});

test('10. Call Sync - Deduplication Test (Submitting same calls again)', async () => {
  const res = await request('POST', '/api/calls/sync', {
    calls: [
      {
        id: call1Id,
        phoneNumber: '+15551234567',
        name: 'Recent Contact',
        type: 'INCOMING',
        timestamp: callTimestamp1,
        duration: 120
      },
      {
        id: call2Id,
        phoneNumber: '+15559876543',
        name: 'Old Contact 1 Year Ago',
        type: 'OUTGOING',
        timestamp: callTimestamp2,
        duration: 345
      }
    ]
  }, {
    Authorization: `Bearer ${testDeviceToken}`
  });

  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(res.json?.received === 2, `Expected 2 received, got ${res.json?.received}`);
  assert(res.json?.inserted === 0, `Expected 0 inserted for exact duplicates, got ${res.json?.inserted}`);
  assert(res.json?.duplicates === 2, `Expected 2 duplicates recognized, got ${res.json?.duplicates}`);
});

test('11. Call Read - Verify Synced & Decrypted Records (GET /api/calls/read)', async () => {
  const res = await request('GET', '/api/calls/read?limit=10', null, {
    Authorization: `Bearer ${testDeviceToken}`
  });

  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(Array.isArray(res.json?.calls), 'Expected calls array');
  assert(res.json?.calls.length >= 2, `Expected at least 2 calls, got ${res.json?.calls.length}`);

  const found1YearOld = res.json.calls.find(c => c.call && c.call.externalId === call2Id);
  assert(found1YearOld, 'Expected to find the 1-year-old call decrypted in the read endpoint');
  assert(found1YearOld.call.name === 'Old Contact 1 Year Ago', 'Name must match decrypted value');
  assert(found1YearOld.decryptionFailed === false, 'decryptionFailed must be false');
});

test('12. Call Read - Stats Endpoint (GET /api/calls/read/stats)', async () => {
  const res = await request('GET', '/api/calls/read/stats', null, {
    Authorization: `Bearer ${testDeviceToken}`
  });

  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(Number(res.json?.stats?.totalCalls) >= 2, `Expected totalCalls >= 2, got ${res.json?.stats?.totalCalls}`);
});

let adminToken;
test('13. Admin Login - Bad Password (POST /api/admin/login)', async () => {
  const res = await request('POST', '/api/admin/login', {
    username: process.env.ADMIN_USERNAME || 'admin',
    password: 'wrong_password_xyz'
  });
  assert(res.status === 401, `Expected 401, got ${res.status}`);
});

test('14. Admin Login - Valid Password (POST /api/admin/login)', async () => {
  const res = await request('POST', '/api/admin/login', {
    username: process.env.ADMIN_USERNAME || 'admin',
    password: process.env.ADMIN_PASSWORD
  });
  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(res.json?.token, 'Expected admin JWT token');
  adminToken = res.json.token;
});

test('15. Admin Stats API (GET /api/admin/stats)', async () => {
  const res = await request('GET', '/api/admin/stats', null, {
    Authorization: `Bearer ${adminToken}`
  });
  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(typeof res.json?.stats?.total_devices === 'number', 'Expected total_devices in stats');
  assert(typeof res.json?.stats?.total_calls === 'number', 'Expected total_calls in stats');
});

test('16. Admin Devices API (GET /api/admin/devices)', async () => {
  const res = await request('GET', '/api/admin/devices', null, {
    Authorization: `Bearer ${adminToken}`
  });
  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(Array.isArray(res.json?.devices), 'Expected devices array');
  const found = res.json.devices.find(d => d.id === testDeviceId);
  assert(found, 'Expected to find registered test device in admin devices list');
});

test('17. Admin Calls API with Search & Filters (GET /api/admin/calls)', async () => {
  const res = await request('GET', `/api/admin/calls?deviceId=${testDeviceId}&q=Old+Contact`, null, {
    Authorization: `Bearer ${adminToken}`
  });
  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(Array.isArray(res.json?.calls), 'Expected calls array');
  assert(res.json.calls.length >= 1, 'Expected at least 1 matched call');
  assert(res.json.calls[0].call.name === 'Old Contact 1 Year Ago', 'Should find old contact call');
});

test('18. App Releases Public Endpoint (GET /api/app/latest)', async () => {
  const res = await request('GET', '/api/app/latest');
  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(res.json?.hasRelease === true, 'Expected hasRelease: true');
  assert(res.json?.versionCode >= 6, `Expected versionCode >= 6, got ${res.json?.versionCode}`);
  assert(res.json?.downloadUrl, 'Expected downloadUrl');
});

test('19. App Releases Admin Protected Endpoint (GET /api/app/releases)', async () => {
  const res = await request('GET', '/api/app/releases', null, {
    Authorization: `Bearer ${adminToken}`
  });
  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(Array.isArray(res.json?.releases), 'Expected releases array');
  assert(res.json.releases.length >= 1, 'Expected at least 1 release in list');
});

// ------------------- ADVANCED REAL-WORLD SIMULATION TESTS -------------------

test('20. Real-Life Simulation: Tolerating Duplicate IDs within a Single Batch', async () => {
  const dupId = 'dup_call_' + Date.now();
  const res = await request('POST', '/api/calls/sync', {
    calls: [
      { id: dupId, phoneNumber: '+1234567890', name: 'Dup Contact', type: 'INCOMING', timestamp: Date.now() - 5000, duration: 10 },
      { id: dupId, phoneNumber: '+1234567890', name: 'Dup Contact', type: 'INCOMING', timestamp: Date.now() - 5000, duration: 10 },
      { id: 'unique_' + Date.now(), phoneNumber: '+1987654321', name: 'Unique Contact', type: 'OUTGOING', timestamp: Date.now(), duration: 25 }
    ]
  }, {
    Authorization: `Bearer ${testDeviceToken}`
  });

  assert(res.status === 200, `Expected 200 (graceful deduplication instead of crash 400), got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(res.json?.duplicates >= 1, 'Expected at least 1 duplicate detected');
  assert(res.json?.inserted >= 1, 'Expected unique call to be inserted');
});

test('21. Real-Life Simulation: Multi-Year History Chunked Sync (30 calls across 3 sequential batches)', async () => {
  const totalCallsToSimulate = 30;
  const chunkSize = 10;
  let totalInserted = 0;
  const now = Date.now();

  for (let b = 0; b < totalCallsToSimulate / chunkSize; b++) {
    const chunk = [];
    for (let i = 0; i < chunkSize; i++) {
      const callIndex = b * chunkSize + i;
      // Spread calls across past 730 days (2 years)
      const dayOffset = (callIndex / totalCallsToSimulate) * 730;
      const callTime = now - Math.floor(dayOffset * 86400000);
      chunk.push({
        id: `sim_2yr_${now}_${callIndex}`,
        phoneNumber: `+1999000${String(callIndex).padStart(4, '0')}`,
        name: `Contact #${callIndex} (${Math.round(dayOffset)}d ago)`,
        type: callIndex % 3 === 0 ? 'INCOMING' : callIndex % 3 === 1 ? 'OUTGOING' : 'MISSED',
        timestamp: callTime,
        duration: (callIndex * 17) % 600
      });
    }

    const res = await request('POST', '/api/calls/sync', { calls: chunk }, {
      Authorization: `Bearer ${testDeviceToken}`
    });

    assert(res.status === 200, `Batch ${b + 1} failed with status ${res.status}`);
    assert(res.json?.success === true, `Batch ${b + 1} failed`);
    totalInserted += (res.json?.inserted || 0);
  }

  assert(totalInserted === totalCallsToSimulate, `Expected ${totalCallsToSimulate} calls inserted, got ${totalInserted}`);
});

test('22. Real-Life Simulation: Querying 2-Year Old History through Admin Search', async () => {
  const res = await request('GET', `/api/admin/calls?deviceId=${testDeviceId}&q=706d+ago&limit=50`, null, {
    Authorization: `Bearer ${adminToken}`
  });
  assert(res.status === 200, `Expected 200, got ${res.status}`);
  assert(res.json?.success === true, 'Expected success: true');
  assert(Array.isArray(res.json?.calls) && res.json.calls.length >= 1, 'Expected to find calls from ~706 days ago');
  assert(res.json.calls[0].call.name.includes('706d ago'), 'Verified ~2-year old call retrieved accurately');
});

test('23. Security & Resilience: Rejecting Oversized Batch (> 500 calls in single request)', async () => {
  const hugeBatch = [];
  for (let i = 0; i < 505; i++) {
    hugeBatch.push({
      id: `oversized_${Date.now()}_${i}`,
      phoneNumber: '+1000000000',
      type: 'INCOMING',
      timestamp: Date.now()
    });
  }
  const res = await request('POST', '/api/calls/sync', { calls: hugeBatch }, {
    Authorization: `Bearer ${testDeviceToken}`
  });
  assert(res.status === 413, `Expected 413 Payload Too Large for 505 calls, got ${res.status}`);
});

// ------------------- RUNNER -------------------
async function run() {
  console.log('🚀 Starting Backend Verification & Test Suite...');
  await initializeDatabase();

  // Start temporary local test server on random free port
  server = http.createServer(app);
  await new Promise((resolve) => {
    server.listen(0, '127.0.0.1', () => {
      const port = server.address().port;
      baseUrl = `http://127.0.0.1:${port}`;
      console.log(`📡 Test server running on ${baseUrl}`);
      resolve();
    });
  });

  let passed = 0;
  let failed = 0;

  for (const t of tests) {
    try {
      const start = Date.now();
      await t.fn();
      const elapsed = Date.now() - start;
      console.log(`  ✅ PASSED: ${t.name} (${elapsed}ms)`);
      passed++;
    } catch (err) {
      console.error(`  ❌ FAILED: ${t.name}`);
      console.error(`     Error: ${err.message}`);
      failed++;
    }
  }

  // Cleanup test data created during test
  try {
    if (testDeviceId) {
      await pool.query('DELETE FROM public.call_logs WHERE device_id = $1', [testDeviceId]);
      await pool.query('DELETE FROM public.devices WHERE id = $1', [testDeviceId]);
      console.log(`🧹 Cleaned up test device ${testDeviceId}`);
    }
  } catch (cleanErr) {
    console.warn('Cleanup warning:', cleanErr.message);
  }

  server.close();
  await closeDatabase();

  console.log('\n======================================');
  console.log(`Test Results: ${passed} PASSED | ${failed} FAILED | Total: ${tests.length}`);
  console.log('======================================');

  if (failed > 0) {
    process.exit(1);
  } else {
    process.exit(0);
  }
}

run().catch((err) => {
  console.error('Test runner fatal error:', err);
  process.exit(1);
});
