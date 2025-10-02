package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.services.EventService;
import au.edu.rmit.sept.webapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Controller for admin dashboard and administrative functions
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);
        return "admin/dashboard";
    }

    // ==================== EVENT MANAGEMENT API ENDPOINTS ====================

    /**
     * Get all events for admin management
     */
    @GetMapping("/api/events")
    @ResponseBody
    public Map<String, Object> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String search) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("error", "Unauthorized");
                return response;
            }

            List<Event> events;
            if (search != null && !search.trim().isEmpty()) {
                events = eventService.searchEvents(search);
            } else {
                events = eventService.getAllEvents();
            }

            // Apply filters
            if (filter != null && !filter.isEmpty()) {
                switch (filter) {
                    case "active":
                        events = events.stream()
                                .filter(e -> e.getStatus() == Event.EventStatus.published)
                                .collect(java.util.stream.Collectors.toList());
                        break;
                    case "flagged":
                        // For now, assume flagged means cancelled events
                        events = events.stream()
                                .filter(e -> e.getStatus() == Event.EventStatus.cancelled)
                                .collect(java.util.stream.Collectors.toList());
                        break;
                    case "upcoming":
                        events = events.stream()
                                .filter(e -> e.getEventDate().isAfter(java.time.LocalDate.now()))
                                .collect(java.util.stream.Collectors.toList());
                        break;
                }
            }

            // Enrich events with creator information
            List<Map<String, Object>> enrichedEvents = events.stream()
                    .map(event -> {
                        Map<String, Object> eventMap = new HashMap<>();
                        eventMap.put("eventId", event.getEventId());
                        eventMap.put("title", event.getTitle());
                        eventMap.put("description", event.getDescription());
                        eventMap.put("eventDate", event.getEventDate());
                        eventMap.put("startTime", event.getStartTime());
                        eventMap.put("endTime", event.getEndTime());
                        eventMap.put("location", event.getLocation());
                        eventMap.put("status", event.getStatus().name());
                        eventMap.put("createdBy", event.getCreatedBy());
                        eventMap.put("createdAt", event.getCreatedAt());
                        
                        // Get creator username
                        Optional<User> creator = userService.findById(event.getCreatedBy());
                        eventMap.put("creatorUsername", creator.map(User::getUsername).orElse("Unknown"));
                        eventMap.put("creatorName", creator.map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown User"));
                        
                        return eventMap;
                    })
                    .collect(java.util.stream.Collectors.toList());

            response.put("events", enrichedEvents);
            response.put("total", enrichedEvents.size());
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("error", "Failed to fetch events: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Get event details for admin
     */
    @GetMapping("/api/events/{eventId}")
    @ResponseBody
    public Map<String, Object> getEvent(@PathVariable Long eventId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("error", "Unauthorized");
                return response;
            }

            Event event = eventService.getEventById(eventId);
            if (event != null) {
                response.put("event", event);
                response.put("success", true);
            } else {
                response.put("error", "Event not found");
            }
            
        } catch (Exception e) {
            response.put("error", "Failed to fetch event: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Delete event (admin only)
     */
    @DeleteMapping("/api/events/{eventId}")
    @ResponseBody
    public Map<String, Object> deleteEvent(@PathVariable Long eventId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("error", "Unauthorized");
                return response;
            }

            eventService.deleteEventByAdmin(eventId);
            response.put("message", "Event deleted successfully");
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("error", "Failed to delete event: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Cancel event (admin only)
     */
    @PutMapping("/api/events/{eventId}/cancel")
    @ResponseBody
    public Map<String, Object> cancelEvent(@PathVariable Long eventId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("error", "Unauthorized");
                return response;
            }

            eventService.cancelEventByAdmin(eventId);
            response.put("message", "Event cancelled successfully");
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("error", "Failed to cancel event: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Uncancel event (admin only)
     */
    @PutMapping("/api/events/{eventId}/uncancel")
    @ResponseBody
    public Map<String, Object> uncancelEvent(@PathVariable Long eventId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("error", "Unauthorized");
                return response;
            }

            eventService.uncancelEventByAdmin(eventId);
            response.put("message", "Event restored successfully");
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("error", "Failed to restore event: " + e.getMessage());
        }
        
        return response;
    }

    // ==================== USER MANAGEMENT API ENDPOINTS ====================

    /**
     * Get all users for admin management
     */
    @GetMapping("/api/users")
    @ResponseBody
    public Map<String, Object> getAllUsers(@RequestParam(required = false) String search) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("error", "Unauthorized");
                return response;
            }

            List<User> users;
            if (search != null && !search.trim().isEmpty()) {
                users = userService.searchUsers(search);
            } else {
                users = userService.getAllUsers();
            }

            response.put("users", users);
            response.put("total", users.size());
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("error", "Failed to fetch users: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Get individual user details for admin
     */
    @GetMapping("/api/users/{userId}")
    @ResponseBody
    public Map<String, Object> getUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("error", "Unauthorized");
                return response;
            }

            Optional<User> userOptional = userService.findById(userId);
            if (userOptional.isPresent()) {
                response.put("user", userOptional.get());
                response.put("success", true);
            } else {
                response.put("error", "User not found");
            }
            
        } catch (Exception e) {
            response.put("error", "Failed to fetch user: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Ban user (admin only)
     */
    @PutMapping("/api/users/{userId}/ban")
    @ResponseBody
    public Map<String, Object> banUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("error", "Unauthorized");
                return response;
            }

            // Prevent admin from banning themselves
            if (currentUser.getUserId().equals(userId)) {
                response.put("error", "Cannot ban yourself");
                return response;
            }

            // Debug logging
            System.out.println("=== ADMIN BAN REQUEST ===");
            System.out.println("Current Admin: " + currentUser.getUsername() + " (ID: " + currentUser.getUserId() + ")");
            System.out.println("Banning User ID: " + userId);

            boolean result = userService.banUser(userId);
            System.out.println("Ban result: " + result);
            
            if (result) {
                response.put("message", "User banned successfully");
                response.put("success", true);
                
                // Verify the ban worked by fetching the user
                var bannedUser = userService.findById(userId);
                if (bannedUser.isPresent()) {
                    System.out.println("Verification - User banned status: " + bannedUser.get().getIsBanned());
                    response.put("userBanned", bannedUser.get().getIsBanned());
                }
            } else {
                response.put("error", "Failed to ban user - user not found or already banned");
                response.put("success", false);
            }
            
        } catch (Exception e) {
            System.out.println("EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Failed to ban user: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Unban user (admin only)
     */
    @PutMapping("/api/users/{userId}/unban")
    @ResponseBody
    public Map<String, Object> unbanUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("error", "Unauthorized");
                return response;
            }

            userService.unbanUser(userId);
            response.put("message", "User unbanned successfully");
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("error", "Failed to unban user: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Remove/Delete user (admin only) - PERMANENT deletion
     */
    @DeleteMapping("/api/users/{userId}")
    @ResponseBody
    public Map<String, Object> removeUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("error", "Unauthorized");
                return response;
            }

            // Prevent admin from deleting themselves
            if (currentUser.getUserId().equals(userId)) {
                response.put("error", "Cannot delete yourself");
                return response;
            }

            // Check if user exists
            Optional<User> userOptional = userService.findById(userId);
            if (!userOptional.isPresent()) {
                response.put("error", "User not found");
                return response;
            }

            User userToDelete = userOptional.get();
            
            // Delete the user (this will also delete any events they created via cascading)
            userService.deleteUser(userId);
            
            response.put("message", "User " + userToDelete.getUsername() + " has been permanently removed");
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("error", "Failed to remove user: " + e.getMessage());
        }
        
        return response;
    }

    // ==================== ANALYTICS API ENDPOINTS ====================

    /**
     * Get dashboard analytics data
     */
    @GetMapping("/api/analytics")
    @ResponseBody
    public Map<String, Object> getAnalytics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.isAdmin()) {
                response.put("error", "Unauthorized");
                return response;
            }

            Map<String, Object> analytics = new HashMap<>();
            
            // User statistics
            UserService.UserStats userStats = userService.getUserStats();
            analytics.put("users", Map.of(
                "total", userStats.getTotalUsers(),
                "active", userStats.getActiveUsers(),
                "students", userStats.getStudents(),
                "organisers", userStats.getOrganisers(),
                "admins", userStats.getAdmins()
            ));
            
            // Event statistics
            List<Event> allEvents = eventService.getAllEvents();
            long totalEvents = allEvents.size();
            long publishedEvents = allEvents.stream()
                    .filter(e -> e.getStatus() == Event.EventStatus.published)
                    .count();
            long cancelledEvents = allEvents.stream()
                    .filter(e -> e.getStatus() == Event.EventStatus.cancelled)
                    .count();
            long upcomingEvents = allEvents.stream()
                    .filter(e -> e.getEventDate().isAfter(java.time.LocalDate.now()))
                    .count();
            
            analytics.put("events", Map.of(
                "total", totalEvents,
                "published", publishedEvents,
                "cancelled", cancelledEvents,
                "upcoming", upcomingEvents
            ));
            
            // Calculate fill rates for recent events
            List<Event> recentEvents = allEvents.stream()
                    .filter(e -> e.getStatus() == Event.EventStatus.published)
                    .filter(e -> e.getEventDate().isAfter(java.time.LocalDate.now().minusDays(30)))
                    .collect(java.util.stream.Collectors.toList());
            
            double avgFillRate = recentEvents.stream()
                    .filter(e -> e.getMaxCapacity() > 0)
                    .mapToDouble(e -> (double) e.getCurrentRsvpCount() / e.getMaxCapacity() * 100)
                    .average()
                    .orElse(0.0);
            
            analytics.put("avgFillRate", Math.round(avgFillRate * 10.0) / 10.0);
            
            response.put("analytics", analytics);
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("error", "Failed to fetch analytics: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Resolve the current authenticated user from Spring Security context.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal principal =
                    (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            return principal.getUser();
        }
        return null;
    }
}
