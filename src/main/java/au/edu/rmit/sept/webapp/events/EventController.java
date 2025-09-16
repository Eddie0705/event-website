package au.edu.rmit.sept.webapp.events;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/events")
public class EventController {

    private static final List<String> CATEGORIES = List.of("Tech", "Music", "Sports", "Career", "Social", "Other");

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(value = "success", required = false) Boolean success,
                                 Model model) {
        if (!model.containsAttribute("eventForm")) {
            model.addAttribute("eventForm", new EventForm());
        }
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("success", success != null && success);
        return "events/event-create";
    }

    @PostMapping
    public String createEvent(@Valid @ModelAttribute("eventForm") EventForm eventForm,
                              BindingResult bindingResult,
                              Model model) {
        // Validate category inclusion
        if (eventForm.getCategory() != null && !CATEGORIES.contains(eventForm.getCategory())) {
            bindingResult.rejectValue("category", "invalid", "Please select a valid category");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("today", LocalDate.now());
            return "events/event-create";
        }
        eventService.create(eventForm);
        return "redirect:/events/new?success=true";
    }
}


