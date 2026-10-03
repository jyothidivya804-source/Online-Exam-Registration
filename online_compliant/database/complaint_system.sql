CREATE DATABASE IF NOT EXISTS complaint_management;
USE complaint_management;

CREATE TABLE users (
 id INT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 email VARCHAR(150) UNIQUE NOT NULL,
 password VARCHAR(255) NOT NULL,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE admins (
 id INT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 email VARCHAR(150) UNIQUE NOT NULL,
 password VARCHAR(255) NOT NULL
);

CREATE TABLE complaints (
 id INT AUTO_INCREMENT PRIMARY KEY,
 complaint_no VARCHAR(30) UNIQUE NOT NULL,
 user_id INT NOT NULL,
 title VARCHAR(200) NOT NULL,
 category VARCHAR(80) NOT NULL,
 description TEXT NOT NULL,
 status ENUM('Pending','In Progress','Resolved','Rejected') DEFAULT 'Pending',
 admin_remark TEXT NULL,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Demo admin: admin@example.com / admin123
-- Password hash generated for PHP password_hash('admin123', PASSWORD_DEFAULT)
INSERT INTO admins(name,email,password) VALUES
('System Administrator','admin@example.com','$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC6o0pJ5G8J2M4f2Wm3K');
