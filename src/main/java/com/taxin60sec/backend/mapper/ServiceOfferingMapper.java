package com.taxin60sec.backend.mapper;

import com.taxin60sec.backend.dto.domain.ServiceOfferingDto;
import com.taxin60sec.backend.entity.ServiceOffering;
import org.springframework.stereotype.Component;

@Component
public class ServiceOfferingMapper {
    public ServiceOfferingDto toDto(ServiceOffering serviceOffering) {
        return new ServiceOfferingDto(
                serviceOffering.getId(),
                serviceOffering.getCode(),
                serviceOffering.getDisplayName(),
                serviceOffering.getDescription(),
                serviceOffering.getCategory(),
                serviceOffering.getEstimatedCompletionDays(),
                serviceOffering.getBasePrice(),
                serviceOffering.getMinimumPrice(),
                serviceOffering.getMaximumPrice(),
                serviceOffering.isActive(),
                serviceOffering.isFeatured(),
                serviceOffering.getDisplayOrder(),
                serviceOffering.getIcon(),
                serviceOffering.getColor(),
                serviceOffering.isRequiresPaymentFirst(),
                serviceOffering.isRequiresDocumentVerification(),
                serviceOffering.getIntakeQuestions() == null || serviceOffering.getIntakeQuestions().isBlank() ? java.util.List.of() : java.util.Arrays.stream(serviceOffering.getIntakeQuestions().split("\\r?\\n")).filter(q -> !q.isBlank()).toList(),
                serviceOffering.getCreatedBy() == null ? null : serviceOffering.getCreatedBy().getId(),
                serviceOffering.getUpdatedBy() == null ? null : serviceOffering.getUpdatedBy().getId()
        );
    }
}
