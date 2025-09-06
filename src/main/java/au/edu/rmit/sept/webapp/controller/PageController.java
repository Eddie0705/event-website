package au.edu.rmit.sept.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PageController {

    @Autowired
    private DataSource dataSource;

    // Your existing mappings
    @GetMapping("/Index")
    public String home() {
        return "Index";
    }

    @GetMapping("/explore")
    public String explore() {
        return "Explore";
    }

    @GetMapping("/calendar")
    public String calendar() {
        return "Calendar";
    }

    @GetMapping("/rspv")
    public String rspv() {
        return "Rspv";
    }

    @GetMapping("/noti")
    public String noti() {
        return "Notification";
    }

    @GetMapping("/profile")
    public String profile() {
        return "Profile";
    }

    // Database test endpoint
    @GetMapping("/db-test")
    @ResponseBody
    public Map<String, Object> testDatabase() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            response.put("status", "SUCCESS");
            response.put("database", connection.getCatalog());
            response.put("message", "Database connection working!");
            return response;
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return response;
        }
    }
}