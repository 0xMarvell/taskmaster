package com.marv.taskmaster.models.dto.request.task;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateTaskRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDateTime dueDate;

    private UUID assigneeId; // Optional: Assign immediately upon creation
}