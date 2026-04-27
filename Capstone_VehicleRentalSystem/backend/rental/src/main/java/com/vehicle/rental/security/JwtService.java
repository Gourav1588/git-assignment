package com.vehicle.rental.security;

import com.vehicle.rental.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

    // Secret key used to sign and verify the JWT
    @Value("${jwt.secret}")
    private String secret;

    // Token expiration time (in milliseconds)
    @Value("${jwt.expiration}")
    private Long expiration;

    // Generates a JWT token after successful login or registration
    public String generateToken(User user) {

        // Log the token generation process
        log.debug("Generating token for user: {}", user.getEmail());

        return Jwts.builder()

                // Set the subject (usually email or username)
                .setSubject(user.getEmail())

                // Add custom claims (extra data inside token)
                .claim("role", user.getRole().name())
                .claim("userId", user.getId())

                // Set token creation time
                .setIssuedAt(new Date())

                // Set token expiration time
                .setExpiration(new Date(
                        System.currentTimeMillis() + expiration))

                // Sign the token using secret key and algorithm
                .signWith(getSignKey(), SignatureAlgorithm.HS256)

                // Build and return the token as a string
                .compact();
    }

    // Extracts the email (subject) from the token
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // Validates the token by checking expiration and structure
    public boolean isTokenValid(String token) {
        try {
            return extractClaims(token)
                    .getExpiration()
                    .after(new Date());
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // Extracts all claims (payload data) from the token
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())   // verify using same secret
                .build()
                .parseClaimsJws(token)         // parse token
                .getBody();                    // return payload
    }

    // Generates signing key from secret string
    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}