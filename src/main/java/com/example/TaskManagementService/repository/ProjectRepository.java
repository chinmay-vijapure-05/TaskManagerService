package com.example.TaskManagementService.repository;

import com.example.TaskManagementService.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerId(Long ownerId);

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :userId")
    List<Project> findByMemberId(Long userId);

    @Query("SELECT p FROM Project p WHERE p.owner.id = :userId OR :userId IN (SELECT m.id FROM p.members m)")
    List<Project> findAllUserProjects(Long userId);
}