const crypto = require("node:crypto");

let runtimeSecret = null;
function getSecret() {
  if (process.env.ADMIN_SESSION_SECRET && process.env.ADMIN_SESSION_SECRET.trim().length >= 16) {
    return process.env.ADMIN_SESSION_SECRET.trim();
  }
  if (process.env.ADMIN_PASSWORD && process.env.ADMIN_PASSWORD.trim().length >= 8) {
    return process.env.ADMIN_PASSWORD.trim();
  }
  if (!runtimeSecret) {
    runtimeSecret = crypto.randomBytes(32).toString("hex");
  }
  return runtimeSecret;
}

function base64UrlEncode(str) {
  return Buffer.from(str)
    .toString("base64")
    .replace(/=/g, "")
    .replace(/\+/g, "-")
    .replace(/\//g, "_");
}

function base64UrlDecode(str) {
  str = str.replace(/-/g, "+").replace(/_/g, "/");
  while (str.length % 4) {
    str += "=";
  }
  return Buffer.from(str, "base64").toString("utf8");
}

function createAdminToken(username) {
  const header = JSON.stringify({ alg: "HS256", typ: "JWT" });
  const payload = JSON.stringify({
    username: username || "admin",
    role: "admin",
    iat: Math.floor(Date.now() / 1000),
    exp: Math.floor(Date.now() / 1000) + 7 * 24 * 60 * 60, // 7 days valid
  });

  const encodedHeader = base64UrlEncode(header);
  const encodedPayload = base64UrlEncode(payload);
  const signatureInput = `${encodedHeader}.${encodedPayload}`;

  const signature = crypto
    .createHmac("sha256", getSecret())
    .update(signatureInput)
    .digest("base64")
    .replace(/=/g, "")
    .replace(/\+/g, "-")
    .replace(/\//g, "_");

  return `${signatureInput}.${signature}`;
}

function verifyAdminToken(token) {
  if (!token || typeof token !== "string") return null;

  const parts = token.split(".");
  if (parts.length !== 3) return null;

  const [encodedHeader, encodedPayload, signature] = parts;
  const signatureInput = `${encodedHeader}.${encodedPayload}`;

  const expectedSignature = crypto
    .createHmac("sha256", getSecret())
    .update(signatureInput)
    .digest("base64")
    .replace(/=/g, "")
    .replace(/\+/g, "-")
    .replace(/\//g, "_");

  try {
    const isMatch = crypto.timingSafeEqual(
      Buffer.from(signature),
      Buffer.from(expectedSignature)
    );
    if (!isMatch) return null;
  } catch {
    return null;
  }

  try {
    const payload = JSON.parse(base64UrlDecode(encodedPayload));
    if (payload.exp && payload.exp < Math.floor(Date.now() / 1000)) {
      return null; // Expired
    }
    if (payload.role !== "admin") return null;
    return payload;
  } catch {
    return null;
  }
}

function adminAuth(req, res, next) {
  const authHeader = req.get("authorization") || "";
  let token = null;

  const match = authHeader.match(/^Bearer\s+(\S+)$/i);
  if (match) {
    token = match[1];
  } else if (req.query && req.query.token) {
    token = String(req.query.token);
  }

  if (!token) {
    return res.status(401).json({
      success: false,
      error: "Admin authentication required. Missing token.",
    });
  }

  const payload = verifyAdminToken(token);
  if (!payload) {
    return res.status(401).json({
      success: false,
      error: "Invalid or expired admin session. Please login again.",
    });
  }

  req.admin = payload;
  next();
}

module.exports = {
  createAdminToken,
  verifyAdminToken,
  adminAuth,
};
