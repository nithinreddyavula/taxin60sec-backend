package com.taxin60sec.backend.dto.domain;

public record CAProfileDto(
        Long id,
        Long userId,
        String membershipNumber,
        String firmName,
        String specialization,
        boolean verified
) {
}
