package com.taxin60sec.backend.dto.domain;

import java.time.Instant;

public record TimelineEventDto(
        Long id,
        String eventType,
        String title,
        String description,
        Long caseId,
        Long actorUserId,
        Instant createdAt
) {
}
