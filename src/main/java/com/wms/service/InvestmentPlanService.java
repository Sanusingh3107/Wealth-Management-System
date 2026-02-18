package com.wms.service;

import com.wms.entity.Client;
import com.wms.entity.InvestmentPlan;
import com.wms.entity.InvestmentPlan.PlanStatus;
import com.wms.entity.InvestmentPlan.RiskAppetite;
import com.wms.repository.ClientRepository;
import com.wms.repository.InvestmentPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ========================================
 * INVESTMENT PLAN SERVICE
 * ========================================
 * 
 */
@Service
@Transactional
public class InvestmentPlanService {
    
    private final InvestmentPlanRepository planRepository;
    private final ClientRepository clientRepository;
    
    public InvestmentPlanService(InvestmentPlanRepository planRepository, 
                                  ClientRepository clientRepository) {
        this.planRepository = planRepository;
        this.clientRepository = clientRepository;
    }
    
    // ========================================
    // CREATE OPERATIONS
    // ========================================
    
    /**
     * CREATE INVESTMENT PLAN
     * ----------------------
     * Creates a new investment plan for a client.
     */
    public InvestmentPlan createPlan(Long clientId, InvestmentPlan plan) {
        // Find the client
        Client client = clientRepository.findById(Objects.requireNonNull(clientId))
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        
        // Set the client
        plan.setClient(client);
        
        // Set default status if not provided
        if (plan.getStatus() == null) {
            plan.setStatus(PlanStatus.ACTIVE);
        }
        
        // Generate default allocation if not provided
        if (plan.getAllocationDetails() == null || plan.getAllocationDetails().isEmpty()) {
            plan.setAllocationDetails(generateDefaultAllocation(plan.getRiskAppetite()));
        }
        
        return planRepository.save(plan);
    }
    
    /**
     * GENERATE DEFAULT ALLOCATION
     * ---------------------------
     * Creates a suggested allocation based on risk appetite.
     */
    private String generateDefaultAllocation(RiskAppetite riskAppetite) {
        switch (riskAppetite) {
            case LOW:
                return "{\"stocks\": 20, \"bonds\": 60, \"cash\": 15, \"alternatives\": 5}";
            case MEDIUM:
                return "{\"stocks\": 50, \"bonds\": 35, \"cash\": 10, \"alternatives\": 5}";
            case HIGH:
                return "{\"stocks\": 75, \"bonds\": 15, \"cash\": 5, \"alternatives\": 5}";
            default:
                return "{\"stocks\": 50, \"bonds\": 35, \"cash\": 10, \"alternatives\": 5}";
        }
    }
    
    // ========================================
    // READ OPERATIONS
    // ========================================
    
    /**
     * GET PLAN BY ID
     * --------------
     */
    @Transactional(readOnly = true)
    public Optional<InvestmentPlan> getPlanById(Long planId) {
        return planRepository.findById(Objects.requireNonNull(planId));
    }
    
    /**
     * GET ALL PLANS FOR CLIENT
     * ------------------------
     */
    @Transactional(readOnly = true)
    public List<InvestmentPlan> getClientPlans(Long clientId) {
        return planRepository.findByClientClientId(clientId);
    }
    
    /**
     * GET ACTIVE PLANS FOR CLIENT
     * ---------------------------
     */
    @Transactional(readOnly = true)
    public List<InvestmentPlan> getActiveClientPlans(Long clientId) {
        return planRepository.findByClientClientIdAndStatus(clientId, PlanStatus.ACTIVE);
    }
    
    /**
     * GET PLANS BY RISK APPETITE
     * --------------------------
     */
    @Transactional(readOnly = true)
    public List<InvestmentPlan> getPlansByRiskAppetite(RiskAppetite riskAppetite) {
        return planRepository.findByRiskAppetite(riskAppetite);
    }
    
    /**
     * GET ALL PLANS
     * -------------
     */
    @Transactional(readOnly = true)
    public List<InvestmentPlan> getAllPlans() {
        return planRepository.findAll();
    }
    
    // ========================================
    // UPDATE OPERATIONS
    // ========================================
    
    /**
     * UPDATE PLAN
     * -----------
     */
    public InvestmentPlan updatePlan(Long planId, InvestmentPlan updatedPlan) {
        InvestmentPlan existingPlan = planRepository.findById(Objects.requireNonNull(planId))
                .orElseThrow(() -> new RuntimeException("Investment plan not found with ID: " + planId));
        
        // Update fields
        existingPlan.setInvestmentObjective(updatedPlan.getInvestmentObjective());
        existingPlan.setRiskAppetite(updatedPlan.getRiskAppetite());
        existingPlan.setAllocationDetails(updatedPlan.getAllocationDetails());
        existingPlan.setTargetAmount(updatedPlan.getTargetAmount());
        existingPlan.setDurationYears(updatedPlan.getDurationYears());
        
        return planRepository.save(existingPlan);
    }
    
    /**
     * UPDATE PLAN STATUS
     * ------------------
     */
    public InvestmentPlan updatePlanStatus(Long planId, PlanStatus newStatus) {
        InvestmentPlan plan = planRepository.findById(Objects.requireNonNull(planId))
                .orElseThrow(() -> new RuntimeException("Investment plan not found with ID: " + planId));
        
        plan.setStatus(newStatus);
        return planRepository.save(plan);
    }
    
    /**
     * PAUSE PLAN
     * ----------
     */
    public InvestmentPlan pausePlan(Long planId) {
        return updatePlanStatus(planId, PlanStatus.PAUSED);
    }
    
    /**
     * RESUME PLAN
     * -----------
     */
    public InvestmentPlan resumePlan(Long planId) {
        return updatePlanStatus(planId, PlanStatus.ACTIVE);
    }
    
    /**
     * COMPLETE PLAN
     * -------------
     */
    public InvestmentPlan completePlan(Long planId) {
        return updatePlanStatus(planId, PlanStatus.COMPLETED);
    }
    
    // ========================================
    // DELETE OPERATIONS
    // ========================================
    
    /**
     * DELETE PLAN
     * -----------
     */
    public void deletePlan(Long planId) {
        if (!planRepository.existsById(Objects.requireNonNull(planId))) {
            throw new RuntimeException("Investment plan not found with ID: " + planId);
        }
        planRepository.deleteById(planId);
    }
    
    // ========================================
    // STATISTICS
    // ========================================
    
    /**
     * COUNT PLANS BY STATUS
     * ---------------------
     */
    @Transactional(readOnly = true)
    public long countPlansByStatus(PlanStatus status) {
        return planRepository.countByStatus(status);
    }
    
    /**
     * COUNT PLANS BY RISK
     * -------------------
     */
    @Transactional(readOnly = true)
    public long countPlansByRiskAppetite(RiskAppetite riskAppetite) {
        return planRepository.countByRiskAppetite(riskAppetite);
    }
}
