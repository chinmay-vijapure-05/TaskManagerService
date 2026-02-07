package com.example.TaskManagementService.service;

import com.example.TaskManagementService.dto.NotificationMessage;
import com.example.TaskManagementService.dto.TaskReminderDto;
import com.example.TaskManagementService.entity.Task;
import com.example.TaskManagementService.entity.TaskStatus;
import com.example.TaskManagementService.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledJobsService {
    private final TaskRepository taskRepository;
    private final WebSocketService webSocketService;

    /**
     * Check for upcoming task deadlines every hour
     * Send reminders for tasks due within 24 hours
     */
    @Scheduled(cron = "0 0 * * * *")  // Every hour at minute 0
    public void checkUpcomingDeadlines() {
        log.info("Running scheduled job: Check upcoming deadlines");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusHours(24);

        List<Task> allTasks = taskRepository.findAll();

        List<TaskReminderDto> upcomingTasks = allTasks.stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> task.getAssignee() != null)
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .filter(task -> task.getStatus() != TaskStatus.CANCELLED)
                .filter(task -> task.getDueDate().isAfter(now) && task.getDueDate().isBefore(tomorrow))
                .map(task -> {
                    long hoursUntilDue = ChronoUnit.HOURS.between(now, task.getDueDate());
                    return new TaskReminderDto(
                            task.getId(),
                            task.getTitle(),
                            task.getAssignee().getEmail(),
                            task.getAssignee().getFullName(),
                            task.getDueDate(),
                            task.getPriority().toString(),
                            hoursUntilDue
                    );
                })
                .collect(Collectors.toList());

        log.info("Found {} tasks due within 24 hours", upcomingTasks.size());

        upcomingTasks.forEach(this::sendTaskReminder);
    }

    /**
     * Check for overdue tasks every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * *")  // Every 6 hours
    public void checkOverdueTasks() {
        log.info("Running scheduled job: Check overdue tasks");

        LocalDateTime now = LocalDateTime.now();

        List<Task> allTasks = taskRepository.findAll();

        List<Task> overdueTasks = allTasks.stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> task.getAssignee() != null)
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .filter(task -> task.getStatus() != TaskStatus.CANCELLED)
                .filter(task -> task.getDueDate().isBefore(now))
                .collect(Collectors.toList());

        log.info("Found {} overdue tasks", overdueTasks.size());

        overdueTasks.forEach(task -> {
            long hoursOverdue = ChronoUnit.HOURS.between(task.getDueDate(), now);

            NotificationMessage notification = new NotificationMessage(
                    "Task Overdue!",
                    String.format("Task '%s' is overdue by %d hours. Priority: %s",
                            task.getTitle(), hoursOverdue, task.getPriority()),
                    "ERROR",
                    task.getAssignee().getEmail()
            );

            webSocketService.sendUserNotification(task.getAssignee().getEmail(), notification);
            log.info("Sent overdue notification for task {} to {}", task.getId(), task.getAssignee().getEmail());
        });
    }

    /**
     * Daily report generation at 8 AM
     */
    @Scheduled(cron = "0 0 8 * * *")  // Every day at 8 AM
    public void generateDailyReport() {
        log.info("Running scheduled job: Generate daily report");

        List<Task> allTasks = taskRepository.findAll();

        long totalTasks = allTasks.size();
        long completedTasks = allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();
        long pendingTasks = allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.TODO ||
                        task.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
        long overdueTasks = allTasks.stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> task.getDueDate().isBefore(LocalDateTime.now()))
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .count();

        String report = String.format(
                "📊 Daily Task Report\n\n" +
                        "Total Tasks: %d\n" +
                        "✅ Completed: %d (%.1f%%)\n" +
                        "⏳ Pending: %d\n" +
                        "🚨 Overdue: %d",
                totalTasks,
                completedTasks,
                totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0,
                pendingTasks,
                overdueTasks
        );

        log.info("Daily report generated:\n{}", report);

        // Broadcast daily report to all users
        webSocketService.broadcastMessage("DAILY_REPORT", report);
    }

    /**
     * Cleanup old completed tasks every week (Sunday at midnight)
     */
    @Scheduled(cron = "0 0 0 * * SUN")  // Every Sunday at midnight
    public void cleanupOldCompletedTasks() {
        log.info("Running scheduled job: Cleanup old completed tasks");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<Task> allTasks = taskRepository.findAll();

        List<Task> oldCompletedTasks = allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .filter(task -> task.getUpdatedAt().isBefore(thirtyDaysAgo))
                .collect(Collectors.toList());

        log.info("Found {} completed tasks older than 30 days", oldCompletedTasks.size());

        // Instead of deleting, we could archive them or just log
        // For safety, we'll just log for now
        oldCompletedTasks.forEach(task -> {
            log.debug("Task {} '{}' completed on {} - eligible for archival",
                    task.getId(), task.getTitle(), task.getUpdatedAt());
        });

        log.info("Cleanup job completed. {} tasks eligible for archival", oldCompletedTasks.size());
    }

    /**
     * Health check ping every 5 minutes (useful for monitoring)
     */
    @Scheduled(fixedRate = 300000)  // Every 5 minutes (300000 ms)
    public void healthCheckPing() {
        log.debug("Scheduler health check - Application is running");
    }

    /**
     * Send task reminder notification
     */
    @Async
    private void sendTaskReminder(TaskReminderDto reminder) {
        NotificationMessage notification = new NotificationMessage(
                "⏰ Task Reminder",
                String.format("Task '%s' is due in %d hours! Priority: %s",
                        reminder.getTaskTitle(),
                        reminder.getHoursUntilDue(),
                        reminder.getPriority()),
                "WARNING",
                reminder.getAssigneeEmail()
        );

        webSocketService.sendUserNotification(reminder.getAssigneeEmail(), notification);
        log.info("Sent reminder for task {} to {}", reminder.getTaskId(), reminder.getAssigneeEmail());
    }
}