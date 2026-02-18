package com.wms.controller;

import com.wms.entity.Client;
import com.wms.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * ========================================
 * CLIENT CONTROLLER
 * ========================================
 */
@Controller
@RequestMapping("/clients")  // Base URL: /clients
public class ClientController {
    
    private final ClientService clientService;
    
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }
    
    // ========================================
    // LIST OPERATIONS
    // ========================================
    
    /**
     * LIST ALL CLIENTS
     * ----------------
     * URL: GET /clients
     */
    @GetMapping
    public String listClients(Model model) {
        List<Client> clients = clientService.getAllClients();
        model.addAttribute("clients", clients);
        return "client/list";  // Returns templates/client/list.html
    }
    
    /**
     * SEARCH CLIENTS
     * --------------
     * URL: GET /clients/search?query=...
     */
    @GetMapping("/search")
    public String searchClients(@RequestParam("query") String query, Model model) {
        List<Client> clients = clientService.searchClients(query);
        model.addAttribute("clients", clients);
        model.addAttribute("searchQuery", query);
        return "client/list";
    }
    
    // ========================================
    // VIEW OPERATIONS
    // ========================================
    
    /**
     * VIEW CLIENT DETAILS
     * -------------------
     * URL: GET /clients/{id}
     * 
     * @PathVariable extracts the {id} from the URL
     */
    @GetMapping("/{id}")
    public String viewClient(@PathVariable("id") Long id, Model model) {
        Client client = clientService.getClientById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));
        
        model.addAttribute("client", client);
        return "client/view";  // Returns templates/client/view.html
    }
    
    // ========================================
    // CREATE OPERATIONS
    // ========================================
    
    /**
     * SHOW REGISTRATION FORM
     * ----------------------
     * URL: GET /clients/new
     */
    @GetMapping("/new")
    public String showRegistrationForm(Model model) {
        model.addAttribute("client", new Client());
        return "client/form";  // Returns templates/client/form.html
    }
    
    /**
     * PROCESS REGISTRATION
     * --------------------
     * URL: POST /clients
     * 
     * @Valid - Triggers validation on the Client object
     * BindingResult - Contains validation errors (must come right after @Valid)
     */
    @PostMapping
    public String registerClient(
            @Valid @ModelAttribute("client") Client client,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        // If validation errors, return to form
        if (bindingResult.hasErrors()) {
            return "client/form";
        }
        
        try {
            clientService.registerClient(client);
            redirectAttributes.addFlashAttribute("success", "Client registered successfully!");
            return "redirect:/clients";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/clients/new";
        }
    }
    
    // ========================================
    // UPDATE OPERATIONS
    // ========================================
    
    /**
     * SHOW EDIT FORM
     * --------------
     * URL: GET /clients/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Client client = clientService.getClientById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));
        
        model.addAttribute("client", client);
        model.addAttribute("isEdit", true);  // Flag to indicate edit mode
        return "client/form";
    }
    
    /**
     * PROCESS UPDATE
     * --------------
     * URL: POST /clients/{id}
     */
    @PostMapping("/{id}")
    public String updateClient(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("client") Client client,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "client/form";
        }
        
        try {
            clientService.updateClientProfile(id, client);
            redirectAttributes.addFlashAttribute("success", "Client updated successfully!");
            return "redirect:/clients/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/clients/" + id + "/edit";
        }
    }
    
    // ========================================
    // DELETE OPERATIONS
    // ========================================
    
    /**
     * DELETE CLIENT
     * -------------
     * URL: POST /clients/{id}/delete
     * 
     * Note: We use POST for delete (not DELETE) because
     * HTML forms only support GET and POST.
     */
    @PostMapping("/{id}/delete")
    public String deleteClient(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            clientService.deleteClient(id);
            redirectAttributes.addFlashAttribute("success", "Client deleted successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting client: " + e.getMessage());
        }
        
        return "redirect:/clients";
    }
}
