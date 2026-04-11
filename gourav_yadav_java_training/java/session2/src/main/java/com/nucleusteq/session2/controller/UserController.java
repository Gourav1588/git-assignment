package com.nucleusteq.session2.controller;

import com.nucleusteq.session2.model.User;
import com.nucleusteq.session2.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller layer for User.
 * Handles all incoming HTTP requests for user operations.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    // Service instance injected via constructor
    private final UserService userService;

    /**
     * Constructor injection of UserService.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /users → returns all users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // GET /users/{id} → returns single user by id
    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id) {
        return userService.getUserById(id);
    }

    // POST /users → creates and returns new user
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }
}