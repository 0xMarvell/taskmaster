package com.marv.taskmaster.models.dto.response.task;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AssignedTaskResponse {
    // Task Details
    private UUID id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime dueDate;
    private boolean isOverdue;

    // Assignee Details
    private UUID assigneeId;
    private String assigneeName;

    // Project Context
    private UUID projectId;
    private String projectName;
}