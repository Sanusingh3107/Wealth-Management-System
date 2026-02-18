package com.wms.repository;

import com.wms.entity.InvestmentPlan;
import com.wms.entity.InvestmentPlan.PlanStatus;
import com.wms.entity.InvestmentPlan.RiskAppetite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ========================================
 * INVESTMENT PLAN REPOSITORY
 * ========================================
 * 
 */
@Repository
public interface InvestmentPlanRepository extends JpaRepository<InvestmentPlan, Long> {
    
    /**
     * FIND PLANS BY CLIENT ID
     * -----------------------
     */
    List<InvestmentPlan> findByClientClientId(Long clientId);
    
    /**
     * FIND PLANS BY STATUS
     * --------------------
     */
    List<InvestmentPlan> findByStatus(PlanStatus status);
    
    /**
     * FIND PLANS BY RISK APPETITE
     * ---------------------------
     */
    List<InvestmentPlan> findByRiskAppetite(RiskAppetite riskAppetite);
    
    /**
     * FIND ACTIVE PLANS FOR A CLIENT
     * ------------------------------
     * Combining multiple conditions.
     * 'And' = multiple WHERE conditions joined with AND
     */
    List<InvestmentPlan> findByClientClientIdAndStatus(Long clientId, PlanStatus status);
    
    /**
     * COUNT PLANS BY STATUS
     * ---------------------
     */
    long countByStatus(PlanStatus status);
    
    /**
     * COUNT PLANS BY RISK APPETITE
     * ----------------------------
     */
    long countByRiskAppetite(RiskAppetite riskAppetite);
    
    /**
     * FIND PLANS WITH HIGH RISK AND ACTIVE STATUS
     * -------------------------------------------
     * Custom JPQL query for complex filtering.
     */
    @Query("SELECT ip FROM InvestmentPlan ip WHERE ip.riskAppetite = :risk AND ip.status = :status")
    List<InvestmentPlan> findByRiskAndStatus(@Param("risk") RiskAppetite risk, @Param("status") PlanStatus status);
    
    /**
     * GET PLAN SUMMARY BY CLIENT
     * --------------------------
     * Custom query to get summary statistics.
     */
    @Query("SELECT COUNT(ip) FROM InvestmentPlan ip WHERE ip.client.clientId = :clientId")
    long countPlansByClientId(@Param("clientId") Long clientId);
    
    /**
     * FIND BY INVESTMENT OBJECTIVE CONTAINING
     * ---------------------------------------
     * Search plans by their objective description.
     */
    List<InvestmentPlan> findByInvestmentObjectiveContainingIgnoreCase(String keyword);
    
    /**
     * CHECK IF CLIENT HAS ANY PLANS
     * -----------------------------
     */
    boolean existsByClientClientId(Long clientId);
}
