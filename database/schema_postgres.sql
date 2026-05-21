-- ============================================================
-- ChemLab System Database Schema - PostgreSQL (Supabase)
-- Run this in Supabase SQL Editor (supabase.com → SQL Editor)
-- ============================================================

-- =====================
-- CUSTOM TYPES (Enums)
-- =====================

CREATE TYPE request_status AS ENUM ('Pending', 'Approved', 'Rejected', 'Returned');
CREATE TYPE apparatus_request_status AS ENUM ('Pending', 'Reviewed');

-- =====================
-- TABLES
-- =====================

-- Departments
CREATE TABLE departments (
    department_id SERIAL PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE
);

-- Student Groups (web login users)
CREATE TABLE student_groups (
    group_id    SERIAL PRIMARY KEY,
    group_name  VARCHAR(100) NOT NULL,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NULL,
    department_id INT NULL REFERENCES departments(department_id),
    department  VARCHAR(100) NULL,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Apparatus Inventory
CREATE TABLE apparatus (
    apparatus_id     SERIAL PRIMARY KEY,
    item_name        VARCHAR(100) NOT NULL,
    current_quantity INT DEFAULT 0
);

-- Borrow Requests (from web / student groups)
CREATE TABLE requests (
    request_id  SERIAL PRIMARY KEY,
    group_id    INT REFERENCES student_groups(group_id),
    apparatus_id INT REFERENCES apparatus(apparatus_id),
    qty         INT,
    status      request_status DEFAULT 'Pending',
    created_at  TIMESTAMP DEFAULT NOW(),
    approved_at TIMESTAMP NULL,
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- Trigger to auto-update updated_at on row update
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_requests_updated_at
BEFORE UPDATE ON requests
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Group Members
CREATE TABLE group_members (
    member_id   SERIAL PRIMARY KEY,
    group_id    INT NOT NULL REFERENCES student_groups(group_id),
    member_name VARCHAR(100) NOT NULL
);

-- Apparatus Requests (students request apparatus not in inventory)
CREATE TABLE apparatus_requests (
    ar_id          SERIAL PRIMARY KEY,
    group_id       INT NOT NULL REFERENCES student_groups(group_id),
    apparatus_name VARCHAR(200) NOT NULL,
    reason         TEXT,
    status         apparatus_request_status DEFAULT 'Pending',
    created_at     TIMESTAMP DEFAULT NOW()
);

-- Desktop Admin/Instructor Users
-- (used by the Java FXML application)
CREATE TABLE users (
    user_id       SERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(30)  DEFAULT 'Admin',  -- 'Admin' or 'instructor'
    year_level    VARCHAR(20)  NULL,
    section       VARCHAR(30)  NULL,
    department    VARCHAR(100) NULL,
    created_at    TIMESTAMP DEFAULT NOW()
);

-- =====================
-- SEED / TEST DATA
-- =====================

INSERT INTO departments (department_name) VALUES
('CHS'),
('ENGINEERING'),
('HIGH SCHOOL');

-- Test student groups (password is the literal string: password123)
-- NOTE: The web login.php compares plaintext - change this to bcrypt hashes in production!
INSERT INTO student_groups (group_name, username, password) VALUES
('Group A', 'groupa', 'password123'),
('Group B', 'groupb', 'password123');

INSERT INTO apparatus (item_name, current_quantity) VALUES
('Beaker 250ml', 20),
('Test Tube', 50),
('Bunsen Burner', 10),
('Erlenmeyer Flask', 15),
('Graduated Cylinder', 12);

-- ============================================================
-- ROW-LEVEL SECURITY (Recommended for Supabase)
-- ============================================================
-- Enable RLS on all tables so that the anon/service_role keys
-- only access what they are allowed to.
-- Run these after creating the tables:

-- ALTER TABLE departments ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE student_groups ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE apparatus ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE requests ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE group_members ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE apparatus_requests ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- For a simpler setup, use the Supabase dashboard connection string
-- with a limited database role (see DEPLOYMENT.md).
