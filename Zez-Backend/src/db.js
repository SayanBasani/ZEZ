require("dotenv").config();
const crypto = require("node:crypto");
const { Pool } = require("pg");

const connectionString = process.env.DATABASE_URL;

if (!connectionString) {
  throw new Error("DATABASE_URL is not configured. Check your .env file.");
}

const pool = new Pool({
  connectionString,
  max: 5,
  idleTimeoutMillis: 30_000,
  connectionTimeoutMillis: 10_000,
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
  await pool.query("SELECT 1");

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

  // Ensure app_releases table exists
  await pool.query(`
    CREATE TABLE IF NOT EXISTS public.app_releases (
      id SERIAL PRIMARY KEY,
      version_name VARCHAR(50) NOT NULL,
      version_code INTEGER NOT NULL,
      release_notes TEXT,
      file_name VARCHAR(255) NOT NULL,
      file_size BIGINT NOT NULL,
      download_url VARCHAR(255) NOT NULL,
      is_active BOOLEAN DEFAULT TRUE,
      uploaded_by VARCHAR(100) DEFAULT 'admin',
      created_at TIMESTAMPTZ DEFAULT NOW()
    );
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

    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS mode VARCHAR(16) DEFAULT 'normal';
    ALTER TABLE public.chat_messages ADD COLUMN IF NOT EXISTS secret_session_id UUID;
    CREATE INDEX IF NOT EXISTS idx_chat_messages_mode ON public.chat_messages(conversation_id, mode);

    -- High performance query indexes
    CREATE INDEX IF NOT EXISTS idx_call_logs_timestamp ON public.call_logs (call_timestamp DESC, id DESC);
    CREATE INDEX IF NOT EXISTS idx_devices_last_seen ON public.devices (last_seen_at DESC NULLS LAST);
    CREATE INDEX IF NOT EXISTS idx_chat_conversations_last_msg ON public.chat_conversations (last_message_time DESC NULLS LAST);
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
