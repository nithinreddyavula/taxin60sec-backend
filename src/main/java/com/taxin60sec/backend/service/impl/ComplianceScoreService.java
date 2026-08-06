package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.compliance.ComplianceScoreResponse;
import com.taxin60sec.backend.dto.compliance.ComplianceScoreResponse.CategoryStatusView;
import com.taxin60sec.backend.dto.compliance.ComplianceScoreResponse.ComplianceItemView;
import com.taxin60sec.backend.entity.ComplianceObligation;
import com.taxin60sec.backend.entity.ServiceOffering;
import com.taxin60sec.backend.entity.enums.ComplianceStatus;
import com.taxin60sec.backend.entity.enums.ComplianceType;
import com.taxin60sec.backend.repository.ComplianceObligationRepository;
import com.taxin60sec.backend.repository.ServiceOfferingRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ComplianceScoreService {

    private static final int OVERDUE_PENALTY = 15;
    private static final int LATE_COMPLETION_PENALTY = 5;

    private static final Map<ComplianceType, String> RECOMMENDED_SERVICE_CODE = Map.of(
            ComplianceType.GST_RETURN, "GST_FILING",
            ComplianceType.ITR_FILING, "INCOME_TAX",
            ComplianceType.ADVANCE_TAX, "INCOME_TAX",
            ComplianceType.ROC_FILING, "STARTUP_SERVICES",
            ComplianceType.TDS_RETURN, "VIRTUAL_CFO"
    );

    // Canonical categories for the Tax Health Report screen (V1.0 spec) - deliberately a
    // small fixed set, not one row per ComplianceType. Every obligation type maps to
    // exactly one of these; a category with no matching obligation still shows up, as
    // NOT_APPLICABLE, so the report always has the same shape regardless of the client.
    private static final Map<ComplianceType, String> CATEGORY_OF = Map.of(
            ComplianceType.ITR_FILING, "INCOME_TAX",
            ComplianceType.ADVANCE_TAX, "INCOME_TAX",
            ComplianceType.GST_RETURN, "GST",
            ComplianceType.ROC_FILING, "ROC",
            ComplianceType.TDS_RETURN, "PAYROLL",
            ComplianceType.OTHER, "OTHER"
    );

    private static final List<String> CATEGORY_ORDER = List.of("INCOME_TAX", "GST", "ROC", "PAYROLL");

    private final ComplianceObligationRepository obligationRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;

    public ComplianceScoreService(
            ComplianceObligationRepository obligationRepository,
            ServiceOfferingRepository serviceOfferingRepository
    ) {
        this.obligationRepository = obligationRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
    }

    @Transactional
    public ComplianceScoreResponse scoreFor(Long clientId) {

        List<ComplianceObligation> obligations =
                obligationRepository.findByClientIdAndDeletedFalseOrderByDueDateAsc(clientId);

        LocalDate today = LocalDate.now();

        // Self-heal: flip anything past its due date and still pending to OVERDUE.
        for (ComplianceObligation o : obligations) {
            if (o.getStatus() == ComplianceStatus.PENDING && o.getDueDate().isBefore(today)) {
                o.setStatus(ComplianceStatus.OVERDUE);
            }
        }

        int score = 100;

        for (ComplianceObligation o : obligations) {

            if (o.getStatus() == ComplianceStatus.OVERDUE) {
                score -= OVERDUE_PENALTY;
            } else if (o.getStatus() == ComplianceStatus.COMPLETED
                    && o.getCompletedAt() != null
                    && o.getCompletedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate().isAfter(o.getDueDate())) {
                score -= LATE_COMPLETION_PENALTY;
            }
        }

        score = Math.max(0, Math.min(100, score));

        String statusLabel;
        if (score >= 85) {
            statusLabel = "Healthy";
        } else if (score >= 60) {
            statusLabel = "Needs attention";
        } else {
            statusLabel = "At risk";
        }

        List<ComplianceItemView> items = obligations.stream()
                .map(o -> new ComplianceItemView(
                        o.getId(),
                        o.getType().name(),
                        o.getTitle(),
                        o.getDueDate(),
                        o.getStatus().name(),
                        recommendedServiceIdFor(o.getType())
                ))
                .toList();

        ComplianceItemView nextDue = obligations.stream()
                .filter(o -> o.getStatus() != ComplianceStatus.COMPLETED)
                .min(Comparator.comparing(ComplianceObligation::getDueDate))
                .map(o -> new ComplianceItemView(
                        o.getId(),
                        o.getType().name(),
                        o.getTitle(),
                        o.getDueDate(),
                        o.getStatus().name(),
                        recommendedServiceIdFor(o.getType())
                ))
                .orElse(null);

        List<CategoryStatusView> categories = CATEGORY_ORDER.stream()
                .map(category -> new CategoryStatusView(category, categoryStatus(obligations, category)))
                .toList();

        return new ComplianceScoreResponse(score, statusLabel, items, nextDue, categories);
    }

    /**
     * Worst-status-wins rollup for one category: any OVERDUE obligation makes the whole
     * category OVERDUE even if others in it are COMPLETED; otherwise any still-PENDING
     * obligation makes it PENDING; otherwise (all COMPLETED) it's COMPLETED; with no
     * matching obligation at all, it's NOT_APPLICABLE - the client has never needed this
     * category, which is itself useful information, not an absence of information.
     */
    private String categoryStatus(List<ComplianceObligation> obligations, String category) {
        List<ComplianceObligation> inCategory = obligations.stream()
                .filter(o -> category.equals(CATEGORY_OF.get(o.getType())))
                .toList();

        if (inCategory.isEmpty()) return "NOT_APPLICABLE";
        if (inCategory.stream().anyMatch(o -> o.getStatus() == ComplianceStatus.OVERDUE)) return "OVERDUE";
        if (inCategory.stream().anyMatch(o -> o.getStatus() == ComplianceStatus.PENDING)) return "PENDING";
        return "COMPLETED";
    }

    private Long recommendedServiceIdFor(ComplianceType type) {

        String code = RECOMMENDED_SERVICE_CODE.get(type);
        if (code == null) return null;

        return serviceOfferingRepository.findAll().stream()
                .filter(s -> code.equals(s.getCode()) && s.isActive())
                .findFirst()
                .map(ServiceOffering::getId)
                .orElse(null);
    }
}