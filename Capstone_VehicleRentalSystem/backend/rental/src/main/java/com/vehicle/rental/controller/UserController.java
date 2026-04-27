package com.vehicle.rental.controller;

import com.vehicle.rental.dto.response.UserResponse;
import com.vehicle.rental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    // Now we inject the Service, NOT the Repository
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        // The Controller simply asks the Service to do the heavy lifting
        List<UserResponse> safeUsers = userService.getAllUsersForAdmin();

        return ResponseEntity.ok(safeUsers);
    }
}