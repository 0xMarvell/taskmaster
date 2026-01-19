package com.marv.taskmaster.controllers;

import com.marv.taskmaster.models.docs.ErrorResponse;
import com.marv.taskmaster.models.dto.request.project.CreateProjectRequest;
import com.marv.taskmaster.models.dto.request.project.UpdateProjectRequest;
import com.marv.taskmaster.models.dto.response.generic.BaseResponse;
import com.marv.taskmaster.models.dto.response.generic.PagedData;
import com.marv.taskmaster.models.dto.response.project.ProjectResponse;
import com.marv.taskmaster.services.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project Management", description = "Endpoints for managing user projects")
public class ProjectController {

    private final ProjectService projectService;

    /* ================================================================
     * POST /api/v1/projects
     * Create a new project
     * ================================================================ */
    @Operation(summary = "Create a new project", description = "Creates a project for the logged-in user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully"),

            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "409", description = "Project name already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<BaseResponse<ProjectResponse>> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse data = projectService.createProject(request);
        return new ResponseEntity<>(
                BaseResponse.success(data, "Project created successfully"),
                HttpStatus.CREATED
        );
    }

    /* ================================================================
     * GET /api/v1/projects
     * Get all projects (Paged)
     * ================================================================ */
    @Operation(summary = "Get all projects", description = "Retrieves paged list of projects for the logged-in user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),

            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<BaseResponse<PagedData<ProjectResponse>>> getMyProjects(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        PagedData<ProjectResponse> data = projectService.getMyProjects(pageable);
        return ResponseEntity.ok(
                BaseResponse.success(data, "Projects retrieved successfully")
        );
    }

    /* ================================================================
     * GET /api/v1/projects/{id}
     * Get single project
     * ================================================================ */
    @Operation(summary = "Get project by ID", description = "Retrieves a single project if it belongs to the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project retrieved successfully"),

            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{projectId}")
    public ResponseEntity<BaseResponse<ProjectResponse>> getProjectById(@PathVariable UUID projectId) {
        ProjectResponse data = projectService.getProjectById(projectId);
        return ResponseEntity.ok(
                BaseResponse.success(data, "Project retrieved successfully")
        );
    }

    /* ================================================================
     * PUT /api/v1/projects/{id}
     * Update project
     * ================================================================ */
    @Operation(summary = "Update project", description = "Updates project name and description.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),

            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "409", description = "Project name already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{projectId}")
    public ResponseEntity<BaseResponse<ProjectResponse>> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request) {

        ProjectResponse data = projectService.updateProject(projectId, request);
        return ResponseEntity.ok(
                BaseResponse.success(data, "Project updated successfully")
        );
    }

    /* ================================================================
     * POST /api/v1/projects/{id}/cancel
     * Cancel project
     * ================================================================ */
    @Operation(summary = "Cancel project", description = "Sets project status to CANCELLED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project cancelled successfully"),

            @ApiResponse(responseCode = "400", description = "Project is already cancelled",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{projectId}/cancel")
    public ResponseEntity<BaseResponse<ProjectResponse>> cancelProject(@PathVariable UUID projectId) {
        ProjectResponse data = projectService.cancelProject(projectId);
        return ResponseEntity.ok(
                BaseResponse.success(data, "Project cancelled successfully")
        );
    }
}