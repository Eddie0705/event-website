package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.config.GoogleMapsConfig;
import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrganiserController
 * Tests organiser dashboard functionality including event management, creation, publishing, and cancellation
 */
class OrganiserControllerTest {

    @InjectMocks
    private OrganiserController organiserController;

    @Mock
    private EventService eventService;

    @Mock
    private GoogleMapsConfig googleMapsConfig;

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
        
        // Initialize the controller with mocked dependencies
        organiserController = new OrganiserController(googleMapsConfig);
        // Use reflection to inject the eventService mock
        try {
            java.lang.reflect.Field eventServiceField = OrganiserController.class.getDeclaredField("eventService");
            eventServiceField.setAccessible(true);
            eventServiceField.set(organiserController, eventService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject eventService mock", e);
        }
    }

    //Dashboard Tests 

    @Test
    void showDashboardWithOrganiserUserReturnsDashboard() {
        User organiser = createOrganiserUser();
        EventService.EventStatistics stats = new EventService.EventStatistics(5L, 2L, 3L, 4L);
        List<Event> events = createSampleEvents();
        Page<Event> eventsPage = new PageImpl<>(events);

        mockSecurityContext(organiser);
        when(eventService.getEventStatistics(organiser.getUserId())).thenReturn(stats);
        when(eventService.getEventsByOrganiser(eq(organiser.getUserId()), any(Pageable.class)))
                .thenReturn(eventsPage);

        String view = organiserController.showDashboard(model, 0, 10, "all");

        assertEquals("organiser/dashboard", view);
        verify(model).addAttribute("currentUser", organiser);
        verify(model).addAttribute("stats", stats);
        verify(model).addAttribute("events", events);
        verify(model).addAttribute("currentFilter", "all");
        verify(model).addAttribute("currentPage", 0);
    }

    @Test
    void showDashboardWithNonOrganiserUserRedirectsToLogin() {
        User student = createStudentUser();
        mockSecurityContext(student);

        String view = organiserController.showDashboard(model, 0, 10, "all");

        assertEquals("redirect:/auth/login", view);
        verify(eventService, never()).getEventStatistics(any());
    }

    @Test
    void showDashboardWithNullUserRedirectsToLogin() {
        mockSecurityContext(null);

        String view = organiserController.showDashboard(model, 0, 10, "all");

        assertEquals("redirect:/auth/login", view);
        verify(eventService, never()).getEventStatistics(any());
    }

    @Test
    void showDashboardWithPublishedFilterReturnsPublishedEvents() {
        User organiser = createOrganiserUser();
        EventService.EventStatistics stats = new EventService.EventStatistics(5L, 2L, 3L, 4L);
        List<Event> allEvents = createSampleEvents();
        List<Event> publishedEvents = allEvents.stream()
                .filter(event -> event.getStatus() == Event.EventStatus.published)
                .toList();
        Page<Event> eventsPage = new PageImpl<>(allEvents);

        mockSecurityContext(organiser);
        when(eventService.getEventStatistics(organiser.getUserId())).thenReturn(stats);
        when(eventService.getEventsByOrganiser(eq(organiser.getUserId()), any(Pageable.class)))
                .thenReturn(eventsPage);

        String view = organiserController.showDashboard(model, 0, 10, "published");

        assertEquals("organiser/dashboard", view);
        verify(model).addAttribute("events", publishedEvents);
        verify(model).addAttribute("currentFilter", "published");
    }

    @Test
    void showDashboardWithDraftFilterReturnsDraftEvents() {
        User organiser = createOrganiserUser();
        EventService.EventStatistics stats = new EventService.EventStatistics(5L, 2L, 3L, 4L);
        List<Event> allEvents = createSampleEvents();
        List<Event> draftEvents = allEvents.stream()
                .filter(event -> event.getStatus() == Event.EventStatus.draft)
                .toList();
        Page<Event> eventsPage = new PageImpl<>(allEvents);

        mockSecurityContext(organiser);
        when(eventService.getEventStatistics(organiser.getUserId())).thenReturn(stats);
        when(eventService.getEventsByOrganiser(eq(organiser.getUserId()), any(Pageable.class)))
                .thenReturn(eventsPage);

        String view = organiserController.showDashboard(model, 0, 10, "draft");

        assertEquals("organiser/dashboard", view);
        verify(model).addAttribute("events", draftEvents);
        verify(model).addAttribute("currentFilter", "draft");
    }

    @Test
    void showDashboardWithUpcomingFilterReturnsUpcomingEvents() {
        User organiser = createOrganiserUser();
        EventService.EventStatistics stats = new EventService.EventStatistics(5L, 2L, 3L, 4L);
        List<Event> upcomingEvents = createUpcomingEvents();

        mockSecurityContext(organiser);
        when(eventService.getEventStatistics(organiser.getUserId())).thenReturn(stats);
        when(eventService.getEventsByOrganiser(organiser.getUserId())).thenReturn(upcomingEvents);

        String view = organiserController.showDashboard(model, 0, 10, "upcoming");

        assertEquals("organiser/dashboard", view);
        verify(model).addAttribute("events", upcomingEvents);
        verify(model).addAttribute("currentFilter", "upcoming");
    }

    @Test
    void showDashboardWithPastFilterReturnsPastEvents() {
        User organiser = createOrganiserUser();
        EventService.EventStatistics stats = new EventService.EventStatistics(5L, 2L, 3L, 4L);
        List<Event> pastEvents = createPastEvents();

        mockSecurityContext(organiser);
        when(eventService.getEventStatistics(organiser.getUserId())).thenReturn(stats);
        when(eventService.getEventsByOrganiser(organiser.getUserId())).thenReturn(pastEvents);

        String view = organiserController.showDashboard(model, 0, 10, "past");

        assertEquals("organiser/dashboard", view);
        verify(model).addAttribute("events", pastEvents);
        verify(model).addAttribute("currentFilter", "past");
    }

     // ========== Create Event Form Tests ==========

     @Test
     void showCreateEventFormWithOrganiserUserReturnsForm() {
         User organiser = createOrganiserUser();
         mockSecurityContext(organiser);
         when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
 
         String view = organiserController.showCreateEventForm(model);
 
         assertEquals("organiser/create-event", view);
         verify(model).addAttribute("currentUser", organiser);
         verify(model).addAttribute("isAuthenticated", true);
         verify(model).addAttribute("googleMapsApiKey", "test-api-key");
     }
 
     @Test
     void showCreateEventFormWithAdminUserReturnsForm() {
         User admin = createAdminUser();
         mockSecurityContext(admin);
         when(googleMapsConfig.getApiKey()).thenReturn("test-api-key");
 
         String view = organiserController.showCreateEventForm(model);
 
         assertEquals("organiser/create-event", view);
         verify(model).addAttribute("currentUser", admin);
         verify(model).addAttribute("isAuthenticated", true);
         verify(model).addAttribute("googleMapsApiKey", "test-api-key");
     }
 
     @Test
     void showCreateEventFormWithNonOrganiserUserRedirectsToLogin() {
         User student = createStudentUser();
         mockSecurityContext(student);
 
         String view = organiserController.showCreateEventForm(model);
 
         assertEquals("redirect:/auth/login", view);
     }
 
     @Test
     void showCreateEventFormWithNullUserRedirectsToLogin() {
         mockSecurityContext(null);
 
         String view = organiserController.showCreateEventForm(model);
 
         assertEquals("redirect:/auth/login", view);
     }
 
     //Create Event Tests
 
     @Test
     void createEventWithValidDataReturnsSuccess() {
         User organiser = createOrganiserUser();
         Event createdEvent = createSampleEvent();
         mockSecurityContext(organiser);
         when(eventService.createEvent(any(Event.class))).thenReturn(createdEvent);
 
         String view = organiserController.createEvent(
                 "Test Event", "Test Description", "2025-12-25", 
                 "10:00", "12:00", "Test Location", 100, "Test Venue", 
                 redirectAttributes);
 
         assertEquals("redirect:/organiser/dashboard", view);
         verify(eventService).createEvent(any(Event.class));
         verify(redirectAttributes).addFlashAttribute("successMessage", "Event 'Test Event' created successfully!");
     }
 
     @Test
     void createEventWithPastDateReturnsError() {
         User organiser = createOrganiserUser();
         mockSecurityContext(organiser);
 
         String view = organiserController.createEvent(
                 "Test Event", "Test Description", "2020-01-01", 
                 "10:00", "12:00", "Test Location", 100, "Test Venue", 
                 redirectAttributes);
 
         assertEquals("redirect:/organiser/create-event", view);
         verify(redirectAttributes).addFlashAttribute("errorMessage", "Event date must be in the future");
         verify(eventService, never()).createEvent(any(Event.class));
     }
 
     @Test
     void createEventWithInvalidTimeReturnsError() {
         User organiser = createOrganiserUser();
         mockSecurityContext(organiser);
 
         String view = organiserController.createEvent(
                 "Test Event", "Test Description", "2025-12-25", 
                 "12:00", "10:00", "Test Location", 100, "Test Venue", 
                 redirectAttributes);
 
         assertEquals("redirect:/organiser/create-event", view);
         verify(redirectAttributes).addFlashAttribute("errorMessage", "End time must be after start time");
         verify(eventService, never()).createEvent(any(Event.class));
     }
 
     @Test
     void createEventWithInvalidCapacityReturnsError() {
         User organiser = createOrganiserUser();
         mockSecurityContext(organiser);
 
         String view = organiserController.createEvent(
                 "Test Event", "Test Description", "2025-12-25", 
                 "10:00", "12:00", "Test Location", 0, "Test Venue", 
                 redirectAttributes);
 
         assertEquals("redirect:/organiser/create-event", view);
         verify(redirectAttributes).addFlashAttribute("errorMessage", "Maximum capacity must be greater than 0");
         verify(eventService, never()).createEvent(any(Event.class));
     }
 
     @Test
     void createEventWithNonOrganiserUserRedirectsToLogin() {
         User student = createStudentUser();
         mockSecurityContext(student);
 
         String view = organiserController.createEvent(
                 "Test Event", "Test Description", "2024-12-25", 
                 "10:00", "12:00", "Test Location", 100, "Test Venue", 
                 redirectAttributes);
 
         assertEquals("redirect:/auth/login", view);
         verify(eventService, never()).createEvent(any(Event.class));
     }
 
     @Test
     void createEventWithNullUserRedirectsToLogin() {
         mockSecurityContext(null);
 
         String view = organiserController.createEvent(
                 "Test Event", "Test Description", "2024-12-25", 
                 "10:00", "12:00", "Test Location", 100, "Test Venue", 
                 redirectAttributes);
 
         assertEquals("redirect:/auth/login", view);
         verify(eventService, never()).createEvent(any(Event.class));
     }
 
     @Test
     void createEventWithExceptionReturnsError() {
         User organiser = createOrganiserUser();
         mockSecurityContext(organiser);
         when(eventService.createEvent(any(Event.class))).thenThrow(new RuntimeException("Database error"));
 
         String view = organiserController.createEvent(
                 "Test Event", "Test Description", "2025-12-25", 
                 "10:00", "12:00", "Test Location", 100, "Test Venue", 
                 redirectAttributes);
 
         assertEquals("redirect:/organiser/create-event", view);
         verify(redirectAttributes).addFlashAttribute("errorMessage", "Error creating event: Database error");
     }

     //Publish Event Tests
 
     @Test
     void publishEventWithOrganiserUserReturnsSuccess() {
         User organiser = createOrganiserUser();
         mockSecurityContext(organiser);
 
         String view = organiserController.publishEvent(1L, redirectAttributes);
 
         assertEquals("redirect:/organiser/dashboard", view);
         verify(eventService).publishEvent(1L, organiser.getUserId());
         verify(redirectAttributes).addFlashAttribute("successMessage", "Event published successfully!");
     }
 
     @Test
     void publishEventWithNonOrganiserUserRedirectsToLogin() {
         User student = createStudentUser();
         mockSecurityContext(student);
 
         String view = organiserController.publishEvent(1L, redirectAttributes);
 
         assertEquals("redirect:/auth/login", view);
         verify(eventService, never()).publishEvent(any(), any());
     }
 
     @Test
     void publishEventWithNullUserRedirectsToLogin() {
         mockSecurityContext(null);
 
         String view = organiserController.publishEvent(1L, redirectAttributes);
 
         assertEquals("redirect:/auth/login", view);
         verify(eventService, never()).publishEvent(any(), any());
     }
 
     @Test
     void publishEventWithExceptionReturnsError() {
         User organiser = createOrganiserUser();
         mockSecurityContext(organiser);
         doThrow(new RuntimeException("Publish failed")).when(eventService).publishEvent(1L, organiser.getUserId());
 
         String view = organiserController.publishEvent(1L, redirectAttributes);
 
         assertEquals("redirect:/organiser/dashboard", view);
         verify(redirectAttributes).addFlashAttribute("errorMessage", "Failed to publish event: Publish failed");
     }
     
     //Cancel Event Tests
 
     @Test
     void cancelEventWithOrganiserUserReturnsSuccess() {
         User organiser = createOrganiserUser();
         mockSecurityContext(organiser);
 
         String view = organiserController.cancelEvent(1L, redirectAttributes);
 
         assertEquals("redirect:/organiser/dashboard", view);
         verify(eventService).cancelEvent(1L, organiser.getUserId());
         verify(redirectAttributes).addFlashAttribute("successMessage", "Event cancelled successfully!");
     }
 
     @Test
     void cancelEventWithNonOrganiserUserRedirectsToLogin() {
         User student = createStudentUser();
         mockSecurityContext(student);
 
         String view = organiserController.cancelEvent(1L, redirectAttributes);
 
         assertEquals("redirect:/auth/login", view);
         verify(eventService, never()).cancelEvent(any(), any());
     }
 
     @Test
     void cancelEventWithNullUserRedirectsToLogin() {
         mockSecurityContext(null);
 
         String view = organiserController.cancelEvent(1L, redirectAttributes);
 
         assertEquals("redirect:/auth/login", view);
         verify(eventService, never()).cancelEvent(any(), any());
     }
 
     @Test
     void cancelEventWithExceptionReturnsError() {
         User organiser = createOrganiserUser();
         mockSecurityContext(organiser);
         doThrow(new RuntimeException("Cancel failed")).when(eventService).cancelEvent(1L, organiser.getUserId());
 
         String view = organiserController.cancelEvent(1L, redirectAttributes);
 
         assertEquals("redirect:/organiser/dashboard", view);
         verify(redirectAttributes).addFlashAttribute("errorMessage", "Failed to cancel event: Cancel failed");
     }

 
     
    //Helper Methods

    private void mockSecurityContext(User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        if (user != null) {
            CustomUserDetailsService.CustomUserPrincipal principal = 
                    mock(CustomUserDetailsService.CustomUserPrincipal.class);
            when(authentication.getPrincipal()).thenReturn(principal);
            when(principal.getUser()).thenReturn(user);
        } else {
            when(authentication.getPrincipal()).thenReturn(null);
        }
        SecurityContextHolder.setContext(securityContext);
    }

    private User createOrganiserUser() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("organiser@test.com");
        user.setUserType(UserType.ORGANISER);
        return user;
    }

    private User createStudentUser() {
        User user = new User();
        user.setUserId(2L);
        user.setEmail("student@test.com");
        user.setUserType(UserType.STUDENT);
        return user;
    }

    private User createAdminUser() {
        User user = new User();
        user.setUserId(3L);
        user.setEmail("admin@test.com");
        user.setUserType(UserType.ADMIN);
        return user;
    }

    private List<Event> createSampleEvents() {
        Event event1 = new Event();
        event1.setEventId(1L);
        event1.setTitle("Published Event");
        event1.setStatus(Event.EventStatus.published);
        event1.setEventDate(LocalDate.now().plusDays(1));

        Event event2 = new Event();
        event2.setEventId(2L);
        event2.setTitle("Draft Event");
        event2.setStatus(Event.EventStatus.draft);
        event2.setEventDate(LocalDate.now().plusDays(2));

        return Arrays.asList(event1, event2);
    }

    private List<Event> createUpcomingEvents() {
        Event event = new Event();
        event.setEventId(1L);
        event.setTitle("Upcoming Event");
        event.setStatus(Event.EventStatus.published);
        event.setEventDate(LocalDate.now().plusDays(1));
        return Arrays.asList(event);
    }

    private List<Event> createPastEvents() {
        Event event = new Event();
        event.setEventId(1L);
        event.setTitle("Past Event");
        event.setStatus(Event.EventStatus.completed);
        event.setEventDate(LocalDate.now().minusDays(1));
        return Arrays.asList(event);
    }

    private Event createSampleEvent() {
        Event event = new Event();
        event.setEventId(1L);
        event.setTitle("Test Event");
        event.setDescription("Test Description");
        event.setEventDate(LocalDate.now().plusDays(1));
        event.setStartTime(LocalTime.of(10, 0));
        event.setEndTime(LocalTime.of(12, 0));
        event.setLocation("Test Location");
        event.setMaxCapacity(100);
        event.setStatus(Event.EventStatus.draft);
        return event;
    }
}
