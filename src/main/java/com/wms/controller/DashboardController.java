package com.wms.controller;

import com.wms.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

/**
 * ========================================
 * DASHBOARD CONTROLLER
 * ========================================
 */
@Controller
public class DashboardController {
    
    private final ClientService clientService;
    private final PortfolioService portfolioService;
    private final InvestmentPlanService planService;
    private final ReportService reportService;
    
    public DashboardController(ClientService clientService,
                               PortfolioService portfolioService,
                               InvestmentPlanService planService,
                               ReportService reportService) {
        this.clientService = clientService;
        this.portfolioService = portfolioService;
        this.planService = planService;
        this.reportService = reportService;
    }
    
    /**
     * SHOW DASHBOARD
     * --------------
     * Main dashboard page showing summary statistics.
     * 
     * Authentication parameter is automatically injected by Spring Security.
     * It contains information about the currently logged-in user.
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model, Authentication authentication) {
        // Get current user's name for personalized greeting
        String username = authentication.getName();
        model.addAttribute("username", username);
        
        // Get summary statistics
        long totalClients = clientService.getClientCount();
        long totalPlans = planService.getAllPlans().size();
        long totalPortfolios = portfolioService.getAllPortfolios().size();
        long totalReports = reportService.countTotalReports();
        BigDecimal totalAUM = portfolioService.calculateTotalAUM();
        
        // Add statistics to model
        model.addAttribute("totalClients", totalClients);
        model.addAttribute("totalPlans", totalPlans);
        model.addAttribute("totalPortfolios", totalPortfolios);
        model.addAttribute("totalReports", totalReports);
        model.addAttribute("totalAUM", totalAUM);
        
        // Get recent data for dashboard widgets
        model.addAttribute("recentClients", clientService.getAllClients());
        model.addAttribute("recentPortfolios", portfolioService.getAllPortfolios());
        
        return "dashboard";  // Returns templates/dashboard.html
    }
}
