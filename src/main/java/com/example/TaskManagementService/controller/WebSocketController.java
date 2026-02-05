package com.example.TaskManagementService.controller;

import com.example.TaskManagementService.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    @MessageMapping("/chat.send")
    @SendTo("/topic/broadcast")
    public NotificationMessage sendMessage(@Payload NotificationMessage message) {
        log.info("Broadcasting message from {}: {}", message.getUserId(), message.getMessage());
        return message;
    }

    @MessageMapping("/chat.join")
    @SendTo("/topic/broadcast")
    public NotificationMessage joinProject(@Payload NotificationMessage message,
                                           SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", message.getUserId());

        log.info("User {} joined", message.getUserId());
        return new NotificationMessage(
                "User Joined",
                message.getUserId() + " joined the project",
                "INFO",
                "SYSTEM"
        );
    }
}