package au.edu.rmit.sept.webapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/Index")
    public String home() {
        return "Index"; // Landing.html in templates
    }

    @GetMapping("/explore")
    public String explore() {
        return "Explore"; // Explore.html in templates
    }

    @GetMapping("/calendar")
    public String calendar() {
        return "Calendar"; // Calendar.html in templates
    }

    @GetMapping("/rspv")
    public String rspv() {
        return "Rspv"; // Rspv.html in templates
    }

    @GetMapping("/noti")
    public String noti() {
        return "Notification"; // Notification.html in templates
    }

    @GetMapping("/profile")
    public String profile() {
        return "Profile"; // Profile.html in templates
    }

    @GetMapping("/Login")
    public String login() {
        return "Login"; // Login.html in templates
    }

}
