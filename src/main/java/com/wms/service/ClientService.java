package com.wms.service;

import com.wms.entity.Client;
import com.wms.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ========================================
 * CLIENT SERVICE
 * ========================================
 */
@Service
@Transactional  // All methods in this class run in a database transaction
public class ClientService {
    
    private final ClientRepository clientRepository;
    
    /**
     * CONSTRUCTOR INJECTION
     * ---------------------
     */
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }
    
    // ========================================
    // CREATE OPERATIONS
    // ========================================
    
    /**
     * REGISTER NEW CLIENT
     * -------------------
     * Creates a new client in the system.
     */
    public Client registerClient(Client client) {
        // Validate email uniqueness
        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new RuntimeException("Email already registered: " + client.getEmail());
        }
        
        // Validate phone uniqueness
        if (clientRepository.existsByPhone(client.getPhone())) {
            throw new RuntimeException("Phone number already registered: " + client.getPhone());
        }
        
        // Save and return the client
        return clientRepository.save(client);
    }
    
    // ========================================
    // READ OPERATIONS
    // ========================================
    
    /**
     * GET CLIENT BY ID
     * ----------------
     * Returns Optional because client might not exist.
     */
    @Transactional(readOnly = true)  // Read-only for better performance
    public Optional<Client> getClientById(Long clientId) {
        return clientRepository.findById(Objects.requireNonNull(clientId));
    }
    
    /**
     * GET CLIENT BY EMAIL
     * -------------------
     */
    @Transactional(readOnly = true)
    public Optional<Client> getClientByEmail(String email) {
        return clientRepository.findByEmail(email);
    }
    
    /**
     * GET ALL CLIENTS
     * ---------------
     */
    @Transactional(readOnly = true)
    public List<Client> getAllClients() {
        return clientRepository.findAllByOrderByNameAsc();
    }
    
    /**
     * SEARCH CLIENTS BY NAME
     * ----------------------
     */
    @Transactional(readOnly = true)
    public List<Client> searchClientsByName(String name) {
        return clientRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * SEARCH CLIENTS (GENERAL)
     * ------------------------
     */
    @Transactional(readOnly = true)
    public List<Client> searchClients(String searchTerm) {
        return clientRepository.searchClients(searchTerm);
    }
    
    // ========================================
    // UPDATE OPERATIONS
    // ========================================
    
    /**
     * UPDATE CLIENT PROFILE
     * ---------------------
     * Updates an existing client's information.
     */
    public Client updateClientProfile(Long clientId, Client updatedClient) {
        // Find existing client
        Client existingClient = clientRepository.findById(Objects.requireNonNull(clientId))
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        
        // Check if email is being changed and if new email is already taken
        if (!existingClient.getEmail().equals(updatedClient.getEmail()) 
                && clientRepository.existsByEmail(updatedClient.getEmail())) {
            throw new RuntimeException("Email already registered: " + updatedClient.getEmail());
        }
        
        // Check if phone is being changed and if new phone is already taken
        if (!existingClient.getPhone().equals(updatedClient.getPhone()) 
                && clientRepository.existsByPhone(updatedClient.getPhone())) {
            throw new RuntimeException("Phone number already registered: " + updatedClient.getPhone());
        }
        
        // Update fields
        existingClient.setName(updatedClient.getName());
        existingClient.setEmail(updatedClient.getEmail());
        existingClient.setPhone(updatedClient.getPhone());
        existingClient.setAddress(updatedClient.getAddress());
        existingClient.setDateOfBirth(updatedClient.getDateOfBirth());
        
        // Save and return
        return clientRepository.save(existingClient);
    }
    
    // ========================================
    // DELETE OPERATIONS
    // ========================================
    
    /**
     * DELETE CLIENT
     * -------------
     * Removes a client from the system.
     */
    public void deleteClient(Long clientId) {
        if (!clientRepository.existsById(Objects.requireNonNull(clientId))) {
            throw new RuntimeException("Client not found with ID: " + clientId);
        }
        clientRepository.deleteById(clientId);
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * CHECK IF EMAIL EXISTS
     * ---------------------
     */
    @Transactional(readOnly = true)
    public boolean isEmailRegistered(String email) {
        return clientRepository.existsByEmail(email);
    }
    
    /**
     * GET CLIENT COUNT
     * ----------------
     */
    @Transactional(readOnly = true)
    public long getClientCount() {
        return clientRepository.count();
    }
}
