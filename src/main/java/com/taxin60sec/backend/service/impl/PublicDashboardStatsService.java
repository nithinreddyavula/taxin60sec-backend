package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.publicintake.PublicDashboardStatsResponse;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.entity.enums.ComplianceStatus;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.ComplianceObligationRepository;
import org.springframework.stereotype.Service;

@Service
public class PublicDashboardStatsService {

    private final CaseRepository cases;
    private final ComplianceObligationRepository obligations;

    public PublicDashboardStatsService(CaseRepository cases, ComplianceObligationRepository obligations) {
        this.cases = cases;
        this.obligations = obligations;
    }

    public PublicDashboardStatsResponse currentStats() {
        long totalClients = cases.countDistinctClients();
        long totalCasesCompleted = cases.countByStatus(CaseStatus.COMPLETED);

        long totalObligations = obligations.countByDeletedFalse();
        Double complianceRate = null;
        if (totalObligations > 0) {
            long overdue = obligations.countByStatusAndDeletedFalse(ComplianceStatus.OVERDUE);
            double rate = ((totalObligations - overdue) * 100.0) / totalObligations;
            complianceRate = Math.round(rate * 10.0) / 10.0;
        }

        return new PublicDashboardStatsResponse(totalClients, totalCasesCompleted, complianceRate);
    }
}