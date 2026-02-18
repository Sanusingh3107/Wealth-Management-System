package com.wms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Portfolio entity
 * Tests constructors, getters, setters, and business methods
 */
class PortfolioTest {

    private Portfolio portfolio;
    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setClientId(1L);
        client.setName("John Doe");

        portfolio = new Portfolio();
        portfolio.setPortfolioId(1L);
        portfolio.setPortfolioName("Retirement Fund");
        portfolio.setTotalValue(new BigDecimal("100000.00"));
        portfolio.setInitialInvestment(new BigDecimal("80000.00"));
        portfolio.setClient(client);
        portfolio.setLastUpdated(LocalDate.now());
        portfolio.setAllocationSummary("{\"stocks\": 60, \"bonds\": 40}");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create portfolio with default constructor")
        void defaultConstructor() {
            Portfolio p = new Portfolio();
            assertNotNull(p.getLastUpdated());
        }

        @Test
        @DisplayName("Should create portfolio with parameterized constructor")
        void parameterizedConstructor() {
            Portfolio p = new Portfolio(client, "Test Fund", new BigDecimal("50000.00"));
            
            assertEquals(client, p.getClient());
            assertEquals("Test Fund", p.getPortfolioName());
            assertEquals(new BigDecimal("50000.00"), p.getTotalValue());
            assertEquals(new BigDecimal("50000.00"), p.getInitialInvestment());
            assertNotNull(p.getLastUpdated());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set portfolioId")
        void portfolioId() {
            portfolio.setPortfolioId(100L);
            assertEquals(100L, portfolio.getPortfolioId());
        }

        @Test
        @DisplayName("Should get and set client")
        void client() {
            Client newClient = new Client();
            newClient.setClientId(2L);
            portfolio.setClient(newClient);
            assertEquals(2L, portfolio.getClient().getClientId());
        }

        @Test
        @DisplayName("Should get and set portfolioName")
        void portfolioName() {
            portfolio.setPortfolioName("New Name");
            assertEquals("New Name", portfolio.getPortfolioName());
        }

        @Test
        @DisplayName("Should get and set totalValue and update lastUpdated")
        void totalValue() {
            LocalDate before = portfolio.getLastUpdated();
            portfolio.setTotalValue(new BigDecimal("150000.00"));
            assertEquals(new BigDecimal("150000.00"), portfolio.getTotalValue());
            assertNotNull(portfolio.getLastUpdated());
        }

        @Test
        @DisplayName("Should get and set allocationSummary")
        void allocationSummary() {
            portfolio.setAllocationSummary("{\"stocks\": 70}");
            assertEquals("{\"stocks\": 70}", portfolio.getAllocationSummary());
        }

        @Test
        @DisplayName("Should get and set lastUpdated")
        void lastUpdated() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            portfolio.setLastUpdated(date);
            assertEquals(date, portfolio.getLastUpdated());
        }

        @Test
        @DisplayName("Should get and set initialInvestment")
        void initialInvestment() {
            portfolio.setInitialInvestment(new BigDecimal("90000.00"));
            assertEquals(new BigDecimal("90000.00"), portfolio.getInitialInvestment());
        }

        @Test
        @DisplayName("Should get and set reports")
        void reports() {
            portfolio.setReports(new ArrayList<>());
            assertNotNull(portfolio.getReports());
            assertEquals(0, portfolio.getReports().size());
        }
    }

    @Nested
    @DisplayName("Business Methods Tests")
    class BusinessMethodsTests {

        @Test
        @DisplayName("Should calculate return percentage correctly")
        void calculateReturnPercentage_Success() {
            // (100000 - 80000) / 80000 * 100 = 25%
            BigDecimal result = portfolio.calculateReturnPercentage();
            assertEquals(new BigDecimal("25.00"), result);
        }

        @Test
        @DisplayName("Should return zero when initial investment is null")
        void calculateReturnPercentage_NullInitialInvestment() {
            portfolio.setInitialInvestment(null);
            assertEquals(BigDecimal.ZERO, portfolio.calculateReturnPercentage());
        }

        @Test
        @DisplayName("Should return zero when initial investment is zero")
        void calculateReturnPercentage_ZeroInitialInvestment() {
            portfolio.setInitialInvestment(BigDecimal.ZERO);
            assertEquals(BigDecimal.ZERO, portfolio.calculateReturnPercentage());
        }

        @Test
        @DisplayName("Should calculate profit/loss correctly - profit")
        void calculateProfitLoss_Profit() {
            // 100000 - 80000 = 20000 profit
            BigDecimal result = portfolio.calculateProfitLoss();
            assertEquals(new BigDecimal("20000.00"), result);
        }

        @Test
        @DisplayName("Should calculate profit/loss correctly - loss")
        void calculateProfitLoss_Loss() {
            portfolio.setTotalValue(new BigDecimal("70000.00"));
            // 70000 - 80000 = -10000 loss
            BigDecimal result = portfolio.calculateProfitLoss();
            assertEquals(new BigDecimal("-10000.00"), result);
        }

        @Test
        @DisplayName("Should return zero when initial investment is null for profit/loss")
        void calculateProfitLoss_NullInitialInvestment() {
            portfolio.setInitialInvestment(null);
            assertEquals(BigDecimal.ZERO, portfolio.calculateProfitLoss());
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return correct string representation")
        void toStringTest() {
            String result = portfolio.toString();
            
            assertTrue(result.contains("portfolioId=1"));
            assertTrue(result.contains("portfolioName='Retirement Fund'"));
            assertTrue(result.contains("totalValue=100000.00"));
        }
    }
}
