package com.wms.controller;

import com.wms.entity.Client;
import com.wms.entity.Portfolio;
import com.wms.service.ClientService;
import com.wms.service.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioController Tests")
class PortfolioControllerTest {

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private ClientService clientService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private PortfolioController controller;

    private Portfolio testPortfolio;
    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setClientId(1L);
        testClient.setName("Test Client");

        testPortfolio = new Portfolio();
        testPortfolio.setPortfolioId(1L);
        testPortfolio.setClient(testClient);
        testPortfolio.setPortfolioName("Test Portfolio");
        testPortfolio.setInitialInvestment(new BigDecimal("100000"));
        testPortfolio.setTotalValue(new BigDecimal("110000"));
        testPortfolio.setLastUpdated(LocalDate.now());
    }

    @Nested
    @DisplayName("List Operations Tests")
    class ListOperationsTests {

        @Test
        @DisplayName("Should list all portfolios with total AUM")
        void listPortfolios_ReturnsListViewWithAUM() {
            List<Portfolio> portfolios = Arrays.asList(testPortfolio);
            when(portfolioService.getAllPortfolios()).thenReturn(portfolios);
            when(portfolioService.calculateTotalAUM()).thenReturn(new BigDecimal("1000000"));
            
            String result = controller.listPortfolios(model);
            
            assertEquals("portfolio/list", result);
            verify(model).addAttribute("portfolios", portfolios);
            verify(model).addAttribute("totalAUM", new BigDecimal("1000000"));
        }

        @Test
        @DisplayName("Should list client portfolios")
        void listClientPortfolios_ReturnsClientPortfoliosView() {
            List<Portfolio> portfolios = Arrays.asList(testPortfolio);
            when(clientService.getClientById(1L)).thenReturn(Optional.of(testClient));
            when(portfolioService.getClientPortfolios(1L)).thenReturn(portfolios);
            when(portfolioService.calculateClientTotalValue(1L)).thenReturn(new BigDecimal("110000"));
            
            String result = controller.listClientPortfolios(1L, model);
            
            assertEquals("portfolio/client-portfolios", result);
            verify(model).addAttribute("client", testClient);
            verify(model).addAttribute("portfolios", portfolios);
            verify(model).addAttribute("totalValue", new BigDecimal("110000"));
        }

        @Test
        @DisplayName("Should throw exception when client not found")
        void listClientPortfolios_ClientNotFound_ThrowsException() {
            when(clientService.getClientById(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> controller.listClientPortfolios(99L, model));
        }
    }

    @Nested
    @DisplayName("View Operations Tests")
    class ViewOperationsTests {

        @Test
        @DisplayName("Should view portfolio details")
        void viewPortfolio_ReturnsViewPage() {
            when(portfolioService.getPortfolioByIdWithReports(1L)).thenReturn(Optional.of(testPortfolio));
            when(portfolioService.calculatePortfolioReturn(1L)).thenReturn(new BigDecimal("10.00"));
            when(portfolioService.getPortfolioPerformanceSummary(1L)).thenReturn("Good performance");
            
            String result = controller.viewPortfolio(1L, model);
            
            assertEquals("portfolio/view", result);
            verify(model).addAttribute("portfolio", testPortfolio);
            verify(model).addAttribute("returnPercentage", new BigDecimal("10.00"));
            verify(model).addAttribute("performanceSummary", "Good performance");
        }

        @Test
        @DisplayName("Should throw exception when portfolio not found")
        void viewPortfolio_NotFound_ThrowsException() {
            when(portfolioService.getPortfolioByIdWithReports(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> controller.viewPortfolio(99L, model));
        }

        @Test
        @DisplayName("Should view portfolio performance")
        void viewPerformance_ReturnsPerformanceView() {
            testPortfolio.setInitialInvestment(new BigDecimal("100000"));
            testPortfolio.setTotalValue(new BigDecimal("110000"));
            when(portfolioService.getPortfolioById(1L)).thenReturn(Optional.of(testPortfolio));
            
            String result = controller.viewPerformance(1L, model);
            
            assertEquals("portfolio/performance", result);
            verify(model).addAttribute("portfolio", testPortfolio);
        }

        @Test
        @DisplayName("Should throw exception when portfolio not found for performance")
        void viewPerformance_NotFound_ThrowsException() {
            when(portfolioService.getPortfolioById(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> controller.viewPerformance(99L, model));
        }
    }

    @Nested
    @DisplayName("Create Operations Tests")
    class CreateOperationsTests {

        @Test
        @DisplayName("Should show create form without client")
        void showCreateForm_NoClient_ReturnsFormView() {
            when(clientService.getAllClients()).thenReturn(Arrays.asList(testClient));
            
            String result = controller.showCreateForm(null, model);
            
            assertEquals("portfolio/form", result);
            verify(model).addAttribute(eq("portfolio"), any(Portfolio.class));
            verify(model).addAttribute("clients", Arrays.asList(testClient));
        }

        @Test
        @DisplayName("Should show create form with pre-selected client")
        void showCreateForm_WithClient_ReturnsFormViewWithSelectedClient() {
            when(clientService.getClientById(1L)).thenReturn(Optional.of(testClient));
            when(clientService.getAllClients()).thenReturn(Arrays.asList(testClient));
            
            String result = controller.showCreateForm(1L, model);
            
            assertEquals("portfolio/form", result);
            verify(model).addAttribute("selectedClientId", 1L);
        }

        @Test
        @DisplayName("Should return to form when binding errors exist")
        void createPortfolio_WithBindingErrors_ReturnsFormView() {
            when(bindingResult.hasErrors()).thenReturn(true);
            when(clientService.getAllClients()).thenReturn(Arrays.asList(testClient));
            
            String result = controller.createPortfolio(testPortfolio, bindingResult, 1L, redirectAttributes, model);
            
            assertEquals("portfolio/form", result);
            verify(portfolioService, never()).createPortfolio(anyLong(), any());
        }

        @Test
        @DisplayName("Should redirect to portfolio view on successful creation")
        void createPortfolio_Success_RedirectsToPortfolio() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(portfolioService.createPortfolio(eq(1L), any())).thenReturn(testPortfolio);
            
            String result = controller.createPortfolio(testPortfolio, bindingResult, 1L, redirectAttributes, model);
            
            assertEquals("redirect:/portfolios", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should redirect to new form when creation fails")
        void createPortfolio_Exception_RedirectsToNewForm() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(portfolioService.createPortfolio(eq(1L), any())).thenThrow(new RuntimeException("Error"));
            
            String result = controller.createPortfolio(testPortfolio, bindingResult, 1L, redirectAttributes, model);
            
            assertEquals("redirect:/portfolios/new?clientId=1", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }

    @Nested
    @DisplayName("Update Operations Tests")
    class UpdateOperationsTests {

        @Test
        @DisplayName("Should show edit form")
        void showEditForm_ReturnsFormView() {
            when(portfolioService.getPortfolioById(1L)).thenReturn(Optional.of(testPortfolio));
            when(clientService.getAllClients()).thenReturn(Arrays.asList(testClient));
            
            String result = controller.showEditForm(1L, model);
            
            assertEquals("portfolio/form", result);
            verify(model).addAttribute("portfolio", testPortfolio);
            verify(model).addAttribute("isEdit", true);
        }

        @Test
        @DisplayName("Should throw exception when portfolio not found for edit")
        void showEditForm_NotFound_ThrowsException() {
            when(portfolioService.getPortfolioById(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> controller.showEditForm(99L, model));
        }

        @Test
        @DisplayName("Should return to form when binding errors on update")
        void updatePortfolio_WithBindingErrors_ReturnsFormView() {
            when(bindingResult.hasErrors()).thenReturn(true);
            when(clientService.getAllClients()).thenReturn(Arrays.asList(testClient));
            
            String result = controller.updatePortfolio(1L, testPortfolio, bindingResult, redirectAttributes, model);
            
            assertEquals("portfolio/form", result);
            verify(model).addAttribute("isEdit", true);
        }

        @Test
        @DisplayName("Should redirect to portfolio on successful update")
        void updatePortfolio_Success_RedirectsToPortfolio() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(portfolioService.updatePortfolio(eq(1L), any())).thenReturn(testPortfolio);
            
            String result = controller.updatePortfolio(1L, testPortfolio, bindingResult, redirectAttributes, model);
            
            assertEquals("redirect:/portfolios", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should redirect to edit form when update fails")
        void updatePortfolio_Exception_RedirectsToEditForm() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(portfolioService.updatePortfolio(eq(1L), any())).thenThrow(new RuntimeException("Error"));
            
            String result = controller.updatePortfolio(1L, testPortfolio, bindingResult, redirectAttributes, model);
            
            assertEquals("redirect:/portfolios/1/edit", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }

        @Test
        @DisplayName("Should update portfolio value")
        void updateValue_Success_RedirectsToPortfolio() {
            when(portfolioService.updatePortfolioValue(eq(1L), any())).thenReturn(testPortfolio);
            
            String result = controller.updateValue(1L, new BigDecimal("120000"), redirectAttributes);
            
            assertEquals("redirect:/portfolios", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should handle update value error")
        void updateValue_Exception_RedirectsWithError() {
            when(portfolioService.updatePortfolioValue(eq(1L), any())).thenThrow(new RuntimeException("Error"));
            
            String result = controller.updateValue(1L, new BigDecimal("120000"), redirectAttributes);
            
            assertEquals("redirect:/portfolios", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }

    @Nested
    @DisplayName("Rebalance Operations Tests")
    class RebalanceOperationsTests {

        @Test
        @DisplayName("Should show rebalance form")
        void showRebalanceForm_ReturnsRebalanceView() {
            when(portfolioService.getPortfolioById(1L)).thenReturn(Optional.of(testPortfolio));
            
            String result = controller.showRebalanceForm(1L, model);
            
            assertEquals("portfolio/rebalance", result);
            verify(model).addAttribute("portfolio", testPortfolio);
        }

        @Test
        @DisplayName("Should throw exception when portfolio not found for rebalance")
        void showRebalanceForm_NotFound_ThrowsException() {
            when(portfolioService.getPortfolioById(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> controller.showRebalanceForm(99L, model));
        }

        @Test
        @DisplayName("Should rebalance portfolio successfully")
        void rebalancePortfolio_Success_RedirectsToPortfolio() {
            when(portfolioService.rebalancePortfolio(eq(1L), anyString())).thenReturn(testPortfolio);
            
            String result = controller.rebalancePortfolio(1L, "New allocation", redirectAttributes);
            
            assertEquals("redirect:/portfolios", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should handle rebalance error")
        void rebalancePortfolio_Exception_RedirectsWithError() {
            when(portfolioService.rebalancePortfolio(eq(1L), anyString())).thenThrow(new RuntimeException("Error"));
            
            String result = controller.rebalancePortfolio(1L, "New allocation", redirectAttributes);
            
            assertEquals("redirect:/portfolios", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }

    @Nested
    @DisplayName("Delete Operations Tests")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should delete portfolio and redirect to list")
        void deletePortfolio_Success_RedirectsToList() {
            doNothing().when(portfolioService).deletePortfolio(1L);
            
            String result = controller.deletePortfolio(1L, redirectAttributes);
            
            assertEquals("redirect:/portfolios", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should handle delete portfolio error")
        void deletePortfolio_Exception_RedirectsWithError() {
            doThrow(new RuntimeException("Error")).when(portfolioService).deletePortfolio(1L);
            
            String result = controller.deletePortfolio(1L, redirectAttributes);
            
            assertEquals("redirect:/portfolios", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }
}
