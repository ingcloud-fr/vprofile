package com.visualpathit.account.serviceTest;

import com.visualpathit.account.model.Role;
import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.RoleRepository;
import com.visualpathit.account.repository.UserRepository;
import com.visualpathit.account.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl
 * Tests business logic without Spring context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setUserEmail("test@example.com");

        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");
    }

    @Test
    @DisplayName("Should save user with encrypted password and USER role")
    void testSave_Success() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encodedPassword";

        when(bCryptPasswordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.save(testUser);

        // Then
        verify(bCryptPasswordEncoder).encode(rawPassword);
        verify(roleRepository).findByName("ROLE_USER");
        verify(userRepository).save(testUser);

        // Verify password was encoded
        assertEquals(encodedPassword, testUser.getPassword());

        // Verify ROLE_USER was assigned
        assertNotNull(testUser.getRoles());
        assertEquals(1, testUser.getRoles().size());
        assertTrue(testUser.getRoles().contains(userRole));
    }

    @Test
    @DisplayName("Should save user even if ROLE_USER doesn't exist in database")
    void testSave_RoleNotFound() {
        // Given
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.save(testUser);

        // Then
        verify(userRepository).save(testUser);
        assertNotNull(testUser.getRoles());
        assertEquals(0, testUser.getRoles().size()); // No role assigned if not found
    }

    @Test
    @DisplayName("Should not assign ROLE_ADMIN on user registration")
    void testSave_NoAdminRole() {
        // Given
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.save(testUser);

        // Then
        verify(roleRepository, never()).findByName("ROLE_ADMIN");
        assertTrue(testUser.getRoles().stream()
                .noneMatch(role -> "ROLE_ADMIN".equals(role.getName())));
    }

    @Test
    @DisplayName("Should update user profile")
    void testUpdate_Success() {
        // Given
        testUser.setUserEmail("newemail@example.com");
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        userService.update(testUser);

        // Then
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername_Success() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(testUser);

        // When
        User foundUser = userService.findByUsername(username);

        // Then
        assertNotNull(foundUser);
        assertEquals(username, foundUser.getUsername());
        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Should return null when user not found by username")
    void testFindByUsername_NotFound() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(null);

        // When
        User foundUser = userService.findByUsername(username);

        // Then
        assertNull(foundUser);
        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Should find user by ID")
    void testFindById_Success() {
        // Given
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(testUser);

        // When
        User foundUser = userService.findById(userId);

        // Then
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should get list of all users")
    void testGetList_Success() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getList();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void testGetList_EmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<User> result = userService.getList();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should handle special characters in password encoding")
    void testSave_SpecialCharactersInPassword() {
        // Given
        String specialPassword = "P@ssw0rd!#$%^&*()";
        testUser.setPassword(specialPassword);

        when(bCryptPasswordEncoder.encode(specialPassword)).thenReturn("encodedSpecialPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.save(testUser);

        // Then
        verify(bCryptPasswordEncoder).encode(specialPassword);
        assertEquals("encodedSpecialPassword", testUser.getPassword());
    }
}
