package com.taxin60sec.backend.dto.healthcheck;

import java.util.List;

public record HealthCheckLeadRequest(
        Long leadId,
        String email,
        String phoneNumber,
        String userType,
        int score,
        String statusLabel,
        String issuesSummary,
        List<String> triggeredCodes
) {}