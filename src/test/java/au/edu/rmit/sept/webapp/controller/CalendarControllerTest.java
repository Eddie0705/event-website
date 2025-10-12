package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.RSVP;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.services.EventService;
import au.edu.rmit.sept.webapp.services.RSVPService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Calendar
 */
class CalendarControllerTest {

    @InjectMocks
    private CalendarController calendarController;

    @Mock
    private EventService eventService;

    @Mock
    private RSVPService rsvpService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    //show Calendar Method Tests

    @Test
    void showCalendarWithDefaultParametersReturnsMonthView() {
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("month", null, model);

       
        assertEquals("calendar/calendar", view);
        verify(model).addAttribute("view", "month");
        verify(model).addAttribute("isAuthenticated", false);
        verify(model).addAttribute("currentUser", null);
    }

    @Test
    void showCalendarWithWeekViewReturnsWeekView() {
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("week", null, model);

        
        assertEquals("calendar/calendar", view);
        verify(model).addAttribute("view", "week");
    }

    @Test
    void showCalendarWithValidDateParameter() {
        
        String testDate = "2025-11-15";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("month", testDate, model);

        
        assertEquals("calendar/calendar", view);
        verify(model).addAttribute("anchorDate", LocalDate.parse(testDate));
    }

    @Test
    void showCalendarWithInvalidDateParameterUsesToday() {
        
        String invalidDate = "invalid-date";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("month", invalidDate, model);

       
        assertEquals("calendar/calendar", view);
        verify(model).addAttribute("anchorDate", LocalDate.now());
    }

    @Test
    void showCalendarWithAuthenticatedUserShowsUserEvents() {
        
        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");

        Event event1 = new Event();
        event1.setEventId(1L);
        event1.setTitle("Test Event 1");
        event1.setEventDate(LocalDate.now().plusDays(1));

        Event event2 = new Event();
        event2.setEventId(2L);
        event2.setTitle("Test Event 2");
        event2.setEventDate(LocalDate.now().plusDays(2));

        RSVP rsvp1 = new RSVP();
        rsvp1.setEvent(event1);
        rsvp1.setUser(user);
        rsvp1.setRSVPStatus(RSVP.RSVPStatus.yes);

        RSVP rsvp2 = new RSVP();
        rsvp2.setEvent(event2);
        rsvp2.setUser(user);
        rsvp2.setRSVPStatus(RSVP.RSVPStatus.yes);

        List<RSVP> userRsvps = Arrays.asList(rsvp1, rsvp2);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        
        CustomUserDetailsService.CustomUserPrincipal principal = 
                mock(CustomUserDetailsService.CustomUserPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUser()).thenReturn(user);

        when(rsvpService.findAllByUserAndRsvpStatus(user, RSVP.RSVPStatus.yes))
                .thenReturn(userRsvps);

        
        String view = calendarController.showCalendar("month", null, model);

        
        assertEquals("calendar/calendar", view);
        verify(model).addAttribute("currentUser", user);
        verify(model).addAttribute("isAuthenticated", true);
        verify(rsvpService).findAllByUserAndRsvpStatus(user, RSVP.RSVPStatus.yes);
    }

    @Test
    void showCalendarWithAnonymousUserShowsAllEvents() {
        
        Event event1 = new Event();
        event1.setEventId(1L);
        event1.setTitle("Public Event 1");
        event1.setEventDate(LocalDate.now().plusDays(1));

        Event event2 = new Event();
        event2.setEventId(2L);
        event2.setTitle("Public Event 2");
        event2.setEventDate(LocalDate.now().plusDays(2));

        List<Event> allEvents = Arrays.asList(event1, event2);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(allEvents);

        
        String view = calendarController.showCalendar("month", null, model);

        
        assertEquals("calendar/calendar", view);
        verify(model).addAttribute("currentUser", null);
        verify(model).addAttribute("isAuthenticated", false);
        verify(eventService).getEventsByDateRange(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void showCalendarAddsAllRequiredModelAttributes() {
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("month", null, model);

        
        assertEquals("calendar/calendar", view);
        
        // Verify all required model attributes are added
        verify(model).addAttribute("view", "month");
        verify(model).addAttribute("today", LocalDate.now());
        verify(model).addAttribute("anchorDate", LocalDate.now());
        verify(model).addAttribute(eq("calendarData"), any(CalendarController.CalendarData.class));
        verify(model).addAttribute(eq("eventsByDate"), any(Map.class));
        verify(model).addAttribute(eq("eventsByDateStr"), any(Map.class));
        verify(model).addAttribute(eq("prevDate"), any(LocalDate.class));
        verify(model).addAttribute(eq("nextDate"), any(LocalDate.class));
        verify(model).addAttribute(eq("yearMonth"), any(YearMonth.class));
    }

    //Calendar Data Building Tests

    @Test
    void buildCalendarDataForMonthView() {
        // This tests the private method indirectly through showCalendar
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("month", "2024-03-15", model);

        
        assertEquals("calendar/calendar", view);
        verify(model).addAttribute(eq("calendarData"), any(CalendarController.CalendarData.class));
    }

    @Test
    void buildCalendarDataForWeekView() {
        // This tests the private method indirectly through showCalendar
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("week", "2024-03-15", model);

        
        assertEquals("calendar/calendar", view);
        verify(model).addAttribute(eq("calendarData"), any(CalendarController.CalendarData.class));
    }

    //Event Filtering Tests

    @Test
    void showCalendarFiltersEventsByDateRange() {
        
        Event eventInRange = new Event();
        eventInRange.setEventId(1L);
        eventInRange.setEventDate(LocalDate.now().plusDays(1));

        Event eventOutOfRange = new Event();
        eventOutOfRange.setEventId(2L);
        eventOutOfRange.setEventDate(LocalDate.now().plusDays(30));

        List<Event> eventsInRange = Arrays.asList(eventInRange);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(eventsInRange);

        
        String view = calendarController.showCalendar("month", null, model);

        
        assertEquals("calendar/calendar", view);
        verify(eventService).getEventsByDateRange(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void showCalendarWithUserRsvpsFiltersCorrectly() {
        
        User user = new User();
        user.setUserId(1L);

        Event rsvpEvent = new Event();
        rsvpEvent.setEventId(1L);
        rsvpEvent.setEventDate(LocalDate.now().plusDays(1));

        Event nonRsvpEvent = new Event();
        nonRsvpEvent.setEventId(2L);
        nonRsvpEvent.setEventDate(LocalDate.now().plusDays(2));

        RSVP rsvp = new RSVP();
        rsvp.setEvent(rsvpEvent);
        rsvp.setUser(user);
        rsvp.setRSVPStatus(RSVP.RSVPStatus.yes);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        
        CustomUserDetailsService.CustomUserPrincipal principal = 
                mock(CustomUserDetailsService.CustomUserPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUser()).thenReturn(user);

        when(rsvpService.findAllByUserAndRsvpStatus(user, RSVP.RSVPStatus.yes))
                .thenReturn(Arrays.asList(rsvp));

       
        String view = calendarController.showCalendar("month", null, model);

        
        assertEquals("calendar/calendar", view);
        verify(rsvpService).findAllByUserAndRsvpStatus(user, RSVP.RSVPStatus.yes);
    }

    //Navigation Tests

    @Test
    void showCalendarCalculatesCorrectNavigationDatesForMonth() {
    
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("month", "2024-03-15", model);

        assertEquals("calendar/calendar", view);
        verify(model).addAttribute(eq("prevDate"), any(LocalDate.class));
        verify(model).addAttribute(eq("nextDate"), any(LocalDate.class));
    }

    @Test
    void showCalendarCalculatesCorrectNavigationDatesForWeek() {
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("week", "2024-03-15", model);

        assertEquals("calendar/calendar", view);
        verify(model).addAttribute(eq("prevDate"), any(LocalDate.class));
        verify(model).addAttribute(eq("nextDate"), any(LocalDate.class));
    }


    @Test
    void showCalendarWithEmptyEventList() {
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("month", null, model);

        assertEquals("calendar/calendar", view);
        verify(model).addAttribute(eq("eventsByDate"), any(Map.class));
        verify(model).addAttribute(eq("eventsByDateStr"), any(Map.class));
    }

    @Test
    void showCalendarWithNullDateParameter() {
       
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("month", null, model);

        assertEquals("calendar/calendar", view);
        verify(model).addAttribute("anchorDate", LocalDate.now());
    }

    @Test
    void showCalendarWithBlankDateParameter() {
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("month", "   ", model);

        assertEquals("calendar/calendar", view);
        verify(model).addAttribute("anchorDate", LocalDate.now());
    }

    @Test
    void showCalendarWithAnonymousUser() {
       
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authentication.getName()).thenReturn("anonymousUser");
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("month", null, model);
        assertEquals("calendar/calendar", view);
        verify(model).addAttribute("currentUser", null);
        verify(model).addAttribute("isAuthenticated", false);
    }

    @Test
    void showCalendarWithNullAuthentication() {
        
        when(securityContext.getAuthentication()).thenReturn(null);
        when(eventService.getEventsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        
        String view = calendarController.showCalendar("month", null, model);

        assertEquals("calendar/calendar", view);
        verify(model).addAttribute("currentUser", null);
        verify(model).addAttribute("isAuthenticated", false);
    }


}