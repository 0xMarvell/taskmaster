package com.marv.taskmaster.services;

import com.marv.taskmaster.models.DTO.request.auth.LoginRequest;
import com.marv.taskmaster.models.DTO.request.auth.SignupRequest;
import com.marv.taskmaster.models.DTO.response.generic.PagedData;
import com.marv.taskmaster.models.DTO.response.auth.LoginResponse;
import com.marv.taskmaster.models.DTO.response.auth.SignupResponse;
import com.marv.taskmaster.models.DTO.response.user.UserDetailResponse;
import com.marv.taskmaster.models.DTO.response.user.UsersResponse;
import com.marv.taskmaster.models.entities.User;
import com.marv.taskmaster.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Registers a new user
     */
    public SignupResponse signup(SignupRequest request) {
        // 1. Validate Email Uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DataIntegrityViolationException("Email already in use");
        }

        // 2. Map Request to Entity manually (or use MapStruct later)
        User newUser = new User();
        newUser.setFirstname(capitalize(request.getFirstname()));
        newUser.setLastname(capitalize(request.getLastname()));
        newUser.setEmail(request.getEmail());

        // TODO: Phase 2 - We will wrap this in BCryptPasswordEncoder
        newUser.setPassword(request.getPassword());

        // 3. Save to Database
        User savedUser = userRepository.save(newUser);

        // 4. Return DTO
        return SignupResponse.builder()
                .user(mapToDetailResponse(savedUser))
                .build();
    }

    /**
     * Authenticates a user
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Find User by Email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 2. Validate Password (Simple string check for Phase 1)
        if (!user.getPassword().equals(request.getPassword())) {
            // Maps to 400 Bad Request in your GlobalHandler
            throw new IllegalArgumentException("Invalid email or password");
        }

        // 3. Generate Token (Placeholder for Phase 2 JWT)
        String fakeToken = "dummy-jwt-token-" + user.getId();

        // 4. Return DTO
        return LoginResponse.builder()
                .token(fakeToken)
                .user(mapToDetailResponse(user))
                .build();
    }

    /**
     * Gets a paged list of users
     */
    public PagedData<UsersResponse> getAllUsers(Pageable pageable) {
        // 1. Fetch Page from DB (Spring Data handles the SQL pagination)
        Page<User> userPage = userRepository.findAll(pageable);

        // 2. Map Entity Page to DTO Page
        Page<UsersResponse> responsePage = userPage.map(this::mapToUsersResponse);

        // 3. Wrap in PagedData (for clean JSON output)
        return new PagedData<>(responsePage);
    }

    /**
     * Gets a single user by ID
     */
    public UserDetailResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        return mapToDetailResponse(user);
    }

    // --- Private Helper Mappers ---

    private UserDetailResponse mapToDetailResponse(User user) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private UsersResponse mapToUsersResponse(User user) {
        return UsersResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .build();
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        value = value.trim().toLowerCase();
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}