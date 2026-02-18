package com.wms.entity;

import com.wms.entity.User.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity
 * Tests constructors, getters, setters, helper methods, and lifecycle callbacks
 */
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("johndoe");
        user.setPassword("encodedPassword");
        user.setEmail("john.doe@example.com");
        user.setFullName("John Doe");
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        
        HashSet<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_ADVISOR);
        user.setRoles(roles);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create user with default constructor")
        void defaultConstructor() {
            User u = new User();
            assertNotNull(u);
            assertNotNull(u.getRoles());
        }

        @Test
        @DisplayName("Should create user with parameterized constructor")
        void parameterizedConstructor() {
            User u = new User("newuser", "password123", "new@example.com", "New User");
            
            assertEquals("newuser", u.getUsername());
            assertEquals("password123", u.getPassword());
            assertEquals("new@example.com", u.getEmail());
            assertEquals("New User", u.getFullName());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set userId")
        void userId() {
            user.setUserId(100L);
            assertEquals(100L, user.getUserId());
        }

        @Test
        @DisplayName("Should get and set username")
        void username() {
            user.setUsername("newusername");
            assertEquals("newusername", user.getUsername());
        }

        @Test
        @DisplayName("Should get and set password")
        void password() {
            user.setPassword("newPassword");
            assertEquals("newPassword", user.getPassword());
        }

        @Test
        @DisplayName("Should get and set email")
        void email() {
            user.setEmail("new@example.com");
            assertEquals("new@example.com", user.getEmail());
        }

        @Test
        @DisplayName("Should get and set fullName")
        void fullName() {
            user.setFullName("New Name");
            assertEquals("New Name", user.getFullName());
        }

        @Test
        @DisplayName("Should get and set roles")
        void roles() {
            HashSet<Role> newRoles = new HashSet<>();
            newRoles.add(Role.ROLE_ADMIN);
            user.setRoles(newRoles);
            assertTrue(user.getRoles().contains(Role.ROLE_ADMIN));
        }

        @Test
        @DisplayName("Should get and set enabled")
        void enabled() {
            user.setEnabled(false);
            assertFalse(user.isEnabled());
        }

        @Test
        @DisplayName("Should get and set accountNonExpired")
        void accountNonExpired() {
            user.setAccountNonExpired(false);
            assertFalse(user.isAccountNonExpired());
        }

        @Test
        @DisplayName("Should get and set accountNonLocked")
        void accountNonLocked() {
            user.setAccountNonLocked(false);
            assertFalse(user.isAccountNonLocked());
        }

        @Test
        @DisplayName("Should get and set credentialsNonExpired")
        void credentialsNonExpired() {
            user.setCredentialsNonExpired(false);
            assertFalse(user.isCredentialsNonExpired());
        }
    }

    @Nested
    @DisplayName("Helper Methods Tests")
    class HelperMethodsTests {

        @Test
        @DisplayName("Should add role successfully")
        void addRole() {
            user.addRole(Role.ROLE_ADMIN);
            assertTrue(user.getRoles().contains(Role.ROLE_ADMIN));
        }

        @Test
        @DisplayName("Should remove role successfully")
        void removeRole() {
            user.removeRole(Role.ROLE_ADVISOR);
            assertFalse(user.getRoles().contains(Role.ROLE_ADVISOR));
        }

        @Test
        @DisplayName("Should return true when user has role")
        void hasRole_True() {
            assertTrue(user.hasRole(Role.ROLE_ADVISOR));
        }

        @Test
        @DisplayName("Should return false when user does not have role")
        void hasRole_False() {
            assertFalse(user.hasRole(Role.ROLE_ADMIN));
        }

        @Test
        @DisplayName("Should return true when user is admin")
        void isAdmin_True() {
            user.addRole(Role.ROLE_ADMIN);
            assertTrue(user.isAdmin());
        }

        @Test
        @DisplayName("Should return false when user is not admin")
        void isAdmin_False() {
            assertFalse(user.isAdmin());
        }
    }

    @Nested
    @DisplayName("Role Enum Tests")
    class RoleEnumTests {

        @Test
        @DisplayName("Should have all expected roles")
        void allRoles() {
            assertEquals(4, Role.values().length);
            assertNotNull(Role.ROLE_ADMIN);
            assertNotNull(Role.ROLE_ADVISOR);
            assertNotNull(Role.ROLE_ANALYST);
            assertNotNull(Role.ROLE_COMPLIANCE_OFFICER);
        }
    }

    @Nested
    @DisplayName("Lifecycle Callback Tests")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("Should set createdAt on onCreate")
        void onCreate() {
            User newUser = new User();
            assertNull(newUser.getCreatedAt());
            newUser.onCreate();
            assertNotNull(newUser.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should get and set createdAt")
        void createdAt() {
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            assertEquals(now, user.getCreatedAt());
        }

        @Test
        @DisplayName("Should get and set lastLoginAt")
        void lastLoginAt() {
            LocalDateTime now = LocalDateTime.now();
            user.setLastLoginAt(now);
            assertEquals(now, user.getLastLoginAt());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return meaningful string representation")
        void toStringTest() {
            String result = user.toString();
            assertNotNull(result);
            assertTrue(result.contains("johndoe"));
            assertTrue(result.contains("john.doe@example.com"));
            assertTrue(result.contains("John Doe"));
        }
    }
}
