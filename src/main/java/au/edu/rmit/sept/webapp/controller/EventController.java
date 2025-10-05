package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import au.edu.rmit.sept.webapp.services.EventService;
import au.edu.rmit.sept.webapp.services.RSVPService;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;
    @Autowired
    private RSVPService rsvpService;

    /**
     * Get current authenticated user from Spring Security
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

    @PostMapping("/create")
    public String createEvent(@RequestParam String title,
            @RequestParam String description,
            @RequestParam String eventDate,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String location,
            @RequestParam Integer maxCapacity,
            @RequestParam(required = false) String venueDetails,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getUserType() != UserType.ORGANISER) {
            return "redirect:/login";
        }

        try {
            LocalDate date = LocalDate.parse(eventDate);
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);

            if (date.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Event date must be in the future");
                return "redirect:/organiser/create-event";
            }

            if (!end.isAfter(start)) {
                redirectAttributes.addFlashAttribute("errorMessage", "End time must be after start time");
                return "redirect:/organiser/create-event";
            }

            if (maxCapacity <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Maximum capacity must be greater than 0");
                return "redirect:/organiser/create-event";
            }

            Event event = new Event();
            event.setTitle(title);
            event.setDescription(description);
            event.setEventDate(date);
            event.setStartTime(start);
            event.setEndTime(end);
            event.setLocation(location);
            event.setMaxCapacity(maxCapacity);
            event.setCurrentRsvpCount(0);

            if (venueDetails != null && !venueDetails.trim().isEmpty()) {
                event.setVenueDetails(venueDetails);
            }

            event.setCreatedBy(currentUser.getUserId());
            event.setClubId(1L); // Set to default club ID of 1
            event.setStatus(Event.EventStatus.published);

            event.setIsPaid(false);
            event.setIsFeatured(false);

            eventService.createEvent(event);

            redirectAttributes.addFlashAttribute("successMessage", "Event '" + title + "' created successfully!");
            return "redirect:/";

        } catch (Exception e) {
            System.out.println("Error creating event: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating event: " + e.getMessage());
            return "redirect:/organiser/create-event";
        }
    }

    @GetMapping("/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        try {
            Event event = eventService.getEventById(id);
            model.addAttribute("event", event);
            return "events/view";
        } catch (RuntimeException e) {
            return "redirect:/events";
        }
    }

    @GetMapping("/{id}/edit")
    public String editEvent(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getUserType() != UserType.ORGANISER) {
            return "redirect:/login";
        }

        try {
            Event event = eventService.getEventById(id);

            if (!event.getCreatedBy().equals(currentUser.getUserId())) {
                return "redirect:/organiser/dashboard";
            }

            model.addAttribute("event", event);
            return "events/edit";
        } catch (RuntimeException e) {
            return "redirect:/organiser/dashboard";
        }
    }

    @PostMapping("/{id}/update")
    public String updateEvent(@PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String eventDate,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String location,
            @RequestParam Integer maxCapacity,
            @RequestParam(required = false) String venueDetails,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getUserType() != UserType.ORGANISER) {
            return "redirect:/login";
        }

        try {
            Event event = eventService.getEventById(id);

            if (!event.getCreatedBy().equals(currentUser.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own events");
                return "redirect:/organiser/dashboard";
            }

            Event updatedEvent = new Event();
            updatedEvent.setTitle(title);
            updatedEvent.setDescription(description);
            updatedEvent.setEventDate(LocalDate.parse(eventDate));
            updatedEvent.setStartTime(LocalTime.parse(startTime));
            updatedEvent.setEndTime(LocalTime.parse(endTime));
            updatedEvent.setLocation(location);
            updatedEvent.setMaxCapacity(maxCapacity);
            updatedEvent.setVenueDetails(venueDetails);

            eventService.updateEvent(id, updatedEvent);

            redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
            return "redirect:/organiser/dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating event: " + e.getMessage());
            return "redirect:/events/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getUserType() != UserType.ORGANISER) {
            return "redirect:/login";
        }

        try {
            eventService.deleteEvent(id, currentUser.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully!");
            return "redirect:/organiser/dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting event: " + e.getMessage());
            return "redirect:/organiser/dashboard";
        }
    }

    @GetMapping("/search")
    public String searchEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String location,
            Model model) {

        search = (search != null && search.isBlank()) ? null : search;
        category = (category != null && category.isBlank()) ? null : category;
        date = (date != null && date.isBlank()) ? null : date;
        location = (location != null && location.isBlank()) ? null : location;

        // ✅ Keep user context and RSVP service so buttons render correctly
        User currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("rsvpService", rsvpService);

        List<Event> filteredEvents = eventService.searchEvents(search, category, date, location);
        model.addAttribute("events", filteredEvents);

        return "fragments/eventCards :: eventsContainer";
    }

}