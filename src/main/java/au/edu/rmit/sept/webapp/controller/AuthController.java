package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;

import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling authentication-related requests
 * Handles login, registration, logout, and user management
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = (CustomUserDetailsService.CustomUserPrincipal) authentication
                    .getPrincipal();
            return userPrincipal.getUser();
        }
        return null;
    }

    /**
     * Display login page
     */
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("user", new User());

        // Preserve redirect parameter for after login
        if (redirect != null && !redirect.isEmpty()) {
            model.addAttribute("redirectUrl", redirect);
        }

        return "auth/login";
    }

    // Login processing is now handled by Spring Security
    // The CustomAuthenticationSuccessHandler will handle successful logins

    /**
     * Display registration page
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            @RequestParam String confirmPassword,
            @RequestParam String userType,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Set userType manually
        if ("organiser".equals(userType)) {
            user.setUserType(UserType.ORGANISER);
            System.out.println("Set to ORGANISER");
        } else {
            user.setUserType(UserType.STUDENT);
            System.out.println("Set to STUDENT");
        }

        System.out.println("User userType after setting: " + user.getUserType());

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        if (!user.getPasswordHash().equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match");
            return "auth/register";
        }

        try {
            User savedUser = userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! Welcome to EventHub, " + savedUser.getFirstName() + "!");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }

    /**
     * Process logout request
     */
    @PostMapping("/logout")
    public String processLogout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully");
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String Logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully");
        return "redirect:/auth/login";
    }

    /**
     * Display forgot password page
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "auth/forgot-password";
    }

    /**
     * Process forgot password request
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam String email,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<User> userOpt = userService.findByEmail(email);

            if (userOpt.isPresent()) {
                // In a real application, you would send an email with reset link
                // For now, we'll just show a success message
                redirectAttributes.addFlashAttribute("successMessage",
                        "If an account with that email exists, a password reset link has been sent.");
            } else {
                // Don't reveal if email exists or not for security
                redirectAttributes.addFlashAttribute("successMessage",
                        "If an account with that email exists, a password reset link has been sent.");
            }

            return "redirect:/auth/login";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to process request: " + e.getMessage());
            return "auth/forgot-password";
        }
    }

    /**
     * Display profile page
     */
    @GetMapping("/profile") 
    public String showProfile(Model model) {
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        // Add currentUser for the base layout navigation
        model.addAttribute("currentUser", currentUser);
        
        // Get fresh user data from database for the profile page
        Optional<User> userOpt = userService.findById(currentUser.getUserId());
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
        } else {
            return "redirect:/auth/login";
        }

        return "user/profile";
    }

    /**
     * Update user profile
     */
    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "user/profile";
        }

        try {
            // Update user information
            User updatedUser = userService.updateUser(user);

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/auth/profile";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
            return "user/profile";
        }
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        // Check password confirmation
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match");
            return "redirect:/auth/profile";
        }

        try {
            boolean success = userService.changePassword(currentUser.getUserId(), currentPassword, newPassword);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to change password: " + e.getMessage());
        }

        return "redirect:/auth/profile";
    }
}
