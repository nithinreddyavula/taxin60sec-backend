package com.taxin60sec.backend.dto.publicintake;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PublicStartRequest(

        @NotNull
        Long serviceOfferingId,

        @NotBlank
        String fullName,

        @Email
        @NotBlank
        String email,

        @NotBlank
        String phoneNumber

) {
}