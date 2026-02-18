package com.wms.config;

import com.wms.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ========================================
 * SECURITY CONFIGURATION
 * ========================================
 * 
 * This class configures Spring Security for our application.
 */
@Configuration  // Tells Spring this is a configuration class
@EnableWebSecurity  // Enables Spring Security features
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    
    // Constructor Injection - Spring automatically provides the service
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * PASSWORD ENCODER
     * ----------------
     * BCrypt is a one-way hashing algorithm that:
     * - Converts plain text password to a hash
     * - Cannot be reversed (secure)
     * - Same password produces different hashes (due to salt)
     * 
     * Example: "password123" -> "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * AUTHENTICATION PROVIDER
     * -----------------------
     * This tells Spring Security:
     * - WHERE to find user information (UserDetailsService)
     * - HOW to verify passwords (PasswordEncoder)
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    /**
     * AUTHENTICATION MANAGER
     * ----------------------
     * The main entry point for authentication.
     * It coordinates the authentication process.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * SECURITY FILTER CHAIN
     * ---------------------
     * This is the MAIN security configuration.
     * It defines which URLs require authentication and authorization.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // PUBLIC PAGES - Anyone can access these without login
                .requestMatchers(
                    "/",                    // Home page
                    "/login",               // Login page
                    "/register",            // Registration page
                    "/css/**",              // CSS files
                    "/js/**",               // JavaScript files
                    "/images/**",           // Images
                    "/webjars/**"           // Web dependencies
                ).permitAll()
                
                // ADMIN ONLY PAGES - Only users with ADMIN role can access
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // COMPLIANCE PAGES - Only ADMIN and COMPLIANCE_OFFICER can access
                .requestMatchers("/compliance/**").hasAnyRole("ADMIN", "COMPLIANCE_OFFICER")
                
                // ALL OTHER PAGES - Require authentication (must be logged in)
                .anyRequest().authenticated()
            )
            
            // Configure form-based login
            .formLogin(form -> form
                .loginPage("/login")                    // Custom login page URL
                .loginProcessingUrl("/login")           // URL to submit login form
                .defaultSuccessUrl("/dashboard", true)  // Where to go after successful login
                .failureUrl("/login?error=true")        // Where to go after failed login
                .usernameParameter("username")          // Form field name for username
                .passwordParameter("password")          // Form field name for password
                .permitAll()                            // Allow everyone to see login page
            )
            
            // Configure logout
            .logout(logout -> logout
                .logoutUrl("/logout")                   // URL to trigger logout
                .logoutSuccessUrl("/login?logout=true") // Where to go after logout
                .invalidateHttpSession(true)            // Clear session data
                .deleteCookies("JSESSIONID")            // Delete session cookie
                .permitAll()
            )
            
            // Use our custom authentication provider
            .authenticationProvider(authenticationProvider());
        
        return http.build();
    }
}
