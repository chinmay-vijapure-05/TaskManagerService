package com.example.TaskManagementService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendTaskReminderEmail(String to, String taskTitle, long hoursUntilDue) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(to);
            message.setSubject("⏰ Task Reminder");

            message.setText(
                    "Hello,\n\n" +
                            "Reminder: Your task \"" + taskTitle + "\" is due in " +
                            hoursUntilDue + " hours.\n\n" +
                            "Please complete it before the deadline.\n\n" +
                            "— TaskForge"
            );

            mailSender.send(message);

            log.info("Reminder email sent to {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}