package com.vehicle.rental.config;

import com.vehicle.rental.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/* =========================================================================
   SECURITY CONFIGURATION
   Centralized security architecture for the DriveEasy platform.
   Configures the filter chain, authorization rules, and stateless session policy.
   ========================================================================= */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Configures the main security filter chain for all HTTP requests.
     * Enforces the "Top-Down" rule priority to ensure secure endpoints are
     * evaluated before broad public permissions.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 1. INTEGRATION: Enable global CORS policies
                .cors(org.springframework.security.config.Customizer.withDefaults())

                // 2. INTEGRITY: Disable CSRF protection for stateless API interactions
                .csrf(csrf -> csrf.disable())

                // 3. PERSISTENCE: Maintain a strictly stateless session policy for JWT usage
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /* =========================================================================
                   4. AUTHORIZATION RULES (ORDER SENSITIVE)
                   ========================================================================= */
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()


                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/vehicles/**", "/api/categories/**").permitAll()


                        .requestMatchers("/api/vehicles/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * Standardized hashing utility for secure password storage and verification.
     * @return BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}