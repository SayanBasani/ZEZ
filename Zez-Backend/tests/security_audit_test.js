const http = require('http');

function makeRequest(options, postData = null) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => {
        let json = null;
        try { json = JSON.parse(data); } catch (_) {}
        resolve({ status: res.statusCode, headers: res.headers, body: data, json });
      });
    });
    req.on('error', reject);
    req.on('timeout', () => { req.destroy(); reject(new Error('Timeout')); });
    req.setTimeout(5000);
    if (postData) req.write(postData);
    req.end();
  });
}

async function runSecurityAudit() {
  console.log('=== STARTING BACKEND SECURITY VULNERABILITY AUDIT ===\n');
  const findings = [];

  // TEST 1: Unauthenticated Admin Endpoint Probing
  console.log('[Test 1] Testing unauthenticated access to admin endpoints...');
  const adminEndpoints = [
    '/api/admin/stats',
    '/api/admin/devices',
    '/api/admin/calls',
    '/api/admin/system/summary',
    '/api/admin/files',
    '/api/admin/users',
    '/api/app/releases'
  ];
  for (const ep of adminEndpoints) {
    const res = await makeRequest({ hostname: '127.0.0.1', port: 3000, path: ep, method: 'GET' });
    if (res.status === 200) {
      findings.push({ severity: 'HIGH', category: 'Broken Access Control', issue: `Endpoint ${ep} is accessible without admin authentication! Status: 200` });
    } else if (res.status === 401 || res.status === 403) {
      console.log(`  âœ“ ${ep} correctly rejected with ${res.status}`);
    } else {
      console.log(`  ? ${ep} returned ${res.status}`);
    }
  }

  // TEST 2: Path Traversal on Storage & Download Endpoints
  console.log('\n[Test 2] Testing Path Traversal / Arbitrary File Access...');
  const traversalPayloads = [
    '/api/storage/file/../../.env',
    '/api/storage/file/..%2f..%2f.env',
    '/api/storage/file/%2e%2e%2f%2e%2e%2f%2e%2e%2f.env',
    '/api/storage/file/....//....//.env',
    '/downloads/../../.env',
    '/downloads/..%2f..%2f.env'
  ];
  for (const payload of traversalPayloads) {
    try {
      const res = await makeRequest({ hostname: '127.0.0.1', port: 3000, path: payload, method: 'GET' });
      if (res.body && (res.body.includes('DATABASE_URL') || res.body.includes('ADMIN_PASSWORD'))) {
        findings.push({ severity: 'CRITICAL', category: 'Path Traversal', issue: `Payload ${payload} leaked sensitive environment variables!` });
      } else if (res.status === 200 && res.body.length > 0 && !res.body.includes('<!DOCTYPE html>')) {
        findings.push({ severity: 'HIGH', category: 'Path Traversal', issue: `Payload ${payload} returned file contents! Status: ${res.status}` });
      } else {
        console.log(`  âœ“ Path traversal ${payload} safely blocked (status ${res.status})`);
      }
    } catch (e) {
      console.log(`  âœ“ Path traversal ${payload} connection rejected: ${e.message}`);
    }
  }

  // TEST 3: Brute Force Throttling on Admin Login
  console.log('\n[Test 3] Testing Rate Limiting on Admin Login...');
  let loginFailCount = 0;
  let rateLimited = false;
  for (let i = 0; i < 25; i++) {
    const postData = JSON.stringify({ username: 'admin', password: 'wrongpassword_' + i });
    const res = await makeRequest({
      hostname: '127.0.0.1',
      port: 3000,
      path: '/api/admin/login',
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(postData) }
    }, postData);
    if (res.status === 429) {
      rateLimited = true;
      break;
    }
    if (res.status === 401) loginFailCount++;
  }
  if (!rateLimited) {
    findings.push({
      severity: 'MEDIUM',
      category: 'Missing Rate Limiting',
      issue: 'Admin login endpoint (/api/admin/login) has no rate limiting; 25 rapid failed attempts succeeded with 401 without 429 Too Many Requests.'
    });
    console.log(`  ! No rate limiting detected: ${loginFailCount} failed login requests accepted without 429`);
  } else {
    console.log(`  âœ“ Rate limiting active on /api/admin/login`);
  }

  // TEST 4: SQL Injection Probing on Query Parameters
  console.log('\n[Test 4] Testing SQL Injection resilience...');
  const sqliPayloads = [
    { path: "/api/chat/messages?conversationId=' OR '1'='1", method: "GET" },
    { path: "/api/chat/messages?conversationId=00000000-0000-0000-0000-000000000000' UNION SELECT 1,2,3--", method: "GET" },
    { path: "/api/admin/devices?q=' OR 1=1--", method: "GET" },
    { path: "/api/calls/read?limit=10; DROP TABLE dummy;--", method: "GET" }
  ];
  for (const item of sqliPayloads) {
    try {
      const res = await makeRequest({ hostname: '127.0.0.1', port: 3000, path: item.path, method: item.method });
      if (res.status === 500 && (res.body.includes('syntax error') || res.body.includes('pg_'))) {
        findings.push({ severity: 'HIGH', category: 'SQL Injection Error Leak', issue: `SQL query failed with database syntax error on ${item.path}: ${res.body}` });
      } else {
        console.log(`  âœ“ SQLi payload on ${item.path} handled safely (status ${res.status})`);
      }
    } catch (e) {
      console.log(`  âœ“ Payload error handled: ${e.message}`);
    }
  }

  // TEST 5: IDOR on Chat Messages
  console.log('\n[Test 5] Testing IDOR on /api/chat/messages...');
  // Request messages with non-participating or missing userId
  const convListRes = await makeRequest({ hostname: '127.0.0.1', port: 3000, path: '/api/chat/conversations?userId=624db85d-2c83-45c3-9b4d-c3f4dd12b7c3', method: 'GET' });
  if (convListRes.json && convListRes.json.conversations && convListRes.json.conversations.length > 0) {
    const testConvId = convListRes.json.conversations[0].id;
    // Attempt to read without userId
    const noUserRes = await makeRequest({ hostname: '127.0.0.1', port: 3000, path: `/api/chat/messages?conversationId=${testConvId}`, method: 'GET' });
    if (noUserRes.json && noUserRes.json.messages && noUserRes.json.messages.length > 0) {
      findings.push({
        severity: 'HIGH',
        category: 'IDOR',
        issue: `Endpoint /api/chat/messages?conversationId=${testConvId} returned full chat messages without verifying user identity/participant!`
      });
      console.log(`  ! IDOR detected: conversation messages accessible without participant validation`);
    } else {
      console.log(`  âœ“ /api/chat/messages requires valid participant`);
    }
  }

  // TEST 6: Denial of Service / Malformed Body Handling
  console.log('\n[Test 6] Testing Malformed Payloads & DoS resilience...');
  const malformedBodies = [
    '{ "broken": json }',
    JSON.stringify({ __proto__: { admin: true } }),
    JSON.stringify({ ids: { androidId: 'A'.repeat(50000) } })
  ];
  for (const b of malformedBodies) {
    try {
      const res = await makeRequest({
        hostname: '127.0.0.1',
        port: 3000,
        path: '/api/devices/register',
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(b) }
      }, b);
      console.log(`  âœ“ Server responded gracefully to malformed body with status ${res.status}`);
    } catch (e) {
      findings.push({ severity: 'HIGH', category: 'DoS / Server Crash', issue: `Server crashed or dropped connection on malformed body: ${e.message}` });
    }
  }

  // TEST 7: Check if server is still alive
  console.log('\n[Test 7] Verifying server health after tests...');
  try {
    const health = await makeRequest({ hostname: '127.0.0.1', port: 3000, path: '/health', method: 'GET' });
    if (health.status === 200) {
      console.log('  âœ“ Server is healthy and running!');
    } else {
      findings.push({ severity: 'CRITICAL', category: 'Availability', issue: `Health check failed with status ${health.status}` });
    }
  } catch (e) {
    findings.push({ severity: 'CRITICAL', category: 'Availability', issue: `Server is down! ${e.message}` });
  }

  console.log('\n=== SECURITY AUDIT SUMMARY ===');
  console.log(`Total Findings: ${findings.length}`);
  findings.forEach((f, idx) => {
    console.log(`\n[${idx + 1}] Severity: ${f.severity} | Category: ${f.category}\n    Details: ${f.issue}`);
  });
  return findings;
}

runSecurityAudit().catch(console.error);
