package au.edu.rmit.sept.webapp.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication success handler
 * Handles successful login and redirects based on user type
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        CustomUserDetailsService.CustomUserPrincipal userPrincipal = (CustomUserDetailsService.CustomUserPrincipal) authentication
                .getPrincipal();

        String userType = userPrincipal.getUserType();

        // Redirect based on user type
        if ("ADMIN".equals(userType)) {
            response.sendRedirect("/admin/dashboard");
        } else if ("ORGANISER".equals(userType)) {
            response.sendRedirect("/organiser/dashboard");
        } else {
            // Default redirect for students
            response.sendRedirect("/");
        }
    }
}
