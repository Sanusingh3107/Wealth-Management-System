package com.wms.service;

import com.wms.entity.Client;
import com.wms.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * Unit tests for ClientService
 * Achieves 100% code coverage for all ClientService methods
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setClientId(1L);
        testClient.setName("John Doe");
        testClient.setEmail("john.doe@example.com");
        testClient.setPhone("1234567890");
        testClient.setAddress("123 Main St");
        testClient.setDateOfBirth(LocalDate.of(1990, 1, 15));
    }

    // ========================================
    // CREATE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Register Client Tests")
    class RegisterClientTests {

        @Test
        @DisplayName("Should register client successfully when email and phone are unique")
        void registerClient_Success() {
            when(clientRepository.existsByEmail(anyString())).thenReturn(false);
            when(clientRepository.existsByPhone(anyString())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            Client result = clientService.registerClient(testClient);

            assertNotNull(result);
            assertEquals("John Doe", result.getName());
            verify(clientRepository).existsByEmail("john.doe@example.com");
            verify(clientRepository).existsByPhone("1234567890");
            verify(clientRepository).save(testClient);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void registerClient_EmailExists_ThrowsException() {
            when(clientRepository.existsByEmail(anyString())).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> clientService.registerClient(testClient));

            assertEquals("Email already registered: john.doe@example.com", exception.getMessage());
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Should throw exception when phone already exists")
        void registerClient_PhoneExists_ThrowsException() {
            when(clientRepository.existsByEmail(anyString())).thenReturn(false);
            when(clientRepository.existsByPhone(anyString())).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> clientService.registerClient(testClient));

            assertEquals("Phone number already registered: 1234567890", exception.getMessage());
            verify(clientRepository, never()).save(any(Client.class));
        }
    }

    // ========================================
    // READ OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Read Client Tests")
    class ReadClientTests {

        @Test
        @DisplayName("Should return client by ID when exists")
        void getClientById_Found() {
            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));

            Optional<Client> result = clientService.getClientById(1L);

            assertTrue(result.isPresent());
            assertEquals("John Doe", result.get().getName());
        }

        @Test
        @DisplayName("Should return empty when client not found by ID")
        void getClientById_NotFound() {
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Client> result = clientService.getClientById(999L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return client by email when exists")
        void getClientByEmail_Found() {
            when(clientRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testClient));

            Optional<Client> result = clientService.getClientByEmail("john.doe@example.com");

            assertTrue(result.isPresent());
            assertEquals("John Doe", result.get().getName());
        }

        @Test
        @DisplayName("Should return empty when client not found by email")
        void getClientByEmail_NotFound() {
            when(clientRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            Optional<Client> result = clientService.getClientByEmail("unknown@example.com");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return all clients ordered by name")
        void getAllClients_ReturnsList() {
            Client client2 = new Client();
            client2.setClientId(2L);
            client2.setName("Jane Smith");

            when(clientRepository.findAllByOrderByNameAsc()).thenReturn(Arrays.asList(testClient, client2));

            List<Client> result = clientService.getAllClients();

            assertEquals(2, result.size());
            verify(clientRepository).findAllByOrderByNameAsc();
        }

        @Test
        @DisplayName("Should search clients by name")
        void searchClientsByName_ReturnsMatches() {
            when(clientRepository.findByNameContainingIgnoreCase("John")).thenReturn(Arrays.asList(testClient));

            List<Client> result = clientService.searchClientsByName("John");

            assertEquals(1, result.size());
            assertEquals("John Doe", result.get(0).getName());
        }

        @Test
        @DisplayName("Should search clients by search term")
        void searchClients_ReturnsMatches() {
            when(clientRepository.searchClients("john")).thenReturn(Arrays.asList(testClient));

            List<Client> result = clientService.searchClients("john");

            assertEquals(1, result.size());
        }
    }

    // ========================================
    // UPDATE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Update Client Tests")
    class UpdateClientTests {

        @Test
        @DisplayName("Should update client successfully with same email and phone")
        void updateClientProfile_SameEmailPhone_Success() {
            Client updatedClient = new Client();
            updatedClient.setName("John Updated");
            updatedClient.setEmail("john.doe@example.com"); // Same email
            updatedClient.setPhone("1234567890"); // Same phone
            updatedClient.setAddress("456 New St");
            updatedClient.setDateOfBirth(LocalDate.of(1990, 1, 15));

            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            Client result = clientService.updateClientProfile(1L, updatedClient);

            assertNotNull(result);
            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("Should update client with new unique email")
        void updateClientProfile_NewUniqueEmail_Success() {
            Client updatedClient = new Client();
            updatedClient.setName("John Updated");
            updatedClient.setEmail("john.new@example.com"); // New email
            updatedClient.setPhone("1234567890");
            updatedClient.setAddress("456 New St");
            updatedClient.setDateOfBirth(LocalDate.of(1990, 1, 15));

            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(clientRepository.existsByEmail("john.new@example.com")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            Client result = clientService.updateClientProfile(1L, updatedClient);

            assertNotNull(result);
            verify(clientRepository).existsByEmail("john.new@example.com");
        }

        @Test
        @DisplayName("Should throw exception when updating to existing email")
        void updateClientProfile_EmailExists_ThrowsException() {
            Client updatedClient = new Client();
            updatedClient.setName("John Updated");
            updatedClient.setEmail("existing@example.com"); // Already exists
            updatedClient.setPhone("1234567890");

            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(clientRepository.existsByEmail("existing@example.com")).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientService.updateClientProfile(1L, updatedClient));

            assertEquals("Email already registered: existing@example.com", exception.getMessage());
        }

        @Test
        @DisplayName("Should update client with new unique phone")
        void updateClientProfile_NewUniquePhone_Success() {
            Client updatedClient = new Client();
            updatedClient.setName("John Updated");
            updatedClient.setEmail("john.doe@example.com");
            updatedClient.setPhone("9876543210"); // New phone
            updatedClient.setAddress("456 New St");
            updatedClient.setDateOfBirth(LocalDate.of(1990, 1, 15));

            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(clientRepository.existsByPhone("9876543210")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            Client result = clientService.updateClientProfile(1L, updatedClient);

            assertNotNull(result);
            verify(clientRepository).existsByPhone("9876543210");
        }

        @Test
        @DisplayName("Should throw exception when updating to existing phone")
        void updateClientProfile_PhoneExists_ThrowsException() {
            Client updatedClient = new Client();
            updatedClient.setName("John Updated");
            updatedClient.setEmail("john.doe@example.com");
            updatedClient.setPhone("5555555555"); // Already exists

            when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(clientRepository.existsByPhone("5555555555")).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientService.updateClientProfile(1L, updatedClient));

            assertEquals("Phone number already registered: 5555555555", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when client not found for update")
        void updateClientProfile_ClientNotFound_ThrowsException() {
            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientService.updateClientProfile(999L, testClient));

            assertEquals("Client not found with ID: 999", exception.getMessage());
        }
    }

    // ========================================
    // DELETE OPERATIONS TESTS
    // ========================================

    @Nested
    @DisplayName("Delete Client Tests")
    class DeleteClientTests {

        @Test
        @DisplayName("Should delete client successfully when exists")
        void deleteClient_Success() {
            when(clientRepository.existsById(1L)).thenReturn(true);
            doNothing().when(clientRepository).deleteById(1L);

            assertDoesNotThrow(() -> clientService.deleteClient(1L));

            verify(clientRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent client")
        void deleteClient_NotFound_ThrowsException() {
            when(clientRepository.existsById(999L)).thenReturn(false);

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientService.deleteClient(999L));

            assertEquals("Client not found with ID: 999", exception.getMessage());
            verify(clientRepository, never()).deleteById(anyLong());
        }
    }

    // ========================================
    // UTILITY METHODS TESTS
    // ========================================

    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityMethodsTests {

        @Test
        @DisplayName("Should return true when email is registered")
        void isEmailRegistered_True() {
            when(clientRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

            assertTrue(clientService.isEmailRegistered("john.doe@example.com"));
        }

        @Test
        @DisplayName("Should return false when email is not registered")
        void isEmailRegistered_False() {
            when(clientRepository.existsByEmail("unknown@example.com")).thenReturn(false);

            assertFalse(clientService.isEmailRegistered("unknown@example.com"));
        }

        @Test
        @DisplayName("Should return client count")
        void getClientCount_ReturnsCount() {
            when(clientRepository.count()).thenReturn(10L);

            assertEquals(10L, clientService.getClientCount());
        }
    }
}
