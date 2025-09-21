package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import au.edu.rmit.sept.webapp.services.UserService;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import au.edu.rmit.sept.webapp.utils.SessionUtils;
import org.springframework.ui.Model;

/*
 *  Unit tests for AdminControllerTest
 */
class AdminControllerTest {

    // Mock controller 
    @InjectMocks
    private AdminController adminController;

    // Mock service
    @Mock
    private UserService userService;

    // Mock HTTP session
    @Mock
    private HttpSession session;

    // mock model
    @Mock
    private Model model;

    // Set up mocks
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // // test for redirection if not admin
    // @Test
    // void adminDashboardNonAdminRedirectsToLogin() {
    //     try (MockedStatic<SessionUtils> sessionUtils = mockStatic(SessionUtils.class)) {
    //         sessionUtils.when(() -> SessionUtils.isAdmin(session)).thenReturn(false);

    //         String view = adminController.adminDashboard(session, model);

    //         assertEquals("redirect:/auth/login", view);
    //     }
    // }

    // // test for admin dashboard 
    // @Test
    // void adminDashboardAdminReturnsDashboardView() {
    //     User adminUser = new User("admin@test.com", "admin", "password", "Admin", "User");

    //     try (MockedStatic<SessionUtils> sessionUtils = mockStatic(SessionUtils.class)) {
    //         sessionUtils.when(() -> SessionUtils.isAdmin(session)).thenReturn(true);
    //         sessionUtils.when(() -> SessionUtils.getCurrentUser(session)).thenReturn(adminUser);

    //         // Mock UserStats
    //         UserService.UserStats stats = mock(UserService.UserStats.class);
    //         when(userService.getUserStats()).thenReturn(stats);

    //         String view = adminController.adminDashboard(session, model);

    //         assertEquals("admin/dashboard", view);
    //         verify(model).addAttribute("currentUser", adminUser);
    //         verify(model).addAttribute("userStats", stats);
    //     }
    // }

    // // test for redirecting to login if not admin
    // @Test
    // void userManagementNonAdminRedirectsToLogin() {
    //     try (MockedStatic<SessionUtils> sessionUtils = mockStatic(SessionUtils.class)) {
    //         sessionUtils.when(() -> SessionUtils.isAdmin(session)).thenReturn(false);

    //         String view = adminController.userManagement(session, model);

    //         assertEquals("redirect:/auth/login", view);
    //     }
    // }

    // // test for user management request 
    // @Test
    // void userManagementReturnsUsersView() {
    //     User adminUser = new User("admin@test.com", "admin", "password", "Admin", "User");

    //     try (MockedStatic<SessionUtils> sessionUtils = mockStatic(SessionUtils.class)) {
    //         sessionUtils.when(() -> SessionUtils.isAdmin(session)).thenReturn(true);
    //         sessionUtils.when(() -> SessionUtils.getCurrentUser(session)).thenReturn(adminUser);

    //         // Mock UserStats
    //         UserService.UserStats stats = mock(UserService.UserStats.class);
    //         when(userService.getUserStats()).thenReturn(stats);

    //         String view = adminController.userManagement(session, model);

    //         assertEquals("admin/users", view);
    //         verify(model).addAttribute("currentUser", adminUser);
    //         verify(model).addAttribute("userStats", stats);
    //     }
    // }
}