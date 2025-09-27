package au.edu.rmit.sept.webapp.config;

import org.springframework.http.HttpMethod;

import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import au.edu.rmit.sept.webapp.security.CustomAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the EventHub application
 * Configures authentication, authorization, and security settings
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomUserDetailsService userDetailsService;

        @Autowired
        private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

        /**
         * Configure security filter chain
         */
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // Disable CSRF for now (can be enabled later if needed)
                                .csrf(csrf -> csrf.disable())

                                // Configure authorization rules
                                .authorizeHttpRequests(authz -> authz
                                                // Public endpoints
                                                .requestMatchers("/", "/Index", "/explore", "/calendar", "/rspv",
                                                                "/noti")
                                                .permitAll()
                                                .requestMatchers("/auth/login", "/auth/register",
                                                                "/auth/forgot-password")
                                                .permitAll()
                                                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**")
                                                .permitAll()
                                                .requestMatchers("/db-test", "/auth-test", "/db-users-test",
                                                                "/test-user-load")
                                                .permitAll()

                                                // Event viewing - Allow anonymous access to view events
                                                .requestMatchers(HttpMethod.GET, "/events/*").permitAll()
                                                .requestMatchers("/events/*/rsvp").authenticated()

                                                // Admin endpoints
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/users/**").hasRole("ADMIN")

                                                // Organiser endpoints
                                                .requestMatchers("/organiser/**").hasAnyRole("ORGANISER", "ADMIN")
                                                .requestMatchers("/events/create").hasAnyRole("ORGANISER", "ADMIN")
                                                .requestMatchers("/events/*/edit").hasAnyRole("ORGANISER", "ADMIN")
                                                .requestMatchers("/events/*/delete").hasAnyRole("ORGANISER", "ADMIN")

                                                // User endpoints (authenticated users)
                                                .requestMatchers("/auth/profile", "/auth/change-password")
                                                .authenticated()
                                                .requestMatchers("/profile").authenticated()

                                                // All other requests require authentication
                                                .anyRequest().authenticated())

                                // Configure form login
                                .formLogin(form -> form
                                                .loginPage("/auth/login")
                                                .loginProcessingUrl("/auth/login")
                                                .usernameParameter("emailOrUsername")
                                                .passwordParameter("password")
                                                .successHandler(authenticationSuccessHandler)
                                                .failureUrl("/auth/login?error=true")
                                                .permitAll())

                                // Configure logout
                                .logout(logout -> logout
                                                .logoutUrl("/auth/logout")
                                                .logoutSuccessUrl("/")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())

                                // Configure session management - FIXED
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .maximumSessions(5) // Allow multiple sessions per user
                                                .maxSessionsPreventsLogin(false)
                                                .and()
                                                .invalidSessionUrl("/auth/login")
                                                .sessionFixation().migrateSession());

                return http.build();
        }

        /**
         * Authentication manager bean
         */
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}