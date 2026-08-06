package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.dto.business.RateCaseExperienceRequest;
import com.taxin60sec.backend.dto.domain.ClientCaseRatingDto;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.ClientCaseRating;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.ClientCaseRatingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Client-facing "how was your experience" rating, shown once a case is completed. This is
 * deliberately separate from CaPerformanceRatingService (admin-only, rates the CA internally
 * for future assignment quality) - this one is the client's own feedback, one per case.
 */
@Service
@Transactional
public class ClientCaseRatingService {

    private final CaseRepository cases;
    private final ClientCaseRatingRepository ratings;

    public ClientCaseRatingService(CaseRepository cases, ClientCaseRatingRepository ratings) {
        this.cases = cases;
        this.ratings = ratings;
    }

    public ClientCaseRatingDto rate(Long caseId, RateCaseExperienceRequest request, User client) {
        Case c = cases.findById(caseId)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Case not found"));

        if (c.getClient() == null || !Objects.equals(c.getClient().getId(), client.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, "Case does not belong to current client");
        }
        if (c.getStatus() != CaseStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "Case is not completed yet");
        }

        ClientCaseRating rating = ratings.findByCaseRef_Id(caseId).orElseGet(ClientCaseRating::new);
        rating.setCaseRef(c);
        rating.setClient(client);
        rating.setScore(request.score());
        rating.setFeedback(request.feedback());

        return toDto(ratings.save(rating));
    }

    public ClientCaseRatingDto getForCase(Long caseId) {
        return ratings.findByCaseRef_Id(caseId)
                .map(this::toDto)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "No rating yet for this case"));
    }

    private ClientCaseRatingDto toDto(ClientCaseRating r) {
        return new ClientCaseRatingDto(
                r.getId(), r.getCaseRef().getId(), r.getCaseRef().getCaseNumber(), r.getScore(), r.getFeedback()
        );
    }
}