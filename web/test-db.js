// Development test script — verifies database connectivity using .env credentials.
// Run with: node test-db.js
require('dotenv').config();
const { Client } = require('pg');

async function run() {
  const host     = process.env.SUPABASE_HOST;
  const port     = parseInt(process.env.SUPABASE_PORT || '6543');
  const user     = process.env.SUPABASE_USER;
  const password = process.env.SUPABASE_PASS;
  const database = process.env.SUPABASE_DB || 'postgres';

  if (!host || !user || !password) {
    console.error('❌ Missing required environment variables. Check your .env file.');
    process.exit(1);
  }

  console.log(`Connecting to: ${host}:${port} as ${user}`);
  const client = new Client({ host, port, database, user, password, ssl: { rejectUnauthorized: false }, connectionTimeoutMillis: 8000 });

  try {
    await client.connect();
    console.log(`\n🎉 SUCCESS! Connected to Supabase via connection pooler!`);
    const res = await client.query('SELECT NOW() AS server_time');
    console.log(`Server Time:`, res.rows[0].server_time);
    const tables = await client.query(`SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name`);
    console.log(`\nTables in database:`);
    tables.rows.forEach(r => console.log(`  - ${r.table_name}`));
    await client.end();
  } catch (err) {
    console.error(`❌ FAILED:`, err.message);
    try { await client.end(); } catch(e){}
  }
}

run();
