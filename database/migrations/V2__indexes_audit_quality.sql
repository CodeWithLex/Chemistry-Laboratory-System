-- ============================================================
-- ChemLab System — Migration V2: Indexes, Audit Log, Quality
-- Run this in: Supabase Dashboard → SQL Editor → New Query
-- ============================================================

-- ============================================================
-- 1. PERFORMANCE INDEXES
-- The requests table is the most-queried table. Add indexes on
-- all columns used in WHERE / JOIN / ORDER BY clauses.
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_requests_group_id
    ON requests(group_id);

CREATE INDEX IF NOT EXISTS idx_requests_apparatus_id
    ON requests(apparatus_id);

CREATE INDEX IF NOT EXISTS idx_requests_status
    ON requests(status);

CREATE INDEX IF NOT EXISTS idx_requests_created_at
    ON requests(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_group_members_group_id
    ON group_members(group_id);

CREATE INDEX IF NOT EXISTS idx_apparatus_requests_group_id
    ON apparatus_requests(group_id);

CREATE INDEX IF NOT EXISTS idx_apparatus_requests_status
    ON apparatus_requests(status);

-- ============================================================
-- 2. AUDIT LOG TABLE
-- Tracks every important admin action (approve, reject, return,
-- add apparatus, delete group). Provides full accountability.
-- ============================================================

CREATE TABLE IF NOT EXISTS audit_log (
    log_id       SERIAL PRIMARY KEY,
    action       VARCHAR(50)  NOT NULL,   -- e.g. 'APPROVE', 'REJECT', 'RETURN', 'ADD_APPARATUS'
    actor_id     INT          REFERENCES users(user_id) ON DELETE SET NULL,
    actor_name   VARCHAR(100) NULL,        -- denormalized for display even if user is deleted
    target_table VARCHAR(50)  NULL,        -- e.g. 'requests', 'apparatus'
    target_id    INT          NULL,        -- the PK of the affected row
    notes        TEXT         NULL,        -- human-readable summary of what changed
    created_at   TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_log_actor_id  ON audit_log(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log(created_at DESC);

-- ============================================================
-- 3. ADD returned_at COLUMN TO requests
-- Enables accurate duration calculation without guessing from
-- updated_at. Updated when admin marks a request "Returned".
-- ============================================================

ALTER TABLE requests
    ADD COLUMN IF NOT EXISTS returned_at TIMESTAMP NULL;

-- ============================================================
-- 4. SOFT DELETE COLUMNS
-- Prevents permanent data loss when apparatus or groups are
-- removed. All queries should add: WHERE deleted_at IS NULL
-- ============================================================

ALTER TABLE apparatus
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

ALTER TABLE student_groups
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

-- ============================================================
-- 5. REMOVE ORPHANED DEPARTMENT VARCHAR COLUMN
-- student_groups already has department_id FK → departments.
-- The duplicate raw VARCHAR column causes confusion and wastes
-- space. Remove it and use the FK join for all queries.
--
-- NOTE: If your application code still references student_groups.department
-- directly, update those queries to JOIN departments first, THEN run this.
-- ============================================================

ALTER TABLE student_groups
    DROP COLUMN IF EXISTS department;

-- ============================================================
-- 6. sessions TABLE (for Node.js connect-pg-simple)
-- Required for persistent server-side sessions. The web server
-- will create this automatically on first start, but you can
-- pre-create it here.
-- ============================================================

CREATE TABLE IF NOT EXISTS session (
    sid    VARCHAR      NOT NULL COLLATE "default",
    sess   JSON         NOT NULL,
    expire TIMESTAMP(6) NOT NULL,
    CONSTRAINT session_pkey PRIMARY KEY (sid) NOT DEFERRABLE INITIALLY IMMEDIATE
);

CREATE INDEX IF NOT EXISTS idx_session_expire ON session(expire);

-- ============================================================
-- Verification queries (run after migration to confirm)
-- ============================================================
-- SELECT COUNT(*) FROM audit_log;                 -- should return 0
-- SELECT column_name FROM information_schema.columns WHERE table_name = 'requests' AND column_name = 'returned_at';
-- SELECT column_name FROM information_schema.columns WHERE table_name = 'student_groups' AND column_name = 'department';
