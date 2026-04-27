package com.vehicle.rental.controller;

import com.vehicle.rental.dto.request.LoginRequest;
import com.vehicle.rental.dto.request.RegisterRequest;
import com.vehicle.rental.dto.response.ApiResponse;
import com.vehicle.rental.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController  // Marks this class as REST controller (returns JSON responses)
@RequestMapping("/api/auth")  // Base URL for all endpoints in this controller
@CrossOrigin(origins = "*")  // Allows requests from any frontend (CORS)
@RequiredArgsConstructor
public class AuthController {

    // Service layer handling business logic
    private final AuthService authService;

    // Endpoint for user registration
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        // Call service to register user and generate token
        String token = authService.register(request);

        // Return success response with token
        return ResponseEntity.ok(
                new ApiResponse("Registration successful", token)
        );
    }

    // Endpoint for user login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody LoginRequest request) {

        // Call service to authenticate user and generate token
        String token = authService.login(request);

        // Return success response with token
        return ResponseEntity.ok(
                new ApiResponse("Login successful", token)
        );
    }
}