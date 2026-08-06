package com.taxin60sec.backend.dto.admin;

import java.time.Instant;

public record AdminClientSummaryResponse(
        Long id,
        String fullName,
        String email,
        String panNumber,
        String status,
        Instant joinedOn,
        long totalCases
) {}