package com.taxin60sec.backend.dto.admin;

import java.util.List;

public record AdminReportsOverviewResponse(
        AdminDashboardResponse caseFunnel,
        RevenueSummaryDto revenue,
        List<CaseVolumeByCategoryDto> volumeByCategory,
        Double averageTurnaroundDays,
        long totalClients,
        long totalVerifiedCAs
) {}