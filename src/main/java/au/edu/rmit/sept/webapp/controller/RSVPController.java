package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
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

    @Autowired
    public RSVPController(RSVPRepository rsvpRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.rsvpRepository = rsvpRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    // Show test form
    @GetMapping("/test")
    public String showTestPage(Model model) {
        model.addAttribute("rsvp", new RSVP());
        return "rsvp/rsvp-test";
    }

    // Handle form submission (create or update RSVP)
    @PostMapping("/submit")
    public String submitRsvp(@ModelAttribute RSVP rsvp, Model model) {

        // Fetch actual User and Event objects
        User user = userRepository.findById(rsvp.getUser().getUserId()).orElse(null);
        Event event = eventRepository.findById(rsvp.getEvent().getEventId()).orElse(null);

        if (user == null || event == null) {
            model.addAttribute("message", "Invalid User or Event ID!");
            return "rsvp/rsvp-test";
        }

        // Check if RSVP already exists
        RSVP existingRsvp = rsvpRepository.findByEventAndUser(event, user).orElse(null);

        if (existingRsvp != null) {
            // Update existing RSVP
            existingRsvp.setRSVPStatus(rsvp.getRsvpStatus());
            rsvpRepository.save(existingRsvp);
            model.addAttribute("message", "RSVP updated successfully!");
            model.addAttribute("rsvp", existingRsvp);
        } else {
            // Create new RSVP
            rsvp.setUser(user);
            rsvp.setEvent(event);
            rsvpRepository.save(rsvp);
            model.addAttribute("message", "RSVP created successfully!");
            model.addAttribute("rsvp", rsvp);
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
