package com.wms.repository;

import com.wms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ========================================
 * USER REPOSITORY
 * ========================================
 * 
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * FIND BY USERNAME
     * ----------------
     * THE MOST IMPORTANT METHOD!
     * Spring Security calls this during login to load user details.
     */
    Optional<User> findByUsername(String username);
    
    /**
     * FIND BY EMAIL
     * -------------
     * Alternative login method (login with email).
     */
    Optional<User> findByEmail(String email);
    
    /**
     * CHECK IF USERNAME EXISTS
     * ------------------------
     * Used during registration to prevent duplicate usernames.
     */
    boolean existsByUsername(String username);
    
    /**
     * CHECK IF EMAIL EXISTS
     * ---------------------
     */
    boolean existsByEmail(String email);
    
    /**
     * FIND ENABLED USERS
     * ------------------
     * Gets only active users who can log in.
     */
    List<User> findByEnabledTrue();
    
    /**
     * FIND DISABLED USERS
     * -------------------
     * Gets deactivated users.
     */
    List<User> findByEnabledFalse();
    
    /**
     * FIND USERS BY ROLE
     * ------------------
     * Custom query because roles are stored in a collection.
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") User.Role role);
    
    /**
     * FIND ADMINS
     * -----------
     */
    default List<User> findAdmins() {
        return findByRole(User.Role.ROLE_ADMIN);
    }
    
    /**
     * UPDATE LAST LOGIN TIME
     * ----------------------
     * Called when user successfully logs in.
     * @Modifying indicates this query changes data.
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.userId = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    /**
     * DISABLE USER ACCOUNT
     * --------------------
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = false WHERE u.userId = :userId")
    void disableUser(@Param("userId") Long userId);
    
    /**
     * ENABLE USER ACCOUNT
     * -------------------
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = true WHERE u.userId = :userId")
    void enableUser(@Param("userId") Long userId);
    
    /**
     * LOCK USER ACCOUNT
     * -----------------
     * After too many failed login attempts.
     */
    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = false WHERE u.userId = :userId")
    void lockUser(@Param("userId") Long userId);
    
    /**
     * SEARCH USERS
     * ------------
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:term% OR u.email LIKE %:term% OR u.fullName LIKE %:term%")
    List<User> searchUsers(@Param("term") String searchTerm);
    
    /**
     * COUNT USERS BY ROLE
     * -------------------
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") User.Role role);
    
    /**
     * FIND USERS WHO HAVEN'T LOGGED IN RECENTLY
     * -----------------------------------------
     * For security - inactive accounts might need review.
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
}
