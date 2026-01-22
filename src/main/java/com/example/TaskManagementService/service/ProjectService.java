package com.example.TaskManagementService.service;

import com.example.TaskManagementService.dto.ProjectRequest;
import com.example.TaskManagementService.dto.ProjectResponse;
import com.example.TaskManagementService.entity.Project;
import com.example.TaskManagementService.entity.User;
import com.example.TaskManagementService.repository.ProjectRepository;
import com.example.TaskManagementService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, String userEmail) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            List<User> members = userRepository.findAllById(request.getMemberIds());
            project.setMembers(members);
        }

        Project saved = projectRepository.save(project);
        return mapToResponse(saved);
    }

    public List<ProjectResponse> getUserProjects(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Project> projects = projectRepository.findAllUserProjects(user.getId());
        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id, String userEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Check if user has access
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!hasAccess(project, user)) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, String userEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!project.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Only owner can update project");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        if (request.getMemberIds() != null) {
            List<User> members = userRepository.findAllById(request.getMemberIds());
            project.setMembers(members);
        }

        Project updated = projectRepository.save(project);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProject(Long id, String userEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!project.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Only owner can delete project");
        }

        projectRepository.delete(project);
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