package com.taxin60sec.backend.dto.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record PayoutLineItemDto(
        Long id,
        String caseNumber,
        BigDecimal amount,
        String escrowStatus,
        BigDecimal platformCommissionAmount,
        BigDecimal caPayoutAmount,
        Instant escrowReleasedAt,
        String escrowReleasedBy
) {}