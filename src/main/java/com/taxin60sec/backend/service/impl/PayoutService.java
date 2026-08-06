package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.domain.CaPayoutSummaryDto;
import com.taxin60sec.backend.dto.domain.PayoutLineItemDto;
import com.taxin60sec.backend.entity.Payment;
import com.taxin60sec.backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Commission & payout dashboard (item #7). Reads only - EscrowService is the only writer
 * of the escrow/payout fields this reports on, kept deliberately separate so "who can
 * change a payout" and "who can view a payout" stay two different concerns.
 */
@Service
@Transactional(readOnly = true)
public class PayoutService {

    private final PaymentRepository payments;

    public PayoutService(PaymentRepository payments) {
        this.payments = payments;
    }

    /** Admin platform-wide view: every verified CA who has at least one processed payment, with running totals. */
    public List<CaPayoutSummaryDto> platformSummary() {
        return payments.payoutSummaryByCa();
    }

    /** Line-item breakdown for one CA - used both by the admin drill-down and the CA's own "my earnings" view. */
    public List<PayoutLineItemDto> historyForCa(Long caUserId) {
        return payments.findByRelatedCase_AssignedCa_IdAndEscrowStatusIsNotNullAndDeletedFalseOrderByCreatedAtDesc(caUserId)
                .stream()
                .map(this::toLineItem)
                .toList();
    }

    private PayoutLineItemDto toLineItem(Payment p) {
        return new PayoutLineItemDto(
                p.getId(),
                p.getRelatedCase() != null ? p.getRelatedCase().getCaseNumber() : null,
                p.getAmount(),
                p.getEscrowStatus() != null ? p.getEscrowStatus().name() : null,
                p.getPlatformCommissionAmount(),
                p.getCaPayoutAmount(),
                p.getEscrowReleasedAt(),
                p.getEscrowReleasedBy()
        );
    }
}