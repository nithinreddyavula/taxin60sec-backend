package com.taxin60sec.backend.dto.business;

import com.taxin60sec.backend.entity.enums.CasePriority;
import com.taxin60sec.backend.entity.enums.ClientTier;

import java.util.List;

public record ClientTierResponse(
        ClientTier tier,
        String label,
        List<String> perks,
        CasePriority defaultCasePriority
) {}