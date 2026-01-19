package com.marv.taskmaster.models.dto.response.project;

import com.marv.taskmaster.models.dto.response.task.TaskResponse;
import com.marv.taskmaster.models.enums.ProjectStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private ProjectStatus status;

    private List<TaskResponse> tasks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}