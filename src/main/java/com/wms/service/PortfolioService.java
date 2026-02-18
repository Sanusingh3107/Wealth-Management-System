package com.wms.service;

import com.wms.entity.Client;
import com.wms.entity.InvestmentPlan;
import com.wms.entity.Portfolio;
import com.wms.repository.ClientRepository;
import com.wms.repository.InvestmentPlanRepository;
import com.wms.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ========================================
 * PORTFOLIO SERVICE
 * ========================================
 * 
 */
@Service
@Transactional
public class PortfolioService {
    
    private final PortfolioRepository portfolioRepository;
    private final ClientRepository clientRepository;
    private final InvestmentPlanRepository investmentPlanRepository;
    
    public PortfolioService(PortfolioRepository portfolioRepository,
                           ClientRepository clientRepository,
                           InvestmentPlanRepository investmentPlanRepository) {
        this.portfolioRepository = portfolioRepository;
        this.clientRepository = clientRepository;
        this.investmentPlanRepository = investmentPlanRepository;
    }
    
    // ========================================
    // CREATE OPERATIONS
    // ========================================
    
    /**
     * CREATE PORTFOLIO
     * ----------------
     * Creates a new investment portfolio for a client.
     */
    public Portfolio createPortfolio(Long clientId, Portfolio portfolio) {
        Client client = clientRepository.findById(Objects.requireNonNull(clientId))
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        
        // Check for duplicate portfolio name
        if (portfolioRepository.existsByClientClientIdAndPortfolioName(clientId, portfolio.getPortfolioName())) {
            throw new RuntimeException("Portfolio with this name already exists for the client");
        }
        
        portfolio.setClient(client);
        portfolio.setLastUpdated(LocalDate.now());
        
        // Set initial investment if not provided
        if (portfolio.getInitialInvestment() == null) {
            portfolio.setInitialInvestment(portfolio.getTotalValue());
        }
        
        return portfolioRepository.save(portfolio);
    }
    
    /**
     * CREATE PORTFOLIO FROM INVESTMENT PLAN
     * -------------------------------------
     * Automatically creates a portfolio based on an investment plan's strategy.
     * 
     * This ensures the portfolio follows the agreed-upon strategy."
     * 
     * @param planId The investment plan to create a portfolio from
     * @param initialInvestment The amount the client is investing
     * @return The newly created portfolio
     */
    public Portfolio createPortfolioFromPlan(Long planId, BigDecimal initialInvestment) {
        InvestmentPlan plan = investmentPlanRepository.findById(Objects.requireNonNull(planId))
                .orElseThrow(() -> new RuntimeException("Investment Plan not found with ID: " + planId));
        
        Client client = plan.getClient();
        
        // Generate portfolio name from plan objective
        String portfolioName = generatePortfolioName(plan);
        
        // Check for duplicate portfolio name
        if (portfolioRepository.existsByClientClientIdAndPortfolioName(client.getClientId(), portfolioName)) {
            throw new RuntimeException("A portfolio for this plan already exists");
        }
        
        // Create the portfolio
        Portfolio portfolio = new Portfolio();
        portfolio.setClient(client);
        portfolio.setInvestmentPlan(plan);
        portfolio.setPortfolioName(portfolioName);
        portfolio.setTotalValue(initialInvestment);
        portfolio.setInitialInvestment(initialInvestment);
        portfolio.setAllocationSummary(convertAllocationToSummary(plan.getAllocationDetails(), initialInvestment));
        portfolio.setLastUpdated(LocalDate.now());
        
        return portfolioRepository.save(portfolio);
    }
    
    /**
     * GENERATE PORTFOLIO NAME
     * -----------------------
     * Creates a meaningful name for the portfolio based on the plan.
     */
    private String generatePortfolioName(InvestmentPlan plan) {
        return plan.getInvestmentObjective() + " Portfolio";
    }
    
    /**
     * CONVERT ALLOCATION TO SUMMARY
     * -----------------------------
     * Converts percentage allocation to actual amounts.
     * 
     * Example:
     * Input:  {"stocks": 60, "bonds": 30, "cash": 10}, ₹100,000
     * Output: "Stocks: ₹60,000 (60%)\nBonds: ₹30,000 (30%)\nCash: ₹10,000 (10%)"
     */
    private String convertAllocationToSummary(String allocationDetails, BigDecimal totalAmount) {
        if (allocationDetails == null || allocationDetails.isEmpty()) {
            return "No allocation details";
        }
        
        try {
            // Simple parsing for JSON like {"stocks": 60, "bonds": 30, "cash": 10}
            StringBuilder summary = new StringBuilder();
            String cleaned = allocationDetails.replace("{", "").replace("}", "").replace("\"", "");
            String[] pairs = cleaned.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.trim().split(":");
                if (keyValue.length == 2) {
                    String assetClass = keyValue[0].trim();
                    int percentage = Integer.parseInt(keyValue[1].trim());
                    BigDecimal amount = totalAmount.multiply(BigDecimal.valueOf(percentage))
                                                   .divide(BigDecimal.valueOf(100));
                    
                    // Capitalize first letter
                    String formattedAsset = assetClass.substring(0, 1).toUpperCase() + assetClass.substring(1);
                    summary.append(String.format("%s: ₹%,.2f (%d%%)\n", formattedAsset, amount, percentage));
                }
            }
            
            return summary.toString().trim();
        } catch (Exception e) {
            return allocationDetails; // Return original if parsing fails
        }
    }
    
    /**
     * GET PORTFOLIOS BY INVESTMENT PLAN
     * ---------------------------------
     * Finds all portfolios created from a specific investment plan.
     */
    @Transactional(readOnly = true)
    public List<Portfolio> getPortfoliosByPlan(Long planId) {
        return portfolioRepository.findByInvestmentPlanPlanId(planId);
    }
    
    // ========================================
    // READ OPERATIONS
    // ========================================
    
    /**
     * GET PORTFOLIO BY ID
     * -------------------
     */
    @Transactional(readOnly = true)
    public Optional<Portfolio> getPortfolioById(Long portfolioId) {
        return portfolioRepository.findById(Objects.requireNonNull(portfolioId));
    }
    
    /**
     * GET PORTFOLIO BY ID WITH REPORTS
     * --------------------------------
     * Eagerly fetches the portfolio with its reports.
     */
    @Transactional(readOnly = true)
    public Optional<Portfolio> getPortfolioByIdWithReports(Long portfolioId) {
        return portfolioRepository.findByIdWithReports(portfolioId);
    }
    
    /**
     * GET ALL PORTFOLIOS FOR CLIENT
     * -----------------------------
     */
    @Transactional(readOnly = true)
    public List<Portfolio> getClientPortfolios(Long clientId) {
        return portfolioRepository.findByClientClientId(clientId);
    }
    
    /**
     * GET ALL PORTFOLIOS
     * ------------------
     */
    @Transactional(readOnly = true)
    public List<Portfolio> getAllPortfolios() {
        return portfolioRepository.findAll();
    }
    
    /**
     * GET TOP PERFORMING PORTFOLIOS
     * -----------------------------
     */
    @Transactional(readOnly = true)
    public List<Portfolio> getTopPerformingPortfolios() {
        return portfolioRepository.findTopPerformingPortfolios();
    }
    
    // ========================================
    // UPDATE OPERATIONS
    // ========================================
    
    /**
     * UPDATE PORTFOLIO
     * ----------------
     */
    public Portfolio updatePortfolio(Long portfolioId, Portfolio updatedPortfolio) {
        Portfolio existing = portfolioRepository.findById(Objects.requireNonNull(portfolioId))
                .orElseThrow(() -> new RuntimeException("Portfolio not found with ID: " + portfolioId));
        
        existing.setPortfolioName(updatedPortfolio.getPortfolioName());
        existing.setTotalValue(updatedPortfolio.getTotalValue());
        existing.setAllocationSummary(updatedPortfolio.getAllocationSummary());
        existing.setLastUpdated(LocalDate.now());
        
        return portfolioRepository.save(existing);
    }
    
    /**
     * UPDATE PORTFOLIO VALUE
     * ----------------------
     * Updates only the value (common operation).
     */
    public Portfolio updatePortfolioValue(Long portfolioId, BigDecimal newValue) {
        Portfolio portfolio = portfolioRepository.findById(Objects.requireNonNull(portfolioId))
                .orElseThrow(() -> new RuntimeException("Portfolio not found with ID: " + portfolioId));
        
        portfolio.setTotalValue(newValue);
        portfolio.setLastUpdated(LocalDate.now());
        
        return portfolioRepository.save(portfolio);
    }
    
    /**
     * REBALANCE PORTFOLIO
     * -------------------
     * Updates allocation summary and marks as updated.
     */
    public Portfolio rebalancePortfolio(Long portfolioId, String newAllocation) {
        Portfolio portfolio = portfolioRepository.findById(Objects.requireNonNull(portfolioId))
                .orElseThrow(() -> new RuntimeException("Portfolio not found with ID: " + portfolioId));
        
        portfolio.setAllocationSummary(newAllocation);
        portfolio.setLastUpdated(LocalDate.now());
        
        return portfolioRepository.save(portfolio);
    }
    
    // ========================================
    // DELETE OPERATIONS
    // ========================================
    
    /**
     * DELETE PORTFOLIO
     * ----------------
     */
    public void deletePortfolio(Long portfolioId) {
        if (!portfolioRepository.existsById(Objects.requireNonNull(portfolioId))) {
            throw new RuntimeException("Portfolio not found with ID: " + portfolioId);
        }
        portfolioRepository.deleteById(portfolioId);
    }
    
    // ========================================
    // ANALYSIS & CALCULATIONS
    // ========================================
    
    /**
     * CALCULATE PORTFOLIO RETURN
     * --------------------------
     * Returns the percentage return for a portfolio.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculatePortfolioReturn(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(Objects.requireNonNull(portfolioId))
                .orElseThrow(() -> new RuntimeException("Portfolio not found with ID: " + portfolioId));
        
        return portfolio.calculateReturnPercentage();
    }
    
    /**
     * CALCULATE CLIENT TOTAL VALUE
     * ----------------------------
     * Sums up all portfolio values for a client.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateClientTotalValue(Long clientId) {
        BigDecimal total = portfolioRepository.calculateClientTotalValue(clientId);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * CALCULATE TOTAL AUM
     * -------------------
     * Assets Under Management - total value of all portfolios.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalAUM() {
        BigDecimal total = portfolioRepository.calculateTotalAUM();
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * GET PORTFOLIOS NEEDING REVIEW
     * -----------------------------
     * Returns portfolios not updated in the last N days.
     */
    @Transactional(readOnly = true)
    public List<Portfolio> getPortfoliosNeedingReview(int daysOld) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysOld);
        return portfolioRepository.findPortfoliosNeedingReview(cutoffDate);
    }
    
    /**
     * GET HIGH VALUE PORTFOLIOS
     * -------------------------
     */
    @Transactional(readOnly = true)
    public List<Portfolio> getHighValuePortfolios(BigDecimal threshold) {
        return portfolioRepository.findByTotalValueGreaterThan(threshold);
    }
    
    /**
     * GET PORTFOLIO PERFORMANCE SUMMARY
     * ---------------------------------
     * Returns a summary of portfolio performance.
     */
    @Transactional(readOnly = true)
    public String getPortfolioPerformanceSummary(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(Objects.requireNonNull(portfolioId))
                .orElseThrow(() -> new RuntimeException("Portfolio not found with ID: " + portfolioId));
        
        BigDecimal returnPct = portfolio.calculateReturnPercentage();
        BigDecimal profitLoss = portfolio.calculateProfitLoss();
        BigDecimal initialInvestment = portfolio.getInitialInvestment() != null ? portfolio.getInitialInvestment() : BigDecimal.ZERO;
        
        String performanceStatus;
        if (returnPct.compareTo(BigDecimal.ZERO) > 0) {
            performanceStatus = "positive growth";
        } else if (returnPct.compareTo(BigDecimal.ZERO) < 0) {
            performanceStatus = "decline";
        } else {
            performanceStatus = "no change";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Portfolio Performance Summary\n");
        summary.append("═══════════════════════════════════════\n\n");
        summary.append(String.format("Portfolio Name: %s\n\n", portfolio.getPortfolioName()));
        summary.append("Financial Overview:\n");
        summary.append(String.format("  • Initial Investment:  ₹%,.2f\n", initialInvestment));
        summary.append(String.format("  • Current Value:       ₹%,.2f\n", portfolio.getTotalValue()));
        summary.append(String.format("  • Profit/Loss:         %s₹%,.2f\n", 
            profitLoss.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "-", 
            profitLoss.abs()));
        summary.append(String.format("  • Return Rate:         %s%.2f%%\n\n", 
            returnPct.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "", 
            returnPct));
        summary.append(String.format("Status: This portfolio has shown %s since inception.\n", performanceStatus));
        summary.append(String.format("\nLast Updated: %s", 
            portfolio.getLastUpdated() != null ? portfolio.getLastUpdated().toString() : "N/A"));
        
        return summary.toString();
    }
}
