-- ============================================
-- FLYWAY MIGRATION V2: Posts Table
-- ============================================
-- Description: Creates the posts table for user timeline/wall
-- Author: Facelink Team
-- Date: 2025-11-14
-- ============================================

-- Create posts table
-- IMPORTANT: All ID columns use BIGINT UNSIGNED for foreign key compatibility
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    content VARCHAR(500) NOT NULL,
    image_url VARCHAR(500),
    author_id BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    likes_count INT DEFAULT 0,
    FOREIGN KEY (author_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for performance
CREATE INDEX idx_posts_author_id ON posts(author_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
