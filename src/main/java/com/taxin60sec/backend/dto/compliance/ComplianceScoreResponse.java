package com.taxin60sec.backend.dto.compliance;

import java.time.LocalDate;
import java.util.List;

public record ComplianceScoreResponse(
        int score,
        String statusLabel,
        List<ComplianceItemView> items,
        ComplianceItemView nextDue,
        List<CategoryStatusView> categories
) {
    public record ComplianceItemView(
            Long id,
            String type,
            String title,
            LocalDate dueDate,
            String status,
            Long recommendedServiceId
    ) {}

    /**
     * The V1.0 spec's Tax Health Report screen: a small, fixed set of categories
     * (Income Tax / GST / ROC / Payroll), each ALWAYS present with a rolled-up status -
     * "NOT_APPLICABLE" when the client has no obligation of that kind at all, rather
     * than the category simply being absent from the list.
     */
    public record CategoryStatusView(String category, String status) {}
}