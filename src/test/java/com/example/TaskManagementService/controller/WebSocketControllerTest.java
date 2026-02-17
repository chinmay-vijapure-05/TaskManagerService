package com.example.TaskManagementService.controller;

import com.example.TaskManagementService.dto.NotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketControllerTest {

    private WebSocketController controller;

    @BeforeEach
    void setUp() {
        controller = new WebSocketController();
    }

    // ==========================
    // sendMessage()
    // ==========================

    @Test
    void shouldReturnMessageWhenValid() {

        NotificationMessage message =
                new NotificationMessage("Test", "Hello", "INFO", "user1");

        NotificationMessage result = controller.sendMessage(message);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user1");
        assertThat(result.getMessage()).isEqualTo("Hello");
    }

    @Test
    void shouldReturnNullWhenMessageIsNull() {

        NotificationMessage result = controller.sendMessage(null);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenUserIdIsNull() {

        NotificationMessage message =
                new NotificationMessage("Test", "Hello", "INFO", null);

        NotificationMessage result = controller.sendMessage(message);

        assertThat(result).isNull();
    }

    // ==========================
    // joinProject()
    // ==========================

    @Test
    void shouldReturnJoinNotificationWhenValid() {

        NotificationMessage message =
                new NotificationMessage("Test", "Join", "INFO", "user1");

        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.create();
        accessor.setSessionAttributes(new HashMap<>());

        NotificationMessage result =
                controller.joinProject(message, accessor);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("User Joined");
        assertThat(result.getUserId()).isEqualTo("SYSTEM");
        assertThat(accessor.getSessionAttributes().get("username"))
                .isEqualTo("user1");
    }

    @Test
    void shouldReturnNullWhenJoinMessageIsInvalid() {

        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.create();
        accessor.setSessionAttributes(new HashMap<>());

        NotificationMessage result =
                controller.joinProject(null, accessor);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenJoinUserIdIsNull() {

        NotificationMessage message =
                new NotificationMessage("Test", "Join", "INFO", null);

        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.create();
        accessor.setSessionAttributes(new HashMap<>());

        NotificationMessage result =
                controller.joinProject(message, accessor);

        assertThat(result).isNull();
    }
}
