-- MySQL database schema for the Student Admission Portal.
CREATE DATABASE IF NOT EXISTS student_admission
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_admission;

CREATE TABLE IF NOT EXISTS students (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  roll_number VARCHAR(20) NOT NULL UNIQUE,
  email VARCHAR(254) NOT NULL UNIQUE,
  course VARCHAR(120) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  document_path VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
