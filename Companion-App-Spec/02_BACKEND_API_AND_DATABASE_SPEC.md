# 02 — Backend API & Database Specification (`Zez-Backend`)

The standalone Companion App connects directly to the **existing `Zez-Backend` Node.js / Express + Neon PostgreSQL server** (plus Backblaze B2 for OTA APK storage when configured). No backend rewrite is needed for core sync, alarms, or music streaming.

---

## 1. Base URLs & Environments

| Environment | Base URL | When Used |
| :--- | :--- | :--- |
| **Production (Render)** | `https://zez-chat-backend.onrender.com` | Real devices & default release builds |
| **Android Emulator Local** | `http://10.0.2.2:3000` | Auto-selected by `PreferencesHelper.isEmulator()` when no override is saved |
| **Custom Override** | Saved in `zez_prefs -> "api_base_url"` | Set via 5-tap Hardware Diagnostic / Developer PIN `7788` Server Dialog |

- **IPv4 DNS Fallback**: `ApiClient` resolves via `Dns.SYSTEM.lookup`, sorts IPv4 first, caches good resolutions, and falls back to Render edge IPs `216.24.57.18` / `216.24.57.16` if DNS fails for `*.onrender.com`.

---

## 2. Device Authentication (`middleware/deviceAuth.js`)

All protected device endpoints expect:
```http
Authorization: Bearer <token>
```
Server behavior:
1. Rejects missing/malformed Bearer token with `401`.
2. Hashes raw `<token>` using **SHA-256 hex digest** (`token_hash`).
3. Queries:
   ```sql
   SELECT id, androidid, persistentid, registration_status
   FROM public.devices
   WHERE token_hash = $1
     AND registration_status = 'active'
   LIMIT 1
   ```
4. Attaches `req.deviceId = device.id` (UUID) and `req.device = device`.
5. Client rule: on HTTP `401`, clear `device_token` in `PreferencesHelper`, call `POST /api/devices/register` once, and retry the request.

---

## 3. PostgreSQL Database Tables

### 3.1 `public.devices`
Used by `routes/devices.js`, `routes/callSync.js`, `routes/admin.js`:
- Primary key: `id` (`UUID`, generated via `gen_random_uuid()`)
- Identifiers: `androidid` (`TEXT`), `persistentid` (`TEXT`)
- Hardware/App metadata: `device_name`, `device_model`, `manufacturer`, `brand`, `platform`, `os_version`, `app_version`
- SIM metadata: `sim1_phone_number`, `sim2_phone_number`, `sim1_carrier`, `sim2_carrier`, `sim1_country_iso`, `sim2_country_iso`, `primary_phone_number`
- Auth & Status: `token_hash` (`TEXT`), `registration_status` (`'active'`), `device_details` (`JSONB`), `last_seen_at`, `updated_at`

### 3.2 `public.call_logs`
Used by `routes/callSync.js` (`POST /api/calls/sync`):
- `id` (`BIGSERIAL`)
- `device_id` (`UUID`, FK -> `public.devices(id)`)
- `external_id_hash` (`TEXT`, SHA-256 hex of `externalId` e.g. `SHA256("android:1234")`)
- `call_timestamp` (`TIMESTAMPTZ`)
- `encrypted_payload` (`TEXT`, JSON envelope encrypted with AES-256-GCM)
- Unique constraint: `UNIQUE (device_id, external_id_hash)` for deduplication (`ON CONFLICT DO NOTHING`)

#### AES-256-GCM Envelope (`src/crypto.js`)
Key comes from server `ENCRYPTION_KEY` (32-byte base64). Stored string format:
```json
{
  "version": 1,
  "iv": "<base64 12-byte IV>",
  "tag": "<base64 16-byte auth tag>",
  "data": "<base64 ciphertext of UTF-8 JSON>"
}
```
Ciphertext decodes to normalized call record:
`{ externalId, phoneNumber, name, type, timestamp, duration, raw }`.

### 3.3 `public.device_notifications` (`src/db.js` lines 131–145)
```sql
CREATE TABLE IF NOT EXISTS public.device_notifications (
  id BIGSERIAL PRIMARY KEY,
  device_id UUID NOT NULL,
  package_name TEXT NOT NULL,
  app_name TEXT,
  title TEXT,
  content TEXT,
  sub_text TEXT,
  post_time BIGINT NOT NULL,
  posted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(device_id, package_name, post_time, title)
);
CREATE INDEX IF NOT EXISTS idx_device_notifs_device_time
  ON public.device_notifications(device_id, post_time DESC);
```

### 3.4 `public.device_app_calls` (`src/db.js` lines 170–186)
```sql
CREATE TABLE IF NOT EXISTS public.device_app_calls (
  id BIGSERIAL PRIMARY KEY,
  device_id UUID NOT NULL,
  app_name TEXT NOT NULL,
  package_name TEXT NOT NULL,
  caller_name TEXT NOT NULL,
  call_type TEXT DEFAULT 'voice',
  call_direction TEXT DEFAULT 'incoming',
  duration_seconds INTEGER DEFAULT 0,
  call_time BIGINT NOT NULL,
  timestamp TIMESTAMPTZ,
  raw_details TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(device_id, package_name, call_time, caller_name, call_direction)
);
CREATE INDEX IF NOT EXISTS idx_device_app_calls_device_time
  ON public.device_app_calls(device_id, call_time DESC);
```

### 3.5 `public.device_alarms` (`src/db.js` lines 148–167)
```sql
CREATE TABLE IF NOT EXISTS public.device_alarms (
  id BIGSERIAL PRIMARY KEY,
  device_id UUID NOT NULL,
  alarm_id BIGINT NOT NULL,
  hour INTEGER NOT NULL,
  minute INTEGER NOT NULL,
  label TEXT,
  is_enabled BOOLEAN NOT NULL DEFAULT true,
  is_vibrate BOOLEAN NOT NULL DEFAULT true,
  is_jit_mode BOOLEAN NOT NULL DEFAULT false,
  days_of_week JSONB DEFAULT '[false,true,true,true,true,true,false]',
  ringtone_title TEXT DEFAULT 'Default Alarm Ringtone',
  ringtone_uri TEXT DEFAULT '',
  created_by TEXT DEFAULT 'device',
  status TEXT DEFAULT 'active',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(device_id, alarm_id)
);
```

### 3.6 `public.app_releases` (`src/db.js` lines 55–68)
Stores OTA APK releases:
`id`, `version_name`, `version_code`, `release_notes`, `file_name`, `file_path`, `b2_key`, `file_size`, `is_active`, `created_at`.

---

## 4. Exact Endpoint Contracts

### 4.1 Health Ping
- **Endpoint**: `GET /api/health` (or `/health`)
- **Auth**: None
- **Response**: `{ "ok": true, ... }` (HTTP 200 confirms connectivity in `ApiClient.ping()`).

### 4.2 Register / Refresh Device
- **Endpoint**: `POST /api/devices/register`
- **Auth**: None (uses `pg_advisory_xact_lock` on `zez-device:<androidId>:<persistentId>`)
- **Request**:
```json
{
  "ids": {
    "androidId": "9f8e7d6c5b4a3210",
    "persistentId": "c1f2e3d4-..."
  },
  "deviceName": "Pixel 7",
  "deviceModel": "Pixel 7",
  "manufacturer": "Google",
  "brand": "google",
  "platform": "Android",
  "osVersion": "14",
  "appVersion": "1.0.0",
  "deviceDetails": {
    "device": {},
    "operatingSystem": {},
    "application": {},
    "storage": {},
    "memory": {},
    "network": {}
  }
}
```
- **Response (`200 OK`)**:
```json
{
  "success": true,
  "deviceId": "uuid-of-device",
  "token": "base64url-32-byte-secret",
  "androidId": "9f8e7d6c5b4a3210",
  "persistentId": "c1f2e3d4-..."
}
```

### 4.3 Sync Cellular Call Logs
- **Endpoint**: `POST /api/calls/sync`
- **Auth**: `Bearer <token>` (`deviceAuth`)
- **Limits**: Server `MAX_BATCH_SIZE = 500`, `MAX_DEVICE_DETAILS_BYTES = 100000`. Client chunks at **100** per POST; server inserts in SQL chunks of **50**.
- **Request**:
```json
{
  "calls": [
    {
      "externalId": "android:482",
      "phoneNumber": "+919876543210",
      "name": "John Doe",
      "type": "INCOMING",
      "timestamp": 1795634000000,
      "duration": 95
    }
  ],
  "deviceDetails": { }
}
```
- **Response (`200 OK`)**:
```json
{
  "success": true,
  "received": 100,
  "inserted": 4,
  "duplicates": 96,
  "acceptedIds": ["android:482", "..."]
}
```

### 4.4 Sync Notifications
- **Endpoint**: `POST /api/devices/notifications/sync`
- **Auth**: `Bearer <token>` (`deviceAuth`)
- **Request**:
```json
{
  "notifications": [
    {
      "packageName": "com.whatsapp",
      "appName": "WhatsApp",
      "title": "Alice",
      "content": "Hey, call me back",
      "subText": "2 messages",
      "postTime": 1795634123456
    }
  ]
}
```
- **Response (`200 OK`)**: `{ "success": true, "count": 1, "totalReceived": 1 }`.

### 4.5 Sync Third-Party App Calls
- **Endpoint**: `POST /api/devices/app-calls/sync`
- **Auth**: `Bearer <token>` (`deviceAuth`)
- **Request**:
```json
{
  "appCalls": [
    {
      "appName": "WhatsApp",
      "packageName": "com.whatsapp",
      "callerName": "Alice",
      "callType": "video",
      "callDirection": "incoming",
      "durationSeconds": 0,
      "callTime": 1795634125000,
      "rawDetails": "Incoming video call"
    }
  ]
}
```
- **Response (`200 OK`)**: `{ "success": true, "count": 1, "totalReceived": 1 }`.

### 4.6 Bidirectional Alarms Sync
- **Endpoint**: `POST /api/devices/alarms/sync`
- **Auth**: `Bearer <token>` (`deviceAuth`)
- **Request**:
```json
{
  "alarms": [
    {
      "id": 1732590001000,
      "hour": 7,
      "minute": 30,
      "label": "Morning",
      "isEnabled": true,
      "isVibrate": true,
      "isJitMode": false,
      "daysOfWeek": [false, true, true, true, true, true, false],
      "ringtoneTitle": "Default Alarm Ringtone",
      "ringtoneUri": ""
    }
  ],
  "deletedAlarmIds": [1732580000000]
}
```
- **Response (`200 OK`)**:
```json
{
  "success": true,
  "activeAlarms": [
    {
      "alarm_id": 1732590001000,
      "hour": 7,
      "minute": 30,
      "label": "Morning",
      "is_enabled": true,
      "is_vibrate": true,
      "is_jit_mode": false,
      "days_of_week": [false, true, true, true, true, true, false],
      "ringtone_title": "Default Alarm Ringtone",
      "ringtone_uri": "",
      "created_by": "admin",
      "status": "active"
    }
  ],
  "deletedAlarmIds": [1732580000000]
}
```
> Important sync behavior: Server updates existing rows **only** `WHERE device_alarms.status != 'deleted'`. If an admin deleted an alarm on the dashboard, `deletedAlarmIds` instructs `AlarmHelper` to remove it locally too.

### 4.7 YMusic Backend Proxy Endpoints (`routes/music.js`)
Fallback used by `YouTubeApiHelper` if direct client-side Innertube extraction is blocked/throttled:
- `GET /api/music/resolve?videoId=<id>` -> `{ success: true, audioUrl, title, author, lengthSeconds, source }`
- `GET /api/music/stream?videoId=<id>` -> HTTP `206` / `200` piped audio stream (`Range` header forwarded)
- `GET /api/music/download?videoId=<id>&title=<title>` -> Piped attachment download stream

### 4.8 OTA Latest Release (`routes/appRelease.js`)
- `GET /api/app/latest` ->
```json
{
  "success": true,
  "hasRelease": true,
  "id": 12,
  "versionName": "1.0.61",
  "versionCode": 61,
  "releaseNotes": "...",
  "fileName": "Quacky-v1.0.61.apk",
  "fileSize": 18450210,
  "downloadUrl": "/api/app/download/12",
  "createdAt": "..."
}
```
> **CRITICAL MULTI-APP WARNING**: Today `GET /api/app/latest` returns the single latest active row in `public.app_releases` (which currently stores **Quacky** APKs!).
> If both **Quacky (`com.zez.app`)** and **Companion (`com.pulse.companion`)** point to the same `Zez-Backend`, tapping `"Check for Updates"` inside the Companion App would fetch Quacky's APK and fail Android's package signature check (or try installing Quacky)!
> **Required Small Backend Addition when ready to ship OTA for Companion**:
> 1. Add `app_id TEXT NOT NULL DEFAULT 'zez'` column to `public.app_releases` (or query param `?appId=companion`).
> 2. Update `GET /api/app/latest?appId=companion` (filtering `WHERE app_id = COALESCE($1, 'zez') AND is_active = true`). Existing Quacky clients call `GET /api/app/latest` with no param and keep getting `'zez'` releases untouched!
> 3. In Companion App's `AppUpdateHelper`, call `GET /api/app/latest?appId=companion`.
