package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.services.RSVPService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RSVPControllerTest {

    @InjectMocks
    private RSVPController rsvpController;

    @Mock
    private RSVPRepository rsvpRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RSVPService rsvpService;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test displaying the RSVP test page
    @Test
    void showTestPageReturnsViewWithRsvp() {
        String view = rsvpController.showTestPage(model);

        assertEquals("rsvp/rsvp-test", view);
        verify(model).addAttribute(eq("rsvp"), any(RSVP.class));
    }

    // Test submitting an RSVP successfully
    @Test
    void submitRsvpSuccessfulSaveRedirectsHome() {
        // Create user and event
        User user = new User();
        user.setUserId(1L);

        Event event = new Event();
        event.setEventId(2L);

        RSVP.RSVPStatus rsvpStatus = RSVP.RSVPStatus.yes;

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
        when(rsvpService.createOrUpdateRsvp(eq(user), eq(event), eq(rsvpStatus), isNull()))
                .thenReturn(new RSVP());

        // Call controller method
        String view = rsvpController.submitRsvp(1L, 2L, "yes", null, model);

        // Assert redirect and that service was called
        assertEquals("redirect:/", view);
        verify(rsvpService).createOrUpdateRsvp(eq(user), eq(event), eq(rsvpStatus), isNull());
        verify(model).addAttribute(eq("message"), contains("RSVP saved successfully"));
    }

    // Test submitting an RSVP with invalid user ID
    @Test
    void submitRsvpUserNotFoundRedirectsLogin() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        String view = rsvpController.submitRsvp(1L, 2L, "yes", null, model);

        assertEquals("redirect:/auth/login", view);
        verify(rsvpService, never()).createOrUpdateRsvp(any(), any(), any(), any());
    }

    // Test submitting an RSVP with invalid event ID
    @Test
    void submitRsvpEventNotFound() {
        // Create user
        User user = new User();
        user.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        // Call controller method
        String view = rsvpController.submitRsvp(1L, 2L, "yes", null, model);

        // Assert stays on form with error message
        assertEquals("rsvp/rsvp-test", view);
        verify(model).addAttribute(eq("message"), contains("Invalid User ID or Event ID"));
        verify(rsvpService, never()).createOrUpdateRsvp(any(), any(), any(), any());
    }

    // Test submitting an RSVP with invalid status
    @Test
    void checkUserRSVPReturnsStatusIfExists() {
        User user = new User();
        Event event = new Event();

        RSVP rsvp = new RSVP();
        rsvp.setRSVPStatus(RSVP.RSVPStatus.yes);

        when(rsvpRepository.getRsvpStatus(event, user)).thenReturn(Optional.of(rsvp.getRsvpStatus()));

        String status = rsvpController.checkUserRsvp(event, user);

        assertEquals("YES", status.toUpperCase());
    }

    // Test submitting an RSVP with no existing RSVP
    @Test
    void checkUserRsvpReturnsDefault() {
        User user = new User();
        Event event = new Event();

        when(rsvpRepository.getRsvpStatus(event, user)).thenReturn(Optional.empty());

        String status = rsvpController.checkUserRsvp(event, user);

        assertEquals("No RSVP yet", status);
    }
}
