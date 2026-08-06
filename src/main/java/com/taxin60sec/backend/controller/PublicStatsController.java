package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.publicintake.PublicDashboardStatsResponse;
import com.taxin60sec.backend.dto.publicintake.ResponseTimeStatsResponse;
import com.taxin60sec.backend.service.impl.PublicDashboardStatsService;
import com.taxin60sec.backend.service.impl.ResponseTimeStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/stats")
public class PublicStatsController {

    private final ResponseTimeStatsService responseTimeStatsService;
    private final PublicDashboardStatsService publicDashboardStatsService;

    public PublicStatsController(ResponseTimeStatsService responseTimeStatsService, PublicDashboardStatsService publicDashboardStatsService) {
        this.responseTimeStatsService = responseTimeStatsService;
        this.publicDashboardStatsService = publicDashboardStatsService;
    }

    @GetMapping("/response-time")
    public ApiResponse<ResponseTimeStatsResponse> responseTime() {
        return ApiResponse.success(
                "Response time stats",
                responseTimeStatsService.currentStats(),
                "/api/v1/public/stats/response-time"
        );
    }

    @GetMapping("/dashboard")
    public ApiResponse<PublicDashboardStatsResponse> dashboard() {
        return ApiResponse.success(
                "Dashboard stats",
                publicDashboardStatsService.currentStats(),
                "/api/v1/public/stats/dashboard"
        );
    }
}