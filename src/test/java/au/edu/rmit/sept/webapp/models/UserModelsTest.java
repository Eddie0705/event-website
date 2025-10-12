package au.edu.rmit.sept.webapp.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class UserModelsTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPasswordHash("hashedpass");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserType(UserType.STUDENT);
        user.setProfilePicture("profile.jpg");
        user.setBio("I love testing!");
        user.setInterests("[\"coding\", \"music\"]");
        user.setIsActive(true);
        user.setIsBanned(false);
        user.setEmailVerified(true);
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, user.getUserId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("testuser", user.getUsername());
        assertEquals("hashedpass", user.getPasswordHash());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(UserType.STUDENT, user.getUserType());
        assertEquals("profile.jpg", user.getProfilePicture());
        assertEquals("I love testing!", user.getBio());
        assertEquals("[\"coding\", \"music\"]", user.getInterests());
        assertTrue(user.getIsActive());
        assertFalse(user.getIsBanned());
        assertTrue(user.getEmailVerified());
    }

    @Test
    void testFullNameConcatenation() {
        assertEquals("John Doe", user.getFullName());
    }

    @Test
    void testUserTypeHelpers() {
        user.setUserType(UserType.ADMIN);
        assertTrue(user.isAdmin());
        assertFalse(user.isOrganiser());
        assertFalse(user.isStudent());

        user.setUserType(UserType.ORGANISER);
        assertTrue(user.isOrganiser());
        assertFalse(user.isAdmin());
        assertFalse(user.isStudent());

        user.setUserType(UserType.STUDENT);
        assertTrue(user.isStudent());
        assertFalse(user.isOrganiser());
        assertFalse(user.isAdmin());
    }

    @Test
    void testToStringContainsKeyDetails() {
        String toString = user.toString();
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("STUDENT"));
    }

    @Test
    void testConstructorSetsRequiredFields() {
        User newUser = new User("a@b.com", "alice", "pass123", "Alice", "Smith");
        assertEquals("a@b.com", newUser.getEmail());
        assertEquals("alice", newUser.getUsername());
        assertEquals("pass123", newUser.getPasswordHash());
        assertEquals("Alice", newUser.getFirstName());
        assertEquals("Smith", newUser.getLastName());
    }
}
