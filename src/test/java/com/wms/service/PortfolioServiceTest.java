package com.wms.service;

import com.wms.entity.Client;
import com.wms.entity.Portfolio;
import com.wms.repository.ClientRepository;
import com.wms.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PortfolioService
 * Achieves 100% code coverage for all PortfolioService methods
 */
@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private Client testClient;
    private Portfolio testPortfolio;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setClientId(1L);
        testClient.setName("John Doe");
        testClient.setEmail("john.doe@example.com");

        testPortfolio = new Portfolio();
        testPortfolio.setPortfolioId(1L);
        testPortfolio.setPortfolioName("Retirement Fund");
        testPortfolio.setTotalValue(new BigDecimal("100000.00"));
        testPortfolio.setInitialInvestment(new BigDecimal("80000.00"));
        testPortfolio.setLastUpdated(LocalDate.now());
        testPortfolio.setClient(testClient);
        testPortfolio.setAllocationSummary("{\"stocks\": 60, \"bonds\": 40}");
    }

    // ========================================
    // CREATE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Create Portfolio Tests")
    class CreatePortfolioTests {

        @Test
        @DisplayName("Should create portfolio successfully")
        void createPortfolio_Success() {
            Portfolio newPortfolio = new Portfolio();
            newPortfolio.setPortfolioName("New Portfolio");
            newPortfolio.setTotalValue(new BigDecimal("50000.00"));
            newPortfolio.setInitialInvestment(new BigDecimal("50000.00"));

            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(portfolioRepository.existsByClientClientIdAndPortfolioName(anyLong(), anyString())).thenReturn(false);
            when(portfolioRepository.save(any(Portfolio.class))).thenReturn(newPortfolio);

            Portfolio result = portfolioService.createPortfolio(1L, newPortfolio);

            assertNotNull(result);
            verify(portfolioRepository).save(any(Portfolio.class));
        }

        @Test
        @DisplayName("Should set initial investment from total value when not provided")
        void createPortfolio_SetsInitialInvestment() {
            Portfolio newPortfolio = new Portfolio();
            newPortfolio.setPortfolioName("New Portfolio");
            newPortfolio.setTotalValue(new BigDecimal("50000.00"));
            // No initial investment set

            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(portfolioRepository.existsByClientClientIdAndPortfolioName(anyLong(), anyString())).thenReturn(false);
            when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Portfolio result = portfolioService.createPortfolio(1L, newPortfolio);

            assertEquals(new BigDecimal("50000.00"), result.getInitialInvestment());
        }

        @Test
        @DisplayName("Should throw exception when client not found")
        void createPortfolio_ClientNotFound_ThrowsException() {
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> portfolioService.createPortfolio(999L, testPortfolio));

            assertEquals("Client not found with ID: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when portfolio name already exists for client")
        void createPortfolio_DuplicateName_ThrowsException() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(portfolioRepository.existsByClientClientIdAndPortfolioName(1L, "Retirement Fund")).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> portfolioService.createPortfolio(1L, testPortfolio));

            assertEquals("Portfolio with this name already exists for the client", exception.getMessage());
        }
    }

    // ========================================
    // READ OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Read Portfolio Tests")
    class ReadPortfolioTests {

        @Test
        @DisplayName("Should return portfolio by ID when exists")
        void getPortfolioById_Found() {
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));

            Optional<Portfolio> result = portfolioService.getPortfolioById(1L);

            assertTrue(result.isPresent());
            assertEquals("Retirement Fund", result.get().getPortfolioName());
        }

        @Test
        @DisplayName("Should return empty when portfolio not found")
        void getPortfolioById_NotFound() {
            when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Portfolio> result = portfolioService.getPortfolioById(999L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return all portfolios for client")
        void getClientPortfolios_ReturnsList() {
            when(portfolioRepository.findByClientClientId(1L)).thenReturn(Arrays.asList(testPortfolio));

            List<Portfolio> result = portfolioService.getClientPortfolios(1L);

            assertEquals(1, result.size());
            assertEquals("Retirement Fund", result.get(0).getPortfolioName());
        }

        @Test
        @DisplayName("Should return all portfolios")
        void getAllPortfolios_ReturnsList() {
            when(portfolioRepository.findAll()).thenReturn(Arrays.asList(testPortfolio));

            List<Portfolio> result = portfolioService.getAllPortfolios();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return top performing portfolios")
        void getTopPerformingPortfolios_ReturnsList() {
            when(portfolioRepository.findTopPerformingPortfolios()).thenReturn(Arrays.asList(testPortfolio));

            List<Portfolio> result = portfolioService.getTopPerformingPortfolios();

            assertEquals(1, result.size());
        }
    }

    // ========================================
    // UPDATE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Update Portfolio Tests")
    class UpdatePortfolioTests {

        @Test
        @DisplayName("Should update portfolio successfully")
        void updatePortfolio_Success() {
            Portfolio updatedPortfolio = new Portfolio();
            updatedPortfolio.setPortfolioName("Updated Portfolio");
            updatedPortfolio.setTotalValue(new BigDecimal("120000.00"));
            updatedPortfolio.setAllocationSummary("{\"stocks\": 70, \"bonds\": 30}");

            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

            Portfolio result = portfolioService.updatePortfolio(1L, updatedPortfolio);

            assertNotNull(result);
            verify(portfolioRepository).save(any(Portfolio.class));
        }

        @Test
        @DisplayName("Should throw exception when portfolio not found for update")
        void updatePortfolio_NotFound_ThrowsException() {
            when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> portfolioService.updatePortfolio(999L, testPortfolio));

            assertEquals("Portfolio not found with ID: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should update portfolio value successfully")
        void updatePortfolioValue_Success() {
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

            Portfolio result = portfolioService.updatePortfolioValue(1L, new BigDecimal("150000.00"));

            assertNotNull(result);
            verify(portfolioRepository).save(any(Portfolio.class));
        }

        @Test
        @DisplayName("Should throw exception when updating value for non-existent portfolio")
        void updatePortfolioValue_NotFound_ThrowsException() {
            when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> portfolioService.updatePortfolioValue(999L, new BigDecimal("150000.00")));

            assertEquals("Portfolio not found with ID: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should rebalance portfolio successfully")
        void rebalancePortfolio_Success() {
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

            Portfolio result = portfolioService.rebalancePortfolio(1L, "{\"stocks\": 50, \"bonds\": 50}");

            assertNotNull(result);
            verify(portfolioRepository).save(any(Portfolio.class));
        }

        @Test
        @DisplayName("Should throw exception when rebalancing non-existent portfolio")
        void rebalancePortfolio_NotFound_ThrowsException() {
            when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> portfolioService.rebalancePortfolio(999L, "{\"stocks\": 50, \"bonds\": 50}"));

            assertEquals("Portfolio not found with ID: 999", exception.getMessage());
        }
    }

    // ========================================
    // DELETE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Delete Portfolio Tests")
    class DeletePortfolioTests {

        @Test
        @DisplayName("Should delete portfolio successfully")
        void deletePortfolio_Success() {
            when(portfolioRepository.existsById(1L)).thenReturn(true);
            doNothing().when(portfolioRepository).deleteById(1L);

            assertDoesNotThrow(() -> portfolioService.deletePortfolio(1L));

            verify(portfolioRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent portfolio")
        void deletePortfolio_NotFound_ThrowsException() {
            when(portfolioRepository.existsById(999L)).thenReturn(false);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> portfolioService.deletePortfolio(999L));

            assertEquals("Portfolio not found with ID: 999", exception.getMessage());
        }
    }

    // ========================================
    // ANALYSIS & CALCULATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Analysis and Calculations Tests")
    class AnalysisCalculationsTests {

        @Test
        @DisplayName("Should calculate portfolio return percentage")
        void calculatePortfolioReturn_Success() {
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));

            BigDecimal result = portfolioService.calculatePortfolioReturn(1L);

            // (100000 - 80000) / 80000 * 100 = 25%
            assertEquals(new BigDecimal("25.00"), result);
        }

        @Test
        @DisplayName("Should throw exception when calculating return for non-existent portfolio")
        void calculatePortfolioReturn_NotFound_ThrowsException() {
            when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> portfolioService.calculatePortfolioReturn(999L));

            assertEquals("Portfolio not found with ID: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should calculate client total value")
        void calculateClientTotalValue_Success() {
            when(portfolioRepository.calculateClientTotalValue(1L)).thenReturn(new BigDecimal("200000.00"));

            BigDecimal result = portfolioService.calculateClientTotalValue(1L);

            assertEquals(new BigDecimal("200000.00"), result);
        }

        @Test
        @DisplayName("Should return zero when client has no portfolios")
        void calculateClientTotalValue_NullReturnsZero() {
            when(portfolioRepository.calculateClientTotalValue(1L)).thenReturn(null);

            BigDecimal result = portfolioService.calculateClientTotalValue(1L);

            assertEquals(BigDecimal.ZERO, result);
        }

        @Test
        @DisplayName("Should calculate total AUM")
        void calculateTotalAUM_Success() {
            when(portfolioRepository.calculateTotalAUM()).thenReturn(new BigDecimal("1000000.00"));

            BigDecimal result = portfolioService.calculateTotalAUM();

            assertEquals(new BigDecimal("1000000.00"), result);
        }

        @Test
        @DisplayName("Should return zero when AUM is null")
        void calculateTotalAUM_NullReturnsZero() {
            when(portfolioRepository.calculateTotalAUM()).thenReturn(null);

            BigDecimal result = portfolioService.calculateTotalAUM();

            assertEquals(BigDecimal.ZERO, result);
        }

        @Test
        @DisplayName("Should return portfolios needing review")
        void getPortfoliosNeedingReview_ReturnsList() {
            when(portfolioRepository.findPortfoliosNeedingReview(any(LocalDate.class)))
                .thenReturn(Arrays.asList(testPortfolio));

            List<Portfolio> result = portfolioService.getPortfoliosNeedingReview(30);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return high value portfolios")
        void getHighValuePortfolios_ReturnsList() {
            when(portfolioRepository.findByTotalValueGreaterThan(new BigDecimal("50000.00")))
                .thenReturn(Arrays.asList(testPortfolio));

            List<Portfolio> result = portfolioService.getHighValuePortfolios(new BigDecimal("50000.00"));

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should get portfolio performance summary")
        void getPortfolioPerformanceSummary_Success() {
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));

            String result = portfolioService.getPortfolioPerformanceSummary(1L);

            assertNotNull(result);
            assertTrue(result.contains("Portfolio Performance Summary"));
            assertTrue(result.contains("Retirement Fund"));
            assertTrue(result.contains("Current Value"));
        }

        @Test
        @DisplayName("Should get portfolio performance summary with decline status")
        void getPortfolioPerformanceSummary_Decline() {
            testPortfolio.setInitialInvestment(new BigDecimal("150000.00"));
            testPortfolio.setTotalValue(new BigDecimal("100000.00")); // Loss
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));

            String result = portfolioService.getPortfolioPerformanceSummary(1L);

            assertNotNull(result);
            assertTrue(result.contains("decline"));
        }

        @Test
        @DisplayName("Should get portfolio performance summary with no change status")
        void getPortfolioPerformanceSummary_NoChange() {
            testPortfolio.setInitialInvestment(new BigDecimal("100000.00"));
            testPortfolio.setTotalValue(new BigDecimal("100000.00")); // No change
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));

            String result = portfolioService.getPortfolioPerformanceSummary(1L);

            assertNotNull(result);
            assertTrue(result.contains("no change"));
        }

        @Test
        @DisplayName("Should throw exception when getting performance summary for non-existent portfolio")
        void getPortfolioPerformanceSummary_NotFound_ThrowsException() {
            when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> portfolioService.getPortfolioPerformanceSummary(999L));

            assertEquals("Portfolio not found with ID: 999", exception.getMessage());
        }
    }
}
