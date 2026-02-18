package com.wms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * ========================================
 * AUDIT LOG ENTITY
 * ========================================
 * If auditors come to inspect, we can show them exactly
 * what happened and when."
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;
    
    /**
     * EVENT TIMESTAMP
     * ---------------
     * Exactly when the event occurred.
     * LocalDateTime includes both date and time.
     */
    @NotNull(message = "Event timestamp is required")
    @Column(nullable = false)
    private LocalDateTime eventTimestamp;
    
    /**
     * EVENT TYPE
     * ----------
     * Category of the event for easier filtering and reporting.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;
    
    /**
     * EVENT DESCRIPTION
     * -----------------
     * Detailed description of what happened.
     * Example: "User john.doe updated client profile for Client ID: 123"
     */
    @NotBlank(message = "Event description is required")
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String eventDescription;
    
    /**
     * USER ID
     * -------
     * The user who performed the action.
     * Can be null for system-generated events.
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * USERNAME
     * --------
     * Stored separately for easier reporting without joins.
     */
    @Column(length = 100)
    private String username;
    
    /**
     * COMPLIANCE STATUS
     * -----------------
     * Whether the action passed or failed compliance checks.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceStatus complianceStatus;
    
    /**
     * ENTITY TYPE
     * -----------
     * What type of entity was affected (Client, Portfolio, etc.)
     */
    @Column(length = 50)
    private String entityType;
    
    /**
     * ENTITY ID
     * ---------
     * The ID of the entity that was affected.
     */
    @Column(name = "entity_id")
    private Long entityId;
    
    /**
     * IP ADDRESS
     * ----------
     * Where the action originated from (for security tracking).
     */
    @Column(length = 50)
    private String ipAddress;
    
    /**
     * OLD VALUE / NEW VALUE
     * ---------------------
     * For tracking changes (what it was vs what it became)
     */
    @Column(columnDefinition = "TEXT")
    private String oldValue;
    
    @Column(columnDefinition = "TEXT")
    private String newValue;
    
    // ========================================
    // ENUMS
    // ========================================
    
    public enum EventType {
        LOGIN,              // User logged in
        LOGOUT,             // User logged out
        CREATE,             // Something was created
        UPDATE,             // Something was updated
        DELETE,             // Something was deleted
        VIEW,               // Something was viewed
        EXPORT,             // Data was exported
        TRANSACTION,        // Financial transaction
        COMPLIANCE_CHECK,   // Compliance verification
        SYSTEM              // System-generated event
    }
    
    public enum ComplianceStatus {
        PASS,       // Action complied with regulations
        FAIL,       // Action violated regulations
        WARNING,    // Action raised concerns but allowed
        PENDING     // Waiting for review
    }
    
    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    public AuditLog() {
        this.eventTimestamp = LocalDateTime.now();
        this.complianceStatus = ComplianceStatus.PASS;
    }
    
    public AuditLog(EventType eventType, String eventDescription, Long userId, String username) {
        this.eventTimestamp = LocalDateTime.now();
        this.eventType = eventType;
        this.eventDescription = eventDescription;
        this.userId = userId;
        this.username = username;
        this.complianceStatus = ComplianceStatus.PASS;
    }
    
    // ========================================
    // STATIC FACTORY METHODS
    // ========================================
    // These make it easier to create common audit log entries
    
    public static AuditLog createLoginEvent(Long userId, String username, String ipAddress) {
        AuditLog log = new AuditLog(EventType.LOGIN, "User logged in", userId, username);
        log.setIpAddress(ipAddress);
        return log;
    }
    
    public static AuditLog createLogoutEvent(Long userId, String username) {
        return new AuditLog(EventType.LOGOUT, "User logged out", userId, username);
    }
    
    public static AuditLog createEntityEvent(EventType type, String entityType, Long entityId, 
                                             String description, Long userId, String username) {
        AuditLog log = new AuditLog(type, description, userId, username);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        return log;
    }
    
    // ========================================
    // GETTERS AND SETTERS
    // ========================================
    
    public Long getLogId() {
        return logId;
    }
    
    public void setLogId(Long logId) {
        this.logId = logId;
    }
    
    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }
    
    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
    
    public String getEventDescription() {
        return eventDescription;
    }
    
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }
    
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
    
    public ComplianceStatus getComplianceStatus() {
        return complianceStatus;
    }
    
    public void setComplianceStatus(ComplianceStatus complianceStatus) {
        this.complianceStatus = complianceStatus;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getOldValue() {
        return oldValue;
    }
    
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
    
    public String getNewValue() {
        return newValue;
    }
    
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
    
    @Override
    public String toString() {
        return "AuditLog{" +
                "logId=" + logId +
                ", eventTimestamp=" + eventTimestamp +
                ", eventType=" + eventType +
                ", eventDescription='" + eventDescription + '\'' +
                ", username='" + username + '\'' +
                ", complianceStatus=" + complianceStatus +
                '}';
    }
}
