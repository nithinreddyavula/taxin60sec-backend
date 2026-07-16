package com.taxin60sec.backend.dto.contact;

import java.time.Instant;

public record ContactResponse(
        Long id,
        String name,
        String email,
        String message,
        Instant createdAt
) {
}
