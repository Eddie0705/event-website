package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import au.edu.rmit.sept.webapp.config.GoogleMapsConfig;
// removed unused import RSVPRepository
import au.edu.rmit.sept.webapp.services.EventService;
import au.edu.rmit.sept.webapp.services.QRCodeService;
import au.edu.rmit.sept.webapp.services.RSVPService;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// removed unused imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
// removed unused imports
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
// removed unused imports
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.Console;
import java.io.StringReader;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
// removed unused imports
import java.util.List;

@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private EventService eventService;

    @Autowired
    private RSVPService rsvpService;

    // removed unused repository field

    @Autowired
    private RSVPController rsvpController;

    @Autowired
    private GoogleMapsConfig googleMapsConfig;

    @Value("${app.base-url}")
    private String baseUrl;

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
            return "redirect:/auth/login";
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
            User currentUser = getCurrentUser();

            model.addAttribute("event", event);
            model.addAttribute("currentUser", currentUser);

            // Use the RSVP controller's method to check status
            if (currentUser != null) {
                String userRsvpStatus = rsvpController.checkUserRsvp(event, currentUser);
                model.addAttribute("userRsvpStatus", userRsvpStatus);

                // Add a simple boolean for easier template logic
                boolean hasRsvped = !userRsvpStatus.equals("No RSVP yet");
                model.addAttribute("hasRsvped", hasRsvped);

                if (hasRsvped) {
                    String attendanceStatus = rsvpService.checkAttendanceStatus(event.getEventId(),
                            currentUser.getUserId());
                    model.addAttribute("attendanceStatus", attendanceStatus);
                }

                if (currentUser.getUserType() == UserType.ADMIN ||
                        (currentUser.getUserType() == UserType.ORGANISER
                                && event.getCreatedBy().equals(currentUser.getUserId()))) {
                    model.addAttribute("canViewQR", true);
                }
            }

            return "events/event-details";
        } catch (RuntimeException e) {
            return "redirect:/";
        }
    }

    // Add RSVP endpoints to this controller
    @PostMapping("/{id}/rsvp")
    public String rsvpToEvent(@PathVariable Long id,
            @RequestParam String action, // "rsvp" or "cancel"
            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        try {
            Event event = eventService.getEventById(id);

            // Check if event has finished
            if (event.isEventFinished()) {
                redirectAttributes.addFlashAttribute("error", "Cannot RSVP to an event that has already finished.");
                return "redirect:/events/" + id;
            }

            if ("rsvp".equals(action)) {
                rsvpService.createOrUpdateRsvp(currentUser, event, RSVP.RSVPStatus.yes, null);
                redirectAttributes.addFlashAttribute("success", "RSVP submitted successfully!");
            } else if ("cancel".equals(action)) {
                rsvpService.createOrUpdateRsvp(currentUser, event, RSVP.RSVPStatus.cancelled, null);
                redirectAttributes.addFlashAttribute("success", "RSVP cancelled successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing RSVP. Please try again.");
        }

        return "redirect:/events/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editEvent(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getUserType() != UserType.ORGANISER) {
            return "redirect:/auth/login";
        }

        try {
            Event event = eventService.getEventById(id);

            if (!event.getCreatedBy().equals(currentUser.getUserId())) {
                return "redirect:/organiser/dashboard";
            }

            model.addAttribute("event", event);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("googleMapsApiKey", googleMapsConfig.getApiKey());
            model.addAttribute("editMode", true);
            return "organiser/create-event";
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
            return "redirect:/auth/login";
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

    @GetMapping("/{id}/checkin")
    public String showCheckInPopup(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login?redirect=/events/" + id + "/checkin";
        }

        try {
            Event event = eventService.getEventById(id);
            model.addAttribute("event", event);
            model.addAttribute("currentUser", currentUser);

            // Use the RSVP controller's method to check status
            if (currentUser != null) {
                String userRsvpStatus = rsvpController.checkUserRsvp(event, currentUser);
                model.addAttribute("userRsvpStatus", userRsvpStatus);

                // Add a simple boolean for easier template logic
                boolean hasRsvped = !userRsvpStatus.equals("No RSVP yet");
                model.addAttribute("hasRsvped", hasRsvped);

                if (hasRsvped) {
                    String attendanceStatus = rsvpService.checkAttendanceStatus(event.getEventId(),
                            currentUser.getUserId());
                    model.addAttribute("attendanceStatus", attendanceStatus);
                }
            }

            // set the attribute so popup appears
            model.addAttribute("checkin", true);

            return "events/event-details";
        } catch (Exception e) {
            return "events/event-details";
        }
    }

    // POST method to checkin to an event
    @PostMapping("/{id}/checkin")
    public String checkInUser(@PathVariable Long id, RedirectAttributes redirectAttributes, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/auth/login?redirect=/events/" + id + "/checkin";
        }

        try {
            Event event = eventService.getEventById(id);
            rsvpService.updateAttendanceStatus(event.getEventId(), currentUser.getUserId(),
                    RSVP.AttendanceStatus.checked_in);
        } catch (IllegalArgumentException e) {
            // Handle known errors (e.g., event not found, cannot check-in before event
            // date)
            redirectAttributes.addFlashAttribute("checkinError",
                    "Ensure you are checking in on the event date and have RSVPed.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("checkinError", "Error during check-in. Please try again.");
        }

        return "redirect:/events/" + id;
    }

    // authenticated endpoint for qr code generation
    @GetMapping("/{id}/qrcode")
    @ResponseBody
    public ResponseEntity<byte[]> getEventQRCode(@PathVariable Long id) {

        // authentication
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Event event = eventService.getEventById(id);

        // only the event's organiser or admin can generate the QR code
        boolean isOrganiser = currentUser.getUserType() == UserType.ORGANISER
                && event.getCreatedBy().equals(currentUser.getUserId());
        boolean isAdmin = currentUser.getUserType() == UserType.ADMIN;

        if (!isOrganiser && !isAdmin) {
            return ResponseEntity.status(403).build();
        }

        // Generate QR linking to the event with /checkin route
        String checkinUrl = baseUrl + "/events/" + event.getEventId() + "/checkin";

        // used 250x250 for size
        byte[] qrImage = qrCodeService.generateQRCodeImage(checkinUrl, 250, 250);

        // return as PNG image
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(qrImage);
    }

    @GetMapping("/{id}/export-rsvps")
    @ResponseBody
    public ResponseEntity<byte[]> exportRsvpsToCsv(@PathVariable("id") Long id) {

        // Authentication
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Event event = eventService.getEventById(id);

            // Permission check (only organiser or admin)
            boolean isOrganiser = currentUser.getUserType() == UserType.ORGANISER
                    && event.getCreatedBy().equals(currentUser.getUserId());

            boolean isAdmin = currentUser.getUserType() == UserType.ADMIN;

            if (!isOrganiser && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Generate CSV ussing rsvpService
            byte[] csvBytes = rsvpService.generateRsvpCsv(event.getEventId());
            String fileName = "event_" + event.getEventId() + "_rsvps.csv";

            // Return downloadable CSV genereated from rsvpService
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .body(csvBytes);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public String searchEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String datePreferred,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) List<String> locations,
            @RequestParam(required = false) String price,
            Model model) {

        User currentUser = getCurrentUser();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("rsvpService", rsvpService); // 👈 critical

        String selectedDate = (datePreferred != null && !datePreferred.isBlank())
                ? datePreferred
                : date;

        List<Event> filteredEvents = eventService.searchFilterEvents(search, selectedDate, locations, price);

        model.addAttribute("events", filteredEvents);

        return "fragments/eventCards :: eventsContainer";
    }

}