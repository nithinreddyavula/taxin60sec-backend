package com.taxin60sec.backend.dto.publicintake;

public record ResponseTimeStatsResponse(
        long sampleSize,
        Long averageResponseSeconds,
        Double slaMetPercentage,
        int slaThresholdSeconds
) {
}