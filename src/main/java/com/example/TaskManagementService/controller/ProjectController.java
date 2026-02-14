package com.example.TaskManagementService.controller;

import com.example.TaskManagementService.dto.PagedResponse;
import com.example.TaskManagementService.dto.ProjectRequest;
import com.example.TaskManagementService.dto.ProjectResponse;
import com.example.TaskManagementService.entity.ProjectStatus;
import com.example.TaskManagementService.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Projects", description = "Project Management APIs")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "Create a new project",
            description = "Creates a new project for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Project created successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProjectResponse response =
                projectService.createProject(request, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all projects",
            description = "Returns all projects owned by the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Projects fetched successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getUserProjects(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                projectService.getUserProjects(userDetails.getUsername())
        );
    }

    @Operation(
            summary = "Search projects with pagination",
            description = "Search and filter projects with optional status, keyword search, sorting and pagination."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Projects fetched successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProjectResponse>> searchProjects(
            @Parameter(description = "Filter by project status")
            @RequestParam(required = false) ProjectStatus status,

            @Parameter(description = "Search keyword in project name")
            @RequestParam(required = false) String search,

            @Parameter(description = "Page number (default: 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (default: 10)")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort by field (default: createdAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction: asc or desc (default: desc)")
            @RequestParam(defaultValue = "desc") String sortDir,

            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                projectService.getUserProjectsPaginated(
                        userDetails.getUsername(),
                        status,
                        search,
                        page,
                        size,
                        sortBy,
                        sortDir
                )
        );
    }

    @Operation(
            summary = "Get project by ID",
            description = "Returns a specific project by ID if owned by authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Project found",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                projectService.getProjectById(id, userDetails.getUsername())
        );
    }

    @Operation(
            summary = "Update project",
            description = "Updates an existing project owned by authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Project updated successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                projectService.updateProject(id, request, userDetails.getUsername())
        );
    }

    @Operation(
            summary = "Delete project",
            description = "Deletes a project owned by authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        projectService.deleteProject(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
