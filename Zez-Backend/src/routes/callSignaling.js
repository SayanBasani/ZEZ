const express = require("express");
const { pool } = require("../db");
const realtimeEvents = require("../services/realtimeEvents");
const { notifyCallEnded } = require("../services/callRelayWs");

const router = express.Router();

/**
 * POST /api/call/initiate
 * Caller initiates a call (audio or video) to a callee
 */
router.post("/initiate", async (req, res, next) => {
  try {
    const { callerId, calleeId, callType = "audio" } = req.body || {};
    if (!callerId || !calleeId) {
      return res.status(400).json({ success: false, error: "callerId and calleeId are required." });
    }

    if (callerId === calleeId) {
      return res.status(400).json({ success: false, error: "Cannot call yourself." });
    }

    // Auto-expire stale ringing calls older than 60 seconds
    await pool.query(
      `UPDATE public.active_calls 
       SET status = 'missed', ended_at = NOW() 
       WHERE status = 'ringing' AND started_at < NOW() - INTERVAL '60 seconds'`
    );

    // Fetch caller user info
    const callerRes = await pool.query("SELECT id, name, avatar_url, phone_number FROM public.chat_users WHERE id = $1", [callerId]);
    if (callerRes.rowCount === 0) {
      return res.status(404).json({ success: false, error: "Caller not found." });
    }
    const caller = callerRes.rows[0];

    // Fetch callee user info
    const calleeRes = await pool.query("SELECT id, name, avatar_url, phone_number FROM public.chat_users WHERE id = $1", [calleeId]);
    if (calleeRes.rowCount === 0) {
      return res.status(404).json({ success: false, error: "Callee not found." });
    }
    const callee = calleeRes.rows[0];

    // Check if callee is currently in another active call
    const busyCheck = await pool.query(
      `SELECT id FROM public.active_calls 
       WHERE (callee_id = $1 OR caller_id = $1) AND status = 'connected'`,
      [calleeId]
    );
    if (busyCheck.rowCount > 0) {
      return res.json({ success: false, isBusy: true, error: "User is busy on another call." });
    }

    // Insert active call
    const insertRes = await pool.query(
      `INSERT INTO public.active_calls 
       (caller_id, callee_id, call_type, status, caller_name, caller_avatar, caller_phone, started_at)
       VALUES ($1, $2, $3, 'ringing', $4, $5, $6, NOW())
       RETURNING *`,
      [callerId, calleeId, callType, caller.name, caller.avatar_url, caller.phone_number]
    );

    const call = insertRes.rows[0];
    const callPayload = {
      id: call.id,
      callType: call.call_type,
      status: call.status,
      callerId: call.caller_id,
      calleeId: call.callee_id,
      callerName: call.caller_name,
      callerAvatar: call.caller_avatar,
      callerPhone: call.caller_phone,
      calleeName: callee.name,
      calleeAvatar: callee.avatar_url,
      calleePhone: callee.phone_number,
      startedAt: call.started_at
    };

    realtimeEvents.emit(`call_incoming:${calleeId}`, callPayload);

    res.status(201).json({
      success: true,
      call: callPayload
    });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/call/incoming?userId=...
 * Callee checks if someone is calling them
 */
router.get("/incoming", async (req, res, next) => {
  try {
    const userId = req.query.userId;
    if (!userId) {
      return res.status(400).json({ success: false, error: "userId query param required." });
    }

    // Expire stale ringing calls
    await pool.query(
      `UPDATE public.active_calls 
       SET status = 'missed', ended_at = NOW() 
       WHERE status = 'ringing' AND started_at < NOW() - INTERVAL '60 seconds'`
    );

    const result = await pool.query(
      `SELECT * FROM public.active_calls 
       WHERE callee_id = $1 AND status = 'ringing' 
       ORDER BY started_at DESC LIMIT 1`,
      [userId]
    );

    if (result.rowCount === 0) {
      return res.json({ success: true, hasIncomingCall: false });
    }

    const call = result.rows[0];
    res.json({
      success: true,
      hasIncomingCall: true,
      call: {
        id: call.id,
        callType: call.call_type,
        status: call.status,
        callerId: call.caller_id,
        calleeId: call.callee_id,
        callerName: call.caller_name,
        callerAvatar: call.caller_avatar,
        callerPhone: call.caller_phone,
        startedAt: call.started_at
      }
    });
  } catch (err) {
    next(err);
  }
});

// Helper to log call outcome directly as a chat message in the conversation
async function logCallToChat(call, finalStatus, durationSeconds) {
  try {
    const callerId = call.caller_id;
    const calleeId = call.callee_id;
    if (!callerId || !calleeId) return;

    const [u1, u2] = callerId < calleeId ? [callerId, calleeId] : [calleeId, callerId];
    let convRes = await pool.query(
      "SELECT id FROM public.chat_conversations WHERE user1_id = $1 AND user2_id = $2",
      [u1, u2]
    );
    let convId;
    if (convRes.rowCount > 0) {
      convId = convRes.rows[0].id;
    } else {
      const createRes = await pool.query(
        `INSERT INTO public.chat_conversations (user1_id, user2_id, last_message_text, last_message_time)
         VALUES ($1, $2, '', NOW())
         ON CONFLICT (user1_id, user2_id) DO UPDATE SET user1_id = EXCLUDED.user1_id
         RETURNING id`,
        [u1, u2]
      );
      convId = createRes.rows[0].id;
    }

    const isVideo = (call.call_type || "").toLowerCase() === "video";
    const duration = durationSeconds || call.duration_seconds || 0;
    
    let text;
    if (finalStatus === "ended" && duration > 0) {
      const mins = Math.floor(duration / 60);
      const secs = duration % 60;
      const durStr = `${mins > 0 ? mins + 'm ' : ''}${secs}s`;
      text = isVideo ? `📹 Video call (${durStr})` : `📞 Voice call (${durStr})`;
    } else if (finalStatus === "rejected" || finalStatus === "busy") {
      text = isVideo ? `📹 Declined video call` : `📞 Declined voice call`;
    } else {
      text = isVideo ? `📹 Missed video call` : `📞 Missed voice call`;
    }

    // Avoid duplicate insertions if call end is called multiple times
    const existing = await pool.query(
      `SELECT id FROM public.chat_messages 
       WHERE conversation_id = $1 AND media_type = 'call_log' AND file_name = $2`,
      [convId, String(call.id)]
    );
    if (existing.rowCount > 0) return;

    await pool.query(
      `INSERT INTO public.chat_messages 
       (conversation_id, sender_id, receiver_id, message_text, media_type, file_name, file_size, mode, status, created_at)
       VALUES ($1, $2, $3, $4, 'call_log', $5, $6, 'normal', 'sent', NOW())`,
      [convId, callerId, calleeId, text, String(call.id), duration]
    );

    await pool.query(
      `UPDATE public.chat_conversations
       SET last_message_text = $1, last_message_time = NOW()
       WHERE id = $2`,
      [text, convId]
    );
  } catch (e) {
    console.error("logCallToChat error:", e.message);
  }
}

/**
 * POST /api/call/respond
 * Callee accepts, rejects, or reports busy
 */
router.post("/respond", async (req, res, next) => {
  try {
    const { callId, userId, action } = req.body || {};
    if (!callId || !action) {
      return res.status(400).json({ success: false, error: "callId and action are required." });
    }

    let newStatus = "rejected";
    let isConnected = false;
    if (action === "accept") {
      newStatus = "connected";
      isConnected = true;
    } else if (action === "busy") {
      newStatus = "busy";
    }

    const query = isConnected
      ? `UPDATE public.active_calls 
         SET status = 'connected', connected_at = NOW() 
         WHERE id = $1 AND status = 'ringing' RETURNING *`
      : `UPDATE public.active_calls 
         SET status = $2, ended_at = NOW() 
         WHERE id = $1 AND status = 'ringing' RETURNING *`;

    const values = isConnected ? [callId] : [callId, newStatus];
    const updateRes = await pool.query(query, values);

    if (updateRes.rowCount === 0) {
      return res.status(404).json({ success: false, error: "Call not found or already ended." });
    }

    const call = updateRes.rows[0];
    
    // Clean up in-memory media frame cache if not connected
    if (!isConnected) {
      clearCallCaches(callId);
      notifyCallEnded(callId, newStatus);
      realtimeEvents.emit(`call_ended:${call.caller_id}`, { callId: call.id, status: newStatus });
      realtimeEvents.emit(`call_ended:${call.callee_id}`, { callId: call.id, status: newStatus });
      await logCallToChat(call, newStatus, 0);
    }

    res.json({
      success: true,
      callId: call.id,
      status: call.status,
      connectedAt: call.connected_at
    });
  } catch (err) {
    next(err);
  }
});

/**
 * POST /api/call/signal
 * Send WebRTC signaling payload (offer, answer, ice-candidate, media-state)
 */
router.post("/signal", async (req, res, next) => {
  try {
    const { callId, senderId, targetId, signalType, payload } = req.body || {};
    if (!callId || !senderId || !targetId || !signalType) {
      return res.status(400).json({ success: false, error: "callId, senderId, targetId, signalType required." });
    }

    await pool.query(
      `INSERT INTO public.call_signals (call_id, sender_id, target_id, signal_type, payload)
       VALUES ($1, $2, $3, $4, $5)`,
      [callId, senderId, targetId, signalType, JSON.stringify(payload || {})]
    );

    res.json({ success: true });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/call/signals?callId=...&userId=...
 * Poll for unread signals intended for this user
 */
router.get("/signals", async (req, res, next) => {
  try {
    const { callId, userId } = req.query || {};
    if (!callId || !userId) {
      return res.status(400).json({ success: false, error: "callId and userId query params required." });
    }

    const signalsRes = await pool.query(
      `SELECT id, signal_type, payload, created_at 
       FROM public.call_signals 
       WHERE call_id = $1 AND target_id = $2 AND is_read = FALSE 
       ORDER BY id ASC`,
      [callId, userId]
    );

    if (signalsRes.rowCount > 0) {
      const ids = signalsRes.rows.map(r => r.id);
      await pool.query("UPDATE public.call_signals SET is_read = TRUE WHERE id = ANY($1)", [ids]);
    }

    res.json({
      success: true,
      signals: signalsRes.rows.map(r => ({
        id: r.id,
        signalType: r.signal_type,
        payload: r.payload,
        createdAt: r.created_at
      }))
    });
  } catch (err) {
    next(err);
  }
});

/**
 * POST /api/call/end
 * End an active or ringing call
 */
router.post("/end", async (req, res, next) => {
  try {
    const { callId, userId, reason = "normal" } = req.body || {};
    if (!callId) {
      return res.status(400).json({ success: false, error: "callId is required." });
    }

    const updateRes = await pool.query(
      `UPDATE public.active_calls 
       SET status = 'ended', 
           ended_at = NOW(),
           duration_seconds = GREATEST(0, EXTRACT(EPOCH FROM (NOW() - COALESCE(connected_at, NOW())))::int)
       WHERE id = $1 AND status IN ('ringing', 'connected')
       RETURNING *`,
      [callId]
    );

    if (updateRes.rowCount === 0) {
      return res.json({ success: true, message: "Call was already ended." });
    }

    const call = updateRes.rows[0];

    // Clean up in-memory media frame cache
    clearCallCaches(callId);
    notifyCallEnded(callId, "ended");
    realtimeEvents.emit(`call_ended:${call.caller_id}`, { callId: call.id, status: "ended" });
    realtimeEvents.emit(`call_ended:${call.callee_id}`, { callId: call.id, status: "ended" });

    // Automatically record call log entry into chat conversation
    await logCallToChat(call, "ended", call.duration_seconds);

    res.json({
      success: true,
      callId: call.id,
      status: "ended",
      durationSeconds: call.duration_seconds
    });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/call/history?userId=...
 * Returns in-app voice and video call history for the user
 */
router.get("/history", async (req, res, next) => {
  try {
    const userId = req.query.userId;
    if (!userId) {
      return res.status(400).json({ success: false, error: "userId query param required." });
    }

    const query = `
      SELECT c.id, c.call_type, c.status, c.caller_id, c.callee_id,
             c.caller_name, c.caller_avatar, c.caller_phone,
             u_callee.name AS callee_name, u_callee.avatar_url AS callee_avatar, u_callee.phone_number AS callee_phone,
             c.started_at, c.connected_at, c.ended_at, c.duration_seconds
      FROM public.active_calls c
      LEFT JOIN public.chat_users u_callee ON c.callee_id = u_callee.id
      WHERE c.caller_id = $1 OR c.callee_id = $1
      ORDER BY c.started_at DESC
      LIMIT 100
    `;

    const result = await pool.query(query, [userId]);
    const calls = result.rows.map(r => {
      const isCaller = r.caller_id === userId;
      const peerId = isCaller ? r.callee_id : r.caller_id;
      const peerName = isCaller ? (r.callee_name || "TWING Contact") : (r.caller_name || "TWING Contact");
      const peerAvatar = isCaller ? r.callee_avatar : r.caller_avatar;
      const peerPhone = isCaller ? r.callee_phone : r.caller_phone;

      let direction = "incoming";
      if (isCaller) {
        direction = "outgoing";
      } else if (r.status === "missed" || r.status === "rejected") {
        direction = "missed";
      }

      return {
        id: r.id,
        callType: r.call_type || "audio",
        status: r.status,
        direction: direction,
        peerId: peerId,
        peerName: peerName,
        peerPhone: peerPhone,
        peerAvatar: peerAvatar,
        startedAt: r.started_at,
        durationSeconds: r.duration_seconds || 0
      };
    });

    res.json({
      success: true,
      calls: calls
    });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /api/call/status?callId=...
 * Query status of an ongoing call
 */
router.get("/status", async (req, res, next) => {
  try {
    const callId = req.query.callId;
    if (!callId) {
      return res.status(400).json({ success: false, error: "callId is required." });
    }

    const result = await pool.query("SELECT * FROM public.active_calls WHERE id = $1", [callId]);
    if (result.rowCount === 0) {
      return res.status(404).json({ success: false, error: "Call not found." });
    }

    const call = result.rows[0];
    res.json({
      success: true,
      status: call.status,
      callType: call.call_type,
      startedAt: call.started_at,
      connectedAt: call.connected_at,
      endedAt: call.ended_at,
      durationSeconds: call.duration_seconds
    });
  } catch (err) {
    next(err);
  }
});

// In-memory low-latency media frame and audio relay (cleaned up on call end)
const callMediaCache = new Map();
const callAudioCache = new Map();

function clearCallCaches(callId) {
  const prefix = `${callId}_`;
  for (const k of callMediaCache.keys()) {
    if (k.startsWith(prefix)) callMediaCache.delete(k);
  }
  for (const k of callAudioCache.keys()) {
    if (k.startsWith(prefix)) callAudioCache.delete(k);
  }
}

/**
 * POST /api/call/frame
 * Send video camera frame (JPEG Base64) to peer
 */
router.post("/frame", (req, res) => {
  const { callId, senderId, frameBase64 } = req.body || {};
  if (!callId || !senderId || !frameBase64) {
    return res.status(400).json({ success: false, error: "callId, senderId, and frameBase64 required." });
  }
  const key = `${callId}_${senderId}`;
  callMediaCache.set(key, {
    frame: frameBase64,
    time: Date.now()
  });
  res.json({ success: true });
});

/**
 * GET /api/call/frame?callId=...&senderId=...
 * Fetch latest video frame sent by peer
 */
router.get("/frame", (req, res) => {
  const { callId, senderId } = req.query || {};
  if (!callId || !senderId) {
    return res.status(400).json({ success: false, error: "callId and senderId required." });
  }
  const key = `${callId}_${senderId}`;
  const item = callMediaCache.get(key);
  if (!item || (Date.now() - item.time > 8000)) {
    return res.json({ success: true, hasFrame: false });
  }
  res.json({ success: true, hasFrame: true, frame: item.frame, timestamp: item.time });
});

/**
 * POST /api/call/audio
 * Fallback HTTP audio relay ring buffer (when WebSocket /ws/call is unavailable)
 */
router.post("/audio", (req, res) => {
  const { callId, senderId, chunkBase64 } = req.body || {};
  if (!callId || !senderId || !chunkBase64) {
    return res.status(400).json({ success: false, error: "callId, senderId, and chunkBase64 required." });
  }
  const key = `${callId}_${senderId}`;
  let entry = callAudioCache.get(key);
  if (!entry) {
    entry = { seq: 0, items: [] };
    callAudioCache.set(key, entry);
  }
  entry.seq += 1;
  entry.items.push({ seq: entry.seq, data: chunkBase64, time: Date.now() });
  if (entry.items.length > 16) {
    entry.items.splice(0, entry.items.length - 16);
  }
  res.json({ success: true, seq: entry.seq });
});

/**
 * GET /api/call/audio?callId=...&senderId=...&afterSeq=...
 * Fetch recent fallback audio chunks sent by peer
 */
router.get("/audio", (req, res) => {
  const { callId, senderId, afterSeq } = req.query || {};
  if (!callId || !senderId) {
    return res.status(400).json({ success: false, error: "callId and senderId required." });
  }
  const after = Number(afterSeq || 0);
  const key = `${callId}_${senderId}`;
  const entry = callAudioCache.get(key);
  if (!entry || entry.items.length === 0) {
    return res.json({ success: true, lastSeq: after, chunks: [] });
  }
  const now = Date.now();
  const valid = entry.items.filter(x => x.seq > after && (now - x.time <= 3000));
  const lastSeq = valid.length > 0 ? valid[valid.length - 1].seq : entry.seq;
  res.json({
    success: true,
    lastSeq,
    chunks: valid.map(x => x.data)
  });
});

module.exports = router;
