package com.taxin60sec.backend.dto.domain;

import java.time.Instant;
import java.util.Set;

public record UserDto(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        boolean active,
        Set<String> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
