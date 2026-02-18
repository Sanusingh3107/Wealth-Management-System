package com.wms.controller;

import com.wms.entity.AuditLog;
import com.wms.entity.AuditLog.EventType;
import com.wms.service.ComplianceService;
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
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComplianceController Tests")
class ComplianceControllerTest {

    @Mock
    private ComplianceService complianceService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ComplianceController complianceController;

    private AuditLog testAuditLog;

    @BeforeEach
    void setUp() {
        testAuditLog = new AuditLog();
        testAuditLog.setLogId(1L);
        testAuditLog.setEventType(EventType.LOGIN);
        testAuditLog.setEventDescription("Test login event");
        testAuditLog.setEventTimestamp(LocalDateTime.now());
        testAuditLog.setUsername("testuser");
    }

    @Nested
    @DisplayName("Dashboard Tests")
    class DashboardTests {

        @Test
        @DisplayName("Should show compliance dashboard with statistics")
        void complianceDashboard_ReturnsViewWithStats() {
            when(complianceService.countTotalLogs()).thenReturn(100L);
            when(complianceService.countComplianceFailures()).thenReturn(5L);
            when(complianceService.countEventsByType(EventType.LOGIN)).thenReturn(50L);
            when(complianceService.getComplianceFailures()).thenReturn(Collections.emptyList());
            when(complianceService.getSuspiciousActivity()).thenReturn(Collections.emptyList());
            
            String result = complianceController.complianceDashboard(model);
            
            assertEquals("compliance/dashboard", result);
            verify(model).addAttribute("totalLogs", 100L);
            verify(model).addAttribute("failureCount", 5L);
            verify(model).addAttribute("loginCount", 50L);
        }
    }

    @Nested
    @DisplayName("Audit Log Operations Tests")
    class AuditLogOperationsTests {

        @Test
        @DisplayName("Should view paginated audit logs")
        void viewAuditLogs_ReturnsPaginatedView() {
            List<AuditLog> logs = Arrays.asList(testAuditLog);
            Page<AuditLog> page = new PageImpl<>(logs);
            when(complianceService.getAllAuditLogs(0, 20)).thenReturn(page);
            
            String result = complianceController.viewAuditLogs(0, 20, model);
            
            assertEquals("compliance/audit-logs", result);
            verify(model).addAttribute("logs", logs);
            verify(model).addAttribute("currentPage", 0);
            verify(model).addAttribute("totalPages", 1);
        }

        @Test
        @DisplayName("Should view audit log detail")
        void viewAuditLogDetail_ReturnsDetailView() {
            when(complianceService.getAuditLogById(1L)).thenReturn(Optional.of(testAuditLog));
            
            String result = complianceController.viewAuditLogDetail(1L, model);
            
            assertEquals("compliance/audit-log-detail", result);
            verify(model).addAttribute("log", testAuditLog);
        }

        @Test
        @DisplayName("Should throw exception when audit log not found")
        void viewAuditLogDetail_NotFound_ThrowsException() {
            when(complianceService.getAuditLogById(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> complianceController.viewAuditLogDetail(99L, model));
        }

        @Test
        @DisplayName("Should filter logs by event type")
        void filterByEventType_ReturnsFilteredLogs() {
            List<AuditLog> logs = Arrays.asList(testAuditLog);
            when(complianceService.getAuditLogsByEventType(EventType.LOGIN)).thenReturn(logs);
            
            String result = complianceController.filterByEventType(EventType.LOGIN, model);
            
            assertEquals("compliance/audit-logs", result);
            verify(model).addAttribute("logs", logs);
            verify(model).addAttribute("selectedType", EventType.LOGIN);
        }

        @Test
        @DisplayName("Should filter logs by date range")
        void filterByDateRange_ReturnsFilteredLogs() {
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            List<AuditLog> logs = Arrays.asList(testAuditLog);
            when(complianceService.getAuditLogsByDateRange(any(), any())).thenReturn(logs);
            
            String result = complianceController.filterByDateRange(startDate, endDate, model);
            
            assertEquals("compliance/audit-logs", result);
            verify(model).addAttribute("logs", logs);
            verify(model).addAttribute("startDate", startDate);
            verify(model).addAttribute("endDate", endDate);
        }

        @Test
        @DisplayName("Should view user activity")
        void viewUserActivity_ReturnsUserActivityView() {
            List<AuditLog> logs = Arrays.asList(testAuditLog);
            when(complianceService.getAuditLogsByUser(1L)).thenReturn(logs);
            
            String result = complianceController.viewUserActivity(1L, model);
            
            assertEquals("compliance/user-activity", result);
            verify(model).addAttribute("logs", logs);
            verify(model).addAttribute("userId", 1L);
        }
    }

    @Nested
    @DisplayName("Compliance Reports Tests")
    class ComplianceReportsTests {

        @Test
        @DisplayName("Should view compliance failures")
        void viewComplianceFailures_ReturnsFailuresView() {
            List<AuditLog> failures = Arrays.asList(testAuditLog);
            when(complianceService.getComplianceFailures()).thenReturn(failures);
            
            String result = complianceController.viewComplianceFailures(model);
            
            assertEquals("compliance/failures", result);
            verify(model).addAttribute("failures", failures);
        }

        @Test
        @DisplayName("Should view suspicious activity")
        void viewSuspiciousActivity_ReturnsSuspiciousView() {
            List<AuditLog> suspicious = Arrays.asList(testAuditLog);
            when(complianceService.getSuspiciousActivity()).thenReturn(suspicious);
            
            String result = complianceController.viewSuspiciousActivity(model);
            
            assertEquals("compliance/suspicious", result);
            verify(model).addAttribute("activities", suspicious);
        }

        @Test
        @DisplayName("Should view compliance summary with dates provided")
        void viewComplianceSummary_WithDates_ReturnsSummaryView() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();
            when(complianceService.getComplianceSummary(any(), any())).thenReturn("Summary");
            when(complianceService.verifyCompliance(any(), any())).thenReturn(true);
            
            String result = complianceController.viewComplianceSummary(startDate, endDate, model);
            
            assertEquals("compliance/summary", result);
            verify(model).addAttribute("summary", "Summary");
            verify(model).addAttribute("isCompliant", true);
        }

        @Test
        @DisplayName("Should view compliance summary with default dates")
        void viewComplianceSummary_WithoutDates_UsesDefaults() {
            when(complianceService.getComplianceSummary(any(), any())).thenReturn("Summary");
            when(complianceService.verifyCompliance(any(), any())).thenReturn(true);
            
            String result = complianceController.viewComplianceSummary(null, null, model);
            
            assertEquals("compliance/summary", result);
            verify(model).addAttribute(eq("startDate"), any(LocalDate.class));
            verify(model).addAttribute(eq("endDate"), any(LocalDate.class));
        }
    }

    @Nested
    @DisplayName("Verify Compliance Tests")
    class VerifyComplianceTests {

        @Test
        @DisplayName("Should show verify form")
        void showVerifyForm_ReturnsVerifyView() {
            String result = complianceController.showVerifyForm(model);
            
            assertEquals("compliance/verify", result);
            verify(model).addAttribute(eq("startDate"), any(LocalDate.class));
            verify(model).addAttribute(eq("endDate"), any(LocalDate.class));
        }

        @Test
        @DisplayName("Should verify compliance and show success for compliant")
        void verifyCompliance_Compliant_RedirectsWithSuccess() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();
            when(complianceService.verifyCompliance(any(), any())).thenReturn(true);
            
            String result = complianceController.verifyCompliance(startDate, endDate, redirectAttributes);
            
            assertTrue(result.contains("redirect:/compliance/summary"));
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should verify compliance and show warning for non-compliant")
        void verifyCompliance_NonCompliant_RedirectsWithWarning() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();
            when(complianceService.verifyCompliance(any(), any())).thenReturn(false);
            
            String result = complianceController.verifyCompliance(startDate, endDate, redirectAttributes);
            
            assertTrue(result.contains("redirect:/compliance/summary"));
            verify(redirectAttributes).addFlashAttribute(eq("warning"), anyString());
        }
    }
}
