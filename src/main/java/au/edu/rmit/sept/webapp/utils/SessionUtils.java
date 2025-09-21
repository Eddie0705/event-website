package au.edu.rmit.sept.webapp.utils;

import au.edu.rmit.sept.webapp.models.User;
import jakarta.servlet.http.HttpSession;

/**
 * Utility class for session management
 * Provides helper methods for working with user sessions
 */
public class SessionUtils {

    /**
     * Get current user from session
     * 
     * @param session the HTTP session
     * @return the current user or null if not authenticated
     */
    public static User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }

    /**
     * Check if user is authenticated
     * 
     * @param session the HTTP session
     * @return true if user is authenticated
     */
    public static boolean isAuthenticated(HttpSession session) {
        return getCurrentUser(session) != null;
    }

    /**
     * Check if current user is admin
     * 
     * @param session the HTTP session
     * @return true if current user is admin
     */
    public static boolean isAdmin(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null && user.isAdmin();
    }

    /**
     * Check if current user is organiser
     * 
     * @param session the HTTP session
     * @return true if current user is organiser
     */
    public static boolean isOrganiser(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null && user.isOrganiser();
    }

    /**
     * Check if current user is student
     * 
     * @param session the HTTP session
     * @return true if current user is student
     */
    public static boolean isStudent(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null && user.isStudent();
    }

    /**
     * Check if current user has specific user type
     * 
     * @param session  the HTTP session
     * @param userType the user type to check
     * @return true if current user has the specified type
     */
    public static boolean hasUserType(HttpSession session, String userType) {
        User user = getCurrentUser(session);
        if (user == null) {
            return false;
        }
        return user.getUserType().toString().equalsIgnoreCase(userType);
    }

    /**
     * Get current user ID from session
     * 
     * @param session the HTTP session
     * @return the user ID or null if not authenticated
     */
    public static Long getCurrentUserId(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null ? user.getUserId() : null;
    }

    /**
     * Get current username from session
     * 
     * @param session the HTTP session
     * @return the username or null if not authenticated
     */
    public static String getCurrentUsername(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null ? user.getUsername() : null;
    }

    /**
     * Get current user email from session
     * 
     * @param session the HTTP session
     * @return the email or null if not authenticated
     */
    public static String getCurrentUserEmail(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null ? user.getEmail() : null;
    }

    /**
     * Check if current user can access admin functions
     * 
     * @param session the HTTP session
     * @return true if user can access admin functions
     */
    public static boolean canAccessAdmin(HttpSession session) {
        return isAdmin(session);
    }

    /**
     * Check if current user can access organiser functions
     * 
     * @param session the HTTP session
     * @return true if user can access organiser functions
     */
    public static boolean canAccessOrganiser(HttpSession session) {
        User user = getCurrentUser(session);
        return user != null && (user.isOrganiser() || user.isAdmin());
    }

    /**
     * Check if current user can modify another user's data
     * 
     * @param session      the HTTP session
     * @param targetUserId the target user ID
     * @return true if user can modify the target user's data
     */
    public static boolean canModifyUser(HttpSession session, Long targetUserId) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return false;
        }

        // Admin can modify any user
        if (currentUser.isAdmin()) {
            return true;
        }

        // Users can modify their own data
        return currentUser.getUserId().equals(targetUserId);
    }

    /**
     * Clear user session
     * 
     * @param session the HTTP session
     */
    public static void clearSession(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Set user in session
     * 
     * @param session the HTTP session
     * @param user    the user to set
     */
    public static void setUserInSession(HttpSession session, User user) {
        if (session != null && user != null) {
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userType", user.getUserType().toString());
        }
    }
}
