package com.wms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ========================================
 * INVESTMENT PLAN ENTITY
 * ======================================== 
 * This entity has a MANY-TO-ONE relationship with Client, meaning:
 * - One client can have MANY investment plans
 * - Each investment plan belongs to ONE client"
 */
@Entity
@Table(name = "investment_plan")
public class InvestmentPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;
    
    /**
     * FOREIGN KEY RELATIONSHIP
     * ------------------------
     * @ManyToOne: Many investment plans can belong to one client
     * @JoinColumn: Specifies the foreign key column in this table
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    /**
     * INVESTMENT OBJECTIVE
     * --------------------
     * What the client wants to achieve with this investment.
     * Examples: Retirement Planning, Child Education, Wealth Growth
     */
    @NotBlank(message = "Investment objective is required")
    @Size(max = 200, message = "Investment objective must be less than 200 characters")
    @Column(nullable = false, length = 200)
    private String investmentObjective;
    
    /**
     * RISK APPETITE
     * -------------
     * How much risk the client is willing to take.
     * Using @Enumerated to map Java enum to database column.
     */
    @NotNull(message = "Risk appetite is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskAppetite riskAppetite;
    
    /**
     * ALLOCATION DETAILS
     * ------------------
     * JSON field storing how money is allocated across different asset classes.
     * Example: {"stocks": 60, "bonds": 30, "cash": 10}
     */
    @Column(columnDefinition = "TEXT")
    private String allocationDetails;
    
    /**
     * TARGET AMOUNT
     * -------------
     * The financial goal the client wants to achieve.
     * BigDecimal is used for precise monetary calculations.
     */
    @DecimalMin(value = "1000.00", message = "Target amount must be at least 1000")
    @Column(precision = 15, scale = 2)
    private BigDecimal targetAmount;
    
    /**
     * INVESTMENT DURATION (in years)
     * ------------------------------
     * How long the client plans to invest.
     */
    @Min(value = 1, message = "Duration must be at least 1 year")
    @Max(value = 50, message = "Duration cannot exceed 50 years")
    private Integer durationYears;
    
    /**
     * PLAN STATUS
     * -----------
     * Active, Paused, or Completed
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus status = PlanStatus.ACTIVE;
    
    /**
     * LINKED PORTFOLIOS
     * -----------------
     * Portfolios created based on this investment plan.
     * One investment plan can have multiple portfolios (e.g., different phases).

     * 1. From Plan → See all portfolios following this strategy
     * 2. From Portfolio → See what plan it's based on"
     */
    @OneToMany(mappedBy = "investmentPlan", cascade = CascadeType.ALL)
    private java.util.List<Portfolio> portfolios = new java.util.ArrayList<>();
    
    /**
     * AUDIT TIMESTAMPS
     * ----------------
     * Track when the plan was created and last updated.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ========================================
    // ENUMS
    // ========================================
    
    /**
     * RISK APPETITE ENUM
     * ------------------
     * Defines the possible values for risk tolerance.
     */
    public enum RiskAppetite {
        LOW,      // Conservative - prefers safe investments
        MEDIUM,   // Balanced - mix of safe and risky
        HIGH      // Aggressive - willing to take high risks for high returns
    }
    
    /**
     * PLAN STATUS ENUM
     * ----------------
     */
    public enum PlanStatus {
        ACTIVE,     // Plan is currently running
        PAUSED,     // Temporarily stopped
        COMPLETED   // Investment goal achieved
    }
    
    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    public InvestmentPlan() {
    }
    
    public InvestmentPlan(Client client, String investmentObjective, RiskAppetite riskAppetite) {
        this.client = client;
        this.investmentObjective = investmentObjective;
        this.riskAppetite = riskAppetite;
        this.status = PlanStatus.ACTIVE;
    }
    
    // ========================================
    // GETTERS AND SETTERS
    // ========================================
    
    public Long getPlanId() {
        return planId;
    }
    
    public void setPlanId(Long planId) {
        this.planId = planId;
    }
    
    public Client getClient() {
        return client;
    }
    
    public void setClient(Client client) {
        this.client = client;
    }
    
    public String getInvestmentObjective() {
        return investmentObjective;
    }
    
    public void setInvestmentObjective(String investmentObjective) {
        this.investmentObjective = investmentObjective;
    }
    
    public RiskAppetite getRiskAppetite() {
        return riskAppetite;
    }
    
    public void setRiskAppetite(RiskAppetite riskAppetite) {
        this.riskAppetite = riskAppetite;
    }
    
    public String getAllocationDetails() {
        return allocationDetails;
    }
    
    public void setAllocationDetails(String allocationDetails) {
        this.allocationDetails = allocationDetails;
    }
    
    public BigDecimal getTargetAmount() {
        return targetAmount;
    }
    
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }
    
    public Integer getDurationYears() {
        return durationYears;
    }
    
    public void setDurationYears(Integer durationYears) {
        this.durationYears = durationYears;
    }
    
    public PlanStatus getStatus() {
        return status;
    }
    
    public void setStatus(PlanStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public java.util.List<Portfolio> getPortfolios() {
        return portfolios;
    }
    
    public void setPortfolios(java.util.List<Portfolio> portfolios) {
        this.portfolios = portfolios;
    }
    
    /**
     * HELPER METHOD: Check if plan has any portfolios
     */
    public boolean hasPortfolios() {
        return portfolios != null && !portfolios.isEmpty();
    }
    
    @Override
    public String toString() {
        return "InvestmentPlan{" +
                "planId=" + planId +
                ", investmentObjective='" + investmentObjective + '\'' +
                ", riskAppetite=" + riskAppetite +
                ", status=" + status +
                '}';
    }
}
