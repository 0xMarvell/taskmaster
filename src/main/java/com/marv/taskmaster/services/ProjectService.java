package com.marv.taskmaster.services;

import com.marv.taskmaster.models.dto.request.project.CreateProjectRequest;
import com.marv.taskmaster.models.dto.request.project.UpdateProjectRequest;
import com.marv.taskmaster.models.dto.response.generic.PagedData;
import com.marv.taskmaster.models.dto.response.project.ProjectResponse;
import com.marv.taskmaster.models.dto.response.task.TaskResponse;
import com.marv.taskmaster.models.entities.Project;
import com.marv.taskmaster.models.entities.Task;
import com.marv.taskmaster.models.entities.User;
import com.marv.taskmaster.models.enums.ProjectStatus;
import com.marv.taskmaster.models.security.CustomUserDetails;
import com.marv.taskmaster.repositories.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;

    /* =========================================================
       1. Create Project
       POST api/v1/projects
       ========================================================= */
    public ProjectResponse createProject(CreateProjectRequest request) {
        User currentUser = getCurrentUser();

        // 1. Validation
        if (projectRepository.existsByNameAndOwnerId(request.getName(), currentUser.getId())) {
            throw new DataIntegrityViolationException("You already have a project named '" + request.getName() + "'");
        }

        // 2. Map & Save
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(currentUser);
        project.setStatus(ProjectStatus.IN_PROGRESS);

        Project savedProject = projectRepository.save(project);

        log.info("Project created: ID={} Name={} Owner={}",
                savedProject.getId(), savedProject.getName(), currentUser.getEmail());

        return mapToResponse(savedProject);
    }

    /* =========================================================
       2. Get All User's Projects
       GET api/v1/projects
       ========================================================= */
    public PagedData<ProjectResponse> getMyProjects(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Project> projectsPage = projectRepository.findByOwnerId(currentUser.getId(), pageable);
        return new PagedData<>(projectsPage.map(this::mapToResponse));
    }

    /* =========================================================
       3. Get Single Project
       GET api/v1/projects/{id}
       ========================================================= */
    public ProjectResponse getProjectById(UUID projectId) {
        Project project = getProjectSecurely(projectId);
        return mapToResponse(project);
    }

    /* =========================================================
       4. Update Project
       PUT api/v1/projects/{id}
       ========================================================= */
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request) {
        Project project = getProjectSecurely(projectId);

        if (!project.getName().equalsIgnoreCase(request.getName()) &&
                projectRepository.existsByNameAndOwnerId(request.getName(), project.getOwner().getId())) {
            throw new DataIntegrityViolationException("Project name '" + request.getName() + "' is already taken.");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project updatedProject = projectRepository.save(project);
        log.info("Project updated: ID={}, Name={}", updatedProject.getId(), updatedProject.getName());

        return mapToResponse(updatedProject);
    }

    /* =========================================================
       5. Cancel Project
       POST api/v1/projects/{id}/cancel
       ========================================================= */
    public ProjectResponse cancelProject(UUID projectId) {
        Project project = getProjectSecurely(projectId);

        if (project.getStatus() == ProjectStatus.CANCELLED) {
            throw new IllegalArgumentException("Project is already cancelled");
        }

        project.setStatus(ProjectStatus.CANCELLED);
        Project savedProject = projectRepository.save(project);

        log.info("Project cancelled: ID={}, Name={}", savedProject.getId(),savedProject.getName());

        return mapToResponse(savedProject);
    }

    // =========================================================
    // Private Helpers
    // =========================================================

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new IllegalStateException("User not found in security context");
    }

    private Project getProjectSecurely(UUID projectId) {
        User currentUser = getCurrentUser();
        return projectRepository.findByIdAndOwnerId(projectId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found or access denied"));
    }

    // --- Mappers ---

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                // Safe mapping for list (handles nulls)
                .tasks(project.getTasks() == null ? Collections.emptyList() :
                        project.getTasks().stream().map(this::mapTaskToResponse).collect(Collectors.toList()))
                .build();
    }

    private TaskResponse mapTaskToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .dueDate(task.getDueDate())
                .isOverdue(task.isOverdue())
                // Handle optional assignee
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ?
                        task.getAssignee().getFirstname() + " " + task.getAssignee().getLastname() : null)
                .build();
    }
}