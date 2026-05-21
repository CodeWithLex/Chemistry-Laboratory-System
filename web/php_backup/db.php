<?php
// ============================================================
// Supabase (PostgreSQL) Database Connection
// ============================================================
// Fill in your Supabase project credentials below.
// You can find these in:
//   Supabase Dashboard → Project Settings → Database
//
// Host     → "Host" field (looks like: db.xxxxxxxxxxxx.supabase.co)
// Password → "Database Password" field
// ============================================================

$SUPABASE_HOST = getenv('SUPABASE_HOST') ?: 'db.YOUR_PROJECT_REF.supabase.co';
$SUPABASE_PORT = getenv('SUPABASE_PORT') ?: '5432';
$SUPABASE_DB   = getenv('SUPABASE_DB')   ?: 'postgres';
$SUPABASE_USER = getenv('SUPABASE_USER') ?: 'postgres';
$SUPABASE_PASS = getenv('SUPABASE_PASS') ?: 'YOUR_SUPABASE_DB_PASSWORD';

$dsn = "pgsql:host={$SUPABASE_HOST};port={$SUPABASE_PORT};dbname={$SUPABASE_DB};sslmode=require";

try {
    $conn = new PDO($dsn, $SUPABASE_USER, $SUPABASE_PASS, [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES   => false,
    ]);
} catch (PDOException $e) {
    die("Database connection failed: " . $e->getMessage());
}
?>
