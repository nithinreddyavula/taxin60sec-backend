package com.taxin60sec.backend.dto.admin;

import java.math.BigDecimal;

public record RevenueSummaryDto(
        BigDecimal totalCollected,
        BigDecimal totalReleasedToCa,
        BigDecimal totalPlatformCommission,
        BigDecimal totalHeldInEscrow,
        BigDecimal totalRefunded
) {}