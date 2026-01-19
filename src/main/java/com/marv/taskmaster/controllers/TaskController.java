package com.marv.taskmaster.controllers;

import com.marv.taskmaster.models.docs.ErrorResponse;
import com.marv.taskmaster.models.dto.request.task.AssignTaskRequest;
import com.marv.taskmaster.models.dto.request.task.CreateTaskRequest;
import com.marv.taskmaster.models.dto.request.task.UpdateTaskRequest;
import com.marv.taskmaster.models.dto.response.generic.BaseResponse;
import com.marv.taskmaster.models.dto.response.task.AssignedTaskResponse;
import com.marv.taskmaster.models.dto.response.task.TaskResponse;
import com.marv.taskmaster.services.TaskService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.marv.taskmaster.models.dto.response.generic.PagedData;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Endpoints for managing tasks within a project")
public class TaskController {

    private final TaskService taskService;

    /* ================================================================
     * GET /api/v1/projects/{projectId}/tasks
     * Get All Tasks (Paged)
     * ================================================================ */
    @Operation(summary = "Get All Tasks", description = "Retrieves paged list of tasks for a specific project.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<BaseResponse<PagedData<TaskResponse>>> getTasks(
            @PathVariable UUID projectId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.ASC) // Default: Due soonest first
            Pageable pageable) {

        PagedData<TaskResponse> data = taskService.getTasksByProject(projectId, pageable);

        return ResponseEntity.ok(
                BaseResponse.success(data, "Tasks retrieved successfully")
        );
    }

    /* ================================================================
     * GET /api/v1/projects/{projectId}/tasks/{taskId}
     * Get Single Task
     * ================================================================ */
    @Operation(summary = "Get Task by ID", description = "Retrieves a single task securely.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Task or Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{taskId}")
    public ResponseEntity<BaseResponse<TaskResponse>> getTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {

        TaskResponse data = taskService.getTaskById(projectId, taskId);

        return ResponseEntity.ok(
                BaseResponse.success(data, "Task retrieved successfully")
        );
    }

    /* ================================================================
     * GET /api/v1/tasks/assigned
     * Get All Tasks Assigned to Me (With Project Context)
     * ================================================================ */
    @Operation(summary = "Get Assigned Tasks", description = "Retrieves all tasks assigned to the logged-in user across all projects, including project names.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/assigned")
    public ResponseEntity<BaseResponse<PagedData<AssignedTaskResponse>>> getMyAssignedTasks(
            @ParameterObject
            @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.ASC)
            Pageable pageable) {

        PagedData<AssignedTaskResponse> data = taskService.getMyAssignedTasks(pageable);

        return ResponseEntity.ok(
                BaseResponse.success(data, "Assigned tasks retrieved successfully")
        );
    }

    /* ================================================================
     * POST /api/v1/projects/{projectId}/tasks
     * Create Task
     * ================================================================ */
    @Operation(summary = "Create Task", description = "Adds a new task to the specified project.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),

            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<BaseResponse<TaskResponse>> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request) {

        TaskResponse data = taskService.createTask(projectId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.success(data, "Task created successfully")
        );
    }

    /* ================================================================
     * PATCH /api/v1/projects/{projectId}/tasks/{taskId}/assign
     * Assign or Reassign Task
     * ================================================================ */
    @Operation(summary = "Assign/Reassign Task", description = "Assigns the task to a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task assigned successfully"),

            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "404", description = "Task, Project, or User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{taskId}/assign")
    public ResponseEntity<BaseResponse<TaskResponse>> assignTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignTaskRequest request) {

        TaskResponse data = taskService.assignTask(projectId, taskId, request);

        return ResponseEntity.ok(
                BaseResponse.success(data, "Task assigned successfully")
        );
    }

    /* ================================================================
     * PUT /api/v1/projects/{projectId}/tasks/{taskId}
     * Update Task Details
     * ================================================================ */
    @Operation(summary = "Update Task Details", description = "Updates title, description, and due date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),

            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "404", description = "Task or Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{taskId}")
    public ResponseEntity<BaseResponse<TaskResponse>> updateTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request) {

        TaskResponse data = taskService.updateTask(projectId, taskId, request);

        return ResponseEntity.ok(
                BaseResponse.success(data, "Task updated successfully")
        );
    }

    /* ================================================================
     * PATCH /api/v1/projects/{projectId}/tasks/{taskId}/cancel
     * Cancel Task
     * ================================================================ */
    @Operation(summary = "Cancel Task", description = "Sets task status to CANCELLED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task cancelled successfully"),

            @ApiResponse(responseCode = "404", description = "Task or Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{taskId}/cancel")
    public ResponseEntity<BaseResponse<TaskResponse>> cancelTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {

        TaskResponse data = taskService.cancelTask(projectId, taskId);

        return ResponseEntity.ok(
                BaseResponse.success(data, "Task cancelled successfully")
        );
    }

    /* ================================================================
     * PATCH /api/v1/projects/{projectId}/tasks/{taskId}/complete
     * Mark Task Done
     * ================================================================ */
    @Operation(summary = "Mark Task Done", description = "Marks task as COMPLETED. Auto-closes project if all tasks are done.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task completed successfully"),

            @ApiResponse(responseCode = "404", description = "Task or Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<BaseResponse<TaskResponse>> markDone(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {

        TaskResponse data = taskService.markTaskAsDone(projectId, taskId);

        return ResponseEntity.ok(
                BaseResponse.success(data, "Task completed successfully")
        );
    }

    /* ================================================================
     * DELETE /api/v1/projects/{projectId}/tasks
     * Bulk Delete Tasks
     * ================================================================ */
    @Operation(summary = "Bulk Delete Tasks", description = "Deletes multiple tasks at once. Expects a JSON array of UUIDs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks deleted successfully"),

            @ApiResponse(responseCode = "403", description = "Access denied (Tasks belong to different project)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),

            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> deleteTasks(
            @PathVariable UUID projectId,
            @RequestBody List<UUID> taskIds) {

        taskService.deleteTasks(projectId, taskIds);

        return ResponseEntity.ok(
                BaseResponse.success(null, "Tasks deleted successfully")
        );
    }
}