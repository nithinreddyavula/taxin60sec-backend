package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.common.PageResponse;
import com.taxin60sec.backend.dto.domain.PaymentBreakdownDto;
import com.taxin60sec.backend.dto.domain.PaymentDto;
import com.taxin60sec.backend.payment.PaymentService;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.impl.PaymentHistoryService;
import com.taxin60sec.backend.service.impl.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;

@RestController @RequestMapping("/api/v1/payments") @RequiredArgsConstructor public class PaymentController {
    private final PaymentService payments;
    private final PricingService pricingService;
    private final PaymentHistoryService paymentHistory;
    private final com.taxin60sec.backend.service.BusinessService business;

    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')")
    public ApiResponse<PageResponse<PaymentDto>> forCase(
            @PathVariable Long caseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        BusinessApi.caseAccess(business, caseId, principal);
        return ApiResponse.success("Case payment history", paymentHistory.listForCase(caseId, page, size), null);
    }

    @GetMapping("/breakdown/{caseId}")
    @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')")
    public ApiResponse<PaymentBreakdownDto> breakdown(@PathVariable Long caseId) {
        return ApiResponse.success("Payment breakdown", pricingService.breakdownFor(caseId), null);
    }

    @PostMapping("/orders")
    public PaymentService.PaymentOrder order(@RequestBody PaymentService.PaymentOrderRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        PaymentService.PaymentOrder order = payments.createOrder(request);
        if (principal != null) {
            paymentHistory.record(principal.getUser(), null, order);
        }
        return order;
    }

    @PostMapping("/orders/for-case/{caseId}")
    public PaymentService.PaymentOrder orderForCase(@PathVariable Long caseId, @AuthenticationPrincipal UserPrincipal principal) {

        BigDecimal amount = pricingService.resolveAmount(caseId);

        PaymentService.PaymentOrder order = payments.createOrder(new PaymentService.PaymentOrderRequest(
                amount,
                "INR",
                "CASE-" + caseId,
                null,
                new HashMap<>()
        ));

        if (principal != null) {
            paymentHistory.record(principal.getUser(), caseId, order);
        }

        return order;
    }

    @PostMapping("/webhooks/{provider}")
    public PaymentService.PaymentEvent webhook(@PathVariable String provider, @RequestBody String payload, @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature, @RequestHeader java.util.Map<String, String> headers) {
        PaymentService.PaymentEvent event = payments.verifyWebhook(new PaymentService.PaymentWebhook(provider, payload, signature, headers));
        paymentHistory.applyWebhookEvent(event);
        return event;
    }

    @PostMapping("/{paymentId}/refunds")
    public PaymentService.Refund refund(@PathVariable String paymentId, @RequestBody PaymentService.RefundRequest request) {
        PaymentService.Refund refund = payments.refund(new PaymentService.RefundRequest(paymentId, request.amount(), request.reason()));
        paymentHistory.applyRefund(refund);
        return refund;
    }

    @GetMapping("/{paymentId}/invoice")
    public PaymentService.Invoice invoice(@PathVariable String paymentId) {
        return payments.invoice(paymentId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')")
    public ApiResponse<PageResponse<PaymentDto>> myPayments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("Payment history", paymentHistory.listForClient(principal.getId(), page, size), null);
    }
}