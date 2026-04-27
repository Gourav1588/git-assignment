package com.vehicle.rental.repository;

import com.vehicle.rental.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository  // Marks this as a Spring Data repository (DAO layer)
public interface UserRepository extends JpaRepository<User, Long> {

    // Fetch user by email (used during login)
    // Returns Optional to safely handle "user not found"
    Optional<User> findByEmail(String email);

    // Check if a user already exists with given email (used during registration)
    boolean existsByEmail(String email);
}