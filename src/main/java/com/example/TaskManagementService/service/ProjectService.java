package com.example.TaskManagementService.service;

import com.example.TaskManagementService.dto.PagedResponse;
import com.example.TaskManagementService.dto.ProjectRequest;
import com.example.TaskManagementService.dto.ProjectResponse;
import com.example.TaskManagementService.entity.Project;
import com.example.TaskManagementService.entity.ProjectStatus;
import com.example.TaskManagementService.entity.User;
import com.example.TaskManagementService.exception.ResourceNotFoundException;
import com.example.TaskManagementService.exception.UnauthorizedException;
import com.example.TaskManagementService.repository.ProjectRepository;
import com.example.TaskManagementService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, String userEmail) {
        log.info("Create project request received by user={}", userEmail);

        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Create project failed: user not found email={}", userEmail);
                    return new ResourceNotFoundException("User", "email", userEmail);
                });

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            List<User> members = userRepository.findAllById(request.getMemberIds());
            project.setMembers(members);
            log.debug("Added {} members to project", members.size());
        }

        Project saved = projectRepository.save(project);
        log.info("Project created successfully id={} owner={}", saved.getId(), userEmail);

        return mapToResponse(saved);
    }

    public List<ProjectResponse> getUserProjects(String userEmail) {
        log.info("Fetching projects for user={}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Fetch projects failed: user not found email={}", userEmail);
                    return new ResourceNotFoundException("User", "email", userEmail);
                });

        List<Project> projects = projectRepository.findAllUserProjects(user.getId());
        log.debug("Found {} projects for user={}", projects.size(), userEmail);

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id, String userEmail) {
        log.info("Fetching project id={} requested by user={}", id, userEmail);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Project not found id={}", id);
                    return new ResourceNotFoundException("Project", "id", id);
                });

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("User not found email={}", userEmail);
                    return new ResourceNotFoundException("User", "email", userEmail);
                });

        if (!hasAccess(project, user)) {
            log.warn("Unauthorized access attempt: user={} projectId={}", userEmail, id);
            throw new UnauthorizedException("You don't have access to this project");
        }

        log.info("Project fetched successfully id={} for user={}", id, userEmail);
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, String userEmail) {
        log.info("Update project request id={} by user={}", id, userEmail);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed: project not found id={}", id);
                    return new ResourceNotFoundException("Project", "id", id);
                });

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Update failed: user not found email={}", userEmail);
                    return new ResourceNotFoundException("User", "email", userEmail);
                });

        if (!project.getOwner().getId().equals(user.getId())) {
            log.warn("Unauthorized update attempt: user={} projectId={}", userEmail, id);
            throw new UnauthorizedException("Only project owner can update this project");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        if (request.getMemberIds() != null) {
            List<User> members = userRepository.findAllById(request.getMemberIds());
            project.setMembers(members);
            log.debug("Updated project members count={}", members.size());
        }

        Project updated = projectRepository.save(project);
        log.info("Project updated successfully id={}", id);

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProject(Long id, String userEmail) {
        log.info("Delete project request id={} by user={}", id, userEmail);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Delete failed: project not found id={}", id);
                    return new ResourceNotFoundException("Project", "id", id);
                });

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Delete failed: user not found email={}", userEmail);
                    return new ResourceNotFoundException("User", "email", userEmail);
                });

        if (!project.getOwner().getId().equals(user.getId())) {
            log.warn("Unauthorized delete attempt: user={} projectId={}", userEmail, id);
            throw new UnauthorizedException("Only project owner can delete this project");
        }

        projectRepository.delete(project);
        log.info("Project deleted successfully id={}", id);
    }

    public PagedResponse<ProjectResponse> getUserProjectsPaginated(
            String userEmail,
            ProjectStatus status,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        log.info(
                "Paginated project fetch user={} page={} size={} status={} search={}",
                userEmail, page, size, status, search
        );

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Pagination failed: user not found email={}", userEmail);
                    return new ResourceNotFoundException("User", "email", userEmail);
                });

        Sort.Direction direction =
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Project> projectPage =
                projectRepository.searchUserProjects(user.getId(), status, search, pageable);

        log.debug("Paginated result totalElements={}", projectPage.getTotalElements());

        List<ProjectResponse> content = projectPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, projectPage);
    }

    private boolean hasAccess(Project project, User user) {
        return project.getOwner().getId().equals(user.getId()) ||
                project.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId()));
    }

    private ProjectResponse mapToResponse(Project project) {
        List<ProjectResponse.MemberDto> members = project.getMembers().stream()
                .map(m -> new ProjectResponse.MemberDto(
                        m.getId(),
                        m.getEmail(),
                        m.getFullName()
                ))
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
