package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.domain.CaPayoutSummaryDto;
import com.taxin60sec.backend.dto.domain.PayoutLineItemDto;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.impl.PayoutService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Commission & payout dashboard endpoints (item #7): platform-wide visibility for admin,
 * and transparency for each CA into their own earnings - two different audiences reading
 * the same underlying PayoutService.
 */
@RestController
public class PayoutController {

    private final PayoutService payoutService;

    public PayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    /** Admin: every CA's running totals - released, held/pending, and platform commission taken. */
    @GetMapping("/api/v1/admin/payouts")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CaPayoutSummaryDto>> platformSummary(HttpServletRequest request) {
        return ApiResponse.success("Payout summary", payoutService.platformSummary(), request.getRequestURI());
    }

    /** Admin: drill into one CA's individual payment/payout line items. */
    @GetMapping("/api/v1/admin/payouts/{caUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PayoutLineItemDto>> forCa(@PathVariable Long caUserId, HttpServletRequest request) {
        return ApiResponse.success("Payout history", payoutService.historyForCa(caUserId), request.getRequestURI());
    }

    /** CA: their own earnings/payout history - same data shape as the admin drill-down, scoped to the logged-in CA. */
    @GetMapping("/api/v1/ca/me/payouts")
    @PreAuthorize("hasRole('CA')")
    public ApiResponse<List<PayoutLineItemDto>> myPayouts(@AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request) {
        return ApiResponse.success("My payouts", payoutService.historyForCa(principal.getId()), request.getRequestURI());
    }
}