package com.marv.taskmaster.models.dto.request.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateTaskRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    private LocalDateTime dueDate;
}