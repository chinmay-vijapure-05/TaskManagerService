package com.example.TaskManagementService.controller;

import com.example.TaskManagementService.dto.PagedResponse;
import com.example.TaskManagementService.dto.ProjectRequest;
import com.example.TaskManagementService.dto.ProjectResponse;
import com.example.TaskManagementService.entity.ProjectStatus;
import com.example.TaskManagementService.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("HTTP POST /api/projects by user={}", userDetails.getUsername());

        ProjectResponse response =
                projectService.createProject(request, userDetails.getUsername());

        log.info("Project created successfully id={} by user={}",
                response.getId(), userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getUserProjects(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("HTTP GET /api/projects by user={}", userDetails.getUsername());

        List<ProjectResponse> projects =
                projectService.getUserProjects(userDetails.getUsername());

        log.info("Fetched {} projects for user={}",
                projects.size(), userDetails.getUsername());

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProjectResponse>> searchProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info(
                "HTTP GET /api/projects/search user={} page={} size={} status={} search={}",
                userDetails.getUsername(), page, size, status, search
        );

        PagedResponse<ProjectResponse> response =
                projectService.getUserProjectsPaginated(
                        userDetails.getUsername(),
                        status,
                        search,
                        page,
                        size,
                        sortBy,
                        sortDir
                );

        log.info("Search projects returned {} items (total={}) for user={}",
                response.getContent().size(),
                response.getTotalElements(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("HTTP GET /api/projects/{} by user={}", id, userDetails.getUsername());

        ProjectResponse response =
                projectService.getProjectById(id, userDetails.getUsername());

        log.info("Fetched project id={} for user={}", id, userDetails.getUsername());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("HTTP PUT /api/projects/{} by user={}", id, userDetails.getUsername());

        ProjectResponse response =
                projectService.updateProject(id, request, userDetails.getUsername());

        log.info("Project updated id={} by user={}", id, userDetails.getUsername());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("HTTP DELETE /api/projects/{} by user={}", id, userDetails.getUsername());

        projectService.deleteProject(id, userDetails.getUsername());

        log.info("Project deleted id={} by user={}", id, userDetails.getUsername());

        return ResponseEntity.noContent().build();
    }
}
