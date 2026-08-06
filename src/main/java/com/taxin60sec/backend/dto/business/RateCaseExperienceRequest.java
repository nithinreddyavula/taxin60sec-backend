package com.taxin60sec.backend.dto.business;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RateCaseExperienceRequest(
        @NotNull @Min(1) @Max(5) Integer score,
        @Size(max = 2000) String feedback
) {}