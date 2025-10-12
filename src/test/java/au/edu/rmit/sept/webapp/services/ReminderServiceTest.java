package au.edu.rmit.sept.webapp.services;

import au.edu.rmit.sept.webapp.models.*;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReminderServiceTest {

    @InjectMocks
    private ReminderService reminderService;

    @Mock
    private RSVPRepository rsvpRepository;

    @Mock
    private EmailService emailService;

    private Event event;
    private User user;
    private RSVP rsvp;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        event = new Event();
        event.setTitle("Hackathon");
        event.setEventDate(LocalDate.now().plusDays(1));
        event.setStartTime(LocalDateTime.now().plusHours(1).toLocalTime());
        event.setEndTime(LocalDateTime.now().plusHours(2).toLocalTime());
        event.setLocation("RMIT Melbourne");

        user = new User();
        user.setEmail("student@rmit.edu.au");
        user.setFirstName("John");
        user.setLastName("Doe");

        rsvp = new RSVP();
        rsvp.setUser(user);
        rsvp.setEvent(event);
        rsvp.setRSVPStatus(RSVP.RSVPStatus.yes);
    }


    // test for sending reminders one hour before event
    @Test
    void checkHourlyReminders_skipsCancelledRSVPs() {
        rsvp.setRSVPStatus(RSVP.RSVPStatus.cancelled);
        when(rsvpRepository.findAll()).thenReturn(List.of(rsvp));

        reminderService.checkHourlyReminders();

        // no email should be sent
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // test for sending reminders one hour before event
    @Test
    void checkDailyReminders_sendsForSpecificDays() {
        event.setEventDate(LocalDate.now().plusDays(7));
        when(rsvpRepository.findAll()).thenReturn(List.of(rsvp));

        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        reminderService.checkDailyReminders();

        verify(emailService).sendEmail(
            eq("student@rmit.edu.au"),
            contains("Hackathon"),
            contains("7 days")
        );
    }

    // test for not sending reminders on non-specified days
    @Test
    void checkDailyReminders_skipsOtherDays() {
        event.setEventDate(LocalDate.now().plusDays(3));
        when(rsvpRepository.findAll()).thenReturn(List.of(rsvp));

        reminderService.checkDailyReminders();

        // no email should be sent
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // test for handling email sending failure
    @Test
    void sendReminder_handlesEmailFailureGracefully() {
        when(rsvpRepository.findAll()).thenReturn(List.of(rsvp));
        doThrow(new RuntimeException("SMTP error")).when(emailService)
            .sendEmail(anyString(), anyString(), anyString());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> reminderService.checkDailyReminders()
        );

        // assert error message contains specific text
        assertTrue(ex.getMessage().contains("Failed to send reminder"));
    }
}
