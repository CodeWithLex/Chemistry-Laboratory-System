// Development test script — tests connection with different username formats.
// Run with: node test-username.js
require('dotenv').config();
const { Client } = require('pg');

async function test(user, label) {
  console.log(`\nTesting: ${label}`);
  const client = new Client({
    host:     process.env.SUPABASE_HOST,
    port:     parseInt(process.env.SUPABASE_PORT || '5432'),
    database: process.env.SUPABASE_DB || 'postgres',
    user:     user,
    password: process.env.SUPABASE_PASS,
    ssl: { rejectUnauthorized: false },
    connectionTimeoutMillis: 5000,
  });

  try {
    await client.connect();
    console.log(`🎉 SUCCESS with username: ${user}`);
    await client.end();
  } catch (err) {
    console.log(`❌ FAILED: ${err.message}`);
    try { await client.end(); } catch(e){}
  }
}

async function run() {
  const baseUser = process.env.SUPABASE_USER || 'postgres';
  const projectRef = baseUser.replace('postgres.', '');
  await test(baseUser, 'Username WITH project ref suffix');
  await test('postgres', 'Username WITHOUT project ref suffix');
}

run();
