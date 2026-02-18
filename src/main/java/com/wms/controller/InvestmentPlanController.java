package com.wms.controller;

import com.wms.entity.Client;
import com.wms.entity.InvestmentPlan;
import com.wms.entity.InvestmentPlan.PlanStatus;
import com.wms.entity.InvestmentPlan.RiskAppetite;
import com.wms.entity.Portfolio;
import com.wms.service.ClientService;
import com.wms.service.InvestmentPlanService;
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
 * INVESTMENT PLAN CONTROLLER
 * ========================================
 * 
 */
@Controller
@RequestMapping("/plans")
public class InvestmentPlanController {
    
    private final InvestmentPlanService planService;
    private final ClientService clientService;
    private final PortfolioService portfolioService;
    
    public InvestmentPlanController(InvestmentPlanService planService, 
                                    ClientService clientService,
                                    PortfolioService portfolioService) {
        this.planService = planService;
        this.clientService = clientService;
        this.portfolioService = portfolioService;
    }
    
    // ========================================
    // LIST OPERATIONS
    // ========================================
    
    /**
     * LIST ALL PLANS
     * --------------
     */
    @GetMapping
    public String listPlans(Model model) {
        List<InvestmentPlan> plans = planService.getAllPlans();
        model.addAttribute("plans", plans);
        return "plan/list";
    }
    
    /**
     * LIST PLANS BY CLIENT
     * --------------------
     */
    @GetMapping("/client/{clientId}")
    public String listClientPlans(@PathVariable("clientId") Long clientId, Model model) {
        Client client = clientService.getClientById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        List<InvestmentPlan> plans = planService.getClientPlans(clientId);
        
        model.addAttribute("client", client);
        model.addAttribute("plans", plans);
        return "plan/client-plans";
    }
    
    // ========================================
    // VIEW OPERATIONS
    // ========================================
    
    /**
     * VIEW PLAN DETAILS
     * -----------------
     * Shows plan details along with any linked portfolios.
     */
    @GetMapping("/{id}")
    public String viewPlan(@PathVariable("id") Long id, Model model) {
        InvestmentPlan plan = planService.getPlanById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found with ID: " + id));
        
        // Get portfolios created from this plan
        List<Portfolio> linkedPortfolios = portfolioService.getPortfoliosByPlan(id);
        
        model.addAttribute("plan", plan);
        model.addAttribute("linkedPortfolios", linkedPortfolios);
        model.addAttribute("hasPortfolios", !linkedPortfolios.isEmpty());
        return "plan/view";
    }
    
    // ========================================
    // PORTFOLIO CREATION FROM PLAN
    // ========================================
    
    /**
     * SHOW CREATE PORTFOLIO FROM PLAN FORM
     * ------------------------------------
     * Shows a form to create a portfolio based on this plan.
     */
    @GetMapping("/{id}/create-portfolio")
    public String showCreatePortfolioForm(@PathVariable("id") Long id, Model model) {
        InvestmentPlan plan = planService.getPlanById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found with ID: " + id));
        
        model.addAttribute("plan", plan);
        model.addAttribute("suggestedAmount", plan.getTargetAmount());
        return "plan/create-portfolio";
    }
    
    /**
     * CREATE PORTFOLIO FROM PLAN
     * --------------------------
     * Automatically creates a portfolio based on the investment plan.
     * 
     * INTERVIEW EXPLANATION:
     * "This is where STRATEGY becomes REALITY. When a client approves
     * their investment plan, we create an actual portfolio that:
     * 1. Links back to the plan for tracking
     * 2. Uses the plan's allocation strategy
     * 3. Sets up the initial investment amount"
     */
    @PostMapping("/{id}/create-portfolio")
    public String createPortfolioFromPlan(
            @PathVariable("id") Long planId,
            @RequestParam("initialInvestment") BigDecimal initialInvestment,
            RedirectAttributes redirectAttributes) {
        
        try {
            Portfolio portfolio = portfolioService.createPortfolioFromPlan(planId, initialInvestment);
            redirectAttributes.addFlashAttribute("success", 
                "Portfolio '" + portfolio.getPortfolioName() + "' created successfully from the investment plan!");
            return "redirect:/portfolios/" + portfolio.getPortfolioId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/plans/" + planId;
        }
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
        
        InvestmentPlan plan = new InvestmentPlan();
        
        // If clientId provided, pre-select the client
        if (clientId != null) {
            Client client = clientService.getClientById(clientId).orElse(null);
            plan.setClient(client);
            model.addAttribute("selectedClientId", clientId);
        }
        
        model.addAttribute("plan", plan);
        model.addAttribute("clients", clientService.getAllClients());
        model.addAttribute("riskAppetites", RiskAppetite.values());
        return "plan/form";
    }
    
    /**
     * PROCESS CREATE
     * --------------
     */
    @PostMapping
    public String createPlan(
            @Valid @ModelAttribute("plan") InvestmentPlan plan,
            BindingResult bindingResult,
            @RequestParam("clientId") Long clientId,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("clients", clientService.getAllClients());
            model.addAttribute("riskAppetites", RiskAppetite.values());
            return "plan/form";
        }
        
        try {
            InvestmentPlan savedPlan = planService.createPlan(clientId, plan);
            redirectAttributes.addFlashAttribute("success", "Investment plan created successfully!");
            return "redirect:/plans/" + savedPlan.getPlanId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/plans/new?clientId=" + clientId;
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
        InvestmentPlan plan = planService.getPlanById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        model.addAttribute("plan", plan);
        model.addAttribute("clients", clientService.getAllClients());
        model.addAttribute("riskAppetites", RiskAppetite.values());
        model.addAttribute("statuses", PlanStatus.values());
        model.addAttribute("isEdit", true);
        return "plan/form";
    }
    
    /**
     * PROCESS UPDATE
     * --------------
     */
    @PostMapping("/{id}")
    public String updatePlan(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("plan") InvestmentPlan plan,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("clients", clientService.getAllClients());
            model.addAttribute("riskAppetites", RiskAppetite.values());
            model.addAttribute("statuses", PlanStatus.values());
            model.addAttribute("isEdit", true);
            return "plan/form";
        }
        
        try {
            planService.updatePlan(id, plan);
            redirectAttributes.addFlashAttribute("success", "Investment plan updated successfully!");
            return "redirect:/plans/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/plans/" + id + "/edit";
        }
    }
    
    // ========================================
    // STATUS OPERATIONS
    // ========================================
    
    /**
     * PAUSE PLAN
     * ----------
     */
    @PostMapping("/{id}/pause")
    public String pausePlan(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            planService.pausePlan(id);
            redirectAttributes.addFlashAttribute("success", "Plan paused successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/plans/" + id;
    }
    
    /**
     * RESUME PLAN
     * -----------
     */
    @PostMapping("/{id}/resume")
    public String resumePlan(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            planService.resumePlan(id);
            redirectAttributes.addFlashAttribute("success", "Plan resumed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/plans/" + id;
    }
    
    /**
     * COMPLETE PLAN
     * -------------
     */
    @PostMapping("/{id}/complete")
    public String completePlan(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            planService.completePlan(id);
            redirectAttributes.addFlashAttribute("success", "Plan marked as completed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/plans/" + id;
    }
    
    // ========================================
    // DELETE OPERATIONS
    // ========================================
    
    /**
     * DELETE PLAN
     * -----------
     */
    @PostMapping("/{id}/delete")
    public String deletePlan(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            planService.deletePlan(id);
            redirectAttributes.addFlashAttribute("success", "Plan deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/plans";
    }
}
