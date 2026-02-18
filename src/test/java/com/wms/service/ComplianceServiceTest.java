package com.wms.service;

import com.wms.entity.AuditLog;
import com.wms.entity.AuditLog.ComplianceStatus;
import com.wms.entity.AuditLog.EventType;
import com.wms.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ComplianceService
 * Achieves 100% code coverage for all ComplianceService methods
 */
@ExtendWith(MockitoExtension.class)
class ComplianceServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private ComplianceService complianceService;

    private AuditLog testAuditLog;

    @BeforeEach
    void setUp() {
        testAuditLog = new AuditLog();
        testAuditLog.setLogId(1L);
        testAuditLog.setEventType(EventType.LOGIN);
        testAuditLog.setEventDescription("User logged in");
        testAuditLog.setUserId(1L);
        testAuditLog.setUsername("johndoe");
        testAuditLog.setEventTimestamp(LocalDateTime.now());
        testAuditLog.setComplianceStatus(ComplianceStatus.PASS);
        testAuditLog.setIpAddress("192.168.1.1");
    }

    // ========================================
    // LOGGING OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Log Audit Event Tests")
    class LogAuditEventTests {

        @Test
        @DisplayName("Should log audit event successfully")
        void logAuditEvent_Success() {
            when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

            AuditLog result = complianceService.logAuditEvent(testAuditLog);

            assertNotNull(result);
            verify(auditLogRepository).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("Should set default timestamp when not provided")
        void logAuditEvent_SetsDefaultTimestamp() {
            AuditLog newLog = new AuditLog();
            newLog.setEventType(EventType.CREATE);
            newLog.setEventDescription("Created something");
            newLog.setEventTimestamp(null);

            when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AuditLog result = complianceService.logAuditEvent(newLog);

            assertNotNull(result.getEventTimestamp());
        }

        @Test
        @DisplayName("Should set default compliance status when not provided")
        void logAuditEvent_SetsDefaultComplianceStatus() {
            AuditLog newLog = new AuditLog();
            newLog.setEventType(EventType.CREATE);
            newLog.setEventDescription("Created something");
            newLog.setEventTimestamp(LocalDateTime.now());
            newLog.setComplianceStatus(null);

            when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AuditLog result = complianceService.logAuditEvent(newLog);

            assertEquals(ComplianceStatus.PASS, result.getComplianceStatus());
        }

        @Test
        @DisplayName("Should log successful login event")
        void logLogin_Success() {
            when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AuditLog result = complianceService.logLogin(1L, "johndoe", "192.168.1.1", true);

            assertNotNull(result);
            assertEquals(EventType.LOGIN, result.getEventType());
            assertEquals(ComplianceStatus.PASS, result.getComplianceStatus());
            assertEquals("User logged in successfully", result.getEventDescription());
        }

        @Test
        @DisplayName("Should log failed login event")
        void logLogin_Failed() {
            when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AuditLog result = complianceService.logLogin(1L, "johndoe", "192.168.1.1", false);

            assertEquals(ComplianceStatus.WARNING, result.getComplianceStatus());
            assertEquals("Failed login attempt", result.getEventDescription());
        }

        @Test
        @DisplayName("Should log logout event")
        void logLogout_Success() {
            when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AuditLog result = complianceService.logLogout(1L, "johndoe");

            assertNotNull(result);
            assertEquals(EventType.LOGOUT, result.getEventType());
        }

        @Test
        @DisplayName("Should log entity change event")
        void logEntityChange_Success() {
            when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AuditLog result = complianceService.logEntityChange(
                EventType.UPDATE, "Client", 1L, "Updated client",
                1L, "johndoe", "{\"name\": \"Old\"}", "{\"name\": \"New\"}"
            );

            assertNotNull(result);
            assertEquals(EventType.UPDATE, result.getEventType());
            assertEquals("Client", result.getEntityType());
        }

        @Test
        @DisplayName("Should log compliance violation")
        void logComplianceViolation_Success() {
            when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AuditLog result = complianceService.logComplianceViolation(
                "Unauthorized access attempt", 1L, "johndoe", "Portfolio", 1L
            );

            assertNotNull(result);
            assertEquals(ComplianceStatus.FAIL, result.getComplianceStatus());
            assertEquals(EventType.COMPLIANCE_CHECK, result.getEventType());
        }
    }

    // ========================================
    // READ OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Read Audit Log Tests")
    class ReadAuditLogTests {

        @Test
        @DisplayName("Should return audit log by ID when exists")
        void getAuditLogById_Found() {
            when(auditLogRepository.findById(1L)).thenReturn(Optional.of(testAuditLog));

            Optional<AuditLog> result = complianceService.getAuditLogById(1L);

            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("Should return empty when audit log not found")
        void getAuditLogById_NotFound() {
            when(auditLogRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<AuditLog> result = complianceService.getAuditLogById(999L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return paginated audit logs")
        void getAllAuditLogs_ReturnsPaginatedList() {
            Page<AuditLog> page = new PageImpl<>(Arrays.asList(testAuditLog));
            when(auditLogRepository.findAllByOrderByEventTimestampDesc(any(PageRequest.class))).thenReturn(page);

            Page<AuditLog> result = complianceService.getAllAuditLogs(0, 10);

            assertEquals(1, result.getContent().size());
        }

        @Test
        @DisplayName("Should return audit logs by user")
        void getAuditLogsByUser_ReturnsList() {
            when(auditLogRepository.findByUserId(1L)).thenReturn(Arrays.asList(testAuditLog));

            List<AuditLog> result = complianceService.getAuditLogsByUser(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return audit logs by event type")
        void getAuditLogsByEventType_ReturnsList() {
            when(auditLogRepository.findByEventType(EventType.LOGIN)).thenReturn(Arrays.asList(testAuditLog));

            List<AuditLog> result = complianceService.getAuditLogsByEventType(EventType.LOGIN);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return compliance failures")
        void getComplianceFailures_ReturnsList() {
            AuditLog failLog = new AuditLog();
            failLog.setComplianceStatus(ComplianceStatus.FAIL);
            
            when(auditLogRepository.findComplianceFailures()).thenReturn(Arrays.asList(failLog));

            List<AuditLog> result = complianceService.getComplianceFailures();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return audit logs by date range")
        void getAuditLogsByDateRange_ReturnsList() {
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();

            when(auditLogRepository.findByEventTimestampBetween(start, end))
                .thenReturn(Arrays.asList(testAuditLog));

            List<AuditLog> result = complianceService.getAuditLogsByDateRange(start, end);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return audit logs for entity")
        void getAuditLogsForEntity_ReturnsList() {
            when(auditLogRepository.findByEntityTypeAndEntityId("Client", 1L))
                .thenReturn(Arrays.asList(testAuditLog));

            List<AuditLog> result = complianceService.getAuditLogsForEntity("Client", 1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return suspicious activity")
        void getSuspiciousActivity_ReturnsList() {
            when(auditLogRepository.findSuspiciousActivity()).thenReturn(Arrays.asList(testAuditLog));

            List<AuditLog> result = complianceService.getSuspiciousActivity();

            assertEquals(1, result.size());
        }
    }

    // ========================================
    // COMPLIANCE VERIFICATION TESTS
    // ========================================

    @Nested
    @DisplayName("Compliance Verification Tests")
    class ComplianceVerificationTests {

        @Test
        @DisplayName("Should return true when no compliance failures")
        void verifyCompliance_NoFailures_ReturnsTrue() {
            AuditLog passLog = new AuditLog();
            passLog.setComplianceStatus(ComplianceStatus.PASS);

            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();

            when(auditLogRepository.findByEventTimestampBetween(start, end))
                .thenReturn(Arrays.asList(passLog));

            boolean result = complianceService.verifyCompliance(start, end);

            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when compliance failures exist")
        void verifyCompliance_HasFailures_ReturnsFalse() {
            AuditLog failLog = new AuditLog();
            failLog.setComplianceStatus(ComplianceStatus.FAIL);

            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();

            when(auditLogRepository.findByEventTimestampBetween(start, end))
                .thenReturn(Arrays.asList(failLog));

            boolean result = complianceService.verifyCompliance(start, end);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should get compliance summary with events")
        void getComplianceSummary_WithEvents() {
            AuditLog passLog = new AuditLog();
            passLog.setComplianceStatus(ComplianceStatus.PASS);
            
            AuditLog failLog = new AuditLog();
            failLog.setComplianceStatus(ComplianceStatus.FAIL);
            
            AuditLog warnLog = new AuditLog();
            warnLog.setComplianceStatus(ComplianceStatus.WARNING);

            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();

            when(auditLogRepository.findByEventTimestampBetween(start, end))
                .thenReturn(Arrays.asList(passLog, failLog, warnLog));

            String result = complianceService.getComplianceSummary(start, end);

            assertNotNull(result);
            assertTrue(result.contains("\"totalEvents\": 3"));
            assertTrue(result.contains("\"passed\": 1"));
            assertTrue(result.contains("\"failed\": 1"));
            assertTrue(result.contains("\"warnings\": 1"));
        }

        @Test
        @DisplayName("Should get compliance summary with no events (100% compliance)")
        void getComplianceSummary_NoEvents() {
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();

            when(auditLogRepository.findByEventTimestampBetween(start, end))
                .thenReturn(Collections.emptyList());

            String result = complianceService.getComplianceSummary(start, end);

            assertTrue(result.contains("\"totalEvents\": 0"));
            assertTrue(result.contains("\"complianceRate\": 100.00"));
        }
    }

    // ========================================
    // STATISTICS TESTS
    // ========================================

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should count events by type")
        void countEventsByType_ReturnsCount() {
            when(auditLogRepository.countByEventType(EventType.LOGIN)).thenReturn(50L);

            assertEquals(50L, complianceService.countEventsByType(EventType.LOGIN));
        }

        @Test
        @DisplayName("Should count compliance failures")
        void countComplianceFailures_ReturnsCount() {
            when(auditLogRepository.countByComplianceStatus(ComplianceStatus.FAIL)).thenReturn(5L);

            assertEquals(5L, complianceService.countComplianceFailures());
        }

        @Test
        @DisplayName("Should count total logs")
        void countTotalLogs_ReturnsCount() {
            when(auditLogRepository.count()).thenReturn(1000L);

            assertEquals(1000L, complianceService.countTotalLogs());
        }
    }

    // ========================================
    // DATA RETENTION TESTS
    // ========================================

    @Nested
    @DisplayName("Data Retention Tests")
    class DataRetentionTests {

        @Test
        @DisplayName("Should delete old logs based on years to keep")
        void deleteOldLogs_Success() {
            doNothing().when(auditLogRepository).deleteByEventTimestampBefore(any(LocalDateTime.class));

            assertDoesNotThrow(() -> complianceService.deleteOldLogs(7));

            verify(auditLogRepository).deleteByEventTimestampBefore(any(LocalDateTime.class));
        }
    }
}
