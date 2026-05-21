// Development test script — tests all Supabase pooler regions to find the fastest one.
// Run with: node test-all-regions.js
require('dotenv').config();
const { Client } = require('pg');

const regions = [
  'us-east-1', 'us-east-2', 'us-west-1', 'us-west-2', 'ca-central-1',
  'eu-west-1', 'eu-west-2', 'eu-west-3', 'eu-central-1', 'eu-central-2',
  'ap-northeast-1', 'ap-northeast-2', 'ap-northeast-3', 'ap-southeast-1', 'ap-southeast-2',
  'ap-south-1', 'ap-south-2', 'sa-east-1', 'me-central-1', 'af-south-1'
];

async function testRegion(region) {
  const host     = `aws-0-${region}.pooler.supabase.com`;
  const port     = parseInt(process.env.SUPABASE_PORT || '6543');
  const user     = process.env.SUPABASE_USER;
  const password = process.env.SUPABASE_PASS;
  const database = process.env.SUPABASE_DB || 'postgres';

  if (!user || !password) {
    console.error('❌ Missing SUPABASE_USER or SUPABASE_PASS in .env');
    process.exit(1);
  }

  console.log(`Testing region: ${region} (${host})...`);
  const client = new Client({ host, port, database, user, password, ssl: { rejectUnauthorized: false }, connectionTimeoutMillis: 3000 });

  try {
    await client.connect();
    console.log(`\n🎉 SUCCESS! Connected to Supabase via ${region}!`);
    const res = await client.query('SELECT NOW() AS server_time');
    console.log(`Server Time:`, res.rows[0].server_time);
    await client.end();
    return true;
  } catch (err) {
    console.log(`❌ Region ${region} failed: ${err.message}`);
    try { await client.end(); } catch (e) {}
    return false;
  }
}

async function run() {
  for (const region of regions) {
    const success = await testRegion(region);
    if (success) { console.log(`\nFound matching region: ${region}!`); break; }
  }
}

run();
