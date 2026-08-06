package com.taxin60sec.backend.mapper;

import com.taxin60sec.backend.dto.domain.PaymentDto;
import com.taxin60sec.backend.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentDto toDto(Payment p) {
        return new PaymentDto(
                p.getId(),
                p.getProviderPaymentId(),
                p.getProvider(),
                p.getReferenceId(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus().name(),
                p.getRelatedCase() != null ? p.getRelatedCase().getCaseNumber() : null,
                p.getCreatedAt()
        );
    }
}