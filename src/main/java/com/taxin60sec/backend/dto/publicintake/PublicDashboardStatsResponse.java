package com.taxin60sec.backend.dto.publicintake;

public record PublicDashboardStatsResponse(
        long totalClients,
        long totalCasesCompleted,
        Double complianceRatePercentage
) {
}