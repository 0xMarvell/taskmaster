package com.marv.taskmaster.repositories;

import com.marv.taskmaster.models.entities.Task;
import com.marv.taskmaster.models.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // Count how many tasks in a project are NOT Completed or Cancelled
    // Used to check if project should be closed
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status IN (:statuses)")
    long countByProjectAndStatusIn(@Param("projectId") UUID projectId, @Param("statuses") List<TaskStatus> statuses);

    // Verify a list of task IDs belong to a specific project (Security for Bulk Delete)
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.id IN :taskIds")
    long countByProjectAndIdIn(@Param("projectId") UUID projectId, @Param("taskIds") List<UUID> taskIds);

    /**
     * Bulk update to mark overdue tasks.
     * Logic:
     * 1. Due date is in the past (< now)
     * 2. isOverdue is currently false (don't update rows that are already marked)
     * 3. Status is NOT Completed or Cancelled (only active tasks can be overdue)
     */
    @Modifying // Required for UPDATE/DELETE queries
    @Query("UPDATE Task t SET t.isOverdue = true WHERE t.dueDate < :now AND t.isOverdue = false AND t.status IN ('PENDING', 'IN_PROGRESS')")
    int markOverdueTasks(@Param("now") LocalDateTime now);

    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    // Fetch all tasks assigned to a specific user across all projects
    Page<Task> findByAssigneeId(UUID assigneeId, Pageable pageable);
}