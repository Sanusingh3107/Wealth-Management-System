package com.wms.entity;

import com.wms.entity.AuditLog.ComplianceStatus;
import com.wms.entity.AuditLog.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditLog entity
 * Tests constructors, getters, setters, factory methods, and enums
 */
class AuditLogTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
        auditLog.setLogId(1L);
        auditLog.setEventType(EventType.LOGIN);
        auditLog.setEventDescription("User logged in successfully");
        auditLog.setUserId(1L);
        auditLog.setUsername("johndoe");
        auditLog.setEventTimestamp(LocalDateTime.now());
        auditLog.setComplianceStatus(ComplianceStatus.PASS);
        auditLog.setEntityType("User");
        auditLog.setEntityId(1L);
        auditLog.setIpAddress("192.168.1.1");
        auditLog.setOldValue("{\"status\": \"inactive\"}");
        auditLog.setNewValue("{\"status\": \"active\"}");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create audit log with default constructor")
        void defaultConstructor() {
            AuditLog log = new AuditLog();
            assertNotNull(log);
            assertNotNull(log.getEventTimestamp());
            assertEquals(ComplianceStatus.PASS, log.getComplianceStatus());
        }

        @Test
        @DisplayName("Should create audit log with parameterized constructor")
        void parameterizedConstructor() {
            AuditLog log = new AuditLog(EventType.CREATE, "Created new client", 1L, "johndoe");
            
            assertEquals(EventType.CREATE, log.getEventType());
            assertEquals("Created new client", log.getEventDescription());
            assertEquals(1L, log.getUserId());
            assertEquals("johndoe", log.getUsername());
            assertNotNull(log.getEventTimestamp());
            assertEquals(ComplianceStatus.PASS, log.getComplianceStatus());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set logId")
        void logId() {
            auditLog.setLogId(100L);
            assertEquals(100L, auditLog.getLogId());
        }

        @Test
        @DisplayName("Should get and set eventTimestamp")
        void eventTimestamp() {
            LocalDateTime timestamp = LocalDateTime.of(2025, 1, 15, 10, 30);
            auditLog.setEventTimestamp(timestamp);
            assertEquals(timestamp, auditLog.getEventTimestamp());
        }

        @Test
        @DisplayName("Should get and set eventType")
        void eventType() {
            auditLog.setEventType(EventType.DELETE);
            assertEquals(EventType.DELETE, auditLog.getEventType());
        }

        @Test
        @DisplayName("Should get and set eventDescription")
        void eventDescription() {
            auditLog.setEventDescription("New description");
            assertEquals("New description", auditLog.getEventDescription());
        }

        @Test
        @DisplayName("Should get and set userId")
        void userId() {
            auditLog.setUserId(100L);
            assertEquals(100L, auditLog.getUserId());
        }

        @Test
        @DisplayName("Should get and set username")
        void username() {
            auditLog.setUsername("newuser");
            assertEquals("newuser", auditLog.getUsername());
        }

        @Test
        @DisplayName("Should get and set complianceStatus")
        void complianceStatus() {
            auditLog.setComplianceStatus(ComplianceStatus.FAIL);
            assertEquals(ComplianceStatus.FAIL, auditLog.getComplianceStatus());
        }

        @Test
        @DisplayName("Should get and set entityType")
        void entityType() {
            auditLog.setEntityType("Portfolio");
            assertEquals("Portfolio", auditLog.getEntityType());
        }

        @Test
        @DisplayName("Should get and set entityId")
        void entityId() {
            auditLog.setEntityId(50L);
            assertEquals(50L, auditLog.getEntityId());
        }

        @Test
        @DisplayName("Should get and set ipAddress")
        void ipAddress() {
            auditLog.setIpAddress("10.0.0.1");
            assertEquals("10.0.0.1", auditLog.getIpAddress());
        }

        @Test
        @DisplayName("Should get and set oldValue")
        void oldValue() {
            auditLog.setOldValue("{\"old\": true}");
            assertEquals("{\"old\": true}", auditLog.getOldValue());
        }

        @Test
        @DisplayName("Should get and set newValue")
        void newValue() {
            auditLog.setNewValue("{\"new\": true}");
            assertEquals("{\"new\": true}", auditLog.getNewValue());
        }
    }

    @Nested
    @DisplayName("Static Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create login event")
        void createLoginEvent() {
            AuditLog log = AuditLog.createLoginEvent(1L, "johndoe", "192.168.1.1");
            
            assertEquals(EventType.LOGIN, log.getEventType());
            assertEquals("User logged in", log.getEventDescription());
            assertEquals(1L, log.getUserId());
            assertEquals("johndoe", log.getUsername());
            assertEquals("192.168.1.1", log.getIpAddress());
        }

        @Test
        @DisplayName("Should create logout event")
        void createLogoutEvent() {
            AuditLog log = AuditLog.createLogoutEvent(1L, "johndoe");
            
            assertEquals(EventType.LOGOUT, log.getEventType());
            assertEquals("User logged out", log.getEventDescription());
            assertEquals(1L, log.getUserId());
            assertEquals("johndoe", log.getUsername());
        }

        @Test
        @DisplayName("Should create entity event")
        void createEntityEvent() {
            AuditLog log = AuditLog.createEntityEvent(
                EventType.UPDATE, "Client", 1L, "Updated client profile", 1L, "johndoe"
            );
            
            assertEquals(EventType.UPDATE, log.getEventType());
            assertEquals("Client", log.getEntityType());
            assertEquals(1L, log.getEntityId());
            assertEquals("Updated client profile", log.getEventDescription());
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {

        @Test
        @DisplayName("Should have all expected EventType values")
        void eventTypeValues() {
            assertEquals(10, EventType.values().length);
            assertNotNull(EventType.LOGIN);
            assertNotNull(EventType.LOGOUT);
            assertNotNull(EventType.CREATE);
            assertNotNull(EventType.UPDATE);
            assertNotNull(EventType.DELETE);
            assertNotNull(EventType.VIEW);
            assertNotNull(EventType.EXPORT);
            assertNotNull(EventType.TRANSACTION);
            assertNotNull(EventType.COMPLIANCE_CHECK);
            assertNotNull(EventType.SYSTEM);
        }

        @Test
        @DisplayName("Should have all expected ComplianceStatus values")
        void complianceStatusValues() {
            assertEquals(4, ComplianceStatus.values().length);
            assertNotNull(ComplianceStatus.PASS);
            assertNotNull(ComplianceStatus.FAIL);
            assertNotNull(ComplianceStatus.WARNING);
            assertNotNull(ComplianceStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return meaningful string representation")
        void toStringTest() {
            String result = auditLog.toString();
            assertNotNull(result);
            assertTrue(result.contains("LOGIN"));
            assertTrue(result.contains("johndoe"));
            assertTrue(result.contains("PASS"));
        }
    }
}
