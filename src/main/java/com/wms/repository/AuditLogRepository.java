package com.wms.repository;

import com.wms.entity.AuditLog;
import com.wms.entity.AuditLog.ComplianceStatus;
import com.wms.entity.AuditLog.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ========================================
 * AUDIT LOG REPOSITORY
 * ========================================
 * 
 * Key features:
 * - Query logs by user (who did what)
 * - Filter by event type (logins, transactions, etc.)
 * - Find compliance violations
 * - Date range queries for audits
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * FIND LOGS BY USER ID
     * --------------------
     */
    List<AuditLog> findByUserId(Long userId);
    
    /**
     * FIND LOGS BY USERNAME
     * ---------------------
     */
    List<AuditLog> findByUsername(String username);
    
    /**
     * FIND LOGS BY EVENT TYPE
     * -----------------------
     */
    List<AuditLog> findByEventType(EventType eventType);
    
    /**
     * FIND LOGS BY COMPLIANCE STATUS
     * ------------------------------
     * Critical for finding compliance violations!
     */
    List<AuditLog> findByComplianceStatus(ComplianceStatus status);
    
    /**
     * FIND COMPLIANCE FAILURES
     * ------------------------
     * Shortcut for finding all failed compliance checks.
     */
    default List<AuditLog> findComplianceFailures() {
        return findByComplianceStatus(ComplianceStatus.FAIL);
    }
    
    /**
     * FIND LOGS BY DATE RANGE
     * -----------------------
     */
    List<AuditLog> findByEventTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * FIND LOGS FOR SPECIFIC ENTITY
     * -----------------------------
     * Example: Find all logs related to Client ID 123
     */
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    /**
     * FIND RECENT LOGS WITH PAGINATION
     * --------------------------------
     * Pageable allows:
     * - Limiting results (e.g., 20 per page)
     * - Sorting (e.g., by timestamp descending)
     * - Offset/skip for pagination
     */
    Page<AuditLog> findAllByOrderByEventTimestampDesc(Pageable pageable);
    
    /**
     * FIND LOGS BY USER IN DATE RANGE
     * -------------------------------
     */
    List<AuditLog> findByUserIdAndEventTimestampBetween(
            Long userId, LocalDateTime start, LocalDateTime end);
    
    /**
     * COUNT LOGS BY EVENT TYPE
     * ------------------------
     */
    long countByEventType(EventType eventType);
    
    /**
     * COUNT COMPLIANCE FAILURES
     * -------------------------
     */
    long countByComplianceStatus(ComplianceStatus status);
    
    /**
     * FIND LOGS BY IP ADDRESS
     * -----------------------
     * Useful for security investigations.
     */
    List<AuditLog> findByIpAddress(String ipAddress);
    
    /**
     * SEARCH LOGS BY DESCRIPTION
     * --------------------------
     */
    List<AuditLog> findByEventDescriptionContainingIgnoreCase(String keyword);
    
    /**
     * FIND RECENT LOGIN ATTEMPTS FOR USER
     * -----------------------------------
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.eventType = 'LOGIN' ORDER BY a.eventTimestamp DESC")
    List<AuditLog> findRecentLoginAttempts(@Param("userId") Long userId);
    
    /**
     * GET ACTIVITY SUMMARY BY USER
     * ----------------------------
     * Returns count of events per event type for a user.
     */
    @Query("SELECT a.eventType, COUNT(a) FROM AuditLog a WHERE a.userId = :userId GROUP BY a.eventType")
    List<Object[]> getActivitySummaryByUser(@Param("userId") Long userId);
    
    /**
     * FIND SUSPICIOUS ACTIVITY
     * ------------------------
     * Multiple failed logins or compliance failures.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.complianceStatus = 'FAIL' OR " +
           "(a.eventType = 'LOGIN' AND a.eventDescription LIKE '%failed%') " +
           "ORDER BY a.eventTimestamp DESC")
    List<AuditLog> findSuspiciousActivity();
    
    /**
     * DELETE OLD LOGS
     * ---------------
     * For data retention policies (keeping logs for X years).
     */
    void deleteByEventTimestampBefore(LocalDateTime date);
}
