package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.ComplianceObligation;
import com.taxin60sec.backend.entity.ServiceOffering;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.ComplianceStatus;
import com.taxin60sec.backend.entity.enums.ComplianceType;
import com.taxin60sec.backend.entity.enums.ServiceCategory;
import com.taxin60sec.backend.repository.ComplianceObligationRepository;
import com.taxin60sec.backend.repository.ServiceOfferingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ComplianceObligationGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceObligationGeneratorService.class);

    private final ComplianceObligationRepository obligations;
    private final ServiceOfferingRepository serviceOfferings;

    public ComplianceObligationGeneratorService(ComplianceObligationRepository obligations, ServiceOfferingRepository serviceOfferings) {
        this.obligations = obligations;
        this.serviceOfferings = serviceOfferings;
    }

    public void generateForCase(Case taxCase) {
        if (taxCase.getClient() == null || taxCase.getServiceOffering() == null) return;
        generate(taxCase.getClient(), taxCase.getServiceOffering(), taxCase);
    }

    /**
     * Called when an anonymous Tax Health Check lead converts (signs up and creates a case). The quiz already
     * flagged a set of service codes as issues — seed a baseline obligation for each one that maps to a
     * trackable compliance type, so the client's dashboard score reflects exactly what the quiz found,
     * not just the one service they happened to purchase.
     */
    public void generateFromHealthCheckLead(User client, Case relatedCase, String triggeredServiceCodesCsv) {
        if (client == null || triggeredServiceCodesCsv == null || triggeredServiceCodesCsv.isBlank()) return;

        List<String> codes = Arrays.stream(triggeredServiceCodesCsv.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .toList();

        for (String code : codes) {
            serviceOfferings.findAll().stream()
                    .filter(offering -> code.equalsIgnoreCase(offering.getCode()))
                    .findFirst()
                    .ifPresent(offering -> generate(client, offering, relatedCase));
        }
    }

    private void generate(User client, ServiceOffering offering, Case relatedCase) {
        Optional<ComplianceType> resolved = resolveType(offering);
        if (resolved.isEmpty()) return;

        ComplianceType type = resolved.get();
        LocalDate dueDate = nextDueDate(type, LocalDate.now());

        boolean alreadyTracked = obligations.findByClientIdAndDeletedFalseOrderByDueDateAsc(client.getId()).stream()
                .anyMatch(o -> o.getType() == type && o.getDueDate().equals(dueDate));
        if (alreadyTracked) return;

        ComplianceObligation obligation = new ComplianceObligation();
        obligation.setClient(client);
        obligation.setType(type);
        obligation.setTitle(titleFor(type, offering));
        obligation.setDueDate(dueDate);
        obligation.setStatus(ComplianceStatus.PENDING);
        obligation.setRelatedCase(relatedCase);

        obligations.save(obligation);
        log.info("Auto-generated {} obligation for client {} due {}", type, client.getId(), dueDate);
    }

    private Optional<ComplianceType> resolveType(ServiceOffering offering) {
        String signal = ((offering.getCode() == null ? "" : offering.getCode())
                + " " + (offering.getDisplayName() == null ? "" : offering.getDisplayName()))
                .toLowerCase(Locale.ROOT);

        if (signal.contains("gst")) return Optional.of(ComplianceType.GST_RETURN);
        if (signal.contains("tds")) return Optional.of(ComplianceType.TDS_RETURN);
        if (signal.contains("roc") || signal.contains("annual filing") || signal.contains("aoc") || signal.contains("mgt")) {
            return Optional.of(ComplianceType.ROC_FILING);
        }
        if (signal.contains("advance tax")) return Optional.of(ComplianceType.ADVANCE_TAX);
        if (signal.contains("itr") || signal.contains("income tax return")) return Optional.of(ComplianceType.ITR_FILING);

        ServiceCategory category = offering.getCategory();
        if (category == null) return Optional.empty();
        return switch (category) {
            case GST -> Optional.of(ComplianceType.GST_RETURN);
            case INCOME_TAX -> Optional.of(ComplianceType.ITR_FILING);
            case STARTUP, COMPLIANCE -> Optional.of(ComplianceType.ROC_FILING);
            default -> Optional.empty();
        };
    }

    private String titleFor(ComplianceType type, ServiceOffering offering) {
        return switch (type) {
            case GST_RETURN -> "GSTR-3B filing";
            case TDS_RETURN -> "TDS return filing";
            case ROC_FILING -> "ROC annual filing";
            case ITR_FILING -> "Income Tax Return filing";
            case ADVANCE_TAX -> "Advance tax payment";
            case OTHER -> offering.getDisplayName() + " deadline";
        };
    }

    private LocalDate nextDueDate(ComplianceType type, LocalDate today) {
        return com.taxin60sec.backend.utils.DeadlineCalculator.nextDueDate(type, today);
    }
}