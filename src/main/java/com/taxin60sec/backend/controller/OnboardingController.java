package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.onboarding.OnboardingOrchestratorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cases/{caseId}/onboarding")
public class OnboardingController {
    private final OnboardingOrchestratorService onboarding;

    @PostMapping("/complete")
    @Operation(summary = "Complete the end-to-end onboarding assessment")
    public ApiResponse<OnboardingOrchestratorService.OnboardingResponse> complete(@PathVariable Long caseId, HttpServletRequest request) {
        return ApiResponse.success("Onboarding assessment completed", onboarding.complete(caseId), request.getRequestURI());
    }

    @GetMapping("/summary")
    @Operation(summary = "Get the persisted onboarding summary")
    public ApiResponse<OnboardingOrchestratorService.OnboardingResponse> summary(@PathVariable Long caseId, HttpServletRequest request) {
        return ApiResponse.success("Onboarding summary", onboarding.summary(caseId), request.getRequestURI());
    }

    @GetMapping("/missing-documents")
    @Operation(summary = "Get uploaded, missing and optional documents")
    public ApiResponse<?> missingDocuments(@PathVariable Long caseId, HttpServletRequest request) {
        return ApiResponse.success("Document checklist", onboarding.missingDocuments(caseId), request.getRequestURI());
    }

    @GetMapping("/pricing")
    @Operation(summary = "Get the calculated onboarding price")
    public ApiResponse<?> pricing(@PathVariable Long caseId, HttpServletRequest request) {
        return ApiResponse.success("Price estimate", onboarding.pricing(caseId), request.getRequestURI());
    }

    @GetMapping("/workflow")
    @Operation(summary = "Get the onboarding workflow status")
    public ApiResponse<OnboardingOrchestratorService.WorkflowStatus> workflow(@PathVariable Long caseId, HttpServletRequest request) {
        return ApiResponse.success("Workflow status", onboarding.workflow(caseId), request.getRequestURI());
    }
}
