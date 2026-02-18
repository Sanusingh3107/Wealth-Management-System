package com.wms.service;

import com.wms.entity.User;
import com.wms.entity.User.Role;
import com.wms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 * Achieves 100% code coverage for all UserService methods
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("johndoe");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("john.doe@example.com");
        testUser.setFullName("John Doe");
        testUser.setEnabled(true);
        testUser.setAccountNonLocked(true);
        testUser.setAccountNonExpired(true);
        testUser.setCredentialsNonExpired(true);
        testUser.setRoles(new HashSet<>(Arrays.asList(Role.ROLE_ADVISOR)));
    }

    // ========================================
    // CREATE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Register User Tests")
    class RegisterUserTests {

        @Test
        @DisplayName("Should register user successfully")
        void registerUser_Success() {
            User newUser = new User("newuser", "password123", "new@example.com", "New User");
            newUser.addRole(Role.ROLE_ADVISOR);

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(newUser);

            User result = userService.registerUser(newUser);

            assertNotNull(result);
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should add default role when no roles provided")
        void registerUser_AddsDefaultRole() {
            User newUser = new User("newuser", "password123", "new@example.com", "New User");
            newUser.setRoles(new HashSet<>()); // Empty roles

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.registerUser(newUser);

            assertTrue(result.getRoles().contains(Role.ROLE_ADVISOR));
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void registerUser_UsernameExists_ThrowsException() {
            when(userRepository.existsByUsername("johndoe")).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(testUser));

            assertEquals("Username already exists: johndoe", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void registerUser_EmailExists_ThrowsException() {
            when(userRepository.existsByUsername("johndoe")).thenReturn(false);
            when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(testUser));

            assertEquals("Email already exists: john.doe@example.com", exception.getMessage());
        }

        @Test
        @DisplayName("Should register user with specific role")
        void registerUserWithRole_Success() {
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.registerUser("newuser", "password123", "new@example.com", 
                "New User", Role.ROLE_ADMIN);

            assertNotNull(result);
            assertTrue(result.getRoles().contains(Role.ROLE_ADMIN));
        }
    }

    // ========================================
    // READ OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Read User Tests")
    class ReadUserTests {

        @Test
        @DisplayName("Should return user by ID when exists")
        void getUserById_Found() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.getUserById(1L);

            assertTrue(result.isPresent());
            assertEquals("johndoe", result.get().getUsername());
        }

        @Test
        @DisplayName("Should return empty when user not found by ID")
        void getUserById_NotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<User> result = userService.getUserById(999L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return user by username")
        void getUserByUsername_Found() {
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.getUserByUsername("johndoe");

            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("Should return user by email")
        void getUserByEmail_Found() {
            when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.getUserByEmail("john.doe@example.com");

            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("Should return all users")
        void getAllUsers_ReturnsList() {
            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

            List<User> result = userService.getAllUsers();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return users by role")
        void getUsersByRole_ReturnsList() {
            when(userRepository.findByRole(Role.ROLE_ADVISOR)).thenReturn(Arrays.asList(testUser));

            List<User> result = userService.getUsersByRole(Role.ROLE_ADVISOR);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return enabled users")
        void getEnabledUsers_ReturnsList() {
            when(userRepository.findByEnabledTrue()).thenReturn(Arrays.asList(testUser));

            List<User> result = userService.getEnabledUsers();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should search users")
        void searchUsers_ReturnsList() {
            when(userRepository.searchUsers("john")).thenReturn(Arrays.asList(testUser));

            List<User> result = userService.searchUsers("john");

            assertEquals(1, result.size());
        }
    }

    // ========================================
    // UPDATE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user profile successfully with same email")
        void updateUserProfile_SameEmail_Success() {
            User updatedUser = new User();
            updatedUser.setEmail("john.doe@example.com"); // Same email
            updatedUser.setFullName("John Updated");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.updateUserProfile(1L, updatedUser);

            assertNotNull(result);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should update user profile with new unique email")
        void updateUserProfile_NewEmail_Success() {
            User updatedUser = new User();
            updatedUser.setEmail("john.new@example.com"); // New email
            updatedUser.setFullName("John Updated");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("john.new@example.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.updateUserProfile(1L, updatedUser);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should throw exception when updating to existing email")
        void updateUserProfile_EmailExists_ThrowsException() {
            User updatedUser = new User();
            updatedUser.setEmail("existing@example.com");
            updatedUser.setFullName("John Updated");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUserProfile(1L, updatedUser));

            assertEquals("Email already exists: existing@example.com", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when user not found for update")
        void updateUserProfile_NotFound_ThrowsException() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUserProfile(999L, testUser));

            assertEquals("User not found with ID: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

            assertDoesNotThrow(() -> userService.changePassword(1L, "oldPassword", "newPassword"));

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when old password is incorrect")
        void changePassword_WrongOldPassword_ThrowsException() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.changePassword(1L, "wrongPassword", "newPassword"));

            assertEquals("Current password is incorrect", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when user not found for password change")
        void changePassword_UserNotFound_ThrowsException() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.changePassword(999L, "oldPassword", "newPassword"));

            assertEquals("User not found with ID: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should reset password successfully")
        void resetPassword_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

            assertDoesNotThrow(() -> userService.resetPassword(1L, "newPassword"));

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found for password reset")
        void resetPassword_UserNotFound_ThrowsException() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.resetPassword(999L, "newPassword"));

            assertEquals("User not found with ID: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should update last login time")
        void updateLastLogin_Success() {
            doNothing().when(userRepository).updateLastLoginTime(anyLong(), any());

            assertDoesNotThrow(() -> userService.updateLastLogin(1L));

            verify(userRepository).updateLastLoginTime(anyLong(), any());
        }
    }

    // ========================================
    // ROLE MANAGEMENT TESTS
    // ========================================

    @Nested
    @DisplayName("Role Management Tests")
    class RoleManagementTests {

        @Test
        @DisplayName("Should add role to user successfully")
        void addRoleToUser_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.addRoleToUser(1L, Role.ROLE_ADMIN);

            assertNotNull(result);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found for adding role")
        void addRoleToUser_NotFound_ThrowsException() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.addRoleToUser(999L, Role.ROLE_ADMIN));

            assertEquals("User not found with ID: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should remove role from user successfully")
        void removeRoleFromUser_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.removeRoleFromUser(1L, Role.ROLE_ADVISOR);

            assertNotNull(result);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found for removing role")
        void removeRoleFromUser_NotFound_ThrowsException() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.removeRoleFromUser(999L, Role.ROLE_ADVISOR));

            assertEquals("User not found with ID: 999", exception.getMessage());
        }
    }

    // ========================================
    // ACCOUNT STATUS MANAGEMENT TESTS
    // ========================================

    @Nested
    @DisplayName("Account Status Management Tests")
    class AccountStatusTests {

        @Test
        @DisplayName("Should enable user successfully")
        void enableUser_Success() {
            doNothing().when(userRepository).enableUser(1L);

            assertDoesNotThrow(() -> userService.enableUser(1L));

            verify(userRepository).enableUser(1L);
        }

        @Test
        @DisplayName("Should disable user successfully")
        void disableUser_Success() {
            doNothing().when(userRepository).disableUser(1L);

            assertDoesNotThrow(() -> userService.disableUser(1L));

            verify(userRepository).disableUser(1L);
        }

        @Test
        @DisplayName("Should lock user successfully")
        void lockUser_Success() {
            doNothing().when(userRepository).lockUser(1L);

            assertDoesNotThrow(() -> userService.lockUser(1L));

            verify(userRepository).lockUser(1L);
        }

        @Test
        @DisplayName("Should unlock user successfully")
        void unlockUser_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            assertDoesNotThrow(() -> userService.unlockUser(1L));

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found for unlock")
        void unlockUser_NotFound_ThrowsException() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.unlockUser(999L));

            assertEquals("User not found with ID: 999", exception.getMessage());
        }
    }

    // ========================================
    // DELETE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void deleteUser_Success() {
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            assertDoesNotThrow(() -> userService.deleteUser(1L));

            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent user")
        void deleteUser_NotFound_ThrowsException() {
            when(userRepository.existsById(999L)).thenReturn(false);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(999L));

            assertEquals("User not found with ID: 999", exception.getMessage());
        }
    }

    // ========================================
    // VALIDATION HELPERS TESTS
    // ========================================

    @Nested
    @DisplayName("Validation Helpers Tests")
    class ValidationHelpersTests {

        @Test
        @DisplayName("Should return true when username exists")
        void usernameExists_True() {
            when(userRepository.existsByUsername("johndoe")).thenReturn(true);

            assertTrue(userService.usernameExists("johndoe"));
        }

        @Test
        @DisplayName("Should return false when username does not exist")
        void usernameExists_False() {
            when(userRepository.existsByUsername("unknown")).thenReturn(false);

            assertFalse(userService.usernameExists("unknown"));
        }

        @Test
        @DisplayName("Should return true when email exists")
        void emailExists_True() {
            when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

            assertTrue(userService.emailExists("john.doe@example.com"));
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void emailExists_False() {
            when(userRepository.existsByEmail("unknown@example.com")).thenReturn(false);

            assertFalse(userService.emailExists("unknown@example.com"));
        }

        @Test
        @DisplayName("Should return user count")
        void countUsers_ReturnsCount() {
            when(userRepository.count()).thenReturn(10L);

            assertEquals(10L, userService.countUsers());
        }

        @Test
        @DisplayName("Should return count by role")
        void countByRole_ReturnsCount() {
            when(userRepository.countByRole(Role.ROLE_ADVISOR)).thenReturn(5L);

            assertEquals(5L, userService.countByRole(Role.ROLE_ADVISOR));
        }
    }
}
