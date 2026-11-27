// In-memory sliding window rate limiter & brute force protection
const loginAttempts = new Map(); // ip -> { count, firstAttempt, blockedUntil }
const apiRequests = new Map(); // ip -> { count, windowStart }

const LOGIN_MAX_ATTEMPTS = 5;
const LOGIN_WINDOW_MS = 60 * 1000; // 1 minute window
const LOGIN_BLOCK_MS = 15 * 60 * 1000; // 15 minute lockout

const API_MAX_REQUESTS = 300;
const API_WINDOW_MS = 60 * 1000; // 1 minute window

function getClientIp(req) {
  const forwarded = req.headers['x-forwarded-for'];
  if (forwarded) {
    return String(forwarded).split(',')[0].trim();
  }
  return req.ip || req.connection?.remoteAddress || '127.0.0.1';
}

function adminLoginRateLimiter(req, res, next) {
  const ip = getClientIp(req);
  const now = Date.now();
  const entry = loginAttempts.get(ip) || { count: 0, firstAttempt: now, blockedUntil: 0 };

  if (entry.blockedUntil > now) {
    const remainingSecs = Math.ceil((entry.blockedUntil - now) / 1000);
    return res.status(429).json({
      success: false,
      error: `Too many failed login attempts. Please try again in ${remainingSecs} seconds.`,
      code: 'TOO_MANY_ATTEMPTS',
      retryAfterSeconds: remainingSecs
    });
  }

  // Reset window if expired
  if (now - entry.firstAttempt > LOGIN_WINDOW_MS && entry.blockedUntil === 0) {
    entry.count = 0;
    entry.firstAttempt = now;
  }

  // Intercept response to record failures
  const originalJson = res.json.bind(res);
  res.json = (body) => {
    if (res.statusCode === 401 || (body && body.success === false && body.error && body.error.toLowerCase().includes('invalid'))) {
      entry.count += 1;
      if (entry.count >= LOGIN_MAX_ATTEMPTS) {
        entry.blockedUntil = Date.now() + LOGIN_BLOCK_MS;
        console.warn(`[Security Alert] IP ${ip} temporarily locked out for ${LOGIN_BLOCK_MS / 1000}s after ${entry.count} failed login attempts.`);
      }
      loginAttempts.set(ip, entry);
    } else if (res.statusCode === 200 && body && body.success === true) {
      // Successful login resets attempts
      loginAttempts.delete(ip);
    }
    return originalJson(body);
  };

  next();
}

function globalApiRateLimiter(req, res, next) {
  // Exempt static assets and local downloads
  if (req.path.startsWith('/downloads') || req.path.startsWith('/uploads') || req.path.startsWith('/admin/images') || req.path.endsWith('.apk')) {
    return next();
  }

  const ip = getClientIp(req);
  const now = Date.now();
  const entry = apiRequests.get(ip) || { count: 0, windowStart: now };

  if (now - entry.windowStart > API_WINDOW_MS) {
    entry.count = 1;
    entry.windowStart = now;
  } else {
    entry.count += 1;
  }
  apiRequests.set(ip, entry);

  if (entry.count > API_MAX_REQUESTS) {
    return res.status(429).json({
      success: false,
      error: 'Rate limit exceeded. Please throttle your requests.',
      code: 'RATE_LIMIT_EXCEEDED'
    });
  }

  next();
}

function securityHeaders(req, res, next) {
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'SAMEORIGIN');
  res.setHeader('Referrer-Policy', 'strict-origin-when-cross-origin');
  res.setHeader('X-XSS-Protection', '1; mode=block');
  next();
}

function sanitizePrototypePollution(req, res, next) {
  if (req.body && typeof req.body === 'object') {
    delete req.body.__proto__;
    delete req.body.constructor;
    delete req.body.prototype;
  }
  next();
}

// Clean up memory maps every 10 minutes
setInterval(() => {
  const now = Date.now();
  for (const [ip, entry] of loginAttempts.entries()) {
    if (entry.blockedUntil < now && now - entry.firstAttempt > LOGIN_WINDOW_MS) {
      loginAttempts.delete(ip);
    }
  }
  for (const [ip, entry] of apiRequests.entries()) {
    if (now - entry.windowStart > API_WINDOW_MS) {
      apiRequests.delete(ip);
    }
  }
}, 10 * 60 * 1000);

module.exports = {
  adminLoginRateLimiter,
  globalApiRateLimiter,
  securityHeaders,
  sanitizePrototypePollution
};
