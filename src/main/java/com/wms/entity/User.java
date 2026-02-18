package com.wms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * ========================================
 * USER ENTITY
 * ========================================
 * 
 * Spring Security uses this entity to authenticate users and check
 * what they're allowed to do based on their roles."
 */
@Entity
@Table(name = "users")  // Using 'users' because 'user' is reserved in some databases
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    
    /**
     * USERNAME
     * --------
     * Unique identifier for login.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * PASSWORD
     * --------
     * Stored as a BCrypt hash, not plain text.
     * BCrypt automatically handles salting.
     */
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;
    
    /**
     * EMAIL
     * -----
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    /**
     * FULL NAME
     * ---------
     */
    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String fullName;
    
    /**
     * ROLES
     * -----
     * A user can have multiple roles.
     * @ElementCollection creates a separate table for roles.
     * EAGER loading means roles are loaded immediately with the user.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();
    
    /**
     * ACCOUNT STATUS FLAGS
     * --------------------
     * These control whether the user can log in.
     */
    @Column(nullable = false)
    private boolean enabled = true;  // Account is active
    
    @Column(nullable = false)
    private boolean accountNonExpired = true;  // Account hasn't expired
    
    @Column(nullable = false)
    private boolean accountNonLocked = true;  // Account isn't locked
    
    @Column(nullable = false)
    private boolean credentialsNonExpired = true;  // Password hasn't expired
    
    /**
     * TIMESTAMPS
     * ----------
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime lastLoginAt;
    
    // ========================================
    // ENUM
    // ========================================
    
    public enum Role {
        ROLE_ADMIN,             // Full system access
        ROLE_ADVISOR,           // Manages clients and portfolios
        ROLE_ANALYST,           // Creates reports and analysis
        ROLE_COMPLIANCE_OFFICER // Monitors compliance and audit logs
    }
    
    // ========================================
    // LIFECYCLE CALLBACKS
    // ========================================
    
    /**
     * @PrePersist runs before the entity is saved for the first time.
     * We use it to set the creation timestamp automatically.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    public User() {
    }
    
    public User(String username, String password, String email, String fullName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
    }
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    /**
     * Add a role to the user.
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    /**
     * Remove a role from the user.
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }
    
    /**
     * Check if user has a specific role.
     */
    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }
    
    /**
     * Check if user is an admin.
     */
    public boolean isAdmin() {
        return this.roles.contains(Role.ROLE_ADMIN);
    }
    
    // ========================================
    // GETTERS AND SETTERS
    // ========================================
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }
    
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }
    
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", roles=" + roles +
                ", enabled=" + enabled +
                '}';
    }
}
