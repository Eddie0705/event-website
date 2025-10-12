package au.edu.rmit.sept.webapp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //  test for sending successsful email
    @Test
    void sendEmail_sendsSuccessfully() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // assert error not thrown
        assertDoesNotThrow(() ->
            emailService.sendEmail("test@rmit.edu.au", "Subject", "Body")
        );

        // verify send method called once
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    // test for sending failed email
    @Test
    void sendEmail_throwsWhenMailFails() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender)
            .send(any(SimpleMailMessage.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            emailService.sendEmail("test@rmit.edu.au", "Subject", "Body")
        );

        // assert error message contains specific text
        assertTrue(ex.getMessage().contains("Failed to send reminder"));
    }
}
