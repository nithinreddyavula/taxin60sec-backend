package com.taxin60sec.backend.dto.domain;

public record AdminProfileDto(
        Long id,
        Long userId,
        String department,
        String designation
) {
}
