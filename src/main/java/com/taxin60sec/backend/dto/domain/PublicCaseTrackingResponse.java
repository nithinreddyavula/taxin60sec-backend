package com.taxin60sec.backend.dto.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PublicCaseTrackingResponse(
        String caseNumber,
        String title,
        String status,
        String workflowStage,
        LocalDate expectedCompletionDate,
        Instant completedAt,
        List<TrackingTimelineItemDto> timeline
) {}