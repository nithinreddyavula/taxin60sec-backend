package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.domain.CaDashboardResponse;
import com.taxin60sec.backend.dto.domain.CAProfileDto;
import com.taxin60sec.backend.dto.domain.PayoutLineItemDto;
import com.taxin60sec.backend.service.BusinessService;
import com.taxin60sec.backend.service.CAProfileService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class CaDashboardService {

    private final CAProfileService caProfileService;
    private final BusinessService businessService;
    private final PayoutService payoutService;

    public CaDashboardService(CAProfileService caProfileService, BusinessService businessService, PayoutService payoutService) {
        this.caProfileService = caProfileService;
        this.businessService = businessService;
        this.payoutService = payoutService;
    }

    public CaDashboardResponse forUser(Long userId) {
        CAProfileDto profile = caProfileService.myProfile(userId);
        Map<String, Long> caseCounts = businessService.statistics(userId);
        List<PayoutLineItemDto> payouts = payoutService.historyForCa(userId);

        BigDecimal totalEarnings = payouts.stream()
                .filter(p -> "RELEASED".equals(p.escrowStatus()) || "PARTIALLY_RELEASED".equals(p.escrowStatus()))
                .map(PayoutLineItemDto::caPayoutAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // caPayoutAmount accumulates only what's actually been released so far, so it isn't a
        // useful "pending" figure for a still-HELD payment. Pending is approximated as the full
        // held amount for cases with nothing released yet - a simplification, not exact accounting.
        BigDecimal pendingEarnings = payouts.stream()
                .filter(p -> "HELD".equals(p.escrowStatus()))
                .map(PayoutLineItemDto::amount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CaDashboardResponse(profile, caseCounts, totalEarnings, pendingEarnings);
    }
}