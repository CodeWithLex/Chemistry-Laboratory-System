'use strict';
require('dotenv').config();

const express   = require('express');
const https     = require('https');
const http      = require('http');
const net       = require('net');


const session   = require('express-session');
const { Pool }  = require('pg');
const bcrypt    = require('bcryptjs');
const helmet    = require('helmet');
const rateLimit = require('express-rate-limit');
const nodemailer= require('nodemailer');
const path      = require('path');
const dns       = require('dns').promises; // Use promises version 


const app          = express();
const PORT         = process.env.PORT || 3000;
const isProduction = process.env.NODE_ENV === 'production';

let pool; // Declare pool globally to be initialized in startServer()


// Trust the first proxy (Render's reverse proxy) so express-rate-limit
// and express-session can read the real client IP from X-Forwarded-For.
if (isProduction) {
  app.set('trust proxy', 1);
}

// ─────────────────────────────────────────────────────────────────────────────
// Environment Validation — Fail fast on startup if required vars are missing.
// This prevents accidentally running with no credentials or broken config.
// ─────────────────────────────────────────────────────────────────────────────
function requireEnv(name) {
  const value = process.env[name];
  if (!value || value.trim() === '') {
    console.error(`[STARTUP ERROR] Required environment variable '${name}' is not set.`);
    console.error('Please configure your .env file. See .env.example for the required format.');
    process.exit(1);
  }
  return value.trim();
}

/**
 * Raw Socket Connectivity Probe
 * Tests if a host:port is reachable to bypass ETIMEDOUT guessing.
 */
function probeConnectivity(host, port, timeout = 5000) {
  return new Promise((resolve) => {
    const socket = new net.Socket();
    let isHandled = false;

    const cleanup = () => {
      if (!isHandled) {
        isHandled = true;
        socket.destroy();
      }
    };

    socket.setTimeout(timeout);
    socket.on('connect', () => { cleanup(); resolve(true); });
    socket.on('timeout', () => { cleanup(); resolve(false); });
    socket.on('error',   () => { cleanup(); resolve(false); });

    socket.connect(port, host);
  });
}

// ─── Startup Logic ────────────────────────────────────────────────────────────
// We wrap the startup in an async function to handle manual DNS resolution.
// This is a definitive fix for IPv6 ENETUNREACH errors on Render/Free-Tier.
async function startServer() {
  let dbHost = requireEnv('SUPABASE_HOST');
  let resolvedMailHost = requireEnv('MAIL_HOST');

  // Manual DNS Resolution to force IPv4
  if (isProduction || dbHost.includes('supabase.co') || dbHost.includes('supabase.com')) {
    try {
      console.log(`[STARTUP] Resolving IPv4 for DB host: ${dbHost}...`);
      const { address } = await dns.lookup(dbHost, { family: 4 });
      console.log(`[STARTUP] DB resolved to IPv4: ${address}`);
      dbHost = address;
    } catch (dnsErr) {
      console.warn(`[STARTUP WARNING] DB DNS resolution failed: ${dnsErr.message}`);
    }
  }

  // Manual DNS Resolution for Mail Host (Fixes ETIMEDOUT on Render)
  if (isProduction || resolvedMailHost.includes('google.com') || resolvedMailHost.includes('gmail.com')) {
    try {
      console.log(`[STARTUP] Resolving IPv4 for Mail host: ${resolvedMailHost}...`);
      const { address } = await dns.lookup(resolvedMailHost, { family: 4 });
      console.log(`[STARTUP] Mail resolved to IPv4: ${address}`);
      resolvedMailHost = address;
    } catch (dnsErr) {
      console.warn(`[STARTUP WARNING] Mail DNS resolution failed: ${dnsErr.message}`);
    }
  }

  // ─── SMTP Connectivity Diagnostics ───
  console.log('[DIAGNOSTIC] Probing SMTP connectivity paths...');
  const probeHosts = ['smtp.gmail.com', 'smtp.googlemail.com'];
  const probePorts = [587, 465, 2525];
  const openPaths = [];

  for (const h of probeHosts) {
    for (const p of probePorts) {
      const isUp = await probeConnectivity(h, p);
      if (isUp) {
        console.log(`[DIAGNOSTIC] SUCCESS: ${h}:${p} is REACHABLE.`);
        openPaths.push({ host: h, port: p });
      } else {
        console.log(`[DIAGNOSTIC] FAILED: ${h}:${p} is TIMED OUT or BLOCKED.`);
      }
    }
  }

  // Initialize Mail Transports
  const MAIL_USERNAME = requireEnv('MAIL_USERNAME');
  const MAIL_PASSWORD = requireEnv('MAIL_PASSWORD');
  const primaryPort   = parseInt(process.env.MAIL_PORT || '587', 10);
  
  const createTransporter = (host, port) => nodemailer.createTransport({
    host: host, 
    port: port,
    secure: port === 465,
    auth: { user: MAIL_USERNAME, pass: MAIL_PASSWORD },
    pool: true,
    maxConnections: 1,
    connectionTimeout: 30000,
    greetingTimeout: 30000,
    socketTimeout: 45000,
    family: 4,
    requireTLS: port === 587 || port === 2525,
    tls: { servername: host, rejectUnauthorized: false }
  });

  global.mailTransports = [];
  
  // 1. Prioritize open paths found by diagnostic
  for (const path of openPaths) {
    global.mailTransports.push({ label: `diag:${path.host}:${path.port}`, transporter: createTransporter(path.host, path.port) });
  }

  // 2. Always include hardcoded fallbacks if no paths were found
  if (global.mailTransports.length === 0) {
    console.warn('[STARTUP WARNING] No direct SMTP paths are reachable. Falling back to default transport rotation.');
    const fallbackHost = resolvedMailHost.includes('googlemail') ? 'smtp.gmail.com' : 'smtp.googlemail.com';
    global.mailTransports.push({ label: `primary:${primaryPort}`, transporter: createTransporter(resolvedMailHost, primaryPort) });
    global.mailTransports.push({ label: `fallback:${fallbackHost}:587`, transporter: createTransporter(fallbackHost, 587) });
  }

  // ─── Database Pool ──────────────────────────────────────────────────────────
  pool = new Pool({
    host:                   dbHost,
    port:                   parseInt(process.env.SUPABASE_PORT || '5432'),
    database:               process.env.SUPABASE_DB || 'postgres',
    user:                   requireEnv('SUPABASE_USER'),
    password:               requireEnv('SUPABASE_PASS'),
    ssl:                    { rejectUnauthorized: false },
    max:                    10,
    idleTimeoutMillis:      30000,
    connectionTimeoutMillis: 10000,
  });


  pool.on('error', (err) => {
    console.error('[DB ERROR] Unexpected error on idle client:', err.message);
  });

  try {
    const client = await pool.connect();
    console.log('[DB] Connected to Supabase PostgreSQL successfully.');
    client.release();
  } catch (err) {
    console.error('[DB FATAL] Failed to connect to Supabase:', err.message);
    console.error('[DB HINT] If your IP changed recently, wait 1-2 mins and Render will retry.');
    // We don't exit here to allow Render to keep trying the deploy
  }

  // Global reference for any tools that need it
  global.dbPool = pool; 




// ─── Middleware ───────────────────────────────────────────────────────────────

const MAIL_FROM      = process.env.MAIL_FROM      || process.env.MAIL_USERNAME;
const MAIL_FROM_NAME = process.env.MAIL_FROM_NAME || 'ChemLab System';
const ADMIN_EMAIL    = process.env.ADMIN_EMAIL    || process.env.MAIL_USERNAME;

// ─── Email Helper ─────────────────────────────────────────────────────────────
async function sendMailWithFallback(mailOptions, logLabel) {
  let lastError;

  for (const { label, transporter } of (global.mailTransports || [])) {
    try {
      const info = await transporter.sendMail(mailOptions);
      console.log(`[MAIL] ${logLabel} sent via ${label} to ${ADMIN_EMAIL}. Message ID: ${info.messageId}`);
      return true;
    } catch (error) {
      lastError = error;
      console.error(`[MAIL] ${logLabel} failed via ${label}:`, {
        code: error.code,
        command: error.command,
        error: error.message,
      });
    }
  }

  console.error(`[MAIL] ${logLabel} failed on all SMTP transports. Check Render outbound SMTP access and Gmail app password.`, {
    error: lastError ? lastError.message : 'Unknown mail error',
  });
  return false;
}

async function sendAdminNotification(groupName, apparatusName, quantity) {
  const mailOptions = {
    from:    `"${MAIL_FROM_NAME}" <${MAIL_FROM}>`,
    to:       ADMIN_EMAIL,
    subject: 'New Apparatus Borrow Request',
    html: `
      <div style="font-family:Arial,sans-serif;padding:20px;max-width:600px;border:1px solid #e2e8f0;border-radius:8px;background:#f7fafc">
        <h2 style="color:#2f855a;border-bottom:2px solid #2f855a;padding-bottom:8px">New Borrow Request</h2>
        <p>A new apparatus borrow request has been submitted:</p>
        <table style="border-collapse:collapse;margin:20px 0;width:100%">
          <tr><td style="padding:10px;border:1px solid #ddd;font-weight:bold;width:30%">Group:</td><td style="padding:10px;border:1px solid #ddd">${groupName}</td></tr>
          <tr><td style="padding:10px;border:1px solid #ddd;font-weight:bold">Apparatus:</td><td style="padding:10px;border:1px solid #ddd">${apparatusName}</td></tr>
          <tr><td style="padding:10px;border:1px solid #ddd;font-weight:bold">Quantity:</td><td style="padding:10px;border:1px solid #ddd">${quantity}</td></tr>
        </table>
        <p style="font-size:14px;color:#4a5568">Please review and approve/reject this request in the ChemLab desktop application.</p>
      </div>`,
    text: `New Borrow Request\nGroup: ${groupName}\nApparatus: ${apparatusName}\nQuantity: ${quantity}`,
  };
  return sendMailWithFallback(mailOptions, 'Borrow request notification');
}

async function sendUnlistedNotification(groupName, apparatus_name, reason) {
  const mailOptions = {
    from:    `"${MAIL_FROM_NAME}" <${MAIL_FROM}>`,
    to:       ADMIN_EMAIL,
    subject: 'Action Required: Unlisted Apparatus Request',
    html: `
      <div style="font-family:Arial,sans-serif;padding:20px;max-width:600px;border:1px solid #e2e8f0;border-radius:8px;background:#fffaf0">
        <h2 style="color:#b7791f;border-bottom:2px solid #b7791f;padding-bottom:8px">Unlisted Apparatus Request</h2>
        <p>A student group is requesting an item not currently in the inventory:</p>
        <table style="border-collapse:collapse;margin:20px 0;width:100%">
          <tr><td style="padding:10px;border:1px solid #ddd;font-weight:bold;width:30%">Group:</td><td style="padding:10px;border:1px solid #ddd">${groupName}</td></tr>
          <tr><td style="padding:10px;border:1px solid #ddd;font-weight:bold">Item Name:</td><td style="padding:10px;border:1px solid #ddd">${apparatus_name}</td></tr>
          <tr><td style="padding:10px;border:1px solid #ddd;font-weight:bold">Reason:</td><td style="padding:10px;border:1px solid #ddd">${reason || 'No reason provided'}</td></tr>
        </table>
        <p style="font-size:14px;color:#4a5568">Please review this in the "Apparatus Requests" tab of the ChemLab desktop application.</p>
      </div>`,
    text: `Unlisted Apparatus Request\nGroup: ${groupName}\nItem: ${apparatus_name}\nReason: ${reason}`,
  };
  return sendMailWithFallback(mailOptions, 'Unlisted apparatus notification');
}

// ─── Middleware ───────────────────────────────────────────────────────────────

// Helmet: sets security-hardening HTTP headers (X-Frame-Options, X-Content-Type-Options, etc.)
// contentSecurityPolicy is set to false here; enable and tune it once you have a CSP policy.
app.use(helmet({ contentSecurityPolicy: false }));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));

// ─── Session — Persistent via PostgreSQL ─────────────────────────────────────
// Sessions survive server restarts. Table is auto-created if missing.
const pgSession = require('connect-pg-simple')(session);

app.use(session({
  store: new pgSession({
    pool,
    tableName:            'session',
    createTableIfMissing: true,  // Auto-creates the sessions table in Supabase
  }),
  secret:            requireEnv('SESSION_SECRET'),
  resave:            false,
  saveUninitialized: false,
  name:              'chemlab.sid',   // Non-default name hides tech fingerprint
  cookie: {
    maxAge:   1000 * 60 * 60 * 2,    // 2 hours
    secure:   isProduction,           // HTTPS only in production (NODE_ENV=production)
    httpOnly: true,                   // Prevent JS from reading the cookie
    sameSite: 'strict',               // CSRF mitigation
  },
}));

// ─── Rate Limiting ────────────────────────────────────────────────────────────
const loginLimiter = rateLimit({
  windowMs:        15 * 60 * 1000, // 15-minute window
  max:             20,              // 20 login attempts per IP per window
  standardHeaders: true,
  legacyHeaders:   false,
  message:         { error: 'Too many login attempts. Please try again in 15 minutes.' },
});

// ─── Auth Middleware ──────────────────────────────────────────────────────────
const requireAuth = (req, res, next) => {
  if (!req.session.groupId) {
    return res.status(401).json({ error: 'Unauthorized. Please log in.' });
  }
  next();
};

// ══════════════════════════════════════════════════════════════════════════════
// API ENDPOINTS
// ══════════════════════════════════════════════════════════════════════════════

// ─── Health Check (for Render Keep-Alive) ─────────────────────────────────────
app.get('/api/health', (req, res) => {
  res.status(200).json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Auth status check

app.get('/api/auth/me', (req, res) => {
  if (req.session.groupId) {
    res.json({ loggedIn: true, groupId: req.session.groupId, groupName: req.session.groupName });
  } else {
    res.json({ loggedIn: false });
  }
});

// ─── Login (rate-limited, BCrypt-aware with legacy plaintext fallback) ────────
app.post('/api/auth/login', loginLimiter, async (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) {
    return res.status(400).json({ error: 'Username and password are required.' });
  }

  try {
    const result = await pool.query(
      'SELECT group_id, group_name, password FROM student_groups WHERE username = $1',
      [username.trim()]
    );

    if (result.rows.length === 0) {
      // Generic message — do NOT reveal whether the username exists (prevents enumeration)
      return res.status(401).json({ error: 'Invalid username or password.' });
    }

    const group  = result.rows[0];
    const stored = group.password;
    let matched    = false;
    let needsRehash = false;

    if (stored.startsWith('$2a$') || stored.startsWith('$2b$') || stored.startsWith('$2y$')) {
      // Proper BCrypt comparison
      matched = await bcrypt.compare(password, stored);
    } else {
      // Legacy plaintext comparison (migration path — upgrade on next line)
      matched     = (password === stored);
      needsRehash = matched;
    }

    if (!matched) {
      return res.status(401).json({ error: 'Invalid username or password.' });
    }

    // Silently upgrade legacy plaintext to BCrypt on first login
    if (needsRehash) {
      try {
        const newHash = await bcrypt.hash(password, 12);
        await pool.query(
          'UPDATE student_groups SET password = $1 WHERE group_id = $2',
          [newHash, group.group_id]
        );
        console.log(`[AUTH] Password upgraded to BCrypt for group_id=${group.group_id}`);
      } catch (rehashErr) {
        // Non-fatal — login still succeeds even if rehash fails
        console.error('[AUTH] Non-fatal: failed to upgrade password hash:', rehashErr.message);
      }
    }

    req.session.groupId   = group.group_id;
    req.session.groupName = group.group_name;
    res.json({ success: true, groupId: group.group_id, groupName: group.group_name });

  } catch (error) {
    console.error('[AUTH] Login error:', { error: error.message });
    res.status(500).json({ error: 'An error occurred. Please try again.' });
  }
});

// Logout
app.post('/api/auth/logout', (req, res) => {
  req.session.destroy(err => {
    if (err) return res.status(500).json({ error: 'Failed to log out.' });
    res.clearCookie('chemlab.sid');
    res.json({ success: true });
  });
});

// Get apparatus list with real availability
app.get('/api/apparatus', requireAuth, async (req, res) => {
  try {
    const queryStr = `
      SELECT a.apparatus_id, a.item_name, a.current_quantity,
        (a.current_quantity - COALESCE(SUM(CASE WHEN r.status IN ('Approved','Pending') THEN r.qty ELSE 0 END), 0))::int AS real_available
      FROM apparatus a
      LEFT JOIN requests r ON a.apparatus_id = r.apparatus_id AND r.status IN ('Approved','Pending')
      WHERE a.current_quantity > 0
        AND (a.deleted_at IS NULL OR a.deleted_at > NOW())
      GROUP BY a.apparatus_id, a.item_name, a.current_quantity
      ORDER BY a.item_name
    `;
    const result = await pool.query(queryStr);
    res.json(result.rows);
  } catch (error) {
    console.error('[API] Fetch apparatus error:', error.message);
    res.status(500).json({ error: 'Failed to retrieve apparatus list.' });
  }
});

// Get my requests & borrowing history
app.get('/api/requests/my', requireAuth, async (req, res) => {
  try {
    const result = await pool.query(
      `SELECT r.request_id, r.qty, r.status, r.created_at, a.item_name
       FROM requests r
       JOIN apparatus a ON r.apparatus_id = a.apparatus_id
       WHERE r.group_id = $1
       ORDER BY r.created_at DESC`,
      [req.session.groupId]
    );
    res.json(result.rows);
  } catch (error) {
    console.error('[API] Fetch my requests error:', error.message);
    res.status(500).json({ error: 'Failed to retrieve your request history.' });
  }
});

// Get other groups' pending requests
app.get('/api/requests/others-pending', requireAuth, async (req, res) => {
  try {
    const result = await pool.query(
      `SELECT r.apparatus_id, g.group_name, r.qty
       FROM requests r
       JOIN student_groups g ON r.group_id = g.group_id
       WHERE r.status = 'Pending' AND r.group_id != $1`,
      [req.session.groupId]
    );
    const grouped = {};
    result.rows.forEach(row => {
      const aid = row.apparatus_id;
      if (!grouped[aid]) grouped[aid] = [];
      grouped[aid].push({ group_name: row.group_name, qty: row.qty });
    });
    res.json(grouped);
  } catch (error) {
    console.error('[API] Fetch others-pending error:', error.message);
    res.status(500).json({ error: 'Failed to retrieve pending requests.' });
  }
});

// Submit a borrow request
app.post('/api/requests/borrow', requireAuth, async (req, res) => {
  const { apparatus_id, qty } = req.body;
  const quantity = parseInt(qty);

  if (!apparatus_id || isNaN(quantity) || quantity < 1) {
    return res.status(400).json({ error: 'Invalid apparatus or quantity.' });
  }

  try {
    // 1. Duplicate check
    const dupCheck = await pool.query(
      "SELECT COUNT(*) AS cnt FROM requests WHERE group_id = $1 AND apparatus_id = $2 AND status IN ('Pending','Approved')",
      [req.session.groupId, parseInt(apparatus_id)]
    );
    if (parseInt(dupCheck.rows[0].cnt) > 0) {
      return res.status(400).json({ code: 'duplicate', error: 'You already have a pending or approved request for this apparatus.' });
    }

    // 2. Availability check
    const availResult = await pool.query(
      `SELECT (current_quantity - COALESCE((SELECT SUM(qty) FROM requests WHERE apparatus_id = $1 AND status IN ('Approved', 'Pending')), 0))::int AS real_available
       FROM apparatus WHERE apparatus_id = $2`,
      [parseInt(apparatus_id), parseInt(apparatus_id)]
    );
    if (availResult.rows.length === 0) {
      return res.status(404).json({ error: 'Apparatus not found.' });
    }
    const realAvailable = availResult.rows[0].real_available;
    if (quantity > realAvailable) {
      return res.status(400).json({ code: 'exceeds_qty', error: `Cannot request that many. Only ${realAvailable} items are available.` });
    }

    // 3. Insert request
    await pool.query(
      "INSERT INTO requests (group_id, apparatus_id, qty, status) VALUES ($1, $2, $3, 'Pending')",
      [req.session.groupId, parseInt(apparatus_id), quantity]
    );

    // 4. Send admin notification (fire-and-forget — does not block response)
    const appResult = await pool.query('SELECT item_name FROM apparatus WHERE apparatus_id = $1', [apparatus_id]);
    sendAdminNotification(req.session.groupName, appResult.rows[0].item_name, quantity);

    res.json({ success: true, message: 'Request submitted! Wait for approval.' });

  } catch (error) {
    console.error('[API] Borrow request error:', error.message);
    res.status(500).json({ error: 'An error occurred while submitting your request.' });
  }
});

// Submit a request for unlisted apparatus
app.post('/api/requests/unlisted', requireAuth, async (req, res) => {
  const { apparatus_name, reason } = req.body;
  if (!apparatus_name || !apparatus_name.trim()) {
    return res.status(400).json({ error: 'Apparatus name is required.' });
  }
  try {
    await pool.query(
      'INSERT INTO apparatus_requests (group_id, apparatus_name, reason) VALUES ($1, $2, $3)',
      [req.session.groupId, apparatus_name.trim(), reason ? reason.trim() : '']
    );

    // Send admin notification
    sendUnlistedNotification(req.session.groupName, apparatus_name.trim(), reason);

    res.json({ success: true, message: 'Your apparatus request has been submitted for admin review.' });
  } catch (error) {
    console.error('[API] Unlisted request error:', error.message);
    res.status(500).json({ error: 'Failed to submit request.' });
  }
});

// Group member endpoints
app.get('/api/group/members', requireAuth, async (req, res) => {
  try {
    const result = await pool.query(
      'SELECT member_id, member_name FROM group_members WHERE group_id = $1 ORDER BY member_name',
      [req.session.groupId]
    );
    res.json(result.rows);
  } catch (error) {
    console.error('[API] Fetch group members error:', error.message);
    res.status(500).json({ error: 'Failed to load group members.' });
  }
});

app.post('/api/group/members', requireAuth, async (req, res) => {
  const { member_name } = req.body;
  if (!member_name || !member_name.trim()) {
    return res.status(400).json({ error: 'Member name is required.' });
  }
  try {
    await pool.query(
      'INSERT INTO group_members (group_id, member_name) VALUES ($1, $2)',
      [req.session.groupId, member_name.trim()]
    );
    res.json({ success: true, message: 'Member added successfully!' });
  } catch (error) {
    console.error('[API] Add group member error:', error.message);
    res.status(500).json({ error: 'Failed to add member.' });
  }
});

app.delete('/api/group/members/:id', requireAuth, async (req, res) => {
  const memberId = parseInt(req.params.id);
  if (isNaN(memberId)) {
    return res.status(400).json({ error: 'Invalid member ID.' });
  }
  try {
    await pool.query(
      'DELETE FROM group_members WHERE member_id = $1 AND group_id = $2',
      [memberId, req.session.groupId]
    );
    res.json({ success: true, message: 'Member removed.' });
  } catch (error) {
    console.error('[API] Delete group member error:', error.message);
    res.status(500).json({ error: 'Failed to remove member.' });
  }
});

// Catch-all: serve index.html for SPA routing
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

  // Start server after DB and DNS are handled
  app.listen(PORT, () => {
    console.log(`[SERVER] ChemLab Server running on port ${PORT}`);
    console.log(`[SERVER] Environment: ${isProduction ? 'production' : 'development'}`);

    // ─── Render Keep-Alive Heartbeat ──────────────────────────────────────────
    // On Render's Free tier, apps spin down after 15 mins of inactivity.
    // This interval pings the public URL every 14 minutes to keep it awake.
    const externalUrl = process.env.RENDER_EXTERNAL_URL;
    if (isProduction && externalUrl) {
      console.log(`[KEEP-ALIVE] Heartbeat initialized. Target: ${externalUrl}/api/health`);
      setInterval(() => {
        const protocol = externalUrl.startsWith('https') ? https : http;
        protocol.get(`${externalUrl}/api/health`, (res) => {
          if (res.statusCode === 200) {
            console.log(`[KEEP-ALIVE] Heartbeat successful at ${new Date().toLocaleTimeString()}`);
          } else {
            console.warn(`[KEEP-ALIVE] Heartbeat returned status: ${res.statusCode}`);
          }
        }).on('error', (err) => {
          console.error('[KEEP-ALIVE] Heartbeat failed:', err.message);
        });
      }, 14 * 60 * 1000); // 14 minutes
    } else if (isProduction) {
      console.warn('[KEEP-ALIVE] No RENDER_EXTERNAL_URL provided. Service may spin down.');
    }
  });

}


// ─────────────────────────────────────────────────────────────────────────────
// Kick off the startup sequence
// ─────────────────────────────────────────────────────────────────────────────
startServer().catch(err => {
  console.error('[FATAL STARTUP ERROR]', err);
  process.exit(1);
});
