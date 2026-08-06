package com.taxin60sec.backend.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RateCaseRequest(
        @NotNull @Min(1) @Max(5) Integer clientSatisfactionScore,
        @Size(max = 2000) String qualityNotes
) {}