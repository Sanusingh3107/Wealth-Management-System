package com.wms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ========================================
 * PORTFOLIO ENTITY
 * ========================================
 */
@Entity
@Table(name = "portfolio")
public class Portfolio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioId;
    
    /**
     * RELATIONSHIP TO CLIENT
     * ----------------------
     * Each portfolio belongs to one client.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    /**
     * PORTFOLIO NAME
     * --------------
     * A friendly name for the portfolio (e.g., "Retirement Fund", "Education Savings")
     */
    @NotBlank(message = "Portfolio name is required")
    @Size(max = 100, message = "Portfolio name must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String portfolioName;
    
    /**
     * TOTAL VALUE
     * -----------
     */
    @NotNull(message = "Total value is required")
    @DecimalMin(value = "0.00", message = "Total value cannot be negative")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalValue;
    
    /**
     * ALLOCATION SUMMARY
     * ------------------
     * JSON representation of how money is distributed.
     * Example: {"STOCKS": 50000, "BONDS": 30000, "REAL_ESTATE": 20000}
     */
    @Column(columnDefinition = "TEXT")
    private String allocationSummary;
    
    /**
     * LAST UPDATED DATE
     * -----------------
     * When the portfolio was last reviewed or modified.
     */
    @NotNull(message = "Last updated date is required")
    @Column(nullable = false)
    private LocalDate lastUpdated;
    
    /**
     * INITIAL INVESTMENT
     * ------------------
     * The amount initially invested (for calculating returns)
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal initialInvestment;
    
    /**
     * LINKED INVESTMENT PLAN
     * ----------------------
     * The investment plan that this portfolio was created from.
     * This creates a direct relationship between strategy (plan) and reality (portfolio).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investment_plan_id")
    private InvestmentPlan investmentPlan;
    
    /**
     * REPORTS RELATIONSHIP
     * --------------------
     * One portfolio can have many reports generated over time.
     */
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new java.util.ArrayList<>();
    
    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    public Portfolio() {
        this.lastUpdated = LocalDate.now();
    }
    
    public Portfolio(Client client, String portfolioName, BigDecimal totalValue) {
        this.client = client;
        this.portfolioName = portfolioName;
        this.totalValue = totalValue;
        this.initialInvestment = totalValue;
        this.lastUpdated = LocalDate.now();
    }
    
    // ========================================
    // BUSINESS METHODS
    // ========================================
    
    /**
     * CALCULATE RETURN PERCENTAGE
     * ---------------------------
     * Calculates the percentage return on investment.
     * Formula: ((Current Value - Initial Investment) / Initial Investment) * 100
     */
    public BigDecimal calculateReturnPercentage() {
        if (initialInvestment == null || initialInvestment.compareTo(BigDecimal.ZERO) == 0 || totalValue == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal profit = totalValue.subtract(initialInvestment);
        return profit.multiply(new BigDecimal("100"))
                     .divide(initialInvestment, 2, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * CALCULATE PROFIT/LOSS
     * ---------------------
     */
    public BigDecimal calculateProfitLoss() {
        if (initialInvestment == null || totalValue == null) {
            return BigDecimal.ZERO;
        }
        return totalValue.subtract(initialInvestment);
    }
    
    // ========================================
    // GETTERS AND SETTERS
    // ========================================
    
    public Long getPortfolioId() {
        return portfolioId;
    }
    
    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }
    
    public Client getClient() {
        return client;
    }
    
    public void setClient(Client client) {
        this.client = client;
    }
    
    public String getPortfolioName() {
        return portfolioName;
    }
    
    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }
    
    public BigDecimal getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
        this.lastUpdated = LocalDate.now();
    }
    
    public String getAllocationSummary() {
        return allocationSummary;
    }
    
    public void setAllocationSummary(String allocationSummary) {
        this.allocationSummary = allocationSummary;
    }
    
    public LocalDate getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public BigDecimal getInitialInvestment() {
        return initialInvestment;
    }
    
    public void setInitialInvestment(BigDecimal initialInvestment) {
        this.initialInvestment = initialInvestment;
    }
    
    public List<Report> getReports() {
        return reports;
    }
    
    public void setReports(List<Report> reports) {
        this.reports = reports;
    }
    
    public InvestmentPlan getInvestmentPlan() {
        return investmentPlan;
    }
    
    public void setInvestmentPlan(InvestmentPlan investmentPlan) {
        this.investmentPlan = investmentPlan;
    }
    
    @Override
    public String toString() {
        return "Portfolio{" +
                "portfolioId=" + portfolioId +
                ", portfolioName='" + portfolioName + '\'' +
                ", totalValue=" + totalValue +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
