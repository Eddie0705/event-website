package au.edu.rmit.sept.webapp.services;

import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit tests for UserService
class UserServiceTest {

    // Mock service, repository, password encoder
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**  registerUser  */
    @Test
    void registerUserSuccess() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("testuser");
        user.setPasswordHash("plain");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User saved = userService.registerUser(user);

        assertEquals("hashed", saved.getPasswordHash());
        assertTrue(saved.getIsActive());
        assertFalse(saved.getIsBanned());
        assertEquals(UserType.STUDENT, saved.getUserType());
    }

    @Test
    void registerUserDupeEmail() {
        User user = new User();
        user.setEmail("test@test.com");
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user));
    }

    /**  authenticateUser  */
    @Test
    void authenticateUserSuccess() {
        User user = new User();
        user.setPasswordHash("hashed");
        user.setIsActive(true);
        user.setIsBanned(false);

        when(userRepository.findByEmailOrUsername("test@test.com", "test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain", "hashed")).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        Optional<User> result = userService.authenticateUser("test@test.com", "plain");
        assertTrue(result.isPresent());
    }

    @Test
    void authenticateUserWrongPassword() {
        User user = new User();
        user.setPasswordHash("hashed");
        user.setIsActive(true);
        user.setIsBanned(false);

        when(userRepository.findByEmailOrUsername("test@test.com", "test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        Optional<User> result = userService.authenticateUser("test@test.com", "wrong");
        assertFalse(result.isPresent());
    }

    /**  changePassword  */
    @Test
    void changePasswordSuccess() {
        User user = new User();
        user.setPasswordHash("hashed");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("newHashed");

        boolean result = userService.changePassword(1L, "old", "new");
        assertTrue(result);
        assertEquals("newHashed", user.getPasswordHash());
    }

    /**  resetPassword  */
    @Test
    void resetPasswordSuccess() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new")).thenReturn("hashed");
        when(userRepository.save(user)).thenReturn(user);

        boolean result = userService.resetPassword(1L, "new");
        assertTrue(result);
        assertEquals("hashed", user.getPasswordHash());
    }

    /**  activate / deactivate  */
    @Test
    void activateUserSuccess() {
        User user = new User();
        user.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.activateUser(1L);
        assertTrue(result);
        assertTrue(user.getIsActive());
    }

    @Test
    void deactivateUserSuccess() {
        User user = new User();
        user.setIsActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.deactivateUser(1L);
        assertTrue(result);
        assertFalse(user.getIsActive());
    }

    /**  getUserStats  */
    @Test
    void getUserStatsReturnsCorrectCounts() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByIsActiveTrue()).thenReturn(8L);
        when(userRepository.countByEmailVerifiedTrue()).thenReturn(5L);
        when(userRepository.countByUserType(UserType.STUDENT)).thenReturn(6L);
        when(userRepository.countByUserType(UserType.ORGANISER)).thenReturn(3L);
        when(userRepository.countByUserType(UserType.ADMIN)).thenReturn(1L);

        UserService.UserStats stats = userService.getUserStats();

        assertEquals(10, stats.getTotalUsers());
        assertEquals(8, stats.getActiveUsers());
        assertEquals(5, stats.getVerifiedUsers());
        assertEquals(6, stats.getStudents());
        assertEquals(3, stats.getOrganisers());
        assertEquals(1, stats.getAdmins());
    }
}
