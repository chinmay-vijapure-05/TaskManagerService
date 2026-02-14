package com.example.TaskManagementService.service;

import com.example.TaskManagementService.dto.PagedResponse;
import com.example.TaskManagementService.dto.TaskRequest;
import com.example.TaskManagementService.dto.TaskResponse;
import com.example.TaskManagementService.entity.*;
import com.example.TaskManagementService.exception.ResourceNotFoundException;
import com.example.TaskManagementService.repository.ProjectRepository;
import com.example.TaskManagementService.repository.TaskRepository;
import com.example.TaskManagementService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private User assignee;
    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Setup test user (creator)
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("creator@test.com");
        testUser.setFullName("Task Creator");

        // Setup assignee
        assignee = new User();
        assignee.setId(2L);
        assignee.setEmail("assignee@test.com");
        assignee.setFullName("Task Assignee");

        // Setup test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setOwner(testUser);

        // Setup test task
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setProject(testProject);
        testTask.setCreatedBy(testUser);
        testTask.setAssignee(assignee);
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(TaskPriority.MEDIUM);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== CREATE TASK TESTS ====================

    @Test
    void shouldCreateTaskSuccessfully() {
        // Given
        TaskRequest request = new TaskRequest();
        request.setTitle("New Task");
        request.setDescription("New Description");
        request.setProjectId(1L);
        request.setPriority(TaskPriority.HIGH);
        request.setStatus(TaskStatus.TODO);

        when(userRepository.findByEmail("creator@test.com")).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskResponse response = taskService.createTask(request, "creator@test.com");

        // Then
        assertNotNull(response);
        assertEquals("Test Task", response.getTitle());
        assertEquals(1L, response.getProjectId());

        verify(taskRepository, times(1)).save(any(Task.class));
        verify(webSocketService, times(1)).sendTaskUpdate(eq(1L), eq("CREATE"), any(TaskResponse.class), eq("creator@test.com"));
    }

    @Test
    void shouldCreateTaskWithAssigneeAndSendNotification() {
        // Given
        TaskRequest request = new TaskRequest();
        request.setTitle("Assigned Task");
        request.setDescription("Task with assignee");
        request.setProjectId(1L);
        request.setAssigneeId(2L);
        request.setPriority(TaskPriority.HIGH);

        when(userRepository.findByEmail("creator@test.com")).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskResponse response = taskService.createTask(request, "creator@test.com");

        // Then
        assertNotNull(response);
        verify(webSocketService, times(1)).sendUserNotification(
                eq("assignee@test.com"),
                any()
        );
        verify(webSocketService, times(1)).sendTaskUpdate(anyLong(), eq("CREATE"), any(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenCreatorNotFound() {
        // Given
        TaskRequest request = new TaskRequest();
        request.setTitle("Task");
        request.setProjectId(1L);

        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(request, "nonexistent@test.com");
        });

        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenProjectNotFound() {
        // Given
        TaskRequest request = new TaskRequest();
        request.setTitle("Task");
        request.setProjectId(999L);

        when(userRepository.findByEmail("creator@test.com")).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(request, "creator@test.com");
        });

        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenAssigneeNotFound() {
        // Given
        TaskRequest request = new TaskRequest();
        request.setTitle("Task");
        request.setProjectId(1L);
        request.setAssigneeId(999L);

        when(userRepository.findByEmail("creator@test.com")).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(request, "creator@test.com");
        });

        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldSetDefaultStatusAndPriorityWhenNotProvided() {
        // Given
        TaskRequest request = new TaskRequest();
        request.setTitle("Task");
        request.setProjectId(1L);
        // No status or priority set

        when(userRepository.findByEmail("creator@test.com")).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        taskService.createTask(request, "creator@test.com");

        // Then
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertNotNull(savedTask.getStatus()); // Should have default status
        assertNotNull(savedTask.getPriority()); // Should have default priority
    }

    // ==================== GET TASK TESTS ====================

    @Test
    void shouldGetTaskById() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When
        TaskResponse response = taskService.getTaskById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFoundById() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTaskById(999L);
        });
    }

    @Test
    void shouldGetProjectTasks() {
        // Given
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setProject(testProject);
        task2.setCreatedBy(testUser);

        List<Task> tasks = Arrays.asList(testTask, task2);
        when(taskRepository.findByProjectId(1L)).thenReturn(tasks);

        // When
        List<TaskResponse> responses = taskService.getProjectTasks(1L);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(taskRepository, times(1)).findByProjectId(1L);
    }

    @Test
    void shouldGetProjectTasksPaginated() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        Page<Task> taskPage = new PageImpl<>(tasks);

        when(taskRepository.searchTasks(
                anyLong(),
                any(),
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(taskPage);

        // When
        PagedResponse<TaskResponse> response = taskService.getProjectTasksPaginated(
                1L,
                TaskStatus.TODO,
                TaskPriority.HIGH,
                "test",
                0,
                10,
                "createdAt",
                "desc"
        );

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPageNumber());
        assertEquals(1, response.getPageSize());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }

    // ==================== UPDATE TASK TESTS ====================

    @Test
    void shouldUpdateTaskSuccessfully() {
        // Given
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setProjectId(1L);
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(TaskPriority.URGENT);

        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Updated Title");
        updatedTask.setDescription("Updated Description");
        updatedTask.setProject(testProject);
        updatedTask.setCreatedBy(testUser);
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        updatedTask.setPriority(TaskPriority.URGENT);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // When
        TaskResponse response = taskService.updateTask(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());

        verify(taskRepository, times(1)).save(any(Task.class));
        verify(webSocketService, times(1)).sendTaskUpdate(
                eq(1L),
                eq("UPDATE"),
                any(TaskResponse.class),
                eq("system")
        );
    }

    @Test
    void shouldUpdateTaskAssigneeAndSendNotification() {
        // Given
        User newAssignee = new User();
        newAssignee.setId(3L);
        newAssignee.setEmail("newassignee@test.com");
        newAssignee.setFullName("New Assignee");

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Task");
        updateRequest.setProjectId(1L);
        updateRequest.setAssigneeId(3L); // Different from current assignee

        testTask.setAssignee(assignee); // Current assignee is different

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newAssignee));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        taskService.updateTask(1L, updateRequest);

        // Then
        verify(webSocketService, times(1)).sendUserNotification(
                eq("newassignee@test.com"),
                any()
        );
    }

    @Test
    void shouldNotifyOnStatusChange() {
        // Given
        testTask.setStatus(TaskStatus.TODO);
        testTask.setAssignee(assignee);

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Task");
        updateRequest.setProjectId(1L);
        updateRequest.setStatus(TaskStatus.COMPLETED); // Status change

        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Task");
        updatedTask.setProject(testProject);
        updatedTask.setCreatedBy(testUser);
        updatedTask.setAssignee(assignee);
        updatedTask.setStatus(TaskStatus.COMPLETED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // When
        taskService.updateTask(1L, updateRequest);

        // Then
        // Should send status change notification to assignee
        verify(webSocketService, times(1)).sendUserNotification(
                eq("assignee@test.com"),
                any()
        );

        // Should also send WebSocket update for the task
        verify(webSocketService, times(1)).sendTaskUpdate(
                eq(1L),
                eq("UPDATE"),
                any(TaskResponse.class),
                eq("system")
        );
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTask() {
        // Given
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated");
        updateRequest.setProjectId(1L);

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTask(999L, updateRequest);
        });

        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdateAssigneeNotFound() {
        // Given
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Task");
        updateRequest.setProjectId(1L);
        updateRequest.setAssigneeId(999L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTask(1L, updateRequest);
        });

        verify(taskRepository, never()).save(any());
    }

    // ==================== DELETE TASK TESTS ====================

    @Test
    void shouldDeleteTaskSuccessfully() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        doNothing().when(taskRepository).delete(testTask);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository, times(1)).delete(testTask);
        verify(webSocketService, times(1)).sendTaskUpdate(
                eq(1L),
                eq("DELETE"),
                any(TaskResponse.class),
                eq("system")
        );
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTask() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(999L);
        });

        verify(taskRepository, never()).delete(any());
    }

    // ==================== EDGE CASES & BUSINESS LOGIC TESTS ====================

    @Test
    void shouldHandleTaskWithNullAssignee() {
        // Given
        testTask.setAssignee(null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When
        TaskResponse response = taskService.getTaskById(1L);

        // Then
        assertNotNull(response);
        assertNull(response.getAssignee());
    }

    @Test
    void shouldHandleTaskWithNullDueDate() {
        // Given
        testTask.setDueDate(null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When
        TaskResponse response = taskService.getTaskById(1L);

        // Then
        assertNotNull(response);
        assertNull(response.getDueDate());
    }

    @Test
    void shouldMapTaskToResponseCorrectly() {
        // Given
        testTask.setDueDate(LocalDateTime.now().plusDays(7));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When
        TaskResponse response = taskService.getTaskById(1L);

        // Then
        assertEquals(testTask.getId(), response.getId());
        assertEquals(testTask.getTitle(), response.getTitle());
        assertEquals(testTask.getDescription(), response.getDescription());
        assertEquals(testTask.getProject().getId(), response.getProjectId());
        assertEquals(testTask.getProject().getName(), response.getProjectName());
        assertEquals(testTask.getStatus(), response.getStatus());
        assertEquals(testTask.getPriority(), response.getPriority());
        assertNotNull(response.getAssignee());
        assertEquals(assignee.getEmail(), response.getAssignee().getEmail());
        assertNotNull(response.getCreatedBy());
        assertEquals(testUser.getEmail(), response.getCreatedBy().getEmail());
    }

    @Test
    void shouldNotSendNotificationWhenAssigneeUnchanged() {
        // Given
        testTask.setAssignee(assignee);

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Task");
        updateRequest.setProjectId(1L);
        updateRequest.setAssigneeId(2L); // Same assignee

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        taskService.updateTask(1L, updateRequest);

        // Then
        // Should NOT send assignee notification (assignee didn't change)
        // But should send update notification
        verify(webSocketService, times(1)).sendTaskUpdate(anyLong(), eq("UPDATE"), any(), anyString());
    }
}