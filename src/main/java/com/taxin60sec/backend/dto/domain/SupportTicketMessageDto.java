package com.taxin60sec.backend.dto.domain;

import java.time.Instant;

public record SupportTicketMessageDto(
        Long id,
        Long ticketId,
        Long senderId,
        String senderName,
        String body,
        Instant createdAt
) {}