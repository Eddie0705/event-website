package au.edu.rmit.sept.webapp.security;

import au.edu.rmit.sept.webapp.models.User;
import au.edu.rmit.sept.webapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Custom UserDetailsService for Spring Security authentication
 * Handles user authentication and updates last login timestamp
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        Optional<User> userOpt = userService.findByEmailOrUsername(emailOrUsername);

        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + emailOrUsername);
        }

        User user = userOpt.get();

        // Check if user is active and not banned
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User account is inactive: " + emailOrUsername);
        }

        if (user.getIsBanned()) {
            throw new UsernameNotFoundException("User account is banned: " + emailOrUsername);
        }

        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        userService.updateUser(user);

        return new CustomUserPrincipal(user);
    }

    /**
     * Custom UserDetails implementation
     */
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // Convert user type to Spring Security role
            String role = "ROLE_" + user.getUserType().name();
            return Collections.singletonList(new SimpleGrantedAuthority(role));
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getEmail(); // Use email as username for Spring Security
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return !user.getIsBanned();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getIsActive();
        }

        // Additional methods to access user information
        public User getUser() {
            return user;
        }

        public Long getUserId() {
            return user.getUserId();
        }

        public String getEmail() {
            return user.getEmail();
        }

        public String getFirstName() {
            return user.getFirstName();
        }

        public String getLastName() {
            return user.getLastName();
        }

        public String getUserType() {
            return user.getUserType().name();
        }
    }
}
