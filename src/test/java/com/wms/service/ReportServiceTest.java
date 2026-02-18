package com.wms.service;

import com.wms.entity.Portfolio;
import com.wms.entity.Report;
import com.wms.entity.Report.ReportType;
import com.wms.repository.PortfolioRepository;
import com.wms.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportService
 * Achieves 100% code coverage for all ReportService methods
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private ReportService reportService;

    private Portfolio testPortfolio;
    private Report testReport;

    @BeforeEach
    void setUp() {
        testPortfolio = new Portfolio();
        testPortfolio.setPortfolioId(1L);
        testPortfolio.setPortfolioName("Retirement Fund");
        testPortfolio.setTotalValue(new BigDecimal("100000.00"));
        testPortfolio.setInitialInvestment(new BigDecimal("80000.00"));
        testPortfolio.setLastUpdated(LocalDate.now());

        testReport = new Report();
        testReport.setReportId(1L);
        testReport.setPortfolio(testPortfolio);
        testReport.setReportTitle("Q4 2025 Performance Report");
        testReport.setReportType(ReportType.QUARTERLY);
        testReport.setReportDate(LocalDate.now());
        testReport.setPerformanceSummary("{\"totalValue\": 100000.00}");
        testReport.setNotes("Good performance this quarter");
        testReport.setGeneratedBy(1L);
    }

    // ========================================
    // CREATE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Generate Report Tests")
    class GenerateReportTests {

        @Test
        @DisplayName("Should generate report successfully")
        void generateReport_Success() {
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(reportRepository.save(any(Report.class))).thenReturn(testReport);

            Report result = reportService.generateReport(1L, testReport);

            assertNotNull(result);
            verify(reportRepository).save(any(Report.class));
        }

        @Test
        @DisplayName("Should set default report type to CUSTOM when not provided")
        void generateReport_SetsDefaultType() {
            Report newReport = new Report();
            newReport.setReportTitle("Custom Report");
            newReport.setPerformanceSummary("{\"data\": true}");

            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Report result = reportService.generateReport(1L, newReport);

            assertEquals(ReportType.CUSTOM, result.getReportType());
        }

        @Test
        @DisplayName("Should generate performance summary when not provided")
        void generateReport_GeneratesPerformanceSummary() {
            Report newReport = new Report();
            newReport.setReportTitle("Auto Summary Report");
            newReport.setReportType(ReportType.MONTHLY);
            // No performance summary

            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Report result = reportService.generateReport(1L, newReport);

            assertNotNull(result.getPerformanceSummary());
            assertTrue(result.getPerformanceSummary().contains("Current Value"));
        }

        @Test
        @DisplayName("Should generate performance summary when provided empty string")
        void generateReport_GeneratesPerformanceSummary_WhenEmptyString() {
            Report newReport = new Report();
            newReport.setReportTitle("Auto Summary Report");
            newReport.setReportType(ReportType.MONTHLY);
            newReport.setPerformanceSummary("   "); // Empty after trim

            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Report result = reportService.generateReport(1L, newReport);

            assertNotNull(result.getPerformanceSummary());
            assertTrue(result.getPerformanceSummary().contains("Portfolio Performance Summary"));
        }

        @Test
        @DisplayName("Should generate performance summary with negative return")
        void generateReport_PerformanceSummary_NegativeReturn() {
            // Setup portfolio with negative return
            testPortfolio.setInitialInvestment(new java.math.BigDecimal("150000.00"));
            testPortfolio.setTotalValue(new java.math.BigDecimal("100000.00")); // Loss
            
            Report newReport = new Report();
            newReport.setReportTitle("Negative Performance Report");
            newReport.setReportType(ReportType.MONTHLY);

            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Report result = reportService.generateReport(1L, newReport);

            assertNotNull(result.getPerformanceSummary());
            assertTrue(result.getPerformanceSummary().contains("decline"));
        }

        @Test
        @DisplayName("Should generate performance summary with zero return")
        void generateReport_PerformanceSummary_ZeroReturn() {
            // Setup portfolio with no change
            testPortfolio.setInitialInvestment(new java.math.BigDecimal("100000.00"));
            testPortfolio.setTotalValue(new java.math.BigDecimal("100000.00")); // No change
            
            Report newReport = new Report();
            newReport.setReportTitle("Zero Performance Report");
            newReport.setReportType(ReportType.MONTHLY);

            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Report result = reportService.generateReport(1L, newReport);

            assertNotNull(result.getPerformanceSummary());
            assertTrue(result.getPerformanceSummary().contains("no change"));
        }

        @Test
        @DisplayName("Should throw exception when portfolio not found")
        void generateReport_PortfolioNotFound_ThrowsException() {
            when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reportService.generateReport(999L, testReport));

            assertEquals("Portfolio not found with ID: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should generate quick report - MONTHLY")
        void generateQuickReport_Monthly_Success() {
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Report result = reportService.generateQuickReport(1L, ReportType.MONTHLY, 1L);

            assertNotNull(result);
            assertEquals(ReportType.MONTHLY, result.getReportType());
            assertTrue(result.getReportTitle().contains("Monthly Performance Report"));
        }

        @Test
        @DisplayName("Should generate quick report - QUARTERLY")
        void generateQuickReport_Quarterly_Success() {
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Report result = reportService.generateQuickReport(1L, ReportType.QUARTERLY, 1L);

            assertTrue(result.getReportTitle().contains("Quarterly Performance Report"));
        }

        @Test
        @DisplayName("Should generate quick report - ANNUAL")
        void generateQuickReport_Annual_Success() {
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Report result = reportService.generateQuickReport(1L, ReportType.ANNUAL, 1L);

            assertTrue(result.getReportTitle().contains("Annual Performance Report"));
        }

        @Test
        @DisplayName("Should generate quick report - TAX")
        void generateQuickReport_Tax_Success() {
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Report result = reportService.generateQuickReport(1L, ReportType.TAX, 1L);

            assertTrue(result.getReportTitle().contains("Tax Summary Report"));
        }

        @Test
        @DisplayName("Should generate quick report - CUSTOM")
        void generateQuickReport_Custom_Success() {
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Report result = reportService.generateQuickReport(1L, ReportType.CUSTOM, 1L);

            assertTrue(result.getReportTitle().contains("Performance Report"));
        }

        @Test
        @DisplayName("Should throw exception when portfolio not found for quick report")
        void generateQuickReport_PortfolioNotFound_ThrowsException() {
            when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reportService.generateQuickReport(999L, ReportType.MONTHLY, 1L));

            assertEquals("Portfolio not found with ID: 999", exception.getMessage());
        }
    }

    // ========================================
    // READ OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Read Report Tests")
    class ReadReportTests {

        @Test
        @DisplayName("Should return report by ID when exists")
        void getReportById_Found() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

            Optional<Report> result = reportService.getReportById(1L);

            assertTrue(result.isPresent());
            assertEquals("Q4 2025 Performance Report", result.get().getReportTitle());
        }

        @Test
        @DisplayName("Should return empty when report not found")
        void getReportById_NotFound() {
            when(reportRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Report> result = reportService.getReportById(999L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return reports for portfolio")
        void getPortfolioReports_ReturnsList() {
            when(reportRepository.findByPortfolioPortfolioId(1L)).thenReturn(Arrays.asList(testReport));

            List<Report> result = reportService.getPortfolioReports(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return reports for client")
        void getClientReports_ReturnsList() {
            when(reportRepository.findReportsByClientId(1L)).thenReturn(Arrays.asList(testReport));

            List<Report> result = reportService.getClientReports(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return all reports ordered by date")
        void getAllReports_ReturnsList() {
            when(reportRepository.findAllByOrderByReportDateDesc()).thenReturn(Arrays.asList(testReport));

            List<Report> result = reportService.getAllReports();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return reports by type")
        void getReportsByType_ReturnsList() {
            when(reportRepository.findByReportType(ReportType.QUARTERLY)).thenReturn(Arrays.asList(testReport));

            List<Report> result = reportService.getReportsByType(ReportType.QUARTERLY);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return reports by date range")
        void getReportsByDateRange_ReturnsList() {
            LocalDate start = LocalDate.now().minusMonths(1);
            LocalDate end = LocalDate.now();
            
            when(reportRepository.findByReportDateBetween(start, end)).thenReturn(Arrays.asList(testReport));

            List<Report> result = reportService.getReportsByDateRange(start, end);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return latest report for portfolio")
        void getLatestReportForPortfolio_ReturnsReport() {
            when(reportRepository.findLatestReportForPortfolio(1L)).thenReturn(testReport);

            Report result = reportService.getLatestReportForPortfolio(1L);

            assertNotNull(result);
            assertEquals("Q4 2025 Performance Report", result.getReportTitle());
        }
    }

    // ========================================
    // UPDATE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Update Report Tests")
    class UpdateReportTests {

        @Test
        @DisplayName("Should update report successfully")
        void updateReport_Success() {
            Report updatedReport = new Report();
            updatedReport.setReportTitle("Updated Report Title");
            updatedReport.setReportType(ReportType.ANNUAL);
            updatedReport.setPerformanceSummary("{\"updated\": true}");
            updatedReport.setNotes("Updated notes");

            when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
            when(reportRepository.save(any(Report.class))).thenReturn(testReport);

            Report result = reportService.updateReport(1L, updatedReport);

            assertNotNull(result);
            verify(reportRepository).save(any(Report.class));
        }

        @Test
        @DisplayName("Should throw exception when report not found for update")
        void updateReport_NotFound_ThrowsException() {
            when(reportRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reportService.updateReport(999L, testReport));

            assertEquals("Report not found with ID: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should add notes to report successfully")
        void addNotesToReport_Success() {
            when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
            when(reportRepository.save(any(Report.class))).thenReturn(testReport);

            Report result = reportService.addNotesToReport(1L, "Additional notes");

            assertNotNull(result);
            verify(reportRepository).save(any(Report.class));
        }

        @Test
        @DisplayName("Should throw exception when report not found for adding notes")
        void addNotesToReport_NotFound_ThrowsException() {
            when(reportRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reportService.addNotesToReport(999L, "Notes"));

            assertEquals("Report not found with ID: 999", exception.getMessage());
        }
    }

    // ========================================
    // DELETE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Delete Report Tests")
    class DeleteReportTests {

        @Test
        @DisplayName("Should delete report successfully")
        void deleteReport_Success() {
            when(reportRepository.existsById(1L)).thenReturn(true);
            doNothing().when(reportRepository).deleteById(1L);

            assertDoesNotThrow(() -> reportService.deleteReport(1L));

            verify(reportRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent report")
        void deleteReport_NotFound_ThrowsException() {
            when(reportRepository.existsById(999L)).thenReturn(false);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reportService.deleteReport(999L));

            assertEquals("Report not found with ID: 999", exception.getMessage());
        }
    }

    // ========================================
    // STATISTICS TESTS
    // ========================================

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should count reports by type")
        void countReportsByType_ReturnsCount() {
            when(reportRepository.countByReportType(ReportType.QUARTERLY)).thenReturn(10L);

            assertEquals(10L, reportService.countReportsByType(ReportType.QUARTERLY));
        }

        @Test
        @DisplayName("Should count total reports")
        void countTotalReports_ReturnsCount() {
            when(reportRepository.count()).thenReturn(50L);

            assertEquals(50L, reportService.countTotalReports());
        }
    }
}
