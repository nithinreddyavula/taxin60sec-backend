package com.taxin60sec.backend.dto.admin;

import java.time.Instant;

public record CaPerformanceRatingDto(
        Long id,
        Long caseId,
        String caseNumber,
        Long caId,
        String caName,
        Integer completionDays,
        Integer clientSatisfactionScore,
        String qualityNotes,
        String ratedByName,
        Instant ratedAt
) {}