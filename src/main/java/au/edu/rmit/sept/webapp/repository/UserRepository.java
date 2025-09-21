package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.models.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 * Provides database operations for user management
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address
     * 
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username
     * 
     * @param username the username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email or username
     * 
     * @param email    the email address
     * @param username the username
     * @return Optional containing the user if found
     */
    Optional<User> findByEmailOrUsername(String email, String username);

    /**
     * Check if email exists in the database
     * 
     * @param email the email address
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if username exists in the database
     * 
     * @param username the username
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Find all active users
     * 
     * @return List of active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Find all users by user type
     * 
     * @param userType the user type
     * @return List of users with the specified type
     */
    List<User> findByUserType(UserType userType);

    /**
     * Find all active users by user type
     * 
     * @param userType the user type
     * @return List of active users with the specified type
     */
    List<User> findByUserTypeAndIsActiveTrue(UserType userType);

    /**
     * Find users created after a specific date
     * 
     * @param date the date
     * @return List of users created after the date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find users who logged in after a specific date
     * 
     * @param date the date
     * @return List of users who logged in after the date
     */
    List<User> findByLastLoginAfter(LocalDateTime date);

    /**
     * Find users with email verification status
     * 
     * @param emailVerified the email verification status
     * @return List of users with the specified verification status
     */
    List<User> findByEmailVerified(Boolean emailVerified);

    /**
     * Find banned users
     * 
     * @return List of banned users
     */
    List<User> findByIsBannedTrue();

    /**
     * Find non-banned users
     * 
     * @return List of non-banned users
     */
    List<User> findByIsBannedFalse();

    /**
     * Custom query to find users by partial name match
     * 
     * @param firstName partial first name
     * @param lastName  partial last name
     * @return List of users matching the criteria
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<User> findByNameContaining(@Param("firstName") String firstName, @Param("lastName") String lastName);

    /**
     * Custom query to find users by search term (name, username, or email)
     * 
     * @param searchTerm the search term
     * @return List of users matching the search term
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findBySearchTerm(@Param("searchTerm") String searchTerm);

    /**
     * Count users by user type
     * 
     * @param userType the user type
     * @return count of users with the specified type
     */
    long countByUserType(UserType userType);

    /**
     * Count active users
     * 
     * @return count of active users
     */
    long countByIsActiveTrue();

    /**
     * Count verified users
     * 
     * @return count of email verified users
     */
    long countByEmailVerifiedTrue();
}
