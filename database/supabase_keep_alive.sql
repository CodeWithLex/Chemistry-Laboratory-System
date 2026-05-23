-- ============================================================
-- ChemLab System - Supabase Keep-Alive Heartbeat
-- Purpose: Prevent Supabase from pausing the project due to inactivity.
-- Instructions: Run this in the Supabase SQL Editor.
-- ============================================================

-- 1. Create Heartbeat Table
CREATE TABLE IF NOT EXISTS system_heartbeat (
    id SERIAL PRIMARY KEY,
    ping_at TIMESTAMP DEFAULT NOW(),
    status VARCHAR(50) DEFAULT 'ACTIVE'
);

-- 2. Create the Heartbeat Stored Procedure
-- This function can be called by an external ping or a pg_cron job.
CREATE OR REPLACE FUNCTION record_heartbeat()
RETURNS void AS $$
BEGIN
    INSERT INTO system_heartbeat (status)
    VALUES ('HEARTBEAT_OK');
    
    -- Cleanup: Keep only the last 30 days of logs
    DELETE FROM system_heartbeat 
    WHERE ping_at < NOW() - INTERVAL '30 days';
END;
$$ LANGUAGE plpgsql;

-- 3. Initial Heartbeat
SELECT record_heartbeat();

-- ============================================================
-- RECOMMENDATION:
-- To keep the DB active 24/7 without manual intervention:
-- 1. Enable 'pg_cron' in Database > Extensions in your Supabase dashboard.
-- 2. Run the following to schedule a daily heartbeat:
--    SELECT cron.schedule('database-keep-alive', '0 0 * * *', 'SELECT record_heartbeat()');
-- ============================================================
