package com.taxin60sec.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Email @Size(max = 180) String email,
        @Size(max = 30) String phoneNumber,
        @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
                message = "must contain uppercase, lowercase, number, and special character"
        )
        String password
) {
}
