package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.dto.admin.CaPerformanceRatingDto;
import com.taxin60sec.backend.dto.admin.RateCaseRequest;
import com.taxin60sec.backend.entity.CaPerformanceRating;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.CaPerformanceRatingRepository;
import com.taxin60sec.backend.repository.CaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.OptionalDouble;

@Service
@Transactional
public class CaPerformanceRatingService {

    private final CaPerformanceRatingRepository ratings;
    private final CaseRepository cases;

    public CaPerformanceRatingService(CaPerformanceRatingRepository ratings, CaseRepository cases) {
        this.ratings = ratings;
        this.cases = cases;
    }

    public CaPerformanceRatingDto rate(Long caseId, RateCaseRequest request, User admin) {
        Case c = cases.findById(caseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Case not found"));

        if (c.getAssignedCa() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "Case has no assigned CA to rate");
        }

        CaPerformanceRating rating = ratings.findByCaseRefId(caseId).orElseGet(CaPerformanceRating::new);
        rating.setCaseRef(c);
        rating.setCa(c.getAssignedCa());
        rating.setCompletionDays(completionDays(c));
        rating.setClientSatisfactionScore(request.clientSatisfactionScore());
        rating.setQualityNotes(request.qualityNotes());
        rating.setRatedBy(admin);
        rating.setRatedAt(Instant.now());

        return toDto(ratings.save(rating));
    }

    public List<CaPerformanceRatingDto> forCa(Long caId) {
        return ratings.findByCaIdOrderByRatedAtDesc(caId).stream().map(this::toDto).toList();
    }

    public Double averageScoreForCa(Long caId) {
        OptionalDouble avg = ratings.findByCaIdOrderByRatedAtDesc(caId).stream()
                .map(CaPerformanceRating::getClientSatisfactionScore)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();
        return avg.isPresent() ? Math.round(avg.getAsDouble() * 10.0) / 10.0 : null;
    }

    private Integer completionDays(Case c) {
        if (c.getAssignedAt() == null || c.getCompletedAt() == null) return null;
        return (int) Duration.between(c.getAssignedAt(), c.getCompletedAt()).toDays();
    }

    private CaPerformanceRatingDto toDto(CaPerformanceRating r) {
        return new CaPerformanceRatingDto(
                r.getId(),
                r.getCaseRef().getId(),
                r.getCaseRef().getCaseNumber(),
                r.getCa().getId(),
                r.getCa().getFullName(),
                r.getCompletionDays(),
                r.getClientSatisfactionScore(),
                r.getQualityNotes(),
                r.getRatedBy() == null ? null : r.getRatedBy().getFullName(),
                r.getRatedAt()
        );
    }
}