package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.services.UserService;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AuthControllerTest {

    // Mock controller, sercvice, model, redirect attributes, authentication, spring security context
    @InjectMocks
    private AuthController authController;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    // Set up mocks and spring security context 
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    /**  Login & Register Page  */

    @Test
    void showLoginPageReturnsLoginView() {
        String view = authController.showLoginPage(model);
        verify(model).addAttribute(eq("user"), any(User.class));
        assertEquals("auth/login", view);
    }

    @Test
    void showRegisterPage_returnsRegisterView() {
        String view = authController.showRegisterPage(model);
        verify(model).addAttribute(eq("user"), any(User.class));
        assertEquals("auth/register", view);
    }

    /**  Registration  */

    @Test
    void processRegistrationPasswordsDontMatch() {
        User user = new User();
        user.setPasswordHash("password1");

        String view = authController.processRegistration(user, mock(org.springframework.validation.BindingResult.class),
                "password2", model, redirectAttributes);

        verify(model).addAttribute(eq("errorMessage"), eq("Passwords do not match"));
        assertEquals("auth/register", view);
    }

    @Test
    void processRegistrationSuccessfulRegistration() {
        User user = new User();
        user.setPasswordHash("password");
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerUser(user)).thenReturn(user);

        String view = authController.processRegistration(user, bindingResult, "password", model, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute(contains("successMessage"), anyString());
        assertEquals("redirect:/auth/login", view);
    }

    /**  Logout  */

    @Test
    void processLogout() {
        var session = mock(jakarta.servlet.http.HttpSession.class);

        String view = authController.processLogout(session, redirectAttributes);

        verify(session).invalidate();
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
        assertEquals("redirect:/", view);
    }

    /**  Forgot Password  */

    @Test
    void showForgotPasswordPage() {
        assertEquals("auth/forgot-password", authController.showForgotPasswordPage());
    }

    @Test
    void processForgotPassword() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        String view = authController.processForgotPassword("test@example.com", model, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
        assertEquals("redirect:/auth/login", view);
    }

    /**  Profile  */

    @Test
    void showProfileNoUser() {
        mockSecurityContext(null);

        String view = authController.showProfile(model);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void showProfileAddUser() {
        User user = new User();
        user.setUserId(1L);
        mockSecurityContext(user);

        when(userService.findById(1L)).thenReturn(Optional.of(user));

        String view = authController.showProfile(model);

        verify(model).addAttribute("user", user);
        assertEquals("user/profile", view);
    }

    /**  Change Password  */

    @Test
    void changePasswordNotLoggedIn() {
        mockSecurityContext(null);

        String view = authController.changePassword("old", "new", "new", redirectAttributes);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void changePasswordDoNotMatch() {
        User user = new User();
        mockSecurityContext(user);

        String view = authController.changePassword("old", "new1", "new2", redirectAttributes);
        assertEquals("redirect:/auth/profile", view);
    }

    /**  API endpoints  */

    @Test
    void checkEmail() {
        when(userService.emailExists("test@example.com")).thenReturn(true);
        Map<String, Object> result = authController.checkEmail("test@example.com");
        assertTrue((Boolean) result.get("exists"));
    }

    @Test
    void checkUsername() {
        when(userService.usernameExists("username")).thenReturn(false);
        Map<String, Object> result = authController.checkUsername("username");
        assertFalse((Boolean) result.get("exists"));
    }

    /**  Private Helper method to mock the spring security context  */

    private void mockSecurityContext(User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        if (user != null) {
            CustomUserDetailsService.CustomUserPrincipal principal =
                    mock(CustomUserDetailsService.CustomUserPrincipal.class);
            when(principal.getUser()).thenReturn(user);
            when(authentication.getPrincipal()).thenReturn(principal);
        } else {
            when(authentication.getPrincipal()).thenReturn(null);
        }
        SecurityContextHolder.setContext(securityContext);
    }
}
