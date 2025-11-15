-- ============================================
-- FLYWAY MIGRATION V1: Initial Schema
-- ============================================
-- Description: Creates the base tables for users and roles
-- Author: Facelink Team
-- Date: 2025-11-14
-- ============================================

-- Create user table
-- IMPORTANT: Column names use camelCase to match JPA property names (no @Column annotations in User.java)
-- IMPORTANT: All ID columns use BIGINT UNSIGNED for foreign key compatibility
CREATE TABLE IF NOT EXISTS user (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    userEmail VARCHAR(255) NOT NULL UNIQUE,
    profileImg VARCHAR(500),
    profileImgPath VARCHAR(500),
    dateOfBirth VARCHAR(255),
    fatherName VARCHAR(255),
    motherName VARCHAR(255),
    gender VARCHAR(50),
    maritalStatus VARCHAR(50),
    permanentAddress VARCHAR(500),
    tempAddress VARCHAR(500),
    primaryOccupation VARCHAR(255),
    secondaryOccupation VARCHAR(255),
    skills TEXT,
    phoneNumber VARCHAR(50),
    secondaryPhoneNumber VARCHAR(50),
    nationality VARCHAR(100),
    language VARCHAR(100),
    workingExperience TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create role table
CREATE TABLE IF NOT EXISTS role (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user_role join table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT UNSIGNED NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for performance
CREATE INDEX idx_user_username ON user(username);
CREATE INDEX idx_user_email ON user(userEmail);
CREATE INDEX idx_role_name ON role(name);

-- Insert default roles
INSERT IGNORE INTO role (id, name) VALUES (1, 'ROLE_USER');
INSERT IGNORE INTO role (id, name) VALUES (2, 'ROLE_ADMIN');
