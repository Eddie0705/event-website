package au.edu.rmit.sept.webapp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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

    // Admin Dashboard endpoints
    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin/dashboard"; // admin/dashboard.html in templates
    }

    @GetMapping("/admin/events")
    public String adminEvents() {
        return "admin/dashboard"; // Redirect to dashboard with events section
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin/dashboard"; // Redirect to dashboard with users section
    }
}
