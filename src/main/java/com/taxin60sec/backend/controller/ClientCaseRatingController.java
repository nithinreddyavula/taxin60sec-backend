package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.business.RateCaseExperienceRequest;
import com.taxin60sec.backend.dto.domain.ClientCaseRatingDto;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.impl.ClientCaseRatingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cases/{caseId}/rating")
public class ClientCaseRatingController {

    private final ClientCaseRatingService service;

    public ClientCaseRatingController(ClientCaseRatingService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<ClientCaseRatingDto> rate(
            @PathVariable Long caseId,
            @Valid @RequestBody RateCaseExperienceRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("Thanks for the feedback", service.rate(caseId, request, principal.getUser()), null);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')")
    public ApiResponse<ClientCaseRatingDto> get(@PathVariable Long caseId) {
        return ApiResponse.success("Rating", service.getForCase(caseId), null);
    }
}