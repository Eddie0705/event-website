package au.edu.rmit.sept.webapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import au.edu.rmit.sept.webapp.services.EmailService;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    void testSendEmail() {
        emailService.sendEmail(
                "stu.62915@gmail.com", 
                "Spring Boot Test Email", 
                "Hello! This is a test email from Spring Boot using Gmail SMTP."
        );
    }
}
