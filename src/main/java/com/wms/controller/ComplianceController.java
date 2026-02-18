package com.wms.controller;

import com.wms.entity.AuditLog;
import com.wms.entity.AuditLog.EventType;
import com.wms.service.ComplianceService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * ========================================
 * COMPLIANCE CONTROLLER
 * ========================================
 * 
 */
@Controller
@RequestMapping("/compliance")
public class ComplianceController {
    
    private final ComplianceService complianceService;
    
    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }
    
    // ========================================
    // DASHBOARD
    // ========================================
    
    /**
     * COMPLIANCE DASHBOARD
     * --------------------
     * Main page showing compliance overview.
     */
    @GetMapping
    public String complianceDashboard(Model model) {
        // Get statistics
        long totalLogs = complianceService.countTotalLogs();
        long failureCount = complianceService.countComplianceFailures();
        long loginCount = complianceService.countEventsByType(EventType.LOGIN);
        
        // Get recent compliance failures
        List<AuditLog> failures = complianceService.getComplianceFailures();
        
        // Get suspicious activity
        List<AuditLog> suspicious = complianceService.getSuspiciousActivity();
        
        model.addAttribute("totalLogs", totalLogs);
        model.addAttribute("failureCount", failureCount);
        model.addAttribute("loginCount", loginCount);
        model.addAttribute("failures", failures);
        model.addAttribute("suspicious", suspicious);
        
        return "compliance/dashboard";
    }
    
    // ========================================
    // AUDIT LOG OPERATIONS
    // ========================================
    
    /**
     * VIEW AUDIT LOGS
     * ---------------
     * Paginated list of all audit logs.
     */
    @GetMapping("/audit-logs")
    public String viewAuditLogs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Model model) {
        
        Page<AuditLog> logsPage = complianceService.getAllAuditLogs(page, size);
        
        model.addAttribute("logs", logsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logsPage.getTotalPages());
        model.addAttribute("totalItems", logsPage.getTotalElements());
        model.addAttribute("eventTypes", EventType.values());
        
        return "compliance/audit-logs";
    }
    
    /**
     * VIEW AUDIT LOG DETAIL
     * ---------------------
     */
    @GetMapping("/audit-logs/{id}")
    public String viewAuditLogDetail(@PathVariable("id") Long id, Model model) {
        AuditLog log = complianceService.getAuditLogById(id)
                .orElseThrow(() -> new RuntimeException("Audit log not found"));
        
        model.addAttribute("log", log);
        return "compliance/audit-log-detail";
    }
    
    /**
     * FILTER LOGS BY EVENT TYPE
     * -------------------------
     */
    @GetMapping("/audit-logs/type/{type}")
    public String filterByEventType(@PathVariable("type") EventType type, Model model) {
        List<AuditLog> logs = complianceService.getAuditLogsByEventType(type);
        
        model.addAttribute("logs", logs);
        model.addAttribute("selectedType", type);
        model.addAttribute("eventTypes", EventType.values());
        
        return "compliance/audit-logs";
    }
    
    /**
     * FILTER LOGS BY DATE RANGE
     * -------------------------
     */
    @GetMapping("/audit-logs/date-range")
    public String filterByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        List<AuditLog> logs = complianceService.getAuditLogsByDateRange(start, end);
        
        model.addAttribute("logs", logs);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("eventTypes", EventType.values());
        
        return "compliance/audit-logs";
    }
    
    /**
     * VIEW USER ACTIVITY
     * ------------------
     */
    @GetMapping("/user-activity/{userId}")
    public String viewUserActivity(@PathVariable("userId") Long userId, Model model) {
        List<AuditLog> logs = complianceService.getAuditLogsByUser(userId);
        
        model.addAttribute("logs", logs);
        model.addAttribute("userId", userId);
        
        return "compliance/user-activity";
    }
    
    // ========================================
    // COMPLIANCE REPORTS
    // ========================================
    
    /**
     * COMPLIANCE FAILURES
     * -------------------
     */
    @GetMapping("/failures")
    public String viewComplianceFailures(Model model) {
        List<AuditLog> failures = complianceService.getComplianceFailures();
        model.addAttribute("failures", failures);
        return "compliance/failures";
    }
    
    /**
     * SUSPICIOUS ACTIVITY
     * -------------------
     */
    @GetMapping("/suspicious")
    public String viewSuspiciousActivity(Model model) {
        List<AuditLog> suspicious = complianceService.getSuspiciousActivity();
        model.addAttribute("activities", suspicious);
        return "compliance/suspicious";
    }
    
    /**
     * COMPLIANCE SUMMARY
     * ------------------
     */
    @GetMapping("/summary")
    public String viewComplianceSummary(
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        
        // Default to last 30 days
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        String summary = complianceService.getComplianceSummary(start, end);
        boolean isCompliant = complianceService.verifyCompliance(start, end);
        
        model.addAttribute("summary", summary);
        model.addAttribute("isCompliant", isCompliant);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        return "compliance/summary";
    }
    
    // ========================================
    // VERIFY COMPLIANCE
    // ========================================
    
    /**
     * VERIFY COMPLIANCE
     * -----------------
     */
    @GetMapping("/verify")
    public String showVerifyForm(Model model) {
        model.addAttribute("startDate", LocalDate.now().minusDays(30));
        model.addAttribute("endDate", LocalDate.now());
        return "compliance/verify";
    }
    
    @PostMapping("/verify")
    public String verifyCompliance(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            RedirectAttributes redirectAttributes) {
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        boolean isCompliant = complianceService.verifyCompliance(start, end);
        
        if (isCompliant) {
            redirectAttributes.addFlashAttribute("success", 
                "Compliance verified! No violations found between " + startDate + " and " + endDate);
        } else {
            redirectAttributes.addFlashAttribute("warning", 
                "Compliance issues found! Please review the failures report.");
        }
        
        return "redirect:/compliance/summary?startDate=" + startDate + "&endDate=" + endDate;
    }
}
