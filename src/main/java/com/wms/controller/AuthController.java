package com.wms.controller;

import com.wms.entity.User;
import com.wms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ========================================
 * AUTHENTICATION CONTROLLER
 * ========================================
 */
@Controller
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * SHOW LOGIN PAGE
     * ---------------
     * Displays the login form.
     * 
     * @param error Optional parameter indicating login failure
     * @param logout Optional parameter indicating successful logout
     * @param model The model to pass data to the view
     * @return The name of the login template
     */
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        
        // Add error message if login failed
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        
        // Add success message if user just logged out
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        
        return "auth/login";  // Returns templates/auth/login.html
    }
    
    /**
     * SHOW REGISTRATION PAGE
     * ----------------------
     * Displays the registration form.
     */
    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        // Create empty User object for the form to bind to
        model.addAttribute("user", new User());
        return "auth/register";  // Returns templates/auth/register.html
    }
    
    /**
     * PROCESS REGISTRATION
     * --------------------
     * Handles the registration form submission.
     * 
     * @Valid triggers validation based on entity annotations
     * BindingResult contains validation errors
     */
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        
        // Check if passwords match
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("passwordError", "Passwords do not match!");
            return "auth/register";
        }
        
        // Check if username already exists
        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("usernameError", "Username already exists!");
            return "auth/register";
        }
        
        // Check if email already exists
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("emailError", "Email already registered!");
            return "auth/register";
        }
        
        try {
            // Register the user
            userService.registerUser(user);
            
            // Add success message and redirect to login
            redirectAttributes.addFlashAttribute("success", 
                "Registration successful! Please login with your credentials.");
            return "redirect:/login";
            
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }
    
    /**
     * HOME PAGE
     * ---------
     * The landing page of the application.
     */
    @GetMapping("/")
    public String homePage() {
        return "home";  // Returns templates/home.html
    }
}
