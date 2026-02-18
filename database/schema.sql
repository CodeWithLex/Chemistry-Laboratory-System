-- ChemLab Database Schema - Additional Tables for Web Borrowing
-- Run this in phpMyAdmin

USE chemlab_system;

-- Student Groups
CREATE TABLE student_groups (
    group_id INT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Apparatus
CREATE TABLE apparatus (
    apparatus_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    quantity INT DEFAULT 0
);

-- Borrow Requests
CREATE TABLE requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT,
    apparatus_id INT,
    qty INT,
    status ENUM('Pending','Approved','Rejected','Returned') DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES student_groups(group_id),
    FOREIGN KEY (apparatus_id) REFERENCES apparatus(apparatus_id)
);

-- Test Data (password is: password123)
INSERT INTO student_groups (group_name, username, password) VALUES 
('Group A', 'groupa', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'),
('Group B', 'groupb', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

INSERT INTO apparatus (name, quantity) VALUES 
('Beaker 250ml', 20),
('Test Tube', 50),
('Bunsen Burner', 10),
('Erlenmeyer Flask', 15),
('Graduated Cylinder', 12);
