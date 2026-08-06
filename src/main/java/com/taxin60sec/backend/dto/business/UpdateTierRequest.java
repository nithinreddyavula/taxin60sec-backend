package com.taxin60sec.backend.dto.business;

import com.taxin60sec.backend.entity.enums.ClientTier;
import jakarta.validation.constraints.NotNull;

public record UpdateTierRequest(
        @NotNull ClientTier tier
) {}