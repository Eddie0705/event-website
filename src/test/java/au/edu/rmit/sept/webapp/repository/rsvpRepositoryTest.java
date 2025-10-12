package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RSVPRepositoryTest {

    @Mock
    private RSVPRepository rsvpRepository;

    private Event testEvent;
    private User testUser;
    private RSVP testRsvp;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUserId(1L);

        testEvent = new Event();
        testEvent.setEventId(1L);

        testRsvp = new RSVP();
        testRsvp.setUser(testUser);
        testRsvp.setEvent(testEvent);
        testRsvp.setRSVPStatus(RSVP.RSVPStatus.yes);
        testRsvp.setAttendanceStatus(RSVP.AttendanceStatus.checked_in);
    }

    @Test
    void getRsvpStatusReturnsCorrectStatus() {
        when(rsvpRepository.findByEventAndUser(testEvent, testUser))
                .thenReturn(Optional.of(testRsvp));

        Optional<RSVP.RSVPStatus> status = rsvpRepository.getRsvpStatus(testEvent, testUser);
    }

    @Test
    void getAttendanceStatusReturnsCorrectValue() {
        when(rsvpRepository.getAttendanceStatus(1L, 1L))
                .thenReturn(Optional.of(RSVP.AttendanceStatus.checked_in));

        Optional<RSVP.AttendanceStatus> status = rsvpRepository.getAttendanceStatus(1L, 1L);
        assertTrue(status.isPresent());
        assertEquals(RSVP.AttendanceStatus.checked_in, status.get());
    }

    @Test
    void updateAttendanceStatusReturnsUpdatedRows() {
        when(rsvpRepository.updateAttendanceStatus(1L, 1L, RSVP.AttendanceStatus.checked_in))
                .thenReturn(1);

        int updatedRows = rsvpRepository.updateAttendanceStatus(1L, 1L, RSVP.AttendanceStatus.checked_in);
        assertEquals(1, updatedRows);
    }
}
