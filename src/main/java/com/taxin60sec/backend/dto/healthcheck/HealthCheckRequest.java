package com.taxin60sec.backend.dto.healthcheck;

import java.util.Map;

public record HealthCheckRequest(
        String userType,
        Map<String, Boolean> answers
) {}