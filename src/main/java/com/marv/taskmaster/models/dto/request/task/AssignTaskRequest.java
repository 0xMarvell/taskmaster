package com.marv.taskmaster.models.dto.request.task;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AssignTaskRequest {
    @NotNull(message = "Assignee ID is required")
    private UUID assigneeId;
}