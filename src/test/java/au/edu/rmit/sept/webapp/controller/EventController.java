package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService.CustomUserPrincipal;
import au.edu.rmit.sept.webapp.services.EventService;
import au.edu.rmit.sept.webapp.services.RSVPService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventControllerTest {

    @InjectMocks
    private EventController eventController;

    @Mock
    private EventService eventService;

    @Mock
    private RSVPService rsvpService;

    @Mock
    private RSVPController rsvpController;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    /* Helper to mock SecurityContextHolder principal -  CustomUserPrincipal */
    private void mockSecurityContext(User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        if (user != null) {
            CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
            when(principal.getUser()).thenReturn(user);
            when(authentication.getPrincipal()).thenReturn(principal);
        } else {
            when(authentication.getPrincipal()).thenReturn(null);
        }
        SecurityContextHolder.setContext(securityContext);
    }

    // create an event
    @Test
    void createEventSuccessful() {
        // Create an organiser user and an event
        User organiser = new User();
        organiser.setUserId(100L);
        organiser.setUserType(UserType.ORGANISER);
        mockSecurityContext(organiser);

        String title = "Test Event";
        String description = "desc";
        String eventDate = LocalDate.now().plusDays(2).toString();
        String startTime = "10:00";
        String endTime = "12:00";
        String location = "RMIT";
        Integer maxCapacity = 50;
        String venueDetails = "Room 1";

        // call controller method
        String view = eventController.createEvent(title, description, eventDate, startTime, endTime,
                location, maxCapacity, venueDetails, redirectAttributes);

        // Assert correct redirect
        assertEquals("redirect:/", view);

        // Capture the event passed to service and assert that it was created by the correct organiser
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventService, times(1)).createEvent(captor.capture());
        Event created = captor.getValue();
        assertEquals(title, created.getTitle());
        assertEquals(organiser.getUserId(), created.getCreatedBy());
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), contains(title));
    }

    //  invalid date when creating event 
    @Test
    void createEventInvalidDate() {
        // crreate an organiser user and an event
        User organiser = new User();
        organiser.setUserId(10L);
        organiser.setUserType(UserType.ORGANISER);
        mockSecurityContext(organiser);

        String view = eventController.createEvent("Old", "d", LocalDate.now().minusDays(1).toString(),
                "10:00", "11:00", "loc", 10, null, redirectAttributes);

        // Assert correct redirect and no event created
        assertEquals("redirect:/organiser/create-event", view);
        verify(eventService, never()).createEvent(any());
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    // invalid time range when creating event
    @Test
    void createEventInvalidTimeRange() {
        User organiser = new User();
        organiser.setUserId(11L);
        organiser.setUserType(UserType.ORGANISER);
        mockSecurityContext(organiser);

        String view = eventController.createEvent("T", "d", LocalDate.now().plusDays(1).toString(),
                "15:00", "14:00", "loc", 10, null, redirectAttributes);

        assertEquals("redirect:/organiser/create-event", view);
        verify(eventService, never()).createEvent(any());
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    // invalid capacity when creating event
    @Test
    void createEventInvalidCapacity() {
        User organiser = new User();
        organiser.setUserId(12L);
        organiser.setUserType(UserType.ORGANISER);
        mockSecurityContext(organiser);

        String view = eventController.createEvent("T", "d", LocalDate.now().plusDays(1).toString(),
                "10:00", "11:00", "loc", 0, null, redirectAttributes);

        assertEquals("redirect:/organiser/create-event", view);
        verify(eventService, never()).createEvent(any());
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    // unauthorized user (not organiser) tries to create event
    @Test
    void createEventUnauthorizedUser() {
        User student = new User();
        student.setUserId(20L);
        student.setUserType(UserType.STUDENT);
        mockSecurityContext(student);

        String view = eventController.createEvent("T", "d", LocalDate.now().plusDays(1).toString(),
                "10:00", "11:00", "loc", 10, null, redirectAttributes);

        assertEquals("redirect:/auth/login", view);
        verify(eventService, never()).createEvent(any());
    }

    // Viewing Events 
    @Test
    void viewEventSuccessWithRsvp() {
        // Create User and event 
        User current = new User();
        current.setUserId(5L);
        mockSecurityContext(current);

        Event event = new Event();
        event.setEventId(99L);
        event.setTitle("E");

        when(eventService.getEventById(99L)).thenReturn(event);
        when(rsvpController.checkUserRsvp(event, current)).thenReturn("Yes");

        // Call controller method
        String view = eventController.viewEvent(99L, model);

        // Assert correct view and model attributes
        assertEquals("events/event-details", view);
        verify(model).addAttribute("event", event);
        verify(model).addAttribute("currentUser", current);
        verify(model).addAttribute("userRsvpStatus", "Yes");
        verify(model).addAttribute("hasRsvped", true);
    }


    // RSVP tests
    @Test
    void rsvpToEventRSVPSuccess() {
        // Create user and event
        User current = new User();
        current.setUserId(7L);
        mockSecurityContext(current);

        Event event = new Event();
        event.setEventId(200L);

        when(eventService.getEventById(200L)).thenReturn(event);

        // Call controller method
        String view = eventController.rsvpToEvent(200L, "rsvp", redirectAttributes);

        // Assert redirect and that RSVP service was called
        assertEquals("redirect:/events/200", view);
        verify(rsvpService).createOrUpdateRsvp(eq(current), eq(event), eq(RSVP.RSVPStatus.yes), isNull());
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
    }

    // cancel RSVP
    @Test
    void rsvpToEventCancelSuccess() {
        // Create user and event
        User current = new User();
        current.setUserId(8L);
        mockSecurityContext(current);

        Event event = new Event();
        event.setEventId(201L);

        // Mock event service to return the event
        when(eventService.getEventById(201L)).thenReturn(event);

        String view = eventController.rsvpToEvent(201L, "cancel", redirectAttributes);

        // Assert redirect and that RSVP service was called
        assertEquals("redirect:/events/201", view);
        verify(rsvpService).createOrUpdateRsvp(eq(current), eq(event), eq(RSVP.RSVPStatus.cancelled), isNull());
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
    }

    // not logged in user tries to RSVP
    @Test
    void rsvpToEventNotLoggedInRedirectsToLogin() {
        mockSecurityContext(null);

        String view = eventController.rsvpToEvent(50L, "rsvp", redirectAttributes);
        assertEquals("redirect:/auth/login", view);
        verify(rsvpService, never()).createOrUpdateRsvp(any(), any(), any(), any());
    }

    // Editing an event 
    @Test
    void editEvent_authorized_creator() {
        // Create organiser user and event
        User organiser = new User();
        organiser.setUserId(30L);
        organiser.setUserType(UserType.ORGANISER);
        mockSecurityContext(organiser);

        Event event = new Event();
        event.setEventId(30L);
        event.setCreatedBy(organiser.getUserId());

        when(eventService.getEventById(30L)).thenReturn(event);

        // Call controller method
        String view = eventController.editEvent(30L, model);

        // Assert correct view and model attribute
        assertEquals("redirect:/organiser/dashboard", view);
        verify(model).addAttribute("event", event);
    }

    // not an organiser tries to edit event
    @Test
    void editEventNotOrganiserRedirectsLogin() {
        User student = new User();
        student.setUserId(31L);
        student.setUserType(UserType.STUDENT);
        mockSecurityContext(student);

        String view = eventController.editEvent(31L, model);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void editEventNotCreatorRedirectsDashboard() {
        // Create organiser user and event
        User organiser = new User();
        organiser.setUserId(32L);
        organiser.setUserType(UserType.ORGANISER);
        mockSecurityContext(organiser);

        Event event = new Event();
        event.setEventId(32L);
        event.setCreatedBy(999L); // different creator

        // Mock event service to return the event
        when(eventService.getEventById(32L)).thenReturn(event);
        
        // Call controller method
        String view = eventController.editEvent(32L, model);
        assertEquals("redirect:/organiser/dashboard", view);
    }

    // Updating an event
    @Test
    void updateEventSuccess() {
        // Create organiser user and event
        User organiser = new User();
        organiser.setUserId(40L);
        organiser.setUserType(UserType.ORGANISER);
        mockSecurityContext(organiser);

        Event event = new Event();
        event.setEventId(40L);
        event.setCreatedBy(organiser.getUserId());

        when(eventService.getEventById(40L)).thenReturn(event);

        // Call controller method
        String view = eventController.updateEvent(
                40L,
                "New title",
                "desc",
                LocalDate.now().plusDays(2).toString(),
                "09:00",
                "10:00",
                "loc",
                20,
                null,
                redirectAttributes
        );

        // Assert redirect and that service was called to update
        assertEquals("redirect:/organiser/dashboard", view);
        verify(eventService).updateEvent(eq(40L), any(Event.class));
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }

    // not an organiser tries to update event
    @Test
    void updateEventNotCreatorSetsErrorAndRedirects() {
        // Create organiser user and event
        User organiser = new User();
        organiser.setUserId(41L);
        organiser.setUserType(UserType.ORGANISER);
        mockSecurityContext(organiser);

        Event event = new Event();
        event.setEventId(41L);
        event.setCreatedBy(999L);

        when(eventService.getEventById(41L)).thenReturn(event);
        // Call controller method
        String view = eventController.updateEvent(
                41L, "t", "d",
                LocalDate.now().plusDays(2).toString(),
                "09:00", "10:00", "loc", 10, null, redirectAttributes
        );

        // Assert redirect and that no update was attempted
        assertEquals("redirect:/organiser/dashboard", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
        verify(eventService, never()).updateEvent(anyLong(), any());
    }

    // exception thrown when fetching event to update
    @Test
    void updateEventExceptionRedirectsToEdit() {
        // Create organiser user
        User organiser = new User();
        organiser.setUserId(42L);
        organiser.setUserType(UserType.ORGANISER);
        mockSecurityContext(organiser);

        when(eventService.getEventById(42L)).thenThrow(new RuntimeException("boom"));

        // Call controller method
        String view = eventController.updateEvent(
                42L, "t", "d", LocalDate.now().plusDays(2).toString(),
                "09:00", "10:00", "loc", 10, null, redirectAttributes
        );
        
        // Assert redirect and that no update was attempted
        assertEquals("redirect:/events/42/edit", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    // Deleting events 
    @Test
    void deleteEventSuccess() {
        // Create organiser user
        User organiser = new User();
        organiser.setUserId(50L);
        organiser.setUserType(UserType.ORGANISER);
        mockSecurityContext(organiser);

        // eventService.deleteEvent should be called without throwing
        String view = eventController.deleteEvent(50L, redirectAttributes);

        // Assert redirect and that service was called
        assertEquals("redirect:/organiser/dashboard", view);
        verify(eventService).deleteEvent(eq(50L), eq(organiser.getUserId()));
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }

    // not an organiser tries to delete event
    @Test
    void deleteEventUnauthorizedRedirectsLogin() {
        mockSecurityContext(null); // not logged in

        String view = eventController.deleteEvent(60L, redirectAttributes);
        assertEquals("redirect:/login", view);
        verify(eventService, never()).deleteEvent(anyLong(), anyLong());
    }

    // exception thrown when deleting event
    @Test
    void deleteEvent_exception_addsErrorAndRedirects() {
        // Create organiser user
        User organiser = new User();
        organiser.setUserId(51L);
        organiser.setUserType(UserType.ORGANISER);
        mockSecurityContext(organiser);

        doThrow(new RuntimeException("boom")).when(eventService).deleteEvent(eq(51L), eq(organiser.getUserId()));
        
        // Call controller method
        String view = eventController.deleteEvent(51L, redirectAttributes);

        // Assert redirect and that service was called
        assertEquals("redirect:/organiser/dashboard", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }
}
