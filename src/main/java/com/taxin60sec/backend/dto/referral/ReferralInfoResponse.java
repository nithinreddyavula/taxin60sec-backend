package com.taxin60sec.backend.dto.referral;

public record ReferralInfoResponse(
        String referralCode,
        String referralShareUrl,
        long referredCount
) {}