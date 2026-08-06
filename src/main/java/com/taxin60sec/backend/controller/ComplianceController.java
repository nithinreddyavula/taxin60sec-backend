package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.compliance.ComplianceScoreResponse;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.impl.ComplianceScoreService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/compliance")
public class ComplianceController {

    private final ComplianceScoreService complianceScoreService;

    public ComplianceController(ComplianceScoreService complianceScoreService) {
        this.complianceScoreService = complianceScoreService;
    }

    @GetMapping("/score")
    @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')")
    public ApiResponse<ComplianceScoreResponse> myScore(@AuthenticationPrincipal UserPrincipal principal) {

        ComplianceScoreResponse response = complianceScoreService.scoreFor(principal.getId());

        return ApiResponse.success("Tax health score", response, null);
    }
}