package com.taxin60sec.backend.dto.domain;

import java.time.Instant;

public record NoticeDto(
        Long id,
        String type,
        String severity,
        String title,
        String message,
        String caseNumber,
        boolean read,
        Instant createdAt
) {}