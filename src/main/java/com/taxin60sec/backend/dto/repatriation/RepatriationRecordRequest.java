package com.taxin60sec.backend.dto.repatriation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepatriationRecordRequest(
        BigDecimal amountUsd,
        LocalDate transactionDate,
        String purpose,
        boolean form15caFiled
) {}