-- Student Admission Portal schema (MySQL 8+)
CREATE DATABASE IF NOT EXISTS student_registration
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_registration;

CREATE TABLE IF NOT EXISTS students (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  roll_number VARCHAR(20) NOT NULL UNIQUE,
  email VARCHAR(254) NOT NULL UNIQUE,
  course VARCHAR(120) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  document_path VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- PHP stores upload metadata here. Servlet uploads also store their generated
-- filename in students.document_path, which StudentFileServlet streams.
CREATE TABLE IF NOT EXISTS student_files (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  student_id BIGINT UNSIGNED NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  stored_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(255) NOT NULL,
  file_type VARCHAR(100) NOT NULL,
  file_size BIGINT UNSIGNED NOT NULL,
  upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_student_files_student
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  INDEX idx_student_files_student_id (student_id)
) ENGINE=InnoDB;
