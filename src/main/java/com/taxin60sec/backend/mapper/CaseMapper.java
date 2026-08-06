package com.taxin60sec.backend.mapper;

import com.taxin60sec.backend.dto.domain.CaseDto;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.ServiceOffering;
import com.taxin60sec.backend.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CaseMapper {
    private static final long SLA_SECONDS = 60;

    public CaseDto toDto(Case taxCase) {
    return toDto(taxCase, false, false);
}

public CaseDto toDto(Case taxCase, boolean maskClientContact) {
    return toDto(taxCase, maskClientContact, false);
}

/**
 * @param maskClientContact when true, the client's email/phone are omitted from the response.
 *        Used for a CA's own case queue - a CA needs to know WHO they're serving (name stays),
 *        but never gets a direct channel to contact them outside Tax60.
 * @param maskInternalNotes when true, staff-only internal notes are omitted - used for the client's own view.
 */
public CaseDto toDto(Case taxCase, boolean maskClientContact, boolean maskInternalNotes) {
    Instant firstResponseAt = taxCase.getFirstResponseAt();
    Long responseSeconds = firstResponseAt == null ? null
            : java.time.Duration.between(taxCase.getCreatedAt(), firstResponseAt).getSeconds();
    boolean slaMet = responseSeconds != null && responseSeconds <= SLA_SECONDS;

    return new CaseDto(
            taxCase.getId(), taxCase.getCaseNumber(), taxCase.getTitle(), taxCase.getDescription(),
            taxCase.getPriority(), taxCase.getWorkflowStage(), taxCase.getStatus(), taxCase.getRemarks(),
            maskInternalNotes ? null : taxCase.getInternalNotes(), taxCase.getExpectedCompletionDate(), taxCase.getCompletedAt(),
            taxCase.getAssignedAt(), idOf(taxCase.getClient()),
            taxCase.getClient() == null ? null : taxCase.getClient().getFullName(),
            maskClientContact || taxCase.getClient() == null ? null : taxCase.getClient().getEmail(),
            maskClientContact || taxCase.getClient() == null ? null : taxCase.getClient().getPhoneNumber(),
            idOf(taxCase.getAssignedCa()),
            taxCase.getAssignedCa() == null ? null : taxCase.getAssignedCa().getFullName(),
            idOf(taxCase.getServiceOffering()), idOf(taxCase.getLastUpdatedBy()), taxCase.isArchived(),
            taxCase.isPaymentRequired(), taxCase.isDocumentVerificationCompleted(), taxCase.isIntakeCompleted(),
            taxCase.getIntakeSummary(), firstResponseAt, responseSeconds, slaMet,
            taxCase.getCreatedAt(), taxCase.getUpdatedAt()
    );
}

    private Long idOf(User user) {
        return user == null ? null : user.getId();
    }

    private Long idOf(ServiceOffering serviceOffering) {
        return serviceOffering == null ? null : serviceOffering.getId();
    }
}