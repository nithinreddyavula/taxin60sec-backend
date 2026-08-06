package com.taxin60sec.backend.dto.domain;

import java.time.Instant;

public record SupportTicketDto(
        Long id,
        Long raisedById,
        String raisedByName,
        Long caseId,
        String caseNumber,
        String subject,
        String status,
        Instant createdAt
) {}