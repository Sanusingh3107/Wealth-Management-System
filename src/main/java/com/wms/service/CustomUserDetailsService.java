package com.wms.service;

import com.wms.entity.User;
import com.wms.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * ========================================
 * CUSTOM USER DETAILS SERVICE
 * ========================================
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * CONSTRUCTOR INJECTION
     * ---------------------
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * LOAD USER BY USERNAME
     * ---------------------
     * This method is called by Spring Security during authentication.
     * 
     * @param username The username entered in the login form
     * @return UserDetails object that Spring Security uses
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    @Transactional(readOnly = true)  // Ensures the session stays open for lazy loading
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Step 1: Find user in database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));
        
        // Step 2: Convert our User to Spring Security's UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),                    // Username
                user.getPassword(),                    // Encoded password
                user.isEnabled(),                      // Is account enabled?
                user.isAccountNonExpired(),            // Has account expired?
                user.isCredentialsNonExpired(),        // Has password expired?
                user.isAccountNonLocked(),             // Is account locked?
                getAuthorities(user)                   // User's roles/permissions
        );
    }
    
    /**
     * GET AUTHORITIES
     * ---------------
     * Converts our User's roles to Spring Security's GrantedAuthority.
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }
}
