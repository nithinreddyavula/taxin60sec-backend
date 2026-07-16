package com.taxin60sec.backend.dto.auth;

import com.taxin60sec.backend.dto.domain.UserDto;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant expiresAt,
        UserDto user
) {
}
