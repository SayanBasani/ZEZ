const assert = require('node:assert');
const crypto = require('node:crypto');

const BASE_URL = 'http://localhost:3000';
const ADMIN_USER = 'admin';
const ADMIN_PASS = 'sayanbasanimandiraghoshbasani';

let adminToken = '';
let testDeviceId = '';
let deviceToken = '';
let testUserId1 = '';
let testUserId2 = '';
let testConvId = '';

let totalTests = 0;
let passedTests = 0;
let failedTests = 0;

async function test(name, fn) {
  totalTests++;
  process.stdout.write(`  [TEST] ${name} ... `);
  try {
    await fn();
    passedTests++;
    console.log('✅ PASS');
  } catch (err) {
    failedTests++;
    console.log(`❌ FAIL\n     Error: ${err.message}`);
  }
}

async function api(path, options = {}) {
  const url = `${BASE_URL}${path}`;
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (adminToken && !headers.Authorization && !options.noAuth) {
    headers.Authorization = `Bearer ${adminToken}`;
  }
  const res = await fetch(url, { ...options, headers });
  let data = null;
  try {
    data = await res.json();
  } catch {
    data = null;
  }
  return { status: res.status, ok: res.ok, data };
}

async function runExtremeTestSuite() {
  console.log('====================================================');
  console.log('🧪 RUNNING EXTREME STRESS & EDGE-CASE TEST SUITE');
  console.log('====================================================\n');

  // ── 1. AUTH & SECURITY EXTREME CASES ──────────────────────────
  console.log('--- 1. Authentication & Security Edge Cases ---');

  await test('Admin login with valid credentials', async () => {
    const res = await api('/api/admin/login', {
      method: 'POST',
      noAuth: true,
      body: JSON.stringify({ username: ADMIN_USER, password: ADMIN_PASS })
    });
    assert.strictEqual(res.status, 200);
    assert.strictEqual(res.data.success, true);
    assert.ok(res.data.token, 'Token must be present');
    adminToken = res.data.token;
  });

  await test('Reject login with wrong password without leaking stack trace', async () => {
    const res = await api('/api/admin/login', {
      method: 'POST',
      noAuth: true,
      body: JSON.stringify({ username: ADMIN_USER, password: 'WRONG_PASSWORD_123' })
    });
    assert.strictEqual(res.status, 401);
    assert.strictEqual(res.data.success, false);
    assert.strictEqual(typeof res.data.error, 'string');
    assert.ok(!res.data.error.includes('[object'), 'Must not return [object Object]');
  });

  await test('Reject login with empty body and empty strings', async () => {
    const res = await api('/api/admin/login', {
      method: 'POST',
      noAuth: true,
      body: JSON.stringify({ username: '', password: '' })
    });
    assert.strictEqual(res.status, 400);
    assert.strictEqual(res.data.success, false);
  });

  await test('Reject forged / tampered JWT token', async () => {
    const forgedToken = adminToken.slice(0, -10) + 'ABCDEFGHIJ';
    const res = await api('/api/admin/stats', {
      headers: { Authorization: `Bearer ${forgedToken}` }
    });
    assert.strictEqual(res.status, 401);
    assert.strictEqual(res.data.success, false);
  });

  await test('Reject malformed Authorization headers (empty / null / non-bearer)', async () => {
    const r1 = await api('/api/admin/stats', { headers: { Authorization: 'Basic dXNlcjpwYXNz' } });
    assert.strictEqual(r1.status, 401);
    const r2 = await api('/api/admin/stats', { headers: { Authorization: 'Bearer ' } });
    assert.strictEqual(r2.status, 401);
  });

  // ── 2. DEVICE REGISTRATION & CALL SYNC EDGE CASES ─────────────
  console.log('\n--- 2. Call Sync & Cryptographic Robustness Edge Cases ---');

  await test('Device registration with minimal valid info', async () => {
    const uniqueId = `test_dev_${Date.now()}`;
    const res = await api('/api/devices/register', {
      method: 'POST',
      noAuth: true,
      body: JSON.stringify({
        deviceId: uniqueId,
        androidId: uniqueId,
        deviceModel: 'ExtremeTestPhone',
        manufacturer: 'TestLab',
        appVersion: '1.0.12'
      })
    });
    assert.strictEqual(res.status, 200);
    assert.strictEqual(res.data.success, true);
    testDeviceId = res.data.deviceId || uniqueId;
    deviceToken = res.data.token;
  });

  await test('Handle corrupted AES encrypted payloads gracefully without crash', async () => {
    // Send random garbage ciphertext
    const corruptedPayload = 'corrupted_garbage_base64_not_aes==';
    const res = await api('/api/calls/sync', {
      method: 'POST',
      noAuth: true,
      headers: { Authorization: `Bearer ${deviceToken}` },
      body: JSON.stringify({
        deviceId: testDeviceId,
        calls: [
          {
            id: `call_${Date.now()}_bad`,
            encryptedPayload: corruptedPayload,
            timestamp: new Date().toISOString()
          }
        ]
      })
    });
    // Server should accept sync or report cleanly, never crash or return 500
    assert.ok(res.status === 200 || res.status === 400);
  });

  await test('Handle huge batch of calls (150 calls) safely', async () => {
    const calls = [];
    const now = Date.now();
    for (let i = 0; i < 150; i++) {
      calls.push({
        id: `batch_${now}_${i}`,
        type: 'INCOMING',
        phoneNumber: `+919876543${String(i).padStart(3, '0')}`,
        name: `Test Contact ${i}`,
        timestamp: new Date(now - i * 60000).toISOString(),
        duration: i * 5
      });
    }

    const res = await api('/api/calls/sync', {
      method: 'POST',
      noAuth: true,
      headers: { Authorization: `Bearer ${deviceToken}` },
      body: JSON.stringify({
        deviceId: testDeviceId,
        calls
      })
    });
    assert.strictEqual(res.status, 200);
    assert.strictEqual(res.data.success, true);
  });

  await test('Handle call logs with extreme boundary timestamps (Year 1970 & 2099)', async () => {
    const res = await api('/api/calls/sync', {
      method: 'POST',
      noAuth: true,
      headers: { Authorization: `Bearer ${deviceToken}` },
      body: JSON.stringify({
        deviceId: testDeviceId,
        calls: [
          {
            id: `call_past_${Date.now()}`,
            type: 'OUTGOING',
            phoneNumber: '+1000000000',
            name: 'Epoch Call',
            timestamp: new Date(0).toISOString(), // 1970
            duration: 10
          },
          {
            id: `call_future_${Date.now()}`,
            type: 'MISSED',
            phoneNumber: '+1999999999',
            name: 'Future Call',
            timestamp: new Date('2099-01-01T00:00:00Z').toISOString(),
            duration: 0
          }
        ]
      })
    });
    assert.strictEqual(res.status, 200);
    assert.strictEqual(res.data.success, true);
  });

  // ── 3. ADMIN QUERIES & SQL INJECTION RESISTANCE ───────────────
  console.log('\n--- 3. Admin Query Stress & SQL Injection Resistance ---');

  await test('SQL injection attempt on /calls search param `q`', async () => {
    const sqli = "'; DROP TABLE call_logs; --";
    const res = await api(`/api/admin/calls?q=${encodeURIComponent(sqli)}`);
    assert.strictEqual(res.status, 200);
    assert.strictEqual(res.data.success, true);
    assert.ok(Array.isArray(res.data.calls));
  });

  await test('SQL injection attempt on /calls deviceId and year', async () => {
    const sqliDev = "1' OR '1'='1";
    const res1 = await api(`/api/admin/calls?deviceId=${encodeURIComponent(sqliDev)}`);
    assert.strictEqual(res1.status, 200);

    const sqliYear = "2026; SELECT pg_sleep(5);";
    const res2 = await api(`/api/admin/calls?year=${encodeURIComponent(sqliYear)}`);
    assert.strictEqual(res2.status, 200);
  });

  await test('Invalid and partial date filters (`mm/dd/yyyy`, `undefined`, `null`)', async () => {
    const res = await api('/api/admin/calls?from=mm/dd/yyyy&to=undefined&month=99&year=abcd');
    assert.strictEqual(res.status, 200);
    assert.strictEqual(res.data.success, true);
    assert.strictEqual(typeof res.data.total, 'number');
  });

  await test('Extreme pagination limits (clamp to max 500, negative page handling)', async () => {
    const res1 = await api('/api/admin/calls?limit=999999&page=-5');
    assert.strictEqual(res1.status, 200);
    assert.ok(res1.data.calls.length <= 500, 'Limit must be clamped to 500 max');
    assert.strictEqual(res1.data.page, 1, 'Negative page must be normalized to 1');
  });

  // ── 4. CHAT SYSTEM & SECRET MODE CONCURRENCY ──────────────────
  console.log('\n--- 4. Chat System, Secret Mode & Ephemeral Audit ---');

  await test('Register users with unicode names & international numbers', async () => {
    const phone1 = `+9199000${Math.floor(Math.random() * 90000 + 10000)}`;
    const phone2 = `+9199111${Math.floor(Math.random() * 90000 + 10000)}`;

    const r1 = await api('/api/chat/register', {
      method: 'POST',
      noAuth: true,
      body: JSON.stringify({
        phoneNumber: phone1,
        name: 'User Alpha 🚀🔥'
      })
    });
    assert.strictEqual(r1.status, 200);
    testUserId1 = r1.data.user.id;

    const r2 = await api('/api/chat/register', {
      method: 'POST',
      noAuth: true,
      body: JSON.stringify({
        phoneNumber: phone2,
        name: 'User Beta 💻✨'
      })
    });
    assert.strictEqual(r2.status, 200);
    testUserId2 = r2.data.user.id;
  });

  await test('Start conversation and send normal messages', async () => {
    const r1 = await api('/api/chat/messages', {
      method: 'POST',
      noAuth: true,
      body: JSON.stringify({
        senderId: testUserId1,
        receiverId: testUserId2,
        messageText: 'Hello Beta! This is a normal test message.'
      })
    });
    assert.strictEqual(r1.status, 200);
    assert.strictEqual(r1.data.success, true);
    testConvId = r1.data.conversationId || r1.data.message?.conversation_id;
  });

  await test('Toggle Secret Mode and verify audit visibility', async () => {
    // 1. Toggle secret mode ON
    const rToggleOn = await api(`/api/chat/conversations/${testConvId}/mode`, {
      method: 'POST',
      noAuth: true,
      body: JSON.stringify({
        userId: testUserId1,
        isSecretMode: true
      })
    });
    assert.strictEqual(rToggleOn.status, 200);
    assert.strictEqual(rToggleOn.data.isSecretMode, true);

    // 2. Send secret message
    const rMsg = await api('/api/chat/messages', {
      method: 'POST',
      noAuth: true,
      body: JSON.stringify({
        senderId: testUserId1,
        receiverId: testUserId2,
        messageText: 'Confidential secret message 🔒🤫'
      })
    });
    assert.strictEqual(rMsg.status, 200);

    // 3. Admin Inspector should audit secret message
    const rAdmin = await api(`/api/admin/chats/conversations/${testConvId}/messages`);
    assert.strictEqual(rAdmin.status, 200);
    const hasSecret = rAdmin.data.messages.some(m => m.mode === 'secret');
    assert.ok(hasSecret, 'Admin must be able to audit secret messages in the backend');

    // 4. Toggle secret mode OFF
    const rToggleOff = await api(`/api/chat/conversations/${testConvId}/mode`, {
      method: 'POST',
      noAuth: true,
      body: JSON.stringify({
        userId: testUserId2,
        isSecretMode: false
      })
    });
    assert.strictEqual(rToggleOff.status, 200);
    assert.strictEqual(rToggleOff.data.isSecretMode, false);
  });

  await test('Rapid concurrent messages simulation (10 parallel messages)', async () => {
    const promises = [];
    for (let i = 0; i < 10; i++) {
      promises.push(
        api('/api/chat/messages', {
          method: 'POST',
          noAuth: true,
          body: JSON.stringify({
            senderId: (i % 2 === 0) ? testUserId1 : testUserId2,
            receiverId: (i % 2 === 0) ? testUserId2 : testUserId1,
            messageText: `Rapid stress message #${i}`
          })
        })
      );
    }
    const results = await Promise.all(promises);
    for (const r of results) {
      assert.strictEqual(r.status, 200);
      assert.strictEqual(r.data.success, true);
    }
  });

  await test('Export chat transcripts (TXT, CSV, JSON)', async () => {
    const rTxt = await api(`/api/admin/chats/export/${testConvId}?format=txt`);
    assert.strictEqual(rTxt.status, 200);
    const rCsv = await api(`/api/admin/chats/export/${testConvId}?format=csv`);
    assert.strictEqual(rCsv.status, 200);
    const rJson = await api(`/api/admin/chats/export/${testConvId}?format=json`);
    assert.strictEqual(rJson.status, 200);
    assert.strictEqual(rJson.data.success, true);
  });

  // ── SUMMARY ──────────────────────────────────────────────────
  console.log('\n====================================================');
  console.log(`📊 TEST SUITE FINISHED: ${passedTests}/${totalTests} PASSED`);
  if (failedTests > 0) {
    console.log(`❌ ${failedTests} TESTS FAILED!`);
    process.exit(1);
  } else {
    console.log('🎉 ALL EXTREME AND STRESS CASES PASSED 100%!');
    console.log('====================================================\n');
    process.exit(0);
  }
}

runExtremeTestSuite().catch(err => {
  console.error('Fatal test error:', err);
  process.exit(1);
});
