require('dotenv').config();
const { pool } = require('../src/db');
const http = require('http');

const BASE_URL = 'http://localhost:3000';

function makeRequest(method, path, body = null, headers = {}) {
  return new Promise((resolve, reject) => {
    const url = new URL(path, BASE_URL);
    const options = {
      hostname: url.hostname,
      port: url.port,
      path: url.pathname + url.search,
      method: method,
      headers: {
        'Content-Type': 'application/json',
        ...headers
      }
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          const parsed = JSON.parse(data);
          resolve({ status: res.statusCode, body: parsed });
        } catch (e) {
          resolve({ status: res.statusCode, body: data });
        }
      });
    });

    req.on('error', reject);
    if (body) {
      req.write(typeof body === 'string' ? body : JSON.stringify(body));
    }
    req.end();
  });
}

async function runChatTestSuite() {
  console.log('====================================================');
  console.log('  ZEZ CHAT & NOTIFICATIONS EXTREME TEST SUITE');
  console.log('====================================================\n');

  let passed = 0;
  let failed = 0;

  function assert(condition, testName) {
    if (condition) {
      console.log(`  ✓ PASS: ${testName}`);
      passed++;
    } else {
      console.error(`  ✗ FAIL: ${testName}`);
      failed++;
    }
  }

  try {
    // 1. User Registration / Lookup
    console.log('[Test Group 1: User Profile & Direct Contact]');
    const user1Res = await makeRequest('POST', '/api/chat/register', {
      phone_number: '9735154080',
      name: 'Physical Device User (Sayan)',
      status_message: 'Testing actual mobile'
    });
    assert(user1Res.status === 200 && user1Res.body.success, 'Register User 1 (Physical Mobile 9735154080)');
    const user1 = user1Res.body.user;

    const user2Res = await makeRequest('POST', '/api/chat/register', {
      phone_number: '1234567890',
      name: 'Android Studio Virtual Device',
      status_message: 'Testing emulator'
    });
    assert(user2Res.status === 200 && user2Res.body.success, 'Register User 2 (Emulator 1234567890)');
    const user2 = user2Res.body.user;

    // Direct contact resolution
    const directRes = await makeRequest('POST', '/api/chat/direct-contact', {
      phone_number: '1234567890',
      name: 'Virtual Phone'
    });
    assert(directRes.status === 200 && directRes.body.user.id === user2.id, 'Direct contact resolves existing user');

    // 2. Normal Mode Message Exchange & Status Ticks
    console.log('\n[Test Group 2: Normal Mode & WhatsApp Status Ticks (Sent -> Delivered -> Read)]');
    const send1 = await makeRequest('POST', '/api/chat/messages/send', {
      sender_id: user1.id,
      receiver_id: user2.id,
      message_text: 'Hello from real phone! 👋',
      mode: 'normal'
    });
    assert(send1.status === 200 && send1.body.message.status === 'sent', 'Message sent by User 1 (status: sent / single ash tick)');
    const msg1 = send1.body.message;
    const convId = msg1.conversation_id;
    assert(!!convId, 'Conversation ID generated: ' + convId);

    // User 2 polls for incoming messages
    const poll2 = await makeRequest('GET', `/api/chat/poll?userId=${user2.id}&since=${new Date(Date.now() - 60000).toISOString()}`);
    assert(poll2.status === 200 && poll2.body.incoming.some(m => m.id === msg1.id), 'User 2 poll receives User 1 message');

    // Verify status was transitioned to 'delivered' upon poll
    const statusCheck1 = await pool.query('SELECT status FROM public.chat_messages WHERE id = $1', [msg1.id]);
    assert(statusCheck1.rows[0].status === 'delivered', 'Message status transitioned to delivered (double ash tick)');

    // User 1 polls for status updates
    const poll1 = await makeRequest('GET', `/api/chat/poll?userId=${user1.id}&conversationId=${convId}`);
    assert(poll1.body.status_updates.some(s => s.id === msg1.id && (s.status === 'delivered' || s.status === 'read')), 'User 1 poll receives delivered status update');

    // User 2 opens conversation -> marks as read
    const readRes = await makeRequest('POST', '/api/chat/messages/status', {
      conversation_id: convId,
      receiver_id: user2.id,
      status: 'read'
    });
    assert(readRes.status === 200 && readRes.body.success, 'User 2 marks messages as read');

    // Verify status updated to 'read' (double blue tick)
    const statusCheck2 = await pool.query('SELECT status FROM public.chat_messages WHERE id = $1', [msg1.id]);
    assert(statusCheck2.rows[0].status === 'read', 'Message status transitioned to read (double blue tick)');

    // 3. Ephemeral Secret Mode Synchronization
    console.log('\n[Test Group 3: Secret Mode Toggle & Ephemeral Synchronization]');
    // User 1 turns on Secret Mode
    const modeOn = await makeRequest('POST', `/api/chat/conversation/${convId}/mode`, {
      isSecretMode: true,
      userId: user1.id
    });
    assert(modeOn.status === 200 && modeOn.body.isSecretMode === true, 'User 1 enables Secret Mode on conversation');
    const secretSessionId = modeOn.body.secretSessionId;
    assert(!!secretSessionId, 'Secret session UUID created: ' + secretSessionId);

    // User 2 polls -> verifies mode synchronized to secret
    const pollMode2 = await makeRequest('GET', `/api/chat/poll?userId=${user2.id}&conversationId=${convId}`);
    assert(pollMode2.body.conversation_mode && pollMode2.body.conversation_mode.isSecretMode === true, 'User 2 poll detects Secret Mode enabled by peer');
    assert(pollMode2.body.conversation_mode.secretSessionId === secretSessionId, 'User 2 gets identical secret session ID');

    // User 1 sends a secret message
    const secretSend = await makeRequest('POST', '/api/chat/messages/send', {
      sender_id: user1.id,
      receiver_id: user2.id,
      message_text: 'This is a secret ephemeral message 🤫',
      mode: 'secret',
      secret_session_id: secretSessionId
    });
    assert(secretSend.status === 200 && secretSend.body.message.mode === 'secret', 'Secret message sent');
    const secretMsgId = secretSend.body.message.id;

    // Normal messages endpoint should NOT return the secret message
    const normalFetch = await makeRequest('GET', `/api/chat/messages?conversationId=${convId}&mode=normal`);
    assert(!normalFetch.body.messages.some(m => m.id === secretMsgId), 'Secret message NOT visible in normal mode message history');

    // Secret messages endpoint with current secretSessionId returns it
    const secretFetch = await makeRequest('GET', `/api/chat/messages?conversationId=${convId}&mode=secret&secretSessionId=${secretSessionId}`);
    assert(secretFetch.body.messages.some(m => m.id === secretMsgId), 'Secret message returned when viewing active secret session');

    // Secret messages endpoint WITHOUT secretSessionId (e.g. fresh launch or re-entry) returns blank/empty screen
    const blankFetch = await makeRequest('GET', `/api/chat/messages?conversationId=${convId}&mode=secret`);
    assert(blankFetch.body.messages.length === 0, 'Secret mode without session ID returns fresh empty screen');

    // User 2 turns off Secret Mode
    const modeOff = await makeRequest('POST', `/api/chat/conversation/${convId}/mode`, {
      isSecretMode: false,
      userId: user2.id
    });
    assert(modeOff.status === 200 && modeOff.body.isSecretMode === false, 'User 2 disables Secret Mode');

    // User 1 polls -> detects mode returned to normal
    const pollMode1 = await makeRequest('GET', `/api/chat/poll?userId=${user1.id}&conversationId=${convId}`);
    assert(pollMode1.body.conversation_mode && pollMode1.body.conversation_mode.isSecretMode === false, 'User 1 poll detects return to Normal Mode');

    // 4. Admin Audit & Full Message Inspection
    console.log('\n[Test Group 4: Admin Audit & Dual-Mode Visibility]');
    // Verify admin can inspect both normal and secret messages in database
    const adminCheck = await pool.query(
      'SELECT id, message_text, mode, secret_session_id FROM public.chat_messages WHERE conversation_id = $1 ORDER BY created_at ASC',
      [convId]
    );
    const hasNormal = adminCheck.rows.some(m => m.mode === 'normal');
    const hasSecret = adminCheck.rows.some(m => m.mode === 'secret');
    assert(hasNormal && hasSecret, 'Database preserves both normal and secret messages for Admin inspection');

    // 5. Extreme Edge Cases & Resilience
    console.log('\n[Test Group 5: Edge Cases & Validation]');
    // Reject empty message
    const emptyRes = await makeRequest('POST', '/api/chat/messages/send', {
      sender_id: user1.id,
      receiver_id: user2.id,
      message_text: '   '
    });
    assert(emptyRes.status === 400, 'Rejects blank message text without media');

    // Unicode / Emojis & international chars
    const emojiRes = await makeRequest('POST', '/api/chat/messages/send', {
      sender_id: user2.id,
      receiver_id: user1.id,
      message_text: '🔥 Test emojis: 🚀🎉🇮🇳❤️ বাংলা / English / 日本語',
      mode: 'normal'
    });
    assert(emojiRes.status === 200 && emojiRes.body.message.message_text.includes('🚀🎉'), 'Unicode & Emojis preserved accurately');

    // Large text payload (2500 chars)
    const longText = 'A'.repeat(2500);
    const longRes = await makeRequest('POST', '/api/chat/messages/send', {
      sender_id: user1.id,
      receiver_id: user2.id,
      message_text: longText,
      mode: 'normal'
    });
    assert(longRes.status === 200 && longRes.body.message.message_text.length === 2500, 'Long message (2500 chars) processed successfully');

    // Future clock skew resilience on poll
    const futureTime = new Date(Date.now() + 60000).toISOString();
    const skewPoll = await makeRequest('GET', `/api/chat/poll?userId=${user1.id}&since=${futureTime}`);
    assert(skewPoll.status === 200 && skewPoll.body.success, 'Poll succeeds with client clock skew protection');

    // Unread count check on conversations
    const convsRes = await makeRequest('GET', `/api/chat/conversations?userId=${user1.id}`);
    assert(convsRes.status === 200 && convsRes.body.conversations.length > 0, 'Get conversations list returns active chats with unread count');

    console.log('\n====================================================');
    console.log(`  TEST RESULTS: ${passed} PASSED, ${failed} FAILED`);
    console.log('====================================================\n');
    process.exit(failed > 0 ? 1 : 0);

  } catch (err) {
    console.error('Fatal error running test suite:', err);
    process.exit(1);
  }
}

runChatTestSuite();
