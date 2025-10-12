package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.*;
import au.edu.rmit.sept.webapp.repository.RSVPRepository;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.services.EventService;
import au.edu.rmit.sept.webapp.services.RSVPService;
import au.edu.rmit.sept.webapp.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PageControllerTest {

    @InjectMocks
    private PageController pageController;

    @Mock
    private UserService userService;

    @Mock
    private EventService eventService;

    @Mock
    private RSVPService rsvpService;

    @Mock
    private RSVPRepository rsvpRepository;

    @Mock
    private Model model;
    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    private void mockCurrentUser(User user) {
        CustomUserDetailsService.CustomUserPrincipal principal =
                new CustomUserDetailsService.CustomUserPrincipal(user);
        when(authentication.getPrincipal()).thenReturn(principal);
    }
    @Test
    void unauthenticatedUserReturnsIndex() {
        // No current user
        String view = pageController.root(model);
        assertEquals("Index", view);
        verify(model).addAttribute("currentUser", null);
        verify(model).addAttribute("isAuthenticated", false);
    }

    @Test
    void adminUserRedirectsToAdminDashboard() {
        User admin = new User();
        admin.setUserId(1L);
        admin.setUserType(UserType.ADMIN);
        mockCurrentUser(admin);

        String view = pageController.root(model);

        assertEquals("redirect:/admin/dashboard", view);
    }

    @Test
    void organiserUserRedirectsToOrganiserDashboard() {
        User organiser = new User();
        organiser.setUserId(2L);
        organiser.setUserType(UserType.ORGANISER);
        mockCurrentUser(organiser);

        String view = pageController.root(model);

        assertEquals("redirect:/organiser/dashboard", view);
    }
    @Test
    void userReturnsIndex() {
        User normalUser = new User();
        normalUser.setUserId(3L);
        normalUser.setUserType(UserType.STUDENT);
        mockCurrentUser(normalUser);

        List<Event> events = List.of(new Event());
        when(eventService.getPublishedEvents()).thenReturn(events);

        String view = pageController.root(model);

        assertEquals("Index", view);
        verify(model).addAttribute("currentUser", normalUser);
        verify(model).addAttribute("isAuthenticated", true);
        verify(model).addAttribute("events", events);
    }

    @Test
    void exploreReturnsSortedEvents() {
        User user = new User();
        mockCurrentUser(user);

        Event upcoming = mock(Event.class);
        when(upcoming.isEventFinished()).thenReturn(false);
        when(upcoming.getEventDate()).thenReturn(LocalDate.now().plusDays(1));

        Event past = mock(Event.class);
        when(past.isEventFinished()).thenReturn(true);
        when(past.getEventDate()).thenReturn(LocalDate.now().minusDays(1));

        List<Event> events = List.of(past, upcoming);
        when(eventService.getPublishedEvents()).thenReturn(events);

        String view = pageController.explore(model);

        assertEquals("Index", view);
        verify(model).addAttribute("events", events);
    }


    @Test
    void profileRedirectsIfNotLoggedIn() {
        SecurityContextHolder.setContext(mock(SecurityContext.class));
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(null);

        String view = pageController.profile(model);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void profileReturnsProfileViewForUser() {
        User user = new User();
        mockCurrentUser(user);

        String view = pageController.profile(model);

        assertEquals("user/profile", view);
        verify(model).addAttribute("user", user);
        verify(model).addAttribute("currentUser", user);
        verify(model).addAttribute("isAuthenticated", true);
    }

}
