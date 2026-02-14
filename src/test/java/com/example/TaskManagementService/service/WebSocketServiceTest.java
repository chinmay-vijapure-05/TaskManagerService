package com.example.TaskManagementService.service;

import com.example.TaskManagementService.dto.NotificationMessage;
import com.example.TaskManagementService.dto.WebSocketMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebSocketServiceTest {

    private SimpMessagingTemplate messagingTemplate;
    private WebSocketService webSocketService;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        webSocketService = new WebSocketService(messagingTemplate);
    }

    @Test
    void shouldSendProjectUpdate() {
        Long projectId = 1L;
        String type = "TASK";
        String action = "CREATE";
        String userId = "user@test.com";
        Object payload = "TestPayload";

        webSocketService.sendProjectUpdate(projectId, type, action, payload, userId);

        ArgumentCaptor<WebSocketMessage> captor = ArgumentCaptor.forClass(WebSocketMessage.class);

        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/project/1"), captor.capture());

        WebSocketMessage message = captor.getValue();

        assertEquals(type, message.getType());
        assertEquals(action, message.getAction());
        assertEquals(payload, message.getPayload());
        assertEquals(userId, message.getUserId());
        assertEquals(projectId, message.getProjectId());
    }

    @Test
    void shouldSendUserNotification() {
        String email = "user@test.com";

        NotificationMessage notification = new NotificationMessage();
        notification.setTitle("Test Title");
        notification.setMessage("Test Message");

        webSocketService.sendUserNotification(email, notification);

        verify(messagingTemplate, times(1))
                .convertAndSend("/queue/notifications/" + email, notification);
    }

    @Test
    void shouldSendTaskUpdateUsingProjectUpdate() {
        Long projectId = 2L;
        String action = "UPDATE";
        String userId = "system";
        Object payload = "TaskPayload";

        webSocketService.sendTaskUpdate(projectId, action, payload, userId);

        ArgumentCaptor<WebSocketMessage> captor = ArgumentCaptor.forClass(WebSocketMessage.class);

        verify(messagingTemplate).convertAndSend(eq("/topic/project/2"), captor.capture());

        WebSocketMessage message = captor.getValue();

        assertEquals("TASK", message.getType());
        assertEquals(action, message.getAction());
        assertEquals(payload, message.getPayload());
        assertEquals(userId, message.getUserId());
        assertEquals(projectId, message.getProjectId());
    }

    @Test
    void shouldSendProjectUpdateMessage() {
        Long projectId = 3L;
        String action = "DELETE";
        String userId = "admin";
        Object payload = "ProjectPayload";

        webSocketService.sendProjectUpdateMessage(projectId, action, payload, userId);

        ArgumentCaptor<WebSocketMessage> captor = ArgumentCaptor.forClass(WebSocketMessage.class);

        verify(messagingTemplate).convertAndSend(eq("/topic/project/3"), captor.capture());

        WebSocketMessage message = captor.getValue();

        assertEquals("PROJECT", message.getType());
        assertEquals(action, message.getAction());
        assertEquals(payload, message.getPayload());
        assertEquals(userId, message.getUserId());
        assertEquals(projectId, message.getProjectId());
    }

    @Test
    void shouldBroadcastMessage() {
        String type = "SYSTEM_ALERT";
        Object payload = "BroadcastPayload";

        webSocketService.broadcastMessage(type, payload);

        ArgumentCaptor<WebSocketMessage> captor = ArgumentCaptor.forClass(WebSocketMessage.class);

        verify(messagingTemplate).convertAndSend(eq("/topic/broadcast"), captor.capture());

        WebSocketMessage message = captor.getValue();

        assertEquals(type, message.getType());
        assertEquals("BROADCAST", message.getAction());
        assertEquals(payload, message.getPayload());
        assertEquals("SYSTEM", message.getUserId());
        assertNull(message.getProjectId());
    }
}
