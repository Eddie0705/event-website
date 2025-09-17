package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.mockito.MockedStatic;

class OrganiserControllerTest {

    // mock controller, session, model
    @InjectMocks
    private OrganiserController organiserController;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**  organiserDashboard  */

    @Test
    void organiserDashboardNonOrganiser() {
        try (MockedStatic<SessionUtils> sessionUtils = mockStatic(SessionUtils.class)) {
            sessionUtils.when(() -> SessionUtils.canAccessOrganiser(session)).thenReturn(false);

            String view = organiserController.organiserDashboard(session, model);
            assertEquals("redirect:/auth/login", view);
        }
    }

    @Test
    void organiserDashboardOrganiser() {
        User organiser = new User("organiser@test.com", "organiser", "password", "Org", "User");

        try (MockedStatic<SessionUtils> sessionUtils = mockStatic(SessionUtils.class)) {
            sessionUtils.when(() -> SessionUtils.canAccessOrganiser(session)).thenReturn(true);
            sessionUtils.when(() -> SessionUtils.getCurrentUser(session)).thenReturn(organiser);

            String view = organiserController.organiserDashboard(session, model);

            assertEquals("organiser/dashboard", view);
            verify(model).addAttribute("currentUser", organiser);
        }
    }

    /**  eventManagement  */

    @Test
    void eventManagementNonOrganiser() {
        try (MockedStatic<SessionUtils> sessionUtils = mockStatic(SessionUtils.class)) {
            sessionUtils.when(() -> SessionUtils.canAccessOrganiser(session)).thenReturn(false);

            String view = organiserController.eventManagement(session, model);
            assertEquals("redirect:/auth/login", view);
        }
    }

    @Test
    void eventManagementOrganiser() {
        User organiser = new User("organiser@test.com", "organiser", "password", "Org", "User");

        try (MockedStatic<SessionUtils> sessionUtils = mockStatic(SessionUtils.class)) {
            sessionUtils.when(() -> SessionUtils.canAccessOrganiser(session)).thenReturn(true);
            sessionUtils.when(() -> SessionUtils.getCurrentUser(session)).thenReturn(organiser);

            String view = organiserController.eventManagement(session, model);

            assertEquals("organiser/events", view);
            verify(model).addAttribute("currentUser", organiser);
        }
    }
}
