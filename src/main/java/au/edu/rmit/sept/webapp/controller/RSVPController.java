package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.RSVP;
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

    @Autowired
    public RSVPController(RSVPRepository rsvpRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.rsvpRepository = rsvpRepository;

    }

    // Show test form
    @GetMapping("/test")
    public String showTestPage(Model model) {
        model.addAttribute("rsvp", new RSVP());
        return "rsvp/rsvp-test"; 
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
