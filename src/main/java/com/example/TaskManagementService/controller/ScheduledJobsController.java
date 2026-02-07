package com.example.TaskManagementService.controller;

import com.example.TaskManagementService.service.ScheduledJobsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class ScheduledJobsController {
    private final ScheduledJobsService scheduledJobsService;

    @PostMapping("/trigger/deadlines")
    public ResponseEntity<Map<String, String>> triggerDeadlineCheck() {
        log.info("Manual trigger: Deadline check");
        scheduledJobsService.checkUpcomingDeadlines();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Deadline check triggered successfully");
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/trigger/overdue")
    public ResponseEntity<Map<String, String>> triggerOverdueCheck() {
        log.info("Manual trigger: Overdue check");
        scheduledJobsService.checkOverdueTasks();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Overdue check triggered successfully");
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/trigger/daily-report")
    public ResponseEntity<Map<String, String>> triggerDailyReport() {
        log.info("Manual trigger: Daily report");
        scheduledJobsService.generateDailyReport();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Daily report generated successfully");
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/trigger/cleanup")
    public ResponseEntity<Map<String, String>> triggerCleanup() {
        log.info("Manual trigger: Cleanup");
        scheduledJobsService.cleanupOldCompletedTasks();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Cleanup job triggered successfully");
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getJobsStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("schedulerEnabled", true);
        status.put("jobs", new String[]{
                "checkUpcomingDeadlines - Every hour",
                "checkOverdueTasks - Every 6 hours",
                "generateDailyReport - Daily at 8 AM",
                "cleanupOldCompletedTasks - Weekly on Sunday",
                "healthCheckPing - Every 5 minutes"
        });

        return ResponseEntity.ok(status);
    }
}