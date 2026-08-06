package com.taxin60sec.backend.dto.business;

import com.taxin60sec.backend.entity.enums.BusinessStatus;
import com.taxin60sec.backend.entity.enums.BusinessType;

import java.time.LocalDate;
import java.util.List;

public record CommandCenterResponse(
        int totalEntities,
        List<EntitySummary> entities
) {
    public record EntitySummary(
            Long id,
            String businessName,
            BusinessType businessType,
            BusinessStatus businessStatus,
            String panNumber,
            String gstin,
            LocalDate incorporationDate,
            Long assignedCaId,
            String assignedCaName
    ) {}
}