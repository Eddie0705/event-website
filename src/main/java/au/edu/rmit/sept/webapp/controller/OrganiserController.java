package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.services.EventService;
import au.edu.rmit.sept.webapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/organiser")
public class OrganiserController {
    @Autowired
    private EventService eventService;


    @Autowired
    private EventService eventService;

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
     * Display organizer dashboard with all events and statistics
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model, 
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(defaultValue = "all") String filter) {
        User currentUser = getCurrentUser();

        if (currentUser == null || currentUser.getUserType() != UserType.ORGANISER) {
            return "redirect:/auth/login";
        }

        // Get event statistics
        EventService.EventStatistics stats = eventService.getEventStatistics(currentUser.getUserId());
        model.addAttribute("stats", stats);

        // Get events with pagination
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage = null;

        // Filter events based on status
        switch (filter.toLowerCase()) {
            case "published":
                eventsPage = eventService.getEventsByOrganiser(currentUser.getUserId(), pageable);
                // Filter only published events
                List<Event> publishedEvents = eventsPage.getContent().stream()
                    .filter(event -> event.getStatus() == Event.EventStatus.PUBLISHED)
                    .toList();
                model.addAttribute("events", publishedEvents);
                break;
            case "draft":
                eventsPage = eventService.getEventsByOrganiser(currentUser.getUserId(), pageable);
                // Filter only draft events
                List<Event> draftEvents = eventsPage.getContent().stream()
                    .filter(event -> event.getStatus() == Event.EventStatus.DRAFT)
                    .toList();
                model.addAttribute("events", draftEvents);
                break;
            case "upcoming":
                List<Event> upcomingEvents = eventService.getEventsByOrganiser(currentUser.getUserId())
                    .stream()
                    .filter(event -> event.getEventDate().isAfter(java.time.LocalDate.now()) && 
                                   event.getStatus() == Event.EventStatus.PUBLISHED)
                    .toList();
                model.addAttribute("events", upcomingEvents);
                break;
            case "past":
                List<Event> pastEvents = eventService.getEventsByOrganiser(currentUser.getUserId())
                    .stream()
                    .filter(event -> event.getEventDate().isBefore(java.time.LocalDate.now()) || 
                                   event.getStatus() == Event.EventStatus.COMPLETED)
                    .toList();
                model.addAttribute("events", pastEvents);
                break;
            default:
                // All events
                eventsPage = eventService.getEventsByOrganiser(currentUser.getUserId(), pageable);
                model.addAttribute("events", eventsPage.getContent());
                break;
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventsPage != null ? eventsPage.getTotalPages() : 1);
        model.addAttribute("totalElements", eventsPage != null ? eventsPage.getTotalElements() : 0);

        return "organiser/dashboard";
    }

    @GetMapping("/create-event")
    public String showCreateEventForm(Model model) {
        User currentUser = getCurrentUser();

        if (currentUser == null || currentUser.getUserType() != UserType.ORGANISER) {
            return "redirect:/auth/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);
        return "organiser/create-event";
    }

    /**
     * Publish an event
     */
    @PostMapping("/events/{id}/publish")
    public String publishEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getUserType() != UserType.ORGANISER) {
            return "redirect:/auth/login";
        }

        try {
            eventService.publishEvent(id, currentUser.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Event published successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to publish event: " + e.getMessage());
        }

        return "redirect:/organiser/dashboard";
    }

    /**
     * Cancel an event
     */
    @PostMapping("/events/{id}/cancel")
    public String cancelEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getUserType() != UserType.ORGANISER) {
            return "redirect:/auth/login";
        }

        try {
            eventService.cancelEvent(id, currentUser.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Event cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel event: " + e.getMessage());
        }

        return "redirect:/organiser/dashboard";
    }
}