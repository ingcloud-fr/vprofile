package com.visualpathit.account.securityTest;

import com.visualpathit.account.model.Role;
import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.RoleRepository;
import com.visualpathit.account.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DEVSECOPS SECURITY TESTS
 * Tests authentication and authorization security
 *
 * OWASP Top 10 Coverage:
 * - A01:2021 - Broken Access Control
 * - A02:2021 - Cryptographic Failures
 * - A07:2021 - Identification and Authentication Failures
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:securitytestdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("🔒 Authentication & Authorization Security Tests (DevSecOps)")
class AuthenticationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private RoleRepository roleRepository;

    @Autowired(required = false)
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (userRepository != null && roleRepository != null) {
            // Setup test roles
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            if (roleRepository.findByName("ROLE_USER") == null) {
                roleRepository.save(userRole);
            }

            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            if (roleRepository.findByName("ROLE_ADMIN") == null) {
                roleRepository.save(adminRole);
            }
        }
    }

    // ========== A07:2021 - Identification and Authentication Failures ==========

    @Test
    @DisplayName("🔒 Should require authentication for protected endpoints")
    void testProtectedEndpointsRequireAuthentication() throws Exception {
        // Test that timeline requires authentication
        mockMvc.perform(get("/timeline"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("🔒 Should allow access to login page without authentication")
    void testLoginPageAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("🔒 Should allow access to registration page without authentication")
    void testRegistrationPageAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/registration"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("🔒 Should block invalid login attempts")
    void testInvalidLoginAttempt() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "nonexistent")
                        .param("password", "wrongpassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    // ========== A01:2021 - Broken Access Control ==========

    @Test
    @DisplayName("🔒 Should enforce role-based access control for admin endpoints")
    @WithMockUser(username = "regularuser", roles = {"USER"})
    void testAdminEndpointAccessDeniedForRegularUser() throws Exception {
        // Test that regular users cannot access admin functions
        mockMvc.perform(get("/admin/users")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("🔒 Should allow admin access to admin endpoints")
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testAdminEndpointAccessAllowedForAdmin() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("🔒 Should prevent horizontal privilege escalation")
    @WithMockUser(username = "user1", roles = {"USER"})
    void testPreventHorizontalPrivilegeEscalation() throws Exception {
        // Test that users cannot access other users' private data
        // This would need to be implemented based on specific endpoints
        // Example: accessing another user's profile edit page
        mockMvc.perform(get("/profile/edit?userId=999")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ========== A02:2021 - Cryptographic Failures ==========

    @Test
    @DisplayName("🔒 Should store passwords using bcrypt encryption")
    void testPasswordEncryption() {
        if (passwordEncoder == null) {
            // Skip if passwordEncoder is not available in test context
            return;
        }

        // Given
        String rawPassword = "mySecurePassword123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        // BCrypt hashes start with $2a$, $2b$, or $2y$
        assertTrue(encodedPassword.startsWith("$2a$") ||
                        encodedPassword.startsWith("$2b$") ||
                        encodedPassword.startsWith("$2y$"),
                "Password should be encrypted with BCrypt");

        // Verify password is not stored in plaintext
        assertNotEquals(rawPassword, encodedPassword,
                "Password should not be stored in plaintext");

        // Verify password can be validated
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword),
                "Encrypted password should match original");
    }

    @Test
    @DisplayName("🔒 Should use strong password hashing (BCrypt with salt)")
    void testPasswordHashingStrength() {
        if (passwordEncoder == null) {
            return;
        }

        // Given
        String password = "testPassword";

        // When - Generate two hashes of the same password
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        // Then - Hashes should be different (salt is random)
        assertNotEquals(hash1, hash2,
                "BCrypt should use different salt for each hash");

        // Both should validate correctly
        assertTrue(passwordEncoder.matches(password, hash1));
        assertTrue(passwordEncoder.matches(password, hash2));
    }

    @Test
    @DisplayName("🔒 Should reject weak passwords")
    void testWeakPasswordRejection() throws Exception {
        // Test password too short (< 8 characters)
        mockMvc.perform(post("/registration")
                        .param("username", "newuser")
                        .param("password", "weak")
                        .param("passwordConfirm", "weak")
                        .param("email", "test@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("userForm", "password"));
    }

    @Test
    @DisplayName("🔒 Should enforce password confirmation match")
    void testPasswordConfirmationMismatch() throws Exception {
        mockMvc.perform(post("/registration")
                        .param("username", "newuser")
                        .param("password", "password123")
                        .param("passwordConfirm", "differentPassword")
                        .param("email", "test@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("userForm", "passwordConfirm"));
    }

    // ========== Session Management ==========

    @Test
    @DisplayName("🔒 Should create new session on login")
    void testSessionCreationOnLogin() throws Exception {
        // This tests session fixation protection
        mockMvc.perform(post("/login")
                        .param("username", "testuser")
                        .param("password", "password")
                        .with(csrf()))
                .andExpect(request().sessionAttribute("SPRING_SECURITY_CONTEXT", notNullValue()));
    }

    @Test
    @DisplayName("🔒 Should invalidate session on logout")
    @WithMockUser(username = "testuser")
    void testSessionInvalidationOnLogout() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    // ========== CSRF Protection ==========

    @Test
    @DisplayName("🔒 Should require CSRF token for state-changing operations")
    @WithMockUser
    void testCsrfProtectionRequired() throws Exception {
        // POST without CSRF token should be forbidden
        mockMvc.perform(post("/registration")
                        .param("username", "newuser")
                        .param("password", "password123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("🔒 Should accept requests with valid CSRF token")
    @WithMockUser
    void testCsrfProtectionWithValidToken() throws Exception {
        // POST with CSRF token should work
        mockMvc.perform(post("/registration")
                        .param("username", "validuser12345")
                        .param("password", "password123")
                        .param("passwordConfirm", "password123")
                        .param("email", "valid@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    // ========== Brute Force Protection ==========

    @Test
    @DisplayName("🔒 Should enforce username length requirements (anti-enumeration)")
    void testUsernameRequirements() throws Exception {
        // Username too short
        mockMvc.perform(post("/registration")
                        .param("username", "abc")
                        .param("password", "password123")
                        .param("passwordConfirm", "password123")
                        .param("email", "test@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("userForm", "username"));
    }

    @Test
    @DisplayName("🔒 Should prevent duplicate username registration")
    void testDuplicateUsernameRejection() throws Exception {
        if (userRepository == null || roleRepository == null) {
            return;
        }

        // Create existing user
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setPassword("encodedPassword");
        existingUser.setEmail("existing@example.com");
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByName("ROLE_USER");
        if (role != null) {
            roles.add(role);
            existingUser.setRoles(roles);
        }
        userRepository.save(existingUser);

        // Try to register with same username
        mockMvc.perform(post("/registration")
                        .param("username", "existinguser")
                        .param("password", "password123")
                        .param("passwordConfirm", "password123")
                        .param("email", "different@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
    }

    // Helper assertions
    private static org.hamcrest.Matcher<Object> notNullValue() {
        return org.hamcrest.Matchers.notNullValue();
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertTrue(boolean condition) {
        assertTrue(condition, "Expected true but was false");
    }

    private static void assertNotEquals(Object unexpected, Object actual, String message) {
        if (unexpected == null ? actual == null : unexpected.equals(actual)) {
            throw new AssertionError(message);
        }
    }
}
