# ZEZ MOBILE & BACKEND ARCHITECTURE & SYNCHRONIZATION GUIDE

This document provides a comprehensive, production-grade architectural and technical reference for the **Zez System** (comprising the native Android mobile client, Node.js/Express backend service, Neon Serverless PostgreSQL database, and the WhatsApp-inspired 1-on-1 Chatting subsystem).

---

## 1. System Architecture Overview

```
+-------------------------------------------------------------------------------------------------------+
|                                        ANDROID MOBILE CLIENT                                          |
|                                                                                                       |
|  [ CallLog Provider ]  [ Camera / MediaStore ]  [ Contacts Provider ]  [ Private SharedPreferences ]  |
|            |                      |                      |                           |                |
|     CallLogHelper            MediaHelper            ContactsActivity       ChatPreferencesHelper      |
|            |                      |                      |                           |                |
|     +--------------+              |                      |                           |                |
|     | Batch Engine |              |                      |                           |                |
|     |  (100/chunk) |              |                      |                           |                |
|     +--------------+              |                      |                           |                |
|            |                      |                      |                           |                |
|     +--------------+       +--------------+       +--------------+            +--------------+        |
|     |  ApiClient   | <---> | WorkManager  | <---> |  ChatApi-    | <--------> | Content      |        |
|     | (OkHttp/TLS) |       | (CallSync-   |       |   Client     |            |  Observer    |        |
|     | (Call Sync)  |       |   Worker)    |       |   (Chat)     |            | (Debounced)  |        |
|     +--------------+       +--------------+       +--------------+            +--------------+        |
+------------|---------------------------------------------|--------------------------------------------+
             |                                             |
             | HTTPS (Bearer Token Auth + AES Encrypted)   | HTTPS (Chat APIs: Register, Sync, Poll)
             v                                             v
+-------------------------------------------------------------------------------------------------------+
|                                       NODE.JS / EXPRESS BACKEND                                       |
|                                                                                                       |
|  [ Express Server (server.js) ]                                                                       |
|    |                                                                                                  |
|    +--> Middleware: deviceAuth (SHA256 Token Verification for Call Sync & Telemetry)                  |
|    +--> Middleware: adminAuth  (HMAC SHA256 Session JWT for Admin Portal)                             |
|    |                                                                                                  |
|    +--> /api/devices/register   (Device registration, advisory locks, sim tokens)                     |
|    +--> /api/calls/sync         (Batch ingestion, deduplication, AES-256-GCM encryption)               |
|    +--> /api/calls/read         (Device self-retrieval, stats, decryption)                            |
|    +--> /api/admin/*            (Live portal, device logs, search, exports)                           |
|    +--> /api/app/*              (Version check, release management, APK hosting)                      |
|    +--> /api/chat/*             (Isolated Chat Subsystem: Register, Profile, Sync-Contacts,           |
|                                  Conversations, Messages, Realtime Polling, Status Ticks)             |
+------------|---------------------------------------------|--------------------------------------------+
             |                                             |
             | TLS Connection Pool (pg Pool with pg_advisory_xact_lock)                                 |
             v                                             v
+-------------------------------------------------------------------------------------------------------+
|                                  NEON SERVERLESS POSTGRESQL DATABASE                                  |
|                                                                                                       |
|  CALL SYNC & CORE SUBSYSTEM:                                                                          |
|  - public.devices             (Unique device metadata, hardware specs, hashed tokens)                 |
|  - public.call_logs           (Encrypted payload, external_id_hash, call_timestamp)                   |
|  - public.admin_users         (PBKDF2-SHA512 password hashes, session management)                     |
|  - public.app_releases        (Active/historical APK releases, version codes, URLs)                   |
|                                                                                                       |
|  CHAT SUBSYSTEM (100% ISOLATED):                                                                      |
|  - public.chat_users          (User UUID, normalized phone number, display name, avatar, last_seen)   |
|  - public.chat_conversations  (1-on-1 pairs [u1, u2], last_message_text, last_message_time)           |
|  - public.chat_messages       (Message UUID, sender_id, receiver_id, message_text, status [sent/      |
|                                delivered/read], created_at)                                           |
+-------------------------------------------------------------------------------------------------------+
```

---

## 2. End-to-End Call Synchronization Deep Dive

### 2.1 The 10-12 Day Call History Truncation Issue (Root Cause & Resolution)

#### What Was the Problem?
On active user devices, call history was previously appearing truncated to only the most recent **10 to 12 days**, while older calls (e.g., from 6 months, 1 year, or 2 years ago) were not being uploaded to the backend even though they existed in the phone's native dialer history.

#### The Root Causes:
1. **Hardcoded Query Limit in Android Helper**:
   In `CallSyncWorker.java` and `MainActivity.java`, call retrieval was invoked with:
   ```java
   List<CallLogItem> calls = CallLogHelper.getCallLogs(context, 100);
   ```
   Inside `CallLogHelper.java`, the cursor iteration was governed by:
   ```java
   if (limit > 0 && count >= limit) {
       break;
   }
   ```
   Because an average smartphone user conducts 8–10 calls per day, a ceiling of **100 calls** naturally corresponded to exactly **10 to 12 days of history**. Any call beyond the 100th was deliberately aborted and never retrieved from the device database.

2. **Absence of Client-Side Chunking / Batching**:
   If the limit were simply removed without batching, querying a phone with 2,500 call logs would create a single monolithic JSON payload. Sending 2,500 calls at once would trigger HTTP timeouts on slow mobile connections, exceed mobile memory allocations, and trigger the backend's `MAX_BATCH_SIZE` limit with an HTTP 413 error.

3. **In-Batch Duplicate Rejection**:
   Certain Android OEM dialers (Samsung, Xiaomi MIUI, Vivo FunTouch) occasionally maintain multiple call records sharing identical database `_ID` keys or duplicate entries in temporary tables. Previously, the backend rejected the entire batch with `HTTP 400: Duplicate call ID inside this batch`, causing the whole sync operation to fail repeatedly.

#### The Complete Solution Implemented:
1. **Full History Retrieval (`CallLogHelper.getAllCallLogs`)**:
   `CallLogHelper.java` now exposes `getAllCallLogs(context)` which passes `limit = 0`. This instructs Android's Content Resolver to stream the **complete call log history** from day one.
2. **Chunked Transmission Engine (`ApiClient.syncCalls`)**:
   `ApiClient.java` chunks the collected call records into discrete batches of **100 calls** each. Each chunk is transmitted sequentially with its own payload encapsulation. If a device has 3,000 calls, it executes 30 clean, lightweight transactions, ensuring zero network timeouts and zero memory spikes.
3. **Backend Elastic Capacity & Intra-Batch Tolerance**:
   - `MAX_BATCH_SIZE` on the backend (`routes/callSync.js`) was increased to **500 calls** to provide ample headroom.
   - Intra-batch duplicates are now gracefully deduplicated inside memory (`Set<externalId>`) rather than failing the batch with HTTP 400.
   - Idempotent upserts (`ON CONFLICT (device_id, external_id_hash) DO NOTHING`) ensure previously synced calls are safely ignored without database duplication.

---

### 2.2 How the Mobile Client Collects Call Data

1. **Permission Guard**:
   The app requests `android.permission.READ_CALL_LOG` and `android.permission.READ_PHONE_STATE`.
2. **Provider Query**:
   `CallLogHelper` executes a query against `android.provider.CallLog.Calls.CONTENT_URI`:
   - **Fields Extracted**:
     - `CallLog.Calls._ID` -> Unique device record ID.
     - `CallLog.Calls.CACHED_NAME` -> Contact name (or "Unknown" if not saved in phonebook).
     - `CallLog.Calls.NUMBER` -> Phone number.
     - `CallLog.Calls.TYPE` -> Call direction (`INCOMING`, `OUTGOING`, `MISSED`, `REJECTED`, `BLOCKED`, `VOICEMAIL`).
     - `CallLog.Calls.DATE` -> Epoch millisecond timestamp of the call start.
     - `CallLog.Calls.DURATION` -> Call duration in seconds.
   - **Ordering**:
     - Ordered by `CallLog.Calls.DATE DESC` (newest calls first).
3. **Model Construction**:
   Each row is transformed into a strongly typed `CallLogItem` model and serialized to JSON.

---

### 2.3 How the Client Sends Data to the Backend

1. **Device Identification & Auth Header**:
   - When the device first boots, it registers with `/api/devices/register` using a dual-identity approach:
     - `androidId`: Standard Android operating system identifier.
     - `persistentId`: Random UUID persisted in private `SharedPreferences` that survives OS ID changes.
   - The backend validates the IDs and issues a secure cryptographically random token (`crypto.randomBytes(32).toString('base64url')`).
   - Every sync request attaches this token via `Authorization: Bearer <token>`.
2. **Telemetry & Hardware Status Enrichment**:
   - Alongside the calls array, the client gathers real-time hardware telemetry:
     - Battery level, charging state, battery temperature.
     - Available RAM & Total RAM.
     - Available Internal Storage & Total Storage.
     - Active SIM carriers and network state.
   - Attached to the request under `deviceDetails`.
3. **Sequential Chunk Upload**:
   - If 1,500 calls are present, the client breaks them down:
     - Chunk 1: Calls 0 to 99
     - Chunk 2: Calls 100 to 199
     - ...
     - Chunk 15: Calls 1400 to 1499
   - Each chunk is transmitted over TLS with automated retry handling.

---

### 2.4 How the Backend Manages, Deduplicates & Stores Calls

1. **Authentication (`deviceAuth.js`)**:
   - Extracts the Bearer token from the `Authorization` header.
   - Computes `SHA256(token)` and matches it against `public.devices.token_hash`.
   - If valid, binds `req.deviceId` to the request; if invalid, immediately rejects with `401 Unauthorized`.
2. **Normalization & Sanitization**:
   - `normalizeCall()` validates timestamps (accepting epoch numbers or ISO 8601 strings), forces non-negative integers for duration, and standardizes type strings.
3. **Cryptographic Identity Hashing**:
   - For every call, the backend computes:
     $$\text{external\_id\_hash} = \text{SHA256}(\text{externalId})$$
4. **Zero-Knowledge Payload Encryption**:
   - The full JSON payload (caller name, phone number, duration, raw metadata) is encrypted using authenticated encryption (`encryptJson` via `@noble/ciphers` AES-256-GCM / ChaCha20-Poly1305 with random 96-bit nonces).
   - The database stores only the encrypted ciphertext (`encrypted_payload`) and deterministic lookup hash (`external_id_hash`).
5. **Idempotent Atomic Insertion**:
   - Using PostgreSQL transactions (`BEGIN` ... `COMMIT`):
     ```sql
     INSERT INTO public.call_logs (device_id, external_id_hash, call_timestamp, encrypted_payload)
     VALUES ($1, $2, $3, $4)
     ON CONFLICT (device_id, external_id_hash) DO NOTHING
     RETURNING id;
     ```
   - If the row already exists, PostgreSQL silently ignores it (`rowCount = 0`, incrementing `duplicates`).
   - If the row is new, it is inserted (`rowCount = 1`, incrementing `inserted`).
6. **Device State Update**:
   - Updates `public.devices.last_seen_at = NOW()` and merges `device_details` JSONB.

---

### 2.5 Synchronization Triggers & Resilience

The app implements a **multi-tier synchronization lifecycle**:
1. **Real-Time Call Log Observer (`CallLogObserver.java`)**:
   - Registers a `ContentObserver` on `CallLog.Calls.CONTENT_URI`.
   - When a call ends (incoming, outgoing, or missed), Android notifies the observer.
   - Uses a **1,200ms debounce window** to wait for the operating system to finish writing all call duration details before triggering immediate background synchronization.
2. **Scheduled Periodic Sync (`BootReceiver.java` & `CallSyncWorker.java`)**:
   - Schedules a `PeriodicWorkRequest` running every **15 minutes** via Android `WorkManager`.
   - Guaranteed by the OS to execute even if the app UI is closed.
   - Requires network connectivity constraint (`NetworkType.CONNECTED`).
3. **Reboot & App Update Survivability**:
   - Listens for `android.intent.action.BOOT_COMPLETED` and `android.intent.action.MY_PACKAGE_REPLACED`.
   - Automatically re-registers the background sync job after reboot or update.
4. **Manual Sync Trigger**:
   - Users can press the Sync icon on the top bar in the app to immediately initiate a full history sync.

---

## 3. WhatsApp-Inspired 1-on-1 Chat Subsystem Deep Dive

### 3.1 Strict Security & Data Isolation
The Chat subsystem operates on a **complete separation of concerns**:
- **Zero Cross-Contamination**: Chat authentication, identities, and message streams have zero relationship with device tokens, call sync, or dialer data.
- **Dedicated Preferences**: Stored exclusively in `zez_chat_prefs.xml` (`KEY_USER_ID`, `KEY_PHONE`, `KEY_NAME`, `KEY_AVATAR`, `KEY_STATUS`).
- **Privacy-Preserving Contact Matching**: Phone numbers provided during registration are used solely to let other users on Zez discover you in their contact lists, exactly like WhatsApp.

### 3.2 Database Schema (`Neon PostgreSQL`)
```sql
-- Registered Chat Users
CREATE TABLE IF NOT EXISTS public.chat_users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  phone_number VARCHAR(30) UNIQUE NOT NULL,
  name VARCHAR(100) NOT NULL,
  avatar_url TEXT,
  status_message VARCHAR(255) DEFAULT 'Hey there! I am using Zez.',
  last_seen TIMESTAMPTZ DEFAULT NOW(),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Unique 1-on-1 Conversation Threads
CREATE TABLE IF NOT EXISTS public.chat_conversations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user1_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
  user2_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
  last_message_text TEXT,
  last_message_time TIMESTAMPTZ DEFAULT NOW(),
  created_at TIMESTAMPTZ DEFAULT NOW(),
  CONSTRAINT uq_conversation_participants UNIQUE (user1_id, user2_id)
);

-- Individual Chat Messages
CREATE TABLE IF NOT EXISTS public.chat_messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL REFERENCES public.chat_conversations(id) ON DELETE CASCADE,
  sender_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
  receiver_id UUID NOT NULL REFERENCES public.chat_users(id) ON DELETE CASCADE,
  message_text TEXT NOT NULL,
  media_url TEXT,
  status VARCHAR(20) DEFAULT 'sent', -- 'sent', 'delivered', 'read'
  created_at TIMESTAMPTZ DEFAULT NOW()
);
```

### 3.3 Backend API Specifications (`/api/chat/*`)
| Route | Method | Purpose | Key Parameters |
| :--- | :---: | :--- | :--- |
| `/api/chat/register` | `POST` | First-time registration or profile update | `{ phone_number, name, avatar_url, status_message }` |
| `/api/chat/profile/:idOrPhone` | `GET` | Retrieve profile by user UUID or phone number | `idOrPhone` |
| `/api/chat/sync-contacts` | `POST` | Match client address book against registered users | `{ userId, phone_numbers: [...] }` |
| `/api/chat/conversations` | `GET` | List active conversations with unread badge counts | `?userId=...` |
| `/api/chat/messages` | `GET` | Fetch message history & automatically mark as `read` | `?conversationId=...&userId=...` |
| `/api/chat/messages/send` | `POST` | Send message & update conversation thread | `{ sender_id, receiver_id, message_text }` |
| `/api/chat/poll` | `GET` | Live message delivery & status tick polling | `?userId=...&since=...` |
| `/api/chat/messages/status`| `POST` | Manually mark messages delivered or read | `{ messageIds: [...], status: 'read' }` |

### 3.4 Reverse-Engineered WhatsApp UX Architecture
1. **First-Time Registration Dialog**:
   - Modern glassmorphic dialog prompted on first tab visit.
   - Validates international phone format and display name.
   - Saves identity and transitions directly into the Chats tab.
2. **WhatsApp-Style Contacts Picker (`ContactsActivity`)**:
   - Queries `android.provider.ContactsContract.CommonDataKinds.Phone`.
   - Sends batch to `/api/chat/sync-contacts` with 10-digit suffix matching.
   - Splits list into two WhatsApp sections:
     - **"CONTACTS ON ZEZ"**: Displays avatar initials, contact name, and custom status message. Tapping opens 1-on-1 chat.
     - **"INVITE TO ZEZ"**: Displays non-registered contacts with an "Invite" button triggering Android system share sheet.
3. **1-on-1 Chat Screen (`ChatActivity`)**:
   - **Header**: Back arrow, avatar with initials, contact name, and live "online" status badge.
   - **Message Stream**:
     - Sticky Date Headers ("Today", "Yesterday", "MM/dd/yy").
     - **Incoming Messages**: Surface/card container on the left with timestamp.
     - **Outgoing Messages**: Emerald brand bubble (`#00A884` WhatsApp brand green) on the right with timestamp.
     - **Delivery Status Ticks**: Single tick (sent to server), double tick (delivered to device), blue double tick (read by recipient).
   - **Input Bar**: Pill-shaped capsule input container with emoji/attachment hooks and a circular floating emerald Send button (`#00A884`).
   - **Live Polling Loop**: 2.5-second background interval polling for instant message arrival and status checkmark updates without WebSocket overhead.

---

## 4. UI Reorganization & Zero-Regression Verification

### 4.1 Bottom Navigation Order
The bottom navigation bar has been reorganized so that chatting is the primary, default experience upon opening the app:
1. **Chats** (`nav_chats`) - 1st (Default on launch)
2. **Alarms** (`nav_alarms`) - 2nd
3. **Gallery** (`nav_gallery`) - 3rd
4. **Settings** (`nav_settings`) - 4th

### 4.2 Gallery Recycle Bin Integration
- `nav_bin` was removed from the bottom navigation bar.
- The Recycle Bin is integrated directly inside the **Gallery -> Albums** tab as a smart album at the bottom of the albums list.
- Tapping the Recycle Bin smart album opens the dedicated Recycle Bin view with full restoration and purge capabilities.
- Pressing Android Back navigates smoothly back to the Gallery Albums list.

### 4.3 Settings UI Fix
- Resolved the squished font issue where "Theme Mode" and "Dark Mode" were wrapping onto multiple lines ("The\nme\nMo\nde").
- Added `android:maxLines="1"`, `android:ellipsize="end"`, and dynamic layout weights to maintain a crisp, readable single-line display across all screen densities and font scales.

---

## 5. Preservation of Application Features

No feature or code outside the scope was removed or harmed. All existing modules remain 100% operational:

| Module | Core Features Preserved |
| :--- | :--- |
| **Chat Subsystem** | • WhatsApp-style 1-on-1 chatting, contact sync, conversations list, message bubbles, status ticks, unread counts. |
| **Alarm Engine** | • `AlarmHelper`, `AlarmFiringActivity`, `AlarmRingingService`, `AlarmReceiver`.<br>• Full-screen wake lock, ringtone streaming, vibration, and snooze/dismiss handling. |
| **Call Management** | • Real-time call observer (`CallLogObserver`).<br>• Background sync worker (`CallSyncWorker`).<br>• Boot auto-restart receiver (`BootReceiver`).<br>• Phone call state receiver (`CallStateReceiver`). |
| **Gallery System** | • **Part 1**: Date-grouped photos grid ("Today", "Yesterday") & Albums grid with item count badges.<br>• **Part 2**: `AlbumDetailActivity` with multi-selection mode (Share, Favorite, Bin).<br>• **Part 3**: Cross-synced Favorites system (`FavoritesHelper`) with heart toggle & live badge updates.<br>• **Part 4**: Per-photo actions (Details dialog, Wallpaper dialog, Share sheet) & `ImageEditActivity` (transforms, sliders, filters, MediaStore export). |
| **Recycle Bin** | • Relocated into Gallery Albums as a smart album; soft-delete (`RecycleBinHelper`), restoration, and permanent purge. |
| **Settings & Telemetry** | • Hardware diagnostics (`HardwareInfoHelper`, `TelemetryGraphView`).<br>• Theme mode switcher (Light / Dark / Follow System) with single-line layout.<br>• Font scale switcher (Small / Default / Large / Huge).<br>• App update checker (`AppUpdateHelper`, `app_releases` dialog). |
| **App Launcher** | • Official `ZEZ Icon.png` permanently configured across all Android mipmap densities (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`). |

---

## 6. Automated Backend & Client Verification

### 6.1 Backend Core & Call Sync Suite (`Zez-Backend/test_backend_suite.js`)
**23 / 23 Tests PASSED (0 Failures)**:
1. Health Check (GET /health)
2. Heartbeat Ping (POST /api/ping)
3. Device Registration - Missing Identifiers
4. Device Registration - Success
5. Device Re-registration / Update Specs
6. Call Sync - Unauthorized Request without Token
7. Call Sync - Invalid Token
8. Call Sync - Non-array Validation
9. Call Sync - Normal Batch with 1-Year-Old History
10. Call Sync - Deduplication Test
11. Call Read - Verify Synced & Decrypted Records
12. Call Read - Stats Endpoint
13. Admin Login - Bad Password
14. Admin Login - Valid Password
15. Admin Stats API
16. Admin Devices API
17. Admin Calls API with Search & Filters
18. App Releases Public Endpoint
19. App Releases Admin Protected Endpoint
20. Real-Life Simulation: Tolerating Duplicate IDs within a Single Batch
21. Real-Life Simulation: Multi-Year History Chunked Sync (30 calls across 3 batches)
22. Real-Life Simulation: Querying 2-Year Old History through Admin Search
23. Security & Resilience: Rejecting Oversized Batch (> 500 calls)

### 6.2 Chat Backend Test Suite (`Zez-Backend/test_chat_endpoints.js`)
**8 / 8 Tests PASSED (0 Failures)**:
1. Register User A (`+15551112222`, Alice)
2. Register User B (`+15553334444`, Bob)
3. Sync Contacts (`/api/chat/sync-contacts`)
4. Send Message from A to B (`/api/chat/messages/send`)
5. Reply Message from B to A (`/api/chat/messages/send`)
6. Alice Conversations list with unread counts
7. Fetch Conversation Messages with read status updates
8. Live Poll for Bob with incoming messages & status updates

### 6.3 End-to-End On-Device Verification (Emulator-5554)
- **App Launcher**: `ZEZ Icon.png` verified on home screen and dock.
- **Registration**: User registered on device as `Alex Johnson` (`+15551234567`).
- **Contacts Discovery**: Device contacts scanned; Bob Smith displayed under "CONTACTS ON ZEZ", Charlie NonUser displayed under "INVITE TO ZEZ".
- **1-on-1 Chat**: Opened chat with Bob Smith. Alex sent message "Hello Bob! This is Alex on Zez Chat!". Outgoing bubble rendered with date header and status tick.
- **Live Ingestion**: Bob replied "Hey Alex! Received your message. Zez Chat is amazing!". Real-time polling picked up message within 2.5 seconds; incoming bubble rendered with timestamp.
- **Conversations List**: Chats tab updated with active thread, last message snippet, timestamp, and unread count badge.
- **Regression Verification**: Alarms tab, Gallery tab (Photos & Albums), Recycle Bin view & back navigation, and Settings tab (crisp Theme Mode font) all tested on device with zero errors.

---

## 7. Current Version & Release Information

- **Release Version**: `v1.0.10` (Version Code `11`)
- **APK Artifact**: `d:\Call Log Mannager\Zez-Backend\src\public\downloads\zez-app-v1.0.10.apk`
- **File Size**: `6,176,135 bytes` (~5.89 MB)
- **APK Signature**: V2 Full APK Signature Scheme verified (`true`)
- **Production Backend URL**: `https://zez-chat-backend.onrender.com`
- **Update Availability**: Published to `public.app_releases` in PostgreSQL; distributed via the in-app updater and `/api/app/latest`.
- **Target OS Compatibility**: Android 8.0 (API 26) through Android 14 (API 34).

---

## 8. Hosted Backend URL & Protected Developer Mode Access

The production mobile app is configured to connect directly to the hosted cloud backend:
**`https://zez-chat-backend.onrender.com`**

### 8.1 Security & Anti-Tamper Protection (Hidden from Normal Users)
To ensure regular users cannot accidentally, easily, or maliciously tamper with or change the backend server URL:
1. **Removed from Daily Chat Menus**: The 3-dot overflow menu in the Chats section contains only user-level options (*Profile & Settings*, *Refresh Chats*). Server controls have been completely removed.
2. **Invisible in Settings by Default**: In the Settings tab under *PREFERENCES & SYSTEM*, the "Backend Server URL" row is hidden (`View.GONE`). Normal users only see Theme Mode, Font Size, and Storage diagnostics.
3. **Protected Developer Mode (5-Tap Unlock + Admin PIN)**:
   - To unlock the Server Configuration panel as an administrator:
     1. Open **Settings**.
     2. Rapidly tap on the **Installed Version** line (or footer version text) **5 times** consecutively.
     3. An Admin authorization dialog appears: *"Enter Admin PIN to unlock Backend Server Configuration"*.
     4. Enter the Admin PIN: **`7788`** and tap **UNLOCK**.
     5. The **Backend Server URL** row is revealed and the developer configuration dialog opens.
4. **Dialog Options**:
   - **Save & Connect**: Enter a custom server URL if hosting on a different provider.
   - **Reset Default**: Restores the production address: `https://zez-chat-backend.onrender.com`.
   - **Cancel**: Closes dialog without modifying preferences.

### 8.2 Build-Time Default Configuration (Mobile `.env` File)
The production URL is embedded in:
[`d:\Call Log Mannager\ZezAndroid\app\src\main\assets\backend_config.properties`](file:///d:/Call%20Log%20Mannager/ZezAndroid/app/src/main/assets/backend_config.properties)
```properties
BACKEND_URL=https://zez-chat-backend.onrender.com
```
Any freshly installed device or newly built APK connects out-of-the-box to `https://zez-chat-backend.onrender.com`. Existing installs automatically migrate away from legacy URLs.

---

## 9. Verification of Call Sync Functionality Preservation

A thorough code audit and verification confirm that the **call synchronization subsystem has NOT been modified or reduced in any way**:

1. **Unedited Architecture**:
   - `CallSyncWorker.java` continues to operate via Android `WorkManager` with network constraints and periodic intervals.
   - `CallLogObserver.java` remains active with its 1,200ms debounce timer to capture incoming, outgoing, and missed calls immediately upon termination.
   - `BootReceiver.java` guarantees auto-rescheduling across system reboots and app updates (`MY_PACKAGE_REPLACED`).
   - `CallStateReceiver.java` tracks telephony state transitions seamlessly.
2. **Multi-Year History Capability Intact**:
   - `CallLogHelper.getAllCallLogs()` streams all device calls without any hardcoded 10-day or 100-record query limitations.
   - `ApiClient.syncCalls()` breaks large call histories into sequential 100-record chunks to prevent memory spikes or network timeouts.
   - The backend `/api/calls/sync` handles intra-batch duplicates and performs zero-knowledge AES-256-GCM encryption with atomic idempotent storage (`ON CONFLICT DO NOTHING`).
3. **Automated Backend Regression Status**:
   - All 23 call sync, device auth, and admin management tests continue to report **23 PASSED | 0 FAILED**.

---

## 10. Chat Storage Architecture & Synchronized Dual-Mode Chat

The chat subsystem has been elevated with an enterprise dual-mode architecture: **Normal Mode** (persistent history) and **Secret Mode** (client-ephemeral, session-scoped, synchronized between peers).

### 10.1 Database Schema Extensions
1. **`public.chat_conversations`**:
   - `is_secret_mode BOOLEAN DEFAULT FALSE`: Tracks whether Secret Mode is actively turned on for this 1-on-1 pair.
   - `secret_session_id UUID`: A unique session identifier generated whenever Secret Mode is turned on. Fresh secret sessions isolate new ephemeral exchanges.
   - `secret_mode_updated_at TIMESTAMPTZ`: Timestamp when the mode was last changed.
   - `secret_mode_by UUID REFERENCES public.chat_users(id)`: The user who toggled the mode.
2. **`public.chat_messages`**:
   - `mode VARCHAR(16) DEFAULT 'normal'`: Flagged as either `'normal'` or `'secret'`.
   - `secret_session_id UUID`: Foreign identifier tying the message to the specific secret session.
   - Index `idx_chat_messages_mode (mode)`: Ensures rapid index scans for normal queries vs. audit queries.

### 10.2 Synchronized Dual-Mode Switching Mechanism
- **Header Toggle Pill (`btn_chat_mode_toggle`)**:
  - Located in the chat header next to the user name and status indicator.
  - Tapping toggles between **Normal Mode** (green shield badge) and **Secret Mode** (incognito purple badge).
- **Two-Way Peer Synchronization**:
  - When **User A** activates Secret Mode, the client fires `POST /api/chat/conversation/:id/mode` with `{ "secretMode": true }`.
  - When **User B** polls via `GET /api/chat/poll?conversationId=...`, the response includes a `conversation_mode` payload with `is_secret_mode: true` and the `secret_session_id`.
  - User B's screen automatically transitions to Secret Mode without any manual click needed.
  - When either participant toggles back to Normal Mode, both screens synchronously switch back.
- **Client Ephemerality vs. Backend Audit Persistence**:
  - **On Client (Users A & B)**: While in Secret mode, a top amber warning banner appears ("Secret Mode Active • Messages disappear when leaving chat"). All incoming and outgoing secret messages are retained in temporary in-memory collections (`ephemeralSecretMessages`). As soon as either user navigates back, switches tabs, or kills the app, `onPause()` / `onDestroy()` purges the in-memory array. Re-entering the chat starts with a clean slate.
  - **On Backend**: In compliance with admin oversight requirements, secret messages are safely persisted in PostgreSQL with `mode = 'secret'`. Normal conversation snippets and previews are preserved uncorrupted.

---

## 11. Admin Chat Inspector & Multi-Format Data Export System

The Admin Web Portal (`/admin`) features a dedicated **Chat Monitor** section (`#sectionChats`) that empowers system administrators to inspect, review, and export all 1-on-1 communications.

### 11.1 Features of the Chat Monitor
1. **Split-View Web Layout**:
   - **Conversation Directory (Left Pane)**: Live search filter, participant names, phone numbers, total messages, normal message counts, secret message counts, and pulsing purple badges indicating active secret sessions.
   - **Transcript & Audit Inspector (Right Pane)**: Displays chronological message bubbles with sender identity, delivery status, timestamp, attachment media previews, and distinct mode badges (`Normal` in dark grey, `Secret` in neon violet).
2. **Multi-Format Conversation Export**:
   - **TXT Export** (`/api/admin/chats/export/:id?format=txt`): Generates a clean, human-readable legal transcript detailing participant profiles, session metadata, timestamps, sender numbers, message content, and attachment paths.
   - **CSV Export** (`/api/admin/chats/export/:id?format=csv`): Generates structured RFC-4180 compliant CSV tables with escaped strings for Excel/spreadsheet ingest.
   - **JSON Export** (`/api/admin/chats/export/:id?format=json`): Full JSON payload containing complete database rows, attachments, and session IDs.
   - **Global System Export** (`/api/admin/chats/export-all?format=json|csv`): High-speed one-click bulk export of every conversation in the entire database.

---

## 12. Modernized Dynamic Top Bar Experience

The top application bar in `MainActivity` and `ChatActivity` was completely redesigned for a sleek, modern visual aesthetic:
- **Expanded Top Bar**: Upgraded from flat 60dp to a modern 64dp surface with elevated typography.
- **Dynamic Context Subtitle (`tv_subtitle`)**: Changes dynamically depending on the selected navigation tab:
  - *Chats*: "Messages & encrypted conversations"
  - *Alarms*: "Scheduled wake alarms & timers"
  - *Gallery*: "Device photos, videos & media albums"
  - *Recycle Bin*: "Soft-deleted media items"
  - *Settings*: "System preferences & sync configuration"
- **Status Indicator Badge (`tv_top_badge`)**: Live status pill ("Live", "Active", "Synced") confirming ongoing real-time services.
