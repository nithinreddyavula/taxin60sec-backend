package com.taxin60sec.backend.dto.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentDto(
        Long id,
        String providerPaymentId,
        String provider,
        String referenceId,
        BigDecimal amount,
        String currency,
        String status,
        String caseNumber,
        Instant createdAt
) {}