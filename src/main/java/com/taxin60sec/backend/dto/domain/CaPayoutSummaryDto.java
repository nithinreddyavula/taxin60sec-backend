package com.taxin60sec.backend.dto.domain;

import java.math.BigDecimal;

public record CaPayoutSummaryDto(
        Long caId,
        String caName,
        BigDecimal totalReleased,
        BigDecimal totalHeld,
        BigDecimal totalCommission,
        Long totalPayments
) {}