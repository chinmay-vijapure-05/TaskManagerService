package com.example.TaskManagementService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheController {
    private final CacheManager cacheManager;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                stats.put(cacheName, "active");
            }
        });

        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllCaches() {
        log.info("Clearing all caches");
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        return ResponseEntity.ok("All caches cleared successfully");
    }

    @DeleteMapping("/clear/{cacheName}")
    public ResponseEntity<String> clearCache(@PathVariable String cacheName) {
        log.info("Clearing cache: {}", cacheName);
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return ResponseEntity.ok("Cache '" + cacheName + "' cleared successfully");
        }
        return ResponseEntity.notFound().build();
    }
}