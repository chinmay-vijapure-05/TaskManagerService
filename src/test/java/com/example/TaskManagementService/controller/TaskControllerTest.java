//package com.example.TaskManagementService.controller;
//
//import com.example.TaskManagementService.BaseIntegrationTest;
//import com.example.TaskManagementService.dto.ProjectRequest;
//import com.example.TaskManagementService.dto.RegisterRequest;
//import com.example.TaskManagementService.dto.TaskRequest;
//import com.example.TaskManagementService.entity.TaskPriority;
//import com.example.TaskManagementService.entity.TaskStatus;
//import com.example.TaskManagementService.repository.ProjectRepository;
//import com.example.TaskManagementService.repository.TaskRepository;
//import com.example.TaskManagementService.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MvcResult;
//
//import java.time.LocalDateTime;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.hamcrest.Matchers.*;
//
//class TaskControllerTest extends BaseIntegrationTest {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ProjectRepository projectRepository;
//
//    @Autowired
//    private TaskRepository taskRepository;
//
//    private String authToken;
//    private Long projectId;
//
//    @BeforeEach
//    void setup() throws Exception {
//        taskRepository.deleteAll();
//        projectRepository.deleteAll();
//        userRepository.deleteAll();
//
//        // Register user
//        RegisterRequest registerRequest = new RegisterRequest();
//        registerRequest.setEmail("task@test.com");
//        registerRequest.setPassword("password123");
//        registerRequest.setFullName("Task User");
//
//        MvcResult authResult = mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(registerRequest)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String authResponse = authResult.getResponse().getContentAsString();
//        authToken = objectMapper.readTree(authResponse).get("token").asText();
//
//        // Create a project
//        ProjectRequest projectRequest = new ProjectRequest();
//        projectRequest.setName("Test Project");
//        projectRequest.setDescription("Test Description");
//
//        MvcResult projectResult = mockMvc.perform(post("/api/projects")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(projectRequest)))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        String projectResponse = projectResult.getResponse().getContentAsString();
//        projectId = objectMapper.readTree(projectResponse).get("id").asLong();
//    }
//
//    @Test
//    void shouldCreateTask() throws Exception {
//        TaskRequest request = new TaskRequest();
//        request.setTitle("New Task");
//        request.setDescription("Task Description");
//        request.setProjectId(projectId);
//        request.setPriority(TaskPriority.HIGH);
//        request.setStatus(TaskStatus.TODO);
//
//        mockMvc.perform(post("/api/tasks")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.title", is("New Task")))
//                .andExpect(jsonPath("$.description", is("Task Description")))
//                .andExpect(jsonPath("$.priority", is("HIGH")))
//                .andExpect(jsonPath("$.status", is("TODO")));
//    }
//
//    @Test
//    void shouldGetProjectTasks() throws Exception {
//        // Create two tasks
//        TaskRequest task1 = new TaskRequest();
//        task1.setTitle("Task 1");
//        task1.setDescription("Description 1");
//        task1.setProjectId(projectId);
//        task1.setPriority(TaskPriority.MEDIUM);
//
//        TaskRequest task2 = new TaskRequest();
//        task2.setTitle("Task 2");
//        task2.setDescription("Description 2");
//        task2.setProjectId(projectId);
//        task2.setPriority(TaskPriority.LOW);
//
//        mockMvc.perform(post("/api/tasks")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(task1)))
//                .andExpect(status().isCreated());
//
//        mockMvc.perform(post("/api/tasks")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(task2)))
//                .andExpect(status().isCreated());
//
//        // Get all tasks for project
//        mockMvc.perform(get("/api/tasks/project/" + projectId)
//                        .header("Authorization", "Bearer " + authToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)));
//    }
//
//    @Test
//    void shouldUpdateTask() throws Exception {
//        // Create task
//        TaskRequest createRequest = new TaskRequest();
//        createRequest.setTitle("Original Task");
//        createRequest.setDescription("Original Description");
//        createRequest.setProjectId(projectId);
//        createRequest.setStatus(TaskStatus.TODO);
//
//        MvcResult createResult = mockMvc.perform(post("/api/tasks")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createRequest)))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        String createResponse = createResult.getResponse().getContentAsString();
//        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();
//
//        // Update task
//        TaskRequest updateRequest = new TaskRequest();
//        updateRequest.setTitle("Updated Task");
//        updateRequest.setDescription("Updated Description");
//        updateRequest.setProjectId(projectId);
//        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
//        updateRequest.setPriority(TaskPriority.URGENT);
//
//        mockMvc.perform(put("/api/tasks/" + taskId)
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.title", is("Updated Task")))
//                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
//                .andExpect(jsonPath("$.priority", is("URGENT")));
//    }
//
//    @Test
//    void shouldDeleteTask() throws Exception {
//        // Create task
//        TaskRequest request = new TaskRequest();
//        request.setTitle("Delete Task");
//        request.setDescription("To be deleted");
//        request.setProjectId(projectId);
//
//        MvcResult createResult = mockMvc.perform(post("/api/tasks")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        String createResponse = createResult.getResponse().getContentAsString();
//        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();
//
//        // Delete task
//        mockMvc.perform(delete("/api/tasks/" + taskId)
//                        .header("Authorization", "Bearer " + authToken))
//                .andExpect(status().isNoContent());
//
//        // Verify deletion
//        mockMvc.perform(get("/api/tasks/" + taskId)
//                        .header("Authorization", "Bearer " + authToken))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void shouldSearchTasksWithFilters() throws Exception {
//        // Create tasks with different statuses
//        TaskRequest todoTask = new TaskRequest();
//        todoTask.setTitle("TODO Task");
//        todoTask.setProjectId(projectId);
//        todoTask.setStatus(TaskStatus.TODO);
//        todoTask.setPriority(TaskPriority.HIGH);
//
//        TaskRequest inProgressTask = new TaskRequest();
//        inProgressTask.setTitle("In Progress Task");
//        inProgressTask.setProjectId(projectId);
//        inProgressTask.setStatus(TaskStatus.IN_PROGRESS);
//        inProgressTask.setPriority(TaskPriority.MEDIUM);
//
//        mockMvc.perform(post("/api/tasks")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(todoTask)))
//                .andExpect(status().isCreated());
//
//        mockMvc.perform(post("/api/tasks")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(inProgressTask)))
//                .andExpect(status().isCreated());
//
//        // Search with status filter
//        mockMvc.perform(get("/api/tasks/search")
//                        .header("Authorization", "Bearer " + authToken)
//                        .param("status", "TODO"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content", hasSize(1)))
//                .andExpect(jsonPath("$.content[0].status", is("TODO")));
//    }
//
//    @Test
//    void shouldValidateTaskTitle() throws Exception {
//        TaskRequest request = new TaskRequest();
//        request.setTitle("AB");  // Too short
//        request.setProjectId(projectId);
//
//        mockMvc.perform(post("/api/tasks")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//}