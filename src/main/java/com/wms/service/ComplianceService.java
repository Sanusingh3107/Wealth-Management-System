package com.wms.service;

import com.wms.entity.AuditLog;
import com.wms.entity.AuditLog.ComplianceStatus;
import com.wms.entity.AuditLog.EventType;
import com.wms.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ========================================
 * COMPLIANCE SERVICE
 * ========================================
 * 
 */
@Service
@Transactional
public class ComplianceService {
    
    private final AuditLogRepository auditLogRepository;
    
    public ComplianceService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    // ========================================
    // LOGGING OPERATIONS
    // ========================================
    
    /**
     * LOG AUDIT EVENT
     * ---------------
     * Records an audit event in the system.
     */
    public AuditLog logAuditEvent(AuditLog auditLog) {
        if (auditLog.getEventTimestamp() == null) {
            auditLog.setEventTimestamp(LocalDateTime.now());
        }
        if (auditLog.getComplianceStatus() == null) {
            auditLog.setComplianceStatus(ComplianceStatus.PASS);
        }
        return auditLogRepository.save(auditLog);
    }
    
    /**
     * LOG LOGIN EVENT
     * ---------------
     */
    public AuditLog logLogin(Long userId, String username, String ipAddress, boolean successful) {
        AuditLog log = new AuditLog();
        log.setEventType(EventType.LOGIN);
        log.setEventDescription(successful ? "User logged in successfully" : "Failed login attempt");
        log.setUserId(userId);
        log.setUsername(username);
        log.setIpAddress(ipAddress);
        log.setComplianceStatus(successful ? ComplianceStatus.PASS : ComplianceStatus.WARNING);
        return auditLogRepository.save(log);
    }
    
    /**
     * LOG LOGOUT EVENT
     * ----------------
     */
    public AuditLog logLogout(Long userId, String username) {
        AuditLog log = AuditLog.createLogoutEvent(userId, username);
        return auditLogRepository.save(Objects.requireNonNull(log));
    }
    
    /**
     * LOG ENTITY CHANGE
     * -----------------
     * Records when an entity is created, updated, or deleted.
     */
    public AuditLog logEntityChange(EventType eventType, String entityType, Long entityId, 
                                     String description, Long userId, String username,
                                     String oldValue, String newValue) {
        AuditLog log = AuditLog.createEntityEvent(eventType, entityType, entityId, 
                                                   description, userId, username);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        return auditLogRepository.save(log);
    }
    
    /**
     * LOG COMPLIANCE VIOLATION
     * ------------------------
     */
    public AuditLog logComplianceViolation(String description, Long userId, String username,
                                           String entityType, Long entityId) {
        AuditLog log = new AuditLog(EventType.COMPLIANCE_CHECK, description, userId, username);
        log.setComplianceStatus(ComplianceStatus.FAIL);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        return auditLogRepository.save(log);
    }
    
    // ========================================
    // READ OPERATIONS
    // ========================================
    
    /**
     * GET AUDIT LOG BY ID
     * -------------------
     */
    @Transactional(readOnly = true)
    public Optional<AuditLog> getAuditLogById(Long logId) {
        return auditLogRepository.findById(Objects.requireNonNull(logId));
    }
    
    /**
     * GET ALL AUDIT LOGS (PAGINATED)
     * ------------------------------
     * Pagination is important for performance with large audit logs.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAllAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.findAllByOrderByEventTimestampDesc(pageable);
    }
    
    /**
     * GET AUDIT LOGS BY USER
     * ----------------------
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }
    
    /**
     * GET AUDIT LOGS BY EVENT TYPE
     * ----------------------------
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByEventType(EventType eventType) {
        return auditLogRepository.findByEventType(eventType);
    }
    
    /**
     * GET COMPLIANCE FAILURES
     * -----------------------
     * Critical for compliance reporting!
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getComplianceFailures() {
        return auditLogRepository.findComplianceFailures();
    }
    
    /**
     * GET AUDIT LOGS BY DATE RANGE
     * ----------------------------
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByEventTimestampBetween(start, end);
    }
    
    /**
     * GET AUDIT LOGS FOR ENTITY
     * -------------------------
     * Get all logs related to a specific entity.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsForEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
    
    /**
     * GET SUSPICIOUS ACTIVITY
     * -----------------------
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getSuspiciousActivity() {
        return auditLogRepository.findSuspiciousActivity();
    }
    
    // ========================================
    // COMPLIANCE VERIFICATION
    // ========================================
    
    /**
     * VERIFY COMPLIANCE
     * -----------------
     * Checks if there are any compliance violations in a date range.
     */
    @Transactional(readOnly = true)
    public boolean verifyCompliance(LocalDateTime start, LocalDateTime end) {
        List<AuditLog> logs = auditLogRepository.findByEventTimestampBetween(start, end);
        return logs.stream()
                .noneMatch(log -> log.getComplianceStatus() == ComplianceStatus.FAIL);
    }
    
    /**
     * GET COMPLIANCE SUMMARY
     * ----------------------
     * Returns a summary of compliance status.
     */
    @Transactional(readOnly = true)
    public String getComplianceSummary(LocalDateTime start, LocalDateTime end) {
        List<AuditLog> logs = auditLogRepository.findByEventTimestampBetween(start, end);
        
        long totalEvents = logs.size();
        long passCount = logs.stream()
                .filter(log -> log.getComplianceStatus() == ComplianceStatus.PASS).count();
        long failCount = logs.stream()
                .filter(log -> log.getComplianceStatus() == ComplianceStatus.FAIL).count();
        long warningCount = logs.stream()
                .filter(log -> log.getComplianceStatus() == ComplianceStatus.WARNING).count();
        
        return String.format(
            "{\"totalEvents\": %d, \"passed\": %d, \"failed\": %d, \"warnings\": %d, " +
            "\"complianceRate\": %.2f}",
            totalEvents, passCount, failCount, warningCount,
            totalEvents > 0 ? (passCount * 100.0 / totalEvents) : 100.0
        );
    }
    
    // ========================================
    // STATISTICS
    // ========================================
    
    /**
     * COUNT EVENTS BY TYPE
     * --------------------
     */
    @Transactional(readOnly = true)
    public long countEventsByType(EventType eventType) {
        return auditLogRepository.countByEventType(eventType);
    }
    
    /**
     * COUNT COMPLIANCE FAILURES
     * -------------------------
     */
    @Transactional(readOnly = true)
    public long countComplianceFailures() {
        return auditLogRepository.countByComplianceStatus(ComplianceStatus.FAIL);
    }
    
    /**
     * COUNT TOTAL LOGS
     * ----------------
     */
    @Transactional(readOnly = true)
    public long countTotalLogs() {
        return auditLogRepository.count();
    }
    
    // ========================================
    // DATA RETENTION
    // ========================================
    
    /**
     * DELETE OLD LOGS
     * ---------------
     * For data retention policies.
     */
    public void deleteOldLogs(int yearsToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusYears(yearsToKeep);
        auditLogRepository.deleteByEventTimestampBefore(cutoffDate);
    }
}
