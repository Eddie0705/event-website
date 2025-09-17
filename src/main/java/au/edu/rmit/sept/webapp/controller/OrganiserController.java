package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.utils.SessionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for organiser dashboard and organiser-specific functions
 */
@Controller
@RequestMapping("/organiser")
public class OrganiserController {

    /**
     * Display organiser dashboard
     */
    @GetMapping("/dashboard")
    public String organiserDashboard(HttpSession session, Model model) {
        // Check if user is organiser or admin
        if (!SessionUtils.canAccessOrganiser(session)) {
            return "redirect:/auth/login";
        }
        
        User currentUser = SessionUtils.getCurrentUser(session);
        
        model.addAttribute("currentUser", currentUser);
        
        return "organiser/dashboard";
    }

    /**
     * Display event management page
     */
    @GetMapping("/events")
    public String eventManagement(HttpSession session, Model model) {
        // Check if user is organiser or admin
        if (!SessionUtils.canAccessOrganiser(session)) {
            return "redirect:/auth/login";
        }
        
        User currentUser = SessionUtils.getCurrentUser(session);
        
        model.addAttribute("currentUser", currentUser);
        
        return "organiser/events";
    }
}
