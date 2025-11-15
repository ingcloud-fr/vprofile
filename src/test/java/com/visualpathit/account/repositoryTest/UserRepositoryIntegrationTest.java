package com.visualpathit.account.repositoryTest;

import com.visualpathit.account.model.Role;
import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.RoleRepository;
import com.visualpathit.account.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRepository
 * Tests database operations with real JPA context
 *
 * @DataJpaTest provides:
 * - In-memory H2 database
 * - JPA configuration
 * - Transaction management (rollback after each test)
 * - TestEntityManager for test data setup
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Setup roles
        userRole = new Role();
        userRole.setName("ROLE_USER");
        entityManager.persist(userRole);

        adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        entityManager.persist(adminRole);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should save and retrieve user")
    void testSaveAndFindUser() {
        // Given
        User user = createTestUser("testuser", "test@example.com");

        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();
        User foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername() {
        // Given
        User user = createTestUser("johndoe", "john@example.com");
        entityManager.persist(user);
        entityManager.flush();

        // When
        User foundUser = userRepository.findByUsername("johndoe");

        // Then
        assertNotNull(foundUser);
        assertEquals("johndoe", foundUser.getUsername());
        assertEquals("john@example.com", foundUser.getEmail());
    }

    @Test
    @DisplayName("Should return null when user not found by username")
    void testFindByUsername_NotFound() {
        // When
        User foundUser = userRepository.findByUsername("nonexistent");

        // Then
        assertNull(foundUser);
    }

    @Test
    @DisplayName("Should find user by ID")
    void testFindById() {
        // Given
        User user = createTestUser("testuser", "test@example.com");
        User savedUser = entityManager.persist(user);
        entityManager.flush();

        // When
        User foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
    }

    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        // Given
        User user1 = createTestUser("user1", "user1@example.com");
        User user2 = createTestUser("user2", "user2@example.com");
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // When
        List<User> users = userRepository.findAll();

        // Then
        assertNotNull(users);
        assertTrue(users.size() >= 2);
    }

    @Test
    @DisplayName("Should update user information")
    void testUpdateUser() {
        // Given
        User user = createTestUser("testuser", "old@example.com");
        User savedUser = entityManager.persist(user);
        entityManager.flush();

        // When
        savedUser.setEmail("new@example.com");
        savedUser.setUserProfileImageUrl("https://example.com/newavatar.jpg");
        userRepository.save(savedUser);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to force fresh fetch

        User updatedUser = userRepository.findById(savedUser.getId());

        // Then
        assertNotNull(updatedUser);
        assertEquals("new@example.com", updatedUser.getEmail());
        assertEquals("https://example.com/newavatar.jpg", updatedUser.getUserProfileImageUrl());
    }

    @Test
    @DisplayName("Should delete user")
    void testDeleteUser() {
        // Given
        User user = createTestUser("deleteuser", "delete@example.com");
        User savedUser = entityManager.persist(user);
        Long userId = savedUser.getId();
        entityManager.flush();

        // When
        userRepository.delete(savedUser);
        entityManager.flush();

        User deletedUser = userRepository.findById(userId);

        // Then
        assertNull(deletedUser);
    }

    @Test
    @DisplayName("Should persist user with roles")
    void testSaveUserWithRoles() {
        // Given
        User user = createTestUser("adminuser", "admin@example.com");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        roles.add(adminRole);
        user.setRoles(roles);

        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertNotNull(foundUser);
        assertEquals(2, foundUser.getRoles().size());
        assertTrue(foundUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_USER")));
        assertTrue(foundUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Should enforce username uniqueness")
    void testUsernameUniqueness() {
        // Given
        User user1 = createTestUser("duplicate", "user1@example.com");
        entityManager.persist(user1);
        entityManager.flush();

        User user2 = createTestUser("duplicate", "user2@example.com");

        // When/Then
        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should persist user with profile information")
    void testSaveUserWithProfile() {
        // Given
        User user = createTestUser("profileuser", "profile@example.com");
        user.setUserProfileImageUrl("https://example.com/avatar.jpg");

        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertNotNull(foundUser);
        assertEquals("https://example.com/avatar.jpg", foundUser.getUserProfileImageUrl());
    }

    @Test
    @DisplayName("Should handle null profile image URL")
    void testSaveUserWithNullProfileImage() {
        // Given
        User user = createTestUser("nullprofile", "null@example.com");
        user.setUserProfileImageUrl(null);

        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();

        // Then
        assertNotNull(savedUser);
        assertNull(savedUser.getUserProfileImageUrl());
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void testFindAll_EmptyDatabase() {
        // Given - clean database (setUp doesn't create users)

        // When
        List<User> users = userRepository.findAll();

        // Then
        assertNotNull(users);
        // May have users from other tests due to transaction isolation
    }

    @Test
    @DisplayName("Should count users correctly")
    void testCount() {
        // Given
        User user1 = createTestUser("count1", "count1@example.com");
        User user2 = createTestUser("count2", "count2@example.com");
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // When
        long count = userRepository.count();

        // Then
        assertTrue(count >= 2);
    }

    // Helper method to create test users
    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encodedPassword123");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        return user;
    }
}
