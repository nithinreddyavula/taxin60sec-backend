package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.admin.CaPerformanceRatingDto;
import com.taxin60sec.backend.dto.admin.RateCaseRequest;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.impl.CaPerformanceRatingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class CaPerformanceController {

    private final CaPerformanceRatingService ratingService;

    public CaPerformanceController(CaPerformanceRatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/cases/{caseId}/rate")
    public ApiResponse<CaPerformanceRatingDto> rate(
            @PathVariable Long caseId,
            @Valid @RequestBody RateCaseRequest body,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("Case rated", ratingService.rate(caseId, body, principal.getUser()), null);
    }

    @GetMapping("/cas/{caId}/ratings")
    public ApiResponse<List<CaPerformanceRatingDto>> forCa(@PathVariable Long caId) {
        return ApiResponse.success("CA ratings", ratingService.forCa(caId), null);
    }

    @GetMapping("/cas/{caId}/ratings/average")
    public ApiResponse<Double> averageForCa(@PathVariable Long caId) {
        return ApiResponse.success("Average score", ratingService.averageScoreForCa(caId), null);
    }
}