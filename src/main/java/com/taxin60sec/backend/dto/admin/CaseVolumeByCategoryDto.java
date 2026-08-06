package com.taxin60sec.backend.dto.admin;

import com.taxin60sec.backend.entity.enums.ServiceCategory;

public record CaseVolumeByCategoryDto(
        ServiceCategory category,
        long caseCount
) {
}