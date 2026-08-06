package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.admin.AdminReportsOverviewResponse;
import com.taxin60sec.backend.dto.admin.RevenueSummaryDto;
import com.taxin60sec.backend.repository.CAProfileRepository;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.PaymentRepository;
import com.taxin60sec.backend.service.AdminCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminReportsService {

    private final AdminCaseService adminCaseService;
    private final PaymentRepository payments;
    private final CaseRepository cases;
    private final CAProfileRepository caProfiles;

    public AdminReportsService(AdminCaseService adminCaseService, PaymentRepository payments, CaseRepository cases, CAProfileRepository caProfiles) {
        this.adminCaseService = adminCaseService;
        this.payments = payments;
        this.cases = cases;
        this.caProfiles = caProfiles;
    }

    public AdminReportsOverviewResponse overview() {
        List<Object[]> row = payments.revenueTotals();
        RevenueSummaryDto revenue;
        if (row.isEmpty()) {
            revenue = new RevenueSummaryDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        } else {
            Object[] r = row.get(0);
            revenue = new RevenueSummaryDto((BigDecimal) r[0], (BigDecimal) r[1], (BigDecimal) r[2], (BigDecimal) r[3], (BigDecimal) r[4]);
        }

        return new AdminReportsOverviewResponse(
                adminCaseService.dashboard(),
                revenue,
                cases.volumeByCategory(),
                cases.averageTurnaroundDays(),
                cases.countDistinctClients(),
                caProfiles.findByVerifiedTrueOrderByCreatedAtAsc().size()
        );
    }
}