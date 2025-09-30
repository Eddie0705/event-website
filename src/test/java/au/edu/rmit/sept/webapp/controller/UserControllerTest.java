package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import au.edu.rmit.sept.webapp.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit tests for UserController
class UserControllerTest {

    // Mock controller, service, session, model, redirect attributes
    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    // Set up mocks
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**  listUsers  */

    @Test
    void listUsersNonAdmin() {
        User user = new User();
        user.setUserId(1L);
        // Not admin
        when(session.getAttribute("user")).thenReturn(user);

        String view = userController.listUsers(0, 10, null, null, session, model);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void listUsersAdmin() {
        User admin = new User();
        admin.setUserId(1L);
        admin.setUserType(UserType.ADMIN);
        when(session.getAttribute("user")).thenReturn(admin);

        List<User> users = List.of(new User(), new User());
        when(userService.getAllUsers()).thenReturn(users);
        when(userService.getUserStats()).thenReturn(mock(UserService.UserStats.class));

        String view = userController.listUsers(0, 10, null, null, session, model);
        assertEquals("admin/users", view);
        verify(model).addAttribute("users", users);
    }

    @Test
    void listUsersSearchTerm() {
        User admin = new User();
        admin.setUserType(UserType.ADMIN);
        when(session.getAttribute("user")).thenReturn(admin);

        List<User> users = List.of(new User());
        when(userService.searchUsers("search")).thenReturn(users);
        when(userService.getUserStats()).thenReturn(mock(UserService.UserStats.class));

        String view = userController.listUsers(0, 10, "search", null, session, model);

        assertEquals("admin/users", view);
        verify(userService).searchUsers("search");
        verify(model).addAttribute("users", users);
    }

    /** - viewUser  */

    @Test
    void viewUserNotLoggedIn() {
        when(session.getAttribute("user")).thenReturn(null);

        String view = userController.viewUser(1L, session, model);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void viewUserNonAdminOwnProfile() {
        User user = new User();
        user.setUserId(1L);
        when(session.getAttribute("user")).thenReturn(user);

        String view = userController.viewUser(2L, session, model);
        assertEquals("redirect:/auth/profile", view);
    }

    @Test
    void viewUserExistingUser() {
        User admin = new User();
        admin.setUserId(1L);
        admin.setUserType(UserType.ADMIN);
        when(session.getAttribute("user")).thenReturn(admin);

        User target = new User();
        target.setUserId(2L);
        when(userService.findById(2L)).thenReturn(Optional.of(target));

        String view = userController.viewUser(2L, session, model);
        assertEquals("user/user-details", view);
        verify(model).addAttribute("user", target);
        verify(model).addAttribute("isOwnProfile", false);
    }

    /**  activateUser  */

    @Test
    void activateUserNonAdmin() {
        User user = new User();
        user.setUserId(1L);
        when(session.getAttribute("user")).thenReturn(user);

        String view = userController.activateUser(2L, session, redirectAttributes);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void activateUserAdmin() {
        User admin = new User();
        admin.setUserType(UserType.ADMIN);
        admin.setUserId(1L);
        when(session.getAttribute("user")).thenReturn(admin);

        when(userService.activateUser(2L)).thenReturn(true);

        String view = userController.activateUser(2L, session, redirectAttributes);
        assertEquals("redirect:/users", view);
        verify(redirectAttributes).addFlashAttribute("successMessage", "User activated successfully");
    }

}
