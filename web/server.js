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

// ─── Environment Summary (Safe to log) ─────────────────────────────────────────
console.log('[STARTUP] Checking Environment...');
console.log(`- NODE_ENV: ${process.env.NODE_ENV}`);
console.log(`- DB_HOST:  ${process.env.SUPABASE_HOST ? 'Present' : 'MISSING'}`);
console.log(`- RESEND:   ${process.env.RESEND_API_KEY ? 'Configured' : 'NOT CONFIGURED'}`);
console.log(`- PORT:     ${process.env.PORT || 3000}`);

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
    // Check if we can run a simple query
    const res = await client.query('SELECT NOW()');
    console.log('[DB] Basic query test successful at:', res.rows[0].now);
    client.release();
  } catch (err) {
    console.error('[DB FATAL] Failed to connect to Supabase!');
    console.error(`[DB ERROR DETAILS] Code: ${err.code || 'N/A'}, Message: ${err.message}`);
    console.error(`[DB DEBUG] Attempted Host: ${dbHost}, User: ${process.env.SUPABASE_USER}`);
    console.error('[DB HINT] Common issues: 1) Wrong credentials, 2) IP Whitelisting in Supabase, 3) IPv6 connectivity issues.');
    // In production, we keep the process alive so Render doesn't loop forever,
    // allowing developers to see the logs.
  }

  // Global reference for any tools that need it
  global.dbPool = pool; 




// ─── Middleware ───────────────────────────────────────────────────────────────

const MAIL_FROM      = process.env.MAIL_FROM      || process.env.MAIL_USERNAME;
const MAIL_FROM_NAME = process.env.MAIL_FROM_NAME || 'ChemLab System';
const ADMIN_EMAIL    = process.env.ADMIN_EMAIL    || process.env.MAIL_USERNAME;

// ─── Email Helper (Resend API Strategy) ───────────────────────────────────────
// Because Render blocks standard SMTP ports on the free tier, we use the Resend 
// HTTP API to reliably deliver emails via standard Port 443 HTTPS traffic.
// 
// When using a free Resend key with no verified domain:
// - FROM must be: onboarding@resend.dev
// - TO must be: the exact email address used to register the Resend account
async function sendEmail(mailOptions, logLabel) {
  const apiKey = process.env.RESEND_API_KEY;
  if (!apiKey || apiKey.trim() === '') {
    console.error(`[MAIL FATAL] Missing RESEND_API_KEY! Please follow the 'resend_migration_guide.md' to activate notifications.`);
    return false;
  }

  try {
    const response = await fetch('https://api.resend.com/emails', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${apiKey}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        from: `ChemLab System <onboarding@resend.dev>`, 
        to: [ADMIN_EMAIL], 
        subject: mailOptions.subject,
        html: mailOptions.html,
        text: mailOptions.text
      })
    });

    const data = await response.json();

    if (!response.ok) {
      console.error(`[MAIL RESEND ERROR] ${logLabel} failed:`, data);
      return false;
    }

    console.log(`[MAIL RESEND SUCCESS] ${logLabel} sent to ${ADMIN_EMAIL}. ID: ${data.id}`);
    return true;
  } catch (error) {
    console.error(`[MAIL RESEND EXCEPTION] ${logLabel} failed due to network error:`, error.message);
    return false;
  }
}

async function sendAdminNotification(groupName, items, labActivity) {
  const itemsHtml = items.map(item => `
    <tr>
      <td style="padding:10px;border:1px solid #ddd">${item.name}</td>
      <td style="padding:10px;border:1px solid #ddd;text-align:center">${item.qty}</td>
    </tr>
  `).join('');

  const mailOptions = {
    from:    `"${MAIL_FROM_NAME}" <${MAIL_FROM}>`,
    to:       ADMIN_EMAIL,
    subject: `New Borrow Request: ${labActivity || 'General Laboratory'}`,
    html: `
      <div style="font-family:Arial,sans-serif;padding:20px;max-width:600px;border:1px solid #e2e8f0;border-radius:8px;background:#f7fafc">
        <h2 style="color:#2f855a;border-bottom:2px solid #2f855a;padding-bottom:8px">New Bulk Request</h2>
        <p><strong>Group:</strong> ${groupName}</p>
        ${labActivity ? `<p><strong>Activity:</strong> ${labActivity}</p>` : ''}
        
        <table style="border-collapse:collapse;margin:20px 0;width:100%;background:#ffffff">
          <thead>
            <tr style="background:#edf2f7"><th style="padding:10px;border:1px solid #ddd;text-align:left">Apparatus</th><th style="padding:10px;border:1px solid #ddd;width:80px">Qty</th></tr>
          </thead>
          <tbody>
            ${itemsHtml}
          </tbody>
        </table>
        <p style="font-size:14px;color:#4a5568">Please review these requests in the ChemLab desktop application.</p>
      </div>`,
    text: `New Borrow Request\nGroup: ${groupName}\nActivity: ${labActivity || 'None'}\nItems:\n${items.map(i => `- ${i.name}: ${i.qty}`).join('\n')}`,
  };
  return sendEmail(mailOptions, 'Bulk borrow request notification');
}

async function sendUnlistedNotification(groupName, apparatus_name, reason, labActivity) {
  const mailOptions = {
    from:    `"${MAIL_FROM_NAME}" <${MAIL_FROM}>`,
    to:       ADMIN_EMAIL,
    subject: `Unlisted Apparatus Request: ${labActivity || 'General Laboratory'}`,
    html: `
      <div style="font-family:Arial,sans-serif;padding:20px;max-width:600px;border:1px solid #e2e8f0;border-radius:8px;background:#fffaf0">
        <h2 style="color:#b7791f;border-bottom:2px solid #b7791f;padding-bottom:8px">Unlisted Apparatus Request</h2>
        <p><strong>Group:</strong> ${groupName}</p>
        ${labActivity ? `<p><strong>Activity:</strong> ${labActivity}</p>` : ''}
        <table style="border-collapse:collapse;margin:20px 0;width:100%">
          <tr><td style="padding:10px;border:1px solid #ddd;font-weight:bold;width:30%">Item Name:</td><td style="padding:10px;border:1px solid #ddd">${apparatus_name}</td></tr>
          <tr><td style="padding:10px;border:1px solid #ddd;font-weight:bold">Reason:</td><td style="padding:10px;border:1px solid #ddd">${reason || 'No reason provided'}</td></tr>
        </table>
        <p style="font-size:14px;color:#4a5568">Please review this in the apparatus requests tab of the ChemLab desktop application.</p>
      </div>`,
    text: `Unlisted Apparatus Request\nGroup: ${groupName}\nActivity: ${labActivity || 'None'}\nItem: ${apparatus_name}\nReason: ${reason}`,
  };
  return sendEmail(mailOptions, 'Unlisted apparatus notification');
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
        (a.current_quantity - COALESCE(SUM(CASE WHEN r.status IN ('Approved','Pending','Released') THEN r.qty ELSE 0 END), 0))::int AS real_available
      FROM apparatus a
      LEFT JOIN requests r ON a.apparatus_id = r.apparatus_id AND r.status IN ('Approved','Pending','Released')
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
      `SELECT r.request_id, r.qty, r.status, r.created_at, r.lab_activity, a.item_name
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

// ─── DASHBOARD SUMMARY (Performance Optimization) ────────────────────────────
// Fetches all dashboard components in a single round-trip.
app.get('/api/dashboard/summary', requireAuth, async (req, res) => {
  try {
    const groupId = req.session.groupId;

    // Run all fetches in parallel for maximum speed
    const [myRequests, othersPending, apparatus] = await Promise.all([
      // 1. My Timeline & History
      pool.query(
        `SELECT r.request_id, r.qty, r.status, r.created_at, r.lab_activity, a.item_name
         FROM requests r
         JOIN apparatus a ON r.apparatus_id = a.apparatus_id
         WHERE r.group_id = $1
         ORDER BY r.created_at DESC LIMIT 50`,
        [groupId]
      ),
      // 2. Others Pending (Joined with names on server)
      pool.query(
        `SELECT r.apparatus_id, a.item_name, g.group_name, r.qty
         FROM requests r
         JOIN student_groups g ON r.group_id = g.group_id
         JOIN apparatus a ON r.apparatus_id = a.apparatus_id
         WHERE r.status = 'Pending' AND r.group_id != $1
         ORDER BY r.created_at ASC`,
        [groupId]
      ),
      // 3. Apparatus List (for select dropdown)
      pool.query(`
        SELECT a.apparatus_id, a.item_name, a.current_quantity,
          (a.current_quantity - COALESCE(SUM(CASE WHEN r.status IN ('Approved','Pending','Released') THEN r.qty ELSE 0 END), 0))::int AS real_available
        FROM apparatus a
        LEFT JOIN requests r ON a.apparatus_id = r.apparatus_id AND r.status IN ('Approved','Pending','Released')
        WHERE a.current_quantity > 0
          AND (a.deleted_at IS NULL OR a.deleted_at > NOW())
        GROUP BY a.apparatus_id, a.item_name, a.current_quantity
        ORDER BY a.item_name
      `)
    ]);

    // Group the 'Others Pending' by item name/id for the UI
    const groupedPending = {};
    othersPending.rows.forEach(row => {
      const aid = row.apparatus_id;
      if (!groupedPending[aid]) {
        groupedPending[aid] = {
          item_name: row.item_name,
          requests: []
        };
      }
      groupedPending[aid].requests.push({ 
        group_name: row.group_name, 
        qty: row.qty 
      });
    });

    res.json({
      myRequests: myRequests.rows,
      othersPending: groupedPending,
      apparatus: apparatus.rows,
      serverTime: new Date().toISOString()
    });

  } catch (error) {
    console.error('[API] Dashboard summary error:', error.message);
    res.status(500).json({ error: 'Failed to generate dashboard summary.' });
  }
});


// Submit bulk borrow requests
app.post('/api/requests/borrow', requireAuth, async (req, res) => {
  const { lab_activity, items } = req.body;

  if (!items || !Array.isArray(items) || items.length === 0) {
    return res.status(400).json({ error: 'No items provided in request.' });
  }

  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const insertedItems = [];

    for (const item of items) {
      const aid = parseInt(item.apparatus_id);
      const quantity = parseInt(item.qty);

      if (isNaN(aid) || isNaN(quantity) || quantity < 1) {
        throw new Error(`Invalid data for item correctly formatted.`);
      }

      // Check current availability
      const availRes = await client.query(
        `SELECT item_name, (current_quantity - COALESCE((SELECT SUM(qty) FROM requests WHERE apparatus_id = $1 AND status IN ('Approved', 'Pending', 'Released')),0))::int AS available
         FROM apparatus WHERE apparatus_id = $1`,
        [aid]
      );

      if (availRes.rows.length === 0) {
        throw new Error(`Apparatus ID ${aid} not found.`);
      }

      const { item_name, available } = availRes.rows[0];
      if (quantity > available) {
        throw new Error(`Quantity ${quantity} for ${item_name} exceeds availability (${available}).`);
      }

      // Insert
      await client.query(
        "INSERT INTO requests (group_id, apparatus_id, qty, status, lab_activity) VALUES ($1, $2, $3, 'Pending', $4)",
        [req.session.groupId, aid, quantity, lab_activity ? lab_activity.trim() : null]
      );
      
      insertedItems.push({ name: item_name, qty: quantity });
    }

    await client.query('COMMIT');
    
    // Notify admin
    sendAdminNotification(req.session.groupName, insertedItems, lab_activity);

    res.json({ success: true, message: 'All requests submitted successfully!' });
  } catch (error) {
    await client.query('ROLLBACK');
    console.error('[API] Bulk borrow error:', error.message);
    res.status(400).json({ error: error.message || 'Failed to submit batch requests.' });
  } finally {
    client.release();
  }
});

// Update unlisted to support lab_activity
app.post('/api/requests/unlisted', requireAuth, async (req, res) => {
  const { apparatus_name, reason, lab_activity } = req.body;
  if (!apparatus_name || !apparatus_name.trim()) {
    return res.status(400).json({ error: 'Apparatus name is required.' });
  }
  try {
    await pool.query(
      'INSERT INTO apparatus_requests (group_id, apparatus_name, reason, lab_activity) VALUES ($1, $2, $3, $4)',
      [req.session.groupId, apparatus_name.trim(), reason ? reason.trim() : '', lab_activity ? lab_activity.trim() : null]
    );

    sendUnlistedNotification(req.session.groupName, apparatus_name.trim(), reason, lab_activity);
    res.json({ success: true, message: 'Unlisted request submitted.' });
  } catch (error) {
    console.error('[API] Unlisted error:', error.message);
    res.status(500).json({ error: 'Database error.' });
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