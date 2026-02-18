package com.wms.config;

import com.wms.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(userDetailsService);
    }

    @Nested
    @DisplayName("Password Encoder Tests")
    class PasswordEncoderTests {

        @Test
        @DisplayName("Should create BCryptPasswordEncoder")
        void passwordEncoder_ReturnsBCryptEncoder() {
            PasswordEncoder encoder = securityConfig.passwordEncoder();
            
            assertNotNull(encoder);
            assertTrue(encoder instanceof BCryptPasswordEncoder);
        }

        @Test
        @DisplayName("Should encode passwords correctly")
        void passwordEncoder_EncodesPassword() {
            PasswordEncoder encoder = securityConfig.passwordEncoder();
            
            String rawPassword = "testPassword123";
            String encodedPassword = encoder.encode(rawPassword);
            
            assertNotEquals(rawPassword, encodedPassword);
            assertTrue(encoder.matches(rawPassword, encodedPassword));
        }

        @Test
        @DisplayName("Should generate different hashes for same password")
        void passwordEncoder_GeneratesDifferentHashes() {
            PasswordEncoder encoder = securityConfig.passwordEncoder();
            
            String rawPassword = "testPassword123";
            String hash1 = encoder.encode(rawPassword);
            String hash2 = encoder.encode(rawPassword);
            
            assertNotEquals(hash1, hash2);
            assertTrue(encoder.matches(rawPassword, hash1));
            assertTrue(encoder.matches(rawPassword, hash2));
        }
    }

    @Nested
    @DisplayName("Authentication Provider Tests")
    class AuthenticationProviderTests {

        @Test
        @DisplayName("Should create DaoAuthenticationProvider")
        void authenticationProvider_CreatesDaoProvider() {
            DaoAuthenticationProvider provider = securityConfig.authenticationProvider();
            
            assertNotNull(provider);
        }
    }

    @Nested
    @DisplayName("Authentication Manager Tests")
    class AuthenticationManagerTests {

        @Test
        @DisplayName("Should create AuthenticationManager from configuration")
        void authenticationManager_CreatesFromConfig() throws Exception {
            when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);
            
            AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);
            
            assertNotNull(result);
            assertEquals(authenticationManager, result);
        }
    }
}
