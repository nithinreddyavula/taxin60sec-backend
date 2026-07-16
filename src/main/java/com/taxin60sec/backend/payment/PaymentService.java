package com.taxin60sec.backend.payment;

import java.math.BigDecimal;

public interface PaymentService {
    String createPaymentOrder(BigDecimal amount, String currency, String referenceId);

    boolean verifyPayment(String externalPaymentId, String signature);
}
