package com.wms.controller;

import com.wms.entity.Client;
import com.wms.service.ClientService;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientController Tests")
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ClientController clientController;

    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setClientId(1L);
        testClient.setName("John Doe");
        testClient.setEmail("john@example.com");
        testClient.setPhone("1234567890");
        testClient.setAddress("123 Main St");
        testClient.setDateOfBirth(LocalDate.of(1990, 1, 15));
    }

    @Nested
    @DisplayName("List Operations Tests")
    class ListOperationsTests {

        @Test
        @DisplayName("Should list all clients")
        void listClients_ReturnsListView() {
            List<Client> clients = Arrays.asList(testClient);
            when(clientService.getAllClients()).thenReturn(clients);
            
            String result = clientController.listClients(model);
            
            assertEquals("client/list", result);
            verify(model).addAttribute("clients", clients);
        }

        @Test
        @DisplayName("Should search clients by query")
        void searchClients_ReturnsListViewWithResults() {
            List<Client> clients = Arrays.asList(testClient);
            when(clientService.searchClients("John")).thenReturn(clients);
            
            String result = clientController.searchClients("John", model);
            
            assertEquals("client/list", result);
            verify(model).addAttribute("clients", clients);
            verify(model).addAttribute("searchQuery", "John");
        }
    }

    @Nested
    @DisplayName("View Operations Tests")
    class ViewOperationsTests {

        @Test
        @DisplayName("Should view client details")
        void viewClient_ReturnsViewPage() {
            when(clientService.getClientById(1L)).thenReturn(Optional.of(testClient));
            
            String result = clientController.viewClient(1L, model);
            
            assertEquals("client/view", result);
            verify(model).addAttribute("client", testClient);
        }

        @Test
        @DisplayName("Should throw exception when client not found")
        void viewClient_NotFound_ThrowsException() {
            when(clientService.getClientById(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> clientController.viewClient(99L, model));
        }
    }

    @Nested
    @DisplayName("Create Operations Tests")
    class CreateOperationsTests {

        @Test
        @DisplayName("Should show registration form")
        void showRegistrationForm_ReturnsFormView() {
            String result = clientController.showRegistrationForm(model);
            
            assertEquals("client/form", result);
            verify(model).addAttribute(eq("client"), any(Client.class));
        }

        @Test
        @DisplayName("Should return to form when binding errors exist")
        void registerClient_WithBindingErrors_ReturnsFormView() {
            when(bindingResult.hasErrors()).thenReturn(true);
            
            String result = clientController.registerClient(testClient, bindingResult, redirectAttributes);
            
            assertEquals("client/form", result);
            verify(clientService, never()).registerClient(any());
        }

        @Test
        @DisplayName("Should redirect to list on successful registration")
        void registerClient_Success_RedirectsToList() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(clientService.registerClient(any())).thenReturn(testClient);
            
            String result = clientController.registerClient(testClient, bindingResult, redirectAttributes);
            
            assertEquals("redirect:/clients", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should redirect to new form when registration fails")
        void registerClient_Exception_RedirectsToNewForm() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(clientService.registerClient(any())).thenThrow(new RuntimeException("Error"));
            
            String result = clientController.registerClient(testClient, bindingResult, redirectAttributes);
            
            assertEquals("redirect:/clients/new", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }

    @Nested
    @DisplayName("Update Operations Tests")
    class UpdateOperationsTests {

        @Test
        @DisplayName("Should show edit form")
        void showEditForm_ReturnsFormView() {
            when(clientService.getClientById(1L)).thenReturn(Optional.of(testClient));
            
            String result = clientController.showEditForm(1L, model);
            
            assertEquals("client/form", result);
            verify(model).addAttribute("client", testClient);
            verify(model).addAttribute("isEdit", true);
        }

        @Test
        @DisplayName("Should throw exception when client not found for edit")
        void showEditForm_NotFound_ThrowsException() {
            when(clientService.getClientById(99L)).thenReturn(Optional.empty());
            
            assertThrows(RuntimeException.class, () -> clientController.showEditForm(99L, model));
        }

        @Test
        @DisplayName("Should return to form when binding errors on update")
        void updateClient_WithBindingErrors_ReturnsFormView() {
            when(bindingResult.hasErrors()).thenReturn(true);
            
            String result = clientController.updateClient(1L, testClient, bindingResult, 
                    redirectAttributes, model);
            
            assertEquals("client/form", result);
            verify(model).addAttribute("isEdit", true);
        }

        @Test
        @DisplayName("Should redirect to view on successful update")
        void updateClient_Success_RedirectsToView() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(clientService.updateClientProfile(eq(1L), any())).thenReturn(testClient);
            
            String result = clientController.updateClient(1L, testClient, bindingResult, 
                    redirectAttributes, model);
            
            assertEquals("redirect:/clients/1", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should redirect to edit form when update fails")
        void updateClient_Exception_RedirectsToEditForm() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(clientService.updateClientProfile(eq(1L), any())).thenThrow(new RuntimeException("Error"));
            
            String result = clientController.updateClient(1L, testClient, bindingResult, 
                    redirectAttributes, model);
            
            assertEquals("redirect:/clients/1/edit", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }

    @Nested
    @DisplayName("Delete Operations Tests")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should delete client and redirect to list")
        void deleteClient_Success_RedirectsToList() {
            doNothing().when(clientService).deleteClient(1L);
            
            String result = clientController.deleteClient(1L, redirectAttributes);
            
            assertEquals("redirect:/clients", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
        }

        @Test
        @DisplayName("Should redirect to list with error when delete fails")
        void deleteClient_Exception_RedirectsToListWithError() {
            doThrow(new RuntimeException("Error")).when(clientService).deleteClient(1L);
            
            String result = clientController.deleteClient(1L, redirectAttributes);
            
            assertEquals("redirect:/clients", result);
            verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        }
    }
}
