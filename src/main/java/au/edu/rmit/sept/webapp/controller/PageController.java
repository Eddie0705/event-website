package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.services.EventService;
import au.edu.rmit.sept.webapp.services.UserService;
import au.edu.rmit.sept.webapp.services.EventService;
import au.edu.rmit.sept.webapp.services.RSVPService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@Controller
public class PageController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private RSVPService rsvpService;

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
    @GetMapping("/")
    public String root(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getUserType() != null &&
                currentUser.getUserType().name().equalsIgnoreCase("ADMIN")) {
            return "redirect:/admin/dashboard";
        }
        // Redirect organizers to their dashboard
        if (currentUser != null && currentUser.getUserType().name().equals("ORGANISER")) {
            return "redirect:/organiser/dashboard";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);

        List<Event> events = eventService.getPublishedEvents();
        // Map<Long, String> rsvpStatusMap = new HashMap<>();

        // for (Event event : events) {
        // String status = currentUser != null ? rsvpService.checkUserRsvp(event,
        // currentUser) : "No RSVP yet";
        // rsvpStatusMap.put(event.getEventId(), status.equals("Yes") ? "yes" : "no");
        // }
        model.addAttribute("rsvpService", rsvpService);
        model.addAttribute("events", events);
        return "Index";
        // List<Event> events = eventService.getPublishedEvents(); // Make sure this
        // returns events
        // // Fetch organizer name for each event
        // Map<Long, String> organizerNames = new HashMap<>();
        // for (Event event : events) {
        // organizerNames.put(event.getEventId(),
        // userService.getUserFullNameById(event.getCreatedBy()));
        // }
        // model.addAttribute("events", events);
        // model.addAttribute("organizerNames", organizerNames);
        // return "Index"; // This will use the same template as /Index

    }

    // Your existing mappings
    @GetMapping("/explore")
    public String explore(Model model) {
        User currentUser = getCurrentUser();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);

        List<Event> events = eventService.getPublishedEvents();
        // Map<Long, String> rsvpStatusMap = new HashMap<>();

        // for (Event event : events) {
        // String status = currentUser != null ? rsvpService.checkUserRsvp(event,
        // currentUser) : "No RSVP yet";
        // rsvpStatusMap.put(event.getEventId(), status.equals("Yes") ? "yes" : "no");
        // }
        model.addAttribute("rsvpService", rsvpService);
        model.addAttribute("events", events);
        return "Index";
        // List<Event> events = eventService.getPublishedEvents(); // Make sure this
        // returns events
        // // Fetch organizer name for each event
        // Map<Long, String> organizerNames = new HashMap<>();
        // for (Event event : events) {
        // organizerNames.put(event.getEventId(),
        // userService.getUserFullNameById(event.getCreatedBy()));
        // }
        // model.addAttribute("events", events);
        // model.addAttribute("organizerNames", organizerNames);
        // return "Index"; // This will use the same template as /Index

    }


    @GetMapping("/rsvp/eventRSVP")
    public String rspv(Model model) {
        User currentUser = getCurrentUser();
        System.out.println("UserId:" + currentUser.getUserId());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);
        List<RSVP> rsvps = rsvpService.getUserRsvps(currentUser);
        model.addAttribute("rsvps", rsvps);

        model.addAttribute("yes", rsvps.stream()
                                    .filter(r -> r.getRsvpStatus() == RSVP.RSVPStatus.yes)
                                    .toList());

        model.addAttribute("cancelled", rsvps.stream()
                                    .filter(r -> r.getRsvpStatus() == RSVP.RSVPStatus.cancelled)
                                    .toList());


        model.addAttribute("maybe", rsvps.stream()
                                    .filter(r -> r.getRsvpStatus() == RSVP.RSVPStatus.maybe)
                                    .toList());

        model.addAttribute("no", rsvps.stream()
                                    .filter(r -> r.getRsvpStatus() == RSVP.RSVPStatus.no)
                                    .toList());


        
        return "rsvp/eventRSVP";
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

    @GetMapping("/logout")
    public String Logout(Model model) {
        return "redirect:/auth/logout";
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

    @Autowired
    private RSVPRepository rsvpRepository;

    // Show test page
    @GetMapping("/test")
    public String showTestPage(Model model) {
        model.addAttribute("rsvp", new RSVP());
        return "rsvp-test";
    }

    // Handle form submission
    @PostMapping("/submit")
    public String submitRsvp(@ModelAttribute RSVP rsvp, Model model) {

        rsvpRepository.save(rsvp);
        model.addAttribute("message", "RSVP saved successfully!");
        model.addAttribute("rsvp", rsvp);
        return "rsvp/rsvp-test";
    }

}