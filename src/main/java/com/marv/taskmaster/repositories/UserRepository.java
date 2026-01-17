package com.marv.taskmaster.repositories;

import com.marv.taskmaster.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Finds a user by email (useful for Login and specific lookups)
    Optional<User> findByEmail(String email);

    // Checks if an email exists (useful for Signup validation)
    boolean existsByEmail(String email);
}