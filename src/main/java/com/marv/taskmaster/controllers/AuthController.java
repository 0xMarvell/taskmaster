package com.marv.taskmaster.controllers;

import com.marv.taskmaster.models.DTO.request.auth.LoginRequest;
import com.marv.taskmaster.models.DTO.request.auth.SignupRequest;
import com.marv.taskmaster.models.DTO.response.auth.LoginResponse;
import com.marv.taskmaster.models.DTO.response.auth.SignupResponse;
import com.marv.taskmaster.models.DTO.response.generic.BaseResponse;
import com.marv.taskmaster.models.docs.ErrorResponse;
import com.marv.taskmaster.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final UserService userService;

    /* ================================================================
     * POST /api/v1/auth/signup
     * Register a new user
     * Public access
     * ================================================================ */
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns the user details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),

            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "409", description = "Email already in use",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse data = userService.signup(request);
        return new ResponseEntity<>(
                BaseResponse.success(data, "User registered successfully"),
                HttpStatus.CREATED
        );
    }

    /* ================================================================
     * POST /api/v1/auth/login
     * Authenticate user
     * Public access
     * ================================================================ */
    @Operation(summary = "User Login", description = "Authenticates credentials and returns a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),

            @ApiResponse(responseCode = "400", description = "Invalid credentials",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse data = userService.login(request);
        return ResponseEntity.ok(
                BaseResponse.success(data, "Login successful")
        );
    }
}