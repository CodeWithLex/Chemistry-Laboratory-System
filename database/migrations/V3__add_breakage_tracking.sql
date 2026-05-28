-- ============================================================
-- ChemLab System — Migration V3: Breakage Tracking
-- ============================================================

ALTER TABLE requests
    ADD COLUMN IF NOT EXISTS broken_qty INT DEFAULT 0;

-- Optional: Index for breakage queries
CREATE INDEX IF NOT EXISTS idx_requests_broken_qty ON requests(broken_qty) WHERE broken_qty > 0;
