package com.taxin60sec.backend.dto.domain;

import com.taxin60sec.backend.entity.enums.ServiceCategory;

import java.math.BigDecimal;

public record ServiceOfferingDto(
        Long id,
        String code,
        String displayName,
        String description,
        ServiceCategory category,
        Integer estimatedCompletionDays,
        BigDecimal basePrice,
        BigDecimal minimumPrice,
        BigDecimal maximumPrice,
        boolean active,
        boolean featured,
        int displayOrder,
        String icon,
        String color,
        boolean requiresPaymentFirst,
        boolean requiresDocumentVerification,
        Long createdByUserId,
        Long updatedByUserId
) {
}
