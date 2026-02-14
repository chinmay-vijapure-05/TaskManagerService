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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private ProjectService projectService;

    private User testUser;
    private User otherUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        // Setup test user (owner)
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("owner@test.com");
        testUser.setFullName("Project Owner");

        // Setup other user
        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@test.com");
        otherUser.setFullName("Other User");

        // Setup test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setOwner(testUser);
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setCreatedAt(LocalDateTime.now());
        testProject.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== CREATE PROJECT TESTS ====================

    @Test
    void shouldCreateProjectSuccessfully() {
        // Given
        ProjectRequest request = new ProjectRequest();
        request.setName("New Project");
        request.setDescription("New Description");

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // When
        ProjectResponse response = projectService.createProject(request, "owner@test.com");

        // Then
        assertNotNull(response);
        assertEquals("Test Project", response.getName());
        assertEquals("Test Description", response.getDescription());
        assertEquals("owner@test.com", response.getOwnerEmail());

        verify(projectRepository, times(1)).save(any(Project.class));
        verify(webSocketService, times(1)).sendProjectUpdateMessage(
                eq(1L),
                eq("CREATE"),
                any(ProjectResponse.class),
                eq("owner@test.com")
        );
    }

    @Test
    void shouldCreateProjectWithMembers() {
        // Given
        ProjectRequest request = new ProjectRequest();
        request.setName("Team Project");
        request.setDescription("Project with team");
        request.setMemberIds(Arrays.asList(2L, 3L));

        User member1 = new User();
        member1.setId(2L);
        member1.setEmail("member1@test.com");
        member1.setFullName("Member One");

        User member2 = new User();
        member2.setId(3L);
        member2.setEmail("member2@test.com");
        member2.setFullName("Member Two");

        List<User> members = Arrays.asList(member1, member2);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findAllById(Arrays.asList(2L, 3L))).thenReturn(members);
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // When
        projectService.createProject(request, "owner@test.com");

        // Then
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());

        Project savedProject = projectCaptor.getValue();
        assertNotNull(savedProject.getMembers());
        assertEquals(2, savedProject.getMembers().size());
    }

    @Test
    void shouldSetProjectOwnerCorrectly() {
        // Given
        ProjectRequest request = new ProjectRequest();
        request.setName("Owner Test Project");
        request.setDescription("Testing ownership");

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // When
        projectService.createProject(request, "owner@test.com");

        // Then
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());

        Project savedProject = projectCaptor.getValue();
        assertEquals(testUser, savedProject.getOwner());
        assertEquals("owner@test.com", savedProject.getOwner().getEmail());
    }

    @Test
    void shouldThrowExceptionWhenOwnerNotFoundDuringCreation() {
        // Given
        ProjectRequest request = new ProjectRequest();
        request.setName("Project");
        request.setDescription("Description");

        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.createProject(request, "nonexistent@test.com");
        });

        verify(projectRepository, never()).save(any());
        verify(webSocketService, never()).sendProjectUpdateMessage(anyLong(), anyString(), any(), anyString());
    }

    @Test
    void shouldSendWebSocketNotificationAfterCreation() {
        // Given
        ProjectRequest request = new ProjectRequest();
        request.setName("WebSocket Project");

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // When
        projectService.createProject(request, "owner@test.com");

        // Then
        verify(webSocketService, times(1)).sendProjectUpdateMessage(
                eq(1L),
                eq("CREATE"),
                any(ProjectResponse.class),
                eq("owner@test.com")
        );
    }

    // ==================== GET PROJECT TESTS ====================

    @Test
    void shouldGetUserProjects() {
        // Given
        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("Project 2");
        project2.setOwner(testUser);

        List<Project> projects = Arrays.asList(testProject, project2);
        Page<Project> projectPage = new PageImpl<>(projects);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));
        when(projectRepository.findAllUserProjects(eq(1L), any(Pageable.class))).thenReturn(projectPage);

        // When
        List<ProjectResponse> responses = projectService.getUserProjects("owner@test.com");

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(projectRepository, times(1)).findAllUserProjects(eq(1L), any(Pageable.class));
    }

    @Test
    void shouldGetProjectById() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));

        // When
        ProjectResponse response = projectService.getProjectById(1L, "owner@test.com");

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Project", response.getName());
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProjectNotFound() {
        // Given
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectById(999L, "owner@test.com");
        });
    }

    @Test
    void shouldThrowExceptionWhenUserLacksAccessToProject() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("unauthorized@test.com")).thenReturn(Optional.of(otherUser));

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            projectService.getProjectById(1L, "unauthorized@test.com");
        });
    }

    @Test
    void shouldAllowAccessToProjectOwner() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));

        // When
        ProjectResponse response = projectService.getProjectById(1L, "owner@test.com");

        // Then
        assertNotNull(response);
        assertEquals("owner@test.com", response.getOwnerEmail());
    }

    @Test
    void shouldAllowAccessToProjectMember() {
        // Given
        testProject.getMembers().add(otherUser);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));

        // When
        ProjectResponse response = projectService.getProjectById(1L, "other@test.com");

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void shouldGetProjectsPaginated() {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        Page<Project> projectPage = new PageImpl<>(projects);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));
        when(projectRepository.searchUserProjects(
                eq(1L),
                any(),
                anyString(),
                any(Pageable.class)
        )).thenReturn(projectPage);

        // When
        PagedResponse<ProjectResponse> response = projectService.getUserProjectsPaginated(
                "owner@test.com",
                ProjectStatus.ACTIVE,
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

    // ==================== UPDATE PROJECT TESTS ====================

    @Test
    void shouldUpdateProjectSuccessfully() {
        // Given
        ProjectRequest updateRequest = new ProjectRequest();
        updateRequest.setName("Updated Project");
        updateRequest.setDescription("Updated Description");

        Project updatedProject = new Project();
        updatedProject.setId(1L);
        updatedProject.setName("Updated Project");
        updatedProject.setDescription("Updated Description");
        updatedProject.setOwner(testUser);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);

        // When
        ProjectResponse response = projectService.updateProject(1L, updateRequest, "owner@test.com");

        // Then
        assertNotNull(response);
        assertEquals("Updated Project", response.getName());
        assertEquals("Updated Description", response.getDescription());

        verify(projectRepository, times(1)).save(any(Project.class));
        verify(webSocketService, times(1)).sendProjectUpdateMessage(
                eq(1L),
                eq("UPDATE"),
                any(ProjectResponse.class),
                eq("owner@test.com")
        );
    }

    @Test
    void shouldUpdateProjectMembers() {
        // Given
        ProjectRequest updateRequest = new ProjectRequest();
        updateRequest.setName("Project");
        updateRequest.setDescription("Description");
        updateRequest.setMemberIds(Arrays.asList(2L, 3L));

        User member1 = new User();
        member1.setId(2L);
        List<User> members = Arrays.asList(member1);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findAllById(anyList())).thenReturn(members);
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // When
        projectService.updateProject(1L, updateRequest, "owner@test.com");

        // Then
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());

        Project savedProject = projectCaptor.getValue();
        assertNotNull(savedProject.getMembers());
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerTriesToUpdate() {
        // Given
        ProjectRequest updateRequest = new ProjectRequest();
        updateRequest.setName("Unauthorized Update");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            projectService.updateProject(1L, updateRequest, "other@test.com");
        });

        verify(projectRepository, never()).save(any());
        verify(webSocketService, never()).sendProjectUpdateMessage(anyLong(), anyString(), any(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProject() {
        // Given
        ProjectRequest updateRequest = new ProjectRequest();
        updateRequest.setName("Update");

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.updateProject(999L, updateRequest, "owner@test.com");
        });

        verify(projectRepository, never()).save(any());
    }

    // ==================== DELETE PROJECT TESTS ====================

    @Test
    void shouldDeleteProjectSuccessfully() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));
        doNothing().when(projectRepository).delete(testProject);

        // When
        projectService.deleteProject(1L, "owner@test.com");

        // Then
        verify(projectRepository, times(1)).delete(testProject);
        verify(webSocketService, times(1)).sendProjectUpdateMessage(
                eq(1L),
                eq("DELETE"),
                any(ProjectResponse.class),
                eq("owner@test.com")
        );
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerTriesToDelete() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            projectService.deleteProject(1L, "other@test.com");
        });

        verify(projectRepository, never()).delete(any());
        verify(webSocketService, never()).sendProjectUpdateMessage(anyLong(), anyString(), any(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentProject() {
        // Given
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.deleteProject(999L, "owner@test.com");
        });

        verify(projectRepository, never()).delete(any());
    }

    @Test
    void shouldSendWebSocketNotificationBeforeDeleting() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));
        doNothing().when(projectRepository).delete(testProject);

        // When
        projectService.deleteProject(1L, "owner@test.com");

        // Then
        verify(webSocketService, times(1)).sendProjectUpdateMessage(
                eq(1L),
                eq("DELETE"),
                any(ProjectResponse.class),
                eq("owner@test.com")
        );
        verify(projectRepository, times(1)).delete(testProject);
    }

    // ==================== EDGE CASES & BUSINESS LOGIC TESTS ====================

    @Test
    void shouldMapProjectToResponseCorrectly() {
        // Given
        testProject.getMembers().add(otherUser);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));

        // When
        ProjectResponse response = projectService.getProjectById(1L, "owner@test.com");

        // Then
        assertEquals(testProject.getId(), response.getId());
        assertEquals(testProject.getName(), response.getName());
        assertEquals(testProject.getDescription(), response.getDescription());
        assertEquals(testProject.getOwner().getEmail(), response.getOwnerEmail());
        assertEquals(testProject.getOwner().getFullName(), response.getOwnerName());
        assertEquals(testProject.getStatus(), response.getStatus());
        assertEquals(1, response.getMembers().size());
    }

    @Test
    void shouldHandleProjectWithNoMembers() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));

        // When
        ProjectResponse response = projectService.getProjectById(1L, "owner@test.com");

        // Then
        assertNotNull(response);
        assertNotNull(response.getMembers());
        assertEquals(0, response.getMembers().size());
    }

    @Test
    void shouldHandleProjectWithNoTasks() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));

        // When
        ProjectResponse response = projectService.getProjectById(1L, "owner@test.com");

        // Then
        assertNotNull(response);
        assertEquals(0, response.getTaskCount());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringGet() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectById(1L, "nonexistent@test.com");
        });
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoProjects() {
        // Given
        Page<Project> emptyPage = new PageImpl<>(Arrays.asList());

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testUser));
        when(projectRepository.findAllUserProjects(eq(1L), any(Pageable.class))).thenReturn(emptyPage);

        // When
        List<ProjectResponse> responses = projectService.getUserProjects("owner@test.com");

        // Then
        assertNotNull(responses);
        assertEquals(0, responses.size());
    }
}