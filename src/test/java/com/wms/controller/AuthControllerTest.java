package com.wms.controller;

import com.wms.entity.User;
import com.wms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AuthController authController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
    }

    @Nested
    @DisplayName("Login Page Tests")
    class LoginPageTests {

        @Test
        @DisplayName("Should show login page without messages")
        void showLoginPage_NoParams_ReturnsLoginView() {
            String result = authController.showLoginPage(null, null, model);
            
            assertEquals("auth/login", result);
            verify(model, never()).addAttribute(eq("error"), any());
            verify(model, never()).addAttribute(eq("message"), any());
        }

        @Test
        @DisplayName("Should show login page with error message")
        void showLoginPage_WithError_AddsErrorMessage() {
            String result = authController.showLoginPage("true", null, model);
            
            assertEquals("auth/login", result);
            verify(model).addAttribute("error", "Invalid username or password!");
        }

        @Test
        @DisplayName("Should show login page with logout message")
        void showLoginPage_WithLogout_AddsLogoutMessage() {
            String result = authController.showLoginPage(null, "true", model);
            
            assertEquals("auth/login", result);
            verify(model).addAttribute("message", "You have been logged out successfully.");
        }

        @Test
        @DisplayName("Should show login page with both error and logout")
        void showLoginPage_WithBoth_AddsBothMessages() {
            String result = authController.showLoginPage("true", "true", model);
            
            assertEquals("auth/login", result);
            verify(model).addAttribute("error", "Invalid username or password!");
            verify(model).addAttribute("message", "You have been logged out successfully.");
        }
    }

    @Nested
    @DisplayName("Registration Page Tests")
    class RegistrationPageTests {

        @Test
        @DisplayName("Should show registration page")
        void showRegistrationPage_ReturnsRegisterView() {
            String result = authController.showRegistrationPage(model);
            
            assertEquals("auth/register", result);
            verify(model).addAttribute(eq("user"), any(User.class));
        }
    }

    @Nested
    @DisplayName("Registration Process Tests")
    class RegistrationProcessTests {

        @Test
        @DisplayName("Should return to form when binding errors exist")
        void registerUser_WithBindingErrors_ReturnsRegisterView() {
            when(bindingResult.hasErrors()).thenReturn(true);
            
            String result = authController.registerUser(testUser, bindingResult, "password123", 
                    redirectAttributes, model);
            
            assertEquals("auth/register", result);
            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("Should return to form when passwords don't match")
        void registerUser_PasswordMismatch_ReturnsRegisterView() {
            when(bindingResult.hasErrors()).thenReturn(false);
            
            String result = authController.registerUser(testUser, bindingResult, "differentPassword", 
                    redirectAttributes, model);
            
            assertEquals("auth/register", result);
            verify(model).addAttribute("passwordError", "Passwords do not match!");
            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("Should return to form when username exists")
        void registerUser_UsernameExists_ReturnsRegisterView() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(userService.usernameExists("testuser")).thenReturn(true);
            
            String result = authController.registerUser(testUser, bindingResult, "password123", 
                    redirectAttributes, model);
            
            assertEquals("auth/register", result);
            verify(model).addAttribute("usernameError", "Username already exists!");
            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("Should return to form when email exists")
        void registerUser_EmailExists_ReturnsRegisterView() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(userService.usernameExists("testuser")).thenReturn(false);
            when(userService.emailExists("test@example.com")).thenReturn(true);
            
            String result = authController.registerUser(testUser, bindingResult, "password123", 
                    redirectAttributes, model);
            
            assertEquals("auth/register", result);
            verify(model).addAttribute("emailError", "Email already registered!");
            verify(userService, never()).registerUser(any());
        }

        @Test
        @DisplayName("Should redirect to login on successful registration")
        void registerUser_Success_RedirectsToLogin() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(userService.usernameExists("testuser")).thenReturn(false);
            when(userService.emailExists("test@example.com")).thenReturn(false);
            when(userService.registerUser(any())).thenReturn(testUser);
            
            String result = authController.registerUser(testUser, bindingResult, "password123", 
                    redirectAttributes, model);
            
            assertEquals("redirect:/login", result);
            verify(redirectAttributes).addFlashAttribute(eq("success"), any(String.class));
            verify(userService).registerUser(testUser);
        }

        @Test
        @DisplayName("Should return to form when registration throws exception")
        void registerUser_Exception_ReturnsRegisterView() {
            when(bindingResult.hasErrors()).thenReturn(false);
            when(userService.usernameExists("testuser")).thenReturn(false);
            when(userService.emailExists("test@example.com")).thenReturn(false);
            when(userService.registerUser(any())).thenThrow(new RuntimeException("Registration failed"));
            
            String result = authController.registerUser(testUser, bindingResult, "password123", 
                    redirectAttributes, model);
            
            assertEquals("auth/register", result);
            verify(model).addAttribute(eq("error"), contains("Registration failed"));
        }
    }

    @Nested
    @DisplayName("Home Page Tests")
    class HomePageTests {

        @Test
        @DisplayName("Should return home view")
        void homePage_ReturnsHomeView() {
            String result = authController.homePage();
            
            assertEquals("home", result);
        }
    }
}
