package com.example.TaskManagementService.controller;

import com.example.TaskManagementService.BaseIntegrationTest;
import com.example.TaskManagementService.dto.ProjectRequest;
import com.example.TaskManagementService.dto.RegisterRequest;
import com.example.TaskManagementService.dto.TaskRequest;
import com.example.TaskManagementService.entity.TaskPriority;
import com.example.TaskManagementService.entity.TaskStatus;
import com.example.TaskManagementService.repository.ProjectRepository;
import com.example.TaskManagementService.repository.TaskRepository;
import com.example.TaskManagementService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

class TaskControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    private String authToken;
    private Long projectId;

    @BeforeEach
    void setup() throws Exception {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("task@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Task User");

        MvcResult authResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        authToken = objectMapper
                .readTree(authResult.getResponse().getContentAsString())
                .get("token").asText();

        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("Test Project");
        projectRequest.setDescription("Test Description");

        MvcResult projectResult = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        projectId = objectMapper
                .readTree(projectResult.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    void shouldCreateTask() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("New Task");
        request.setDescription("Task Description");
        request.setProjectId(projectId);
        request.setPriority(TaskPriority.HIGH);
        request.setStatus(TaskStatus.TODO);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("Task Description"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.projectId").value(projectId));
    }

    @Test
    void shouldGetProjectTasks() throws Exception {
        TaskRequest task1 = new TaskRequest();
        task1.setTitle("Task 1");
        task1.setProjectId(projectId);

        TaskRequest task2 = new TaskRequest();
        task2.setTitle("Task 2");
        task2.setProjectId(projectId);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks/project/" + projectId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Task 1", "Task 2")));
    }

    @Test
    void shouldUpdateTask() throws Exception {
        TaskRequest createRequest = new TaskRequest();
        createRequest.setTitle("Original Task");
        createRequest.setProjectId(projectId);

        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Task");
        updateRequest.setProjectId(projectId);
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(TaskPriority.URGENT);

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("URGENT"));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("Delete Task");
        request.setProjectId(projectId);

        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchTasksWithFilters() throws Exception {
        TaskRequest todoTask = new TaskRequest();
        todoTask.setTitle("TODO Task");
        todoTask.setProjectId(projectId);
        todoTask.setStatus(TaskStatus.TODO);

        TaskRequest inProgressTask = new TaskRequest();
        inProgressTask.setTitle("In Progress Task");
        inProgressTask.setProjectId(projectId);
        inProgressTask.setStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoTask)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inProgressTask)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks/search")
                        .header("Authorization", "Bearer " + authToken)
                        .param("projectId", projectId.toString())
                        .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("TODO"));
    }

    @Test
    void shouldValidateTaskTitle() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("AB");
        request.setProjectId(projectId);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
