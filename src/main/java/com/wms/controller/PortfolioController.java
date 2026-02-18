package com.wms.controller;

import com.wms.entity.Client;
import com.wms.entity.Portfolio;
import com.wms.service.ClientService;
import com.wms.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * ========================================
 * PORTFOLIO CONTROLLER
 * ========================================

 */
@Controller
@RequestMapping("/portfolios")
public class PortfolioController {
    
    private final PortfolioService portfolioService;
    private final ClientService clientService;
    
    public PortfolioController(PortfolioService portfolioService, ClientService clientService) {
        this.portfolioService = portfolioService;
        this.clientService = clientService;
    }
    
    // ========================================
    // LIST OPERATIONS
    // ========================================
    
    /**
     * LIST ALL PORTFOLIOS
     * -------------------
     */
    @GetMapping
    public String listPortfolios(Model model) {
        List<Portfolio> portfolios = portfolioService.getAllPortfolios();
        BigDecimal totalAUM = portfolioService.calculateTotalAUM();
        
        model.addAttribute("portfolios", portfolios);
        model.addAttribute("totalAUM", totalAUM);
        return "portfolio/list";
    }
    
    /**
     * LIST CLIENT PORTFOLIOS
     * ----------------------
     */
    @GetMapping("/client/{clientId}")
    public String listClientPortfolios(@PathVariable("clientId") Long clientId, Model model) {
        Client client = clientService.getClientById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        List<Portfolio> portfolios = portfolioService.getClientPortfolios(clientId);
        BigDecimal totalValue = portfolioService.calculateClientTotalValue(clientId);
        
        model.addAttribute("client", client);
        model.addAttribute("portfolios", portfolios);
        model.addAttribute("totalValue", totalValue);
        return "portfolio/client-portfolios";
    }
    
    // ========================================
    // VIEW OPERATIONS
    // ========================================
    
    /**
     * VIEW PORTFOLIO DETAILS
     * ----------------------
     */
    @GetMapping("/{id}")
    public String viewPortfolio(@PathVariable("id") Long id, Model model) {
        Portfolio portfolio = portfolioService.getPortfolioByIdWithReports(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found with ID: " + id));
        
        BigDecimal returnPercentage = portfolioService.calculatePortfolioReturn(id);
        String performanceSummary = portfolioService.getPortfolioPerformanceSummary(id);
        
        model.addAttribute("portfolio", portfolio);
        model.addAttribute("returnPercentage", returnPercentage);
        model.addAttribute("performanceSummary", performanceSummary);
        return "portfolio/view";
    }
    
    /**
     * VIEW PORTFOLIO PERFORMANCE
     * --------------------------
     */
    @GetMapping("/{id}/performance")
    public String viewPerformance(@PathVariable("id") Long id, Model model) {
        Portfolio portfolio = portfolioService.getPortfolioById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        model.addAttribute("portfolio", portfolio);
        model.addAttribute("returnPercentage", portfolio.calculateReturnPercentage());
        model.addAttribute("profitLoss", portfolio.calculateProfitLoss());
        return "portfolio/performance";
    }
    
    // ========================================
    // CREATE OPERATIONS
    // ========================================
    
    /**
     * SHOW CREATE FORM
     * ----------------
     */
    @GetMapping("/new")
    public String showCreateForm(
            @RequestParam(value = "clientId", required = false) Long clientId,
            Model model) {
        
        Portfolio portfolio = new Portfolio();
        
        if (clientId != null) {
            Client client = clientService.getClientById(clientId).orElse(null);
            portfolio.setClient(client);
            model.addAttribute("selectedClientId", clientId);
        }
        
        model.addAttribute("portfolio", portfolio);
        model.addAttribute("clients", clientService.getAllClients());
        return "portfolio/form";
    }
    
    /**
     * PROCESS CREATE
     * --------------
     */
    @PostMapping
    public String createPortfolio(
            @Valid @ModelAttribute("portfolio") Portfolio portfolio,
            BindingResult bindingResult,
            @RequestParam(value = "clientId", required = false) Long clientId,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        // If clientId is not provided as a request param, get it from the portfolio's client
        if (clientId == null && portfolio.getClient() != null) {
            clientId = portfolio.getClient().getClientId();
        }
        
        if (clientId == null) {
            model.addAttribute("clients", clientService.getAllClients());
            model.addAttribute("error", "Please select a client");
            return "portfolio/form";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("clients", clientService.getAllClients());
            return "portfolio/form";
        }
        
        try {
            Portfolio saved = portfolioService.createPortfolio(clientId, portfolio);
            redirectAttributes.addFlashAttribute("success", "Portfolio '" + saved.getPortfolioName() + "' created successfully!");
            return "redirect:/portfolios";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/portfolios/new?clientId=" + clientId;
        }
    }
    
    // ========================================
    // UPDATE OPERATIONS
    // ========================================
    
    /**
     * SHOW EDIT FORM
     * --------------
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Portfolio portfolio = portfolioService.getPortfolioById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        model.addAttribute("portfolio", portfolio);
        model.addAttribute("clients", clientService.getAllClients());
        model.addAttribute("isEdit", true);
        if (portfolio.getClient() != null) {
            model.addAttribute("selectedClientId", portfolio.getClient().getClientId());
        }
        return "portfolio/form";
    }
    
    /**
     * PROCESS UPDATE
     * --------------
     */
    @PostMapping("/{id}")
    public String updatePortfolio(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("portfolio") Portfolio portfolio,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("clients", clientService.getAllClients());
            model.addAttribute("isEdit", true);
            return "portfolio/form";
        }
        
        try {
            portfolioService.updatePortfolio(id, portfolio);
            redirectAttributes.addFlashAttribute("success", "Portfolio updated successfully!");
            return "redirect:/portfolios";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/portfolios/" + id + "/edit";
        }
    }
    
    /**
     * UPDATE VALUE ONLY
     * -----------------
     */
    @PostMapping("/{id}/update-value")
    public String updateValue(
            @PathVariable("id") Long id,
            @RequestParam("newValue") BigDecimal newValue,
            RedirectAttributes redirectAttributes) {
        
        try {
            portfolioService.updatePortfolioValue(id, newValue);
            redirectAttributes.addFlashAttribute("success", "Portfolio value updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portfolios";
    }
    
    /**
     * REBALANCE PORTFOLIO
     * -------------------
     */
    @GetMapping("/{id}/rebalance")
    public String showRebalanceForm(@PathVariable("id") Long id, Model model) {
        Portfolio portfolio = portfolioService.getPortfolioById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        model.addAttribute("portfolio", portfolio);
        return "portfolio/rebalance";
    }
    
    @PostMapping("/{id}/rebalance")
    public String rebalancePortfolio(
            @PathVariable("id") Long id,
            @RequestParam("allocationSummary") String allocationSummary,
            RedirectAttributes redirectAttributes) {
        
        try {
            portfolioService.rebalancePortfolio(id, allocationSummary);
            redirectAttributes.addFlashAttribute("success", "Portfolio rebalanced successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portfolios";
    }
    
    // ========================================
    // DELETE OPERATIONS
    // ========================================
    
    @PostMapping("/{id}/delete")
    public String deletePortfolio(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            portfolioService.deletePortfolio(id);
            redirectAttributes.addFlashAttribute("success", "Portfolio deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portfolios";
    }
}
