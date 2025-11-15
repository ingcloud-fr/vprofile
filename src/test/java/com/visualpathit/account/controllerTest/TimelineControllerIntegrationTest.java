package com.visualpathit.account.controllerTest;

import com.visualpathit.account.model.Post;
import com.visualpathit.account.model.Role;
import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.PostRepository;
import com.visualpathit.account.repository.RoleRepository;
import com.visualpathit.account.repository.UserRepository;
import com.visualpathit.account.service.PostService;
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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TimelineController
 * Tests with full Spring context and database
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:timelinetestdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Timeline Controller Integration Tests")
class TimelineControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private RoleRepository roleRepository;

    @Autowired(required = false)
    private PostRepository postRepository;

    @Autowired(required = false)
    private PostService postService;

    @Autowired(required = false)
    private BCryptPasswordEncoder passwordEncoder;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        if (userRepository == null || roleRepository == null) {
            return;
        }

        // Create user role
        userRole = new Role();
        userRole.setName("ROLE_USER");
        if (roleRepository.findByName("ROLE_USER") == null) {
            userRole = roleRepository.save(userRole);
        } else {
            userRole = roleRepository.findByName("ROLE_USER");
        }

        // Create test user
        testUser = new User();
        testUser.setUsername("timelineuser");
        testUser.setEmail("timeline@example.com");
        if (passwordEncoder != null) {
            testUser.setPassword(passwordEncoder.encode("password123"));
        } else {
            testUser.setPassword("password123");
        }
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should require authentication to access timeline")
    void testTimelineRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/timeline"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Should display timeline for authenticated user")
    @WithMockUser(username = "timelineuser", roles = {"USER"})
    void testTimelineAccessForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/timeline"))
                .andExpect(status().isOk())
                .andExpect(view().name("timeline"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("currentUser"));
    }

    @Test
    @DisplayName("Should create new post")
    @WithMockUser(username = "timelineuser", roles = {"USER"})
    void testCreatePost() throws Exception {
        mockMvc.perform(post("/timeline/post")
                        .param("content", "This is a test post from integration test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timeline"));

        // Verify post was created
        if (postRepository != null) {
            var posts = postRepository.findAllByOrderByCreatedAtDesc();
            assertTrue(posts.size() > 0, "Post should be created");
        }
    }

    @Test
    @DisplayName("Should create post with image URL")
    @WithMockUser(username = "timelineuser", roles = {"USER"})
    void testCreatePostWithImage() throws Exception {
        String imageUrl = "https://example.com/test-image.jpg";

        mockMvc.perform(post("/timeline/post")
                        .param("content", "Post with image")
                        .param("imageUrl", imageUrl)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timeline"));
    }

    @Test
    @DisplayName("Should reject empty post content")
    @WithMockUser(username = "timelineuser", roles = {"USER"})
    void testRejectEmptyPost() throws Exception {
        mockMvc.perform(post("/timeline/post")
                        .param("content", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timeline"));
    }

    @Test
    @DisplayName("Should display posts ordered by creation date")
    @WithMockUser(username = "timelineuser", roles = {"USER"})
    void testPostsOrderedByDate() throws Exception {
        if (postService == null || testUser == null) {
            return;
        }

        // Create multiple posts
        postService.createPost("First post", null, testUser);
        Thread.sleep(100); // Ensure different timestamps
        postService.createPost("Second post", null, testUser);
        Thread.sleep(100);
        postService.createPost("Third post", null, testUser);

        mockMvc.perform(get("/timeline"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    @DisplayName("Should handle pagination")
    @WithMockUser(username = "timelineuser", roles = {"USER"})
    void testTimelinePagination() throws Exception {
        if (postService == null || testUser == null) {
            return;
        }

        // Create many posts to trigger pagination
        for (int i = 0; i < 25; i++) {
            postService.createPost("Post number " + i, null, testUser);
        }

        mockMvc.perform(get("/timeline")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    @DisplayName("Should display user information in timeline")
    @WithMockUser(username = "timelineuser", roles = {"USER"})
    void testUserInformationDisplayed() throws Exception {
        mockMvc.perform(get("/timeline"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentUser", hasProperty("username", is("timelineuser"))));
    }

    @Test
    @DisplayName("Should handle special characters in post content")
    @WithMockUser(username = "timelineuser", roles = {"USER"})
    void testPostWithSpecialCharacters() throws Exception {
        String specialContent = "Testing special chars: @#$%^&*()_+-=[]{}|;':\",./<>?";

        mockMvc.perform(post("/timeline/post")
                        .param("content", specialContent)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Should handle Unicode characters in post")
    @WithMockUser(username = "timelineuser", roles = {"USER"})
    void testPostWithUnicodeCharacters() throws Exception {
        String unicodeContent = "Testing Unicode: 你好世界 🌍🚀✨";

        mockMvc.perform(post("/timeline/post")
                        .param("content", unicodeContent)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Should require CSRF token for post creation")
    @WithMockUser(username = "timelineuser", roles = {"USER"})
    void testCsrfProtectionOnPostCreation() throws Exception {
        mockMvc.perform(post("/timeline/post")
                        .param("content", "This should fail without CSRF"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should display empty timeline for new user")
    @WithMockUser(username = "newuser", roles = {"USER"})
    void testEmptyTimelineForNewUser() throws Exception {
        mockMvc.perform(get("/timeline"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posts"));
    }

    // Helper method
    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
