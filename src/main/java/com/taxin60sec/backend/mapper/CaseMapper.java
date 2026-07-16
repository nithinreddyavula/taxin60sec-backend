package com.taxin60sec.backend.mapper;

import com.taxin60sec.backend.dto.domain.CaseDto;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.ServiceOffering;
import com.taxin60sec.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CaseMapper {
    public CaseDto toDto(Case taxCase) {
        return new CaseDto(
                taxCase.getId(),
                taxCase.getCaseNumber(),
                taxCase.getTitle(),
                taxCase.getDescription(),
                taxCase.getPriority(),
                taxCase.getWorkflowStage(),
                taxCase.getStatus(),
                taxCase.getRemarks(),
                taxCase.getInternalNotes(),
                taxCase.getExpectedCompletionDate(),
                taxCase.getCompletedAt(),
                taxCase.getAssignedAt(),
                idOf(taxCase.getClient()),
                idOf(taxCase.getAssignedCa()),
                idOf(taxCase.getServiceOffering()),
                idOf(taxCase.getLastUpdatedBy()),
                taxCase.isArchived(),
                taxCase.isPaymentRequired(),
                taxCase.isDocumentVerificationCompleted(),
                taxCase.getCreatedAt(),
                taxCase.getUpdatedAt()
        );
    }

    private Long idOf(User user) {
        return user == null ? null : user.getId();
    }

    private Long idOf(ServiceOffering serviceOffering) {
        return serviceOffering == null ? null : serviceOffering.getId();
    }
}
