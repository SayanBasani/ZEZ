const http = require('http');

function post(path, body) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(body);
    const req = http.request({
      hostname: '127.0.0.1',
      port: 3000,
      path,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(data)
      }
    }, res => {
      let buf = '';
      res.on('data', c => buf += c);
      res.on('end', () => resolve({ status: res.statusCode, data: JSON.parse(buf) }));
    });
    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

function get(path) {
  return new Promise((resolve, reject) => {
    const req = http.request({
      hostname: '127.0.0.1',
      port: 3000,
      path,
      method: 'GET'
    }, res => {
      let buf = '';
      res.on('data', c => buf += c);
      res.on('end', () => resolve({ status: res.statusCode, data: JSON.parse(buf) }));
    });
    req.on('error', reject);
    req.end();
  });
}

async function run() {
  console.log("--- Testing Chat Backend ---");

  // 1. Register User A (Alice)
  const regA = await post('/api/chat/register', {
    phone_number: '+15551112222',
    name: 'Alice Developer',
    status_message: 'Working on Zez Chat!'
  });
  console.log("1. Register User A:", regA.status, regA.data.success ? "SUCCESS" : "FAIL");
  const userA = regA.data.user;

  // 2. Register User B (Bob)
  const regB = await post('/api/chat/register', {
    phone_number: '+15553334444',
    name: 'Bob Tester',
    status_message: 'Testing realtime sync'
  });
  console.log("2. Register User B:", regB.status, regB.data.success ? "SUCCESS" : "FAIL");
  const userB = regB.data.user;

  // 3. Sync Contacts from Alice's phonebook
  const syncContacts = await post('/api/chat/sync-contacts', {
    phone_numbers: ['+1 555-333-4444', '5551112222', '+19998887777'],
    current_user_id: userA.id
  });
  console.log("3. Sync Contacts:", syncContacts.status, "Registered found:", syncContacts.data.registered.length, "Not registered:", syncContacts.data.not_registered.length);

  // 4. Send Message from Alice to Bob
  const sendMsg = await post('/api/chat/messages/send', {
    sender_id: userA.id,
    receiver_id: userB.id,
    message_text: 'Hello Bob, how is WhatsApp-style chat working?'
  });
  console.log("4. Send Message from A to B:", sendMsg.status, sendMsg.data.success ? "SUCCESS" : "FAIL", "msg id:", sendMsg.data.message.id);

  // 5. Send Reply from Bob to Alice
  const replyMsg = await post('/api/chat/messages/send', {
    sender_id: userB.id,
    receiver_id: userA.id,
    message_text: 'It is working completely smoothly!'
  });
  console.log("5. Reply Message from B to A:", replyMsg.status, replyMsg.data.success ? "SUCCESS" : "FAIL");

  // 6. Get Alice's conversations
  const convsA = await get(`/api/chat/conversations?userId=${userA.id}`);
  console.log("6. Alice Conversations:", convsA.status, "Count:", convsA.data.conversations.length, "Last msg:", convsA.data.conversations[0]?.last_message_text);

  // 7. Get messages in conversation
  const convId = convsA.data.conversations[0]?.conversation_id;
  const msgs = await get(`/api/chat/messages?conversationId=${convId}&userId=${userA.id}`);
  console.log("7. Conversation Messages:", msgs.status, "Count:", msgs.data.messages.length);

  // 8. Poll for Bob
  const pollBob = await get(`/api/chat/poll?userId=${userB.id}&since=${new Date(Date.now() - 60000).toISOString()}`);
  console.log("8. Poll for Bob:", pollBob.status, "Incoming:", pollBob.data.incoming.length, "Status updates:", pollBob.data.status_updates.length);

  console.log("--- Chat Backend Test Suite COMPLETE ---");
}

run().catch(console.error);
