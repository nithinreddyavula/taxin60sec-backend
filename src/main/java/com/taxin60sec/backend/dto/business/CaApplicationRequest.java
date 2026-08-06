package com.taxin60sec.backend.dto.business;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CaApplicationRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @NotBlank String phoneNumber,
        @NotBlank @Size(min = 8) String password,
        @NotBlank @Size(max = 80) String membershipNumber,
        @NotBlank @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "Enter a valid PAN number") String panNumber,
        @Size(max = 180) String firmName,
        @Size(max = 1000) String specialization
) {}