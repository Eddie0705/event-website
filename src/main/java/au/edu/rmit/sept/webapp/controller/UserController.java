package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import au.edu.rmit.sept.webapp.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for user management operations
 * Handles user listing, searching, and administrative functions
 */
@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Display all users (admin only)
     */
    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String userType,
            HttpSession session,
            Model model) {

        // Check if user is admin
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/auth/login";
        }

        List<User> users;

        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search);
        } else if (userType != null && !userType.isEmpty()) {
            try {
                UserType type = UserType.fromValue(userType);
                users = userService.getUsersByType(type);
            } catch (IllegalArgumentException e) {
                users = userService.getAllUsers();
            }
        } else {
            users = userService.getAllUsers();
        }

        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("userType", userType);
        model.addAttribute("userStats", userService.getUserStats());

        return "admin/users";
    }

    /**
     * Display user details
     */
    @GetMapping("/{userId}")
    public String viewUser(@PathVariable Long userId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        // Users can only view their own profile unless they're admin
        if (!currentUser.isAdmin() && !currentUser.getUserId().equals(userId)) {
            return "redirect:/auth/profile";
        }

        Optional<User> userOpt = userService.findById(userId);

        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            model.addAttribute("isOwnProfile", currentUser.getUserId().equals(userId));
            return "user/user-details";
        } else {
            return "redirect:/users";
        }
    }

    /**
     * Activate user (admin only)
     */
    @PostMapping("/{userId}/activate")
    public String activateUser(@PathVariable Long userId, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/auth/login";
        }

        boolean success = userService.activateUser(userId);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "User activated successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to activate user");
        }

        return "redirect:/users";
    }

    /**
     * Deactivate user (admin only)
     */
    @PostMapping("/{userId}/deactivate")
    public String deactivateUser(@PathVariable Long userId, HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/auth/login";
        }

        boolean success = userService.deactivateUser(userId);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "User deactivated successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to deactivate user");
        }

        return "redirect:/users";
    }

    /**
     * Ban user (admin only)
     */
    @PostMapping("/{userId}/ban")
    public String banUser(@PathVariable Long userId, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/auth/login";
        }

        // Prevent admin from banning themselves
        if (currentUser.getUserId().equals(userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot ban yourself");
            return "redirect:/users";
        }

        boolean success = userService.banUser(userId);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "User banned successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to ban user");
        }

        return "redirect:/users";
    }

    /**
     * Unban user (admin only)
     */
    @PostMapping("/{userId}/unban")
    public String unbanUser(@PathVariable Long userId, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/auth/login";
        }

        boolean success = userService.unbanUser(userId);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "User unbanned successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to unban user");
        }

        return "redirect:/users";
    }

    /**
     * Reset user password (admin only)
     */
    @PostMapping("/{userId}/reset-password")
    public String resetPassword(
            @PathVariable Long userId,
            @RequestParam String newPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/auth/login";
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password cannot be empty");
            return "redirect:/users/" + userId;
        }

        boolean success = userService.resetPassword(userId, newPassword);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Password reset successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reset password");
        }

        return "redirect:/users/" + userId;
    }

    /**
     * API endpoint to get user statistics
     */
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> getUserStats(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isAdmin()) {
            response.put("error", "Unauthorized");
            return response;
        }

        UserService.UserStats stats = userService.getUserStats();
        response.put("totalUsers", stats.getTotalUsers());
        response.put("activeUsers", stats.getActiveUsers());
        response.put("verifiedUsers", stats.getVerifiedUsers());
        response.put("students", stats.getStudents());
        response.put("organisers", stats.getOrganisers());
        response.put("admins", stats.getAdmins());

        return response;
    }

    /**
     * API endpoint to search users
     */
    @GetMapping("/search")
    @ResponseBody
    public Map<String, Object> searchUsers(@RequestParam String term, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isAdmin()) {
            response.put("error", "Unauthorized");
            return response;
        }

        List<User> users = userService.searchUsers(term);
        response.put("users", users);
        response.put("count", users.size());

        return response;
    }

    /**
     * API endpoint to get users by type
     */
    @GetMapping("/by-type")
    @ResponseBody
    public Map<String, Object> getUsersByType(@RequestParam String userType, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isAdmin()) {
            response.put("error", "Unauthorized");
            return response;
        }

        try {
            UserType type = UserType.fromValue(userType);
            List<User> users = userService.getUsersByType(type);
            response.put("users", users);
            response.put("count", users.size());
        } catch (IllegalArgumentException e) {
            response.put("error", "Invalid user type");
        }

        return response;
    }
}
