package com.example.TaskManagementService.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheControllerTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private CacheController controller;

    @Test
    void shouldReturnCacheStats() {
        when(cacheManager.getCacheNames()).thenReturn(List.of("tasks", "projects"));
        when(cacheManager.getCache("tasks")).thenReturn(cache);
        when(cacheManager.getCache("projects")).thenReturn(cache);

        ResponseEntity<Map<String, Object>> response = controller.getCacheStats();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("active", response.getBody().get("tasks"));
        assertEquals("active", response.getBody().get("projects"));
    }

    @Test
    void shouldClearAllCaches() {
        when(cacheManager.getCacheNames()).thenReturn(List.of("tasks"));
        when(cacheManager.getCache("tasks")).thenReturn(cache);

        ResponseEntity<String> response = controller.clearAllCaches();

        verify(cache, times(1)).clear();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("All caches cleared successfully", response.getBody());
    }

    @Test
    void shouldClearSpecificCache() {
        when(cacheManager.getCache("tasks")).thenReturn(cache);

        ResponseEntity<String> response = controller.clearCache("tasks");

        verify(cache, times(1)).clear();
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("tasks"));
    }

    @Test
    void shouldReturn404WhenCacheNotFound() {
        when(cacheManager.getCache("unknown")).thenReturn(null);

        ResponseEntity<String> response = controller.clearCache("unknown");

        assertEquals(404, response.getStatusCodeValue());
    }
}
