package com.taxin60sec.backend.dto.domain;

import java.time.Instant;

public record CaseMessageDto(
        Long id,
        Long caseId,
        Long senderId,
        String senderName,
        String senderRole,
        String content,
        Instant createdAt,
        boolean read
) {}