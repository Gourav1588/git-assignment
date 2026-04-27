package com.vehicle.rental.security;

import com.vehicle.rental.entity.User;
import com.vehicle.rental.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Service to handle JWT operations (validate, extract data)
    private final JwtService jwtService;

    // Repository to fetch user from database
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Read Authorization header from request
        String authHeader = request.getHeader("Authorization");

        // If header is missing or not Bearer type, skip authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // Validate token (checks signature and expiration)
        if (!jwtService.isTokenValid(token)) {
            log.error("Invalid or expired JWT token");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract user identity (email) from token
        String email = jwtService.extractEmail(token);

        // Fetch user from database using email
        Optional<User> userOptional = userRepository.findByEmail(email);

        // If user not found, continue without authentication
        if (userOptional.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Convert User entity into Spring Security compatible object
        CustomUserDetails userDetails =
                new CustomUserDetails(userOptional.get());

        // Create authentication object with user details and roles
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,                   // principal (who user is)
                        null,                          // credentials (not needed here)
                        userDetails.getAuthorities()   // roles/permissions
                );

        // Attach request-specific details (IP, session, etc.)
        authToken.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        // Store authentication in SecurityContext
        // After this, Spring Security treats the user as authenticated
        SecurityContextHolder.getContext()
                .setAuthentication(authToken);

        log.debug("Authenticated user: {}", email);

        // Continue request processing (next filter or controller)
        filterChain.doFilter(request, response);
    }
}