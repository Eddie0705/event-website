package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganiserControllerTest {

    @InjectMocks
    private OrganiserController organiserController;

    @Mock
    private EventService eventService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private User organiserUser;
    private CustomUserDetailsService.CustomUserPrincipal organiserPrincipal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        organiserUser = new User();
        organiserUser.setUserId(1L);
        organiserUser.setEmail("org@test.com");
        organiserUser.setUserType(UserType.ORGANISER);

        organiserPrincipal = new CustomUserDetailsService.CustomUserPrincipal(organiserUser);

        // Mock security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // test for redirection if user is not logged in
    @Test
    void showDashboardRedirectsWhenUserNotLoggedIn() {
        when(securityContext.getAuthentication()).thenReturn(null);

        String view = organiserController.showDashboard(model, 0, 10, "all");
        assertEquals("redirect:/auth/login", view);
    }


    // test for redirection if user is not organiser
    @Test
    void showDashboard_redirectsWhenUserNotOrganiser() {
        User normalUser = new User();
        normalUser.setUserType(UserType.STUDENT);
        CustomUserDetailsService.CustomUserPrincipal principal = new CustomUserDetailsService.CustomUserPrincipal(normalUser);

        // Mock authentication to return a non-organiser user
        when(authentication.getPrincipal()).thenReturn(principal);

        String view = organiserController.showDashboard(model, 0, 10, "all");
        assertEquals("redirect:/auth/login", view);
    }


    // test for filtering published events
    @Test
    void showCreateEventForm_redirectsWhenNotOrganiser() {
        User nonOrg = new User();
        nonOrg.setUserType(UserType.STUDENT);
        CustomUserDetailsService.CustomUserPrincipal principal = new CustomUserDetailsService.CustomUserPrincipal(nonOrg);
        // Mock authentication to return a non-organiser user
        when(authentication.getPrincipal()).thenReturn(principal);

        String view = organiserController.showCreateEventForm(model);
        assertEquals("redirect:/auth/login", view);
    }


    // test for publishing event

    @Test
    void publishEvent_redirectsWhenNotOrganiser() {
        when(authentication.getPrincipal()).thenReturn(null);

        String view = organiserController.publishEvent(5L, redirectAttributes);
        assertEquals("redirect:/auth/login", view);
    }

    
    // test for cancelling event
    @Test
    void cancelEvent_redirectsWhenNotOrganiser() {
        when(authentication.getPrincipal()).thenReturn(null);

        String view = organiserController.cancelEvent(5L, redirectAttributes);
        assertEquals("redirect:/auth/login", view);
    }

}
