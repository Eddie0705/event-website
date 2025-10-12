package au.edu.rmit.sept.webapp.services;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RSVPServiceTest {

    @Mock
    private RSVPRepository rsvpRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;  

    @Mock
    private EventService eventService;

    @InjectMocks
    private RSVPService rsvpService;  

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /** creating and updating RSVP */
    @Test
    void createNewRsvpSuccess() {
        // Arrange
        User user = new User();
        user.setEmail("whatever@mailinator.com");
        user.setFirstName("Test");
        user.setLastName("User");
        Event event = new Event();
        event.setTitle("Tech Talk");

        RSVP.RSVPStatus status = RSVP.RSVPStatus.yes;
        RSVP.AttendanceStatus attendance = RSVP.AttendanceStatus.checked_in;

        when(rsvpRepository.findByEventAndUser(event, user)).thenReturn(Optional.empty());
        when(rsvpRepository.save(any(RSVP.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        RSVP saved = rsvpService.createOrUpdateRsvp(user, event, status, attendance);

        // Assert
        assertEquals(user, saved.getUser());
        assertEquals(event, saved.getEvent());
        assertEquals(status, saved.getRsvpStatus());
        assertEquals(attendance, saved.getAttendanceStatus());

        // Verify that the email was sent ✅
        verify(emailService, times(1))
                .sendEmail(eq(user.getEmail()), contains("RSVP"), anyString());
    }

    @Test
    void updateExistingRsvpSuccess() {
        // Arrange
        User user = new User();
        user.setEmail("whatever@mailinator.com");
        user.setFirstName("Test");
        user.setLastName("User");

        Event event = new Event();
        event.setTitle("Networking Night");

        RSVP existing = new RSVP();
        existing.setUser(user);
        existing.setEvent(event);
        existing.setRSVPStatus(RSVP.RSVPStatus.yes);
        existing.setAttendanceStatus(RSVP.AttendanceStatus.not_checked);

        when(rsvpRepository.findByEventAndUser(event, user)).thenReturn(Optional.of(existing));
        when(rsvpRepository.save(any(RSVP.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        RSVP updated = rsvpService.createOrUpdateRsvp(user, event, RSVP.RSVPStatus.no, null);

        // Assert
        assertEquals(RSVP.RSVPStatus.no, updated.getRsvpStatus());
        assertEquals(RSVP.AttendanceStatus.not_checked, updated.getAttendanceStatus());

        // Email should still be triggered ✅
        verify(emailService, times(1))
                .sendEmail(eq(user.getEmail()), contains("RSVP"), anyString());
    }

    /** get RSVP status */
    @Test
    void getRsvpStatusFound() {
        User user = new User();
        Event event = new Event();
        RSVP rsvp = new RSVP();
        rsvp.setRSVPStatus(RSVP.RSVPStatus.yes);

        when(rsvpRepository.findByEventAndUser(event, user)).thenReturn(Optional.of(rsvp));

        RSVP.RSVPStatus status = rsvp.getRsvpStatus();

        assertEquals(RSVP.RSVPStatus.yes, status);
    }
}
