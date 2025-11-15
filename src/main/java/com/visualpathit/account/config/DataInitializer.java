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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    @Value("${ADMIN_EMAIL:admin@facelink.com}")
    private String adminEmail;

    private boolean alreadySetup = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }
        logger.info("Starting data initialization...");

        // Check if admin already exists
        User existingAdmin = userRepository.findByUsername(adminUsername);
        if (existingAdmin != null) {
            logger.info("Admin user already exists, updating password to ensure it's correct");
            // Update admin password to ensure it matches the expected value
            existingAdmin.setPassword(bCryptPasswordEncoder.encode(adminPassword));
            userRepository.save(existingAdmin);
            logger.info("Admin password updated successfully");
            alreadySetup = true;
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
        logger.info("Creating admin user: {}", adminUsername);
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(bCryptPasswordEncoder.encode(adminPassword));
        admin.setUserEmail(adminEmail);

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
        logger.info("Creating welcome post from admin");

        String welcomeMessage = "Bienvenue sur Facelink ! 🎉\n\n" +
                "Facelink est votre nouveau réseau social professionnel. Ici, vous pouvez partager vos expériences DevOps, " +
                "échanger avec d'autres professionnels de l'IT, et découvrir les dernières tendances en automatisation et CI/CD.\n\n" +
                "N'hésitez pas à créer votre premier post, compléter votre profil, et commencer à construire votre réseau !\n\n" +
                "Astuce : Vous pouvez ajouter des images à vos posts en utilisant une URL d'image.\n\n" +
                "Bonne navigation ! 🚀";

        Post welcomePost = new Post();
        welcomePost.setContent(welcomeMessage);
        welcomePost.setAuthor(admin);
        welcomePost.setCreatedAt(LocalDateTime.now().minusHours(1));
        welcomePost.setLikesCount(0);
        postRepository.save(welcomePost);

        logger.info("Welcome post created successfully");
    }
}
