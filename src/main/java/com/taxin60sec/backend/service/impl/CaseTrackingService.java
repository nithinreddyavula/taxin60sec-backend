package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.dto.domain.CaseTrackingLinkDto;
import com.taxin60sec.backend.dto.domain.PublicCaseTrackingResponse;
import com.taxin60sec.backend.dto.domain.TrackingTimelineItemDto;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.TimelineEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class CaseTrackingService {

    private static final long TOKEN_VALIDITY_DAYS = 90;

    private final CaseRepository cases;
    private final TimelineEventRepository timelineEvents;

    public CaseTrackingService(CaseRepository cases, TimelineEventRepository timelineEvents) {
        this.cases = cases;
        this.timelineEvents = timelineEvents;
    }

    public CaseTrackingLinkDto linkFor(Long caseId, User requester) {
        Case c = cases.findById(caseId)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Case not found"));

        if (c.getClient() == null || !Objects.equals(c.getClient().getId(), requester.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, "Case does not belong to current client");
        }

        boolean expired = c.getPublicAccessExpiry() == null || c.getPublicAccessExpiry().isBefore(LocalDateTime.now());
        if (c.getPublicAccessToken() == null || expired) {
            c.setPublicAccessToken(UUID.randomUUID().toString().replace("-", ""));
            c.setPublicAccessExpiry(LocalDateTime.now().plusDays(TOKEN_VALIDITY_DAYS));
        }

        return new CaseTrackingLinkDto(c.getCaseNumber(), c.getPublicAccessToken(), c.getPublicAccessExpiry());
    }

    @Transactional(readOnly = true)
    public PublicCaseTrackingResponse track(String caseNumber, String token) {
        ApiException notFound = new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "No case found for that tracking link");

        Case c = cases.findByCaseNumber(caseNumber).filter(x -> !x.isDeleted()).orElseThrow(() -> notFound);

        boolean expired = c.getPublicAccessExpiry() == null || c.getPublicAccessExpiry().isBefore(LocalDateTime.now());
        if (token == null || c.getPublicAccessToken() == null || !c.getPublicAccessToken().equals(token) || expired) {
            throw notFound;
        }

        var timeline = timelineEvents.findByTaxCaseIdAndDeletedFalseOrderByCreatedAtAsc(c.getId())
                .stream()
                .map(e -> new TrackingTimelineItemDto(e.getTitle(), e.getDescription(), e.getCreatedAt()))
                .toList();

        return new PublicCaseTrackingResponse(
                c.getCaseNumber(),
                c.getTitle(),
                c.getStatus() != null ? c.getStatus().name() : null,
                c.getWorkflowStage() != null ? c.getWorkflowStage().name() : null,
                c.getExpectedCompletionDate(),
                c.getCompletedAt(),
                timeline
        );
    }
}