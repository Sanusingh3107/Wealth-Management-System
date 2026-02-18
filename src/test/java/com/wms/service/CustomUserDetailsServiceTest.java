package com.wms.service;

import com.wms.entity.User;
import com.wms.entity.User.Role;
import com.wms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CustomUserDetailsService
 * Achieves 100% code coverage for all CustomUserDetailsService methods
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

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
        testUser.setAccountNonExpired(true);
        testUser.setAccountNonLocked(true);
        testUser.setCredentialsNonExpired(true);
        
        HashSet<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_ADVISOR);
        testUser.setRoles(roles);
    }

    @Test
    @DisplayName("Should load user by username successfully")
    void loadUserByUsername_Success() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        assertEquals(1, result.getAuthorities().size());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void loadUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername("unknown"));

        assertEquals("User not found with username: unknown", exception.getMessage());
    }

    @Test
    @DisplayName("Should return user with multiple roles")
    void loadUserByUsername_MultipleRoles() {
        testUser.addRole(Role.ROLE_ADMIN);
        testUser.addRole(Role.ROLE_COMPLIANCE_OFFICER);

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

        assertEquals(3, result.getAuthorities().size());
    }

    @Test
    @DisplayName("Should return disabled user correctly")
    void loadUserByUsername_DisabledUser() {
        testUser.setEnabled(false);

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

        assertFalse(result.isEnabled());
    }

    @Test
    @DisplayName("Should return locked user correctly")
    void loadUserByUsername_LockedUser() {
        testUser.setAccountNonLocked(false);

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

        assertFalse(result.isAccountNonLocked());
    }

    @Test
    @DisplayName("Should return expired user correctly")
    void loadUserByUsername_ExpiredUser() {
        testUser.setAccountNonExpired(false);

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

        assertFalse(result.isAccountNonExpired());
    }

    @Test
    @DisplayName("Should return user with expired credentials correctly")
    void loadUserByUsername_ExpiredCredentials() {
        testUser.setCredentialsNonExpired(false);

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("johndoe");

        assertFalse(result.isCredentialsNonExpired());
    }
}
