const express = require("express");
const path = require("path");
const fs = require("fs");
const multer = require("multer");
const { pool } = require("../db");

const router = express.Router();

// Ensure local upload storage directories exist
const mediaUploadDir = path.join(__dirname, "../public/uploads/chat_media");
const avatarUploadDir = path.join(__dirname, "../public/uploads/avatars");
if (!fs.existsSync(mediaUploadDir)) fs.mkdirSync(mediaUploadDir, { recursive: true });
if (!fs.existsSync(avatarUploadDir)) fs.mkdirSync(avatarUploadDir, { recursive: true });

const mediaStorage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, mediaUploadDir),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    const cleanExt = ext || ".bin";
    const unique = `media-${Date.now()}-${Math.random().toString(36).substring(2, 8)}${cleanExt}`;
    cb(null, unique);
  }
});

const avatarStorage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, avatarUploadDir),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    const cleanExt = ext || ".jpg";
    const unique = `avatar-${Date.now()}-${Math.random().toString(36).substring(2, 8)}${cleanExt}`;
    cb(null, unique);
  }
});

const uploadMedia = multer({
  storage: mediaStorage,
  limits: { fileSize: 50 * 1024 * 1024 } // 50MB max upload
});

const uploadAvatar = multer({
  storage: avatarStorage,
  limits: { fileSize: 10 * 1024 * 1024 } // 10MB max avatar
});

function normalizePhone(raw) {
  if (!raw) return "";
  let digits = raw.replace(/[^\d+]/g, "");
  // If starts with 00, replace with +
  if (digits.startsWith("00")) {
    digits = "+" + digits.substring(2);
  }
  return digits;
}

// 1. Register or update chat user
// POST /api/chat/register
router.post("/register", async (req, res) => {
  try {
    const { phone_number, name, avatar_url, status_message } = req.body;
    if (!phone_number || !name) {
      return res.status(400).json({ error: "phone_number and name are required." });
    }

    const cleanPhone = normalizePhone(phone_number);
    const displayName = String(name).trim();
    const avatar = avatar_url || null;
    const status = status_message || "Hey there! I am using Zez.";

    const query = `
      INSERT INTO public.chat_users (phone_number, name, avatar_url, status_message, last_seen)
      VALUES ($1, $2, $3, $4, NOW())
      ON CONFLICT (phone_number) 
      DO UPDATE SET 
        name = EXCLUDED.name,
        avatar_url = COALESCE(EXCLUDED.avatar_url, chat_users.avatar_url),
        status_message = COALESCE(EXCLUDED.status_message, chat_users.status_message),
        last_seen = NOW()
      RETURNING id, phone_number, name, avatar_url, status_message, last_seen, created_at;
    `;

    const result = await pool.query(query, [cleanPhone, displayName, avatar, status]);
    return res.json({ success: true, user: result.rows[0] });
  } catch (err) {
    console.error("Error in /api/chat/register:", err);
    return res.status(500).json({ error: "Internal server error registering user." });
  }
});

// 2. Get user profile
// GET /api/chat/profile/:idOrPhone
router.get("/profile/:idOrPhone", async (req, res) => {
  try {
    const { idOrPhone } = req.params;
    const isUuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(idOrPhone);

    const query = isUuid
      ? "SELECT id, phone_number, name, avatar_url, status_message, last_seen FROM public.chat_users WHERE id = $1"
      : "SELECT id, phone_number, name, avatar_url, status_message, last_seen FROM public.chat_users WHERE phone_number = $1";

    const result = await pool.query(query, [isUuid ? idOrPhone : normalizePhone(idOrPhone)]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: "User not found." });
    }

    return res.json({ success: true, user: result.rows[0] });
  } catch (err) {
    console.error("Error in /api/chat/profile:", err);
    return res.status(500).json({ error: "Internal server error fetching profile." });
  }
});

// 3. Update user profile
// POST /api/chat/profile
router.post("/profile", async (req, res) => {
  try {
    const { id, name, avatar_url, status_message } = req.body;
    if (!id) {
      return res.status(400).json({ error: "User id is required." });
    }

    const updates = [];
    const values = [id];
    let idx = 2;

    if (name !== undefined) {
      updates.push(`name = $${idx++}`);
      values.push(name.trim());
    }
    if (avatar_url !== undefined) {
      updates.push(`avatar_url = $${idx++}`);
      values.push(avatar_url);
    }
    if (status_message !== undefined) {
      updates.push(`status_message = $${idx++}`);
      values.push(status_message.trim());
    }

    updates.push(`last_seen = NOW()`);

    if (updates.length === 1) {
      return res.status(400).json({ error: "No fields to update." });
    }

    const query = `
      UPDATE public.chat_users 
      SET ${updates.join(", ")}
      WHERE id = $1
      RETURNING id, phone_number, name, avatar_url, status_message, last_seen;
    `;

    const result = await pool.query(query, values);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: "User not found." });
    }

    return res.json({ success: true, user: result.rows[0] });
  } catch (err) {
    console.error("Error updating profile:", err);
    return res.status(500).json({ error: "Internal server error updating profile." });
  }
});

// 3b. Upload Chat Media (Images, Videos, Files, Audio) - Zero 3rd party storage
// POST /api/chat/upload
router.post("/upload", uploadMedia.single("file"), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: "No file uploaded." });
    }

    const mime = req.file.mimetype || "";
    let mediaType = "file";
    if (mime.startsWith("image/")) {
      mediaType = "image";
    } else if (mime.startsWith("video/")) {
      mediaType = "video";
    } else if (mime.startsWith("audio/")) {
      mediaType = "audio";
    }

    const relativeUrl = `/uploads/chat_media/${req.file.filename}`;
    return res.json({
      success: true,
      url: relativeUrl,
      fileName: req.file.originalname,
      fileSize: req.file.size,
      mediaType
    });
  } catch (err) {
    console.error("Error in /api/chat/upload:", err);
    return res.status(500).json({ error: "Failed to upload chat media." });
  }
});

// 3c. Upload Chat User Profile Picture
// POST /api/chat/upload-avatar
router.post("/upload-avatar", uploadAvatar.single("avatar"), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: "No avatar image provided." });
    }

    const relativeUrl = `/uploads/avatars/${req.file.filename}`;
    const { userId } = req.body;

    if (userId) {
      await pool.query(
        "UPDATE public.chat_users SET avatar_url = $1, last_seen = NOW() WHERE id = $2",
        [relativeUrl, userId]
      );
    }

    return res.json({
      success: true,
      avatar_url: relativeUrl
    });
  } catch (err) {
    console.error("Error in /api/chat/upload-avatar:", err);
    return res.status(500).json({ error: "Failed to upload avatar." });
  }
});

// 3d. Direct Contact lookup or placeholder creation
// POST /api/chat/direct-contact
router.post("/direct-contact", async (req, res) => {
  try {
    const { phone_number, name } = req.body;
    if (!phone_number) {
      return res.status(400).json({ error: "phone_number is required." });
    }
    const cleanPhone = normalizePhone(phone_number);
    const displayName = (name && String(name).trim()) || cleanPhone;

    const query = `
      INSERT INTO public.chat_users (phone_number, name)
      VALUES ($1, $2)
      ON CONFLICT (phone_number)
      DO UPDATE SET
        name = CASE WHEN chat_users.name = chat_users.phone_number THEN EXCLUDED.name ELSE chat_users.name END
      RETURNING id, phone_number, name, avatar_url, status_message, last_seen;
    `;
    const result = await pool.query(query, [cleanPhone, displayName]);
    return res.json({ success: true, user: result.rows[0] });
  } catch (err) {
    console.error("Error in /api/chat/direct-contact:", err);
    return res.status(500).json({ error: "Failed to resolve contact." });
  }
});

// 4. Match / Sync Contacts
// POST /api/chat/sync-contacts
router.post("/sync-contacts", async (req, res) => {
  try {
    const { phone_numbers, current_user_id } = req.body;
    if (!Array.isArray(phone_numbers)) {
      return res.status(400).json({ error: "phone_numbers must be an array." });
    }

    // Clean and normalize incoming numbers
    const cleanNumbers = phone_numbers.map(normalizePhone).filter(Boolean);
    if (cleanNumbers.length === 0) {
      return res.json({ registered: [], not_registered: [] });
    }

    // Build suffix matches for phone matching (to match both +919876543210 and 9876543210)
    // We search chat_users where phone_number IN (cleanNumbers) OR right 10 digits match
    const result = await pool.query(
      `SELECT id, phone_number, name, avatar_url, status_message, last_seen
       FROM public.chat_users
       WHERE phone_number = ANY($1::text[])
          OR RIGHT(phone_number, 10) = ANY(
               SELECT RIGHT(x, 10) FROM unnest($1::text[]) AS x WHERE LENGTH(x) >= 10
             )`,
      [cleanNumbers]
    );

    const registeredMap = new Map();
    for (const user of result.rows) {
      // Exclude current user from contact list
      if (current_user_id && user.id === current_user_id) continue;
      registeredMap.set(user.phone_number, user);
      if (user.phone_number.length >= 10) {
        registeredMap.set(user.phone_number.slice(-10), user);
      }
    }

    const registered = [];
    const notRegistered = [];
    const seenRegisteredIds = new Set();

    for (const num of phone_numbers) {
      const clean = normalizePhone(num);
      const last10 = clean.length >= 10 ? clean.slice(-10) : clean;
      const matchedUser = registeredMap.get(clean) || (last10 ? registeredMap.get(last10) : null);

      if (matchedUser) {
        if (!seenRegisteredIds.has(matchedUser.id)) {
          seenRegisteredIds.add(matchedUser.id);
          registered.push({
            ...matchedUser,
            contact_source_number: num
          });
        }
      } else {
        notRegistered.push(num);
      }
    }

    return res.json({ registered, not_registered: notRegistered });
  } catch (err) {
    console.error("Error in /api/chat/sync-contacts:", err);
    return res.status(500).json({ error: "Internal server error syncing contacts." });
  }
});

// Helper to find or create conversation between 2 users
async function getOrCreateConversation(userA, userB) {
  // Sort UUIDs to enforce unique constraint
  const [u1, u2] = userA < userB ? [userA, userB] : [userB, userA];

  let res = await pool.query(
    "SELECT id, user1_id, user2_id, last_message_text, last_message_time, created_at FROM public.chat_conversations WHERE user1_id = $1 AND user2_id = $2",
    [u1, u2]
  );

  if (res.rows.length === 0) {
    res = await pool.query(
      `INSERT INTO public.chat_conversations (user1_id, user2_id, last_message_text, last_message_time)
       VALUES ($1, $2, '', NOW())
       ON CONFLICT (user1_id, user2_id) DO UPDATE SET user1_id = EXCLUDED.user1_id
       RETURNING id, user1_id, user2_id, last_message_text, last_message_time, created_at`,
      [u1, u2]
    );
  }

  return res.rows[0];
}

// 5. Get conversations for user
// GET /api/chat/conversations?userId=...
router.get("/conversations", async (req, res) => {
  try {
    const { userId } = req.query;
    if (!userId) {
      return res.status(400).json({ error: "userId parameter is required." });
    }

    // Update user's last_seen
    pool.query("UPDATE public.chat_users SET last_seen = NOW() WHERE id = $1", [userId]).catch(() => {});

    const query = `
      SELECT 
        c.id AS conversation_id,
        c.last_message_text,
        c.last_message_time,
        c.created_at,
        CASE WHEN c.user1_id = $1 THEN u2.id ELSE u1.id END AS other_user_id,
        CASE WHEN c.user1_id = $1 THEN u2.name ELSE u1.name END AS other_user_name,
        CASE WHEN c.user1_id = $1 THEN u2.phone_number ELSE u1.phone_number END AS other_user_phone,
        CASE WHEN c.user1_id = $1 THEN u2.avatar_url ELSE u1.avatar_url END AS other_user_avatar,
        CASE WHEN c.user1_id = $1 THEN u2.last_seen ELSE u1.last_seen END AS other_user_last_seen,
        COALESCE((
          SELECT COUNT(*)::int 
          FROM public.chat_messages m 
          WHERE m.conversation_id = c.id 
            AND m.receiver_id = $1 
            AND m.status != 'read'
        ), 0) AS unread_count
      FROM public.chat_conversations c
      JOIN public.chat_users u1 ON c.user1_id = u1.id
      JOIN public.chat_users u2 ON c.user2_id = u2.id
      WHERE c.user1_id = $1 OR c.user2_id = $1
      ORDER BY c.last_message_time DESC NULLS LAST;
    `;

    const result = await pool.query(query, [userId]);
    return res.json({ success: true, conversations: result.rows });
  } catch (err) {
    console.error("Error in /api/chat/conversations:", err);
    return res.status(500).json({ error: "Internal server error fetching conversations." });
  }
});

// 6. Get messages for conversation
// GET /api/chat/messages?conversationId=...&userId=...&limit=50&before=...
router.get("/messages", async (req, res) => {
  try {
    const { conversationId, userId, otherUserId, limit = 50, before } = req.query;

    let convId = conversationId;
    if (!convId && userId && otherUserId) {
      const conv = await getOrCreateConversation(userId, otherUserId);
      convId = conv.id;
    }

    if (!convId) {
      return res.status(400).json({ error: "conversationId or (userId and otherUserId) required." });
    }

    const queryParams = [convId, parseInt(limit, 10) || 50];
    let beforeClause = "";
    if (before) {
      queryParams.push(before);
      beforeClause = `AND m.created_at < $${queryParams.length}`;
    }

    const query = `
      SELECT 
        m.id,
        m.conversation_id,
        m.sender_id,
        m.receiver_id,
        m.message_text,
        m.media_url,
        m.media_type,
        m.file_name,
        m.file_size,
        m.status,
        m.created_at
      FROM public.chat_messages m
      WHERE m.conversation_id = $1
        ${beforeClause}
      ORDER BY m.created_at DESC
      LIMIT $2;
    `;

    const result = await pool.query(query, queryParams);

    // Auto mark received unread messages as 'read' if userId is viewing
    if (userId) {
      await pool.query(
        `UPDATE public.chat_messages 
         SET status = 'read' 
         WHERE conversation_id = $1 
           AND receiver_id = $2 
           AND status != 'read'`,
        [convId, userId]
      );
    }

    // Return in ascending chronological order for chat view
    const messages = result.rows.reverse();
    return res.json({ success: true, conversation_id: convId, messages });
  } catch (err) {
    console.error("Error in /api/chat/messages:", err);
    return res.status(500).json({ error: "Internal server error fetching messages." });
  }
});

// 7. Send message
// POST /api/chat/messages/send
router.post("/messages/send", async (req, res) => {
  try {
    const { sender_id, receiver_id, message_text, media_url, media_type, file_name, file_size } = req.body;
    if (!sender_id || !receiver_id) {
      return res.status(400).json({ error: "sender_id and receiver_id are required." });
    }

    let finalMessageText = message_text ? String(message_text).trim() : "";
    if (!finalMessageText && media_url) {
      if (media_type === "image") finalMessageText = "📷 Photo";
      else if (media_type === "video") finalMessageText = "🎥 Video";
      else if (media_type === "audio") finalMessageText = "🎵 Audio";
      else finalMessageText = file_name ? `📄 ${file_name}` : "📎 Attachment";
    }

    if (!finalMessageText) {
      return res.status(400).json({ error: "message_text or media_url is required." });
    }

    // Get or create conversation
    const conversation = await getOrCreateConversation(sender_id, receiver_id);

    // Insert message
    const msgQuery = `
      INSERT INTO public.chat_messages (conversation_id, sender_id, receiver_id, message_text, media_url, media_type, file_name, file_size, status)
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, 'sent')
      RETURNING id, conversation_id, sender_id, receiver_id, message_text, media_url, media_type, file_name, file_size, status, created_at;
    `;

    const msgRes = await pool.query(msgQuery, [
      conversation.id,
      sender_id,
      receiver_id,
      finalMessageText,
      media_url || null,
      media_type || null,
      file_name || null,
      file_size ? parseInt(file_size, 10) : null
    ]);

    const sentMessage = msgRes.rows[0];

    // Update conversation last message info
    await pool.query(
      `UPDATE public.chat_conversations 
       SET last_message_text = $1, last_message_time = $2 
       WHERE id = $3`,
      [sentMessage.message_text, sentMessage.created_at, conversation.id]
    );

    // Update sender last_seen
    pool.query("UPDATE public.chat_users SET last_seen = NOW() WHERE id = $1", [sender_id]).catch(() => {});

    return res.json({ success: true, message: sentMessage });
  } catch (err) {
    console.error("Error in /api/chat/messages/send:", err);
    return res.status(500).json({ error: "Internal server error sending message." });
  }
});

// 8. Poll for new messages / status updates
// GET /api/chat/poll?userId=...&since=...
router.get("/poll", async (req, res) => {
  try {
    const { userId, since } = req.query;
    if (!userId) {
      return res.status(400).json({ error: "userId is required." });
    }

    let sinceDate;
    if (since) {
      const parsed = new Date(since);
      // Guard against client clock skew: if client time is > 2 seconds into the future, fallback to last 30s
      if (isNaN(parsed.getTime()) || parsed.getTime() > (Date.now() + 2000)) {
        sinceDate = new Date(Date.now() - 30000);
      } else {
        sinceDate = parsed;
      }
    } else {
      sinceDate = new Date(Date.now() - 30000);
    }

    // Update last_seen
    pool.query("UPDATE public.chat_users SET last_seen = NOW() WHERE id = $1", [userId]).catch(() => {});

    // Incoming messages
    const incomingRes = await pool.query(
      `SELECT id, conversation_id, sender_id, receiver_id, message_text, media_url, media_type, file_name, file_size, status, created_at
       FROM public.chat_messages
       WHERE receiver_id = $1 AND created_at > $2
       ORDER BY created_at ASC`,
      [userId, sinceDate]
    );

    // If incoming messages found, mark them delivered
    if (incomingRes.rows.length > 0) {
      const msgIds = incomingRes.rows.map(m => m.id);
      await pool.query(
        `UPDATE public.chat_messages SET status = 'delivered' WHERE id = ANY($1::uuid[]) AND status = 'sent'`,
        [msgIds]
      );
    }

    // Status updates on messages sent by user (e.g. they changed from sent -> delivered -> read)
    const statusRes = await pool.query(
      `SELECT id, conversation_id, status
       FROM public.chat_messages
       WHERE sender_id = $1 AND created_at > $2`,
      [userId, sinceDate]
    );

    return res.json({
      success: true,
      timestamp: new Date().toISOString(),
      incoming: incomingRes.rows,
      status_updates: statusRes.rows
    });
  } catch (err) {
    console.error("Error in /api/chat/poll:", err);
    return res.status(500).json({ error: "Internal server error polling chat." });
  }
});

// 9. Update message status (e.g. when user opens a chat screen)
// POST /api/chat/messages/status
router.post("/messages/status", async (req, res) => {
  try {
    const { conversation_id, receiver_id, status = "read" } = req.body;
    if (!conversation_id || !receiver_id) {
      return res.status(400).json({ error: "conversation_id and receiver_id required." });
    }

    await pool.query(
      `UPDATE public.chat_messages 
       SET status = $1 
       WHERE conversation_id = $2 AND receiver_id = $3 AND status != $1`,
      [status, conversation_id, receiver_id]
    );

    return res.json({ success: true });
  } catch (err) {
    console.error("Error updating message status:", err);
    return res.status(500).json({ error: "Internal server error." });
  }
});

module.exports = router;
