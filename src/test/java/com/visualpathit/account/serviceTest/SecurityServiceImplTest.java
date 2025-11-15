package com.visualpathit.account.serviceTest;

import com.visualpathit.account.service.SecurityServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SecurityServiceImpl
 * Tests authentication and authorization logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityService Unit Tests")
class SecurityServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private SecurityServiceImpl securityService;

    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext();

        // Setup test user details
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        testUserDetails = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .authorities(authorities)
                .build();
    }

    @Test
    @DisplayName("Should return logged-in username when user is authenticated")
    void testFindLoggedInUsername_Authenticated() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUserDetails, null, testUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        String username = securityService.findLoggedInUsername();

        // Then
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should return null when no user is authenticated")
    void testFindLoggedInUsername_NotAuthenticated() {
        // Given - no authentication set

        // When
        String username = securityService.findLoggedInUsername();

        // Then
        assertNull(username);
    }

    @Test
    @DisplayName("Should return null when principal is not UserDetails")
    void testFindLoggedInUsername_InvalidPrincipal() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "stringPrincipal", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        String username = securityService.findLoggedInUsername();

        // Then
        assertNull(username);
    }

    @Test
    @DisplayName("Should successfully auto-login user after registration")
    void testAutologin_Success() {
        // Given
        String username = "newuser";
        String password = "password123";

        when(userDetailsService.loadUserByUsername(username)).thenReturn(testUserDetails);
        when(request.getSession(true)).thenReturn(session);

        // When
        boolean result = securityService.autologin(username, password, request);

        // Then
        assertTrue(result);
        verify(userDetailsService).loadUserByUsername(username);
        verify(request).getSession(true);
        verify(session).setAttribute(eq("SPRING_SECURITY_CONTEXT"), any(SecurityContext.class));

        // Verify authentication was set in security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertEquals(testUserDetails, auth.getPrincipal());
    }

    @Test
    @DisplayName("Should fail auto-login when user not found")
    void testAutologin_UserNotFound() {
        // Given
        String username = "nonexistent";
        String password = "password123";

        when(userDetailsService.loadUserByUsername(username))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // When
        boolean result = securityService.autologin(username, password, request);

        // Then
        assertFalse(result);
        verify(userDetailsService).loadUserByUsername(username);
        verify(request, never()).getSession(anyBoolean());
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    @DisplayName("Should fail auto-login when session creation fails")
    void testAutologin_SessionCreationFails() {
        // Given
        String username = "testuser";
        String password = "password123";

        when(userDetailsService.loadUserByUsername(username)).thenReturn(testUserDetails);
        when(request.getSession(true)).thenThrow(new RuntimeException("Session error"));

        // When
        boolean result = securityService.autologin(username, password, request);

        // Then
        assertFalse(result);
        verify(userDetailsService).loadUserByUsername(username);
        verify(request).getSession(true);
    }

    @Test
    @DisplayName("Should persist SecurityContext in session during auto-login")
    void testAutologin_SecurityContextPersisted() {
        // Given
        String username = "testuser";
        String password = "password123";

        when(userDetailsService.loadUserByUsername(username)).thenReturn(testUserDetails);
        when(request.getSession(true)).thenReturn(session);

        // When
        securityService.autologin(username, password, request);

        // Then
        verify(session).setAttribute(eq("SPRING_SECURITY_CONTEXT"), any(SecurityContext.class));
    }

    @Test
    @DisplayName("Should include user authorities in auto-login authentication")
    void testAutologin_WithAuthorities() {
        // Given
        String username = "adminuser";
        String password = "adminpass";

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        UserDetails adminUser = User.builder()
                .username(username)
                .password("encodedPassword")
                .authorities(authorities)
                .build();

        when(userDetailsService.loadUserByUsername(username)).thenReturn(adminUser);
        when(request.getSession(true)).thenReturn(session);

        // When
        boolean result = securityService.autologin(username, password, request);

        // Then
        assertTrue(result);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(2, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Should handle null password in auto-login")
    void testAutologin_NullPassword() {
        // Given
        String username = "testuser";
        String password = null;

        when(userDetailsService.loadUserByUsername(username)).thenReturn(testUserDetails);
        when(request.getSession(true)).thenReturn(session);

        // When
        boolean result = securityService.autologin(username, password, request);

        // Then
        assertTrue(result); // Auto-login doesn't verify password
    }
}
