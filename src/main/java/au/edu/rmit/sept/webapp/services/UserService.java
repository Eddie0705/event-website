package au.edu.rmit.sept.webapp.services;

import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for User-related business logic
 * Handles user registration, authentication, and management
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new user
     * 
     * @param user the user to register
     * @return the saved user
     * @throws IllegalArgumentException if email or username already exists
     */
    public User registerUser(User user) {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        // Hash the password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        // Set default values
        user.setIsActive(true);
        user.setIsBanned(false);
        user.setEmailVerified(false);

        return userRepository.save(user);
    }

    /**
     * Authenticate user by email/username and password
     * 
     * @param emailOrUsername email or username
     * @param password        plain text password
     * @return Optional containing the user if authentication successful
     */
    public Optional<User> authenticateUser(String emailOrUsername, String password) {
        Optional<User> userOpt = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check if user is active and not banned
            if (user.getIsActive() && !user.getIsBanned()) {
                // Verify password
                if (passwordEncoder.matches(password, user.getPasswordHash())) {
                    // Update last login
                    user.setLastLogin(LocalDateTime.now());
                    userRepository.save(user);
                    return Optional.of(user);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Find user by ID
     * 
     * @param userId the user ID
     * @return Optional containing the user if found
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Find user by email
     * 
     * @param email the email address
     * @return Optional containing the user if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by username
     * 
     * @param username the username
     * @return Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email or username
     * 
     * @param emailOrUsername email or username
     * @return Optional containing the user if found
     */
    public Optional<User> findByEmailOrUsername(String emailOrUsername) {
        return userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername);
    }

    /**
     * Update user information
     * 
     * @param user the user to update
     * @return the updated user
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Change user password
     * 
     * @param userId      the user ID
     * @param oldPassword the old password
     * @param newPassword the new password
     * @return true if password changed successfully
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Verify old password
            if (passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
                // Set new password
                user.setPasswordHash(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }

        return false;
    }

    /**
     * Reset user password (admin function)
     * 
     * @param userId      the user ID
     * @param newPassword the new password
     * @return true if password reset successfully
     */
    public boolean resetPassword(Long userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }

        return false;
    }

    /**
     * Activate user account
     * 
     * @param userId the user ID
     * @return true if user activated successfully
     */
    public boolean activateUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(true);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    /**
     * Deactivate user account
     * 
     * @param userId the user ID
     * @return true if user deactivated successfully
     */
    public boolean deactivateUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(false);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    /**
     * Ban user account
     * 
     * @param userId the user ID
     * @return true if user banned successfully
     */
    public boolean banUser(Long userId) {
        System.out.println("=== USER SERVICE BAN USER ===");
        System.out.println("Looking for user with ID: " + userId);
        
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("Found user: " + user.getUsername() + " (was banned: " + user.getIsBanned() + ")");
            
            user.setIsBanned(true);
            user.setIsActive(false);
            
            System.out.println("Setting user banned to: " + user.getIsBanned());
            System.out.println("Setting user active to: " + user.getIsActive());
            
            User savedUser = userRepository.save(user);
            System.out.println("User saved. Verifying ban status: " + savedUser.getIsBanned());
            
            return true;
        } else {
            System.out.println("User with ID " + userId + " not found!");
            return false;
        }
    }

    /**
     * Unban user account
     * 
     * @param userId the user ID
     * @return true if user unbanned successfully
     */
    public boolean unbanUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsBanned(false);
            user.setIsActive(true);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    /**
     * Verify user email
     * 
     * @param userId the user ID
     * @return true if email verified successfully
     */
    public boolean verifyEmail(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    /**
     * Get all users
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get all active users
     * 
     * @return List of active users
     */
    public List<User> getActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    /**
     * Get users by type
     * 
     * @param userType the user type
     * @return List of users with the specified type
     */
    public List<User> getUsersByType(UserType userType) {
        return userRepository.findByUserType(userType);
    }

    /**
     * Search users by term
     * 
     * @param searchTerm the search term
     * @return List of users matching the search term
     */
    public List<User> searchUsers(String searchTerm) {
        return userRepository.findBySearchTerm(searchTerm);
    }

    /**
     * Check if email exists
     * 
     * @param email the email address
     * @return true if email exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if username exists
     * 
     * @param username the username
     * @return true if username exists
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Get user statistics
     * 
     * @return User statistics
     */
    public UserStats getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long verifiedUsers = userRepository.countByEmailVerifiedTrue();
        long students = userRepository.countByUserType(UserType.STUDENT);
        long organisers = userRepository.countByUserType(UserType.ORGANISER);
        long admins = userRepository.countByUserType(UserType.ADMIN);

        return new UserStats(totalUsers, activeUsers, verifiedUsers, students, organisers, admins);
    }

    public String getUserFullNameById(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getFullName()) // Assuming your User entity has a getFullName() method
                .orElse("Unknown Organizer");
    }

    /**
     * Inner class for user statistics
     */
    public static class UserStats {
        private final long totalUsers;
        private final long activeUsers;
        private final long verifiedUsers;
        private final long students;
        private final long organisers;
        private final long admins;

        public UserStats(long totalUsers, long activeUsers, long verifiedUsers,
                long students, long organisers, long admins) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.verifiedUsers = verifiedUsers;
            this.students = students;
            this.organisers = organisers;
            this.admins = admins;
        }

        // Getters
        public long getTotalUsers() {
            return totalUsers;
        }

        public long getActiveUsers() {
            return activeUsers;
        }

        public long getVerifiedUsers() {
            return verifiedUsers;
        }

        public long getStudents() {
            return students;
        }

        public long getOrganisers() {
            return organisers;
        }

        public long getAdmins() {
            return admins;
        }
    }

    /**
     * Permanently delete a user (admin only)
     * This will also cascade delete any events created by this user
     * WARNING: This is permanent and cannot be undone
     */
    @Transactional
    public void deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        User user = userOptional.get();
        
        // Additional safety check - prevent deletion of the last admin
        if (user.getUserType() == UserType.ADMIN) {
            long adminCount = userRepository.countByUserType(UserType.ADMIN);
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot delete the last remaining admin user");
            }
        }
        
        // Handle foreign key constraints manually before deleting the user
        // Since we don't have all entity repositories, we'll use native SQL queries
        // to handle the RESTRICT constraints
        
        // Delete events created by this user (this will cascade to RSVPs, feedback, etc.)
        eventRepository.deleteByCreatedBy(userId);
        
        // Delete clubs created by this user using native query
        entityManager.createNativeQuery("DELETE FROM clubs WHERE created_by = ?")
                    .setParameter(1, userId)
                    .executeUpdate();
        
        // Delete audit logs where this user was the admin using native query
        entityManager.createNativeQuery("DELETE FROM audit_log WHERE admin_id = ?")
                    .setParameter(1, userId)
                    .executeUpdate();
        
        // Delete payments made by this user using native query
        entityManager.createNativeQuery("DELETE FROM payments WHERE user_id = ?")
                    .setParameter(1, userId)
                    .executeUpdate();
        
        // Other entities with CASCADE will be handled automatically by the database
        // Delete the user
        userRepository.delete(user);
    }
}
