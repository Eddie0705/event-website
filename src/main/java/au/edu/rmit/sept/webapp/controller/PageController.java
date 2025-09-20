package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.User;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.Event.EventStatus;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.services.EventService;
import au.edu.rmit.sept.webapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class PageController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserService userService;
    @Autowired
    private EventService eventService;

    public PageController() {
        System.out.println("PageController created!");
    }

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

    // Your existing mappings
    @GetMapping({ "/", "/explore" })
    public String index(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);

        // if (eventService == null) {
        // System.out.println("eventService is null!");
        // } else {
        // System.out.println("eventService is ready");
        // }

        // try {
        // List<Event> events = eventService.getPublishedEvents();
        // System.out.println("Loaded events: " + events.size());
        // model.addAttribute("events", events);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        return "Index";
    }

    @GetMapping("/calendar")
    public String calendar(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);
        return "Calendar";
    }

    @GetMapping("/rspv")
    public String rspv(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);
        return "Rspv";
    }

    @GetMapping("/noti")
    public String noti(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);
        return "Notification";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("user", currentUser);
        model.addAttribute("isAuthenticated", true);
        return "user/profile";
    }

    @GetMapping("/myevents")
    public String myEvents(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);
        return "myevents";
    }

    // Database test endpoint
    @GetMapping("/db-test")
    @ResponseBody
    public Map<String, Object> testDatabase() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            response.put("status", "SUCCESS");
            response.put("database", connection.getCatalog());
            response.put("message", "Database connection working!");
            return response;
        } catch (Exception e) {

            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return response;
        }
    }

    // Test endpoint to check authentication
    @GetMapping("/auth-test")
    @ResponseBody
    public Map<String, Object> testAuth() {
        Map<String, Object> response = new HashMap<>();
        User currentUser = getCurrentUser();

        if (currentUser != null) {
            response.put("authenticated", true);
            response.put("userId", currentUser.getUserId());
            response.put("email", currentUser.getEmail());
            response.put("userType", currentUser.getUserType().name());
            response.put("lastLogin", currentUser.getLastLogin());
        } else {
            response.put("authenticated", false);
        }

        return response;
    }
}