# TWING Native Call Recording Integration: User Guide & Troubleshooting

**Version:** 1.0.31 (Build 32)  
**Applicability:** Vivo, OnePlus, Samsung, Xiaomi, Google Phone, and all Android devices running Android 11 to Android 17.

---

## 1. Quick Start Setup (One-Time Configuration)

To automatically record all cellular calls with full two-way audio (both your voice and the other party's voice) and have them organized in TWING, follow these two simple steps:

### Step 1: Enable Automatic Call Recording in your Phone's Dialer
Your phone dialer has hardware-level access to record calls with crystal clarity. Make sure it is enabled:

#### Vivo / iQOO Phones (Funtouch OS / OriginOS)
1. Open your phone's **Settings** app or open the green **Phone** app.
2. Tap the **3-dot menu** (top right) or go to **Settings > Phone > Record settings**.
3. Select **Record all calls automatically**.
4. Calls will be recorded silently without any announcement.

#### OnePlus / OPPO / Realme Phones (OxygenOS / ColorOS)
- **If you have ODialer installed:**
  1. Open the **ODialer** app.
  2. Tap the **3-dot menu** > **Settings** > **Call recording**.
  3. Turn on **Record all calls**.
  4. Calls are recorded silently with zero warning beeps.
- **If your phone came with Google Phone:**
  1. We recommend installing **ODialer** from the Oppo App Market or APKMirror for automatic silent recording.
  2. If using Google Phone, tap the **Record** button during an active call, and use the **Share to TWING** feature after the call ends (see Section 3).

#### Samsung Galaxy Phones (One UI)
1. Open the **Phone** app.
2. Tap the **3-dot menu** (top right) > **Settings**.
3. Tap **Record calls**.
4. Turn on **Auto record calls**.

#### Xiaomi / Redmi / POCO Phones (HyperOS / MIUI)
1. Open the **Phone** app.
2. Tap the **Settings icon** (top right).
3. Tap **Call recording**.
4. Turn on **Record calls automatically**.

---

### Step 2: Link the Recording Folder in TWING
1. Open **TWING**.
2. Tap the **Settings** tab (gear icon on the bottom right).
3. Tap **Storage & Hardware**.
4. Under the **Call Recording** card, tap **Link Recording Folder**.
5. The Android system file picker will open. Navigate to your dialer's recording folder:
   - **Vivo:** Look for `Record/Call` or `Recordings/Call`
   - **OnePlus (ODialer):** Look for `Recordings/Call Recordings`
   - **Samsung:** Look for `Recordings/Call`
   - **Xiaomi:** Look for `MIUI/sound_recorder/call_rec`
6. Tap **"Use this folder"** at the bottom, and tap **"Allow"** when prompted.
7. You are all set! TWING now has permanent permission to read and automatically import every call recording.

---

## 2. How the Automatic Import Works

Once linked, you don't need to do anything:
1. Make or receive normal cellular phone calls using your regular dialer.
2. When the call ends, TWING's background service (`RecordingSyncWorker`) automatically checks the linked folder.
3. TWING matches the audio file to your call history, identifying the contact name, phone number, direction, and duration.
4. The recording appears immediately inside TWING's **Calls > Recordings** tab with an emerald green **"Native Cellular"** badge.
5. You can play, pause, seek, share, rename, or delete the recording at any time.

---

## 3. Universal Manual Fallback ("Share to TWING")

If you use a phone with Google Phone or an unsupported dialer:
1. Open your phone's dialer or voice recorder app.
2. Find the recorded call.
3. Tap **Share** (or the share icon).
4. Select **TWING** from the Android share sheet.
5. TWING will instantly import the recording into your private library and match it to your call history.

---

## 4. Troubleshooting & FAQs

### Q1: My recordings are not importing automatically after a call ends.
- **Cause 1: The folder is not linked.**  
  Go to TWING **Settings > Storage & Hardware > Call Recording**, and check if the folder is marked as "Connected". If it says "Not Linked", tap **Link Recording Folder**.
- **Cause 2: Dialer did not record the call.**  
  Check if your phone dialer actually recorded the call. Open your file manager and check if a new `.m4a` or `.mp3` file exists in the dialer's recording folder. If not, verify that auto-recording is enabled in your dialer's settings (see Step 1).
- **Cause 3: Battery optimization killed background sync.**  
  Some OEM aggressive power managers kill background apps. Go to phone **Settings > Apps > TWING > Battery > Set to "Unrestricted"** or **"Don't optimize"**.
- **Immediate manual refresh:**  
  In TWING, open the **Calls** tab, select **Recordings**, and pull down on the list to refresh. If the folder is linked, this will instantly scan for any unimported recordings.

### Q2: Why does an old recording say "Audio capture silenced by Android OS"?
- On modern Android 11–17, third-party apps trying to record the microphone directly during a cellular call receive silent audio due to Android's OS hardware security policy.
- Recordings imported from your native phone dialer do **NOT** have this warning because the native dialer records real, loud two-way audio. They display a green **"Native Cellular"** badge.

### Q3: Why does Google Phone say out loud "This call is now being recorded"?
- Google Phone intentionally plays an audible voice announcement for privacy reasons in certain regions.
- **Solution for OnePlus/Oppo users:** Install **ODialer** from the Oppo App Market. ODialer replaces Google Phone and records calls completely silently without announcements.
- **Solution for Vivo/Samsung users:** Use the pre-installed Vivo or Samsung Phone dialer instead of Google Phone.

### Q4: My recordings show as "Unknown Contact".
- Ensure TWING has been granted **Call Logs** and **Contacts** permissions. Go to phone **Settings > Apps > TWING > Permissions** and ensure **Call logs** and **Contacts** are set to **"Allow"**.
- Once granted, TWING automatically matches phone numbers to your phone contacts.

### Q5: Does linking the folder delete my original recordings?
- **No.** TWING only reads and copies the recordings into TWING's private storage for playback, organization, and cloud backup. Your original files in the dialer's folder remain completely untouched.
