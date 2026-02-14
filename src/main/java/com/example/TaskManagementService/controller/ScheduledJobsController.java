package com.example.TaskManagementService.controller;

import com.example.TaskManagementService.service.ScheduledJobsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Scheduled Jobs", description = "Manual triggers and monitoring for background scheduled jobs")
@SecurityRequirement(name = "BearerAuth")
public class ScheduledJobsController {

    private final ScheduledJobsService scheduledJobsService;

    @Operation(summary = "Trigger deadline check job",
            description = "Manually triggers the job that checks for upcoming task deadlines.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deadline check triggered successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    @PostMapping("/trigger/deadlines")
    public ResponseEntity<Map<String, String>> triggerDeadlineCheck() {
        log.info("Manual trigger: Deadline check");
        scheduledJobsService.checkUpcomingDeadlines();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Deadline check triggered successfully");
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Trigger overdue tasks check",
            description = "Manually triggers the job that identifies and updates overdue tasks.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overdue check triggered successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    @PostMapping("/trigger/overdue")
    public ResponseEntity<Map<String, String>> triggerOverdueCheck() {
        log.info("Manual trigger: Overdue check");
        scheduledJobsService.checkOverdueTasks();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Overdue check triggered successfully");
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Generate daily report",
            description = "Manually triggers the daily task summary report generation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Daily report generated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    @PostMapping("/trigger/daily-report")
    public ResponseEntity<Map<String, String>> triggerDailyReport() {
        log.info("Manual trigger: Daily report");
        scheduledJobsService.generateDailyReport();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Daily report generated successfully");
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Trigger cleanup job",
            description = "Manually triggers cleanup of old completed tasks.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cleanup job triggered successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    @PostMapping("/trigger/cleanup")
    public ResponseEntity<Map<String, String>> triggerCleanup() {
        log.info("Manual trigger: Cleanup");
        scheduledJobsService.cleanupOldCompletedTasks();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Cleanup job triggered successfully");
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get scheduler status",
            description = "Returns information about scheduled background jobs and their execution intervals.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scheduler status retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
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
