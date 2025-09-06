package au.edu.rmit.sept.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple test controller to verify database connection
 * This only provides API endpoints (no web pages)
 */
@RestController
@RequestMapping("/test")
public class DatabaseConnectionController {

    @Autowired
    private DataSource dataSource;

    /**
     * Quick database connection check
     * Access: http://localhost:8080/db-check/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            response.put("status", "CONNECTED");
            response.put("database", connection.getCatalog());
            response.put("message", "Database connection successful");

            // Quick table count check
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                        "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = 'eventhub_db'");
                if (rs.next()) {
                    response.put("tables", rs.getInt("count"));
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Validate data access by checking sample records
     * Access: http://localhost:8080/db-check/data
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> validateDataAccess() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            response.put("status", "SUCCESS");

            // Check users table
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
                if (rs.next()) {
                    response.put("users", rs.getInt("count"));
                }
            }

            // Check events table
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM events");
                if (rs.next()) {
                    response.put("events", rs.getInt("count"));
                }
            }

            // Check categories table
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM event_categories");
                if (rs.next()) {
                    response.put("categories", rs.getInt("count"));
                }
            }

            // Get sample category names to prove we can read actual data
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT category_name FROM event_categories LIMIT 3");
                StringBuilder sampleCategories = new StringBuilder();
                while (rs.next()) {
                    if (sampleCategories.length() > 0)
                        sampleCategories.append(", ");
                    sampleCategories.append(rs.getString("category_name"));
                }
                response.put("sampleCategories", sampleCategories.toString());
            }

            response.put("message", "Data access validated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("message", "Data access failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}