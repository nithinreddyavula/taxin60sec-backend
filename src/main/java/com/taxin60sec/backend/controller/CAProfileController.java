package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.business.CaApplicationRequest;
import com.taxin60sec.backend.dto.business.PayoutDestinationRequest;
import com.taxin60sec.backend.dto.domain.CAProfileDto;
import com.taxin60sec.backend.dto.domain.CaDashboardResponse;
import com.taxin60sec.backend.entity.enums.BackgroundCheckStatus;
import com.taxin60sec.backend.entity.enums.CAAvailability;
import com.taxin60sec.backend.entity.enums.CATier;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.CAProfileService;
import com.taxin60sec.backend.service.impl.CaDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ca")
public class CAProfileController {

    private final CAProfileService caProfileService;
    private final CaDashboardService caDashboardService;

    public CAProfileController(CAProfileService caProfileService, CaDashboardService caDashboardService) {
        this.caProfileService = caProfileService;
        this.caDashboardService = caDashboardService;
    }

    @PostMapping("/apply")
    public ApiResponse<CAProfileDto> apply(@Valid @RequestBody CaApplicationRequest body, HttpServletRequest request) {
        return ApiResponse.success("Application submitted - pending verification", caProfileService.apply(body), request.getRequestURI());
    }

    /** CA Portal → Dashboard (V1.0 spec, first item) - profile, case counts, and earnings in one call. */
    @GetMapping("/me/dashboard")
    @PreAuthorize("hasRole('CA')")
    public ApiResponse<CaDashboardResponse> myDashboard(@AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request) {
        return ApiResponse.success("CA dashboard", caDashboardService.forUser(principal.getId()), request.getRequestURI());
    }

    @GetMapping("/me/profile")
    @PreAuthorize("hasAnyRole('CA','ADMIN')")
    public ApiResponse<CAProfileDto> myProfile(@AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request) {
        return ApiResponse.success("CA profile", caProfileService.myProfile(principal.getId()), request.getRequestURI());
    }

    @PostMapping(value = "/me/documents", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('CA')")
    public ApiResponse<CAProfileDto> uploadDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        return ApiResponse.success("Document uploaded", caProfileService.uploadDocument(principal.getId(), documentType, file), request.getRequestURI());
    }

    @PostMapping("/me/agreement/accept")
    @PreAuthorize("hasRole('CA')")
    public ApiResponse<CAProfileDto> acceptAgreement(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        String version = body.getOrDefault("agreementVersion", "v1");
        return ApiResponse.success("Partner agreement accepted", caProfileService.acceptAgreement(principal.getId(), version), request.getRequestURI());
    }

    @PatchMapping("/me/availability")
    @PreAuthorize("hasRole('CA')")
    public ApiResponse<CAProfileDto> setAvailability(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body,
            HttpServletRequest request
    ) {
        CAAvailability availability = CAAvailability.valueOf(body.get("availability").toUpperCase());
        return ApiResponse.success("Availability updated", caProfileService.setAvailability(principal.getId(), availability), request.getRequestURI());
    }

    @PutMapping("/me/payout-destination")
    @PreAuthorize("hasRole('CA')")
    public ApiResponse<CAProfileDto> setPayoutDestination(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PayoutDestinationRequest body,
            HttpServletRequest request
    ) {
        return ApiResponse.success("Payout destination saved", caProfileService.setPayoutDestination(principal.getId(), body), request.getRequestURI());
    }

    @GetMapping("/applications/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CAProfileDto>> pending(HttpServletRequest request) {
        return ApiResponse.success("Pending applications", caProfileService.pendingApplications(), request.getRequestURI());
    }

    @GetMapping("/verified")
    @PreAuthorize("hasAnyRole('ADMIN','CA')")
    public ApiResponse<List<CAProfileDto>> verified(HttpServletRequest request) {
        return ApiResponse.success("Verified CAs", caProfileService.verifiedCAs(), request.getRequestURI());
    }

    @PostMapping("/applications/{profileId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CAProfileDto> verify(@PathVariable Long profileId, HttpServletRequest request) {
        return ApiResponse.success("CA verified", caProfileService.verify(profileId), request.getRequestURI());
    }

    @PostMapping("/applications/{profileId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> reject(@PathVariable Long profileId, HttpServletRequest request) {
        caProfileService.reject(profileId);
        return ApiResponse.success("Application rejected", null, request.getRequestURI());
    }

    @PatchMapping("/applications/{profileId}/tier")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CAProfileDto> setTier(@PathVariable Long profileId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        CATier tier = CATier.valueOf(body.get("tier").toUpperCase());
        return ApiResponse.success("Tier updated", caProfileService.setTier(profileId, tier), request.getRequestURI());
    }

    @PatchMapping("/applications/{profileId}/background-check")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CAProfileDto> setBackgroundCheckStatus(@PathVariable Long profileId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        BackgroundCheckStatus status = BackgroundCheckStatus.valueOf(body.get("status").toUpperCase());
        return ApiResponse.success("Background check status updated", caProfileService.setBackgroundCheckStatus(profileId, status), request.getRequestURI());
    }
}