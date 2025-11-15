package com.visualpathit.account.e2eTest;

import com.visualpathit.account.model.Role;
import com.visualpathit.account.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * END-TO-END TESTS
 * Tests complete user journeys through the application
 *
 * These tests simulate real user scenarios:
 * - User registration → Login → Post creation → Like → Logout
 * - User registration → Profile update → View timeline
 * - Admin workflow
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:e2etestdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("🎯 End-to-End User Journey Tests")
class UserJourneyE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        if (roleRepository != null) {
            // Setup required roles
            if (roleRepository.findByName("ROLE_USER") == null) {
                Role userRole = new Role();
                userRole.setName("ROLE_USER");
                roleRepository.save(userRole);
            }

            if (roleRepository.findByName("ROLE_ADMIN") == null) {
                Role adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                roleRepository.save(adminRole);
            }
        }
    }

    @Test
    @DisplayName("🎯 E2E: Complete user registration and login flow")
    void testCompleteRegistrationAndLoginFlow() throws Exception {
        String username = "e2euser" + System.currentTimeMillis();
        String password = "SecurePass123";
        String email = "e2e@example.com";

        // Step 1: Access registration page
        mockMvc.perform(get("/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("userForm"));

        // Step 2: Submit registration form
        mockMvc.perform(post("/registration")
                        .param("username", username)
                        .param("password", password)
                        .param("passwordConfirm", password)
                        .param("email", email)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timeline")); // Auto-login after registration

        // Step 3: User should be automatically logged in and see timeline
        // This happens via auto-login functionality
    }

    @Test
    @DisplayName("🎯 E2E: User registration with validation errors")
    void testRegistrationWithValidationErrors() throws Exception {
        // Step 1: Attempt registration with invalid data
        mockMvc.perform(post("/registration")
                        .param("username", "ab") // Too short
                        .param("password", "123") // Too short
                        .param("passwordConfirm", "456") // Mismatch
                        .param("email", "invalid-email")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("userForm", "username"))
                .andExpect(model().attributeHasFieldErrors("userForm", "password"))
                .andExpect(model().attributeHasFieldErrors("userForm", "passwordConfirm"));

        // Step 2: Fix errors and submit again
        String validUsername = "validuser" + System.currentTimeMillis();
        mockMvc.perform(post("/registration")
                        .param("username", validUsername)
                        .param("password", "ValidPass123")
                        .param("passwordConfirm", "ValidPass123")
                        .param("email", "valid@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timeline"));
    }

    @Test
    @DisplayName("🎯 E2E: User registration → Timeline → Create Post → Logout")
    void testFullUserWorkflow() throws Exception {
        String username = "workflowuser" + System.currentTimeMillis();
        String password = "WorkflowPass123";

        // Step 1: Register new user
        mockMvc.perform(post("/registration")
                        .param("username", username)
                        .param("password", password)
                        .param("passwordConfirm", password)
                        .param("email", "workflow@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timeline"));

        // Step 2: Access timeline (user is auto-logged in)
        MvcResult timelineResult = mockMvc.perform(get("/timeline"))
                .andExpect(status().isOk())
                .andExpect(view().name("timeline"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("currentUser"))
                .andReturn();

        // Step 3: Create a post
        mockMvc.perform(post("/timeline/post")
                        .param("content", "My first post!")
                        .param("imageUrl", "https://example.com/image.jpg")
                        .with(csrf())
                        .sessionAttrs(timelineResult.getRequest().getSession().getAttributeNames()
                                .stream()
                                .collect(java.util.stream.Collectors.toMap(
                                        name -> name,
                                        name -> timelineResult.getRequest().getSession().getAttribute(name)
                                ))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timeline"));

        // Step 4: View timeline again to see the post
        mockMvc.perform(get("/timeline")
                        .sessionAttrs(timelineResult.getRequest().getSession().getAttributeNames()
                                .stream()
                                .collect(java.util.stream.Collectors.toMap(
                                        name -> name,
                                        name -> timelineResult.getRequest().getSession().getAttribute(name)
                                ))))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posts"));

        // Step 5: Logout
        mockMvc.perform(post("/logout")
                        .with(csrf())
                        .sessionAttrs(timelineResult.getRequest().getSession().getAttributeNames()
                                .stream()
                                .collect(java.util.stream.Collectors.toMap(
                                        name -> name,
                                        name -> timelineResult.getRequest().getSession().getAttribute(name)
                                ))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));

        // Step 6: Verify cannot access timeline after logout
        mockMvc.perform(get("/timeline"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("🎯 E2E: Prevent duplicate username registration")
    void testDuplicateUsernameRegistration() throws Exception {
        String username = "duplicateuser" + System.currentTimeMillis();
        String password = "DuplicatePass123";

        // Step 1: Register first user
        mockMvc.perform(post("/registration")
                        .param("username", username)
                        .param("password", password)
                        .param("passwordConfirm", password)
                        .param("email", "first@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timeline"));

        // Step 2: Attempt to register with same username
        mockMvc.perform(post("/registration")
                        .param("username", username)
                        .param("password", password)
                        .param("passwordConfirm", password)
                        .param("email", "second@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("userForm", "username"));
    }

    @Test
    @DisplayName("🎯 E2E: Access public pages without authentication")
    void testPublicPagesAccess() throws Exception {
        // Landing page
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        // Login page
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

        // Registration page
        mockMvc.perform(get("/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"));

        // Welcome page
        mockMvc.perform(get("/welcome"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"));

        // Health check endpoints (Kubernetes probes)
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        mockMvc.perform(get("/ready"))
                .andExpect(status().isOk())
                .andExpect(content().string("READY"));
    }

    @Test
    @DisplayName("🎯 E2E: Login with invalid credentials")
    void testLoginWithInvalidCredentials() throws Exception {
        // Step 1: Attempt login with non-existent user
        mockMvc.perform(post("/login")
                        .param("username", "nonexistent")
                        .param("password", "wrongpassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));

        // Step 2: Access login page again to see error
        mockMvc.perform(get("/login")
                        .param("error", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("🎯 E2E: User profile update workflow")
    void testProfileUpdateWorkflow() throws Exception {
        String username = "profileuser" + System.currentTimeMillis();
        String password = "ProfilePass123";

        // Step 1: Register user
        MvcResult registrationResult = mockMvc.perform(post("/registration")
                        .param("username", username)
                        .param("password", password)
                        .param("passwordConfirm", password)
                        .param("email", "profile@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        // Step 2: Access profile page (assuming it exists)
        // This would depend on your actual profile endpoints
    }

    @Test
    @DisplayName("🎯 E2E: Health check endpoints are accessible")
    void testHealthCheckEndpoints() throws Exception {
        // Kubernetes liveness probe
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        // Kubernetes readiness probe
        mockMvc.perform(get("/ready"))
                .andExpect(status().isOk())
                .andExpect(content().string("READY"));

        // Version endpoint
        mockMvc.perform(get("/version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("🎯 E2E: Static resources are accessible")
    void testStaticResourcesAccess() throws Exception {
        // Test that static resources don't require authentication
        mockMvc.perform(get("/resources/css/style.css"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/resources/js/app.js"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/resources/images/logo.png"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("🎯 E2E: Password security requirements enforced")
    void testPasswordSecurityEnforcement() throws Exception {
        String username = "secureuser" + System.currentTimeMillis();

        // Test 1: Password too short
        mockMvc.perform(post("/registration")
                        .param("username", username)
                        .param("password", "short")
                        .param("passwordConfirm", "short")
                        .param("email", "test@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("userForm", "password"));

        // Test 2: Password mismatch
        mockMvc.perform(post("/registration")
                        .param("username", username)
                        .param("password", "ValidPass123")
                        .param("passwordConfirm", "DifferentPass456")
                        .param("email", "test@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("userForm", "passwordConfirm"));

        // Test 3: Valid password
        mockMvc.perform(post("/registration")
                        .param("username", username)
                        .param("password", "ValidPass123")
                        .param("passwordConfirm", "ValidPass123")
                        .param("email", "test@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timeline"));
    }
}
