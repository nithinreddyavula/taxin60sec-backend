package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.dto.business.CaseIntakeResponse;
import com.taxin60sec.backend.dto.business.IntakeRequests;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.CaseIntakeService;
import com.taxin60sec.backend.service.BusinessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/intake")
public class CaseIntakeController {
    private final CaseIntakeService intake;
    private final BusinessService business;
    public CaseIntakeController(CaseIntakeService intake, BusinessService business) { this.intake = intake; this.business = business; }

    @PostMapping("/cases") @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<CaseIntakeResponse> start(@Valid @RequestBody IntakeRequests.Start request, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("Intake ready", intake.startOrResume(request, principal.getUser()), null);
    }

    @PostMapping("/cases/{caseId}/answers") @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<CaseIntakeResponse> answers(@PathVariable Long caseId, @Valid @RequestBody IntakeRequests.Answers request, @AuthenticationPrincipal UserPrincipal principal) {
        CaseIntakeResponse response = intake.review(caseId);
        if (!response.taxCase().clientId().equals(principal.getId())) throw new ApiException(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, "Case does not belong to current client");
        return ApiResponse.success("Intake updated", intake.recordAnswers(caseId, request, principal.getUser()), null);
    }

    @GetMapping("/cases/{caseId}") @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')")
    public ApiResponse<CaseIntakeResponse> review(@PathVariable Long caseId, @AuthenticationPrincipal UserPrincipal principal) {
        CaseIntakeResponse response = intake.review(caseId);
        boolean admin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean client = response.taxCase().clientId().equals(principal.getId());
        if (!admin && !client && !business.assignedTo(caseId, principal.getUser())) throw new ApiException(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, "Case is not assigned to current user");
        return ApiResponse.success("Intake retrieved", response, null);
    }
}
