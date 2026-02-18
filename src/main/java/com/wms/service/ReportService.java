package com.wms.service;

import com.wms.entity.Portfolio;
import com.wms.entity.Report;
import com.wms.entity.Report.ReportType;
import com.wms.repository.PortfolioRepository;
import com.wms.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ========================================
 * REPORT SERVICE
 * ========================================
 * 
 */
@Service
@Transactional
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final PortfolioRepository portfolioRepository;
    
    public ReportService(ReportRepository reportRepository,
                        PortfolioRepository portfolioRepository) {
        this.reportRepository = reportRepository;
        this.portfolioRepository = portfolioRepository;
    }
    
    // ========================================
    // CREATE OPERATIONS
    // ========================================
    
    /**
     * GENERATE REPORT
     * ---------------
     * Creates a new report for a portfolio.
     */
    public Report generateReport(Long portfolioId, Report report) {
        Portfolio portfolio = portfolioRepository.findById(Objects.requireNonNull(portfolioId))
                .orElseThrow(() -> new RuntimeException("Portfolio not found with ID: " + portfolioId));
        
        report.setPortfolio(portfolio);
        report.setReportDate(LocalDate.now());
        
        // Set default report type if not provided
        if (report.getReportType() == null) {
            report.setReportType(ReportType.CUSTOM);
        }
        
        // Generate performance summary if not provided or empty
        if (report.getPerformanceSummary() == null || report.getPerformanceSummary().trim().isEmpty()) {
            report.setPerformanceSummary(generatePerformanceSummary(portfolio));
        }
        
        return reportRepository.save(report);
    }
    
    /**
     * GENERATE QUICK REPORT
     * ---------------------
     * Creates a report with minimal input.
     */
    public Report generateQuickReport(Long portfolioId, ReportType reportType, Long userId) {
        Portfolio portfolio = portfolioRepository.findById(Objects.requireNonNull(portfolioId))
                .orElseThrow(() -> new RuntimeException("Portfolio not found with ID: " + portfolioId));
        
        Report report = new Report();
        report.setPortfolio(portfolio);
        report.setReportType(reportType);
        report.setReportTitle(generateReportTitle(reportType, portfolio.getPortfolioName()));
        report.setReportDate(LocalDate.now());
        report.setPerformanceSummary(generatePerformanceSummary(portfolio));
        report.setGeneratedBy(userId);
        
        return reportRepository.save(report);
    }
    
    /**
     * GENERATE PERFORMANCE SUMMARY
     * ----------------------------
     * Creates a user-friendly summary of portfolio performance.
     */
    private String generatePerformanceSummary(Portfolio portfolio) {
        java.math.BigDecimal returnPct = portfolio.calculateReturnPercentage();
        java.math.BigDecimal profitLoss = portfolio.calculateProfitLoss();
        java.math.BigDecimal initialInvestment = portfolio.getInitialInvestment() != null 
            ? portfolio.getInitialInvestment() 
            : portfolio.getTotalValue();
        
        String performanceStatus;
        if (returnPct.compareTo(java.math.BigDecimal.ZERO) > 0) {
            performanceStatus = "positive growth";
        } else if (returnPct.compareTo(java.math.BigDecimal.ZERO) < 0) {
            performanceStatus = "decline";
        } else {
            performanceStatus = "no change";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Portfolio Performance Summary\n");
        summary.append("═══════════════════════════════════════\n\n");
        summary.append(String.format("Portfolio: %s\n\n", portfolio.getPortfolioName()));
        summary.append("Financial Overview:\n");
        summary.append(String.format("  • Initial Investment:  ₹%,.2f\n", initialInvestment));
        summary.append(String.format("  • Current Value:       ₹%,.2f\n", portfolio.getTotalValue()));
        summary.append(String.format("  • Profit/Loss:         %s₹%,.2f\n", 
            profitLoss.compareTo(java.math.BigDecimal.ZERO) >= 0 ? "+" : "-", 
            profitLoss.abs()));
        summary.append(String.format("  • Return Rate:         %s%.2f%%\n\n", 
            returnPct.compareTo(java.math.BigDecimal.ZERO) >= 0 ? "+" : "", 
            returnPct));
        summary.append(String.format("Status: This portfolio has shown %s since inception.\n", performanceStatus));
        summary.append(String.format("\nLast Updated: %s", 
            portfolio.getLastUpdated() != null ? portfolio.getLastUpdated().toString() : "N/A"));
        
        return summary.toString();
    }
    
    /**
     * GENERATE REPORT TITLE
     * ---------------------
     */
    private String generateReportTitle(ReportType type, String portfolioName) {
        String prefix;
        switch (type) {
            case MONTHLY:
                prefix = "Monthly Performance Report";
                break;
            case QUARTERLY:
                prefix = "Quarterly Performance Report";
                break;
            case ANNUAL:
                prefix = "Annual Performance Report";
                break;
            case TAX:
                prefix = "Tax Summary Report";
                break;
            default:
                prefix = "Performance Report";
        }
        return prefix + " - " + portfolioName + " - " + LocalDate.now();
    }
    
    // ========================================
    // READ OPERATIONS
    // ========================================
    
    /**
     * GET REPORT BY ID
     * ----------------
     */
    @Transactional(readOnly = true)
    public Optional<Report> getReportById(Long reportId) {
        return reportRepository.findById(Objects.requireNonNull(reportId));
    }
    
    /**
     * GET REPORTS FOR PORTFOLIO
     * -------------------------
     */
    @Transactional(readOnly = true)
    public List<Report> getPortfolioReports(Long portfolioId) {
        return reportRepository.findByPortfolioPortfolioId(portfolioId);
    }
    
    /**
     * GET REPORTS FOR CLIENT
     * ----------------------
     */
    @Transactional(readOnly = true)
    public List<Report> getClientReports(Long clientId) {
        return reportRepository.findReportsByClientId(clientId);
    }
    
    /**
     * GET ALL REPORTS
     * ---------------
     */
    @Transactional(readOnly = true)
    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByReportDateDesc();
    }
    
    /**
     * GET REPORTS BY TYPE
     * -------------------
     */
    @Transactional(readOnly = true)
    public List<Report> getReportsByType(ReportType reportType) {
        return reportRepository.findByReportType(reportType);
    }
    
    /**
     * GET REPORTS BY DATE RANGE
     * -------------------------
     */
    @Transactional(readOnly = true)
    public List<Report> getReportsByDateRange(LocalDate startDate, LocalDate endDate) {
        return reportRepository.findByReportDateBetween(startDate, endDate);
    }
    
    /**
     * GET LATEST REPORT FOR PORTFOLIO
     * -------------------------------
     */
    @Transactional(readOnly = true)
    public Report getLatestReportForPortfolio(Long portfolioId) {
        return reportRepository.findLatestReportForPortfolio(portfolioId);
    }
    
    // ========================================
    // UPDATE OPERATIONS
    // ========================================
    
    /**
     * UPDATE REPORT
     * -------------
     */
    public Report updateReport(Long reportId, Report updatedReport) {
        Report existing = reportRepository.findById(Objects.requireNonNull(reportId))
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
        
        existing.setReportTitle(updatedReport.getReportTitle());
        existing.setReportType(updatedReport.getReportType());
        existing.setPerformanceSummary(updatedReport.getPerformanceSummary());
        existing.setNotes(updatedReport.getNotes());
        
        return reportRepository.save(existing);
    }
    
    /**
     * ADD NOTES TO REPORT
     * -------------------
     */
    public Report addNotesToReport(Long reportId, String notes) {
        Report report = reportRepository.findById(Objects.requireNonNull(reportId))
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
        
        report.setNotes(notes);
        return reportRepository.save(report);
    }
    
    // ========================================
    // DELETE OPERATIONS
    // ========================================
    
    /**
     * DELETE REPORT
     * -------------
     */
    public void deleteReport(Long reportId) {
        if (!reportRepository.existsById(Objects.requireNonNull(reportId))) {
            throw new RuntimeException("Report not found with ID: " + reportId);
        }
        reportRepository.deleteById(reportId);
    }
    
    // ========================================
    // STATISTICS
    // ========================================
    
    /**
     * COUNT REPORTS BY TYPE
     * ---------------------
     */
    @Transactional(readOnly = true)
    public long countReportsByType(ReportType reportType) {
        return reportRepository.countByReportType(reportType);
    }
    
    /**
     * COUNT TOTAL REPORTS
     * -------------------
     */
    @Transactional(readOnly = true)
    public long countTotalReports() {
        return reportRepository.count();
    }
}
