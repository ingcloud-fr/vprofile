package com.visualpathit.account.config;

import com.visualpathit.account.model.Post;
import com.visualpathit.account.model.Role;
import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.PostRepository;
import com.visualpathit.account.repository.RoleRepository;
import com.visualpathit.account.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Initializes default data on application startup:
 * - Admin user with ROLE_ADMIN
 * - 5 welcome posts from admin
 */
@Component
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private boolean alreadySetup = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }
        logger.info("Starting data initialization...");

        // Check if admin already exists
        User existingAdmin = userRepository.findByUsername("admin");
        if (existingAdmin != null) {
            logger.info("Admin user already exists, skipping initialization");
            return;
        }

        // Create ROLE_USER if it doesn't exist
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole == null) {
            logger.info("Creating ROLE_USER");
            userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);
        }

        // Create ROLE_ADMIN if it doesn't exist
        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            logger.info("Creating ROLE_ADMIN");
            adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }

        // Create admin user
        logger.info("Creating admin user");
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(bCryptPasswordEncoder.encode("admin123"));
        admin.setUserEmail("admin@vprofile.com");

        // Assign both ROLE_USER and ROLE_ADMIN
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(userRole);
        adminRoles.add(adminRole);
        admin.setRoles(adminRoles);

        admin = userRepository.save(admin);
        logger.info("Admin user created successfully with ID: {}", admin.getId());

        // Create 5 welcome posts from admin
        createAdminPosts(admin);

        alreadySetup = true;
        logger.info("Data initialization completed successfully!");
    }

    private void createAdminPosts(User admin) {
        logger.info("Creating welcome posts from admin");

        String[] postContents = {
            "Bienvenue sur vProfile ! 🎉 Notre réseau social est maintenant opérationnel.",
            "N'hésitez pas à partager vos idées et à vous connecter avec d'autres utilisateurs !",
            "Les nouvelles fonctionnalités arrivent bientôt : likes, commentaires, et bien plus !",
            "Rappel : soyez respectueux dans vos publications et suivez les règles de la communauté.",
            "Astuce : vous pouvez ajouter des images à vos posts en utilisant une URL !"
        };

        // Create posts with slight time differences (older posts first)
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < postContents.length; i++) {
            Post post = new Post();
            post.setContent(postContents[i]);
            post.setAuthor(admin);
            // Make older posts appear further in the past (5 hours ago, 4 hours ago, etc.)
            post.setCreatedAt(now.minusHours(postContents.length - i));
            post.setLikesCount(0);
            postRepository.save(post);
            logger.info("Created admin post {}/{}", i + 1, postContents.length);
        }

        logger.info("All {} admin posts created successfully", postContents.length);
    }
}
