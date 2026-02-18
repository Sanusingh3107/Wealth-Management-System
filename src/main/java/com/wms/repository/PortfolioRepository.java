package com.wms.repository;

import com.wms.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ========================================
 * PORTFOLIO REPOSITORY
 * ========================================
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    /**
     * FIND PORTFOLIOS BY CLIENT
     * -------------------------
     */
    List<Portfolio> findByClientClientId(Long clientId);
    
    /**
     * FIND BY PORTFOLIO NAME
     * ----------------------
     */
    Optional<Portfolio> findByPortfolioNameAndClientClientId(String portfolioName, Long clientId);
    
    /**
     * FIND PORTFOLIOS BY VALUE RANGE
     * ------------------------------
     * 'Between' = WHERE column BETWEEN value1 AND value2
     */
    List<Portfolio> findByTotalValueBetween(BigDecimal minValue, BigDecimal maxValue);
    
    /**
     * FIND HIGH VALUE PORTFOLIOS
     * --------------------------
     * 'GreaterThan' = WHERE column > value
     */
    List<Portfolio> findByTotalValueGreaterThan(BigDecimal value);
    
    /**
     * FIND PORTFOLIOS NOT UPDATED RECENTLY
     * ------------------------------------
     * 'Before' = WHERE date < value
     * Useful for finding portfolios that might need attention.
     */
    List<Portfolio> findByLastUpdatedBefore(LocalDate date);
    
    /**
     * FIND PORTFOLIOS UPDATED AFTER A DATE
     * ------------------------------------
     */
    List<Portfolio> findByLastUpdatedAfter(LocalDate date);
    
    /**
     * COUNT PORTFOLIOS BY CLIENT
     * --------------------------
     */
    long countByClientClientId(Long clientId);
    
    /**
     * CALCULATE TOTAL ASSETS UNDER MANAGEMENT (AUM)
     * ---------------------------------------------
     * Sums up the total value of ALL portfolios.
     * This is a key metric for wealth management firms.
     */
    @Query("SELECT SUM(p.totalValue) FROM Portfolio p")
    BigDecimal calculateTotalAUM();
    
    /**
     * CALCULATE CLIENT'S TOTAL PORTFOLIO VALUE
     * ----------------------------------------
     */
    @Query("SELECT SUM(p.totalValue) FROM Portfolio p WHERE p.client.clientId = :clientId")
    BigDecimal calculateClientTotalValue(@Param("clientId") Long clientId);
    
    /**
     * FIND TOP PERFORMING PORTFOLIOS
     * ------------------------------
     * Based on current value vs initial investment.
     */
    @Query("SELECT p FROM Portfolio p WHERE p.totalValue > p.initialInvestment ORDER BY (p.totalValue - p.initialInvestment) DESC")
    List<Portfolio> findTopPerformingPortfolios();
    
    /**
     * FIND PORTFOLIOS NEEDING REVIEW
     * ------------------------------
     * Portfolios not updated in the last N days.
     */
    @Query("SELECT p FROM Portfolio p WHERE p.lastUpdated < :cutoffDate ORDER BY p.lastUpdated ASC")
    List<Portfolio> findPortfoliosNeedingReview(@Param("cutoffDate") LocalDate cutoffDate);
    
    /**
     * CHECK IF PORTFOLIO EXISTS FOR CLIENT
     * ------------------------------------
     */
    boolean existsByClientClientIdAndPortfolioName(Long clientId, String portfolioName);
    
    /**
     * FIND PORTFOLIO BY ID WITH REPORTS
     * ----------------------------------
     * Eagerly fetches the portfolio with its client and reports to avoid LazyInitializationException.
     */
    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.client LEFT JOIN FETCH p.reports WHERE p.portfolioId = :portfolioId")
    Optional<Portfolio> findByIdWithReports(@Param("portfolioId") Long portfolioId);
    
    /**
     * FIND ALL ORDERED BY VALUE
     * -------------------------
     */
    List<Portfolio> findAllByOrderByTotalValueDesc();
    
    /**
     * FIND PORTFOLIOS BY INVESTMENT PLAN
     * ----------------------------------
     * Gets all portfolios created from a specific investment plan.
     * Useful for tracking plan execution and performance.
     */
    List<Portfolio> findByInvestmentPlanPlanId(Long planId);
    
    /**
     * CHECK IF PORTFOLIO EXISTS FOR INVESTMENT PLAN
     * ---------------------------------------------
     * Prevents duplicate portfolio creation from the same plan.
     */
    boolean existsByInvestmentPlanPlanId(Long planId);
}
