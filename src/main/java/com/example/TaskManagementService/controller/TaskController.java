package com.example.TaskManagementService.controller;

import com.example.TaskManagementService.dto.PagedResponse;
import com.example.TaskManagementService.dto.TaskRequest;
import com.example.TaskManagementService.dto.TaskResponse;
import com.example.TaskManagementService.entity.TaskPriority;
import com.example.TaskManagementService.entity.TaskStatus;
import com.example.TaskManagementService.service.TaskService;
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
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("HTTP POST /api/tasks by user={} projectId={}",
                userDetails.getUsername(), request.getProjectId());

        TaskResponse response =
                taskService.createTask(request, userDetails.getUsername());

        log.info("Task created id={} by user={}",
                response.getId(), userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponse>> getProjectTasks(
            @PathVariable Long projectId) {

        log.info("HTTP GET /api/tasks/project/{}", projectId);

        List<TaskResponse> tasks =
                taskService.getProjectTasks(projectId);

        log.info("Fetched {} tasks for projectId={}",
                tasks.size(), projectId);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<TaskResponse>> searchTasks(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info(
                "HTTP GET /api/tasks/search projectId={} page={} size={} status={} priority={} search={}",
                projectId, page, size, status, priority, search
        );

        PagedResponse<TaskResponse> response =
                taskService.getProjectTasksPaginated(
                        projectId,
                        status,
                        priority,
                        search,
                        page,
                        size,
                        sortBy,
                        sortDir
                );

        log.info("Search tasks returned {} items (total={})",
                response.getContent().size(),
                response.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {

        log.info("HTTP GET /api/tasks/{}", id);

        TaskResponse response =
                taskService.getTaskById(id);

        log.info("Fetched task id={}", id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {

        log.info("HTTP PUT /api/tasks/{}", id);

        TaskResponse response =
                taskService.updateTask(id, request);

        log.info("Task updated id={}", id);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {

        log.info("HTTP DELETE /api/tasks/{}", id);

        taskService.deleteTask(id);

        log.info("Task deleted id={}", id);

        return ResponseEntity.noContent().build();
    }
}
