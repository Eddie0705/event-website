package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.models.Event;
import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.services.EventService;
import au.edu.rmit.sept.webapp.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for AdminController
 * Tests all admin dashboard functionality including analytics, event management, and user management
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private UserService userService;


    private User adminUser;
    private User regularUser;
    private Event testEvent;
    private UserService.UserStats userStats;

    @BeforeEach
    void setUp() {
        
        // Create test users
        adminUser = new User();
        adminUser.setUserId(1L);
        adminUser.setEmail("admin@test.com");
        adminUser.setUsername("admin");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setUserType(UserType.ADMIN);
        adminUser.setIsActive(true);
        adminUser.setEmailVerified(true);
        adminUser.setPasswordHash("password123");

        regularUser = new User();
        regularUser.setUserId(2L);
        regularUser.setEmail("user@test.com");
        regularUser.setUsername("user");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setUserType(UserType.STUDENT);
        regularUser.setIsActive(true);
        regularUser.setEmailVerified(true);
        regularUser.setPasswordHash("password123");

        // Create test event
        testEvent = new Event();
        testEvent.setEventId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setEventDate(LocalDate.now().plusDays(7));
        testEvent.setStartTime(LocalTime.of(10, 0));
        testEvent.setEndTime(LocalTime.of(12, 0));
        testEvent.setLocation("Test Location");
        testEvent.setMaxCapacity(100);
        testEvent.setCurrentRsvpCount(50);
        testEvent.setCreatedBy(1L);
        testEvent.setStatus(Event.EventStatus.published);
        testEvent.setIsPaid(false);
        testEvent.setPrice(BigDecimal.ZERO);

        // Create user stats
        userStats = new UserService.UserStats(100, 80, 75, 60, 20, 5);
    }

    private void setupAdminSecurityContext() {
        CustomUserDetailsService.CustomUserPrincipal principal = 
            new CustomUserDetailsService.CustomUserPrincipal(adminUser);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        when(authentication.getPrincipal()).thenReturn(principal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupRegularUserSecurityContext() {
        CustomUserDetailsService.CustomUserPrincipal principal = 
            new CustomUserDetailsService.CustomUserPrincipal(regularUser);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        when(authentication.getPrincipal()).thenReturn(principal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }


    // ==================== DASHBOARD TESTS ====================

    @Test
    void dashboard_WithAdminUser_ReturnsDashboardView() throws Exception {
        setupAdminSecurityContext();
        
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
        
        clearSecurityContext();
    }

    @Test
    void dashboard_WithRegularUser_ReturnsForbidden() throws Exception {
        setupRegularUserSecurityContext();
        
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
        
        clearSecurityContext();
    }

    @Test
    void dashboard_WithNoAuthentication_ReturnsUnauthorized() throws Exception {
        clearSecurityContext();
        
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection());
    }

    // ==================== EVENTS API TESTS ====================

    @Test
    void getAllEvents_WithAdminUser_ReturnsEvents() throws Exception {
        setupAdminSecurityContext();
        
        List<Event> events = Arrays.asList(testEvent);
        
        when(eventService.getAllEvents()).thenReturn(events);
        when(userService.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(get("/admin/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.events").isArray())
                .andExpect(jsonPath("$.events[0].title").value("Test Event"))
                .andExpect(jsonPath("$.events[0].creatorName").value("Admin User"));
        
        clearSecurityContext();
    }

    @Test
    void getAllEvents_WithSearchTerm_ReturnsFilteredEvents() throws Exception {
        setupAdminSecurityContext();
        List<Event> events = Arrays.asList(testEvent);
        
        when(eventService.searchEvents("test")).thenReturn(events);
        when(userService.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(get("/admin/api/events")
                .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.events").isArray())
                .andExpect(jsonPath("$.events[0].title").value("Test Event"));

        verify(eventService).searchEvents("test");
        
        clearSecurityContext();
    }

    @Test
    void getAllEvents_WithActiveFilter_ReturnsPublishedEvents() throws Exception {
        setupAdminSecurityContext();
        List<Event> events = Arrays.asList(testEvent);
        
        when(eventService.getAllEvents()).thenReturn(events);
        when(userService.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(get("/admin/api/events")
                .param("filter", "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.events").isArray())
                .andExpect(jsonPath("$.events[0].status").value("published"));
        
        clearSecurityContext();
    }

    @Test
    void getAllEvents_WithUpcomingFilter_ReturnsFutureEvents() throws Exception {
        setupAdminSecurityContext();
        List<Event> events = Arrays.asList(testEvent);
        
        when(eventService.getAllEvents()).thenReturn(events);
        when(userService.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(get("/admin/api/events")
                .param("filter", "upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.events").isArray());
        
        clearSecurityContext();
    }

    @Test
    void getAllEvents_WithNonAdminUser_ReturnsUnauthorized() throws Exception {
        setupRegularUserSecurityContext();

        mockMvc.perform(get("/admin/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
        
        clearSecurityContext();
    }

    @Test
    void getAllEvents_WithNoAuthentication_ReturnsUnauthorized() throws Exception {
        clearSecurityContext();

        mockMvc.perform(get("/admin/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // ==================== ANALYTICS API TESTS ====================

    @Test
    void getAnalytics_WithAdminUser_ReturnsAnalytics() throws Exception {
        setupAdminSecurityContext();
        List<Event> events = Arrays.asList(testEvent);
        
        when(userService.getUserStats()).thenReturn(userStats);
        when(eventService.getAllEvents()).thenReturn(events);

        mockMvc.perform(get("/admin/api/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.analytics.users.total").value(100))
                .andExpect(jsonPath("$.analytics.users.active").value(80))
                .andExpect(jsonPath("$.analytics.users.students").value(60))
                .andExpect(jsonPath("$.analytics.users.organisers").value(20))
                .andExpect(jsonPath("$.analytics.users.admins").value(5))
                .andExpect(jsonPath("$.analytics.events.total").value(1))
                .andExpect(jsonPath("$.analytics.events.published").value(1))
                .andExpect(jsonPath("$.analytics.avgFillRate").exists());
    }

    @Test
    void getAnalytics_WithNonAdminUser_ReturnsUnauthorized() throws Exception {
        setupRegularUserSecurityContext();

        mockMvc.perform(get("/admin/api/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void getAnalytics_WithNoAuthentication_ReturnsUnauthorized() throws Exception {
        // No authentication annotation - test unauthenticated access

        mockMvc.perform(get("/admin/api/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void getAnalytics_WithServiceException_ReturnsError() throws Exception {
        setupAdminSecurityContext();
        when(userService.getUserStats()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/admin/api/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Failed to fetch analytics: Database error"));
    }

    // ==================== USER MANAGEMENT API TESTS ====================

    @Test
    void getAllUsers_WithAdminUser_ReturnsUsers() throws Exception {
        setupAdminSecurityContext();
        List<User> users = Arrays.asList(adminUser, regularUser);
        
        when(userService.getAllUsers()).thenReturn(users);
        when(userService.getUserStats()).thenReturn(userStats);

        mockMvc.perform(get("/admin/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].email").value("admin@test.com"))
                .andExpect(jsonPath("$.users[1].email").value("user@test.com"));
    }

    @Test
    void getAllUsers_WithSearchTerm_ReturnsFilteredUsers() throws Exception {
        setupAdminSecurityContext();
        List<User> users = Arrays.asList(adminUser);
        
        when(userService.searchUsers("admin")).thenReturn(users);
        when(userService.getUserStats()).thenReturn(userStats);

        mockMvc.perform(get("/admin/api/users")
                .param("search", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].email").value("admin@test.com"));

        verify(userService).searchUsers("admin");
    }

    @Test
    void getAllUsers_WithNonAdminUser_ReturnsUnauthorized() throws Exception {
        setupRegularUserSecurityContext();

        mockMvc.perform(get("/admin/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // ==================== EVENT STATUS UPDATE TESTS ====================

    @Test
    void updateEventStatus_WithAdminUser_UpdatesStatus() throws Exception {
        setupAdminSecurityContext();
        Event updatedEvent = new Event();
        updatedEvent.setEventId(1L);
        updatedEvent.setStatus(Event.EventStatus.cancelled);
        
        when(eventService.getEventById(1L)).thenReturn(testEvent);
        when(eventService.updateEvent(eq(1L), any(Event.class))).thenReturn(updatedEvent);

        mockMvc.perform(put("/admin/api/events/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"cancelled\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.event.status").value("cancelled"));

        verify(eventService).updateEvent(eq(1L), any(Event.class));
    }

    @Test
    void updateEventStatus_WithNonAdminUser_ReturnsUnauthorized() throws Exception {
        setupRegularUserSecurityContext();
        mockMvc.perform(put("/admin/api/events/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"cancelled\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void updateEventStatus_WithInvalidEventId_ReturnsError() throws Exception {
        setupAdminSecurityContext();
        when(eventService.getEventById(999L)).thenThrow(new RuntimeException("Event not found"));

        mockMvc.perform(put("/admin/api/events/999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"cancelled\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Event not found"));
    }

    // ==================== USER MANAGEMENT TESTS ====================

    @Test
    void updateUserStatus_WithAdminUser_UpdatesStatus() throws Exception {
        setupAdminSecurityContext();
        User updatedUser = new User();
        updatedUser.setUserId(2L);
        updatedUser.setIsActive(false);
        
        when(userService.findById(2L)).thenReturn(Optional.of(regularUser));
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/admin/api/users/2/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.isActive").value(false));

        verify(userService).updateUser(any(User.class));
    }

    @Test
    void updateUserStatus_WithNonAdminUser_ReturnsUnauthorized() throws Exception {
        setupRegularUserSecurityContext();
        mockMvc.perform(put("/admin/api/users/2/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void updateUserStatus_WithInvalidUserId_ReturnsError() throws Exception {
        setupAdminSecurityContext();
        when(userService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/admin/api/users/999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isActive\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void getAllEvents_WithServiceException_ReturnsError() throws Exception {
        setupAdminSecurityContext();
        when(eventService.getAllEvents()).thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/admin/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Failed to fetch events: Database connection failed"));
    }

    @Test
    void getAllUsers_WithServiceException_ReturnsError() throws Exception {
        setupAdminSecurityContext();
        when(userService.getAllUsers()).thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/admin/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Failed to fetch users: Database connection failed"));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    void getAllEvents_WithEmptyEventList_ReturnsEmptyArray() throws Exception {
        setupAdminSecurityContext();
        when(eventService.getAllEvents()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/admin/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.events").isArray())
                .andExpect(jsonPath("$.events").isEmpty());
    }

    @Test
    void getAllUsers_WithEmptyUserList_ReturnsEmptyArray() throws Exception {
        setupAdminSecurityContext();
        when(userService.getAllUsers()).thenReturn(new ArrayList<>());
        when(userService.getUserStats()).thenReturn(userStats);

        mockMvc.perform(get("/admin/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users").isEmpty());
    }

    @Test
    void getAnalytics_WithNoEvents_ReturnsZeroCounts() throws Exception {
        setupAdminSecurityContext();
        when(userService.getUserStats()).thenReturn(userStats);
        when(eventService.getAllEvents()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/admin/api/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.analytics.events.total").value(0))
                .andExpect(jsonPath("$.analytics.events.published").value(0))
                .andExpect(jsonPath("$.analytics.events.cancelled").value(0))
                .andExpect(jsonPath("$.analytics.events.upcoming").value(0));
    }

    // ==================== ADDITIONAL EVENT MANAGEMENT TESTS ====================

    @Test
    void cancelEvent_WithAdminUser_CancelsEvent() throws Exception {
        setupAdminSecurityContext();
        when(eventService.cancelEventByAdmin(1L)).thenReturn(testEvent);

        mockMvc.perform(put("/admin/api/events/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Event cancelled successfully"));

        verify(eventService).cancelEventByAdmin(1L);
    }

    @Test
    void cancelEvent_WithNonAdminUser_ReturnsUnauthorized() throws Exception {
        setupRegularUserSecurityContext();
        mockMvc.perform(put("/admin/api/events/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void cancelEvent_WithServiceException_ReturnsError() throws Exception {
        setupAdminSecurityContext();
        doThrow(new RuntimeException("Event not found")).when(eventService).cancelEventByAdmin(1L);

        mockMvc.perform(put("/admin/api/events/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Failed to cancel event: Event not found"));
    }

    @Test
    void uncancelEvent_WithAdminUser_UncancelsEvent() throws Exception {
        setupAdminSecurityContext();
        when(eventService.uncancelEventByAdmin(1L)).thenReturn(testEvent);

        mockMvc.perform(put("/admin/api/events/1/uncancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Event restored successfully"));

        verify(eventService).uncancelEventByAdmin(1L);
    }

    @Test
    void uncancelEvent_WithNonAdminUser_ReturnsUnauthorized() throws Exception {
        setupRegularUserSecurityContext();
        mockMvc.perform(put("/admin/api/events/1/uncancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // ==================== ADDITIONAL USER MANAGEMENT TESTS ====================

    @Test
    void getUser_WithAdminUser_ReturnsUser() throws Exception {
        setupAdminSecurityContext();
        when(userService.findById(2L)).thenReturn(Optional.of(regularUser));

        mockMvc.perform(get("/admin/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.email").value("user@test.com"))
                .andExpect(jsonPath("$.user.username").value("user"));
    }

    @Test
    void getUser_WithNonAdminUser_ReturnsUnauthorized() throws Exception {
        setupRegularUserSecurityContext();
        mockMvc.perform(get("/admin/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void getUser_WithInvalidUserId_ReturnsError() throws Exception {
        setupAdminSecurityContext();
        when(userService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/api/users/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void banUser_WithAdminUser_BansUser() throws Exception {
        setupAdminSecurityContext();
        when(userService.banUser(2L)).thenReturn(true);

        mockMvc.perform(put("/admin/api/users/2/ban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User banned successfully"));

        verify(userService).banUser(2L);
    }

    @Test
    void banUser_WithNonAdminUser_ReturnsUnauthorized() throws Exception {
        setupRegularUserSecurityContext();
        mockMvc.perform(put("/admin/api/users/2/ban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void banUser_WithServiceFailure_ReturnsError() throws Exception {
        setupAdminSecurityContext();
        when(userService.banUser(2L)).thenReturn(false);

        mockMvc.perform(put("/admin/api/users/2/ban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Failed to ban user"));
    }

    @Test
    void unbanUser_WithAdminUser_UnbansUser() throws Exception {
        setupAdminSecurityContext();
        when(userService.unbanUser(2L)).thenReturn(true);

        mockMvc.perform(put("/admin/api/users/2/unban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User unbanned successfully"));

        verify(userService).unbanUser(2L);
    }

    @Test
    void unbanUser_WithNonAdminUser_ReturnsUnauthorized() throws Exception {
        setupRegularUserSecurityContext();
        mockMvc.perform(put("/admin/api/users/2/unban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void activateUser_WithAdminUser_ActivatesUser() throws Exception {
        setupAdminSecurityContext();
        when(userService.activateUser(2L)).thenReturn(true);

        mockMvc.perform(put("/admin/api/users/2/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User activated successfully"));

        verify(userService).activateUser(2L);
    }

    @Test
    void deactivateUser_WithAdminUser_DeactivatesUser() throws Exception {
        setupAdminSecurityContext();
        when(userService.deactivateUser(2L)).thenReturn(true);

        mockMvc.perform(put("/admin/api/users/2/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deactivated successfully"));

        verify(userService).deactivateUser(2L);
    }

    // ==================== COMPREHENSIVE FILTER TESTS ====================

    @Test
    void getAllEvents_WithFlaggedFilter_ReturnsCancelledEvents() throws Exception {
        setupAdminSecurityContext();
        Event cancelledEvent = new Event();
        cancelledEvent.setEventId(2L);
        cancelledEvent.setTitle("Cancelled Event");
        cancelledEvent.setStatus(Event.EventStatus.cancelled);
        cancelledEvent.setCreatedBy(1L);
        
        List<Event> events = Arrays.asList(testEvent, cancelledEvent);
        
        when(eventService.getAllEvents()).thenReturn(events);
        when(userService.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(get("/admin/api/events")
                .param("filter", "flagged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.events").isArray())
                .andExpect(jsonPath("$.events[0].status").value("cancelled"));
    }

    @Test
    void getAllEvents_WithInvalidFilter_ReturnsAllEvents() throws Exception {
        setupAdminSecurityContext();
        List<Event> events = Arrays.asList(testEvent);
        
        when(eventService.getAllEvents()).thenReturn(events);
        when(userService.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(get("/admin/api/events")
                .param("filter", "invalid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.events").isArray());
    }

    // ==================== COMPREHENSIVE SEARCH TESTS ====================

    @Test
    void getAllEvents_WithEmptySearchTerm_ReturnsAllEvents() throws Exception {
        setupAdminSecurityContext();
        List<Event> events = Arrays.asList(testEvent);
        
        when(eventService.getAllEvents()).thenReturn(events);
        when(userService.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(get("/admin/api/events")
                .param("search", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.events").isArray());

        verify(eventService).getAllEvents();
    }

    @Test
    void getAllEvents_WithWhitespaceSearchTerm_ReturnsAllEvents() throws Exception {
        setupAdminSecurityContext();
        List<Event> events = Arrays.asList(testEvent);
        
        when(eventService.getAllEvents()).thenReturn(events);
        when(userService.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(get("/admin/api/events")
                .param("search", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.events").isArray());

        verify(eventService).getAllEvents();
    }

    // ==================== COMPREHENSIVE ERROR HANDLING TESTS ====================

    @Test
    void getAllEvents_WithNullPointerException_ReturnsError() throws Exception {
        setupAdminSecurityContext();
        when(eventService.getAllEvents()).thenThrow(new NullPointerException("Null pointer"));

        mockMvc.perform(get("/admin/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Failed to fetch events: Null pointer"));
    }

    @Test
    void getAnalytics_WithIllegalArgumentException_ReturnsError() throws Exception {
        setupAdminSecurityContext();
        when(userService.getUserStats()).thenThrow(new IllegalArgumentException("Invalid argument"));

        mockMvc.perform(get("/admin/api/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Failed to fetch analytics: Invalid argument"));
    }

    @Test
    void getAllUsers_WithSecurityException_ReturnsError() throws Exception {
        setupAdminSecurityContext();
        when(userService.getAllUsers()).thenThrow(new SecurityException("Security violation"));

        mockMvc.perform(get("/admin/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Failed to fetch users: Security violation"));
    }
}
