package com.vehicle.rental.service;

import com.vehicle.rental.dto.response.UserResponse;
import com.vehicle.rental.entity.User;
import com.vehicle.rental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service // Tells Spring Boot this is where the business logic lives
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsersForAdmin() {
        // Fetch users from DB
        List<User> users = userRepository.findAll();

        // Convert to safe DTOs
        return users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().name()
                ))
                .collect(Collectors.toList());
    }
}