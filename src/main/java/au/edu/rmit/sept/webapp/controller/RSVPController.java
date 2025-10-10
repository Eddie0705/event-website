package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.services.RSVPService;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
@Controller
@RequestMapping("/rsvp")
public class RSVPController {

    private final RSVPRepository rsvpRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RSVPService rsvpService;

    private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
            && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUser();
    }
    return null;
}
    

    @Autowired
    public RSVPController(RSVPRepository rsvpRepository, UserRepository userRepository, EventRepository eventRepository, RSVPService rsvpService) {
        this.rsvpRepository = rsvpRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.rsvpService = rsvpService;
    }

    @GetMapping("/test")
    public String showTestPage(Model model) {
        RSVP rsvp = new RSVP();
        rsvp.setUser(new User()); // avoids null in template
        rsvp.setEvent(new Event());
        model.addAttribute("rsvp", rsvp);
        return "rsvp/rsvp-test";
    }

    @PostMapping("/submit")
    public String submitRsvp(@RequestParam Long userId,
                            @RequestParam Long eventId,
                            @RequestParam String rsvpStatus,
                            @RequestParam(required = false) String attendanceStatus,
                            Model model) {


        User user = userRepository.findById(userId).orElse(null);
        Event event = eventRepository.findById(eventId).orElse(null);
      
        if (user == null) {
            return "redirect:/auth/login";
        }
        RSVP rsvp = new RSVP(); // default non-null object
        rsvp.setUser(user);
        rsvp.setEvent(event);

        

        if (user == null || event == null) {
            model.addAttribute("message", "Invalid User ID or Event ID!");
            model.addAttribute("rsvp", rsvp);
            return "rsvp/rsvp-test";
        }

        // Check if event has finished
        if (event.isEventFinished()) {
            model.addAttribute("message", "Cannot RSVP to an event that has already finished!");
            model.addAttribute("rsvp", rsvp);
            return "rsvp/rsvp-test";
        }

        // Convert Strings to enums safely
        RSVP.RSVPStatus rsvpEnum = null;
        RSVP.AttendanceStatus attendanceEnum = null;
        try {
            rsvpEnum = RSVP.RSVPStatus.valueOf(rsvpStatus.toLowerCase());
        } catch (IllegalArgumentException ignored) {}
        if (attendanceStatus != null && !attendanceStatus.isEmpty()) {
            try {
                attendanceEnum = RSVP.AttendanceStatus.valueOf(attendanceStatus.toLowerCase());
            } catch (IllegalArgumentException ignored) {}
        }

        RSVP savedRsvp = rsvpService.createOrUpdateRsvp(user, event, rsvpEnum, attendanceEnum);
        model.addAttribute("message", "RSVP saved successfully!");
        model.addAttribute("rsvp", savedRsvp);

        return "redirect:/";
    }
    // Check a user's RSVP status for an event
    public String checkUserRsvp(Event event, User user) {
        return rsvpRepository.getRsvpStatus(event, user)
                .map(RSVP.RSVPStatus::name)
                .orElse("No RSVP yet");
    }

    // @GetMapping("/eventRSVP")
    // public String showMyRsvps(Model model) {

    //     User currentUser = getCurrentUser();
    //     model.addAttribute("user", currentUser);
    //     model.addAttribute("isAuthenticated", currentUser != null);

    //     // List<RSVP> rsvps = rsvpService.getUserRsvps(currentUser);
    //     // model.addAttribute("rsvps", rsvps);

    //     // model.addAttribute("yes", rsvps.stream()
    //     //                             .filter(r -> r.getRsvpStatus() == RSVP.RSVPStatus.yes)
    //     //                             .toList());

    //     // model.addAttribute("no", rsvps.stream()
    //     //                             .filter(r -> r.getRsvpStatus() == RSVP.RSVPStatus.no)
    //     //                             .toList());

    //     // model.addAttribute("maybe", rsvps.stream()
    //     //                                 .filter(r -> r.getRsvpStatus() == RSVP.RSVPStatus.maybe)
    //     //                                 .toList());

    //     // model.addAttribute("cancelled", rsvps.stream()
    //     //                                     .filter(r -> r.getRsvpStatus() == RSVP.RSVPStatus.cancelled)
    //     //                                     .toList());

    //     return "eventRSVP";
    // }
}
