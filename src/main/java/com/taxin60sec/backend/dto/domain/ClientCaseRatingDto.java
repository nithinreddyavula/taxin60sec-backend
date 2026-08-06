package com.taxin60sec.backend.dto.domain;

public record ClientCaseRatingDto(
        Long id,
        Long caseId,
        String caseNumber,
        Integer score,
        String feedback
) {}