package com.wms.service;

import com.wms.entity.User;
import com.wms.entity.User.Role;
import com.wms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ========================================
 * USER SERVICE
 * ========================================
 * 
 */
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // ========================================
    // CREATE OPERATIONS
    // ========================================
    
    /**
     * REGISTER NEW USER
     * -----------------
     * Creates a new user with encoded password.
     */
    public User registerUser(User user) {
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default role if none provided
        if (user.getRoles().isEmpty()) {
            user.addRole(Role.ROLE_ADVISOR);
        }
        
        return userRepository.save(user);
    }
    
    /**
     * REGISTER USER WITH ROLE
     * -----------------------
     */
    public User registerUser(String username, String password, String email, 
                            String fullName, Role role) {
        User user = new User(username, password, email, fullName);
        user.addRole(role);
        return registerUser(user);
    }
    
    // ========================================
    // READ OPERATIONS
    // ========================================
    
    /**
     * GET USER BY ID
     * --------------
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(Objects.requireNonNull(userId));
    }
    
    /**
     * GET USER BY USERNAME
     * --------------------
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * GET USER BY EMAIL
     * -----------------
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * GET ALL USERS
     * -------------
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * GET USERS BY ROLE
     * -----------------
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * GET ENABLED USERS
     * -----------------
     */
    @Transactional(readOnly = true)
    public List<User> getEnabledUsers() {
        return userRepository.findByEnabledTrue();
    }
    
    /**
     * SEARCH USERS
     * ------------
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        return userRepository.searchUsers(searchTerm);
    }
    
    // ========================================
    // UPDATE OPERATIONS
    // ========================================
    
    /**
     * UPDATE USER PROFILE
     * -------------------
     */
    public User updateUserProfile(Long userId, User updatedUser) {
        User existing = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Check email uniqueness if changed
        if (!existing.getEmail().equals(updatedUser.getEmail()) 
                && userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new RuntimeException("Email already exists: " + updatedUser.getEmail());
        }
        
        existing.setEmail(updatedUser.getEmail());
        existing.setFullName(updatedUser.getFullName());
        
        return userRepository.save(existing);
    }
    
    /**
     * CHANGE PASSWORD
     * ---------------
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Encode and set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    /**
     * RESET PASSWORD (Admin function)
     * --------------------------------
     */
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    /**
     * UPDATE LAST LOGIN
     * -----------------
     */
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLoginTime(userId, LocalDateTime.now());
    }
    
    // ========================================
    // ROLE MANAGEMENT
    // ========================================
    
    /**
     * ADD ROLE TO USER
     * ----------------
     */
    public User addRoleToUser(Long userId, Role role) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.addRole(role);
        return userRepository.save(user);
    }
    
    /**
     * REMOVE ROLE FROM USER
     * ---------------------
     */
    public User removeRoleFromUser(Long userId, Role role) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.removeRole(role);
        return userRepository.save(user);
    }
    
    // ========================================
    // ACCOUNT STATUS MANAGEMENT
    // ========================================
    
    /**
     * ENABLE USER
     * -----------
     */
    public void enableUser(Long userId) {
        userRepository.enableUser(userId);
    }
    
    /**
     * DISABLE USER
     * ------------
     */
    public void disableUser(Long userId) {
        userRepository.disableUser(userId);
    }
    
    /**
     * LOCK USER
     * ---------
     */
    public void lockUser(Long userId) {
        userRepository.lockUser(userId);
    }
    
    /**
     * UNLOCK USER
     * -----------
     */
    public void unlockUser(Long userId) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }
    
    // ========================================
    // DELETE OPERATIONS
    // ========================================
    
    /**
     * DELETE USER
     * -----------
     */
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(Objects.requireNonNull(userId))) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }
    
    // ========================================
    // VALIDATION HELPERS
    // ========================================
    
    /**
     * CHECK IF USERNAME EXISTS
     * ------------------------
     */
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * CHECK IF EMAIL EXISTS
     * ---------------------
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * COUNT USERS
     * -----------
     */
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }
    
    /**
     * COUNT BY ROLE
     * -------------
     */
    @Transactional(readOnly = true)
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }
}
