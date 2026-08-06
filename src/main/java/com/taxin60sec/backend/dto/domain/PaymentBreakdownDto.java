package com.taxin60sec.backend.dto.domain;

import java.math.BigDecimal;

public record PaymentBreakdownDto(
        BigDecimal totalAmount,
        BigDecimal expertFee,
        BigDecimal platformFee,
        BigDecimal gstAmount,
        String currency
) {}