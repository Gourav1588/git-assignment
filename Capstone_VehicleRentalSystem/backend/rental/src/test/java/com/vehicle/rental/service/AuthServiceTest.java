package com.vehicle.rental.service;

import com.vehicle.rental.dto.request.LoginRequest;
import com.vehicle.rental.dto.request.RegisterRequest;
import com.vehicle.rental.entity.User;
import com.vehicle.rental.exception.BadRequestException;
import com.vehicle.rental.repository.UserRepository;
import com.vehicle.rental.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link AuthService}.
 * Validates registration, login, and security measures like preventing account enumeration.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest testRegisterRequest;
    private LoginRequest testLoginRequest;
    private User testUser;
    private final String MOCK_TOKEN = "eyJhbGciOiJIUzI1NiJ9.mock_jwt_token_here";

    @BeforeEach
    void setUp() {
        // Setup a dummy registration request
        testRegisterRequest = new RegisterRequest();
        testRegisterRequest.setName("Test User");
        testRegisterRequest.setEmail("test@driveeasy.com");
        testRegisterRequest.setPassword("securePassword123");

        // Setup a dummy login request
        testLoginRequest = new LoginRequest();
        testLoginRequest.setEmail("test@driveeasy.com");
        testLoginRequest.setPassword("securePassword123");

        // Setup the expected User entity
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@driveeasy.com");
        testUser.setPassword("encoded_securePassword123");
        testUser.setRole(User.Role.USER);
    }

    // =========================================================================
    // REGISTRATION TESTS
    // =========================================================================

    @Test
    void register_Success() {
        // Arrange
        when(userRepository.existsByEmail("test@driveeasy.com")).thenReturn(false);
        when(passwordEncoder.encode("securePassword123")).thenReturn("encoded_securePassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn(MOCK_TOKEN);

        // Act
        String token = authService.register(testRegisterRequest);

        // Assert
        assertNotNull(token);
        assertEquals(MOCK_TOKEN, token);

        // Verify that the repository checked the exact trimmed email
        verify(userRepository, times(1)).existsByEmail("test@driveeasy.com");
        // Verify the user was actually saved to the database
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        // Arrange: Simulate that the email is already in the database
        when(userRepository.existsByEmail("test@driveeasy.com")).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authService.register(testRegisterRequest);
        });

        assertEquals("Email already registered", exception.getMessage());

        // Verify we stopped before doing any expensive password hashing or saving
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // =========================================================================
    // LOGIN TESTS
    // =========================================================================

    @Test
    void login_Success() {
        // Arrange: User is found, and password matches
        when(userRepository.findByEmail("test@driveeasy.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("securePassword123", "encoded_securePassword123")).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn(MOCK_TOKEN);

        // Act
        String token = authService.login(testLoginRequest);

        // Assert
        assertNotNull(token);
        assertEquals(MOCK_TOKEN, token);
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        // Arrange: Email doesn't exist in the database
        when(userRepository.findByEmail("test@driveeasy.com")).thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authService.login(testLoginRequest);
        });

        // Verifying the generic message to prevent account enumeration
        assertEquals("Invalid email or password", exception.getMessage());

        // Ensure we never tried to check a password since the user doesn't exist
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        // Arrange: User is found, but the password check fails
        when(userRepository.findByEmail("test@driveeasy.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("securePassword123", "encoded_securePassword123")).thenReturn(false);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authService.login(testLoginRequest);
        });

        // Verifying it gives the EXACT same error message as UserNotFound
        assertEquals("Invalid email or password", exception.getMessage());

        // Ensure no token was generated
        verify(jwtService, never()).generateToken(any(User.class));
    }
}