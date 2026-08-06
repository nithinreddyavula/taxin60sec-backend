package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.entity.Payment;
import com.taxin60sec.backend.mapper.PaymentMapper;
import com.taxin60sec.backend.payment.EscrowService;
import com.taxin60sec.backend.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Client- and admin-facing endpoints for the escrow release flow (item #5). Kept as its
 * own controller since escrow release is a payment-domain action on a case, not a
 * case-workflow transition - it belongs next to EscrowService, not folded into the case
 * status/stage endpoints in BusinessControllers.java.
 */
@RestController
@RequestMapping("/api/v1")
public class EscrowController {

    private final EscrowService escrowService;
    private final PaymentMapper paymentMapper;

    public EscrowController(EscrowService escrowService, PaymentMapper paymentMapper) {
        this.escrowService = escrowService;
        this.paymentMapper = paymentMapper;
    }

    /** Client confirms the case is delivered to their satisfaction - releases held escrow to the CA's payout. */
    @PostMapping("/cases/{caseId}/escrow/release")
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<List<com.taxin60sec.backend.dto.domain.PaymentDto>> releaseAsClient(
            @PathVariable Long caseId,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request
    ) {
        List<Payment> released = escrowService.releaseForClientConfirmation(caseId, principal.getUser());
        return ApiResponse.success("Escrow released to CA payout", released.stream().map(paymentMapper::toDto).toList(), request.getRequestURI());
    }

    /** Admin override release - does not require the client to have confirmed first. */
    @PostMapping("/admin/cases/{caseId}/escrow/release")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<com.taxin60sec.backend.dto.domain.PaymentDto>> releaseAsAdmin(
            @PathVariable Long caseId,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request
    ) {
        List<Payment> released = escrowService.releaseByAdmin(caseId, principal.getUser());
        return ApiResponse.success("Escrow released by admin override", released.stream().map(paymentMapper::toDto).toList(), request.getRequestURI());
    }
}