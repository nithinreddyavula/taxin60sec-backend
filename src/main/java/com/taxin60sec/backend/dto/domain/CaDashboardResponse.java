package com.taxin60sec.backend.dto.domain;

import java.math.BigDecimal;
import java.util.Map;

public record CaDashboardResponse(
        CAProfileDto profile,
        Map<String, Long> caseCounts,
        BigDecimal totalEarnings,
        BigDecimal pendingEarnings
) {}