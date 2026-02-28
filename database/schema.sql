-- ChemLab Database Schema - Additional Tables for Web Borrowing
-- Run this in phpMyAdmin

USE chemlab_system;

-- Student Groups
CREATE TABLE student_groups (
    group_id INT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NULL,
    department_id INT NULL
);

-- Apparatus
CREATE TABLE apparatus (
    apparatus_id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    current_quantity INT DEFAULT 0
);

-- Borrow Requests
CREATE TABLE requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT,
    apparatus_id INT,
    qty INT,
    status ENUM('Pending','Approved','Rejected','Returned') DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES student_groups(group_id),
    FOREIGN KEY (apparatus_id) REFERENCES apparatus(apparatus_id)
);

-- Group Members (Feature 1: allow students to add members to their group)
CREATE TABLE group_members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    member_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (group_id) REFERENCES student_groups(group_id)
);

-- Apparatus Requests (Feature 3: students request apparatus not in inventory)
CREATE TABLE apparatus_requests (
    ar_id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    apparatus_name VARCHAR(200) NOT NULL,
    reason TEXT,
    status ENUM('Pending','Reviewed') DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES student_groups(group_id)
);

-- Departments (Feature 5)
CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE
);

-- Add foreign key for departments in student_groups
ALTER TABLE student_groups ADD FOREIGN KEY (department_id) REFERENCES departments(department_id);

-- Test Data (password is: password123)
INSERT INTO student_groups (group_name, username, password) VALUES 
('Group A', 'groupa', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'),
('Group B', 'groupb', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

INSERT INTO apparatus (item_name, current_quantity) VALUES 
('Beaker 250ml', 20),
('Test Tube', 50),
('Bunsen Burner', 10),
('Erlenmeyer Flask', 15),
('Graduated Cylinder', 12);

INSERT INTO departments (department_name) VALUES
('CHS'),
('ENGINEERING'),
('HIGH SCHOOL');

-- =====================================================================
-- MIGRATION SCRIPTS (Run these if upgrading an existing database)
-- =====================================================================

-- Migration: Add approved_at column for duration tracking (Feature 6)
-- ALTER TABLE requests ADD COLUMN approved_at TIMESTAMP NULL AFTER status;

-- Migration: Add department_id column to student_groups (Feature 5)
-- ALTER TABLE student_groups ADD COLUMN department_id INT NULL;
-- ALTER TABLE student_groups ADD FOREIGN KEY (department_id) REFERENCES departments(department_id);

-- Migration: Add role column to users table for instructor support (Feature 4)
-- ALTER TABLE users ADD COLUMN role ENUM('admin','instructor') DEFAULT 'admin';

-- Migration: If your database still uses the old column names (name, quantity), run these ALTER statements:
-- ALTER TABLE apparatus CHANGE COLUMN name item_name VARCHAR(100) NOT NULL;
-- ALTER TABLE apparatus CHANGE COLUMN quantity current_quantity INT DEFAULT 0;

-- Migration: If the requests table already exists, run this to add updated_at:
-- ALTER TABLE requests ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Migration: add email column to student_groups (needed for desktop email notifications)
-- ALTER TABLE student_groups ADD COLUMN email VARCHAR(255) NULL;
