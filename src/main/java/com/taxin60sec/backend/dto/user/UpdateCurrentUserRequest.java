package com.taxin60sec.backend.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateCurrentUserRequest(
        @Size(max = 160) String fullName,
        @Size(max = 30) String phoneNumber
) {
}
