package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.PageResponse;
import com.taxin60sec.backend.dto.domain.PaymentDto;
import com.taxin60sec.backend.entity.Payment;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.PaymentStatus;
import com.taxin60sec.backend.mapper.PaymentMapper;
import com.taxin60sec.backend.payment.PaymentService;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.PaymentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists a local record of every payment order/event so clients can see payment history.
 * PaymentServiceImpl and the payment providers stay provider-agnostic and untouched - this
 * service is called explicitly from PaymentController alongside the existing calls.
 */
@Service
@Transactional
public class PaymentHistoryService {

    private final PaymentRepository payments;
    private final CaseRepository cases;
    private final PaymentMapper mapper;

    public PaymentHistoryService(PaymentRepository payments, CaseRepository cases, PaymentMapper mapper) {
        this.payments = payments;
        this.cases = cases;
        this.mapper = mapper;
    }

    /** Called the moment an order is created with the provider, so it shows up in "My Payments" even before any webhook arrives. */
    public void record(User client, Long caseId, PaymentService.PaymentOrder order) {
        Payment p = new Payment();
        p.setClient(client);
        p.setProvider(order.provider());
        p.setProviderPaymentId(order.id());
        p.setReferenceId(order.referenceId());
        p.setAmount(order.amount());
        p.setCurrency(order.currency());
        p.setStatus(statusFrom(order.status()));
        if (caseId != null) {
            cases.findById(caseId).ifPresent(p::setRelatedCase);
        }
        payments.save(p);
    }

    /** Webhooks are the source of truth for final payment status - update the matching local row if we have one. */
    public void applyWebhookEvent(PaymentService.PaymentEvent event) {
        if (event == null || event.paymentId() == null) return;
        payments.findByProviderPaymentId(event.paymentId()).ifPresent(p -> {
            String type = event.type() == null ? "" : event.type().toLowerCase();
            if (type.contains("fail")) {
                p.setStatus(PaymentStatus.FAILED);
            } else if (type.contains("refund")) {
                p.setStatus(PaymentStatus.REFUNDED);
                p.setEscrowStatus(com.taxin60sec.backend.entity.enums.EscrowStatus.REFUNDED);
            } else if (type.contains("captur") || type.contains("paid") || type.contains("success")) {
                p.setStatus(PaymentStatus.SUCCESS);
                // Payment has landed on the platform - it's now held in escrow, not yet
                // payable to the CA. See EscrowService for the release gate.
                if (p.getEscrowStatus() == null) {
                    p.setEscrowStatus(com.taxin60sec.backend.entity.enums.EscrowStatus.HELD);
                }
            }
        });
    }

    public void applyRefund(PaymentService.Refund refund) {
        if (refund == null || refund.paymentId() == null) return;
        payments.findByProviderPaymentId(refund.paymentId()).ifPresent(p -> {
            p.setStatus(PaymentStatus.REFUNDED);
            p.setEscrowStatus(com.taxin60sec.backend.entity.enums.EscrowStatus.REFUNDED);
        });
    }

    public PageResponse<PaymentDto> listForClient(Long clientId, int page, int size) {
        var result = payments.findByClientIdAndDeletedFalseOrderByCreatedAtDesc(
                clientId, PageRequest.of(Math.max(page, 0), size <= 0 ? 20 : size)
        );
        return new PageResponse<>(
                result.getContent().stream().map(mapper::toDto).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages()
        );
    }

    /** Powers the Case Workspace's "Payments" tab (V1.0 spec) - a single case's payment history, not the client's entire history. */
    public PageResponse<PaymentDto> listForCase(Long caseId, int page, int size) {
        var result = payments.findByRelatedCase_IdAndDeletedFalseOrderByCreatedAtDesc(
                caseId, PageRequest.of(Math.max(page, 0), size <= 0 ? 20 : size)
        );
        return new PageResponse<>(
                result.getContent().stream().map(mapper::toDto).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages()
        );
    }

    private PaymentStatus statusFrom(String providerStatus) {
        if (providerStatus == null) return PaymentStatus.CREATED;
        String s = providerStatus.toLowerCase();
        if (s.contains("fail")) return PaymentStatus.FAILED;
        if (s.contains("refund")) return PaymentStatus.REFUNDED;
        if (s.contains("captur") || s.contains("paid") || s.contains("success")) return PaymentStatus.SUCCESS;
        return PaymentStatus.CREATED;
    }
}