package com.example.TaskManagementService.service;

import com.example.TaskManagementService.dto.ProjectRequest;
import com.example.TaskManagementService.dto.ProjectResponse;
import com.example.TaskManagementService.dto.PagedResponse;
import com.example.TaskManagementService.entity.Project;
import com.example.TaskManagementService.entity.ProjectStatus;
import com.example.TaskManagementService.entity.User;
import com.example.TaskManagementService.exception.ResourceNotFoundException;
import com.example.TaskManagementService.exception.UnauthorizedException;
import com.example.TaskManagementService.repository.ProjectRepository;
import com.example.TaskManagementService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", allEntries = true)
    })
    public ProjectResponse createProject(ProjectRequest request, String userEmail) {
        log.info("Creating new project '{}' for user: {}", request.getName(), userEmail);

        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            List<User> members = userRepository.findAllById(request.getMemberIds());
            project.setMembers(members);
            log.debug("Added {} members to project '{}'", members.size(), request.getName());
        }

        Project saved = projectRepository.save(project);
        ProjectResponse response = mapToResponse(saved);

        log.info("Project created successfully with ID: {} by user: {}", saved.getId(), userEmail);

        // Send WebSocket notification
        webSocketService.sendProjectUpdateMessage(saved.getId(), "CREATE", response, userEmail);

        return response;
    }

    @Cacheable(value = "projects", key = "'user:' + #userEmail")
    public List<ProjectResponse> getUserProjects(String userEmail) {
        log.debug("Fetching all projects for user: {} (checking cache first)", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        List<Project> projects = projectRepository.findAllUserProjects(user.getId(), PageRequest.of(0, 100)).getContent();
        log.info("Found {} projects for user: {} (cached)", projects.size(), userEmail);

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PagedResponse<ProjectResponse> getUserProjectsPaginated(
            String userEmail,
            ProjectStatus status,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        log.debug("Searching projects for user: {} with filters - status: {}, search: {}, page: {}",
                userEmail, status, search, page);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Project> projectPage = projectRepository.searchUserProjects(user.getId(), status, search, pageable);

        List<ProjectResponse> content = projectPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("Retrieved {} projects (page {}/{}) for user: {}",
                content.size(), page + 1, projectPage.getTotalPages(), userEmail);

        return new PagedResponse<>(content, projectPage);
    }

    @Cacheable(value = "projects", key = "#id")
    public ProjectResponse getProjectById(Long id, String userEmail) {
        log.debug("Fetching project with ID: {} for user: {} (checking cache first)", id, userEmail);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (!hasAccess(project, user)) {
            log.warn("Access denied - User {} attempted to access project {}", userEmail, id);
            throw new UnauthorizedException("You don't have access to this project");
        }

        log.debug("Project {} retrieved successfully by user: {} (cached)", id, userEmail);
        return mapToResponse(project);
    }

    @Transactional
    @Caching(
            put = @CachePut(value = "projects", key = "#id"),
            evict = @CacheEvict(value = "projects", key = "'user:' + #userEmail")
    )
    public ProjectResponse updateProject(Long id, ProjectRequest request, String userEmail) {
        log.info("Updating project {} by user: {}", id, userEmail);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (!project.getOwner().getId().equals(user.getId())) {
            log.warn("Unauthorized update attempt - User {} is not owner of project {}", userEmail, id);
            throw new UnauthorizedException("Only project owner can update this project");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        if (request.getMemberIds() != null) {
            List<User> members = userRepository.findAllById(request.getMemberIds());
            project.setMembers(members);
            log.debug("Updated members for project {}: {} members", id, members.size());
        }

        Project updated = projectRepository.save(project);
        ProjectResponse response = mapToResponse(updated);

        log.info("Project {} updated successfully by user: {} (cache updated)", id, userEmail);

        // Send WebSocket notification
        webSocketService.sendProjectUpdateMessage(id, "UPDATE", response, userEmail);

        return response;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projects", key = "'user:' + #userEmail"),
            @CacheEvict(value = "tasks", allEntries = true)
    })
    public void deleteProject(Long id, String userEmail) {
        log.info("Deleting project {} by user: {}", id, userEmail);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (!project.getOwner().getId().equals(user.getId())) {
            log.warn("Unauthorized delete attempt - User {} is not owner of project {}", userEmail, id);
            throw new UnauthorizedException("Only project owner can delete this project");
        }

        // Send WebSocket notification before deleting
        webSocketService.sendProjectUpdateMessage(
                id,
                "DELETE",
                new ProjectResponse(id, project.getName(), null, null, null, null, null, null, null, null),
                userEmail
        );

        projectRepository.delete(project);
        log.info("Project {} deleted successfully by user: {} (cache cleared)", id, userEmail);
    }

    private boolean hasAccess(Project project, User user) {
        return project.getOwner().getId().equals(user.getId()) ||
                project.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));
    }

    private ProjectResponse mapToResponse(Project project) {
        List<ProjectResponse.MemberDto> members = project.getMembers().stream()
                .map(m -> new ProjectResponse.MemberDto(m.getId(), m.getEmail(), m.getFullName()))
                .collect(Collectors.toList());

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getEmail(),
                project.getOwner().getFullName(),
                members,
                project.getStatus(),
                project.getTasks() != null ? project.getTasks().size() : 0,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}