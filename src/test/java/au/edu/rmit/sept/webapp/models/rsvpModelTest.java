package au.edu.rmit.sept.webapp.models;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RSVPTest {

    @Test
    void testGettersAndSetters() {
        Event event = new Event();
        User user = new User();

        RSVP rsvp = new RSVP();
        rsvp.setEvent(event);
        rsvp.setUser(user);
        rsvp.setRSVPStatus(RSVP.RSVPStatus.yes);
        rsvp.setAttendanceStatus(RSVP.AttendanceStatus.checked_in);
        rsvp.setQrCode("QR123");
        LocalDateTime now = LocalDateTime.now();
        rsvp.setCheckInTime(now);

        assertEquals(event, rsvp.getEvent());
        assertEquals(user, rsvp.getUser());
        assertEquals(RSVP.RSVPStatus.yes, rsvp.getRsvpStatus());
        assertEquals(RSVP.AttendanceStatus.checked_in, rsvp.getAttendanceStatus());
        assertEquals("QR123", rsvp.getQrCode());
        assertEquals(now, rsvp.getCheckInTime());
    }

    @Test
    void testConstructorWithParameters() {
        Event event = new Event();
        User user = new User();
        RSVP rsvp = new RSVP(event, user, RSVP.RSVPStatus.maybe);

        assertEquals(event, rsvp.getEvent());
        assertEquals(user, rsvp.getUser());
        assertEquals(RSVP.RSVPStatus.maybe, rsvp.getRsvpStatus());
        assertEquals(RSVP.AttendanceStatus.not_checked, rsvp.getAttendanceStatus());
        assertNotNull(rsvp.getCreatedAt());
        assertNotNull(rsvp.getUpdatedAt());
    }

    @Test
    void testUpdatedAtChangesOnSetter() throws InterruptedException {
        RSVP rsvp = new RSVP();
        LocalDateTime original = rsvp.getUpdatedAt();

        Thread.sleep(10); 
        rsvp.setRSVPStatus(RSVP.RSVPStatus.no);
        assertTrue(rsvp.getUpdatedAt().isAfter(original));

        Thread.sleep(10);
        rsvp.setAttendanceStatus(RSVP.AttendanceStatus.checked_in);
        assertTrue(rsvp.getUpdatedAt().isAfter(original));
    }

    @Test
    void testEnumValues() {
        assertEquals(4, RSVP.RSVPStatus.values().length);
        assertEquals(3, RSVP.AttendanceStatus.values().length);

        assertEquals("yes", RSVP.RSVPStatus.yes.name());
        assertEquals("checked_in", RSVP.AttendanceStatus.checked_in.name());
    }
}
