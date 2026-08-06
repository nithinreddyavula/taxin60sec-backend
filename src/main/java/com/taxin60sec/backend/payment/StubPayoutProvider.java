package com.taxin60sec.backend.payment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "payout.provider.razorpayx.enabled", havingValue = "false", matchIfMissing = true)
public class StubPayoutProvider implements PayoutProvider {

    @Override
    public String name() {
        return "stub";
    }

    @Override
    public PayoutResult payout(PayoutRequest request) {
        return new PayoutResult(
                "PENDING_PROVIDER_SETUP",
                null,
                "RazorpayX payouts are not configured yet - set payout.provider.razorpayx.enabled and credentials to send this automatically.",
                null,
                null
        );
    }
}