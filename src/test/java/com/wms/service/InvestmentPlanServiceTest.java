package com.wms.service;

import com.wms.entity.Client;
import com.wms.entity.InvestmentPlan;
import com.wms.entity.InvestmentPlan.PlanStatus;
import com.wms.entity.InvestmentPlan.RiskAppetite;
import com.wms.repository.ClientRepository;
import com.wms.repository.InvestmentPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InvestmentPlanService
 * Achieves 100% code coverage for all InvestmentPlanService methods
 */
@ExtendWith(MockitoExtension.class)
class InvestmentPlanServiceTest {

    @Mock
    private InvestmentPlanRepository planRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private InvestmentPlanService investmentPlanService;

    private Client testClient;
    private InvestmentPlan testPlan;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setClientId(1L);
        testClient.setName("John Doe");
        testClient.setEmail("john.doe@example.com");

        testPlan = new InvestmentPlan();
        testPlan.setPlanId(1L);
        testPlan.setClient(testClient);
        testPlan.setInvestmentObjective("Retirement Planning");
        testPlan.setRiskAppetite(RiskAppetite.MEDIUM);
        testPlan.setTargetAmount(new BigDecimal("500000.00"));
        testPlan.setDurationYears(20);
        testPlan.setStatus(PlanStatus.ACTIVE);
        testPlan.setAllocationDetails("{\"stocks\": 50, \"bonds\": 35, \"cash\": 10, \"alternatives\": 5}");
    }

    // ========================================
    // CREATE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Create Plan Tests")
    class CreatePlanTests {

        @Test
        @DisplayName("Should create plan successfully")
        void createPlan_Success() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(planRepository.save(any(InvestmentPlan.class))).thenReturn(testPlan);

            InvestmentPlan result = investmentPlanService.createPlan(1L, testPlan);

            assertNotNull(result);
            verify(planRepository).save(any(InvestmentPlan.class));
        }

        @Test
        @DisplayName("Should set default status to ACTIVE when not provided")
        void createPlan_SetsDefaultStatus() {
            InvestmentPlan newPlan = new InvestmentPlan();
            newPlan.setInvestmentObjective("New Objective");
            newPlan.setRiskAppetite(RiskAppetite.LOW);
            newPlan.setAllocationDetails("{\"stocks\": 20}");
            // Status not set

            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(planRepository.save(any(InvestmentPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

            InvestmentPlan result = investmentPlanService.createPlan(1L, newPlan);

            assertEquals(PlanStatus.ACTIVE, result.getStatus());
        }

        @Test
        @DisplayName("Should generate default allocation for LOW risk appetite")
        void createPlan_GeneratesDefaultAllocation_LowRisk() {
            InvestmentPlan newPlan = new InvestmentPlan();
            newPlan.setInvestmentObjective("Conservative Investment");
            newPlan.setRiskAppetite(RiskAppetite.LOW);
            // No allocation provided

            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(planRepository.save(any(InvestmentPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

            InvestmentPlan result = investmentPlanService.createPlan(1L, newPlan);

            assertNotNull(result.getAllocationDetails());
            assertTrue(result.getAllocationDetails().contains("\"stocks\": 20"));
        }

        @Test
        @DisplayName("Should generate default allocation for MEDIUM risk appetite")
        void createPlan_GeneratesDefaultAllocation_MediumRisk() {
            InvestmentPlan newPlan = new InvestmentPlan();
            newPlan.setInvestmentObjective("Balanced Investment");
            newPlan.setRiskAppetite(RiskAppetite.MEDIUM);

            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(planRepository.save(any(InvestmentPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

            InvestmentPlan result = investmentPlanService.createPlan(1L, newPlan);

            assertTrue(result.getAllocationDetails().contains("\"stocks\": 50"));
        }

        @Test
        @DisplayName("Should generate default allocation for HIGH risk appetite")
        void createPlan_GeneratesDefaultAllocation_HighRisk() {
            InvestmentPlan newPlan = new InvestmentPlan();
            newPlan.setInvestmentObjective("Aggressive Investment");
            newPlan.setRiskAppetite(RiskAppetite.HIGH);

            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(planRepository.save(any(InvestmentPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

            InvestmentPlan result = investmentPlanService.createPlan(1L, newPlan);

            assertTrue(result.getAllocationDetails().contains("\"stocks\": 75"));
        }

        @Test
        @DisplayName("Should generate default allocation when allocationDetails is empty string")
        void createPlan_GeneratesDefaultAllocation_EmptyString() {
            InvestmentPlan newPlan = new InvestmentPlan();
            newPlan.setInvestmentObjective("New Investment");
            newPlan.setRiskAppetite(RiskAppetite.LOW);
            newPlan.setAllocationDetails("");

            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(planRepository.save(any(InvestmentPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

            InvestmentPlan result = investmentPlanService.createPlan(1L, newPlan);

            assertNotNull(result.getAllocationDetails());
            assertTrue(result.getAllocationDetails().contains("\"stocks\": 20"));
        }

        @Test
        @DisplayName("Should throw exception when client not found")
        void createPlan_ClientNotFound_ThrowsException() {
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> investmentPlanService.createPlan(999L, testPlan));

            assertEquals("Client not found with ID: 999", exception.getMessage());
        }
    }

    // ========================================
    // READ OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Read Plan Tests")
    class ReadPlanTests {

        @Test
        @DisplayName("Should return plan by ID when exists")
        void getPlanById_Found() {
            when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));

            Optional<InvestmentPlan> result = investmentPlanService.getPlanById(1L);

            assertTrue(result.isPresent());
            assertEquals("Retirement Planning", result.get().getInvestmentObjective());
        }

        @Test
        @DisplayName("Should return empty when plan not found")
        void getPlanById_NotFound() {
            when(planRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<InvestmentPlan> result = investmentPlanService.getPlanById(999L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return all plans for client")
        void getClientPlans_ReturnsList() {
            when(planRepository.findByClientClientId(1L)).thenReturn(Arrays.asList(testPlan));

            List<InvestmentPlan> result = investmentPlanService.getClientPlans(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return active plans for client")
        void getActiveClientPlans_ReturnsList() {
            when(planRepository.findByClientClientIdAndStatus(1L, PlanStatus.ACTIVE))
                .thenReturn(Arrays.asList(testPlan));

            List<InvestmentPlan> result = investmentPlanService.getActiveClientPlans(1L);

            assertEquals(1, result.size());
            assertEquals(PlanStatus.ACTIVE, result.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return plans by risk appetite")
        void getPlansByRiskAppetite_ReturnsList() {
            when(planRepository.findByRiskAppetite(RiskAppetite.MEDIUM)).thenReturn(Arrays.asList(testPlan));

            List<InvestmentPlan> result = investmentPlanService.getPlansByRiskAppetite(RiskAppetite.MEDIUM);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return all plans")
        void getAllPlans_ReturnsList() {
            when(planRepository.findAll()).thenReturn(Arrays.asList(testPlan));

            List<InvestmentPlan> result = investmentPlanService.getAllPlans();

            assertEquals(1, result.size());
        }
    }

    // ========================================
    // UPDATE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Update Plan Tests")
    class UpdatePlanTests {

        @Test
        @DisplayName("Should update plan successfully")
        void updatePlan_Success() {
            InvestmentPlan updatedPlan = new InvestmentPlan();
            updatedPlan.setInvestmentObjective("Updated Objective");
            updatedPlan.setRiskAppetite(RiskAppetite.HIGH);
            updatedPlan.setAllocationDetails("{\"stocks\": 70}");
            updatedPlan.setTargetAmount(new BigDecimal("600000.00"));
            updatedPlan.setDurationYears(25);

            when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(planRepository.save(any(InvestmentPlan.class))).thenReturn(testPlan);

            InvestmentPlan result = investmentPlanService.updatePlan(1L, updatedPlan);

            assertNotNull(result);
            verify(planRepository).save(any(InvestmentPlan.class));
        }

        @Test
        @DisplayName("Should throw exception when plan not found for update")
        void updatePlan_NotFound_ThrowsException() {
            when(planRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> investmentPlanService.updatePlan(999L, testPlan));

            assertEquals("Investment plan not found with ID: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should update plan status successfully")
        void updatePlanStatus_Success() {
            when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(planRepository.save(any(InvestmentPlan.class))).thenReturn(testPlan);

            InvestmentPlan result = investmentPlanService.updatePlanStatus(1L, PlanStatus.PAUSED);

            assertNotNull(result);
            verify(planRepository).save(any(InvestmentPlan.class));
        }

        @Test
        @DisplayName("Should throw exception when plan not found for status update")
        void updatePlanStatus_NotFound_ThrowsException() {
            when(planRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> investmentPlanService.updatePlanStatus(999L, PlanStatus.PAUSED));

            assertEquals("Investment plan not found with ID: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should pause plan successfully")
        void pausePlan_Success() {
            when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(planRepository.save(any(InvestmentPlan.class))).thenReturn(testPlan);

            InvestmentPlan result = investmentPlanService.pausePlan(1L);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should resume plan successfully")
        void resumePlan_Success() {
            testPlan.setStatus(PlanStatus.PAUSED);
            when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(planRepository.save(any(InvestmentPlan.class))).thenReturn(testPlan);

            InvestmentPlan result = investmentPlanService.resumePlan(1L);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should complete plan successfully")
        void completePlan_Success() {
            when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
            when(planRepository.save(any(InvestmentPlan.class))).thenReturn(testPlan);

            InvestmentPlan result = investmentPlanService.completePlan(1L);

            assertNotNull(result);
        }
    }

    // ========================================
    // DELETE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Delete Plan Tests")
    class DeletePlanTests {

        @Test
        @DisplayName("Should delete plan successfully")
        void deletePlan_Success() {
            when(planRepository.existsById(1L)).thenReturn(true);
            doNothing().when(planRepository).deleteById(1L);

            assertDoesNotThrow(() -> investmentPlanService.deletePlan(1L));

            verify(planRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent plan")
        void deletePlan_NotFound_ThrowsException() {
            when(planRepository.existsById(999L)).thenReturn(false);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> investmentPlanService.deletePlan(999L));

            assertEquals("Investment plan not found with ID: 999", exception.getMessage());
        }
    }

    // ========================================
    // STATISTICS TESTS
    // ========================================

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should count plans by status")
        void countPlansByStatus_ReturnsCount() {
            when(planRepository.countByStatus(PlanStatus.ACTIVE)).thenReturn(5L);

            assertEquals(5L, investmentPlanService.countPlansByStatus(PlanStatus.ACTIVE));
        }

        @Test
        @DisplayName("Should count plans by risk appetite")
        void countPlansByRiskAppetite_ReturnsCount() {
            when(planRepository.countByRiskAppetite(RiskAppetite.MEDIUM)).thenReturn(3L);

            assertEquals(3L, investmentPlanService.countPlansByRiskAppetite(RiskAppetite.MEDIUM));
        }
    }
}
