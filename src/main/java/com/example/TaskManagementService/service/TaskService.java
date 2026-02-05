package com.example.TaskManagementService.service;

import com.example.TaskManagementService.dto.NotificationMessage;
import com.example.TaskManagementService.dto.TaskRequest;
import com.example.TaskManagementService.dto.TaskResponse;
import com.example.TaskManagementService.dto.PagedResponse;
import com.example.TaskManagementService.entity.Project;
import com.example.TaskManagementService.entity.Task;
import com.example.TaskManagementService.entity.User;
import com.example.TaskManagementService.exception.ResourceNotFoundException;
import com.example.TaskManagementService.repository.ProjectRepository;
import com.example.TaskManagementService.repository.TaskRepository;
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
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;  // Add this

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "projects", key = "#request.projectId")
    })
    public TaskResponse createTask(TaskRequest request, String userEmail) {
        log.info("Creating new task '{}' in project {} by user: {}",
                request.getTitle(), request.getProjectId(), userEmail);

        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

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
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee", "id", request.getAssigneeId()));
            task.setAssignee(assignee);

            // Send notification to assignee
            webSocketService.sendUserNotification(
                    assignee.getEmail(),
                    new NotificationMessage(
                            "New Task Assigned",
                            "You have been assigned to task: " + task.getTitle(),
                            "INFO",
                            assignee.getEmail()
                    )
            );
        }

        Task saved = taskRepository.save(task);
        TaskResponse response = mapToResponse(saved);

        log.info("Task created successfully with ID: {} in project: {}", saved.getId(), request.getProjectId());

        // Send WebSocket update to all project members
        webSocketService.sendTaskUpdate(request.getProjectId(), "CREATE", response, userEmail);

        return response;
    }

    public List<TaskResponse> getProjectTasks(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PagedResponse<TaskResponse> getProjectTasksPaginated(
            Long projectId,
            com.example.TaskManagementService.entity.TaskStatus status,
            com.example.TaskManagementService.entity.TaskPriority priority,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Task> taskPage = taskRepository.searchTasks(projectId, status, priority, search, pageable);

        List<TaskResponse> content = taskPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, taskPage);
    }

    @Cacheable(value = "tasks", key = "#id")
    public TaskResponse getTaskById(Long id) {
        log.debug("Fetching task with ID: {} (checking cache first)", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
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
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        String oldStatus = task.getStatus() != null ? task.getStatus().toString() : null;
        User oldAssignee = task.getAssignee();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : task.getStatus());
        task.setPriority(request.getPriority() != null ? request.getPriority() : task.getPriority());
        task.setDueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee", "id", request.getAssigneeId()));

            // Notify if assignee changed
            if (oldAssignee == null || !oldAssignee.getId().equals(assignee.getId())) {
                webSocketService.sendUserNotification(
                        assignee.getEmail(),
                        new NotificationMessage(
                                "Task Assigned",
                                "You have been assigned to task: " + task.getTitle(),
                                "INFO",
                                assignee.getEmail()
                        )
                );
            }

            task.setAssignee(assignee);
        }

        Task updated = taskRepository.save(task);
        TaskResponse response = mapToResponse(updated);

        // Send WebSocket update
        webSocketService.sendTaskUpdate(task.getProject().getId(), "UPDATE", response, "system");

        // Notify on status change
        String newStatus = updated.getStatus() != null ? updated.getStatus().toString() : null;
        if (oldStatus != null && !oldStatus.equals(newStatus)) {
            if (updated.getAssignee() != null) {
                webSocketService.sendUserNotification(
                        updated.getAssignee().getEmail(),
                        new NotificationMessage(
                                "Task Status Changed",
                                "Task '" + updated.getTitle() + "' status changed to: " + newStatus,
                                "INFO",
                                updated.getAssignee().getEmail()
                        )
                );
            }
        }

        return response;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#id"),
            @CacheEvict(value = "projects", allEntries = true)
    })
    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        Long projectId = task.getProject().getId();
        String taskTitle = task.getTitle();

        taskRepository.delete(task);

        // Send WebSocket update
        webSocketService.sendTaskUpdate(projectId, "DELETE",
                new TaskResponse(id, taskTitle, null, projectId, null, null, null, null, null, null, null, null),
                "system");
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