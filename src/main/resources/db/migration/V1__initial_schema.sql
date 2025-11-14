-- ============================================
-- FLYWAY MIGRATION V1: Initial Schema
-- ============================================
-- Description: Creates the base tables for users and roles
-- Author: vProfile Team
-- Date: 2025-11-14
-- ============================================

-- Create user table
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL UNIQUE,
    profile_img VARCHAR(500),
    profile_img_path VARCHAR(500),
    date_of_birth VARCHAR(255),
    father_name VARCHAR(255),
    mother_name VARCHAR(255),
    gender VARCHAR(50),
    marital_status VARCHAR(50),
    permanent_address VARCHAR(500),
    temp_address VARCHAR(500),
    primary_occupation VARCHAR(255),
    secondary_occupation VARCHAR(255),
    skills TEXT,
    phone_number VARCHAR(50),
    secondary_phone_number VARCHAR(50),
    nationality VARCHAR(100),
    language VARCHAR(100),
    working_experience TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create role table
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user_role join table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for performance
CREATE INDEX idx_user_username ON user(username);
CREATE INDEX idx_user_email ON user(user_email);
CREATE INDEX idx_role_name ON role(name);

-- Insert default roles
INSERT IGNORE INTO role (id, name) VALUES (1, 'ROLE_USER');
INSERT IGNORE INTO role (id, name) VALUES (2, 'ROLE_ADMIN');
