//package com.example.TaskManagementService.controller;
//
//import com.example.TaskManagementService.BaseIntegrationTest;
//import com.example.TaskManagementService.dto.ProjectRequest;
//import com.example.TaskManagementService.dto.RegisterRequest;
//import com.example.TaskManagementService.repository.ProjectRepository;
//import com.example.TaskManagementService.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MvcResult;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.hamcrest.Matchers.*;
//
//class ProjectControllerTest extends BaseIntegrationTest {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ProjectRepository projectRepository;
//
//    private String authToken;
//
//    @BeforeEach
//    void setup() throws Exception {
//        projectRepository.deleteAll();
//        userRepository.deleteAll();
//
//        // Register and login a user
//        RegisterRequest registerRequest = new RegisterRequest();
//        registerRequest.setEmail("project@test.com");
//        registerRequest.setPassword("password123");
//        registerRequest.setFullName("Project User");
//
//        MvcResult result = mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(registerRequest)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String response = result.getResponse().getContentAsString();
//        authToken = objectMapper.readTree(response).get("token").asText();
//    }
//
//    @Test
//    void shouldCreateProject() throws Exception {
//        ProjectRequest request = new ProjectRequest();
//        request.setName("Test Project");
//        request.setDescription("Test Description");
//
//        mockMvc.perform(post("/api/projects")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.name", is("Test Project")))
//                .andExpect(jsonPath("$.description", is("Test Description")))
//                .andExpect(jsonPath("$.ownerEmail", is("project@test.com")));
//    }
//
//    @Test
//    void shouldGetAllUserProjects() throws Exception {
//        // Create two projects
//        ProjectRequest project1 = new ProjectRequest();
//        project1.setName("Project 1");
//        project1.setDescription("Description 1");
//
//        ProjectRequest project2 = new ProjectRequest();
//        project2.setName("Project 2");
//        project2.setDescription("Description 2");
//
//        mockMvc.perform(post("/api/projects")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(project1)))
//                .andExpect(status().isCreated());
//
//        mockMvc.perform(post("/api/projects")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(project2)))
//                .andExpect(status().isCreated());
//
//        // Get all projects
//        mockMvc.perform(get("/api/projects")
//                        .header("Authorization", "Bearer " + authToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].name", is("Project 1")))
//                .andExpect(jsonPath("$[1].name", is("Project 2")));
//    }
//
//    @Test
//    void shouldGetProjectById() throws Exception {
//        // Create a project
//        ProjectRequest request = new ProjectRequest();
//        request.setName("Get Project");
//        request.setDescription("Get Description");
//
//        MvcResult createResult = mockMvc.perform(post("/api/projects")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        String createResponse = createResult.getResponse().getContentAsString();
//        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();
//
//        // Get project by ID
//        mockMvc.perform(get("/api/projects/" + projectId)
//                        .header("Authorization", "Bearer " + authToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(projectId.intValue())))
//                .andExpect(jsonPath("$.name", is("Get Project")));
//    }
//
//    @Test
//    void shouldUpdateProject() throws Exception {
//        // Create a project
//        ProjectRequest createRequest = new ProjectRequest();
//        createRequest.setName("Original Name");
//        createRequest.setDescription("Original Description");
//
//        MvcResult createResult = mockMvc.perform(post("/api/projects")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createRequest)))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        String createResponse = createResult.getResponse().getContentAsString();
//        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();
//
//        // Update project
//        ProjectRequest updateRequest = new ProjectRequest();
//        updateRequest.setName("Updated Name");
//        updateRequest.setDescription("Updated Description");
//
//        mockMvc.perform(put("/api/projects/" + projectId)
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name", is("Updated Name")))
//                .andExpect(jsonPath("$.description", is("Updated Description")));
//    }
//
//    @Test
//    void shouldDeleteProject() throws Exception {
//        // Create a project
//        ProjectRequest request = new ProjectRequest();
//        request.setName("Delete Project");
//        request.setDescription("To be deleted");
//
//        MvcResult createResult = mockMvc.perform(post("/api/projects")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        String createResponse = createResult.getResponse().getContentAsString();
//        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();
//
//        // Delete project
//        mockMvc.perform(delete("/api/projects/" + projectId)
//                        .header("Authorization", "Bearer " + authToken))
//                .andExpect(status().isNoContent());
//
//        // Verify deletion
//        mockMvc.perform(get("/api/projects/" + projectId)
//                        .header("Authorization", "Bearer " + authToken))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void shouldRequireAuthentication() throws Exception {
//        mockMvc.perform(get("/api/projects"))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void shouldValidateProjectName() throws Exception {
//        ProjectRequest request = new ProjectRequest();
//        request.setName("AB");  // Too short
//        request.setDescription("Description");
//
//        mockMvc.perform(post("/api/projects")
//                        .header("Authorization", "Bearer " + authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//}