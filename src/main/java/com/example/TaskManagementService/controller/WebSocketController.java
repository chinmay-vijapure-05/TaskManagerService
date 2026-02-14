package com.example.TaskManagementService.controller;

import com.example.TaskManagementService.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    /**
     * Handles broadcast chat messages.
     *
     * Client sends to: /app/chat.send
     * Broadcasts to:   /topic/broadcast
     */
    @MessageMapping("/chat.send")
    @SendTo("/topic/broadcast")
    public NotificationMessage sendMessage(@Payload NotificationMessage message) {

        if (message == null || message.getUserId() == null) {
            log.warn("Received invalid WebSocket message");
            return null;
        }

        log.info("Broadcasting message from {}: {}",
                message.getUserId(),
                message.getMessage());

        return message;
    }

    /**
     * Handles user join events.
     *
     * Client sends to: /app/chat.join
     * Broadcasts to:   /topic/broadcast
     */
    @MessageMapping("/chat.join")
    @SendTo("/topic/broadcast")
    public NotificationMessage joinProject(@Payload NotificationMessage message,
                                           SimpMessageHeaderAccessor headerAccessor) {

        if (message == null || message.getUserId() == null) {
            log.warn("Invalid join request received");
            return null;
        }

        // Store username in WebSocket session
        Objects.requireNonNull(headerAccessor.getSessionAttributes())
                .put("username", message.getUserId());

        log.info("User {} joined the WebSocket session", message.getUserId());

        return new NotificationMessage(
                "User Joined",
                message.getUserId() + " joined the project",
                "INFO",
                "SYSTEM"
        );
    }
}
