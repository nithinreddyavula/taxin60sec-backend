package com.taxin60sec.backend.dto.domain;

public record ClientProfileDto(
        Long id,
        Long userId,
        String businessName,
        String panNumber,
        String gstin,
        String address
) {
}
