package com.example.TaskManagementService.service;

import com.example.TaskManagementService.dto.PagedResponse;
import com.example.TaskManagementService.dto.TaskRequest;
import com.example.TaskManagementService.dto.TaskResponse;
import com.example.TaskManagementService.entity.*;
import com.example.TaskManagementService.exception.ResourceNotFoundException;
import com.example.TaskManagementService.repository.ProjectRepository;
import com.example.TaskManagementService.repository.TaskRepository;
import com.example.TaskManagementService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "projects", key = "#request.projectId")
    })
    public TaskResponse createTask(TaskRequest request, String userEmail) {
        log.info("Creating new task '{}' in project {} by user: {}",
                request.getTitle(), request.getProjectId(), userEmail);

        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Create task failed: user not found email={}", userEmail);
                    return new ResourceNotFoundException("User", "email", userEmail);
                });

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> {
                    log.warn("Create task failed: project not found id={}", request.getProjectId());
                    return new ResourceNotFoundException("Project", "id", request.getProjectId());
                });

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);
        task.setCreatedBy(creator);
        task.setStatus(request.getStatus() != null ? request.getStatus() : task.getStatus());
        task.setPriority(request.getPriority() != null ? request.getPriority() : task.getPriority());
        task.setDueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> {
                        log.warn("Create task failed: assignee not found id={}", request.getAssigneeId());
                        return new ResourceNotFoundException("Assignee", "id", request.getAssigneeId());
                    });
            task.setAssignee(assignee);
            log.debug("Task assigned to userId={}", assignee.getId());
        }

        Task saved = taskRepository.save(task);
        log.info("Task created successfully id={} projectId={}", saved.getId(), project.getId());

        return mapToResponse(saved);
    }

    public List<TaskResponse> getProjectTasks(Long projectId) {
        log.info("Fetching tasks for projectId={}", projectId);

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        log.debug("Found {} tasks for projectId={}", tasks.size(), projectId);

        return tasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "tasks", key = "#id")
    public TaskResponse getTaskById(Long id) {
        log.debug("Fetching task with ID: {} (checking cache first)", id);
        log.info("Fetching task id={}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found id={}", id);
                    return new ResourceNotFoundException("Task", "id", id);
                });

        log.info("Task fetched successfully id={}", id);
        return mapToResponse(task);
    }

    @Transactional
    @Caching(
            put = @CachePut(value = "tasks", key = "#id"),
            evict = @CacheEvict(value = "projects", allEntries = true)
    )
    public TaskResponse updateTask(Long id, TaskRequest request) {
        log.info("Updating task {} - status: {}, priority: {}", id, request.getStatus(), request.getPriority());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed: task not found id={}", id);
                    return new ResourceNotFoundException("Task", "id", id);
                });

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : task.getStatus());
        task.setPriority(request.getPriority() != null ? request.getPriority() : task.getPriority());
        task.setDueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> {
                        log.warn("Update failed: assignee not found id={}", request.getAssigneeId());
                        return new ResourceNotFoundException("Assignee", "id", request.getAssigneeId());
                    });
            task.setAssignee(assignee);
            log.debug("Updated task assignee userId={}", assignee.getId());
        }

        Task updated = taskRepository.save(task);
        log.info("Task updated successfully id={}", id);

        return mapToResponse(updated);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#id"),
            @CacheEvict(value = "projects", allEntries = true)
    })
    public void deleteTask(Long id) {
        log.info("Delete task request id={}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Delete failed: task not found id={}", id);
                    return new ResourceNotFoundException("Task", "id", id);
                });

        taskRepository.delete(task);
        log.info("Task deleted successfully id={}", id);
    }

    public PagedResponse<TaskResponse> getProjectTasksPaginated(
            Long projectId,
            TaskStatus status,
            TaskPriority priority,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        log.info(
                "Paginated task fetch projectId={} page={} size={} status={} priority={} search={}",
                projectId, page, size, status, priority, search
        );

        Sort.Direction direction =
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Task> taskPage =
                taskRepository.searchTasks(projectId, status, priority, search, pageable);

        log.debug("Paginated task result totalElements={}", taskPage.getTotalElements());

        List<TaskResponse> content = taskPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, taskPage);
    }

    private TaskResponse mapToResponse(Task task) {
        TaskResponse.AssigneeDto assigneeDto = null;
        if (task.getAssignee() != null) {
            assigneeDto = new TaskResponse.AssigneeDto(
                    task.getAssignee().getId(),
                    task.getAssignee().getEmail(),
                    task.getAssignee().getFullName()
            );
        }

        TaskResponse.AssigneeDto creatorDto = new TaskResponse.AssigneeDto(
                task.getCreatedBy().getId(),
                task.getCreatedBy().getEmail(),
                task.getCreatedBy().getFullName()
        );

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getProject().getId(),
                task.getProject().getName(),
                assigneeDto,
                creatorDto,
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
