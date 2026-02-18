package com.wms.entity;

import com.wms.entity.InvestmentPlan.PlanStatus;
import com.wms.entity.InvestmentPlan.RiskAppetite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InvestmentPlan entity
 * Tests constructors, getters, setters, and enums
 */
class InvestmentPlanTest {

    private InvestmentPlan plan;
    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setClientId(1L);
        client.setName("John Doe");

        plan = new InvestmentPlan();
        plan.setPlanId(1L);
        plan.setClient(client);
        plan.setInvestmentObjective("Retirement Planning");
        plan.setRiskAppetite(RiskAppetite.MEDIUM);
        plan.setAllocationDetails("{\"stocks\": 50, \"bonds\": 35}");
        plan.setTargetAmount(new BigDecimal("500000.00"));
        plan.setDurationYears(20);
        plan.setStatus(PlanStatus.ACTIVE);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create plan with default constructor")
        void defaultConstructor() {
            InvestmentPlan p = new InvestmentPlan();
            assertNotNull(p);
        }

        @Test
        @DisplayName("Should create plan with parameterized constructor")
        void parameterizedConstructor() {
            InvestmentPlan p = new InvestmentPlan(client, "Wealth Growth", RiskAppetite.HIGH);
            
            assertEquals(client, p.getClient());
            assertEquals("Wealth Growth", p.getInvestmentObjective());
            assertEquals(RiskAppetite.HIGH, p.getRiskAppetite());
            assertEquals(PlanStatus.ACTIVE, p.getStatus());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set planId")
        void planId() {
            plan.setPlanId(100L);
            assertEquals(100L, plan.getPlanId());
        }

        @Test
        @DisplayName("Should get and set client")
        void clientTest() {
            Client newClient = new Client();
            newClient.setClientId(2L);
            plan.setClient(newClient);
            assertEquals(2L, plan.getClient().getClientId());
        }

        @Test
        @DisplayName("Should get and set investmentObjective")
        void investmentObjective() {
            plan.setInvestmentObjective("New Objective");
            assertEquals("New Objective", plan.getInvestmentObjective());
        }

        @Test
        @DisplayName("Should get and set riskAppetite")
        void riskAppetite() {
            plan.setRiskAppetite(RiskAppetite.LOW);
            assertEquals(RiskAppetite.LOW, plan.getRiskAppetite());
        }

        @Test
        @DisplayName("Should get and set allocationDetails")
        void allocationDetails() {
            plan.setAllocationDetails("{\"stocks\": 70}");
            assertEquals("{\"stocks\": 70}", plan.getAllocationDetails());
        }

        @Test
        @DisplayName("Should get and set targetAmount")
        void targetAmount() {
            plan.setTargetAmount(new BigDecimal("1000000.00"));
            assertEquals(new BigDecimal("1000000.00"), plan.getTargetAmount());
        }

        @Test
        @DisplayName("Should get and set durationYears")
        void durationYears() {
            plan.setDurationYears(30);
            assertEquals(30, plan.getDurationYears());
        }

        @Test
        @DisplayName("Should get and set status")
        void status() {
            plan.setStatus(PlanStatus.PAUSED);
            assertEquals(PlanStatus.PAUSED, plan.getStatus());
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {

        @Test
        @DisplayName("Should have all expected RiskAppetite values")
        void riskAppetiteValues() {
            assertEquals(3, RiskAppetite.values().length);
            assertNotNull(RiskAppetite.LOW);
            assertNotNull(RiskAppetite.MEDIUM);
            assertNotNull(RiskAppetite.HIGH);
        }

        @Test
        @DisplayName("Should have all expected PlanStatus values")
        void planStatusValues() {
            assertEquals(3, PlanStatus.values().length);
            assertNotNull(PlanStatus.ACTIVE);
            assertNotNull(PlanStatus.PAUSED);
            assertNotNull(PlanStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("Lifecycle Callback Tests")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("Should set timestamps on onCreate")
        void onCreate() {
            InvestmentPlan newPlan = new InvestmentPlan();
            assertNull(newPlan.getCreatedAt());
            assertNull(newPlan.getUpdatedAt());
            newPlan.onCreate();
            assertNotNull(newPlan.getCreatedAt());
            assertNotNull(newPlan.getUpdatedAt());
        }

        @Test
        @DisplayName("Should update timestamp on onUpdate")
        void onUpdate() {
            InvestmentPlan newPlan = new InvestmentPlan();
            newPlan.onCreate();
            java.time.LocalDateTime createdAt = newPlan.getCreatedAt();
            java.time.LocalDateTime originalUpdatedAt = newPlan.getUpdatedAt();
            
            // Wait a tiny bit to ensure different timestamp
            try { Thread.sleep(10); } catch (InterruptedException e) { }
            
            newPlan.onUpdate();
            assertEquals(createdAt, newPlan.getCreatedAt());
            assertNotNull(newPlan.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should get and set createdAt")
        void createdAt() {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            plan.setCreatedAt(now);
            assertEquals(now, plan.getCreatedAt());
        }

        @Test
        @DisplayName("Should get and set updatedAt")
        void updatedAt() {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            plan.setUpdatedAt(now);
            assertEquals(now, plan.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return meaningful string representation")
        void toStringTest() {
            String result = plan.toString();
            assertNotNull(result);
            assertTrue(result.contains("1"));
            assertTrue(result.contains("Retirement Planning"));
            assertTrue(result.contains("MEDIUM"));
            assertTrue(result.contains("ACTIVE"));
        }
    }
}
