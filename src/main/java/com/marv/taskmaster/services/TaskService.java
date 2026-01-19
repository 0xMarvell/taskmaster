package com.marv.taskmaster.services;

import com.marv.taskmaster.models.dto.request.task.AssignTaskRequest;
import com.marv.taskmaster.models.dto.request.task.CreateTaskRequest;
import com.marv.taskmaster.models.dto.request.task.UpdateTaskRequest;
import com.marv.taskmaster.models.dto.response.generic.PagedData;
import com.marv.taskmaster.models.dto.response.task.AssignedTaskResponse;
import com.marv.taskmaster.models.dto.response.task.TaskResponse;
import com.marv.taskmaster.models.entities.Project;
import com.marv.taskmaster.models.entities.Task;
import com.marv.taskmaster.models.entities.User;
import com.marv.taskmaster.models.enums.ProjectStatus;
import com.marv.taskmaster.models.enums.TaskStatus;
import com.marv.taskmaster.models.security.CustomUserDetails;
import com.marv.taskmaster.repositories.ProjectRepository;
import com.marv.taskmaster.repositories.TaskRepository;
import com.marv.taskmaster.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;


    @Transactional
    public TaskResponse createTask(UUID projectId, CreateTaskRequest request) {
        Project project = getProjectSecurely(projectId);

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new EntityNotFoundException("Assignee not found"));
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setProject(project);
        task.setAssignee(assignee);
        task.setStatus(TaskStatus.PENDING);

        // Adding a task to a closed project forces it to In Progress
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            project.setStatus(ProjectStatus.IN_PROGRESS);
            projectRepository.save(project);
        }

        Task savedTask = taskRepository.save(task);
        return mapToResponse(savedTask);
    }


    public PagedData<TaskResponse> getTasksByProject(UUID projectId, Pageable pageable) {
        // 1. Security Check: Ensure user owns the project
        getProjectSecurely(projectId);

        // 2. Fetch Tasks
        Page<Task> tasksPage = taskRepository.findByProjectId(projectId, pageable);

        return new PagedData<>(tasksPage.map(this::mapToResponse));
    }


    public TaskResponse getTaskById(UUID projectId, UUID taskId) {
        // Re-use existing secure helper
        Task task = getTaskSecurely(projectId, taskId);
        return mapToResponse(task);
    }


    public PagedData<AssignedTaskResponse> getMyAssignedTasks(Pageable pageable) {
        User currentUser = getCurrentUser();

        Page<Task> tasksPage = taskRepository.findByAssigneeId(currentUser.getId(), pageable);

        // Map to the new specific DTO
        return new PagedData<>(tasksPage.map(this::mapToAssignedResponse));
    }


    public TaskResponse assignTask(UUID projectId, UUID taskId, AssignTaskRequest request) {
        Task task = getTaskSecurely(projectId, taskId);

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        task.setAssignee(assignee);
        // Automatically move to IN_PROGRESS if it was pending
        if (task.getStatus() == TaskStatus.PENDING) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }

        Task savedTask = taskRepository.save(task);
        log.info("Task {} assigned to user {}", taskId, assignee.getEmail());

        return mapToResponse(savedTask);
    }


    public TaskResponse updateTask(UUID projectId, UUID taskId, UpdateTaskRequest request) {
        Task task = getTaskSecurely(projectId, taskId);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        return mapToResponse(taskRepository.save(task));
    }


    public TaskResponse cancelTask(UUID projectId, UUID taskId) {
        Task task = getTaskSecurely(projectId, taskId);
        task.setStatus(TaskStatus.CANCELLED);
        return mapToResponse(taskRepository.save(task));
    }


    @Transactional
    public TaskResponse markTaskAsDone(UUID projectId, UUID taskId) {
        Task task = getTaskSecurely(projectId, taskId);

        task.setStatus(TaskStatus.COMPLETED);
        Task savedTask = taskRepository.save(task);

        // Check for remaining open tasks
        checkAndCompleteProject(task.getProject());

        return mapToResponse(savedTask);
    }


    @Transactional
    public void deleteTasks(UUID projectId, List<UUID> taskIds) {
        Project project = getProjectSecurely(projectId);

        // Security Check
        long validCount = taskRepository.countByProjectAndIdIn(projectId, taskIds);
        if (validCount != taskIds.size()) {
            throw new AccessDeniedException("One or more tasks do not belong to the specified project");
        }

        taskRepository.deleteAllById(taskIds);
        log.info("Deleted {} tasks from project {}", taskIds.size(), projectId);

        checkAndCompleteProject(project);
    }

    // --- Private Helpers ---

    private void checkAndCompleteProject(Project project) {
        // Count tasks that are NOT (Completed or Cancelled)
        // i.e., Count PENDING or IN_PROGRESS
        long openTasks = taskRepository.countByProjectAndStatusIn(
                project.getId(),
                Arrays.asList(TaskStatus.PENDING, TaskStatus.IN_PROGRESS)
        );

        if (openTasks == 0) {
            log.info("No open tasks remaining. Marking project {} as COMPLETED", project.getId());
            project.setStatus(ProjectStatus.COMPLETED);
            projectRepository.save(project);
        }
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((CustomUserDetails) principal).getUser();
    }

    private Project getProjectSecurely(UUID projectId) {
        User currentUser = getCurrentUser();
        return projectRepository.findByIdAndOwnerId(projectId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found or access denied"));
    }

    private Task getTaskSecurely(UUID projectId, UUID taskId) {
        // 1. Ensure project belongs to user
        getProjectSecurely(projectId);

        // 2. Ensure task belongs to project
        return taskRepository.findById(taskId)
                .filter(t -> t.getProject().getId().equals(projectId))
                .orElseThrow(() -> new EntityNotFoundException("Task not found in this project"));
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .dueDate(task.getDueDate())
                .isOverdue(task.isOverdue())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ?
                        task.getAssignee().getFirstname() + " " + task.getAssignee().getLastname() : null)
                .build();
    }

    private AssignedTaskResponse mapToAssignedResponse(Task task) {
        return AssignedTaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .dueDate(task.getDueDate())
                .isOverdue(task.isOverdue())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ?
                        task.getAssignee().getFirstname() + " " + task.getAssignee().getLastname() : null)
                // Add the Project Context
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .build();
    }
}