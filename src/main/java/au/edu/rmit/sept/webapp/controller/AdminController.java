package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.services.UserService;
import au.edu.rmit.sept.webapp.utils.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for admin dashboard and administrative functions
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    /**
     * Display admin dashboard
     */
    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        // Check if user is admin
        if (!SessionUtils.isAdmin(session)) {
            return "redirect:/auth/login";
        }

        User currentUser = SessionUtils.getCurrentUser(session);
        UserService.UserStats userStats = userService.getUserStats();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userStats", userStats);

        return "admin/dashboard";
    }

    /**
     * Display user management page
     */
    @GetMapping("/users")
    public String userManagement(HttpSession session, Model model) {
        // Check if user is admin
        if (!SessionUtils.isAdmin(session)) {
            return "redirect:/auth/login";
        }

        User currentUser = SessionUtils.getCurrentUser(session);
        UserService.UserStats userStats = userService.getUserStats();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userStats", userStats);

        return "admin/users";
    }
}
