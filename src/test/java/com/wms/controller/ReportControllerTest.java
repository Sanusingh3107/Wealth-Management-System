package com.wms.controller;

import com.wms.entity.Client;
import com.wms.entity.Portfolio;
import com.wms.entity.Report;
import com.wms.entity.Report.ReportType;
import com.wms.entity.User;
import com.wms.service.ClientService;
import com.wms.service.PortfolioService;
import com.wms.service.ReportService;
import com.wms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController Tests")
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private ClientService clientService;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReportController controller;

    private Report testReport;
    private Portfolio testPortfolio;
    private Client testClient;
    private User testUser;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setClientId(1L);
        testClient.setName("Test Client");

        testPortfolio = new Portfolio();
        testPortfolio.setPortfolioId(1L);
        testPortfolio.setClient(testClient);
        testPortfolio.setPortfolioName("Test Portfolio");

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("admin");

        testReport = new Report();
        testReport.setReportId(1L);
        testReport.setPortfolio(testPortfolio);
        testReport.setReportTitle("Q1 Report");
        testReport.setReportType(ReportType.QUARTERLY);
        testReport.setReportDate(LocalDate.now());
        testReport.setPerformanceSummary("Good performance");
        testReport.setGeneratedBy(1L);
    }

    @Nested
    @DisplayName("List Operations Tests")
    class ListOperationsTests {

        @Test
        @DisplayName("Should list all reports")
        void listReports_ReturnsListView() {
            List<Report> reports = Arrays.asList(testReport);
            when(reportService.getAllReports()).thenReturn(reports);
            
            String result = controller.listReports(model);
            
            assertEquals("report/list", result);
            verify(model).addAttribute("reports", reports);
            verify(model).addAttribute("reportTypes", ReportType.values());
        }

        @Test
        @DisplayName("Should list reports by type")
        void listReportsByType_ReturnsFilteredList() {
            List<Report> reports = Arrays.asList(testReport);
            when(reportService.getReportsByType(ReportType.QUARTERLY)).thenReturn(reports);
            
            String result = controller.listReportsByType(ReportType.QUARTERLY, model);
            
            assertEquals("report/list", result);
            verify(model).addAttribute("reports", reports);
            verify(model).addAttribute("selectedType", ReportType.QUARTERLY);
        }

        @Test
        @DisplayName("Should list portfolio reports")
        void listPortfolioReports_ReturnsPortfolioReportsView() {
            List<Report> reports = Arrays.asList(testReport);
            when(portfolioService.getPortfolioById(1L)).thenReturn(Optional.of(testPortfolio));
            when(reportService.getPortfolioReports(1L)).thenReturn(reports);
            
            String result = controller.listPortfolioReports(1L, model);
            
            assertEquals("report/portfolio-reports", result);
            verify(model).addAttribute("portfolio", testPortfolio);
            verify(model).addAttribute("reports", reports);
        }

        @Test
        @DisplayName("Should throw exception when portfolio not found")
        void listPortfolioReports_PortfolioNotFound_ThrowsException() {
            when(portfolioService.getPortfolioById(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> controller.listPortfolioReports(99L, model));
        }

        @Test
        @DisplayName("Should list client reports")
        void listClientReports_ReturnsClientReportsView() {
            List<Report> reports = Arrays.asList(testReport);
            when(reportService.getClientReports(1L)).thenReturn(reports);
            
            String result = controller.listClientReports(1L, model);
            
            assertEquals("report/list", result);
            verify(model).addAttribute("reports", reports);
            verify(model).addAttribute("clientId", 1L);
        }
    }

    @Nested
    @DisplayName("View Operations Tests")
    class ViewOperationsTests {

        @Test
        @DisplayName("Should view report details")
        void viewReport_ReturnsViewPage() {
            when(reportService.getReportById(1L)).thenReturn(Optional.of(testReport));
            when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
            
            String result = controller.viewReport(1L, model);
            
            assertEquals("report/view", result);
            verify(model).addAttribute("report", testReport);
            verify(model).addAttribute("generatedByUsername", "admin");
        }

        @Test
        @DisplayName("Should view report without generatedBy")
        void viewReport_NoGeneratedBy_ReturnsViewPage() {
            testReport.setGeneratedBy(null);
            when(reportService.getReportById(1L)).thenReturn(Optional.of(testReport));
            
            String result = controller.viewReport(1L, model);
            
            assertEquals("report/view", result);
            verify(model).addAttribute("report", testReport);
            verify(userService, never()).getUserById(any());
        }

        @Test
        @DisplayName("Should throw exception when report not found")
        void viewReport_NotFound_ThrowsException() {
            when(reportService.getReportById(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> controller.viewReport(99L, model));
        }

        @Test
        @DisplayName("Should parse allocation summary for display")
        void viewReport_WithAllocationSummary_ParsesAllocations() {
            testPortfolio.setAllocationSummary("VTI: $21,800 (40%)\nBND: $19,075 (35%)");
            when(reportService.getReportById(1L)).thenReturn(Optional.of(testReport));
            when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
            
            String result = controller.viewReport(1L, model);
            
            assertEquals("report/view", result);
            verify(model).addAttribute(eq("allocations"), anyList());
        }
    }

    @Nested
    @DisplayName("Generate Operations Tests")
    class GenerateOperationsTests {

        @Test
        @DisplayName("Should show new form")
        void showNewForm_ReturnsFormView() {
            when(portfolioService.getAllPortfolios()).thenReturn(Arrays.asList(testPortfolio));
            
            String result = controller.showNewForm(null, model);
            
            assertEquals("report/form", result);
            verify(model).addAttribute(eq("report"), any(Report.class));
            verify(model).addAttribute("portfolios", Arrays.asList(testPortfolio));
            verify(model).addAttribute("reportTypes", ReportType.values());
        }

        @Test
        @DisplayName("Should show generate form with pre-selected portfolio")
        void showGenerateForm_WithPortfolio_ReturnsFormViewWithSelectedPortfolio() {
            when(portfolioService.getPortfolioById(1L)).thenReturn(Optional.of(testPortfolio));
            when(portfolioService.getAllPortfolios()).thenReturn(Arrays.asList(testPortfolio));
            
            String result = controller.showGenerateForm(1L, model);
            
            assertEquals("report/form", result);
            verify(model).addAttribute("selectedPortfolioId", 1L);
        }

        @Test
        @DisplayName("Should return to form when binding errors exist")
        void createReport_WithBindingErrors_ReturnsFormView() {
            when(bindingResult.hasErrors()).thenReturn(true);
            when(portfolioService.getAllPortfolios()).thenReturn(Arrays.asList(testPortfolio));
            
            String result = controller.createReport(testReport, bindingResult, 1L, authentication, 
                    redirectAttributes, model);
            
            assertEquals("report/form", result);
            verify(reportService, never()).generateReport(anyLong(), any());
        }

        @Test
        @DisplayName("Should redirect to report on successful creation")
        void createReport_Success_RedirectsToReport() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(authentication.getName()).thenReturn("admin");
            when(userService.getUserByUsername("admin")).thenReturn(Optional.of(testUser));
            when(reportService.generateReport(eq(1L), any())).thenReturn(testReport);
            
            String result = controller.createReport(testReport, bindingResult, 1L, authentication, 
                    redirectAttributes, model);
            
            assertEquals("redirect:/reports/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should redirect to generate form when creation fails")
        void createReport_Exception_RedirectsToGenerateForm() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(reportService.generateReport(eq(1L), any())).thenThrow(new RuntimeException("Error"));
            
            String result = controller.createReport(testReport, bindingResult, 1L, null, 
                    redirectAttributes, model);
            
            assertEquals("redirect:/reports/generate?portfolioId=1", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }

        @Test
        @DisplayName("Should generate report with POST to /generate")
        void generateReport_Success_RedirectsToReport() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(authentication.getName()).thenReturn("admin");
            when(userService.getUserByUsername("admin")).thenReturn(Optional.of(testUser));
            when(reportService.generateReport(eq(1L), any())).thenReturn(testReport);
            
            String result = controller.generateReport(testReport, bindingResult, 1L, authentication, 
                    redirectAttributes, model);
            
            assertEquals("redirect:/reports/1", result);
        }

        @Test
        @DisplayName("Should quick generate report")
        void quickGenerate_Success_RedirectsToReport() {
            when(reportService.generateQuickReport(eq(1L), eq(ReportType.QUARTERLY), any())).thenReturn(testReport);
            
            String result = controller.quickGenerate(1L, ReportType.QUARTERLY, authentication, redirectAttributes);
            
            assertEquals("redirect:/reports/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should handle quick generate error")
        void quickGenerate_Exception_RedirectsToPortfolio() {
            when(reportService.generateQuickReport(eq(1L), eq(ReportType.QUARTERLY), any()))
                    .thenThrow(new RuntimeException("Error"));
            
            String result = controller.quickGenerate(1L, ReportType.QUARTERLY, authentication, redirectAttributes);
            
            assertEquals("redirect:/portfolios/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }

    @Nested
    @DisplayName("Update Operations Tests")
    class UpdateOperationsTests {

        @Test
        @DisplayName("Should add notes to report")
        void addNotes_Success_RedirectsToReport() {
            when(reportService.addNotesToReport(eq(1L), anyString())).thenReturn(testReport);
            
            String result = controller.addNotes(1L, "New notes", redirectAttributes);
            
            assertEquals("redirect:/reports/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should handle add notes error")
        void addNotes_Exception_RedirectsWithError() {
            when(reportService.addNotesToReport(eq(1L), anyString())).thenThrow(new RuntimeException("Error"));
            
            String result = controller.addNotes(1L, "New notes", redirectAttributes);
            
            assertEquals("redirect:/reports/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }

    @Nested
    @DisplayName("Delete Operations Tests")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should delete report and redirect to list")
        void deleteReport_Success_RedirectsToList() {
            doNothing().when(reportService).deleteReport(1L);
            
            String result = controller.deleteReport(1L, redirectAttributes);
            
            assertEquals("redirect:/reports", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should handle delete report error")
        void deleteReport_Exception_RedirectsWithError() {
            doThrow(new RuntimeException("Error")).when(reportService).deleteReport(1L);
            
            String result = controller.deleteReport(1L, redirectAttributes);
            
            assertEquals("redirect:/reports", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }

    @Nested
    @DisplayName("Download Operations Tests")
    class DownloadOperationsTests {

        @Test
        @DisplayName("Should redirect with info message for download placeholder")
        void downloadReport_RedirectsWithInfo() {
            String result = controller.downloadReport(1L, redirectAttributes);
            
            assertEquals("redirect:/reports/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("info"), anyString());
        }
    }
}
