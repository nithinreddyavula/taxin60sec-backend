package com.taxin60sec.backend.dto.admin;

public record AssignableCaResponse(
        Long id,
        String fullName,
        String email,
        long activeCaseload
) {}