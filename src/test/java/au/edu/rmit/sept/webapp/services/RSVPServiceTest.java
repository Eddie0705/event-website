package au.edu.rmit.sept.webapp.services;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit tests for RSVPService
class RSVPServiceTest {
    @InjectMocks
    private RSVPService rsvpService;

    @Mock
    private RSVPRepository rsvpRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /** creating and updating RSVP */
    @Test
    void createNewRsvpSuccess() {
        // Create user and event
        User user = new User();
        Event event = new Event();

        RSVP.RSVPStatus status = RSVP.RSVPStatus.yes;
        RSVP.AttendanceStatus attendance = RSVP.AttendanceStatus.checked_in;

        when(rsvpRepository.findByEventAndUser(event, user)).thenReturn(Optional.empty());
        when(rsvpRepository.save(any(RSVP.class))).thenAnswer(i -> i.getArgument(0));

        // Call service method
        RSVP saved = rsvpService.createOrUpdateRsvp(user, event, status, attendance);

        // Check saved RSVP has correct fields
        assertEquals(user, saved.getUser());
        assertEquals(event, saved.getEvent());
        assertEquals(status, saved.getRsvpStatus());
        assertEquals(attendance, saved.getAttendanceStatus());
    }

    @Test
    void updateExistingRsvpSuccess() {
        User user = new User();
        Event event = new Event();

        RSVP existing = new RSVP();
        existing.setUser(user);
        existing.setEvent(event);

        when(rsvpRepository.findByEventAndUser(event, user)).thenReturn(Optional.of(existing));
        when(rsvpRepository.save(any(RSVP.class))).thenAnswer(i -> i.getArgument(0));

        // Call service method to update RSVP status
        RSVP updated = rsvpService.createOrUpdateRsvp(user, event, RSVP.RSVPStatus.no, null);

        // Check updated RSVP has new status but unchanged attendance
        assertEquals(RSVP.RSVPStatus.no, updated.getRsvpStatus());
        assertEquals(RSVP.AttendanceStatus.not_checked, updated.getAttendanceStatus());
    }

    /** get RSVP status */
    @Test
    void getRsvpStatusFound() {
        User user = new User();
        Event event = new Event();
        RSVP rsvp = new RSVP();
        rsvp.setRSVPStatus(RSVP.RSVPStatus.yes);

        when(rsvpRepository.findByEventAndUser(event, user)).thenReturn(Optional.of(rsvp));

        // Call service method
        RSVP.RSVPStatus status = rsvp.getRsvpStatus();

        // Check status is as expected
        assertEquals(RSVP.RSVPStatus.yes, status);
    }


}
