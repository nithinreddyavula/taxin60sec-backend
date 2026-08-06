package com.taxin60sec.backend.dto.domain;

import java.time.Instant;

public record CallSessionDto(
        Long id,
        Long caseId,
        Long requestedById,
        String requestedByName,
        String status,
        String maskedNumber,
        String provider,
        Instant createdAt,
        Instant connectedAt,
        Instant endedAt,
        Integer durationSeconds
) {}