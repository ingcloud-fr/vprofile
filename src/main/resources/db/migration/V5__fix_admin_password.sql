-- ============================================
-- FLYWAY MIGRATION V5: Fix Admin Password
-- ============================================
-- Description: Updates the admin password to ensure it correctly matches "admin123"
-- Author: Facelink Team
-- Date: 2025-11-15
-- ============================================

-- Update admin password with correct BCrypt hash for "admin123"
-- Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- This hash is generated with BCrypt strength 10 and corresponds to "admin123"
UPDATE user
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE username = 'admin';
