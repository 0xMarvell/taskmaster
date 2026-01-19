package com.marv.taskmaster.controllers;

import com.marv.taskmaster.models.dto.response.generic.PagedData;
import com.marv.taskmaster.models.dto.response.generic.BaseResponse;
import com.marv.taskmaster.models.dto.response.user.UserDetailResponse;
import com.marv.taskmaster.models.dto.response.user.UsersResponse;
import com.marv.taskmaster.models.docs.ErrorResponse;
import com.marv.taskmaster.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for viewing and managing users")
public class UserController {

    private final UserService userService;

    /* ================================================================
     * GET /api/v1/users
     * Get all users (Paged)
     * ================================================================ */
    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all registered users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),

            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<BaseResponse<PagedData<UsersResponse>>> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 10,sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        PagedData<UsersResponse> data = userService.getAllUsers(pageable);

        return ResponseEntity.ok(
                BaseResponse.success(data, "Users retrieved successfully")
        );
    }

    /* ================================================================
     * GET /api/v1/users/{id}
     * Get user by ID
     * ================================================================ */
    @Operation(summary = "Get user by ID", description = "Retrieves detailed information for a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),

            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<BaseResponse<UserDetailResponse>> getUserById(@PathVariable UUID userId) {
        UserDetailResponse data = userService.getUserById(userId);

        return ResponseEntity.ok(
                BaseResponse.success(data, "User details retrieved successfully")
        );
    }
}