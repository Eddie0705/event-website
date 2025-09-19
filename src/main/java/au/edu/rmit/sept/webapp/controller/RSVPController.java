package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.services.RSVPService;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/rsvp")
public class RSVPController {

    private final RSVPRepository rsvpRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RSVPService rsvpService;
    

    @Autowired
    public RSVPController(RSVPRepository rsvpRepository, UserRepository userRepository, EventRepository eventRepository, RSVPService rsvpService) {
        this.rsvpRepository = rsvpRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.rsvpService = rsvpService;
    }

    // Show test form
    @GetMapping("/test")
    public String showTestPage(Model model) {
        model.addAttribute("rsvp", new RSVP());
        return "rsvp/rsvp-test";
    }

    @PostMapping("/submit")
    public String submitRsvp(
            @RequestParam Long userId,
            @RequestParam Long eventId,
            @RequestParam RSVP.RSVPStatus rsvpStatus,
            @RequestParam(required = false) RSVP.AttendanceStatus attendanceStatus,
            Model model) {

        User user = userRepository.findById(userId).orElse(null);
        Event event = eventRepository.findById(eventId).orElse(null);

        if (user == null || event == null) {
            model.addAttribute("message", "Invalid User or Event ID!");
            return "rsvp/rsvp-test";
        }

        RSVP existingRsvp = rsvpRepository.findByEventAndUser(event, user).orElse(null);

        if (existingRsvp != null) {
            existingRsvp.setRSVPStatus(rsvpStatus);
            if (attendanceStatus != null) existingRsvp.setAttendanceStatus(attendanceStatus);
            rsvpRepository.save(existingRsvp);
            model.addAttribute("message", "RSVP updated successfully!");
            model.addAttribute("rsvp", existingRsvp);
        } else {
            RSVP newRsvp = new RSVP();
            newRsvp.setUser(user);
            newRsvp.setEvent(event);
            newRsvp.setRSVPStatus(rsvpStatus);
            if (attendanceStatus != null) newRsvp.setAttendanceStatus(attendanceStatus);
            rsvpRepository.save(newRsvp);
            model.addAttribute("message", "RSVP created successfully!");
            model.addAttribute("rsvp", newRsvp);
        }

        return "rsvp/rsvp-test";
    }

    // Check a user's RSVP status for an event
    public String checkUserRsvp(Event event, User user) {
        return rsvpRepository.getRsvpStatus(event, user)
                .map(RSVP.RSVPStatus::name)
                .orElse("No RSVP yet");
    }
}
