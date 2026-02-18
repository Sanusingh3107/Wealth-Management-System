package com.wms.controller;

import com.wms.entity.Client;
import com.wms.entity.InvestmentPlan;
import com.wms.entity.InvestmentPlan.PlanStatus;
import com.wms.entity.InvestmentPlan.RiskAppetite;
import com.wms.service.ClientService;
import com.wms.service.InvestmentPlanService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentPlanController Tests")
class InvestmentPlanControllerTest {

    @Mock
    private InvestmentPlanService planService;

    @Mock
    private ClientService clientService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private InvestmentPlanController controller;

    private InvestmentPlan testPlan;
    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setClientId(1L);
        testClient.setName("Test Client");

        testPlan = new InvestmentPlan();
        testPlan.setPlanId(1L);
        testPlan.setClient(testClient);
        testPlan.setInvestmentObjective("Retirement");
        testPlan.setRiskAppetite(RiskAppetite.MEDIUM);
        testPlan.setTargetAmount(new BigDecimal("100000"));
        testPlan.setDurationYears(10);
        testPlan.setStatus(PlanStatus.ACTIVE);
    }

    @Nested
    @DisplayName("List Operations Tests")
    class ListOperationsTests {

        @Test
        @DisplayName("Should list all plans")
        void listPlans_ReturnsListView() {
            List<InvestmentPlan> plans = Arrays.asList(testPlan);
            when(planService.getAllPlans()).thenReturn(plans);
            
            String result = controller.listPlans(model);
            
            assertEquals("plan/list", result);
            verify(model).addAttribute("plans", plans);
        }

        @Test
        @DisplayName("Should list plans by client")
        void listClientPlans_ReturnsClientPlansView() {
            List<InvestmentPlan> plans = Arrays.asList(testPlan);
            when(clientService.getClientById(1L)).thenReturn(Optional.of(testClient));
            when(planService.getClientPlans(1L)).thenReturn(plans);
            
            String result = controller.listClientPlans(1L, model);
            
            assertEquals("plan/client-plans", result);
            verify(model).addAttribute("client", testClient);
            verify(model).addAttribute("plans", plans);
        }

        @Test
        @DisplayName("Should throw exception when client not found")
        void listClientPlans_ClientNotFound_ThrowsException() {
            when(clientService.getClientById(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> controller.listClientPlans(99L, model));
        }
    }

    @Nested
    @DisplayName("View Operations Tests")
    class ViewOperationsTests {

        @Test
        @DisplayName("Should view plan details")
        void viewPlan_ReturnsViewPage() {
            when(planService.getPlanById(1L)).thenReturn(Optional.of(testPlan));
            
            String result = controller.viewPlan(1L, model);
            
            assertEquals("plan/view", result);
            verify(model).addAttribute("plan", testPlan);
        }

        @Test
        @DisplayName("Should throw exception when plan not found")
        void viewPlan_NotFound_ThrowsException() {
            when(planService.getPlanById(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> controller.viewPlan(99L, model));
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
            
            assertEquals("plan/form", result);
            verify(model).addAttribute(eq("plan"), any(InvestmentPlan.class));
            verify(model).addAttribute("clients", Arrays.asList(testClient));
            verify(model).addAttribute("riskAppetites", RiskAppetite.values());
        }

        @Test
        @DisplayName("Should show create form with pre-selected client")
        void showCreateForm_WithClient_ReturnsFormViewWithSelectedClient() {
            when(clientService.getClientById(1L)).thenReturn(Optional.of(testClient));
            when(clientService.getAllClients()).thenReturn(Arrays.asList(testClient));
            
            String result = controller.showCreateForm(1L, model);
            
            assertEquals("plan/form", result);
            verify(model).addAttribute("selectedClientId", 1L);
        }

        @Test
        @DisplayName("Should return to form when binding errors exist")
        void createPlan_WithBindingErrors_ReturnsFormView() {
            when(bindingResult.hasErrors()).thenReturn(true);
            when(clientService.getAllClients()).thenReturn(Arrays.asList(testClient));
            
            String result = controller.createPlan(testPlan, bindingResult, 1L, redirectAttributes, model);
            
            assertEquals("plan/form", result);
            verify(planService, never()).createPlan(anyLong(), any());
        }

        @Test
        @DisplayName("Should redirect to plan view on successful creation")
        void createPlan_Success_RedirectsToPlan() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(planService.createPlan(eq(1L), any())).thenReturn(testPlan);
            
            String result = controller.createPlan(testPlan, bindingResult, 1L, redirectAttributes, model);
            
            assertEquals("redirect:/plans/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should redirect to new form when creation fails")
        void createPlan_Exception_RedirectsToNewForm() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(planService.createPlan(eq(1L), any())).thenThrow(new RuntimeException("Error"));
            
            String result = controller.createPlan(testPlan, bindingResult, 1L, redirectAttributes, model);
            
            assertEquals("redirect:/plans/new?clientId=1", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }

    @Nested
    @DisplayName("Update Operations Tests")
    class UpdateOperationsTests {

        @Test
        @DisplayName("Should show edit form")
        void showEditForm_ReturnsFormView() {
            when(planService.getPlanById(1L)).thenReturn(Optional.of(testPlan));
            when(clientService.getAllClients()).thenReturn(Arrays.asList(testClient));
            
            String result = controller.showEditForm(1L, model);
            
            assertEquals("plan/form", result);
            verify(model).addAttribute("plan", testPlan);
            verify(model).addAttribute("isEdit", true);
            verify(model).addAttribute("statuses", PlanStatus.values());
        }

        @Test
        @DisplayName("Should throw exception when plan not found for edit")
        void showEditForm_NotFound_ThrowsException() {
            when(planService.getPlanById(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> controller.showEditForm(99L, model));
        }

        @Test
        @DisplayName("Should return to form when binding errors on update")
        void updatePlan_WithBindingErrors_ReturnsFormView() {
            when(bindingResult.hasErrors()).thenReturn(true);
            when(clientService.getAllClients()).thenReturn(Arrays.asList(testClient));
            
            String result = controller.updatePlan(1L, testPlan, bindingResult, redirectAttributes, model);
            
            assertEquals("plan/form", result);
            verify(model).addAttribute("isEdit", true);
        }

        @Test
        @DisplayName("Should redirect to plan on successful update")
        void updatePlan_Success_RedirectsToPlan() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(planService.updatePlan(eq(1L), any())).thenReturn(testPlan);
            
            String result = controller.updatePlan(1L, testPlan, bindingResult, redirectAttributes, model);
            
            assertEquals("redirect:/plans/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should redirect to edit form when update fails")
        void updatePlan_Exception_RedirectsToEditForm() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(planService.updatePlan(eq(1L), any())).thenThrow(new RuntimeException("Error"));
            
            String result = controller.updatePlan(1L, testPlan, bindingResult, redirectAttributes, model);
            
            assertEquals("redirect:/plans/1/edit", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }

    @Nested
    @DisplayName("Status Operations Tests")
    class StatusOperationsTests {

        @Test
        @DisplayName("Should pause plan successfully")
        void pausePlan_Success_RedirectsWithSuccess() {
            when(planService.pausePlan(1L)).thenReturn(testPlan);
            
            String result = controller.pausePlan(1L, redirectAttributes);
            
            assertEquals("redirect:/plans/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should handle pause plan error")
        void pausePlan_Exception_RedirectsWithError() {
            when(planService.pausePlan(1L)).thenThrow(new RuntimeException("Error"));
            
            String result = controller.pausePlan(1L, redirectAttributes);
            
            assertEquals("redirect:/plans/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }

        @Test
        @DisplayName("Should resume plan successfully")
        void resumePlan_Success_RedirectsWithSuccess() {
            when(planService.resumePlan(1L)).thenReturn(testPlan);
            
            String result = controller.resumePlan(1L, redirectAttributes);
            
            assertEquals("redirect:/plans/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should handle resume plan error")
        void resumePlan_Exception_RedirectsWithError() {
            when(planService.resumePlan(1L)).thenThrow(new RuntimeException("Error"));
            
            String result = controller.resumePlan(1L, redirectAttributes);
            
            assertEquals("redirect:/plans/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }

        @Test
        @DisplayName("Should complete plan successfully")
        void completePlan_Success_RedirectsWithSuccess() {
            when(planService.completePlan(1L)).thenReturn(testPlan);
            
            String result = controller.completePlan(1L, redirectAttributes);
            
            assertEquals("redirect:/plans/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should handle complete plan error")
        void completePlan_Exception_RedirectsWithError() {
            when(planService.completePlan(1L)).thenThrow(new RuntimeException("Error"));
            
            String result = controller.completePlan(1L, redirectAttributes);
            
            assertEquals("redirect:/plans/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }

    @Nested
    @DisplayName("Delete Operations Tests")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should delete plan and redirect to list")
        void deletePlan_Success_RedirectsToList() {
            doNothing().when(planService).deletePlan(1L);
            
            String result = controller.deletePlan(1L, redirectAttributes);
            
            assertEquals("redirect:/plans", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should handle delete plan error")
        void deletePlan_Exception_RedirectsWithError() {
            doThrow(new RuntimeException("Error")).when(planService).deletePlan(1L);
            
            String result = controller.deletePlan(1L, redirectAttributes);
            
            assertEquals("redirect:/plans", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }
}
