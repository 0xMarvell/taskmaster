package com.marv.taskmaster.services;

import com.marv.taskmaster.models.dto.request.auth.LoginRequest;
import com.marv.taskmaster.models.dto.request.auth.SignupRequest;
import com.marv.taskmaster.models.dto.response.auth.LoginResponse;
import com.marv.taskmaster.models.dto.response.auth.SignupResponse;
import com.marv.taskmaster.models.dto.response.user.UserDetailResponse;
import com.marv.taskmaster.models.entities.User;
import com.marv.taskmaster.models.security.CustomUserDetails;
import com.marv.taskmaster.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public SignupResponse signup(SignupRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                String errorMsg = "Email already in use";
                log.error("Signup attempt failed for user {} at {}: {}",
                        request.getEmail(), LocalDateTime.now(), errorMsg);
                throw new DataIntegrityViolationException(errorMsg);
            }

            User newUser = new User();
            newUser.setFirstname(capitalize(request.getFirstname()));
            newUser.setLastname(capitalize(request.getLastname()));
            newUser.setEmail(request.getEmail());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));

            User savedUser = userRepository.save(newUser);

            log.info("Signup successful for user {} {} at {}",
                    savedUser.getFirstname(), savedUser.getLastname(), LocalDateTime.now());

            return SignupResponse.builder()
                    .user(mapToDetailResponse(savedUser))
                    .build();

        } catch (Exception e) {
            if (!(e instanceof DataIntegrityViolationException)) {
                log.error("Signup attempt failed for user {} at {}: {}",
                        request.getEmail(), LocalDateTime.now(), e.getMessage());
            }
            throw e;
        }
    }


    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            String jwtToken = jwtService.generateToken(new CustomUserDetails(user));

            log.info("Login successful for user {} at {}",
                    user.getEmail(), LocalDateTime.now());

            return LoginResponse.builder()
                    .token(jwtToken)
                    .user(mapToDetailResponse(user))
                    .expiresIn(jwtService.getJwtExpiration() / 1000)
                    .build();

        } catch (AuthenticationException e) {
            log.error("Login attempt failed for user {} at {}: Invalid Credentials",
                    request.getEmail(), LocalDateTime.now());
            throw new IllegalArgumentException("Invalid email or password");
        } catch (Exception e) {
            log.error("Login attempt failed for user {} at {}: {}",
                    request.getEmail(), LocalDateTime.now(), e.getMessage());
            throw e;
        }
    }

    // --- Private Helpers ---

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

    private String capitalize(String value) {
        if (value == null || value.isBlank()) return value;
        value = value.trim().toLowerCase();
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}