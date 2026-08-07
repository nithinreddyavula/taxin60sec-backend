package com.taxin60sec.backend.dto.admin;

import java.time.Instant;
import java.util.List;

public record AdminClientDetailResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        String status,
        Instant joinedOn,
        String businessName,
        String panNumber,
        String gstin,
        String address,
        String tier,
        String referralCode,
        String referredByCode,
        int referralCredits,
        long totalCases,
        List<AdminClientCaseSummary> cases
) {}