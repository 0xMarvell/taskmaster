package com.marv.taskmaster.repositories;

import com.marv.taskmaster.models.entities.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * GET ALL PROJECTS (Paged)
     * Finds all projects belonging to a specific user.
     */
    Page<Project> findByOwnerId(UUID ownerId, Pageable pageable);

    /**
     * GET SINGLE PROJECT (Secure)
     * Finds a project by ID, BUT only if it belongs to the specific owner.
     * Prevents User A from guessing User B's project ID and accessing it.
     */
    Optional<Project> findByIdAndOwnerId(UUID id, UUID ownerId);

    /**
     * DUPLICATE CHECK
     * Checks if the user already has a project with this name (case-insensitive).
     */
    boolean existsByNameAndOwnerId(String name, UUID ownerId);
}