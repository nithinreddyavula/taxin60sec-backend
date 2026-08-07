package com.taxin60sec.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Public, no-DB-touch endpoint used by the cron-job.org keep-alive ping to stop
 * Render's free tier from sleeping the instance after 15 minutes of no traffic.
 * Kept separate from /actuator/health, which also checks DB connectivity and would
 * needlessly wake the Neon database on every ping.
 */
@RestController
public class HealthController {

    @GetMapping("/api/v1/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
        ));
    }
}