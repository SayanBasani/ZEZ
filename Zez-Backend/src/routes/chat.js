const express = require("express");
const path = require("path");
const fs = require("fs");
const multer = require("multer");
const { pool } = require("../db");
const b2Storage = require("../services/b2StorageService");
const { saveToStorageCache } = require("./storage");

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
  let digits = String(raw).replace(/\D/g, "");
  // Indian phone number normalization: handle +91, 0091, 91, and leading 0
  if (digits.startsWith("0091")) {
    digits = digits.substring(4);
  } else if (digits.length === 12 && digits.startsWith("91")) {
    digits = digits.substring(2);
  } else if (digits.length === 11 && digits.startsWith("0")) {
    digits = digits.substring(1);
  }
  return digits;
}

// 1. Register or update chat user
// POST /api/chat/register
router.post("/register", async (req, res) => {
  try {
    const rawPhone = req.body.phone_number || req.body.phoneNumber;
    const rawName = req.body.name || req.body.displayName;
    const rawAvatar = req.body.avatar_url || req.body.avatarUrl;
    const rawStatus = req.body.status_message || req.body.statusMessage;

    if (!rawPhone || !rawName) {
      return res.status(400).json({ success: false, error: "Phone number and name are required." });
    }

    const cleanPhone = normalizePhone(rawPhone);
    const displayName = String(rawName).trim();
    const avatar = rawAvatar || null;
    const status = rawStatus || "Hey there! I am using Zez.";

    // Check if user already exists by exact phone or matching last 10 digits (to merge +91 vs 10-digit)
    const existing = await pool.query(
      `SELECT id FROM public.chat_users 
       WHERE phone_number = $1 
          OR RIGHT(REGEXP_REPLACE(phone_number, '\\D', '', 'g'), 10) = RIGHT($1, 10)
       LIMIT 1`,
      [cleanPhone]
    );

    let query;
    let params;
    if (existing.rows.length > 0) {
      query = `
        UPDATE public.chat_users 
        SET phone_number = $1,
            name = $2,
            avatar_url = COALESCE($3, avatar_url),
            status_message = COALESCE($4, status_message),
            last_seen = NOW()
        WHERE id = $5
        RETURNING id, phone_number, name, avatar_url, status_message, last_seen, created_at;
      `;
      params = [cleanPhone, displayName, avatar, status, existing.rows[0].id];
    } else {
      query = `
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
      params = [cleanPhone, displayName, avatar, status];
    }

    const result = await pool.query(query, params);
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

// 3b. Upload Chat Media (Images, Videos, Files, Audio) - Backblaze B2 Object Storage
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

    let relativeUrl;
    let objectKey = null;
    let storageProvider = "local";

    try {
      const uploadResult = await b2Storage.uploadFile({
        filePath: req.file.path,
        mimeType: mime || "application/octet-stream",
        category: mediaType === "video" ? "videos" : (mediaType === "image" ? "chat-media" : "documents"),
        originalName: req.file.originalname
      });
      objectKey = uploadResult.objectKey;
      relativeUrl = `/api/storage/file/${uploadResult.objectKey}`;
      storageProvider = "backblaze_b2";

      // Record in public.media_files
      await pool.query(
        `INSERT INTO public.media_files (
           storage_provider, bucket_name, object_key, original_file_name,
           mime_type, file_size, file_category, uploaded_by, upload_status, metadata
         ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
         ON CONFLICT (object_key) DO NOTHING`,
        [
          "backblaze_b2",
          uploadResult.bucketName,
          uploadResult.objectKey,
          req.file.originalname,
          mime || "application/octet-stream",
          req.file.size,
          mediaType === "video" ? "videos" : "chat-media",
          "user",
          "completed",
          JSON.stringify({ etag: uploadResult.etag })
        ]
      );

      // Clean up local temp file and preserve in instant local storage cache
      saveToStorageCache(uploadResult.objectKey, req.file.path);
      try { if (fs.existsSync(req.file.path)) fs.unlinkSync(req.file.path); } catch (_) {}
    } catch (b2Err) {
      console.warn("Backblaze B2 upload error, falling back to local disk:", b2Err.message);
      relativeUrl = `/uploads/chat_media/${req.file.filename}`;
    }

    return res.json({
      success: true,
      url: relativeUrl,
      objectKey,
      fileName: req.file.originalname,
      fileSize: req.file.size,
      mediaType,
      storage: storageProvider
    });
  } catch (err) {
    console.error("Error in /api/chat/upload:", err);
    return res.status(500).json({ error: "Failed to upload chat media." });
  }
});

// 3c. Upload Chat User Profile Picture - Backblaze B2 Object Storage
// POST /api/chat/upload-avatar
router.post("/upload-avatar", uploadAvatar.single("avatar"), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: "No avatar image provided." });
    }

    const { userId } = req.body;
    let relativeUrl;
    let objectKey = null;
    let storageProvider = "local";

    try {
      const uploadResult = await b2Storage.uploadFile({
        filePath: req.file.path,
        mimeType: req.file.mimetype || "image/jpeg",
        category: "avatars",
        originalName: req.file.originalname
      });
      objectKey = uploadResult.objectKey;
      relativeUrl = `/api/storage/file/${uploadResult.objectKey}`;
      storageProvider = "backblaze_b2";

      // Record in public.media_files
      await pool.query(
        `INSERT INTO public.media_files (
           user_id, storage_provider, bucket_name, object_key, original_file_name,
           mime_type, file_size, file_category, uploaded_by, upload_status, metadata
         ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
         ON CONFLICT (object_key) DO NOTHING`,
        [
          userId || null,
          "backblaze_b2",
          uploadResult.bucketName,
          uploadResult.objectKey,
          req.file.originalname,
          req.file.mimetype || "image/jpeg",
          req.file.size,
          "avatars",
          "user",
          "completed",
          JSON.stringify({ etag: uploadResult.etag })
        ]
      );

      // Clean up local temp file and preserve in instant local storage cache
      saveToStorageCache(uploadResult.objectKey, req.file.path);
      try { if (fs.existsSync(req.file.path)) fs.unlinkSync(req.file.path); } catch (_) {}
    } catch (b2Err) {
      console.warn("Backblaze B2 avatar upload error, falling back to local disk:", b2Err.message);
      relativeUrl = `/uploads/avatars/${req.file.filename}`;
    }

    if (userId) {
      await pool.query(
        "UPDATE public.chat_users SET avatar_url = $1, last_seen = NOW() WHERE id = $2",
        [relativeUrl, userId]
      );
    }

    return res.json({
      success: true,
      avatar_url: relativeUrl,
      objectKey,
      storage: storageProvider
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

    // Check if user already exists with this phone or last 10 digits
    const existing = await pool.query(
      `SELECT id, phone_number, name, avatar_url, status_message, last_seen 
       FROM public.chat_users 
       WHERE phone_number = $1 
          OR RIGHT(REGEXP_REPLACE(phone_number, '\\D', '', 'g'), 10) = RIGHT($1, 10)
       LIMIT 1`,
      [cleanPhone]
    );

    if (existing.rows.length > 0) {
      return res.json({ success: true, user: existing.rows[0] });
    }

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
    "SELECT id, user1_id, user2_id, is_secret_mode, secret_session_id, last_message_text, last_message_time, created_at FROM public.chat_conversations WHERE user1_id = $1 AND user2_id = $2",
    [u1, u2]
  );

  if (res.rows.length === 0) {
    res = await pool.query(
      `INSERT INTO public.chat_conversations (user1_id, user2_id, last_message_text, last_message_time)
       VALUES ($1, $2, '', NOW())
       ON CONFLICT (user1_id, user2_id) DO UPDATE SET user1_id = EXCLUDED.user1_id
       RETURNING id, user1_id, user2_id, is_secret_mode, secret_session_id, last_message_text, last_message_time, created_at`,
      [u1, u2]
    );
  }

  return res.rows[0];
}

// 4b. Get conversation mode
// GET /api/chat/conversation/:id/mode
router.get(["/conversation/:id/mode", "/conversations/:id/mode"], async (req, res) => {
  try {
    const convId = req.params.id;
    const result = await pool.query(
      "SELECT id, is_secret_mode, secret_session_id, secret_mode_updated_at, secret_mode_by FROM public.chat_conversations WHERE id = $1",
      [convId]
    );
    if (result.rows.length === 0) {
      return res.status(404).json({ error: "Conversation not found." });
    }
    const row = result.rows[0];
    return res.json({
      success: true,
      conversationId: row.id,
      isSecretMode: !!row.is_secret_mode,
      secretSessionId: row.secret_session_id,
      secretModeUpdatedAt: row.secret_mode_updated_at,
      secretModeBy: row.secret_mode_by
    });
  } catch (err) {
    console.error("Error in GET /conversation/:id/mode:", err);
    return res.status(500).json({ error: "Failed to retrieve conversation mode." });
  }
});

// 4c. Set / Toggle conversation mode (Normal <-> Secret Mode)
// POST /api/chat/conversation/:id/mode
router.post(["/conversation/:id/mode", "/conversations/:id/mode"], async (req, res) => {
  try {
    const convId = req.params.id;
    const { isSecretMode, userId } = req.body;
    const modeBool = !!isSecretMode;

    let query;
    let values;
    if (modeBool) {
      // Turning Secret Mode ON: generate a new secret_session_id
      query = `
        UPDATE public.chat_conversations 
        SET is_secret_mode = true,
            secret_session_id = gen_random_uuid(),
            secret_mode_updated_at = NOW(),
            secret_mode_by = $2
        WHERE id = $1
        RETURNING id, is_secret_mode, secret_session_id, secret_mode_updated_at, secret_mode_by;
      `;
      values = [convId, userId || null];
    } else {
      // Turning Secret Mode OFF
      query = `
        UPDATE public.chat_conversations 
        SET is_secret_mode = false,
            secret_mode_updated_at = NOW(),
            secret_mode_by = $2
        WHERE id = $1
        RETURNING id, is_secret_mode, secret_session_id, secret_mode_updated_at, secret_mode_by;
      `;
      values = [convId, userId || null];
    }

    const result = await pool.query(query, values);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: "Conversation not found." });
    }
    const row = result.rows[0];
    return res.json({
      success: true,
      conversationId: row.id,
      isSecretMode: !!row.is_secret_mode,
      secretSessionId: row.secret_session_id,
      secretModeUpdatedAt: row.secret_mode_updated_at,
      secretModeBy: row.secret_mode_by
    });
  } catch (err) {
    console.error("Error in POST /conversation/:id/mode:", err);
    return res.status(500).json({ error: "Failed to toggle conversation mode." });
  }
});

// 5. Get conversations for user
// GET /api/chat/conversations?userId=...
router.get("/conversations", async (req, res) => {
  try {
    const { userId } = req.query;
    if (!userId) {
      return res.status(400).json({ error: "userId parameter is required." });
    }

    // Update user's last_seen and mark pending sent messages as delivered
    pool.query("UPDATE public.chat_users SET last_seen = NOW() WHERE id = $1", [userId]).catch(() => {});
    pool.query(
      "UPDATE public.chat_messages SET status = 'delivered', updated_at = NOW() WHERE receiver_id = $1 AND status = 'sent'",
      [userId]
    ).catch(() => {});

    const query = `
      SELECT 
        c.id AS conversation_id,
        c.last_message_text,
        c.last_message_time,
        c.created_at,
        c.is_secret_mode,
        c.secret_session_id,
        c.secret_mode_updated_at,
        CASE WHEN c.user1_id = $1 AND c.user2_id = $1 THEN $1
             WHEN c.user1_id = $1 THEN u2.id
             ELSE u1.id END AS other_user_id,
        CASE WHEN c.user1_id = $1 AND c.user2_id = $1 THEN 'You (Message yourself)'
             WHEN c.user1_id = $1 THEN u2.name
             ELSE u1.name END AS other_user_name,
        CASE WHEN c.user1_id = $1 AND c.user2_id = $1 THEN u1.phone_number
             WHEN c.user1_id = $1 THEN u2.phone_number
             ELSE u1.phone_number END AS other_user_phone,
        CASE WHEN c.user1_id = $1 AND c.user2_id = $1 THEN u1.avatar_url
             WHEN c.user1_id = $1 THEN u2.avatar_url
             ELSE u1.avatar_url END AS other_user_avatar,
        CASE WHEN c.user1_id = $1 AND c.user2_id = $1 THEN u1.last_seen
             WHEN c.user1_id = $1 THEN u2.last_seen
             ELSE u1.last_seen END AS other_user_last_seen,
        COALESCE((
          SELECT COUNT(*)::int 
          FROM public.chat_messages m 
          WHERE m.conversation_id = c.id 
            AND m.receiver_id = $1 
            AND m.sender_id != $1
            AND m.status != 'read'
        ), 0) AS unread_count
      FROM public.chat_conversations c
      JOIN public.chat_users u1 ON c.user1_id = u1.id
      JOIN public.chat_users u2 ON c.user2_id = u2.id
      WHERE (c.user1_id = $1 OR c.user2_id = $1)
        AND NOT (COALESCE(c.deleted_for_users, '[]'::jsonb) ? $1::text)
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
// GET /api/chat/messages?conversationId=...&userId=...&limit=50&before=...&mode=normal|secret&secretSessionId=...
router.get("/messages", async (req, res) => {
  try {
    const { conversationId, userId, otherUserId, limit = 50, before, mode = "normal", secretSessionId } = req.query;

    let convId = conversationId;
    let convRow = null;
    if (!convId && userId && otherUserId) {
      convRow = await getOrCreateConversation(userId, otherUserId);
      convId = convRow.id;
    } else if (convId) {
      const cRes = await pool.query(
        "SELECT id, is_secret_mode, secret_session_id, secret_mode_updated_at FROM public.chat_conversations WHERE id = $1",
        [convId]
      );
      if (cRes.rows.length > 0) {
        convRow = cRes.rows[0];
      }
    }

    if (!convId) {
      return res.status(400).json({ error: "conversationId or (userId and otherUserId) required." });
    }

    // Ephemeral Secret Mode rule:
    // If client requests secret mode without a secretSessionId (fresh launch / re-entry),
    // they get an empty message list (fresh blank screen as requested)
    if (mode === "secret" && !secretSessionId) {
      return res.json({
        success: true,
        conversation_id: convId,
        isSecretMode: convRow ? !!convRow.is_secret_mode : false,
        secretSessionId: convRow ? convRow.secret_session_id : null,
        messages: []
      });
    }

    const queryParams = [convId, parseInt(limit, 10) || 50];

    let userDeleteClause = "";
    if (userId) {
      queryParams.push(userId);
      userDeleteClause = `AND NOT (COALESCE(m.deleted_for_users, '[]'::jsonb) ? $${queryParams.length}::text)`;
    }

    let modeClause = "";
    if (mode === "secret") {
      queryParams.push(secretSessionId);
      modeClause = `AND m.mode = 'secret' AND m.secret_session_id = $${queryParams.length}`;
    } else {
      modeClause = `AND COALESCE(m.mode, 'normal') = 'normal'`;
    }

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
        CASE WHEN m.is_deleted_for_everyone THEN '🚫 This message was deleted' ELSE m.message_text END AS message_text,
        CASE WHEN m.is_deleted_for_everyone THEN NULL ELSE m.media_url END AS media_url,
        m.media_type,
        m.file_name,
        m.file_size,
        m.mode,
        m.secret_session_id,
        m.status,
        CASE WHEN m.is_deleted_for_everyone THEN NULL ELSE m.reaction END AS reaction,
        m.is_deleted_for_everyone,
        m.created_at
      FROM public.chat_messages m
      WHERE m.conversation_id = $1
        ${userDeleteClause}
        ${modeClause}
        ${beforeClause}
      ORDER BY m.created_at DESC
      LIMIT $2;
    `;

    const result = await pool.query(query, queryParams);

    // Auto mark received unread messages as 'read' if userId is viewing
    if (userId) {
      await pool.query(
        `UPDATE public.chat_messages 
         SET status = 'read', updated_at = NOW() 
         WHERE conversation_id = $1 
           AND receiver_id = $2 
           AND status != 'read'`,
        [convId, userId]
      );
    }

    // Return in ascending chronological order for chat view
    const messages = result.rows.reverse();

    // Query peer status for accurate online / last seen indicator
    let peerStatus = null;
    const targetPeerId = otherUserId || (convRow ? (convRow.user1_id === userId ? convRow.user2_id : convRow.user1_id) : null);
    if (targetPeerId && targetPeerId !== userId) {
      const pRes = await pool.query("SELECT id, last_seen, name FROM public.chat_users WHERE id = $1", [targetPeerId]);
      if (pRes.rows.length > 0) {
        const pRow = pRes.rows[0];
        const isOnline = pRow.last_seen && (Date.now() - new Date(pRow.last_seen).getTime() < 12000);
        peerStatus = {
          userId: pRow.id,
          isOnline: !!isOnline,
          lastSeen: pRow.last_seen
        };
      }
    }

    return res.json({
      success: true,
      timestamp: new Date().toISOString(),
      conversation_id: convId,
      isSecretMode: convRow ? !!convRow.is_secret_mode : false,
      secretSessionId: convRow ? convRow.secret_session_id : null,
      peer_status: peerStatus,
      messages
    });
  } catch (err) {
    console.error("Error in /api/chat/messages:", err);
    return res.status(500).json({ error: "Internal server error fetching messages." });
  }
});

// 7. Send message
// POST /api/chat/messages/send or /api/chat/messages
router.post(["/messages/send", "/messages"], async (req, res) => {
  try {
    const sender_id = req.body.sender_id || req.body.senderId;
    const receiver_id = req.body.receiver_id || req.body.receiverId;
    const message_text = req.body.message_text ?? req.body.messageText;
    const media_url = req.body.media_url ?? req.body.mediaUrl;
    const media_type = req.body.media_type ?? req.body.mediaType;
    const file_name = req.body.file_name ?? req.body.fileName;
    const file_size = req.body.file_size ?? req.body.fileSize;
    const mode = req.body.mode || "normal";
    const secret_session_id = req.body.secret_session_id || req.body.secretSessionId;

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

    const isSecret = mode === "secret" || conversation.is_secret_mode;
    const finalMode = isSecret ? "secret" : "normal";
    const sessionUuid = isSecret ? (secret_session_id || conversation.secret_session_id) : null;
    const initialStatus = (sender_id === receiver_id) ? 'read' : 'sent';

    // Insert message
    const msgQuery = `
      INSERT INTO public.chat_messages (
        conversation_id, sender_id, receiver_id, message_text,
        media_url, media_type, file_name, file_size, mode, secret_session_id, status
      )
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
      RETURNING id, conversation_id, sender_id, receiver_id, message_text,
                media_url, media_type, file_name, file_size, mode, secret_session_id, status, created_at;
    `;

    const msgRes = await pool.query(msgQuery, [
      conversation.id,
      sender_id,
      receiver_id,
      finalMessageText,
      media_url || null,
      media_type || null,
      file_name || null,
      file_size ? parseInt(file_size, 10) : null,
      finalMode,
      sessionUuid,
      initialStatus
    ]);

    const sentMessage = msgRes.rows[0];

    // For normal messages, update conversation snippet and revive conversation if previously deleted
    if (finalMode === "normal") {
      await pool.query(
        `UPDATE public.chat_conversations 
         SET last_message_text = $1, last_message_time = $2,
             deleted_for_users = '[]'::jsonb
         WHERE id = $3`,
        [sentMessage.message_text, sentMessage.created_at, conversation.id]
      );
    }

    // Update sender last_seen
    pool.query("UPDATE public.chat_users SET last_seen = NOW() WHERE id = $1", [sender_id]).catch(() => {});

    return res.json({ success: true, message: sentMessage });
  } catch (err) {
    console.error("Error in /api/chat/messages/send:", err);
    return res.status(500).json({ error: "Internal server error sending message." });
  }
});

// 8. Poll for new messages / status updates
// GET /api/chat/poll?userId=...&since=...&conversationId=...
router.get("/poll", async (req, res) => {
  try {
    const { userId, since, conversationId } = req.query;
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
        // Apply a 5-second overlap safety buffer so clock drifts or same-millisecond messages aren't skipped
        sinceDate = new Date(parsed.getTime() - 5000);
      }
    } else {
      sinceDate = new Date(Date.now() - 30000);
    }

    // Update last_seen
    pool.query("UPDATE public.chat_users SET last_seen = NOW() WHERE id = $1", [userId]).catch(() => {});

    // Incoming messages
    const incomingRes = await pool.query(
      `SELECT id, conversation_id, sender_id, receiver_id, 
              CASE WHEN is_deleted_for_everyone THEN '🚫 This message was deleted' ELSE message_text END AS message_text,
              CASE WHEN is_deleted_for_everyone THEN NULL ELSE media_url END AS media_url,
              media_type, file_name, file_size, mode, secret_session_id, status,
              CASE WHEN is_deleted_for_everyone THEN NULL ELSE reaction END AS reaction,
              is_deleted_for_everyone,
              created_at
       FROM public.chat_messages
       WHERE receiver_id = $1 
         AND NOT (COALESCE(deleted_for_users, '[]'::jsonb) ? $1::text)
         AND (created_at >= $2 OR updated_at >= $2)
       ORDER BY created_at ASC`,
      [userId, sinceDate]
    );

    // If incoming messages found, mark them delivered with updated_at timestamp
    if (incomingRes.rows.length > 0) {
      const msgIds = incomingRes.rows.map(m => m.id);
      await pool.query(
        `UPDATE public.chat_messages SET status = 'delivered', updated_at = NOW() WHERE id = ANY($1::uuid[]) AND status = 'sent'`,
        [msgIds]
      );
    }

    // If viewing a specific conversation, auto-mark any messages sent to this user as 'read'
    if (conversationId && userId) {
      await pool.query(
        `UPDATE public.chat_messages 
         SET status = 'read', updated_at = NOW() 
         WHERE conversation_id = $1 AND receiver_id = $2 AND status != 'read'`,
        [conversationId, userId]
      ).catch(() => {});
    }

    // Status & reaction & delete updates on messages in conversation
    let statusQuery = `
      SELECT id, conversation_id, status, reaction, is_deleted_for_everyone,
             CASE WHEN is_deleted_for_everyone THEN '🚫 This message was deleted' ELSE message_text END AS message_text
      FROM public.chat_messages
      WHERE (sender_id = $1 OR receiver_id = $1) AND (COALESCE(updated_at, created_at) > $2 OR created_at > $2)
    `;
    let statusParams = [userId, sinceDate];

    // If conversation is being inspected, return status of messages modified since last poll
    if (conversationId) {
      statusQuery = `
        SELECT id, conversation_id, status, reaction, is_deleted_for_everyone,
               CASE WHEN is_deleted_for_everyone THEN '🚫 This message was deleted' ELSE message_text END AS message_text
        FROM public.chat_messages
        WHERE conversation_id = $1 
          AND (sender_id = $2 OR receiver_id = $2) 
          AND (COALESCE(updated_at, created_at) >= $3 OR created_at >= $3)
        ORDER BY created_at DESC
        LIMIT 100
      `;
      statusParams = [conversationId, userId, sinceDate];
    }
    const statusRes = await pool.query(statusQuery, statusParams);

    // If active conversationId is monitored, check its synchronized mode status and peer online status
    let conversationMode = null;
    let peerStatus = null;
    if (conversationId) {
      const modeRes = await pool.query(
        "SELECT id, is_secret_mode, secret_session_id, secret_mode_updated_at, secret_mode_by FROM public.chat_conversations WHERE id = $1",
        [conversationId]
      );
      if (modeRes.rows.length > 0) {
        const row = modeRes.rows[0];
        conversationMode = {
          conversationId: row.id,
          isSecretMode: !!row.is_secret_mode,
          secretSessionId: row.secret_session_id,
          secretModeUpdatedAt: row.secret_mode_updated_at,
          secretModeBy: row.secret_mode_by
        };
      }

      if (userId) {
        const peerRes = await pool.query(
          `SELECT u.id, u.last_seen, u.name, u.avatar_url 
           FROM public.chat_conversations c
           JOIN public.chat_users u ON (u.id = CASE WHEN c.user1_id = $1::uuid THEN c.user2_id ELSE c.user1_id END)
           WHERE c.id = $2::uuid AND u.id != $1::uuid
           LIMIT 1`,
          [userId, conversationId]
        );
        if (peerRes.rows.length > 0) {
          const peer = peerRes.rows[0];
          const isOnline = peer.last_seen && (Date.now() - new Date(peer.last_seen).getTime() < 12000);
          peerStatus = {
            userId: peer.id,
            isOnline: !!isOnline,
            lastSeen: peer.last_seen,
            avatarUrl: peer.avatar_url,
            name: peer.name
          };
        }
      }
    }

    // Check for active ringing incoming call
    let incomingCall = null;
    try {
      const callRes = await pool.query(
        `SELECT id, call_type, status, caller_id, caller_name, caller_avatar, caller_phone, started_at
         FROM public.active_calls 
         WHERE callee_id = $1 AND status = 'ringing' AND started_at > NOW() - INTERVAL '60 seconds'
         ORDER BY started_at DESC LIMIT 1`,
        [userId]
      );
      if (callRes.rowCount > 0) {
        const c = callRes.rows[0];
        incomingCall = {
          id: c.id,
          callType: c.call_type,
          callerId: c.caller_id,
          callerName: c.caller_name,
          callerAvatar: c.caller_avatar,
          callerPhone: c.caller_phone,
          startedAt: c.started_at
        };
      }
    } catch (_) {}

    return res.json({
      success: true,
      timestamp: new Date().toISOString(),
      incoming: incomingRes.rows,
      status_updates: statusRes.rows,
      conversation_mode: conversationMode,
      peer_status: peerStatus,
      incoming_call: incomingCall
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
       SET status = $1, updated_at = NOW() 
       WHERE conversation_id = $2 AND receiver_id = $3 AND status != $1`,
      [status, conversation_id, receiver_id]
    );

    return res.json({ success: true });
  } catch (err) {
    console.error("Error updating message status:", err);
    return res.status(500).json({ error: "Internal server error." });
  }
});

// 10. React with emoji to a message
// POST /api/chat/messages/:id/react
router.post("/messages/:id/react", async (req, res) => {
  try {
    const messageId = req.params.id;
    const { reaction, userId } = req.body;
    const cleanReaction = reaction ? String(reaction).trim() : null;

    const result = await pool.query(
      `UPDATE public.chat_messages 
       SET reaction = $1, updated_at = NOW() 
       WHERE id = $2 
       RETURNING id, conversation_id, sender_id, receiver_id, reaction, updated_at`,
      [cleanReaction, messageId]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: "Message not found." });
    }

    return res.json({ success: true, message: result.rows[0] });
  } catch (err) {
    console.error("Error in /api/chat/messages/:id/react:", err);
    return res.status(500).json({ error: "Failed to set reaction." });
  }
});

// 11. Delete message (Delete for me / Delete for everyone)
// POST /api/chat/messages/:id/delete
router.post("/messages/:id/delete", async (req, res) => {
  try {
    const messageId = req.params.id;
    const { userId, type } = req.body; // type: "for_everyone" or "for_me"
    if (!userId) {
      return res.status(400).json({ error: "userId is required." });
    }

    const checkRes = await pool.query("SELECT id, sender_id, receiver_id, conversation_id FROM public.chat_messages WHERE id = $1", [messageId]);
    if (checkRes.rows.length === 0) {
      return res.status(404).json({ error: "Message not found." });
    }
    const msg = checkRes.rows[0];

    if (type === "for_everyone") {
      // Only sender can delete for everyone
      if (msg.sender_id !== userId) {
        return res.status(403).json({ error: "Only the sender can delete a message for everyone." });
      }
      await pool.query(
        `UPDATE public.chat_messages 
         SET is_deleted_for_everyone = TRUE, 
             message_text = '🚫 This message was deleted', 
             media_url = NULL, 
             reaction = NULL, 
             updated_at = NOW() 
         WHERE id = $1`,
        [messageId]
      );
      return res.json({ success: true, type: "for_everyone", id: messageId });
    } else {
      // Delete for me
      await pool.query(
        `UPDATE public.chat_messages 
         SET deleted_for_users = COALESCE(deleted_for_users, '[]'::jsonb) || jsonb_build_array($1::text),
             updated_at = NOW()
         WHERE id = $2`,
        [userId, messageId]
      );
      return res.json({ success: true, type: "for_me", id: messageId });
    }
  } catch (err) {
    console.error("Error in /api/chat/messages/:id/delete:", err);
    return res.status(500).json({ error: "Failed to delete message." });
  }
});

// 12. Delete / hide conversation(s) for user
// POST /api/chat/conversations/delete
router.post(["/conversations/delete", "/conversations/batch-delete"], async (req, res) => {
  try {
    const { userId, conversationIds, conversationId } = req.body;
    if (!userId) {
      return res.status(400).json({ error: "userId parameter is required." });
    }

    const ids = Array.isArray(conversationIds) 
      ? conversationIds.filter(Boolean)
      : (conversationId ? [conversationId] : []);

    if (ids.length === 0) {
      return res.status(400).json({ error: "conversationIds array or conversationId required." });
    }

    // 1. Mark conversations as deleted for this user
    await pool.query(
      `UPDATE public.chat_conversations
       SET deleted_for_users = CASE 
         WHEN COALESCE(deleted_for_users, '[]'::jsonb) ? $1::text THEN deleted_for_users
         ELSE COALESCE(deleted_for_users, '[]'::jsonb) || jsonb_build_array($1::text)
       END
       WHERE id = ANY($2::uuid[])`,
      [userId, ids]
    );

    // 2. Also mark existing messages in these conversations as deleted for this user
    await pool.query(
      `UPDATE public.chat_messages
       SET deleted_for_users = CASE 
         WHEN COALESCE(deleted_for_users, '[]'::jsonb) ? $1::text THEN deleted_for_users
         ELSE COALESCE(deleted_for_users, '[]'::jsonb) || jsonb_build_array($1::text)
       END
       WHERE conversation_id = ANY($2::uuid[])`,
      [userId, ids]
    );

    return res.json({ success: true, count: ids.length, deletedIds: ids });
  } catch (err) {
    console.error("Error in /api/chat/conversations/delete:", err);
    return res.status(500).json({ error: "Failed to delete conversation(s)." });
  }
});

// 13. Clear conversation messages for user
// POST /api/chat/conversations/:id/clear
router.post(["/conversations/:id/clear", "/conversation/:id/clear"], async (req, res) => {
  try {
    const conversationId = req.params.id;
    const { userId } = req.body;
    if (!conversationId || !userId) {
      return res.status(400).json({ error: "conversationId and userId required." });
    }

    await pool.query(
      `UPDATE public.chat_messages
       SET deleted_for_users = CASE 
         WHEN COALESCE(deleted_for_users, '[]'::jsonb) ? $1::text THEN deleted_for_users
         ELSE COALESCE(deleted_for_users, '[]'::jsonb) || jsonb_build_array($1::text)
       END
       WHERE conversation_id = $2`,
      [userId, conversationId]
    );

    return res.json({ success: true, message: "Chat messages cleared." });
  } catch (err) {
    console.error("Error clearing conversation messages:", err);
    return res.status(500).json({ error: "Failed to clear conversation messages." });
  }
});

module.exports = router;
