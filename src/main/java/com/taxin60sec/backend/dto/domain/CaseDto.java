package com.taxin60sec.backend.dto.domain;

import com.taxin60sec.backend.entity.enums.CasePriority;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.entity.enums.WorkflowStage;

import java.time.Instant;
import java.time.LocalDate;

public record CaseDto(
        Long id,
        String caseNumber,
        String title,
        String description,
        CasePriority priority,
        WorkflowStage workflowStage,
        CaseStatus status,
        String remarks,
        String internalNotes,
        LocalDate expectedCompletionDate,
        Instant completedAt,
        Instant assignedAt,
        Long clientId,
        Long assignedCaId,
        Long serviceOfferingId,
        Long lastUpdatedByUserId,
        boolean archived,
        boolean paymentRequired,
        boolean documentVerificationCompleted,
        boolean intakeCompleted,
        String intakeSummary,
        Instant createdAt,
        Instant updatedAt
) {
}
