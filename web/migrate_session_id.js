require('dotenv').config();
const { Pool } = require('pg');

const pool = new Pool({
  host: process.env.SUPABASE_HOST,
  port: parseInt(process.env.SUPABASE_PORT || '5432'),
  database: process.env.SUPABASE_DB || 'postgres',
  user: process.env.SUPABASE_USER,
  password: process.env.SUPABASE_PASS,
  ssl: { rejectUnauthorized: false }
});

async function migrate() {
  try {
    console.log('Starting migration...');
    await pool.query(`
      ALTER TABLE requests ADD COLUMN IF NOT EXISTS session_id UUID;
      CREATE INDEX IF NOT EXISTS idx_requests_session_id ON requests(session_id);
      
      -- Also add to apparatus_requests for consistency
      ALTER TABLE apparatus_requests ADD COLUMN IF NOT EXISTS session_id UUID;
      CREATE INDEX IF NOT EXISTS idx_app_requests_session_id ON apparatus_requests(session_id);
    `);
    console.log('Migration successful: session_id added to requests.');
  } catch (err) {
    console.error('Migration failed:', err.message);
  } finally {
    await pool.end();
  }
}

migrate();
