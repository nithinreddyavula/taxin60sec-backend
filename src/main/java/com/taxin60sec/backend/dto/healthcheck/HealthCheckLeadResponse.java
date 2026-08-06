package com.taxin60sec.backend.dto.healthcheck;

import java.time.Instant;

public record HealthCheckLeadResponse(
        Long id,
        String email,
        String phoneNumber,
        String userType,
        int score,
        String statusLabel,
        boolean converted,
        Instant createdAt
) {}