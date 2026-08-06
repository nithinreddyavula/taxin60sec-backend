package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.domain.CaseTrackingLinkDto;
import com.taxin60sec.backend.dto.domain.PublicCaseTrackingResponse;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.impl.CaseTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CaseTrackingController {

    private final CaseTrackingService trackingService;

    public CaseTrackingController(CaseTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping("/api/v1/cases/{caseId}/tracking-link")
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<CaseTrackingLinkDto> trackingLink(
            @PathVariable Long caseId,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request
    ) {
        return ApiResponse.success("Tracking link", trackingService.linkFor(caseId, principal.getUser()), request.getRequestURI());
    }

    @GetMapping("/api/v1/public/track")
    public ApiResponse<PublicCaseTrackingResponse> track(
            @RequestParam String caseNumber,
            @RequestParam String token,
            HttpServletRequest request
    ) {
        return ApiResponse.success("Case status", trackingService.track(caseNumber, token), request.getRequestURI());
    }
}