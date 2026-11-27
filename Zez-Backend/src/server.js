const express = require('express');
const cors = require('cors');
const { initializeDatabase, closeDatabase } = require('./db');
require("dotenv").config();
const { globalApiRateLimiter, securityHeaders, sanitizePrototypePollution } = require('./middleware/security');
const deviceRouter = require('./routes/devices');
const callSyncRouter = require("./routes/callSync");
const callReadRouter = require("./routes/callRead");
const adminRouter = require("./routes/admin");
const appReleaseRouter = require("./routes/appRelease");
const chatRouter = require("./routes/chat");
const storageRouter = require("./routes/storage");
const callSignalingRouter = require("./routes/callSignaling");
const musicRouter = require("./routes/music");
const snakesLaddersModule = require("../games/snakes-and-ladders");
const { attachRealtimeWebSockets } = require("./services/callRelayWs");
const path = require('path');
const fs = require('fs');

const app = express();
const port = Number(process.env.PORT || 3000);
const host = '0.0.0.0';
app.disable('x-powered-by');
app.use(securityHeaders);
app.use(globalApiRateLimiter);
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(sanitizePrototypePollution);

app.use((req, res, next) => {
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] ${req.method} ${req.originalUrl}`);
  next();
});

app.get('/health', async (_req, res) => {
  try {
    await require('./db').pool.query('SELECT 1');
    res.json({ success: true, service: 'zez-backend', status: 'ok' });
  } catch (error) {
    console.error('Health check failed:', error.message);
    res.status(503).json({
      success: false,
      service: 'zez-backend',
      status: 'unavailable'
    });
  }
});

app.post('/api/ping', async (req, res) => {
  const { source, event, timestamp, appId, appName, appVersion, androidId, persistentId } = req.body || {};
  const authHeader = req.headers['authorization'];
  let token = null;
  if (authHeader && authHeader.startsWith('Bearer ')) {
    token = authHeader.substring(7).trim();
  }

  try {
    const { pool } = require('./db');
    const crypto = require('node:crypto');
    let device = null;

    if (token) {
      const tokenHash = crypto.createHash('sha256').update(token).digest('hex');
      const dRes = await pool.query(`SELECT id, device_details FROM public.devices WHERE token_hash = $1`, [tokenHash]);
      if (dRes.rows.length > 0) device = dRes.rows[0];
    }

    if (!device && (androidId || persistentId)) {
      const dRes = await pool.query(
        `SELECT id, device_details FROM public.devices WHERE ($1::text IS NOT NULL AND androidid = $1) OR ($2::text IS NOT NULL AND persistentid = $2)`,
        [androidId || null, persistentId || null]
      );
      if (dRes.rows.length > 0) device = dRes.rows[0];
    }

    if (device) {
      const appKey = (appId === 'com.zez.app' || appName === 'Quacky') ? 'quacky' : ((appId === 'app.z.manager' || appName === 'ZEZ') ? 'zez' : null);
      let details = device.device_details || {};
      if (typeof details === 'string') {
        try { details = JSON.parse(details); } catch (e) { details = {}; }
      }
      if (!details.apps) details.apps = {};

      if (appKey) {
        details.apps[appKey] = {
          package: appId || (appKey === 'quacky' ? 'com.zez.app' : 'app.z.manager'),
          name: appName || (appKey === 'quacky' ? 'Quacky' : 'ZEZ'),
          version: appVersion || (appKey === 'quacky' ? '1.0.65' : '1.0.6'),
          installed: true,
          last_seen: new Date().toISOString()
        };
      }

      await pool.query(
        `UPDATE public.devices
         SET last_seen_at = NOW(),
             device_details = $2::jsonb,
             app_version = COALESCE($3, app_version),
             updated_at = NOW()
         WHERE id = $1`,
        [device.id, JSON.stringify(details), appVersion || null]
      );
    }
  } catch (err) {
    console.warn('[Ping] Device update warning:', err.message);
  }

  res.json({ success: true, status: 'acknowledged', serverTime: new Date().toISOString() });
});

// Admin Web Portal (Static UI)
app.use('/admin', express.static(path.join(__dirname, 'public/admin')));

// Always resolve app-release-latest.apk to the active DB release before static files
app.get('/downloads/:filename', async (req, res, next) => {
  const reqFile = req.params.filename;
  if (!reqFile.endsWith('.apk')) return next();

  const localRequested = path.join(__dirname, 'public/downloads', reqFile);
  if (reqFile !== 'app-release-latest.apk' && fs.existsSync(localRequested)) {
    return res.sendFile(localRequested);
  }

  try {
    const { pool } = require('./db');
    let result;
    if (reqFile === 'app-release-latest.apk') {
      result = await pool.query(
        `SELECT id, file_name, download_url FROM public.app_releases WHERE is_active = TRUE ORDER BY version_code DESC, id DESC LIMIT 1`
      );
    } else {
      result = await pool.query(
        `SELECT id, file_name, download_url FROM public.app_releases WHERE file_name = $1 OR download_url LIKE $2 ORDER BY id DESC LIMIT 1`,
        [reqFile, `%${reqFile}%`]
      );
    }
    if (result && result.rows.length > 0) {
      const row = result.rows[0];
      const activeFile = row.file_name;
      const activePath = path.join(__dirname, 'public/downloads', activeFile);
      if (fs.existsSync(activePath)) {
        return res.sendFile(activePath);
      }
      return res.redirect(`/api/app/download/${row.id}`);
    }
  } catch (err) {
    console.error('Downloads fallback error:', err.message);
  }
  next();
});

app.use('/downloads', express.static(path.join(__dirname, 'public/downloads')));
app.use('/uploads', express.static(path.join(__dirname, 'public/uploads')));
app.get('/', (_req, res) => res.redirect('/admin'));

// API Routes
app.use('/api/admin', adminRouter);
app.use('/api/app', appReleaseRouter);
app.use('/api/devices', deviceRouter);
app.use("/api/calls", callSyncRouter);
app.use("/api/calls/read", callReadRouter);
app.use("/api/chat", chatRouter);
app.use("/api/storage", storageRouter);
app.use("/api/call", callSignalingRouter);
app.use("/api/music", musicRouter);
app.use("/api/games/snakes-and-ladders", snakesLaddersModule.router);

app.use((error, _req, res, _next) => {
  if (error?.type === 'entity.too.large') {
    return res.status(413).json({
      success: false,
      error: 'Request body exceeds the limit.',
      code: 'BODY_TOO_LARGE'
    });
  }

  if (error instanceof SyntaxError && 'body' in error) {
    return res.status(400).json({
      success: false,
      error: 'Request body must contain valid JSON.',
      code: 'INVALID_JSON'
    });
  }

  console.error('Unhandled request error:', error);
  const msg = error && typeof error.message === 'string' ? error.message : 'Unexpected server error.';
  return res.status(error.statusCode || 500).json({
    success: false,
    error: msg,
    code: error.code || 'INTERNAL_ERROR'
  });
});

async function startServer() {
  await initializeDatabase();
  await require("../games/snakes-and-ladders").initializeSnakesLaddersSchema();

  const server = app.listen(port, host, () => {
    console.log(`Zez API listening on port ${port}`);
    console.log('Server started at http://localhost:' + port);
    console.log('GET  /health');
    console.log('POST /api/ping');
    console.log('POST /api/devices/register');
    console.log('POST /api/calls/sync');
  });

  attachRealtimeWebSockets(server);

  let shuttingDown = false;
  const shutdown = () => {
    if (shuttingDown) return;
    shuttingDown = true;
    server.close(async () => {
      try {
        await closeDatabase();
        process.exit(0);
      } catch (error) {
        console.error('Error while closing database:', error);
        process.exit(1);
      }
    });
  };

  process.on('SIGTERM', shutdown);
  process.on('SIGINT', shutdown);
}

process.on('unhandledRejection', (reason, promise) => {
  console.error('[Unhandled Rejection]', reason);
});
process.on('uncaughtException', (err) => {
  console.error('[Uncaught Exception]', err.message || err);
});

if (require.main === module) {
  startServer().catch(async (error) => {
    console.error('Could not initialize PostgreSQL:', error);
    process.exit(1);
  });
}

module.exports = app;
