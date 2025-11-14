-- ============================================
-- FLYWAY MIGRATION V4: Admin User & Welcome Post
-- ============================================
-- Description: Inserts the default admin user with ROLE_ADMIN and ROLE_USER,
--              and creates a welcome post from the admin
-- Author: vProfile Team
-- Date: 2025-11-14
-- ============================================

-- Note: The admin user will also be created by DataInitializer.java if it doesn't exist
-- This provides a fallback for fresh database installations

-- Insert admin user if not exists
-- Password: admin123 (BCrypt hash)
-- Hash generated with BCrypt strength 10
INSERT IGNORE INTO user (id, username, password, user_email)
VALUES (
    1,
    'admin',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD99JXfULWPmEziq',
    'admin@vprofile.com'
);

-- Assign ROLE_USER to admin
INSERT IGNORE INTO user_role (user_id, role_id)
VALUES (1, 1);

-- Assign ROLE_ADMIN to admin
INSERT IGNORE INTO user_role (user_id, role_id)
VALUES (1, 2);

-- Insert welcome post from admin
INSERT IGNORE INTO posts (id, content, author_id, created_at, likes_count)
VALUES (
    1,
    'Bienvenue sur vProfile ! 🎉

vProfile est votre nouveau réseau social professionnel. Ici, vous pouvez partager vos expériences DevOps, échanger avec d''autres professionnels de l''IT, et découvrir les dernières tendances en automatisation et CI/CD.

N''hésitez pas à créer votre premier post, compléter votre profil, et commencer à construire votre réseau !

Astuce : Vous pouvez ajouter des images à vos posts en utilisant une URL d''image.

Bonne navigation ! 🚀',
    1,
    NOW(),
    0
);
