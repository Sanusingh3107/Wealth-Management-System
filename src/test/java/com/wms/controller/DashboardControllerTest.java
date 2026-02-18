package com.wms.controller;

import com.wms.entity.Client;
import com.wms.entity.Portfolio;
import com.wms.service.ClientService;
import com.wms.service.InvestmentPlanService;
import com.wms.service.PortfolioService;
import com.wms.service.ReportService;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController Tests")
class DashboardControllerTest {

    @Mock
    private ClientService clientService;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private InvestmentPlanService planService;

    @Mock
    private ReportService reportService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    @Nested
    @DisplayName("Dashboard Display Tests")
    class DashboardDisplayTests {

        @Test
        @DisplayName("Should show dashboard with all statistics")
        void showDashboard_ReturnsViewWithStats() {
            // Setup
            when(authentication.getName()).thenReturn("admin");
            when(clientService.getClientCount()).thenReturn(10L);
            when(planService.getAllPlans()).thenReturn(Collections.emptyList());
            when(portfolioService.getAllPortfolios()).thenReturn(Collections.emptyList());
            when(reportService.countTotalReports()).thenReturn(5L);
            when(portfolioService.calculateTotalAUM()).thenReturn(new BigDecimal("1000000.00"));
            when(clientService.getAllClients()).thenReturn(Collections.emptyList());
            
            // Execute
            String result = dashboardController.showDashboard(model, authentication);
            
            // Verify
            assertEquals("dashboard", result);
            verify(model).addAttribute("username", "admin");
            verify(model).addAttribute("totalClients", 10L);
            verify(model).addAttribute("totalPlans", 0L);
            verify(model).addAttribute("totalPortfolios", 0L);
            verify(model).addAttribute("totalReports", 5L);
            verify(model).addAttribute("totalAUM", new BigDecimal("1000000.00"));
        }

        @Test
        @DisplayName("Should show dashboard with recent data")
        void showDashboard_IncludesRecentData() {
            Client client = new Client();
            client.setName("Test Client");
            
            Portfolio portfolio = new Portfolio();
            portfolio.setPortfolioName("Test Portfolio");
            
            when(authentication.getName()).thenReturn("advisor");
            when(clientService.getClientCount()).thenReturn(1L);
            when(planService.getAllPlans()).thenReturn(Collections.emptyList());
            when(portfolioService.getAllPortfolios()).thenReturn(Arrays.asList(portfolio));
            when(reportService.countTotalReports()).thenReturn(0L);
            when(portfolioService.calculateTotalAUM()).thenReturn(BigDecimal.ZERO);
            when(clientService.getAllClients()).thenReturn(Arrays.asList(client));
            
            String result = dashboardController.showDashboard(model, authentication);
            
            assertEquals("dashboard", result);
            verify(model).addAttribute(eq("recentClients"), anyList());
            verify(model).addAttribute(eq("recentPortfolios"), anyList());
        }
    }
}
