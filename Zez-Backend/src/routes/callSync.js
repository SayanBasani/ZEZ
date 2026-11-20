const express = require("express");
const crypto = require("node:crypto");
const { pool } = require("../db");
const { encryptJson } = require("../crypto");
const { deviceAuth } = require("../middleware/deviceAuth");

const router = express.Router();
const MAX_BATCH_SIZE = 500;
const MAX_DEVICE_DETAILS_BYTES = 100_000;

function httpError(statusCode, message) {
  const error = new Error(message);
  error.statusCode = statusCode;
  return error;
}

function getTimestamp(value) {
  if (value === null || value === undefined || value === "") return null;
  const numeric = Number(value);
  const date = Number.isFinite(numeric) && numeric > 0 ? new Date(numeric) : new Date(value);
  if (Number.isNaN(date.getTime())) {
    throw httpError(400, "A call has an invalid timestamp.");
  }
  return date.toISOString();
}

function normalizeCall(input) {
  if (!input || typeof input !== "object" || Array.isArray(input)) {
    throw httpError(400, "Each call must be an object.");
  }

  const externalId = input.externalId ?? input.id ?? input.callId ?? input.call_id;
  if (!externalId) {
    throw httpError(400, "A stable call identifier is required.");
  }

  const durationNumber = Number(input.duration ?? 0);
  return {
    externalId: String(externalId),
    phoneNumber: String(input.phoneNumber ?? ""),
    name: String(input.name ?? ""),
    type: String(input.type ?? "UNKNOWN"),
    timestamp: getTimestamp(input.timestamp),
    duration: Math.max(0, Math.trunc(durationNumber)),
    raw: input.raw ?? input,
  };
}

router.post("/sync", deviceAuth, async (req, res, next) => {
  const body = req.body ?? {};
  const calls = body.calls;

  try {
    if (!Array.isArray(calls)) {
      throw httpError(400, "'calls' must be an array.");
    }
    if (calls.length > MAX_BATCH_SIZE) {
      throw httpError(413, `A maximum of ${MAX_BATCH_SIZE} calls is allowed per batch.`);
    }

    const details = body.deviceDetails;
    const records = calls.map(normalizeCall);

    const seen = new Set();
    const uniqueRecords = [];
    let duplicates = 0;
    for (const record of records) {
      if (seen.has(record.externalId)) {
        duplicates += 1;
        continue;
      }
      seen.add(record.externalId);
      uniqueRecords.push(record);
    }

    const client = await pool.connect();
    let inserted = 0;

    try {
      await client.query("BEGIN");

      if (details !== undefined) {
        const appVersion = details && details.application && details.application.appVersion ? details.application.appVersion : null;
        if (appVersion) {
          await client.query(
            `UPDATE public.devices
             SET device_details = COALESCE(device_details, '{}'::jsonb) || $2::jsonb,
                 app_version = $3,
                 last_seen_at = NOW(),
                 updated_at = NOW()
             WHERE id = $1`,
            [req.deviceId, JSON.stringify(details), appVersion]
          );
        } else {
          await client.query(
            `UPDATE public.devices
             SET device_details = COALESCE(device_details, '{}'::jsonb) || $2::jsonb,
                 last_seen_at = NOW(),
                 updated_at = NOW()
             WHERE id = $1`,
            [req.deviceId, JSON.stringify(details)]
          );
        }
      } else {
        await client.query(
          `UPDATE public.devices
           SET last_seen_at = NOW(),
               updated_at = NOW()
           WHERE id = $1`,
          [req.deviceId]
        );
      }

      // Batch insert with single multi-row queries for massive performance boost
      const recordsToInsert = uniqueRecords.map(r => {
        const externalIdHash = crypto
          .createHash("sha256")
          .update(r.externalId)
          .digest("hex");
        const encryptedPayload = encryptJson(r);
        return {
          deviceId: req.deviceId,
          externalIdHash,
          timestamp: r.timestamp,
          encryptedPayload
        };
      });

      const CHUNK_SIZE = 50;
      for (let i = 0; i < recordsToInsert.length; i += CHUNK_SIZE) {
        const chunk = recordsToInsert.slice(i, i + CHUNK_SIZE);
        const valuePlaceholders = [];
        const queryParams = [];

        chunk.forEach((item, idx) => {
          const offset = idx * 4;
          valuePlaceholders.push(`($${offset + 1}, $${offset + 2}, $${offset + 3}, $${offset + 4})`);
          queryParams.push(item.deviceId, item.externalIdHash, item.timestamp, item.encryptedPayload);
        });

        const batchSql = `
          INSERT INTO public.call_logs (device_id, external_id_hash, call_timestamp, encrypted_payload)
          VALUES ${valuePlaceholders.join(", ")}
          ON CONFLICT (device_id, external_id_hash) DO NOTHING
          RETURNING id
        `;

        const res = await client.query(batchSql, queryParams);
        inserted += res.rowCount;
      }
      duplicates += (uniqueRecords.length - inserted);

      await client.query("COMMIT");

      return res.status(200).json({
        success: true,
        received: records.length,
        inserted,
        duplicates,
        acceptedIds: records.map((record) => record.externalId),
      });
    } catch (error) {
      await client.query("ROLLBACK");
      throw error;
    } finally {
      client.release();
    }
  } catch (error) {
    next(error);
  }
});

module.exports = router;
