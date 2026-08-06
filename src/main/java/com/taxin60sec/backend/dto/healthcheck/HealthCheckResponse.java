package com.taxin60sec.backend.dto.healthcheck;

import java.math.BigDecimal;
import java.util.List;

public record HealthCheckResponse(
        int score,
        String statusLabel,
        List<Issue> issues,
        List<Recommendation> recommendations,
        List<CategoryStatus> categories
) {
    public record Issue(String title, String severity) {}

    public record Recommendation(
            Long serviceId,
            String code,
            String displayName,
            BigDecimal priceFrom,
            Integer turnaroundDays
    ) {}

    /** status: HEALTHY, ATTENTION, CRITICAL, or NOT_APPLICABLE (category doesn't apply to this persona). */
    public record CategoryStatus(String category, String status, String detail) {}
}