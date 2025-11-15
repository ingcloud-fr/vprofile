package com.visualpathit.account.validatorTest;

import com.visualpathit.account.model.User;
import com.visualpathit.account.service.UserService;
import com.visualpathit.account.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserValidator
 * Tests validation rules for user registration
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserValidator Unit Tests")
class UserValidatorTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserValidator userValidator;

    private User testUser;
    private Errors errors;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("validuser");
        testUser.setPassword("validpassword123");
        testUser.setPasswordConfirm("validpassword123");
        testUser.setUserEmail("valid@example.com");

        errors = new BeanPropertyBindingResult(testUser, "user");
    }

    @Test
    @DisplayName("Should support User class")
    void testSupports_UserClass() {
        // When/Then
        assertTrue(userValidator.supports(User.class));
    }

    @Test
    @DisplayName("Should not support other classes")
    void testSupports_OtherClass() {
        // When/Then
        assertFalse(userValidator.supports(String.class));
        assertFalse(userValidator.supports(Object.class));
    }

    @Test
    @DisplayName("Should pass validation for valid user")
    void testValidate_ValidUser() {
        // Given
        when(userService.findByUsername(testUser.getUsername())).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertFalse(errors.hasErrors());
    }

    // ========== USERNAME VALIDATION TESTS ==========

    @Test
    @DisplayName("Should reject empty username")
    void testValidate_EmptyUsername() {
        // Given
        testUser.setUsername("");

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("username"));
        assertEquals("NotEmpty", errors.getFieldError("username").getCode());
    }

    @Test
    @DisplayName("Should reject null username")
    void testValidate_NullUsername() {
        // Given
        testUser.setUsername(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("username"));
    }

    @Test
    @DisplayName("Should reject whitespace-only username")
    void testValidate_WhitespaceUsername() {
        // Given
        testUser.setUsername("   ");

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("username"));
        assertEquals("NotEmpty", errors.getFieldError("username").getCode());
    }

    @Test
    @DisplayName("Should reject username shorter than 5 characters")
    void testValidate_UsernameTooShort() {
        // Given
        testUser.setUsername("abc");
        when(userService.findByUsername("abc")).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("username"));
        assertEquals("Size.userForm.username", errors.getFieldError("username").getCode());
    }

    @Test
    @DisplayName("Should accept username with exactly 5 characters")
    void testValidate_UsernameMinLength() {
        // Given
        testUser.setUsername("abcde");
        when(userService.findByUsername("abcde")).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertFalse(errors.hasFieldErrors("username"));
    }

    @Test
    @DisplayName("Should accept username with exactly 32 characters")
    void testValidate_UsernameMaxLength() {
        // Given
        String maxUsername = "a".repeat(32);
        testUser.setUsername(maxUsername);
        when(userService.findByUsername(maxUsername)).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertFalse(errors.hasFieldErrors("username"));
    }

    @Test
    @DisplayName("Should reject username longer than 32 characters")
    void testValidate_UsernameTooLong() {
        // Given
        String longUsername = "a".repeat(33);
        testUser.setUsername(longUsername);
        when(userService.findByUsername(longUsername)).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("username"));
        assertEquals("Size.userForm.username", errors.getFieldError("username").getCode());
    }

    @Test
    @DisplayName("Should reject duplicate username")
    void testValidate_DuplicateUsername() {
        // Given
        User existingUser = new User();
        existingUser.setUsername("validuser");
        when(userService.findByUsername("validuser")).thenReturn(existingUser);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("username"));
        assertEquals("Duplicate.userForm.username", errors.getFieldError("username").getCode());
    }

    // ========== PASSWORD VALIDATION TESTS ==========

    @Test
    @DisplayName("Should reject empty password")
    void testValidate_EmptyPassword() {
        // Given
        testUser.setPassword("");
        testUser.setPasswordConfirm("");
        when(userService.findByUsername(testUser.getUsername())).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("password"));
        assertEquals("NotEmpty", errors.getFieldError("password").getCode());
    }

    @Test
    @DisplayName("Should reject null password")
    void testValidate_NullPassword() {
        // Given
        testUser.setPassword(null);
        testUser.setPasswordConfirm(null);
        when(userService.findByUsername(testUser.getUsername())).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("password"));
    }

    @Test
    @DisplayName("Should reject whitespace-only password")
    void testValidate_WhitespacePassword() {
        // Given
        testUser.setPassword("   ");
        testUser.setPasswordConfirm("   ");
        when(userService.findByUsername(testUser.getUsername())).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("password"));
        assertEquals("NotEmpty", errors.getFieldError("password").getCode());
    }

    @Test
    @DisplayName("Should reject password shorter than 8 characters")
    void testValidate_PasswordTooShort() {
        // Given
        testUser.setPassword("short");
        testUser.setPasswordConfirm("short");
        when(userService.findByUsername(testUser.getUsername())).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("password"));
        assertEquals("Size.userForm.password", errors.getFieldError("password").getCode());
    }

    @Test
    @DisplayName("Should accept password with exactly 8 characters")
    void testValidate_PasswordMinLength() {
        // Given
        testUser.setPassword("12345678");
        testUser.setPasswordConfirm("12345678");
        when(userService.findByUsername(testUser.getUsername())).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertFalse(errors.hasFieldErrors("password"));
    }

    @Test
    @DisplayName("Should accept password with exactly 32 characters")
    void testValidate_PasswordMaxLength() {
        // Given
        String maxPassword = "a".repeat(32);
        testUser.setPassword(maxPassword);
        testUser.setPasswordConfirm(maxPassword);
        when(userService.findByUsername(testUser.getUsername())).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertFalse(errors.hasFieldErrors("password"));
    }

    @Test
    @DisplayName("Should reject password longer than 32 characters")
    void testValidate_PasswordTooLong() {
        // Given
        String longPassword = "a".repeat(33);
        testUser.setPassword(longPassword);
        testUser.setPasswordConfirm(longPassword);
        when(userService.findByUsername(testUser.getUsername())).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("password"));
        assertEquals("Size.userForm.password", errors.getFieldError("password").getCode());
    }

    // ========== PASSWORD CONFIRMATION TESTS ==========

    @Test
    @DisplayName("Should reject mismatched password confirmation")
    void testValidate_PasswordMismatch() {
        // Given
        testUser.setPassword("password123");
        testUser.setPasswordConfirm("different456");
        when(userService.findByUsername(testUser.getUsername())).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("passwordConfirm"));
        assertEquals("Diff.userForm.passwordConfirm", errors.getFieldError("passwordConfirm").getCode());
    }

    @Test
    @DisplayName("Should accept matching password confirmation")
    void testValidate_PasswordMatch() {
        // Given
        testUser.setPassword("password123");
        testUser.setPasswordConfirm("password123");
        when(userService.findByUsername(testUser.getUsername())).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertFalse(errors.hasFieldErrors("passwordConfirm"));
    }

    @Test
    @DisplayName("Should accept password with special characters")
    void testValidate_PasswordWithSpecialChars() {
        // Given
        testUser.setPassword("P@ssw0rd!#$%");
        testUser.setPasswordConfirm("P@ssw0rd!#$%");
        when(userService.findByUsername(testUser.getUsername())).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertFalse(errors.hasFieldErrors("password"));
    }

    // ========== COMBINED VALIDATION TESTS ==========

    @Test
    @DisplayName("Should report multiple validation errors")
    void testValidate_MultipleErrors() {
        // Given
        testUser.setUsername("ab"); // Too short
        testUser.setPassword("123"); // Too short
        testUser.setPasswordConfirm("456"); // Mismatch
        when(userService.findByUsername("ab")).thenReturn(null);

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("username"));
        assertTrue(errors.hasFieldErrors("password"));
        assertTrue(errors.hasFieldErrors("passwordConfirm"));
        assertTrue(errors.getErrorCount() >= 3);
    }

    @Test
    @DisplayName("Should prioritize NotEmpty over Size validation")
    void testValidate_EmptyUsernameAndPassword() {
        // Given
        testUser.setUsername("");
        testUser.setPassword("");
        testUser.setPasswordConfirm("");

        // When
        userValidator.validate(testUser, errors);

        // Then
        assertTrue(errors.hasFieldErrors("username"));
        assertTrue(errors.hasFieldErrors("password"));
        // NotEmpty is checked before Size
        assertEquals("NotEmpty", errors.getFieldError("username").getCode());
        assertEquals("NotEmpty", errors.getFieldError("password").getCode());
    }
}
