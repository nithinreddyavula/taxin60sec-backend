package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.repatriation.RepatriationRecordRequest;
import com.taxin60sec.backend.dto.repatriation.RepatriationSummaryResponse;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.impl.RepatriationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nri/repatriation")
public class RepatriationController {

    private final RepatriationService repatriationService;

    public RepatriationController(RepatriationService repatriationService) {
        this.repatriationService = repatriationService;
    }

    @GetMapping
    public ApiResponse<RepatriationSummaryResponse> mySummary(@AuthenticationPrincipal UserPrincipal principal) {

        return ApiResponse.success(
                "Repatriation summary",
                repatriationService.summaryFor(principal.getId()),
                null
        );
    }

    @PostMapping
    public ApiResponse<RepatriationSummaryResponse> addRecord(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody RepatriationRecordRequest request
    ) {

        return ApiResponse.success(
                "Record added",
                repatriationService.addRecord(principal.getId(), request),
                null
        );
    }
}