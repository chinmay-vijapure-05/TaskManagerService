package com.example.TaskManagementService.repository;

import com.example.TaskManagementService.entity.Project;
import com.example.TaskManagementService.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerId(Long ownerId);

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :userId")
    List<Project> findByMemberId(Long userId);

    @Query("SELECT p FROM Project p WHERE p.owner.id = :userId OR :userId IN (SELECT m.id FROM p.members m)")
    List<Project> findAllUserProjects(Long userId);

    @Query("SELECT p FROM Project p WHERE p.owner.id = :userId OR :userId IN (SELECT m.id FROM p.members m)")
    Page<Project> findAllUserProjects(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("SELECT p FROM Project p WHERE (p.owner.id = :userId OR :userId IN (SELECT m.id FROM p.members m)) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR :search = '' OR LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Project> searchUserProjects(
            @Param("userId") Long userId,
            @Param("status") ProjectStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}