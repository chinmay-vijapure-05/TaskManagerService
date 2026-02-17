package com.example.TaskManagementService.controller;

import com.example.TaskManagementService.service.ScheduledJobsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledJobsControllerTest {

    @Mock
    private ScheduledJobsService scheduledJobsService;

    @InjectMocks
    private ScheduledJobsController controller;

    @Test
    void shouldTriggerDeadlineCheck() {
        ResponseEntity<Map<String, String>> response = controller.triggerDeadlineCheck();

        verify(scheduledJobsService, times(1)).checkUpcomingDeadlines();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("completed", response.getBody().get("status"));
    }

    @Test
    void shouldTriggerOverdueCheck() {
        ResponseEntity<Map<String, String>> response = controller.triggerOverdueCheck();

        verify(scheduledJobsService, times(1)).checkOverdueTasks();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("completed", response.getBody().get("status"));
    }

    @Test
    void shouldTriggerDailyReport() {
        ResponseEntity<Map<String, String>> response = controller.triggerDailyReport();

        verify(scheduledJobsService, times(1)).generateDailyReport();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("completed", response.getBody().get("status"));
    }

    @Test
    void shouldTriggerCleanup() {
        ResponseEntity<Map<String, String>> response = controller.triggerCleanup();

        verify(scheduledJobsService, times(1)).cleanupOldCompletedTasks();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("completed", response.getBody().get("status"));
    }

    @Test
    void shouldReturnJobsStatus() {
        ResponseEntity<Map<String, Object>> response = controller.getJobsStatus();

        assertEquals(200, response.getStatusCodeValue());
        assertTrue((Boolean) response.getBody().get("schedulerEnabled"));
        assertNotNull(response.getBody().get("jobs"));
    }
}
