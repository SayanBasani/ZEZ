const crypto = require('node:crypto');
const { gcm } = require("@noble/ciphers/aes.js");
const { managedNonce, hexToBytes, bytesToUtf8 } = require("@noble/ciphers/utils.js");

function getKey() {
  const key = process.env.ENCRYPTION_KEY;
  if (!key) {
    throw new Error('ENCRYPTION_KEY is not configured.');
  }
  const decoded = Buffer.from(key, 'base64');
  if (decoded.length !== 32) {
    throw new Error('ENCRYPTION_KEY must be a valid base64-encoded 32-byte key.');
  }
  return decoded;
}

function encryptJson(value) {
  const iv = crypto.randomBytes(12);
  const cipher = crypto.createCipheriv('aes-256-gcm', getKey(), iv);

  const plaintext = Buffer.from(JSON.stringify(value), 'utf8');
  const ciphertext = Buffer.concat([
    cipher.update(plaintext),
    cipher.final()
  ]);

  return JSON.stringify({
    version: 1,
    iv: iv.toString('base64'),
    tag: cipher.getAuthTag().toString('base64'),
    data: ciphertext.toString('base64')
  });
}

function decryptJson(encrypted) {
  const envelope = JSON.parse(encrypted);
  const iv = Buffer.from(envelope.iv, 'base64');
  const tag = Buffer.from(envelope.tag, 'base64');
  const ciphertext = Buffer.from(envelope.data, 'base64');

  const decipher = crypto.createDecipheriv('aes-256-gcm', getKey(), iv);
  decipher.setAuthTag(tag);

  const plaintext = Buffer.concat([
    decipher.update(ciphertext),
    decipher.final()
  ]);

  return JSON.parse(plaintext.toString('utf8'));
}

const portableGcm = managedNonce(gcm);

function decryptPortableHex(encryptedHex) {
  const keyHex = process.env.PORTABLE_ENCRYPTION_KEY;
  if (!keyHex || !/^[0-9a-fA-F]{64}$/.test(keyHex)) {
    throw new Error("PORTABLE_ENCRYPTION_KEY must be a 64-character hex key.");
  }
  const key = hexToBytes(keyHex);
  const encrypted = hexToBytes(encryptedHex);
  const plaintext = portableGcm(key).decrypt(encrypted);
  const json = bytesToUtf8(plaintext);
  return JSON.parse(json);
}

function decryptStoredPayload(encrypted) {
  if (typeof encrypted !== "string") {
    throw new Error("Encrypted payload must be a string.");
  }
  if (/^(?:[0-9a-fA-F]{2})+$/.test(encrypted)) {
    return decryptPortableHex(encrypted);
  }
  return decryptJson(encrypted);
}

module.exports = {
  encryptJson,
  decryptJson,
  decryptStoredPayload,
};
