-- ============================================
-- FLYWAY MIGRATION V3: Post Likes Table
-- ============================================
-- Description: Creates the post_likes table for tracking post likes
-- Author: vProfile Team
-- Date: 2025-11-14
-- ============================================

-- Create post_likes table
-- IMPORTANT: All ID columns use BIGINT UNSIGNED for foreign key compatibility
CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_like (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for performance
CREATE INDEX idx_post_likes_post_id ON post_likes(post_id);
CREATE INDEX idx_post_likes_user_id ON post_likes(user_id);
