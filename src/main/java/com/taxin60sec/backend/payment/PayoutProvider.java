package com.taxin60sec.backend.payment;

import com.taxin60sec.backend.entity.enums.PayoutMethod;

import java.math.BigDecimal;

public interface PayoutProvider {

    String name();

    PayoutResult payout(PayoutRequest request);

    record PayoutRequest(
            String reference,
            BigDecimal amount,
            String currency,
            String accountHolderName,
            String email,
            String phone,
            PayoutMethod method,
            String bankAccountNumber,
            String bankIfsc,
            String upiId,
            String existingContactId,
            String existingFundAccountId
    ) {}

    record PayoutResult(
            String providerStatus,
            String providerReference,
            String failureReason,
            String contactId,
            String fundAccountId
    ) {}
}