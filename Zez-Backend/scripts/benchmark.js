async function benchmark() {
  const base = 'http://localhost:3000/api/admin';

  const t0 = Date.now();
  const loginRes = await fetch(`${base}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: process.env.ADMIN_PASSWORD || 'your_admin_password' })
  });
  const loginTime = Date.now() - t0;
  const { token } = await loginRes.json();
  console.log(`POST /login: ${loginTime}ms`);

  const headers = { Authorization: `Bearer ${token}` };

  async function measure(name, url) {
    const start = Date.now();
    const res = await fetch(base + url, { headers });
    const data = await res.json();
    const elapsed = Date.now() - start;
    console.log(`${name} (${url}): ${elapsed}ms (status: ${res.status}, items: ${data.calls?.length ?? data.devices?.length ?? data.conversations?.length ?? 'N/A'})`);
    return elapsed;
  }

  await measure('Stats', '/stats');
  await measure('Devices', '/devices');
  await measure('Global Calls (limit 50)', '/calls?limit=50');
  await measure('Global Calls (limit 300)', '/calls?limit=300');
  await measure('Chats Conversations', '/chats/conversations');
  await measure('App Releases', '/../app/releases');

  process.exit(0);
}

benchmark().catch(err => {
  console.error(err);
  process.exit(1);
});
