package com.vehicle.rental.service;

import com.vehicle.rental.dto.request.UserProfileUpdateRequest;
import com.vehicle.rental.dto.response.UserResponse;
import com.vehicle.rental.entity.User;
import com.vehicle.rental.mapper.UserMapper;
import com.vehicle.rental.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link UserService}.
 * Validates admin retrieval and partial profile updates (handling nulls and blank inputs).
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testResponse;
    private UserProfileUpdateRequest testRequest;

    @BeforeEach
    void setUp() {
        // Build the User as they currently exist in the database
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@driveeasy.com");
        testUser.setName("Old Name");
        testUser.setPassword("oldPassword123");
        testUser.setRole(User.Role.USER);

        // Build the expected response
        testResponse = new UserResponse();
        testResponse.setId(1L);
        testResponse.setEmail("test@driveeasy.com");
        testResponse.setName("Old Name");

        // Prepare an empty request (tests will fill this in as needed)
        testRequest = new UserProfileUpdateRequest();
    }

    // =========================================================================
    // ADMIN TESTS
    // =========================================================================

    @Test
    void getAllUsersForAdmin_Success() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

        // Act
        List<UserResponse> result = userService.getAllUsersForAdmin();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll();
    }

    // =========================================================================
    // PROFILE UPDATE TESTS
    // =========================================================================

    @Test
    void updateProfile_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("ghost@driveeasy.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateProfile("ghost@driveeasy.com", testRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfile_UpdateAllFields_Success() {
        // Arrange: User provides a new name (with extra spaces to test trimming) and a new password
        testRequest.setName("  New Name  ");
        testRequest.setPassword("newSecurePass99");

        when(userRepository.findByEmail("test@driveeasy.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newSecurePass99")).thenReturn("encoded_newSecurePass99");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Simulating the mapper
        testResponse.setName("New Name");
        when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

        // Act
        UserResponse result = userService.updateProfile("test@driveeasy.com", testRequest);

        // Assert
        // 1. Verify the entity was updated correctly
        assertEquals("New Name", testUser.getName(), "Name should be updated and trimmed");
        assertEquals("encoded_newSecurePass99", testUser.getPassword(), "Password should be encoded and updated");

        // 2. Verify we saved the changes
        verify(userRepository, times(1)).save(testUser);
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    void updateProfile_NoFieldsProvided_IgnoresUpdates() {
        // Arrange: Request is completely empty (Name and Password are null)
        when(userRepository.findByEmail("test@driveeasy.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

        // Act
        userService.updateProfile("test@driveeasy.com", testRequest);

        // Assert: The original details should remain completely untouched
        assertEquals("Old Name", testUser.getName());
        assertEquals("oldPassword123", testUser.getPassword());

        // Ensure we didn't waste resources encoding a blank password
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, times(1)).save(testUser); // Still saves, but saves the exact same data
    }

    @Test
    void updateProfile_BlankFieldsProvided_IgnoresUpdates() {
        // Arrange: Request has fields, but they are just empty spaces
        testRequest.setName("   ");
        testRequest.setPassword("");

        when(userRepository.findByEmail("test@driveeasy.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

        // Act
        userService.updateProfile("test@driveeasy.com", testRequest);

        // Assert: The original details should remain untouched because blank strings shouldn't trigger an update
        assertEquals("Old Name", testUser.getName());
        assertEquals("oldPassword123", testUser.getPassword());

        verify(passwordEncoder, never()).encode(anyString());
    }
}