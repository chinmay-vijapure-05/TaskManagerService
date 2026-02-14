package com.example.TaskManagementService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cache Management", description = "Cache monitoring and administration APIs")
@SecurityRequirement(name = "BearerAuth")
public class CacheController {

    private final CacheManager cacheManager;

    @Operation(summary = "Get cache statistics",
            description = "Returns all active cache names currently managed by the application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cache statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
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

    @Operation(summary = "Clear all caches",
            description = "Clears all application caches managed by the CacheManager.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All caches cleared successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
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

    @Operation(summary = "Clear specific cache",
            description = "Clears a specific cache by name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cache cleared successfully"),
            @ApiResponse(responseCode = "404", description = "Cache not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
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
