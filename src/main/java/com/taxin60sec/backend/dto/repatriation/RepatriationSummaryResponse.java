package com.taxin60sec.backend.dto.repatriation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RepatriationSummaryResponse(
        String financialYear,
        BigDecimal limitUsd,
        BigDecimal usedUsd,
        BigDecimal remainingUsd,
        List<RepatriationRecordView> records,
        String disclaimer
) {
    public record RepatriationRecordView(
            Long id,
            BigDecimal amountUsd,
            LocalDate transactionDate,
            String purpose,
            boolean form15caFiled
    ) {}
}